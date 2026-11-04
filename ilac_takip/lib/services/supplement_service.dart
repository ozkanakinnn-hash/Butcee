import '../data/app_database.dart';
import '../data/supplement_catalog.dart';
import '../domain/adherence.dart';
import '../domain/dose_rule.dart';
import '../domain/gs1_parser.dart';
import '../domain/models.dart';
import 'alarm_planner.dart';
import 'settings_store.dart';

/// Takviye listesi + planı (UI ve PDF için tek görünüm).
class SupplementPlan {
  const SupplementPlan(this.medicine, this.schedule);
  final Medicine medicine;
  final Schedule? schedule;

  SupplementInfo? get info => catalogById(medicine.catalogId);
  DoseRule? get rule => schedule?.rule;

  /// Planın saatleri (dilim gruplama ve çakışma kontrolü için).
  List<TimeOfDayHM> get times => switch (rule) {
        FixedTimesRule(:final times) => times,
        CyclicRule(:final inner) => inner.times,
        IntervalRule(:final firstDose, :final intervalHours) => [
            for (var h = firstDose.h; h < firstDose.h + 24; h += intervalHours) (h: h % 24, m: firstDose.m),
          ],
        _ => const [],
      };

  bool get isAsNeeded => rule is AsNeededRule;
}

/// Takviye ekleme, hazır setler, dilim saatleri ve çakışma kontrolü.
class SupplementService {
  SupplementService({required this.db, required this.planner, required this.clock});

  final AppDatabase db;
  final AlarmPlanner planner;
  final Clock clock;

  static DoseRule ruleFromSlots({
    required List<String> slots,
    required AppSettings settings,
    required DateTime start,
    double quantity = 1,
    MealRelation meal = MealRelation.none,
    int everyNDays = 1,
    Set<int>? weekdays,
  }) =>
      FixedTimesRule(
        startDate: start,
        quantity: quantity,
        meal: meal,
        times: [for (final s in slots) parseHm(settings.slotTime(s))],
        everyNDays: everyNDays,
        weekdays: weekdays,
      );

  Future<int> addSupplement({
    required Medicine medicine,
    required DoseRule rule,
    List<String> slots = const [],
    double initialStock = 0,
    ScannedDrugCode? scanned,
    bool persistent = false,
  }) async {
    final id = await db.insertMedicine(medicine.copyWith(totalStock: 0));
    if (scanned != null) {
      await db.addScannedPack(id, scanned, initialStock, clock());
    } else if (initialStock > 0) {
      await db.adjustStock(id, initialStock);
    }
    await db.upsertSchedule(Schedule(
      id: 'sch_${clock().microsecondsSinceEpoch}_$id',
      medicineId: id,
      rule: rule,
      slots: slots,
      isCritical: medicine.isCritical,
      persistent: PersistentAlarm(enabled: persistent),
      graceMinutes: medicine.isCritical ? 30 : 60,
    ));
    await planner.reconcile();
    return id;
  }

  /// Hazır seti ekler; kullanıcıda zaten olan takviyeler atlanır. Eklenen sayıyı döner.
  Future<int> applyPreset(SupplementPreset preset, AppSettings settings, String locale) async {
    final existing = (await db.medicines()).map((m) => m.catalogId).whereType<String>().toSet();
    var added = 0;
    for (final item in preset.items) {
      if (existing.contains(item.catalogId)) continue;
      final info = catalogById(item.catalogId);
      if (info == null) continue;
      await addSupplement(
        medicine: Medicine(
          name: info.name.of(locale),
          category: info.category,
          catalogId: info.id,
          doseAmount: info.defaultAmount,
          doseUnit: info.unit,
          form: info.unit == 'capsule' ? 'capsule' : 'tablet',
          criticalStockLevel: 5,
        ),
        rule: ruleFromSlots(slots: [item.slot], settings: settings, start: dateOnly(clock()), meal: info.meal),
        slots: [item.slot],
      );
      existing.add(item.catalogId);
      added++;
    }
    return added;
  }

  /// Dilim saati değişince o dilimi kullanan tüm planları yeniden yazar ve alarmları yeniden kurar.
  Future<void> applySlotTimes(AppSettings settings) async {
    for (final s in await db.allSchedules()) {
      if (s.slots.isEmpty) continue;
      final newTimes = [for (final slot in s.slots) parseHm(settings.slotTime(slot))];
      final DoseRule? rule = switch (s.rule) {
        FixedTimesRule r => FixedTimesRule(
            startDate: r.startDate, endDate: r.endDate, quantity: r.quantity, meal: r.meal,
            times: newTimes, weekdays: r.weekdays, everyNDays: r.everyNDays),
        CyclicRule r => CyclicRule(
            startDate: r.startDate, endDate: r.endDate, quantity: r.quantity, meal: r.meal,
            onDays: r.onDays, offDays: r.offDays,
            inner: FixedTimesRule(
                startDate: r.inner.startDate, quantity: r.inner.quantity, meal: r.inner.meal,
                times: newTimes, weekdays: r.inner.weekdays, everyNDays: r.inner.everyNDays)),
        _ => null,
      };
      if (rule == null) continue;
      if (DoseRule.encode(rule) == DoseRule.encode(s.rule)) continue;
      await db.upsertSchedule(s.copyWith(rule: rule));
      await planner.rebuildSchedule(s.id);
    }
  }

  Future<List<SupplementPlan>> plans() async {
    final schedules = await db.allSchedules();
    return [
      for (final m in await db.medicines())
        SupplementPlan(m, schedules.where((s) => s.medicineId == m.id).firstOrNull),
    ];
  }

  Future<List<Conflict>> conflicts() async => findConflicts([
        for (final p in await plans())
          if (p.schedule != null && !p.schedule!.paused && !p.isAsNeeded)
            PlannedItem(
              medicineId: p.medicine.id!,
              catalogId: p.medicine.catalogId,
              name: p.medicine.displayName,
              hours: p.times.map((t) => t.h).toSet(),
            ),
      ]);

  /// Takviye başına son [days] günlük uyum.
  Future<Map<int, AdherenceStats>> perMedicineAdherence({int days = 30}) async {
    final now = clock();
    final from = DateTime(now.year, now.month, now.day - days + 1);
    final logs = await db.dosesBetween(from, now.add(const Duration(days: 1)));
    final byMed = <int, List<DoseLog>>{};
    for (final l in logs) {
      byMed.putIfAbsent(l.medicineId, () => []).add(l);
    }
    return {
      for (final e in byMed.entries)
        e.key: computeAdherence(e.value, from: from, to: now.add(const Duration(days: 1)), now: now),
    };
  }
}

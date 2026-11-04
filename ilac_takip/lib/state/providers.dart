import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../bootstrap.dart';
import '../data/supplement_catalog.dart';
import '../services/supplement_service.dart';
import '../domain/adherence.dart';
import '../domain/dose_rule.dart';
import '../domain/models.dart';
import '../services/settings_store.dart';

/// `main()` içinde `overrideWithValue` ile verilir.
final servicesProvider = Provider<AppServices>((ref) => throw UnimplementedError('servicesProvider'));

final settingsProvider = NotifierProvider<SettingsNotifier, AppSettings>(SettingsNotifier.new);

class SettingsNotifier extends Notifier<AppSettings> {
  @override
  AppSettings build() => ref.read(servicesProvider).settings.read();

  Future<void> update(AppSettings Function(AppSettings) change) async {
    final before = state;
    state = change(state);
    final services = ref.read(servicesProvider);
    await services.settings.write(state);
    if (before.languageCode != state.languageCode) {
      await services.applyLanguage(state.languageCode);
    }
  }
}

/// Veritabanı her değiştiğinde ve her dakika başında tetiklenir
/// (bekleyen dozların "zamanı geldi" durumuna geçmesi için).
final dbTickProvider = StreamProvider<int>((ref) {
  final db = ref.watch(servicesProvider).db;
  final controller = StreamController<int>();
  var tick = 0;
  controller.add(tick);
  final sub = db.changes.listen((_) => controller.add(++tick));
  final timer = Timer.periodic(const Duration(minutes: 1), (_) => controller.add(++tick));
  ref.onDispose(() {
    sub.cancel();
    timer.cancel();
    controller.close();
  });
  return controller.stream;
});

class DoseView {
  const DoseView(this.dose, this.medicine, this.schedule);
  final DoseLog dose;
  final Medicine medicine;
  final Schedule? schedule;

  bool get canSnooze => dose.snoozeCount < (schedule?.maxSnoozes ?? 3);
  int get maxSnoozes => schedule?.maxSnoozes ?? 3;
}

Future<List<DoseView>> _views(AppServices s, List<DoseLog> doses) async {
  final meds = {for (final m in await s.db.medicines()) m.id: m};
  final out = <DoseView>[];
  for (final d in doses) {
    final m = meds[d.medicineId];
    if (m == null) continue;
    out.add(DoseView(d, m, await s.db.schedule(d.scheduleId)));
  }
  return out;
}

/// "Bekleyenler" paneli — bildirimlerden değil veritabanından çizilir.
final pendingDosesProvider = FutureProvider<List<DoseView>>((ref) async {
  ref.watch(dbTickProvider);
  final s = ref.watch(servicesProvider);
  return _views(s, await s.db.pendingDoses(DateTime.now()));
});

final todayDosesProvider = FutureProvider<List<DoseView>>((ref) async {
  ref.watch(dbTickProvider);
  final s = ref.watch(servicesProvider);
  final start = dateOnly(DateTime.now());
  return _views(s, await s.db.dosesBetween(start, DateTime(start.year, start.month, start.day + 1)));
});

final todayStatsProvider = Provider<AdherenceStats>((ref) {
  final today = (ref.watch(todayDosesProvider).value ?? const <DoseView>[]).where((v) => !v.dose.isExtra).toList();
  final taken = today.where((v) => v.dose.status == DoseStatus.taken).length;
  return AdherenceStats(planned: today.length, onTime: taken);
});

final medicinesProvider = FutureProvider<List<Medicine>>((ref) async {
  ref.watch(dbTickProvider);
  return ref.watch(servicesProvider).db.medicines();
});

final doseViewProvider = FutureProvider.family<DoseView?, String>((ref, logId) async {
  ref.watch(dbTickProvider);
  final s = ref.watch(servicesProvider);
  final d = await s.db.dose(logId);
  if (d == null) return null;
  final v = await _views(s, [d]);
  return v.isEmpty ? null : v.first;
});

// ───────────── Takviye paneli ─────────────

final plansProvider = FutureProvider<List<SupplementPlan>>((ref) async {
  ref.watch(dbTickProvider);
  return ref.watch(servicesProvider).supplements.plans();
});

final conflictsProvider = FutureProvider<List<Conflict>>((ref) async {
  ref.watch(dbTickProvider);
  return ref.watch(servicesProvider).supplements.conflicts();
});

class StatsData {
  const StatsData({
    required this.days30,
    required this.week,
    required this.month,
    required this.streak,
    required this.moods,
    required this.perMedicine,
    required this.score,
  });

  final List<DayAdherence> days30;
  final AdherenceStats week;
  final AdherenceStats month;
  final int streak;

  /// "yyyy-MM-dd" → 1..5
  final Map<String, int> moods;
  final Map<int, AdherenceStats> perMedicine;
  final ({int total, int adherencePart, int moodPart}) score;
}

final statsProvider = FutureProvider<StatsData>((ref) async {
  ref.watch(dbTickProvider);
  final s = ref.watch(servicesProvider);
  final now = DateTime.now();
  final from30 = DateTime(now.year, now.month, now.day - 29);
  final from7 = DateTime(now.year, now.month, now.day - 6);
  final end = DateTime(now.year, now.month, now.day + 1);
  final logs = await s.db.dosesBetween(from30, end);
  final days = dailyBreakdown(logs, from: from30, days: 30, now: now);
  final month = computeAdherence(logs, from: from30, to: end, now: now);
  final moods = await s.db.moods(from30, end);
  final avgMood = moods.isEmpty ? null : moods.values.reduce((a, b) => a + b) / moods.length;
  return StatsData(
    days30: days,
    week: computeAdherence(logs, from: from7, to: end, now: now),
    month: month,
    streak: currentStreak(days, now),
    moods: moods,
    perMedicine: await s.supplements.perMedicineAdherence(),
    score: balanceScore(month.adherence, avgMood),
  );
});

String dayKey(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

final todayMoodProvider = FutureProvider<int?>((ref) async {
  ref.watch(dbTickProvider);
  final now = DateTime.now();
  final m = await ref.watch(servicesProvider).db.moods(dateOnly(now), DateTime(now.year, now.month, now.day + 1));
  return m[dayKey(now)];
});

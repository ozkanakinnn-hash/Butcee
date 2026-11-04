import 'dose_rule.dart';

enum MealRelation { none, beforeMeal, afterMeal, withMeal, emptyStomach }

enum DoseStatus { pending, snoozed, taken, skipped, missed }

enum SkipReason { unspecified, feelingUnwell, outOfStock, doctorAdvice, other }

/// Takviye/ilaç kategorisi.
enum ItemCategory { vitamin, mineral, fattyAcid, herbal, probiotic, protein, multi, medicine, other }

class Medicine {
  const Medicine({
    this.id,
    required this.name,
    this.nickname,
    this.form = 'tablet',
    this.barcode,
    this.totalStock = 0,
    this.criticalStockLevel = 5,
    this.packageSize,
    this.isCritical = false,
    this.expiryDates = const [],
    this.category = ItemCategory.vitamin,
    this.catalogId,
    this.doseAmount,
    this.doseUnit,
  });

  final int? id;
  final String name;

  /// Basit Mod'da gösterilen takma ad (ör. "Beyaz küçük hap — kalp ilacı").
  final String? nickname;
  final String form;
  final String? barcode;
  final double totalStock;
  final double criticalStockLevel;
  final int? packageSize;
  final bool isCritical;
  final List<DateTime> expiryDates;
  final ItemCategory category;

  /// `SupplementCatalog` kimliği (ör. "vitD"); etkileşim ve RDA bilgisi buradan gelir.
  final String? catalogId;

  /// Etiket dozu (ör. 1000 IU, 500 mg) — doz başına adet [DoseLog.quantity] ile çarpılır.
  final double? doseAmount;
  final String? doseUnit;

  String get displayName => nickname?.isNotEmpty == true ? nickname! : name;
  bool get isLowStock => totalStock <= criticalStockLevel;

  Medicine copyWith({int? id, double? totalStock, List<DateTime>? expiryDates, double? criticalStockLevel}) => Medicine(
        id: id ?? this.id,
        name: name,
        nickname: nickname,
        form: form,
        barcode: barcode,
        totalStock: totalStock ?? this.totalStock,
        criticalStockLevel: criticalStockLevel ?? this.criticalStockLevel,
        packageSize: packageSize,
        isCritical: isCritical,
        expiryDates: expiryDates ?? this.expiryDates,
        category: category,
        catalogId: catalogId,
        doseAmount: doseAmount,
        doseUnit: doseUnit,
      );

  Map<String, Object?> toRow() => {
        if (id != null) 'id': id,
        'name': name,
        'nickname': nickname,
        'form': form,
        'barcode': barcode,
        'total_stock': totalStock,
        'critical_stock_level': criticalStockLevel,
        'package_size': packageSize,
        'is_critical': isCritical ? 1 : 0,
        'expiry_dates': expiryDates.map(_ymd).join(','),
        'category': category.name,
        'catalog_id': catalogId,
        'dose_amount': doseAmount,
        'dose_unit': doseUnit,
      };

  factory Medicine.fromRow(Map<String, Object?> r) => Medicine(
        id: r['id'] as int,
        name: r['name'] as String,
        nickname: r['nickname'] as String?,
        form: r['form'] as String,
        barcode: r['barcode'] as String?,
        totalStock: (r['total_stock'] as num).toDouble(),
        criticalStockLevel: (r['critical_stock_level'] as num).toDouble(),
        packageSize: r['package_size'] as int?,
        isCritical: r['is_critical'] == 1,
        expiryDates: ((r['expiry_dates'] as String?) ?? '')
            .split(',')
            .where((e) => e.isNotEmpty)
            .map(DateTime.parse)
            .toList(),
        category: ItemCategory.values.asNameMap()[r['category']] ?? ItemCategory.vitamin,
        catalogId: r['catalog_id'] as String?,
        doseAmount: (r['dose_amount'] as num?)?.toDouble(),
        doseUnit: r['dose_unit'] as String?,
      );
}

String _ymd(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

class PersistentAlarm {
  const PersistentAlarm({this.enabled = false, this.repeatEveryMinutes = 3, this.maxRepeats = 10});
  final bool enabled;
  final int repeatEveryMinutes;

  /// En fazla 15 (bildirim kimlik şeması: doseIntId*16 + k).
  final int maxRepeats;
}

class Schedule {
  const Schedule({
    required this.id,
    required this.medicineId,
    required this.rule,
    this.isCritical = false,
    this.persistent = const PersistentAlarm(),
    this.graceMinutes = 60,
    this.maxSnoozes = 3,
    this.paused = false,
    this.slots = const [],
  });

  final String id;
  final int medicineId;
  final DoseRule rule;
  final bool isCritical;
  final PersistentAlarm persistent;
  final int graceMinutes;
  final int maxSnoozes;
  final bool paused;

  /// Hatırlatma dilimleri (ör. ["earlyMorning","evening"]). Dilim saati değişince kural yeniden yazılır.
  final List<String> slots;

  Schedule copyWith({DoseRule? rule, bool? paused, List<String>? slots}) => Schedule(
        id: id,
        medicineId: medicineId,
        rule: rule ?? this.rule,
        isCritical: isCritical,
        persistent: persistent,
        graceMinutes: graceMinutes,
        maxSnoozes: maxSnoozes,
        paused: paused ?? this.paused,
        slots: slots ?? this.slots,
      );

  Map<String, Object?> toRow() => {
        'id': id,
        'medicine_id': medicineId,
        'rule_json': DoseRule.encode(rule),
        'is_critical': isCritical ? 1 : 0,
        'persistent_enabled': persistent.enabled ? 1 : 0,
        'persistent_every_min': persistent.repeatEveryMinutes,
        'persistent_max': persistent.maxRepeats,
        'grace_minutes': graceMinutes,
        'max_snoozes': maxSnoozes,
        'paused': paused ? 1 : 0,
        'slots': slots.join(','),
      };

  factory Schedule.fromRow(Map<String, Object?> r) => Schedule(
        id: r['id'] as String,
        medicineId: r['medicine_id'] as int,
        rule: DoseRule.decode(r['rule_json'] as String),
        isCritical: r['is_critical'] == 1,
        persistent: PersistentAlarm(
          enabled: r['persistent_enabled'] == 1,
          repeatEveryMinutes: r['persistent_every_min'] as int,
          maxRepeats: r['persistent_max'] as int,
        ),
        graceMinutes: r['grace_minutes'] as int,
        maxSnoozes: r['max_snoozes'] as int,
        paused: r['paused'] == 1,
        slots: ((r['slots'] as String?) ?? '').split(',').where((e) => e.isNotEmpty).toList(),
      );
}

/// Tek bir doz örneği. [logId] deterministiktir: `scheduleId_yyyyMMddTHHmm`.
class DoseLog {
  const DoseLog({
    this.intId,
    required this.logId,
    required this.scheduleId,
    required this.medicineId,
    required this.scheduledTime,
    required this.graceUntil,
    this.quantity = 1,
    this.meal = MealRelation.none,
    this.status = DoseStatus.pending,
    this.actionTime,
    this.snoozedUntil,
    this.snoozeCount = 0,
    this.skipReason,
    this.note,
    this.slot,
    this.isExtra = false,
  });

  /// Yerel otomatik artan kimlik — bildirim kimliklerinin tabanı.
  final int? intId;
  final String logId;
  final String scheduleId;
  final int medicineId;
  final DateTime scheduledTime;
  final DateTime graceUntil;
  final double quantity;
  final MealRelation meal;
  final DoseStatus status;
  final DateTime? actionTime;
  final DateTime? snoozedUntil;
  final int snoozeCount;
  final SkipReason? skipReason;
  final String? note;

  /// Dozun ait olduğu hatırlatma dilimi (sessize alınmış dilimlerde alarm kurulmaz).
  final String? slot;

  /// Plan dışı ek kayıt ("Bir doz daha kaydet") — uyum hesabına girmez.
  final bool isExtra;

  bool get isResolved => status == DoseStatus.taken || status == DoseStatus.skipped;

  /// Alarmın çalacağı an.
  DateTime get fireAt => status == DoseStatus.snoozed && snoozedUntil != null ? snoozedUntil! : scheduledTime;

  static String buildLogId(String scheduleId, DateTime t) {
    String two(int v) => v.toString().padLeft(2, '0');
    return '${scheduleId}_${t.year}${two(t.month)}${two(t.day)}T${two(t.hour)}${two(t.minute)}';
  }

  factory DoseLog.planned(Schedule s, DateTime t, {String? slot}) => DoseLog(
        logId: buildLogId(s.id, t),
        scheduleId: s.id,
        medicineId: s.medicineId,
        scheduledTime: t,
        graceUntil: t.add(Duration(minutes: s.graceMinutes)),
        quantity: s.rule.quantity,
        meal: s.rule.meal,
        slot: slot,
      );

  /// "Bir doz daha kaydet": plan dışı, alınmış olarak kaydedilir.
  factory DoseLog.extra(Schedule s, DateTime at) => DoseLog(
        logId: '${s.id}_extra_${at.microsecondsSinceEpoch}',
        scheduleId: s.id,
        medicineId: s.medicineId,
        scheduledTime: at,
        graceUntil: at,
        quantity: s.rule.quantity,
        meal: s.rule.meal,
        status: DoseStatus.taken,
        actionTime: at,
        isExtra: true,
      );

  DoseLog copyWith({
    DoseStatus? status,
    DateTime? actionTime,
    DateTime? Function()? snoozedUntil,
    int? snoozeCount,
    DateTime? graceUntil,
    SkipReason? Function()? skipReason,
    String? note,
  }) =>
      DoseLog(
        intId: intId,
        logId: logId,
        scheduleId: scheduleId,
        medicineId: medicineId,
        scheduledTime: scheduledTime,
        graceUntil: graceUntil ?? this.graceUntil,
        quantity: quantity,
        meal: meal,
        status: status ?? this.status,
        actionTime: actionTime ?? this.actionTime,
        snoozedUntil: snoozedUntil != null ? snoozedUntil() : this.snoozedUntil,
        snoozeCount: snoozeCount ?? this.snoozeCount,
        skipReason: skipReason != null ? skipReason() : this.skipReason,
        note: note ?? this.note,
        slot: slot,
        isExtra: isExtra,
      );

  Map<String, Object?> toRow() => {
        if (intId != null) 'int_id': intId,
        'log_id': logId,
        'schedule_id': scheduleId,
        'medicine_id': medicineId,
        'scheduled_time': scheduledTime.millisecondsSinceEpoch,
        'grace_until': graceUntil.millisecondsSinceEpoch,
        'quantity': quantity,
        'meal': meal.name,
        'status': status.name,
        'action_time': actionTime?.millisecondsSinceEpoch,
        'snoozed_until': snoozedUntil?.millisecondsSinceEpoch,
        'snooze_count': snoozeCount,
        'skip_reason': skipReason?.name,
        'note': note,
        'slot': slot,
        'is_extra': isExtra ? 1 : 0,
      };

  factory DoseLog.fromRow(Map<String, Object?> r) {
    DateTime? ts(Object? v) => v == null ? null : DateTime.fromMillisecondsSinceEpoch(v as int);
    return DoseLog(
      intId: r['int_id'] as int?,
      logId: r['log_id'] as String,
      scheduleId: r['schedule_id'] as String,
      medicineId: r['medicine_id'] as int,
      scheduledTime: ts(r['scheduled_time'])!,
      graceUntil: ts(r['grace_until'])!,
      quantity: (r['quantity'] as num).toDouble(),
      meal: MealRelation.values.byName(r['meal'] as String),
      status: DoseStatus.values.byName(r['status'] as String),
      actionTime: ts(r['action_time']),
      snoozedUntil: ts(r['snoozed_until']),
      snoozeCount: r['snooze_count'] as int,
      skipReason: r['skip_reason'] == null ? null : SkipReason.values.byName(r['skip_reason'] as String),
      note: r['note'] as String?,
      slot: r['slot'] as String?,
      isExtra: r['is_extra'] == 1,
    );
  }
}

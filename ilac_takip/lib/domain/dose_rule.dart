import 'dart:convert';

import 'models.dart';

typedef TimeOfDayHM = ({int h, int m});

/// Esnek dozaj kuralları. Tüm zamanlar cihazın yerel saatindedir.
sealed class DoseRule {
  const DoseRule({
    required this.startDate,
    this.endDate,
    this.quantity = 1,
    this.meal = MealRelation.none,
  });

  /// Yalnızca tarih kısmı kullanılır.
  final DateTime startDate;

  /// Dahil (bu günün dozları da üretilir).
  final DateTime? endDate;
  final double quantity;
  final MealRelation meal;

  /// [from, to) aralığındaki doz zamanları, artan sırada.
  Iterable<DateTime> occurrences(DateTime from, DateTime to);

  /// Stok tükenme tahmini için ortalama günlük tüketim.
  double averageDailyQuantity();

  bool get raisesAlarms => this is! AsNeededRule;

  Map<String, Object?> toJson();

  static String encode(DoseRule r) => jsonEncode(r.toJson());
  static DoseRule decode(String s) => fromJson(jsonDecode(s) as Map<String, Object?>);

  static DoseRule fromJson(Map<String, Object?> j) {
    final start = DateTime.parse(j['startDate'] as String);
    final end = j['endDate'] == null ? null : DateTime.parse(j['endDate'] as String);
    final qty = (j['quantity'] as num? ?? 1).toDouble();
    final meal = MealRelation.values.byName(j['meal'] as String? ?? 'none');
    switch (j['type']) {
      case 'fixedTimes':
        return FixedTimesRule(
          startDate: start,
          endDate: end,
          quantity: qty,
          meal: meal,
          times: (j['times'] as List).cast<String>().map(parseHm).toList(),
          weekdays: (j['weekdays'] as List?)?.cast<int>().toSet(),
          everyNDays: j['everyNDays'] as int? ?? 1,
        );
      case 'interval':
        return IntervalRule(
          startDate: start,
          endDate: end,
          quantity: qty,
          meal: meal,
          firstDose: parseHm(j['firstDose'] as String),
          intervalHours: j['intervalHours'] as int,
        );
      case 'cyclic':
        final inner = fromJson((j['inner'] as Map).cast<String, Object?>()) as FixedTimesRule;
        return CyclicRule(
          startDate: start,
          endDate: end,
          quantity: qty,
          meal: meal,
          inner: inner,
          onDays: j['onDays'] as int,
          offDays: j['offDays'] as int,
        );
      case 'asNeeded':
        return AsNeededRule(
          startDate: start,
          endDate: end,
          quantity: qty,
          maxPerDay: j['maxPerDay'] as int,
          minIntervalHours: j['minIntervalHours'] as int,
        );
      default:
        throw FormatException('Bilinmeyen kural türü: ${j['type']}');
    }
  }

  Map<String, Object?> baseJson(String type) => {
        'type': type,
        'startDate': _ymd(startDate),
        if (endDate != null) 'endDate': _ymd(endDate!),
        'quantity': quantity,
        'meal': meal.name,
      };

  bool isActiveOn(DateTime day) {
    final d = dateOnly(day);
    if (d.isBefore(dateOnly(startDate))) return false;
    if (endDate != null && d.isAfter(dateOnly(endDate!))) return false;
    return true;
  }
}

/// "Günde 3 kez 08:00/14:00/20:00", "gün aşırı", "Pzt-Çar-Cum".
class FixedTimesRule extends DoseRule {
  const FixedTimesRule({
    required super.startDate,
    super.endDate,
    super.quantity,
    super.meal,
    required this.times,
    this.weekdays,
    this.everyNDays = 1,
  }) : assert(everyNDays >= 1);

  final List<TimeOfDayHM> times;

  /// DateTime.monday..DateTime.sunday; null = her gün.
  final Set<int>? weekdays;

  /// 2 = gün aşırı.
  final int everyNDays;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) sync* {
    final sorted = [...times]..sort((a, b) => (a.h * 60 + a.m).compareTo(b.h * 60 + b.m));
    var day = dateOnly(from);
    while (day.isBefore(to)) {
      final idx = daysBetween(startDate, day);
      final active = isActiveOn(day) &&
          idx % everyNDays == 0 &&
          (weekdays == null || weekdays!.contains(day.weekday));
      if (active) {
        for (final t in sorted) {
          final dt = DateTime(day.year, day.month, day.day, t.h, t.m);
          if (!dt.isBefore(from) && dt.isBefore(to)) yield dt;
        }
      }
      day = DateTime(day.year, day.month, day.day + 1); // DST güvenli
    }
  }

  @override
  double averageDailyQuantity() =>
      quantity * times.length / everyNDays * ((weekdays?.length ?? 7) / 7);

  @override
  Map<String, Object?> toJson() => {
        ...baseJson('fixedTimes'),
        'times': times.map(formatHm).toList(),
        if (weekdays != null) 'weekdays': (weekdays!.toList()..sort()),
        'everyNDays': everyNDays,
      };
}

/// "8 saatte bir, ilk doz 07:00" — gece yarısını aşan döngüler doğru hesaplanır.
class IntervalRule extends DoseRule {
  const IntervalRule({
    required super.startDate,
    super.endDate,
    super.quantity,
    super.meal,
    required this.firstDose,
    required this.intervalHours,
  }) : assert(intervalHours >= 1);

  final TimeOfDayHM firstDose;
  final int intervalHours;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) sync* {
    var t = DateTime(startDate.year, startDate.month, startDate.day, firstDose.h, firstDose.m);
    if (t.isBefore(from)) {
      final steps = from.difference(t).inMinutes ~/ (intervalHours * 60);
      t = t.add(Duration(hours: steps * intervalHours));
    }
    while (t.isBefore(to)) {
      if (endDate != null && dateOnly(t).isAfter(dateOnly(endDate!))) return;
      if (!t.isBefore(from)) yield t;
      t = t.add(Duration(hours: intervalHours));
    }
  }

  @override
  double averageDailyQuantity() => quantity * 24 / intervalHours;

  @override
  Map<String, Object?> toJson() => {
        ...baseJson('interval'),
        'firstDose': formatHm(firstDose),
        'intervalHours': intervalHours,
      };
}

/// "21 gün kullan, 7 gün ara ver".
class CyclicRule extends DoseRule {
  const CyclicRule({
    required super.startDate,
    super.endDate,
    super.quantity,
    super.meal,
    required this.inner,
    required this.onDays,
    required this.offDays,
  });

  final FixedTimesRule inner;
  final int onDays;
  final int offDays;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) => inner
      .occurrences(from, to)
      .where((dt) => isActiveOn(dt) && daysBetween(startDate, dt) % (onDays + offDays) < onDays);

  @override
  double averageDailyQuantity() => inner.averageDailyQuantity() * onDays / (onDays + offDays);

  @override
  Map<String, Object?> toJson() => {
        ...baseJson('cyclic'),
        'inner': inner.toJson(),
        'onDays': onDays,
        'offDays': offDays,
      };
}

/// "Ağrı olduğunda, günde en fazla 3" — alarm kurulmaz.
class AsNeededRule extends DoseRule {
  const AsNeededRule({
    required super.startDate,
    super.endDate,
    super.quantity,
    required this.maxPerDay,
    required this.minIntervalHours,
  });

  final int maxPerDay;
  final int minIntervalHours;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) => const [];

  @override
  double averageDailyQuantity() => 0;

  @override
  Map<String, Object?> toJson() => {
        ...baseJson('asNeeded'),
        'maxPerDay': maxPerDay,
        'minIntervalHours': minIntervalHours,
      };
}

DateTime dateOnly(DateTime d) => DateTime(d.year, d.month, d.day);

int daysBetween(DateTime a, DateTime b) =>
    DateTime.utc(b.year, b.month, b.day).difference(DateTime.utc(a.year, a.month, a.day)).inDays;

TimeOfDayHM parseHm(String s) {
  final p = s.split(':');
  return (h: int.parse(p[0]), m: int.parse(p[1]));
}

String formatHm(TimeOfDayHM t) => '${t.h.toString().padLeft(2, '0')}:${t.m.toString().padLeft(2, '0')}';

String _ymd(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

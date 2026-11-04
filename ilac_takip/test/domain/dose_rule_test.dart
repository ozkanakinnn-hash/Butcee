import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';

void main() {
  final start = DateTime(2026, 11, 2); // Pazartesi

  group('FixedTimesRule', () {
    test('günde 3 kez, saat sırası korunur', () {
      final r = FixedTimesRule(startDate: start, times: [(h: 20, m: 0), (h: 8, m: 0), (h: 14, m: 0)]);
      final occ = r.occurrences(DateTime(2026, 11, 2), DateTime(2026, 11, 3)).toList();
      expect(occ, [DateTime(2026, 11, 2, 8), DateTime(2026, 11, 2, 14), DateTime(2026, 11, 2, 20)]);
      expect(r.averageDailyQuantity(), 3);
    });

    test('gün aşırı', () {
      final r = FixedTimesRule(startDate: start, times: [(h: 9, m: 0)], everyNDays: 2);
      final days = r.occurrences(start, DateTime(2026, 11, 9)).map((d) => d.day).toList();
      expect(days, [2, 4, 6, 8]);
      expect(r.averageDailyQuantity(), 0.5);
    });

    test('yalnızca Pzt-Çar-Cum', () {
      final r = FixedTimesRule(
        startDate: start,
        times: [(h: 9, m: 0)],
        weekdays: {DateTime.monday, DateTime.wednesday, DateTime.friday},
      );
      final wd = r.occurrences(start, DateTime(2026, 11, 9)).map((d) => d.weekday).toList();
      expect(wd, [DateTime.monday, DateTime.wednesday, DateTime.friday]);
    });

    test('başlangıçtan önce ve bitişten sonra doz üretilmez', () {
      final r = FixedTimesRule(startDate: start, endDate: DateTime(2026, 11, 3), times: [(h: 9, m: 0)]);
      final occ = r.occurrences(DateTime(2026, 10, 30), DateTime(2026, 11, 10)).toList();
      expect(occ, [DateTime(2026, 11, 2, 9), DateTime(2026, 11, 3, 9)]);
    });

    test('aralık sınırları: from dahil, to hariç', () {
      final r = FixedTimesRule(startDate: start, times: [(h: 9, m: 0)]);
      expect(r.occurrences(DateTime(2026, 11, 3, 9), DateTime(2026, 11, 4, 9)).toList(), [DateTime(2026, 11, 3, 9)]);
    });
  });

  group('IntervalRule', () {
    test('8 saatte bir gece yarısını aşar', () {
      final r = IntervalRule(startDate: start, firstDose: (h: 7, m: 0), intervalHours: 8);
      final occ = r.occurrences(DateTime(2026, 11, 2), DateTime(2026, 11, 3, 12)).toList();
      expect(occ, [
        DateTime(2026, 11, 2, 7),
        DateTime(2026, 11, 2, 15),
        DateTime(2026, 11, 2, 23),
        DateTime(2026, 11, 3, 7),
      ]);
      expect(r.averageDailyQuantity(), 3);
    });

    test('ileri bir aralığa hızlı atlar', () {
      final r = IntervalRule(startDate: start, firstDose: (h: 7, m: 0), intervalHours: 8);
      final occ = r.occurrences(DateTime(2026, 12, 1, 8), DateTime(2026, 12, 1, 23, 30)).toList();
      expect(occ, [DateTime(2026, 12, 1, 15), DateTime(2026, 12, 1, 23)]);
    });
  });

  test('CyclicRule: 3 gün kullan, 2 gün ara', () {
    final r = CyclicRule(
      startDate: start,
      inner: FixedTimesRule(startDate: start, times: [(h: 9, m: 0)]),
      onDays: 3,
      offDays: 2,
    );
    final days = r.occurrences(start, DateTime(2026, 11, 12)).map((d) => d.day).toList();
    expect(days, [2, 3, 4, 7, 8, 9]);
    expect(r.averageDailyQuantity(), closeTo(0.6, 1e-9));
  });

  test('AsNeededRule alarm üretmez', () {
    final r = AsNeededRule(startDate: start, maxPerDay: 3, minIntervalHours: 6);
    expect(r.occurrences(start, DateTime(2026, 12, 1)), isEmpty);
    expect(r.raisesAlarms, isFalse);
  });

  test('JSON gidiş-dönüş tüm kural türlerini korur', () {
    final rules = <DoseRule>[
      FixedTimesRule(
        startDate: start,
        quantity: 0.5,
        meal: MealRelation.afterMeal,
        times: [(h: 8, m: 30)],
        weekdays: {1, 5},
        everyNDays: 2,
      ),
      IntervalRule(startDate: start, endDate: DateTime(2026, 12, 1), firstDose: (h: 7, m: 0), intervalHours: 12),
      CyclicRule(startDate: start, inner: FixedTimesRule(startDate: start, times: [(h: 9, m: 0)]), onDays: 21, offDays: 7),
      AsNeededRule(startDate: start, maxPerDay: 2, minIntervalHours: 8),
    ];
    for (final r in rules) {
      final back = DoseRule.decode(DoseRule.encode(r));
      expect(back.runtimeType, r.runtimeType);
      expect(back.toJson(), r.toJson());
      expect(back.occurrences(start, DateTime(2026, 11, 30)).toList(), r.occurrences(start, DateTime(2026, 11, 30)).toList());
    }
  });

  test('DoseLog.buildLogId deterministiktir', () {
    expect(DoseLog.buildLogId('sch_1', DateTime(2026, 1, 5, 7, 3)), 'sch_1_20260105T0703');
  });
}

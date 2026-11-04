import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/domain/adherence.dart';
import 'package:ilac_takip/domain/dose_transitions.dart';
import 'package:ilac_takip/domain/models.dart';

DoseLog _log(DateTime t, {DoseStatus status = DoseStatus.pending, DateTime? action, int graceMin = 60}) => DoseLog(
      logId: DoseLog.buildLogId('s', t),
      scheduleId: 's',
      medicineId: 1,
      scheduledTime: t,
      graceUntil: t.add(Duration(minutes: graceMin)),
      status: status,
      actionTime: action,
    );

void main() {
  final t9 = DateTime(2026, 11, 4, 9);

  group('DoseTransitions', () {
    test('take ertelemeyi temizler', () {
      final snoozed = DoseTransitions.snooze(_log(t9), t9, const Duration(minutes: 10), maxSnoozes: 3)!;
      final taken = DoseTransitions.take(snoozed, t9.add(const Duration(minutes: 12)));
      expect(taken.status, DoseStatus.taken);
      expect(taken.snoozedUntil, isNull);
      expect(taken.snoozeCount, 1);
    });

    test('snooze sayacı ve azami erteleme', () {
      var l = _log(t9);
      for (var i = 0; i < 3; i++) {
        l = DoseTransitions.snooze(l, t9, const Duration(minutes: 10), maxSnoozes: 3)!;
      }
      expect(l.snoozeCount, 3);
      expect(DoseTransitions.snooze(l, t9, const Duration(minutes: 10), maxSnoozes: 3), isNull);
    });

    test('erteleme grace süresini gerekirse uzatır', () {
      final l = _log(t9, graceMin: 30);
      final s = DoseTransitions.snooze(l, t9.add(const Duration(minutes: 25)), const Duration(minutes: 10), maxSnoozes: 3)!;
      expect(s.snoozedUntil, t9.add(const Duration(minutes: 35)));
      expect(s.graceUntil, t9.add(const Duration(minutes: 50))); // 35 + 15
    });

    test('alınmış doz ertelenemez / atlanamaz', () {
      final taken = DoseTransitions.take(_log(t9), t9);
      expect(DoseTransitions.snooze(taken, t9, const Duration(minutes: 10), maxSnoozes: 3), isNull);
      expect(DoseTransitions.skip(taken, t9, SkipReason.other).status, DoseStatus.taken);
    });

    test('süresi dolan bekleyen doz kaçırıldı olur; kaçırılan sonradan alınabilir', () {
      final l = _log(t9);
      expect(DoseTransitions.markMissedIfExpired(l, t9.add(const Duration(minutes: 59))), isNull);
      final missed = DoseTransitions.markMissedIfExpired(l, t9.add(const Duration(minutes: 60)))!;
      expect(missed.status, DoseStatus.missed);
      expect(DoseTransitions.take(missed, t9.add(const Duration(hours: 2))).status, DoseStatus.taken);
    });
  });

  group('computeAdherence', () {
    final from = DateTime(2026, 11, 1);
    final to = DateTime(2026, 11, 8);
    final now = DateTime(2026, 11, 4, 12);

    test('zamanında / geç / atlanan / kaçırılan / hariç tutulanlar', () {
      final logs = [
        _log(DateTime(2026, 11, 1, 9), status: DoseStatus.taken, action: DateTime(2026, 11, 1, 9, 20)),
        _log(DateTime(2026, 11, 2, 9), status: DoseStatus.taken, action: DateTime(2026, 11, 2, 11)),
        _log(DateTime(2026, 11, 3, 9), status: DoseStatus.skipped, action: DateTime(2026, 11, 3, 9)),
        _log(DateTime(2026, 11, 3, 21), status: DoseStatus.missed),
        _log(DateTime(2026, 11, 4, 9)), // süresi dolmuş pending → kaçırıldı sayılır
        _log(DateTime(2026, 11, 4, 11, 30)), // grace dolmadı → hariç
        _log(DateTime(2026, 11, 5, 9)), // gelecek → hariç
        _log(DateTime(2026, 10, 31, 9), status: DoseStatus.taken, action: DateTime(2026, 10, 31, 9)), // aralık dışı
      ];
      final s = computeAdherence(logs, from: from, to: to, now: now);
      expect(s.planned, 5);
      expect(s.onTime, 1);
      expect(s.late, 1);
      expect(s.skipped, 1);
      expect(s.missed, 2);
      expect(s.adherence, closeTo(0.4, 1e-9));
      expect(s.onTimeRate, closeTo(0.2, 1e-9));
    });

    test('boş liste sıfır döner', () {
      expect(computeAdherence(const [], from: from, to: to, now: now).adherence, 0);
    });
  });
}

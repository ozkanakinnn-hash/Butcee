import 'models.dart';

/// Doz durum makinesinin saf geçişleri (yan etkisiz, birim testi kolay).
///
/// pending ──take──▶ taken
///    │  └──skip──▶ skipped
///    └──snooze──▶ snoozed(until) ──take/skip/snooze──▶ ...
/// pending/snoozed ──graceUntil geçti──▶ missed ──take(geç)──▶ taken
class DoseTransitions {
  DoseTransitions._();

  static const graceAfterSnooze = Duration(minutes: 15);

  static DoseLog take(DoseLog log, DateTime at, {String? note}) {
    if (log.status == DoseStatus.taken) return log;
    return log.copyWith(
      status: DoseStatus.taken,
      actionTime: at,
      snoozedUntil: () => null,
      skipReason: () => null,
      note: note,
    );
  }

  static DoseLog skip(DoseLog log, DateTime at, SkipReason reason) {
    if (log.isResolved) return log;
    return log.copyWith(
      status: DoseStatus.skipped,
      actionTime: at,
      snoozedUntil: () => null,
      skipReason: () => reason,
    );
  }

  /// Erteleme hakkı bittiyse `null` döner.
  static DoseLog? snooze(DoseLog log, DateTime now, Duration by, {required int maxSnoozes}) {
    if (log.isResolved || log.status == DoseStatus.missed) return null;
    if (log.snoozeCount >= maxSnoozes) return null;
    final until = now.add(by);
    final minGrace = until.add(graceAfterSnooze);
    return log.copyWith(
      status: DoseStatus.snoozed,
      snoozedUntil: () => until,
      snoozeCount: log.snoozeCount + 1,
      graceUntil: log.graceUntil.isAfter(minGrace) ? log.graceUntil : minGrace,
    );
  }

  static DoseLog? markMissedIfExpired(DoseLog log, DateTime now) {
    final waiting = log.status == DoseStatus.pending || log.status == DoseStatus.snoozed;
    if (!waiting || now.isBefore(log.graceUntil)) return null;
    return log.copyWith(status: DoseStatus.missed, snoozedUntil: () => null);
  }

  /// "Geri al" — yalnızca kullanıcının az önce yaptığı işlemi geri döndürür.
  static DoseLog revert(DoseLog log) => log.copyWith(
        status: DoseStatus.pending,
        snoozedUntil: () => null,
        skipReason: () => null,
      );
}

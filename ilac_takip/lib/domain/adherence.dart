import 'models.dart';

class AdherenceStats {
  const AdherenceStats({
    this.planned = 0,
    this.onTime = 0,
    this.late = 0,
    this.skipped = 0,
    this.missed = 0,
  });

  final int planned;
  final int onTime;
  final int late;
  final int skipped;
  final int missed;

  int get taken => onTime + late;
  double get adherence => planned == 0 ? 0 : taken / planned;
  double get onTimeRate => planned == 0 ? 0 : onTime / planned;
}

/// Uyum = (zamanında + geç) / (planlanan − gelecek − süresi dolmamış bekleyenler).
AdherenceStats computeAdherence(
  Iterable<DoseLog> logs, {
  required DateTime from,
  required DateTime to,
  required DateTime now,
  Duration onTimeWindow = const Duration(minutes: 60),
}) {
  var planned = 0, onTime = 0, late = 0, skipped = 0, missed = 0;
  for (final l in logs) {
    if (l.scheduledTime.isBefore(from) || !l.scheduledTime.isBefore(to)) continue;
    if (l.scheduledTime.isAfter(now)) continue;
    final waiting = l.status == DoseStatus.pending || l.status == DoseStatus.snoozed;
    if (waiting && l.graceUntil.isAfter(now)) continue;

    planned++;
    switch (l.status) {
      case DoseStatus.taken:
        final diff = l.actionTime!.difference(l.scheduledTime).abs();
        if (diff <= onTimeWindow) {
          onTime++;
        } else {
          late++;
        }
      case DoseStatus.skipped:
        skipped++;
      case DoseStatus.missed:
      case DoseStatus.pending:
      case DoseStatus.snoozed:
        missed++;
    }
  }
  return AdherenceStats(planned: planned, onTime: onTime, late: late, skipped: skipped, missed: missed);
}

/// Plan ve son 14 günün gerçek tüketimine göre stok bitiş tahmini.
DateTime? estimateRunOut({
  required double stock,
  required double plannedDaily,
  required double actualDailyLast14,
  required DateTime now,
}) {
  final daily = actualDailyLast14 > 0 ? 0.6 * plannedDaily + 0.4 * actualDailyLast14 : plannedDaily;
  if (daily <= 0) return null;
  return now.add(Duration(minutes: (stock / daily * 24 * 60).floor()));
}

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
    if (l.isExtra) continue;
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

/// Bir günün özeti (istatistik ekranındaki noktalar ve seri hesabı için).
class DayAdherence {
  const DayAdherence(this.day, this.stats);
  final DateTime day;
  final AdherenceStats stats;

  bool get hasPlan => stats.planned > 0;
  bool get complete => hasPlan && stats.taken == stats.planned;
}

/// [from] gününden itibaren [days] günlük döküm.
List<DayAdherence> dailyBreakdown(Iterable<DoseLog> logs, {required DateTime from, required int days, required DateTime now}) {
  final list = logs.toList();
  return List.generate(days, (i) {
    final start = DateTime(from.year, from.month, from.day + i);
    final end = DateTime(from.year, from.month, from.day + i + 1);
    return DayAdherence(start, computeAdherence(list, from: start, to: end, now: now));
  });
}

/// Art arda eksiksiz gün sayısı. Bugün henüz tamamlanmadıysa dünden geriye sayılır;
/// planı olmayan günler seriyi bozmaz.
int currentStreak(List<DayAdherence> days, DateTime now) {
  final today = DateTime(now.year, now.month, now.day);
  var streak = 0;
  for (final d in days.reversed) {
    if (d.day.isAfter(today)) continue;
    final isToday = d.day == today;
    if (!d.hasPlan) continue;
    if (d.complete) {
      streak++;
    } else if (isToday) {
      continue; // bugün hâlâ devam ediyor
    } else {
      break;
    }
  }
  return streak;
}

/// Denge puanı (0–100): uyum en fazla 60, öz bildirim (ruh hâli 1–5) en fazla 40 puan.
/// Ruh hâli kaydı yoksa yalnızca uyumdan hesaplanır.
({int total, int adherencePart, int moodPart}) balanceScore(double adherence, double? averageMood) {
  if (averageMood == null) {
    final t = (adherence * 100).round();
    return (total: t, adherencePart: t, moodPart: 0);
  }
  final a = (adherence * 60).round();
  final m = ((averageMood.clamp(1, 5) - 1) / 4 * 40).round();
  return (total: a + m, adherencePart: a, moodPart: m);
}

import '../data/app_database.dart';
import '../domain/dose_transitions.dart';
import '../domain/models.dart';
import 'alarm_scheduler.dart';

/// Alarm başlık/gövde metinlerini üretir (yerelleştirilmiş).
abstract class DoseTextBuilder {
  String title(Medicine m, DoseLog d);
  String body(Medicine m, DoseLog d);
  String repeatTitle(Medicine m, DoseLog d);
}

typedef Clock = DateTime Function();

/// Yerel DB'den alarmları kurar. Tetikleyiciler: açılış, ön plana gelme, plan değişimi,
/// her doz işlemi, saat dilimi değişimi.
class AlarmPlanner {
  AlarmPlanner({
    required this.db,
    required this.scheduler,
    required this.texts,
    required this.clock,
    this.window = const Duration(hours: 48),
    this.materializeAhead = const Duration(days: 7),
  });

  final AppDatabase db;
  final AlarmScheduler scheduler;
  DoseTextBuilder texts;
  final Clock clock;
  final Duration window;
  final Duration materializeAhead;

  /// Doz örneklerini üretir, süresi dolanları "kaçırıldı" yapar, pencere içindekileri kurar,
  /// artık istenmeyenleri iptal eder. Kurulan doz sayısını döner.
  Future<int> reconcile() async {
    final now = clock();
    await materialize(now);
    await markExpired(now);

    final wanted = <int>{};
    var used = 0;
    var scheduled = 0;
    final pending = await scheduler.pendingIds();

    for (final d in await db.alarmableDoses(now.subtract(const Duration(hours: 1)), now.add(window))) {
      final planned = await plannedFor(d);
      if (planned == null) continue;
      final cost = planned.notificationCount(repeatCap: scheduler.repeatCap);
      if (used + cost > scheduler.pendingBudget) break;
      used += cost;
      for (var k = 0; k < cost; k++) {
        wanted.add(d.intId! * AlarmIds.slots + k);
      }
      if (!pending.contains(AlarmIds.main(d.intId!))) {
        await scheduler.schedule(planned);
        scheduled++;
      }
    }

    for (final id in pending) {
      if (id >= AlarmIds.reservedFrom) continue; // test/doz dışı bildirimler
      if (!wanted.contains(id)) await scheduler.cancelId(id);
    }
    db.notifyChanged();
    return scheduled;
  }

  /// Tüm doz alarmlarını iptal edip yeniden kurar (dil değişimi, saat dilimi değişimi).
  Future<void> rescheduleAll() async {
    for (final id in await scheduler.pendingIds()) {
      if (id < AlarmIds.reservedFrom) await scheduler.cancelId(id);
    }
    await reconcile();
  }

  /// Planın değiştiği durumda: gelecekteki bekleyenleri sil, alarmlarını iptal et, yeniden kur.
  Future<void> rebuildSchedule(String scheduleId) async {
    final now = clock();
    for (final d in await db.alarmableDoses(now, now.add(materializeAhead))) {
      if (d.scheduleId == scheduleId && d.status == DoseStatus.pending) {
        await scheduler.cancelDose(d.intId!);
      }
    }
    await db.deleteFuturePending(scheduleId, now);
    await reconcile();
  }

  Future<void> materialize(DateTime now) async {
    final from = now.subtract(const Duration(hours: 24));
    final to = now.add(materializeAhead);
    for (final s in await db.activeSchedules()) {
      if (!s.rule.raisesAlarms) continue;
      for (final t in s.rule.occurrences(from, to)) {
        await db.insertDoseIfAbsent(DoseLog.planned(s, t));
      }
    }
  }

  Future<void> markExpired(DateTime now) async {
    for (final d in await db.expiredWaitingDoses(now)) {
      final missed = DoseTransitions.markMissedIfExpired(d, now);
      if (missed == null) continue;
      await db.updateDose(missed);
      if (d.intId != null) await scheduler.cancelDose(d.intId!);
      await db.enqueueOutbox('doseLog', d.logId, now);
    }
  }

  Future<PlannedDose?> plannedFor(DoseLog d) async {
    final schedule = await db.schedule(d.scheduleId);
    final medicine = await db.medicine(d.medicineId);
    if (schedule == null || medicine == null || d.intId == null) return null;
    final critical = schedule.isCritical || medicine.isCritical;
    return PlannedDose(
      intId: d.intId!,
      logId: d.logId,
      fireAt: d.fireAt,
      title: texts.title(medicine, d),
      body: texts.body(medicine, d),
      repeatTitle: texts.repeatTitle(medicine, d),
      isCritical: critical,
      persistent: schedule.persistent.enabled,
      repeatEveryMinutes: schedule.persistent.repeatEveryMinutes,
      maxRepeats: schedule.persistent.maxRepeats,
      canSnooze: d.snoozeCount < schedule.maxSnoozes,
    );
  }
}

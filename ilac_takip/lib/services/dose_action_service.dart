import '../data/app_database.dart';
import '../domain/dose_transitions.dart';
import '../domain/models.dart';
import 'alarm_planner.dart';
import 'alarm_scheduler.dart';
import 'local_notification_scheduler.dart';

/// Düşük stok uyarısı metni.
typedef LowStockText = ({String title, String body}) Function(Medicine m);

/// Aldım / ertele / atla işlemlerinin TEK giriş noktası (UI, bildirim aksiyonu, arka plan izolatı).
///
/// Her işlem: (1) yerel DB'yi günceller, (2) alarmları iptal eder/yeniden kurar,
/// (3) Firestore senkronu için outbox'a yazar.
class DoseActionService {
  DoseActionService({
    required this.db,
    required this.scheduler,
    required this.planner,
    required this.clock,
    this.lowStockText,
    this.defaultSnooze = const Duration(minutes: 10),
  });

  final AppDatabase db;
  final AlarmScheduler scheduler;
  final AlarmPlanner planner;
  final Clock clock;
  LowStockText? lowStockText;
  final Duration defaultSnooze;

  Future<void> handleNotificationAction(String? actionId, String? logId) async {
    if (logId == null) return;
    switch (actionId) {
      case NotificationActions.take:
        await take(logId);
      case NotificationActions.snooze:
        await snooze(logId);
      case NotificationActions.skip:
        await skip(logId, SkipReason.unspecified);
    }
  }

  /// Önceki hâli döner (geri alma için).
  Future<DoseLog?> take(String logId, {DateTime? at, String? note}) async {
    final log = await db.dose(logId);
    if (log == null || log.status == DoseStatus.taken) return null;
    final now = clock();
    final updated = DoseTransitions.take(log, at ?? now, note: note);
    await db.transaction((txn) async {
      await db.updateDose(updated, txn: txn);
      await db.adjustStock(log.medicineId, -log.quantity, txn: txn);
      await db.enqueueOutbox('doseLog', logId, now, txn: txn);
    });
    if (log.intId != null) await scheduler.cancelDose(log.intId!);
    await _checkLowStock(log.medicineId);
    return log;
  }

  /// Erteleme hakkı bittiyse `false`.
  Future<bool> snooze(String logId, {Duration? by}) async {
    final log = await db.dose(logId);
    if (log == null) return false;
    final schedule = await db.schedule(log.scheduleId);
    final now = clock();
    final updated = DoseTransitions.snooze(log, now, by ?? defaultSnooze, maxSnoozes: schedule?.maxSnoozes ?? 3);
    if (updated == null) return false;
    await db.transaction((txn) async {
      await db.updateDose(updated, txn: txn);
      await db.enqueueOutbox('doseLog', logId, now, txn: txn);
    });
    if (log.intId != null) {
      await scheduler.cancelDose(log.intId!);
      final planned = await planner.plannedFor(updated);
      try {
        if (planned != null) await scheduler.schedule(planned);
      } on Exception catch (e) {
        planner.lastError = e; // erteleme kaydedildi; alarm kurulamadıysa panelde yine görünür
      }
    }
    return true;
  }

  Future<DoseLog?> skip(String logId, SkipReason reason) async {
    final log = await db.dose(logId);
    if (log == null || log.isResolved) return null;
    final now = clock();
    await db.transaction((txn) async {
      await db.updateDose(DoseTransitions.skip(log, now, reason), txn: txn);
      await db.enqueueOutbox('doseLog', logId, now, txn: txn);
    });
    if (log.intId != null) await scheduler.cancelDose(log.intId!);
    return log;
  }

  /// "Bir doz daha kaydet" — plan dışı alım; stok düşer, uyum hesabına girmez.
  Future<DoseLog?> logExtra(int medicineId) async {
    final schedules = await db.schedulesForMedicine(medicineId);
    if (schedules.isEmpty) return null;
    final now = clock();
    final d = DoseLog.extra(schedules.first, now);
    await db.insertExtraDose(d, now: now);
    await _checkLowStock(medicineId);
    return d;
  }

  /// "Geri al": [previous] hâline döndürür, alındıysa stoğu iade eder, alarmları yeniden kurar.
  Future<void> undo(DoseLog previous) async {
    final current = await db.dose(previous.logId);
    if (current == null) return;
    final now = clock();
    await db.transaction((txn) async {
      await db.updateDose(previous, txn: txn);
      if (current.status == DoseStatus.taken) {
        await db.adjustStock(current.medicineId, current.quantity, txn: txn);
      }
      await db.enqueueOutbox('doseLog', previous.logId, now, txn: txn);
    });
    await planner.reconcile();
  }

  Future<void> _checkLowStock(int medicineId) async {
    final m = await db.medicine(medicineId);
    final text = lowStockText;
    if (m == null || text == null || !m.isLowStock) return;
    final t = text(m);
    await scheduler.showNow(id: AlarmIds.lowStock(medicineId), title: t.title, body: t.body);
  }
}

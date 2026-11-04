/// Bir doz için kurulacak alarmın platformdan bağımsız tanımı.
class PlannedDose {
  const PlannedDose({
    required this.intId,
    required this.logId,
    required this.fireAt,
    required this.title,
    required this.body,
    required this.repeatTitle,
    this.isCritical = false,
    this.persistent = false,
    this.repeatEveryMinutes = 3,
    this.maxRepeats = 0,
    this.canSnooze = true,
  });

  final int intId;
  final String logId;
  final DateTime fireAt;
  final String title;
  final String body;
  final String repeatTitle;
  final bool isCritical;
  final bool persistent;
  final int repeatEveryMinutes;
  final int maxRepeats;
  final bool canSnooze;

  /// Ana alarm + ısrarcı tekrarlar.
  int notificationCount({required int repeatCap}) =>
      1 + (persistent ? (maxRepeats < repeatCap ? maxRepeats : repeatCap) : 0);
}

/// Bildirim kimlik şeması: ana alarm `intId*16`, tekrarlar `intId*16 + k` (1 ≤ k ≤ 15).
class AlarmIds {
  AlarmIds._();
  static const slots = 16;
  static int main(int intId) => intId * slots;
  static int repeat(int intId, int k) {
    assert(k >= 1 && k < slots);
    return intId * slots + k;
  }

  static int doseOf(int notificationId) => notificationId ~/ slots;

  /// Doz dışı bildirimler (düşük stok vb.) bu tabanın üstünde.
  static const miscBase = 1 << 30;
  static int lowStock(int medicineId) => miscBase + medicineId;

  /// Test alarmı için ayrılmış doz bloğu; planlayıcı bu bloğa ve üstüne dokunmaz.
  static const testIntId = miscBase ~/ slots - 1;
  static const reservedFrom = testIntId * slots;
}

/// Alarm kurma soyutlaması — gerçek uygulama `LocalNotificationAlarmScheduler`,
/// testlerde sahte uygulama kullanılır.
abstract class AlarmScheduler {
  /// iOS'ta aynı anda en fazla 64 bekleyen bildirim olabilir.
  int get pendingBudget;

  /// Bir doz için platformun izin verdiği en fazla tekrar sayısı.
  int get repeatCap;

  Future<void> schedule(PlannedDose dose);
  Future<void> cancelDose(int intId);
  Future<void> cancelId(int notificationId);
  Future<Set<int>> pendingIds();
  Future<void> showNow({required int id, required String title, required String body});
}

import 'dart:io' show Platform;

import 'package:flutter/services.dart';

import 'local_notification_scheduler.dart';

class ReliabilityStatus {
  const ReliabilityStatus({
    required this.notifications,
    required this.exactAlarms,
    required this.batteryUnrestricted,
    required this.fullScreen,
    required this.manufacturer,
  });

  final bool notifications;
  final bool exactAlarms;
  final bool batteryUnrestricted;
  final bool fullScreen;
  final String manufacturer;

  bool get isAndroid => manufacturer != 'apple';

  /// Uygulamaları agresif kapatan üreticiler (bkz. dontkillmyapp.com).
  bool get needsOemGuide => const {
        'xiaomi', 'redmi', 'poco', 'huawei', 'honor', 'oppo', 'realme', 'vivo', 'oneplus', 'samsung', 'meizu', 'asus',
      }.contains(manufacturer);
}

/// İzin ve pil kısıtlaması durumunu okur, ilgili ayar ekranlarını açar.
/// Android tarafı: `android/app/src/main/kotlin/.../MainActivity.kt` (kanal `app/reliability`).
class ReliabilityService {
  ReliabilityService(this.scheduler);

  final LocalNotificationAlarmScheduler scheduler;
  static const _channel = MethodChannel('app/reliability');

  Future<ReliabilityStatus> check() async {
    final notifications = await scheduler.notificationsEnabled();
    if (!Platform.isAndroid) {
      return ReliabilityStatus(
        notifications: notifications,
        exactAlarms: true,
        batteryUnrestricted: true,
        fullScreen: true,
        manufacturer: 'apple',
      );
    }
    final s = Map<String, Object?>.from(await _channel.invokeMethod<Map>('status') ?? const {});
    return ReliabilityStatus(
      notifications: notifications,
      exactAlarms: s['canScheduleExactAlarms'] as bool? ?? await scheduler.canScheduleExact(),
      batteryUnrestricted: s['ignoringBatteryOptimizations'] as bool? ?? false,
      fullScreen: s['canUseFullScreenIntent'] as bool? ?? true,
      manufacturer: (s['manufacturer'] as String? ?? '').toLowerCase(),
    );
  }

  Future<void> requestNotifications() => scheduler.requestNotifications();
  Future<void> requestExactAlarms() => scheduler.requestExactAlarms();
  Future<void> requestFullScreen() => scheduler.requestFullScreen();
  Future<void> openBatterySettings() => _channel.invokeMethod('openBatterySettings');
  Future<void> openOemAutostart() => _channel.invokeMethod('openOemAutostart');
}

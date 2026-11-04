import 'dart:io' show Platform;
import 'dart:typed_data';

import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_timezone/flutter_timezone.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

import 'alarm_scheduler.dart';

/// Bildirim/aksiyon metinleri (yerelleştirilmiş, arka plan izolatında da kurulabilir).
class NotificationLabels {
  const NotificationLabels({
    required this.take,
    required this.snooze,
    required this.skip,
    required this.criticalChannel,
    required this.criticalChannelDesc,
    required this.normalChannel,
    required this.infoChannel,
  });

  final String take;
  final String snooze;
  final String skip;
  final String criticalChannel;
  final String criticalChannelDesc;
  final String normalChannel;
  final String infoChannel;
}

class NotificationActions {
  NotificationActions._();
  static const take = 'take';
  static const snooze = 'snooze';
  static const skip = 'skip';
  static const darwinCategory = 'dose';
  static const darwinCategoryNoSnooze = 'dose_no_snooze';
}

Future<void> initTimeZone() async {
  tzdata.initializeTimeZones();
  try {
    final info = await FlutterTimezone.getLocalTimezone();
    tz.setLocalLocation(tz.getLocation(info.identifier));
  } on Exception {
    tz.setLocalLocation(tz.getLocation('Europe/Istanbul'));
  }
}

class LocalNotificationAlarmScheduler implements AlarmScheduler {
  LocalNotificationAlarmScheduler(this.plugin, this.labels);

  final FlutterLocalNotificationsPlugin plugin;
  NotificationLabels labels;

  static const criticalChannelId = 'dose_critical_v1';
  static const normalChannelId = 'dose_normal_v1';
  static const infoChannelId = 'info_v1';

  /// Android `Notification.FLAG_INSISTENT`: kullanıcı etkileşene kadar ses döngüde çalar.
  static const _flagInsistent = 4;

  @override
  int get pendingBudget => Platform.isIOS ? 60 : 400;

  @override
  int get repeatCap => Platform.isIOS ? 3 : AlarmIds.slots - 1;

  Future<void> initialize({
    required DidReceiveNotificationResponseCallback onForeground,
    required DidReceiveBackgroundNotificationResponseCallback onBackground,
  }) async {
    await plugin.initialize(
      settings: InitializationSettings(
        android: const AndroidInitializationSettings('@mipmap/ic_launcher'),
        iOS: DarwinInitializationSettings(
          requestAlertPermission: false,
          requestBadgePermission: false,
          requestSoundPermission: false,
          notificationCategories: [
            DarwinNotificationCategory(NotificationActions.darwinCategory, actions: [
              DarwinNotificationAction.plain(NotificationActions.take, labels.take),
              DarwinNotificationAction.plain(NotificationActions.snooze, labels.snooze),
              DarwinNotificationAction.plain(NotificationActions.skip, labels.skip,
                  options: {DarwinNotificationActionOption.destructive}),
            ]),
            DarwinNotificationCategory(NotificationActions.darwinCategoryNoSnooze, actions: [
              DarwinNotificationAction.plain(NotificationActions.take, labels.take),
              DarwinNotificationAction.plain(NotificationActions.skip, labels.skip,
                  options: {DarwinNotificationActionOption.destructive}),
            ]),
          ],
        ),
      ),
      onDidReceiveNotificationResponse: onForeground,
      onDidReceiveBackgroundNotificationResponse: onBackground,
    );
    await createChannels();
  }

  Future<void> createChannels() async {
    final android = plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    await android?.createNotificationChannel(AndroidNotificationChannel(
      criticalChannelId,
      labels.criticalChannel,
      description: labels.criticalChannelDesc,
      importance: Importance.max,
      audioAttributesUsage: AudioAttributesUsage.alarm, // sessiz modda da alarm ses seviyesinden
    ));
    await android?.createNotificationChannel(AndroidNotificationChannel(
      normalChannelId,
      labels.normalChannel,
      importance: Importance.high,
    ));
    await android?.createNotificationChannel(AndroidNotificationChannel(
      infoChannelId,
      labels.infoChannel,
      importance: Importance.defaultImportance,
    ));
  }

  @override
  Future<void> schedule(PlannedDose d) async {
    final when = tz.TZDateTime.from(d.fireAt, tz.local);
    final now = tz.TZDateTime.now(tz.local);
    final details = _details(d);
    final mode = d.isCritical ? AndroidScheduleMode.alarmClock : AndroidScheduleMode.exactAllowWhileIdle;

    if (when.isAfter(now)) {
      await plugin.zonedSchedule(
        id: AlarmIds.main(d.intId),
        title: d.title,
        body: d.body,
        scheduledDate: when,
        notificationDetails: details,
        androidScheduleMode: mode,
        payload: d.logId,
      );
    }
    if (!d.persistent) return;
    final repeats = d.maxRepeats < repeatCap ? d.maxRepeats : repeatCap;
    for (var k = 1; k <= repeats; k++) {
      final at = when.add(Duration(minutes: d.repeatEveryMinutes * k));
      if (!at.isAfter(now)) continue;
      await plugin.zonedSchedule(
        id: AlarmIds.repeat(d.intId, k),
        title: d.repeatTitle,
        body: d.body,
        scheduledDate: at,
        notificationDetails: details,
        androidScheduleMode: AndroidScheduleMode.alarmClock,
        payload: d.logId,
      );
    }
  }

  @override
  Future<void> cancelDose(int intId) async {
    for (var k = 0; k < AlarmIds.slots; k++) {
      await plugin.cancel(id: intId * AlarmIds.slots + k);
    }
  }

  @override
  Future<void> cancelId(int notificationId) => plugin.cancel(id: notificationId);

  @override
  Future<Set<int>> pendingIds() async =>
      (await plugin.pendingNotificationRequests()).map((r) => r.id).toSet();

  @override
  Future<void> showNow({required int id, required String title, required String body}) => plugin.show(
        id: id,
        title: title,
        body: body,
        notificationDetails: const NotificationDetails(
          android: AndroidNotificationDetails(infoChannelId, 'Info', importance: Importance.defaultImportance),
          iOS: DarwinNotificationDetails(),
        ),
      );

  NotificationDetails _details(PlannedDose d) => NotificationDetails(
        android: AndroidNotificationDetails(
          d.isCritical ? criticalChannelId : normalChannelId,
          d.isCritical ? labels.criticalChannel : labels.normalChannel,
          importance: d.isCritical ? Importance.max : Importance.high,
          priority: Priority.max,
          category: AndroidNotificationCategory.alarm,
          fullScreenIntent: d.isCritical,
          ongoing: d.isCritical,
          autoCancel: false,
          audioAttributesUsage: d.isCritical ? AudioAttributesUsage.alarm : AudioAttributesUsage.notification,
          additionalFlags: d.isCritical ? Int32List.fromList(const [_flagInsistent]) : null,
          styleInformation: BigTextStyleInformation(d.body),
          actions: [
            AndroidNotificationAction(NotificationActions.take, labels.take),
            if (d.canSnooze) AndroidNotificationAction(NotificationActions.snooze, labels.snooze),
            AndroidNotificationAction(NotificationActions.skip, labels.skip),
          ],
        ),
        iOS: DarwinNotificationDetails(
          categoryIdentifier:
              d.canSnooze ? NotificationActions.darwinCategory : NotificationActions.darwinCategoryNoSnooze,
          interruptionLevel: d.isCritical ? InterruptionLevel.timeSensitive : InterruptionLevel.active,
          threadIdentifier: 'dose',
        ),
      );

  // ───────────── İzinler ─────────────

  AndroidFlutterLocalNotificationsPlugin? get _android =>
      plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();

  Future<bool> notificationsEnabled() async {
    if (Platform.isIOS) {
      final ios = plugin.resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>();
      return (await ios?.checkPermissions())?.isEnabled ?? false;
    }
    return await _android?.areNotificationsEnabled() ?? false;
  }

  Future<bool> canScheduleExact() async =>
      Platform.isIOS || (await _android?.canScheduleExactNotifications() ?? false);

  Future<void> requestNotifications() async {
    await _android?.requestNotificationsPermission();
    await plugin
        .resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>()
        ?.requestPermissions(alert: true, badge: true, sound: true);
  }

  Future<void> requestExactAlarms() async => _android?.requestExactAlarmsPermission();
  Future<void> requestFullScreen() async => _android?.requestFullScreenIntentPermission();
}

import 'dart:ui';

import 'package:flutter/widgets.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

import 'data/app_database.dart';
import 'data/drug_catalog.dart';
import 'l10n/app_localizations.dart';
import 'services/alarm_planner.dart';
import 'services/dose_action_service.dart';
import 'services/local_notification_scheduler.dart';
import 'services/localized_texts.dart';
import 'services/reliability_service.dart';
import 'services/settings_store.dart';

/// Uygulamanın tüm servisleri. Hem UI izolatında hem de bildirim aksiyonlarının
/// çalıştığı arka plan izolatında aynı şekilde kurulur.
class AppServices {
  AppServices._({
    required this.db,
    required this.settings,
    required this.scheduler,
    required this.planner,
    required this.actions,
    required this.catalog,
    required this.reliability,
  });

  final AppDatabase db;
  final SettingsStore settings;
  final LocalNotificationAlarmScheduler scheduler;
  final AlarmPlanner planner;
  final DoseActionService actions;
  final DrugCatalogRepository catalog;
  final ReliabilityService reliability;

  /// Testlerde sahte zamanlayıcı/planlayıcı ile kurmak için.
  @visibleForTesting
  factory AppServices.forTest({
    required AppDatabase db,
    required SettingsStore settings,
    required AlarmPlanner planner,
    required DoseActionService actions,
  }) {
    final scheduler = LocalNotificationAlarmScheduler(
      FlutterLocalNotificationsPlugin(),
      notificationLabels(resolveLocalizations('tr')),
    );
    return AppServices._(
      db: db,
      settings: settings,
      scheduler: scheduler,
      planner: planner,
      actions: actions,
      catalog: DrugCatalogRepository(db),
      reliability: ReliabilityService(scheduler),
    );
  }

  static Future<AppServices> create() async {
    await initTimeZone();
    final settings = await SettingsStore.load();
    final t = resolveLocalizations(settings.read().languageCode);
    final db = await AppDatabase.open();
    final scheduler = LocalNotificationAlarmScheduler(FlutterLocalNotificationsPlugin(), notificationLabels(t));
    await scheduler.initialize(
      onForeground: _onForegroundResponse,
      onBackground: onBackgroundNotificationResponse,
    );
    final planner = AlarmPlanner(db: db, scheduler: scheduler, texts: LocalizedDoseTexts(t), clock: DateTime.now);
    final actions = DoseActionService(
      db: db,
      scheduler: scheduler,
      planner: planner,
      clock: DateTime.now,
      lowStockText: lowStockTexts(t),
    );
    return AppServices._(
      db: db,
      settings: settings,
      scheduler: scheduler,
      planner: planner,
      actions: actions,
      catalog: DrugCatalogRepository(db),
      reliability: ReliabilityService(scheduler),
    );
  }

  /// Dil değişince bildirim metinlerini ve kanal adlarını günceller, alarmları yeniden kurar.
  Future<void> applyLanguage(String? languageCode) async {
    final t = resolveLocalizations(languageCode);
    scheduler.labels = notificationLabels(t);
    planner.texts = LocalizedDoseTexts(t);
    actions.lowStockText = lowStockTexts(t);
    await scheduler.createChannels();
    await planner.rescheduleAll();
  }

  /// UI izolatı bildirim dokunuşlarını buradan dinler (router'a yönlendirme için).
  static void Function(NotificationResponse response)? foregroundHandler;

  static void _onForegroundResponse(NotificationResponse r) => foregroundHandler?.call(r);
}

AppLocalizations resolveLocalizations(String? languageCode) {
  final code = languageCode ?? PlatformDispatcher.instance.locale.languageCode;
  final supported = AppLocalizations.supportedLocales.map((l) => l.languageCode);
  return lookupAppLocalizations(Locale(supported.contains(code) ? code : 'tr'));
}

/// Uygulama kapalıyken "Aldım / Ertele / Atla" aksiyonları bu izolatta çalışır.
/// Firebase'e değil yerel veritabanına yazar; senkron outbox üzerinden sonra yapılır.
@pragma('vm:entry-point')
Future<void> onBackgroundNotificationResponse(NotificationResponse response) async {
  WidgetsFlutterBinding.ensureInitialized();
  DartPluginRegistrant.ensureInitialized();
  final services = await AppServices.create();
  try {
    await services.actions.handleNotificationAction(response.actionId, response.payload);
  } finally {
    await services.db.close();
  }
}

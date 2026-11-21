import 'dart:async';
import 'dart:convert';

import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart' show defaultTargetPlatform, TargetPlatform;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show MethodChannel;
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:geolocator/geolocator.dart' show Geolocator;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

import '../app_settings.dart';
import '../data/observances.dart';
import '../theme.dart';

/// Arka planda gelen "notification" mesajlarını sistem kendisi gösterir; burada iş yok.
/// (Top-level ve vm:entry-point olmalı.)
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {}

/// Bildirim izni durumu.
enum NotificationPermission { granted, denied, blocked }

/// Bildirime dokunulunca açılacak hedef.
sealed class NotificationTarget {
  const NotificationTarget();

  static NotificationTarget? fromData(Map<String, dynamic> data) {
    switch (data['type']) {
      case 'article':
        final url = data['url'] as String?;
        if (url == null || url.isEmpty) return null;
        return ArticleTarget(url: url, title: data['title'] as String? ?? '', source: data['source'] as String? ?? '');
      case 'match':
        final id = int.tryParse('${data['fixture_id']}');
        return id == null ? null : MatchTarget(id);
    }
    return null;
  }
}

class ArticleTarget extends NotificationTarget {
  const ArticleTarget({required this.url, required this.title, required this.source});
  final String url;
  final String title;
  final String source;
}

class MatchTarget extends NotificationTarget {
  const MatchTarget(this.fixtureId);
  final int fixtureId;
}

/// FCM + yerel bildirimler: kanallar, izin, konu abonelikleri ve dokunma yönlendirmesi.
///
/// Kanallar (sunucudaki `android.notification.channelId` ile aynı):
///  - news     : ilgi alanı haber özetleri
///  - breaking_news : Son Dakika (yüksek öncelik, telefonun varsayılan bildirim sesi)
///  - quiet    : gece 00:00–08:00 sessiz bildirimler (olağanüstü durumlar hariç)
///  - goal_alert : gol / gol iptali — telefonun bildirim sesi (kullanıcı kanal ayarından değiştirebilir)
///  - match    : maç başladı / devre / sonuç
class NotificationService {
  NotificationService._();

  static final instance = NotificationService._();

  final _local = FlutterLocalNotificationsPlugin();
  final _taps = StreamController<NotificationTarget>.broadcast();
  bool _ready = false;
  AppSettings? _settings;

  /// Bildirime dokunma olayları (MaterialApp bunu dinleyip ilgili sayfayı açar).
  Stream<NotificationTarget> get taps => _taps.stream;

  static const channels = <AndroidNotificationChannel>[
    AndroidNotificationChannel(
      'news',
      'Haber özetleri',
      description: 'İlgi alanlarındaki yeni haberler',
      importance: Importance.defaultImportance,
    ),
    AndroidNotificationChannel(
      'breaking_news',
      'Son Dakika',
      description: 'Son dakika haberleri (ses: telefonun bildirim sesi; buradan değiştirilebilir)',
      importance: Importance.high,
    ),
    // Gece 00:00–08:00: deprem, afet, savaş, olağanüstü hal dışındaki bildirimler sessiz gelir (sunucu seçer).
    AndroidNotificationChannel(
      'quiet',
      'Gece (sessiz)',
      description: '00:00–08:00 arası bildirimler sessiz ve titreşimsiz gelir',
      importance: Importance.low,
      playSound: false,
      enableVibration: false,
    ),
    AndroidNotificationChannel(
      'goal_alert',
      'Goller',
      description: 'Takip ettiğin takımların golleri (ses: telefonun bildirim sesi; buradan değiştirilebilir)',
      importance: Importance.max,
    ),
    AndroidNotificationChannel(
      'match',
      'Maç durumu',
      description: 'Maç başladı, devre arası ve maç sonucu',
      importance: Importance.high,
    ),
    AndroidNotificationChannel(
      'observances',
      'Bayram, kandil ve anma günleri',
      description: 'Bayramlarda kutlama, kandillerde tebrik, anma günlerinde anma bildirimi',
      importance: Importance.high,
    ),
  ];

  /// Uygulama açılışında bir kez çağrılır (Firebase başlatıldıktan sonra).
  Future<void> init(AppSettings settings) async {
    if (_ready) return;
    _ready = true;
    _settings = settings;

    FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);

    await _local.initialize(
      settings: const InitializationSettings(
        android: AndroidInitializationSettings('ic_stat_news'),
        // İzin Firebase Messaging ile istenir (requestPermission); burada sorulmaz.
        iOS: DarwinInitializationSettings(
          requestAlertPermission: false,
          requestBadgePermission: false,
          requestSoundPermission: false,
        ),
      ),
      onDidReceiveNotificationResponse: (r) => _handlePayload(r.payload),
    );
    final android = _local.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    for (final c in channels) {
      await android?.createNotificationChannel(c);
    }
    // Kanal sesi sonradan değiştirilemez (Android kuralı) → özel sesli eski kanallar silinir; yerine telefonun sesi.
    for (final old in const ['breaking', 'breaking_alert', 'goals']) {
      await android?.deleteNotificationChannel(channelId: old);
    }

    // Uygulama ön plandayken sistem bildirim göstermez → yerel bildirimle göster.
    FirebaseMessaging.onMessage.listen(_showForeground);
    FirebaseMessaging.onMessageOpenedApp.listen((m) => _emit(m.data));
    final initial = await FirebaseMessaging.instance.getInitialMessage();
    if (initial != null) {
      // İlk karenin çizilmesini bekle ki Navigator hazır olsun.
      WidgetsBinding.instance.addPostFrameCallback((_) => _emit(initial.data));
    }
    // Uygulamanın kendi kurduğu bildirimler (haber, canlı skor — Kotlin): dokunma verisi bu kanaldan gelir.
    const launchChannel = MethodChannel('akinai/launch');
    launchChannel.setMethodCallHandler((call) async {
      if (call.method == 'payload') _handlePayload(call.arguments as String?);
    });
    try {
      final native = await launchChannel.invokeMethod<String>('takePayload');
      if (native != null) WidgetsBinding.instance.addPostFrameCallback((_) => _handlePayload(native));
    } catch (_) {}
    // Bildirimdeki "Daha az bildirim" düğmesi ayarı uygulama dışında değiştirir → öne gelince yeniden oku.
    _lifecycle = AppLifecycleListener(onResume: () {
      settings.reloadFromDisk();
      // iOS: abonelik APNs jetonu gelmeden yapılamaz; eksik kaldıysa uygulama her öne geldiğinde tamamlanır.
      if (defaultTargetPlatform == TargetPlatform.iOS) syncTopics();
    });
    // iOS: jeton (izin verildikten sonra) geç gelebilir → geldiği an abone ol.
    if (defaultTargetPlatform == TargetPlatform.iOS) {
      FirebaseMessaging.instance.onRegistered.listen((_) => syncTopics());
    }

    final launch = await _local.getNotificationAppLaunchDetails();
    if (launch?.didNotificationLaunchApp ?? false) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _handlePayload(launch!.notificationResponse?.payload));
    }

    settings.addListener(syncTopics);
    settings.addListener(scheduleObservances);
    await syncTopics();
    await scheduleObservances();

    // Daha önce kurulmuş (ilgi alanlarını seçmiş) ama izni hiç sorulmamış kullanıcıya bir kez sor.
    if (settings.hasChosenInterests) {
      final prefs = await SharedPreferences.getInstance();
      if (!prefs.containsKey(_askedKey) && await permissionStatus() != NotificationPermission.granted) {
        WidgetsBinding.instance.addPostFrameCallback((_) => requestPermission());
      }
    }
  }

  // Uygulama boyunca yaşar (dinleyici çöp toplanmasın diye alanda tutulur).
  // ignore: unused_field
  AppLifecycleListener? _lifecycle;

  void _emit(Map<String, dynamic> data) {
    final target = NotificationTarget.fromData(data);
    if (target != null) _taps.add(target);
  }

  void _handlePayload(String? payload) {
    if (payload == null || payload.isEmpty) return;
    try {
      _emit(Map<String, dynamic>.from(jsonDecode(payload) as Map));
    } catch (_) {}
  }

  Future<void> _showForeground(RemoteMessage m) async {
    final n = m.notification;
    if (n == null) return;
    // iOS canlı skor: sistem tek bildirimi yerinde günceller; uygulama açıkken her güncellemeyi ayrıca gösterme.
    if (m.data['type'] == 'match_live') return;
    final channelId = n.android?.channelId ?? 'news';
    final channel = channels.firstWhere((c) => c.id == channelId, orElse: () => channels.first);
    await _local.show(
      id: (m.messageId ?? '${DateTime.now().millisecondsSinceEpoch}').hashCode,
      title: n.title,
      body: n.body,
      payload: jsonEncode(m.data),
      notificationDetails: NotificationDetails(
        android: AndroidNotificationDetails(
          channel.id,
          channel.name,
          channelDescription: channel.description,
          importance: channel.importance,
          priority: channel.importance.value >= Importance.high.value ? Priority.high : Priority.defaultPriority,
          icon: 'ic_stat_news',
          color: AppTheme.navy,
          sound: channel.sound,
          tag: n.android?.tag,
          styleInformation: BigTextStyleInformation(n.body ?? ''),
        ),
        iOS: const DarwinNotificationDetails(presentAlert: true, presentBanner: true, presentList: true, presentSound: true),
      ),
    );
  }

  // ── Bayram ve anma günleri (cihazda zamanlanır, sunucu gerekmez) ─────────
  static const _observanceIdMin = 5000;
  static const _observanceIdMax = 5999;
  String _scheduledKey = '';

  /// Önümüzdeki 12 ayın bayram/anma bildirimlerini İstanbul saatiyle zamanlar.
  /// "Yaklaşık zaman" modu: kesin alarm izni (Play'de yalnızca alarm/takvim uygulamalarına verilir) gerekmez;
  /// birkaç dakika sapabilir — bu yüzden 10 Kasım bildirimi 09.00'da gider ve 09.05'i hatırlatır.
  Future<void> scheduleObservances() async {
    final settings = _settings;
    if (settings == null) return;
    final english = (settings.locale ?? WidgetsBinding.instance.platformDispatcher.locale).languageCode == 'en';
    final key = '${settings.observanceNotifications}|$english|${DateTime.now().year}-${DateTime.now().month}';
    if (key == _scheduledKey) return; // ayar/dil/ay değişmediyse yeniden kurmaya gerek yok
    _scheduledKey = key;
    try {
      for (final p in await _local.pendingNotificationRequests()) {
        if (p.id >= _observanceIdMin && p.id <= _observanceIdMax) await _local.cancel(id: p.id);
      }
      if (!settings.observanceNotifications) return;
      tzdata.initializeTimeZones();
      final ist = tz.getLocation('Europe/Istanbul');
      final now = tz.TZDateTime.now(ist);
      final channel = channels.firstWhere((c) => c.id == 'observances');
      for (final o in upcomingObservances(DateTime(now.year, now.month, now.day, now.hour, now.minute))) {
        final d = o.date;
        await _local.zonedSchedule(
          id: o.notificationId,
          scheduledDate: tz.TZDateTime(ist, d.year, d.month, d.day, d.hour, d.minute),
          title: o.title(english: english),
          body: o.body(english: english),
          androidScheduleMode: AndroidScheduleMode.inexactAllowWhileIdle,
          notificationDetails: NotificationDetails(
            android: AndroidNotificationDetails(
              channel.id,
              channel.name,
              channelDescription: channel.description,
              importance: Importance.high,
              priority: Priority.high,
              icon: 'ic_stat_news',
              color: o.observance.color,
              styleInformation: BigTextStyleInformation(o.body(english: english)),
            ),
            iOS: const DarwinNotificationDetails(presentAlert: true, presentBanner: true, presentList: true, presentSound: true),
          ),
        );
      }
    } catch (e) {
      _scheduledKey = '';
      debugPrint('Bayram/anma bildirimleri zamanlanamadı: $e');
    }
  }

  // ── İzin ─────────────────────────────────────────────────────────────────
  // permission_handler yerine Firebase Messaging kullanılır (permission_handler'ın son sürümü
  // projenin desteklemediği compileSdk 37'yi istiyor). Android sistem penceresi iki kez
  // reddedilince bir daha gösterilmez; bunu "blocked" sayar ve uygulama ayarlarını açarız.
  static const _askedKey = 'notif_permission_asked';

  Future<NotificationPermission> permissionStatus() async {
    // Firebase başlatılamadıysa (ör. yapılandırma dosyası yok) ayarlar ekranı yine açılabilmeli.
    AuthorizationStatus st;
    try {
      st = (await FirebaseMessaging.instance.getNotificationSettings()).authorizationStatus;
    } catch (_) {
      return NotificationPermission.denied;
    }
    if (st == AuthorizationStatus.authorized || st == AuthorizationStatus.provisional) {
      return NotificationPermission.granted;
    }
    final prefs = await SharedPreferences.getInstance();
    return (prefs.getInt(_askedKey) ?? 0) >= 2 ? NotificationPermission.blocked : NotificationPermission.denied;
  }

  /// İzin ister; sistem penceresi artık açılmıyorsa uygulama ayarlarını açar.
  Future<NotificationPermission> requestPermission() async {
    final prefs = await SharedPreferences.getInstance();
    final asked = prefs.getInt(_askedKey) ?? 0;
    if (asked >= 2) {
      await Geolocator.openAppSettings(); // uygulama bilgi sayfası (bildirim anahtarı orada)
      return permissionStatus();
    }
    final st = (await FirebaseMessaging.instance.requestPermission()).authorizationStatus;
    if (st == AuthorizationStatus.authorized || st == AuthorizationStatus.provisional) {
      await prefs.remove(_askedKey);
      await syncTopics();
      return NotificationPermission.granted;
    }
    await prefs.setInt(_askedKey, asked + 1);
    return permissionStatus();
  }

  /// Uygulamanın sistem ayarları sayfası.
  static Future<void> openSystemSettings() => Geolocator.openAppSettings();

  // ── Konu abonelikleri ────────────────────────────────────────────────────
  bool _syncing = false;
  bool _again = false;

  static const _iosTopicsResetKey = 'ios_topics_reset_v2';
  bool _iosRegistered = false;

  /// iOS'ta konu aboneliği APNs jetonu gelmeden çalışmaz; kısa süre beklenir.
  Future<bool> _waitForApnsToken(FirebaseMessaging messaging) async {
    for (var i = 0; i < 10; i++) {
      if (await messaging.getAPNSToken() != null) return true;
      await Future<void>.delayed(const Duration(milliseconds: 500));
    }
    return false;
  }

  /// Ayarlara göre FCM konularına abone olur / abonelikten çıkar (yalnızca fark).
  Future<void> syncTopics() async {
    final settings = _settings;
    if (settings == null) return;
    if (_syncing) {
      _again = true;
      return;
    }
    _syncing = true;
    try {
      do {
        _again = false;
        final desired = settings.desiredTopics;
        var current = settings.subscribedTopics;
        final messaging = FirebaseMessaging.instance;
        if (defaultTargetPlatform == TargetPlatform.iOS) {
          if (!await _waitForApnsToken(messaging)) {
            // Bildirim izni yok ya da APNs henüz hazır değil: abonelik sonra (izin/öne gelme) yeniden denenir.
            return;
          }
          // Firebase iOS SDK 13: cihaz FCM'e açıkça kaydedilir (jeton artık kendiliğinden oluşmayabilir).
          // Oturum başına bir kez (register → onRegistered → syncTopics döngüsü olmasın).
          if (!_iosRegistered) {
            _iosRegistered = true;
            await messaging.register();
            // ignore: deprecated_member_use
            await messaging.getToken();
          }
          // İlk sürümler aboneliği sessizce kaçırmış olabilir → bir kez tüm konular yeniden kurulur.
          final prefs = await SharedPreferences.getInstance();
          if (prefs.getBool(_iosTopicsResetKey) != true) current = <String>{};
        }
        for (final t in desired.difference(current)) {
          await messaging.subscribeToTopic(t);
        }
        for (final t in current.difference(desired)) {
          await messaging.unsubscribeFromTopic(t);
        }
        await settings.setSubscribedTopics(desired);
        if (defaultTargetPlatform == TargetPlatform.iOS) {
          await (await SharedPreferences.getInstance()).setBool(_iosTopicsResetKey, true);
        }
      } while (_again);
    } catch (e, st) {
      debugPrint('Bildirim konuları güncellenemedi: $e');
      // Mağaza sürümünde görünür olsun (Crashlytics → ölümcül olmayan hata)
      try {
        FirebaseCrashlytics.instance.recordError(e, st, reason: 'Bildirim konu aboneliği');
      } catch (_) {}
    } finally {
      _syncing = false;
    }
  }
}

import 'dart:async';
import 'dart:convert';

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:geolocator/geolocator.dart' show Geolocator;
import 'package:shared_preferences/shared_preferences.dart';

import '../app_settings.dart';

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
///  - breaking : Son Dakika (yüksek öncelik)
///  - goals    : gol / gol iptali — özel ses `res/raw/goal_whistle.wav`
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
      'breaking',
      'Son Dakika',
      description: 'Son dakika haberleri',
      importance: Importance.high,
    ),
    AndroidNotificationChannel(
      'goals',
      'Goller',
      description: 'Takip ettiğin takımların golleri',
      importance: Importance.max,
      sound: RawResourceAndroidNotificationSound('goal_whistle'),
    ),
    AndroidNotificationChannel(
      'match',
      'Maç durumu',
      description: 'Maç başladı, devre arası ve maç sonucu',
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
      settings: const InitializationSettings(android: AndroidInitializationSettings('ic_stat_news')),
      onDidReceiveNotificationResponse: (r) => _handlePayload(r.payload),
    );
    final android = _local.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    for (final c in channels) {
      await android?.createNotificationChannel(c);
    }

    // Uygulama ön plandayken sistem bildirim göstermez → yerel bildirimle göster.
    FirebaseMessaging.onMessage.listen(_showForeground);
    FirebaseMessaging.onMessageOpenedApp.listen((m) => _emit(m.data));
    final initial = await FirebaseMessaging.instance.getInitialMessage();
    if (initial != null) {
      // İlk karenin çizilmesini bekle ki Navigator hazır olsun.
      WidgetsBinding.instance.addPostFrameCallback((_) => _emit(initial.data));
    }
    final launch = await _local.getNotificationAppLaunchDetails();
    if (launch?.didNotificationLaunchApp ?? false) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _handlePayload(launch!.notificationResponse?.payload));
    }

    settings.addListener(syncTopics);
    await syncTopics();
  }

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
          color: const Color(0xFF0A84FF),
          sound: channel.sound,
          tag: n.android?.tag,
          styleInformation: BigTextStyleInformation(n.body ?? ''),
        ),
      ),
    );
  }

  // ── İzin ─────────────────────────────────────────────────────────────────
  // permission_handler yerine Firebase Messaging kullanılır (permission_handler'ın son sürümü
  // projenin desteklemediği compileSdk 37'yi istiyor). Android sistem penceresi iki kez
  // reddedilince bir daha gösterilmez; bunu "blocked" sayar ve uygulama ayarlarını açarız.
  static const _askedKey = 'notif_permission_asked';

  Future<NotificationPermission> permissionStatus() async {
    final st = (await FirebaseMessaging.instance.getNotificationSettings()).authorizationStatus;
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
        final current = settings.subscribedTopics;
        final messaging = FirebaseMessaging.instance;
        for (final t in desired.difference(current)) {
          await messaging.subscribeToTopic(t);
        }
        for (final t in current.difference(desired)) {
          await messaging.unsubscribeFromTopic(t);
        }
        await settings.setSubscribedTopics(desired);
      } while (_again);
    } catch (e) {
      debugPrint('Bildirim konuları güncellenemedi: $e');
    } finally {
      _syncing = false;
    }
  }
}

import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/foundation.dart';

/// Uzaktan ayarlar: Firestore `app_config/main` belgesi (Firebase Console'dan düzenlenir, uygulama
/// güncellemesi gerekmez). Belge yoksa veya bir alan eksikse varsayılan değer kullanılır.
///
/// ```
/// app_config/main {
///   show_live_scores: true,     // canlı skor şeridi
///   show_market: true,          // sağ menüdeki piyasa paneli
///   show_briefing: true,        // "5 dakikada gündem" kartı
///   ads_enabled: true,          // tüm reklamlar
///   announcement: "",           // ana sayfada duyuru şeridi (boşsa gizli)
///   announcement_url: "",       // duyuruya dokununca açılacak adres (isteğe bağlı)
///   announcement_level: "info", // info | warning
///   min_build: 0,               // bu derleme numarasından eskiler "güncelle" uyarısı görür
///   update_url: "",             // güncelleme bağlantısı (Play Store sayfası)
///   live_stale_minutes: 20,     // skoru bu kadar dakikadır güncellenmeyen canlı maç gizlenir
/// }
/// ```
@immutable
class RemoteConfig {
  const RemoteConfig({
    this.showLiveScores = true,
    this.showMarket = true,
    this.showBriefing = true,
    this.adsEnabled = true,
    this.announcement = '',
    this.announcementUrl = '',
    this.announcementWarning = false,
    this.minBuild = 0,
    this.updateUrl = '',
    this.liveStaleMinutes = 20,
  });

  factory RemoteConfig.fromMap(Map<String, dynamic>? d) {
    if (d == null) return const RemoteConfig();
    bool flag(String k, bool def) => d[k] is bool ? d[k] as bool : def;
    String text(String k) => (d[k] is String ? d[k] as String : '').trim();
    int number(String k, int def) => d[k] is num ? (d[k] as num).toInt() : def;
    return RemoteConfig(
      showLiveScores: flag('show_live_scores', true),
      showMarket: flag('show_market', true),
      showBriefing: flag('show_briefing', true),
      adsEnabled: flag('ads_enabled', true),
      announcement: text('announcement'),
      announcementUrl: text('announcement_url'),
      announcementWarning: text('announcement_level') == 'warning',
      minBuild: number('min_build', 0),
      updateUrl: text('update_url'),
      liveStaleMinutes: number('live_stale_minutes', 20).clamp(5, 240),
    );
  }

  final bool showLiveScores;
  final bool showMarket;
  final bool showBriefing;
  final bool adsEnabled;
  final String announcement;
  final String announcementUrl;
  final bool announcementWarning;
  final int minBuild;
  final String updateUrl;
  final int liveStaleMinutes;

  /// Bu derleme güncellenmeli mi?
  bool needsUpdate(int build) => minBuild > 0 && build > 0 && build < minBuild;
}

/// `app_config/main` belgesini canlı dinler; son değeri tutar.
class RemoteConfigService extends ValueNotifier<RemoteConfig> {
  RemoteConfigService._() : super(const RemoteConfig());

  static final instance = RemoteConfigService._();

  StreamSubscription<DocumentSnapshot<Map<String, dynamic>>>? _sub;

  /// Firebase başlatıldıktan sonra bir kez çağrılır. Hata olursa varsayılanlar geçerli kalır.
  void start({FirebaseFirestore? firestore}) {
    if (_sub != null) return;
    _sub = (firestore ?? FirebaseFirestore.instance)
        .doc('app_config/main')
        .snapshots()
        .listen((d) => value = RemoteConfig.fromMap(d.data()), onError: (Object e) => debugPrint('Uzaktan ayar: $e'));
  }

  /// Testler için.
  @visibleForTesting
  void debugSet(RemoteConfig config) => value = config;
}

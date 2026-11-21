import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Ana akıştaki haber dizilimi.
enum FeedLayout {
  /// İlk haber büyük kart, diğerleri sağda küçük görselli liste (varsayılan)
  standard,

  /// Her haber büyük görselli kart
  large,

  /// Görselsiz, yalnızca başlık ve kaynak (en çok haber sığar)
  compact,
}

/// Kullanıcı tercihleri: dil, tema ve ilgi alanları (sıralı kategori kimlikleri).
/// SharedPreferences'ta, yalnızca cihazda saklanır.
class AppSettings extends ChangeNotifier {
  AppSettings._(this._prefs);

  static const _languageKey = 'language';
  static const _themeKey = 'theme_mode';
  static const _interestsKey = 'interests';
  static const _newsNotifKey = 'notify_news';
  static const _matchNotifKey = 'notify_matches';
  /// Yerel (Kotlin) "Daha az bildirim" düğmesi de bu anahtarı yazar: FlutterSharedPreferences → flutter.notif_level
  static const notifLevelKey = 'notif_level';
  static const _bulletinKey = 'notify_bulletins';
  static const _weatherKey = 'weather_card';
  static const _teamsKey = 'followed_teams';
  static const _topicsKey = 'subscribed_topics';
  static const _layoutKey = 'feed_layout';
  static const _observanceKey = 'notify_observances';
  static const _quakeKey = 'notify_earthquakes';
  // Android tarafı (LiveScoreNotifier.kt) aynı anahtarı "flutter.liveScorePanel" olarak okur — adı değiştirme.
  static const _liveScoreKey = 'liveScorePanel';
  static const _recentSearchKey = 'recent_searches';

  /// Futbol takım seçimi bu ilgi alanına bağlıdır.
  static const footballCategoryId = 'futbol';

  /// Takip edilebilecek en fazla takım (Firestore arrayContainsAny sınırı 10).
  static const maxTeams = 10;
  static const supportedLocales = [Locale('tr'), Locale('en')];

  /// İlk açılışta önerilen ilgi alanları (kullanıcı değiştirebilir).
  static const defaultInterests = ['son-dakika', 'gundem', 'ekonomi', 'spor', 'teknoloji'];

  final SharedPreferences _prefs;

  static Future<AppSettings> load() async => AppSettings._(await SharedPreferences.getInstance());

  // ── Dil ──────────────────────────────────────────────────────────────────
  Locale? get locale {
    final code = _prefs.getString(_languageKey);
    return code == null ? null : Locale(code);
  }

  Future<void> setLocale(Locale? value) async {
    if (value == null) {
      await _prefs.remove(_languageKey);
    } else {
      await _prefs.setString(_languageKey, value.languageCode);
    }
    notifyListeners();
  }

  // ── Tema ─────────────────────────────────────────────────────────────────
  ThemeMode get themeMode => switch (_prefs.getString(_themeKey)) {
    'light' => ThemeMode.light,
    'dark' => ThemeMode.dark,
    _ => ThemeMode.system,
  };

  Future<void> setThemeMode(ThemeMode mode) async {
    if (mode == ThemeMode.system) {
      await _prefs.remove(_themeKey);
    } else {
      await _prefs.setString(_themeKey, mode.name);
    }
    notifyListeners();
  }

  // ── İlgi alanları ────────────────────────────────────────────────────────
  /// Kullanıcı hiç seçim yapmadıysa false (ilk açılışta seçim ekranı gösterilir).
  bool get hasChosenInterests => _prefs.containsKey(_interestsKey);

  /// Takip edilen kategorilerin sıralı kimlikleri. Seçim yapılmadıysa önerilenler.
  List<String> get interests => _prefs.getStringList(_interestsKey) ?? List.of(defaultInterests);

  Future<void> setInterests(List<String> ids) async {
    await _prefs.setStringList(_interestsKey, List.unmodifiable(ids));
    notifyListeners();
  }

  // ── Bildirimler ──────────────────────────────────────────────────────────
  /// İlgi alanı haber bildirimleri (varsayılan açık; sistem izni ayrıca gerekir).
  bool get newsNotifications => _prefs.getBool(_newsNotifKey) ?? true;
  Future<void> setNewsNotifications(bool v) async {
    await _prefs.setBool(_newsNotifKey, v);
    notifyListeners();
  }

  /// Bildirim yoğunluğu (sunucu bu seviyeye göre gönderir). Varsayılan: Dengeli.
  NotifLevel get notifLevel =>
      NotifLevel.values.firstWhere((l) => l.name == _prefs.getString(notifLevelKey), orElse: () => NotifLevel.balanced);
  Future<void> setNotifLevel(NotifLevel v) async {
    await _prefs.setString(notifLevelKey, v.name);
    notifyListeners();
  }

  /// Deprem uyarıları (M≥4,5, sunucu 'deprem' konusuna gönderir). Varsayılan açık.
  bool get earthquakeAlerts => _prefs.getBool(_quakeKey) ?? true;
  Future<void> setEarthquakeAlerts(bool v) async {
    await _prefs.setBool(_quakeKey, v);
    notifyListeners();
  }

  /// Sabah ve akşam "5 dakikada gündem".
  bool get bulletins => _prefs.getBool(_bulletinKey) ?? true;
  Future<void> setBulletins(bool v) async {
    await _prefs.setBool(_bulletinKey, v);
    notifyListeners();
  }

  /// Bildirim düğmesi gibi uygulama dışından yapılan değişiklikleri okur (uygulama öne gelince).
  Future<void> reloadFromDisk() async {
    final before = notifLevel;
    await _prefs.reload();
    if (notifLevel != before) notifyListeners();
  }

  /// Takip edilen takımların maç/gol bildirimleri.
  bool get matchNotifications => _prefs.getBool(_matchNotifKey) ?? true;
  Future<void> setMatchNotifications(bool v) async {
    await _prefs.setBool(_matchNotifKey, v);
    notifyListeners();
  }

  // ── Konum / hava durumu ──────────────────────────────────────────────────
  /// Ana ekrandaki hava durumu kartı (konum izni verildiğinde açılır).
  bool get weatherEnabled => _prefs.getBool(_weatherKey) ?? false;
  Future<void> setWeatherEnabled(bool v) async {
    await _prefs.setBool(_weatherKey, v);
    notifyListeners();
  }

  // ── Futbol ───────────────────────────────────────────────────────────────
  bool get followsFootball => interests.contains(footballCategoryId);

  /// Takip edilen takım kimlikleri (`galatasaray`, `besiktas` …).
  List<String> get followedTeams => _prefs.getStringList(_teamsKey) ?? const [];

  Future<void> setFollowedTeams(List<String> ids) async {
    await _prefs.setStringList(_teamsKey, ids.take(maxTeams).toList());
    notifyListeners();
  }

  /// Bildirim konuları: ilgi alanları → `news_<id>`, seviye → `lvl_*` (+ `calm`), bülten, takımlar → `team_<id>`.
  Set<String> get desiredTopics => {
    if (newsNotifications) ...{
      for (final id in interests) 'news_$id',
      ...notifLevel.topics,
      if (bulletins) 'bulletin',
    },
    if (matchNotifications && followsFootball)
      for (final t in followedTeams) 'team_$t',
    // Türkiye milli maçları: futbol ilgisi seçili ve canlı skor paneli açık olanlara (sunucu: football.js NATIONAL_TOPIC)
    if (followsFootball && liveScorePanel) 'national_tr',
    if (earthquakeAlerts) 'deprem',
  };

  /// En son abone olunan konular (fark hesaplamak için; NotificationService kullanır).
  Set<String> get subscribedTopics => (_prefs.getStringList(_topicsKey) ?? const []).toSet();
  Future<void> setSubscribedTopics(Set<String> topics) => _prefs.setStringList(_topicsKey, topics.toList());

  // ── Haber dizilimi ───────────────────────────────────────────────────────
  /// Sade: iki dizilim (standart / kompakt). Eski "büyük kart" seçimi standarda döner.
  FeedLayout get feedLayout {
    final l = FeedLayout.values.firstWhere((l) => l.name == _prefs.getString(_layoutKey), orElse: () => FeedLayout.standard);
    return l == FeedLayout.large ? FeedLayout.standard : l;
  }

  Future<void> setFeedLayout(FeedLayout layout) async {
    await _prefs.setString(_layoutKey, layout.name);
    notifyListeners();
  }

  // ── Son aramalar ─────────────────────────────────────────────────────────
  List<String> get recentSearches => _prefs.getStringList(_recentSearchKey) ?? const [];

  Future<void> addRecentSearch(String q) async {
    final t = q.trim();
    if (t.isEmpty) return;
    final list = [t, ...recentSearches.where((e) => e.toLowerCase() != t.toLowerCase())].take(8).toList();
    await _prefs.setStringList(_recentSearchKey, list);
  }

  Future<void> clearRecentSearches() => _prefs.remove(_recentSearchKey);

  // ── Bayram ve anma günleri ───────────────────────────────────────────────
  /// Resmi bayramlarda kutlama, anma günlerinde anma bildirimi. Haber uygulamasında beklenmediği için varsayılan
  /// KAPALI (açmış kullanıcının seçimi korunur).
  bool get observanceNotifications => _prefs.getBool(_observanceKey) ?? false;
  Future<void> setObservanceNotifications(bool v) async {
    await _prefs.setBool(_observanceKey, v);
    notifyListeners();
  }

  /// Maç boyunca bildirim alanında sabit, kendini güncelleyen canlı skor paneli (Android).
  bool get liveScorePanel => _prefs.getBool(_liveScoreKey) ?? true;
  Future<void> setLiveScorePanel(bool v) async {
    await _prefs.setBool(_liveScoreKey, v);
    notifyListeners();
  }
}

/// Bildirim yoğunluğu → abone olunacak seviye konuları (sunucu: src/notify.js).
enum NotifLevel {
  important(['lvl_important', 'calm']),
  balanced(['lvl_balanced', 'calm']),
  all(['lvl_all']);

  const NotifLevel(this.topics);
  final List<String> topics;
}

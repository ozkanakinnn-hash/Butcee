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
  static const _weatherKey = 'weather_card';
  static const _teamsKey = 'followed_teams';
  static const _topicsKey = 'subscribed_topics';
  static const _layoutKey = 'feed_layout';
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

  /// Takip edilen takımların API-Football kimlikleri.
  List<int> get followedTeams =>
      (_prefs.getStringList(_teamsKey) ?? const []).map(int.tryParse).whereType<int>().toList();

  Future<void> setFollowedTeams(List<int> ids) async {
    await _prefs.setStringList(_teamsKey, ids.take(maxTeams).map((e) => '$e').toList());
    notifyListeners();
  }

  /// Bildirim konuları: ilgi alanları → `news_<id>`, takımlar → `team_<id>` (ilgili ayar açıksa).
  Set<String> get desiredTopics => {
    if (newsNotifications)
      for (final id in interests) 'news_$id',
    if (matchNotifications && followsFootball)
      for (final t in followedTeams) 'team_$t',
  };

  /// En son abone olunan konular (fark hesaplamak için; NotificationService kullanır).
  Set<String> get subscribedTopics => (_prefs.getStringList(_topicsKey) ?? const []).toSet();
  Future<void> setSubscribedTopics(Set<String> topics) => _prefs.setStringList(_topicsKey, topics.toList());

  // ── Haber dizilimi ───────────────────────────────────────────────────────
  FeedLayout get feedLayout =>
      FeedLayout.values.firstWhere((l) => l.name == _prefs.getString(_layoutKey), orElse: () => FeedLayout.standard);

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
}

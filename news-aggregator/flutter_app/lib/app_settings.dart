import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Kullanıcı tercihleri: dil, tema ve ilgi alanları (sıralı kategori kimlikleri).
/// SharedPreferences'ta, yalnızca cihazda saklanır.
class AppSettings extends ChangeNotifier {
  AppSettings._(this._prefs);

  static const _languageKey = 'language';
  static const _themeKey = 'theme_mode';
  static const _interestsKey = 'interests';
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
}

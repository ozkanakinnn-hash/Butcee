import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Kullanıcının dil tercihi (null = sistem dili). SharedPreferences'ta saklanır.
class AppSettings extends ChangeNotifier {
  AppSettings._(this._prefs);

  static const _languageKey = 'language';
  static const supportedLocales = [Locale('tr'), Locale('en')];

  final SharedPreferences _prefs;

  static Future<AppSettings> load() async => AppSettings._(await SharedPreferences.getInstance());

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
}

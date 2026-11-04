import 'dart:convert';
import 'dart:math';

import 'package:crypto/crypto.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AppSettings {
  const AppSettings({
    this.languageCode,
    this.seniorMode = false,
    this.textScale = 1.3,
    this.ttsOnAlarm = true,
    this.caregiverPhone,
    this.pinHash,
  });

  /// null = sistem dili.
  final String? languageCode;
  final bool seniorMode;
  final double textScale;
  final bool ttsOnAlarm;
  final String? caregiverPhone;
  final String? pinHash;

  bool get hasPin => pinHash != null;

  AppSettings copyWith({
    String? Function()? languageCode,
    bool? seniorMode,
    double? textScale,
    bool? ttsOnAlarm,
    String? Function()? caregiverPhone,
    String? Function()? pinHash,
  }) =>
      AppSettings(
        languageCode: languageCode != null ? languageCode() : this.languageCode,
        seniorMode: seniorMode ?? this.seniorMode,
        textScale: textScale ?? this.textScale,
        ttsOnAlarm: ttsOnAlarm ?? this.ttsOnAlarm,
        caregiverPhone: caregiverPhone != null ? caregiverPhone() : this.caregiverPhone,
        pinHash: pinHash != null ? pinHash() : this.pinHash,
      );
}

/// Kalıcı ayarlar (SharedPreferences). Arka plan izolatında da okunabilir.
class SettingsStore {
  SettingsStore(this._prefs);
  final SharedPreferences _prefs;

  static Future<SettingsStore> load() async => SettingsStore(await SharedPreferences.getInstance());

  AppSettings read() => AppSettings(
        languageCode: _prefs.getString('languageCode'),
        seniorMode: _prefs.getBool('seniorMode') ?? false,
        textScale: _prefs.getDouble('textScale') ?? 1.3,
        ttsOnAlarm: _prefs.getBool('ttsOnAlarm') ?? true,
        caregiverPhone: _prefs.getString('caregiverPhone'),
        pinHash: _prefs.getString('pinHash'),
      );

  Future<void> write(AppSettings s) async {
    await _setOrClear('languageCode', s.languageCode);
    await _prefs.setBool('seniorMode', s.seniorMode);
    await _prefs.setDouble('textScale', s.textScale);
    await _prefs.setBool('ttsOnAlarm', s.ttsOnAlarm);
    await _setOrClear('caregiverPhone', s.caregiverPhone);
    await _setOrClear('pinHash', s.pinHash);
  }

  Future<void> _setOrClear(String key, String? value) async {
    if (value == null) {
      await _prefs.remove(key);
    } else {
      await _prefs.setString(key, value);
    }
  }

  /// "tuz:sha256(tuz+pin)" — yerel ayar kilidi içindir, güvenlik sınırı değildir.
  static String hashPin(String pin, {String? salt}) {
    final s = salt ?? base64Url.encode(List.generate(12, (_) => Random.secure().nextInt(256)));
    return '$s:${sha256.convert(utf8.encode('$s$pin'))}';
  }

  static bool verifyPin(String pin, String stored) {
    final i = stored.indexOf(':');
    if (i < 0) return false;
    return hashPin(pin, salt: stored.substring(0, i)) == stored;
  }
}

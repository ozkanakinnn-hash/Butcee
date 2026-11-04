import 'dart:convert';
import 'dart:math';

import 'package:crypto/crypto.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Hatırlatma dilimleri ve varsayılan saatleri.
const defaultSlotTimes = <String, String>{
  'earlyMorning': '07:00',
  'lateMorning': '09:00',
  'noon': '13:00',
  'afternoon': '16:00',
  'evening': '19:00',
  'lateEvening': '22:00',
};

class AppSettings {
  const AppSettings({
    this.remindersEnabled = true,
    this.slotTimes = defaultSlotTimes,
    this.mutedSlots = const {},
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

  final bool remindersEnabled;

  /// dilim kimliği → "SS:dd"
  final Map<String, String> slotTimes;
  final Set<String> mutedSlots;

  String slotTime(String slot) => slotTimes[slot] ?? defaultSlotTimes[slot] ?? '09:00';
  bool isSlotMuted(String? slot) => !remindersEnabled || (slot != null && mutedSlots.contains(slot));

  AppSettings copyWith({
    String? Function()? languageCode,
    bool? seniorMode,
    double? textScale,
    bool? ttsOnAlarm,
    String? Function()? caregiverPhone,
    String? Function()? pinHash,
    bool? remindersEnabled,
    Map<String, String>? slotTimes,
    Set<String>? mutedSlots,
  }) =>
      AppSettings(
        remindersEnabled: remindersEnabled ?? this.remindersEnabled,
        slotTimes: slotTimes ?? this.slotTimes,
        mutedSlots: mutedSlots ?? this.mutedSlots,
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
        remindersEnabled: _prefs.getBool('remindersEnabled') ?? true,
        slotTimes: {
          ...defaultSlotTimes,
          ...((jsonDecode(_prefs.getString('slotTimes') ?? '{}') as Map).cast<String, String>()),
        },
        mutedSlots: (_prefs.getStringList('mutedSlots') ?? const []).toSet(),
      );

  Future<void> write(AppSettings s) async {
    await _setOrClear('languageCode', s.languageCode);
    await _prefs.setBool('seniorMode', s.seniorMode);
    await _prefs.setDouble('textScale', s.textScale);
    await _prefs.setBool('ttsOnAlarm', s.ttsOnAlarm);
    await _setOrClear('caregiverPhone', s.caregiverPhone);
    await _setOrClear('pinHash', s.pinHash);
    await _prefs.setBool('remindersEnabled', s.remindersEnabled);
    await _prefs.setString('slotTimes', jsonEncode(s.slotTimes));
    await _prefs.setStringList('mutedSlots', s.mutedSlots.toList());
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

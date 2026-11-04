import 'dart:convert';

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
    this.ttsOnAlarm = true,
  });

  /// null = sistem dili.
  final String? languageCode;
  final bool ttsOnAlarm;

  final bool remindersEnabled;

  /// dilim kimliği → "SS:dd"
  final Map<String, String> slotTimes;
  final Set<String> mutedSlots;

  String slotTime(String slot) => slotTimes[slot] ?? defaultSlotTimes[slot] ?? '09:00';
  bool isSlotMuted(String? slot) => !remindersEnabled || (slot != null && mutedSlots.contains(slot));

  AppSettings copyWith({
    String? Function()? languageCode,
    bool? ttsOnAlarm,
    bool? remindersEnabled,
    Map<String, String>? slotTimes,
    Set<String>? mutedSlots,
  }) =>
      AppSettings(
        remindersEnabled: remindersEnabled ?? this.remindersEnabled,
        slotTimes: slotTimes ?? this.slotTimes,
        mutedSlots: mutedSlots ?? this.mutedSlots,
        languageCode: languageCode != null ? languageCode() : this.languageCode,
        ttsOnAlarm: ttsOnAlarm ?? this.ttsOnAlarm,
      );
}

/// Kalıcı ayarlar (SharedPreferences). Arka plan izolatında da okunabilir.
class SettingsStore {
  SettingsStore(this._prefs);
  final SharedPreferences _prefs;

  static Future<SettingsStore> load() async => SettingsStore(await SharedPreferences.getInstance());

  AppSettings read() => AppSettings(
        languageCode: _prefs.getString('languageCode'),
        ttsOnAlarm: _prefs.getBool('ttsOnAlarm') ?? true,
        remindersEnabled: _prefs.getBool('remindersEnabled') ?? true,
        slotTimes: {
          ...defaultSlotTimes,
          ...((jsonDecode(_prefs.getString('slotTimes') ?? '{}') as Map).cast<String, String>()),
        },
        mutedSlots: (_prefs.getStringList('mutedSlots') ?? const []).toSet(),
      );

  Future<void> write(AppSettings s) async {
    await _setOrClear('languageCode', s.languageCode);
    await _prefs.setBool('ttsOnAlarm', s.ttsOnAlarm);
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

  /// Eski sürümlerden kalan, artık kullanılmayan ayarları siler.
  Future<void> removeLegacyKeys() async {
    for (final k in const ['seniorMode', 'textScale', 'caregiverPhone', 'pinHash']) {
      await _prefs.remove(k);
    }
  }
}

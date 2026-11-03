package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class AppThemeMode {
  LIGHT,
  DARK,
  SYSTEM
}

/** Kullanıcının seçebileceği üç renk teması (açık + koyu varyantlı). */
enum class AppColorTheme(val title: String, val subtitle: String, val swatches: List<Long>) {
  SARAY("Saray", "Krem, bej ve Osmanlı altını", listOf(0xFFFDFBF7, 0xFF9E6B20, 0xFF3F634B)),
  IZNIK("İznik Çinisi", "Kobalt mavisi, turkuaz ve mercan kırmızısı", listOf(0xFFFAFAF6, 0xFF1A3F7A, 0xFF2A8C9C, 0xFFC0392B)),
  ZUMRUT("Zümrüt", "Zümrüt yeşili ve altın, Ravza esintisi", listOf(0xFFF5F8F3, 0xFF1E6B4F, 0xFFC9A227))
}

enum class IftarSoundChoice(val title: String, val subtitle: String, val iconEmoji: String) {
  RAMAZAN_TOPU("Ramazan Topu Sesi", "İftar vakti için net ve gür geleneksel Ramazan topu", "💥"),
  SUFI_NEY("Huzur Veren Ney Sesi", "Tüm bildirim ve vakit hatırlatıcıları için saf, dinlendirici ney", "🎶")
}

class AppPreferences(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("ramazan_rehberi_prefs", Context.MODE_PRIVATE)

  var isOnboardingCompleted: Boolean
    get() = prefs.getBoolean("key_onboarding_completed", false)
    set(value) = prefs.edit().putBoolean("key_onboarding_completed", value).apply()

  var userName: String
    get() = prefs.getString("key_user_name", "") ?: ""
    set(value) = prefs.edit().putString("key_user_name", value).apply()

  var country: String
    get() = prefs.getString("key_country", "Türkiye") ?: "Türkiye"
    set(value) = prefs.edit().putString("key_country", value).apply()

  var province: String
    get() = prefs.getString("key_province", "Manisa") ?: "Manisa"
    set(value) = prefs.edit().putString("key_province", value).apply()

  var district: String
    get() = prefs.getString("key_district", "Saruhanlı") ?: "Saruhanlı"
    set(value) = prefs.edit().putString("key_district", value).apply()

  var themeMode: AppThemeMode
    get() {
      val name = prefs.getString("key_theme_mode", AppThemeMode.SYSTEM.name)
      return try {
        AppThemeMode.valueOf(name ?: AppThemeMode.SYSTEM.name)
      } catch (e: Exception) {
        AppThemeMode.SYSTEM
      }
    }
    set(value) = prefs.edit().putString("key_theme_mode", value.name).apply()

  var colorTheme: AppColorTheme
    get() {
      val name = prefs.getString("key_color_theme", AppColorTheme.SARAY.name)
      return AppColorTheme.entries.firstOrNull { it.name == name } ?: AppColorTheme.SARAY
    }
    set(value) = prefs.edit().putString("key_color_theme", value.name).apply()

  /** Test için bayram süslemesi önizlemesi: "", "RAMAZAN" veya "KURBAN". */
  var festivalPreview: String
    get() = prefs.getString("key_festival_preview", "") ?: ""
    set(value) = prefs.edit().putString("key_festival_preview", value).apply()

  /** Uygulama dili: "tr" veya "en". */
  var language: String
    get() = prefs.getString("key_language", "tr") ?: "tr"
    set(value) = prefs.edit().putString("key_language", value).apply()

  var notificationsEnabled: Boolean
    get() = prefs.getBoolean("key_notif_enabled", false)
    set(value) = prefs.edit().putBoolean("key_notif_enabled", value).apply()

  // 5 Vakit Namaz Bildirim Onayları
  var notifyImsak: Boolean
    get() = prefs.getBoolean("key_notify_imsak", true)
    set(value) = prefs.edit().putBoolean("key_notify_imsak", value).apply()

  var notifyOgle: Boolean
    get() = prefs.getBoolean("key_notify_ogle", true)
    set(value) = prefs.edit().putBoolean("key_notify_ogle", value).apply()

  var notifyIkindi: Boolean
    get() = prefs.getBoolean("key_notify_ikindi", true)
    set(value) = prefs.edit().putBoolean("key_notify_ikindi", value).apply()

  var notifyIftar: Boolean
    get() = prefs.getBoolean("key_notify_iftar", true)
    set(value) = prefs.edit().putBoolean("key_notify_iftar", value).apply()

  var notifyYatsi: Boolean
    get() = prefs.getBoolean("key_notify_yatsi", true)
    set(value) = prefs.edit().putBoolean("key_notify_yatsi", value).apply()

  // Vaktinden Önce Hatırlatıcılar & Dinamik Dakika Barları
  var remindBeforeImsak: Boolean
    get() = prefs.getBoolean("key_remind_before_imsak", false)
    set(value) = prefs.edit().putBoolean("key_remind_before_imsak", value).apply()

  var remindBeforeImsakMinutes: Int
    get() = prefs.getInt("key_remind_before_imsak_min", 45) // Sahur için varsayılan 45 dk
    set(value) = prefs.edit().putInt("key_remind_before_imsak_min", value).apply()

  var remindBeforeOgle: Boolean
    get() = prefs.getBoolean("key_remind_before_ogle", false)
    set(value) = prefs.edit().putBoolean("key_remind_before_ogle", value).apply()

  var remindBeforeOgleMinutes: Int
    get() = prefs.getInt("key_remind_before_ogle_min", 15)
    set(value) = prefs.edit().putInt("key_remind_before_ogle_min", value).apply()

  var remindBeforeIkindi: Boolean
    get() = prefs.getBoolean("key_remind_before_ikindi", false)
    set(value) = prefs.edit().putBoolean("key_remind_before_ikindi", value).apply()

  var remindBeforeIkindiMinutes: Int
    get() = prefs.getInt("key_remind_before_ikindi_min", 15)
    set(value) = prefs.edit().putInt("key_remind_before_ikindi_min", value).apply()

  var remindBeforeIftar: Boolean
    get() = prefs.getBoolean("key_remind_before_iftar", true)
    set(value) = prefs.edit().putBoolean("key_remind_before_iftar", value).apply()

  var remindBeforeIftarMinutes: Int
    get() = prefs.getInt("key_remind_before_iftar_min", 15) // İftar öncesi varsayılan 15 dk
    set(value) = prefs.edit().putInt("key_remind_before_iftar_min", value).apply()

  var remindBeforeYatsi: Boolean
    get() = prefs.getBoolean("key_remind_before_yatsi", false)
    set(value) = prefs.edit().putBoolean("key_remind_before_yatsi", value).apply()

  var remindBeforeYatsiMinutes: Int
    get() = prefs.getInt("key_remind_before_yatsi_min", 15)
    set(value) = prefs.edit().putInt("key_remind_before_yatsi_min", value).apply()

  // Hatırlatıcıların cihaz yeniden başlasa da kurulabilmesi için son bilinen vakitler
  var lastImsak: String
    get() = prefs.getString("key_last_imsak", "05:45") ?: "05:45"
    set(value) = prefs.edit().putString("key_last_imsak", value).apply()

  var lastAksam: String
    get() = prefs.getString("key_last_aksam", "18:45") ?: "18:45"
    set(value) = prefs.edit().putString("key_last_aksam", value).apply()

  var iftarSound: IftarSoundChoice
    get() {
      val name = prefs.getString("key_iftar_sound", IftarSoundChoice.RAMAZAN_TOPU.name)
      return try {
        IftarSoundChoice.valueOf(name ?: IftarSoundChoice.RAMAZAN_TOPU.name)
      } catch (e: Exception) {
        IftarSoundChoice.RAMAZAN_TOPU
      }
    }
    set(value) = prefs.edit().putString("key_iftar_sound", value.name).apply()

  // Oruç Tarihleri ve Günlük Oruç Durumu ("YYYY-MM-DD" -> "fasted", "unfasted", "broken")
  fun getFastingStatus(dateKey: String): String? {
    return prefs.getString("status_$dateKey", null)
  }

  fun setFastingStatus(dateKey: String, status: String) {
    prefs.edit().putString("status_$dateKey", status).apply()
    val dates = getFastingDates().toMutableSet()
    if (status == "fasted") {
      dates.add(dateKey)
    } else {
      dates.remove(dateKey)
    }
    prefs.edit().putStringSet("key_fasting_dates", dates).apply()
  }

  fun getFastingDates(): Set<String> {
    return prefs.getStringSet("key_fasting_dates", emptySet()) ?: emptySet()
  }

  fun toggleFastingDate(dateKey: String): Boolean {
    val current = getFastingDates().toMutableSet()
    val isNowFasted = if (current.contains(dateKey)) {
      current.remove(dateKey)
      prefs.edit().remove("status_$dateKey").apply()
      false
    } else {
      current.add(dateKey)
      prefs.edit().putString("status_$dateKey", "fasted").apply()
      true
    }
    prefs.edit().putStringSet("key_fasting_dates", current).apply()
    return isNowFasted
  }

  // Kaza ve Nafile Sayaçları
  var kazaDebtCount: Int
    get() = prefs.getInt("key_kaza_debt_count", 0)
    set(value) = prefs.edit().putInt("key_kaza_debt_count", value.coerceAtLeast(0)).apply()

  var nafileCount: Int
    get() = prefs.getInt("key_nafile_count", 0)
    set(value) = prefs.edit().putInt("key_nafile_count", value.coerceAtLeast(0)).apply()

  var kazaReminderEnabled: Boolean
    get() = prefs.getBoolean("key_kaza_reminder_enabled", true)
    set(value) = prefs.edit().putBoolean("key_kaza_reminder_enabled", value).apply()

  // Ramazan Manevi Notları (Basit liste string saklama)
  fun getNotes(): List<String> {
    val raw = prefs.getString("key_ramadan_notes", "") ?: ""
    return if (raw.isBlank()) emptyList() else raw.split("###NOTE_SEP###")
  }

  fun addNote(note: String) {
    if (note.isBlank()) return
    val current = getNotes().toMutableList()
    current.add(0, note.trim())
    prefs.edit().putString("key_ramadan_notes", current.joinToString("###NOTE_SEP###")).apply()
  }

  fun deleteNote(index: Int) {
    val current = getNotes().toMutableList()
    if (index in current.indices) {
      current.removeAt(index)
      prefs.edit().putString("key_ramadan_notes", current.joinToString("###NOTE_SEP###")).apply()
    }
  }
}

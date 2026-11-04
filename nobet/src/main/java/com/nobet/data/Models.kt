package com.nobet.data

/** Vardiya türü. Saatler gün içindeki dakika (0..1439). endMin <= startMin ise vardiya ertesi güne taşar. */
data class ShiftType(
  val id: String,
  val nameTr: String,
  val nameEn: String,
  val letterTr: String,
  val letterEn: String,
  val color: Long,
  val startMin: Int,
  val endMin: Int,
  val isOff: Boolean = false,
  val builtIn: Boolean = true,
) {
  val durationMin: Int
    get() = if (isOff) 0 else {
      val d = endMin - startMin
      if (d <= 0) d + 1440 else d
    }

  /** 23:00–06:00 aralığına en az 3 saat denk geliyorsa gece vardiyasıdır. */
  val isNight: Boolean
    get() {
      if (isOff) return false
      var n = 0
      for (i in 0 until durationMin) {
        val m = (startMin + i) % 1440
        if (m >= 23 * 60 || m < 6 * 60) n++
      }
      return n >= 180
    }
}

data class Pattern(
  val id: String,
  val nameTr: String,
  val nameEn: String,
  val descTr: String,
  val descEn: String,
  val seq: List<String>,
  val premium: Boolean,
)

data class SleepEntry(
  val id: Long,
  val start: Long,
  val end: Long,
  val quality: Int,
  val nap: Boolean,
) {
  val minutes: Int get() = ((end - start) / 60000L).toInt()
}

data class EnergyEntry(
  val id: Long,
  val time: Long,
  val level: Int,
  val tags: List<String>,
  val note: String,
)

object Builtins {
  const val DAY = "D"
  const val EVENING = "E"
  const val NIGHT = "N"
  const val FULL = "F"
  const val OFF = "O"

  /** Özel vardiyalar için sakin, yüksek kontrastlı palet. */
  val customPalette = listOf(
    0xFF2FA3A0, 0xFF8B66C9, 0xFFD0679B, 0xFF5FA35A, 0xFFA07855, 0xFF5C7A99,
  )

  fun defaultTypes(eightHour: Boolean = false): List<ShiftType> = listOf(
    ShiftType(DAY, "Gündüz", "Day", "Gü", "D", 0xFFF2B33D, 8 * 60, if (eightHour) 16 * 60 else 20 * 60),
    ShiftType(EVENING, "Akşam", "Evening", "A", "E", 0xFFE07B53, if (eightHour) 16 * 60 else 14 * 60, if (eightHour) 0 else 22 * 60),
    ShiftType(NIGHT, "Gece", "Night", "Ge", "N", 0xFF4B5BC4, if (eightHour) 0 else 20 * 60, 8 * 60),
    ShiftType(FULL, "24 Saat", "24 Hours", "24", "24", 0xFF8B66C9, 8 * 60, 8 * 60),
    ShiftType(OFF, "İzin", "Off", "İ", "O", 0xFF9AA7B4, 0, 0, isOff = true),
  )

  private val D = DAY
  private val E = EVENING
  private val N = NIGHT
  private val F = FULL
  private val O = OFF

  private fun rep(id: String, n: Int) = List(n) { id }

  val patterns: List<Pattern> = listOf(
    Pattern(
      "223", "2-2-3 (Panama)", "2-2-3 (Panama)",
      "2 çalış, 2 izin, 3 çalış, 2 izin, 2 çalış, 3 izin", "2 on, 2 off, 3 on, 2 off, 2 on, 3 off",
      listOf(D, D, O, O, D, D, D, O, O, D, D, O, O, O), premium = false,
    ),
    Pattern(
      "4on4off", "4 çalış 4 izin", "4-on-4-off",
      "4 gün çalış, 4 gün izin", "4 days on, 4 days off",
      rep(D, 4) + rep(O, 4), premium = false,
    ),
    Pattern(
      "52", "5-2 Hafta içi", "5-2 Weekdays",
      "5 gün çalış, 2 gün izin", "5 days on, 2 days off",
      rep(D, 5) + rep(O, 2), premium = false,
    ),
    Pattern(
      "2dn4o", "2 Gündüz · 2 Gece · 4 İzin", "2 Days · 2 Nights · 4 Off",
      "Hastanelerde yaygın 12 saatlik düzen", "Common 12-hour hospital rotation",
      rep(D, 2) + rep(N, 2) + rep(O, 4), premium = true,
    ),
    Pattern(
      "1236", "12/36", "12/36",
      "12 saat çalış, 36 saat dinlen", "12 hours on, 36 hours off",
      listOf(D, O), premium = true,
    ),
    Pattern(
      "2448", "24/48", "24/48",
      "24 saat çalış, 48 saat dinlen (güvenlik, itfaiye)", "24 hours on, 48 hours off (security, fire)",
      listOf(F, O, O), premium = true,
    ),
    Pattern(
      "3shift", "Haftalık 3'lü vardiya", "Weekly 3-shift rotation",
      "Sabah → Akşam → Gece, her hafta değişir", "Morning → Evening → Night, changes weekly",
      rep(D, 5) + rep(O, 2) + rep(E, 5) + rep(O, 2) + rep(N, 5) + rep(O, 2), premium = true,
    ),
    Pattern(
      "4dn", "4 Gündüz · 4 İzin · 4 Gece · 4 İzin", "4 Days · 4 Off · 4 Nights · 4 Off",
      "16 günlük dönüşümlü düzen", "16-day rotating cycle",
      rep(D, 4) + rep(O, 4) + rep(N, 4) + rep(O, 4), premium = true,
    ),
    Pattern(
      "dupont", "DuPont", "DuPont",
      "4 gece, 3 izin, 3 gündüz, 1 izin, 3 gece, 3 izin, 4 gündüz, 7 izin", "4N, 3 off, 3D, 1 off, 3N, 3 off, 4D, 7 off",
      rep(N, 4) + rep(O, 3) + rep(D, 3) + rep(O, 1) + rep(N, 3) + rep(O, 3) + rep(D, 4) + rep(O, 7), premium = true,
    ),
  )

  fun suggestedFor(role: String): List<String> = when (role) {
    "nurse" -> listOf("2dn4o", "1236", "223")
    "factory" -> listOf("3shift", "223", "52")
    "security" -> listOf("2448", "1236", "4on4off")
    "hospitality" -> listOf("52", "3shift", "4on4off")
    else -> listOf("223", "4on4off", "52")
  }

  val energyTags = listOf("caffeine", "exercise", "stress", "lateMeal", "alcohol", "nap")
}

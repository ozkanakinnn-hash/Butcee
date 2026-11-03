package com.example.data

import java.util.Calendar

/** Cami kartında gösterilen günlük motivasyon mesajları (Türkçe / İngilizce). */
object DailyMessages {
  private val messages = listOf(
    "Allah'ın rahmetinden asla ümit kesme." to "Never lose hope in Allah's mercy.",
    "Kalpler ancak Allah'ı anmakla huzur bulur." to "Hearts find peace only in the remembrance of Allah.",
    "Her zorlukla beraber bir kolaylık vardır." to "With every hardship comes ease.",
    "Bugün bir gülümseme de sadakadır." to "Even a smile today is charity.",
    "Sabır, imanın yarısıdır." to "Patience is half of faith.",
    "Şükreden kalbe nimet artar." to "Blessings increase for the grateful heart.",
    "Namaz, müminin miracıdır." to "Prayer is the ascension of the believer.",
    "En hayırlınız, ahlakı en güzel olanınızdır." to "The best of you are those with the best character.",
    "Dua, müminin silahıdır." to "Dua is the weapon of the believer.",
    "Az da olsa devamlı olan amel en sevimlisidir." to "The most beloved deeds are those done consistently, even if small.",
    "Allah, sabredenlerle beraberdir." to "Allah is with those who are patient.",
    "Bugün kimsenin kalbini kırma." to "Do not break anyone's heart today.",
    "İyilik yap, denize at; balık bilmezse Halik bilir." to "Do good and let it go; if no one knows, the Creator knows.",
    "Tevekkül et; Allah sana yeter." to "Trust in Allah; He is sufficient for you."
  )

  fun today(english: Boolean, cal: Calendar = Calendar.getInstance()): String {
    val (tr, en) = messages[cal.get(Calendar.DAY_OF_YEAR) % messages.size]
    return if (english) en else tr
  }
}

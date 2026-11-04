package com.nobet.i18n

import com.nobet.data.Pattern
import com.nobet.data.ShiftType
import com.nobet.data.Store
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Türkçe + İngilizce: her metin iki dilde yan yana yazılır, böylece hiçbir metin tek dilde kalmaz.
 * Store.lang bir Compose state olduğu için dil değişince arayüz anında güncellenir.
 */
fun t(tr: String, en: String): String = if (Store.isEn) en else tr

fun appLocale(): Locale = if (Store.isEn) Locale.ENGLISH else Locale.forLanguageTag("tr-TR")

fun fmtDur(min: Int): String {
  val m = if (min < 0) 0 else min
  val h = m / 60
  val r = m % 60
  return if (Store.isEn) {
    when {
      h == 0 -> "${r}m"
      r == 0 -> "${h}h"
      else -> "${h}h ${r}m"
    }
  } else {
    when {
      h == 0 -> "$r dk"
      r == 0 -> "$h sa"
      else -> "$h sa $r dk"
    }
  }
}

fun fmtHours(min: Int): String {
  val s = String.format(appLocale(), "%.1f", min / 60f)
  return if (Store.isEn) "$s h" else "$s sa"
}

fun fmtNum(v: Float): String = String.format(appLocale(), "%.1f", v)

fun fmtClock(minOfDay: Int): String {
  val m = ((minOfDay % 1440) + 1440) % 1440
  return String.format(Locale.ROOT, "%02d:%02d", m / 60, m % 60)
}

fun fmtDate(d: LocalDate, pattern: String = "d MMMM EEEE"): String =
  d.format(DateTimeFormatter.ofPattern(pattern, appLocale()))

fun dowShort(d: DayOfWeek): String = d.getDisplayName(TextStyle.SHORT, appLocale())

fun typeName(t: ShiftType) = if (Store.isEn) t.nameEn else t.nameTr
fun typeLetter(t: ShiftType) = if (Store.isEn) t.letterEn else t.letterTr
fun patternName(p: Pattern) = if (Store.isEn) p.nameEn else p.nameTr
fun patternDesc(p: Pattern) = if (Store.isEn) p.descEn else p.descTr

fun relDay(d: LocalDate): String {
  val today = Store.today()
  return when (d) {
    today -> t("Bugün", "Today")
    today.plusDays(1) -> t("Yarın", "Tomorrow")
    today.minusDays(1) -> t("Dün", "Yesterday")
    else -> fmtDate(d, "d MMM EEE")
  }
}

fun tagName(tag: String): String = when (tag) {
  "caffeine" -> t("Kafein", "Caffeine")
  "exercise" -> t("Egzersiz", "Exercise")
  "stress" -> t("Stres", "Stress")
  "lateMeal" -> t("Geç yemek", "Late meal")
  "alcohol" -> t("Alkol", "Alcohol")
  "nap" -> t("Şekerleme", "Nap")
  else -> tag
}

fun roleName(r: String): String = when (r) {
  "nurse" -> t("Hemşire / Sağlık", "Nurse / Healthcare")
  "factory" -> t("Fabrika / Üretim", "Factory / Production")
  "security" -> t("Güvenlik", "Security")
  "hospitality" -> t("Otel / Turizm", "Hotel / Hospitality")
  else -> t("Diğer", "Other")
}

fun energyLabel(v: Int): String = when {
  v <= 2 -> t("Tükenmiş", "Exhausted")
  v <= 4 -> t("Yorgun", "Tired")
  v <= 6 -> t("İdare eder", "Okay")
  v <= 8 -> t("İyi", "Good")
  else -> t("Tam enerji", "Fully charged")
}

val qualityEmoji = listOf("😫", "😕", "😐", "🙂", "😴")

fun qualityLabel(q: Int): String = when (q) {
  1 -> t("Çok kötü", "Very poor")
  2 -> t("Kötü", "Poor")
  3 -> t("Orta", "Fair")
  4 -> t("İyi", "Good")
  else -> t("Harika", "Great")
}

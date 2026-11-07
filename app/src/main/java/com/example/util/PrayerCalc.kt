package com.example.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * İnternet yokken veya koordinata göre: Diyanet yöntemine yakın astronomik hesap
 * (imsak 18°, yatsı 17°, Şafii ikindi, Diyanet temkinleri).
 */
object PrayerCalc {
  /** 81 il merkezinin yaklaşık koordinatları (enlem, boylam). */
  val provinceCoords: Map<String, Pair<Double, Double>> = mapOf(
    "Adana" to (37.00 to 35.32),
    "Adıyaman" to (37.76 to 38.28),
    "Afyonkarahisar" to (38.76 to 30.54),
    "Ağrı" to (39.72 to 43.05),
    "Amasya" to (40.65 to 35.83),
    "Ankara" to (39.93 to 32.86),
    "Antalya" to (36.89 to 30.71),
    "Artvin" to (41.18 to 41.82),
    "Aydın" to (37.85 to 27.85),
    "Balıkesir" to (39.65 to 27.88),
    "Bilecik" to (40.14 to 29.98),
    "Bingöl" to (38.88 to 40.50),
    "Bitlis" to (38.40 to 42.11),
    "Bolu" to (40.74 to 31.61),
    "Burdur" to (37.72 to 30.29),
    "Bursa" to (40.19 to 29.06),
    "Çanakkale" to (40.15 to 26.41),
    "Çankırı" to (40.60 to 33.62),
    "Çorum" to (40.55 to 34.95),
    "Denizli" to (37.78 to 29.09),
    "Diyarbakır" to (37.91 to 40.24),
    "Edirne" to (41.68 to 26.56),
    "Elazığ" to (38.67 to 39.22),
    "Erzincan" to (39.75 to 39.49),
    "Erzurum" to (39.90 to 41.27),
    "Eskişehir" to (39.78 to 30.52),
    "Gaziantep" to (37.07 to 37.38),
    "Giresun" to (40.91 to 38.39),
    "Gümüşhane" to (40.46 to 39.48),
    "Hakkari" to (37.58 to 43.74),
    "Hatay" to (36.20 to 36.16),
    "Isparta" to (37.76 to 30.55),
    "Mersin" to (36.80 to 34.63),
    "İstanbul" to (41.01 to 28.98),
    "İzmir" to (38.42 to 27.14),
    "Kars" to (40.60 to 43.10),
    "Kastamonu" to (41.38 to 33.78),
    "Kayseri" to (38.73 to 35.48),
    "Kırklareli" to (41.73 to 27.22),
    "Kırşehir" to (39.15 to 34.16),
    "Kocaeli" to (40.77 to 29.94),
    "Konya" to (37.87 to 32.48),
    "Kütahya" to (39.42 to 29.98),
    "Malatya" to (38.35 to 38.31),
    "Manisa" to (38.61 to 27.43),
    "Kahramanmaraş" to (37.58 to 36.94),
    "Mardin" to (37.31 to 40.74),
    "Muğla" to (37.22 to 28.36),
    "Muş" to (38.74 to 41.49),
    "Nevşehir" to (38.62 to 34.71),
    "Niğde" to (37.97 to 34.68),
    "Ordu" to (40.98 to 37.88),
    "Rize" to (41.02 to 40.52),
    "Sakarya" to (40.78 to 30.40),
    "Samsun" to (41.29 to 36.33),
    "Siirt" to (37.93 to 41.94),
    "Sinop" to (42.03 to 35.15),
    "Sivas" to (39.75 to 37.02),
    "Tekirdağ" to (40.98 to 27.51),
    "Tokat" to (40.31 to 36.55),
    "Trabzon" to (41.00 to 39.72),
    "Tunceli" to (39.11 to 39.55),
    "Şanlıurfa" to (37.16 to 38.80),
    "Uşak" to (38.68 to 29.41),
    "Van" to (38.50 to 43.38),
    "Yozgat" to (39.82 to 34.81),
    "Zonguldak" to (41.45 to 31.79),
    "Aksaray" to (38.37 to 34.03),
    "Bayburt" to (40.26 to 40.23),
    "Karaman" to (37.18 to 33.22),
    "Kırıkkale" to (39.85 to 33.51),
    "Batman" to (37.88 to 41.13),
    "Şırnak" to (37.52 to 42.46),
    "Bartın" to (41.63 to 32.34),
    "Ardahan" to (41.11 to 42.70),
    "Iğdır" to (39.92 to 44.04),
    "Yalova" to (40.65 to 29.27),
    "Karabük" to (41.20 to 32.62),
    "Kilis" to (36.72 to 37.12),
    "Osmaniye" to (37.07 to 36.25),
    "Düzce" to (40.84 to 31.16)
  )

  /** [imsak, güneş, öğle, ikindi, akşam, yatsı] "HH:mm". [tzHours] null ise cihaz saat dilimi. */
  fun times(lat: Double, lon: Double, cal: Calendar = Calendar.getInstance(), tzHours: Double? = null): List<String> {
    val tz = tzHours ?: (TimeZone.getDefault().getOffset(cal.timeInMillis) / 3_600_000.0)
    var y = cal.get(Calendar.YEAR).toDouble(); var m = (cal.get(Calendar.MONTH) + 1).toDouble()
    val d = cal.get(Calendar.DAY_OF_MONTH).toDouble()
    if (m <= 2) { y -= 1; m += 12 }
    val a = floor(y / 100); val b = 2 - a + floor(a / 4)
    val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + d + b - 1524.5
    val dd = jd - 2451545.0
    val g = fix(357.529 + 0.98560028 * dd)
    val q = fix(280.459 + 0.98564736 * dd)
    val l = fix(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)))
    val e = 23.439 - 0.00000036 * dd
    val ra = deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15
    val decl = deg(asin(sin(rad(e)) * sin(rad(l))))
    var eqt = q / 15 - fixHour(ra)
    if (eqt > 12) eqt -= 24; if (eqt < -12) eqt += 24
    val noon = 12 + tz - lon / 15 - eqt
    fun ha(angle: Double): Double {
      val x = (-sin(rad(angle)) - sin(rad(lat)) * sin(rad(decl))) / (cos(rad(lat)) * cos(rad(decl)))
      return deg(acos(x.coerceIn(-1.0, 1.0))) / 15
    }
    val asrAlt = -deg(atan(1 / (1 + tan(rad(abs(lat - decl))))))
    fun fmt(h: Double, temkin: Int): String {
      val mins = (((h * 60 + temkin).roundToInt() % 1440) + 1440) % 1440
      return String.format(Locale.ROOT, "%02d:%02d", mins / 60, mins % 60)
    }
    return listOf(fmt(noon - ha(18.0), 0), fmt(noon - ha(0.833), -7), fmt(noon, 5),
      fmt(noon + ha(asrAlt), 4),
      // Akşam: Diyanet gibi güneş batışı yukarı yuvarlanır + 7 dk
      fmt((kotlin.math.ceil(sunsetMinutes(lat, lon, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), tz) - 1e-6) + 7) / 60.0, 0),
      fmt(noon + ha(17.0), -1))
  }

  /** NOAA yöntemiyle astronomik güneş batışı (yerel saat, dakika; saniye hassasiyetinde). */
  fun sunsetMinutes(lat: Double, lon: Double, y: Int, m: Int, d: Int, tzHours: Double): Double {
    var yy = y.toDouble(); var mm = m.toDouble()
    if (mm <= 2) { yy -= 1; mm += 12 }
    val a = floor(yy / 100); val b = 2 - a + floor(a / 4)
    val jd = floor(365.25 * (yy + 4716)) + floor(30.6001 * (mm + 1)) + d + b - 1524.5
    var t = 1080.0
    repeat(3) {
      val tc = (jd + (t - tzHours * 60) / 1440 - 2451545) / 36525
      val l0 = (280.46646 + tc * (36000.76983 + tc * 0.0003032)) % 360
      val ma = 357.52911 + tc * (35999.05029 - 0.0001537 * tc)
      val e = 0.016708634 - tc * (0.000042037 + 0.0000001267 * tc)
      val c = sin(rad(ma)) * (1.914602 - tc * (0.004817 + 0.000014 * tc)) + sin(rad(2 * ma)) * (0.019993 - 0.000101 * tc) + sin(rad(3 * ma)) * 0.000289
      val lam = l0 + c - 0.00569 - 0.00478 * sin(rad(125.04 - 1934.136 * tc))
      val eps0 = 23 + (26 + (21.448 - tc * (46.815 + tc * (0.00059 - tc * 0.001813))) / 60) / 60
      val eps = eps0 + 0.00256 * cos(rad(125.04 - 1934.136 * tc))
      val dec = deg(asin(sin(rad(eps)) * sin(rad(lam))))
      val yv = tan(rad(eps / 2)).let { it * it }
      val eqt = 4 * deg(yv * sin(2 * rad(l0)) - 2 * e * sin(rad(ma)) + 4 * e * yv * sin(rad(ma)) * cos(2 * rad(l0)) -
        0.5 * yv * yv * sin(4 * rad(l0)) - 1.25 * e * e * sin(2 * rad(ma)))
      val x = (cos(rad(90.833)) / (cos(rad(lat)) * cos(rad(dec))) - tan(rad(lat)) * tan(rad(dec))).coerceIn(-1.0, 1.0)
      t = 720 - 4 * lon - eqt + tzHours * 60 + 4 * deg(acos(x))
    }
    return t
  }

  /**
   * Diyanet akşam vaktini, astronomik güneş batışını dakikaya YUKARI yuvarlayıp 7 dk temkin ekleyerek verir.
   * Servis (Aladhan) en yakın dakikaya yuvarladığından bazı günler 1 dk erken kalır; o günlerde 1 dk ileri alınır.
   * Fark yalnızca 0 veya +1 dakika olabilir.
   */
  fun diyanetMaghrib(serviceMaghrib: String, lat: Double, lon: Double, y: Int, m: Int, d: Int, tzHours: Double): String {
    val p = serviceMaghrib.split(":").mapNotNull { it.trim().take(2).toIntOrNull() }
    if (p.size != 2) return serviceMaghrib
    val service = p[0] * 60 + p[1]
    val diyanet = kotlin.math.ceil(sunsetMinutes(lat, lon, y, m, d, tzHours) - 1e-6).toInt() + 7
    val fixed = if (diyanet == service + 1) diyanet else service
    return String.format(Locale.ROOT, "%02d:%02d", fixed / 60, fixed % 60)
  }

  private fun rad(d: Double) = d * Math.PI / 180
  private fun deg(r: Double) = r * 180 / Math.PI
  private fun fix(a: Double): Double { val r = a % 360; return if (r < 0) r + 360 else r }
  private fun fixHour(a: Double): Double { val r = a % 24; return if (r < 0) r + 24 else r }
}

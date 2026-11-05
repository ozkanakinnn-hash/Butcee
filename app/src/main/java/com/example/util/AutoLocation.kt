package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.example.data.LocationsData
import com.example.data.TurkeyProvinces
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * GPS ile otomatik konum: telefonun konumundan il / ilçe bulunur.
 * Türkiye'de il ve ilçe listedeki adlarla eşleştirilir (vakitler Diyanet ilçe takviminden gelir);
 * yurt dışında ya da eşleşmezse koordinat kaydedilir ve vakitler koordinata göre hesaplanır.
 */
object AutoLocation {

  data class Detected(
    val country: String,
    val province: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    /** true: Türkiye'de il eşleşti (Diyanet ilçe vakitleri); false: koordinata göre hesap. */
    val matchedTurkey: Boolean
  )

  fun hasPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

  /** Konumu bulur ve il/ilçeye çevirir; izin yoksa veya konum alınamazsa null. */
  suspend fun detect(context: Context): Detected? = withContext(Dispatchers.IO) {
    if (!hasPermission(context)) return@withContext null
    val loc = currentLocation(context) ?: return@withContext null
    reverse(context, loc.latitude, loc.longitude)
  }

  @SuppressLint("MissingPermission")
  private suspend fun currentLocation(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, "fused")
      .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    // 1) Son 30 dakikadaki bilinen konum yeterli (pil dostu)
    val recent = providers.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
      .maxByOrNull { it.time }
    if (recent != null && System.currentTimeMillis() - recent.time < 30 * 60_000L) return recent
    // 2) Tek seferlik yeni konum (en fazla 12 sn)
    val fresh = withTimeoutOrNull(12_000L) {
      val provider = providers.firstOrNull { it == LocationManager.NETWORK_PROVIDER } ?: providers.firstOrNull() ?: return@withTimeoutOrNull null
      suspendCancellableCoroutine<Location?> { cont ->
        try {
          if (Build.VERSION.SDK_INT >= 30) {
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            lm.getCurrentLocation(provider, signal, context.mainExecutor) { l -> if (cont.isActive) cont.resume(l) }
          } else {
            @Suppress("DEPRECATION")
            lm.requestSingleUpdate(provider, { l -> if (cont.isActive) cont.resume(l) }, android.os.Looper.getMainLooper())
          }
        } catch (_: Exception) {
          if (cont.isActive) cont.resume(null)
        }
      }
    }
    return fresh ?: recent
  }

  /** "Şanlıurfa" → "sanliurfa": Türkçe karakter ve büyük/küçük harf farkını yok sayar. */
  fun norm(s: String): String = s.lowercase(Locale.forLanguageTag("tr"))
    .replace('ç', 'c').replace('ğ', 'g').replace('ı', 'i').replace('ö', 'o').replace('ş', 's').replace('ü', 'u')
    .replace('â', 'a').replace('î', 'i').replace('û', 'u')
    .replace(" ili", "").replace(" province", "").replace(" merkez", "")
    .filter { it.isLetter() }

  private fun reverse(context: Context, lat: Double, lon: Double): Detected? {
    if (!Geocoder.isPresent()) return null
    val address = try {
      @Suppress("DEPRECATION")
      Geocoder(context, Locale.forLanguageTag("tr-TR")).getFromLocation(lat, lon, 1)?.firstOrNull()
    } catch (_: Exception) { null } ?: return null

    val code = address.countryCode?.uppercase(Locale.ROOT)
    if (code == "TR") {
      val provinceRaw = address.adminArea ?: ""
      val province = TurkeyProvinces.list.firstOrNull { norm(it.name) == norm(provinceRaw) }
        ?: TurkeyProvinces.list.firstOrNull { norm(provinceRaw).contains(norm(it.name)) }
      if (province != null) {
        val candidates = listOfNotNull(address.subAdminArea, address.locality, address.subLocality)
        val district = candidates.firstNotNullOfOrNull { c -> province.districts.firstOrNull { norm(it) == norm(c) } }
          ?: candidates.firstOrNull { it.isNotBlank() && norm(it) != norm(province.name) }
          ?: province.districts.firstOrNull { it == "Merkez" } ?: province.districts.first()
        return Detected("Türkiye", province.name, district, lat, lon, matchedTurkey = true)
      }
    }
    val country = LocationsData.countries.firstOrNull { it.code == code }?.name ?: address.countryName ?: "Türkiye"
    val city = address.locality ?: address.subAdminArea ?: address.adminArea ?: return null
    val district = address.subLocality ?: city
    return Detected(country, city, district, lat, lon, matchedTurkey = false)
  }
}

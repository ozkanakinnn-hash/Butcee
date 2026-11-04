package com.example.util

import android.app.Activity
import android.content.Context
import com.example.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Reklam onayı (Google UMP — GDPR/KVKK) ve AdMob başlatma.
 * - Onay formu gerekiyorsa gösterilir; reklam ancak izin alınabildiğinde yüklenir.
 * - MobileAds ana iş parçacığını bloklamasın diye arka planda başlatılır.
 * - Dini içerikli uygulama: reklam içerik sınırı "PG" (alkol/kumar gibi yetişkin içerikleri sınırlar;
 *   ayrıca AdMob panelinden hassas kategoriler engellenmelidir).
 */
object AdsConsent {
  private val started = AtomicBoolean(false)
  private val _canShowAds = MutableStateFlow(false)
  val canShowAds: StateFlow<Boolean> = _canShowAds

  private fun info(context: Context): ConsentInformation = UserMessagingPlatform.getConsentInformation(context)

  fun gather(activity: Activity) {
    val params = ConsentRequestParameters.Builder().build()
    val ci = info(activity)
    try {
      ci.requestConsentInfoUpdate(
        activity, params,
        {
          UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
            if (ci.canRequestAds()) startAds(activity)
          }
        },
        { _ -> if (ci.canRequestAds()) startAds(activity) }
      )
    } catch (_: Exception) { }
    // Önceki oturumda izin alındıysa beklemeden başlat
    if (ci.canRequestAds()) startAds(activity)
  }

  private fun startAds(context: Context) {
    if (!started.compareAndSet(false, true)) return
    val app = context.applicationContext
    Thread {
      try {
        MobileAds.setRequestConfiguration(
          RequestConfiguration.Builder()
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_PG)
            .build()
        )
        MobileAds.initialize(app) { _canShowAds.value = true }
      } catch (_: Exception) { }
    }.start()
  }

  /** Ayarlar'daki "Reklam gizlilik seçenekleri" düğmesi gerekli mi? */
  fun privacyOptionsRequired(context: Context): Boolean =
    try {
      info(context).privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    } catch (_: Exception) { false }

  fun showPrivacyOptions(activity: Activity) {
    try { UserMessagingPlatform.showPrivacyOptionsForm(activity) { } } catch (_: Exception) { }
  }

  val bannerUnitId: String get() = BuildConfig.ADMOB_BANNER_ID
}

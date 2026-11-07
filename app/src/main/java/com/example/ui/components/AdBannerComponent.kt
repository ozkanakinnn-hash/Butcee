package com.example.ui.components

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdBannerComponent(
  modifier: Modifier = Modifier
) {
  var adFailed by remember { mutableStateOf(false) }
  // Reklam yalnızca kullanıcı onayı (UMP) alınıp SDK başlatıldıktan sonra yüklenir
  val canShowAds by com.example.util.AdsConsent.canShowAds.collectAsState()
  // Premium (reklamsız) üyelerde reklam gösterilmez
  val isPremium by com.example.billing.PremiumManager.isPremium.collectAsState()

  if (!adFailed && canShowAds && !isPremium) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("admob_banner_component"),
      contentAlignment = Alignment.Center
    ) {
      AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
          AdView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setAdSize(AdSize.BANNER)
            adUnitId = com.example.util.AdsConsent.bannerUnitId
            adListener = object : AdListener() {
              override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                android.util.Log.w("AdMob", "Ad failed to load: ${loadAdError.message}")
                adFailed = true
              }
            }
            try {
              loadAd(AdRequest.Builder().build())
            } catch (e: Exception) {
              android.util.Log.w("AdMob", "Ad load exception: ${e.message}")
              adFailed = true
            }
          }
        },
        // Ekrandan çıkınca reklam görünümünü serbest bırak (bellek sızıntısı olmasın)
        onRelease = { it.destroy() }
      )
    }
  }
}

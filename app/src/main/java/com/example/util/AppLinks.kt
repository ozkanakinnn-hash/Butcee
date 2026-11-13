package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Uygulamanın dış bağlantıları (gizlilik politikası, abonelik yönetimi). */
object AppLinks {
  /** GitHub Pages: depo Ayarlar → Pages → "main / docs" seçilince bu adreste yayınlanır. */
  const val PRIVACY_URL = "https://ozkanakinnn-hash.github.io/Butcee/ezan-vakit/privacy-policy.html"

  fun subscriptionsUrl(packageName: String, productId: String? = null): String =
    if (productId == null) "https://play.google.com/store/account/subscriptions"
    else "https://play.google.com/store/account/subscriptions?sku=$productId&package=$packageName"

  fun open(context: Context, url: String) {
    try {
      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
      Toast.makeText(context, "Bağlantı açılamadı / Could not open link", Toast.LENGTH_SHORT).show()
    }
  }
}

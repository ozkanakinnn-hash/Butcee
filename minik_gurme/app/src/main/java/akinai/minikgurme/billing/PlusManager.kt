package akinai.minikgurme.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import akinai.minikgurme.BuildConfig
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Minik Gurme Plus aboneliği (Google Play Billing).
 *
 * Play Console'da şu iki abonelik ürünü oluşturulmalı (her biri tek temel planlı, otomatik yenilenen):
 *  - [PRODUCT_MONTHLY] aylık, [PRODUCT_YEARLY] yıllık. Fiyatlar Play Console'dan gelir; uygulamada sabit fiyat yok.
 * Not: Satın alma doğrulaması cihazda yapılır. Gelir büyüdüğünde sunucu taraflı doğrulama (Play Developer API) önerilir.
 */
object PlusManager {
    const val PRODUCT_MONTHLY = "minikgurme_plus_monthly"
    const val PRODUCT_YEARLY = "minikgurme_plus_yearly"
    private const val PREFS = "app_prefs"
    private const val KEY_CACHE = "plus_active_cache"
    private const val KEY_DEBUG = "plus_debug_override"

    data class Plan(val productId: String, val price: String, val period: String, val offerToken: String, val details: ProductDetails?)

    /** Plus etkin mi (Compose durumu; değişince ekranlar güncellenir). */
    var isPlus by mutableStateOf(false)
        private set
    var plans by mutableStateOf<List<Plan>>(emptyList())
        private set
    /** Kullanıcıya gösterilecek son durum mesajı (Türkçe kaynak; ekranda t() ile çevrilir). */
    var message by mutableStateOf<String?>(null)
    var available by mutableStateOf(false)
        private set

    private var client: BillingClient? = null
    private var appContext: Context? = null
    private var purchased = false
    private var debugOverride = false

    fun init(context: Context) {
        val c = context.applicationContext
        appContext = c
        val p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        purchased = p.getBoolean(KEY_CACHE, false)
        debugOverride = BuildConfig.DEBUG && p.getBoolean(KEY_DEBUG, false)
        refreshState()
        if (client != null) return
        try {
            client = BillingClient.newBuilder(c)
                .setListener { result, purchases -> onPurchases(result, purchases) }
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build()
            client?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    available = result.responseCode == BillingClient.BillingResponseCode.OK
                    if (available) { queryPlans(); restore(silent = true) }
                }
                override fun onBillingServiceDisconnected() { available = false }
            })
        } catch (_: Throwable) {
            available = false
        }
    }

    private fun refreshState() { isPlus = purchased || debugOverride }

    private fun saveCache(active: Boolean) {
        purchased = active
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_CACHE, active)?.apply()
        refreshState()
    }

    /** Yalnızca geliştirme sürümünde: Plus'ı satın almadan dene. */
    fun setDebugOverride(context: Context, on: Boolean) {
        if (!BuildConfig.DEBUG) return
        debugOverride = on
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DEBUG, on).apply()
        refreshState()
    }
    val debugOverrideEnabled: Boolean get() = debugOverride

    private fun queryPlans() {
        val products = listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY).map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
        }
        client?.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { _, list ->
            plans = list.mapNotNull { d ->
                val offer = d.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
                val phase = offer.pricingPhases.pricingPhaseList.lastOrNull() ?: return@mapNotNull null
                Plan(d.productId, phase.formattedPrice, phase.billingPeriod, offer.offerToken, d)
            }.sortedBy { if (it.productId == PRODUCT_YEARLY) 0 else 1 }
        }
    }

    fun purchase(activity: Activity, plan: Plan) {
        val d = plan.details
        val c = client
        if (d == null || c == null || !available) {
            message = "Satın alma şu an kullanılamıyor. Uygulamanın Google Play'den yüklendiğinden emin olun."
            return
        }
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).setOfferToken(plan.offerToken).build())
        ).build()
        c.launchBillingFlow(activity, params)
    }

    /** Satın alımları geri yükle (telefon değişiminde / yeniden kurulumda). */
    fun restore(silent: Boolean = false) {
        val c = client
        if (c == null || !available) {
            if (!silent) message = "Google Play'e bağlanılamadı. İnternet bağlantınızı kontrol edip tekrar deneyin."
            return
        }
        c.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { _, list ->
            val active = list.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            list.forEach { acknowledge(it) }
            saveCache(active)
            if (!silent) message = if (active) "Plus aboneliğiniz geri yüklendi." else "Etkin bir Plus aboneliği bulunamadı."
        }
    }

    private fun onPurchases(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases.orEmpty().forEach { acknowledge(it) }
                if (purchases.orEmpty().any { it.purchaseState == Purchase.PurchaseState.PURCHASED }) {
                    saveCache(true); message = "Minik Gurme Plus'a hoş geldiniz!"
                } else if (purchases.orEmpty().any { it.purchaseState == Purchase.PurchaseState.PENDING }) {
                    message = "Ödemeniz onay bekliyor. Onaylanınca Plus otomatik açılır."
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restore()
            else -> message = "Satın alma tamamlanamadı. Lütfen tekrar deneyin."
        }
    }

    private fun acknowledge(p: Purchase) {
        if (p.purchaseState != Purchase.PurchaseState.PURCHASED || p.isAcknowledged) return
        client?.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { }
    }
}

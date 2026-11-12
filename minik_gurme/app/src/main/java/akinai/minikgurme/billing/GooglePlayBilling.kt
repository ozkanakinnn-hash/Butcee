package akinai.minikgurme.billing

import android.app.Activity
import android.content.Context
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
 * Minik Gurme Plus aboneliği — Android: Google Play Billing (ortak durum: [PlusManager]).
 *
 * Play Console'da şu iki abonelik ürünü oluşturulmalı (her biri tek temel planlı, otomatik yenilenen):
 *  - [PRODUCT_MONTHLY] aylık, [PRODUCT_YEARLY] yıllık. Fiyatlar Play Console'dan gelir; uygulamada sabit fiyat yok.
 * Not: Satın alma doğrulaması cihazda yapılır. Gelir büyüdüğünde sunucu taraflı doğrulama (Play Developer API) önerilir.
 */
object GooglePlayBilling : PlusManager.Store {
    private val PRODUCT_MONTHLY = PlusManager.PRODUCT_MONTHLY
    private val PRODUCT_YEARLY = PlusManager.PRODUCT_YEARLY
    private const val PREFS = "app_prefs"
    private const val KEY_CACHE = "plus_active_cache"
    private const val KEY_DEBUG = "plus_debug_override"

    private class Offer(val token: String, val details: ProductDetails)

    /** Satın alma penceresi için açık ekran (MainActivity ayarlar). */
    var activity: java.lang.ref.WeakReference<Activity>? = null

    private var available: Boolean
        get() = PlusManager.available
        set(v) { PlusManager.available = v }
    private var message: String?
        get() = PlusManager.message
        set(v) { PlusManager.message = v }

    private var client: BillingClient? = null
    private var appContext: Context? = null
    private var purchased = false
    private var debugOverride = false

    fun init(context: Context) {
        PlusManager.store = this
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

    private fun refreshState() {
        PlusManager.isPlus = purchased || debugOverride
        PlusManager.debugOverrideEnabled = debugOverride
    }

    private fun saveCache(active: Boolean) {
        purchased = active
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_CACHE, active)?.apply()
        refreshState()
    }

    /** Yalnızca geliştirme sürümünde: Plus'ı satın almadan dene. */
    override fun setDebugOverride(on: Boolean) {
        if (!BuildConfig.DEBUG) return
        debugOverride = on
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_DEBUG, on)?.apply()
        refreshState()
    }

    private fun queryPlans() {
        val products = listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY).map {
            QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
        }
        client?.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { _, list ->
            PlusManager.plans = list.mapNotNull { d ->
                val offer = d.subscriptionOfferDetails?.firstOrNull() ?: return@mapNotNull null
                val phase = offer.pricingPhases.pricingPhaseList.lastOrNull() ?: return@mapNotNull null
                PlusManager.Plan(d.productId, phase.formattedPrice, phase.billingPeriod, Offer(offer.offerToken, d))
            }.sortedBy { if (it.productId == PRODUCT_YEARLY) 0 else 1 }
        }
    }

    override fun purchase(plan: PlusManager.Plan) {
        val offer = plan.platformData as? Offer
        val c = client
        val act = activity?.get()
        if (offer == null || c == null || act == null || !available) {
            message = "Satın alma şu an kullanılamıyor. Uygulamanın Google Play'den yüklendiğinden emin olun."
            return
        }
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(offer.details).setOfferToken(offer.token).build())
        ).build()
        c.launchBillingFlow(act, params)
    }

    /** Satın alımları geri yükle (telefon değişiminde / yeniden kurulumda). */
    override fun restore(silent: Boolean) {
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
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restore(false)
            else -> message = "Satın alma tamamlanamadı. Lütfen tekrar deneyin."
        }
    }

    private fun acknowledge(p: Purchase) {
        if (p.purchaseState != Purchase.PurchaseState.PURCHASED || p.isAcknowledged) return
        client?.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { }
    }
}

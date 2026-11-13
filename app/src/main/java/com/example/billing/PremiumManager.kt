package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reklamsız Premium üyelik — Google Play Faturalandırma.
 *
 * Play Console'da tanımlanması gereken ürünler (kimlikler birebir aynı olmalı):
 *  - Abonelik:  premium  (temel planlar: "aylik" ve "yillik")
 *  - Tek seferlik ürün: premium_lifetime  (ömür boyu reklamsız)
 *
 * Premium durumu cihazda saklanır ve her açılışta Google Play'den doğrulanır
 * (iptal / iade / süre dolumu yakalanır).
 */
object PremiumManager : PurchasesUpdatedListener {

  const val SUB_ID = "premium"
  const val LIFETIME_ID = "premium_lifetime"
  private const val PREFS = "premium"

  data class Plan(
    val productId: String,
    val title: String,
    val price: String,
    val period: String,          // "P1M", "P1Y" veya "" (ömür boyu)
    val freeTrial: String?,      // ör. "P7D"
    val details: ProductDetails,
    val offerToken: String?
  )

  enum class Status { UNKNOWN, CONNECTING, READY, UNAVAILABLE }

  private val _isPremium = MutableStateFlow(false)
  val isPremium: StateFlow<Boolean> = _isPremium

  private val _plans = MutableStateFlow<List<Plan>>(emptyList())
  val plans: StateFlow<List<Plan>> = _plans

  private val _status = MutableStateFlow(Status.UNKNOWN)
  val status: StateFlow<Status> = _status

  private val _message = MutableStateFlow<String?>(null)
  val message: StateFlow<String?> = _message

  private var client: BillingClient? = null
  private lateinit var appContext: Context

  fun init(context: Context) {
    appContext = context.applicationContext
    _isPremium.value = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("active", false)
    if (client == null) {
      client = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()
    }
    connect()
  }

  private fun connect() {
    val c = client ?: return
    if (c.isReady) { refresh(); return }
    _status.value = Status.CONNECTING
    try {
      c.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(result: BillingResult) {
          if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            _status.value = Status.READY
            refresh()
          } else {
            _status.value = Status.UNAVAILABLE
          }
        }
        override fun onBillingServiceDisconnected() { _status.value = Status.UNKNOWN }
      })
    } catch (_: Exception) {
      _status.value = Status.UNAVAILABLE
    }
  }

  /** Ürün fiyatlarını ve mevcut satın almaları yeniler. */
  fun refresh() {
    val c = client ?: return
    if (!c.isReady) { connect(); return }
    queryProducts(c)
    restore()
  }

  private fun queryProducts(c: BillingClient) {
    val subs = QueryProductDetailsParams.newBuilder().setProductList(
      listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(SUB_ID).setProductType(ProductType.SUBS).build())
    ).build()
    // Play Billing 8: sonuç QueryProductDetailsResult (bulunan + bulunamayan ürünler)
    c.queryProductDetailsAsync(subs) { r, result ->
      if (r.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
      val list = result.productDetailsList
      val subPlans = list.flatMap { pd ->
        pd.subscriptionOfferDetails.orEmpty().map { offer ->
          // Son (ücretli) fiyat aşaması; varsa ücretsiz deneme aşaması
          val phases = offer.pricingPhases.pricingPhaseList
          val paid = phases.lastOrNull()
          val trial = phases.firstOrNull { it.priceAmountMicros == 0L }?.billingPeriod
          Plan(pd.productId, pd.name, paid?.formattedPrice.orEmpty(), paid?.billingPeriod.orEmpty(), trial, pd, offer.offerToken)
        }
      }
        // Aynı dönem için yalnız bir teklif (deneme içeren tercih edilir)
        .groupBy { it.period }.map { (_, v) -> v.firstOrNull { it.freeTrial != null } ?: v.first() }
      merge(subPlans, keepLifetime = true)
    }
    val inapp = QueryProductDetailsParams.newBuilder().setProductList(
      listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(LIFETIME_ID).setProductType(ProductType.INAPP).build())
    ).build()
    c.queryProductDetailsAsync(inapp) { r, result ->
      if (r.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
      val list = result.productDetailsList
      val one = list.mapNotNull { pd ->
        pd.oneTimePurchaseOfferDetails?.let { Plan(pd.productId, pd.name, it.formattedPrice, "", null, pd, null) }
      }
      merge(one, keepLifetime = false)
    }
  }

  @Synchronized
  private fun merge(newPlans: List<Plan>, keepLifetime: Boolean) {
    val kept = _plans.value.filter { if (keepLifetime) it.productId == LIFETIME_ID else it.productId != LIFETIME_ID }
    _plans.value = (kept + newPlans).sortedBy {
      when (it.period) { "P1M" -> 0; "P1Y" -> 1; "" -> 2; else -> 3 }
    }
  }

  /** Satın alımları geri yükle / doğrula (abonelik + ömür boyu). */
  fun restore(onDone: ((Boolean) -> Unit)? = null) {
    val c = client ?: run { onDone?.invoke(false); return }
    if (!c.isReady) { connect(); onDone?.invoke(_isPremium.value); return }
    val pending = java.util.concurrent.atomic.AtomicInteger(2)
    val foundFlag = java.util.concurrent.atomic.AtomicBoolean(false)
    fun finish() {
      if (pending.decrementAndGet() == 0) { val f = foundFlag.get(); setPremium(f); onDone?.invoke(f) }
    }
    listOf(ProductType.SUBS, ProductType.INAPP).forEach { type ->
      c.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(type).build()) { r, purchases ->
        if (r.responseCode == BillingClient.BillingResponseCode.OK) {
          purchases.forEach { p ->
            if (p.purchaseState == Purchase.PurchaseState.PURCHASED &&
              p.products.any { it == SUB_ID || it == LIFETIME_ID }
            ) {
              foundFlag.set(true)
              acknowledge(p)
            }
          }
        } else if (_isPremium.value) {
          // Play'e ulaşılamadıysa mevcut durumu koru (çevrimdışı kullanıcıyı cezalandırma)
          foundFlag.set(true)
        }
        finish()
      }
    }
  }

  fun launch(activity: Activity, plan: Plan) {
    val c = client
    if (c == null || !c.isReady) { _message.value = "store_unavailable"; connect(); return }
    val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(plan.details)
      .apply { plan.offerToken?.let { setOfferToken(it) } }
      .build()
    c.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
  }

  override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
    when (result.responseCode) {
      BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { p ->
        when (p.purchaseState) {
          Purchase.PurchaseState.PURCHASED -> { acknowledge(p); setPremium(true); _message.value = "thanks" }
          Purchase.PurchaseState.PENDING -> _message.value = "pending"
          else -> {}
        }
      }
      BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> { restore(); _message.value = "restored" }
      BillingClient.BillingResponseCode.USER_CANCELED -> {}
      else -> _message.value = "error"
    }
  }

  private fun acknowledge(p: Purchase) {
    if (p.isAcknowledged) return
    // 3 gün içinde onaylanmayan satın almalar Google tarafından iade edilir
    client?.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) { }
  }

  private fun setPremium(active: Boolean) {
    _isPremium.value = active
    if (::appContext.isInitialized) {
      appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("active", active).apply()
    }
  }

  fun consumeMessage() { _message.value = null }
}

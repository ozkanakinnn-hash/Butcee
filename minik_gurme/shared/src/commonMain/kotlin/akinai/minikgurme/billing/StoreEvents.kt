package akinai.minikgurme.billing

import akinai.minikgurme.shared.KeyValueStore

/** Mağazadan gelen bir abonelik ürünü (iOS: StoreKit 2 Product). */
data class StoreProduct(val id: String, val price: String, val period: String)

/**
 * Mağaza olaylarının ortak işlenmesi: ürünler, satın alma sonucu, abonelik durumu → [PlusManager] durumu.
 * iOS'ta Swift köprüsü (iosApp/StoreBridge.swift) bu olayları çağırır; mantık burada olduğu için Android testleriyle doğrulanır.
 * Mesajlar Türkçe kaynak metindir, ekranda çevrilir.
 */
object StoreEvents {
    private const val KEY_CACHE = "plus_active_cache"
    private val prefs by lazy { KeyValueStore("app_prefs") }

    /** Plus durumu değişince (ör. öğün hatırlatıcılarını yeniden kurmak için). */
    var onPlusChanged: (Boolean) -> Unit = {}

    /** Açılışta son bilinen durumu yükler (çevrimdışı da Plus çalışsın). */
    fun loadCache() { PlusManager.isPlus = prefs.getBoolean(KEY_CACHE, false) }

    fun onProducts(products: List<StoreProduct>) {
        PlusManager.plans = products
            .map { PlusManager.Plan(it.id, it.price, it.period) }
            .sortedBy { if (it.productId == PlusManager.PRODUCT_YEARLY) 0 else 1 }
        PlusManager.available = products.isNotEmpty()
    }

    fun onEntitlement(active: Boolean) {
        prefs.putBoolean(KEY_CACHE, active)
        if (PlusManager.isPlus != active) {
            PlusManager.isPlus = active
            onPlusChanged(active)
        }
    }

    /** code: "success" | "pending" | "cancelled" | "failed" */
    fun onPurchaseResult(code: String) {
        PlusManager.message = when (code) {
            "success" -> "Minik Gurme Plus'a hoş geldiniz!"
            "pending" -> "Ödemeniz onay bekliyor. Onaylanınca Plus otomatik açılır."
            "cancelled" -> null
            else -> "Satın alma tamamlanamadı. Lütfen tekrar deneyin."
        }
    }

    fun onRestoreResult(active: Boolean, silent: Boolean = false) {
        if (!silent) PlusManager.message =
            if (active) "Plus aboneliğiniz geri yüklendi." else "Etkin bir Plus aboneliği bulunamadı."
    }
}

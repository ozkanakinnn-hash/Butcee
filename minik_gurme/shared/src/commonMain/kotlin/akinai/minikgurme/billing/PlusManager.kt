package akinai.minikgurme.billing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Minik Gurme Plus aboneliğinin ortak durumu (ekranlar bunu okur).
 * Satın alma platforma özeldir: Android → Google Play Billing, iOS → App Store (StoreKit). Platform [store]'u kurar.
 */
object PlusManager {
    const val PRODUCT_MONTHLY = "minikgurme_plus_monthly"
    const val PRODUCT_YEARLY = "minikgurme_plus_yearly"

    /** Mağazadaki bir plan; [platformData] mağazanın kendi nesnesi (Android ProductDetails vb.). */
    data class Plan(val productId: String, val price: String, val period: String, val platformData: Any? = null)

    interface Store {
        fun purchase(plan: Plan)
        fun restore(silent: Boolean = false)
        fun setDebugOverride(on: Boolean) {}
    }

    /** Plus etkin mi (Compose durumu; değişince ekranlar güncellenir). Platform günceller. */
    var isPlus by mutableStateOf(false)
    var plans by mutableStateOf<List<Plan>>(emptyList())
    /** Kullanıcıya gösterilecek son durum mesajı (Türkçe kaynak; ekranda t() ile çevrilir). */
    var message by mutableStateOf<String?>(null)
    var available by mutableStateOf(false)
    /** Yalnızca test sürümünde: satın almadan Plus'ı dene. */
    var debugOverrideEnabled by mutableStateOf(false)

    var store: Store = object : Store {
        override fun purchase(plan: Plan) { message = "Satın alma şu an kullanılamıyor." }
        override fun restore(silent: Boolean) {}
    }

    fun purchase(plan: Plan) = store.purchase(plan)
    fun restore(silent: Boolean = false) = store.restore(silent)
    fun setDebugOverride(on: Boolean) = store.setDebugOverride(on)
}

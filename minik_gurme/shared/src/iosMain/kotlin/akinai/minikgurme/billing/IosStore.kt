package akinai.minikgurme.billing

import akinai.minikgurme.reminders.IosReminders

/**
 * Swift tarafının (iosApp/StoreBridge.swift, StoreKit 2) uyguladığı köprü. Sonuçlar [StoreEvents] üzerinden döner.
 * StoreKit 2 yalnızca Swift'ten kullanılabildiği için ödeme çağrıları Swift'te yapılır.
 */
interface IosStoreBridge {
    fun start(productIds: List<String>)
    fun purchase(productId: String)
    /** Mağazayla eşitleyip etkin aboneliği arar; bitince StoreEvents.onRestoreResult çağrılır. */
    fun restore()
}

/** iPhone'da [PlusManager]'ın mağazası: App Store aboneliği. */
object IosStore : PlusManager.Store {
    /** Swift uygulaması açılırken atanır. */
    var bridge: IosStoreBridge? = null

    private val productIds = listOf(PlusManager.PRODUCT_YEARLY, PlusManager.PRODUCT_MONTHLY)

    fun start() {
        PlusManager.store = this
        StoreEvents.loadCache()
        StoreEvents.onPlusChanged = { IosReminders.reschedule() }
        bridge?.start(productIds)
    }

    override fun purchase(plan: PlusManager.Plan) {
        val b = bridge
        if (b == null || !PlusManager.available) {
            PlusManager.message = "Satın alma şu an kullanılamıyor. İnternet bağlantınızı kontrol edip tekrar deneyin."
            return
        }
        b.purchase(plan.productId)
    }

    override fun restore(silent: Boolean) {
        val b = bridge
        if (b == null) {
            if (!silent) PlusManager.message = "App Store'a bağlanılamadı. İnternet bağlantınızı kontrol edip tekrar deneyin."
            return
        }
        b.restore()
    }
}

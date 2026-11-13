package akinai.minikgurme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.billing.PlusManager
import akinai.minikgurme.billing.StoreEvents
import akinai.minikgurme.billing.StoreProduct
import akinai.minikgurme.data.local.PlatformContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** iPhone aboneliğinin ortak durum mantığı (Swift köprüsü bu olayları çağırır). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StoreEventsTest {
    private fun reset() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        PlatformContext.init(ctx)
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        PlusManager.isPlus = false; PlusManager.plans = emptyList(); PlusManager.message = null; PlusManager.available = false
        StoreEvents.onPlusChanged = {}
    }

    @Test
    fun productsAreSortedYearlyFirst() {
        reset()
        StoreEvents.onProducts(listOf(StoreProduct("minikgurme_plus_monthly", "₺59,99", "P1M"), StoreProduct("minikgurme_plus_yearly", "₺399,99", "P1Y")))
        assertEquals(listOf("minikgurme_plus_yearly", "minikgurme_plus_monthly"), PlusManager.plans.map { it.productId })
        assertTrue(PlusManager.available)
        StoreEvents.onProducts(emptyList()); assertFalse(PlusManager.available)
    }

    @Test
    fun entitlementTurnsPlusOnOffCachesAndNotifies() {
        reset()
        val changes = mutableListOf<Boolean>(); StoreEvents.onPlusChanged = { changes += it }
        StoreEvents.onEntitlement(true); StoreEvents.onEntitlement(true)
        assertTrue(PlusManager.isPlus); assertEquals(listOf(true), changes)
        // Uygulama yeniden açılınca önbellekten (çevrimdışı) Plus devam eder
        PlusManager.isPlus = false; StoreEvents.loadCache(); assertTrue(PlusManager.isPlus)
        StoreEvents.onEntitlement(false)   // süre doldu / iade
        assertFalse(PlusManager.isPlus); assertEquals(listOf(true, false), changes)
        PlusManager.isPlus = true; StoreEvents.loadCache(); assertFalse(PlusManager.isPlus)
    }

    @Test
    fun purchaseAndRestoreMessages() {
        reset()
        StoreEvents.onPurchaseResult("success"); assertEquals("Minik Gurme Plus'a hoş geldiniz!", PlusManager.message)
        StoreEvents.onPurchaseResult("pending"); assertTrue(PlusManager.message!!.contains("onay bekliyor"))
        StoreEvents.onPurchaseResult("cancelled"); assertNull(PlusManager.message)
        StoreEvents.onPurchaseResult("failed"); assertTrue(PlusManager.message!!.contains("tamamlanamadı"))
        StoreEvents.onRestoreResult(false); assertTrue(PlusManager.message!!.contains("bulunamadı"))
        StoreEvents.onRestoreResult(true); assertTrue(PlusManager.message!!.contains("geri yüklendi"))
    }
}

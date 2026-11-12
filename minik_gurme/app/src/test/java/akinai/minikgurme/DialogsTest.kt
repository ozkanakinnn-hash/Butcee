package akinai.minikgurme

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.ui.components.AddGrowthRecordDialog
import akinai.minikgurme.ui.components.EditProfileDialog
import akinai.minikgurme.ui.theme.MinikGurmeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Diyaloglar ayrı test edilir: Robolectric'te ekran niteleyicisi (w411dp-xxhdpi) verilince
 * metin alanlı her diyalog (en sade AlertDialog dahil) "boşta değil" hatasıyla takılıyor; bu bir test aracı kısıtı.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DialogsTest {
    @get:Rule val compose = createComposeRule()

    private fun has(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun editGrowthRecordIsPrefilledAndSaves() {
        var saved: Triple<Float, Float, Int>? = null
        val rec = GrowthRecord(id = 3, date = "2026-11-09", weightKg = 5.5f, heightCm = 65f, ageMonths = 6)
        compose.setContent {
            MinikGurmeTheme {
                AddGrowthRecordDialog(editing = rec, onDismiss = {}, onSave = { _, w, h, _, age, _ -> saved = Triple(w, h, age) })
            }
        }
        compose.waitForIdle()
        assertTrue(has("Ölçümü Düzenle"))
        assertTrue(has("5.50")); assertTrue(has("65.0"))
        compose.onNodeWithTag("growth_input_weight").performClick()
        compose.onAllNodesWithText("Kaydet").fetchSemanticsNodes().let { assertTrue(it.isNotEmpty()) }
        compose.onAllNodes(androidx.compose.ui.test.hasText("Kaydet") and androidx.compose.ui.test.hasClickAction())[0].performClick()
        compose.waitForIdle()
        assertEquals(Triple(5.5f, 65f, 6), saved)
    }

    @Test
    fun editProfileDialogOpens() {
        compose.setContent {
            MinikGurmeTheme { EditProfileDialog(initialName = "Kübra", initialAge = 6, initialStartDate = "2026-11-09", onDismiss = {}, onSave = { _, _, _, _ -> }) }
        }
        compose.waitForIdle()
        assertTrue(has("Kübra"))
    }
}

package com.example

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.i18n.AppLanguage
import com.example.i18n.Translator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Uygulamayı açar, her sekmeyi Türkçe ve İngilizce gezer, dil seçiciyi dener (çökme kontrolü). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class AppSmokeTest {
    @get:Rule val compose = createEmptyComposeRule()

    private val tabs = listOf("nav_today", "nav_foods100", "nav_recipes", "nav_recommendations", "nav_guide")

    private fun launch(lang: String): ActivityScenario<MainActivity> {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(ctx, MainActivity::class.java).putExtra("lang", lang)
        return ActivityScenario.launch(intent)
    }

    private fun visitAllTabs() {
        for (tag in tabs) {
            compose.onNodeWithTag(tag).performClick()
            compose.waitForIdle()
        }
        compose.onNodeWithTag("top_profile_pill").performClick()
        compose.waitForIdle()
    }

    @Test
    fun turkishTabsOpen() {
        launch("tr").use {
            compose.waitForIdle()
            assertTrue(compose.onAllNodesWithText("Öğünler").fetchSemanticsNodes().isNotEmpty())
            visitAllTabs()
        }
    }

    @Test
    fun englishTabsOpenAndTranslate() {
        launch("en").use {
            compose.waitForIdle()
            assertEquals("en", AppLanguage.current)
            assertTrue(compose.onAllNodesWithText("Meals").fetchSemanticsNodes().isNotEmpty())
            assertTrue(compose.onAllNodesWithText("Recipes").fetchSemanticsNodes().isNotEmpty())
            visitAllTabs()
            assertTrue(compose.onAllNodesWithText("Baby Profile & Statistics").fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun languageSwitchInProfile() {
        launch("tr").use {
            compose.waitForIdle()
            compose.onNodeWithTag("top_profile_pill").performClick()
            compose.waitForIdle()
            compose.onNodeWithTag("lang_en").performClick()
            compose.waitForIdle()
            assertEquals("en", AppLanguage.current)
            assertTrue(compose.onAllNodesWithText("Meals").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithTag("lang_tr").performClick()
            compose.waitForIdle()
            assertTrue(compose.onAllNodesWithText("Öğünler").fetchSemanticsNodes().isNotEmpty())
        }
    }

    @Test
    fun templatePatternsTranslate() {
        launch("en").use {
            assertEquals("Day 5 saved.", Translator.toEnglish("5. gün kaydedildi."))
        }
    }
}

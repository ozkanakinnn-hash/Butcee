package akinai.minikgurme

import akinai.minikgurme.data.local.closeAll
import akinai.minikgurme.data.local.getDatabase
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.community.Community
import akinai.minikgurme.community.ContentFilter
import akinai.minikgurme.data.local.AppDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Ebeveyn Topluluğu: katılım, gönderi, yorum, beğeni, şikâyet, engelleme, moderasyon ve hesap silme (sahte arka uçla). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class CommunityTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val ctx get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var fake: FakeCommunityBackend
    private val original = Community.backend
    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        TestResources.init()
        AppDatabase.closeAll()
        ctx.deleteDatabase("minik_gurme_db")
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().putBoolean(MainActivity.KEY_ONBOARDED, true)
            .putBoolean("plus_debug_override", true).commit() // yazmak Plus'a özel
        ctx.getSharedPreferences("community", Context.MODE_PRIVATE).edit().clear().commit()
        fake = FakeCommunityBackend()
        Community.backend = fake
    }

    @After
    fun tearDown() { Community.backend = original }

    private fun launch(lang: String = "tr") =
        ActivityScenario.launch<MainActivity>(Intent(ctx, MainActivity::class.java).putExtra("lang", lang)).also { scenario = it }

    private fun has(text: String) = compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    private fun hasTag(tag: String) = compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    private fun shot(name: String) {
        val dir = File(System.getProperty("shots.dir") ?: "build/smoke-shots").apply { mkdirs() }
        compose.waitForIdle()
        scenario?.onActivity { a ->
            val v = a.window.decorView
            val bmp = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(android.graphics.Canvas(bmp))
            File(dir, "$name.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    private fun setPlus(on: Boolean) =
        ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putBoolean("plus_debug_override", on).commit()

    private fun joinAs(nick: String) {
        compose.onNodeWithTag("nav_community").performClick(); compose.waitForIdle()
        compose.onNodeWithTag("community_open_join").performClick(); compose.waitForIdle()
        compose.onNodeWithTag("community_nick").performTextInput(nick)
        compose.onNodeWithTag("community_accept").performScrollTo().performClick()
        compose.onNodeWithTag("community_join").performScrollTo().performClick()
        compose.waitUntil(5000) { hasTag("community_new_post") }
    }

    private fun seedSample() {
        fake.seed("m6", "u1", "Elif (Ada'nın annesi)", "Brokoliyi buharda 8 dakika pişirip parmak boyunda verdim, çok sevdi! Siz hangi sebzeyle başladınız?", minutesAgo = 25, likes = listOf("u2", "u3", "u4"), commentCount = 2)
        fake.seed("m6", "u2", "Burak", "6. ayda yumurtaya başlamak için önce sarısını mı vermeliyim, yoksa bütün olarak mı? Doktorumuz bütün olarak olabilir dedi ama emin olamadım.", minutesAgo = 140, likes = listOf("u1"), commentCount = 1)
        fake.seed("m6", "u3", "Zeynep", "Kabak püresine biraz zeytinyağı eklemek iyi fikir mi?", minutesAgo = 60 * 26)
    }

    @Test
    fun joinPostCommentLike() {
        seedSample()
        launch().use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_community").performClick(); compose.waitForIdle()
            // Okumak için katılmak gerekmez
            assertTrue(has("Brokoliyi buharda")); assertFalse(fake.signedIn)
            compose.onNodeWithTag("community_open_join").performClick(); compose.waitForIdle()
            assertTrue(has("Ebeveyn Topluluğu"))
            shot("tr_light_community_join")
            // Kurallar kabul edilmeden katılınamaz
            compose.onNodeWithTag("community_nick").performTextInput("Defne'nin annesi")
            compose.onNodeWithTag("community_join").performScrollTo().performClick(); compose.waitForIdle()
            assertFalse(Community.joined)
            compose.onNodeWithTag("community_accept").performScrollTo().performClick()
            compose.onNodeWithTag("community_join").performScrollTo().performClick()
            compose.waitUntil(5000) { hasTag("community_new_post") }
            assertTrue(Community.joined); assertEquals("Defne'nin annesi", Community.nick); assertTrue(fake.signedIn)
            assertTrue(has("Brokoliyi buharda"))
            shot("tr_light_community_feed")

            // Yeni gönderi
            compose.onNodeWithTag("community_new_post").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("community_post_input").performTextInput("Avokadoyu ezip mi verdiniz, dilim olarak mı? 7 aylık, BLW yapıyoruz.")
            compose.onNodeWithTag("compose_topic_blw").performClick()
            shot("tr_light_community_compose")
            compose.onNodeWithTag("community_post_send").performClick()
            compose.waitUntil(5000) { hasTag("community_new_post") }
            val mine = fake.posts.value.single { it.uid == "me" }
            assertEquals("blw", mine.topic); assertEquals("Defne'nin annesi", mine.nick)
            assertTrue("Yeni gönderinin başlığı açılmalı", has("Avokadoyu ezip"))

            // Bekleme süresi: hemen ikinci paylaşım engellenir
            compose.onNodeWithTag("community_new_post").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("community_post_input").performTextInput("İkinci deneme mesajı")
            compose.onNodeWithTag("community_post_send").performClick(); compose.waitForIdle()
            assertTrue(has("saniye sonra"))
            assertEquals(1, fake.posts.value.count { it.uid == "me" })
            compose.onNodeWithTag("community_back").performClick(); compose.waitForIdle()

            // Başka başlıktaki gönderiyi aç, beğen, yorum yaz
            compose.onNodeWithTag("community_topic_m6").performClick(); compose.waitForIdle()
            val elif = fake.posts.value.first { it.uid == "u1" }
            compose.onNodeWithTag("like_${elif.id}").performClick(); compose.waitForIdle()
            assertTrue("me" in fake.posts.value.first { it.id == elif.id }.likedBy)
            fake.seedComment("m6", elif.id, "u2", "Burak", "Biz havuçla başladık, tatlı olduğu için hemen kabul etti.")
            compose.onNodeWithTag("community_post_${elif.id}").performClick(); compose.waitForIdle()
            assertTrue(has("Biz havuçla başladık"))
            Community.markWrote(0) // bekleme süresini sıfırla
            compose.onNodeWithTag("community_comment_input").performTextInput("Biz de brokoliyle başladık, sapından tutmak kolay oluyor.")
            compose.onNodeWithTag("community_comment_send").performClick()
            compose.waitUntil(5000) { has("sapından tutmak kolay") }
            assertEquals(1, Community.myComments.size)
            shot("tr_light_community_detail")
        }
    }

    @Test
    fun filterBlocksLinksPhonesAndProfanity() {
        assertNotNull(ContentFilter.check("Ucuz mama için www.ornek.com adresine bakın", 2000))
        assertNotNull(ContentFilter.check("Bana 0532 123 45 67 numarasından ulaşın", 2000))
        assertNotNull(ContentFilter.check("anne@example.com yazın", 2000))
        assertNotNull(ContentFilter.check("Bu ne biçim s1ktir cevap", 2000))
        assertNotNull(ContentFilter.check("a", 2000))
        // Masum kelimeler engellenmemeli (eksik, götürmek, sıkıntı, Ahmet, 6. ay, 120 ml)
        assertNull(ContentFilter.check("Kaşığı götürmekte zorlanıyor, eksik bir şey mi yapıyoruz? 6. ay, günde 120 ml su.", 2000))
        assertNull(ContentFilter.check("I'm worried about the shitake... no, shiitake mushrooms. Are they safe at 9 months?", 2000))
        assertNotNull(ContentFilter.checkNick("x"))
        assertNull(ContentFilter.checkNick("Defne'nin annesi"))
    }

    @Test
    fun reportHidesAndBlockRemovesUser() {
        seedSample()
        val spam = fake.seed("m6", "u9", "Satıcı", "Harika ürünler için profilime bakın", minutesAgo = 5)
        val hidden = fake.seed("m6", "u8", "Kaba", "Çok şikâyet alan gönderi", minutesAgo = 4, reports = listOf("a", "b", "c"))
        launch().use {
            compose.waitForIdle()
            joinAs("Test")
            assertTrue(has("Harika ürünler")); assertFalse("3 şikâyet alan gönderi gizli olmalı", has("Çok şikâyet alan"))
            // Şikâyet et → sebep seç → kendisi için hemen gizlenir
            compose.onNodeWithTag("menu_post_$spam").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("menu_report").performClick(); compose.waitForIdle()
            assertTrue(has("Neden şikâyet ediyorsunuz?"))
            shot("tr_light_community_report")
            compose.onAllNodesWithText("Spam / reklam").fetchSemanticsNodes().let { assertTrue(it.isNotEmpty()) }
            compose.onNodeWithTag("report_confirm").performClick(); compose.waitForIdle()
            assertFalse(has("Harika ürünler"))
            assertEquals(1, fake.reports.size)
            // Engelle → kullanıcının tüm gönderileri kaybolur
            val burak = fake.posts.value.first { it.uid == "u2" }
            compose.onNodeWithTag("menu_post_${burak.id}").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("menu_block").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("block_confirm").performClick(); compose.waitForIdle()
            assertFalse(has("yumurtaya başlamak"))
            assertTrue("u2" in Community.blocked)
            assertTrue(hidden.isNotEmpty())
        }
    }

    @Test
    fun moderatorSeesHiddenAndApproves() {
        fake.moderator = true
        val id = fake.seed("m6", "u8", "Ayşe", "Yanlışlıkla şikâyet edilen gönderi", minutesAgo = 4, reports = listOf("a", "b", "c"))
        launch().use {
            compose.waitForIdle()
            joinAs("Moderatör")
            compose.waitUntil(5000) { has("Herkesten gizlendi") }
            shot("tr_light_community_moderator")
            compose.onNodeWithTag("mod_approve").performClick(); compose.waitForIdle()
            assertTrue(fake.posts.value.first { it.id == id }.modApproved)
            assertFalse(has("Herkesten gizlendi"))
        }
    }

    @Test
    fun deleteCommunityAccountFromSettings() {
        launch().use {
            compose.waitForIdle()
            joinAs("Silinecek")
            kotlinx.coroutines.runBlocking { fake.createPost("allergy", "Silinecek", "Benim gönderim", "tr") }
            compose.onNodeWithTag("top_settings").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("settings_delete_community").performScrollTo()
            shot("tr_light_community_settings")
            compose.onNodeWithTag("settings_delete_community").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("settings_confirm").performClick()
            compose.waitUntil(5000) { !Community.joined }
            assertTrue(fake.accountDeleted)
            assertTrue(fake.posts.value.none { it.uid == "me" })
            assertFalse(hasTag("settings_delete_community"))
        }
    }

    @Test
    fun readingIsFreeWritingNeedsPlus() {
        setPlus(false)
        seedSample()
        launch().use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_community").performClick(); compose.waitForIdle()
            assertTrue("Plus olmadan okunabilmeli", has("Brokoliyi buharda"))
            assertFalse("Okumak hesap açmamalı", fake.signedIn)
            assertTrue(has("Plus üyelerine özel"))
            shot("tr_light_community_free_feed")
            // Gönderi detayı açılır, yorum alanı kilitli
            val elif = fake.posts.value.first { it.uid == "u1" }
            compose.onNodeWithTag("community_post_${elif.id}").performClick(); compose.waitForIdle()
            assertTrue(hasTag("community_comment_locked")); assertFalse(hasTag("community_comment_input"))
            shot("tr_light_community_free_detail")
            compose.onNodeWithTag("community_back").performClick(); compose.waitForIdle()
            // Gönderi yaz → Plus sayfası
            compose.onNodeWithTag("community_new_post").performClick(); compose.waitForIdle()
            assertTrue(has("Minik Gurme Plus")); assertTrue(has("Toplulukta soru sor"))
            assertTrue(fake.posts.value.none { it.uid == "me" })
        }
    }

    @Test
    fun freeUserCanJoinToLikeAndReport() {
        setPlus(false)
        seedSample()
        launch().use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_community").performClick(); compose.waitForIdle()
            val elif = fake.posts.value.first { it.uid == "u1" }
            // Beğenmek için katılım istenir, sonra beğeni çalışır
            compose.onNodeWithTag("like_${elif.id}").performClick(); compose.waitForIdle()
            assertTrue(hasTag("community_nick"))
            compose.onNodeWithTag("community_nick").performTextInput("Ücretsiz")
            compose.onNodeWithTag("community_accept").performScrollTo().performClick()
            compose.onNodeWithTag("community_join").performScrollTo().performClick()
            compose.waitUntil(5000) { hasTag("like_${elif.id}") }
            compose.onNodeWithTag("like_${elif.id}").performClick(); compose.waitForIdle()
            assertTrue("me" in fake.posts.value.first { it.id == elif.id }.likedBy)
        }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun darkAndEnglishFeed() {
        seedSample()
        launch("en").use {
            compose.waitForIdle()
            compose.onNodeWithTag("nav_community").performClick(); compose.waitForIdle()
            compose.onNodeWithTag("community_open_join").performClick(); compose.waitForIdle()
            assertTrue(has("Parent Community"))
            shot("en_dark_community_join")
            compose.onNodeWithTag("community_nick").performTextInput("Ada's mum")
            compose.onNodeWithTag("community_accept").performScrollTo().performClick()
            compose.onNodeWithTag("community_join").performScrollTo().performClick()
            compose.waitUntil(5000) { hasTag("community_new_post") }
            assertTrue("FAB", compose.onAllNodesWithText("Write a post", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()); assertTrue("chip", has("6 months"))
            // Kullanıcı içeriği çevrilmez
            assertTrue(has("Brokoliyi buharda"))
            shot("en_dark_community_feed")
        }
    }
}

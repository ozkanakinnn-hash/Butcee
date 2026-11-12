package akinai.minikgurme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import akinai.minikgurme.community.RestCommunityBackend
import akinai.minikgurme.data.local.PlatformContext
import akinai.minikgurme.shared.KeyValueStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * iOS'ta kullanılan Firebase REST topluluk arka ucunu, gerçek güvenlik kurallarıyla Firebase emülatöründe dener.
 * Yalnızca emülatörle çalıştırılınca koşar:
 *   firebase emulators:exec --only auth,firestore "./gradlew :app:testDebugUnitTest --tests '*RestCommunityEmulatorTest'"
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RestCommunityEmulatorTest {
    private val fsHost = System.getenv("FIRESTORE_EMULATOR_HOST")
    private val authHost = System.getenv("FIREBASE_AUTH_EMULATOR_HOST")
    private val project = "demo-minik"
    private val http = HttpClient(OkHttp)

    private fun backend(name: String) = RestCommunityBackend(
        apiKey = "fake-key", projectId = project, http = http,
        authBase = "http://$authHost/identitytoolkit.googleapis.com/v1",
        tokenBase = "http://$authHost/securetoken.googleapis.com/v1",
        firestoreHost = "http://$fsHost", pollMillis = 300_000,
        store = KeyValueStore("fa_$name")
    )

    private suspend fun makeModerator(uid: String) {
        // Emülatörde "owner" jetonu kuralları atlar (yalnızca test kurulumu için)
        http.patch("http://$fsHost/v1/projects/$project/databases/(default)/documents/mods/$uid") {
            header("Authorization", "Bearer owner"); header("Content-Type", "application/json")
            setBody("""{"fields":{"ad":{"stringValue":"test"}}}""")
        }
    }

    @Test
    fun fullCommunityFlowAgainstRealRules() = runBlocking {
        assumeTrue("Firebase emülatörü çalışmıyor", fsHost != null && authHost != null)
        PlatformContext.init(ApplicationProvider.getApplicationContext<Context>())
        val alice = backend("alice"); val bob = backend("bob"); val reader = backend("reader")

        // Hesapsız okuma serbest
        assertEquals(emptyList<Any>(), reader.posts("m6").first())
        assertNull(reader.currentUid())

        val aliceId = alice.signIn(); val bobId = bob.signIn()
        assertTrue(aliceId.isNotBlank() && aliceId != bobId)

        val postId = alice.createPost("m6", "Alice", "Brokoliyle başladık, çok sevdi!", "tr")
        val p = reader.posts("m6").first().single()
        assertEquals(postId, p.id); assertEquals(aliceId, p.uid); assertEquals("Alice", p.nick)
        assertTrue("sunucu zamanı yazılmalı", p.createdAt > 1_700_000_000_000)

        // Beğeni (yalnızca kendi kimliği), yorum + sayaç
        bob.setLiked("m6", postId, true)
        assertEquals(listOf(bobId), reader.post("m6", postId).first()!!.likedBy)
        bob.setLiked("m6", postId, false)
        assertTrue(reader.post("m6", postId).first()!!.likedBy.isEmpty())
        val cId = bob.addComment("m6", postId, "Bob", "Biz de havuçla başladık.")
        assertEquals(1, reader.post("m6", postId).first()!!.commentCount)
        assertEquals("Biz de havuçla başladık.", reader.comments("m6", postId).first().single().text)

        // Şikâyet: listeye kendi kimliği eklenir; ikinci kez şikâyet reddedilir
        bob.report("m6", postId, null, "Spam / reklam")
        assertEquals(listOf(bobId), reader.post("m6", postId).first()!!.reportedBy)
        assertTrue(runCatching { bob.report("m6", postId, null, "Spam / reklam") }.isFailure)

        // Moderatör olmayan onaylayamaz; moderatör onaylar
        assertFalse(bob.isModerator())
        assertTrue(runCatching { bob.approve("m6", postId, null) }.isFailure)
        makeModerator(aliceId)
        assertTrue(alice.isModerator())
        alice.approve("m6", postId, null)
        val approved = reader.post("m6", postId).first()!!
        assertTrue(approved.modApproved); assertTrue(approved.reportedBy.isEmpty())

        // Başkasının gönderisini silemez; kendi yorumunu siler
        assertTrue(runCatching { bob.deletePost("m6", postId) }.isFailure)
        bob.deleteComment("m6", postId, cId)
        assertEquals(0, reader.post("m6", postId).first()!!.commentCount)

        // Hesap silme: tüm gönderiler gider, oturum kapanır
        alice.createPost("blw", "Alice", "İkinci gönderi", "tr")
        alice.deleteAccount(emptyList())
        assertTrue(reader.posts("m6").first().isEmpty()); assertTrue(reader.posts("blw").first().isEmpty())
        assertNull(alice.currentUid())
        assertNotNull(bob.currentUid())
    }
}

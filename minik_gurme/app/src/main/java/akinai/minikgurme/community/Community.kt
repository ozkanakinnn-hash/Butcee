package akinai.minikgurme.community

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import akinai.minikgurme.i18n.AppLanguage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Ebeveyn Topluluğu (uygulama içi forum).
 *
 * Firebase anonim giriş + Firestore. Yapı:
 *   topics/{topic}/posts/{postId}                  gönderi
 *   topics/{topic}/posts/{postId}/comments/{id}    yorum
 *   reports/{id}                                   şikâyet kaydı (yalnızca moderatör okur)
 *   mods/{uid}                                     moderatör listesi (yalnızca konsoldan eklenir)
 * Güvenlik kuralları: docs/minik-gurme/firestore.rules
 *
 * Bebek profili ve sağlık kayıtları buraya ASLA gönderilmez; yalnızca kullanıcının yazdığı metin ve takma adı gider.
 */

data class CommunityPost(
    val id: String,
    val topic: String,
    val uid: String,
    val nick: String,
    val text: String,
    val createdAt: Long,
    val likedBy: List<String> = emptyList(),
    val reportedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val modApproved: Boolean = false,
    val lang: String = "tr"
)

data class CommunityComment(
    val id: String,
    val uid: String,
    val nick: String,
    val text: String,
    val createdAt: Long,
    val reportedBy: List<String> = emptyList(),
    val modApproved: Boolean = false
)

object CommunityConfig {
    /** true: okumak herkese açık, yazmak yalnızca Plus üyelerine. */
    const val WRITE_PLUS_ONLY = true
    /** Bu kadar farklı kişi şikâyet edince içerik otomatik gizlenir (moderatör onaylayana kadar). */
    const val HIDE_AFTER_REPORTS = 3
    const val POST_MAX = 2000
    const val COMMENT_MAX = 1000
    const val NICK_MIN = 2
    const val NICK_MAX = 30
    /** İki paylaşım arasındaki en kısa süre (spam önleme). */
    const val COOLDOWN_MS = 20_000L
}

/** Konu başlıkları: anahtar (Firestore'daki ad, DEĞİŞTİRMEYİN) → Türkçe etiket. */
val COMMUNITY_TOPICS = listOf(
    "m6" to "6. ay",
    "m7_9" to "7–9 ay",
    "m10_12" to "10–12 ay",
    "blw" to "BLW",
    "allergy" to "Alerji",
    "recipes" to "Tarifler",
    "sleep" to "Uyku & rutin"
)

fun topicLabel(key: String) = COMMUNITY_TOPICS.firstOrNull { it.first == key }?.second ?: key

/** Bebeğin ayına göre ilk açılacak başlık. */
fun defaultTopicFor(ageMonths: Int) = when {
    ageMonths <= 6 -> "m6"
    ageMonths <= 9 -> "m7_9"
    else -> "m10_12"
}

interface CommunityBackend {
    fun currentUid(): String?
    suspend fun signIn(): String
    fun posts(topic: String): Flow<List<CommunityPost>>
    fun post(topic: String, postId: String): Flow<CommunityPost?>
    fun comments(topic: String, postId: String): Flow<List<CommunityComment>>
    suspend fun isModerator(): Boolean
    suspend fun createPost(topic: String, nick: String, text: String, lang: String): String
    suspend fun addComment(topic: String, postId: String, nick: String, text: String): String
    suspend fun setLiked(topic: String, postId: String, liked: Boolean)
    suspend fun report(topic: String, postId: String, commentId: String?, reason: String)
    suspend fun deletePost(topic: String, postId: String)
    suspend fun deleteComment(topic: String, postId: String, commentId: String)
    /** Moderatör: şikâyetleri temizleyip içeriği yeniden görünür yapar. */
    suspend fun approve(topic: String, postId: String, commentId: String?)
    /** Kullanıcının tüm gönderilerini, verilen yorumlarını ve hesabını siler. */
    suspend fun deleteAccount(myCommentPaths: Collection<String>)
}

/** Uygulama genelindeki topluluk durumu (takma ad, engellenenler, şikâyet edilenler). */
object Community {
    private const val PREFS = "community"

    /** Testlerde sahte arka uçla değiştirilir. */
    var backend: CommunityBackend by mutableStateOf(LazyFirestore)

    var nick by mutableStateOf("")
        private set
    var joined by mutableStateOf(false)
        private set
    var blocked by mutableStateOf(emptySet<String>())
        private set
    var reported by mutableStateOf(emptySet<String>())
        private set
    var myComments by mutableStateOf(emptySet<String>())
        private set
    private var lastWrite = 0L

    fun init(context: Context) {
        val p = prefs(context)
        nick = p.getString("nick", "") ?: ""
        joined = p.getBoolean("joined", false) && nick.isNotBlank()
        blocked = p.getStringSet("blocked", emptySet())!!.toSet()
        reported = p.getStringSet("reported", emptySet())!!.toSet()
        myComments = p.getStringSet("my_comments", emptySet())!!.toSet()
        lastWrite = p.getLong("last_write", 0L)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun join(context: Context, newNick: String) {
        nick = newNick.trim(); joined = true
        prefs(context).edit().putString("nick", nick).putBoolean("joined", true).apply()
    }

    fun rename(context: Context, newNick: String) {
        nick = newNick.trim()
        prefs(context).edit().putString("nick", nick).apply()
    }

    fun block(context: Context, uid: String) {
        blocked = blocked + uid
        prefs(context).edit().putStringSet("blocked", blocked).apply()
    }

    fun clearBlocked(context: Context) {
        blocked = emptySet()
        prefs(context).edit().putStringSet("blocked", blocked).apply()
    }

    fun markReported(context: Context, key: String) {
        reported = reported + key
        prefs(context).edit().putStringSet("reported", reported).apply()
    }

    fun rememberComment(context: Context, path: String) {
        myComments = myComments + path
        prefs(context).edit().putStringSet("my_comments", myComments).apply()
    }

    fun forgetComment(context: Context, path: String) {
        myComments = myComments - path
        prefs(context).edit().putStringSet("my_comments", myComments).apply()
    }

    /** Spam önleme: son paylaşımdan bu yana kalan bekleme (ms), 0 = yazabilir. */
    fun cooldownLeft(now: Long = System.currentTimeMillis()): Long =
        (lastWrite + CommunityConfig.COOLDOWN_MS - now).coerceAtLeast(0)

    fun markWrote(context: Context, now: Long = System.currentTimeMillis()) {
        lastWrite = now
        prefs(context).edit().putLong("last_write", now).apply()
    }

    /** Topluluktan ayrılınca cihazdaki tüm topluluk bilgisini siler. */
    fun leave(context: Context) {
        prefs(context).edit().clear().apply()
        init(context)
    }

    fun postKey(topic: String, postId: String) = "post:$topic/$postId"
    fun commentKey(topic: String, postId: String, commentId: String) = "comment:$topic/$postId/$commentId"

    /** Kullanıcıya gösterilsin mi? (engelli kullanıcı, kendi şikâyet ettiği ya da çok şikâyet alan içerik gizlenir) */
    fun isVisible(authorUid: String, reportedBy: List<String>, approved: Boolean, key: String, isMod: Boolean): Boolean {
        if (authorUid in blocked || key in reported) return false
        return isMod || !isAutoHidden(reportedBy, approved)
    }

    fun isAutoHidden(reportedBy: List<String>, approved: Boolean) =
        !approved && reportedBy.toSet().size >= CommunityConfig.HIDE_AFTER_REPORTS
}

/** Paylaşmadan önce metni denetler. Sorun varsa Türkçe hata mesajı döner, yoksa null. */
object ContentFilter {
    // Tam kelime olarak engellenenler (kısa olduklarından masum kelimelerin içinde geçebilirler)
    private val exactWords = setOf(
        "amk", "aq", "amq", "sik", "sikik", "göt", "götü", "oç", "piç", "ibne", "gavat", "pic",
        "fuck", "fck", "shit", "dick", "cunt", "slut", "twat"
    )
    // Bu köklerle başlayan kelimeler engellenir
    private val prefixes = listOf(
        "orospu", "yarrak", "yarak", "pezevenk", "kahpe", "şerefsiz", "yavşak", "amına", "amcık", "siktir",
        "sikerim", "sikeyim", "siktiğ", "götveren", "kaltak", "fucking", "motherfuck", "bitch", "asshole", "bastard", "whore"
    )
    private val link = Regex("""(https?://|www\.|\b[\p{L}\d-]+\.(com|net|org|tr|io|ly|me|app|shop|info|biz|co)\b)""", RegexOption.IGNORE_CASE)
    private val email = Regex("""[\w.+-]+@[\w-]+\.[\w.]+""")
    private val phone = Regex("""(\+?\d[\d\s().-]{8,}\d)""")

    fun check(text: String, max: Int): String? {
        val trimmed = text.trim()
        if (trimmed.length < 2) return "Lütfen en az 2 karakter yazın."
        if (trimmed.length > max) return "Metin çok uzun."
        if (link.containsMatchIn(trimmed)) return "Bağlantı ve reklam paylaşımına izin verilmiyor."
        if (email.containsMatchIn(trimmed) || phone.containsMatchIn(trimmed)) return "Güvenliğiniz için telefon numarası veya e-posta paylaşmayın."
        if (hasProfanity(trimmed)) return "Lütfen saygılı bir dil kullanın."
        return null
    }

    fun checkNick(nick: String): String? {
        val n = nick.trim()
        if (n.length < CommunityConfig.NICK_MIN || n.length > CommunityConfig.NICK_MAX) return "Takma ad 2–30 karakter olmalı."
        if (hasProfanity(n) || link.containsMatchIn(n)) return "Bu takma ad kullanılamaz."
        return null
    }

    fun hasProfanity(text: String): Boolean {
        val words = text.lowercase(Locale.forLanguageTag("tr"))
            .replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a').replace('@', 'a').replace('$', 's')
            .split(Regex("""[^\p{L}]+""")).filter { it.isNotEmpty() }
        return words.any { w -> w in exactWords || prefixes.any { w.startsWith(it) } }
    }
}

/** "şimdi", "5 dk", "3 sa", "2 g" ya da tarih. */
fun communityTimeAgo(time: Long, now: Long = System.currentTimeMillis()): String {
    val en = AppLanguage.current == "en"
    val min = ((now - time) / 60_000).coerceAtLeast(0)
    return when {
        min < 1 -> if (en) "now" else "şimdi"
        min < 60 -> if (en) "${min}m" else "$min dk"
        min < 60 * 24 -> if (en) "${min / 60}h" else "${min / 60} sa"
        min < 60 * 24 * 7 -> if (en) "${min / (60 * 24)}d" else "${min / (60 * 24)} g"
        else -> SimpleDateFormat("d MMM", Locale.forLanguageTag(AppLanguage.current)).format(Date(time))
    }
}

/** Firebase'e yalnızca topluluk ilk kullanıldığında dokunur (uygulama açılışını yavaşlatmaz). */
private object LazyFirestore : CommunityBackend by lazyBackend()

private fun lazyBackend(): CommunityBackend {
    val real by lazy { FirestoreBackend() }
    return object : CommunityBackend {
        override fun currentUid() = real.currentUid()
        override suspend fun signIn() = real.signIn()
        override fun posts(topic: String) = real.posts(topic)
        override fun post(topic: String, postId: String) = real.post(topic, postId)
        override fun comments(topic: String, postId: String) = real.comments(topic, postId)
        override suspend fun isModerator() = real.isModerator()
        override suspend fun createPost(topic: String, nick: String, text: String, lang: String) = real.createPost(topic, nick, text, lang)
        override suspend fun addComment(topic: String, postId: String, nick: String, text: String) = real.addComment(topic, postId, nick, text)
        override suspend fun setLiked(topic: String, postId: String, liked: Boolean) = real.setLiked(topic, postId, liked)
        override suspend fun report(topic: String, postId: String, commentId: String?, reason: String) = real.report(topic, postId, commentId, reason)
        override suspend fun deletePost(topic: String, postId: String) = real.deletePost(topic, postId)
        override suspend fun deleteComment(topic: String, postId: String, commentId: String) = real.deleteComment(topic, postId, commentId)
        override suspend fun approve(topic: String, postId: String, commentId: String?) = real.approve(topic, postId, commentId)
        override suspend fun deleteAccount(myCommentPaths: Collection<String>) = real.deleteAccount(myCommentPaths)
    }
}

private class FirestoreBackend : CommunityBackend {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private fun postsCol(topic: String) = db.collection("topics").document(topic).collection("posts")
    private fun commentsCol(topic: String, postId: String) = postsCol(topic).document(postId).collection("comments")
    private fun uid() = auth.currentUser?.uid ?: error("not signed in")

    /** Sunucu yanıt vermezse sonsuza kadar beklemesin. */
    private suspend fun <T> net(block: suspend () -> T): T = withTimeout(20_000) { block() }

    override fun currentUid() = auth.currentUser?.uid

    override suspend fun signIn(): String =
        auth.currentUser?.uid ?: net { auth.signInAnonymously().await().user!!.uid }

    override fun posts(topic: String): Flow<List<CommunityPost>> = callbackFlow {
        val reg = postsCol(topic).orderBy("createdAt", Query.Direction.DESCENDING).limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.map { it.toPost(topic) } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override fun post(topic: String, postId: String): Flow<CommunityPost?> = callbackFlow {
        val reg = postsCol(topic).document(postId).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(if (snap != null && snap.exists()) snap.toPost(topic) else null)
        }
        awaitClose { reg.remove() }
    }

    override fun comments(topic: String, postId: String): Flow<List<CommunityComment>> = callbackFlow {
        val reg = commentsCol(topic, postId).orderBy("createdAt", Query.Direction.ASCENDING).limit(300)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.map { it.toComment() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun isModerator(): Boolean = runCatching {
        net { db.collection("mods").document(uid()).get().await().exists() }
    }.getOrDefault(false)

    override suspend fun createPost(topic: String, nick: String, text: String, lang: String): String {
        val doc = postsCol(topic).document()
        net {
            doc.set(
                mapOf(
                    "uid" to uid(), "nick" to nick, "text" to text, "createdAt" to FieldValue.serverTimestamp(),
                    "likedBy" to emptyList<String>(), "reportedBy" to emptyList<String>(),
                    "commentCount" to 0, "modApproved" to false, "lang" to lang
                )
            ).await()
        }
        return doc.id
    }

    override suspend fun addComment(topic: String, postId: String, nick: String, text: String): String {
        val doc = commentsCol(topic, postId).document()
        net {
            db.batch()
                .set(doc, mapOf(
                    "uid" to uid(), "nick" to nick, "text" to text, "createdAt" to FieldValue.serverTimestamp(),
                    "reportedBy" to emptyList<String>(), "modApproved" to false
                ))
                .update(postsCol(topic).document(postId), "commentCount", FieldValue.increment(1))
                .commit().await()
        }
        return doc.id
    }

    override suspend fun setLiked(topic: String, postId: String, liked: Boolean) {
        val u = uid()
        net { postsCol(topic).document(postId).update("likedBy", if (liked) FieldValue.arrayUnion(u) else FieldValue.arrayRemove(u)).await() }
    }

    override suspend fun report(topic: String, postId: String, commentId: String?, reason: String) {
        val u = uid()
        val target = if (commentId == null) postsCol(topic).document(postId) else commentsCol(topic, postId).document(commentId)
        val path = if (commentId == null) "$topic/$postId" else "$topic/$postId/$commentId"
        net {
            db.batch()
                .update(target, "reportedBy", FieldValue.arrayUnion(u))
                .set(db.collection("reports").document("${u}_${path.replace('/', '_')}"), mapOf(
                    "reporter" to u, "path" to path, "reason" to reason, "createdAt" to FieldValue.serverTimestamp()
                ))
                .commit().await()
        }
    }

    override suspend fun deletePost(topic: String, postId: String) {
        net {
            val comments = commentsCol(topic, postId).get().await()
            comments.documents.chunked(400).forEach { chunk ->
                val b = db.batch(); chunk.forEach { b.delete(it.reference) }; b.commit().await()
            }
            postsCol(topic).document(postId).delete().await()
        }
    }

    override suspend fun deleteComment(topic: String, postId: String, commentId: String) {
        net { commentsCol(topic, postId).document(commentId).delete().await() }
        runCatching { net { postsCol(topic).document(postId).update("commentCount", FieldValue.increment(-1)).await() } }
    }

    override suspend fun approve(topic: String, postId: String, commentId: String?) {
        val target = if (commentId == null) postsCol(topic).document(postId) else commentsCol(topic, postId).document(commentId)
        net { target.update(mapOf("modApproved" to true, "reportedBy" to emptyList<String>())).await() }
    }

    override suspend fun deleteAccount(myCommentPaths: Collection<String>) {
        val u = uid()
        for ((topic, _) in COMMUNITY_TOPICS) {
            val mine = net { postsCol(topic).whereEqualTo("uid", u).get().await() }
            mine.documents.forEach { deletePost(topic, it.id) }
        }
        myCommentPaths.forEach { path ->
            val parts = path.split('/')
            if (parts.size == 3) runCatching { deleteComment(parts[0], parts[1], parts[2]) }
        }
        net { auth.currentUser?.delete()?.await() }
        auth.signOut()
    }

    private fun DocumentSnapshot.time(): Long =
        getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: System.currentTimeMillis()

    private fun DocumentSnapshot.strings(field: String): List<String> =
        (get(field) as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    private fun DocumentSnapshot.toPost(topic: String) = CommunityPost(
        id = id, topic = topic, uid = getString("uid") ?: "", nick = getString("nick") ?: "", text = getString("text") ?: "",
        createdAt = time(), likedBy = strings("likedBy"), reportedBy = strings("reportedBy"),
        commentCount = (getLong("commentCount") ?: 0L).toInt().coerceAtLeast(0),
        modApproved = getBoolean("modApproved") ?: false, lang = getString("lang") ?: "tr"
    )

    private fun DocumentSnapshot.toComment() = CommunityComment(
        id = id, uid = getString("uid") ?: "", nick = getString("nick") ?: "", text = getString("text") ?: "",
        createdAt = time(), reportedBy = strings("reportedBy"), modApproved = getBoolean("modApproved") ?: false
    )
}

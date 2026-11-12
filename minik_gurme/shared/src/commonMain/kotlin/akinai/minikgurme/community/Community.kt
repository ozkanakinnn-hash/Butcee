package akinai.minikgurme.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.shared.KeyValueStore
import akinai.minikgurme.shared.currentTimeMillis
import akinai.minikgurme.util.DateKit
import akinai.minikgurme.util.lowercaseTr
import kotlinx.coroutines.flow.Flow

/*
 * Ebeveyn Topluluğu (uygulama içi forum).
 *
 * Firebase anonim giriş + Firestore. Yapı:
 *   topics/{topic}/posts/{postId}                  gönderi
 *   topics/{topic}/posts/{postId}/comments/{id}    yorum
 *   reports/{id}                                   şikâyet kaydı (yalnızca moderatör okur)
 *   mods/{uid}                                     moderatör listesi (yalnızca konsoldan eklenir)
 * Güvenlik kuralları: minik_gurme/docs/firestore.rules
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
    var backend: CommunityBackend by mutableStateOf(UnconfiguredBackend)

    /** Platform arka ucu kurulmadan çağrılırsa hata verir (Android: Firebase, iOS: Firebase iOS). */
    val Unconfigured: CommunityBackend = UnconfiguredBackend

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

    fun init() {
        val p = prefs
        nick = p.getString("nick", "") ?: ""
        joined = p.getBoolean("joined", false) && nick.isNotBlank()
        blocked = p.getStringSet("blocked")
        reported = p.getStringSet("reported")
        myComments = p.getStringSet("my_comments")
        lastWrite = p.getLong("last_write", 0L)
    }

    private val prefs by lazy { KeyValueStore(PREFS) }

    fun join(newNick: String) {
        nick = newNick.trim(); joined = true
        prefs.putString("nick", nick); prefs.putBoolean("joined", true)
    }

    fun rename(newNick: String) {
        nick = newNick.trim()
        prefs.putString("nick", nick)
    }

    fun block(uid: String) {
        blocked = blocked + uid
        prefs.putStringSet("blocked", blocked)
    }

    fun clearBlocked() {
        blocked = emptySet()
        prefs.putStringSet("blocked", blocked)
    }

    fun markReported(key: String) {
        reported = reported + key
        prefs.putStringSet("reported", reported)
    }

    fun rememberComment(path: String) {
        myComments = myComments + path
        prefs.putStringSet("my_comments", myComments)
    }

    fun forgetComment(path: String) {
        myComments = myComments - path
        prefs.putStringSet("my_comments", myComments)
    }

    /** Spam önleme: son paylaşımdan bu yana kalan bekleme (ms), 0 = yazabilir. */
    fun cooldownLeft(now: Long = currentTimeMillis()): Long =
        (lastWrite + CommunityConfig.COOLDOWN_MS - now).coerceAtLeast(0)

    fun markWrote(now: Long = currentTimeMillis()) {
        lastWrite = now
        prefs.putLong("last_write", now)
    }

    /** Topluluktan ayrılınca cihazdaki tüm topluluk bilgisini siler. */
    fun leave() {
        prefs.clear()
        init()
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
        val words = text.lowercaseTr()
            .replace('0', 'o').replace('1', 'i').replace('3', 'e').replace('4', 'a').replace('@', 'a').replace('$', 's')
            .split(Regex("""[^\p{L}]+""")).filter { it.isNotEmpty() }
        return words.any { w -> w in exactWords || prefixes.any { w.startsWith(it) } }
    }
}

/** "şimdi", "5 dk", "3 sa", "2 g" ya da tarih. */
fun communityTimeAgo(time: Long, now: Long = currentTimeMillis()): String {
    val en = AppLanguage.current == "en"
    val min = ((now - time) / 60_000).coerceAtLeast(0)
    return when {
        min < 1 -> if (en) "now" else "şimdi"
        min < 60 -> if (en) "${min}m" else "$min dk"
        min < 60 * 24 -> if (en) "${min / 60}h" else "${min / 60} sa"
        min < 60 * 24 * 7 -> if (en) "${min / (60 * 24)}d" else "${min / (60 * 24)} g"
        else -> DateKit.format(DateKit.fromMillis(time), "d MMM")
    }
}


private object UnconfiguredBackend : CommunityBackend {
    private fun no(): Nothing = error("Topluluk arka ucu kurulmadı")
    override fun currentUid(): String? = null
    override suspend fun signIn(): String = no()
    override fun posts(topic: String) = kotlinx.coroutines.flow.flow<List<CommunityPost>> { no() }
    override fun post(topic: String, postId: String) = kotlinx.coroutines.flow.flow<CommunityPost?> { no() }
    override fun comments(topic: String, postId: String) = kotlinx.coroutines.flow.flow<List<CommunityComment>> { no() }
    override suspend fun isModerator() = false
    override suspend fun createPost(topic: String, nick: String, text: String, lang: String): String = no()
    override suspend fun addComment(topic: String, postId: String, nick: String, text: String): String = no()
    override suspend fun setLiked(topic: String, postId: String, liked: Boolean) = no()
    override suspend fun report(topic: String, postId: String, commentId: String?, reason: String) = no()
    override suspend fun deletePost(topic: String, postId: String) = no()
    override suspend fun deleteComment(topic: String, postId: String, commentId: String) = no()
    override suspend fun approve(topic: String, postId: String, commentId: String?) = no()
    override suspend fun deleteAccount(myCommentPaths: Collection<String>) = no()
}

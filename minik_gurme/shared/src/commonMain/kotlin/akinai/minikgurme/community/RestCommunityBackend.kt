package akinai.minikgurme.community

import akinai.minikgurme.shared.KeyValueStore
import akinai.minikgurme.shared.currentTimeMillis
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.random.Random

/**
 * Topluluk arka ucu: Firebase Auth (anonim) + Firestore, **REST API** üzerinden (iOS'ta Firebase SDK gerekmez).
 * Android'deki Firebase SDK ile aynı veri yapısını ve güvenlik kurallarını kullanır.
 * Canlı dinleme yerine kısa aralıklı yenileme + her yazmadan sonra anında yenileme.
 */
class RestCommunityBackend(
    private val apiKey: String,
    private val projectId: String,
    private val http: HttpClient,
    /** Test için emülatör adresleri verilebilir. */
    private val authBase: String = "https://identitytoolkit.googleapis.com/v1",
    private val tokenBase: String = "https://securetoken.googleapis.com/v1",
    firestoreHost: String = "https://firestore.googleapis.com",
    /** iOS'a kısıtlı API anahtarları için (X-Ios-Bundle-Identifier). */
    private val extraHeaders: Map<String, String> = emptyMap(),
    private val pollMillis: Long = 8_000,
    private val store: KeyValueStore = KeyValueStore("firebase_auth")
) : CommunityBackend {

    private val docsRoot = "projects/$projectId/databases/(default)/documents"
    private val fsBase = "$firestoreHost/v1"
    private val json = Json { ignoreUnknownKeys = true }
    private val authLock = Mutex()
    /** Her yazmadan sonra artar; açık listeler hemen yenilenir. */
    private val changes = MutableStateFlow(0)

    private var uid: String? = store.getString("uid")
    private var refreshToken: String? = store.getString("refresh")
    private var idToken: String? = null
    private var idTokenExpiry = 0L

    // ---------------- Kimlik ----------------

    override fun currentUid(): String? = uid

    override suspend fun signIn(): String = authLock.withLock {
        uid?.let { if (refreshToken != null) return it }
        val r = post("$authBase/accounts:signUp?key=$apiKey", buildJsonObject { put("returnSecureToken", true) })
        val o = r.jsonObject
        saveTokens(o.str("localId")!!, o.str("idToken")!!, o.str("refreshToken")!!, o.str("expiresIn")?.toLongOrNull() ?: 3600)
        uid!!
    }

    private fun saveTokens(newUid: String, id: String, refresh: String, expiresIn: Long) {
        uid = newUid; idToken = id; refreshToken = refresh
        idTokenExpiry = currentTimeMillis() + (expiresIn - 60) * 1000
        store.putString("uid", newUid); store.putString("refresh", refresh)
    }

    /** Geçerli kimlik jetonu (gerekirse yeniler); giriş yapılmadıysa null. */
    private suspend fun token(): String? = authLock.withLock {
        val refresh = refreshToken ?: return null
        if (idToken != null && currentTimeMillis() < idTokenExpiry) return idToken
        val resp = http.request("$tokenBase/token?key=$apiKey") {
            method = HttpMethod.Post
            extraHeaders.forEach { (k, v) -> header(k, v) }
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("grant_type=refresh_token&refresh_token=$refresh")
        }
        val o = parse(resp).jsonObject
        saveTokens(o.str("user_id")!!, o.str("id_token")!!, o.str("refresh_token")!!, o.str("expires_in")?.toLongOrNull() ?: 3600)
        idToken
    }

    private fun signOutLocal() {
        uid = null; idToken = null; refreshToken = null
        store.remove("uid"); store.remove("refresh")
    }

    // ---------------- HTTP ----------------

    private class HttpError(val status: Int, body: String) : Exception("HTTP $status: ${body.take(300)}")

    private suspend fun parse(resp: HttpResponse): JsonElement {
        val text = resp.bodyAsText()
        if (!resp.status.isSuccess()) throw HttpError(resp.status.value, text)
        return if (text.isBlank()) JsonObject(emptyMap()) else json.parseToJsonElement(text)
    }

    private suspend fun call(method: HttpMethod, url: String, body: JsonElement? = null, auth: Boolean = true): JsonElement {
        val tok = if (auth) token() else null
        val resp = http.request(url) {
            this.method = method
            extraHeaders.forEach { (k, v) -> header(k, v) }
            if (tok != null) header("Authorization", "Bearer $tok")
            if (body != null) { contentType(ContentType.Application.Json); setBody(body.toString()) }
        }
        return parse(resp)
    }

    private suspend fun post(url: String, body: JsonElement, auth: Boolean = false) = call(HttpMethod.Post, url, body, auth)

    private fun docName(path: String) = "$docsRoot/$path"
    private fun postPath(topic: String, id: String) = "topics/$topic/posts/$id"
    private fun commentPath(topic: String, postId: String, id: String) = "${postPath(topic, postId)}/comments/$id"

    private fun newId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    // ---------------- Firestore değer dönüşümleri ----------------

    private fun sv(s: String) = buildJsonObject { put("stringValue", s) }
    private fun bv(b: Boolean) = buildJsonObject { put("booleanValue", b) }
    private fun iv(i: Long) = buildJsonObject { put("integerValue", i.toString()) }
    private fun emptyArr() = buildJsonObject { putJsonObject("arrayValue") {} }
    private fun arr(vararg s: String) = buildJsonObject { putJsonObject("arrayValue") { putJsonArray("values") { s.forEach { add(sv(it)) } } } }

    private fun JsonObject.str(k: String) = this[k]?.jsonPrimitive?.contentOrNull
    private fun JsonObject.field(k: String): JsonObject? = (this["fields"] as? JsonObject)?.get(k) as? JsonObject
    private fun JsonObject.fStr(k: String) = field(k)?.str("stringValue")
    private fun JsonObject.fBool(k: String) = field(k)?.get("booleanValue")?.jsonPrimitive?.booleanOrNull
    private fun JsonObject.fInt(k: String) = field(k)?.let { it.str("integerValue")?.toLongOrNull() ?: it.str("doubleValue")?.toDoubleOrNull()?.toLong() }
    private fun JsonObject.fList(k: String): List<String> =
        (field(k)?.get("arrayValue") as? JsonObject)?.get("values")?.jsonArray?.mapNotNull { (it as? JsonObject)?.str("stringValue") } ?: emptyList()
    private fun JsonObject.fTime(k: String): Long = field(k)?.str("timestampValue")?.let { parseRfc3339(it) } ?: currentTimeMillis()
    private fun JsonObject.id() = str("name")!!.substringAfterLast('/')

    private fun toPost(topic: String, d: JsonObject) = CommunityPost(
        id = d.id(), topic = topic, uid = d.fStr("uid") ?: "", nick = d.fStr("nick") ?: "", text = d.fStr("text") ?: "",
        createdAt = d.fTime("createdAt"), likedBy = d.fList("likedBy"), reportedBy = d.fList("reportedBy"),
        commentCount = (d.fInt("commentCount") ?: 0L).toInt().coerceAtLeast(0),
        modApproved = d.fBool("modApproved") ?: false, lang = d.fStr("lang") ?: "tr"
    )

    private fun toComment(d: JsonObject) = CommunityComment(
        id = d.id(), uid = d.fStr("uid") ?: "", nick = d.fStr("nick") ?: "", text = d.fStr("text") ?: "",
        createdAt = d.fTime("createdAt"), reportedBy = d.fList("reportedBy"), modApproved = d.fBool("modApproved") ?: false
    )

    // ---------------- Sorgular ----------------

    private suspend fun runQuery(parentPath: String, collection: String, orderDesc: Boolean? = null, whereUid: String? = null, limit: Int = 100): List<JsonObject> {
        val q = buildJsonObject {
            putJsonObject("structuredQuery") {
                putJsonArray("from") { add(buildJsonObject { put("collectionId", collection) }) }
                if (whereUid != null) putJsonObject("where") {
                    putJsonObject("fieldFilter") {
                        putJsonObject("field") { put("fieldPath", "uid") }
                        put("op", "EQUAL"); put("value", sv(whereUid))
                    }
                }
                if (orderDesc != null) putJsonArray("orderBy") {
                    add(buildJsonObject {
                        putJsonObject("field") { put("fieldPath", "createdAt") }
                        put("direction", if (orderDesc) "DESCENDING" else "ASCENDING")
                    })
                }
                put("limit", limit)
            }
        }
        val parent = if (parentPath.isEmpty()) docsRoot else docName(parentPath)
        val res = call(HttpMethod.Post, "$fsBase/$parent:runQuery", q, auth = refreshToken != null)
        return res.jsonArray.mapNotNull { (it as? JsonObject)?.get("document") as? JsonObject }
    }

    private suspend fun getDoc(path: String): JsonObject? = try {
        call(HttpMethod.Get, "$fsBase/${docName(path)}", auth = refreshToken != null).jsonObject
    } catch (e: HttpError) { if (e.status == 404) null else throw e }

    /** Açık kaldığı sürece yenilenen akış; hata olursa akış hata ile biter (ekran "yüklenemedi" gösterir). */
    private fun <T> polling(fetch: suspend () -> T): Flow<T> = channelFlow {
        launch {
            changes.collectLatest {
                while (true) {
                    send(fetch())
                    delay(pollMillis)
                }
            }
        }
    }

    override fun posts(topic: String): Flow<List<CommunityPost>> =
        polling { runQuery("topics/$topic", "posts", orderDesc = true, limit = 100).map { toPost(topic, it) } }

    override fun post(topic: String, postId: String): Flow<CommunityPost?> =
        polling { getDoc(postPath(topic, postId))?.let { toPost(topic, it) } }

    override fun comments(topic: String, postId: String): Flow<List<CommunityComment>> =
        polling { runQuery(postPath(topic, postId), "comments", orderDesc = false, limit = 300).map { toComment(it) } }

    override suspend fun isModerator(): Boolean = runCatching { uid?.let { getDoc("mods/$it") } != null }.getOrDefault(false)

    // ---------------- Yazma (commit) ----------------

    private suspend fun commit(vararg writes: JsonObject) {
        call(HttpMethod.Post, "$fsBase/$docsRoot:commit", buildJsonObject { put("writes", JsonArray(writes.toList())) })
        changes.value++
    }

    private fun create(path: String, fields: Map<String, JsonObject>, serverTimeField: String? = null) = buildJsonObject {
        putJsonObject("update") {
            put("name", docName(path))
            put("fields", JsonObject(fields))
        }
        if (serverTimeField != null) putJsonArray("updateTransforms") {
            add(buildJsonObject { put("fieldPath", serverTimeField); put("setToServerValue", "REQUEST_TIME") })
        }
        putJsonObject("currentDocument") { put("exists", false) }
    }

    private fun transform(path: String, field: String, op: String, value: JsonElement) = buildJsonObject {
        putJsonObject("transform") {
            put("document", docName(path))
            putJsonArray("fieldTransforms") { add(buildJsonObject { put("fieldPath", field); put(op, value) }) }
        }
    }

    private fun delete(path: String) = buildJsonObject { put("delete", docName(path)) }

    override suspend fun createPost(topic: String, nick: String, text: String, lang: String): String {
        val me = signIn(); val id = newId()
        commit(create(postPath(topic, id), mapOf(
            "uid" to sv(me), "nick" to sv(nick), "text" to sv(text), "likedBy" to emptyArr(), "reportedBy" to emptyArr(),
            "commentCount" to iv(0), "modApproved" to bv(false), "lang" to sv(lang)
        ), serverTimeField = "createdAt"))
        return id
    }

    override suspend fun addComment(topic: String, postId: String, nick: String, text: String): String {
        val me = signIn(); val id = newId()
        commit(
            create(commentPath(topic, postId, id), mapOf(
                "uid" to sv(me), "nick" to sv(nick), "text" to sv(text), "reportedBy" to emptyArr(), "modApproved" to bv(false)
            ), serverTimeField = "createdAt"),
            transform(postPath(topic, postId), "commentCount", "increment", iv(1))
        )
        return id
    }

    override suspend fun setLiked(topic: String, postId: String, liked: Boolean) {
        val me = signIn()
        val values = arr(me)["arrayValue"]!!
        commit(transform(postPath(topic, postId), "likedBy", if (liked) "appendMissingElements" else "removeAllFromArray", values))
    }

    override suspend fun report(topic: String, postId: String, commentId: String?, reason: String) {
        val me = signIn()
        val target = if (commentId == null) postPath(topic, postId) else commentPath(topic, postId, commentId)
        val path = if (commentId == null) "$topic/$postId" else "$topic/$postId/$commentId"
        commit(
            transform(target, "reportedBy", "appendMissingElements", arr(me)["arrayValue"]!!),
            create("reports/${me}_${path.replace('/', '_')}", mapOf(
                "reporter" to sv(me), "path" to sv(path), "reason" to sv(reason)
            ), serverTimeField = "createdAt")
        )
    }

    override suspend fun deletePost(topic: String, postId: String) {
        signIn()
        // Tek atomik işlem: gönderi silinemiyorsa (yetki yoksa) yorumlara da dokunulmaz.
        val comments = runQuery(postPath(topic, postId), "comments", limit = 499)
        val writes = comments.map { buildJsonObject { put("delete", it.str("name")!!) } } + delete(postPath(topic, postId))
        commit(*writes.toTypedArray())
    }

    override suspend fun deleteComment(topic: String, postId: String, commentId: String) {
        signIn()
        commit(delete(commentPath(topic, postId, commentId)))
        runCatching { commit(transform(postPath(topic, postId), "commentCount", "increment", iv(-1))) }
    }

    override suspend fun approve(topic: String, postId: String, commentId: String?) {
        signIn()
        val target = if (commentId == null) postPath(topic, postId) else commentPath(topic, postId, commentId)
        commit(buildJsonObject {
            putJsonObject("update") {
                put("name", docName(target))
                put("fields", JsonObject(mapOf("modApproved" to bv(true), "reportedBy" to emptyArr())))
            }
            putJsonObject("updateMask") { putJsonArray("fieldPaths") { add(JsonPrimitive("modApproved")); add(JsonPrimitive("reportedBy")) } }
            putJsonObject("currentDocument") { put("exists", true) }
        })
    }

    override suspend fun deleteAccount(myCommentPaths: Collection<String>) {
        val me = signIn()
        for ((topic, _) in COMMUNITY_TOPICS) {
            runQuery("topics/$topic", "posts", whereUid = me, limit = 500).forEach { deletePost(topic, it.id()) }
        }
        myCommentPaths.forEach { p ->
            val parts = p.split('/')
            if (parts.size == 3) runCatching { deleteComment(parts[0], parts[1], parts[2]) }
        }
        token()?.let { post("$authBase/accounts:delete?key=$apiKey", buildJsonObject { put("idToken", it) }) }
        signOutLocal()
    }
}

/** "2026-11-12T13:05:00.123456Z" → epoch ms (Firestore zaman damgası, her zaman UTC). */
internal fun parseRfc3339(s: String): Long {
    val m = Regex("""(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d+))?Z""").matchEntire(s) ?: return 0L
    val (y, mo, d, h, mi, se) = m.destructured
    val frac = m.groupValues[7].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
    val days = kotlinx.datetime.LocalDate(y.toInt(), mo.toInt(), d.toInt()).toEpochDays().toLong()
    return ((days * 24 + h.toLong()) * 60 + mi.toLong()) * 60_000 + se.toLong() * 1000 + frac
}

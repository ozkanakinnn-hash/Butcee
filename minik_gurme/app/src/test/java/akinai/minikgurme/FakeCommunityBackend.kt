package akinai.minikgurme

import akinai.minikgurme.community.COMMUNITY_TOPICS
import akinai.minikgurme.community.CommunityBackend
import akinai.minikgurme.community.CommunityComment
import akinai.minikgurme.community.CommunityPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Bellek içi topluluk arka ucu (Firebase'e bağlanmadan testler için). */
class FakeCommunityBackend(var me: String = "me", var moderator: Boolean = false) : CommunityBackend {
    val posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val comments = MutableStateFlow<Map<String, List<CommunityComment>>>(emptyMap())
    val reports = mutableListOf<Triple<String, String?, String>>()
    var signedIn = false
    var accountDeleted = false
    private var seq = 0L
    private var clock = System.currentTimeMillis() - 3_600_000

    override fun currentUid() = if (signedIn) me else null
    override suspend fun signIn(): String { signedIn = true; return me }

    override fun posts(topic: String): Flow<List<CommunityPost>> =
        posts.map { all -> all.filter { it.topic == topic }.sortedByDescending { it.createdAt } }
    override fun post(topic: String, postId: String): Flow<CommunityPost?> =
        posts.map { all -> all.firstOrNull { it.topic == topic && it.id == postId } }
    override fun comments(topic: String, postId: String): Flow<List<CommunityComment>> =
        comments.map { it["$topic/$postId"].orEmpty().sortedBy { c -> c.createdAt } }

    override suspend fun isModerator() = moderator

    /** Test verisi: başka bir kullanıcının gönderisi. */
    fun seed(topic: String, uid: String, nick: String, text: String, minutesAgo: Long = 30, likes: List<String> = emptyList(), reports: List<String> = emptyList(), commentCount: Int = 0): String {
        val id = "p${++seq}"
        posts.value = posts.value + CommunityPost(id, topic, uid, nick, text, System.currentTimeMillis() - minutesAgo * 60_000, likes, reports, commentCount)
        return id
    }

    fun seedComment(topic: String, postId: String, uid: String, nick: String, text: String, minutesAgo: Long = 10): String {
        val id = "c${++seq}"
        val key = "$topic/$postId"
        comments.value = comments.value + (key to (comments.value[key].orEmpty() + CommunityComment(id, uid, nick, text, System.currentTimeMillis() - minutesAgo * 60_000)))
        return id
    }

    private fun updatePost(topic: String, id: String, f: (CommunityPost) -> CommunityPost) {
        posts.value = posts.value.map { if (it.topic == topic && it.id == id) f(it) else it }
    }

    override suspend fun createPost(topic: String, nick: String, text: String, lang: String): String {
        val id = "p${++seq}"
        posts.value = posts.value + CommunityPost(id, topic, me, nick, text, System.currentTimeMillis(), lang = lang)
        return id
    }

    override suspend fun addComment(topic: String, postId: String, nick: String, text: String): String {
        val id = "c${++seq}"
        val key = "$topic/$postId"
        comments.value = comments.value + (key to (comments.value[key].orEmpty() + CommunityComment(id, me, nick, text, System.currentTimeMillis())))
        updatePost(topic, postId) { it.copy(commentCount = it.commentCount + 1) }
        return id
    }

    override suspend fun setLiked(topic: String, postId: String, liked: Boolean) =
        updatePost(topic, postId) { it.copy(likedBy = if (liked) (it.likedBy + me).distinct() else it.likedBy - me) }

    override suspend fun report(topic: String, postId: String, commentId: String?, reason: String) {
        reports += Triple("$topic/$postId", commentId, reason)
        if (commentId == null) updatePost(topic, postId) { it.copy(reportedBy = (it.reportedBy + me).distinct()) }
    }

    override suspend fun deletePost(topic: String, postId: String) {
        posts.value = posts.value.filterNot { it.topic == topic && it.id == postId }
        comments.value = comments.value - "$topic/$postId"
    }

    override suspend fun deleteComment(topic: String, postId: String, commentId: String) {
        val key = "$topic/$postId"
        comments.value = comments.value + (key to comments.value[key].orEmpty().filterNot { it.id == commentId })
        updatePost(topic, postId) { it.copy(commentCount = (it.commentCount - 1).coerceAtLeast(0)) }
    }

    override suspend fun approve(topic: String, postId: String, commentId: String?) =
        updatePost(topic, postId) { it.copy(modApproved = true, reportedBy = emptyList()) }

    override suspend fun deleteAccount(myCommentPaths: Collection<String>) {
        for ((topic, _) in COMMUNITY_TOPICS) posts.value.filter { it.topic == topic && it.uid == me }.forEach { deletePost(topic, it.id) }
        myCommentPaths.forEach { p -> p.split('/').takeIf { it.size == 3 }?.let { deleteComment(it[0], it[1], it[2]) } }
        accountDeleted = true; signedIn = false
    }
}

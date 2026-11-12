package akinai.minikgurme.community

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

/** Android topluluk arka ucu: Firebase Auth (anonim) + Firestore. */
/** Firebase'e yalnızca topluluk ilk kullanıldığında dokunur (uygulama açılışını yavaşlatmaz). */
object AndroidCommunityBackend : CommunityBackend by lazyBackend()

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
            // Tek atomik işlem: gönderi silinemiyorsa (yetki yoksa) yorumlara da dokunulmaz.
            val comments = commentsCol(topic, postId).limit(499).get().await()
            val b = db.batch()
            comments.documents.forEach { b.delete(it.reference) }
            b.delete(postsCol(topic).document(postId))
            b.commit().await()
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

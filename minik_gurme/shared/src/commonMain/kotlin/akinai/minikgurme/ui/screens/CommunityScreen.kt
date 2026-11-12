package akinai.minikgurme.ui.screens

import akinai.minikgurme.ui.platform.PlatformBackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import akinai.minikgurme.community.COMMUNITY_TOPICS
import akinai.minikgurme.community.Community
import akinai.minikgurme.community.CommunityComment
import akinai.minikgurme.community.CommunityConfig
import akinai.minikgurme.community.CommunityPost
import akinai.minikgurme.community.ContentFilter
import akinai.minikgurme.community.communityTimeAgo
import akinai.minikgurme.community.defaultTopicFor
import akinai.minikgurme.community.topicLabel
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.i18n.t
import akinai.minikgurme.ui.theme.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

private sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data object Failed : Load<Nothing>
    data class Ready<T>(val value: T) : Load<T>
}

private val REPORT_REASONS = listOf(
    "Hakaret / uygunsuz dil",
    "Spam / reklam",
    "Yanlış veya tehlikeli sağlık bilgisi",
    "Kişisel bilgi paylaşımı",
    "Diğer"
)

/** Ebeveyn Topluluğu: katılım, konu başlıkları, gönderiler, yorumlar, şikâyet/engelleme ve moderasyon. */
@Composable
fun CommunityScreen(
    ageMonths: Int,
    isPlus: Boolean,
    onOpenPaywall: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit = {}
) {
    val backend = Community.backend
    // "feed" | "compose" | "join" | "post:<topic>/<id>"
    var view by rememberSaveable { mutableStateOf("feed") }
    // Katıldıktan sonra dönülecek görünüm
    var afterJoin by rememberSaveable { mutableStateOf("feed") }
    var topic by rememberSaveable { mutableStateOf(defaultTopicFor(ageMonths)) }
    var isMod by remember { mutableStateOf(false) }
    val joined = Community.joined
    LaunchedEffect(backend, joined) {
        isMod = joined && runCatching { backend.signIn(); backend.isModerator() }.getOrDefault(false)
    }
    PlatformBackHandler(enabled = view != "feed") { view = if (view == "join") afterJoin.takeIf { it.startsWith("post:") } ?: "feed" else "feed" }

    // Okumak herkese açık; yazmak (gönderi/yorum) Plus'a özel olabilir. Beğeni ve şikâyet için katılmak yeterli.
    val canWrite = !CommunityConfig.WRITE_PLUS_ONLY || isPlus
    val requireJoin: (String) -> Unit = { back -> afterJoin = back; view = "join" }
    val openCompose = {
        when {
            !canWrite -> onOpenPaywall()
            !joined -> requireJoin("compose")
            else -> view = "compose"
        }
    }

    when {
        view == "join" -> JoinCommunity(
            onOpenTerms, onOpenPrivacy,
            onBack = { view = afterJoin.takeIf { it.startsWith("post:") } ?: "feed" },
            onJoined = { view = afterJoin }
        )
        view == "compose" -> ComposePost(topic, onBack = { view = "feed" }, onPosted = { newTopic -> topic = newTopic; view = "feed" })
        view.startsWith("post:") -> {
            val (tp, id) = view.removePrefix("post:").split('/', limit = 2).let { it[0] to it[1] }
            PostDetail(tp, id, isMod, canWrite, onOpenPaywall, onBack = { view = "feed" }, onNeedJoin = { requireJoin(view) })
        }
        else -> Feed(
            topic, isMod, onTopic = { topic = it }, onOpenPost = { view = "post:${it.topic}/${it.id}" },
            onCompose = openCompose, canWrite = canWrite, onOpenPaywall = onOpenPaywall, onNeedJoin = { requireJoin("feed") }
        )
    }
}

@Composable
private fun JoinCommunity(onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit, onBack: () -> Unit, onJoined: () -> Unit) {
    val scope = rememberCoroutineScope()
    var nick by rememberSaveable { mutableStateOf("") }
    var accepted by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val nickError = if (nick.isBlank()) null else ContentFilter.checkNick(nick)

    Column(Modifier.fillMaxSize()) {
    BackBar("Topluluğa katıl", onBack)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 16.dp).imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.background(androidx.compose.ui.graphics.Brush.linearGradient(HeroGradient)).padding(20.dp)) {
                Icon(Icons.Filled.Forum, contentDescription = null, tint = OnHero, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(10.dp))
                Text("Ebeveyn Topluluğu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = OnHero)
                Spacer(Modifier.height(4.dp))
                Text("Ek gıda yolculuğundaki diğer ebeveynlerle deneyim paylaşın, soru sorun.", style = MaterialTheme.typography.bodyMedium, color = OnHero)
            }
        }

        OutlinedTextField(
            value = nick, onValueChange = { if (it.length <= CommunityConfig.NICK_MAX) nick = it },
            label = { Text("Takma ad") },
            placeholder = { Text("ör. Defne'nin annesi") },
            singleLine = true, isError = nickError != null,
            supportingText = { Text(nickError ?: "Gönderilerinizde bu ad görünür. Tam adınızı yazmanız gerekmez.") },
            modifier = Modifier.fillMaxWidth().testTag("community_nick"),
            colors = fieldColors()
        )

        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, CardBorder)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Topluluk kuralları", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                listOf(
                    "Saygılı olun; hakaret, küfür ve ayrımcılık yasaktır.",
                    "Reklam, bağlantı ve satış paylaşımı yapılmaz.",
                    "Paylaşımlar ebeveyn deneyimidir, tıbbi tavsiye değildir. Sağlık sorunlarında doktorunuza danışın.",
                    "Kendinizin ve bebeğinizin kişisel bilgilerini (tam ad, telefon, adres) paylaşmayın.",
                    "Kurallara uymayan içerikler şikâyet üzerine gizlenir ve silinir."
                ).forEachIndexed { i, rule ->
                    Row {
                        Text("${i + 1}.", fontWeight = FontWeight.Bold, color = CoralPrimary, modifier = Modifier.width(22.dp))
                        Text(rule, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { accepted = !accepted }.testTag("community_accept")) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it }, colors = CheckboxDefaults.colors(checkedColor = CoralPrimary))
            Column {
                Text("Topluluk kurallarını ve Kullanım Koşulları'nı kabul ediyor, Gizlilik Politikası'nda açıklandığı şekilde takma adımın ve paylaşımlarımın işlenmesine açık rıza veriyorum.", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Kullanım Koşulları", style = MaterialTheme.typography.labelMedium, color = CoralPrimary, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onOpenTerms).padding(vertical = 4.dp))
                    Text("Gizlilik Politikası", style = MaterialTheme.typography.labelMedium, color = CoralPrimary, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onOpenPrivacy).padding(vertical = 4.dp))
                }
            }
        }

        error?.let { Text(it, color = ReactionAllergy, style = MaterialTheme.typography.bodySmall) }

        Button(
            onClick = {
                busy = true; error = null
                scope.launch {
                    val r = runCatching { Community.backend.signIn() }
                    busy = false
                    if (r.isSuccess) { Community.join(nick); onJoined() } else error = t("Bağlantı kurulamadı. İnternet bağlantınızı kontrol edip tekrar deneyin.")
                }
            },
            enabled = !busy && accepted && nick.isNotBlank() && nickError == null,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("community_join"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = OnAccent)
        ) {
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = OnAccent, strokeWidth = 2.dp)
            else Text("Topluluğa katıl", fontWeight = FontWeight.Bold)
        }
        Text(
            "Anonim bir hesapla katılırsınız; e-posta veya telefon istenmez. Bebeğinizin profil ve sağlık kayıtları telefonunuzda kalır, toplulukla paylaşılmaz.",
            style = MaterialTheme.typography.bodySmall, color = TextTertiary
        )
        Spacer(Modifier.height(12.dp))
    }
    }
}

/** Katılmamış veya Plus olmayan kullanıcıya ne yapabileceğini anlatan şerit. */
@Composable
private fun AccessBanner(canWrite: Boolean, onOpenPaywall: () -> Unit, onNeedJoin: () -> Unit) {
    if (canWrite && Community.joined) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp).fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)).background(CoralSoft).padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            .testTag("community_access_banner")
    ) {
        Icon(if (canWrite) Icons.Filled.Forum else Icons.Filled.Lock, contentDescription = null, tint = CoralDark, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (canWrite) "Soru sormak ve yorum yazmak için takma adınızla katılın." else "Okumak herkese açık. Gönderi ve yorum yazmak Plus üyelerine özel.",
            style = MaterialTheme.typography.labelMedium, color = CoralDark, modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = if (canWrite) onNeedJoin else onOpenPaywall,
            modifier = Modifier.testTag(if (canWrite) "community_open_join" else "community_go_plus")
        ) { Text(if (canWrite) "Katıl" else "Plus'a geç", fontWeight = FontWeight.Bold, color = CoralPrimary) }
    }
}

@Composable
private fun Feed(
    topic: String,
    isMod: Boolean,
    onTopic: (String) -> Unit,
    onOpenPost: (CommunityPost) -> Unit,
    onCompose: () -> Unit,
    canWrite: Boolean,
    onOpenPaywall: () -> Unit,
    onNeedJoin: () -> Unit
) {
    val backend = Community.backend
    val state by produceState<Load<List<CommunityPost>>>(Load.Loading, topic, backend) {
        value = Load.Loading
        backend.posts(topic).catch { value = Load.Failed }.collect { value = Load.Ready(it) }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(COMMUNITY_TOPICS) { (key, label) ->
                    TopicChip(label, key == topic, Modifier.testTag("community_topic_$key")) { onTopic(key) }
                }
            }
            AccessBanner(canWrite, onOpenPaywall, onNeedJoin)
            Disclaimer()
            when (val s = state) {
                Load.Loading -> CenterBox { CircularProgressIndicator(color = CoralPrimary) }
                Load.Failed -> CenterBox { Text("Gönderiler yüklenemedi. İnternet bağlantınızı kontrol edin.", color = TextSecondary) }
                is Load.Ready -> {
                    val visible = s.value.filter { Community.isVisible(it.uid, it.reportedBy, it.modApproved, Community.postKey(it.topic, it.id), isMod) }
                    if (visible.isEmpty()) CenterBox {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Bu başlıkta henüz gönderi yok.", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("İlk soruyu siz sorun!", color = TextSecondary)
                        }
                    } else LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("community_feed")
                    ) {
                        items(visible, key = { it.id }) { post ->
                            PostCard(post, isMod, full = false, onOpen = { onOpenPost(post) }, onDeleted = {}, onNeedJoin = onNeedJoin)
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCompose,
            icon = { Icon(if (canWrite) Icons.Filled.Edit else Icons.Filled.Lock, contentDescription = null) },
            text = { Text("Gönderi yaz", fontWeight = FontWeight.Bold) },
            containerColor = CoralPrimary, contentColor = OnAccent,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).testTag("community_new_post")
        )
    }
}

@Composable
private fun PostCard(post: CommunityPost, isMod: Boolean, full: Boolean, onOpen: () -> Unit, onDeleted: () -> Unit, onNeedJoin: () -> Unit) {
    val scope = rememberCoroutineScope()
    val backend = Community.backend
    val me = backend.currentUid()
    val liked = me != null && me in post.likedBy
    val hidden = Community.isAutoHidden(post.reportedBy, post.modApproved)
    var dialog by remember { mutableStateOf<String?>(null) } // report | block | delete

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, if (hidden) AllergyBorder else CardBorder),
        modifier = Modifier.fillMaxWidth().then(if (full) Modifier else Modifier.clickable(onClick = onOpen)).testTag("community_post_${post.id}")
    ) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 4.dp)) {
            AuthorRow(post.uid, post.nick, post.createdAt, extra = if (full) null else topicLabel(post.topic)) {
                ItemMenu(
                    own = post.uid == me, isMod = isMod, tagPrefix = "post_${post.id}",
                    onReport = { if (Community.joined) dialog = "report" else onNeedJoin() }, onBlock = { dialog = "block" }, onDelete = { dialog = "delete" }
                )
            }
            if (hidden) ModBanner(post.reportedBy.size) {
                scope.launch { runCatching { backend.approve(post.topic, post.id, null) } }
            }
            Text(
                post.text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary,
                maxLines = if (full) Int.MAX_VALUE else 6, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 12.dp, top = 6.dp), translate = false
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (!Community.joined) onNeedJoin()
                        else scope.launch { runCatching { backend.setLiked(post.topic, post.id, !liked) } }
                    },
                    modifier = Modifier.testTag("like_${post.id}")
                ) {
                    Icon(if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, contentDescription = t("Beğen"), tint = if (liked) ReactionAllergy else TextSecondary)
                }
                Text("${post.likedBy.size}", color = TextSecondary, style = MaterialTheme.typography.labelLarge, translate = false)
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = t("Yorumlar"), tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("${post.commentCount}", color = TextSecondary, style = MaterialTheme.typography.labelLarge, translate = false)
            }
        }
    }

    ItemDialogs(
        dialog = dialog, onDismiss = { dialog = null }, what = "gönderi",
        onReport = { reason ->
            Community.markReported(Community.postKey(post.topic, post.id))
            scope.launch { runCatching { backend.report(post.topic, post.id, null, reason) } }
        },
        onBlock = { Community.block(post.uid) },
        onDelete = {
            scope.launch { if (runCatching { backend.deletePost(post.topic, post.id) }.isSuccess) onDeleted() }
        }
    )
}

@Composable
private fun PostDetail(topic: String, postId: String, isMod: Boolean, canWrite: Boolean, onOpenPaywall: () -> Unit, onBack: () -> Unit, onNeedJoin: () -> Unit) {
    val scope = rememberCoroutineScope()
    val backend = Community.backend
    val post by produceState<Load<CommunityPost?>>(Load.Loading, topic, postId, backend) {
        backend.post(topic, postId).catch { value = Load.Failed }.collect { value = Load.Ready(it) }
    }
    val comments by produceState<List<CommunityComment>>(emptyList(), topic, postId, backend) {
        backend.comments(topic, postId).catch { }.collect { value = it }
    }
    var input by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        BackBar(topicLabel(topic), onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).testTag("community_detail"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val p = post) {
                Load.Loading -> item { CenterBox(Modifier.height(200.dp)) { CircularProgressIndicator(color = CoralPrimary) } }
                Load.Failed -> item { Text("Gönderi yüklenemedi.", color = TextSecondary) }
                is Load.Ready -> {
                    val value = p.value
                    if (value == null) item { Text("Bu gönderi silinmiş.", color = TextSecondary, modifier = Modifier.padding(vertical = 24.dp)) }
                    else {
                        item { PostCard(value, isMod, full = true, onOpen = {}, onDeleted = onBack, onNeedJoin = onNeedJoin) }
                        item {
                            Text("Yorumlar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(top = 6.dp))
                        }
                        val visible = comments.filter { Community.isVisible(it.uid, it.reportedBy, it.modApproved, Community.commentKey(topic, postId, it.id), isMod) }
                        if (visible.isEmpty()) item { Text("Henüz yorum yok. İlk yorumu siz yazın.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
                        items(visible, key = { it.id }) { c -> CommentCard(topic, postId, c, isMod, onNeedJoin) }
                    }
                }
            }
        }
        if ((post as? Load.Ready)?.value != null && (!canWrite || !Community.joined)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 16.dp, vertical = 6.dp).testTag("community_comment_locked")
            ) {
                Icon(if (canWrite) Icons.Filled.Forum else Icons.Filled.Lock, contentDescription = null, tint = if (canWrite) CoralPrimary else OchreWarm, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (canWrite) "Yorum yazmak için topluluğa katılın." else "Yorum yazmak Plus üyelerine özel.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                TextButton(onClick = if (canWrite) onNeedJoin else onOpenPaywall) { Text(if (canWrite) "Katıl" else "Plus'a geç", fontWeight = FontWeight.Bold, color = CoralPrimary) }
            }
        } else if ((post as? Load.Ready)?.value != null) {
            error?.let { Text(it, color = ReactionAllergy, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp)) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().background(CardSurface).padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = input, onValueChange = { if (it.length <= CommunityConfig.COMMENT_MAX) input = it; error = null },
                    placeholder = { Text("Yorum yaz…") },
                    maxLines = 4, shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("community_comment_input"),
                    colors = fieldColors(), enabled = canWrite
                )
                IconButton(
                    onClick = {
                        if (!canWrite) { onOpenPaywall(); return@IconButton }
                        val problem = ContentFilter.check(input, CommunityConfig.COMMENT_MAX)
                        val wait = Community.cooldownLeft()
                        when {
                            problem != null -> error = t(problem)
                            wait > 0 -> error = t("Lütfen ${(wait + 999) / 1000} saniye sonra tekrar deneyin.")
                            else -> {
                                sending = true
                                val text = input.trim()
                                scope.launch {
                                    val r = runCatching { backend.addComment(topic, postId, Community.nick, text) }
                                    sending = false
                                    r.onSuccess { id ->
                                        Community.markWrote(); Community.rememberComment("$topic/$postId/$id"); input = ""
                                    }.onFailure { error = t("Gönderilemedi. İnternet bağlantınızı kontrol edin.") }
                                }
                            }
                        }
                    },
                    enabled = !sending && input.isNotBlank(),
                    modifier = Modifier.testTag("community_comment_send")
                ) {
                    Icon(if (canWrite) Icons.AutoMirrored.Filled.Send else Icons.Filled.Lock, contentDescription = t("Gönder"), tint = CoralPrimary)
                }
            }
        }
    }
}

@Composable
private fun CommentCard(topic: String, postId: String, c: CommunityComment, isMod: Boolean, onNeedJoin: () -> Unit) {
    val scope = rememberCoroutineScope()
    val backend = Community.backend
    val hidden = Community.isAutoHidden(c.reportedBy, c.modApproved)
    var dialog by remember { mutableStateOf<String?>(null) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CanvasBackground),
        border = BorderStroke(1.dp, if (hidden) AllergyBorder else CardBorder),
        modifier = Modifier.fillMaxWidth().testTag("community_comment_${c.id}")
    ) {
        Column(Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 10.dp)) {
            AuthorRow(c.uid, c.nick, c.createdAt, extra = null, small = true) {
                ItemMenu(
                    own = c.uid == backend.currentUid(), isMod = isMod, tagPrefix = "comment_${c.id}",
                    onReport = { if (Community.joined) dialog = "report" else onNeedJoin() }, onBlock = { dialog = "block" }, onDelete = { dialog = "delete" }
                )
            }
            if (hidden) ModBanner(c.reportedBy.size) { scope.launch { runCatching { backend.approve(topic, postId, c.id) } } }
            Text(c.text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.padding(end = 12.dp), translate = false)
        }
    }
    ItemDialogs(
        dialog = dialog, onDismiss = { dialog = null }, what = "yorum",
        onReport = { reason ->
            Community.markReported(Community.commentKey(topic, postId, c.id))
            scope.launch { runCatching { backend.report(topic, postId, c.id, reason) } }
        },
        onBlock = { Community.block(c.uid) },
        onDelete = {
            scope.launch {
                if (runCatching { backend.deleteComment(topic, postId, c.id) }.isSuccess) Community.forgetComment("$topic/$postId/${c.id}")
            }
        }
    )
}

@Composable
private fun ComposePost(initialTopic: String, onBack: () -> Unit, onPosted: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var topic by rememberSaveable { mutableStateOf(initialTopic) }
    var text by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        BackBar("Yeni gönderi", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Başlık", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = TextSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(COMMUNITY_TOPICS) { (key, label) ->
                    TopicChip(label, key == topic, Modifier.testTag("compose_topic_$key")) { topic = key }
                }
            }
            OutlinedTextField(
                value = text, onValueChange = { if (it.length <= CommunityConfig.POST_MAX) text = it; error = null },
                placeholder = { Text("Sorunuzu veya deneyiminizi yazın…") },
                minLines = 6, shape = RoundedCornerShape(16.dp),
                supportingText = { Text("${text.length}/${CommunityConfig.POST_MAX}", translate = false) },
                modifier = Modifier.fillMaxWidth().testTag("community_post_input"),
                colors = fieldColors()
            )
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = OchreSoft)) {
                Row(Modifier.padding(12.dp)) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = OchreDark, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "İpucu: Soru sorarken bebeğinizin ayını ve beslenme yönteminizi (püre/BLW) belirtin. Acil sağlık sorunlarında topluluğu değil, doktorunuzu veya 112'yi arayın.",
                        style = MaterialTheme.typography.bodySmall, color = OchreText
                    )
                }
            }
            error?.let { Text(it, color = ReactionAllergy, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("community_error")) }
        }
        Button(
            onClick = {
                val problem = ContentFilter.check(text, CommunityConfig.POST_MAX)
                val wait = Community.cooldownLeft()
                when {
                    problem != null -> error = t(problem)
                    wait > 0 -> error = t("Lütfen ${(wait + 999) / 1000} saniye sonra tekrar deneyin.")
                    else -> {
                        sending = true
                        val body = text.trim()
                        scope.launch {
                            val r = runCatching { Community.backend.createPost(topic, Community.nick, body, AppLanguage.current) }
                            sending = false
                            r.onSuccess { Community.markWrote(); onPosted(topic) }
                                .onFailure { error = t("Gönderilemedi. İnternet bağlantınızı kontrol edin.") }
                        }
                    }
                }
            },
            enabled = !sending && text.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp).testTag("community_post_send"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = OnAccent)
        ) {
            if (sending) CircularProgressIndicator(Modifier.size(20.dp), color = OnAccent, strokeWidth = 2.dp)
            else Text("Paylaş", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Ortak parçalar ----------

@Composable
private fun AuthorRow(uid: String, nick: String, time: Long, extra: String?, small: Boolean = false, menu: @Composable () -> Unit) {
    val palette = listOf(CoralPrimary, SageGreen, OchreWarm, SlateBlue, SoftPurple)
    val color = palette[(uid.hashCode() and 0x7fffffff) % palette.size]
    val size = if (small) 28.dp else 36.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(size).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            androidx.compose.material3.Text(
                nick.trim().take(1).uppercase().ifEmpty { "?" }, color = OnAccent, fontWeight = FontWeight.Bold,
                style = if (small) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(nick, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = if (small) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall, translate = false)
            val meta = listOfNotNull(communityTimeAgo(time), extra?.let { t(it) }).joinToString(" · ")
            Text(meta, color = TextTertiary, style = MaterialTheme.typography.labelSmall, translate = false)
        }
        menu()
    }
}

@Composable
private fun ItemMenu(own: Boolean, isMod: Boolean, tagPrefix: String, onReport: () -> Unit, onBlock: () -> Unit, onDelete: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }, modifier = Modifier.testTag("menu_$tagPrefix")) {
            Icon(Icons.Filled.MoreVert, contentDescription = t("Seçenekler"), tint = TextSecondary)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (!own) {
                DropdownMenuItem(text = { Text("Şikâyet et") }, onClick = { open = false; onReport() }, modifier = Modifier.testTag("menu_report"))
                DropdownMenuItem(text = { Text("Kullanıcıyı engelle") }, onClick = { open = false; onBlock() }, modifier = Modifier.testTag("menu_block"))
            }
            if (own || isMod) {
                DropdownMenuItem(text = { Text(if (own) "Sil" else "Sil (moderatör)", color = ReactionAllergy) }, onClick = { open = false; onDelete() }, modifier = Modifier.testTag("menu_delete"))
            }
        }
    }
}

@Composable
private fun ItemDialogs(dialog: String?, onDismiss: () -> Unit, what: String, onReport: (String) -> Unit, onBlock: () -> Unit, onDelete: () -> Unit) {
    when (dialog) {
        "report" -> {
            var reason by remember { mutableStateOf(REPORT_REASONS.first()) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Neden şikâyet ediyorsunuz?") },
                text = {
                    Column {
                        REPORT_REASONS.forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().selectable(selected = r == reason, onClick = { reason = r }).padding(vertical = 2.dp)
                            ) {
                                RadioButton(selected = r == reason, onClick = { reason = r }, colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary))
                                Text(r, color = TextPrimary)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("İçerik sizin için hemen gizlenir; birden fazla şikâyet alırsa herkes için gizlenir ve incelenir.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                confirmButton = { TextButton(onClick = { onReport(reason); onDismiss() }, modifier = Modifier.testTag("report_confirm")) { Text("Şikâyet et", color = ReactionAllergy, fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
            )
        }
        "block" -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Kullanıcı engellensin mi?") },
            text = { Text("Bu kullanıcının gönderilerini ve yorumlarını artık görmeyeceksiniz. Ayarlar'dan engeli kaldırabilirsiniz.") },
            confirmButton = { TextButton(onClick = { onBlock(); onDismiss() }, modifier = Modifier.testTag("block_confirm")) { Text("Engelle", color = ReactionAllergy, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
        )
        "delete" -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(if (what == "yorum") "Yorum silinsin mi?" else "Gönderi silinsin mi?") },
            text = { Text("Bu işlem geri alınamaz.") },
            confirmButton = { TextButton(onClick = { onDelete(); onDismiss() }, modifier = Modifier.testTag("delete_confirm")) { Text("Sil", color = ReactionAllergy, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun ModBanner(reports: Int, onApprove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 12.dp, top = 6.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ReactionAllergySoft).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(t("Herkesten gizlendi · $reports şikâyet"), color = AllergyText, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), translate = false)
        TextButton(onClick = onApprove, modifier = Modifier.testTag("mod_approve")) { Text("Onayla", fontWeight = FontWeight.Bold, color = SageDark) }
    }
}

@Composable
private fun TopicChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) CoralPrimary else CardSurface)
            .then(if (selected) Modifier else Modifier.background(Color.Transparent))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, color = if (selected) OnAccent else TextSecondary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Disclaimer() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(OchreSoft).padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = OchreDark, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text("Paylaşımlar ebeveyn deneyimidir, tıbbi tavsiye değildir.", style = MaterialTheme.typography.labelMedium, color = OchreText)
    }
}

@Composable
private fun BackBar(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("community_back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun CenterBox(modifier: Modifier = Modifier.fillMaxSize(), content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CoralPrimary, unfocusedBorderColor = CardBorder, cursorColor = CoralPrimary,
    focusedLabelColor = CoralPrimary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
)

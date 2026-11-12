package akinai.minikgurme.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import akinai.minikgurme.billing.PlusManager
import akinai.minikgurme.community.Community
import akinai.minikgurme.community.ContentFilter
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.i18n.t
import akinai.minikgurme.reminders.Reminders
import akinai.minikgurme.platform.Platform
import akinai.minikgurme.ui.theme.*
import akinai.minikgurme.util.Babies
import kotlinx.coroutines.launch

/** Ayarlar: Plus, bebekler, görünüm, dil, hatırlatıcılar, veriler ve yasal metinler. */
@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onResetStatistics: () -> Unit,
    onClearShopping: () -> Unit,
    onOpenPaywall: () -> Unit = {},
    onOpenPlanner: () -> Unit = {},
    onOpenLegal: (String) -> Unit = {},
    onSwitchBaby: (String) -> Unit = {},
    onAddBaby: () -> Unit = {},
    onDeleteBaby: (String) -> Unit = {},
    onDataImported: () -> Unit = {}
) {
    val platform = Platform.services
    val scope = rememberCoroutineScope()
    var confirm by remember { mutableStateOf<String?>(null) } // "stats" | "shopping" | "import" | "baby:<id>"
    var pendingImport by remember { mutableStateOf<Any?>(null) }
    var allergenOn by remember { mutableStateOf(Reminders.allergenOn()) }
    var mealsOn by remember { mutableStateOf(Reminders.mealsOn()) }
    var busy by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    val babies = remember { Babies.all() }
    val activeId = remember { Babies.activeId() }
    val isPlus = PlusManager.isPlus

    fun toast(msg: String) = platform.toast(msg)

    val ensureNotifPermission = platform.rememberNotificationPermission { granted ->
        if (!granted) toast("Bildirim izni verilmedi; hatırlatıcılar görünmeyecek.")
    }
    val exportBackup = platform.rememberBackupExport { ok -> toast(if (ok) "Yedek kaydedildi." else "Yedek kaydedilemedi.") }
    val pickBackup = platform.rememberBackupPicker { file -> pendingImport = file; confirm = "import" }

    confirm?.let { what ->
        val (title, body, action) = when {
            what == "stats" -> Triple("İstatistikler sıfırlansın mı?", "Tüm öğün kayıtları ve alerjen testleri kalıcı olarak silinecek. Bebek profili ve boy/kilo ölçümleri korunur. Bu işlem geri alınamaz.", "Evet, sıfırla")
            what == "shopping" -> Triple("Alışveriş listesi temizlensin mi?", "Listedeki tüm ürünler silinecek. Bu işlem geri alınamaz.", "Evet, temizle")
            what == "unblock" -> Triple("Engeller kaldırılsın mı?", "Engellediğiniz kullanıcıların gönderilerini ve yorumlarını yeniden göreceksiniz.", "Engelleri kaldır")
            what == "community" -> Triple("Topluluk hesabınız silinsin mi?", "Toplulukta paylaştığınız tüm gönderiler ve yorumlar kalıcı olarak silinecek. Bebek kayıtlarınız etkilenmez. Bu işlem geri alınamaz.", "Evet, sil")
            what == "import" -> Triple("Yedek geri yüklensin mi?", "Bu bebeğin mevcut tüm kayıtları yedekteki verilerle değiştirilecek.", "Geri yükle")
            else -> Triple("Bebek profili silinsin mi?", "Bu bebeğe ait tüm kayıtlar kalıcı olarak silinecek. Bu işlem geri alınamaz.", "Evet, sil")
        }
        AlertDialog(
            onDismissRequest = { confirm = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = ReactionAllergy) },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            what == "stats" -> onResetStatistics()
                            what == "shopping" -> onClearShopping()
                            what == "import" -> pendingImport?.let { uri ->
                                scope.launch {
                                    val r = platform.importBackup(uri)
                                    if (r.isSuccess) { toast("Yedek geri yüklendi."); onDataImported() }
                                    else toast(r.exceptionOrNull()?.message ?: "Yedek okunamadı.")
                                }
                            }
                            what.startsWith("baby:") -> onDeleteBaby(what.removePrefix("baby:"))
                            what == "unblock" -> Community.clearBlocked()
                            what == "community" -> if (!busy) {
                                busy = true
                                scope.launch {
                                    val r = runCatching {
                                        Community.backend.signIn()
                                        Community.backend.deleteAccount(Community.myComments)
                                    }
                                    busy = false
                                    if (r.isSuccess) { Community.leave(); toast("Topluluk hesabınız ve paylaşımlarınız silindi.") }
                                    else toast("Silinemedi. İnternet bağlantınızı kontrol edip tekrar deneyin.")
                                }
                            }
                        }
                        confirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReactionAllergy, contentColor = OnAccent),
                    modifier = Modifier.testTag("settings_confirm")
                ) { Text(action) }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Vazgeç") } }
        )
    }

    if (renaming) {
        var nick by remember { mutableStateOf(Community.nick) }
        val err = ContentFilter.checkNick(nick)
        AlertDialog(
            onDismissRequest = { renaming = false },
            title = { Text("Takma ad") },
            text = {
                Column {
                    androidx.compose.material3.OutlinedTextField(
                        value = nick, onValueChange = { if (it.length <= 30) nick = it }, singleLine = true, isError = err != null,
                        modifier = Modifier.fillMaxWidth().testTag("settings_nick_input")
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(err ?: "Yeni ad, bundan sonraki paylaşımlarınızda görünür.", style = MaterialTheme.typography.bodySmall, color = if (err != null) ReactionAllergy else TextSecondary)
                }
            },
            confirmButton = { TextButton(onClick = { Community.rename(nick); renaming = false }, enabled = err == null) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text("Vazgeç") } }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
            Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }

        // --- Plus ---
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(HeroGradient))
                .clickable { onOpenPaywall() }.padding(16.dp).testTag("settings_plus")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = OnHero, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Minik Gurme Plus", fontWeight = FontWeight.ExtraBold, color = OnHero, style = MaterialTheme.typography.titleMedium)
                    Text(if (isPlus) "Plus etkin" else "Menü planlayıcı, doktor raporu, çoklu bebek ve daha fazlası",
                        style = MaterialTheme.typography.bodySmall, color = OnHero.copy(alpha = 0.9f))
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = OnHero)
            }
        }
        Section("Plus özellikleri") {
            SettingRow(Icons.Filled.CalendarMonth, "Haftalık menü planlayıcı", "7 günlük plan ve alışveriş listesi", tag = "settings_planner", locked = !isPlus) { if (isPlus) onOpenPlanner() else onOpenPaywall() }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.PictureAsPdf, "Doktor raporu (PDF)", "Büyüme, alerjen ve tepkiler; paylaşın", tag = "settings_report", locked = !isPlus) {
                if (!isPlus) onOpenPaywall() else if (!busy) {
                    busy = true
                    scope.launch {
                        val r = platform.shareDoctorReport()
                        busy = false
                        r.onFailure { toast("Rapor oluşturulamadı.") }
                    }
                }
            }
        }

        // --- Bebekler ---
        Section("Bebek") {
            SettingRow(Icons.Filled.Edit, "Bebek profilini düzenle", "Ad, yaş, cinsiyet ve başlangıç tarihi", tag = "settings_edit_profile", onClick = onEditProfile)
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            babies.forEach { b ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(enabled = b.id != activeId) { onSwitchBaby(b.id) }.padding(vertical = 6.dp).testTag("baby_${b.id}")) {
                    Box(Modifier.size(30.dp).clip(RoundedCornerShape(15.dp)).background(if (b.id == activeId) CoralPrimary else CoralSoft), contentAlignment = Alignment.Center) {
                        if (b.id == activeId) Icon(Icons.Filled.Check, contentDescription = null, tint = OnAccent, modifier = Modifier.size(18.dp))
                        else androidx.compose.material3.Text(b.name.take(1).ifEmpty { "?" }, color = CoralDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    androidx.compose.material3.Text(b.name.ifBlank { t("Bebek") }, color = TextPrimary, fontWeight = if (b.id == activeId) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    if (b.id != Babies.MAIN) IconButton(onClick = { confirm = "baby:${b.id}" }) { Icon(Icons.Filled.DeleteOutline, contentDescription = t("Sil"), tint = TextTertiary) }
                }
            }
            SettingRow(Icons.Filled.Add, "Bebek ekle", "İkizler ve kardeşler için ayrı profil", tag = "settings_add_baby", locked = !isPlus) { if (isPlus) onAddBaby() else onOpenPaywall() }
        }

        Section("Görünüm") {
            Text("Tema", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Segmented(listOf("system" to "Sistem", "light" to "Açık", "dark" to "Koyu"), ThemePrefs.mode, "theme_") { ThemePrefs.set(it) }
        }
        Section("Dil / Language") {
            Segmented(listOf("tr" to "Türkçe", "en" to "English"), AppLanguage.current, "settings_lang_", translate = false) { AppLanguage.set(it) }
        }

        Section("Hatırlatıcılar") {
            ToggleRow("Alerjen kontrolü", "Devam eden 3 gün denemesi varsa her akşam 19:00", allergenOn, "toggle_allergen") {
                allergenOn = it; if (it) ensureNotifPermission(); Reminders.setAllergen(it)
            }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            ToggleRow("Öğün hatırlatıcıları", "08:30 · 12:30 · 18:00", mealsOn && isPlus, "toggle_meals", locked = !isPlus) {
                if (!isPlus) onOpenPaywall() else { mealsOn = it; if (it) ensureNotifPermission(); Reminders.setMeals(it) }
            }
        }

        if (Community.joined) Section("Topluluk") {
            SettingRow(Icons.Filled.Badge, "Takma ad", Community.nick, tag = "settings_nick") { renaming = true }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.Block, "Engellenen kullanıcılar", t("${Community.blocked.size} kişi") + if (Community.blocked.isEmpty()) "" else t(" · engeli kaldırmak için dokunun"), tag = "settings_blocked") {
                if (Community.blocked.isNotEmpty()) confirm = "unblock"
            }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.Fingerprint, "Topluluk kimliğim", t("Moderatör eklemek için kopyalayın"), tag = "settings_uid") {
                val uid = runCatching { Community.backend.currentUid() }.getOrNull()
                if (uid != null) {
                    platform.copyText(uid)
                    toast("Kimlik kopyalandı.")
                } else toast("Önce topluluğa bağlanın.")
            }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.PersonRemove, "Topluluk hesabımı sil", "Tüm gönderi ve yorumlarınız silinir", tint = ReactionAllergy, tag = "settings_delete_community") { confirm = "community" }
        }

        Section("Veriler") {
            SettingRow(Icons.Filled.Download, "Verileri dışa aktar", "Telefonunuza yedek dosyası (JSON) kaydedin", tag = "settings_export") { exportBackup() }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.Upload, "Yedekten geri yükle", "Daha önce kaydettiğiniz yedeği açın", tag = "settings_import") { pickBackup() }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.RestartAlt, "Beslenme istatistiklerini sıfırla", "Öğün kayıtları ve alerjen testleri", tint = ReactionAllergy, tag = "settings_reset_stats") { confirm = "stats" }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.DeleteSweep, "Alışveriş listesini temizle", "Tüm ürünleri sil", tint = ReactionAllergy, tag = "settings_clear_shopping") { confirm = "shopping" }
        }

        Section("Hakkında") {
            Text("Minik Gurme • Sürüm $versionName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Bu uygulamadaki bilgiler genel rehberlik içindir ve tıbbi tavsiye yerine geçmez. Bebeğinizin beslenmesiyle ilgili kararlar için çocuk doktorunuza danışın.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            SettingRow(Icons.Filled.PrivacyTip, "Gizlilik Politikası", "Bebek verileriniz telefonunuzda kalır", tag = "settings_privacy") { onOpenLegal("privacy") }
            SettingRow(Icons.Filled.Gavel, "Kullanım Koşulları", "Abonelik ve sorumluluk koşulları", tag = "settings_terms") { onOpenLegal("terms") }
            SettingRow(Icons.Filled.Description, "Açık Kaynak Lisansları", "Yazı tipi, ikonlar ve kütüphaneler", tag = "settings_licenses") { onOpenLegal("licenses") }
        }

        if (platform.isDebugBuild) {
            Section("Geliştirici (yalnızca test sürümü)") {
                ToggleRow("Plus'ı test et", "Satın almadan Plus özelliklerini açar", PlusManager.debugOverrideEnabled, "toggle_debug_plus") {
                    PlusManager.setDebugOverride(it)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface), border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CoralDark)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Segmented(options: List<Pair<String, String>>, selected: String, tagPrefix: String, translate: Boolean = true, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (code, label) ->
            val sel = selected == code
            val shown = if (translate) t(label) else label
            if (sel) {
                Button(onClick = { onSelect(code) }, modifier = Modifier.weight(1f).testTag("$tagPrefix$code"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = OnAccent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) { androidx.compose.material3.Text(shown, maxLines = 1, fontWeight = FontWeight.Bold) }
            } else {
                OutlinedButton(onClick = { onSelect(code) }, modifier = Modifier.weight(1f).testTag("$tagPrefix$code"),
                    border = BorderStroke(1.dp, CardBorder), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) { androidx.compose.material3.Text(shown, maxLines = 1, color = TextSecondary) }
            }
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, tint: androidx.compose.ui.graphics.Color = CoralPrimary, tag: String, locked: Boolean = false, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp).testTag(tag)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(if (locked) Icons.Filled.Lock else Icons.Filled.ChevronRight, contentDescription = if (locked) t("Plus") else null, tint = if (locked) OchreWarm else TextTertiary)
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, tag: String, locked: Boolean = false, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (locked) { Spacer(Modifier.width(6.dp)); Icon(Icons.Filled.Lock, contentDescription = null, tint = OchreWarm, modifier = Modifier.size(16.dp)) }
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onChange, modifier = Modifier.testTag(tag),
            colors = SwitchDefaults.colors(checkedTrackColor = CoralPrimary, checkedThumbColor = OnAccent))
    }
}

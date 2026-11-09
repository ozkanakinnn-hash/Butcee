package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLanguage
import com.example.i18n.Text
import com.example.i18n.t
import com.example.ui.theme.*

/** Ayarlar: görünüm (tema), dil, bebek profili, veriler ve hakkında. */
@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onResetStatistics: () -> Unit,
    onClearShopping: () -> Unit
) {
    val context = LocalContext.current
    var confirm by remember { mutableStateOf<String?>(null) } // "stats" | "shopping"

    confirm?.let { what ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = ReactionAllergy) },
            title = { Text(if (what == "stats") "İstatistikler sıfırlansın mı?" else "Alışveriş listesi temizlensin mi?") },
            text = {
                Text(
                    if (what == "stats") "Tüm öğün kayıtları ve alerjen testleri kalıcı olarak silinecek. Bebek profili ve boy/kilo ölçümleri korunur. Bu işlem geri alınamaz."
                    else "Listedeki tüm ürünler silinecek. Bu işlem geri alınamaz."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (what == "stats") onResetStatistics() else onClearShopping()
                        confirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReactionAllergy, contentColor = OnAccent),
                    modifier = Modifier.testTag("settings_confirm")
                ) { Text(if (what == "stats") "Evet, sıfırla" else "Evet, temizle") }
            },
            dismissButton = { TextButton(onClick = { confirm = null }) { Text("Vazgeç") } }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
            Text("Ayarlar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }

        Section("Görünüm") {
            Text("Tema", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Segmented(
                options = listOf("system" to "📱 Sistem", "light" to "☀️ Açık", "dark" to "🌙 Koyu"),
                selected = ThemePrefs.mode,
                tagPrefix = "theme_",
                onSelect = { ThemePrefs.set(context, it) }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "“Sistem” seçiliyken uygulama telefonunuzun açık/koyu mod ayarını izler.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }

        Section("Dil / Language") {
            Segmented(
                options = listOf("tr" to "Türkçe", "en" to "English"),
                selected = AppLanguage.current,
                tagPrefix = "settings_lang_",
                translate = false,
                onSelect = { AppLanguage.set(context, it) }
            )
        }

        Section("Bebek") {
            SettingRow(Icons.Filled.Edit, "Bebek profilini düzenle", "Ad, yaş ve başlangıç tarihi", tag = "settings_edit_profile", onClick = onEditProfile)
        }

        Section("Veriler") {
            SettingRow(Icons.Filled.RestartAlt, "Beslenme istatistiklerini sıfırla", "Öğün kayıtları ve alerjen testleri", tint = ReactionAllergy, tag = "settings_reset_stats") { confirm = "stats" }
            HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
            SettingRow(Icons.Filled.DeleteSweep, "Alışveriş listesini temizle", "Tüm ürünleri sil", tint = ReactionAllergy, tag = "settings_clear_shopping") { confirm = "shopping" }
        }

        Section("Hakkında") {
            Text("Minik Gurme • Sürüm $versionName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "Bu uygulamadaki bilgiler genel rehberlik içindir ve tıbbi tavsiye yerine geçmez. Bebeğinizin beslenmesiyle ilgili kararlar için çocuk doktorunuza danışın.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CoralDark
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Segmented(
    options: List<Pair<String, String>>,
    selected: String,
    tagPrefix: String,
    translate: Boolean = true,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (code, label) ->
            val sel = selected == code
            val shown = if (translate) t(label) else label
            if (sel) {
                Button(
                    onClick = { onSelect(code) },
                    modifier = Modifier.weight(1f).testTag("$tagPrefix$code"),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = OnAccent),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) { androidx.compose.material3.Text(shown, maxLines = 1, fontWeight = FontWeight.Bold) }
            } else {
                OutlinedButton(
                    onClick = { onSelect(code) },
                    modifier = Modifier.weight(1f).testTag("$tagPrefix$code"),
                    border = BorderStroke(1.dp, CardBorder),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                ) { androidx.compose.material3.Text(shown, maxLines = 1, color = TextSecondary) }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color = CoralPrimary,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
    }
}

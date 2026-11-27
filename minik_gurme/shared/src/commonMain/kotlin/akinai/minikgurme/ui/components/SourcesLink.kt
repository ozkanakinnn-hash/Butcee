package akinai.minikgurme.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.ui.theme.*

/** Kaynaklar sayfasını açan uygulama geneli bağlantı (MainScreen ayarlar). App Store 1.4.1: kaynaklar kolay bulunmalı. */
object SourcesNav {
    var open: () -> Unit = {}
}

/** Tıbbi/beslenme bilgisi gösteren ekranların altında: "Kaynaklar: DSÖ, ESPGHAN, Sağlık Bakanlığı, AAP" bağlantısı. */
@Composable
fun SourcesLink(modifier: Modifier = Modifier) {
    Surface(
        onClick = { SourcesNav.open() },
        shape = RoundedCornerShape(14.dp),
        color = SlateBlueSoft.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, BlueBorder),
        modifier = modifier.fillMaxWidth().testTag("sources_link")
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = SlateBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Bilgilerin kaynakları", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SlateBlue)
                Text("DSÖ, ESPGHAN, T.C. Sağlık Bakanlığı, AAP — bağlantılarıyla", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SlateBlue, modifier = Modifier.size(18.dp))
        }
    }
}

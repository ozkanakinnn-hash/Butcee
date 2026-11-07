package com.example.ui.components

import androidx.compose.foundation.clickable

import com.example.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyFooter(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 16.dp, horizontal = 24.dp)
      .testTag("privacy_footer_section"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = t("Gizlilik & Güvenlik Güvencesi"),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = t("🔒 Notlarınız ve kayıtlarınız cihazınızda kalır; hesap gerekmez. Reklam ve bildirim hizmetleri için Gizlilik Politikası'na bakın."),
      fontSize = 10.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      lineHeight = 14.sp
    )
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Text(
      text = t("Gizlilik Politikası ›"),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier
        .padding(top = 4.dp)
        .clickable { com.example.util.AppLinks.open(ctx, com.example.util.AppLinks.PRIVACY_URL) }
        .padding(6.dp)
    )
  }
}

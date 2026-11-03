package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.i18n.AppLanguage
import com.example.i18n.t

private data class RadioChannel(val id: String, val title: String, val urlRes: Int)

private val allChannels = listOf(
  RadioChannel("diyanet", "Diyanet Radyo", R.string.radio_diyanet_url),
  RadioChannel("kuran", "Kur'an Radyo", R.string.radio_diyanet_kuran_url),
  RadioChannel("risalet", "Risalet Radyo", R.string.radio_risalet_url)
)

/** İngilizce seçiliyse yalnızca Kur'an Radyo gösterilir. */
private val channels: List<RadioChannel>
  get() = if (AppLanguage.isEnglish) allChannels.filter { it.id == "kuran" } else allChannels

/** Diyanet'in resmî "Diyanet Radyo TV" uygulaması (Google Play paket adı). */
private const val DIYANET_APP_PACKAGE = "gov.diyanet.diyanetradyotv"

/**
 * Diyanet radyoları. Telif / yayın hakları nedeniyle yayın uygulama İÇİNDE çalınmaz:
 * Diyanet'in resmî uygulaması yüklüyse o açılır, değilse yayın Diyanet'in kendi canlı yayın
 * sayfasında açılır. Böylece uygulama yayını yeniden dağıtmaz.
 */
@Composable
fun DiyanetRadioCard(modifier: Modifier = Modifier) {
  val context = LocalContext.current

  fun open(channel: RadioChannel) {
    // 1) Resmî Diyanet uygulaması
    val appIntent = context.packageManager.getLaunchIntentForPackage(DIYANET_APP_PACKAGE)
    if (appIntent != null) {
      runCatching { context.startActivity(appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.onSuccess { return }
    }
    // 2) Resmî canlı yayın sayfası
    runCatching {
      context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(channel.urlRes))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("diyanet_radio_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Radio, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(t(if (channels.size == 1) "Kur'an Radyo" else "Diyanet Radyo"), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          Text(t("Resmî Diyanet uygulamasında / sitesinde dinleyin"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        channels.forEach { ch ->
          Row(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.primary)
              .clickable { open(ch) }
              .padding(vertical = 9.dp, horizontal = 6.dp)
              .testTag("radio_${ch.id}"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = t(ch.title),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
              maxLines = 1,
              textAlign = TextAlign.Center
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        t("Canlı yayınlar Diyanet'in kendi platformunda açılır."),
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

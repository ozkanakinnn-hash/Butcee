@file:OptIn(ExperimentalMaterial3Api::class)

package com.nobet.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobet.data.ShiftType
import com.nobet.i18n.t
import com.nobet.i18n.typeLetter

@Composable
fun NCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  container: Color = MaterialTheme.colorScheme.surface,
  content: @Composable ColumnScope.() -> Unit,
) {
  val colors = CardDefaults.cardColors(containerColor = container)
  val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  val shape = RoundedCornerShape(16.dp)
  if (onClick != null) {
    Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border) {
      Column(Modifier.padding(16.dp), content = content)
    }
  } else {
    Card(modifier = modifier, shape = shape, colors = colors, border = border) {
      Column(Modifier.padding(16.dp), content = content)
    }
  }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
  Text(
    text,
    modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight = FontWeight.SemiBold,
  )
}

@Composable
fun ShiftBadge(type: ShiftType, size: Dp = 32.dp) {
  val c = shiftColor(type)
  val base = Modifier.size(size).clip(CircleShape)
  val mod = if (type.isOff) base.border(1.5.dp, c, CircleShape) else base.background(c)
  Box(mod, contentAlignment = Alignment.Center) {
    Text(
      typeLetter(type),
      color = if (type.isOff) c else onColor(c),
      fontSize = (size.value * 0.36f).sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
    )
  }
}

@Composable
fun LockedCard(title: String, body: String, onUnlock: () -> Unit) {
  NCard(Modifier.fillMaxWidth(), onClick = onUnlock, container = MaterialTheme.colorScheme.primaryContainer) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
      }
    }
    Spacer(Modifier.height(8.dp))
    Text(
      t("Premium ile aç →", "Unlock with Premium →"),
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.SemiBold,
    )
  }
}

@Composable
fun StatTile(title: String, value: String, sub: String = "", modifier: Modifier = Modifier) {
  NCard(modifier) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 1)
    if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
  }
}

@Composable
fun BarChart(
  values: List<Float>,
  labels: List<String>,
  color: Color,
  goal: Float? = null,
  modifier: Modifier = Modifier,
) {
  val maxV = maxOf(values.maxOrNull() ?: 0f, goal ?: 0f).coerceAtLeast(1f) * 1.15f
  val grid = MaterialTheme.colorScheme.outlineVariant
  val goalC = MaterialTheme.colorScheme.tertiary
  Column(modifier.fillMaxWidth()) {
    Canvas(Modifier.fillMaxWidth().height(140.dp)) {
      val n = values.size.coerceAtLeast(1)
      val slot = size.width / n
      val bw = slot * 0.62f
      for (i in 0..3) {
        val y = size.height * i / 3f
        drawLine(grid, Offset(0f, y), Offset(size.width, y), 1f)
      }
      values.forEachIndexed { i, v ->
        val h = (size.height * (v / maxV)).coerceAtLeast(if (v > 0f) 4f else 0f)
        drawRoundRect(
          color,
          topLeft = Offset(i * slot + (slot - bw) / 2f, size.height - h),
          size = Size(bw, h),
          cornerRadius = CornerRadius(bw / 4f, bw / 4f),
        )
      }
      if (goal != null) {
        val y = size.height * (1f - goal / maxV)
        drawLine(goalC, Offset(0f, y), Offset(size.width, y), 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)))
      }
    }
    if (labels.isNotEmpty()) {
      Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        labels.forEach {
          Text(
            it,
            Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

/** Yatay çubuk: etiket + değer (0..max). */
@Composable
fun HBar(label: String, value: Float, max: Float, valueText: String, color: Color) {
  Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(Modifier.fillMaxWidth()) {
      Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
      Text(valueText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(4.dp))
    val track = MaterialTheme.colorScheme.surfaceVariant
    Canvas(Modifier.fillMaxWidth().height(10.dp)) {
      drawRoundRect(track, cornerRadius = CornerRadius(size.height / 2, size.height / 2))
      val w = size.width * (value / max.coerceAtLeast(0.01f)).coerceIn(0f, 1f)
      drawRoundRect(color, size = Size(w, size.height), cornerRadius = CornerRadius(size.height / 2, size.height / 2))
    }
  }
}

@Composable
fun TimePickDialog(title: String, initialMin: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
  val st = rememberTimePickerState(initialHour = (initialMin / 60) % 24, initialMinute = initialMin % 60, is24Hour = true)
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = { onPick(st.hour * 60 + st.minute) }) { Text(t("Tamam", "OK")) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(t("İptal", "Cancel")) } },
    title = { Text(title) },
    text = { TimeInput(state = st) },
  )
}

@Composable
fun ConfirmDialog(title: String, body: String, confirm: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = MaterialTheme.colorScheme.error) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(t("Vazgeç", "Cancel")) } },
    title = { Text(title) },
    text = { Text(body) },
  )
}

@Composable
fun BigButton(text: String, modifier: Modifier = Modifier, outlined: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
  val m = modifier.fillMaxWidth().height(56.dp)
  if (outlined) {
    OutlinedButton(onClick = onClick, modifier = m, enabled = enabled, shape = RoundedCornerShape(28.dp)) { Text(text, fontSize = 16.sp) }
  } else {
    Button(onClick = onClick, modifier = m, enabled = enabled, shape = RoundedCornerShape(28.dp)) { Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
  }
}

@Composable
fun Gap(h: Dp = 12.dp) = Spacer(Modifier.height(h))

@Suppress("unused")
val Arr = Arrangement.spacedBy(12.dp)

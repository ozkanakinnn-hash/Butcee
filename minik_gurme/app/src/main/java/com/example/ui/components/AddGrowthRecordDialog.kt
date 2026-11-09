package com.example.ui.components

import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke
import com.example.i18n.t
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddGrowthRecordDialog(
    currentBabyAgeMonths: Int = 7,
    latestWeightKg: Float? = null,
    latestHeightCm: Float? = null,
    editing: com.example.data.model.GrowthRecord? = null,
    onDismiss: () -> Unit,
    onSave: (date: String, weightKg: Float, heightCm: Float, headCircumferenceCm: Float?, ageMonths: Int, notes: String) -> Unit
) {
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var dateText by remember { mutableStateOf(editing?.date ?: todayDate) }
    var ageMonths by remember { mutableStateOf((editing?.ageMonths ?: currentBabyAgeMonths).coerceAtLeast(0)) }
    var weightText by remember {
        mutableStateOf(editing?.let { String.format(Locale.US, "%.2f", it.weightKg) } ?: if (latestWeightKg != null && latestWeightKg > 0) String.format(Locale.US, "%.2f", latestWeightKg) else "")
    }
    var heightText by remember {
        mutableStateOf(editing?.let { String.format(Locale.US, "%.1f", it.heightCm) } ?: if (latestHeightCm != null && latestHeightCm > 0) String.format(Locale.US, "%.1f", latestHeightCm) else "")
    }
    var headCircumferenceText by remember { mutableStateOf(editing?.headCircumferenceCm?.let { String.format(Locale.US, "%.1f", it) } ?: "") }
    var notesText by remember { mutableStateOf(editing?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CoralSoft),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.ui.components.FoodImage("📏", 23.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (editing != null) "Ölçümü Düzenle" else "Yeni Ölçüm Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Boy & kilo gelişimini kaydet",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarih & Ay Seçimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date input
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Tarih (YYYY-AA-GG)", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("growth_input_date"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    // Month counter
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { if (ageMonths > 0) ageMonths-- },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = t("Azalt"),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$ageMonths. Ay",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Bebek Yaşı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 9.sp
                                )
                            }
                            IconButton(
                                onClick = { ageMonths++ },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = t("Arttır"),
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Kilo Input
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        errorMessage = null
                    },
                    label = { Text("Kilo (kg) * (ör: 8.45)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_input_weight"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "kg",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                // Boy Input
                OutlinedTextField(
                    value = heightText,
                    onValueChange = {
                        heightText = it
                        errorMessage = null
                    },
                    label = { Text("Boy (cm) * (ör: 69.5)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_input_height"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = SageDark,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "cm",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                // Baş Çevresi Input (Opsiyonel)
                OutlinedTextField(
                    value = headCircumferenceText,
                    onValueChange = { headCircumferenceText = it },
                    label = { Text("Baş Çevresi (cm) (İsteğe bağlı)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_input_head"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ChildCare,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Text(
                            text = "cm",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                // Notlar
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Doktor / Gelişim Notu (İsteğe bağlı)") },
                    placeholder = { Text("Örn: Doktor kilosu çok iyi dedi, ek gıdaya devam.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("growth_input_notes"),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val weight = weightText.replace(',', '.').toFloatOrNull()
                    val height = heightText.replace(',', '.').toFloatOrNull()
                    val head = headCircumferenceText.replace(',', '.').toFloatOrNull()

                    if (weight == null || weight <= 0f) {
                        errorMessage = "Lütfen geçerli bir kilo değeri girin (ör: 8.45)"
                        return@Button
                    }
                    if (height == null || height <= 0f) {
                        errorMessage = "Lütfen geçerli bir boy değeri girin (ör: 69.5)"
                        return@Button
                    }

                    val cleanDate = dateText.trim().ifBlank { todayDate }
                    onSave(cleanDate, weight, height, head, ageMonths, notesText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("save_growth_record_btn")
            ) {
                Text(
                    text = "Kaydet",
                    fontWeight = FontWeight.Bold,
                    color = OnAccent
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = "İptal",
                    color = TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

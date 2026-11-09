package com.example.ui.components

import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke
import com.example.i18n.t
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.ReactionAllergy
import com.example.ui.theme.ReactionAllergySoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionNoteDialog(
    foodName: String,
    initialReaction: String = "loved",
    initialSymptoms: String = "",
    initialNotes: String = "",
    onDismiss: () -> Unit = {},
    onSave: (reaction: String, symptoms: String, notes: String) -> Unit
) {
    var selectedReaction by remember { mutableStateOf(initialReaction.ifBlank { "loved" }) }
    val selectedSymptoms = remember {
        mutableStateListOf<String>().apply {
            if (initialSymptoms.isNotBlank()) {
                addAll(initialSymptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
    }
    var notesText by remember { mutableStateOf(initialNotes) }

    val commonSymptoms = listOf(
        "Hapşırma",
        "Öksürük veya hırıltı",
        "Nefes almada güçlük (ACİL)",
        "Ciltte döküntü / kızarıklık",
        "Dudak veya yüzde şişme",
        "Kusma",
        "İshal / mukuslu kaka",
        "Aşırı huzursuzluk / ağlama",
        "Gözlerde sulanma ve kaşıntı",
        "Ağız çevresinde kızarıklık"
    )

    val reactionTypes = listOf(
        Triple("loved", "❤️ Çok Sevdi", SageSoft),
        Triple("neutral", "😐 Nötr / Alışıyor", SlateBlueSoft),
        Triple("disliked", "😣 Sevmedi / İtti", OchreSoft),
        Triple("allergy", "🚨 Alerji / Tepki", ReactionAllergySoft)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("reaction_note_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, CardBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reaksiyon & Not Kaydet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = foodName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = t("Kapat"),
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 1. Genel Tepki Seçimi
                Text(
                    text = "GENEL REAKSİYON",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reactionTypes.forEach { (type, label, _) ->
                        val isSelected = selectedReaction == type
                        Surface(
                            onClick = { selectedReaction = type },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                if (type == "allergy") ReactionAllergy else CoralPrimary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color.Transparent else CardBorder
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // 2. Gözlenen Belirtiler (Multi-select)
                Text(
                    text = "GÖZLEMLENEN BELİRTİLER (VARSA SEÇİN)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonSymptoms.forEach { symptom ->
                        val isChecked = selectedSymptoms.contains(symptom)
                        val isEmergency = symptom.contains("ACİL")
                        Surface(
                            onClick = {
                                if (isChecked) selectedSymptoms.remove(symptom)
                                else {
                                    selectedSymptoms.add(symptom)
                                    selectedReaction = "allergy"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                isChecked && isEmergency -> ReactionAllergy
                                isChecked -> CoralSoft
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isChecked && isEmergency -> ReactionAllergy
                                    isChecked -> CoralPrimary
                                    else -> CardBorder
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEmergency) {
                                    Icon(
                                        imageVector = Icons.Default.Emergency,
                                        contentDescription = null,
                                        tint = if (isChecked) Color.White else ReactionAllergy,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = symptom,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isChecked && isEmergency -> Color.White
                                        isChecked -> CoralDark
                                        else -> TextPrimary
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Notlar
                Text(
                    text = "GÖZLEM NOTLARI",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = {
                        Text(
                            text = "Kaç tatlı kaşığı yedi, nasıl hazırlandı, ne zaman tepki verdi...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .testTag("reaction_notes_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // 4. Clinical Guidance & Emergency Alerts (Matching Screenshot 1)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ReactionAllergySoft.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, AllergyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = ReactionAllergy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alerjik bir reaksiyondan şüpheleniyorsanız, yiyeceği yedirmeyi derhal bırakın. Küçük bir miktar yedirmiş olsanız bile daha fazla beslemekten kaçının.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AllergyText,
                                lineHeight = 18.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = ReactionAllergy,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bebeğinizde nefes almada güçlük, hırıltılı solunum, dudak/boğazda şişme gibi ciddi belirtiler görülüyorsa vakit kaybetmeden 112 Acil servisi arayın.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ReactionAllergy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // 5. Kaydet Butonu
                Button(
                    onClick = {
                        val sympStr = selectedSymptoms.joinToString(", ")
                        onSave(selectedReaction, sympStr, notesText.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_reaction_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text(
                        text = "Kaydet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

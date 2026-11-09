package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import com.example.i18n.t
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AllergenTest
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.OchreDark
import com.example.ui.theme.OchreSoft
import com.example.ui.theme.OchreWarm
import com.example.ui.theme.ReactionAllergy
import com.example.ui.theme.ReactionAllergySoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ThreeDayRuleScreen(
    tests: List<AllergenTest>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenAddTest: () -> Unit,
    onUpdateDay: (Long, Int) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onDeleteTest: (Long) -> Unit,
    onOpenReactionNote: (AllergenTest) -> Unit = {}
) {
    var testToDelete by remember { mutableStateOf<AllergenTest?>(null) }

    val filterOptions = listOf("Tümü", "Devam Edenler", "Güvenli Olanlar", "Reaksiyon / Riskli")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Header Title & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "3 Gün Kuralı & Alerjenler",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Yeni besinleri 3 gün boyunca tek tek ve az miktarla deneyin",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onOpenAddTest,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier.testTag("add_allergen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Besin Ekle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Rule Explanation Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CoralSoft),
                border = BorderStroke(1.dp, CoralBorder)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CoralPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3 Gün Kuralı Nasıl Uygulanır?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yeni bir besini ilk kez verirken 3 gün boyunca başka yeni besin eklemeden, sabah/öğle saatlerinde az miktarla (1 çay kaşığı) başlayıp kademeli artırın. Döküntü, kusma veya sindirim değişimini 2 saat boyunca gözlemleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B2A1C),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = TextSecondary,
                            selectedContainerColor = CoralPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) CoralPrimary else CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Allergen Test Cards
        if (tests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🥑", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Bu filtrede kayıtlı besin bulunamadı",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yeni bir alerjen denemesi eklemek için 'Besin Ekle' butonuna dokunun.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(tests, key = { it.id }) { test ->
                AllergenTestCard(
                    test = test,
                    onUpdateDay = { day -> onUpdateDay(test.id, day) },
                    onToggleReaction = {
                        val newStatus = if (test.status == "Reaksiyon") "Devam Ediyor" else "Reaksiyon"
                        onUpdateStatus(test.id, newStatus)
                    },
                    onOpenReactionNote = { onOpenReactionNote(test) },
                    onDelete = { testToDelete = test }
                )
            }
        }

        // Alerjik Reaksiyon Belirtileri (Dr. Eda Tezgel infografiği: Hafif vs Ciddi)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Alerjik Reaksiyon Belirtileri Nelerdir?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hafif
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = OchreSoft
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚠️ Hafif Belirtiler",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OchreDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Ağız çevresinde kızarıklık\n• Kurdeşen (deride döküntü)\n• Hafif kusma / ishal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B4B03),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        // Ciddi
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = ReactionAllergySoft
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🚨 Ciddi (Acil Durum)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ReactionAllergy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Nefes darlığı / hırıltı\n• Yüz, dudak, dil şişliği\n• Fışkırır tarzda kusma\n• Acil 112 aranmalıdır!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF881337),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Delete Confirmation
    testToDelete?.let { test ->
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text("Kaydı Sil", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("\"${test.food}\" için başlatılan 3 gün alerjen test kaydını silmek istediğinize emin misiniz?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTest(test.id)
                        testToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { testToDelete = null }) {
                    Text("İptal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun AllergenTestCard(
    test: AllergenTest,
    onUpdateDay: (Int) -> Unit,
    onToggleReaction: () -> Unit,
    onOpenReactionNote: () -> Unit = {},
    onDelete: () -> Unit
) {
    val statusBadge = when (test.status) {
        "Güvenli" -> Triple("✅ Güvenli", SageSoft, SageDark)
        "Reaksiyon" -> Triple("🚨 Reaksiyon Görüldü", ReactionAllergySoft, ReactionAllergy)
        else -> Triple("⏳ Devam Ediyor", OchreSoft, OchreDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("allergen_card_${test.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Food name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CoralSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🥣", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = test.food,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (test.startDate.isNotBlank()) {
                            Text(
                                text = "Başlangıç: ${test.startDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }

                Surface(
                    color = statusBadge.second,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = statusBadge.first,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusBadge.third,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Day Stepper Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Deneme Aşaması",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = "${test.day} / 3. Gün Tamamlandı",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stepLabels = listOf("1. Gün: Tadım", "2. Gün: Artır", "3. Gün: Kontrol")
                    for (dayNum in 1..3) {
                        val isActive = test.day >= dayNum
                        val bgColor by animateColorAsState(
                            targetValue = if (isActive) SageGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            label = "dayBtnColor"
                        )
                        val textColor = if (isActive) Color.White else TextSecondary

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onUpdateDay(dayNum) }
                                .testTag("day_${dayNum}_btn_${test.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = if (!isActive) BorderStroke(1.dp, CardBorder) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = "$dayNum. Gün",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (test.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "“${test.notes}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Actions: Reaction Toggle + Reaction Note + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onToggleReaction,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (test.status == "Reaksiyon") SageDark else ReactionAllergy
                        ),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (test.status == "Reaksiyon") "Reaksiyonu Kaldır" else "🚨 Reaksiyon",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (test.status == "Reaksiyon") SageDark else ReactionAllergy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenReactionNote,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CoralBorder),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "📝 Not & Belirtiler",
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = t("Sil"),
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

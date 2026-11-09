package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import com.example.i18n.t
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.guide.FoodDirectoryData
import com.example.data.guide.PediatricGuideData
import com.example.data.model.AllergenTest
import com.example.data.model.BabyProfile
import com.example.data.model.FoodDirectoryItem
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
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationsScreen(
    profile: BabyProfile,
    tests: List<AllergenTest>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenAddTest: () -> Unit,
    onUpdateDay: (Long, Int) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onDeleteTest: (Long) -> Unit,
    onOpenReactionNote: (AllergenTest) -> Unit = {},
    onSelectFood: (FoodDirectoryItem) -> Unit = {}
) {
    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: Günün & Ayın Önerileri, 1: 3 Gün Alerjen Takibi

    val currentMonthGuide = remember(profile.ageMonths) {
        PediatricGuideData.monthlyGuides.firstOrNull { it.month == profile.ageMonths }
            ?: PediatricGuideData.monthlyGuides.first()
    }

    // Recommended superfoods for this month
    val recommendedFoods = remember(profile.ageMonths) {
        FoodDirectoryData.allFoods.filter { it.minAgeMonths <= profile.ageMonths }.take(4)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("recommendations_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                HeroGradient
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PEDİATRİK BESLENME ÖNERİLERİ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${profile.ageMonths}. Ay Rehberi",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CoralDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "${profile.name} İçin Akıllı Öneriler 💡",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "Uzm. Dr. Eda Nur Tezgel Oruç protokollerine uygun ay ay beslenme, doku ve güvenlik tavsiyeleri.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.95f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 2. Segmented Section Tabs: "Günün & Ayın Önerileri" vs "3 Gün Alerjen Takibi"
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        onClick = { selectedSectionTab = 0 },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedSectionTab == 0) CoralPrimary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (selectedSectionTab == 0) Color.White else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Beslenme Önerileri",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedSectionTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSectionTab == 0) Color.White else TextPrimary
                            )
                        }
                    }

                    Surface(
                        onClick = { selectedSectionTab = 1 },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedSectionTab == 1) CoralPrimary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = if (selectedSectionTab == 1) Color.White else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3 Gün Kuralı (${tests.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedSectionTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSectionTab == 1) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        if (selectedSectionTab == 0) {
            // === TAB 0: BESLENME ÖNERİLERİ & AY AY TAVSİYELER ===

            // Month Goals Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎯", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${profile.ageMonths}. Ayın Temel Hedefleri",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentMonthGuide.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        currentMonthGuide.goals.forEach { goal ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = goal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Örnek Günlük Rutin Önerisi (Matching Dr. Eda infographic)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏰", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Önerilen Günlük Öğün Akışı",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Saatler bebeğinizin uykusuna göre esnetilebilir",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Sabah
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OchreSoft,
                            border = BorderStroke(1.dp, OchreBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "☀️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Sabah (Uyanış): Anne Sütü veya Formül Mama",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OchreDark
                                    )
                                    Text(
                                        text = "İlk yıl beslenmesinin %70'ini anne sütü/mama oluşturmaya devam eder.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Kuşluk / Öğle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoralSoft,
                            border = BorderStroke(1.dp, CoralBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🥣", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Öğle: Kıymalı Sebze Ezmesi / Yoğurt Denemesi",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralDark
                                    )
                                    Text(
                                        text = "Demir depoları için 2 tatlı kaşığı kıymalı kabak/havuç ezmesi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // İkindi / Akşam
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SageSoft,
                            border = BorderStroke(1.dp, SageBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🌙", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "İkindi / Akşam: Anne Sütü & Küçük Meyve Tadımı",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SageDark
                                    )
                                    Text(
                                        text = "Cam rendeden geçirilmiş çeyrek elma veya avokado püresi.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sinerji Önerisi: Demir + C Vitamini
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = SlateBlueSoft,
                    border = BorderStroke(1.dp, BlueBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🥩🍋", fontSize = 26.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Altın Emilim Kuralı: Demir + C Vitamini",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateBlue
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kıymalı veya mercimekli sebze yemeklerine birkaç damla limon sıkmak, demir emilimini 3 katına çıkarır!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Sıradaki Süper Besin Önerileri
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SIRADAKİ DENENECEK SÜPER BESİNLER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recommendedFoods.forEach { food ->
                            Surface(
                                onClick = { onSelectFood(food) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = food.emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = food.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = food.ageDisplay,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CoralDark,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sık Yapılan Hatalar & Yasaklar Kartı
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ReactionAllergySoft.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, AllergyBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = ReactionAllergy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1 Yaşına Kadar Kesinlikle Yasak Olanlar",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ReactionAllergy
                            )
                        }

                        Text(
                            text = "❌ Bal (Botulizm felci riski)\n❌ Tuz ve İlave Şeker (Böbrek yükü ve tat eşiği)\n❌ Çiğ süt / Pastörize edilmemiş peynir\n❌ Çiğ sert kuruyemiş ve çiğ havuç (Boğulma tehlikesi)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AllergyText,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            // === TAB 1: 3 GÜN KURALI & ALERJEN TAKİBİ ===
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "3 Gün Alerjen Takibi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Her yeni besini 3 gün tek başına deneyin",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = onOpenAddTest,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Besin Ekle", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Chips
            item {
                val filterOptions = listOf("Tümü", "Devam Edenler", "Güvenli Olanlar", "Reaksiyon / Riskli")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CoralPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Tests List
            if (tests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🧪", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Kayıtlı alerjen denemesi yok",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "'Besin Ekle' butonuna basarak yumurta, yoğurt veya yer fıstığı gibi besinleri 3 gün takip edebilirsiniz.",
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
                        onDelete = { onDeleteTest(test.id) }
                    )
                }
            }
        }
    }
}

package akinai.minikgurme.ui.components

import akinai.minikgurme.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import akinai.minikgurme.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import akinai.minikgurme.data.guide.KnowledgeBaseFoodItem
import akinai.minikgurme.data.guide.PediatricKnowledgeBaseData
import akinai.minikgurme.data.guide.WeeklyPediatricGuide
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.OchreBorder
import akinai.minikgurme.ui.theme.OchreDark
import akinai.minikgurme.ui.theme.OchreSoft
import akinai.minikgurme.ui.theme.OchreWarm
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.ReactionAllergySoft
import akinai.minikgurme.ui.theme.SageBorder
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PediatricKnowledgeBaseView(
    initialMonth: Int = 6,
    onOpenRecipesForMonth: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableIntStateOf(initialMonth.coerceIn(6, 12)) }
    var expandedGuideId by remember { mutableStateOf<String?>("guide_${initialMonth.coerceIn(6, 12)}_w1") }

    val displayedGuides = remember(searchQuery, selectedMonth) {
        if (searchQuery.isNotBlank()) {
            PediatricKnowledgeBaseData.searchGuides(searchQuery)
        } else {
            PediatricKnowledgeBaseData.getGuidesForMonth(selectedMonth)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Clinical Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kb_hero_banner"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CoralSoft),
            border = BorderStroke(1.dp, CoralBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                .background(CardSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            akinai.minikgurme.ui.components.FoodImage("📚", 26.dp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Bilgi Bankası",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Haftalık beslenme ve gelişim protokolü",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Bebeğinizin ayına ve haftasına özel beslenme hedefleri, pütürlüye geçiş basamakları, örnek günlük rutinler ve güvenli hazırlık yönergeleri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kb_search_input"),
            placeholder = { Text("Bilgi Bankasında ara (örn: demir, BLW, püre, yoğurt, kabızlık)...", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CoralPrimary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = t("Temizle"))
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = CoralPrimary,
                unfocusedBorderColor = CardBorder
            ),
            singleLine = true
        )

        // Month Selector Chips (when not searching)
        if (searchQuery.isBlank()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val months = listOf(6, 7, 8, 9, 10, 12)
                items(months) { m ->
                    val isSelected = selectedMonth == m
                    val label = when (m) {
                        10 -> "10-11. Ay"
                        12 -> "12+ Ay (1 Yaş)"
                        else -> "$m. Ay"
                    }
                    Surface(
                        modifier = Modifier
                            .clickable {
                                selectedMonth = m
                                expandedGuideId = "guide_${m}_w1"
                            }
                            .testTag("kb_month_chip_$m"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) CoralPrimary else CardBorder),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAccent else TextPrimary
                        )
                    }
                }
            }
        }

        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (searchQuery.isNotBlank()) "ARAMA SONUÇLARI (${displayedGuides.size})" else "$selectedMonth. AY HAFTALIK REHBERLERİ (${displayedGuides.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            if (onOpenRecipesForMonth != null) {
                Text(
                    text = "İlgili Tarifler",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary,
                    modifier = Modifier
                        .clickable { onOpenRecipesForMonth(selectedMonth) }
                        .padding(4.dp)
                )
            }
        }

        // List of Weekly Guides
        if (displayedGuides.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    akinai.minikgurme.ui.components.FoodImage("🔍", 42.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aranan terimle eşleşen rehber bulunamadı",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Farklı bir kelime (örn: 'demir', 'pütürlü', 'avokado', 'alerji') aramayı deneyin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            displayedGuides.forEach { guide ->
                val isExpanded = expandedGuideId == guide.id
                WeeklyGuideCard(
                    guide = guide,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedGuideId = if (isExpanded) null else guide.id
                    }
                )
            }
        }
    }
}

@Composable
private fun WeeklyGuideCard(
    guide: WeeklyPediatricGuide,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_guide_${guide.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isExpanded) CoralBorder else CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Week Badge + Title + Expand Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpanded) CoralPrimary else CoralSoft
                    ) {
                        Text(
                            text = "${guide.weekNumber}. Hafta",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) OnAccent else CoralDark
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = guide.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = guide.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1
                        )
                    }
                }

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Kapat" else "Aç",
                        tint = CoralPrimary
                    )
                }
            }

            // Expanded Detailed Medical Body
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // 1. Gelişimsel & Pediatrik Hedef
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SageSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SageBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            akinai.minikgurme.ui.components.FoodImage("🎯", 23.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Gelişimsel & Beslenme Hedefi",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = guide.developmentalGoal,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Kıvam & Doku Kılavuzu
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = OchreSoft.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, OchreBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            akinai.minikgurme.ui.components.FoodImage("🥣", 23.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Doku & Kıvam Aşaması",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OchreDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = guide.textureMilestone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Örnek Günlük Rutin
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SlateBlueSoft.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = SlateBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Örnek Günlük Beslenme Rutini",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = guide.dailyRoutine,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Bu Haftanın Önerilen Besinleri (Cards)
                    if (guide.weeklyFoods.isNotEmpty()) {
                        Text(
                            text = "BU HAFTANIN ODAK BESİNLERİ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        guide.weeklyFoods.forEach { food ->
                            WeeklyFoodItemCard(food = food)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5. Pediatristin Altın Kuralları
                    if (guide.goldenRules.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Altın Kurallar",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                guide.goldenRules.forEach { rule ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "•", color = CoralPrimary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = rule,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Güvenlik & Kırmızı Bayraklar
                    if (guide.safetyAndAlerts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ReactionAllergySoft.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, ReactionAllergy.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ReactionAllergy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Güvenlik & Dikkat Kırmızı Bayrakları",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ReactionAllergy
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                guide.safetyAndAlerts.forEach { alert ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        akinai.minikgurme.ui.components.FoodImage("⚠", 16.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = alert,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7. Sık Sorulan Sorular (FAQs)
                    if (guide.faqs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.faqs.forEach { faq ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${faq.question}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = faq.answer,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // 8. Tıbbi Kaynak Dipnotu
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = SageDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kaynak: ${guide.medicalSource}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyFoodItemCard(food: KnowledgeBaseFoodItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CardSurface),
                contentAlignment = Alignment.Center
            ) {
                akinai.minikgurme.ui.components.FoodImage(food.emoji, 26.dp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (food.allergenLevel.contains("Alerjen") || food.allergenLevel.contains("Gözlem")) ReactionAllergySoft else SageSoft
                    ) {
                        Text(
                            text = food.allergenLevel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (food.allergenLevel.contains("Alerjen") || food.allergenLevel.contains("Gözlem")) ReactionAllergy else SageDark,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${food.prepInstructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Text(
                    text = "Porsiyon: ${food.portionGuide} • ${food.pediatricBenefit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import com.example.i18n.t
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.data.guide.FoodStartMonthData
import com.example.data.guide.FoodStartRecommendation
import com.example.data.guide.ForbiddenFoodItem
import com.example.data.guide.PediatricGuideData
import com.example.ui.components.PediatricKnowledgeBaseView
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
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
import com.example.ui.theme.SoftPurple
import com.example.ui.theme.SoftPurpleSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GuideScreen(
    selectedMonth: Int,
    selectedTab: String,
    onMonthSelect: (Int) -> Unit,
    onTabSelect: (String) -> Unit
) {
    var activeStarterWeek by remember { mutableIntStateOf(1) }
    val readinessChecks = remember { mutableStateMapOf<Int, Boolean>() }

    // Hangi Besin Hangi Ayda? Filtre Durumları
    var foodSearchQuery by remember { mutableStateOf("") }
    var selectedFilterMonth by remember { mutableIntStateOf(0) } // 0: Tüm Aylar
    var selectedFilterCategory by remember { mutableStateOf("Tümü") }

    val currentMonthGuide = PediatricGuideData.monthlyGuides.find { it.month == selectedMonth }
        ?: PediatricGuideData.monthlyGuides.first()

    val filteredFoods = remember(foodSearchQuery, selectedFilterMonth, selectedFilterCategory) {
        FoodStartMonthData.allRecommendations.filter { food ->
            val matchesMonth = when (selectedFilterMonth) {
                0 -> true
                6 -> food.startMonth == 6
                7 -> food.startMonth == 7
                8 -> food.startMonth == 8
                9 -> food.startMonth in 9..10
                11 -> food.startMonth in 11..12
                12 -> food.startMonth >= 12
                else -> food.startMonth == selectedFilterMonth
            }

            val matchesCategory = if (selectedFilterCategory == "Tümü") true else {
                food.category.contains(selectedFilterCategory.substringBefore(" ").trim(), ignoreCase = true)
            }

            val matchesSearch = if (foodSearchQuery.isBlank()) true else {
                food.name.contains(foodSearchQuery, ignoreCase = true) ||
                food.whyIntroduce.contains(foodSearchQuery, ignoreCase = true) ||
                food.servingTip.contains(foodSearchQuery, ignoreCase = true) ||
                food.category.contains(foodSearchQuery, ignoreCase = true)
            }

            matchesMonth && matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Header Title & Pediatric Certification Badge
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ek Gıda & Beslenme Rehberi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Surface(
                        color = SageSoft,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = SageDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WHO & CDC Uyumlu",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageDark
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bebek yaşına uygun besin önerileri ve ay ay başlama takvimi",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Top Category Navigation Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val tabs = listOf(
                    Triple("knowledge_base", "📚 Bilgi Bankası", "bilgi_bankasi_tab"),
                    Triple("foods_by_month", "🥕 Hangi Besin Hangi Ayda?", "besin_ay_tab"),
                    Triple("month", "📅 Ay Ay Kılavuz & Menü", "ay_rehber_tab"),
                    Triple("forbidden", "🚫 1 Yaş Yasakları", "yasaklar_tab"),
                    Triple("starter", "🗓️ 4 Haftalık Plan", "starter_plan_tab"),
                    Triple("safety", "🛡️ Güvenlik & Boğulma", "guvenlik_tab"),
                    Triple("mistakes", "💡 Sık Yapılan 6 Hata", "hatalar_tab"),
                    Triple("readiness", "✅ Hazır mıyız?", "hazir_oluş_tab")
                )

                items(tabs) { (key, label, tag) ->
                    val isSelected = selectedTab == key
                    Surface(
                        modifier = Modifier
                            .clickable { onTabSelect(key) }
                            .testTag(tag),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSelected) CoralPrimary else CardBorder),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAccent else TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB: PEDİATRİK BİLGİ BANKASI (HAFTALIK REHBERLER)
        // -------------------------------------------------------------
        if (selectedTab == "knowledge_base") {
            item {
                PediatricKnowledgeBaseView(
                    initialMonth = selectedMonth
                )
            }
        }

        // -------------------------------------------------------------
        // TAB 0: HANGİ BESİN HANGİ AYDA BAŞLAR? (BESİN REHBERİ)
        // -------------------------------------------------------------
        if (selectedTab == "foods_by_month") {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CoralSoft),
                    border = BorderStroke(1.dp, CoralBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hangi Besin Hangi Ayda Başlar?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralDark
                            )
                        }
                        Text(
                            text = "Bebeklerin sindirim sistemi, böbrek fonksiyonları ve çiğneme refleksine göre pediatrik rehberlerle hazırlanan başlangıç tablosu. Arama veya filtreleri kullanarak istediğiniz gıdayı inceleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = foodSearchQuery,
                    onValueChange = { foodSearchQuery = it },
                    placeholder = { Text("Besin ara: örn. Avokado, Yumurta, Balık, Yoğurt...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (foodSearchQuery.isNotBlank()) {
                            IconButton(onClick = { foodSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = t("Temizle"),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guide_food_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Month Filter Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BAŞLANGIÇ AYINA GÖRE FİLTRELE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    val monthFilters = listOf(
                        0 to "Tüm Aylar",
                        6 to "6. Ay (Başlangıç)",
                        7 to "7. Ay (Çeşitlilik)",
                        8 to "8. Ay (Pütürlü)",
                        9 to "9-10. Ay (Balık)",
                        11 to "11-12. Ay (Sofra)",
                        12 to "12+ Ay (1 Yaş)"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(monthFilters) { (monthVal, label) ->
                            val isSelected = selectedFilterMonth == monthVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterMonth = monthVal },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CoralSoft,
                                    selectedLabelColor = CoralDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CoralBorder
                                )
                            )
                        }
                    }
                }
            }

            // Category Filter Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "BESİN KATEGORİSİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    val categoryFilters = listOf(
                        "Tümü", "Sebze", "Meyve", "Protein & Et", "Süt Ürünü", "Tahıl & Bakliyat", "Sağlıklı Yağlar"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoryFilters) { cat ->
                            val isSelected = selectedFilterCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OchreSoft,
                                    selectedLabelColor = OchreDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = OchreWarm
                                )
                            )
                        }
                    }
                }
            }

            // Result Count and Reset Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toplam ${filteredFoods.size} besin önerisi listeleniyor",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    if (selectedFilterMonth != 0 || selectedFilterCategory != "Tümü" || foodSearchQuery.isNotBlank()) {
                        Text(
                            text = "Filtreleri Temizle",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralPrimary,
                            modifier = Modifier
                                .clickable {
                                    foodSearchQuery = ""
                                    selectedFilterMonth = 0
                                    selectedFilterCategory = "Tümü"
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Food Cards List
            if (filteredFoods.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Aradığınız kriterlere uygun besin bulunamadı",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lütfen arama terimini veya filtre seçimlerini değiştirin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredFoods) { food ->
                    FoodIntroCard(food = food)
                }
            }
        }

        // -------------------------------------------------------------
        // TAB: 1 YAŞ YASAKLARI & KRİTİK GÜVENLİK
        // -------------------------------------------------------------
        if (selectedTab == "forbidden") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ReactionAllergySoft),
                    border = BorderStroke(1.5.dp, ReactionAllergy)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = ReactionAllergy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1 Yaşından Önce KESİNLİKLE Yasak Olanlar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ReactionAllergy
                            )
                        }
                        Text(
                            text = "Bebeklerin böbrekleri, karaciğerleri ve sindirim mikrobiyotası henüz tam gelişmemiştir. Aşağıdaki gıdalar botulizm, mikroskobik kanama ve boğulma gibi hayati riskler taşıdığından 12 aydan önce kesinlikle verilmemelidir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            items(FoodStartMonthData.forbiddenFoodsBeforeOneYear) { forbidden ->
                ForbiddenFoodCard(item = forbidden)
            }
        }

        // -------------------------------------------------------------
        // TAB 1: AY AY REHBER (6, 7, 8, 9, 10, 11, 12. AYLAR)
        // -------------------------------------------------------------
        if (selectedTab == "month") {
            // Month Selector Pills
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PediatricGuideData.monthlyGuides) { guide ->
                        val isSelected = guide.month == selectedMonth
                        Surface(
                            modifier = Modifier
                                .clickable { onMonthSelect(guide.month) }
                                .testTag("month_pill_${guide.month}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CoralSoft else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) CoralPrimary else CardBorder
                            ),
                            shadowElevation = if (isSelected) 1.dp else 0.dp
                        ) {
                            Text(
                                text = guide.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CoralDark else TextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Month Guide Content Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Title & Summary
                        Column {
                            Text(
                                text = currentMonthGuide.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentMonthGuide.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }

                        // 1. Hedefler
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🎯 BU AYIN TEMEL HEDEFLERİ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            currentMonthGuide.goals.forEach { goal ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SageGreen,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = goal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // 2. Kıvamlar
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🥣 KIVAM BASAMAKLARI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currentMonthGuide.textures.forEach { texture ->
                                    Surface(
                                        color = CoralSoft,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CoralBorder)
                                    ) {
                                        Text(
                                            text = texture,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralDark,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Uygun Besinler
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🥦 ÖNERİLEN VE EKLENEBİLECEK BESİNLER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                currentMonthGuide.recommendedFoods.forEach { food ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CardBorder)
                                    ) {
                                        Text(
                                            text = "✨ $food",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Örnek Günlük Menü Akışı
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = OchreSoft,
                            border = BorderStroke(1.dp, OchreWarm.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = OchreDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ÖRNEK GÜNLÜK MENÜ VE ÖĞÜN AKIŞI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OchreDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = currentMonthGuide.sampleMealRoutine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OchreText,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // 5. Dikkat & Kesin Yasaklar
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ReactionAllergySoft,
                            border = BorderStroke(1.dp, ReactionAllergy.copy(alpha = 0.25f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ReactionAllergy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DİKKAT & KESİN YASAKLAR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ReactionAllergy
                                    )
                                }
                                currentMonthGuide.warnings.forEach { warning ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Block,
                                            contentDescription = null,
                                            tint = ReactionAllergy,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = warning,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AllergyText,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        // 6. Doktor Notu
                        if (currentMonthGuide.doctorNote.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(text = "🩺", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentMonthGuide.doctorNote,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 2: 4 HAFTALIK BAŞLANGIÇ PLANI (6. AY ADIM ADIM)
        // -------------------------------------------------------------
        if (selectedTab == "starter") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CoralSoft),
                    border = BorderStroke(1.dp, CoralBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "6. Ay Ek Gıda Yolculuğu: 4 Haftalık Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CoralDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "İlk 28 gün boyunca bebeğinizi sebzeler, demir kaynakları, yumurta, yoğurt ve alerjenlerle kademeli tanıştıran pediatrik program.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Week Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PediatricGuideData.fourWeekStarterPlan.forEach { plan ->
                        val isSelected = activeStarterWeek == plan.weekNumber
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeStarterWeek = plan.weekNumber },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSelected) CoralPrimary else CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${plan.weekNumber}. Hafta",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) OnAccent else TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Active Week Content
            val plan = PediatricGuideData.fourWeekStarterPlan.find { it.weekNumber == activeStarterWeek }
                ?: PediatricGuideData.fourWeekStarterPlan.first()

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = plan.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = plan.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // Focus Foods
                        Text(
                            text = "BU HAFTANIN ODAK BESİNLERİ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        plan.focusFoods.forEach { food ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = food.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = food.prepTip,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Daily Schedule
                        Text(
                            text = "7 GÜNLÜK ADIM ADIM MENÜ AKIŞI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            plan.dailyPlan.forEach { day ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardSurface,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = day.dayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = CoralPrimary
                                            )
                                            Text(
                                                text = day.mealTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = day.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 3: GÜVENLİK, ÖĞÜRME VS BOĞULMA, GÜVENLİ KESİM
        // -------------------------------------------------------------
        if (selectedTab == "safety") {
            // ÖĞÜRME (GAGGING) VS BOĞULMA (CHOKING) HAYATİ KART
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ReactionAllergySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = ReactionAllergy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Öğürme ve Boğulma Arasındaki Hayati Fark",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Öğürme doğal bir savunma refleksidir; boğulma ise sessizdir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Comparison Rows
                        PediatricGuideData.gaggingVsChokingList.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = item.symptom,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Gagging side
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = SageSoft
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "✅ Öğürme",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageDark
                                                )
                                                Text(
                                                    text = item.gaggingFeature,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextPrimary,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        // Choking side
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = ReactionAllergySoft
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "🚨 Boğulma",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ReactionAllergy
                                                )
                                                Text(
                                                    text = item.chokingFeature,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = AllergyText,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // GÜVENLİ KESİM & TEHLİKELİ BESİNLER
            item {
                Text(
                    text = "BOĞULMA RİSKİ OLAN GIDALAR VE GÜVENLİ HAZIRLAMA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }

            items(PediatricGuideData.safetyTips) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "⚠️ ${tip.foodName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tehlike: ${tip.hazard}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReactionAllergy,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageDark,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tip.safePreparation,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SageDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 4: SIK YAPILAN 6 HATA & DEMİR FORMÜLÜ
        // -------------------------------------------------------------
        if (selectedTab == "mistakes") {
            item {
                Text(
                    text = "EK GIDADAKİ EN YAYGIN 6 HATA VE DOĞRUSU",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
            }

            items(PediatricGuideData.commonMistakesList) { mistake ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = mistake.icon, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = mistake.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Wrong
                        Surface(
                            color = ReactionAllergySoft,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "❌ ${mistake.wrongAction}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AllergyText,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Right
                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "✅ Doğrusu: ${mistake.correctAction}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = SageDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Demiri Destekleme / C Vitamini Formülü
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = OchreSoft),
                    border = BorderStroke(1.dp, OchreWarm.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "💡 Altın Kural: Bitkisel Demir + C Vitamini",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OchreDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Mercimek, nohut ve yulaf gibi bitkisel demir kaynaklarının vücut tarafından emilimini artırmak için yanına mandalina, portakal, kivi, çilek veya kırmızı biber gibi C vitamini zengini besinler ekleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OchreText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // TAB 5: EK GIDAYA HAZIR MIYIZ? (İNTERAKTİF KONTROL TESTİ)
        // -------------------------------------------------------------
        if (selectedTab == "readiness") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CoralSoft),
                    border = BorderStroke(1.dp, CoralBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Bebeğiniz Ek Gıdaya Hazır mı?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Yaklaşık 6. ayda bebeğinizin gelişimsel belirtilerini işaretleyerek hazır olup olmadığını değerlendirin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Checklist Items
            items(PediatricGuideData.readinessChecklist.indices.toList()) { index ->
                val item = PediatricGuideData.readinessChecklist[index]
                val isChecked = readinessChecks[index] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { readinessChecks[index] = !isChecked },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) SageSoft.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isChecked) SageGreen else CardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { readinessChecks[index] = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = SageGreen,
                                uncheckedColor = TextTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Myth Busters Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = OchreSoft),
                    border = BorderStroke(1.dp, OchreWarm.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ Sadece Bu Belirtiler Yeterli Değil! (Doğru Bilinen Yanlışlar)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OchreDark
                        )
                        PediatricGuideData.readinessMythBusters.forEach { myth ->
                            Text(
                                text = myth,
                                style = MaterialTheme.typography.bodySmall,
                                color = OchreText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodIntroCard(food: FoodStartRecommendation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_intro_${food.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Emoji, Name, Category and Month Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CoralSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = food.emoji,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = food.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Month Badge
                val (monthBg, monthColor) = when (food.startMonth) {
                    6 -> SageSoft to SageDark
                    7 -> OchreSoft to OchreDark
                    8 -> CoralSoft to CoralDark
                    9, 10 -> SlateBlueSoft to SlateBlue
                    11 -> SoftPurpleSoft to SoftPurple
                    else -> ReactionAllergySoft to ReactionAllergy
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = monthBg,
                    border = BorderStroke(1.dp, monthColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = monthColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = food.startMonthLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = monthColor
                        )
                    }
                }
            }

            // Kıvam & İlk Sunum
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🥣 İlk Sunum & Kıvam:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = food.texture,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.servingTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            // Neden Bu Ayda Başlanır?
            Column {
                Text(
                    text = "💡 Neden Bu Ayda ve Faydası:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CoralDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = food.whyIntroduce,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }

            // Warning Badges: Allergen & Choking Hazard
            if (food.isAllergen || food.isChokingHazard) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (food.isAllergen) {
                        Surface(
                            color = ReactionAllergySoft,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ReactionAllergy.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ReactionAllergy,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Alerjen Besin (3 Gün Kuralı)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ReactionAllergy,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (food.isChokingHazard) {
                        Surface(
                            color = SoftPurpleSoft,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, SoftPurple.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SoftPurple,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Boğulma Riski (İyice Pişirilmeli / Ezilmeli)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftPurple,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Special Notes
            if (food.specialNotes.isNotBlank()) {
                Surface(
                    color = OchreSoft.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "📌 ${food.specialNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OchreText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ForbiddenFoodCard(item: ForbiddenFoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, ReactionAllergySoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(ReactionAllergySoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = ReactionAllergy,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.severityLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp
                    )
                }
            }

            // Neden Yasak
            Column {
                Text(
                    text = "🧬 Neden 1 Yaşa Kadar Yasak?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ReactionAllergy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
            }

            // Olası Risk
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReactionAllergySoft.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "🚨 Risk: ${item.risk}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ReactionAllergy,
                        lineHeight = 17.sp
                    )
                }
            }

            // Ne Zaman Başlanabilir
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SageSoft.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "✅ Güvenli Başlangıç: ${item.whenAllowed}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = SageDark,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

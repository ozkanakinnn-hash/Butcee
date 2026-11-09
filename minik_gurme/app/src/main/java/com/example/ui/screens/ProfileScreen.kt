package com.example.ui.screens

import com.example.ui.theme.*
import androidx.compose.foundation.BorderStroke
import com.example.i18n.t
import com.example.i18n.AppLanguage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AllergenTest
import com.example.data.model.BabyProfile
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
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
import com.example.ui.theme.ReactionLoved
import com.example.ui.theme.ReactionLovedSoft
import com.example.ui.theme.ReactionNeutral
import com.example.ui.theme.ReactionNeutralSoft
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

private fun getFoodEmoji(foodName: String): String {
    val lower = foodName.lowercase()
    return when {
        "yumurta" in lower -> "🥚"
        "yoğurt" in lower || "süt" in lower || "labne" in lower || "peynir" in lower || "lor" in lower -> "🥛"
        "avokado" in lower -> "🥑"
        "fıstık" in lower || "fındık" in lower || "ceviz" in lower || "badem" in lower -> "🥜"
        "balık" in lower || "somon" in lower || "levrek" in lower || "mezgit" in lower -> "🐟"
        "tahin" in lower || "susam" in lower -> "🌱"
        "havuç" in lower -> "🥕"
        "kabak" in lower || "brokoli" in lower -> "🥦"
        "elma" in lower -> "🍎"
        "muz" in lower -> "🍌"
        "armut" in lower -> "🍐"
        "patates" in lower -> "🥔"
        "et" in lower || "kıyma" in lower || "köfte" in lower -> "🥩"
        "tavuk" in lower -> "🍗"
        "mercimek" in lower || "nohut" in lower || "fasulye" in lower -> "🫘"
        "yulaf" in lower || "irmik" in lower || "makarna" in lower || "ekmek" in lower || "buğday" in lower -> "🌾"
        else -> "🥣"
    }
}

@Composable
fun ProfileScreen(
    profile: BabyProfile,
    allMeals: List<Meal>,
    allAllergens: List<AllergenTest>,
    growthRecords: List<GrowthRecord> = emptyList(),
    onOpenEditProfile: () -> Unit,
    onOpenGrowthTracking: () -> Unit = {},
    onResetStatistics: () -> Unit = {}
) {
    var showResetDialog by remember { mutableStateOf(false) }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = ReactionAllergy) },
            title = { Text("İstatistikler sıfırlansın mı?") },
            text = {
                Text("Tüm öğün kayıtları ve alerjen testleri kalıcı olarak silinecek. Bebek profili ve boy/kilo ölçümleri korunur. Bu işlem geri alınamaz.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStatistics()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReactionAllergy, contentColor = Color.White),
                    modifier = Modifier.testTag("reset_confirm")
                ) { Text("Evet, sıfırla") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Vazgeç") }
            }
        )
    }
    val latestGrowth = remember(growthRecords) {
        growthRecords.maxByOrNull { it.ageMonths }
    }
    val totalMeals = allMeals.size
    val safeFoods = allAllergens.count { it.status == "Güvenli" }
    val activeTests = allAllergens.count { it.status == "Devam Ediyor" }
    val reactionsCount = allMeals.count { it.reaction == "Alerji Sinyali" } + allAllergens.count { it.status == "Reaksiyon" }

    val lovedCount = allMeals.count { it.reaction == "Sevdi" }
    val neutralCount = allMeals.count { it.reaction == "Nötr" }
    val rejectedCount = allMeals.count { it.reaction == "İstemedi" }

    val safeFoodList = remember(allAllergens) {
        allAllergens.filter { it.status == "Güvenli" }
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

        // Title
        item {
            Text(
                text = "Bebek Profili & İstatistikler",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }

        // Dil seçimi / Language
        item {
            LanguageCard()
        }

        // Baby Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("baby_profile_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Large Avatar Initial
                        Image(
                            painter = painterResource(id = R.drawable.img_baby_avatar),
                            contentDescription = profile.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${profile.ageMonths}. Ayında Minik Gurme 👶",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CoralDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (profile.startDate.isNotBlank()) {
                                Text(
                                    text = "Başlangıç: ${profile.startDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onOpenEditProfile,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("edit_profile_btn"),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Profili Düzenle",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Gelişim Takibi & Büyüme Kartı
        item {
            Card(
                onClick = onOpenGrowthTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_growth_tracking_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CoralSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📏", fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gelişim & Büyüme Takibi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SageSoft
                            ) {
                                Text(
                                    text = "WHO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (latestGrowth != null) {
                                "${latestGrowth.ageMonths}. Ay: ${latestGrowth.weightKg} kg • ${latestGrowth.heightCm} cm • Boy/Kilo Eğrisi"
                            } else {
                                "Boy ve kilo ölçümlerini kaydedin, zaman grafiğini izleyin."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = t("Gelişim Takibi Aç"),
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BESLENME İSTATİSTİKLERİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_stats_button")
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = ReactionAllergy)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sıfırla", color = ReactionAllergy, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Meals
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = CoralSoft,
                        border = BorderStroke(1.dp, CoralBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$totalMeals",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = CoralDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toplam Öğün",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralText
                            )
                        }
                    }

                    // Safe Foods
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = SageSoft,
                        border = BorderStroke(1.dp, SageBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$safeFoods",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = SageDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Güvenli Gıda",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SageText
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Active Tests
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = OchreSoft,
                        border = BorderStroke(1.dp, OchreBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$activeTests",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = OchreDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Devam Eden Test",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = OchreText
                            )
                        }
                    }

                    // Reactions
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = ReactionAllergySoft,
                        border = BorderStroke(1.dp, AllergyBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$reactionsCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = ReactionAllergy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Alerji Sinyali",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = AllergyText
                            )
                        }
                    }
                }
            }
        }

        // Güvenli Gıdalarım Kartı (Sleek Safe Foods List Card)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safe_foods_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row: Icon + Title + Count badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SageSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Güvenli Gıdalarım",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "3 gün kuralını başarıyla tamamlayan besinler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${safeFoodList.size} Onaylı",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (safeFoodList.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🛡️", fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Henüz onaylanmış güvenli gıda yok",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "3 Gün Kuralı sekmesinden yeni besinleri deneyip 3. günü tamamladığınızda güvenli gıdalarınız burada listelenir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            safeFoodList.forEach { safeFood ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SageSoft.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, SageBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = getFoodEmoji(safeFood.food),
                                                fontSize = 22.sp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = safeFood.food,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                if (safeFood.notes.isNotBlank()) {
                                                    Text(
                                                        text = safeFood.notes,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextSecondary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                } else if (safeFood.startDate.isNotBlank()) {
                                                    Text(
                                                        text = "Test Tarihi: ${safeFood.startDate}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextTertiary
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            color = SageSoft,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, SageBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = t("Güvenli"),
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Güvenli",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SageText
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
        }

        // Reaction Breakdown Card
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tadım & Sevme Dağılımı",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "😋 Severek Yenenler", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "$lovedCount öğün", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SageDark)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "😐 Kararsız Kalınanlar", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "$neutralCount öğün", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SlateBlue)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "😣 İstenmeyenler / Reddedilenler", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "$rejectedCount öğün", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OchreDark)
                    }
                }
            }
        }

        // Milestones
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = OchreDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ek Gıda Kilometre Taşları",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    val milestones = listOf(
                        Pair("İlk kaşık tadımı (6. Ay)", totalMeals > 0),
                        Pair("3 Gün Kuralı ile ilk besin testi", safeFoods > 0 || activeTests > 0),
                        Pair("Çatalla ezilmiş dokulara geçiş (7. Ay)", profile.ageMonths >= 7),
                        Pair("Yumuşak parmak gıda denemesi (8. Ay)", profile.ageMonths >= 8),
                        Pair("Aile sofrasına tam uyum (12. Ay+)", profile.ageMonths >= 12)
                    )

                    milestones.forEach { (title, completed) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (completed) SageDark else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (completed) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (completed) TextPrimary else TextTertiary
                            )
                        }
                    }
                }
            }
        }

        // Medical Disclaimer
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Minik Gurme, Dünya Sağlık Örgütü (WHO), CDC ve Türk Pediatri Kurumu rehberlerine dayalı bir takip asistanıdır. Tıbbi teşhis yerine geçmez. Alerji şüphesinde hekiminize danışınız.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

/** Uygulama dili seçici (Türkçe / English). Seçim kaydedilir ve tüm ekranlar anında yeniden çizilir. */
@Composable
private fun LanguageCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("language_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🌐 Dil / Language",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("tr" to "Türkçe", "en" to "English").forEach { (code, label) ->
                    val selected = AppLanguage.current == code
                    val tag = Modifier.weight(1f).testTag("lang_$code")
                    if (selected) {
                        Button(onClick = {}, modifier = tag) {
                            androidx.compose.material3.Text(label, maxLines = 1)
                        }
                    } else {
                        OutlinedButton(onClick = { AppLanguage.set(context, code) }, modifier = tag) {
                            androidx.compose.material3.Text(label, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

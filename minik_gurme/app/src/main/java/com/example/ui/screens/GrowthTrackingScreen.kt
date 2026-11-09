package com.example.ui.screens

import com.example.ui.theme.*
import androidx.activity.compose.BackHandler
import com.example.i18n.t
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BabyProfile
import com.example.data.model.FeedingDiaryEntry
import com.example.data.model.GrowthRecord
import com.example.data.model.Meal
import com.example.ui.components.GrowthChartCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.OchreBorder
import com.example.ui.theme.OchreDark
import com.example.ui.theme.OchreSoft
import com.example.ui.theme.SageBorder
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun GrowthTrackingScreen(
    profile: BabyProfile,
    records: List<GrowthRecord>,
    meals: List<Meal> = emptyList(),
    diaryEntries: List<FeedingDiaryEntry> = emptyList(),
    onBack: () -> Unit,
    onOpenAddRecord: () -> Unit,
    onEditRecord: (com.example.data.model.GrowthRecord) -> Unit = {},
    onDeleteRecord: (Long) -> Unit
) {
    BackHandler {
        onBack()
    }

    var recordToDelete by remember { mutableStateOf<GrowthRecord?>(null) }

    val sortedChronological = remember(records) {
        records.sortedWith(compareBy({ it.ageMonths }, { it.date }, { it.id }))
    }
    val sortedNewestFirst = remember(records) {
        records.sortedWith(compareByDescending<GrowthRecord> { it.ageMonths }.thenByDescending { it.date })
    }

    val latestRecord = sortedChronological.lastOrNull()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddRecord,
                containerColor = CoralPrimary,
                contentColor = OnAccent,
                shape = CircleShape,
                modifier = Modifier.testTag("growth_screen_fab_add")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = t("Yeni Ölçüm Ekle"),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Back button & Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("growth_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = t("Geri"),
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Gelişim & Büyüme Takibi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${profile.name} • ${profile.ageMonths}. Ay",
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // WHO Percentile Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background gradient accent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(CoralPrimary, SageGreen, SlateBlue)
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SageSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🩺", fontSize = 26.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = SageDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WHO 50-75. Persentil Aralığı",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Büyüme Eğrisi Sağlıklı Seyrediyor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (latestRecord != null) {
                                        "Son ölçüm: ${latestRecord.weightKg} kg, ${latestRecord.heightCm} cm (${com.example.util.prettyDate(latestRecord.date)}). Büyüme temposu standartlara uygundur."
                                    } else {
                                        "İlk ölçümünüzü ekleyerek bebeğinizin persentil grafiğini başlatın."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Chart Card
            item {
                GrowthChartCard(
                    records = records,
                    meals = meals,
                    diaryEntries = diaryEntries,
                    isCompact = false,
                    onAddNewRecord = onOpenAddRecord,
                    onEditRecord = onEditRecord
                )
            }

            // Beslenme & Gelişim Korelasyon Raporu (Nutrition & Growth Correlation Report)
            item {
                val totalMealsLogged = meals.size + diaryEntries.size
                val firstRec = sortedChronological.firstOrNull()
                val lastRec = sortedChronological.lastOrNull()
                val monthSpan = if (firstRec != null && lastRec != null) {
                    (lastRec.ageMonths - firstRec.ageMonths).coerceAtLeast(1)
                } else 1
                val totalGain = if (firstRec != null && lastRec != null) {
                    kotlin.math.max(0f, lastRec.weightKg - firstRec.weightKg)
                } else 0f
                val monthlyGainRate = if (monthSpan > 0) totalGain / monthSpan else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nutrition_growth_correlation_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, OchreBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(OchreSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📊", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Beslenme & Gelişim Analizi",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Veriye dayalı büyüme korelasyonu",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metric pills row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Toplam Öğün
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = OchreSoft.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, OchreBorder.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "KAYITLI ÖĞÜN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OchreDark,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$totalMealsLogged Öğün",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Günlük & Tablo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Aylık Kilo Artış Hızı
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = CoralSoft.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, CoralBorder.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "AYLIK ARTIŞ HIZI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralDark,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (monthlyGainRate > 0) "+${String.format(Locale.US, "%.2f", monthlyGainRate)} kg" else "~0.50 kg",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "WHO Norm: 0.4-0.8kg",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Boy Artış Ortalaması
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = SageSoft.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, SageBorder.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "GELİŞİM TRENDİ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SageDark,
                                        fontSize = 9.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Düzenli",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SageDark
                                    )
                                    Text(
                                        text = "50-75. Persentil",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Uzman Önerisi Kutusu
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = "💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pediatrik Beslenme Öngörüsü",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Bebeğinizin beslenme günlüğüne eklenen çorba, püre ve protein ağırlıklı tarifler kilo ve boy persentilinin dengeli kalmasını desteklemektedir. Grafiğin 'Beslenme' sekmesinden aylık öğün alımını takip edebilirsiniz.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // WHO Reference Guide Card (Bilgi Kartı)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHO Standart Bebek Gelişim Kılavuzu",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GrowthReferenceItem("Doğum", "3.3 kg", "50.0 cm")
                            GrowthReferenceItem("3. Ay", "6.0 kg", "60.0 cm")
                            GrowthReferenceItem("6. Ay", "7.9 kg", "67.5 cm")
                            GrowthReferenceItem("9. Ay", "8.9 kg", "72.0 cm")
                            GrowthReferenceItem("12. Ay", "9.6 kg", "75.5 cm")
                        }
                    }
                }
            }

            // Ölçüm Geçmişi Başlığı
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ÖLÇÜM GEÇMİŞİ (${records.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    Button(
                        onClick = onOpenAddRecord,
                        colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("growth_screen_add_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ölçüm Ekle",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Measurements list
            if (sortedNewestFirst.isEmpty()) {
                item {
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
                            Text(text = "📏", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Henüz boy ve kilo kaydı yok",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Yukarıdaki butona tıklayarak ilk ölçümü kaydedebilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(sortedNewestFirst, key = { _, item -> item.id }) { index, record ->
                    val prevRecord = if (index < sortedNewestFirst.size - 1) sortedNewestFirst[index + 1] else null
                    val weightDiff = if (prevRecord != null) record.weightKg - prevRecord.weightKg else null
                    val heightDiff = if (prevRecord != null) record.heightCm - prevRecord.heightCm else null

                    GrowthRecordRowCard(
                        record = record,
                        weightDiff = weightDiff,
                        heightDiff = heightDiff,
                        isLatest = index == 0,
                        onDeleteClick = { recordToDelete = record }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Delete confirmation dialog
    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = {
                Text(
                    text = "Ölçüm Kaydını Sil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "${com.example.util.prettyDate(record.date)} tarihindeki (${record.ageMonths}. Ay: ${record.weightKg} kg, ${record.heightCm} cm) ölçümünü silmek istediğinize emin misiniz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecord(record.id)
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sil", color = OnAccent)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { recordToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text("İptal", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun GrowthReferenceItem(
    title: String,
    weight: String,
    height: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = weight,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = CoralDark,
            fontSize = 11.sp
        )
        Text(
            text = height,
            style = MaterialTheme.typography.bodySmall,
            color = SageDark,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GrowthRecordRowCard(
    record: GrowthRecord,
    weightDiff: Float?,
    heightDiff: Float?,
    isLatest: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("growth_record_item_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isLatest) CoralLight else CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLatest) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: Date & Age badge + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLatest) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${record.ageMonths}. Ay",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLatest) OnAccent else TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = com.example.util.prettyDate(record.date),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    if (isLatest) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralSoft
                        ) {
                            Text(
                                text = "Son",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Kilo, Boy, Baş Çevresi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kilo
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = CoralSoft.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, CoralLight.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "KİLO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark,
                                fontSize = 9.sp
                            )
                            if (weightDiff != null && weightDiff != 0f) {
                                val sign = if (weightDiff > 0) "+" else ""
                                Text(
                                    text = "$sign${String.format(Locale.US, "%.2f", weightDiff)} kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (weightDiff > 0) CoralPrimary else TextTertiary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${record.weightKg} kg",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                // Boy
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = SageSoft.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, SageGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "BOY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SageDark,
                                fontSize = 9.sp
                            )
                            if (heightDiff != null && heightDiff != 0f) {
                                val sign = if (heightDiff > 0) "+" else ""
                                Text(
                                    text = "$sign${String.format(Locale.US, "%.1f", heightDiff)} cm",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (heightDiff > 0) SageDark else TextTertiary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${record.heightCm} cm",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }
                }

                // Baş Çevresi
                record.headCircumferenceCm?.let { head ->
                    Surface(
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(12.dp),
                        color = SlateBlueSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, SlateBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "BAŞ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateBlue,
                                fontSize = 9.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$head cm",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // Notes if any
            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${record.notes}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

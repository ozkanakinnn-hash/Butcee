package akinai.minikgurme.ui.components

import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import akinai.minikgurme.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.data.model.FeedingDiaryEntry
import akinai.minikgurme.data.model.GrowthRecord
import akinai.minikgurme.data.model.Meal
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.OchreBorder
import akinai.minikgurme.ui.theme.OchreDark
import akinai.minikgurme.ui.theme.OchreSoft
import akinai.minikgurme.ui.theme.OchreWarm
import akinai.minikgurme.ui.theme.SageBorder
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class GrowthMetricType {
    WEIGHT,
    HEIGHT,
    NUTRITION_CORRELATION
}

/**
 * WHO Çocuk Büyüme Standartları — 50. persentil (medyan), 0–24 ay.
 * Kaynak: WHO Child Growth Standards (2006), "weight-for-age" ve "length-for-age" tabloları.
 * Cinsiyet seçilmemişse kız ve erkek medyanlarının ortalaması kullanılır.
 */
object WhoStandardsData {
    /** "girl" | "boy" | "" (bilinmiyor). Profil değişince MainScreen günceller. */
    var sex by androidx.compose.runtime.mutableStateOf("")

    private val months = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 18, 21, 24)
    private val weightBoy = floatArrayOf(3.3f, 4.5f, 5.6f, 6.4f, 7.0f, 7.5f, 7.9f, 8.3f, 8.6f, 8.9f, 9.2f, 9.4f, 9.6f, 10.3f, 10.9f, 11.5f, 12.2f)
    private val weightGirl = floatArrayOf(3.2f, 4.2f, 5.1f, 5.8f, 6.4f, 6.9f, 7.3f, 7.6f, 7.9f, 8.2f, 8.5f, 8.7f, 8.9f, 9.6f, 10.2f, 10.9f, 11.5f)
    private val heightBoy = floatArrayOf(49.9f, 54.7f, 58.4f, 61.4f, 63.9f, 65.9f, 67.6f, 69.2f, 70.6f, 72.0f, 73.3f, 74.5f, 75.7f, 79.1f, 82.3f, 85.1f, 87.8f)
    private val heightGirl = floatArrayOf(49.1f, 53.7f, 57.1f, 59.8f, 62.1f, 64.0f, 65.7f, 67.3f, 68.7f, 70.1f, 71.5f, 72.8f, 74.0f, 77.5f, 80.7f, 83.7f, 86.4f)

    private fun interp(table: FloatArray, ageMonths: Int): Float {
        val m = ageMonths.coerceIn(0, 24)
        val i = months.indexOfLast { it <= m }
        if (months[i] == m || i == months.lastIndex) return table[i]
        val f = (m - months[i]).toFloat() / (months[i + 1] - months[i])
        return table[i] + (table[i + 1] - table[i]) * f
    }

    private fun pick(boy: FloatArray, girl: FloatArray, ageMonths: Int, sex: String): Float = when (sex) {
        "boy" -> interp(boy, ageMonths)
        "girl" -> interp(girl, ageMonths)
        else -> (interp(boy, ageMonths) + interp(girl, ageMonths)) / 2f
    }

    fun getWhoWeight(ageMonths: Int, sex: String = this.sex): Float = pick(weightBoy, weightGirl, ageMonths, sex)
    fun getWhoHeight(ageMonths: Int, sex: String = this.sex): Float = pick(heightBoy, heightGirl, ageMonths, sex)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrowthChartCard(
    records: List<GrowthRecord>,
    meals: List<Meal> = emptyList(),
    diaryEntries: List<FeedingDiaryEntry> = emptyList(),
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onOpenFullTracking: (() -> Unit)? = null,
    onAddNewRecord: (() -> Unit)? = null,
    onEditRecord: ((GrowthRecord) -> Unit)? = null
) {
    var selectedMetric by remember { mutableStateOf(GrowthMetricType.WEIGHT) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }
    var showWhoReferenceLine by remember { mutableStateOf(true) }

    val sortedRecords = remember(records) {
        records.sortedWith(compareBy({ it.ageMonths }, { it.date }, { it.id }))
    }

    val latestRecord = sortedRecords.lastOrNull()
    val firstRecord = sortedRecords.firstOrNull()

    // Toplam artış hesaplamaları
    val totalWeightGain = remember(firstRecord, latestRecord) {
        if (firstRecord != null && latestRecord != null) {
            max(0f, latestRecord.weightKg - firstRecord.weightKg)
        } else 0f
    }

    val totalHeightGain = remember(firstRecord, latestRecord) {
        if (firstRecord != null && latestRecord != null) {
            max(0f, latestRecord.heightCm - firstRecord.heightCm)
        } else 0f
    }

    // Seçili noktanın detayları
    val selectedRecord = remember(selectedPointIndex, sortedRecords) {
        if (selectedPointIndex in sortedRecords.indices) sortedRecords[selectedPointIndex] else latestRecord
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("growth_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Başlık ve Eylem
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = onOpenFullTracking != null) { onOpenFullTracking?.invoke() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                when (selectedMetric) {
                                    GrowthMetricType.WEIGHT -> CoralSoft
                                    GrowthMetricType.HEIGHT -> SageSoft
                                    GrowthMetricType.NUTRITION_CORRELATION -> OchreSoft
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedMetric) {
                                GrowthMetricType.WEIGHT -> Icons.Default.MonitorWeight
                                GrowthMetricType.HEIGHT -> Icons.Default.Straighten
                                GrowthMetricType.NUTRITION_CORRELATION -> Icons.Default.Restaurant
                            },
                            contentDescription = null,
                            tint = when (selectedMetric) {
                                GrowthMetricType.WEIGHT -> CoralPrimary
                                GrowthMetricType.HEIGHT -> SageDark
                                GrowthMetricType.NUTRITION_CORRELATION -> OchreDark
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = "Büyüme & Beslenme Grafiği",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            if (onOpenFullTracking != null) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = t("Detay"),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = if (latestRecord != null) "${latestRecord.ageMonths}. Ay • Son Ölçüm: ${akinai.minikgurme.util.prettyDate(latestRecord.date)}" else "Henüz ölçüm girilmedi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                if (onAddNewRecord != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onAddNewRecord,
                        shape = CircleShape,
                        color = CoralSoft,
                        border = BorderStroke(1.dp, CoralBorder),
                        modifier = Modifier.testTag("chart_add_record_btn")
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = t("Yeni Ölçüm Ekle"),
                                tint = CoralPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrik kutuları: üçü de aynı boyutta; kilo ve boy kalem simgesiyle düzenlenebilir
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    label = "KİLO (KG)",
                    value = if (latestRecord != null) "${String.format(Locale.US, "%.2f", latestRecord.weightKg)} kg" else "-",
                    sub = if (totalWeightGain > 0) "+${String.format(Locale.US, "%.1f", totalWeightGain)} kg" else null,
                    selected = selectedMetric == GrowthMetricType.WEIGHT,
                    soft = CoralSoft, border = CoralBorder, accent = CoralDark,
                    onClick = { selectedMetric = GrowthMetricType.WEIGHT },
                    onEdit = if (latestRecord != null && onEditRecord != null) ({ onEditRecord(latestRecord) }) else null,
                    tag = "metric_weight",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                MetricBox(
                    label = "BOY (CM)",
                    value = if (latestRecord != null) "${String.format(Locale.US, "%.1f", latestRecord.heightCm)} cm" else "-",
                    sub = if (totalHeightGain > 0) "+${String.format(Locale.US, "%.1f", totalHeightGain)} cm" else null,
                    selected = selectedMetric == GrowthMetricType.HEIGHT,
                    soft = SageSoft, border = SageBorder, accent = SageDark,
                    onClick = { selectedMetric = GrowthMetricType.HEIGHT },
                    onEdit = if (latestRecord != null && onEditRecord != null) ({ onEditRecord(latestRecord) }) else null,
                    tag = "metric_height",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gösterge Açıklamaları (Legend)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                run {
                    // Bebeğinizin eğrisi
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (selectedMetric) {
                                        GrowthMetricType.WEIGHT -> CoralPrimary
                                        GrowthMetricType.HEIGHT -> SageDark
                                        GrowthMetricType.NUTRITION_CORRELATION -> OchreDark
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bebeğinizin Gelişimi",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    // WHO Standart eğrisi
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showWhoReferenceLine = !showWhoReferenceLine }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(SlateBlue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "WHO 50. Persentil",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SlateBlue,
                            fontSize = 11.sp
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Veri Görselleştirmesi
            val chartHeight = if (isCompact) 150.dp else 200.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                if (sortedRecords.size < 2) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Zamana bağlı grafik için en az 2 ölçüm gereklidir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                } else {
                    InteractiveGrowthCanvas(
                        records = sortedRecords,
                        meals = meals,
                        diaryEntries = diaryEntries,
                        metricType = selectedMetric,
                        showWhoReference = showWhoReferenceLine,
                        selectedPointIndex = selectedPointIndex,
                        onSelectPoint = { index -> selectedPointIndex = index },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Seçilen Veri Noktası Denetçisi (Interactive Point Inspector Popup)
            selectedRecord?.let { record ->
                val whoWeight = WhoStandardsData.getWhoWeight(record.ageMonths)
                val whoHeight = WhoStandardsData.getWhoHeight(record.ageMonths)
                val mealsInMonth = remember(record, meals, diaryEntries) {
                    val count = meals.count { it.date.startsWith(record.date.take(7)) } +
                            diaryEntries.count { it.date.startsWith(record.date.take(7)) }
                    count
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${record.ageMonths}. Ay • ${akinai.minikgurme.util.prettyDate(record.date)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Kilo: ${record.weightKg} kg (WHO: ${String.format(Locale.US, "%.1f", whoWeight)}kg) • Boy: ${record.heightCm} cm (WHO: ${String.format(Locale.US, "%.1f", whoHeight)}cm)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (onEditRecord != null) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = akinai.minikgurme.i18n.t("Ölçümü düzenle"),
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable { onEditRecord(record) }
                                    .padding(6.dp)
                                    .testTag("edit_record_inline")
                            )
                        }
                        // Beslenme özeti rozeti
                        Surface(
                            color = OchreSoft,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, OchreBorder)
                        ) {
                            Text(
                                text = "$mealsInMonth Öğün",
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OchreDark,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Alt bilgi; "Tümünü Gör" ayrı satırda, altta
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = SageDark,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Büyüme Eğrisi & Beslenme",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (onOpenFullTracking != null && isCompact) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    onClick = onOpenFullTracking,
                    shape = RoundedCornerShape(12.dp),
                    color = CoralSoft,
                    border = BorderStroke(1.dp, CoralBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_growth_see_all_btn")
                ) {
                    Text(
                        text = "Tümünü Gör →",
                        style = MaterialTheme.typography.labelLarge,
                        color = CoralDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveGrowthCanvas(
    records: List<GrowthRecord>,
    meals: List<Meal>,
    diaryEntries: List<FeedingDiaryEntry>,
    metricType: GrowthMetricType,
    showWhoReference: Boolean,
    selectedPointIndex: Int,
    onSelectPoint: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = when (metricType) {
        GrowthMetricType.WEIGHT -> CoralPrimary
        GrowthMetricType.HEIGHT -> SageDark
        GrowthMetricType.NUTRITION_CORRELATION -> CoralPrimary
    }

    val gradientColors = when (metricType) {
        GrowthMetricType.WEIGHT -> listOf(CoralPrimary.copy(alpha = 0.35f), CoralLight.copy(alpha = 0.05f))
        GrowthMetricType.HEIGHT -> listOf(SageGreen.copy(alpha = 0.35f), SageSoft.copy(alpha = 0.05f))
        GrowthMetricType.NUTRITION_CORRELATION -> listOf(OchreWarm.copy(alpha = 0.35f), OchreSoft.copy(alpha = 0.05f))
    }

    val values = remember(records, metricType) {
        records.map {
            when (metricType) {
                GrowthMetricType.WEIGHT, GrowthMetricType.NUTRITION_CORRELATION -> it.weightKg
                GrowthMetricType.HEIGHT -> it.heightCm
            }
        }
    }

    val whoValues = remember(records, metricType) {
        records.map {
            when (metricType) {
                GrowthMetricType.WEIGHT, GrowthMetricType.NUTRITION_CORRELATION -> WhoStandardsData.getWhoWeight(it.ageMonths)
                GrowthMetricType.HEIGHT -> WhoStandardsData.getWhoHeight(it.ageMonths)
            }
        }
    }

    // Min ve Max sınırları
    val allValues = if (showWhoReference) values + whoValues else values
    val rawMin = allValues.minOrNull() ?: 0f
    val rawMax = allValues.maxOrNull() ?: 10f
    val paddingSpan = if (rawMax == rawMin) 1f else (rawMax - rawMin) * 0.22f
    val minY = max(0f, rawMin - paddingSpan)
    val maxY = rawMax + paddingSpan

    // Öğün sayıları (Beslenme Korelasyonu için)
    val monthlyMeals = remember(records, meals, diaryEntries) {
        records.map { r ->
            val count = meals.count { it.date.startsWith(r.date.take(7)) } +
                    diaryEntries.count { it.date.startsWith(r.date.take(7)) }
            max(count, if (r.ageMonths >= 6) 18 else 0)
        }
    }
    val maxMeals = (monthlyMeals.maxOrNull() ?: 1).coerceAtLeast(1)

    Canvas(
        modifier = modifier.pointerInput(records) {
            detectTapGestures { offset ->
                val width = size.width
                val leftPad = 28.dp.toPx()
                val rightPad = 28.dp.toPx()
                val usableWidth = width - leftPad - rightPad
                val n = records.size
                if (n > 1) {
                    var closestIndex = 0
                    var minDistance = Float.MAX_VALUE
                    for (i in 0 until n) {
                        val pointX = leftPad + (i.toFloat() / (n - 1).coerceAtLeast(1)) * usableWidth
                        val distance = kotlin.math.abs(offset.x - pointX)
                        if (distance < minDistance) {
                            minDistance = distance
                            closestIndex = i
                        }
                    }
                    onSelectPoint(closestIndex)
                }
            }
        }
    ) {
        val width = size.width
        val height = size.height
        val leftPad = 26.dp.toPx()
        val rightPad = 26.dp.toPx()
        val topPad = 22.dp.toPx()
        val bottomPad = 24.dp.toPx()

        val usableWidth = width - leftPad - rightPad
        val usableHeight = height - topPad - bottomPad

        val n = records.size
        val babyPoints = mutableListOf<Offset>()
        val whoPoints = mutableListOf<Offset>()

        for (i in 0 until n) {
            val x = leftPad + (i.toFloat() / (n - 1).coerceAtLeast(1)) * usableWidth

            // Bebeğin noktası
            val v = values[i]
            val normY = if (maxY == minY) 0.5f else (v - minY) / (maxY - minY)
            val y = topPad + (1f - normY) * usableHeight
            babyPoints.add(Offset(x, y))

            // WHO standart noktası
            val wv = whoValues[i]
            val whoNormY = if (maxY == minY) 0.5f else (wv - minY) / (maxY - minY)
            val whoY = topPad + (1f - whoNormY) * usableHeight
            whoPoints.add(Offset(x, whoY))
        }

        // Yatay grid çizgileri (3 rehber çizgi)
        val gridLines = 3
        for (i in 0..gridLines) {
            val gy = topPad + (usableHeight / gridLines) * i
            drawLine(
                color = ChartGrid,
                start = Offset(leftPad, gy),
                end = Offset(width - rightPad, gy),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Beslenme Korelasyon Modu: Besin/Öğün alım hacmi çubukları
        if (metricType == GrowthMetricType.NUTRITION_CORRELATION) {
            val barWidth = 16.dp.toPx()
            for (i in 0 until n) {
                val pt = babyPoints[i]
                val mealFraction = (monthlyMeals[i].toFloat() / maxMeals).coerceIn(0.1f, 1f)
                val barHeight = usableHeight * 0.35f * mealFraction
                val barTop = height - bottomPad - barHeight

                drawRoundRect(
                    color = OchreWarm.copy(alpha = 0.28f),
                    topLeft = Offset(pt.x - barWidth / 2, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        // WHO Standart Referans Eğrisi (Kesikli çizgi)
        if (showWhoReference && whoPoints.size > 1) {
            val whoPath = Path()
            whoPath.moveTo(whoPoints[0].x, whoPoints[0].y)
            for (i in 0 until whoPoints.size - 1) {
                val p0 = whoPoints[i]
                val p1 = whoPoints[i + 1]
                val midX = (p0.x + p1.x) / 2
                whoPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }

            drawPath(
                path = whoPath,
                color = SlateBlue.copy(alpha = 0.75f),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )
        }

        // Bebeğin Büyüme Eğrisi ve Degradeli Dolgu
        if (babyPoints.isNotEmpty()) {
            val strokePath = Path()
            val fillPath = Path()

            strokePath.moveTo(babyPoints[0].x, babyPoints[0].y)
            fillPath.moveTo(babyPoints[0].x, height - bottomPad)
            fillPath.lineTo(babyPoints[0].x, babyPoints[0].y)

            for (i in 0 until babyPoints.size - 1) {
                val p0 = babyPoints[i]
                val p1 = babyPoints[i + 1]
                val midX = (p0.x + p1.x) / 2
                strokePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                fillPath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }

            fillPath.lineTo(babyPoints.last().x, height - bottomPad)
            fillPath.close()

            // Degradeli alan dolgusu
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = topPad,
                    endY = height - bottomPad
                )
            )

            // Eğri çizgisi
            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // Değer ve Ay Etiketleri için Boya Nesneleri
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 9.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val monthLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 8.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            // Noktaları Çiz
            for (i in babyPoints.indices) {
                val pt = babyPoints[i]
                val isSelected = i == selectedPointIndex
                val isLatest = i == babyPoints.size - 1
                val r = if (isSelected) 6.dp.toPx() else if (isLatest) 5.dp.toPx() else 4.dp.toPx()

                // Seçili nokta vurgulama halkası
                if (isSelected) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = r + 6.dp.toPx(),
                        center = pt
                    )
                }

                // Dış renkli halka
                drawCircle(
                    color = primaryColor,
                    radius = r,
                    center = pt
                )

                // İç beyaz merkez
                drawCircle(
                    color = OnAccent,
                    radius = r - 1.5.dp.toPx(),
                    center = pt
                )

                // Değer etiketi
                if (n <= 5 || isLatest || isSelected || i == 0) {
                    val unit = if (metricType == GrowthMetricType.HEIGHT) "cm" else "kg"
                    val vText = if (metricType == GrowthMetricType.HEIGHT) {
                        String.format(Locale.US, "%.0f", values[i])
                    } else {
                        String.format(Locale.US, "%.1f", values[i])
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "$vText$unit",
                        pt.x,
                        pt.y - 8.dp.toPx(),
                        textPaint
                    )
                }

                // X Ekseni Ay Etiketi
                val monthText = akinai.minikgurme.i18n.t("${records[i].ageMonths}.Ay")
                drawContext.canvas.nativeCanvas.drawText(
                    monthText,
                    pt.x,
                    height - 6.dp.toPx(),
                    monthLabelPaint
                )
            }
        }
    }
}

/** Kilo / Boy / Beslenme kutusu. Etiket ve değer tek satır; büyük yazı boyutunda da taşmaz. */
@Composable
private fun MetricBox(
    label: String,
    value: String,
    sub: String?,
    selected: Boolean,
    soft: androidx.compose.ui.graphics.Color,
    border: androidx.compose.ui.graphics.Color,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) soft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (selected) border else CardBorder),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) accent else TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (onEdit != null) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = akinai.minikgurme.i18n.t("Düzenle"),
                        tint = accent,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onEdit)
                            .padding(3.dp)
                            .testTag("${tag}_edit")
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = sub ?: " ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

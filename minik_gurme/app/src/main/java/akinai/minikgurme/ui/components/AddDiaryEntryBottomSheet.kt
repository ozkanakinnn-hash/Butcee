package akinai.minikgurme.ui.components

import akinai.minikgurme.util.DateKit
import akinai.minikgurme.ui.theme.*
import androidx.compose.foundation.BorderStroke
import akinai.minikgurme.i18n.t
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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import akinai.minikgurme.i18n.Text
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
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.OchreDark
import akinai.minikgurme.ui.theme.OchreSoft
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.ReactionAllergySoft
import akinai.minikgurme.ui.theme.ReactionLoved
import akinai.minikgurme.ui.theme.ReactionLovedSoft
import akinai.minikgurme.ui.theme.ReactionNeutral
import akinai.minikgurme.ui.theme.ReactionNeutralSoft
import akinai.minikgurme.ui.theme.ReactionRejected
import akinai.minikgurme.ui.theme.ReactionRejectedSoft
import akinai.minikgurme.ui.theme.SageDark
import akinai.minikgurme.ui.theme.SageGreen
import akinai.minikgurme.ui.theme.SageSoft
import akinai.minikgurme.ui.theme.SlateBlue
import akinai.minikgurme.ui.theme.SlateBlueSoft
import akinai.minikgurme.ui.theme.TextPrimary
import akinai.minikgurme.ui.theme.TextSecondary
import akinai.minikgurme.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddDiaryEntryBottomSheet(
    sheetState: SheetState,
    initialDate: String,
    onDismiss: () -> Unit,
    onSaveEntry: (
        date: String,
        time: String,
        mealType: String,
        foodItems: String,
        portionAmount: String,
        texture: String,
        appetite: String,
        reaction: String,
        reactionNotes: String,
        notes: String
    ) -> Unit
) {
    val todayStr = remember { DateKit.todayIso() }
    val yesterdayStr = remember { DateKit.isoPlusDays(DateKit.todayIso(), -1) }
    val currentTimeStr = remember { DateKit.nowHm() }

    var selectedDate by remember { mutableStateOf(initialDate.ifBlank { todayStr }) }
    var entryTime by remember { mutableStateOf(currentTimeStr) }
    var mealType by remember { mutableStateOf("Kahvaltı / Sabah") }
    var foodItems by remember { mutableStateOf("") }
    var portionAmount by remember { mutableStateOf("3 tatlı kaşığı") }
    var texture by remember { mutableStateOf("Çatalla Ezilmiş") }
    var appetite by remember { mutableStateOf("İştahla Bitirdi 😋") }
    var reaction by remember { mutableStateOf("Sorunsuz / Keyifli ✨") }
    var notes by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val quickFoodSuggestions = listOf(
        "🥑 Avokado", "🍳 Yumurta sarısı", "🥣 Yulaf lapası", "🥛 Doğal yoğurt",
        "🥕 Buharda havuç", "🥒 Kabak püresi", "🥩 Dana kıyma", "🍎 Elma püresi",
        "🍌 Muz dilimleri", "🍲 İlikli kemik suyu", "🥔 Tatlı patates", "🌿 Soğuk sıkım zeytinyağı"
    )

    val quickPortions = listOf(
        "1-2 tatlı kaşığı", "3 tatlı kaşığı", "Yarım küçük kase",
        "1 tam kase", "BLW (2 yumuşak çubuk)", "1 çay bardağı"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
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
                            .background(CoralSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Beslenme Günlüğüne Ekle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Bebeğin tükettiği besin ve tarih kaydı",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .testTag("close_diary_modal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = t("Kapat"),
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Tarih Seçimi
            Text(
                text = "KAYIT TARİHİ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDate == todayStr,
                    onClick = { selectedDate = todayStr },
                    label = { Text("Bugün (${todayStr.takeLast(5)})") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralSoft,
                        selectedLabelColor = CoralDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedDate == todayStr,
                        borderColor = CoralBorder
                    )
                )

                FilterChip(
                    selected = selectedDate == yesterdayStr,
                    onClick = { selectedDate = yesterdayStr },
                    label = { Text("Dün (${yesterdayStr.takeLast(5)})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OchreSoft,
                        selectedLabelColor = OchreDark
                    )
                )
            }

            // Tarih ve Saat Girdisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Tarih (YYYY-AA-GG)") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder
                    )
                )

                OutlinedTextField(
                    value = entryTime,
                    onValueChange = { entryTime = it },
                    label = { Text("Saat (SS:DD)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = CoralPrimary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralPrimary,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Öğün Tipi Seçimi
            Text(
                text = "ÖĞÜN ZAMANI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val mealTimes = listOf(
                "Kahvaltı / Sabah" to "☀️",
                "Öğle Yemeği" to "🌤️",
                "İkindi / Ara Öğün" to "🥛",
                "Akşam Yemeği" to "🌙",
                "Tadım Denemesi" to "🥄"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                mealTimes.forEach { (type, emoji) ->
                    val isSelected = mealType == type
                    Surface(
                        onClick = { mealType = type },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.5.dp, if (isSelected) CoralBorder else CardBorder),
                        modifier = Modifier.testTag("diary_meal_type_${type.take(4)}")
                    ) {
                        Text(
                            text = "$emoji $type",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) CoralDark else TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Verilen Besinler
            Text(
                text = "VERİLEN GIDALAR / BESİNLER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = foodItems,
                onValueChange = {
                    foodItems = it
                    if (it.isNotBlank()) showError = false
                },
                placeholder = { Text("Örn: Buharda kabak, haşlanmış yumurta sarısı, avokado") },
                isError = showError,
                supportingText = {
                    if (showError) {
                        Text("Lütfen yenen en az bir gıda belirtin", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diary_food_items_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = CardBorder
                )
            )

            // Hızlı Besin Ekleme Çipleri
            Text(
                text = "Hızlı Besin Ekle (+):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickFoodSuggestions.forEach { suggestion ->
                    Surface(
                        onClick = {
                            val cleanName = suggestion.substringAfter(" ").trim()
                            foodItems = if (foodItems.isBlank()) {
                                cleanName
                            } else {
                                "$foodItems, $cleanName"
                            }
                            showError = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "+ $suggestion",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Porsiyon & Miktar
            Text(
                text = "PORSİYON / TÜKETİLEN MİKTAR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickPortions.forEach { portion ->
                    val isSelected = portionAmount == portion
                    Surface(
                        onClick = { portionAmount = portion },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) SlateBlueSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.5.dp, if (isSelected) SlateBlue else CardBorder)
                    ) {
                        Text(
                            text = portion,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SlateBlue else TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Yiyecek Kıvamı
            Text(
                text = "YİYECEK KIVAMI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val textures = listOf(
                "🥣 Pürüzsüz Püre",
                "🍴 Çatalla Ezilmiş",
                "🍲 Pütürlü Ezme",
                "🥕 Parmak Gıda (BLW)"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                textures.forEach { tex ->
                    val rawTex = tex.substringAfter(" ").trim()
                    val isSelected = texture.contains(rawTex.take(5))
                    Surface(
                        onClick = { texture = rawTex },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) SageSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.5.dp, if (isSelected) SageGreen else CardBorder)
                    ) {
                        Text(
                            text = tex,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SageDark else TextPrimary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Bebeğin İştah Durumu
            Text(
                text = "BEBEĞİN İŞTAHI / TEPKİSİ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val appetites = listOf(
                "İştahla Bitirdi 😋" to (ReactionLovedSoft to ReactionLoved),
                "Severek Yedi 😊" to (SageSoft to SageGreen),
                "Az Tadına Baktı 🤏" to (ReactionNeutralSoft to ReactionNeutral),
                "Reddetti / İstemedi 🙅‍♂️" to (ReactionRejectedSoft to ReactionRejected)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                appetites.forEach { (item, colors) ->
                    val isSelected = appetite == item
                    Surface(
                        onClick = { appetite = item },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) colors.first else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.5.dp, if (isSelected) colors.second else CardBorder)
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.second else TextPrimary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Alerji / Sindirim Gözlemi
            Text(
                text = "ALERJİ & SİNDİRİM GÖZLEMİ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            val reactions = listOf(
                "Sorunsuz / Keyifli ✨" to (SageSoft to SuccessGreen),
                "Hafif Gaz / Huysuzluk 💨" to (OchreSoft to OchreDark),
                "Kızarıklık / Döküntü ⚠️" to (ReactionAllergySoft to ReactionAllergy),
                "Reflü / Tükürme 🚨" to (ReactionRejectedSoft to ReactionRejected)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                reactions.forEach { (item, colors) ->
                    val isSelected = reaction == item
                    Surface(
                        onClick = { reaction = item },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) colors.first else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.5.dp, if (isSelected) colors.second else CardBorder)
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.second else TextPrimary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Ebeveyn Notu
            Text(
                text = "GÖZLEMLER VE HAZIRLAMA NOTU",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Örn: Buharda 15 dk pişti, 1 tatlı kaşığı zeytinyağı eklendi. Keyfi çok yerindeydi.") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = CardBorder
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Kaydet Butonu
            Button(
                onClick = {
                    if (foodItems.isBlank()) {
                        showError = true
                    } else {
                        onSaveEntry(
                            selectedDate,
                            entryTime,
                            mealType,
                            foodItems.trim(),
                            portionAmount,
                            texture,
                            appetite,
                            reaction,
                            "",
                            notes.trim()
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_diary_entry_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Günlüğe Kaydet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnAccent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

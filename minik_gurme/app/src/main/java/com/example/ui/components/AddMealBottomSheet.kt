package com.example.ui.components

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
import androidx.compose.material.icons.filled.Restaurant
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
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
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
import com.example.ui.theme.ReactionRejected
import com.example.ui.theme.ReactionRejectedSoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageSoft
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

private data class MealTimeItem(
    val type: String,
    val emoji: String,
    val textColor: Color,
    val containerColor: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMealBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveMeal: (mealType: String, foodItems: String, texture: String, reaction: String, notes: String) -> Unit
) {
    var mealType by remember { mutableStateOf("Sabah") }
    var foodItems by remember { mutableStateOf("") }
    var texture by remember { mutableStateOf("Çatalla Ezilmiş") }
    var reaction by remember { mutableStateOf("Sevdi") }
    var notes by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val quickFoodSuggestions = listOf(
        "Yumurta sarısı", "Avokado", "Buharda kabak", "Organik ev yoğurdu",
        "Kıymalı havuç", "Kırmızı mercimek", "Didiklenmiş tavuk", "Elma püresi",
        "Muz dilimleri", "Yulaf lapası"
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CoralSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = CoralPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Yeni Öğün / Tadım Ekle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Besin çeşitliliğini ve kıvam gelişimini kaydedin",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = t("Kapat"),
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Öğün Zamanı
            Text(
                text = "ÖĞÜN ZAMANI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val mealTypes = listOf(
                    MealTimeItem("Sabah", "☀️", OchreDark, OchreSoft),
                    MealTimeItem("Öğle", "🌤️", SageDark, SageSoft),
                    MealTimeItem("İkindi", "🥛", SlateBlue, SlateBlueSoft),
                    MealTimeItem("Akşam", "🌙", CoralDark, CoralSoft)
                )
                mealTypes.forEach { item ->
                    val isSelected = mealType == item.type
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { mealType = item.type },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) item.containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.textColor else CardBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = item.emoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.type,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) item.textColor else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Verilen Gıdalar
            Text(
                text = "VERİLEN GIDALAR / TADIM İÇERİĞİ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = foodItems,
                onValueChange = {
                    foodItems = it
                    if (showError && it.isNotBlank()) showError = false
                },
                placeholder = {
                    Text(
                        "Örn: Haşlanmış kabak, avokado ezmesi, çeyrek yumurta sarısı...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                },
                isError = showError,
                supportingText = if (showError) {
                    { Text("Lütfen sunulan gıdaları yazınız.", color = CoralPrimary) }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_items_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick suggestions
            Text(
                text = "Sık Kullanılanlar:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickFoodSuggestions.forEach { suggestion ->
                    FilterChip(
                        selected = foodItems.contains(suggestion),
                        onClick = {
                            foodItems = if (foodItems.isBlank()) {
                                suggestion
                            } else if (!foodItems.contains(suggestion)) {
                                "$foodItems, $suggestion"
                            } else {
                                foodItems
                            }
                        },
                        label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = TextPrimary,
                            selectedContainerColor = CoralSoft,
                            selectedLabelColor = CoralPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = foodItems.contains(suggestion),
                            borderColor = if (foodItems.contains(suggestion)) CoralPrimary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Yiyecek Kıvamı
            Text(
                text = "YİYECEK KIVAMI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val textures = listOf(
                Pair("Pürüzsüz Püre", "🥣 1. Pürüzsüz Püre (Homojen, akışkan)"),
                Pair("Çatalla Ezilmiş", "🍴 2. Çatalla Ezilmiş (Hafif pütürlü doku)"),
                Pair("Pütürlü Ezme", "🍲 3. Pütürlü Ezme (Belirgin yumuşak parçalar)"),
                Pair("Parmak Gıda", "🥕 4. Yumuşak Parmak Gıdalar (BLW usulü)")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                textures.forEach { (key, label) ->
                    val isSelected = texture == key
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { texture = key },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) CoralSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CoralPrimary else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CoralDark else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Bebeğin Tepkisi
            Text(
                text = "BEBEĞİN TEPKİSİ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val reactions = listOf(
                    Triple("Sevdi", "😋 Sevdi", ReactionLoved to ReactionLovedSoft),
                    Triple("Nötr", "😐 Kararsız", ReactionNeutral to ReactionNeutralSoft),
                    Triple("İstemedi", "😣 İstemedi", OchreDark to OchreSoft),
                    Triple("Alerji Sinyali", "🚨 Reaksiyon", ReactionAllergy to ReactionAllergySoft)
                )
                reactions.forEach { (key, label, colors) ->
                    val isSelected = reaction == key
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { reaction = key },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) colors.second else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) colors.first else CardBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.first else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Notlar & Gözlemler
            Text(
                text = "GÖZLEM VE NOTLAR (İSTEĞE BAĞLI)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = {
                    Text(
                        "Porsiyon miktarı (örn: 2 tatlı kaşığı), keyif durumu veya gaz gözlemi...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meal_notes_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralPrimary,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    if (foodItems.isBlank()) {
                        showError = true
                    } else {
                        onSaveMeal(mealType, foodItems.trim(), texture, reaction, notes.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_meal_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Öğünü Kaydet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

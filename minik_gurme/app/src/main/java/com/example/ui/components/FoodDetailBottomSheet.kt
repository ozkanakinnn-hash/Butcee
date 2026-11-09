package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import com.example.i18n.t
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodDirectoryItem
import com.example.data.model.TriedFood
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralBorder
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralLight
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.OchreDark
import com.example.ui.theme.OchreSoft
import com.example.ui.theme.ReactionAllergy
import com.example.ui.theme.ReactionAllergySoft
import com.example.ui.theme.SageDark
import com.example.ui.theme.SageSoft
import com.example.ui.theme.SlateBlue
import com.example.ui.theme.SlateBlueSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodDetailBottomSheet(
    food: FoodDirectoryItem,
    triedInfo: TriedFood?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onOpenReactionDialog: () -> Unit,
    onAddToMeal: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("food_detail_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag indicator & Close Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CoralSoft,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = food.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CoralDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
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

            // Realistic Food Photo (for Avocado or key foods)
            if (food.id == "avokado") {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avocado_serving),
                        contentDescription = t("Avokado Hazırlama Yöntemleri"),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }

            // Food Hero Banner (Matching Screenshots 2, 3, 4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CoralLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = food.emoji, fontSize = 44.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = food.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            // Key Fact Badges Grid (Images 2 & 4: Yaş, Alerjen, Boğulma, Bağırsak)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Age Badge
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = SlateBlueSoft,
                    border = BorderStroke(1.dp, Color(0xFFD0E1FD))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Başlangıç",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateBlue
                        )
                        Text(
                            text = food.ageDisplay,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateBlue
                        )
                    }
                }

                // Allergen Badge
                Surface(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(16.dp),
                    color = if (food.isAllergen) ReactionAllergySoft else SageSoft,
                    border = BorderStroke(
                        1.dp,
                        if (food.isAllergen) Color(0xFFFBC4C8) else Color(0xFFBCE3DD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ortak Alerjen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (food.isAllergen) ReactionAllergy else SageDark
                        )
                        Text(
                            text = if (food.isAllergen) "⚠️ Evet" else "✓ Hayır",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (food.isAllergen) ReactionAllergy else SageDark
                        )
                    }
                }

                // Choking Hazard Badge
                Surface(
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(16.dp),
                    color = if (food.isChokingHazard) OchreSoft else SageSoft,
                    border = BorderStroke(
                        1.dp,
                        if (food.isChokingHazard) Color(0xFFF9E8B8) else Color(0xFFBCE3DD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Boğulma Tehlikesi",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (food.isChokingHazard) OchreDark else SageDark
                        )
                        Text(
                            text = if (food.isChokingHazard) "Dikkat!" else "Düşük",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (food.isChokingHazard) OchreDark else SageDark
                        )
                    }
                }
            }

            // Nutrients Pills (Image 3: Besin Etiketleri)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ZENGİN OLDUĞU BESİN ÖĞELERİ",
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
                    food.nutrients.forEach { nutrient ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "✨ $nutrient",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // How to Serve by Age (Matching Screenshot 2: "Nasıl Sunulacağını Bilin - Tam Hazırlık Yöntemleri")
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SageSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🥣", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Nasıl Sunulacağını Bilin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Yaşa göre doğru kesim ve hazırlık yöntemleri",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // 6m+ Stage
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralSoft.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, CoralBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "6m+ İlk Başlangıç (Püre veya Kalın Parmak Şerit)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = food.serving6m,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // 9m+ Stage
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OchreSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFFF9E8B8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "9m+ Kıskaç Tutuşu (Lokma Büyüklüğünde Parçalar)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OchreDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = food.serving9m,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // 12m+ Stage
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SageSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color(0xFFBCE3DD)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "12m+ / 18m+ Kendi Kendine Beslenme",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SageDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = food.serving12m,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Safety Warning Box (Image 3: Boğulma ve Güvenlik Kutusu)
            if (food.safetyAlert.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (food.isChokingHazard) OchreSoft else ReactionAllergySoft.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (food.isChokingHazard) Color(0xFFF9E8B8) else Color(0xFFFBC4C8)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (food.isChokingHazard) OchreDark else ReactionAllergy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = food.safetyAlert,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (food.isChokingHazard) Color(0xFF6B4B03) else Color(0xFF881337),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Storage Tips (Image 3: Saklama Kutusu)
            if (food.storageTips.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = OchreDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = food.storageTips,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Current Trial Status (if already tried)
            if (triedInfo != null && triedInfo.isTried) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SageSoft,
                    border = BorderStroke(1.dp, Color(0xFFBCE3DD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SageDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Bebeğiniz bu gıdayı tattı!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark
                                )
                                if (triedInfo.triedDate.isNotBlank()) {
                                    Text(
                                        text = "Tadım tarihi: ${triedInfo.triedDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Text(
                            text = when (triedInfo.reaction) {
                                "loved" -> "❤️ Sevdi"
                                "disliked" -> "😣 Beğenmedi"
                                "allergy" -> "🚨 Alerji"
                                else -> "😐 Nötr"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Bottom Actions (Matching Screenshot 1 & 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenReactionDialog,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CoralBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("record_reaction_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tepki Kaydet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CoralPrimary
                    )
                }

                Button(
                    onClick = onAddToMeal,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("add_food_to_meal_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Öğüne Ekle",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

package akinai.minikgurme.ui.components

import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import akinai.minikgurme.ui.theme.*
import androidx.compose.foundation.BorderStroke
import akinai.minikgurme.i18n.t
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import akinai.minikgurme.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import akinai.minikgurme.data.model.BabyRecipe
import akinai.minikgurme.ui.theme.CardBorder
import akinai.minikgurme.ui.theme.CoralBorder
import akinai.minikgurme.ui.theme.CoralDark
import akinai.minikgurme.ui.theme.CoralLight
import akinai.minikgurme.ui.theme.CoralPrimary
import akinai.minikgurme.ui.theme.CoralSoft
import akinai.minikgurme.ui.theme.ReactionAllergy
import akinai.minikgurme.ui.theme.SageBorder
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
fun RecipeDetailBottomSheet(
    recipe: BabyRecipe,
    babyAgeMonths: Int = 7,
    isFavorite: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToMeals: () -> Unit,
    onAddToDiary: () -> Unit = onAddToMeals,
    onAddToShopping: (factor: Float, ingredient: akinai.minikgurme.data.model.RecipeIngredient?) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Malzemeler, 1: Talimatlar
    val base = remember(recipe.id) { akinai.minikgurme.util.baseServings(recipe.servings) }
    var portions by remember(recipe.id) { mutableIntStateOf(base) }
    val factor = portions.toFloat() / base

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
                .testTag("recipe_detail_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Age Badge + Favorite + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = CoralSoft,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = recipe.ageLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CoralDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    if (recipe.ageMonths <= babyAgeMonths) {
                        Surface(
                            color = SageSoft,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SageBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SageDark,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Bebeğinize Uygun",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isFavorite) ReactionAllergySoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = t("Favori"),
                            tint = if (isFavorite) ReactionAllergy else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
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
            }

            // Recipe Banner (Image 6: Görsel, Başlık, Süre, Porsiyon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CoralLight),
                    contentAlignment = Alignment.Center
                ) {
                    akinai.minikgurme.ui.components.FoodImage(recipe.emoji, 57.dp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = recipe.prepTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = akinai.minikgurme.util.scaleAmount(akinai.minikgurme.i18n.t(recipe.servings), factor),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Tags Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recipe.tags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CoralSoft.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = CoralDark,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Tab Row: Malzemeler vs Talimatlar (Matching Screenshot 6)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = CoralPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CoralPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Malzemeler (${recipe.ingredients.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Talimatlar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Porsiyon ayarı + tümünü alışveriş listesine ekle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (portions > 1) portions-- },
                        modifier = Modifier.size(34.dp).testTag("portion_minus")
                    ) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = CoralPrimary) }
                    Text(
                        text = "$portions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp).testTag("portion_value")
                    )
                    IconButton(
                        onClick = { if (portions < 12) portions++ },
                        modifier = Modifier.size(34.dp).testTag("portion_plus")
                    ) { Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = CoralPrimary) }
                    Text(
                        text = "porsiyon",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onAddToShopping(factor, null) },
                        modifier = Modifier.testTag("add_all_to_shopping")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp), tint = CoralDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tümünü listeye ekle", color = CoralDark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                // Malzemeler Listesi
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recipe.ingredients.forEach { ingredient ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, CardBorder),
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
                                    akinai.minikgurme.ui.components.FoodImage(ingredient.emoji, 26.dp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = ingredient.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CoralSoft
                                ) {
                                    Text(
                                        text = akinai.minikgurme.util.scaleAmount(akinai.minikgurme.i18n.t(ingredient.amount), factor),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onAddToShopping(factor, ingredient) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = akinai.minikgurme.i18n.t("Listeye ekle"), tint = CoralPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Talimatlar Listesi
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    recipe.instructions.forEachIndexed { index, step ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(CoralPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OnAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Doctor's Tip Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SageSoft.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, SageBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = SageDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = recipe.doctorTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = SageText,
                        lineHeight = 18.sp
                    )
                }
            }

            // Action Buttons: Beslenme Günlüğüne Kaydet + Öğünlere Ekle
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2. Bugünün Öğünlerine Ekle
                Button(
                    onClick = onAddToMeals,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_recipe_to_meals_btn"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = OnAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bugünün öğünlerine ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

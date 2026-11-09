package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShoppingItem
import com.example.i18n.Text
import com.example.i18n.t
import com.example.ui.theme.*
import com.example.util.scaleAmount

/** Tariflerden eklenen malzemeler ve elle eklenen kalemlerle alışveriş listesi. */
@Composable
fun ShoppingListScreen(
    items: List<ShoppingItem>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onToggle: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onClearChecked: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    val remaining = items.count { !it.checked }
    val submit = { if (input.isNotBlank()) { onAdd(input); input = "" } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = t("Geri"), tint = TextPrimary) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alışveriş Listesi 🛒", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(
                        if (items.isEmpty()) "Tariflerden malzeme ekleyin veya aşağıya yazın." else "$remaining kalem alınacak",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                if (items.any { it.checked }) {
                    TextButton(onClick = onClearChecked, modifier = Modifier.testTag("shopping_clear_checked")) {
                        Text("Alınanları temizle", color = CoralDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(60) },
                    placeholder = { Text("Ürün ekle (örn: avokado)", color = TextTertiary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).testTag("shopping_input")
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = submit, modifier = Modifier.size(52.dp).testTag("shopping_add")) {
                    Icon(Icons.Filled.Add, contentDescription = t("Ekle"))
                }
            }
        }
        if (items.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SageSoft),
                    border = BorderStroke(1.dp, SageBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Text("🧺", fontSize = 40.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Listeniz boş. Bir tarifi açıp “Tümünü listeye ekle”ye dokunun.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SageText
                        )
                    }
                }
            }
        }
        items(items, key = { it.id }) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, if (item.checked) CardBorder else CoralBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("shopping_item")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = { onToggle(item) },
                        colors = CheckboxDefaults.colors(checkedColor = SageGreen)
                    )
                    androidx.compose.material3.Text(item.emoji, fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.checked) TextTertiary else TextPrimary,
                            textDecoration = if (item.checked) TextDecoration.LineThrough else null
                        )
                        if (item.source.isNotBlank()) {
                            Text(item.source, style = MaterialTheme.typography.labelSmall, color = TextTertiary, maxLines = 1)
                        }
                    }
                    if (item.amount.isNotBlank()) {
                        Text(
                            scaleAmount(t(item.amount), item.factor),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CoralDark
                        )
                    }
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Filled.Close, contentDescription = t("Sil"), tint = TextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

package com.example.ui.components

import com.example.ui.theme.*
import androidx.compose.foundation.clickable
import com.example.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.i18n.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    initialName: String,
    initialAge: Int,
    initialStartDate: String,
    onDismiss: () -> Unit,
    onSave: (name: String, age: Int, startDate: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var age by remember { mutableIntStateOf(initialAge) }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var expandedAgeDropdown by remember { mutableStateOf(false) }

    val ageOptions = listOf(
        Pair(6, "6. Ay (İlk Başlangıç)"),
        Pair(7, "7. Ay (Çeşitlilik Artışı)"),
        Pair(8, "8. Ay (Pütürlü & Parmak Gıda)"),
        Pair(9, "9. Ay (Sofra Benzeri Besinler)"),
        Pair(10, "10. Ay (Sofra Beslenmesine Geçiş)"),
        Pair(11, "11. Ay (Bağımsız Beslenme)"),
        Pair(12, "12. Ay+ (Aile Sofrası)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Bebeğin Profilini Düzenle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                Column {
                    Text(
                        text = "BEBEĞİN ADI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Örn: Alp, Defne...", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }

                // Age Dropdown
                Column {
                    Text(
                        text = "KAÇ AYLIK?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedAgeDropdown,
                        onExpandedChange = { expandedAgeDropdown = !expandedAgeDropdown }
                    ) {
                        val currentLabel = ageOptions.find { it.first == age }?.second ?: "$age. Ay"
                        OutlinedTextField(
                            value = currentLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAgeDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("profile_age_dropdown"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralPrimary,
                                unfocusedBorderColor = CardBorder
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAgeDropdown,
                            onDismissRequest = { expandedAgeDropdown = false }
                        ) {
                            ageOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.second) },
                                    onClick = {
                                        age = option.first
                                        expandedAgeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Start Date
                Column {
                    Text(
                        text = "EK GIDAYA BAŞLAMA TARİHİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        placeholder = { Text("YYYY-AA-GG", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_startdate_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name.trim(), age, startDate.trim())
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralPrimary,
                    contentColor = OnAccent
                ),
                modifier = Modifier.testTag("profile_save_button")
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Vazgeç", color = TextSecondary)
            }
        }
    )
}

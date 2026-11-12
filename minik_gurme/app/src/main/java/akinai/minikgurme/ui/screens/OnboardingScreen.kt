package akinai.minikgurme.ui.screens

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.i18n.AppLanguage
import akinai.minikgurme.i18n.Text
import akinai.minikgurme.ui.theme.*

/** "4,5" ve "4.5" girişlerini kabul eder. */
private fun parseDecimal(s: String): Float? = s.trim().replace(',', '.').toFloatOrNull()

/**
 * İlk açılış ekranı: bebeğin adı, kaç aylık olduğu, kilosu ve boyu sorulur.
 * Kaydedilince uygulama bu bilgilerle (istatistikler sıfırdan) başlar.
 */
@Composable
fun OnboardingScreen(
    onOpenPrivacy: () -> Unit = {},
    onComplete: (name: String, ageMonths: Int, weightKg: Float, heightCm: Float, feedingStyle: String, sex: String) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var months by rememberSaveable { mutableIntStateOf(6) }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var tried by rememberSaveable { mutableStateOf(false) }
    var style by rememberSaveable { mutableStateOf("mixed") }
    var sex by rememberSaveable { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }

    val w = parseDecimal(weight)
    val h = parseDecimal(height)
    val nameOk = name.isNotBlank()
    val weightOk = w != null && w in 1.5f..30f
    val heightOk = h != null && h in 40f..130f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dil seçimi (ilk açılışta)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                listOf("tr" to "TR", "en" to "EN").forEach { (code, label) ->
                    val selected = AppLanguage.current == code
                    OutlinedButton(
                        onClick = { AppLanguage.set(context, code) },
                        modifier = Modifier.padding(start = 6.dp).height(34.dp).testTag("onb_lang_$code"),
                        border = BorderStroke(1.dp, if (selected) CoralPrimary else CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) CoralSoft else CardSurface
                        )
                    ) {
                        androidx.compose.material3.Text(label, color = if (selected) CoralDark else TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(akinai.minikgurme.R.drawable.logo_emblem),
                contentDescription = "Minik Gurme",
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Minik Gurme'ye hoş geldiniz",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Önerileri bebeğinize göre hazırlayabilmemiz için birkaç bilgiye ihtiyacımız var.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(30) },
                        label = { Text("Bebeğinizin adı") },
                        singleLine = true,
                        isError = tried && !nameOk,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth().testTag("onb_name")
                    )

                    Column {
                        Text("Kaç aylık?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(onClick = { if (months > 0) months-- }, modifier = Modifier.testTag("onb_month_minus")) {
                                Icon(Icons.Filled.Remove, contentDescription = null)
                            }
                            Text(
                                "$months. Ay",
                                modifier = Modifier.weight(1f).testTag("onb_month_value"),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralDark
                            )
                            FilledTonalIconButton(onClick = { if (months < 36) months++ }, modifier = Modifier.testTag("onb_month_plus")) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }
                        }
                        if (months < 6) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Ek gıdaya genellikle 6. ayda başlanır; uygulama 6. ay rehberini gösterir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OchreText
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5) },
                            label = { Text("Kilo (kg)") },
                            placeholder = { androidx.compose.material3.Text("7,5") },
                            singleLine = true,
                            isError = tried && !weightOk,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            modifier = Modifier.weight(1f).testTag("onb_weight")
                        )
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5) },
                            label = { Text("Boy (cm)") },
                            placeholder = { androidx.compose.material3.Text("67") },
                            singleLine = true,
                            isError = tried && !heightOk,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            modifier = Modifier.weight(1f).testTag("onb_height")
                        )
                    }
                    Column {
                        Text("Cinsiyet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("girl" to "Kız", "boy" to "Erkek").forEach { (code, label) ->
                                val sel = sex == code
                                OutlinedButton(
                                    onClick = { sex = code },
                                    modifier = Modifier.weight(1f).height(46.dp).testTag("onb_sex_$code"),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) CoralPrimary else CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (sel) CoralSoft else CardSurface)
                                ) { Text(label, color = if (sel) CoralDark else TextSecondary, fontWeight = FontWeight.Bold) }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Büyüme eğrisi WHO'nun kız/erkek tablolarıyla karşılaştırılır.", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                    Column {
                        Text("Püre mi, BLW mi?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("puree", "🥣", "Püre"),
                                Triple("blw", "🥕", "BLW"),
                                Triple("mixed", "🍽️", "Karma")
                            ).forEach { (code, emoji, label) ->
                                val sel = style == code
                                OutlinedButton(
                                    onClick = { style = code },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("onb_style_$code"),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) CoralPrimary else CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (sel) CoralSoft else CardSurface),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("$emoji $label", color = if (sel) CoralDark else TextSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            when (style) {
                                "puree" -> "Kaşıkla, pürüzsüzden pütürlüye geçen tarifler öne çıkar."
                                "blw" -> "Bebeğin kendi tuttuğu parmak besinler öne çıkar."
                                else -> "Püre ve parmak besinler birlikte gösterilir."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }

                    // Tıbbi uyarı onayı (zorunlu)
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { consent = !consent }
                            .testTag("onb_consent")
                    ) {
                        androidx.compose.material3.Checkbox(checked = consent, onCheckedChange = { consent = it })
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                "Uygulamadaki bilgilerin genel rehberlik amaçlı olduğunu, tıbbi tavsiye yerine geçmediğini anladım.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (tried && !consent) ReactionAllergy else TextSecondary
                            )
                            Text(
                                "Gizlilik politikası",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary,
                                modifier = Modifier.padding(top = 4.dp).clickable { onOpenPrivacy() }
                            )
                        }
                    }

                    if (tried && !(nameOk && weightOk && heightOk)) {
                        Text(
                            "Lütfen adı, kiloyu (1,5–30 kg) ve boyu (40–130 cm) kontrol edin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReactionAllergy
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    tried = true
                    if (nameOk && weightOk && heightOk && consent) onComplete(name.trim(), months, w!!, h!!, style, sex)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("onb_start"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Başlayalım", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Bu bilgileri daha sonra Profil ekranından değiştirebilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(1.dp))
        }
    }
}

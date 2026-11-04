@file:OptIn(ExperimentalMaterial3Api::class)

package com.nobet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nobet.data.Store
import com.nobet.i18n.t
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

/** Para birimi biçimi: Türkçede ₺, İngilizcede cihaz bölgesi (yoksa ABD). */
private fun currencyLocale(): Locale =
  if (!Store.isEn) Locale.forLanguageTag("tr-TR")
  else Locale.getDefault().takeIf { it.country.isNotEmpty() } ?: Locale.US

private fun parseNum(s: String): Double? = s.trim().replace(" ", "").replace(",", ".").toDoubleOrNull()

/** Bu ay takvimdeki çalışma saatleri (izin günleri hariç), 0,5 saate yuvarlanmış. */
private fun calendarHoursThisMonth(): Double {
  val ym = YearMonth.from(Store.today())
  val min = (1..ym.lengthOfMonth()).mapNotNull { Store.workOn(ym.atDay(it)) }.sumOf { it.type.durationMin }
  return Math.round(min / 60.0 * 2) / 2.0
}

/** Araçlar → Saatlik Ücret Hesaplayıcı (PRD §2.13, Ek B.10). Tamamen isteğe bağlı, yalnızca cihazda. */
@Composable
fun HourlyRateScreen() {
  val salary = parseNum(Store.toolSalary)
  val hours = parseNum(Store.toolHours)
  val valid = salary != null && salary > 0 && hours != null && hours in 1.0..744.0
  val fmt = NumberFormat.getCurrencyInstance(currencyLocale()).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(t("Saatlik Ücret Hesaplayıcı", "Hourly Rate Calculator")) },
        navigationIcon = { IconButton(onClick = { Nav.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, t("Geri", "Back")) } },
      )
    },
  ) { pad ->
    Column(
      Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        t("İsteğe bağlı bir araçtır. Girdiğiniz bilgiler yalnızca bu cihazda kalır; paylaşılmaz.",
          "This tool is optional. What you enter stays on this device and is never shared."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      OutlinedTextField(
        value = Store.toolSalary,
        onValueChange = { v ->
          Store.toolSalary = v.filter { it.isDigit() || it == ',' || it == '.' }.take(12)
          Store.save()
        },
        label = { Text(t("Aylık net maaş", "Net monthly salary")) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = Store.toolHours,
        onValueChange = { v ->
          Store.toolHours = v.filter { it.isDigit() || it == ',' || it == '.' }.take(6)
          Store.save()
        },
        label = { Text(t("Bu ay çalışılan toplam saat", "Total hours worked this month")) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
      )
      val cal = calendarHoursThisMonth()
      OutlinedButton(
        onClick = {
          Store.toolHours = if (cal % 1.0 == 0.0) cal.toLong().toString() else cal.toString().replace('.', if (Store.isEn) '.' else ',')
          Store.save()
        },
        enabled = cal > 0,
      ) {
        Text(t("Saatleri takvimimden al", "Use hours from my calendar") + if (cal > 0) " (${com.nobet.i18n.fmtNum(cal.toFloat()).removeSuffix(",0").removeSuffix(".0")})" else "")
      }

      NCard(Modifier.fillMaxWidth(), container = MaterialTheme.colorScheme.primaryContainer) {
        Text(t("Saatlik ücretiniz", "Your hourly wage"), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(
          if (valid) fmt.format(salary!! / hours!!) + t(" / saat", " / hour") else "–",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        if (!valid) {
          Text(
            t("Maaşı ve 1–744 arası toplam saati girin.", "Enter your salary and total hours between 1 and 744."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
      Text(
        t("Bu yalnızca kişisel bilginiz için basit bir hesaplayıcıdır; resmî bordro bilgisi değildir. Fazla mesai, gece primi, kesintiler ve vergiler dahil değildir.",
          "This is a simple calculator for your own reference, not official payroll information. It does not include overtime, night-shift premiums, deductions or taxes."),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row {
        TextButton(onClick = {
          Store.toolSalary = ""
          Store.toolHours = ""
          Store.save()
        }) { Text(t("Temizle", "Clear"), color = MaterialTheme.colorScheme.error) }
      }
      Spacer(Modifier.height(24.dp))
    }
  }
}

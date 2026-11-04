@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.nobet.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nobet.data.Builtins
import com.nobet.data.Store
import com.nobet.i18n.fmtClock
import com.nobet.i18n.fmtDur
import com.nobet.i18n.patternDesc
import com.nobet.i18n.patternName
import com.nobet.i18n.relDay
import com.nobet.i18n.roleName
import com.nobet.i18n.t
import com.nobet.i18n.typeName
import com.nobet.i18n.workTypeName

private const val LAST = 10

@Composable
fun Onboarding() {
  var page by remember { mutableIntStateOf(0) }
  var patternId by remember { mutableStateOf<String?>(null) }
  var offset by remember { mutableIntStateOf(0) }
  var editType by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // (typeId, isStart)

  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { page = LAST }

  fun finish() {
    val p = Builtins.patterns.firstOrNull { it.id == patternId }
    if (p != null) Store.applyPattern(p, Store.today(), offset, 365)
    Store.onboarded = true
    Store.changed()
  }

  BackHandler(enabled = page > 0) { page = if (page == 7 && patternId == null) 5 else page - 1 }

  Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
    if (page in 3 until LAST) {
      LinearProgressIndicator(progress = { (page - 2) / (LAST - 2f) }, modifier = Modifier.fillMaxWidth())
      Gap(16.dp)
    }
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      when (page) {
        0 -> Welcome(
          Icons.Filled.NightsStay,
          t("Gece de dünyayı ayakta tutanlar için.", "Built for people who keep the world running at night."),
          t("Mesainiz 9-5 değil; uygulamanız da olmamalı.", "Your schedule isn't 9 to 5 — your app shouldn't be either."),
        )
        1 -> Welcome(
          Icons.Filled.CalendarMonth,
          t("Vardiyalarınız saniyeler içinde hazır.", "Your shifts, sorted in seconds."),
          t("Düzeninizi bir kez seçin, önümüzdeki ayları biz dolduralım.", "Pick your rotation once — we fill in the months ahead."),
        )
        2 -> Welcome(
          Icons.Filled.Bedtime,
          t("Her vardiyadan sonra nasıl toparlandığınızı görün.", "See how well you recover after every shift."),
          t("Her vardiyanın ardından dört kısa soru, zamanla nazik içgörüler ve vardiyanıza uygun hatırlatmalar.",
            "Four quick questions after each shift, gentle insights over time and reminders that fit your shifts."),
        )
        3 -> {
          Title(t("Dilinizi seçin", "Choose your language"))
          listOf("tr" to "Türkçe", "en" to "English").forEach { (code, label) ->
            ChoiceCard(label, selected = (Store.isEn && code == "en") || (!Store.isEn && code == "tr")) {
              Store.lang = code
              Store.save()
            }
          }
          Text(t("Daha sonra Ayarlar'dan değiştirebilirsiniz.", "You can change this later in Settings."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        4 -> {
          Title(t("Biraz kendinizden bahsedin", "Tell us a little about you"))
          Text(
            t("Tüm alanlar isteğe bağlıdır; dilediğiniz zaman atlayabilir ve Ayarlar → Profil'den değiştirebilirsiniz.",
              "Every field is optional — skip anything and change it later in Settings → Profile."),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Gap()
          ProfileFields(onWorkTypePicked = { w ->
            // Çalışma şekli tek bir düzene karşılık geliyorsa bir sonraki adımda onu önceden seç
            patternId = Builtins.suggestedFor("", w).firstOrNull() ?: patternId
          })
        }
        5 -> {
          Title(t("Vardiya düzeniniz nasıl?", "How does your rotation work?"))
          val sug = Builtins.suggestedFor(Store.role, Store.workType)
          SectionTitle(t("Size önerilenler", "Suggested for you"))
          Builtins.patterns.filter { it.id in sug }.forEach { p ->
            ChoiceCard(patternName(p), patternDesc(p), selected = patternId == p.id) { patternId = p.id }
          }
          SectionTitle(t("Diğer düzenler", "Other patterns"))
          Builtins.patterns.filter { it.id !in sug }.forEach { p ->
            ChoiceCard(patternName(p), patternDesc(p), selected = patternId == p.id) { patternId = p.id }
          }
          Gap()
          ChoiceCard(t("Vardiyaları kendim ekleyeceğim", "I'll add shifts manually"), selected = false) {
            patternId = null
            page = 7
          }
        }
        6 -> {
          val p = Builtins.patterns.firstOrNull { it.id == patternId } ?: Builtins.patterns.first()
          Title(t("Bugün döngünün hangi günü?", "Which day of the cycle is today?"))
          Text(t("Bugün yapacağınız (veya izinliyseniz bulunduğunuz) güne dokunun.", "Tap the day you are on today."), color = MaterialTheme.colorScheme.onSurfaceVariant)
          Gap()
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            p.seq.forEachIndexed { i, id ->
              val ty = Store.typeById(id) ?: return@forEachIndexed
              Surface(
                onClick = { offset = i },
                shape = RoundedCornerShape(12.dp),
                color = if (offset == i) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              ) {
                Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                  Text("${i + 1}", style = MaterialTheme.typography.labelSmall)
                  ShiftBadge(ty, 30.dp)
                }
              }
            }
          }
          Gap(20.dp)
          SectionTitle(t("Önümüzdeki 7 gün", "Next 7 days"))
          for (k in 0 until 7) {
            val ty = Store.typeById(p.seq[(k + offset) % p.seq.size]) ?: continue
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
              ShiftBadge(ty, 28.dp)
              Spacer(Modifier.width(12.dp))
              Text(relDay(Store.today().plusDays(k.toLong())), Modifier.weight(1f))
              Text(typeName(ty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
        7 -> {
          Title(t("Vardiyalarınız ne zaman başlıyor ve bitiyor?", "When do your shifts start and end?"))
          Store.types.filter { !it.isOff }.forEach { ty ->
            NCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                ShiftBadge(ty)
                Spacer(Modifier.width(12.dp))
                Text(typeName(ty), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { editType = ty.id to true }) { Text(fmtClock(ty.startMin)) }
                Text("–")
                TextButton(onClick = { editType = ty.id to false }) { Text(fmtClock(ty.endMin)) }
              }
            }
          }
        }
        8 -> {
          Title(t("Genellikle ne kadar uyku sizi dinlenmiş hissettirir? (isteğe bağlı)", "How much sleep usually helps you feel rested? (optional)"))
          Text(fmtDur(Store.sleepGoalMin), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          Slider(
            value = Store.sleepGoalMin.toFloat(),
            onValueChange = { Store.sleepGoalMin = ((it / 15f).toInt() * 15) },
            valueRange = 300f..600f,
          )
          Text(t("Yalnızca toparlanma puanınızda kullanılır; hedef olarak gösterilmez. Atlarsanız 7 sa 30 dk varsayılır.", "Only used for your recovery score; never shown as a target. If you skip, 7 h 30 m is assumed."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        9 -> {
          Welcome(
            Icons.Filled.Notifications,
            t("Gece vardiyasından önce haber verelim mi?", "Want a heads-up before your night shift?"),
            t("Dinlenmeniz için hatırlatırız — uyurken asla.", "We'll remind you to rest — never while you sleep."),
          )
          NCard(Modifier.fillMaxWidth()) {
            Text("Nöbet · " + t("şimdi", "now"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(t("Yarın gece vardiyası 🌙", "Night shift tomorrow 🌙"), fontWeight = FontWeight.SemiBold)
            Text(t("Sabah biraz geç kalkmaya ve bu akşamı sakin geçirmeye çalışın.", "Try to sleep in and keep tonight relaxed."), style = MaterialTheme.typography.bodySmall)
          }
        }
        else -> {
          val w = Builtins.patterns.firstOrNull { it.id == patternId }?.let { p ->
            (0 until 30).firstNotNullOfOrNull { k ->
              Store.typeById(p.seq[(k + offset) % p.seq.size])?.takeIf { !it.isOff }?.let { k to it }
            }
          }
          val hi = if (Store.name.isBlank()) "" else ", ${Store.name.trim()}"
          Welcome(
            Icons.Filled.NightsStay,
            t("Hazırsınız$hi.", "You're all set$hi."),
            if (w != null) {
              t("Sıradaki vardiyanız: ", "Your next shift: ") + "${typeName(w.second)} · ${relDay(Store.today().plusDays(w.first.toLong()))} ${fmtClock(w.second.startMin)}"
            } else {
              t("Takvimden vardiyalarınızı ekleyebilirsiniz.", "You can add your shifts from the calendar.")
            },
          )
          Text(
            t("Bir sonraki vardiyanızdan sonra nasıl toparlandığınızı görmek için dört kısa soru soracağız.", "After your next shift we'll ask four quick questions to see how you're recovering."),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }

    Gap()
    when (page) {
      in 0..2 -> {
        BigButton(if (page == 2) t("Başlayalım", "Get started") else t("İleri", "Next")) { page++ }
        TextButton(onClick = { page = 3 }, modifier = Modifier.fillMaxWidth()) { Text(t("Atla", "Skip")) }
      }
      8 -> {
        BigButton(t("Devam", "Continue")) { page++ }
        TextButton(onClick = {
          Store.sleepGoalMin = 450
          page++
        }, modifier = Modifier.fillMaxWidth()) { Text(t("Atla", "Skip")) }
      }
      4 -> {
        BigButton(t("Devam", "Continue")) { page++ }
        TextButton(onClick = { page++ }, modifier = Modifier.fillMaxWidth()) { Text(t("Atla", "Skip")) }
      }
      5 -> {
        BigButton(t("Devam", "Continue"), enabled = patternId != null) { page = 6 }
        TextButton(onClick = { patternId = null; page = 7 }, modifier = Modifier.fillMaxWidth()) { Text(t("Şimdilik atla", "Skip for now")) }
      }
      9 -> {
        BigButton(t("Hatırlatmalara izin ver", "Allow reminders")) {
          if (Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS) else page = LAST
        }
        TextButton(onClick = { page = LAST }, modifier = Modifier.fillMaxWidth()) { Text(t("Şimdi değil", "Not now")) }
      }
      LAST -> {
        BigButton(t("Bugün ekranına git", "Go to Today")) { finish() }
      }
      else -> BigButton(t("Devam", "Continue")) { page++ }
    }
  }

  editType?.let { (id, isStart) ->
    val ty = Store.typeById(id)
    if (ty != null) {
      TimePickDialog(
        if (isStart) t("Başlangıç", "Start") else t("Bitiş", "End"),
        if (isStart) ty.startMin else ty.endMin,
        { editType = null },
      ) { m ->
        Store.upsertType(if (isStart) ty.copy(startMin = m) else ty.copy(endMin = m))
        editType = null
      }
    }
  }
}

/**
 * İsteğe bağlı profil alanları: ad/takma ad, meslek (hızlı seçim + serbest unvan), çalışma şekli.
 * Onboarding'de ve Ayarlar → Profil'de aynı bileşen kullanılır. Seçili bir öğeye tekrar dokunmak seçimi kaldırır.
 */
@Composable
fun ProfileFields(onWorkTypePicked: (String) -> Unit = {}) {
  OutlinedTextField(
    value = Store.name,
    onValueChange = {
      Store.name = it.take(30)
      Store.save()
    },
    label = { Text(t("Ad veya takma ad (isteğe bağlı)", "Name or nickname (optional)")) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  Gap()
  SectionTitle(t("Meslek (isteğe bağlı)", "Profession (optional)"))
  val roles: List<Pair<String, ImageVector>> = listOf(
    "nurse" to Icons.Filled.MedicalServices,
    "factory" to Icons.Filled.Factory,
    "security" to Icons.Filled.Security,
    "hospitality" to Icons.Filled.Hotel,
    "other" to Icons.Filled.MoreHoriz,
  )
  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    roles.forEach { (r, icon) ->
      FilterChip(
        selected = Store.role == r,
        onClick = {
          val newRole = if (Store.role == r) "" else r
          Store.role = newRole
          // Fabrika seçilirse 8 saatlik vardiya saatleri varsayılır (özel saatler ezilmez, yalnızca yerleşik türler)
          if (newRole == "factory" || (r == "factory" && newRole.isEmpty())) {
            val defs = Builtins.defaultTypes(newRole == "factory")
            defs.forEach { d -> Store.types.indexOfFirst { it.id == d.id }.takeIf { it >= 0 }?.let { Store.types[it] = d } }
          }
          Store.save()
        },
        label = { Text(roleName(r)) },
        leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
      )
    }
  }
  Gap(8.dp)
  OutlinedTextField(
    value = Store.jobTitle,
    onValueChange = {
      Store.jobTitle = it.take(40)
      Store.save()
    },
    label = { Text(t("Unvan (ör. Yoğun bakım hemşiresi)", "Job title (e.g. ICU nurse)")) },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  Gap()
  SectionTitle(t("Çalışma şekli (isteğe bağlı)", "Work type (optional)"))
  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Builtins.workTypes.forEach { w ->
      FilterChip(
        selected = Store.workType == w,
        onClick = {
          Store.workType = if (Store.workType == w) "" else w
          Store.save()
          if (Store.workType.isNotEmpty()) onWorkTypePicked(Store.workType)
        },
        label = { Text(workTypeName(w)) },
      )
    }
  }
}

@Composable
private fun Welcome(icon: ImageVector, title: String, body: String) {
  Column(Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Surface(shape = RoundedCornerShape(40.dp), color = MaterialTheme.colorScheme.primaryContainer) {
      Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
      }
    }
    Gap(32.dp)
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Gap()
    Text(body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun Title(text: String) {
  Text(text, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
fun ChoiceCard(title: String, sub: String = "", selected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      if (selected) 2.dp else 1.dp,
      if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
    ),
    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
  ) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(14.dp))
      }
      Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

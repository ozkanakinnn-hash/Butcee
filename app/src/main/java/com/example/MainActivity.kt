package com.example

import com.example.i18n.t
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reminder.ReminderScheduler
import com.example.data.AppPreferences
import com.example.data.AppColorTheme
import com.example.data.AppThemeMode
import com.example.data.CountdownResult
import com.example.data.HolidayCountdownCalculator
import com.example.data.HolidayCountdownInfo
import com.example.data.PrayerRepository
import com.example.data.PrayerTimeUtils
import com.example.data.PrayerTimes
import com.example.data.RamadanCalendar
import com.example.data.RamadanMenus
import com.example.data.SpiritualQuotes
import com.example.ui.components.CannonConfettiAnimation
import com.example.ui.dialogs.LocationSelectorDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.screens.FastingCalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MenusScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SpiritualScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NotificationHelper
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.MoreHoriz
import com.example.data.SurahInfo
import com.example.ui.screens.FastingTrackerScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.NearbyMosquesScreen
import com.example.ui.screens.QiblaFinderScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SpiritualNotesScreen
import com.example.ui.screens.SurahDetailScreen

sealed class ActiveSubScreen {
  data object Qibla : ActiveSubScreen()
  data object Mosques : ActiveSubScreen()
  data object FastingTracker : ActiveSubScreen()
  data object QuranList : ActiveSubScreen()
  data class SurahDetail(val surah: SurahInfo, val fromTab: Boolean = false) : ActiveSubScreen()
  data object FastingGuide : ActiveSubScreen()
  data object SpiritualNotes : ActiveSubScreen()
  data object Zikirmatik : ActiveSubScreen()
  data object Esma : ActiveSubScreen()
}

data class NavTabItem(
  val title: String,
  val icon: ImageVector? = null,
  val iconRes: Int? = null,
  val testTag: String
)

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppHolder.context = applicationContext
    com.example.util.CrashReporter.install(this)
    val lastCrash = com.example.util.CrashReporter.consumeLastCrash(this)
    enableEdgeToEdge()

    // Initialize Google Mobile Ads SDK (AdMob)
    try {
      MobileAds.initialize(this) {}
    } catch (e: Exception) {
      // Ignored in test environment
    }

    // Create Notification Channel for prayers and iftar
    NotificationHelper.createNotificationChannel(this)

    // Initialize Firebase and subscribe to all_users topic for push campaigns
    try {
      com.google.firebase.FirebaseApp.initializeApp(this)
      com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            android.util.Log.d("FCM", "Subscribed to all_users topic successfully")
          }
        }
    } catch (e: Exception) {
      android.util.Log.w("FCM", "Firebase init: ${e.message}")
    }

    val prefs = AppPreferences(this)
    com.example.i18n.AppLanguage.current = prefs.language
    val prayerRepo = PrayerRepository(this)

    setContent {
      var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }
      var currentColorTheme by remember { mutableStateOf(prefs.colorTheme) }
      var currentLanguage by remember { mutableStateOf(prefs.language) }
      val systemInDark = isSystemInDarkTheme()
      val isDark = when (currentThemeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemInDark
      }

      var isOnboardingCompleted by remember { mutableStateOf(prefs.isOnboardingCompleted) }
      var crashReport by remember { mutableStateOf(lastCrash) }

      MyApplicationTheme(darkTheme = isDark, colorTheme = currentColorTheme) {
        // Önceki açılışta çökme olduysa raporu göster
        crashReport?.let { report ->
          androidx.compose.material3.AlertDialog(
            onDismissRequest = { crashReport = null },
            title = { Text(t("Uygulama beklenmedik şekilde kapandı")) },
            text = {
              Text(
                "Sorunu çözebilmemiz için hata raporunu kopyalayıp geliştiriciye gönderebilirsiniz.\n\n" +
                  report.lineSequence().take(6).joinToString("\n"),
                fontSize = 11.sp
              )
            },
            confirmButton = {
              androidx.compose.material3.TextButton(onClick = {
                com.example.util.CrashReporter.copyToClipboard(this@MainActivity, report)
                android.widget.Toast.makeText(this@MainActivity, "Hata raporu kopyalandı", android.widget.Toast.LENGTH_SHORT).show()
                crashReport = null
              }) { Text(t("Raporu Kopyala")) }
            },
            dismissButton = {
              androidx.compose.material3.TextButton(onClick = { crashReport = null }) { Text(t("Kapat")) }
            }
          )
        }
        if (!isOnboardingCompleted) {
          OnboardingScreen(
            prefs = prefs,
            onComplete = {
              isOnboardingCompleted = true
            }
          )
        } else {
          androidx.compose.runtime.key(currentLanguage) {
            RamazanRehberiApp(
              prefs = prefs,
              prayerRepo = prayerRepo,
              onThemeChanged = { currentThemeMode = it },
              onColorThemeChanged = { currentColorTheme = it },
              onLanguageChanged = { lang ->
                com.example.i18n.AppLanguage.current = lang
                currentLanguage = lang
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun RamazanRehberiApp(
  prefs: AppPreferences,
  prayerRepo: PrayerRepository,
  onThemeChanged: (AppThemeMode) -> Unit,
  onColorThemeChanged: (AppColorTheme) -> Unit,
  onLanguageChanged: (String) -> Unit = {}
) {
  val coroutineScope = rememberCoroutineScope()
  var currentTab by remember { mutableIntStateOf(0) }
  var activeSubScreen by remember { mutableStateOf<ActiveSubScreen?>(null) }

  // Dialog Visibility states
  var showLocationDialog by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showCannonCelebration by remember { mutableStateOf(false) }

  // Dynamic Prayer Times State
  var prayerTimes by remember {
    mutableStateOf(
      PrayerTimes(
        imsak = "05:45",
        gunes = "07:10",
        ogle = "13:05",
        ikindi = "16:20",
        aksam = "18:45",
        yatsi = "20:05",
        dateStr = "02.11.2026",
        city = prefs.province,
        district = prefs.district
      )
    )
  }

  // Load prayer times whenever location changes or on start
  fun loadPrayers() {
    coroutineScope.launch {
      val result = prayerRepo.getPrayerTimes(city = prefs.province, district = prefs.district, country = prefs.country)
      prayerTimes = result
      ReminderScheduler.onPrayerTimesLoaded(AppHolder.context, result)
      com.example.util.WearSync.send(AppHolder.context, result, prefs)
    }
  }

  LaunchedEffect(prefs.country, prefs.province, prefs.district) {
    loadPrayers()
  }

  // 1-Second Live Countdown Ticker
  var countdown by remember {
    mutableStateOf(PrayerTimeUtils.calculateCountdown(prayerTimes, prefs.getFastingDates()))
  }
  var holidayInfo by remember {
    mutableStateOf(HolidayCountdownCalculator.calculate())
  }

  LaunchedEffect(prayerTimes) {
    while (isActive) {
      countdown = PrayerTimeUtils.calculateCountdown(prayerTimes, prefs.getFastingDates())
      holidayInfo = HolidayCountdownCalculator.calculate()
      delay(1000)
    }
  }

  // Ramazan'da Ramazan'ın gününe göre, dışında yılın gününe göre döner
  val ramadanDay = remember { RamadanCalendar.dayOfRamadan() }
  val todayMenu = remember {
    RamadanMenus.getMenuForDay(ramadanDay ?: java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR))
  }
  val todaySpiritual = remember { SpiritualQuotes.forToday() }

  // 5 Tabs: Ana Sayfa, Ramazan Rehberi, Takvim, Ayet & Dua, Dahası
  val navTabs = listOf(
    NavTabItem(t("Ana Sayfa"), icon = Icons.Default.Home, testTag = "nav_tab_home"),
    // Ramazan'da "Ramazan Rehberi", diğer aylarda "Kur'an-ı Kerim" sekmesi
    if (ramadanDay != null) NavTabItem(t("Ramazan Rehberi"), iconRes = R.drawable.ic_ramadan_guide, testTag = "nav_tab_guide")
    else NavTabItem(t("Kur'an-ı Kerim"), iconRes = R.drawable.ic_quran_tab, testTag = "nav_tab_quran"),
    NavTabItem(t("Takvim"), icon = Icons.Default.CalendarMonth, testTag = "nav_tab_calendar"),
    NavTabItem(t("Ayet & Dua"), iconRes = R.drawable.ic_quran_mushaf, testTag = "nav_tab_spiritual"),
    NavTabItem(t("Dahası"), icon = Icons.Default.MoreHoriz, testTag = "nav_tab_more")
  )

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("bottom_navigation_bar")
        ) {
          navTabs.forEachIndexed { index, tab ->
            val isSelected = currentTab == index
            NavigationBarItem(
              selected = isSelected,
              onClick = { currentTab = index },
              icon = {
                if (tab.iconRes != null) {
                  Icon(
                    painter = painterResource(tab.iconRes),
                    contentDescription = tab.title,
                    modifier = Modifier.size(24.dp)
                  )
                } else if (tab.icon != null) {
                  Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    modifier = Modifier.size(24.dp)
                  )
                }
              },
              label = {
                // Tüm etiketler tek satır: ikonlar aynı hizada kalır
                Text(
                  text = tab.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  letterSpacing = (-0.2).sp,
                  maxLines = 1,
                  softWrap = false,
                  overflow = TextOverflow.Visible,
                  fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
              ),
              alwaysShowLabel = true,
              modifier = Modifier.testTag(tab.testTag)
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          0 -> HomeScreen(
            prefs = prefs,
            prayerTimes = prayerTimes,
            countdown = countdown,
            holidayInfo = holidayInfo,
            menu = todayMenu,
            spiritualItem = todaySpiritual,
            onOpenLocationDialog = { showLocationDialog = true },
            onOpenSettings = { showSettingsDialog = true },
            onOpenAllMenus = { if (ramadanDay != null) currentTab = 1 else activeSubScreen = ActiveSubScreen.FastingGuide },
            onOpenQibla = { activeSubScreen = ActiveSubScreen.Qibla },
            onOpenMosques = { activeSubScreen = ActiveSubScreen.Mosques },
            onOpenZikirmatik = { activeSubScreen = ActiveSubScreen.Zikirmatik },
            onFireCannon = {
              showCannonCelebration = true
              NotificationHelper.triggerCannonHaptics(AppHolder.context)
            }
          )
          1 -> if (ramadanDay != null) MenusScreen(
            prefs = prefs,
            prayerTimes = prayerTimes,
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker }
          ) else QuranScreen(
            onSelectSurah = { surah: SurahInfo -> activeSubScreen = ActiveSubScreen.SurahDetail(surah, fromTab = true) },
            onNavigateBack = { currentTab = 0 }
          )
          2 -> FastingCalendarScreen(
            prefs = prefs,
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker }
          )
          3 -> SpiritualScreen(
            prefs = prefs
          )
          4 -> MoreHubScreen(
            prefs = prefs,
            onOpenQuran = { activeSubScreen = ActiveSubScreen.QuranList },
            isRamadan = ramadanDay != null,
            onOpenFastingGuide = { activeSubScreen = ActiveSubScreen.FastingGuide },
            onOpenQibla = { activeSubScreen = ActiveSubScreen.Qibla },
            onOpenMosques = { activeSubScreen = ActiveSubScreen.Mosques },
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker },
            onOpenNotes = { activeSubScreen = ActiveSubScreen.SpiritualNotes },
            onOpenSettings = { showSettingsDialog = true },
            onOpenZikirmatik = { activeSubScreen = ActiveSubScreen.Zikirmatik },
            onOpenEsma = { activeSubScreen = ActiveSubScreen.Esma }
          )
        }
      }
    }

    // Active SubScreen Overlay
    activeSubScreen?.let { subScreen ->
      BackHandler {
        if (subScreen is ActiveSubScreen.SurahDetail && !subScreen.fromTab) {
          activeSubScreen = ActiveSubScreen.QuranList
        } else {
          activeSubScreen = null
        }
      }

      // Alt ekranlar durum çubuğu / gezinme çubuğu altına girmesin (geri düğmesi görünür kalsın)
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .windowInsetsPadding(WindowInsets.systemBars)
      ) {
        when (subScreen) {
          is ActiveSubScreen.Qibla -> QiblaFinderScreen(
            prefs = prefs,
            onNavigateBack = { activeSubScreen = null }
          )
          is ActiveSubScreen.Mosques -> NearbyMosquesScreen(
            prefs = prefs,
            onNavigateBack = { activeSubScreen = null }
          )
          is ActiveSubScreen.FastingTracker -> FastingTrackerScreen(
            prefs = prefs,
            onNavigateBack = { activeSubScreen = null },
            onOpenCalendar = {
              activeSubScreen = null
              currentTab = 2
            }
          )
          is ActiveSubScreen.QuranList -> QuranScreen(
            onSelectSurah = { surah: SurahInfo -> activeSubScreen = ActiveSubScreen.SurahDetail(surah) },
            onNavigateBack = { activeSubScreen = null }
          )
          is ActiveSubScreen.SurahDetail -> SurahDetailScreen(
            surah = subScreen.surah,
            onNavigateBack = { activeSubScreen = if (subScreen.fromTab) null else ActiveSubScreen.QuranList }
          )
          is ActiveSubScreen.FastingGuide -> androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Row(
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              androidx.compose.material3.IconButton(onClick = { activeSubScreen = null }) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = t("Geri"),
                  tint = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = t("Oruç Rehberi"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
              )
            }
            MenusScreen(
              prefs = prefs,
              prayerTimes = prayerTimes,
              onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker },
              modifier = Modifier.weight(1f)
            )
          }
          is ActiveSubScreen.Zikirmatik -> com.example.ui.screens.ZikirmatikScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.Esma -> com.example.ui.screens.EsmaulHusnaScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.SpiritualNotes -> SpiritualNotesScreen(
            prefs = prefs,
            onNavigateBack = { activeSubScreen = null }
          )
        }
      }
    }

    // Cannon Celebration Particle Overlay
    CannonConfettiAnimation(
      visible = showCannonCelebration,
      onDismiss = { showCannonCelebration = false }
    )

    // Location Selector Dialog
    if (showLocationDialog) {
      LocationSelectorDialog(
        currentCountry = prefs.country,
        currentCity = prefs.province,
        currentDistrict = prefs.district,
        onLocationSelected = { country, city, district ->
          prefs.country = country
          prefs.province = city
          prefs.district = district
          loadPrayers()
        },
        onDismiss = { showLocationDialog = false }
      )
    }

    // Settings Dialog
    if (showSettingsDialog) {
      SettingsDialog(
        prefs = prefs,
        onThemeChanged = onThemeChanged,
        onColorThemeChanged = onColorThemeChanged,
        onLanguageChanged = onLanguageChanged,
        onChangeLocation = {
          showSettingsDialog = false
          showLocationDialog = true
        },
        onDismiss = {
          showSettingsDialog = false
          // Ayarlardaki hatırlatıcı değişikliklerini uygula
          ReminderScheduler.rescheduleAll(AppHolder.context)
        }
      )
    }
  }
}

object AppHolder {
  lateinit var context: android.content.Context
}

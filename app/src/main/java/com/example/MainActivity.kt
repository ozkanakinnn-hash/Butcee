package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
  data class SurahDetail(val surah: SurahInfo) : ActiveSubScreen()
  data object SpiritualNotes : ActiveSubScreen()
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
    val prayerRepo = PrayerRepository(this)

    setContent {
      var currentThemeMode by remember { mutableStateOf(prefs.themeMode) }
      var currentColorTheme by remember { mutableStateOf(prefs.colorTheme) }
      val systemInDark = isSystemInDarkTheme()
      val isDark = when (currentThemeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemInDark
      }

      var isOnboardingCompleted by remember { mutableStateOf(prefs.isOnboardingCompleted) }

      MyApplicationTheme(darkTheme = isDark, colorTheme = currentColorTheme) {
        if (!isOnboardingCompleted) {
          OnboardingScreen(
            prefs = prefs,
            onComplete = {
              isOnboardingCompleted = true
            }
          )
        } else {
          RamazanRehberiApp(
            prefs = prefs,
            prayerRepo = prayerRepo,
            onThemeChanged = { currentThemeMode = it },
            onColorThemeChanged = { currentColorTheme = it }
          )
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
  onColorThemeChanged: (AppColorTheme) -> Unit
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

  val todayMenu = remember { RamadanMenus.getMenuForDay(1) }
  val todaySpiritual = remember { SpiritualQuotes.getForDay(1) }

  // 5 Tabs: Ana Sayfa, Ramazan Rehberi, Takvim, Ayet & Dua, Dahası
  val navTabs = listOf(
    NavTabItem("Ana Sayfa", icon = Icons.Default.Home, testTag = "nav_tab_home"),
    NavTabItem("Ramazan Rehberi", iconRes = R.drawable.ic_ramadan_guide, testTag = "nav_tab_guide"),
    NavTabItem("Takvim", icon = Icons.Default.CalendarMonth, testTag = "nav_tab_calendar"),
    NavTabItem("Ayet & Dua", iconRes = R.drawable.ic_quran_mushaf, testTag = "nav_tab_spiritual"),
    NavTabItem("Dahası", icon = Icons.Default.MoreHoriz, testTag = "nav_tab_more")
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
            onOpenAllMenus = { currentTab = 1 },
            onOpenQibla = { activeSubScreen = ActiveSubScreen.Qibla },
            onOpenMosques = { activeSubScreen = ActiveSubScreen.Mosques },
            onFireCannon = {
              showCannonCelebration = true
              NotificationHelper.triggerCannonHaptics(AppHolder.context)
            }
          )
          1 -> MenusScreen(
            prefs = prefs,
            prayerTimes = prayerTimes,
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker }
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
            onOpenQibla = { activeSubScreen = ActiveSubScreen.Qibla },
            onOpenMosques = { activeSubScreen = ActiveSubScreen.Mosques },
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker },
            onOpenNotes = { activeSubScreen = ActiveSubScreen.SpiritualNotes },
            onOpenSettings = { showSettingsDialog = true }
          )
        }
      }
    }

    // Active SubScreen Overlay
    activeSubScreen?.let { subScreen ->
      BackHandler {
        if (subScreen is ActiveSubScreen.SurahDetail) {
          activeSubScreen = ActiveSubScreen.QuranList
        } else {
          activeSubScreen = null
        }
      }

      Box(modifier = Modifier.fillMaxSize()) {
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
            onNavigateBack = { activeSubScreen = ActiveSubScreen.QuranList }
          )
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

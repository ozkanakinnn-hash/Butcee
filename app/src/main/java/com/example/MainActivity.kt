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
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.qibla.QiblaFinderScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SpiritualNotesScreen
import com.example.ui.screens.SurahDetailScreen

sealed class ActiveSubScreen {
  data object Qibla : ActiveSubScreen()
  data object Mosques : ActiveSubScreen()
  data object FastingTracker : ActiveSubScreen()
  data class QuranList(val tab: Int = 0) : ActiveSubScreen()
  /** Sayfa sayfa Kur'an okuyucu (1-604). */
  data class QuranReader(val page: Int, val fromTab: Boolean = false) : ActiveSubScreen()
  data class SurahDetail(val surah: SurahInfo, val fromTab: Boolean = false) : ActiveSubScreen()
  data object FastingGuide : ActiveSubScreen()
  data object GreetingCards : ActiveSubScreen()
  data object KazaNamazi : ActiveSubScreen()
  data object KandilDualari : ActiveSubScreen()
  data object NotificationWizard : ActiveSubScreen()
  data object Premium : ActiveSubScreen()
  data object SpiritualNotes : ActiveSubScreen()
  data object Zikirmatik : ActiveSubScreen()
  data object Esma : ActiveSubScreen()
  data object Calculators : ActiveSubScreen()
  data object Imsakiye : ActiveSubScreen()
  data object Backup : ActiveSubScreen()
}

data class NavTabItem(
  val title: String,
  val icon: ImageVector? = null,
  val iconRes: Int? = null,
  val testTag: String
)

class MainActivity : ComponentActivity() {

  override fun onResume() {
    super.onResume()
    // Abonelik iptal/yenileme durumunu her dönüşte Google Play'den doğrula
    com.example.billing.PremiumManager.refresh()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AppHolder.context = applicationContext
    // Kur'an metnini (6236 ayet) arka planda yükle
    Thread {
      // Açılışı yavaşlatmasın: düşük öncelikli ve biraz gecikmeli yükle
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
      try { Thread.sleep(2500) } catch (_: InterruptedException) { }
      com.example.data.QuranText.preload(applicationContext)
    }.start()
    com.example.util.CrashReporter.install(this)
    val lastCrash = com.example.util.CrashReporter.consumeLastCrash(this)
    enableEdgeToEdge()

    // Premium üyelik (Google Play Faturalandırma): durum doğrulanır, ürün fiyatları alınır
    com.example.billing.PremiumManager.init(this)

    // Reklam onayı (UMP) → izin varsa AdMob arka planda başlatılır
    com.example.util.AdsConsent.gather(this)

    // Create Notification Channel for prayers and iftar
    NotificationHelper.createNotificationChannel(this)

    // Initialize Firebase and subscribe to all_users topic for push campaigns
    try {
      com.google.firebase.FirebaseApp.initializeApp(this)
      com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("all_users")
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            if (com.example.BuildConfig.DEBUG) android.util.Log.d("FCM", "Subscribed to all_users topic")
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
  var showWidgetPicker by remember { mutableStateOf(false) }
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
      val result = prayerRepo.getPrayerTimes(
        city = prefs.province, district = prefs.district, country = prefs.country,
        lat = if (prefs.useCoordinates) prefs.latitude else null,
        lon = if (prefs.useCoordinates) prefs.longitude else null
      )
      prayerTimes = result
      ReminderScheduler.onPrayerTimesLoaded(AppHolder.context, result)
      com.example.util.WearSync.send(AppHolder.context, result, prefs)
    }
  }

  LaunchedEffect(prefs.country, prefs.province, prefs.district) {
    loadPrayers()
  }

  // GPS ile otomatik konum: uygulama her öne geldiğinde il/ilçe kontrol edilir, değiştiyse vakitler güncellenir
  val autoContext = androidx.compose.ui.platform.LocalContext.current
  fun runAutoLocation() {
    if (!prefs.autoLocation || !com.example.util.AutoLocation.hasPermission(autoContext)) return
    coroutineScope.launch {
      val d = com.example.util.AutoLocation.detect(autoContext) ?: return@launch
      val changed = d.province != prefs.province || d.district != prefs.district || d.country != prefs.country ||
        prefs.useCoordinates != !d.matchedTurkey
      prefs.latitude = d.latitude
      prefs.longitude = d.longitude
      if (changed) {
        prefs.country = d.country
        prefs.province = d.province
        prefs.district = d.district
        prefs.useCoordinates = !d.matchedTurkey
        loadPrayers()
        android.widget.Toast.makeText(
          autoContext,
          (if (com.example.i18n.AppLanguage.isEnglish) "Location updated: " else "Konum güncellendi: ") + "${d.province} / ${d.district}",
          android.widget.Toast.LENGTH_SHORT
        ).show()
      }
    }
  }
  val autoLocPermission = androidx.activity.compose.rememberLauncherForActivityResult(
    androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
  ) { granted -> if (granted.values.any { it }) runAutoLocation() }
  LaunchedEffect(Unit) {
    if (prefs.autoLocation && !com.example.util.AutoLocation.hasPermission(autoContext) && !prefs.autoLocationAsked) {
      prefs.autoLocationAsked = true
      autoLocPermission.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION))
    }
  }
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
      if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) runAutoLocation()
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
            onOpenGreetingCards = { activeSubScreen = ActiveSubScreen.GreetingCards },
            zikirRefreshKey = activeSubScreen,
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
            onSelectSurah = { surah: SurahInfo -> activeSubScreen = ActiveSubScreen.QuranReader(com.example.data.QuranText.firstPageOfSurah(surah.number), fromTab = true) },
            onOpenPage = { p -> activeSubScreen = ActiveSubScreen.QuranReader(p, fromTab = true) },
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
            onOpenQuran = { activeSubScreen = ActiveSubScreen.QuranList() },
            onOpenHatim = { activeSubScreen = ActiveSubScreen.QuranList(tab = 1) },
            onOpenKaza = { activeSubScreen = ActiveSubScreen.KazaNamazi },
            onOpenKandil = { activeSubScreen = ActiveSubScreen.KandilDualari },
            onOpenWizard = { activeSubScreen = ActiveSubScreen.NotificationWizard },
            onOpenPremium = { activeSubScreen = ActiveSubScreen.Premium },
            isRamadan = ramadanDay != null,
            onOpenFastingGuide = { activeSubScreen = ActiveSubScreen.FastingGuide },
            onOpenGreetingCards = { activeSubScreen = ActiveSubScreen.GreetingCards },
            onAddWidget = { showWidgetPicker = true },
            onOpenQibla = { activeSubScreen = ActiveSubScreen.Qibla },
            onOpenMosques = { activeSubScreen = ActiveSubScreen.Mosques },
            onOpenFastingTracker = { activeSubScreen = ActiveSubScreen.FastingTracker },
            onOpenNotes = { activeSubScreen = ActiveSubScreen.SpiritualNotes },
            onOpenSettings = { showSettingsDialog = true },
            onOpenZikirmatik = { activeSubScreen = ActiveSubScreen.Zikirmatik },
            onOpenEsma = { activeSubScreen = ActiveSubScreen.Esma },
            onOpenCalculators = { activeSubScreen = ActiveSubScreen.Calculators },
            onOpenImsakiye = { activeSubScreen = ActiveSubScreen.Imsakiye },
            onOpenBackup = { activeSubScreen = ActiveSubScreen.Backup }
          )
        }
      }
    }

    // Active SubScreen Overlay
    activeSubScreen?.let { subScreen ->
      BackHandler {
        if ((subScreen is ActiveSubScreen.SurahDetail && !subScreen.fromTab) || (subScreen is ActiveSubScreen.QuranReader && !subScreen.fromTab)) {
          activeSubScreen = ActiveSubScreen.QuranList()
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
            onSelectSurah = { surah: SurahInfo -> activeSubScreen = ActiveSubScreen.QuranReader(com.example.data.QuranText.firstPageOfSurah(surah.number)) },
            onOpenPage = { p -> activeSubScreen = ActiveSubScreen.QuranReader(p) },
            initialTab = subScreen.tab,
            onNavigateBack = { activeSubScreen = null }
          )
          is ActiveSubScreen.QuranReader -> com.example.ui.screens.QuranReaderScreen(
            startPage = subScreen.page,
            onNavigateBack = { activeSubScreen = if (subScreen.fromTab) null else ActiveSubScreen.QuranList() }
          )
          is ActiveSubScreen.SurahDetail -> SurahDetailScreen(
            surah = subScreen.surah,
            onNavigateBack = { activeSubScreen = if (subScreen.fromTab) null else ActiveSubScreen.QuranList() }
          )
          is ActiveSubScreen.KazaNamazi -> com.example.ui.screens.KazaNamaziScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.KandilDualari -> com.example.ui.screens.KandilDualariScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.Premium -> com.example.ui.screens.PremiumScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.NotificationWizard -> com.example.ui.screens.NotificationWizardScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.GreetingCards -> com.example.ui.screens.GreetingCardsScreen(onNavigateBack = { activeSubScreen = null })
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
                text = t("Ramazan Rehberi"),
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
          is ActiveSubScreen.Calculators -> com.example.ui.screens.CalculatorsScreen(onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.Imsakiye -> com.example.ui.screens.ImsakiyeScreen(prefs = prefs, onNavigateBack = { activeSubScreen = null })
          is ActiveSubScreen.Backup -> {
            val act = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
            com.example.ui.screens.BackupScreen(
              onNavigateBack = { activeSubScreen = null },
              onRestored = {
                ReminderScheduler.rescheduleAll(AppHolder.context)
                act?.recreate()
              }
            )
          }
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
          // Listeden elle seçim: otomatik konum kapanır (Ayarlar'dan tekrar açılabilir)
          prefs.autoLocation = false
          prefs.useCoordinates = false
          prefs.country = country
          prefs.province = city
          prefs.district = district
          loadPrayers()
        },
        onDismiss = { showLocationDialog = false }
      )
    }

    // Settings Dialog
    if (showWidgetPicker) {
      androidx.compose.material3.AlertDialog(
        onDismissRequest = { showWidgetPicker = false },
        title = { Text(t("Ana Ekran Widget'ı")) },
        text = {
          androidx.compose.foundation.layout.Column {
            listOf(
              com.example.widget.WidgetUpdater.Kind.NEXT_PRAYER to t("🕌 Sonraki Namaz & Geri Sayım"),
              com.example.widget.WidgetUpdater.Kind.PRAYER_TIMES to t("🗓️ Günün Vakitleri"),
              com.example.widget.WidgetUpdater.Kind.SPIRITUAL to t("📖 Günün Manevi İklimi"),
              com.example.widget.WidgetUpdater.Kind.FASTING to t("🏮 Oruç & İftar")
            ).forEach { (kind, label) ->
              androidx.compose.material3.TextButton(
                onClick = {
                  showWidgetPicker = false
                  com.example.widget.WidgetUpdater.requestPin(AppHolder.context, kind)
                },
                modifier = Modifier.fillMaxWidth()
              ) { Text(label, modifier = Modifier.fillMaxWidth()) }
            }
          }
        },
        confirmButton = {
          androidx.compose.material3.TextButton(onClick = { showWidgetPicker = false }) { Text(t("Kapat")) }
        }
      )
    }

    if (showSettingsDialog) {
      // Ayarlar hangi yoldan kapanırsa kapansın (kapat, sihirbaz, premium, konum) hatırlatıcıları yeniden kur
      androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { ReminderScheduler.rescheduleAll(AppHolder.context) }
      }
      SettingsDialog(
        prefs = prefs,
        onThemeChanged = onThemeChanged,
        onColorThemeChanged = onColorThemeChanged,
        onLanguageChanged = onLanguageChanged,
        onChangeLocation = {
          showSettingsDialog = false
          showLocationDialog = true
        },
        onOpenWizard = {
          showSettingsDialog = false
          activeSubScreen = ActiveSubScreen.NotificationWizard
        },
        onAutoLocationEnabled = { runAutoLocation() },
        onOpenPremium = {
          showSettingsDialog = false
          activeSubScreen = ActiveSubScreen.Premium
        },
        onDismiss = {
          showSettingsDialog = false
        }
      )
    }
  }
}

object AppHolder {
  lateinit var context: android.content.Context
}

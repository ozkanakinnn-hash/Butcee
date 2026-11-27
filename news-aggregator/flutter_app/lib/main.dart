import 'dart:ui';

import 'package:firebase_app_check/firebase_app_check.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'ads/ads_service.dart';
import 'app_settings.dart';
import 'utils/storage.dart';
import 'l10n/app_strings.dart';
import 'pages/root_shell.dart';
import 'pages/article_webview_page.dart';
import 'pages/briefing_page.dart';
import 'pages/quakes_page.dart';
import 'pages/interests_page.dart';
import 'pages/match_detail_page.dart';
import 'services/library_store.dart';
import 'services/news_widget.dart';
import 'services/notification_service.dart';
import 'services/team_follow_sync.dart';
import 'theme.dart';

/// Çökme raporları (Firebase Crashlytics): yalnızca sürüm derlemelerinde gönderilir.
void _setupCrashReporting() {
  final crashlytics = FirebaseCrashlytics.instance;
  crashlytics.setCrashlyticsCollectionEnabled(kReleaseMode);
  FlutterError.onError = crashlytics.recordFlutterFatalError;
  PlatformDispatcher.instance.onError = (error, stack) {
    crashlytics.recordError(error, stack, fatal: true);
    return true;
  };
}

/// iOS Firebase seçenekleri (yalnızca iOS'ta ve değerler verilmişse); aksi halde null → plist/json kullanılır.
FirebaseOptions? _iosOptions() {
  if (defaultTargetPlatform != TargetPlatform.iOS) return null;
  const apiKey = String.fromEnvironment('FIREBASE_IOS_API_KEY');
  const appId = String.fromEnvironment('FIREBASE_IOS_APP_ID');
  const senderId = String.fromEnvironment('FIREBASE_IOS_SENDER_ID');
  const projectId = String.fromEnvironment('FIREBASE_IOS_PROJECT_ID', defaultValue: 'haber-68a3c');
  if (apiKey.isEmpty || appId.isEmpty || senderId.isEmpty) return null;
  return const FirebaseOptions(
    apiKey: apiKey,
    appId: appId,
    messagingSenderId: senderId,
    projectId: projectId,
    storageBucket: String.fromEnvironment('FIREBASE_IOS_STORAGE_BUCKET'),
    iosBundleId: 'akinai.app1',
  );
}

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final settings = await AppSettings.load();
  await LibraryStore.load(); // kaydedilenler, okuma geçmişi, gizlenen kaynaklar (cihazda)

  var firebaseReady = true;
  try {
    // Android: android/app/google-services.json. iOS: GoogleService-Info.plist varsa oradan, yoksa
    // derleme anında verilen --dart-define=FIREBASE_IOS_* değerlerinden (depo herkese açık; anahtar commit edilmez).
    await Firebase.initializeApp(options: _iosOptions());
    AppStorage.configure(); // Firestore ve görsel önbellek sınırları
    _setupCrashReporting();
    // App Check: Android'de Play Integrity, iOS'ta App Attest (yedek: DeviceCheck). Konsolda "zorunlu" yapılana
    // kadar yalnızca ölçülür; zorunlu kılma YALNIZCA mağaza dağıtımı başladığında açılmalı.
    FirebaseAppCheck.instance
        .activate(
          providerAndroid: kReleaseMode ? const AndroidPlayIntegrityProvider() : const AndroidDebugProvider(),
          providerApple: kReleaseMode ? const AppleAppAttestWithDeviceCheckFallbackProvider() : const AppleDebugProvider(),
        )
        .catchError((Object e) => debugPrint('App Check başlatılamadı: $e'));
  } catch (e) {
    debugPrint('Firebase başlatılamadı: $e');
    firebaseReady = false;
  }

  runApp(NewsApp(settings: settings, firebaseReady: firebaseReady));

  if (firebaseReady) {
    // Bildirim kanalları, dokunma yönlendirmesi ve konu abonelikleri (hata uygulamayı durdurmaz).
    NotificationService.instance.taps.listen(_openTarget);
    NotificationService.instance.init(settings).catchError((Object e) => debugPrint('Bildirim başlatılamadı: $e'));
    // Takip edilen takımlar → isimsiz sayaç (sunucu yalnızca bu takımların maçlarına canlı skor isteği harcar)
    TeamFollowSync(settings).start();
    // Android ana ekran widget'ı: açılışta ve uygulama her öne geldiğinde gündem başlıkları
    NewsWidget.refresh();
    AppLifecycleListener(onResume: NewsWidget.refresh);
  }
  // Reklamlar: önce onay (UMP), sonra AdMob. İlk kare çizildikten sonra; hata uygulamayı durdurmaz.
  // NO_ADS: yalnızca App Review ekran kaydı derlemesi (otomatik test, onay penceresine dokunamaz).
  if (!const bool.fromEnvironment('NO_ADS')) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      AdsService.instance.init().catchError((Object e) => debugPrint('Reklamlar başlatılamadı: $e'));
    });
  }
}

/// Bildirime dokunulunca ilgili sayfayı aç.
final navigatorKey = GlobalKey<NavigatorState>();

void _openTarget(NotificationTarget target) {
  final nav = navigatorKey.currentState;
  if (nav == null) return;
  switch (target) {
    case ArticleTarget(:final url, :final title, :final source):
      nav.push(ArticleWebViewPage.route(url: url, sourceName: source, articleTitle: title));
    case MatchTarget(:final fixtureId):
      nav.push(MaterialPageRoute<void>(builder: (_) => MatchDetailPage(fixtureId: fixtureId)));
    case BriefingTarget(:final id):
      nav.push(BriefingPage.route(id: id));
    case QuakesTarget():
      nav.push(QuakesPage.route());
  }
}

class NewsApp extends StatelessWidget {
  const NewsApp({super.key, required this.settings, this.firebaseReady = true});

  final AppSettings settings;
  final bool firebaseReady;

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: settings,
      builder: (context, _) => MaterialApp(
        navigatorKey: navigatorKey,
        title: 'Haber Akışı',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.light(),
        darkTheme: AppTheme.dark(),
        themeMode: settings.themeMode,
        locale: settings.locale,
        supportedLocales: AppSettings.supportedLocales,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: !firebaseReady
            ? const _SetupRequiredPage()
            : settings.hasChosenInterests
            ? RootShell(settings: settings)
            // İlk açılış: ilgi alanı seçimi; "Devam et" kaydedince ana ekran açılır.
            : InterestsPage(
                settings: settings,
                onboarding: true,
                // Kurulumda bildirim izni de sorulur (Android 13+ sistem penceresi)
                requestNotifications: firebaseReady ? () => NotificationService.instance.requestPermission() : null,
              ),
      ),
    );
  }
}

/// google-services.json eklenmeden derlenirse çökmek yerine yönlendirme gösterir.
class _SetupRequiredPage extends StatelessWidget {
  const _SetupRequiredPage();

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.cloud_off, size: 56),
              const SizedBox(height: 16),
              Text(s.setupRequired, style: Theme.of(context).textTheme.titleMedium, textAlign: TextAlign.center),
              const SizedBox(height: 8),
              Text(s.setupRequiredDesc, textAlign: TextAlign.center),
            ],
          ),
        ),
      ),
    );
  }
}

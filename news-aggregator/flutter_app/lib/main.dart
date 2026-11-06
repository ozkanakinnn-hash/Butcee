import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'app_settings.dart';
import 'l10n/app_strings.dart';
import 'pages/home_page.dart';
import 'pages/interests_page.dart';
import 'theme.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final settings = await AppSettings.load();

  var firebaseReady = true;
  try {
    // android/app/google-services.json varsa seçenekler oradan okunur.
    await Firebase.initializeApp();
  } catch (e) {
    debugPrint('Firebase başlatılamadı: $e');
    firebaseReady = false;
  }

  runApp(NewsApp(settings: settings, firebaseReady: firebaseReady));
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
            ? HomePage(settings: settings)
            // İlk açılış: ilgi alanı seçimi; "Devam et" kaydedince ana ekran açılır.
            : InterestsPage(settings: settings, onboarding: true),
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

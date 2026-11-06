import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'app_settings.dart';
import 'l10n/app_strings.dart';
import 'pages/home_page.dart';

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

  static ThemeData _theme(Brightness brightness) => ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFFC62828), brightness: brightness),
        appBarTheme: const AppBarTheme(scrolledUnderElevation: 0),
      );

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: settings,
      builder: (context, _) => MaterialApp(
        title: 'Haber Akışı',
        debugShowCheckedModeBanner: false,
        theme: _theme(Brightness.light),
        darkTheme: _theme(Brightness.dark),
        locale: settings.locale,
        supportedLocales: AppSettings.supportedLocales,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: firebaseReady ? HomePage(settings: settings) : const _SetupRequiredPage(),
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

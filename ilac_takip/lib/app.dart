import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'bootstrap.dart';
import 'l10n/app_localizations.dart';
import 'state/providers.dart';
import 'ui/alarm/alarm_screen.dart';
import 'ui/home/home_screen.dart';
import 'ui/medicine/add_medicine_screen.dart';
import 'ui/medicine/medicines_screen.dart';
import 'ui/scan/scan_screen.dart';
import 'ui/senior/senior_home_screen.dart';
import 'ui/settings/reliability_screen.dart';
import 'ui/settings/settings_screen.dart';
import 'ui/theme.dart';

GoRouter buildRouter({String initialLocation = '/'}) => GoRouter(
      initialLocation: initialLocation,
      routes: [
        GoRoute(path: '/', builder: (_, _) => const _HomeSwitch()),
        GoRoute(path: '/alarm/:logId', builder: (_, s) => AlarmScreen(logId: s.pathParameters['logId']!)),
        GoRoute(path: '/scan', builder: (_, _) => const ScanScreen()),
        GoRoute(path: '/medicines', builder: (_, _) => const MedicinesScreen()),
        GoRoute(
          path: '/medicines/add',
          builder: (_, s) => AddMedicineScreen(args: s.extra as AddMedicineArgs? ?? const AddMedicineArgs()),
        ),
        GoRoute(path: '/settings', builder: (_, _) => const SettingsScreen()),
        GoRoute(path: '/reliability', builder: (_, _) => const ReliabilityScreen()),
      ],
    );

class _HomeSwitch extends ConsumerWidget {
  const _HomeSwitch();

  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      ref.watch(settingsProvider.select((s) => s.seniorMode)) ? const SeniorHomeScreen() : const HomeScreen();
}

class IlacTakipApp extends ConsumerStatefulWidget {
  const IlacTakipApp({super.key, required this.router});
  final GoRouter router;

  @override
  ConsumerState<IlacTakipApp> createState() => _IlacTakipAppState();
}

class _IlacTakipAppState extends ConsumerState<IlacTakipApp> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    AppServices.foregroundHandler = (r) {
      final logId = r.payload;
      if (r.actionId == null) {
        if (logId != null && logId != 'test') widget.router.push('/alarm/$logId');
      } else {
        ref.read(servicesProvider).actions.handleNotificationAction(r.actionId, logId);
      }
    };
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    AppServices.foregroundHandler = null;
    super.dispose();
  }

  /// Ön plana gelince: arka plan izolatının yaptığı değişiklikleri al, alarmları uzlaştır.
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      ref.read(servicesProvider).planner.reconcile();
    }
  }

  @override
  Widget build(BuildContext context) {
    final settings = ref.watch(settingsProvider);
    final senior = settings.seniorMode;
    return MaterialApp.router(
      onGenerateTitle: (c) => AppLocalizations.of(c).appTitle,
      debugShowCheckedModeBanner: false,
      routerConfig: widget.router,
      locale: settings.languageCode == null ? null : Locale(settings.languageCode!),
      supportedLocales: AppLocalizations.supportedLocales,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: senior ? seniorTheme(Brightness.light) : normalTheme(Brightness.light),
      darkTheme: senior ? seniorTheme(Brightness.dark) : normalTheme(Brightness.dark),
      builder: (context, child) {
        if (!senior) return child!;
        final mq = MediaQuery.of(context);
        final system = mq.textScaler.scale(1);
        return MediaQuery(
          data: mq.copyWith(textScaler: TextScaler.linear((system * settings.textScale).clamp(1.0, 2.2))),
          child: child!,
        );
      },
    );
  }
}

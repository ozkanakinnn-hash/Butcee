import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'bootstrap.dart';
import 'l10n/app_localizations.dart';
import 'state/providers.dart';
import 'services/crash_reporter.dart';
import 'ui/alarm/alarm_screen.dart';
import 'ui/learn/learn_screen.dart';
import 'ui/panel/interactions_screen.dart';
import 'ui/panel/panel_screen.dart';
import 'ui/settings/reliability_screen.dart';
import 'ui/settings/reminders_screen.dart';
import 'ui/settings/settings_screen.dart';
import 'ui/shell/main_shell.dart';
import 'ui/stats/stats_screen.dart';
import 'ui/supplements/add_supplement_screen.dart';
import 'ui/supplements/catalog_picker_screen.dart';
import 'ui/supplements/presets_screen.dart';
import 'ui/supplements/supplement_detail_screen.dart';
import 'ui/supplements/supplements_screen.dart';
import 'ui/theme.dart';
import 'ui/today/today_screen.dart';
import 'ui/widgets/common.dart';

final rootNavigatorKey = GlobalKey<NavigatorState>();

GoRouter buildRouter({String initialLocation = '/'}) => GoRouter(
      navigatorKey: rootNavigatorKey,
      initialLocation: initialLocation,
      routes: [
        StatefulShellRoute.indexedStack(
          builder: (_, _, shell) => MainShell(shell: shell),
          branches: [
            StatefulShellBranch(routes: [GoRoute(path: '/', builder: (_, _) => const TodayScreen())]),
            StatefulShellBranch(routes: [GoRoute(path: '/supplements', builder: (_, _) => const SupplementsScreen())]),
            StatefulShellBranch(routes: [GoRoute(path: '/panel', builder: (_, _) => const PanelScreen())]),
            StatefulShellBranch(routes: [GoRoute(path: '/stats', builder: (_, _) => const StatsScreen())]),
            StatefulShellBranch(routes: [GoRoute(path: '/learn', builder: (_, _) => const LearnScreen())]),
          ],
        ),
        GoRoute(path: '/alarm/:logId', builder: (_, s) => AlarmScreen(logId: s.pathParameters['logId']!)),
        GoRoute(path: '/catalog', builder: (_, _) => const CatalogPickerScreen()),
        GoRoute(
          path: '/supplements/add',
          builder: (_, s) => AddSupplementScreen(args: s.extra as AddSupplementArgs? ?? const AddSupplementArgs()),
        ),
        GoRoute(
          path: '/supplement/:id',
          builder: (_, s) => SupplementDetailScreen(medicineId: int.parse(s.pathParameters['id']!)),
        ),
        GoRoute(path: '/presets', builder: (_, _) => const PresetsScreen()),
        GoRoute(path: '/interactions', builder: (_, _) => const InteractionsScreen()),
        GoRoute(path: '/reminders', builder: (_, _) => const RemindersScreen()),
        GoRoute(path: '/settings', builder: (_, _) => const SettingsScreen()),
        GoRoute(path: '/reliability', builder: (_, _) => const ReliabilityScreen()),
      ],
    );

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
    WidgetsBinding.instance.addPostFrameCallback((_) => _showPendingCrashReport());
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

  /// Önceki oturumda kaydedilmiş bir hata varsa kullanıcıya kopyalama seçeneğiyle göster.
  Future<void> _showPendingCrashReport() async {
    final report = await CrashReporter.pendingReport();
    final ctx = rootNavigatorKey.currentContext;
    if (report == null || ctx == null || !ctx.mounted) return;
    await showDialog<void>(
      context: ctx,
      builder: (dctx) {
        final t = dctx.l10n;
        return AlertDialog(
          icon: const Icon(Icons.bug_report_outlined),
          title: Text(t.crashTitle),
          content: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(t.crashBody),
            const SizedBox(height: 8),
            SizedBox(
              height: 140,
              child: SingleChildScrollView(child: SelectableText(report, style: const TextStyle(fontSize: 11, fontFamily: 'monospace'))),
            ),
          ]),
          actions: [
            TextButton(onPressed: () => Navigator.pop(dctx), child: Text(t.dismiss)),
            FilledButton.icon(
              icon: const Icon(Icons.copy),
              label: Text(t.copyReport),
              onPressed: () async {
                await Clipboard.setData(ClipboardData(text: report));
                if (dctx.mounted) Navigator.pop(dctx);
              },
            ),
          ],
        );
      },
    );
    await CrashReporter.clear();
  }

  /// Ön plana gelince: arka plan izolatının yaptığı değişiklikleri al, alarmları uzlaştır.
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      ref.read(servicesProvider).planner.reconcile().catchError((Object e, StackTrace st) {
        CrashReporter.record(e, st, context: 'reconcile(resumed)');
        return 0;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final settings = ref.watch(settingsProvider);
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
      theme: normalTheme(Brightness.light),
      darkTheme: normalTheme(Brightness.dark),
    );
  }
}

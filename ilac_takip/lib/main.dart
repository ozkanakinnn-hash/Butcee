import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'bootstrap.dart';
import 'services/crash_reporter.dart';
import 'state/providers.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await CrashReporter.install();

  final AppServices services;
  try {
    services = await AppServices.create();
  } on Object catch (e, st) {
    // Açılış hatası beyaz ekranda takılmak yerine okunabilir bir hata ekranı gösterir.
    CrashReporter.record(e, st, context: 'startup');
    runApp(StartupErrorApp(error: '$e\n\n$st'));
    return;
  }
  await startApp(services);
}

/// Servisler kurulduktan sonra uygulamayı başlatır (entegrasyon testleri de bunu kullanır).
Future<void> startApp(AppServices services) async {
  try {
    await services.catalog.seedFromAsset(rootBundle);
  } on Object catch (e, st) {
    CrashReporter.record(e, st, context: 'catalog seed');
  }

  // Uygulama bir alarm bildirimine dokunularak açıldıysa doğrudan alarm ekranı.
  String? payload;
  try {
    final launch = await services.scheduler.plugin.getNotificationAppLaunchDetails();
    if (launch?.didNotificationLaunchApp ?? false) payload = launch?.notificationResponse?.payload;
  } on Object catch (e, st) {
    CrashReporter.record(e, st, context: 'launch details');
  }

  // Alarm kurulamaması (ör. tam zamanlı alarm izni yok) açılışı ASLA engellememeli.
  try {
    await services.planner.reconcile();
  } on Object catch (e, st) {
    CrashReporter.record(e, st, context: 'reconcile(startup)');
  }

  final openAlarm = payload != null && payload != 'test';
  runApp(ProviderScope(
    overrides: [servicesProvider.overrideWithValue(services)],
    child: IlacTakipApp(router: buildRouter(initialLocation: openAlarm ? '/alarm/$payload' : '/')),
  ));
}

class StartupErrorApp extends StatelessWidget {
  const StartupErrorApp({super.key, required this.error});
  final String error;

  @override
  Widget build(BuildContext context) => MaterialApp(
        home: Scaffold(
          body: SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                const Icon(Icons.error_outline, size: 48),
                const SizedBox(height: 12),
                const Text('Uygulama başlatılamadı / The app could not start',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
                const SizedBox(height: 12),
                Expanded(child: SingleChildScrollView(child: SelectableText(error, style: const TextStyle(fontSize: 11)))),
                FilledButton.icon(
                  icon: const Icon(Icons.copy),
                  label: const Text('Raporu kopyala / Copy report'),
                  onPressed: () => Clipboard.setData(ClipboardData(text: error)),
                ),
              ]),
            ),
          ),
        ),
      );
}

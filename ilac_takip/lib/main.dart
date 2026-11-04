import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'bootstrap.dart';
import 'state/providers.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final services = await AppServices.create();
  await services.catalog.seedFromAsset(rootBundle);

  // Uygulama bir alarm bildirimine dokunularak açıldıysa doğrudan alarm ekranı.
  final launch = await services.scheduler.plugin.getNotificationAppLaunchDetails();
  final payload = launch?.notificationResponse?.payload;
  final openedFromAlarm = (launch?.didNotificationLaunchApp ?? false) && payload != null && payload != 'test';

  await services.planner.reconcile();

  runApp(ProviderScope(
    overrides: [servicesProvider.overrideWithValue(services)],
    child: IlacTakipApp(router: buildRouter(initialLocation: openedFromAlarm ? '/alarm/$payload' : '/')),
  ));
}

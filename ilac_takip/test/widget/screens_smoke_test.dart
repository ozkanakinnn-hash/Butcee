import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/app.dart';
import 'package:ilac_takip/bootstrap.dart';
import 'package:ilac_takip/data/app_database.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/dose_action_service.dart';
import 'package:ilac_takip/services/settings_store.dart';
import 'package:ilac_takip/state/providers.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../services/fakes.dart';

/// Ekranların gerçek verilerle (bekleyen, ertelenen, kaçırılan dozlar) hatasız çizildiğini doğrular.
void main() {
  late AppDatabase db;
  late AppServices services;

  Future<void> seed(WidgetTester tester, {required bool senior, String lang = 'tr'}) async {
    await tester.runAsync(() async {
      SharedPreferences.setMockInitialValues({
        'seniorMode': senior,
        'languageCode': lang,
        'caregiverPhone': '+900000000000',
      });
      db = await openTestDb();
      final now = DateTime.now();
      final clock = TestClock(now);
      final alarms = FakeAlarmScheduler();
      final planner = AlarmPlanner(db: db, scheduler: alarms, texts: FakeTexts(), clock: clock.call);
      final actions = DoseActionService(db: db, scheduler: alarms, planner: planner, clock: clock.call);
      services = AppServices.forTest(
        db: db,
        settings: await SettingsStore.load(),
        planner: planner,
        actions: actions,
      );
      final medId = await db.insertMedicine(const Medicine(
        name: 'Coraspin 100 mg',
        nickname: 'Beyaz küçük hap — kalp ilacı',
        totalStock: 3,
        criticalStockLevel: 5,
      ));
      final times = [
        now.subtract(const Duration(hours: 3)),
        now.subtract(const Duration(minutes: 20)),
        now.add(const Duration(hours: 2)),
        now.add(const Duration(hours: 4)),
      ].map((t) => (h: t.hour, m: t.minute)).toSet().toList();
      await db.upsertSchedule(Schedule(
        id: 'sch_1',
        medicineId: medId,
        rule: FixedTimesRule(
          startDate: now.subtract(const Duration(days: 1)),
          times: times,
          meal: MealRelation.afterMeal,
        ),
      ));
      await planner.reconcile();
      final today = await db.dosesBetween(dateOnly(now), now);
      if (today.length >= 2) await actions.snooze(today.last.logId);
    });
  }

  Future<void> pumpApp(WidgetTester tester, {String location = '/'}) async {
    tester.view.physicalSize = const Size(1080, 2340);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(ProviderScope(
      overrides: [servicesProvider.overrideWithValue(services)],
      child: IlacTakipApp(router: buildRouter(initialLocation: location)),
    ));
    // DB okumaları gerçek G/Ç olduğu için runAsync içinde beklenir.
    for (var i = 0; i < 5; i++) {
      await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
      await tester.pump();
    }
  }

  tearDown(() async => db.close());

  testWidgets('normal ana ekran: bekleyenler paneli ve bugünkü liste', (tester) async {
    await seed(tester, senior: false);
    await pumpApp(tester);
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
    expect(find.text('İlaç Takip'), findsOneWidget);
    expect(find.text('Bugün'), findsOneWidget);
    expect(find.textContaining('Beyaz küçük hap'), findsWidgets);
  });

  testWidgets('Basit Mod ana ekranı (İngilizce)', (tester) async {
    await seed(tester, senior: true, lang: 'en');
    await pumpApp(tester);
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
    await tester.dragUntilVisible(find.text('NEXT'), find.byType(Scrollable).first, const Offset(0, -300));
    expect(find.text('NEXT'), findsOneWidget);
    await tester.dragUntilVisible(
        find.text('📞 CALL MY CAREGIVER'), find.byType(Scrollable).first, const Offset(0, -300));
    expect(find.text('📞 CALL MY CAREGIVER'), findsOneWidget);
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
  });

  testWidgets('ilaç ekleme formu', (tester) async {
    await seed(tester, senior: false);
    await pumpApp(tester, location: '/medicines/add');
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
    expect(find.text('Ne sıklıkla?'), findsOneWidget);
    await tester.tap(find.text('Saat aralığıyla'));
    await tester.pump();
    expect(find.text('İlk doz saati'), findsOneWidget);
  });

  testWidgets('ilaçlarım ve ayarlar ekranları', (tester) async {
    await seed(tester, senior: false);
    await pumpApp(tester, location: '/medicines');
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
    expect(find.text('Stok azaldı'), findsOneWidget);

    await pumpApp(tester, location: '/settings');
    { final e = tester.takeException(); expect(e, isNull, reason: "$e"); }
    expect(find.text('Basit Mod'), findsOneWidget);
  });
}

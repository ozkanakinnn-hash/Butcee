import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/app.dart';
import 'package:ilac_takip/bootstrap.dart';
import 'package:ilac_takip/data/app_database.dart';
import 'package:ilac_takip/data/supplement_catalog.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/dose_action_service.dart';
import 'package:ilac_takip/services/settings_store.dart';
import 'package:ilac_takip/state/providers.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../services/fakes.dart';

/// Tüm ekranların gerçek verilerle (bekleyen, ertelenen, çakışan takviyeler) 360×780 dp'de
/// hatasız ve taşmadan çizildiğini doğrular.
void main() {
  late AppDatabase db;
  late AppServices services;

  Future<void> seed(WidgetTester tester, {String lang = 'tr'}) async {
    await tester.runAsync(() async {
      SharedPreferences.setMockInitialValues({'languageCode': lang});
      db = await openTestDb();
      final now = DateTime.now();
      final clock = TestClock(now);
      final alarms = FakeAlarmScheduler();
      final settings = await SettingsStore.load();
      final planner = AlarmPlanner(db: db, scheduler: alarms, texts: FakeTexts(), clock: clock.call);
      final actions = DoseActionService(db: db, scheduler: alarms, planner: planner, clock: clock.call);
      services = AppServices.forTest(db: db, settings: settings, planner: planner, actions: actions);
      final s = settings.read();
      await services.supplements.applyPreset(supplementPresets.first, s, lang);
      await services.supplements.applyPreset(supplementPresets.last, s, lang); // demir geç sabahta
      // Kalsiyum'u demirle aynı dilime koy → etkileşim uyarısı.
      final ca = catalogById('calcium')!;
      await services.supplements.addSupplement(
        medicine: Medicine(name: ca.name.of(lang), category: ca.category, catalogId: 'calcium', doseAmount: 500, doseUnit: 'mg', totalStock: 3, criticalStockLevel: 5),
        rule: FixedTimesRule(startDate: now.subtract(const Duration(days: 2)), times: [(h: 9, m: 0)], meal: MealRelation.withMeal),
        slots: ['lateMorning'],
        initialStock: 3,
      );
      // Gerektikçe bir takviye.
      await services.supplements.addSupplement(
        medicine: const Medicine(name: 'Probiyotik', category: ItemCategory.probiotic, catalogId: 'probiotic'),
        rule: AsNeededRule(startDate: now, maxPerDay: 2, minIntervalHours: 4),
      );
      await planner.reconcile();
      final today = await db.dosesBetween(dateOnly(now), now);
      if (today.isNotEmpty) await actions.take(today.first.logId);
      if (today.length > 1) await actions.snooze(today.last.logId);
      await db.setMood(now, 4);
    });
  }

  Future<void> pumpApp(WidgetTester tester, {String location = '/'}) async {
    tester.view.physicalSize = const Size(1080, 2340);
    tester.view.devicePixelRatio = 3;
    addTearDown(tester.view.reset);
    await tester.pumpWidget(ProviderScope(
      overrides: [servicesProvider.overrideWithValue(services)],
      child: IlacTakipApp(key: UniqueKey(), router: buildRouter(initialLocation: location)),
    ));
    await settle(tester);
  }

  void expectNoError(WidgetTester tester) {
    final e = tester.takeException();
    expect(e, isNull, reason: '$e');
  }

  tearDown(() async => db.close());

  testWidgets('Bugün: halka, etkileşim uyarısı, dilim grupları, gerektikçe, ruh hâli', (tester) async {
    await seed(tester);
    await pumpApp(tester);
    expectNoError(tester);
    expect(find.byKey(const Key('todayRing')), findsOneWidget);
    expect(find.byKey(const Key('conflictCard')), findsOneWidget);
    await tester.dragUntilVisible(find.text('GEÇ SABAH'), find.byType(Scrollable).first, const Offset(0, -300));
    expect(find.textContaining('YEMEKLE BİRLİKTE'), findsWidgets, reason: 'Türkçe büyük İ');
    await tester.dragUntilVisible(find.text('Bugün nasıl hissediyorsun?'), find.byType(Scrollable).first, const Offset(0, -400));
    expectNoError(tester);
  });

  testWidgets('sekmeler: Takviyeler, Panel, İstatistik, Öğren', (tester) async {
    await seed(tester);
    await pumpApp(tester);
    for (final (tab, expected) in [
      ('Takviyeler', 'Takviyelerim'),
      ('Panel', 'Tüm araçlar tek yerde'),
      ('İstatistik', 'Denge puanı'),
      ('Öğren', 'RDA, AI ve UL farkı'),
    ]) {
      await tester.tap(find.descendant(of: find.byType(NavigationBar), matching: find.text(tab)));
      await settle(tester);
      expectNoError(tester);
      expect(find.text(expected), findsWidgets, reason: tab);
    }
    // Öğren alt sekmeleri
    await tester.tap(find.text('Semptomlar'));
    await settle(tester);
    await tester.tap(find.byType(Checkbox).first);
    await settle(tester);
    expectNoError(tester);
    await tester.tap(find.text('Emilim'));
    await settle(tester);
    await tester.tap(find.text('Azaltır').first);
    await settle(tester);
    expectNoError(tester);
    expect(find.text('Kahve / çay + demir'), findsOneWidget);
  });

  testWidgets('sekmeden yeni sayfa açma (Hero etiket çakışması olmamalı)', (tester) async {
    await seed(tester);
    await pumpApp(tester);
    await tester.tap(find.descendant(of: find.byType(NavigationBar), matching: find.text('Takviyeler')));
    await settle(tester);
    await tester.tap(find.text('Demir').first);
    await settle(tester);
    expectNoError(tester);
    expect(find.text('REFERANS DEĞERLER'), findsOneWidget);
  });

  testWidgets('takviye ayrıntısı: referans değerler ve ayrı alın listesi', (tester) async {
    await seed(tester);
    final plans = await tester.runAsync(() => services.supplements.plans());
    final iron = plans!.firstWhere((p) => p.medicine.catalogId == 'iron');
    await pumpApp(tester, location: '/supplement/${iron.medicine.id}');
    expectNoError(tester);
    expect(find.textContaining('RDA'), findsWidgets);
    await tester.dragUntilVisible(find.text('AYRI ALIN'), find.byType(Scrollable).first, const Offset(0, -300));
    expectNoError(tester);
  });

  testWidgets('katalog → ekleme formu: sıklık seçenekleri ve çakışma uyarısı', (tester) async {
    await seed(tester);
    await pumpApp(tester, location: '/catalog');
    expectNoError(tester);
    await tester.enterText(find.byType(TextField).first, 'çinko');
    await settle(tester);
    await tester.tap(find.text('Çinko'));
    await settle(tester);
    expectNoError(tester);
    expect(find.text('Ne sıklıkta?'), findsOneWidget);
    final cycle = find.text('Kullanım / ara döngüsü');
    await tester.scrollUntilVisible(cycle, 150, scrollable: find.byType(Scrollable).last);
    await tester.pump();
    await tester.tap(cycle);
    await settle(tester);
    expectNoError(tester);
    expect(find.text('Kullanım (gün)'), findsOneWidget);
  });

  testWidgets('hazır setler, etkileşimler, hatırlatıcılar, ayarlar, güvenilirlik', (tester) async {
    await seed(tester);
    for (final (loc, expected) in [
      ('/presets', 'Kemik sağlığı'),
      ('/interactions', 'Etkileşim kontrolü'),
      ('/reminders', 'Hatırlatıcıları etkinleştir'),
      ('/settings', 'Alarm güvenilirliği'),
    ]) {
      await pumpApp(tester, location: loc);
      expectNoError(tester);
      expect(find.text(expected), findsWidgets, reason: loc);
    }
  });

  testWidgets('İngilizce arayüz', (tester) async {
    await seed(tester, lang: 'en');
    await pumpApp(tester);
    expectNoError(tester);
    expect(find.text('Today'), findsWidgets);
    expect(find.text('Supplements'), findsOneWidget);
  });
}

/// DB okumaları gerçek G/Ç olduğu için runAsync içinde beklenir.
Future<void> settle(WidgetTester tester) async {
  for (var i = 0; i < 6; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 40)));
    await tester.pump(const Duration(milliseconds: 100));
  }
}

import 'dart:io';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/bootstrap.dart';
import 'package:ilac_takip/data/supplement_catalog.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/main.dart' as app;
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/settings_store.dart';
import 'package:ilac_takip/services/supplement_service.dart';
import 'package:integration_test/integration_test.dart';

/// Gerçek cihaz/emülatörde (Android 14: tam zamanlı alarm izni varsayılan KAPALI):
/// 1) Servisler kurulur, takviyeler eklenir ve alarmlar kurulur — çökme olmamalı.
/// 2) Uygulama açılır, sekmeler gezilir ve ekran görüntüleri alınır.
void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  Future<void> pumpFor(WidgetTester tester, [int ms = 1500]) async {
    for (var i = 0; i < ms ~/ 100; i++) {
      await tester.pump(const Duration(milliseconds: 100));
    }
  }

  Future<void> shot(WidgetTester tester, String name) async {
    await pumpFor(tester, 800);
    final e = tester.takeException();
    if (e != null) debugPrint('ADIM $name HATA: ${e.toString().split('\n').take(3).join(' | ')}');
    await binding.takeScreenshot(name);
    debugPrint('ADIM $name tamam');
  }

  testWidgets('açılış, takviye ekleme ve alarm kurma çökmeden çalışır; ekran görüntüleri', (tester) async {
    // Emülatörün sistem dili İngilizce; ekran görüntüleri Türkçe arayüzden alınır.
    final store = await SettingsStore.load();
    await store.write(store.read().copyWith(languageCode: () => 'tr'));
    final services = await AppServices.create();
    final exact = await services.scheduler.canScheduleExact();
    debugPrint('Tam zamanlı alarm izni: $exact (Android 14 yeni kurulumda false beklenir)');

    // ── Örnek veri: iki hazır set + demirle aynı saatte kalsiyum (etkileşim uyarısı için).
    final settings = services.settings.read();
    await services.supplements.applyPreset(supplementPresets.firstWhere((p) => p.id == 'balanced'), settings, 'tr');
    await services.supplements.applyPreset(supplementPresets.firstWhere((p) => p.id == 'energy'), settings, 'tr');
    final ca = catalogById('calcium')!;
    await services.supplements.addSupplement(
      medicine: Medicine(name: ca.name.tr, category: ca.category, catalogId: ca.id, doseAmount: 500, doseUnit: 'mg', criticalStockLevel: 5),
      rule: SupplementService.ruleFromSlots(slots: const ['lateMorning'], settings: settings, start: dateOnly(DateTime.now()), meal: MealRelation.withMeal),
      slots: const ['lateMorning'],
      initialStock: 4,
    );

    // ── Asıl doğrulama: izin yokken bile reconcile hata fırlatmamalı ve bildirimler kurulmalı.
    await services.planner.reconcile();
    final pending = await services.scheduler.pendingIds();
    debugPrint('Kurulan bildirim sayısı: ${pending.length}, son hata: ${services.planner.lastError}');
    expect(services.planner.lastError, isNull);
    expect(pending, isNotEmpty);

    // ── İstatistikler için geçmiş 14 gün (yaklaşık %85 uyum) ve ruh hâli.
    final rnd = Random(7);
    final now = DateTime.now();
    final start14 = DateTime(now.year, now.month, now.day - 14);
    for (final s in await services.db.allSchedules()) {
      final r = s.rule;
      if (r is! FixedTimesRule) continue;
      await services.db.upsertSchedule(s.copyWith(
          rule: FixedTimesRule(startDate: start14, times: r.times, meal: r.meal, quantity: r.quantity, weekdays: r.weekdays, everyNDays: r.everyNDays)));
    }
    for (final s in await services.db.activeSchedules()) {
      for (final t in s.rule.occurrences(start14, DateTime(now.year, now.month, now.day))) {
        await services.db.insertDoseIfAbsent(DoseLog.planned(s, t, slot: AlarmPlanner.slotFor(s, t)));
        final d = (await services.db.dose(DoseLog.buildLogId(s.id, t)))!;
        final taken = rnd.nextDouble() < 0.86;
        await services.db.updateDose(d.copyWith(
          status: taken ? DoseStatus.taken : DoseStatus.missed,
          actionTime: taken ? t.add(Duration(minutes: rnd.nextInt(40))) : null,
        ));
      }
    }
    for (var i = 1; i <= 14; i++) {
      await services.db.setMood(DateTime(now.year, now.month, now.day - i), 3 + rnd.nextInt(3));
    }
    // Bugünün ilk dozunu al, birini ertele.
    final today = await services.db.dosesBetween(dateOnly(now), now.add(const Duration(days: 1)));
    if (today.isNotEmpty) await services.actions.take(today.first.logId);
    await services.db.setMood(now, 4);

    // ── Uygulamayı gerçek açılış yoluyla başlat.
    await app.startApp(services);
    await pumpFor(tester, 2500);
    expect(tester.takeException(), isNull);
    if (Platform.isAndroid) await binding.convertFlutterSurfaceToImage();

    await shot(tester, '01_bugun');
    Future<void> tab(String label, String name) async {
      await tester.tap(find.descendant(of: find.byType(NavigationBar), matching: find.text(label)));
      await shot(tester, name);
      expect(tester.takeException(), isNull);
    }

    await tab('Takviyeler', '02_takviyeler');
    await tester.tap(find.text('Demir').first);
    await shot(tester, '03_takviye_ayrinti');
    await tester.pageBack();
    await pumpFor(tester, 800);

    await tab('Panel', '04_panel');
    await tester.tap(find.text('Etkileşim kontrolü'));
    await shot(tester, '05_etkilesim');
    await tester.pageBack();
    await pumpFor(tester, 800);
    await tester.tap(find.text('Hazır setler'));
    await shot(tester, '06_hazir_setler');
    await tester.pageBack();
    await pumpFor(tester, 800);

    await tab('İstatistik', '07_istatistik');
    await tab('Öğren', '08_ogren_temel');
    await tester.tap(find.text('Semptomlar'));
    await pumpFor(tester, 600);
    await tester.tap(find.byType(Checkbox).at(0));
    await tester.tap(find.byType(Checkbox).at(1));
    await shot(tester, '09_ogren_semptom');
    await tester.tap(find.text('Emilim'));
    await shot(tester, '10_ogren_emilim');

    await tab('Bugün', '11_bugun_tekrar');
    await tester.tap(find.byType(FloatingActionButton));
    await pumpFor(tester, 800);
    await tester.tap(find.text('C Vitamini').first);
    await shot(tester, '12_takviye_ekle');
    expect(tester.takeException(), isNull);
  });
}

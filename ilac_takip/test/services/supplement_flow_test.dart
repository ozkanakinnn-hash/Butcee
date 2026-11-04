import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart' show Locale;
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/data/app_database.dart';
import 'package:ilac_takip/data/supplement_catalog.dart';
import 'package:ilac_takip/domain/adherence.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/l10n/app_localizations.dart';
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/alarm_scheduler.dart';
import 'package:ilac_takip/services/dose_action_service.dart';
import 'package:ilac_takip/services/local_notification_scheduler.dart';
import 'package:ilac_takip/services/localized_texts.dart';
import 'package:ilac_takip/services/report_service.dart';
import 'package:ilac_takip/services/settings_store.dart';
import 'package:ilac_takip/services/supplement_service.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

import 'fakes.dart';

/// Alarm kurmayı reddeden zamanlayıcı (Android 14'te tam zamanlı alarm izni yokken olan durum).
class ThrowingScheduler extends FakeAlarmScheduler {
  @override
  Future<void> schedule(PlannedDose d) async =>
      throw PlatformException(code: 'exact_alarms_not_permitted', message: 'Exact alarms are not permitted');
}

void main() {
  late AppDatabase db;
  late FakeAlarmScheduler alarms;
  late TestClock clock;
  late AlarmPlanner planner;
  late SupplementService svc;
  var settings = const AppSettings();
  var dbOpen = false;

  Future<void> setUpWith({FakeAlarmScheduler? scheduler}) async {
    db = await openTestDb();
    dbOpen = true;
    alarms = scheduler ?? FakeAlarmScheduler();
    clock = TestClock(DateTime(2026, 11, 4, 6));
    planner = AlarmPlanner(
      db: db,
      scheduler: alarms,
      texts: FakeTexts(),
      clock: clock.call,
      isSlotMuted: (slot) => settings.isSlotMuted(slot),
    );
    svc = SupplementService(db: db, planner: planner, clock: clock.call);
  }

  setUp(() {
    settings = const AppSettings();
    dbOpen = false;
  });
  tearDown(() async {
    if (dbOpen) await db.close();
  });

  group('çökme düzeltmesi', () {
    test('tam zamanlı alarm izni yokken mod seçimi inexact olur', () {
      expect(chooseScheduleMode(exactAllowed: false, critical: true), AndroidScheduleMode.inexactAllowWhileIdle);
      expect(chooseScheduleMode(exactAllowed: false, critical: false), AndroidScheduleMode.inexactAllowWhileIdle);
      expect(chooseScheduleMode(exactAllowed: true, critical: true), AndroidScheduleMode.alarmClock);
      expect(chooseScheduleMode(exactAllowed: true, critical: false), AndroidScheduleMode.exactAllowWhileIdle);
    });

    test('alarm kurulamasa bile takviye eklenir ve reconcile hata fırlatmaz', () async {
      await setUpWith(scheduler: ThrowingScheduler());
      final id = await svc.addSupplement(
        medicine: const Medicine(name: 'D', catalogId: 'vitD'),
        rule: SupplementService.ruleFromSlots(slots: ['earlyMorning'], settings: settings, start: DateTime(2026, 11, 4)),
        slots: ['earlyMorning'],
      );
      expect(id, greaterThan(0));
      await expectLater(planner.reconcile(), completes);
      expect(planner.lastError, isA<PlatformException>());
      expect((await db.dosesBetween(DateTime(2026, 11, 4), DateTime(2026, 11, 5))).length, 1);
    });

    test('ertelemede alarm kurulamazsa erteleme yine kaydedilir', () async {
      await setUpWith(scheduler: ThrowingScheduler());
      await svc.addSupplement(
        medicine: const Medicine(name: 'D', catalogId: 'vitD'),
        rule: SupplementService.ruleFromSlots(slots: ['earlyMorning'], settings: settings, start: DateTime(2026, 11, 4)),
        slots: ['earlyMorning'],
      );
      final actions = DoseActionService(db: db, scheduler: alarms, planner: planner, clock: clock.call);
      clock.now = DateTime(2026, 11, 4, 7, 1);
      final logId = DoseLog.buildLogId((await db.allSchedules()).single.id, DateTime(2026, 11, 4, 7));
      expect(await actions.snooze(logId), isTrue);
      expect((await db.dose(logId))!.status, DoseStatus.snoozed);
    });
  });

  test('v1 veritabanı v3\'e kayıpsız geçer', () async {
    sqfliteFfiInit();
    final path = '${(await databaseFactoryFfi.getDatabasesPath())}/migration_test_${DateTime.now().microsecondsSinceEpoch}.db';
    final v1 = await databaseFactoryFfi.openDatabase(path,
        options: OpenDatabaseOptions(version: 1, onCreate: (d, _) => AppDatabase.createV1SchemaForTest(d)));
    await v1.insert('medicines', {'name': 'Eski ilaç', 'form': 'tablet', 'total_stock': 10});
    await v1.insert('drug_catalog', {'barcode': '0000000000017', 'name': 'ÖRNEK', 'form': 'tablet'});
    await v1.close();

    db = await AppDatabase.open(factory: databaseFactoryFfi, path: path);
    dbOpen = true;
    final m = (await db.medicines()).single;
    expect(m.name, 'Eski ilaç');
    expect(m.category, ItemCategory.vitamin);
    expect(m.totalStock, 10);
    expect(await db.catalogEntry('0000000000017'), isNull, reason: 'v3 örnek kataloğu siler');
    await db.setMood(DateTime(2026, 11, 4), 5);
    expect((await db.moods(DateTime(2026, 11, 4), DateTime(2026, 11, 5))).values.single, 5);
    await databaseFactoryFfi.deleteDatabase(path);
  });

  test('bakım: eski kuyruk, geçmiş ve ruh hâli silinir; bekleyen dozlar korunur', () async {
    await setUpWith();
    await svc.addSupplement(
      medicine: const Medicine(name: 'D', catalogId: 'vitD'),
      rule: SupplementService.ruleFromSlots(slots: ['earlyMorning'], settings: settings, start: DateTime(2026, 11, 4)),
      slots: ['earlyMorning'],
    );
    final s = (await db.allSchedules()).single;
    final old = DateTime(2025, 1, 1, 7);
    await db.insertDoseIfAbsent(DoseLog.planned(s, old).copyWith(status: DoseStatus.taken, actionTime: old));
    await db.enqueueOutbox('doseLog', 'x', DateTime(2026, 9, 1));
    await db.enqueueOutbox('doseLog', 'y', DateTime(2026, 11, 3));
    await db.setMood(DateTime(2025, 1, 1), 3);
    expect(await db.pruneOldData(DateTime(2026, 11, 4)), 3);
    expect(await db.outboxCount(), 1);
    expect(await db.dose(DoseLog.buildLogId(s.id, old)), isNull);
    expect((await db.dosesBetween(DateTime(2026, 11, 4), DateTime(2026, 11, 5))).length, 1, reason: 'bugünkü doz kalır');
  });

  test('hazır set ekler, dozlara dilim yazar, ikinci kez eklemez', () async {
    await setUpWith();
    final bone = supplementPresets.firstWhere((p) => p.id == 'bone');
    expect(await svc.applyPreset(bone, settings, 'tr'), 4);
    expect(await svc.applyPreset(bone, settings, 'tr'), 0);
    final doses = await db.dosesBetween(DateTime(2026, 11, 4), DateTime(2026, 11, 5));
    final ca = (await svc.plans()).firstWhere((p) => p.medicine.catalogId == 'calcium');
    final caDose = doses.firstWhere((d) => d.medicineId == ca.medicine.id);
    expect(caDose.slot, 'noon');
    expect(caDose.scheduledTime, DateTime(2026, 11, 4, 13));
    expect(await svc.conflicts(), isEmpty, reason: 'hazır setler kendi içinde çakışmamalı');
  });

  test('tüm hazır setler çakışmasız', () {
    for (final p in supplementPresets) {
      final items = [
        for (final (i, it) in p.items.indexed)
          PlannedItem(medicineId: i, catalogId: it.catalogId, name: it.catalogId, hours: {parseHm(defaultSlotTimes[it.slot]!).h}),
      ];
      expect(findConflicts(items), isEmpty, reason: p.id);
    }
  });

  test('aynı saatte kalsiyum + demir çakışır; 2 saat ara yeterli', () {
    PlannedItem item(int id, String c, int h) => PlannedItem(medicineId: id, catalogId: c, name: c, hours: {h});
    expect(findConflicts([item(1, 'calcium', 9), item(2, 'iron', 9)]).single.rule.a, 'calcium');
    expect(findConflicts([item(1, 'calcium', 9), item(2, 'iron', 10)]), hasLength(1));
    expect(findConflicts([item(1, 'calcium', 13), item(2, 'iron', 9)]), isEmpty);
    expect(findConflicts([item(1, 'vitD', 9), item(2, 'calcium', 9)]), isEmpty, reason: 'sinerji çakışma değildir');
  });

  test('dilim saati değişince planlar ve alarmlar güncellenir', () async {
    await setUpWith();
    await svc.addSupplement(
      medicine: const Medicine(name: 'Mg', catalogId: 'magnesium'),
      rule: SupplementService.ruleFromSlots(slots: ['evening'], settings: settings, start: DateTime(2026, 11, 4)),
      slots: ['evening'],
    );
    expect(alarms.mainAlarmTimes().first, DateTime(2026, 11, 4, 19));
    settings = settings.copyWith(slotTimes: {...settings.slotTimes, 'evening': '20:30'});
    await svc.applySlotTimes(settings);
    expect(alarms.mainAlarmTimes().first, DateTime(2026, 11, 4, 20, 30));
  });

  test('sessize alınan dilim ve genel anahtar alarm kurmaz', () async {
    await setUpWith();
    await svc.addSupplement(
      medicine: const Medicine(name: 'Mg', catalogId: 'magnesium'),
      rule: SupplementService.ruleFromSlots(slots: ['evening'], settings: settings, start: DateTime(2026, 11, 4)),
      slots: ['evening'],
    );
    expect(alarms.pending, isNotEmpty);
    settings = settings.copyWith(mutedSlots: {'evening'});
    await planner.rescheduleAll();
    expect(alarms.pending, isEmpty);
    settings = settings.copyWith(mutedSlots: {}, remindersEnabled: false);
    await planner.rescheduleAll();
    expect(alarms.pending, isEmpty);
    settings = settings.copyWith(remindersEnabled: true);
    await planner.rescheduleAll();
    expect(alarms.pending, isNotEmpty);
  });

  test('ek doz stok düşer ama uyum hesabına girmez', () async {
    await setUpWith();
    final id = await svc.addSupplement(
      medicine: const Medicine(name: 'Probiyotik', catalogId: 'probiotic'),
      rule: AsNeededRule(startDate: DateTime(2026, 11, 4), maxPerDay: 2, minIntervalHours: 4),
      initialStock: 10,
    );
    final actions = DoseActionService(db: db, scheduler: alarms, planner: planner, clock: clock.call);
    await actions.logExtra(id);
    expect((await db.medicine(id))!.totalStock, 9);
    final logs = await db.dosesBetween(DateTime(2026, 11, 4), DateTime(2026, 11, 5));
    expect(logs.single.isExtra, isTrue);
    expect(computeAdherence(logs, from: DateTime(2026, 11, 4), to: DateTime(2026, 11, 5), now: DateTime(2026, 11, 5)).planned, 0);
  });

  test('üst sınır hesabı: D vitamini IU/mcg dönüşümü', () {
    final d = catalogById('vitD')!;
    expect(dailyTotalInCatalogUnit(d, 1000, 'IU', 2), 2000);
    expect(dailyTotalInCatalogUnit(d, 125, 'mcg', 1), 5000); // 125 mcg = 5000 IU > UL 4000
    expect(dailyTotalInCatalogUnit(d, 1, 'tablet', 1), isNull);
    expect(dailyTotalInCatalogUnit(catalogById('magnesium')!, 0.5, 'g', 1), 500);
  });

  group('seri ve denge puanı', () {
    DayAdherence day(int d, int planned, int taken) =>
        DayAdherence(DateTime(2026, 11, d), AdherenceStats(planned: planned, onTime: taken));

    test('bugün tamamlanmadıysa dünden geriye sayar; plansız günler seriyi bozmaz', () {
      final days = [day(1, 2, 1), day(2, 2, 2), day(3, 0, 0), day(4, 2, 2), day(5, 2, 1)];
      expect(currentStreak(days, DateTime(2026, 11, 5, 10)), 2);
      expect(currentStreak([...days.sublist(0, 4), day(5, 2, 2)], DateTime(2026, 11, 5, 22)), 3);
    });

    test('puan: ruh hâli yoksa yalnızca uyum', () {
      expect(balanceScore(0.9, null).total, 90);
      final s = balanceScore(0.95, 4);
      expect(s.adherencePart, 57);
      expect(s.moodPart, 30);
      expect(s.total, 87);
    });
  });

  group('PDF', () {
    TestWidgetsFlutterBinding.ensureInitialized();

    test('Türkçe saat biçimi: initIntl olmadan hata, sonrasında çalışır (açılış çökmesi)', () async {
      final t = lookupAppLocalizations(const Locale('tr'));
      final m = const Medicine(id: 1, name: 'D', doseAmount: 1000, doseUnit: 'IU');
      final d = DoseLog(logId: 'x', scheduleId: 's', medicineId: 1, scheduledTime: DateTime(2026, 11, 4, 7), graceUntil: DateTime(2026, 11, 4, 8), meal: MealRelation.withMeal);
      expect(() => LocalizedDoseTexts(t).body(m, d), throwsA(isA<Exception>()));
      await initIntl();
      expect(LocalizedDoseTexts(t).body(m, d), '07:00 · 1 tablet · YEMEKLE BİRLİKTE');
    });

    test('program ve doktor raporu Türkçe karakterlerle oluşur', () async {
      await setUpWith();
      await svc.applyPreset(supplementPresets.first, settings, 'tr');
      final t = lookupAppLocalizations(const Locale('tr'));
      final plans = await svc.plans();
      final sections = ReportService.programSections(t, plans, settings);
      expect(sections.first.title, 'Erken sabah');
      expect(sections.first.time, '07:00');
      expect(sections.last.title, 'Akşam');

      final bytes = await ReportService.build(
        t: t,
        plans: plans,
        settings: settings,
        total: const AdherenceStats(planned: 10, onTime: 9),
        perMedicine: const {},
      );
      expect(String.fromCharCodes(bytes.take(4)), '%PDF');
      expect(bytes.length, greaterThan(5000));
    });
  });
}

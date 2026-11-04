import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/data/app_database.dart';
import 'package:ilac_takip/domain/dose_rule.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/alarm_scheduler.dart';
import 'package:ilac_takip/services/dose_action_service.dart';
import 'package:ilac_takip/services/local_notification_scheduler.dart';

import 'fakes.dart';

void main() {
  late AppDatabase db;
  late FakeAlarmScheduler alarms;
  late TestClock clock;
  late AlarmPlanner planner;
  late DoseActionService actions;
  late int medId;

  Future<void> setUpWith({FakeAlarmScheduler? scheduler, Schedule Function(int medId)? schedule}) async {
    db = await openTestDb();
    alarms = scheduler ?? FakeAlarmScheduler();
    clock = TestClock(DateTime(2026, 11, 4, 8));
    planner = AlarmPlanner(db: db, scheduler: alarms, texts: FakeTexts(), clock: clock.call);
    actions = DoseActionService(
      db: db,
      scheduler: alarms,
      planner: planner,
      clock: clock.call,
      lowStockText: (m) => (title: 'low', body: m.name),
    );
    medId = await db.insertMedicine(const Medicine(name: 'Aspirin', totalStock: 6, criticalStockLevel: 5));
    await db.upsertSchedule(schedule?.call(medId) ??
        Schedule(
          id: 'sch_1',
          medicineId: medId,
          rule: FixedTimesRule(startDate: DateTime(2026, 11, 4), times: [(h: 9, m: 0), (h: 21, m: 0)]),
        ));
  }

  tearDown(() => db.close());

  test('reconcile 7 günlük doz üretir ve yalnızca 48 saatlik pencereyi kurar; idempotenttir', () async {
    await setUpWith();
    expect(await planner.reconcile(), 4);
    expect(alarms.mainAlarmTimes(), [
      DateTime(2026, 11, 4, 9),
      DateTime(2026, 11, 4, 21),
      DateTime(2026, 11, 5, 9),
      DateTime(2026, 11, 5, 21),
    ]);
    expect((await db.dosesBetween(DateTime(2026, 11, 4), DateTime(2026, 11, 12))).length, 14);

    expect(await planner.reconcile(), 0); // ikinci çalıştırma hiçbir şey kurmaz
    expect(alarms.pending.length, 4);
  });

  test('aldım: durum, stok, outbox, alarm iptali ve düşük stok uyarısı', () async {
    await setUpWith();
    await planner.reconcile();
    clock.now = DateTime(2026, 11, 4, 9, 5);
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));

    final previous = await actions.take(logId);
    expect(previous!.status, DoseStatus.pending);
    final d = (await db.dose(logId))!;
    expect(d.status, DoseStatus.taken);
    expect(d.actionTime, DateTime(2026, 11, 4, 9, 5));
    expect((await db.medicine(medId))!.totalStock, 5);
    expect(await db.outboxCount(), 1);
    expect(alarms.pending.containsKey(AlarmIds.main(d.intId!)), isFalse);
    expect(alarms.shown.single.title, 'low'); // 5 ≤ kritik seviye

    expect(await actions.take(logId), isNull); // çift dokunma: stok tekrar düşmez
    expect((await db.medicine(medId))!.totalStock, 5);
  });

  test('geri al: stok iade edilir ve alarm yeniden kurulur', () async {
    await setUpWith();
    await planner.reconcile();
    clock.now = DateTime(2026, 11, 4, 8, 30);
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));
    final previous = await actions.take(logId);
    await actions.undo(previous!);
    final d = (await db.dose(logId))!;
    expect(d.status, DoseStatus.pending);
    expect((await db.medicine(medId))!.totalStock, 6);
    expect(alarms.pending.containsKey(AlarmIds.main(d.intId!)), isTrue);
  });

  test('ertele: yeni zamana kurulur, panelde görünür, hak bitince reddedilir', () async {
    await setUpWith();
    await planner.reconcile();
    clock.now = DateTime(2026, 11, 4, 9, 1);
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));

    expect(await actions.snooze(logId), isTrue);
    final d = (await db.dose(logId))!;
    expect(d.status, DoseStatus.snoozed);
    expect(alarms.pending[AlarmIds.main(d.intId!)]!.at, DateTime(2026, 11, 4, 9, 11));

    final panel = await db.pendingDoses(clock.now);
    expect(panel.map((e) => e.logId), contains(logId));

    expect(await actions.snooze(logId), isTrue);
    expect(await actions.snooze(logId), isTrue);
    expect(await actions.snooze(logId), isFalse); // varsayılan azami 3
    expect((await db.dose(logId))!.snoozeCount, 3);
  });

  test('bildirim aksiyonları aynı servise yönlenir', () async {
    await setUpWith();
    await planner.reconcile();
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));
    await actions.handleNotificationAction(NotificationActions.skip, logId);
    expect((await db.dose(logId))!.status, DoseStatus.skipped);
    expect((await db.medicine(medId))!.totalStock, 6);
  });

  test('grace süresi dolan doz kaçırıldı olur ve panelde 24 saat kalır', () async {
    await setUpWith();
    await planner.reconcile();
    clock.now = DateTime(2026, 11, 4, 10, 1); // 09:00 + 60 dk grace
    await planner.reconcile();
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));
    expect((await db.dose(logId))!.status, DoseStatus.missed);
    expect((await db.pendingDoses(clock.now)).map((e) => e.logId), contains(logId));
    expect((await db.pendingDoses(DateTime(2026, 11, 5, 9, 30))).map((e) => e.logId), isNot(contains(logId)));
  });

  test('ısrarcı alarm zinciri kurulur ve "aldım" tümünü iptal eder', () async {
    await setUpWith(
      schedule: (id) => Schedule(
        id: 'sch_1',
        medicineId: id,
        rule: FixedTimesRule(startDate: DateTime(2026, 11, 4), times: [(h: 9, m: 0)]),
        isCritical: true,
        persistent: const PersistentAlarm(enabled: true, repeatEveryMinutes: 3, maxRepeats: 5),
      ),
    );
    await planner.reconcile();
    final logId = DoseLog.buildLogId('sch_1', DateTime(2026, 11, 4, 9));
    final intId = (await db.dose(logId))!.intId!;
    final chain = alarms.pending.keys.where((k) => AlarmIds.doseOf(k) == intId).toList()..sort();
    expect(chain.length, 6);
    expect(alarms.pending[AlarmIds.repeat(intId, 5)]!.at, DateTime(2026, 11, 4, 9, 15));

    await actions.take(logId);
    expect(alarms.pending.keys.where((k) => AlarmIds.doseOf(k) == intId), isEmpty);
  });

  test('iOS bütçesi: bekleyen bildirim sayısı sınırı aşılmaz', () async {
    await setUpWith(
      scheduler: FakeAlarmScheduler(pendingBudget: 10, repeatCap: 3),
      schedule: (id) => Schedule(
        id: 'sch_1',
        medicineId: id,
        rule: IntervalRule(startDate: DateTime(2026, 11, 4), firstDose: (h: 9, m: 0), intervalHours: 4),
        persistent: const PersistentAlarm(enabled: true, maxRepeats: 10),
      ),
    );
    await planner.reconcile();
    expect(alarms.pending.length, lessThanOrEqualTo(10));
    expect(alarms.pending.length, 8); // 2 doz × (1 + 3 tekrar)
  });

  test('plan değişince gelecekteki bekleyenler yeniden üretilir', () async {
    await setUpWith();
    await planner.reconcile();
    await db.upsertSchedule(Schedule(
      id: 'sch_1',
      medicineId: medId,
      rule: FixedTimesRule(startDate: DateTime(2026, 11, 4), times: [(h: 10, m: 30)]),
    ));
    await planner.rebuildSchedule('sch_1');
    expect(alarms.mainAlarmTimes(), [DateTime(2026, 11, 4, 10, 30), DateTime(2026, 11, 5, 10, 30)]);
  });

  test('ilaç silinince alarmları iptal edilir', () async {
    await setUpWith();
    await planner.reconcile();
    await db.deleteMedicine(medId);
    await planner.reconcile();
    expect(alarms.pending, isEmpty);
  });
}

import 'package:ilac_takip/data/app_database.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/services/alarm_planner.dart';
import 'package:ilac_takip/services/alarm_scheduler.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';

/// Bellekte bildirim kaydı tutan sahte zamanlayıcı.
class FakeAlarmScheduler implements AlarmScheduler {
  FakeAlarmScheduler({this.pendingBudget = 400, this.repeatCap = 15});

  @override
  final int pendingBudget;
  @override
  final int repeatCap;

  final Map<int, ({DateTime at, String title, String? logId})> pending = {};
  final List<({int id, String title, String body})> shown = [];

  @override
  Future<void> schedule(PlannedDose d) async {
    pending[AlarmIds.main(d.intId)] = (at: d.fireAt, title: d.title, logId: d.logId);
    if (!d.persistent) return;
    final n = d.maxRepeats < repeatCap ? d.maxRepeats : repeatCap;
    for (var k = 1; k <= n; k++) {
      pending[AlarmIds.repeat(d.intId, k)] =
          (at: d.fireAt.add(Duration(minutes: d.repeatEveryMinutes * k)), title: d.repeatTitle, logId: d.logId);
    }
  }

  @override
  Future<void> cancelDose(int intId) async =>
      pending.removeWhere((id, _) => AlarmIds.doseOf(id) == intId);

  @override
  Future<void> cancelId(int id) async => pending.remove(id);

  @override
  Future<Set<int>> pendingIds() async => pending.keys.toSet();

  @override
  Future<void> showNow({required int id, required String title, required String body}) async =>
      shown.add((id: id, title: title, body: body));

  List<DateTime> mainAlarmTimes() => (pending.entries.where((e) => e.key % AlarmIds.slots == 0).map((e) => e.value.at).toList()
    ..sort());
}

class FakeTexts implements DoseTextBuilder {
  @override
  String title(Medicine m, DoseLog d) => 'T:${m.name}';
  @override
  String body(Medicine m, DoseLog d) => 'B';
  @override
  String repeatTitle(Medicine m, DoseLog d) => 'R:${m.name}';
}

/// Elle ilerletilebilen saat.
class TestClock {
  TestClock(this.now);
  DateTime now;
  DateTime call() => now;
  void advance(Duration d) => now = now.add(d);
}

Future<AppDatabase> openTestDb() {
  sqfliteFfiInit();
  return AppDatabase.open(factory: databaseFactoryFfi, path: inMemoryDatabasePath);
}

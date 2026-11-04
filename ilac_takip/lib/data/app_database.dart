import 'dart:async';

import 'package:flutter/foundation.dart' show visibleForTesting;
import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

import '../domain/models.dart';

/// Yerel doğruluk kaynağı (offline-first). Alarm kararları yalnızca buradan verilir.
///
/// Not: Arka plan izolatı (bildirim aksiyonları) aynı dosyaya yazar; UI izolatı
/// bunu [changes] akışından değil, uygulama ön plana gelince [notifyChanged] ile öğrenir.
class AppDatabase {
  AppDatabase._(this._db);

  final Database _db;
  final _changes = StreamController<void>.broadcast();

  static const _version = 4;

  static Future<AppDatabase> open({DatabaseFactory? factory, String? path}) async {
    final f = factory ?? databaseFactory;
    final dbPath = path ?? p.join(await f.getDatabasesPath(), 'ilac_takip.db');
    final db = await f.openDatabase(
      dbPath,
      options: OpenDatabaseOptions(
        version: _version,
        onConfigure: (db) => db.execute('PRAGMA foreign_keys = ON'),
        onCreate: (db, _) async {
          await _createSchemaV1(db);
          await _migrateV1toV2(db);
          await _migrateV3toV4(db);
        },
        onUpgrade: (db, from, to) async {
          if (from < 2) await _migrateV1toV2(db);
          if (from < 3) await _migrateV2toV3(db);
          if (from < 4) await _migrateV3toV4(db);
        },
      ),
    );
    return AppDatabase._(db);
  }

  static Future<void> _createSchemaV1(Database db) async {
    final b = db.batch();
    b.execute('''
      CREATE TABLE medicines(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        nickname TEXT,
        form TEXT NOT NULL,
        barcode TEXT,
        total_stock REAL NOT NULL DEFAULT 0,
        critical_stock_level REAL NOT NULL DEFAULT 5,
        package_size INTEGER,
        is_critical INTEGER NOT NULL DEFAULT 0,
        expiry_dates TEXT
      )''');
    b.execute('CREATE INDEX idx_medicines_barcode ON medicines(barcode)');
    b.execute('''
      CREATE TABLE schedules(
        id TEXT PRIMARY KEY,
        medicine_id INTEGER NOT NULL REFERENCES medicines(id) ON DELETE CASCADE,
        rule_json TEXT NOT NULL,
        is_critical INTEGER NOT NULL DEFAULT 0,
        persistent_enabled INTEGER NOT NULL DEFAULT 0,
        persistent_every_min INTEGER NOT NULL DEFAULT 3,
        persistent_max INTEGER NOT NULL DEFAULT 10,
        grace_minutes INTEGER NOT NULL DEFAULT 60,
        max_snoozes INTEGER NOT NULL DEFAULT 3,
        paused INTEGER NOT NULL DEFAULT 0
      )''');
    b.execute('''
      CREATE TABLE dose_logs(
        int_id INTEGER PRIMARY KEY AUTOINCREMENT,
        log_id TEXT NOT NULL UNIQUE,
        schedule_id TEXT NOT NULL REFERENCES schedules(id) ON DELETE CASCADE,
        medicine_id INTEGER NOT NULL REFERENCES medicines(id) ON DELETE CASCADE,
        scheduled_time INTEGER NOT NULL,
        grace_until INTEGER NOT NULL,
        quantity REAL NOT NULL,
        meal TEXT NOT NULL,
        status TEXT NOT NULL,
        action_time INTEGER,
        snoozed_until INTEGER,
        snooze_count INTEGER NOT NULL DEFAULT 0,
        skip_reason TEXT,
        note TEXT
      )''');
    b.execute('CREATE INDEX idx_dose_logs_time ON dose_logs(scheduled_time)');
    b.execute('CREATE INDEX idx_dose_logs_status ON dose_logs(status, scheduled_time)');
    b.execute('''
      CREATE TABLE scanned_packs(
        serial TEXT PRIMARY KEY,
        barcode TEXT NOT NULL,
        medicine_id INTEGER NOT NULL REFERENCES medicines(id) ON DELETE CASCADE,
        lot TEXT,
        expiry TEXT,
        added_quantity REAL NOT NULL,
        scanned_at INTEGER NOT NULL
      )''');
    b.execute('''
      CREATE TABLE drug_catalog(
        barcode TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        active_ingredient TEXT,
        form TEXT NOT NULL,
        package_size INTEGER,
        prescription_type TEXT,
        leaflet_url TEXT
      )''');
    // Firestore senkronu için bekleyen değişiklikler (Firebase entegrasyonu bu tabloyu boşaltır).
    b.execute('''
      CREATE TABLE outbox(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        entity TEXT NOT NULL,
        entity_id TEXT NOT NULL,
        created_at INTEGER NOT NULL
      )''');
    await b.commit(noResult: true);
  }

  /// v2: takviye kategorisi/katalog/doz birimi, hatırlatma dilimleri, ek doz kaydı, ruh hâli.
  static Future<void> _migrateV1toV2(Database db) async {
    final b = db.batch();
    b.execute("ALTER TABLE medicines ADD COLUMN category TEXT NOT NULL DEFAULT 'vitamin'");
    b.execute('ALTER TABLE medicines ADD COLUMN catalog_id TEXT');
    b.execute('ALTER TABLE medicines ADD COLUMN dose_amount REAL');
    b.execute('ALTER TABLE medicines ADD COLUMN dose_unit TEXT');
    b.execute("ALTER TABLE schedules ADD COLUMN slots TEXT NOT NULL DEFAULT ''");
    b.execute('ALTER TABLE dose_logs ADD COLUMN slot TEXT');
    b.execute('ALTER TABLE dose_logs ADD COLUMN is_extra INTEGER NOT NULL DEFAULT 0');
    b.execute('CREATE TABLE mood_logs(day TEXT PRIMARY KEY, mood INTEGER NOT NULL)');
    await b.commit(noResult: true);
  }

  /// v3: ilk sürümün uygulamaya gömdüğü uydurma örnek ilaç kataloğu satırlarını siler.
  static Future<void> _migrateV2toV3(Database db) =>
      db.delete('drug_catalog', where: 'barcode IN (?, ?)', whereArgs: ['0000000000017', '0000000000024']);

  /// v4: karekod tarama kaldırıldı → ilaç kataloğu ve taranan kutu tabloları silinir.
  static Future<void> _migrateV3toV4(Database db) async {
    await db.execute('DROP TABLE IF EXISTS drug_catalog');
    await db.execute('DROP TABLE IF EXISTS scanned_packs');
  }

  /// Test için: v1 şemasıyla açılmış bir veritabanı oluşturur.
  static Future<void> createV1SchemaForTest(Database db) => _createSchemaV1(db);

  @visibleForTesting
  Future<bool> hasTable(String name) async =>
      (await _db.query('sqlite_master', where: "type = 'table' AND name = ?", whereArgs: [name])).isNotEmpty;

  Stream<void> get changes => _changes.stream;
  void notifyChanged() => _changes.add(null);

  Future<void> close() async {
    await _changes.close();
    await _db.close();
  }

  // ───────────── İlaçlar ─────────────

  Future<int> insertMedicine(Medicine m) async {
    final id = await _db.insert('medicines', m.toRow());
    notifyChanged();
    return id;
  }

  Future<Medicine?> medicine(int id) async {
    final r = await _db.query('medicines', where: 'id = ?', whereArgs: [id]);
    return r.isEmpty ? null : Medicine.fromRow(r.first);
  }

  Future<List<Medicine>> medicines() async =>
      (await _db.query('medicines', orderBy: 'name COLLATE NOCASE')).map(Medicine.fromRow).toList();

  Future<void> adjustStock(int medicineId, double delta, {Transaction? txn}) async {
    await (txn ?? _db).rawUpdate(
      'UPDATE medicines SET total_stock = MAX(0, total_stock + ?) WHERE id = ?',
      [delta, medicineId],
    );
    if (txn == null) notifyChanged();
  }

  Future<void> deleteMedicine(int id) async {
    await _db.delete('medicines', where: 'id = ?', whereArgs: [id]);
    notifyChanged();
  }

  // ───────────── Ruh hâli (öz bildirim) ─────────────

  Future<void> setMood(DateTime day, int mood) async {
    await _db.insert('mood_logs', {'day': _day(day), 'mood': mood}, conflictAlgorithm: ConflictAlgorithm.replace);
    notifyChanged();
  }

  Future<Map<String, int>> moods(DateTime from, DateTime to) async {
    final r = await _db.query('mood_logs', where: 'day >= ? AND day < ?', whereArgs: [_day(from), _day(to)]);
    return {for (final x in r) x['day'] as String: x['mood'] as int};
  }

  static String _day(DateTime d) =>
      '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  // ───────────── Planlar ─────────────

  Future<void> upsertSchedule(Schedule s) async {
    await _db.insert('schedules', s.toRow(), conflictAlgorithm: ConflictAlgorithm.replace);
    notifyChanged();
  }

  Future<Schedule?> schedule(String id) async {
    final r = await _db.query('schedules', where: 'id = ?', whereArgs: [id]);
    return r.isEmpty ? null : Schedule.fromRow(r.first);
  }

  Future<List<Schedule>> activeSchedules() async =>
      (await _db.query('schedules', where: 'paused = 0')).map(Schedule.fromRow).toList();

  Future<List<Schedule>> allSchedules() async =>
      (await _db.query('schedules')).map(Schedule.fromRow).toList();

  Future<List<Schedule>> schedulesForMedicine(int medicineId) async =>
      (await _db.query('schedules', where: 'medicine_id = ?', whereArgs: [medicineId]))
          .map(Schedule.fromRow)
          .toList();

  /// Planı değişen gelecekteki bekleyen dozları siler (yeniden üretilecek).
  Future<void> deleteFuturePending(String scheduleId, DateTime now) async {
    await _db.delete('dose_logs',
        where: "schedule_id = ? AND status = 'pending' AND scheduled_time > ?",
        whereArgs: [scheduleId, now.millisecondsSinceEpoch]);
  }

  // ───────────── Doz kayıtları ─────────────

  /// Deterministik log_id sayesinde idempotent. Eklendiyse `true`.
  Future<bool> insertDoseIfAbsent(DoseLog d) async {
    final id = await _db.insert('dose_logs', d.toRow(), conflictAlgorithm: ConflictAlgorithm.ignore);
    return id != 0;
  }

  Future<void> insertExtraDose(DoseLog d, {required DateTime now}) async {
    await _db.transaction((txn) async {
      await txn.insert('dose_logs', d.toRow());
      await adjustStock(d.medicineId, -d.quantity, txn: txn);
      await enqueueOutbox('doseLog', d.logId, now, txn: txn);
    });
    notifyChanged();
  }

  Future<DoseLog?> dose(String logId) async {
    final r = await _db.query('dose_logs', where: 'log_id = ?', whereArgs: [logId]);
    return r.isEmpty ? null : DoseLog.fromRow(r.first);
  }

  Future<void> updateDose(DoseLog d, {Transaction? txn}) async {
    await (txn ?? _db).update('dose_logs', d.toRow()..remove('int_id'), where: 'log_id = ?', whereArgs: [d.logId]);
    if (txn == null) notifyChanged();
  }

  Future<T> transaction<T>(Future<T> Function(Transaction txn) action) async {
    final r = await _db.transaction(action);
    notifyChanged();
    return r;
  }

  Future<void> enqueueOutbox(String entity, String entityId, DateTime now, {Transaction? txn}) =>
      (txn ?? _db).insert('outbox', {
        'entity': entity,
        'entity_id': entityId,
        'created_at': now.millisecondsSinceEpoch,
      });

  Future<int> outboxCount() async =>
      Sqflite.firstIntValue(await _db.rawQuery('SELECT COUNT(*) FROM outbox')) ?? 0;

  /// Alarm kurulacak dozlar: bekleyen/ertelenmiş, ateşlenme zamanı [from, to).
  Future<List<DoseLog>> alarmableDoses(DateTime from, DateTime to) async {
    final rows = await _db.rawQuery('''
      SELECT * FROM dose_logs
      WHERE status IN ('pending','snoozed')
        AND COALESCE(CASE WHEN status = 'snoozed' THEN snoozed_until END, scheduled_time) >= ?
        AND COALESCE(CASE WHEN status = 'snoozed' THEN snoozed_until END, scheduled_time) < ?
      ORDER BY COALESCE(CASE WHEN status = 'snoozed' THEN snoozed_until END, scheduled_time)
    ''', [from.millisecondsSinceEpoch, to.millisecondsSinceEpoch]);
    return rows.map(DoseLog.fromRow).toList();
  }

  /// "Bekleyenler" paneli: zamanı gelmiş bekleyenler + ertelenenler + son 24 saatte kaçırılanlar.
  Future<List<DoseLog>> pendingDoses(DateTime now) async {
    final rows = await _db.rawQuery('''
      SELECT * FROM dose_logs
      WHERE (status = 'pending' AND scheduled_time <= ?)
         OR status = 'snoozed'
         OR (status = 'missed' AND scheduled_time >= ?)
      ORDER BY scheduled_time
    ''', [now.millisecondsSinceEpoch, now.subtract(const Duration(hours: 24)).millisecondsSinceEpoch]);
    return rows.map(DoseLog.fromRow).toList();
  }

  Future<List<DoseLog>> dosesBetween(DateTime from, DateTime to) async {
    final rows = await _db.query('dose_logs',
        where: 'scheduled_time >= ? AND scheduled_time < ?',
        whereArgs: [from.millisecondsSinceEpoch, to.millisecondsSinceEpoch],
        orderBy: 'scheduled_time');
    return rows.map(DoseLog.fromRow).toList();
  }

  Future<List<DoseLog>> expiredWaitingDoses(DateTime now) async {
    final rows = await _db.query('dose_logs',
        where: "status IN ('pending','snoozed') AND grace_until <= ?", whereArgs: [now.millisecondsSinceEpoch]);
    return rows.map(DoseLog.fromRow).toList();
  }

  // ───────────── Bakım ─────────────

  /// Senkron kuyruğu (Firebase bağlanana kadar kimse okumaz), ruh hâli ve doz geçmişi
  /// sınırsız büyümesin diye eski kayıtları siler. Silinen satır sayısını döner.
  Future<int> pruneOldData(DateTime now, {int outboxDays = 30, int historyDays = 400}) async {
    final outboxCut = now.subtract(Duration(days: outboxDays)).millisecondsSinceEpoch;
    final historyCut = now.subtract(Duration(days: historyDays));
    var n = 0;
    n += await _db.delete('outbox', where: 'created_at < ?', whereArgs: [outboxCut]);
    n += await _db.delete('dose_logs',
        where: "scheduled_time < ? AND status NOT IN ('pending','snoozed')", whereArgs: [historyCut.millisecondsSinceEpoch]);
    n += await _db.delete('mood_logs', where: 'day < ?', whereArgs: [_day(historyCut)]);
    if (n > 500) await _db.execute('VACUUM'); // dosyayı fiziksel olarak küçült
    return n;
  }
}

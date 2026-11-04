import 'dart:async';

import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

import '../domain/gs1_parser.dart';
import '../domain/models.dart';

/// Yerel doğruluk kaynağı (offline-first). Alarm kararları yalnızca buradan verilir.
///
/// Not: Arka plan izolatı (bildirim aksiyonları) aynı dosyaya yazar; UI izolatı
/// bunu [changes] akışından değil, uygulama ön plana gelince [notifyChanged] ile öğrenir.
class AppDatabase {
  AppDatabase._(this._db);

  final Database _db;
  final _changes = StreamController<void>.broadcast();

  static const _version = 1;

  static Future<AppDatabase> open({DatabaseFactory? factory, String? path}) async {
    final f = factory ?? databaseFactory;
    final dbPath = path ?? p.join(await f.getDatabasesPath(), 'ilac_takip.db');
    final db = await f.openDatabase(
      dbPath,
      options: OpenDatabaseOptions(
        version: _version,
        onConfigure: (db) => db.execute('PRAGMA foreign_keys = ON'),
        onCreate: (db, _) => _createSchema(db),
      ),
    );
    return AppDatabase._(db);
  }

  static Future<void> _createSchema(Database db) async {
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

  Future<Medicine?> medicineByBarcode(String barcode) async {
    final r = await _db.query('medicines', where: 'barcode = ?', whereArgs: [barcode], limit: 1);
    return r.isEmpty ? null : Medicine.fromRow(r.first);
  }

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

  /// Karekodlu kutuyu stoğa ekler. Aynı seri no daha önce eklendiyse `false`.
  Future<bool> addScannedPack(int medicineId, ScannedDrugCode code, double quantity, DateTime now) async {
    final ok = await _db.transaction((txn) async {
      if (code.serial != null) {
        final exists = await txn.query('scanned_packs', where: 'serial = ?', whereArgs: [code.serial]);
        if (exists.isNotEmpty) return false;
        await txn.insert('scanned_packs', {
          'serial': code.serial,
          'barcode': code.barcode13,
          'medicine_id': medicineId,
          'lot': code.lot,
          'expiry': code.expiry?.toIso8601String().substring(0, 10),
          'added_quantity': quantity,
          'scanned_at': now.millisecondsSinceEpoch,
        });
      }
      await adjustStock(medicineId, quantity, txn: txn);
      if (code.expiry != null) {
        final r = await txn.query('medicines', columns: ['expiry_dates'], where: 'id = ?', whereArgs: [medicineId]);
        final existing = ((r.first['expiry_dates'] as String?) ?? '').split(',').where((e) => e.isNotEmpty).toSet();
        existing.add(code.expiry!.toIso8601String().substring(0, 10));
        await txn.update('medicines', {'expiry_dates': (existing.toList()..sort()).join(',')},
            where: 'id = ?', whereArgs: [medicineId]);
      }
      return true;
    });
    if (ok) notifyChanged();
    return ok;
  }

  Future<bool> isPackScanned(String serial) async =>
      (await _db.query('scanned_packs', where: 'serial = ?', whereArgs: [serial])).isNotEmpty;

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

  // ───────────── Katalog ─────────────

  Future<void> upsertCatalog(List<Map<String, Object?>> rows) async {
    final b = _db.batch();
    for (final r in rows) {
      b.insert('drug_catalog', r, conflictAlgorithm: ConflictAlgorithm.replace);
    }
    await b.commit(noResult: true);
  }

  Future<Map<String, Object?>?> catalogEntry(String barcode) async {
    final r = await _db.query('drug_catalog', where: 'barcode = ?', whereArgs: [barcode]);
    return r.isEmpty ? null : r.first;
  }

  Future<int> catalogCount() async =>
      Sqflite.firstIntValue(await _db.rawQuery('SELECT COUNT(*) FROM drug_catalog')) ?? 0;
}

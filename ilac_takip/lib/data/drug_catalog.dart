import 'dart:convert';

import 'package:flutter/services.dart';

import '../domain/gs1_parser.dart';
import '../domain/models.dart';
import 'app_database.dart';

class DrugCatalogEntry {
  const DrugCatalogEntry({
    required this.barcode,
    required this.name,
    this.activeIngredient,
    this.form = 'tablet',
    this.packageSize,
    this.prescriptionType,
    this.leafletUrl,
  });

  final String barcode;
  final String name;
  final String? activeIngredient;
  final String form;
  final int? packageSize;
  final String? prescriptionType;
  final String? leafletUrl;

  factory DrugCatalogEntry.fromRow(Map<String, Object?> r) => DrugCatalogEntry(
        barcode: r['barcode'] as String,
        name: r['name'] as String,
        activeIngredient: r['active_ingredient'] as String?,
        form: r['form'] as String? ?? 'tablet',
        packageSize: r['package_size'] as int?,
        prescriptionType: r['prescription_type'] as String?,
        leafletUrl: r['leaflet_url'] as String?,
      );
}

/// Tarama sonucu ne yapılacağı.
sealed class ScanOutcome {
  const ScanOutcome();
}

class DuplicatePack extends ScanOutcome {
  const DuplicatePack();
}

/// Kullanıcı bu ilacı zaten kullanıyor → stoğa ekle.
class AddStock extends ScanOutcome {
  const AddStock(this.medicine, this.quantity);
  final Medicine medicine;
  final int quantity;
}

/// Katalogda bulundu, kullanıcıda yok → yeni ilaç ön izleme.
class NewFromCatalog extends ScanOutcome {
  const NewFromCatalog(this.entry);
  final DrugCatalogEntry entry;
}

/// Hiçbir yerde yok → elle giriş.
class NotInCatalog extends ScanOutcome {
  const NotInCatalog();
}

/// Arama sırası: kullanıcının ilaçları → yerel katalog → (ileride) Firestore → elle giriş.
class DrugCatalogRepository {
  DrugCatalogRepository(this._db, {this.remoteLookup});

  final AppDatabase _db;

  /// Firebase bağlandığında `drugCatalog/{barcode}` sorgusu buraya verilir.
  final Future<DrugCatalogEntry?> Function(String barcode)? remoteLookup;

  /// Uygulamayla gelen örnek katalog paketini ilk açılışta yükler.
  Future<void> seedFromAsset(AssetBundle bundle, {String path = 'assets/catalog/sample_catalog.json'}) async {
    if (await _db.catalogCount() > 0) return;
    final list = (jsonDecode(await bundle.loadString(path)) as List).cast<Map<String, Object?>>();
    await _db.upsertCatalog(list);
  }

  Future<DrugCatalogEntry?> lookup(String barcode) async {
    final local = await _db.catalogEntry(barcode);
    if (local != null) return DrugCatalogEntry.fromRow(local);
    final remote = remoteLookup;
    if (remote == null) return null;
    try {
      final e = await remote(barcode).timeout(const Duration(seconds: 4));
      if (e != null) {
        await _db.upsertCatalog([
          {
            'barcode': e.barcode,
            'name': e.name,
            'active_ingredient': e.activeIngredient,
            'form': e.form,
            'package_size': e.packageSize,
            'prescription_type': e.prescriptionType,
            'leaflet_url': e.leafletUrl,
          }
        ]);
      }
      return e;
    } on Exception {
      return null; // ağ yok → elle girişe düş
    }
  }

  Future<ScanOutcome> resolve(ScannedDrugCode code) async {
    if (code.serial != null && await _db.isPackScanned(code.serial!)) return const DuplicatePack();
    final owned = await _db.medicineByBarcode(code.barcode13);
    final entry = await lookup(code.barcode13);
    if (owned != null) return AddStock(owned, owned.packageSize ?? entry?.packageSize ?? 0);
    if (entry != null) return NewFromCatalog(entry);
    return const NotInCatalog();
  }
}

# Mobil İlaç Takip Uygulaması — 2. Aşama İleri Düzey Özellikler

> **Kapsam:** Karekod/Barkod okutma · Doktor & Bakım Veren paylaşımı · Eczane & konum servisleri
> **Platform:** Flutter (Android + iOS) · Firebase (Auth, Firestore, Cloud Messaging, Cloud Functions, Storage, Hosting)
> **Tarih:** 2026-11-04 · **Durum:** Taslak v1

---

## 0. Ortak Temeller (üç modülün de dayandığı kararlar)

| Karar | Gerekçe |
|---|---|
| **Offline-first**: alarmlar ve doz kayıtları önce yerel veritabanına (`drift`/SQLite) yazılır, Firestore'a arka planda senkronlanır | Alarm çalması asla internete bağlı olmamalı. Firestore'un çevrimdışı önbelleği tek başına yeterli değildir (bildirim aksiyonları arka plan izolatında çalışır). |
| **Sunucu tarafı mantık Cloud Functions'ta** (v2, TypeScript) | "Kaçırıldı" tespiti, bakım verene bildirim, eczane API anahtarlarının gizlenmesi, doktor paylaşım linki. |
| **Sağlık verisi = KVKK md. 6 "özel nitelikli kişisel veri"** | Paylaşım öncesi **açık rıza** ekranı, paylaşımı tek dokunuşla geri alma, veri silme (hesap silindiğinde Cloud Function ile kaskad silme), Firestore bölgesi olarak `europe-west*`. |
| **Türkçe + İngilizce dil desteği** (tüm uygulamalarda zorunlu kural) | `flutter_localizations` + `intl` + ARB dosyaları (`app_tr.arb`, `app_en.arb`); sunucudan gönderilen bildirimler alıcının `locale` alanına göre yerelleştirilir. |
| **Tıbbi tavsiye vermeme** | Prospektüs içeriği resmi kaynağa yönlendirilir; uygulama doz önermez, yalnızca kullanıcının/doktorun girdiğini hatırlatır. |

### Ortak paket listesi

| Amaç | Paket |
|---|---|
| Durum yönetimi | `flutter_riverpod` (veya `bloc`) |
| Yerel veritabanı | `drift` + `sqlite3_flutter_libs` |
| Firebase | `firebase_core`, `firebase_auth`, `cloud_firestore`, `firebase_messaging`, `cloud_functions`, `firebase_storage` |
| Yerel bildirim/alarm | `flutter_local_notifications`, `timezone`, `flutter_timezone` |
| Yerelleştirme | `flutter_localizations`, `intl` |

### Firestore koleksiyon haritası (2. aşama sonrası)

```
users/{uid}                         ← profil, rol, locale, seniorMode
  ├─ devices/{deviceId}             ← FCM token'ları
  ├─ medicines/{medicineId}         ← (genişletildi: gtin, karekod, reçete, son kullanma)
  ├─ schedules/{scheduleId}         ← doz kuralları (bkz. 02 dokümanı)
  ├─ doseLogs/{logId}               ← (genişletildi: pending/snoozed/missed durumları)
  ├─ scannedPacks/{serial}          ← aynı kutunun iki kez stoğa eklenmesini önler
  └─ prescriptions/{rxId}           ← reçete / e-reçete kayıtları
careLinks/{patientId_caregiverId}   ← hasta ↔ bakım veren bağlantısı (deterministik ID)
careInvites/{inviteCode}            ← 6 haneli / QR davet kodları
reportShares/{token}                ← doktor için süreli, salt okunur rapor linki
drugCatalog/{barcode13}             ← merkezi ilaç kataloğu (yalnızca sunucu yazar)
catalogMeta/current                 ← katalog sürümü + yerel paket indirme yolu
drugSubmissions/{id}                ← katalogda olmayan ilaç için kullanıcı önerisi (moderasyon)
pharmacyCache/{il_ilce_tarih}       ← nöbetçi eczane önbelleği (yalnızca sunucu yazar)
```

---

# 1. Karekod / Barkod Okutma Entegrasyonu

## 1.1 Arka plan: Türkiye'de ilaç kutusundaki kodlar

Türkiye'deki ilaç kutularında iki kod bulunur ve **ikisi farklı bilgi taşır**:

| Kod | Biçim | İçerik |
|---|---|---|
| **Barkod** | EAN-13 (Türkiye için `869` ile başlar) | Yalnızca ürün kimliği (GTIN-13) |
| **Karekod** (İTS — İlaç Takip Sistemi) | **GS1 DataMatrix** (QR değildir!) | `(01)` GTIN-14 · `(21)` seri no · `(17)` son kullanma tarihi `YYAAGG` · `(10)` parti/lot no |

**Rakip uygulamaların "İlaç bulunamadı" hatasının iki ana nedeni:**
1. Tarayıcı yalnızca QR/EAN için ayarlanmıştır, DataMatrix okunmaz.
2. Karekoddan gelen GTIN-14 (`08699...`) katalogdaki EAN-13 (`8699...`) ile normalize edilmeden aranır.

Karekod bize üç bonus sağlar: **son kullanma tarihi otomatik girilir**, **seri no ile aynı kutu iki kez eklenemez**, **lot no ile geri çağırma (recall) uyarısı** ileride yapılabilir.

## 1.2 İşlevsel gereksinimler

| ID | Gereksinim |
|---|---|
| BR-1 | Tarayıcı EAN-13, GS1 DataMatrix ve QR kodları aynı ekranda okumalıdır. |
| BR-2 | Okunan kod GTIN-13'e normalize edilir; GS1 ise seri no, SKT ve lot ayrıştırılır. |
| BR-3 | Arama sırası: **yerel katalog → Firestore katalog → manuel giriş**. Ağ yoksa yerel katalog yeterli olmalıdır. |
| BR-4 | İlaç kullanıcının listesinde **zaten varsa** "Stoğa ekle (+30 tablet)" önerilir; yoksa "Yeni ilaç ekle" akışı açılır. |
| BR-5 | Aynı karekod seri numarası ikinci kez okutulursa "Bu kutu zaten eklendi" uyarısı verilir. |
| BR-6 | SKT okunduysa ilaca kaydedilir; SKT'den 30 gün önce hatırlatma kurulur. |
| BR-7 | Bulunamayan ilaçta kullanıcı adı elle girer (veya kutu fotoğrafından OCR ile önerilir) ve öneri `drugSubmissions`'a moderasyon için gönderilir. |
| BR-8 | Kamera izni reddedilirse manuel barkod girişi (13 hane klavye) sunulur. |
| BR-9 | Tarama sırasında el feneri, otomatik odak ve titreşimli/sesli onay bulunur. |

## 1.3 Akış (durum makinesi)

```
[Tara] ──kod okundu──▶ [Ayrıştır]
                          │ GS1? → gtin, serial, expiry, lot
                          │ EAN? → gtin
                          ▼
                   [Seri daha önce eklendi mi?] ──evet──▶ ⚠ "Bu kutu zaten eklendi"
                          │ hayır
                          ▼
                   [Yerel katalog] ──bulundu──┐
                          │ yok               │
                   [Firestore katalog] ─bulundu┤
                          │ yok               ▼
                   [Manuel giriş + OCR]   [Kullanıcıda bu ilaç var mı?]
                          │                   │ evet → [Stoğa ekle önizleme]
                          ▼                   │ hayır → [Yeni ilaç önizleme → doz planı sihirbazı]
                   drugSubmissions'a gönder    ▼
                                         [Kaydet: medicine + scannedPacks]
```

## 1.4 Veri kaynağı mimarisi

Türkiye'de ilaç prospektüsü için herkese açık, resmi bir REST API **yoktur**. TİTCK (Türkiye İlaç ve Tıbbi Cihaz Kurumu), ruhsatlı ürünlerin barkodlarını içeren listeleri (ör. beşeri tıbbi ürünler / fiyat listeleri, Excel) ve ürün bazında **KÜB** (Kısa Ürün Bilgisi) ile **KT** (Kullanma Talimatı) belgelerini web sitesinde yayınlar. Bu nedenle **kendi kataloğumuzu** kuruyoruz:

```
┌──────────────────────────┐   haftalık (Cloud Scheduler)
│ TİTCK listeleri (Excel)  │──────────────┐
└──────────────────────────┘              ▼
                              ┌───────────────────────────┐
                              │ CF: importDrugCatalog     │  normalize + diff
                              └────────────┬──────────────┘
                     ┌─────────────────────┼─────────────────────────┐
                     ▼                     ▼                         ▼
          Firestore drugCatalog/{gtin}   Storage: catalog/v{N}.sqlite.gz   catalogMeta/current {version:N}
                     ▲                     │
       çevrimiçi yedek arama          uygulama ilk açılışta + sürüm değişince indirir
                     │                     ▼
               ┌──────────── Flutter: yerel SQLite katalog (drift) ───────────┐
               │  1) yerel arama (çevrimdışı, <5 ms)                          │
               │  2) bulunamazsa Firestore get(drugCatalog/{gtin})            │
               │  3) bulunamazsa manuel giriş → drugSubmissions (moderasyon)  │
               └──────────────────────────────────────────────────────────────┘
```

- **Uygulama paketine gömülü ilk sürüm** (`assets/catalog_v1.sqlite`): ilk açılışta internet olmasa bile tarama çalışır.
- **Sürüm kontrolü**: açılışta `catalogMeta/current.version` > yerel sürüm ise Storage'dan sıkıştırılmış dosya indirilir (Wi-Fi tercihli).
- **Prospektüs**: KT/KÜB belgeleri **kopyalanıp barındırılmaz**; resmi belge bağlantısı (`leafletUrl`) uygulama içi tarayıcıda açılır (güncellik ve telif sorumluluğu).
- **Topluluk katkısı**: `drugSubmissions` kayıtları yönetici panelinde onaylanınca `drugCatalog`'a yazılır.
- **İngilizce kullanıcılar / yurt dışı ürünleri**: katalog sağlayıcısı bir arayüz (`DrugCatalogSource`) arkasındadır; ileride openFDA NDC gibi kaynaklar eklenebilir.

### Firestore şeması — `drugCatalog/{barcode13}`

```json
{
  "barcode": "8699514010012",
  "gtin14": "08699514010012",
  "name": "Aspirin 100 mg 30 Tablet",
  "brand": "Aspirin",
  "activeIngredients": [{ "name": "Asetilsalisilik asit", "strength": "100 mg" }],
  "form": "tablet",
  "packageSize": 30,
  "packageUnit": "tablet",
  "atcCode": "B01AC06",
  "manufacturer": "Örnek İlaç A.Ş.",
  "prescriptionType": "normal",
  "leafletUrl": "https://… (TİTCK KT belgesi)",
  "smpcUrl": "https://… (TİTCK KÜB belgesi)",
  "searchTokens": ["aspirin", "asetilsalisilik", "100"],
  "source": "titck",
  "isActive": true,
  "updatedAt": "2026-11-01T03:00:00Z"
}
```

> `prescriptionType`: `normal` | `red` | `green` | `purple` | `orange` | `otc` (Türkiye'deki beyaz/kırmızı/yeşil/mor/turuncu reçete türleri). Reçete yenileme hatırlatıcısında kullanılır.

### Genişletilmiş `medicines/{medicineId}` (MVP şemasının üzerine)

```json
{
  "medicineId": "med_abc123",
  "userId": "user_xyz789",
  "name": "Aspirin 100mg",
  "form": "tablet",
  "barcode": "8699514010012",
  "catalogRef": "drugCatalog/8699514010012",
  "totalStock": 30,
  "criticalStockLevel": 5,
  "packageSize": 30,
  "doseUnit": "tablet",
  "isCritical": false,
  "instructions": { "mealRelation": "afterMeal", "offsetMinutes": 30, "note": "Bol su ile" },
  "expiryDates": ["2028-03-31"],
  "photoUrl": null,
  "prescriptionId": "rx_001",
  "source": "scan",
  "createdAt": "2026-11-04T10:00:00Z",
  "updatedAt": "2026-11-04T10:00:00Z"
}
```

### `users/{uid}/scannedPacks/{serial}`

```json
{
  "serial": "10023456789AB",
  "barcode": "8699514010012",
  "medicineId": "med_abc123",
  "lot": "B2401",
  "expiry": "2028-03-31",
  "addedQuantity": 30,
  "scannedAt": "2026-11-04T10:00:00Z"
}
```

## 1.5 Flutter uygulaması

**Paketler:** `mobile_scanner` (ML Kit / Apple Vision tabanlı; DataMatrix destekler), `google_mlkit_text_recognition` (kutu fotoğrafından OCR, opsiyonel), `permission_handler`, `haptic_feedback` (veya `HapticFeedback` çekirdek API), `webview_flutter` / `url_launcher` (prospektüs).

### GS1 karekod ayrıştırıcı

```dart
/// Türkiye ilaç karekodu (GS1 DataMatrix) ve EAN-13 barkod ayrıştırıcı.
class ScannedDrugCode {
  final String barcode13;   // katalog anahtarı
  final String? serial;     // (21)
  final DateTime? expiry;   // (17)
  final String? lot;        // (10)
  const ScannedDrugCode(this.barcode13, {this.serial, this.expiry, this.lot});

  bool get isKarekod => serial != null;
}

class Gs1Parser {
  static const _gs = '\u001d'; // FNC1 / Group Separator
  static const _fixedLengths = {'01': 14, '17': 6, '11': 6, '15': 6};

  static ScannedDrugCode? parse(String raw) {
    var s = raw.trim();
    // Bazı tarayıcılar sembol tanımlayıcısı ekler: "]d2"
    if (s.startsWith(']d2') || s.startsWith(']C1') || s.startsWith(']Q3')) s = s.substring(3);

    // Düz EAN-13 / GTIN-14
    if (RegExp(r'^\d{13}$').hasMatch(s)) return ScannedDrugCode(s);
    if (RegExp(r'^\d{14}$').hasMatch(s)) return ScannedDrugCode(_toBarcode13(s));

    // Bazı cihazlar GS1'i "(01)0869...(21)..." insan-okur biçiminde verir
    if (s.startsWith('(')) {
      s = s.replaceAllMapped(RegExp(r'\((\d{2})\)'), (m) => '$_gs${m[1]}').substring(1);
    }

    final ais = <String, String>{};
    var i = 0;
    while (i + 2 <= s.length) {
      if (s[i] == _gs) { i++; continue; }
      final ai = s.substring(i, i + 2);
      i += 2;
      final fixed = _fixedLengths[ai];
      String value;
      if (fixed != null) {
        if (i + fixed > s.length) return null;
        value = s.substring(i, i + fixed);
        i += fixed;
      } else {
        final end = s.indexOf(_gs, i);
        value = s.substring(i, end == -1 ? s.length : end);
        i = end == -1 ? s.length : end + 1;
      }
      ais[ai] = value;
    }

    final gtin = ais['01'];
    if (gtin == null) return null;
    return ScannedDrugCode(
      _toBarcode13(gtin),
      serial: ais['21'],
      lot: ais['10'],
      expiry: ais['17'] != null ? _parseExpiry(ais['17']!) : null,
    );
  }

  static String _toBarcode13(String gtin14) =>
      gtin14.length == 14 && gtin14.startsWith('0') ? gtin14.substring(1) : gtin14;

  /// YYAAGG — GG = 00 ise ayın son günü kabul edilir (GS1 kuralı).
  static DateTime? _parseExpiry(String v) {
    final y = 2000 + int.parse(v.substring(0, 2));
    final m = int.parse(v.substring(2, 4));
    final d = int.parse(v.substring(4, 6));
    if (m < 1 || m > 12) return null;
    return d == 0 ? DateTime(y, m + 1, 0) : DateTime(y, m, d);
  }
}
```

### Katalog deposu (yerel → uzak → yok)

```dart
sealed class LookupResult {}
class Found extends LookupResult { final DrugCatalogEntry entry; Found(this.entry); }
class NotFound extends LookupResult {}

class DrugCatalogRepository {
  DrugCatalogRepository(this._localDb, this._firestore);
  final CatalogDatabase _localDb;           // drift
  final FirebaseFirestore _firestore;

  Future<LookupResult> lookup(String barcode13) async {
    final local = await _localDb.findByBarcode(barcode13);
    if (local != null) return Found(local);

    try {
      final snap = await _firestore
          .doc('drugCatalog/$barcode13')
          .get()
          .timeout(const Duration(seconds: 4));
      if (snap.exists) {
        final entry = DrugCatalogEntry.fromJson(snap.data()!);
        await _localDb.upsert(entry); // bir dahaki sefere çevrimdışı
        return Found(entry);
      }
    } on Exception {
      // Ağ yok / zaman aşımı → manuel girişe düş
    }
    return NotFound();
  }
}
```

### Tarama ekranı

```dart
class ScanScreen extends ConsumerStatefulWidget {
  const ScanScreen({super.key});
  @override
  ConsumerState<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends ConsumerState<ScanScreen> {
  final _controller = MobileScannerController(
    formats: const [BarcodeFormat.dataMatrix, BarcodeFormat.ean13, BarcodeFormat.qrCode],
    detectionSpeed: DetectionSpeed.noDuplicates,
  );
  bool _busy = false;

  Future<void> _onDetect(BarcodeCapture capture) async {
    if (_busy) return;
    final raw = capture.barcodes.firstOrNull?.rawValue;
    final code = raw == null ? null : Gs1Parser.parse(raw);
    if (code == null) return;

    _busy = true;
    HapticFeedback.mediumImpact();
    await _controller.stop();

    final result = await ref.read(scanFlowProvider).handle(code);
    if (!mounted) return;
    switch (result) {
      case ScanFlowResult.duplicatePack:
        _showSnack(context.l10n.scanDuplicatePack);
      case ScanFlowResult.addStock(:final medicine, :final quantity):
        context.push('/scan/add-stock', extra: (medicine, quantity, code));
      case ScanFlowResult.newMedicine(:final entry):
        context.push('/scan/preview', extra: (entry, code));
      case ScanFlowResult.notFound:
        context.push('/medicines/manual', extra: code);
    }
    _busy = false;
    await _controller.start();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(context.l10n.scanTitle), actions: [
          IconButton(icon: const Icon(Icons.flash_on), onPressed: _controller.toggleTorch),
          IconButton(icon: const Icon(Icons.keyboard), onPressed: () => context.push('/scan/manual')),
        ]),
        body: Stack(children: [
          MobileScanner(controller: _controller, onDetect: _onDetect),
          const ScanOverlay(), // ortada kare çerçeve + "Kutunun üzerindeki karekodu çerçeveye alın"
        ]),
      );

  void _showSnack(String m) => ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(m)));

  @override
  void dispose() { _controller.dispose(); super.dispose(); }
}
```

### Stoğa ekleme (atomik + aynı kutu koruması)

```dart
Future<void> addPackToStock({
  required String uid,
  required String medicineId,
  required ScannedDrugCode code,
  required int quantity,
}) async {
  final db = FirebaseFirestore.instance;
  final medRef = db.doc('users/$uid/medicines/$medicineId');
  await db.runTransaction((tx) async {
    if (code.serial != null) {
      final packRef = db.doc('users/$uid/scannedPacks/${code.serial}');
      if ((await tx.get(packRef)).exists) throw DuplicatePackException();
      tx.set(packRef, {
        'serial': code.serial, 'barcode': code.barcode13, 'medicineId': medicineId,
        'lot': code.lot, 'expiry': code.expiry?.toIso8601String(),
        'addedQuantity': quantity, 'scannedAt': FieldValue.serverTimestamp(),
      });
    }
    tx.update(medRef, {
      'totalStock': FieldValue.increment(quantity),
      if (code.expiry != null) 'expiryDates': FieldValue.arrayUnion([code.expiry!.toIso8601String().substring(0, 10)]),
      'updatedAt': FieldValue.serverTimestamp(),
    });
  });
}
```

> Not: Firestore işlemleri (transaction) çevrimdışıyken başarısız olur. Çevrimdışıysa aynı mantık yerel `drift` veritabanında çalıştırılır ve senkronizasyon kuyruğuna alınır.

## 1.6 UI/UX akışı

```
┌─────────────────────────┐   ┌─────────────────────────┐   ┌─────────────────────────┐
│ ◀  İlaç Tara      🔦 ⌨ │   │ ◀  İlaç Bulundu         │   │ ◀  Doz Planı (1/3)      │
│                         │   │ ┌─────┐ Aspirin 100 mg  │   │                         │
│   ┌───────────────┐     │   │ │ 📦  │ 30 tablet       │   │ Ne sıklıkla?            │
│   │               │     │   │ └─────┘ Asetilsalisilik │   │ ( ) Günde X kez         │
│   │   [ kamera ]  │ ──▶ │   │ SKT: 31.03.2028 ✓      │──▶│ ( ) X saatte bir        │
│   │               │     │   │ Reçete: Normal          │   │ ( ) Gün aşırı           │
│   └───────────────┘     │   │ 📄 Kullanma Talimatı ↗  │   │ ( ) Haftanın günleri    │
│ Karekodu çerçeveye alın │   │                         │   │ ( ) Gerektiğinde        │
│ ───────────────────────│   │ [ İLACIMA EKLE ]        │   │                         │
│ Barkodu elle gir        │   │ [ Yanlış ilaç ]         │   │ [ DEVAM ]               │
└─────────────────────────┘   └─────────────────────────┘   └─────────────────────────┘
```

1. **Giriş noktaları:** Ana ekran FAB "➕ İlaç Ekle" → alt menü "📷 Kutuyu Tara" (önerilen, en üstte) / "✍ Elle Ekle". İlaç detayında "📦 Yeni kutu ekle" (stok artırma).
2. **İzin istemi:** Kamera izni öncesi kısa açıklama ekranı ("Karekodu okumak için kameranız kullanılır, görüntü kaydedilmez").
3. **Tarama:** Okuma anında titreşim + kısa yeşil çerçeve animasyonu; 8 sn içinde okunamazsa ipucu: "Kutuyu düz tutun, karekod genelde yan yüzdedir".
4. **Önizleme:** Katalog verisi + karekoddan gelen SKT. "Yanlış ilaç" düğmesi arama ekranına götürür (isimle arama, yerel FTS).
5. **Zaten var ise:** "Bu ilaç listenizde var. Stoğa **+30 tablet** eklensin mi?" → tek dokunuş.
6. **Doz planı sihirbazı:** 3 adım — sıklık → saatler & aç/tok → stok & kritik seviye. (Kural modeli için bkz. `02-rakip-analizi-ve-mimari.md`.)
7. **Bulunamadı:** "Bu ilacı henüz tanımıyoruz" → ad / form / kutu adedi alanları (kutu fotoğrafından OCR önerisi) → "Kaydet ve kataloğa önermek için gönder" onay kutusu.

---

# 2. Doktor ve Bakım Veren (Yakın) Paylaşım Modülü

## 2.1 Roller ve yetkiler

| Yetki | Hasta | Bakım Veren (yakın) | Doktor |
|---|---|---|---|
| Hesap gereksinimi | Evet | Evet (uygulama içi hesap) | **Hayır** — süreli web linki / QR |
| İlaç listesi görme | ✅ | ✅ (`canViewLogs`) | ✅ (rapor kapsamında) |
| Doz geçmişi / uyum oranı | ✅ | ✅ (`canViewLogs`) | ✅ (seçilen tarih aralığı) |
| Anlık bildirim (alındı/atlandı/kaçırıldı) | — | ✅ (`canReceiveAlerts`) | ❌ |
| Düşük stok bildirimi | ✅ | ✅ (`canReceiveStockAlerts`) | ❌ |
| Doz planı düzenleme | ✅ | Opsiyonel (`canEditSchedules`) | ❌ |
| Hasta adına "Aldı" işaretleme | ✅ | Opsiyonel (`canMarkDoses`) | ❌ |
| Basit Mod ayarlarını kilitleme | ✅ | Opsiyonel (`canManageSeniorMode`) | ❌ |
| Paylaşımı sonlandırma | ✅ (her zaman) | ✅ (kendi bağlantısını) | — (link süresi dolar) |

**İlkeler:** Varsayılan en az yetki; hasta tüm izinleri tek tek açıp kapatabilir; her izin değişikliği `careLinks/{id}/audit` alt koleksiyonuna yazılır. Bir hastanın birden fazla bakım vereni, bir bakım verenin birden fazla hastası olabilir (ör. anne + baba).

## 2.2 Eşleşme (davet) akışı

```
Hasta: Ayarlar → "Yakınımla Paylaş" → izinleri seç → KVKK açık rıza onayı
     → CF createCareInvite → 6 haneli kod + QR (24 saat geçerli)
Bakım veren: "Hastama bağlan" → kodu yaz / QR okut
     → CF acceptCareInvite → careLinks/{patientId_caregiverId} status: "pending"
Hasta: bildirim "Ayşe sizi takip etmek istiyor" → Onayla → status: "active"
```

> Ters yön de desteklenir: bakım veren yaşlı hasta için davet oluşturur, hasta telefonunda sadece "Onayla"ya basar (Basit Mod'da tek büyük düğme).

### `careLinks/{patientId}_{caregiverId}` (MVP şemasının genişletilmiş hâli)

Deterministik belge kimliği güvenlik kurallarında tek `get()` ile yetki kontrolü sağlar.

```json
{
  "linkId": "user_xyz789_user_caretaker456",
  "patientId": "user_xyz789",
  "caregiverId": "user_caretaker456",
  "caregiverDisplayName": "Ayşe (Kızı)",
  "relation": "child",
  "status": "active",
  "permissions": {
    "canViewLogs": true,
    "canReceiveAlerts": true,
    "canReceiveStockAlerts": true,
    "canEditSchedules": false,
    "canMarkDoses": false,
    "canManageSeniorMode": false
  },
  "alertPrefs": {
    "onTaken": false,
    "onSkipped": true,
    "onMissed": true,
    "missedGraceMinutes": 30,
    "quietHours": { "start": "23:00", "end": "07:00", "exceptCritical": true }
  },
  "consent": { "version": "kvkk-2026-1", "acceptedAt": "2026-11-04T11:14:00Z" },
  "invitedBy": "user_xyz789",
  "linkedAt": "2026-11-04T11:15:00Z",
  "updatedAt": "2026-11-04T11:15:00Z"
}
```

`status`: `pending` | `active` | `rejected` | `revoked`

### `careInvites/{code}`

```json
{ "code": "482913", "patientId": "user_xyz789", "permissions": { "...": "..." },
  "createdBy": "user_xyz789", "expiresAt": "2026-11-05T11:00:00Z", "usedBy": null }
```

### `users/{uid}/devices/{deviceId}`

```json
{ "token": "fcm_token…", "platform": "android", "locale": "tr", "appVersion": "2.0.0",
  "updatedAt": "2026-11-04T11:20:00Z" }
```

### Firestore güvenlik kuralları (özet)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {

    function signedIn() { return request.auth != null; }
    function isSelf(uid) { return signedIn() && request.auth.uid == uid; }
    function link(patientId) {
      return get(/databases/$(db)/documents/careLinks/$(patientId + '_' + request.auth.uid)).data;
    }
    function hasPerm(patientId, perm) {
      return signedIn()
        && exists(/databases/$(db)/documents/careLinks/$(patientId + '_' + request.auth.uid))
        && link(patientId).status == 'active'
        && link(patientId).permissions[perm] == true;
    }

    match /users/{uid} {
      allow read, write: if isSelf(uid);

      match /medicines/{id} {
        allow read:  if isSelf(uid) || hasPerm(uid, 'canViewLogs');
        allow write: if isSelf(uid) || hasPerm(uid, 'canEditSchedules');
      }
      match /schedules/{id} {
        allow read:  if isSelf(uid) || hasPerm(uid, 'canViewLogs');
        allow write: if isSelf(uid) || hasPerm(uid, 'canEditSchedules');
      }
      match /doseLogs/{id} {
        allow read: if isSelf(uid) || hasPerm(uid, 'canViewLogs');
        allow create, update: if isSelf(uid)
          || (hasPerm(uid, 'canMarkDoses')
              && request.resource.data.markedBy == request.auth.uid);
        allow delete: if false;   // geçmiş silinmez (rapor güvenilirliği)
      }
      match /devices/{id}       { allow read, write: if isSelf(uid); }
      match /scannedPacks/{id}  { allow read, write: if isSelf(uid); }
      match /prescriptions/{id} { allow read, write: if isSelf(uid); }
    }

    match /careLinks/{linkId} {
      allow read: if signedIn()
        && (resource.data.patientId == request.auth.uid || resource.data.caregiverId == request.auth.uid);
      // Sadece hasta izinleri/durumu güncelleyebilir; bakım veren yalnızca kendini "revoked" yapabilir
      allow update: if signedIn() && (
        resource.data.patientId == request.auth.uid
        || (resource.data.caregiverId == request.auth.uid
            && request.resource.data.diff(resource.data).affectedKeys().hasOnly(['status', 'updatedAt'])
            && request.resource.data.status == 'revoked'));
      allow create, delete: if false;   // yalnızca Cloud Functions (Admin SDK)
    }

    match /drugCatalog/{id}   { allow read: if signedIn(); allow write: if false; }
    match /catalogMeta/{id}   { allow read: if signedIn(); allow write: if false; }
    match /drugSubmissions/{id} {
      allow create: if signedIn() && request.resource.data.submittedBy == request.auth.uid;
    }
    match /careInvites/{id}   { allow read, write: if false; }  // Cloud Functions
    match /reportShares/{id}  { allow read, write: if false; }  // Cloud Functions
    match /pharmacyCache/{id} { allow read: if signedIn(); allow write: if false; }
  }
}
```

## 2.3 Raporlama: uyum oranı + PDF/QR

### Tanımlar

| Durum | Koşul |
|---|---|
| **Zamanında alındı** | `taken` ve `|actionTime − scheduledTime| ≤ onTimeWindow` (varsayılan ±60 dk; ilaç bazında ayarlanabilir) |
| **Geç alındı** | `taken` ama pencere dışında |
| **Atlandı** | Kullanıcı bilinçli olarak `skipped` seçti (nedeni ile: "yan etki", "doktor önerisi", "ilaç bitti"…) |
| **Kaçırıldı** | Hiç işlem yapılmadı; `graceUntil` geçti → sunucu `missed` yaptı |
| Hariç tutulur | Gelecekteki dozlar, `asNeeded` (gerektiğinde) ilaçlar, duraklatılmış planlar |

```
Uyum oranı (%)          = (zamanında + geç) / (planlanan − hariç) × 100
Zamanında alım oranı (%) = zamanında / (planlanan − hariç) × 100
```

### Hesaplayıcı (saf Dart — birim testi kolay)

```dart
class AdherenceStats {
  final int planned, onTime, late, skipped, missed;
  const AdherenceStats(this.planned, this.onTime, this.late, this.skipped, this.missed);
  double get adherence => planned == 0 ? 0 : (onTime + late) / planned;
  double get onTimeRate => planned == 0 ? 0 : onTime / planned;
}

AdherenceStats computeAdherence(
  Iterable<DoseLog> logs, {
  required DateTime from,
  required DateTime to,
  required DateTime now,
  Duration onTimeWindow = const Duration(minutes: 60),
}) {
  var planned = 0, onTime = 0, late = 0, skipped = 0, missed = 0;
  for (final l in logs) {
    if (l.isAsNeeded || l.scheduledTime.isBefore(from) || !l.scheduledTime.isBefore(to)) continue;
    if (l.scheduledTime.isAfter(now)) continue;
    // Henüz grace süresi dolmamış bekleyen doz → rapora girmez
    if ((l.status == DoseStatus.pending || l.status == DoseStatus.snoozed) && l.graceUntil.isAfter(now)) continue;

    planned++;
    switch (l.status) {
      case DoseStatus.taken:
        final diff = l.actionTime!.difference(l.scheduledTime).abs();
        diff <= onTimeWindow ? onTime++ : late++;
      case DoseStatus.skipped:
        skipped++;
      default:
        missed++; // missed veya süresi dolmuş pending/snoozed
    }
  }
  return AdherenceStats(planned, onTime, late, skipped, missed);
}
```

> Rapor verisi büyük aralıklarda (90 gün × 4 doz × 5 ilaç ≈ 1.800 kayıt) yerel `drift` veritabanından okunur; Firestore okuma maliyeti oluşmaz. Gece Cloud Function ile `users/{uid}/dailyStats/{yyyy-mm-dd}` özetleri üretilirse bakım veren paneli ve web raporu hızlanır.

### PDF içeriği

1. Başlık: hasta adı (veya baş harfleri — gizlilik seçeneği), doğum yılı, tarih aralığı, oluşturulma tarihi, dil (TR/EN).
2. Özet kutuları: **Genel uyum %**, zamanında %, atlanan, kaçırılan.
3. İlaç bazında tablo: ilaç, doz planı, aç/tok, planlanan / alınan / geç / atlanan / kaçırılan, uyum %.
4. Günlük ısı haritası (takvim ızgarası: yeşil = tümü alındı, sarı = kısmi, kırmızı = kaçırıldı).
5. Atlama nedenleri ve kullanıcı notları ("baş dönmesi yaptı").
6. **QR kod** → doktorun telefonunda açılan, süreli web raporu (aynı veri, etkileşimli).
7. Alt bilgi: "Bu rapor hastanın kendi beyanına dayanır; tıbbi teşhis yerine geçmez."

### PDF üretimi (`pdf` + `printing` + `share_plus`)

```dart
Future<Uint8List> buildAdherencePdf(ReportData d, AppLocalizations t) async {
  final font = pw.Font.ttf(await rootBundle.load('assets/fonts/NotoSans-Regular.ttf')); // Türkçe karakterler için
  final bold = pw.Font.ttf(await rootBundle.load('assets/fonts/NotoSans-Bold.ttf'));
  final doc = pw.Document(theme: pw.ThemeData.withFont(base: font, bold: bold));

  doc.addPage(pw.MultiPage(
    pageFormat: PdfPageFormat.a4,
    header: (_) => pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceBetween, children: [
      pw.Text(t.reportTitle, style: pw.TextStyle(fontSize: 18, fontWeight: pw.FontWeight.bold)),
      pw.Text('${d.fromLabel} – ${d.toLabel}'),
    ]),
    build: (_) => [
      pw.Row(children: [
        _statBox(t.adherence, '${(d.total.adherence * 100).toStringAsFixed(0)}%'),
        _statBox(t.onTime, '${(d.total.onTimeRate * 100).toStringAsFixed(0)}%'),
        _statBox(t.skipped, '${d.total.skipped}'),
        _statBox(t.missed, '${d.total.missed}'),
      ]),
      pw.SizedBox(height: 16),
      pw.TableHelper.fromTextArray(
        headers: [t.medicine, t.plan, t.planned, t.taken, t.skipped, t.missed, '%'],
        data: d.perMedicine.map((m) => [
          m.name, m.planLabel, '${m.stats.planned}', '${m.stats.onTime + m.stats.late}',
          '${m.stats.skipped}', '${m.stats.missed}', (m.stats.adherence * 100).toStringAsFixed(0),
        ]).toList(),
      ),
      pw.SizedBox(height: 16),
      _heatmap(d.daily),
      if (d.shareUrl != null) ...[
        pw.SizedBox(height: 16),
        pw.Row(children: [
          pw.BarcodeWidget(barcode: pw.Barcode.qrCode(), data: d.shareUrl!, width: 90, height: 90),
          pw.SizedBox(width: 12),
          pw.Expanded(child: pw.Text(t.reportQrHint(d.shareExpiresLabel!))),
        ]),
      ],
      pw.SizedBox(height: 12),
      pw.Text(t.reportDisclaimer, style: const pw.TextStyle(fontSize: 8)),
    ],
  ));
  return doc.save();
}

// Önizleme / yazdırma / paylaşma
await Printing.layoutPdf(onLayout: (_) => buildAdherencePdf(data, l10n));
// veya: await Printing.sharePdf(bytes: bytes, filename: 'uyum-raporu.pdf');
```

### Doktor için süreli paylaşım linki (QR)

Doktorun uygulama kurması beklenmez. Hasta "Doktorumla Paylaş" der → tarih aralığı + süre (24 saat / 7 gün) seçer → Cloud Function rastgele 128-bit token üretir.

```json
// reportShares/{token}   (token = 22 karakterlik base64url, tahmin edilemez)
{
  "patientId": "user_xyz789",
  "range": { "from": "2026-10-01", "to": "2026-10-31" },
  "medicineIds": null,
  "locale": "tr",
  "createdAt": "2026-11-04T12:00:00Z",
  "expiresAt": "2026-11-11T12:00:00Z",
  "revoked": false,
  "viewCount": 0,
  "lastViewedAt": null
}
```

- URL: `https://<proje>.web.app/r/{token}` → Firebase Hosting rewrite → HTTPS Cloud Function `renderReport`: token geçerliyse salt okunur HTML rapor döner (Firestore kurallarına **public erişim açılmaz**).
- Hasta, "Aktif paylaşımlarım" listesinden linki anında iptal edebilir; her görüntüleme `viewCount`'u artırır ve hastaya "Raporunuz görüntülendi" bildirimi gidebilir.
- Rate limit: aynı IP'den dakikada N istek; token kaba kuvvet denemelerine karşı 404 ve sabit gecikme.

## 2.4 Canlı takip ve anlık bildirim (FCM)

### Neden sunucu tarafı?
Hasta telefonu kapalı, çevrimdışı ya da pil bitmişse "kaçırıldı" bilgisini hasta cihazı **gönderemez**. Bu yüzden:
- **Alındı / atlandı** → hasta cihazı `doseLogs`'u günceller → **Firestore tetikleyicisi** bakım verene bildirir.
- **Kaçırıldı** → **zamanlanmış Cloud Function** (her 5 dk) süresi dolan bekleyen dozları `missed` yapar → aynı tetikleyici bildirir.

Bunun için sunucunun gelecekteki dozları bilmesi gerekir: hasta cihazı önümüzdeki **7 günün** doz örneklerini `doseLogs` içine `status: "pending"` olarak önceden yazar (plan değiştiğinde yeniden üretir). Bu, MVP'deki `logs` modelinin genişletilmiş hâlidir:

### `users/{uid}/doseLogs/{logId}`

```json
{
  "logId": "sch_111_20261104T0900",
  "scheduleId": "sch_111",
  "medicineId": "med_abc123",
  "userId": "user_xyz789",
  "scheduledTime": "2026-11-04T09:00:00Z",
  "graceUntil": "2026-11-04T09:30:00Z",
  "actionTime": "2026-11-04T09:05:00Z",
  "status": "taken",
  "snoozeCount": 1,
  "snoozedUntil": null,
  "skipReason": null,
  "markedBy": "user_xyz789",
  "isCritical": false,
  "isAsNeeded": false,
  "note": "Tok karnına içildi",
  "updatedAt": "2026-11-04T09:05:02Z"
}
```

`status`: `pending` | `snoozed` | `taken` | `skipped` | `missed`
`logId` deterministiktir (`scheduleId_yyyyMMddTHHmm`) → aynı doz iki kez oluşturulamaz, çevrimdışı çakışmalar birleşir.

### Cloud Functions (TypeScript, v2)

```ts
// functions/src/care.ts
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();
const db = getFirestore();
const REGION = "europe-west1";

/** 1) Doz durumu değiştiğinde bakım verenlere bildir */
export const onDoseStatusChanged = onDocumentUpdated(
  { document: "users/{patientId}/doseLogs/{logId}", region: REGION },
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;
    if (!["taken", "skipped", "missed"].includes(after.status)) return;

    const { patientId } = event.params;
    const links = await db.collection("careLinks")
      .where("patientId", "==", patientId)
      .where("status", "==", "active")
      .where("permissions.canReceiveAlerts", "==", true)
      .get();

    const [patient, medicine] = await Promise.all([
      db.doc(`users/${patientId}`).get(),
      db.doc(`users/${patientId}/medicines/${after.medicineId}`).get(),
    ]);

    await Promise.all(links.docs.map(async (l) => {
      const link = l.data();
      const pref = link.alertPrefs ?? {};
      const wants =
        (after.status === "taken" && pref.onTaken) ||
        (after.status === "skipped" && pref.onSkipped !== false) ||
        (after.status === "missed" && pref.onMissed !== false);
      if (!wants) return;
      if (inQuietHours(pref.quietHours) && !(after.isCritical && pref.quietHours?.exceptCritical)) return;

      const devices = await db.collection(`users/${link.caregiverId}/devices`).get();
      for (const d of devices.docs) {
        const { token, locale } = d.data();
        const msg = buildMessage(after.status, locale ?? "tr", {
          patient: patient.get("displayName"), medicine: medicine.get("name"),
        });
        try {
          await getMessaging().send({
            token,
            notification: msg,
            data: { type: "dose_status", patientId, logId: event.params.logId, status: after.status },
            android: {
              priority: "high",
              notification: { channelId: after.status === "missed" ? "care_critical" : "care_info" },
            },
            apns: { payload: { aps: { sound: "default",
              "interruption-level": after.status === "missed" ? "time-sensitive" : "active" } } },
          });
        } catch (e: any) {
          if (e.code === "messaging/registration-token-not-registered") await d.ref.delete();
        }
      }
    }));
  });

/** 2) Her 5 dakikada süresi dolan dozları "missed" yap */
export const markMissedDoses = onSchedule(
  { schedule: "every 5 minutes", region: REGION, timeZone: "Europe/Istanbul" },
  async () => {
    const now = Timestamp.now();
    // Bileşik indeks: collectionGroup doseLogs (status ASC, graceUntil ASC)
    const due = await db.collectionGroup("doseLogs")
      .where("status", "in", ["pending", "snoozed"])
      .where("graceUntil", "<=", now)
      .limit(500)
      .get();

    const writer = db.bulkWriter();
    due.docs.forEach((d) => writer.update(d.ref, { status: "missed", updatedAt: now }));
    await writer.close(); // her güncelleme onDoseStatusChanged'i tetikler
  });

function buildMessage(status: string, locale: string, p: { patient: string; medicine: string }) {
  const tr: Record<string, { title: string; body: string }> = {
    taken:   { title: "✅ İlaç alındı",    body: `${p.patient}, ${p.medicine} ilacını aldı.` },
    skipped: { title: "⚠️ İlaç atlandı",   body: `${p.patient}, ${p.medicine} dozunu atladı.` },
    missed:  { title: "🔴 İlaç kaçırıldı", body: `${p.patient}, ${p.medicine} dozunu almadı. Aramak ister misiniz?` },
  };
  const en: Record<string, { title: string; body: string }> = {
    taken:   { title: "✅ Dose taken",   body: `${p.patient} took ${p.medicine}.` },
    skipped: { title: "⚠️ Dose skipped", body: `${p.patient} skipped ${p.medicine}.` },
    missed:  { title: "🔴 Dose missed",  body: `${p.patient} hasn't taken ${p.medicine}. Call them?` },
  };
  return (locale.startsWith("en") ? en : tr)[status];
}

function inQuietHours(q?: { start: string; end: string }): boolean {
  if (!q) return false;
  const now = new Date().toLocaleTimeString("tr-TR", { timeZone: "Europe/Istanbul", hour12: false }).slice(0, 5);
  return q.start <= q.end ? now >= q.start && now < q.end : now >= q.start || now < q.end;
}
```

> **Ölçek notu:** "Her 5 dk" yaklaşımı ±5 dk hassasiyet verir ve küçük/orta ölçekte ucuzdur. Dakika hassasiyeti gerekirse her `pending` doz yazıldığında `graceUntil` zamanına **Cloud Tasks** görevi kuyruklanır (görev çalıştığında doz hâlâ bekliyorsa `missed` yapılır).
> **Sessiz saatler** için alıcının saat dilimi `users/{uid}.timeZone` alanından okunmalıdır (örnekte sabit İstanbul).

### Flutter tarafı (bakım veren cihazı)

```dart
Future<void> initPush(String uid, String locale) async {
  final fm = FirebaseMessaging.instance;
  await fm.requestPermission(alert: true, badge: true, sound: true);
  final token = await fm.getToken();
  final deviceId = await DeviceIdStore.get(); // ilk açılışta üretilen UUID
  Future<void> save(String? t) => FirebaseFirestore.instance
      .doc('users/$uid/devices/$deviceId')
      .set({'token': t, 'platform': Platform.operatingSystem, 'locale': locale,
            'updatedAt': FieldValue.serverTimestamp()}, SetOptions(merge: true));
  await save(token);
  fm.onTokenRefresh.listen(save);

  FirebaseMessaging.onMessageOpenedApp.listen((m) {
    if (m.data['type'] == 'dose_status') {
      router.push('/care/${m.data['patientId']}'); // hastanın günlük paneli
    }
  });
}
```

Android'de `care_critical` (yüksek önem, özel ses) ve `care_info` (varsayılan) kanalları uygulama açılışında `flutter_local_notifications` ile oluşturulur.

### Bakım veren paneli (UX)

```
┌──────────────────────────────┐
│ Annem (Fatma)    🟢 Bugün %75 │
│ ───────────────────────────── │
│ 🔴 09:00 Coraspin   KAÇIRILDI │  [📞 Ara]
│ ✅ 13:00 Glifor     13:04     │
│ ⏳ 20:00 Glifor     bekliyor   │
│ 📦 Glifor stok: 4 gün kaldı   │  [Eczane bul]
│ ───────────────────────────── │
│ [ Haftalık rapor ]  [ Ayarlar ]│
└──────────────────────────────┘
```

---

# 3. Eczane Entegrasyonu ve Konum Servisleri

## 3.1 Harita ve konum mimarisi

**Paketler:** `geolocator` (konum), `permission_handler`, `google_maps_flutter` **veya** `flutter_map` + `latlong2` (OSM karoları), `url_launcher` (arama/yol tarifi), `cached_network_image`.

| Seçenek | Artı | Eksi |
|---|---|---|
| **Google Maps + Places API (New)** | Zengin veri (açık/kapalı, telefon, puan), tanıdık arayüz | Kullanım başına ücret, API anahtarı yönetimi |
| **OpenStreetMap (`flutter_map`) + Overpass API** | Ücretsiz, anahtar yok | Eczane verisi bölgeye göre eksik olabilir; açık/kapalı bilgisi zayıf; genel Overpass sunucularında kullanım limitleri var |

**Öneri:** Harita görünümü için soyut bir `MapView` bileşeni; veri için `PharmacyProvider` arayüzü. Varsayılan olarak **Places API (New) Nearby Search** bir **Cloud Function proxy** üzerinden çağrılır (anahtar istemciye gömülmez, sonuçlar önbelleğe alınır, kota kontrol edilir). OSM yedek sağlayıcı olarak kalır.

```
Flutter ──callable──▶ CF nearbyPharmacies(lat,lng,radius)
                         ├─ geohash(6) önbellek anahtarı → pharmacyCache (TTL 24 saat)
                         ├─ Places API (New) searchNearby  includedTypes:["pharmacy"]
                         └─ yedek: Overpass  node["amenity"="pharmacy"](around:r,lat,lng)
Flutter ──callable──▶ CF dutyPharmacies(il, ilce)
                         ├─ pharmacyCache/{il_ilce_nobetTarihi}
                         └─ DutyPharmacySource (bkz. 3.2)
```

Places API (New) isteği (sunucuda):

```ts
const res = await fetch("https://places.googleapis.com/v1/places:searchNearby", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "X-Goog-Api-Key": process.env.PLACES_KEY!,   // Secret Manager
    "X-Goog-FieldMask": "places.id,places.displayName,places.location,places.formattedAddress," +
                        "places.nationalPhoneNumber,places.currentOpeningHours.openNow",
  },
  body: JSON.stringify({
    includedTypes: ["pharmacy"],
    maxResultCount: 20,
    rankPreference: "DISTANCE",
    locationRestriction: { circle: { center: { latitude: lat, longitude: lng }, radius: 3000 } },
    languageCode: locale,
  }),
});
```

> `X-Goog-FieldMask` yalnızca gereken alanları ister → maliyet düşer.

## 3.2 Nöbetçi eczaneler

Türkiye'de nöbetçi eczane listesi **il eczacı odaları** tarafından yayınlanır; tüm ülke için resmi ve ücretsiz tek bir API **yoktur**. Seçenekler:

1. **Ticari nöbetçi eczane API'leri** (il/ilçe bazında JSON döner, aylık ücretli) — en hızlı başlangıç.
2. **İl eczacı odası sayfaları** — kullanım koşulları izin veriyorsa ve yazılı izin alınarak; il bazlı ayrıştırıcı bakımı gerekir.
3. **Yedek:** Google'da "nöbetçi eczane {ilçe}" araması veya e-Devlet/odalar sayfasına yönlendirme (veri yoksa kullanıcı yine de çözüme ulaşır).

Mimari olarak hepsi aynı arayüzü uygular, il bazında yapılandırılır:

```ts
interface DutyPharmacySource {
  supports(il: string): boolean;
  fetch(il: string, ilce?: string): Promise<DutyPharmacy[]>;
}
```

### `pharmacyCache/{il}_{ilce}_{nobetTarihi}`

```json
{
  "il": "istanbul",
  "ilce": "kadikoy",
  "dutyDate": "2026-11-04",
  "validUntil": "2026-11-05T08:30:00+03:00",
  "source": "provider_x",
  "pharmacies": [
    {
      "name": "Örnek Eczanesi",
      "address": "Caferağa Mah. …",
      "phone": "+902160000000",
      "location": { "lat": 40.987, "lng": 29.028 },
      "geohash": "sxk9…",
      "note": "Hastane karşısı"
    }
  ],
  "fetchedAt": "2026-11-04T17:05:00Z"
}
```

> Nöbet dönemleri genellikle akşamdan ertesi sabaha kadar sürer ve geçiş saati ilden ile değişebilir; `validUntil` alanı sağlayıcıdan alınır, bilinmiyorsa temkinli kısa bir TTL kullanılır. Ekranda her zaman "Son güncelleme: 17:05 · Gitmeden önce aramanızı öneririz" yazar.

### Flutter: liste + harita ekranı

```dart
class PharmacyScreen extends ConsumerWidget {
  const PharmacyScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tab = ref.watch(pharmacyTabProvider);           // yakındakiler | nöbetçiler
    final list = ref.watch(pharmaciesProvider(tab));      // AsyncValue<List<Pharmacy>>
    return Scaffold(
      appBar: AppBar(title: Text(context.l10n.pharmacies)),
      body: Column(children: [
        SegmentedButton<PharmacyTab>(
          segments: [
            ButtonSegment(value: PharmacyTab.nearby, label: Text(context.l10n.nearby)),
            ButtonSegment(value: PharmacyTab.onDuty, label: Text(context.l10n.onDuty)),
          ],
          selected: {tab},
          onSelectionChanged: (s) => ref.read(pharmacyTabProvider.notifier).state = s.first,
        ),
        SizedBox(height: 260, child: PharmacyMap(pharmacies: list.valueOrNull ?? const [])),
        Expanded(
          child: list.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (e, _) => PharmacyErrorView(onSearchWeb: () => openWebFallback(ref)),
            data: (items) => ListView.builder(
              itemCount: items.length,
              itemBuilder: (_, i) => PharmacyTile(
                pharmacy: items[i],
                onCall: () => launchUrl(Uri.parse('tel:${items[i].phone}')),
                onDirections: () => launchUrl(
                  Uri.parse('https://www.google.com/maps/dir/?api=1'
                      '&destination=${items[i].lat},${items[i].lng}'),
                  mode: LaunchMode.externalApplication,
                ),
              ),
            ),
          ),
        ),
      ]),
    );
  }
}
```

Konum izni reddedilirse: il/ilçe seçici (Türkiye 81 il + ilçe listesi yerel JSON) ile çalışır; konum yalnızca "uygulama kullanılırken" istenir, arka planda konum **kullanılmaz**.

## 3.3 Reçete ve e-reçete mantığı

### Gerçekçi kapsam
E-reçete sistemi (SGK Medula, e-Nabız) üçüncü taraf hasta uygulamalarına açık bir API sunmaz. Bu nedenle uygulama **reçeteyi yazmaz veya sorgulamaz**; kullanıcının elindeki bilgiyi (doktorun SMS ile verdiği **e-reçete kodu**, reçete tarihi, reçete türü, kaç kutu yazıldığı) saklar ve **zamanında hatırlatır**.

### `users/{uid}/prescriptions/{rxId}`

```json
{
  "rxId": "rx_001",
  "medicineIds": ["med_abc123"],
  "eRxCode": "1A2B3C4",
  "type": "normal",
  "doctorName": "Dr. Ahmet Yılmaz",
  "issuedAt": "2026-10-01",
  "validUntil": null,
  "boxesPrescribed": 3,
  "boxesFilled": 1,
  "isChronic": true,
  "renewLeadDays": 7,
  "photoUrl": "gs://…/rx_001.jpg",
  "createdAt": "2026-10-01T15:00:00Z"
}
```

> `validUntil` kullanıcı tarafından girilir (reçete türüne göre değişir); uygulama yasal süre varsaymaz.

### Stok tükenme tahmini

```dart
/// Plan kurallarından son 14 günün gerçek tüketimine ağırlık vererek günlük tüketimi tahmin eder.
DateTime? estimateRunOut(Medicine m, List<DoseRule> rules, List<DoseLog> last14d, DateTime now) {
  final planned = rules.fold<double>(0, (s, r) => s + r.averageDailyQuantity()); // ör. 8 saatte bir 1 tb → 3
  final actual = last14d.where((l) => l.status == DoseStatus.taken)
      .fold<double>(0, (s, l) => s + l.quantity) / 14;
  final daily = actual > 0 ? (0.6 * planned + 0.4 * actual) : planned;
  if (daily <= 0) return null;               // "gerektiğinde" ilaç
  final days = m.totalStock / daily;
  return now.add(Duration(hours: (days * 24).floor()));
}
```

### Hatırlatıcı kuralları

| Tetikleyici | Eylem |
|---|---|
| `totalStock ≤ criticalStockLevel` | "📦 Glifor için 4 gün kaldı" bildirimi → **[Eczane bul] [Reçeteyi göster] [Ertele]** |
| Tükenme tarihi − `renewLeadDays` (varsayılan 7) | "📝 Reçete yenileme zamanı" → kronik ilaçsa **[Doktor randevusu]** (MHRS / hastane uygulamasına dış bağlantı) |
| Tükenme tarihi hafta sonu / resmi tatile denk geliyorsa | Hatırlatma önceki iş gününe çekilir (resmi tatil listesi yerel JSON) |
| `boxesFilled < boxesPrescribed` | "Reçetenizde 2 kutu daha var, eczaneden alabilirsiniz" (doktora gitmeye gerek yok) |
| SKT − 30 gün (karekoddan) | "⏰ Bir kutunun son kullanma tarihi yaklaşıyor" |
| Stok 0 ve doz zamanı | Alarm ekranında "Stok bitti — yine de aldım / Eczane bul" |

Hatırlatıcılar **günlük yerel kontrol** (`workmanager` periyodik görevi + uygulama açılışı) ile hesaplanır; bakım verende `canReceiveStockAlerts` varsa Cloud Function `onDocumentUpdated(medicines)` ile ona da bildirilir.

### Eczane yönlendirme akışı

```
Bildirim "Glifor bitmek üzere"
   └─[Eczane bul]─▶ Eczane ekranı (saat 19:00 sonrası ise "Nöbetçiler" sekmesi otomatik seçili)
                      └─ Eczane kartı: [📞 Ara] [🧭 Yol tarifi] [📋 E-reçete kodunu kopyala]
                                          └─ eczacıya söylenecek kod büyük punto ile gösterilir
```

> "E-reçete kodunu göster" ekranı eczane tezgâhında kullanılmak üzere tam ekran, yüksek kontrastlı ve ekran parlaklığını artıran (`screen_brightness`) bir görünümdür.

---

## 4. Uygulama yol haritası (öneri)

| Sprint | İçerik |
|---|---|
| S1 (2 hf) | Katalog içe aktarma CF + yerel SQLite paketi, GS1 ayrıştırıcı (+ birim testleri), tarama ekranı |
| S2 (2 hf) | Önizleme / stoğa ekleme / manuel giriş + OCR, `scannedPacks`, SKT hatırlatıcısı |
| S3 (2 hf) | careLinks davet akışı, güvenlik kuralları + emulator testleri, FCM tetikleyicileri, kaçırılan doz CF |
| S4 (2 hf) | Uyum hesaplayıcı, PDF + QR, doktor web raporu (Hosting + CF) |
| S5 (2 hf) | Eczane proxy CF, harita/liste, nöbetçi sağlayıcı, reçete modeli ve yenileme hatırlatıcıları |

**Test notları:** Firestore kuralları `@firebase/rules-unit-testing` ile emülatörde; Cloud Functions `firebase-functions-test`; GS1 ayrıştırıcı ve uyum hesaplayıcı saf Dart birim testleri; tarama akışı gerçek ilaç kutularıyla (en az 20 farklı üretici) saha testi.

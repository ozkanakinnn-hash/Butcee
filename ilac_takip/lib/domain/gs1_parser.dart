/// Türkiye ilaç karekodu (GS1 DataMatrix) ve EAN-13 barkod ayrıştırıcı.
///
/// Karekod alanları: (01) GTIN-14 · (21) seri no · (17) son kullanma YYAAGG · (10) lot.
library;

class ScannedDrugCode {
  const ScannedDrugCode(this.barcode13, {this.serial, this.expiry, this.lot});

  /// Katalog anahtarı (GTIN-14'ün baştaki 0'ı atılmış hâli).
  final String barcode13;
  final String? serial;
  final DateTime? expiry;
  final String? lot;

  bool get isKarekod => serial != null;

  @override
  String toString() =>
      'ScannedDrugCode($barcode13, serial: $serial, expiry: $expiry, lot: $lot)';
}

class Gs1Parser {
  Gs1Parser._();

  static const _gs = '\u001d'; // FNC1 / Group Separator
  static const _fixedLengths = {'01': 14, '17': 6, '11': 6, '15': 6};
  static const _symbologyPrefixes = [']d2', ']C1', ']Q3', ']e0'];

  static ScannedDrugCode? parse(String raw) {
    var s = raw.trim();
    for (final p in _symbologyPrefixes) {
      if (s.startsWith(p)) {
        s = s.substring(p.length);
        break;
      }
    }
    if (s.isEmpty) return null;

    // Düz EAN-13 / GTIN-14
    if (RegExp(r'^\d{13}$').hasMatch(s)) return ScannedDrugCode(s);
    if (RegExp(r'^\d{14}$').hasMatch(s)) return ScannedDrugCode(_toBarcode13(s));

    // İnsan-okur biçim: "(01)08699...(21)ABC"
    if (s.startsWith('(')) {
      s = s
          .replaceAllMapped(RegExp(r'\((\d{2})\)'), (m) => '$_gs${m[1]}')
          .substring(1);
    }
    if (s.startsWith(_gs)) s = s.substring(1);

    final ais = <String, String>{};
    var i = 0;
    while (i + 2 <= s.length) {
      if (s[i] == _gs) {
        i++;
        continue;
      }
      final ai = s.substring(i, i + 2);
      if (!RegExp(r'^\d{2}$').hasMatch(ai)) return null;
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
    if (gtin == null || !RegExp(r'^\d{14}$').hasMatch(gtin)) return null;
    final exp = ais['17'];
    return ScannedDrugCode(
      _toBarcode13(gtin),
      serial: ais['21'],
      lot: ais['10'],
      expiry: exp != null ? parseExpiry(exp) : null,
    );
  }

  static String _toBarcode13(String gtin14) =>
      gtin14.length == 14 && gtin14.startsWith('0') ? gtin14.substring(1) : gtin14;

  /// YYAAGG — GG = 00 ise ayın son günü (GS1 kuralı).
  static DateTime? parseExpiry(String v) {
    if (!RegExp(r'^\d{6}$').hasMatch(v)) return null;
    final y = 2000 + int.parse(v.substring(0, 2));
    final m = int.parse(v.substring(2, 4));
    final d = int.parse(v.substring(4, 6));
    if (m < 1 || m > 12 || d > 31) return null;
    return d == 0 ? DateTime(y, m + 1, 0) : DateTime(y, m, d);
  }
}

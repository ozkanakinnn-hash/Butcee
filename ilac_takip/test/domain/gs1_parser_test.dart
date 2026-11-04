import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/domain/gs1_parser.dart';

void main() {
  const gs = '\u001d';

  group('Gs1Parser', () {
    test('düz EAN-13 barkod', () {
      final c = Gs1Parser.parse('8699514010012')!;
      expect(c.barcode13, '8699514010012');
      expect(c.isKarekod, isFalse);
    });

    test('GTIN-14 baştaki 0 atılarak 13 haneye normalize edilir', () {
      expect(Gs1Parser.parse('08699514010012')!.barcode13, '8699514010012');
    });

    test('karekod: GTIN, seri, SKT ve lot (GS ayırıcı ile)', () {
      final c = Gs1Parser.parse('010869951401001221ABC123XYZ${gs}17280331${'10'}B2401')!;
      expect(c.barcode13, '8699514010012');
      expect(c.serial, 'ABC123XYZ');
      expect(c.expiry, DateTime(2028, 3, 31));
      expect(c.lot, 'B2401');
      expect(c.isKarekod, isTrue);
    });

    test(']d2 sembol öneki ve farklı alan sırası', () {
      final c = Gs1Parser.parse(']d2010869951401001217270600${'10'}LOT7${gs}21SN99')!;
      expect(c.barcode13, '8699514010012');
      expect(c.lot, 'LOT7');
      expect(c.serial, 'SN99');
    });

    test('SKT gün = 00 → ayın son günü (şubat, artık yıl)', () {
      final c = Gs1Parser.parse('01086995140100121728020021X')!;
      expect(c.expiry, DateTime(2028, 2, 29));
    });

    test('insan-okur biçim (01)…(21)…', () {
      final c = Gs1Parser.parse('(01)08699514010012(17)281231(21)SER1')!;
      expect(c.barcode13, '8699514010012');
      expect(c.expiry, DateTime(2028, 12, 31));
      expect(c.serial, 'SER1');
    });

    test('geçersiz girdiler null döner', () {
      expect(Gs1Parser.parse(''), isNull);
      expect(Gs1Parser.parse('merhaba'), isNull);
      expect(Gs1Parser.parse('0108699'), isNull); // eksik GTIN
      expect(Gs1Parser.parse('21SADECESERI'), isNull); // GTIN yok
    });

    test('geçersiz ay içeren SKT null olur ama kod okunur', () {
      final c = Gs1Parser.parse('010869951401001217281399')!;
      expect(c.expiry, isNull);
    });
  });
}

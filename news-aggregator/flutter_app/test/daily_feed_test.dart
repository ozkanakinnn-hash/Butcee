import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/backend/schema/articles_record.dart';
import 'package:haber_akisi/pages/home_page.dart';

void main() {
  final now = DateTime(2026, 11, 22, 20, 0);
  ArticlesRecord a(int hoursAgo) => ArticlesRecord.local(
    id: 'a$hoursAgo',
    title: 'Haber $hoursAgo',
    originalUrl: 'https://ornek.com/$hoursAgo',
    publishedAt: now.subtract(Duration(hours: hoursAgo)),
  );

  test('günlük akış: 24 saatten eski haber gösterilmez, eskiye ulaşınca sayfa istenmez', () {
    final r = dailyFeed([a(1), a(5), a(23), a(26), a(40)], now);
    expect(r.items.map((x) => x.id), ['a1', 'a5', 'a23']);
    expect(r.full, isTrue);
  });

  test('günlük akış: en fazla 30 haber', () {
    final list = [for (var i = 0; i < 45; i++) a(0)];
    final r = dailyFeed(list, now);
    expect(r.items.length, 30);
    expect(r.full, isTrue);
  });

  test('günlük akış: son 24 saatte hiç haber yoksa sekme boş kalmaz (en yeni 10)', () {
    final r = dailyFeed([for (var i = 30; i < 50; i++) a(i)], now);
    expect(r.items.length, 10);
    expect(r.items.first.id, 'a30');
  });

  test('günlük akış: sınıra gelinmediyse daha fazla sayfa istenebilir', () {
    final r = dailyFeed([a(1), a(2)], now);
    expect(r.full, isFalse);
  });
}

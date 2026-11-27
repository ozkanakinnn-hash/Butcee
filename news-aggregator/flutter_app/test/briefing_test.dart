import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/pages/briefing_page.dart';
import 'package:haber_akisi/theme.dart';
import 'package:haber_akisi/utils/briefing_slot.dart';
import 'package:shared_preferences/shared_preferences.dart';

Widget _app(Widget home, {bool dark = false}) => MaterialApp(
  theme: dark ? AppTheme.dark() : AppTheme.light(),
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: home,
);

Map<String, dynamic> _briefing(String id) => {
  'id': id,
  'slot': id.endsWith('pm') ? 'pm' : 'am',
  'day': id.substring(0, 10),
  'items': [
    {
      'id': 'a', 'title': 'İkinci ara tatil ne zaman?', 'url': 'https://ornek.com/a', 'source': 'CNN Türk',
      'category_id': 'egitim', 'source_count': 7, 'summary': 'Veliler resmî açıklamayı bekliyor.',
    },
    {'id': 'b', 'title': 'Merkez Bankası faiz kararını açıkladı', 'url': 'https://ornek.com/b', 'source': 'AA', 'category_id': 'son-dakika', 'source_count': 9},
    {'id': 'c', 'title': 'Meclis bütçe görüşmelerine başladı', 'url': 'https://ornek.com/c', 'source': 'NTV', 'category_id': 'gundem', 'source_count': 5},
    {'id': 'd', 'title': 'Hakkinen İstanbul\'a geldi', 'url': 'https://ornek.com/d', 'source': 'Motorsport.com', 'category_id': 'formula-1', 'source_count': 4},
  ],
  'by_category': {},
  'ai_summary': 'Gündemin başında ara tatil var.',
};

void main() {
  group('bülten dilimi (sunucuyla aynı kural, İstanbul = UTC+3)', () {
    test('08:30 öncesi önceki günün akşamı, 08:30–19:00 sabah, 19:00 sonrası akşam', () {
      expect(expectedBriefingId(DateTime.utc(2026, 11, 28, 5, 29)), '2026-11-27-pm'); // 08:29
      expect(expectedBriefingId(DateTime.utc(2026, 11, 28, 5, 30)), '2026-11-28-am'); // 08:30
      expect(expectedBriefingId(DateTime.utc(2026, 11, 28, 15, 59)), '2026-11-28-am'); // 18:59
      expect(expectedBriefingId(DateTime.utc(2026, 11, 28, 16, 0)), '2026-11-28-pm'); // 19:00
      expect(expectedBriefingId(DateTime.utc(2026, 11, 27, 21, 30)), '2026-11-28-am'.replaceAll('28-am', '27-pm')); // 00:30
      expect(isCurrentBriefing({'id': '2026-11-27-pm'}, DateTime.utc(2026, 11, 28, 6)), isFalse);
      expect(isCurrentBriefing({'id': '2026-11-28-am'}, DateTime.utc(2026, 11, 28, 6)), isTrue);
    });
  });

  group('gazete düzeninde gündem ekranı', () {
    setUp(() => SharedPreferences.setMockInitialValues({}));
    final now = DateTime.utc(2026, 11, 28, 6); // 09:00 İstanbul

    testWidgets('güncel bülten: künye, manşet + spot, günün özeti, kısa kısa, son dakika etiketi', (tester) async {
      tester.view.physicalSize = const Size(1080, 4000);
      tester.view.devicePixelRatio = 2.5;
      addTearDown(tester.view.reset);
      final db = FakeFirebaseFirestore();
      await db.doc('briefings/latest').set(_briefing('2026-11-28-am'));
      await tester.pumpWidget(_app(BriefingPage(repository: NewsRepository(firestore: db), now: now)));
      await tester.pumpAndSettle();

      expect(find.text('SABAH BASKISI'), findsNothing, reason: 'baskı adı iki parçalı yazılır');
      expect(find.textContaining('28 KASIM 2026, CUMARTESİ'), findsOneWidget);
      expect(find.text('Haber Akışı'), findsOneWidget);
      expect(find.text('İkinci ara tatil ne zaman?'), findsOneWidget);
      expect(find.text('Veliler resmî açıklamayı bekliyor.'), findsOneWidget);
      expect(find.textContaining('GÜNÜN ÖZETİ'), findsOneWidget);
      expect(find.textContaining('SON DAKİKA'), findsOneWidget);
      expect(find.text('Hakkinen İstanbul\'a geldi'), findsOneWidget);
      expect(find.byType(Image), findsWidgets, reason: 'logo');
    });

    testWidgets('dünkü bülten "bugünkü" gibi gösterilmez; istenirse önceki baskı açılır', (tester) async {
      tester.view.physicalSize = const Size(1080, 4000);
      tester.view.devicePixelRatio = 2.5;
      addTearDown(tester.view.reset);
      final db = FakeFirebaseFirestore();
      await db.doc('briefings/latest').set(_briefing('2026-11-27-pm'));
      await tester.pumpWidget(_app(BriefingPage(repository: NewsRepository(firestore: db), now: now), dark: true));
      await tester.pumpAndSettle();

      expect(find.text('İkinci ara tatil ne zaman?'), findsNothing);
      expect(find.textContaining('hazırlanıyor'), findsOneWidget);
      await tester.tap(find.text('Önceki baskıyı göster'));
      await tester.pumpAndSettle();
      expect(find.text('İkinci ara tatil ne zaman?'), findsOneWidget);
      expect(find.textContaining('AKŞAM'), findsOneWidget);
    });

    testWidgets('bildirimden belirli bülten açılınca tarih kontrolü yapılmaz', (tester) async {
      tester.view.physicalSize = const Size(1080, 4000);
      tester.view.devicePixelRatio = 2.5;
      addTearDown(tester.view.reset);
      final db = FakeFirebaseFirestore();
      await db.doc('briefings/2026-11-27-pm').set(_briefing('2026-11-27-pm'));
      await tester.pumpWidget(_app(BriefingPage(id: '2026-11-27-pm', repository: NewsRepository(firestore: db), now: now)));
      await tester.pumpAndSettle();
      expect(find.text('İkinci ara tatil ne zaman?'), findsOneWidget);
    });
  });
}

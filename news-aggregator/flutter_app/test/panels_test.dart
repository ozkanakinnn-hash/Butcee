import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/pages/notifications_page.dart';
import 'package:haber_akisi/pages/search_page.dart';
import 'package:haber_akisi/services/weather_service.dart';
import 'package:haber_akisi/theme.dart';
import 'package:haber_akisi/utils/search_text.dart';
import 'package:haber_akisi/widgets/market_panel.dart';
import 'package:shared_preferences/shared_preferences.dart';

Widget _app(Widget home) => MaterialApp(
  theme: AppTheme.dark(),
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: home,
);

/// Beklenen değerler sunucudaki JS foldForSearch çıktısından alındı (birebir eşleşmeli).
const _vectors = [
  ("İSTANBUL'da Şiddetli Yağış", "istanbul da siddetli yagis"),
  ("Galatasaray–Fenerbahçe: 3-2", "galatasaray fenerbahce 3 2"),
  ("IŞIK ve ıhlamur ÇİÇEĞİ", "isik ve ihlamur cicegi"),
  ("Müzik & Sinema: Türkiye’nin “en iyi” 10 filmi", "muzik sinema turkiye nin en iyi 10 filmi"),
  ("Ankara Büyükşehir’de ÖĞRETMEN ataması", "ankara buyuksehir de ogretmen atamasi"),
  ("Café Société Ünlü Şef", "cafe societe unlu sef"),
  ("BIST 100 %2,5 yükseldi", "bist 100 2 5 yukseldi"),
  ("Kılıçdaroğlu: Seçim ÜÇ ay sonra", "kilicdaroglu secim uc ay sonra"),
];

void main() {
  test('arama sadeleştirme sunucuyla (JS) birebir aynı', () {
    for (final (input, expected) in _vectors) {
      expect(foldForSearch(input), expected, reason: input);
    }
  });

  test('arama planı: en uzun kelime 10 harfe kısaltılır, kısa kelimeler atlanır', () {
    final p = searchPlan('GS Galatasaray kazandı')!;
    expect(p.key, 'galatasara');
    expect(p.words, ['galatasaray', 'kazandi']);
    expect(searchPlan('ab'), isNull);
  });

  test('Türkçe sayı biçimi', () {
    expect(formatTr(50131.256, 2), '50.131,26');
    expect(formatTr(42.1234, 3), '42,123');
    expect(formatTr(10500, 0), '10.500');
  });

  test('haber dizilimi ayarı saklanır', () async {
    SharedPreferences.setMockInitialValues({});
    final s = await AppSettings.load();
    expect(s.feedLayout, FeedLayout.standard);
    await s.setFeedLayout(FeedLayout.compact);
    expect((await AppSettings.load()).feedLayout, FeedLayout.compact);
  });

  testWidgets('arama: sonuçlar diğer kelimelere göre süzülür, son aramalara eklenir', (tester) async {
    SharedPreferences.setMockInitialValues({});
    final settings = await AppSettings.load();
    final db = FakeFirebaseFirestore();
    Future<void> add(String id, String title, int hour) => db.doc('articles/$id').set({
      'title': title,
      'source_name': 'NTV',
      'original_url': 'https://x/$id',
      'published_at': Timestamp.fromDate(DateTime(2026, 11, 6, hour)),
      'search_keywords': [
        for (final w in foldForSearch('$title NTV').split(' '))
          for (var n = 3; n <= w.length && n <= 10; n++) w.substring(0, n),
      ],
    });
    await add('a', 'Galatasaray Avrupa\'da kazandı', 10);
    await add('b', 'Galatasaray kampa girdi', 12);
    await add('c', 'İstanbul\'da yağış', 11);

    await tester.pumpWidget(
      _app(
        SearchPage(
          settings: settings,
          repository: NewsRepository(firestore: db),
        ),
      ),
    );
    await tester.enterText(find.byType(TextField), 'galatasaray kazan');
    await tester.testTextInput.receiveAction(TextInputAction.search);
    await tester.pumpAndSettle();
    expect(find.text('Galatasaray Avrupa\'da kazandı'), findsOneWidget);
    expect(find.text('Galatasaray kampa girdi'), findsNothing);
    expect(settings.recentSearches, ['galatasaray kazan']);

    await tester.enterText(find.byType(TextField), 'İSTANBUL');
    await tester.pump(const Duration(milliseconds: 500));
    await tester.pumpAndSettle();
    expect(find.text('İstanbul\'da yağış'), findsOneWidget);
  });

  testWidgets('bildirimler: yalnızca kullanıcının konuları, en yeni önce, Son Dakika etiketi', (tester) async {
    SharedPreferences.setMockInitialValues({
      'interests': ['gundem', 'son-dakika'],
    });
    final settings = await AppSettings.load();
    final db = FakeFirebaseFirestore();
    await db.doc('categories/gundem').set({'name': 'Gündem', 'order_index': 1});
    Future<void> log(
      String id,
      String topic,
      String title, {
      String channel = 'news',
      String cat = 'gundem',
      int minutesAgo = 180,
    }) => db.doc('notifications/$id').set({
      'topics': [topic],
      'type': 'article',
      'title': 'Başlık',
      'body': title,
      'article_title': title,
      'source': 'NTV',
      'category_id': cat,
      'channel': channel,
      'url': 'https://x/$id',
      'sent_at': Timestamp.fromDate(DateTime.now().subtract(Duration(minutes: minutesAgo))),
    });
    await log('8000_a', 'news_gundem', 'Eski gündem haberi', minutesAgo: 200);
    await log('7000_b', 'news_son-dakika', 'Yeni son dakika', channel: 'breaking', cat: 'son-dakika', minutesAgo: 190);
    await log('6000_c', 'news_magazin', 'Takip edilmeyen magazin');

    await tester.pumpWidget(
      _app(
        NotificationsPage(
          settings: settings,
          repository: NewsRepository(firestore: db),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('BİLDİRİMLER'), findsOneWidget);
    expect(find.text('Takip edilmeyen magazin'), findsNothing);
    final yeni = tester.getTopLeft(find.text('Yeni son dakika')).dy;
    final eski = tester.getTopLeft(find.text('Eski gündem haberi')).dy;
    expect(yeni < eski, isTrue, reason: 'en yeni üstte');
    expect(find.text('SON DAKİKA'), findsOneWidget);
    expect(find.text('GÜNDEM'), findsOneWidget);
    expect(find.text('3 sa önce'), findsNWidgets(2));
  });

  testWidgets('piyasa paneli değerleri ve değişim yönünü gösterir', (tester) async {
    SharedPreferences.setMockInitialValues({});
    final settings = await AppSettings.load();
    final db = FakeFirebaseFirestore();
    await db.doc('market/latest').set({
      'source': 'Yahoo Finance',
      'updated_at': Timestamp.fromDate(DateTime(2026, 11, 6, 14, 30)),
      'items': [
        {'id': 'usdtry', 'label': 'Dolar', 'unit': '₺', 'value': 42.1234, 'change_pct': 0.35, 'decimals': 4},
        {'id': 'gram_gold', 'label': 'Gram Altın', 'unit': '₺', 'value': 5401.5, 'change_pct': -1.2, 'decimals': 2},
        {'id': 'bist100', 'label': 'BIST 100', 'unit': '', 'value': 10512.3, 'change_pct': null, 'decimals': 2},
      ],
    });
    await tester.pumpWidget(
      _app(
        Scaffold(
          body: MarketPanel(
            settings: settings,
            repository: NewsRepository(firestore: db),
            weather: WeatherService(),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Piyasalar'), findsOneWidget);
    expect(find.text('42,123 ₺'), findsOneWidget);
    expect(find.text('5.402 ₺'), findsOneWidget);
    expect(find.text('10.512'), findsOneWidget);
    expect(find.text('%0,35'), findsOneWidget);
    expect(find.byIcon(Icons.arrow_drop_up), findsOneWidget);
    expect(find.byIcon(Icons.arrow_drop_down), findsOneWidget);
    expect(find.text('Konumundaki hava durumunu göster'), findsOneWidget);
  });
}

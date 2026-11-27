import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/backend/remote_config.dart';
import 'package:haber_akisi/backend/schema/extra_records.dart';
import 'package:haber_akisi/backend/schema/football_records.dart';
import 'package:haber_akisi/pages/briefing_page.dart';
import 'package:haber_akisi/services/notification_service.dart';
import 'package:haber_akisi/theme.dart';
import 'package:haber_akisi/widgets/remote_banner.dart';

Widget _app(Widget home) => MaterialApp(
  theme: AppTheme.light(),
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: Scaffold(body: home),
);

Map<String, dynamic> _briefing(DateTime created, {String ai = 'Bugün gündemde faiz kararı ve bütçe var.'}) => {
  'id': '2026-11-06-am',
  'slot': 'am',
  'title': 'Sabah gündemi',
  'ai_summary': ai,
  'created_at': Timestamp.fromDate(created),
  'items': [
    {'id': 'a', 'title': 'Merkez Bankası faiz kararını açıkladı', 'url': 'https://ornek.com/a', 'source': 'AA', 'source_count': 6},
    {'id': 'b', 'title': 'Meclis bütçe görüşmelerine başladı', 'url': 'https://ornek.com/b', 'source': 'NTV', 'source_count': 1},
    {'id': 'c', 'title': '', 'url': 'https://ornek.com/c'}, // bozuk madde atlanır
  ],
  'by_category': {
    'ekonomi': [
      {'id': 'a', 'title': 'Merkez Bankası faiz kararını açıkladı', 'url': 'https://ornek.com/a'},
    ],
  },
};

void main() {
  group('uzaktan ayar (app_config/main)', () {
    test('belge yoksa varsayılanlar; alanlar okunur, yanlış türler yok sayılır', () {
      const d = RemoteConfig();
      final empty = RemoteConfig.fromMap(null);
      expect(empty.showLiveScores, d.showLiveScores);
      expect(empty.adsEnabled, isTrue);

      final c = RemoteConfig.fromMap({
        'show_live_scores': false,
        'show_market': 'hayır', // yanlış tür → varsayılan
        'ads_enabled': false,
        'announcement': '  Bakım çalışması  ',
        'announcement_level': 'warning',
        'min_build': 12,
        'live_stale_minutes': 1, // alt sınır 5
      });
      expect(c.showLiveScores, isFalse);
      expect(c.showMarket, isTrue);
      expect(c.adsEnabled, isFalse);
      expect(c.announcement, 'Bakım çalışması');
      expect(c.announcementWarning, isTrue);
      expect(c.liveStaleMinutes, 5);
      expect(c.needsUpdate(11), isTrue);
      expect(c.needsUpdate(12), isFalse);
      expect(c.needsUpdate(0), isFalse, reason: 'derleme numarası bilinmiyorsa uyarı yok');
    });

    testWidgets('duyuru ve güncelleme şeridi gösterilir; boşsa yer kaplamaz', (tester) async {
      final config = ValueNotifier(const RemoteConfig());
      await tester.pumpWidget(_app(RemoteBanner(config: config, buildNumber: 5)));
      expect(find.byType(Material), findsWidgets);
      expect(find.textContaining('Bakım'), findsNothing);

      config.value = const RemoteConfig(announcement: 'Bakım çalışması var', minBuild: 6, updateUrl: 'https://play.google.com');
      await tester.pump();
      expect(find.text('Bakım çalışması var'), findsOneWidget);
      expect(find.textContaining('Yeni sürüm'), findsOneWidget);
      expect(find.text('Güncelle'), findsOneWidget);
    });
  });

  group('canlı skor bayatlığı', () {
    final now = DateTime(2026, 11, 23, 21, 0);
    MatchRecord m({required bool live, required DateTime kickoff, DateTime? updated, bool finished = false}) =>
        MatchRecord.fromMap({
          'fixture_id': 1,
          'kickoff': Timestamp.fromDate(kickoff),
          'is_live': live,
          'is_finished': finished,
          if (updated != null) 'updated_at': Timestamp.fromDate(updated),
        });

    test('güncellenmeyen canlı maç ve saati geçtiği hâlde başlamamış görünen maç bayattır', () {
      expect(m(live: true, kickoff: now.subtract(const Duration(minutes: 40)), updated: now.subtract(const Duration(minutes: 2))).isStale(now), isFalse);
      expect(m(live: true, kickoff: now.subtract(const Duration(minutes: 40)), updated: now.subtract(const Duration(minutes: 30))).isStale(now), isTrue);
      expect(m(live: false, kickoff: now.subtract(const Duration(minutes: 90))).isStale(now), isTrue);
      expect(m(live: false, kickoff: now.add(const Duration(hours: 1))).isStale(now), isFalse);
      expect(m(live: false, finished: true, kickoff: now.subtract(const Duration(hours: 3))).isStale(now), isFalse);
    });
  });

  group('5 dakikada gündem', () {
    test('kayıt okunur; bozuk madde atlanır; tazelik', () {
      final created = DateTime(2026, 11, 6, 8, 30);
      final b = BriefingRecord.fromMap(_briefing(created));
      expect(b.items.length, 2);
      expect(b.isMorning, isTrue);
      expect(b.byCategory['ekonomi']!.single.id, 'a');
      expect(b.isFresh(created.add(const Duration(hours: 3))), isTrue);
      expect(b.isFresh(created.add(const Duration(hours: 15))), isFalse);
    });

    testWidgets('ekran: AI özeti, maddeler ve kaynak sayısı', (tester) async {
      final b = BriefingRecord.fromMap(_briefing(DateTime.now()));
      await tester.pumpWidget(_app(BriefingView(briefing: b)));
      expect(find.text('Yapay zekâ özeti'), findsOneWidget);
      expect(find.text('Bugün gündemde faiz kararı ve bütçe var.'), findsOneWidget);
      expect(find.text('Merkez Bankası faiz kararını açıkladı'), findsOneWidget);
      expect(find.text('AA · 6 kaynakta'), findsOneWidget);
      expect(find.text('NTV'), findsOneWidget, reason: 'tek kaynaklıda sayı yazmaz');
    });

    testWidgets('ana sayfa kartı: taze bülten görünür, özet yoksa başlıklar; eski bülten gizlenir', (tester) async {
      final db = FakeFirebaseFirestore();
      final repo = NewsRepository(firestore: db);
      await db.doc('briefings/latest').set(_briefing(DateTime.now(), ai: ''));
      await tester.pumpWidget(_app(BriefingCard(repository: repo)));
      await tester.pumpAndSettle();
      expect(find.text('Sabah gündemi'), findsOneWidget);
      expect(find.textContaining('• Merkez Bankası'), findsOneWidget);

      await db.doc('briefings/latest').set(_briefing(DateTime.now().subtract(const Duration(days: 1))));
      await tester.pumpAndSettle();
      expect(find.text('Sabah gündemi'), findsNothing);
    });

    test('bülten bildirimine dokununca gündem ekranı, diğerleri habere/maça', () {
      final t = NotificationTarget.fromData({'type': 'article', 'url': 'https://x', 'bulletin': '1', 'briefing_id': '2026-11-06-am'});
      expect(t, isA<BriefingTarget>());
      expect((t as BriefingTarget).briefingId, '2026-11-06-am');
      expect(NotificationTarget.fromData({'type': 'article', 'url': 'https://x', 'bulletin': '1', 'briefing_id': ''}), isA<ArticleTarget>());
      expect(NotificationTarget.fromData({'type': 'match', 'fixture_id': '7'}), isA<MatchTarget>());
    });
  });
}

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/remote_config.dart';
import 'package:haber_akisi/backend/schema/football_records.dart';
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

}

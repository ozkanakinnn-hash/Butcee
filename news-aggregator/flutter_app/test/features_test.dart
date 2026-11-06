import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/backend/schema/football_records.dart';
import 'package:haber_akisi/l10n/app_strings.dart';
import 'package:haber_akisi/pages/interests_page.dart';
import 'package:haber_akisi/pages/team_picker_page.dart';
import 'package:haber_akisi/services/notification_service.dart';
import 'package:haber_akisi/services/weather_service.dart';
import 'package:haber_akisi/theme.dart';
import 'package:haber_akisi/widgets/live_matches.dart';
import 'package:shared_preferences/shared_preferences.dart';

Widget _app(Widget home) => MaterialApp(
  theme: AppTheme.light(),
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: home,
);

Map<String, dynamic> _match({String status = '2H', int? elapsed = 67, bool live = true, bool finished = false}) => {
  'fixture_id': 77,
  'kickoff': Timestamp.fromDate(DateTime(2026, 11, 8, 20)),
  'status': {'short': status, 'elapsed': elapsed},
  'home': {
    'id': 'galatasaray',
    'name': 'Galatasaray',
    'short': 'GS',
    'colors': ['#A90432', '#FDB912'],
    'logo': '',
  },
  'away': {
    'id': 'fenerbahce',
    'name': 'Fenerbahçe',
    'short': 'FB',
    'colors': ['#0A1E5E', '#FFED00'],
    'logo': '',
  },
  'goals': {'home': 2, 'away': 1},
  'league': {'round': 'Regular Season - 12'},
  'is_live': live,
  'is_finished': finished,
  'team_ids': ['galatasaray', 'fenerbahce'],
  'goal_events': [
    {'minute': 67, 'extra': 2, 'team_id': 'galatasaray', 'player': 'M. Icardi', 'detail': 'Penalty'},
  ],
};

void main() {
  test('bildirim konuları: ilgi alanları + (futbol takipteyse) takımlar, ayarlara göre', () async {
    SharedPreferences.setMockInitialValues({
      'interests': ['gundem', 'futbol'],
      'followed_teams': ['galatasaray', 'fenerbahce'],
    });
    final s = await AppSettings.load();
    expect(s.desiredTopics, {'news_gundem', 'news_futbol', 'team_galatasaray', 'team_fenerbahce'});
    await s.setMatchNotifications(false);
    expect(s.desiredTopics, {'news_gundem', 'news_futbol'});
    await s.setMatchNotifications(true);
    await s.setNewsNotifications(false);
    expect(s.desiredTopics, {'team_galatasaray', 'team_fenerbahce'});
    await s.setInterests(['gundem']); // futbol bırakılınca takım bildirimleri de durur
    expect(s.desiredTopics, isEmpty);
  });

  test('takım sayısı en fazla 10', () async {
    SharedPreferences.setMockInitialValues({});
    final s = await AppSettings.load();
    await s.setFollowedTeams(List.generate(15, (i) => 'takim$i'));
    expect(s.followedTeams.length, AppSettings.maxTeams);
  });

  test('bildirim verisi → açılacak hedef', () {
    final a = NotificationTarget.fromData({'type': 'article', 'url': 'https://x.com/1', 'title': 'T', 'source': 'NTV'});
    expect(a, isA<ArticleTarget>());
    expect((a as ArticleTarget).url, 'https://x.com/1');
    final m = NotificationTarget.fromData({'type': 'match', 'fixture_id': '1001'});
    expect((m as MatchTarget).fixtureId, 1001);
    expect(NotificationTarget.fromData({'type': 'article'}), isNull);
    expect(NotificationTarget.fromData({'type': 'bilinmeyen'}), isNull);
  });

  test('MET Norway yanıtı ayrıştırılır, sembol Türkçeleştirilir', () {
    final w = WeatherService.parseMetNorway({
      'properties': {
        'timeseries': [
          {
            'data': {
              'instant': {
                'details': {'air_temperature': 14.6},
              },
              'next_1_hours': {
                'summary': {'symbol_code': 'partlycloudy_day'},
              },
            },
          },
        ],
      },
    }, 'İstanbul');
    expect(w!.temperature, 14.6);
    expect(w.baseSymbol, 'partlycloudy');
    expect(describeWeather(w.baseSymbol).tr, 'Parçalı bulutlu');
    expect(describeWeather('lightrain').tr, 'Hafif yağmurlu');
    expect(describeWeather('clearsky', night: true).tr, 'Açık');
    expect(
      WeatherService.parseMetNorway({
        'properties': {'timeseries': []},
      }, ''),
      isNull,
    );
  });

  test('maç kaydı ve durum etiketi', () {
    final s = S.forLanguage('tr');
    final live = MatchRecord.fromMap(_match());
    expect(live.homeGoals, 2);
    expect(live.goals.single.minuteLabel, "67+2'");
    expect(matchStatusLabel(live, s), "CANLI 67'");
    expect(matchStatusLabel(MatchRecord.fromMap(_match(status: 'HT')), s), 'Devre');
    expect(matchStatusLabel(MatchRecord.fromMap(_match(status: 'FT', live: false, finished: true)), s), 'MS');
    final ns = MatchRecord.fromMap(_match(status: 'NS', elapsed: null, live: false));
    expect(matchStatusLabel(ns, s, now: DateTime(2026, 11, 8, 9)), 'Bugün 20:00');
    expect(matchStatusLabel(ns, s, now: DateTime(2026, 11, 7, 9)), 'Yarın 20:00');
  });

  testWidgets('takım seçimi kaydedilir; futbol takipteyse ilgi alanlarında takım satırı görünür', (tester) async {
    SharedPreferences.setMockInitialValues({
      'interests': ['futbol'],
    });
    final settings = await AppSettings.load();
    final db = FakeFirebaseFirestore();
    await db.doc('categories/futbol').set({'name': 'Futbol', 'icon_name': 'sports_soccer', 'order_index': 5});
    await db.doc('football_teams/galatasaray').set({
      'name': 'Galatasaray',
      'short': 'GS',
      'colors': ['#A90432', '#FDB912'],
    });
    await db.doc('football_teams/fenerbahce').set({
      'name': 'Fenerbahçe',
      'short': 'FB',
      'colors': ['#0A1E5E', '#FFED00'],
    });
    await db.doc('football_teams/hatayspor').set({'name': 'Hatayspor', 'short': 'HTY', 'active': false});
    final repo = NewsRepository(firestore: db);

    await tester.pumpWidget(_app(InterestsPage(settings: settings, repository: repo)));
    await tester.pumpAndSettle();
    expect(find.text('Takımını seç'), findsOneWidget);

    await tester.tap(find.text('Takımını seç'));
    await tester.pumpAndSettle();
    expect(find.byType(TeamPickerPage), findsOneWidget);
    await tester.tap(find.text('Galatasaray'));
    await tester.pumpAndSettle();
    expect(settings.followedTeams, ['galatasaray']);
    expect(find.text('Hatayspor'), findsNothing, reason: 'ligde olmayan takım listelenmez');

    await tester.enterText(find.byType(TextField), 'fener');
    await tester.pumpAndSettle();
    expect(find.text('Galatasaray'), findsNothing);
    await tester.tap(find.text('Fenerbahçe'));
    await tester.pumpAndSettle();
    expect(settings.followedTeams, ['galatasaray', 'fenerbahce']);

    await tester.tap(find.text('Tamam'));
    await tester.pumpAndSettle();
    expect(find.text('Takımların'), findsOneWidget);
    expect(find.text('2 takım seçildi'), findsOneWidget);
  });

  testWidgets('maç kartı canlı skoru gösterir', (tester) async {
    MatchRecord? tapped;
    await tester.pumpWidget(
      _app(
        Scaffold(
          body: LiveMatchesStrip(matches: [MatchRecord.fromMap(_match())], onTap: (m) => tapped = m),
        ),
      ),
    );
    await tester.pump();
    expect(find.text("CANLI 67'"), findsOneWidget);
    expect(find.text('2'), findsOneWidget);
    expect(find.text('1'), findsOneWidget);
    await tester.tap(find.text('Galatasaray'));
    expect(tapped?.fixtureId, 77);
  });
}

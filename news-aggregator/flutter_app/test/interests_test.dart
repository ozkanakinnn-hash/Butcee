import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/pages/interests_page.dart';
import 'package:haber_akisi/theme.dart';
import 'package:shared_preferences/shared_preferences.dart';

Future<NewsRepository> _repo() async {
  final db = FakeFirebaseFirestore();
  const cats = [
    ('son-dakika', 'Son Dakika', 'bolt', 0, 'breaking'),
    ('gundem', 'Gündem', 'newspaper', 1, null),
    ('spor', 'Spor', 'sports_soccer', 4, null),
    ('saglik', 'Sağlık', 'volunteer_activism', 6, null),
    ('yasam', 'Yaşam', 'spa', 12, null),
  ];
  for (final (id, name, icon, order, special) in cats) {
    await db.doc('categories/$id').set({'name': name, 'icon_name': icon, 'order_index': order, 'special': ?special});
  }
  return NewsRepository(firestore: db);
}

Widget _app(Widget home, {ThemeMode mode = ThemeMode.light}) => MaterialApp(
  theme: AppTheme.light(),
  darkTheme: AppTheme.dark(),
  themeMode: mode,
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: home,
);

void main() {
  testWidgets('ilk açılış: önerilenler takipte, + ile eklenir, Devam et kaydeder', (tester) async {
    SharedPreferences.setMockInitialValues({});
    final settings = await AppSettings.load();
    final repo = await _repo();
    var done = false;
    var permissionAsked = 0;
    expect(settings.hasChosenInterests, isFalse);

    await tester.pumpWidget(
      _app(
        InterestsPage(
          settings: settings,
          repository: repo,
          onboarding: true,
          onDone: () => done = true,
          requestNotifications: () async => permissionAsked++,
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Neleri okumak istersin?'), findsOneWidget);
    // Önerilenlerden sunucuda olanlar: son-dakika, gundem, spor → 3
    expect(find.text('Devam et (3)'), findsOneWidget);

    await tester.tap(find.byTooltip('Sağlık ekle'));
    await tester.pumpAndSettle();
    expect(find.text('Devam et (4)'), findsOneWidget);
    expect(find.byTooltip('Sağlık kaldır'), findsOneWidget);

    await tester.tap(find.byTooltip('Gündem kaldır'));
    await tester.pumpAndSettle();

    // Onaydan önce kaydedilmez.
    expect(settings.hasChosenInterests, isFalse);
    await tester.tap(find.text('Devam et (3)'));
    await tester.pumpAndSettle();
    expect(done, isTrue);
    expect(permissionAsked, 1, reason: 'kurulumda bildirim izni bir kez sorulur');
    expect(settings.interests, ['son-dakika', 'spor', 'saglik']);
  });

  testWidgets('düzenleme ekranı değişiklikleri anında kaydeder', (tester) async {
    SharedPreferences.setMockInitialValues({
      'interests': ['gundem'],
    });
    final settings = await AppSettings.load();
    await tester.pumpWidget(_app(InterestsPage(settings: settings, repository: await _repo())));
    await tester.pumpAndSettle();

    await tester.tap(find.byTooltip('Yaşam ekle'));
    await tester.pumpAndSettle();
    expect(settings.interests, ['gundem', 'yasam']);
    expect(find.text('Devam et (2)'), findsNothing);
  });

  testWidgets('koyu tema tam siyah zemin kullanır', (tester) async {
    SharedPreferences.setMockInitialValues({'theme_mode': 'dark'});
    final settings = await AppSettings.load();
    expect(settings.themeMode, ThemeMode.dark);
    await tester.pumpWidget(
      _app(
        InterestsPage(settings: settings, repository: await _repo()),
        mode: settings.themeMode,
      ),
    );
    await tester.pumpAndSettle();
    final scaffold = tester.widget<Scaffold>(find.byType(Scaffold));
    final ctx = tester.element(find.byType(Scaffold));
    expect(scaffold.backgroundColor ?? Theme.of(ctx).scaffoldBackgroundColor, Colors.black);
  });
}

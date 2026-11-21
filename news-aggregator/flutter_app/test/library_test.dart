import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/backend/schema/articles_record.dart';
import 'package:haber_akisi/pages/home_page.dart';
import 'package:haber_akisi/pages/library_page.dart';
import 'package:haber_akisi/services/library_store.dart';
import 'package:haber_akisi/theme.dart';
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

ArticlesRecord _article(String id, {String source = 'NTV'}) =>
    ArticlesRecord.local(id: id, title: 'Başlık $id', originalUrl: 'https://x/$id', sourceName: source);

void main() {
  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    await LibraryStore.load();
  });

  test('kaydet / çıkar ve cihazda kalıcılık', () async {
    final lib = LibraryStore.instance;
    final a = _article('a1');
    expect(lib.isSaved(a), isFalse);
    expect(await lib.toggleSaved(a), isTrue);
    expect(lib.isSaved(a), isTrue);

    // Yeniden yükleyince (uygulama yeniden açılınca) kayıt durur.
    final again = await LibraryStore.load();
    expect(again.saved.single.title, 'Başlık a1');
    expect(again.saved.single.toRecord().originalUrl, 'https://x/a1');

    expect(await again.toggleSaved(a), isFalse);
    expect(again.saved, isEmpty);
  });

  test('okuma geçmişi: en yeni önce, tekrar açılan başa geçer, temizlenir', () async {
    final lib = LibraryStore.instance;
    await lib.markRead(_article('a1'));
    await lib.markRead(_article('a2'));
    await lib.markRead(_article('a1'));
    expect(lib.history.map((e) => e.id), ['a1', 'a2']);
    expect(lib.isRead(_article('a2')), isTrue);
    await lib.clearHistory();
    expect(lib.history, isEmpty);
    expect(lib.isRead(_article('a2')), isFalse);
  });

  test('gizlenen kaynak akışta görünmez, geri alınır', () async {
    final lib = LibraryStore.instance;
    await lib.muteSource('Kaynak X');
    expect(lib.isVisible(_article('a1', source: 'Kaynak X')), isFalse);
    expect(lib.isVisible(_article('a2', source: 'NTV')), isTrue);
    await lib.unmuteSource('Kaynak X');
    expect(lib.isVisible(_article('a1', source: 'Kaynak X')), isTrue);
  });

  testWidgets('kart menüsünden kaynak gizlenince akıştan kalkar; kaydet düğmesi Kaydedilenler\'e ekler', (
    tester,
  ) async {
    final db = FakeFirebaseFirestore();
    for (var i = 0; i < 4; i++) {
      await db.doc('articles/a$i').set({
        'title': 'Haber $i',
        'source_name': i.isEven ? 'NTV' : 'Kaynak X',
        'original_url': 'https://x/$i',
        'published_at': Timestamp.fromDate(DateTime(2026, 11, 6, 12).subtract(Duration(minutes: i))),
      });
    }
    await tester.pumpWidget(
      _app(
        Scaffold(
          body: ArticleFeed(repository: NewsRepository(firestore: db), layout: FeedLayout.compact),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.text('Haber 1'), findsOneWidget);

    // Kaynağı gizle (⋮ → "Kaynak X haberlerini gösterme")
    await tester.tap(find.byKey(const ValueKey('more-a1')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('option-mute')));
    await tester.pumpAndSettle();
    expect(find.text('Haber 1'), findsNothing);
    expect(find.text('Haber 3'), findsNothing);
    expect(find.text('Haber 0'), findsOneWidget);

    // Kaydet (⋮ → Kaydet); kartta yer imi işareti belirir
    await tester.tap(find.byKey(const ValueKey('more-a0')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('option-save')));
    await tester.pumpAndSettle();
    expect(find.byKey(const ValueKey('saved-mark-a0')), findsOneWidget);
    expect(LibraryStore.instance.saved.single.id, 'a0');
    expect(find.text('Kaydedilenlere eklendi'), findsOneWidget);
  });

  testWidgets('Kaydedilenler: boş durum ve kaydırarak kaldırma', (tester) async {
    await tester.pumpWidget(_app(const SavedPage()));
    expect(find.text('Henüz kaydedilen haber yok'), findsOneWidget);

    await LibraryStore.instance.toggleSaved(_article('a1'));
    await tester.pumpAndSettle();
    expect(find.text('Başlık a1'), findsOneWidget);

    await tester.drag(find.text('Başlık a1'), const Offset(-600, 0));
    await tester.pumpAndSettle();
    expect(LibraryStore.instance.saved, isEmpty);
    expect(find.text('Geri al'), findsOneWidget);
  });
}

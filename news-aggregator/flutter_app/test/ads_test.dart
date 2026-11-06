import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/ads/ad_widgets.dart';
import 'package:haber_akisi/ads/ads_config.dart';
import 'package:haber_akisi/ads/ads_service.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/pages/home_page.dart';
import 'package:haber_akisi/theme.dart';

void main() {
  test('debug derlemede Google test reklam kimlikleri kullanılır', () {
    expect(AdsConfig.nativeId, startsWith('ca-app-pub-3940256099942544/'));
    expect(AdsConfig.bannerId, startsWith('ca-app-pub-3940256099942544/'));
    expect(AdsConfig.interstitialId, startsWith('ca-app-pub-3940256099942544/'));
  });

  test('reklamlar başlatılmadan (onay yokken) geçiş reklamı gösterilmez', () {
    expect(AdsService.instance.ready, isFalse);
    AdsService.instance.onArticleClosed(); // hata vermemeli, hiçbir şey yapmamalı
  });

  testWidgets('akışta her 6 haberden sonra doğal reklam alanı var; onay yokken yer kaplamaz', (tester) async {
    final db = FakeFirebaseFirestore();
    for (var i = 0; i < 14; i++) {
      await db.doc('articles/a$i').set({
        'title': 'Haber $i',
        'source_name': 'NTV',
        'original_url': 'https://x/$i',
        'published_at': Timestamp.fromDate(DateTime(2026, 11, 6, 12).subtract(Duration(minutes: i))),
      });
    }
    await tester.pumpWidget(
      MaterialApp(
        theme: AppTheme.dark(),
        locale: const Locale('tr'),
        supportedLocales: AppSettings.supportedLocales,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: Scaffold(
          body: ArticleFeed(
            repository: NewsRepository(firestore: db),
            layout: FeedLayout.compact,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    expect(find.byType(NativeAdSlot, skipOffstage: false), findsWidgets);
    // Onay/başlatma yokken reklam alanı boş (SizedBox.shrink) — "Reklam" etiketi görünmez.
    expect(find.text('Reklam'), findsNothing);
  });
}

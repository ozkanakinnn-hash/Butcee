import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ilac_takip/domain/models.dart';
import 'package:ilac_takip/l10n/app_localizations.dart';
import 'package:ilac_takip/services/localized_texts.dart';
import 'package:ilac_takip/ui/theme.dart';
import 'package:ilac_takip/ui/widgets/common.dart';

Widget _wrap(Widget child, Locale locale, {bool senior = false}) => MaterialApp(
      locale: locale,
      supportedLocales: AppLocalizations.supportedLocales,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: senior ? seniorTheme(Brightness.light) : normalTheme(Brightness.light),
      home: Scaffold(body: child),
    );

void main() {
  testWidgets('aç/tok rozeti Türkçe ve İngilizce', (tester) async {
    await tester.pumpWidget(_wrap(const MealBadge(MealRelation.afterMeal, large: true), const Locale('tr')));
    expect(find.textContaining('TOK KARNINA'), findsOneWidget);

    await tester.pumpWidget(_wrap(const MealBadge(MealRelation.afterMeal, large: true), const Locale('en')));
    expect(find.textContaining('AFTER MEAL'), findsOneWidget);
  });

  testWidgets('MealRelation.none rozet çizmez', (tester) async {
    await tester.pumpWidget(_wrap(const MealBadge(MealRelation.none), const Locale('tr')));
    expect(find.byType(Container), findsNothing);
  });

  testWidgets('Basit Mod birincil düğmesi en az 72 dp yüksekliğinde', (tester) async {
    await tester.pumpWidget(_wrap(
      FilledButton(onPressed: () {}, child: const Text('ALDIM')),
      const Locale('tr'),
      senior: true,
    ));
    expect(tester.getSize(find.byType(FilledButton)).height, greaterThanOrEqualTo(72));
  });

  test('Türkçe büyük harf: i → İ', () {
    expect(upper('Yemekle birlikte', 'tr'), 'YEMEKLE BİRLİKTE');
    expect(upper('Ayrı alın', 'tr'), 'AYRI ALIN');
    expect(upper('With meal', 'en'), 'WITH MEAL');
  });

  test('her iki dilde de tüm metinler mevcut ve çoğullar doğru', () {
    final tr = lookupAppLocalizations(const Locale('tr'));
    final en = lookupAppLocalizations(const Locale('en'));
    expect(tr.pendingTitle(1), '1 takviye bekliyor');
    expect(en.pendingTitle(3), '3 doses waiting');
    expect(tr.everyNDaysLabel(2), 'Gün aşırı');
    expect(en.everyNDaysLabel(3), 'Every 3 days');
  });
}

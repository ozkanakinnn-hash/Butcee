import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/l10n/app_strings.dart';
import 'package:haber_akisi/utils/time_ago.dart';

Widget _localized(Locale locale, String Function(BuildContext) builder) => MaterialApp(
  locale: locale,
  supportedLocales: const [Locale('tr'), Locale('en')],
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: Builder(builder: (c) => Text(builder(c))),
);

void main() {
  testWidgets('Türkçe ve İngilizce metinler', (tester) async {
    await tester.pumpWidget(_localized(const Locale('tr'), (c) => S.of(c).all));
    expect(find.text('Tümü'), findsOneWidget);
    await tester.pumpWidget(_localized(const Locale('en'), (c) => S.of(c).all));
    expect(find.text('All'), findsOneWidget);
  });

  test('timeAgo göreli zamanı biçimlendirir', () {
    final now = DateTime(2026, 11, 6, 12);
    final s = S.forLanguage('tr');
    expect(timeAgo(now.subtract(const Duration(seconds: 20)), s, now: now), 'az önce');
    expect(timeAgo(now.subtract(const Duration(minutes: 5)), s, now: now), '5 dk önce');
    expect(timeAgo(now.subtract(const Duration(hours: 3)), s, now: now), '3 sa önce');
    expect(timeAgo(now.subtract(const Duration(hours: 30)), s, now: now), 'dün');
    expect(timeAgo(DateTime(2026, 10, 1), s, now: now), '01.10.2026');
  });
}

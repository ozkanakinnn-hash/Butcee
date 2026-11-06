import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/data/observances.dart';
import 'package:haber_akisi/widgets/observance_banner.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  test('10 Kasım 2026: 88. yıl, saat 09.00, saygı duruşu hatırlatması, anma türü', () {
    final o = observanceOn(DateTime(2026, 11, 10))!;
    expect(o.observance.id, '10-kasim');
    expect(o.observance.isCommemoration, isTrue);
    expect(o.date, DateTime(2026, 11, 10, 9, 0));
    expect(o.body(), contains('88. yılında'));
    expect(o.body(), contains('09.05'));
    expect(o.body(english: true), contains('88th'));
  });

  test('yıl sayıları: Cumhuriyetin 103. yılı (2026), 23 Nisan 107. yıl (2027), Çanakkale 112. yıl (2027)', () {
    expect(observanceOn(DateTime(2026, 10, 29))!.body(), contains('103. yılı'));
    expect(observanceOn(DateTime(2027, 4, 23))!.body(), contains('107. yıl'));
    expect(observanceOn(DateTime(2027, 3, 18))!.body(), contains('112. yıl'));
  });

  test('yaklaşanlar: 6 Kasım 2026\'dan sonraki ilk günler sırayla; geçmiş günler yok', () {
    final list = upcomingObservances(DateTime(2026, 11, 6, 12));
    expect(list.take(3).map((e) => e.observance.id), ['10-kasim', 'ogretmenler-gunu', 'yilbasi']);
    expect(list.first.date, DateTime(2026, 11, 10, 9));
    expect(list.every((e) => e.date.isAfter(DateTime(2026, 11, 6, 12))), isTrue);
    // Dini bayramlar tablodan: 2027 Ramazan 9 Mart, Kurban 16 Mayıs
    expect(list.firstWhere((e) => e.observance.id == 'ramazan-bayrami').date, DateTime(2027, 3, 9, 9));
    expect(list.firstWhere((e) => e.observance.id == 'kurban-bayrami').date, DateTime(2027, 5, 16, 9));
    // Bildirim kimlikleri benzersiz ve 5000–5999 aralığında
    final ids = list.map((e) => e.notificationId).toSet();
    expect(ids.length, list.length);
    expect(ids.every((id) => id >= 5000 && id <= 5999), isTrue);
  });

  test('sıradan bir günde gün yok; ayar varsayılan açık', () async {
    expect(observanceOn(DateTime(2026, 11, 6)), isNull);
    SharedPreferences.setMockInitialValues({});
    expect((await AppSettings.load()).observanceNotifications, isTrue);
  });

  Widget app(Widget child) => MaterialApp(
    locale: const Locale('tr'),
    supportedLocales: AppSettings.supportedLocales,
    localizationsDelegates: const [
      GlobalMaterialLocalizations.delegate,
      GlobalWidgetsLocalizations.delegate,
      GlobalCupertinoLocalizations.delegate,
    ],
    home: Scaffold(body: child),
  );

  testWidgets('10 Kasım şeridi siyah, 29 Ekim şeridi kırmızı; sıradan günde şerit yok', (tester) async {
    await tester.pumpWidget(app(ObservanceBanner(now: DateTime(2026, 11, 10, 8))));
    expect(find.text('🖤 10 Kasım — Atatürk\'ü Anıyoruz'), findsOneWidget);
    Color bg() =>
        ((tester
                    .widget<Container>(
                      find.descendant(of: find.byType(ObservanceBanner), matching: find.byType(Container)).first,
                    )
                    .decoration
                as BoxDecoration)
            .color)!;
    expect(bg(), const Color(0xFF111111));

    await tester.pumpWidget(app(ObservanceBanner(now: DateTime(2026, 10, 29))));
    expect(find.textContaining('Cumhuriyet Bayramı'), findsOneWidget);
    expect(bg(), const Color(0xFFE30A17));

    await tester.pumpWidget(app(ObservanceBanner(now: DateTime(2026, 11, 6))));
    expect(find.byType(Container), findsNothing);
  });
}

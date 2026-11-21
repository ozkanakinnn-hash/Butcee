// App Review için ekran kaydı akışı (iPhone simülatöründe, gerçek Firebase verisiyle).
// Çalıştırma: .github/workflows/news-ios-review-video.yml (kayıt simctl recordVideo ile alınır).
// Kurulum ekranı ve bildirim izni penceresi atlanır (sistem penceresi otomatik testte kapatılamaz);
// uygulama doğrudan ana akışla açılır. Adımlar canlı (gerçek zamanlı) oynatılır ki videoda izlenebilsin.
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/main.dart' as app;
import 'package:integration_test/integration_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  final binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  binding.framePolicy = LiveTestWidgetsFlutterBindingFramePolicy.fullyLive;

  Future<void> wait(WidgetTester t, int ms) async {
    final end = DateTime.now().add(Duration(milliseconds: ms));
    while (DateTime.now().isBefore(end)) {
      await t.pump(const Duration(milliseconds: 50));
    }
  }

  Future<void> step(String name, Future<void> Function() body) async {
    try {
      await body();
      debugPrint('[REVIEW] ✔ $name');
    } catch (e) {
      debugPrint('[REVIEW] ✘ $name: $e');
    }
  }

  testWidgets('review flow', (t) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList('interests', ['son-dakika', 'gundem', 'ekonomi', 'dunya', 'teknoloji', 'futbol']);
    await prefs.setBool('chose_interests', true);
    await prefs.setBool('notif_permission_asked', true);
    await prefs.setString('language', 'en');
    await prefs.setBool('weather_card', true);

    await app.main();
    await wait(t, 9000); // akış yüklensin

    await step('akışı kaydır', () async {
      final list = find.byType(Scrollable).last;
      await t.drag(list, const Offset(0, -900));
      await wait(t, 1800);
      await t.drag(list, const Offset(0, -900));
      await wait(t, 1800);
      await t.drag(list, const Offset(0, 1800));
      await wait(t, 1500);
    });

    await step('kategori sekmeleri', () async {
      final tabs = find.byType(Tab);
      for (final i in [1, 2, 3]) {
        if (tabs.evaluate().length > i) {
          await t.tap(tabs.at(i));
          await wait(t, 2600);
        }
      }
      await t.tap(tabs.first);
      await wait(t, 2500);
    });

    await step('haberi yayıncının sitesinde aç', () async {
      final cards = find.byType(InkWell);
      // İlk görünür haber kartı (üst kısımdaki hava/skor kartlarını atlamak için birkaç aday dene)
      for (var i = 0; i < cards.evaluate().length && i < 12; i++) {
        final w = cards.at(i);
        final size = t.getSize(w);
        if (size.height > 90 && size.width > 300) {
          await t.tap(w);
          break;
        }
      }
      await wait(t, 9000);
      final close = find.byIcon(Icons.close);
      if (close.evaluate().isNotEmpty) {
        await t.tap(close.first);
      } else {
        app.navigatorKey.currentState?.maybePop();
      }
      await wait(t, 2000);
    });

    await step('piyasa ve hava paneli', () async {
      await t.tap(find.byIcon(Icons.menu_rounded).first);
      await wait(t, 4000);
      await t.tapAt(const Offset(20, 400)); // paneli kapat
      await wait(t, 1500);
    });

    await step('arama', () async {
      await t.tap(find.byIcon(Icons.search_rounded).last);
      await wait(t, 1500);
      await t.enterText(find.byType(TextField).first, 'ekonomi');
      await wait(t, 4500);
    });

    await step('bildirimler', () async {
      await t.tap(find.byIcon(Icons.notifications_none_rounded).last);
      await wait(t, 4000);
    });

    await step('ilgi alanları', () async {
      await t.tap(find.byIcon(Icons.dashboard_customize_outlined).last);
      await wait(t, 3500);
      await t.drag(find.byType(Scrollable).last, const Offset(0, -600));
      await wait(t, 2500);
    });

    await step('ayarlar', () async {
      await t.tap(find.byIcon(Icons.home_outlined).last);
      await wait(t, 2000);
      await t.tap(find.byIcon(Icons.menu_rounded).first);
      await wait(t, 2000);
      final settings = find.byIcon(Icons.settings_outlined);
      await t.tap(settings.evaluate().isNotEmpty ? settings.first : find.byIcon(Icons.settings).first);
      await wait(t, 3000);
      await t.drag(find.byType(Scrollable).last, const Offset(0, -700));
      await wait(t, 3000);
      await t.drag(find.byType(Scrollable).last, const Offset(0, -700));
      await wait(t, 3000);
    });
  }, timeout: const Timeout(Duration(minutes: 6)));
}

// ignore_for_file: invalid_use_of_visible_for_testing_member
// Play Store tanıtım görselleri için UYGULAMANIN GERÇEK EKRANLARINI yakalar.
//
// Uygulama widget'ları (RootShell, HomePage, MarketPanel, SettingsPage…) sahte Firestore verisiyle gerçekten
// çalıştırılır ve her ekran PNG olarak yazılır. Üretilen dosyalar tanitim-gorselleri/olustur.js ile telefon
// çerçevesine yerleştirilir.
//
// Çalıştırma (flutter_app dizininde):
//   PROMO_OUT=/çıktı/klasörü PROMO_IMAGES=/görseller/images.json \
//     flutter test tool/promo/capture.dart
//
// Not: Dosya adı `_test.dart` ile bitmediği için normal `flutter test` koşusuna dahil olmaz.

import 'dart:convert';
import 'dart:io';
import 'dart:ui' as ui;

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:file/local.dart' as fs;
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/pages/interests_page.dart';
import 'package:haber_akisi/pages/match_detail_page.dart';
import 'package:haber_akisi/pages/notifications_page.dart';
import 'package:haber_akisi/pages/root_shell.dart';
import 'package:haber_akisi/pages/settings_page.dart';
import 'package:haber_akisi/services/weather_service.dart';
import 'package:haber_akisi/theme.dart';
import 'package:haber_akisi/utils/storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Telefon ekranı (mantıksal piksel) ve yoğunluk: 720×1500 PNG.
/// Test ortamında GPU yok; 3x yoğunlukta piksele dökme dakikalar sürüyordu. 2x, çerçeveye yerleşen
/// ~684 piksel genişlik için fazlasıyla yeterli.
const _screen = Size(360, 750);
const _dpr = 2.0;

final _outDir = Platform.environment['PROMO_OUT'] ?? 'build/promo';
final _captureKey = GlobalKey();

void main() {
  // Her ekran AYRI bir işlemde yakalanır: widget ağacının sökülmesi bu ortamda dakikalar sürdüğü için
  // dosya yazılır yazılmaz exit(0) ile çıkılır (tool/promo/yakala.sh hepsini sırayla çalıştırır).
  final only = Platform.environment['PROMO_ONLY'] ?? '01-akis';
  final sw = Stopwatch();

  testWidgets('tanıtım ekranı: $only', (tester) async {
    sw.start();
    debugDisableShadows = true;
    await _loadFonts();
    AppStorage.images = _FileCache(_localImages());
    WeatherService.shared = _FixedWeather();
    Directory(_outDir).createSync(recursive: true);
    SharedPreferences.setMockInitialValues({
      'flutter.interests': ['son-dakika', 'gundem', 'ekonomi', 'futbol', 'teknoloji', 'bilim'],
      'flutter.chose_interests': true,
      'flutter.followed_teams': ['yesil-fk', 'mavi-sk'],
      'flutter.weather_card': true,
      'flutter.notify_news': true,
      'flutter.notify_matches': true,
      'flutter.notif_level': 'balanced',
      'flutter.notify_bulletins': true,
      'flutter.liveScorePanel': true,
      'flutter.language': 'tr',
    });
    _mockPlugins();
    final settings = await AppSettings.load();
    final db = FakeFirebaseFirestore();
    await _seed(db);
    final repo = NewsRepository(firestore: db);

    switch (only) {
      case '01-akis':
        await _shot(tester, only, _app(RootShell(settings: settings, repository: repo)));
      case '02-canli-skor':
        await _shot(tester, only, _app(MatchDetailPage(fixtureId: 9001, repository: repo)));
      case '03-bildirim-ayarlari':
        await _shot(tester, only, _app(SettingsPage(settings: settings)), after: (t) async {
          await t.drag(find.byType(Scrollable).first, const Offset(0, -265));
          await _settle(t);
        });
      case '04-piyasa-hava':
        await _shot(tester, only, _app(RootShell(settings: settings, repository: repo)), after: (t) async {
          await t.tap(find.byIcon(Icons.menu_rounded));
          await _settle(t);
        });
      case '05-ilgi-alanlari':
        await _shot(tester, only, _app(InterestsPage(settings: settings, repository: repo)));
      case '06-bildirimler':
        await _shot(tester, only, _app(NotificationsPage(settings: settings, repository: repo)));
      case '07-akis-koyu':
        await _shot(tester, only, _app(RootShell(settings: settings, repository: repo), dark: true));
      default:
        throw ArgumentError('Bilinmeyen ekran: $only');
    }
    debugPrint('[PROMO] $only bitti (${sw.elapsedMilliseconds} ms)');
    // Doğrudan çık: testin kapanışı (ağacın sökülmesi) bu ortamda dakikalar sürüyor.
    // Not: testWidgets gövdesi sahte zaman bölgesinde çalışır; burada Future.delayed BEKLENMEZ (asla dönmez).
    exit(0);
  });
}

Widget _app(Widget home, {bool dark = false}) => MaterialApp(
  debugShowCheckedModeBanner: false,
  theme: dark ? AppTheme.dark() : AppTheme.light(),
  locale: const Locale('tr'),
  supportedLocales: AppSettings.supportedLocales,
  localizationsDelegates: const [
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ],
  home: home,
);

/// Ekranı kurar, yerleşmesini bekler ve PNG yazar.
Future<void> _shot(
  WidgetTester tester,
  String name,
  Widget app, {
  Future<void> Function(WidgetTester)? after,
}) async {
  tester.view.devicePixelRatio = _dpr;
  tester.view.physicalSize = _screen * _dpr;
  addTearDown(tester.view.reset);

  await tester.pumpWidget(RepaintBoundary(key: _captureKey, child: app));
  await _settle(tester);
  if (after != null) await after(tester);
  // Görseller yerel dosyadan okunup çözülsün: gerçek I/O yalnızca runAsync içinde ilerler, çizim ise
  // kare başına olur → ikisi dönüşümlü çalıştırılır.
  for (var i = 0; i < 12; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 80)));
    await tester.pump(const Duration(milliseconds: 100));
  }

  debugPrint('[PROMO]   · $name yakalanıyor');
  final boundary = _captureKey.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final image = await boundary.toImage(pixelRatio: _dpr);
  final bytes = await image.toByteData(format: ui.ImageByteFormat.png);
  File('$_outDir/$name.png').writeAsBytesSync(bytes!.buffer.asUint8List());
  image.dispose();
  debugPrint('[PROMO]   · $name yazıldı');
  // Ağ/akış hataları (eklentisiz ortam) görüntüyü engellemesin
  tester.takeException();
}

/// pumpAndSettle sonsuz animasyonlarda takılır; sabit sayıda kare yeterli.
Future<void> _settle(WidgetTester tester) async {
  for (var i = 0; i < 8; i++) {
    await tester.pump(const Duration(milliseconds: 120));
  }
}

/// Ayarlar ekranı konum iznini sorar; test ortamında eklenti yok → "izin verildi" döndürülür.
void _mockPlugins() {
  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
    const MethodChannel('flutter.baseflow.com/geolocator'),
    (call) async => call.method == 'checkPermission' ? 2 : null, // 2 = whileInUse
  );
}

// ── Fontlar ────────────────────────────────────────────────────────────────
/// Test ortamı varsayılan olarak kutu font kullanır; Flutter SDK'nın Roboto ve Material simgelerini yükler.
Future<void> _loadFonts() async {
  final root = Platform.environment['FLUTTER_ROOT'] ??
      File(Platform.resolvedExecutable).parent.parent.parent.parent.path;
  final dir = Directory('$root/bin/cache/artifacts/material_fonts');
  if (!dir.existsSync()) {
    throw StateError('Font klasörü bulunamadı: ${dir.path} (FLUTTER_ROOT tanımlayın)');
  }
  Future<void> load(String family, List<String> files) async {
    final loader = FontLoader(family);
    for (final f in files) {
      final file = File('${dir.path}/$f');
      if (file.existsSync()) loader.addFont(Future.value(file.readAsBytesSync().buffer.asByteData()));
    }
    await loader.load();
  }

  await load('Roboto', [
    'Roboto-Regular.ttf',
    'Roboto-Medium.ttf',
    'Roboto-Bold.ttf',
    'Roboto-Black.ttf',
    'Roboto-Light.ttf',
    'Roboto-Italic.ttf',
  ]);
  await load('MaterialIcons', ['MaterialIcons-Regular.otf']);
}

// ── Görseller ──────────────────────────────────────────────────────────────
Map<String, String> _localImages() {
  final path = Platform.environment['PROMO_IMAGES'];
  if (path == null) return const {};
  return (jsonDecode(File(path).readAsStringSync()) as Map).cast<String, String>();
}

/// Uygulamanın görsel önbelleği yerine: URL → yerel dosya.
class _FileCache implements BaseCacheManager {
  _FileCache(this.files);

  final Map<String, String> files;

  @override
  Stream<FileResponse> getFileStream(
    String url, {
    String? key,
    Map<String, String>? headers,
    bool withProgress = false,
  }) async* {
    final path = files[url];
    if (path == null) return;
    yield FileInfo(const fs.LocalFileSystem().file(path), FileSource.Cache, DateTime.now().add(const Duration(days: 1)), url);
  }

  @override
  dynamic noSuchMethod(Invocation invocation) =>
      throw UnimplementedError('${invocation.memberName} tanıtım görseli aracında kullanılmıyor');
}

class _FixedWeather extends WeatherService {
  @override
  Future<Weather?> current({Locale? locale}) async =>
      const Weather(place: 'Saruhanlı', temperature: 19, symbol: 'partlycloudy_day');
}

// ── Sahte veri ─────────────────────────────────────────────────────────────
/// Gerçek haber/yayıncı/oyuncu adı KULLANILMAZ (telif ve kişilik hakkı): hepsi temsilidir.
Future<void> _seed(FakeFirebaseFirestore db) async {
  const categories = [
    ('son-dakika', 'Son Dakika', 'bolt', 0, 'breaking'),
    ('gundem', 'Gündem', 'newspaper', 1, ''),
    ('ekonomi', 'Ekonomi', 'trending_up', 2, ''),
    ('futbol', 'Futbol', 'sports_soccer', 3, ''),
    ('dunya', 'Dünya', 'public', 4, ''),
    ('teknoloji', 'Teknoloji', 'devices', 5, ''),
    ('saglik', 'Sağlık', 'health', 6, ''),
    ('bilim', 'Bilim', 'science', 7, ''),
    ('kultur-sanat', 'Kültür-Sanat', 'palette', 8, ''),
    ('otomobil', 'Otomobil', 'car', 9, ''),
    ('basketbol', 'Basketbol', 'sports_basketball', 10, ''),
    ('seyahat', 'Seyahat', 'flight', 11, ''),
  ];
  for (final (id, name, icon, order, special) in categories) {
    await db.doc('categories/$id').set({
      'name': name,
      'name_en': name,
      'icon_name': icon,
      'order_index': order,
      'special': special,
      'placeholder_image_url': '',
    });
  }

  final now = DateTime.now();
  const img = 'https://gorsel.local';
  final articles = <(String, String, String, String, String, int, bool)>[
    (
      'a1',
      'Şehir içi ulaşımda yeni dönem: kart yerine telefonla geçiş bugün başladı',
      'Uygulamalı geçiş sistemi ilk etapta üç hatta devreye alındı; yıl sonunda tüm ağa yayılacak.',
      '$img/haber-sehir.jpg',
      'gundem',
      6,
      true,
    ),
    (
      'a2',
      'Piyasalarda gün sonu: altın yükselişini sürdürdü, kurlar yatay seyretti',
      '',
      '$img/haber-ekonomi.jpg',
      'ekonomi',
      22,
      false,
    ),
    (
      'a3',
      'Ligde haftanın maçı nefes kesti: üç puan uzatma dakikalarında geldi',
      '',
      '$img/haber-futbol.jpg',
      'futbol',
      48,
      false,
    ),
    (
      'a4',
      'Yerli yazılım girişimi yeni yatırım turunu tamamladı',
      '',
      '$img/haber-teknoloji.jpg',
      'teknoloji',
      75,
      false,
    ),
    (
      'a5',
      'Basketbolda derbi öncesi kadro açıklandı',
      '',
      '$img/haber-basketbol.jpg',
      'basketbol',
      96,
      false,
    ),
    (
      'a6',
      'Araştırma: düzenli yürüyüş uyku kalitesini belirgin biçimde artırıyor',
      '',
      '',
      'bilim',
      130,
      false,
    ),
  ];
  const sources = [
    ('Haber Ajansı', '$img/logo-a.png'),
    ('Ekonomi Gazetesi', '$img/logo-b.png'),
    ('Spor Servisi', '$img/logo-c.png'),
    ('Teknoloji Masası', '$img/logo-a.png'),
    ('Spor Servisi', '$img/logo-c.png'),
    ('Bilim Bülteni', '$img/logo-b.png'),
  ];
  for (var i = 0; i < articles.length; i++) {
    final (id, title, summary, image, cat, minutesAgo, breaking) = articles[i];
    final (source, logo) = sources[i];
    final at = now.subtract(Duration(minutes: minutesAgo));
    await db.doc('articles/$id').set({
      'title': title,
      'summary': summary,
      'image_url': image,
      'original_url': 'https://ornek.local/$id',
      'source_name': source,
      'source_logo_url': logo,
      'category_id': db.doc('categories/$cat'),
      'published_at': Timestamp.fromDate(at),
      'content_hash': id,
      if (breaking) 'breaking_at': Timestamp.fromDate(at),
    });
  }

  for (final (id, name, short, colors) in const [
    ('yesil-fk', 'Yeşil FK', 'YFK', ['#1b8f4a', '#ffffff']),
    ('mavi-sk', 'Mavi SK', 'MSK', ['#1d4ed8', '#ffffff']),
  ]) {
    await db.doc('football_teams/$id').set({'name': name, 'short': short, 'colors': colors, 'active': true});
  }

  await db.doc('matches/9001').set({
    'fixture_id': 9001,
    'kickoff': Timestamp.fromDate(now.subtract(const Duration(minutes: 67))),
    'status': {'short': '2H', 'elapsed': 67},
    'league': {'id': 1, 'name': 'Süper Lig', 'round': '13. Hafta'},
    'home': {'id': 'yesil-fk', 'name': 'Yeşil FK', 'short': 'YFK', 'colors': ['#1b8f4a', '#ffffff']},
    'away': {'id': 'mavi-sk', 'name': 'Mavi SK', 'short': 'MSK', 'colors': ['#1d4ed8', '#ffffff']},
    'goals': {'home': 2, 'away': 1},
    'goal_events': [
      {'minute': 21, 'extra': 0, 'team_id': 'yesil-fk', 'player': 'A. Yılmaz', 'detail': 'Normal Goal'},
      {'minute': 39, 'extra': 0, 'team_id': 'mavi-sk', 'player': 'C. Şahin', 'detail': 'Normal Goal'},
      {'minute': 58, 'extra': 0, 'team_id': 'yesil-fk', 'player': 'M. Arslan', 'detail': 'Penalty'},
    ],
    'team_ids': ['yesil-fk', 'mavi-sk'],
    'is_live': true,
    'is_finished': false,
  });

  await db.doc('market/latest').set({
    'items': [
      {'id': 'usdtry', 'label': 'Dolar', 'unit': '₺', 'value': 42.185, 'change_pct': 0.21, 'decimals': 4},
      {'id': 'eurtry', 'label': 'Euro', 'unit': '₺', 'value': 48.732, 'change_pct': -0.12, 'decimals': 4},
      {'id': 'gram_gold', 'label': 'Gram Altın', 'unit': '₺', 'value': 5612.4, 'change_pct': 0.84, 'decimals': 2},
      {'id': 'gold_oz', 'label': 'Ons Altın', 'unit': r'$', 'value': 4138.25, 'change_pct': 0.56, 'decimals': 2},
    ],
    'source': 'Döviz: TCMB gösterge kurları · Altın: Truncgil Finans',
    'source_updated': '15:30',
    'updated_at': Timestamp.fromDate(now),
  });

  final notifications = [
    ('n1', 'SON DAKİKA', 'Şehir içi ulaşımda yeni dönem: kart yerine telefonla geçiş', 'news_son-dakika', 'breaking_news', 'article', 6),
    ('n2', 'Sabah Bülteni', '• Ulaşımda yeni dönem\n• Altın yükselişini sürdürdü\n• Ligde haftanın maçı nefes kesti', 'bulletin', 'news', 'article', 190),
    ('n3', 'GOL! Yeşil FK 2-1 Mavi SK', "58' M. Arslan (pen.)", 'team_yesil-fk', 'goal_alert', 'match', 9),
    ('n4', '3 yeni haber', 'Ekonomi: Altın yükselişini sürdürdü\nTeknoloji: Yerli girişim yatırım aldı', 'news_ekonomi', 'news', 'article', 65),
  ];
  for (final (id, title, body, topic, channel, type, minutesAgo) in notifications) {
    await db.doc('notifications/$id').set({
      'topics': [topic],
      'type': type,
      'title': title,
      'body': body,
      'article_title': type == 'article' ? body.split('\n').first : '',
      'source': 'Haber Ajansı',
      'url': 'https://ornek.local/$id',
      'category_id': topic.startsWith('news_') ? topic.substring(5) : '',
      'channel': channel,
      'fixture_id': type == 'match' ? 9001 : '',
      'sent_at': Timestamp.fromDate(now.subtract(Duration(minutes: minutesAgo))),
    });
  }
}

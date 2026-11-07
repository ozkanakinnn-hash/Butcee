import 'dart:io';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_cache_manager/flutter_cache_manager.dart';
import 'package:path_provider/path_provider.dart';
import 'package:webview_flutter/webview_flutter.dart';

/// Cihazda kullanılan yer ve bellek sınırları + "Önbelleği temizle".
///
/// Varsayılanlar bir haber uygulaması için fazla cömert:
///  - Firestore çevrimdışı önbelleği 100 MB → 20 MB
///  - Görsel dosya önbelleği 200 dosya / 30 gün → 150 dosya / 3 gün (haberler zaten günlük)
///  - Bellekteki çözülmüş görseller 100 MB → 50 MB
class AppStorage {
  AppStorage._();

  static const firestoreCacheBytes = 20 * 1024 * 1024;
  static const memoryImageCacheBytes = 50 * 1024 * 1024;

  /// Haber görselleri ve kaynak logoları için sınırlı önbellek.
  static final images = CacheManager(
    Config('haber_gorselleri', stalePeriod: const Duration(days: 3), maxNrOfCacheObjects: 150),
  );

  /// Uygulama açılışında (Firebase başlatıldıktan hemen sonra, ilk sorgudan önce) çağrılır.
  static void configure({bool firestore = true}) {
    PaintingBinding.instance.imageCache.maximumSizeBytes = memoryImageCacheBytes;
    if (firestore) {
      FirebaseFirestore.instance.settings = const Settings(
        persistenceEnabled: true,
        cacheSizeBytes: firestoreCacheBytes,
      );
    }
  }

  /// Önbellek klasörünün boyutu (bayt). Okunamazsa 0.
  static Future<int> cacheSizeBytes() async {
    try {
      final dir = await getTemporaryDirectory();
      var total = 0;
      await for (final e in dir.list(recursive: true, followLinks: false)) {
        if (e is File) total += await e.length();
      }
      return total;
    } catch (_) {
      return 0;
    }
  }

  /// Görsel önbelleği, eski sürümün varsayılan önbelleği, bellekteki görseller ve WebView önbelleği.
  static Future<void> clearCaches() async {
    await images.emptyCache();
    await DefaultCacheManager().emptyCache(); // önceki sürümlerden kalanlar
    PaintingBinding.instance.imageCache
      ..clear()
      ..clearLiveImages();
    try {
      final c = WebViewController();
      await c.clearCache();
      await c.clearLocalStorage();
    } catch (_) {}
  }

  /// "12,4 MB" biçimi.
  static String formatBytes(int bytes) {
    if (bytes < 1024 * 1024) return '${(bytes / 1024).toStringAsFixed(0)} KB';
    return '${(bytes / (1024 * 1024)).toStringAsFixed(1).replaceAll('.', ',')} MB';
  }
}

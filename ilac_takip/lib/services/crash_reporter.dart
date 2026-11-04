import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';

/// Yakalanmamış hataları dosyaya yazar; sonraki açılışta kullanıcıya "Raporu kopyala" gösterilir.
/// (Firebase Crashlytics bağlanana kadar sahadaki çökmeleri görmenin yolu.)
class CrashReporter {
  CrashReporter._();

  static File? _file;
  static const _maxBytes = 64 * 1024;

  static Future<void> install() async {
    try {
      final dir = await getApplicationSupportDirectory();
      _file = File('${dir.path}/crash_log.txt');
    } on Object {
      _file = null;
    }

    final previous = FlutterError.onError;
    FlutterError.onError = (details) {
      record(details.exception, details.stack, context: details.context?.toDescription());
      previous?.call(details);
    };
    PlatformDispatcher.instance.onError = (error, stack) {
      record(error, stack, context: 'PlatformDispatcher');
      return true; // uygulamayı kapatma; hata kaydedildi
    };
  }

  static void record(Object error, StackTrace? stack, {String? context}) {
    debugPrint('HATA${context == null ? '' : ' ($context)'}: $error');
    final f = _file;
    if (f == null) return;
    try {
      final entry = StringBuffer()
        ..writeln('=== ${DateTime.now().toIso8601String()} ===')
        ..writeln('Platform: ${Platform.operatingSystem} ${Platform.operatingSystemVersion}')
        ..writeln('Bağlam: ${context ?? '-'}')
        ..writeln('Hata: $error')
        ..writeln(stack?.toString() ?? '(yığın yok)');
      if (f.existsSync() && f.lengthSync() > _maxBytes) f.deleteSync();
      f.writeAsStringSync(entry.toString(), mode: FileMode.append, flush: true);
    } on Object {
      // Raporlayıcı asla kendi başına hata üretmemeli.
    }
  }

  static Future<String?> pendingReport() async {
    final f = _file;
    if (f == null || !f.existsSync()) return null;
    final text = await f.readAsString();
    return text.trim().isEmpty ? null : text;
  }

  static Future<void> clear() async {
    final f = _file;
    if (f != null && f.existsSync()) await f.delete();
  }
}

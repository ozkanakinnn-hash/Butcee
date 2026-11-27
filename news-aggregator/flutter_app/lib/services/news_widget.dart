import 'package:flutter/foundation.dart';
import 'package:home_widget/home_widget.dart';

import '../backend/news_repository.dart';
import '../utils/briefing_slot.dart';

/// Android ana ekran widget'ını günceller: gündemin (briefings/latest; yoksa ilk haberler) 3 başlığı.
/// iOS widget'ı ayrı bir Xcode uzantısı gerektirir (sonraki sürüm); iOS'ta bu çağrı hiçbir şey yapmaz.
class NewsWidget {
  NewsWidget._();

  static const _android = 'akinai.app1.NewsWidgetProvider';

  /// Uygulama açılınca ve öne gelince çağrılır. Hata uygulamayı etkilemez.
  static Future<void> refresh({NewsRepository repository = const NewsRepository()}) async {
    if (defaultTargetPlatform != TargetPlatform.android) return;
    try {
      // Önbellekteki eski bülten değil, sunucudaki güncel bülten (eski bültenle widget doldurulmaz).
      final briefing = await repository.fetchBriefing().timeout(const Duration(seconds: 10));
      if (!isCurrentBriefing(briefing, DateTime.now())) return;
      final items = ((briefing?['items'] as List?) ?? const []).whereType<Map>().map((m) => '${m['title'] ?? ''}');
      final lines = items.where((t) => t.isNotEmpty).take(3).toList();
      if (lines.isEmpty) return;
      for (var i = 0; i < 3; i++) {
        await HomeWidget.saveWidgetData<String>('w_line${i + 1}', i < lines.length ? '• ${lines[i]}' : '');
      }
      final now = DateTime.now();
      await HomeWidget.saveWidgetData<String>(
        'w_time',
        '${now.hour.toString().padLeft(2, '0')}:${now.minute.toString().padLeft(2, '0')}',
      );
      await HomeWidget.updateWidget(qualifiedAndroidName: _android);
    } catch (e) {
      debugPrint('Widget güncellenemedi: $e');
    }
  }
}

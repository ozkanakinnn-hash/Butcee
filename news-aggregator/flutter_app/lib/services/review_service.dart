import 'package:flutter/foundation.dart';
import 'package:in_app_review/in_app_review.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Mağaza puanı isteği: 5. haber okumasından sonra, uygulama en az 2 gündür kurulu ise, YALNIZCA BİR KEZ.
/// Google/Apple'ın kendi penceresi kullanılır (kendi "puan ver" ekranımız yok; mağaza kuralları kotayı kendisi uygular).
class ReviewService {
  ReviewService._();
  static final instance = ReviewService._();

  static const _readsKey = 'review_reads';
  static const _askedKey = 'review_asked';
  static const _firstKey = 'review_first_seen_ms';
  static const minReads = 5;
  static const minAge = Duration(days: 2);

  /// Haber ekranı kapanınca çağrılır. Hata uygulamayı etkilemez.
  Future<void> onArticleRead({InAppReview? review, DateTime? now}) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      if (prefs.getBool(_askedKey) ?? false) return;
      final t = now ?? DateTime.now();
      final first = prefs.getInt(_firstKey) ?? t.millisecondsSinceEpoch;
      if (!prefs.containsKey(_firstKey)) await prefs.setInt(_firstKey, first);
      final reads = (prefs.getInt(_readsKey) ?? 0) + 1;
      await prefs.setInt(_readsKey, reads);
      final oldEnough = t.difference(DateTime.fromMillisecondsSinceEpoch(first)) >= minAge;
      if (reads < minReads || !oldEnough) return;
      final r = review ?? InAppReview.instance;
      if (!await r.isAvailable()) return;
      await prefs.setBool(_askedKey, true);
      await r.requestReview();
    } catch (e) {
      debugPrint('Puan isteği gösterilemedi: $e');
    }
  }
}

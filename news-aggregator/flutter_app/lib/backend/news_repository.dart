import 'package:cloud_firestore/cloud_firestore.dart';

import '../config.dart';
import 'schema/articles_record.dart';
import 'schema/categories_record.dart';

/// Bir sayfa haber + sonraki sayfa için imleç.
class ArticlePage {
  const ArticlePage(this.items, this.lastDocument, this.hasMore);

  final List<ArticlesRecord> items;
  final DocumentSnapshot<Map<String, dynamic>>? lastDocument;
  final bool hasMore;
}

/// Firestore sorguları tek yerde. (FlutterFlow'daki "Backend Query" ayarlarının kod karşılığı.)
class NewsRepository {
  const NewsRepository();

  /// Kategori sekmeleri: order_index'e göre sıralı, canlı dinlenir.
  Stream<List<CategoriesRecord>> watchCategories() => CategoriesRecord.collection
      .orderBy('order_index')
      .snapshots()
      .map((qs) => qs.docs.map(CategoriesRecord.fromSnapshot).toList());

  /// Haber akışı: en yeni önce, sayfa sayfa (sonsuz kaydırma).
  /// [category] null ise "Tümü" sekmesi. Kategori filtresi için
  /// (category_id ASC, published_at DESC) birleşik dizini gerekir — firestore.indexes.json.
  Future<ArticlePage> fetchArticles({
    DocumentReference<Map<String, dynamic>>? category,
    DocumentSnapshot<Map<String, dynamic>>? startAfter,
    int pageSize = AppConfig.pageSize,
  }) async {
    Query<Map<String, dynamic>> q = ArticlesRecord.collection;
    if (category != null) q = q.where('category_id', isEqualTo: category);
    q = q.orderBy('published_at', descending: true).limit(pageSize);
    if (startAfter != null) q = q.startAfterDocument(startAfter);

    final snap = await q.get();
    return ArticlePage(
      snap.docs.map(ArticlesRecord.fromSnapshot).toList(),
      snap.docs.isEmpty ? startAfter : snap.docs.last,
      snap.docs.length == pageSize,
    );
  }
}

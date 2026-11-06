import 'package:cloud_firestore/cloud_firestore.dart';

import '../config.dart';
import 'schema/articles_record.dart';
import 'schema/categories_record.dart';
import 'schema/football_records.dart';

/// Bir sayfa haber + sonraki sayfa için imleç.
class ArticlePage {
  const ArticlePage(this.items, this.lastDocument, this.hasMore);

  final List<ArticlesRecord> items;
  final DocumentSnapshot<Map<String, dynamic>>? lastDocument;
  final bool hasMore;
}

/// Firestore sorguları tek yerde. (FlutterFlow'daki "Backend Query" ayarlarının kod karşılığı.)
class NewsRepository {
  const NewsRepository({this.firestore});

  /// Testlerde sahte Firestore verilebilir; boşsa uygulamanın Firestore'u kullanılır.
  final FirebaseFirestore? firestore;

  FirebaseFirestore get _db => firestore ?? FirebaseFirestore.instance;

  /// Kategori sekmeleri: order_index'e göre sıralı, canlı dinlenir.
  Stream<List<CategoriesRecord>> watchCategories() => _db
      .collection('categories')
      .orderBy('order_index')
      .snapshots()
      .map((qs) => qs.docs.map(CategoriesRecord.fromSnapshot).toList());

  /// Haber akışı: en yeni önce, sayfa sayfa (sonsuz kaydırma).
  /// [category] null ise "Tümü" sekmesi. Kategori filtresi için
  /// (category_id ASC, published_at DESC) birleşik dizini gerekir — firestore.indexes.json.
  ///
  /// Son Dakika ([CategoriesRecord.isBreaking]): orderBy('breaking_at') yalnızca bu alana sahip
  /// haberleri döndürür; tek alanlı otomatik dizin yeterlidir.
  Future<ArticlePage> fetchArticles({
    CategoriesRecord? category,
    DocumentSnapshot<Map<String, dynamic>>? startAfter,
    int pageSize = AppConfig.pageSize,
  }) async {
    Query<Map<String, dynamic>> q = _db.collection('articles');
    if (category != null && category.isBreaking) {
      q = q.orderBy('breaking_at', descending: true);
    } else {
      if (category != null) q = q.where('category_id', isEqualTo: category.reference);
      q = q.orderBy('published_at', descending: true);
    }
    q = q.limit(pageSize);
    if (startAfter != null) q = q.startAfterDocument(startAfter);

    final snap = await q.get();
    return ArticlePage(
      snap.docs.map(ArticlesRecord.fromSnapshot).toList(),
      snap.docs.isEmpty ? startAfter : snap.docs.last,
      snap.docs.length == pageSize,
    );
  }

  /// Süper Lig takımları (ada göre).
  Stream<List<TeamRecord>> watchTeams() => _db
      .collection('football_teams')
      .orderBy('name')
      .snapshots()
      .map((qs) => qs.docs.map(TeamRecord.fromSnapshot).toList());

  /// Takip edilen takımların son/sıradaki maçları (bot 3 günden eskileri siler).
  /// arrayContainsAny en fazla 10 değer alır; sıralama istemcide yapılır (ek dizin gerekmez).
  Stream<List<MatchRecord>> watchMatches(List<int> teamIds) {
    if (teamIds.isEmpty) return Stream.value(const []);
    return _db
        .collection('matches')
        .where('team_ids', arrayContainsAny: teamIds.take(10).toList())
        .limit(30)
        .snapshots()
        .map((qs) {
          final list = qs.docs.map(MatchRecord.fromSnapshot).toList();
          // Canlı olanlar önce, sonra başlama saatine göre (yakın olan önce)
          final now = DateTime.now();
          list.sort((a, b) {
            if (a.isLive != b.isLive) return a.isLive ? -1 : 1;
            return a.kickoff.difference(now).abs().compareTo(b.kickoff.difference(now).abs());
          });
          return list;
        });
  }

  Stream<MatchRecord?> watchMatch(int fixtureId) =>
      _db.doc('matches/$fixtureId').snapshots().map((d) => d.exists ? MatchRecord.fromSnapshot(d) : null);
}

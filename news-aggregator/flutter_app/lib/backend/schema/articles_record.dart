import 'package:cloud_firestore/cloud_firestore.dart';

/// `articles` koleksiyonu (Cloud Function'ın RSS'ten çektiği haberler).
class ArticlesRecord {
  ArticlesRecord._(this.reference, Map<String, dynamic> data)
    : title = data['title'] as String? ?? '',
      summary = data['summary'] as String? ?? '',
      imageUrl = data['image_url'] as String? ?? '',
      originalUrl = data['original_url'] as String? ?? '',
      sourceName = data['source_name'] as String? ?? '',
      sourceLogoUrl = data['source_logo_url'] as String? ?? '',
      categoryId = data['category_id'] as DocumentReference<Map<String, dynamic>>?,
      publishedAt = (data['published_at'] as Timestamp?)?.toDate(),
      contentHash = data['content_hash'] as String? ?? '',
      breakingAt = (data['breaking_at'] as Timestamp?)?.toDate(),
      topicOnly = data['topic_only'] as bool? ?? false,
      alsoBy = ((data['also_by'] as List?) ?? const []).whereType<String>().toList();

  static CollectionReference<Map<String, dynamic>> get collection => FirebaseFirestore.instance.collection('articles');

  factory ArticlesRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap) =>
      ArticlesRecord._(snap.reference, snap.data() ?? const {});

  final DocumentReference<Map<String, dynamic>> reference;
  final String title;
  final String summary;
  final String imageUrl;
  final String originalUrl;
  final String sourceName;
  final String sourceLogoUrl;
  final DocumentReference<Map<String, dynamic>>? categoryId;
  final DateTime? publishedAt;
  final String contentHash;

  /// Son Dakika olarak işaretlendiyse zamanı.
  final DateTime? breakingAt;
  bool get isBreaking => breakingAt != null;

  /// Yalnızca kendi konu sekmesinde gösterilir (ör. Investing → yalnızca Ekonomi); son dakikaysa her yerde.
  final bool topicOnly;

  /// "Bu haberi kimler verdi?": aynı olayı veren yayıncılar (sunucu 2+ kaynakta yazar). Boşsa tek kaynak.
  final List<String> alsoBy;

  /// "Tümü" akışında görünür mü?
  bool get showInAllFeed => !topicOnly || isBreaking;
}

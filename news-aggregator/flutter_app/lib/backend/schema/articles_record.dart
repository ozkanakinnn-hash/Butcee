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
      contentHash = data['content_hash'] as String? ?? '';

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
}

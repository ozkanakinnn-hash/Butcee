import 'package:cloud_firestore/cloud_firestore.dart';

/// `sources` koleksiyonu (haber kaynakları). Uygulama yalnızca okur; yazma Cloud Function'dadır.
class SourcesRecord {
  SourcesRecord._(this.reference, Map<String, dynamic> data)
    : sourceName = data['source_name'] as String? ?? '',
      rssUrl = data['rss_url'] as String? ?? '',
      categoryId = data['category_id'] as DocumentReference<Map<String, dynamic>>?,
      logoUrl = data['logo_url'] as String? ?? '',
      isActive = data['is_active'] as bool? ?? false;

  static CollectionReference<Map<String, dynamic>> get collection => FirebaseFirestore.instance.collection('sources');

  factory SourcesRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap) =>
      SourcesRecord._(snap.reference, snap.data() ?? const {});

  final DocumentReference<Map<String, dynamic>> reference;
  final String sourceName;
  final String rssUrl;
  final DocumentReference<Map<String, dynamic>>? categoryId;
  final String logoUrl;
  final bool isActive;
}

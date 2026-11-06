import 'package:cloud_firestore/cloud_firestore.dart';

/// `notifications/{id}` — botun gönderdiği bildirimlerin kaydı (Bildirimler sekmesi).
class NotificationRecord {
  NotificationRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap)
    : id = snap.id,
      topics = ((snap.data()?['topics'] as List?) ?? const []).cast<String>(),
      type = snap.data()?['type'] as String? ?? 'article',
      title = snap.data()?['title'] as String? ?? '',
      body = snap.data()?['body'] as String? ?? '',
      image = snap.data()?['image'] as String? ?? '',
      url = snap.data()?['url'] as String? ?? '',
      articleTitle = snap.data()?['article_title'] as String? ?? '',
      source = snap.data()?['source'] as String? ?? '',
      categoryId = snap.data()?['category_id'] as String? ?? '',
      fixtureId = int.tryParse('${snap.data()?['fixture_id'] ?? ''}'),
      channel = snap.data()?['channel'] as String? ?? '',
      sentAt = (snap.data()?['sent_at'] as Timestamp?)?.toDate();

  final String id;
  final List<String> topics;
  final String type;
  final String title;
  final String body;
  final String image;
  final String url;
  final String articleTitle;
  final String source;
  final String categoryId;
  final int? fixtureId;
  final String channel;
  final DateTime? sentAt;

  bool get isBreaking => channel == 'breaking' || categoryId == 'son-dakika';
  bool get isMatch => type == 'match';

  /// Listede gösterilecek ana metin: haber başlığı veya maç bildirimi başlığı.
  String get headline => isMatch ? title : (articleTitle.isNotEmpty ? articleTitle : body.split('\n').first);
}

/// `market/latest` içindeki tek satır.
class MarketItem {
  MarketItem.fromMap(Map<String, dynamic> m)
    : id = m['id'] as String? ?? '',
      label = m['label'] as String? ?? '',
      unit = m['unit'] as String? ?? '',
      value = (m['value'] as num?)?.toDouble() ?? 0,
      changePct = (m['change_pct'] as num?)?.toDouble(),
      decimals = (m['decimals'] as num?)?.toInt() ?? 2;

  final String id;
  final String label;
  final String unit;
  final double value;
  final double? changePct;
  final int decimals;
}

class MarketSnapshot {
  MarketSnapshot.fromMap(Map<String, dynamic> d)
    : items = ((d['items'] as List?) ?? const [])
          .map((e) => MarketItem.fromMap(Map<String, dynamic>.from(e as Map)))
          .toList(),
      source = d['source'] as String? ?? '',
      updatedAt = (d['updated_at'] as Timestamp?)?.toDate();

  final List<MarketItem> items;
  final String source;
  final DateTime? updatedAt;
}

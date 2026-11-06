import 'package:cloud_firestore/cloud_firestore.dart';

/// `categories` koleksiyonu — FlutterFlow'un ürettiği `CategoriesRecord` ile aynı alan adları.
class CategoriesRecord {
  CategoriesRecord._(this.reference, Map<String, dynamic> data)
      : name = data['name'] as String? ?? '',
        nameEn = data['name_en'] as String? ?? '',
        iconName = data['icon_name'] as String? ?? '',
        orderIndex = (data['order_index'] as num?)?.toInt() ?? 0,
        placeholderImageUrl = data['placeholder_image_url'] as String? ?? '';

  static CollectionReference<Map<String, dynamic>> get collection =>
      FirebaseFirestore.instance.collection('categories');

  factory CategoriesRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap) =>
      CategoriesRecord._(snap.reference, snap.data() ?? const {});

  final DocumentReference<Map<String, dynamic>> reference;
  final String name;
  final String nameEn;
  final String iconName;
  final int orderIndex;
  final String placeholderImageUrl;

  String localizedName(bool english) => english && nameEn.isNotEmpty ? nameEn : name;
}

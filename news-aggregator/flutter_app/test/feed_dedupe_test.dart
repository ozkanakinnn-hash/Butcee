import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/backend/news_repository.dart';
import 'package:haber_akisi/backend/schema/categories_record.dart';

void main() {
  test('Gündem sekmesi Son Dakika haberlerini göstermez; Son Dakika sekmesi gösterir; Tümü hepsini', () async {
    final db = FakeFirebaseFirestore();
    await db.doc('categories/gundem').set({'name': 'Gündem', 'order_index': 1});
    await db.doc('categories/son-dakika').set({'name': 'Son Dakika', 'order_index': 0, 'special': 'breaking'});
    final gundem = db.doc('categories/gundem');
    final sd = db.doc('categories/son-dakika');
    Future<void> add(String id, DocumentReference cat, int hour, {bool breaking = false}) =>
        db.doc('articles/$id').set({
          'title': id,
          'category_id': cat,
          'published_at': Timestamp.fromDate(DateTime(2026, 11, 6, hour)),
          if (breaking) 'breaking_at': Timestamp.fromDate(DateTime(2026, 11, 6, hour)),
        });
    await add('gundem-normal', gundem, 10);
    await add('gundem-isaretli', gundem, 11, breaking: true); // Gündem kaynağından, Son Dakika'da da görüldü
    await add('sondakika', sd, 12, breaking: true);
    await db.doc('categories/ekonomi').set({'name': 'Ekonomi', 'order_index': 2});
    await add('ekonomi-sondakika', db.doc('categories/ekonomi'), 9, breaking: true); // konu sekmesinde gizlenmez

    final repo = NewsRepository(firestore: db);
    final cats = {for (final c in await repo.watchCategories().first) c.id: c};
    Future<List<String>> titles(CategoriesRecord? c) async =>
        (await repo.fetchArticles(category: c)).items.map((a) => a.title).toList();

    expect(await titles(cats['gundem']), ['gundem-normal']);
    expect(await titles(cats['son-dakika']), ['sondakika', 'gundem-isaretli', 'ekonomi-sondakika']);
    expect(await titles(cats['ekonomi']), ['ekonomi-sondakika']);
    expect(await titles(null), ['sondakika', 'gundem-isaretli', 'gundem-normal', 'ekonomi-sondakika']);
  });
}

import 'package:fake_cloud_firestore/fake_cloud_firestore.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:haber_akisi/app_settings.dart';
import 'package:haber_akisi/services/team_follow_sync.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  test('takip edilen takımlar isimsiz sayaca yalnızca fark olarak yazılır', () async {
    SharedPreferences.setMockInitialValues({});
    final db = FakeFirebaseFirestore();
    final settings = await AppSettings.load();
    final sync = TeamFollowSync(settings, firestore: db);
    Future<int?> count(String id) async => (await db.doc('team_follows/$id').get()).data()?['count'] as int?;

    await settings.setFollowedTeams(['besiktas', 'galatasaray']);
    await sync.sync();
    expect(await count('besiktas'), 1);
    expect(await count('galatasaray'), 1);

    // Aynı liste tekrar → yazma yok
    await sync.sync();
    expect(await count('besiktas'), 1);

    // Galatasaray bırakıldı, Fenerbahçe eklendi
    await settings.setFollowedTeams(['besiktas', 'fenerbahce']);
    await sync.sync();
    expect(await count('galatasaray'), 0);
    expect(await count('fenerbahce'), 1);
    expect(await count('besiktas'), 1);
    // yalnızca sayı; kişisel bilgi yok
    expect((await db.doc('team_follows/besiktas').get()).data()!.keys, ['count']);
  });
}

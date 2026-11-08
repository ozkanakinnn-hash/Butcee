import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../app_settings.dart';

/// Takip edilen takımları sunucuya İSİMSİZ sayaç olarak bildirir (`team_follows/{takım}.count` ±1).
///
/// Kim takip ediyor bilgisi gönderilmez; sunucu yalnızca takip edilen takımların maçları için canlı skor isteği
/// harcar (ücretsiz plandaki günlük 100 isteği verimli kullanmak için). Yalnızca farklar yazılır; son bildirilen
/// liste cihazda saklanır. Çevrimdışıyken Firestore yazmaları kuyruğa alır.
class TeamFollowSync {
  TeamFollowSync(this._settings, {FirebaseFirestore? firestore}) : _db = firestore;

  final AppSettings _settings;
  final FirebaseFirestore? _db;
  static const _syncedKey = 'team_follow_synced';
  bool _running = false;
  bool _again = false;

  void start() {
    _settings.addListener(sync);
    sync();
  }

  /// Takım listesindeki değişikliği sayaçlara yansıtır.
  Future<void> sync() async {
    if (_running) {
      _again = true;
      return;
    }
    _running = true;
    try {
      do {
        _again = false;
        final prefs = await SharedPreferences.getInstance();
        final synced = (prefs.getStringList(_syncedKey) ?? const []).toSet();
        final desired = _settings.followedTeams.toSet();
        if (synced.length == desired.length && synced.containsAll(desired)) break;
        final db = _db ?? FirebaseFirestore.instance;
        final col = db.collection('team_follows');
        for (final id in desired.difference(synced)) {
          await col.doc(id).set({'count': FieldValue.increment(1)}, SetOptions(merge: true));
        }
        for (final id in synced.difference(desired)) {
          await col.doc(id).update({'count': FieldValue.increment(-1)});
        }
        await prefs.setStringList(_syncedKey, desired.toList());
      } while (_again);
    } catch (e) {
      debugPrint('Takım takip sayacı güncellenemedi: $e');
    } finally {
      _running = false;
    }
  }
}

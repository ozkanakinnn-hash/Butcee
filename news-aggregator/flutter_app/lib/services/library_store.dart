import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../backend/schema/articles_record.dart';

/// Kullanıcının kişisel haber kitaplığı — yalnızca cihazda (SharedPreferences) saklanır, sunucuya gitmez:
///  - Kaydedilenler (sonra oku)
///  - Okuma geçmişi (okunan haberler akışta soluk görünür)
///  - Gizlenen kaynaklar (akışta ve aramada gösterilmez)
class LibraryStore extends ChangeNotifier {
  LibraryStore._(this._prefs) {
    _saved = _readList(_savedKey);
    _history = _readList(_historyKey);
    _muted = (_prefs?.getStringList(_mutedKey) ?? const []).toList();
    _readIds = {for (final e in _history) e.id};
  }

  static const _savedKey = 'library_saved';
  static const _historyKey = 'library_history';
  static const _mutedKey = 'library_muted_sources';

  /// Saklanan en fazla kayıt (eskiler düşer).
  static const maxSaved = 300;
  static const maxHistory = 200;

  /// Uygulama genelinde tek örnek. Yüklenmeden kullanılırsa (testler) yalnızca bellekte çalışır.
  static LibraryStore instance = LibraryStore._(null);

  static Future<LibraryStore> load() async => instance = LibraryStore._(await SharedPreferences.getInstance());

  final SharedPreferences? _prefs;
  late List<LibraryEntry> _saved;
  late List<LibraryEntry> _history;
  late List<String> _muted;
  late Set<String> _readIds;

  List<LibraryEntry> _readList(String key) {
    final raw = _prefs?.getString(key);
    if (raw == null) return [];
    try {
      return (jsonDecode(raw) as List).map((e) => LibraryEntry.fromJson(e as Map<String, dynamic>)).toList();
    } catch (_) {
      return [];
    }
  }

  Future<void> _write(String key, List<LibraryEntry> list) async =>
      _prefs?.setString(key, jsonEncode([for (final e in list) e.toJson()]));

  // ── Kaydedilenler ────────────────────────────────────────────────────────
  /// En son kaydedilen önce.
  List<LibraryEntry> get saved => List.unmodifiable(_saved);

  bool isSaved(ArticlesRecord a) => _saved.any((e) => e.matches(a));

  /// Kaydedildiyse çıkarır, değilse ekler. Sonuç: artık kayıtlı mı?
  Future<bool> toggleSaved(ArticlesRecord a) async {
    final had = isSaved(a);
    if (had) {
      _saved.removeWhere((e) => e.matches(a));
    } else {
      _saved.insert(0, LibraryEntry.from(a));
      if (_saved.length > maxSaved) _saved.removeRange(maxSaved, _saved.length);
    }
    notifyListeners();
    await _write(_savedKey, _saved);
    return !had;
  }

  Future<void> removeSaved(LibraryEntry e) async {
    _saved.removeWhere((x) => x.id == e.id);
    notifyListeners();
    await _write(_savedKey, _saved);
  }

  /// Geri al (silinen kaydı eski yerine koyar).
  Future<void> restoreSaved(LibraryEntry e, int index) async {
    if (_saved.any((x) => x.id == e.id)) return;
    _saved.insert(index.clamp(0, _saved.length), e);
    notifyListeners();
    await _write(_savedKey, _saved);
  }

  // ── Okuma geçmişi ────────────────────────────────────────────────────────
  /// En son okunan önce.
  List<LibraryEntry> get history => List.unmodifiable(_history);

  bool isRead(ArticlesRecord a) => _readIds.contains(LibraryEntry.idOf(a));

  Future<void> markRead(ArticlesRecord a) async {
    final entry = LibraryEntry.from(a);
    _history.removeWhere((e) => e.id == entry.id);
    _history.insert(0, entry);
    if (_history.length > maxHistory) _history.removeRange(maxHistory, _history.length);
    _readIds = {for (final e in _history) e.id};
    notifyListeners();
    await _write(_historyKey, _history);
  }

  Future<void> clearHistory() async {
    _history.clear();
    _readIds = {};
    notifyListeners();
    await _prefs?.remove(_historyKey);
  }

  // ── Gizlenen kaynaklar ───────────────────────────────────────────────────
  List<String> get mutedSources => List.unmodifiable(_muted);

  bool isMuted(String sourceName) => sourceName.isNotEmpty && _muted.contains(sourceName);

  /// Akışta gösterilecek mi? (kaynağı gizlenmediyse)
  bool isVisible(ArticlesRecord a) => !isMuted(a.sourceName);

  Future<void> muteSource(String sourceName) async {
    if (sourceName.isEmpty || _muted.contains(sourceName)) return;
    _muted.add(sourceName);
    notifyListeners();
    await _prefs?.setStringList(_mutedKey, _muted);
  }

  Future<void> unmuteSource(String sourceName) async {
    _muted.remove(sourceName);
    notifyListeners();
    await _prefs?.setStringList(_mutedKey, _muted);
  }
}

/// Cihazda saklanan haber kopyası (yalnızca başlık, kısa özet, kaynak ve bağlantı).
@immutable
class LibraryEntry {
  const LibraryEntry({
    required this.id,
    required this.title,
    required this.url,
    this.summary = '',
    this.imageUrl = '',
    this.source = '',
    this.logo = '',
    this.publishedAt,
    required this.at,
  });

  final String id;
  final String title;
  final String url;
  final String summary;
  final String imageUrl;
  final String source;
  final String logo;
  final DateTime? publishedAt;

  /// Kaydedildiği / okunduğu an.
  final DateTime at;

  /// Firestore kimliği yoksa (eski kayıt) adres kullanılır.
  static String idOf(ArticlesRecord a) => a.id.isNotEmpty ? a.id : a.originalUrl;

  bool matches(ArticlesRecord a) => id == idOf(a) || (url.isNotEmpty && url == a.originalUrl);

  factory LibraryEntry.from(ArticlesRecord a) => LibraryEntry(
    id: idOf(a),
    title: a.title,
    url: a.originalUrl,
    summary: a.summary,
    imageUrl: a.imageUrl,
    source: a.sourceName,
    logo: a.sourceLogoUrl,
    publishedAt: a.publishedAt,
    at: DateTime.now(),
  );

  ArticlesRecord toRecord() => ArticlesRecord.local(
    id: id,
    title: title,
    originalUrl: url,
    summary: summary,
    imageUrl: imageUrl,
    sourceName: source,
    sourceLogoUrl: logo,
    publishedAt: publishedAt,
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    't': title,
    'u': url,
    if (summary.isNotEmpty) 's': summary,
    if (imageUrl.isNotEmpty) 'i': imageUrl,
    if (source.isNotEmpty) 'src': source,
    if (logo.isNotEmpty) 'l': logo,
    if (publishedAt != null) 'p': publishedAt!.millisecondsSinceEpoch,
    'at': at.millisecondsSinceEpoch,
  };

  factory LibraryEntry.fromJson(Map<String, dynamic> j) => LibraryEntry(
    id: j['id'] as String? ?? '',
    title: j['t'] as String? ?? '',
    url: j['u'] as String? ?? '',
    summary: j['s'] as String? ?? '',
    imageUrl: j['i'] as String? ?? '',
    source: j['src'] as String? ?? '',
    logo: j['l'] as String? ?? '',
    publishedAt: j['p'] == null ? null : DateTime.fromMillisecondsSinceEpoch(j['p'] as int),
    at: DateTime.fromMillisecondsSinceEpoch(j['at'] as int? ?? 0),
  );
}

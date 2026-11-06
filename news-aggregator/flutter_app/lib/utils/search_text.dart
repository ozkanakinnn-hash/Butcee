/// Arama metnini sadeleştirir — sunucudaki `foldForSearch` (firebase/functions/src/text.js) ile AYNI kurallar:
/// İ/I → i, küçük harf, ç→c ğ→g ı→i ö→o ş→s ü→u (â î û dahil), harf/rakam dışı → boşluk.
String foldForSearch(String text) {
  const map = {
    'ç': 'c',
    'ğ': 'g',
    'ı': 'i',
    'ö': 'o',
    'ş': 's',
    'ü': 'u',
    'â': 'a',
    'î': 'i',
    'û': 'u',
    'é': 'e',
    'è': 'e',
    'ê': 'e',
    'ë': 'e',
    'á': 'a',
    'à': 'a',
    'ä': 'a',
    'í': 'i',
    'ó': 'o',
    'ô': 'o',
    'ú': 'u',
    'ñ': 'n',
  };
  final lower = text.replaceAll('İ', 'i').replaceAll('I', 'i').toLowerCase();
  final buf = StringBuffer();
  for (final ch in lower.split('')) {
    buf.write(map[ch] ?? ch);
  }
  return buf.toString().replaceAll(RegExp(r'[^a-z0-9]+'), ' ').trim();
}

/// Sunucu kelimelerin 3..10 harflik ön eklerini saklar.
const searchPrefixMax = 10;

/// Sorgudan Firestore'a gönderilecek anahtar (en uzun kelimenin ilk 10 harfi) ve
/// istemcide süzülecek tüm kelimeler. Anahtar 3 harften kısaysa null.
({String key, List<String> words})? searchPlan(String query) {
  final words = foldForSearch(query).split(' ').where((w) => w.length >= 3).toList();
  if (words.isEmpty) return null;
  final longest = words.reduce((a, b) => b.length > a.length ? b : a);
  final key = longest.length > searchPrefixMax ? longest.substring(0, searchPrefixMax) : longest;
  return (key: key, words: words);
}

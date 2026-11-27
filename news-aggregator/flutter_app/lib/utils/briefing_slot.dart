/// Sunucudaki bülten dilimi kuralı (notify.js → bulletinSlot) ile birebir aynı:
/// İstanbul saatiyle 08:30–19:00 sabah (`{gün}-am`), 19:00–08:30 akşam (`{gün}-pm`; gece yarısından sonra önceki gün).
/// Türkiye yaz saati uygulamadığı için İstanbul = UTC+3 (cihazın saat diliminden bağımsız).
DateTime istanbulTime(DateTime t) => t.toUtc().add(const Duration(hours: 3));

String _ymd(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

/// Şu an geçerli olması gereken bülten kimliği, ör. `2026-11-28-am`.
String expectedBriefingId(DateTime now) {
  final t = istanbulTime(now);
  final m = t.hour * 60 + t.minute;
  if (m >= 19 * 60) return '${_ymd(t)}-pm';
  if (m >= 8 * 60 + 30) return '${_ymd(t)}-am';
  return '${_ymd(t.subtract(const Duration(days: 1)))}-pm';
}

/// Belge şu anki dilimin bülteni mi? (değilse eski — "dünkü bülten" gösterilmez)
bool isCurrentBriefing(Map<String, dynamic>? data, DateTime now) =>
    data != null && data['id'] == expectedBriefingId(now);

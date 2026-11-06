import '../l10n/app_strings.dart';

/// "5 dk önce", "2 sa önce", "dün", "06.11.2026" biçiminde göreli zaman.
String timeAgo(DateTime? date, S s, {DateTime? now}) {
  if (date == null) return '';
  final current = now ?? DateTime.now();
  final diff = current.difference(date);
  if (diff.inMinutes < 1) return s.justNow;
  if (diff.inMinutes < 60) return s.minutesAgo(diff.inMinutes);
  if (diff.inHours < 24) return s.hoursAgo(diff.inHours);
  if (diff.inDays < 2) return s.yesterday;
  if (diff.inDays < 7) return s.daysAgo(diff.inDays);
  String two(int v) => v.toString().padLeft(2, '0');
  return '${two(date.day)}.${two(date.month)}.${date.year}';
}

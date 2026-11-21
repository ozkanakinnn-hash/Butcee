import 'package:flutter/material.dart';

import '../backend/news_repository.dart';
import '../l10n/app_strings.dart';

/// Son depremler (Türkiye ve yakın çevresi, son 6 saat, M≥3). Kaynak: AFAD (yedek EMSC).
class QuakesPage extends StatelessWidget {
  const QuakesPage({super.key, this.repository = const NewsRepository()});

  final NewsRepository repository;

  static Route<void> route() => MaterialPageRoute<void>(builder: (_) => const QuakesPage());

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.recentQuakes)),
      body: StreamBuilder<Map<String, dynamic>?>(
        stream: repository.watchQuakes(),
        builder: (context, snap) {
          final data = snap.data;
          final items = ((data?['items'] as List?) ?? const []).whereType<Map>().toList();
          if (snap.connectionState == ConnectionState.waiting && data == null) {
            return const Center(child: CircularProgressIndicator());
          }
          if (items.isEmpty) return Center(child: Text(s.quakesEmpty));
          return ListView.separated(
            itemCount: items.length + 1,
            separatorBuilder: (_, _) => const Divider(height: 1),
            itemBuilder: (context, i) {
              if (i == items.length) {
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text('Kaynak: ${data?['provider'] ?? 'AFAD'}', style: theme.textTheme.bodySmall),
                );
              }
              final q = items[i];
              final mag = (q['mag'] as num?)?.toDouble() ?? 0;
              final t = DateTime.fromMillisecondsSinceEpoch((q['time_ms'] as num?)?.toInt() ?? 0).toLocal();
              final depth = (q['depth'] as num?)?.round();
              final strong = mag >= 4.5;
              return ListTile(
                leading: CircleAvatar(
                  backgroundColor: strong ? const Color(0xFFD62828) : theme.colorScheme.surfaceContainerHighest,
                  child: Text(
                    mag.toStringAsFixed(1),
                    style: TextStyle(fontWeight: FontWeight.w800, color: strong ? Colors.white : theme.colorScheme.onSurface),
                  ),
                ),
                title: Text('${q['place'] ?? ''}'.isEmpty ? 'Türkiye' : '${q['place']}'),
                subtitle: Text(
                  '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}'
                  '${depth != null ? ' · $depth km' : ''}',
                ),
              );
            },
          );
        },
      ),
    );
  }
}

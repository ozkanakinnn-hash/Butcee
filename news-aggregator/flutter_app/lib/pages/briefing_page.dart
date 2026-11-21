import 'package:flutter/material.dart';

import '../backend/news_repository.dart';
import '../l10n/app_strings.dart';
import 'article_webview_page.dart';

/// "5 dakikada gündem": sabah 08:30 ve akşam 19:00'da en çok yayıncının verdiği 5–7 haber.
/// Sunucu `briefings/{gün}-{am|pm}` ve `briefings/latest` belgelerini yazar.
class BriefingPage extends StatelessWidget {
  const BriefingPage({super.key, this.id, this.repository = const NewsRepository()});

  final String? id;
  final NewsRepository repository;

  static Route<void> route({String? id}) => MaterialPageRoute<void>(builder: (_) => BriefingPage(id: id));

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return StreamBuilder<Map<String, dynamic>?>(
      stream: repository.watchBriefing(id),
      builder: (context, snap) {
        final data = snap.data;
        final items = ((data?['items'] as List?) ?? const []).whereType<Map>().toList();
        final evening = data?['slot'] == 'pm';
        return Scaffold(
          appBar: AppBar(
            title: Text(evening ? s.briefingEvening : s.briefingMorning),
          ),
          body: snap.connectionState == ConnectionState.waiting && data == null
              ? const Center(child: CircularProgressIndicator())
              : items.isEmpty
                  ? Center(child: Padding(padding: const EdgeInsets.all(24), child: Text(s.briefingEmpty, textAlign: TextAlign.center)))
                  : ListView.separated(
                      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
                      itemCount: items.length + 1,
                      separatorBuilder: (_, i) => i == 0 ? const SizedBox(height: 8) : const Divider(height: 24),
                      itemBuilder: (context, i) {
                        if (i == 0) {
                          return Text(
                            '${evening ? '🌙' : '☀️'} ${s.briefingSubtitle} · ${data?['day'] ?? ''}',
                            style: theme.textTheme.titleSmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                          );
                        }
                        final it = items[i - 1];
                        final sources = ((it['sources'] as List?) ?? const []).whereType<String>().toList();
                        final title = '${it['title'] ?? ''}';
                        final url = '${it['url'] ?? ''}';
                        return InkWell(
                          borderRadius: BorderRadius.circular(12),
                          onTap: url.isEmpty
                              ? null
                              : () => Navigator.of(context).push(
                                  ArticleWebViewPage.route(url: url, sourceName: '${it['source'] ?? ''}', articleTitle: title),
                                ),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              CircleAvatar(
                                radius: 14,
                                backgroundColor: theme.colorScheme.primaryContainer,
                                child: Text('$i', style: TextStyle(color: theme.colorScheme.onPrimaryContainer, fontWeight: FontWeight.w800)),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(title, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                                    const SizedBox(height: 4),
                                    Text(
                                      sources.length >= 2
                                          ? '${s.inSources(sources.length)}: ${sources.take(5).join(', ')}'
                                          : '${it['source'] ?? ''}',
                                      style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
        );
      },
    );
  }
}

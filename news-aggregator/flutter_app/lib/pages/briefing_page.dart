import 'package:flutter/material.dart';

import '../backend/news_repository.dart';
import '../backend/schema/extra_records.dart';
import '../l10n/app_strings.dart';
import '../utils/time_ago.dart';
import 'article_webview_page.dart';

/// "5 dakikada gündem": sabah/akşam bülteni — yapay zekâ özeti + en çok kaynağın verdiği haberler.
/// [briefingId] boşsa son bülten açılır (bildirimden gelince o bültenin kimliği verilir).
class BriefingPage extends StatefulWidget {
  const BriefingPage({super.key, this.briefingId, this.repository = const NewsRepository()});

  final String? briefingId;
  final NewsRepository repository;

  static Route<void> route({String? briefingId, NewsRepository repository = const NewsRepository()}) =>
      MaterialPageRoute<void>(builder: (_) => BriefingPage(briefingId: briefingId, repository: repository));

  @override
  State<BriefingPage> createState() => _BriefingPageState();
}

class _BriefingPageState extends State<BriefingPage> {
  late final Stream<BriefingRecord?> _stream = widget.repository.watchBriefing(widget.briefingId);

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.briefing)),
      body: StreamBuilder<BriefingRecord?>(
        stream: _stream,
        builder: (context, snap) {
          if (snap.connectionState == ConnectionState.waiting && !snap.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final b = snap.data;
          if (b == null || b.items.isEmpty) {
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(32),
                child: Text(s.briefingUnavailable, textAlign: TextAlign.center),
              ),
            );
          }
          return BriefingView(briefing: b);
        },
      ),
    );
  }
}

/// Bülten içeriği (test edilebilir olsun diye ayrı).
class BriefingView extends StatelessWidget {
  const BriefingView({super.key, required this.briefing});

  final BriefingRecord briefing;

  void _open(BuildContext context, BriefingItem i) => Navigator.of(context).push(
    ArticleWebViewPage.route(url: i.url, sourceName: i.source, articleTitle: i.title),
  );

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;
    final b = briefing;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
      children: [
        Row(
          children: [
            Icon(b.isMorning ? Icons.wb_sunny_outlined : Icons.nightlight_outlined, size: 20, color: muted),
            const SizedBox(width: 8),
            Text(
              b.isMorning ? s.briefingMorning : s.briefingEvening,
              style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
            ),
            const Spacer(),
            Text(timeAgo(b.createdAt, s), style: theme.textTheme.bodySmall?.copyWith(color: muted)),
          ],
        ),
        if (b.aiSummary.isNotEmpty) ...[
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: theme.colorScheme.primaryContainer.withValues(alpha: 0.45),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(Icons.auto_awesome_rounded, size: 16, color: theme.colorScheme.primary),
                    const SizedBox(width: 6),
                    Text(
                      s.briefingAiSummary,
                      style: theme.textTheme.labelLarge?.copyWith(color: theme.colorScheme.primary),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(b.aiSummary, style: theme.textTheme.bodyLarge?.copyWith(height: 1.4)),
                const SizedBox(height: 8),
                Text(s.briefingAiNote, style: theme.textTheme.bodySmall?.copyWith(color: muted)),
              ],
            ),
          ),
        ],
        const SizedBox(height: 20),
        Text(s.briefingTopStories, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
        const SizedBox(height: 4),
        for (var n = 0; n < b.items.length; n++)
          _ItemTile(index: n + 1, item: b.items[n], onTap: () => _open(context, b.items[n])),
      ],
    );
  }
}

class _ItemTile extends StatelessWidget {
  const _ItemTile({required this.index, required this.item, required this.onTap});

  final int index;
  final BriefingItem item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;
    final meta = [if (item.source.isNotEmpty) item.source, if (item.sourceCount >= 2) s.inSources(item.sourceCount)];
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 4),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 28,
              child: Text(
                '$index',
                style: theme.textTheme.titleMedium?.copyWith(color: theme.colorScheme.primary, fontWeight: FontWeight.w800),
              ),
            ),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(item.title, style: theme.textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w600)),
                  if (meta.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(meta.join(' · '), style: theme.textTheme.bodySmall?.copyWith(color: muted)),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Ana sayfadaki küçük gündem kartı: özetin ilk satırları, dokununca [BriefingPage].
class BriefingCard extends StatelessWidget {
  const BriefingCard({super.key, required this.repository});

  final NewsRepository repository;

  @override
  Widget build(BuildContext context) => StreamBuilder<BriefingRecord?>(
    stream: repository.watchLatestBriefing(),
    builder: (context, snap) {
      final b = snap.data;
      if (b == null || !b.isFresh(DateTime.now())) return const SizedBox.shrink();
      final s = S.of(context);
      final theme = Theme.of(context);
      final preview = b.aiSummary.isNotEmpty ? b.aiSummary : b.items.take(3).map((i) => '• ${i.title}').join('\n');
      return Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
        child: Material(
          color: theme.colorScheme.surfaceContainer,
          borderRadius: BorderRadius.circular(16),
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: () => Navigator.of(context).push(BriefingPage.route(briefingId: b.id, repository: repository)),
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        b.aiSummary.isNotEmpty ? Icons.auto_awesome_rounded : Icons.newspaper_rounded,
                        size: 18,
                        color: theme.colorScheme.primary,
                      ),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          b.isMorning ? s.briefingMorning : s.briefingEvening,
                          style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w800),
                        ),
                      ),
                      Text(s.briefingRead, style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.primary)),
                      Icon(Icons.chevron_right_rounded, size: 18, color: theme.colorScheme.primary),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(preview, maxLines: 3, overflow: TextOverflow.ellipsis, style: theme.textTheme.bodyMedium),
                ],
              ),
            ),
          ),
        ),
      );
    },
  );
}

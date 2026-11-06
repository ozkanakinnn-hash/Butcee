import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../backend/schema/articles_record.dart';
import '../l10n/app_strings.dart';
import '../utils/time_ago.dart';

/// Haber kartı.
/// - Normal: solda başlık + "kaynak · zaman", sağda kare kapak görseli.
/// - [featured]: akışın ilk haberi; tam genişlikte 16:9 görsel, altında büyük başlık.
class ArticleCard extends StatelessWidget {
  const ArticleCard({
    super.key,
    required this.article,
    required this.onTap,
    this.fallbackIcon,
    this.featured = false,
    this.compact = false,
  });

  final ArticlesRecord article;
  final VoidCallback onTap;
  final IconData? fallbackIcon;
  final bool featured;

  /// Görselsiz sade satır.
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = S.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    final placeholder = Container(
      color: theme.colorScheme.surfaceContainerHighest,
      alignment: Alignment.center,
      child: Icon(fallbackIcon ?? Icons.article_outlined, color: muted),
    );

    Widget image({double? memWidth}) => article.imageUrl.isEmpty
        ? placeholder
        : CachedNetworkImage(
            imageUrl: article.imageUrl,
            fit: BoxFit.cover,
            memCacheWidth: memWidth?.toInt(),
            placeholder: (_, _) => placeholder,
            errorWidget: (_, _, _) => placeholder,
          );

    final meta = _Meta(article: article, time: timeAgo(article.publishedAt, s), color: muted);

    if (compact) {
      return InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                article.title,
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700, height: 1.28),
              ),
              const SizedBox(height: 6),
              meta,
            ],
          ),
        ),
      );
    }

    if (featured) {
      return InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: AspectRatio(aspectRatio: 16 / 9, child: image(memWidth: 1080)),
              ),
              const SizedBox(height: 12),
              Text(
                article.title,
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.w800,
                  height: 1.22,
                  letterSpacing: -0.2,
                ),
              ),
              if (article.summary.isNotEmpty) ...[
                const SizedBox(height: 6),
                Text(
                  article.summary,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.bodyMedium?.copyWith(color: muted, height: 1.35),
                ),
              ],
              const SizedBox(height: 8),
              meta,
            ],
          ),
        ),
      );
    }

    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    article.title,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700, height: 1.28),
                  ),
                  const SizedBox(height: 8),
                  meta,
                ],
              ),
            ),
            const SizedBox(width: 14),
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: SizedBox(width: 92, height: 92, child: image(memWidth: 276)),
            ),
          ],
        ),
      ),
    );
  }
}

/// "● Kaynak · 5 dk önce" satırı (kaynak logosu varsa küçük daire içinde).
class _Meta extends StatelessWidget {
  const _Meta({required this.article, required this.time, required this.color});

  final ArticlesRecord article;
  final String time;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.bodySmall?.copyWith(color: color, fontWeight: FontWeight.w500);
    return Row(
      children: [
        if (article.sourceLogoUrl.isNotEmpty) ...[
          ClipOval(
            child: CachedNetworkImage(
              imageUrl: article.sourceLogoUrl,
              width: 16,
              height: 16,
              fit: BoxFit.cover,
              errorWidget: (_, _, _) => const SizedBox(width: 16, height: 16),
            ),
          ),
          const SizedBox(width: 6),
        ],
        Flexible(
          child: Text(
            [article.sourceName, time].where((e) => e.isNotEmpty).join(' · '),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: style,
          ),
        ),
      ],
    );
  }
}

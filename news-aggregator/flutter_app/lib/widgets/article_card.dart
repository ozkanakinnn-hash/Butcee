import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../backend/schema/articles_record.dart';
import '../l10n/app_strings.dart';
import '../utils/time_ago.dart';

/// Haber kartı: solda kapak görseli, sağda kalın başlık ve altında "Kaynak · zaman" (küçük gri).
class ArticleCard extends StatelessWidget {
  const ArticleCard({super.key, required this.article, required this.onTap, this.fallbackIcon});

  final ArticlesRecord article;
  final VoidCallback onTap;

  /// Görsel yoksa / yüklenemezse gösterilecek kategori ikonu.
  final IconData? fallbackIcon;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = S.of(context);
    final meta = [article.sourceName, timeAgo(article.publishedAt, s)].where((e) => e.isNotEmpty).join(' · ');

    final placeholder = Container(
      color: theme.colorScheme.surfaceContainerHighest,
      alignment: Alignment.center,
      child: Icon(fallbackIcon ?? Icons.article_outlined, color: theme.colorScheme.onSurfaceVariant),
    );

    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: SizedBox(
                width: 112,
                height: 80,
                child: article.imageUrl.isEmpty
                    ? placeholder
                    : CachedNetworkImage(
                        imageUrl: article.imageUrl,
                        fit: BoxFit.cover,
                        memCacheWidth: 336,
                        placeholder: (_, _) => placeholder,
                        errorWidget: (_, _, _) => placeholder,
                      ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    article.title,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700, height: 1.25),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    meta,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

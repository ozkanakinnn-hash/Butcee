import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../backend/schema/articles_record.dart';
import '../l10n/app_strings.dart';
import '../services/library_store.dart';
import '../utils/storage.dart';
import '../utils/time_ago.dart';
import 'article_actions.dart';

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
    this.showActions = true,
    this.dimWhenRead = true,
  });

  final ArticlesRecord article;
  final VoidCallback onTap;
  final IconData? fallbackIcon;
  final bool featured;

  /// Görselsiz sade satır.
  final bool compact;

  /// Kaydet düğmesi ve "⋮" seçenekler menüsü.
  final bool showActions;

  /// Okunan haberin başlığı soluk gösterilir (Geçmiş ekranında kapalı).
  final bool dimWhenRead;

  @override
  Widget build(BuildContext context) => ListenableBuilder(
    listenable: LibraryStore.instance,
    builder: (context, _) => _build(context),
  );

  Widget _build(BuildContext context) {
    final theme = Theme.of(context);
    final read = dimWhenRead && LibraryStore.instance.isRead(article);
    final titleColor = read ? theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.75) : null;
    final s = S.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    // Görseli olmayan (ör. telif nedeniyle görseli kapatılan) haberlerde kaynağın logosu gösterilir.
    final placeholder = Container(
      color: theme.colorScheme.surfaceContainerHighest,
      alignment: Alignment.center,
      child: article.sourceLogoUrl.isNotEmpty
          ? ClipRRect(
              borderRadius: BorderRadius.circular(10),
              child: CachedNetworkImage(
                cacheManager: AppStorage.images,
                imageUrl: article.sourceLogoUrl,
                width: 44,
                height: 44,
                memCacheWidth: 132,
                fadeInDuration: Duration.zero,
                fit: BoxFit.contain,
                errorWidget: (_, _, _) => Icon(fallbackIcon ?? Icons.article_outlined, color: muted),
              ),
            )
          : Icon(fallbackIcon ?? Icons.article_outlined, color: muted),
    );

    Widget image({double? memWidth}) => article.imageUrl.isEmpty
        ? placeholder
        : CachedNetworkImage(
            cacheManager: AppStorage.images,
            imageUrl: article.imageUrl,
            fit: BoxFit.cover,
            memCacheWidth: memWidth?.toInt(),
            // Varsayılan 0,5 sn belirme + 1 sn yer tutucu sönmesi görselleri "geç geliyor" gösteriyordu.
            fadeInDuration: const Duration(milliseconds: 120),
            fadeOutDuration: Duration.zero,
            placeholderFadeInDuration: Duration.zero,
            placeholder: (_, _) => placeholder,
            errorWidget: (_, _, _) => placeholder,
          );

    final metaLine = _Meta(article: article, time: timeAgo(article.publishedAt, s), color: muted);
    final meta = showActions
        ? Row(
            children: [
              Expanded(child: metaLine),
              _Actions(article: article, color: muted),
            ],
          )
        : metaLine;

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
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700, height: 1.28, color: titleColor),
              ),
              const SizedBox(height: 2),
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
                  color: titleColor,
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
              const SizedBox(height: 4),
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
                    style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700, height: 1.28, color: titleColor),
                  ),
                  const SizedBox(height: 4),
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

/// Kart altındaki seçenekler: kayıtlıysa küçük yer imi işareti + "⋮" (kaydet, paylaş, kaynağı gizle).
class _Actions extends StatelessWidget {
  const _Actions({required this.article, required this.color});

  final ArticlesRecord article;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final saved = LibraryStore.instance.isSaved(article);
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (saved)
          Padding(
            padding: const EdgeInsets.only(left: 6),
            child: Icon(
              Icons.bookmark_rounded,
              key: ValueKey('saved-mark-${article.id}'),
              size: 16,
              color: Theme.of(context).colorScheme.primary,
              semanticLabel: s.savedArticles,
            ),
          ),
        IconButton(
          key: ValueKey('more-${article.id}'),
          constraints: const BoxConstraints.tightFor(width: 32, height: 28),
          padding: EdgeInsets.zero,
          iconSize: 20,
          visualDensity: VisualDensity.compact,
          tooltip: s.moreOptions,
          color: color,
          icon: const Icon(Icons.more_vert_rounded),
          onPressed: () => showArticleOptions(context, article),
        ),
      ],
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
              cacheManager: AppStorage.images,
              imageUrl: article.sourceLogoUrl,
              width: 16,
              height: 16,
              memCacheWidth: 48,
              fadeInDuration: Duration.zero,
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
        if (article.alsoBy.length >= 2) ...[
          const SizedBox(width: 8),
          _SourcesChip(sources: article.alsoBy),
        ],
      ],
    );
  }
}

/// "3 kaynakta" rozeti: dokununca aynı olayı veren yayıncıların listesi.
class _SourcesChip extends StatelessWidget {
  const _SourcesChip({required this.sources});

  final List<String> sources;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final scheme = Theme.of(context).colorScheme;
    return InkWell(
      borderRadius: BorderRadius.circular(10),
      onTap: () => showModalBottomSheet<void>(
        context: context,
        showDragHandle: true,
        builder: (ctx) => SafeArea(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(s.whoReported, style: Theme.of(ctx).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 4),
                Text(s.whoReportedDesc(sources.length), style: Theme.of(ctx).textTheme.bodySmall),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [for (final src in sources) Chip(label: Text(src), visualDensity: VisualDensity.compact)],
                ),
              ],
            ),
          ),
        ),
      ),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
        decoration: BoxDecoration(
          color: scheme.primaryContainer,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Text(
          s.inSources(sources.length),
          style: Theme.of(context).textTheme.labelSmall?.copyWith(color: scheme.onPrimaryContainer, fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

/// Yeni gelen haberlerin görsellerini (ilk [count] tanesi) ekrana gelmeden önce diske indirir;
/// kaydırınca görsel beklemeden hazır olur. Hatalar sessizce yok sayılır.
void prefetchArticleImages(BuildContext context, Iterable<ArticlesRecord> articles, {int count = 10}) {
  final urls = <String>{
    for (final a in articles.take(count)) ...[
      if (a.imageUrl.isNotEmpty) a.imageUrl,
      if (a.sourceLogoUrl.isNotEmpty) a.sourceLogoUrl,
    ],
  };
  for (final url in urls) {
    // Ön yükleme hatası akışı etkilememeli (ör. "çevrimdışı" sayılmamalı).
    try {
      AppStorage.images.getSingleFile(url).ignore(); // önbellekte varsa indirmez
    } catch (_) {}
  }
}

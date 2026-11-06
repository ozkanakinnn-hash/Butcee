import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/articles_record.dart';
import '../backend/schema/categories_record.dart';
import '../l10n/app_strings.dart';
import '../widgets/article_card.dart';
import '../widgets/category_icons.dart';
import 'article_webview_page.dart';
import 'interests_page.dart';
import 'settings_page.dart';

/// Ana ekran: üstte "Tümü" + kullanıcının ilgi alanları (seçtiği sırayla), altında haber akışı.
class HomePage extends StatelessWidget {
  const HomePage({super.key, required this.settings, this.repository = const NewsRepository()});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    return StreamBuilder<List<CategoriesRecord>>(
      stream: repository.watchCategories(),
      builder: (context, snapshot) {
        final all = snapshot.data ?? const <CategoriesRecord>[];
        return ListenableBuilder(
          listenable: settings,
          builder: (context, _) {
            final byId = {for (final c in all) c.id: c};
            final tabs = [
              for (final id in settings.interests)
                if (byId[id] != null) byId[id]!,
            ];
            // Sekme listesi değişirse TabController yeniden kurulsun.
            return DefaultTabController(
              key: ValueKey(tabs.map((c) => c.id).join(',')),
              length: tabs.length + 1,
              child: Scaffold(
                appBar: AppBar(
                  title: Text(s.appName),
                  actions: [
                    IconButton(
                      icon: const Icon(Icons.tune_rounded),
                      tooltip: s.manageInterests,
                      onPressed: () => Navigator.of(context).push(
                        MaterialPageRoute<void>(
                          builder: (_) => InterestsPage(settings: settings, repository: repository),
                        ),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.settings_outlined),
                      tooltip: s.settings,
                      onPressed: () =>
                          Navigator.of(context)
                              .push(MaterialPageRoute<void>(builder: (_) => SettingsPage(settings: settings))),
                    ),
                  ],
                  bottom: PreferredSize(
                    preferredSize: const Size.fromHeight(48),
                    child: Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: SizedBox(
                        height: 36,
                        child: TabBar(
                          isScrollable: true,
                          tabAlignment: TabAlignment.start,
                          padding: const EdgeInsets.symmetric(horizontal: 12),
                          tabs: [
                            Tab(text: s.all),
                            for (final c in tabs)
                              Tab(
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    if (c.isBreaking) ...[
                                      const Icon(Icons.bolt_rounded, size: 16),
                                      const SizedBox(width: 2),
                                    ],
                                    Text(c.localizedName(s.isEnglish)),
                                  ],
                                ),
                              ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                body: TabBarView(
                  children: [
                    ArticleFeed(key: const PageStorageKey('feed-all'), repository: repository),
                    for (final c in tabs)
                      ArticleFeed(key: PageStorageKey('feed-${c.id}'), repository: repository, category: c),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }
}

/// Tek bir sekmenin haber listesi: sonsuz kaydırma (20'şer) + aşağı çekip yenileme.
class ArticleFeed extends StatefulWidget {
  const ArticleFeed({super.key, required this.repository, this.category});

  final NewsRepository repository;
  final CategoriesRecord? category;

  @override
  State<ArticleFeed> createState() => _ArticleFeedState();
}

class _ArticleFeedState extends State<ArticleFeed> with AutomaticKeepAliveClientMixin {
  final _scroll = ScrollController();
  final _items = <ArticlesRecord>[];
  DocumentSnapshot<Map<String, dynamic>>? _cursor;
  bool _hasMore = true;
  bool _loading = false;
  Object? _error;

  @override
  bool get wantKeepAlive => true; // Sekmeler arası geçişte liste ve kaydırma konumu korunur.

  @override
  void initState() {
    super.initState();
    _scroll.addListener(_onScroll);
    _loadMore();
  }

  @override
  void dispose() {
    _scroll.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scroll.position.extentAfter < 600) _loadMore();
  }

  Future<void> _loadMore() async {
    if (_loading || !_hasMore) return;
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final page = await widget.repository.fetchArticles(category: widget.category, startAfter: _cursor);
      if (!mounted) return;
      setState(() {
        _items.addAll(page.items);
        _cursor = page.lastDocument;
        _hasMore = page.hasMore;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// Aşağı çekip yenileme: listeyi sıfırlayıp ilk sayfayı yeniden çeker.
  Future<void> _refresh() async {
    try {
      final page = await widget.repository.fetchArticles(category: widget.category);
      if (!mounted) return;
      setState(() {
        _items
          ..clear()
          ..addAll(page.items);
        _cursor = page.lastDocument;
        _hasMore = page.hasMore;
        _error = null;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e);
    }
  }

  void _open(ArticlesRecord a) {
    unawaited(
      Navigator.of(context)
          .push(ArticleWebViewPage.route(url: a.originalUrl, sourceName: a.sourceName, articleTitle: a.title)),
    );
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    final s = S.of(context);
    final icon = iconForCategory(widget.category?.iconName);

    if (_items.isEmpty) {
      if (_loading) return const Center(child: CircularProgressIndicator());
      return RefreshIndicator(
        onRefresh: _refresh,
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            const SizedBox(height: 120),
            Icon(_error != null ? Icons.cloud_off : icon, size: 48),
            const SizedBox(height: 12),
            Text(_error != null ? s.loadError : s.noArticles, textAlign: TextAlign.center),
            if (_error != null)
              Center(
                child: TextButton(onPressed: _loadMore, child: Text(s.retry)),
              ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _refresh,
      child: ListView.separated(
        controller: _scroll,
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: _items.length + 1,
        separatorBuilder: (_, i) => i == 0 ? const SizedBox(height: 4) : const Divider(indent: 20, endIndent: 20),
        itemBuilder: (context, i) {
          if (i == _items.length) return _footer(s);
          final a = _items[i];
          return ArticleCard(
            article: a,
            fallbackIcon: icon,
            featured: i == 0 && a.imageUrl.isNotEmpty,
            onTap: () => _open(a),
          );
        },
      ),
    );
  }

  Widget _footer(S s) {
    if (_error != null) {
      return Center(
        child: TextButton(onPressed: _loadMore, child: Text(s.retry)),
      );
    }
    if (_hasMore) {
      return const Padding(
        padding: EdgeInsets.all(24),
        child: Center(child: SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2))),
      );
    }
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Center(child: Text(s.endOfFeed, style: Theme.of(context).textTheme.bodySmall)),
    );
  }
}

import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/articles_record.dart';
import '../backend/schema/categories_record.dart';
import '../backend/schema/football_records.dart';
import '../services/weather_service.dart';
import '../widgets/live_matches.dart';
import '../widgets/market_panel.dart';
import '../widgets/weather_card.dart';
import '../l10n/app_strings.dart';
import '../widgets/article_card.dart';
import '../widgets/category_icons.dart';
import 'article_webview_page.dart';
import 'match_detail_page.dart';
import 'team_picker_page.dart';

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
                endDrawer: MarketPanel(settings: settings, repository: repository, weather: WeatherService.shared),
                appBar: AppBar(
                  title: Text(s.appName),
                  actions: [
                    Builder(
                      builder: (context) => IconButton(
                        icon: const Icon(Icons.menu_rounded),
                        tooltip: s.menu,
                        onPressed: () => Scaffold.of(context).openEndDrawer(),
                      ),
                    ),
                    const SizedBox(width: 4),
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
                    ArticleFeed(
                      key: const PageStorageKey('feed-all'),
                      repository: repository,
                      layout: settings.feedLayout,
                      header: _AllHeader(settings: settings, repository: repository),
                    ),
                    for (final c in tabs)
                      ArticleFeed(
                        key: PageStorageKey('feed-${c.id}'),
                        repository: repository,
                        layout: settings.feedLayout,
                        category: c,
                        header: c.id == AppSettings.footballCategoryId
                            ? _FootballHeader(settings: settings, repository: repository)
                            : null,
                      ),
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

/// "Tümü" sekmesinin üstü: hava durumu kartı + takip edilen takımların canlı maçları.
class _AllHeader extends StatelessWidget {
  const _AllHeader({required this.settings, required this.repository});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (settings.weatherEnabled) WeatherCard(service: WeatherService.shared),
        if (settings.followsFootball && settings.followedTeams.isNotEmpty)
          _MatchesStream(
            settings: settings,
            repository: repository,
            // Tümü sekmesinde yalnızca canlı veya bugünkü maçlar
            filter: (m) => m.isLive || DateUtils.isSameDay(m.kickoff.toLocal(), DateTime.now()),
          ),
      ],
    );
  }
}

/// "Futbol" sekmesinin üstü: takım seçimi + maçlar.
class _FootballHeader extends StatelessWidget {
  const _FootballHeader({required this.settings, required this.repository});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final teams = settings.followedTeams;
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(8, 4, 8, 0),
          child: TextButton.icon(
            style: TextButton.styleFrom(alignment: Alignment.centerLeft),
            icon: const Icon(Icons.shield_outlined),
            label: Text(teams.isEmpty ? s.chooseTeams : '${s.myTeams} · ${s.teamsSelected(teams.length)}'),
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => TeamPickerPage(settings: settings, repository: repository),
              ),
            ),
          ),
        ),
        if (teams.isNotEmpty) _MatchesStream(settings: settings, repository: repository),
      ],
    );
  }
}

class _MatchesStream extends StatefulWidget {
  const _MatchesStream({required this.settings, required this.repository, this.filter});

  final AppSettings settings;
  final NewsRepository repository;
  final bool Function(MatchRecord)? filter;

  @override
  State<_MatchesStream> createState() => _MatchesStreamState();
}

class _MatchesStreamState extends State<_MatchesStream> {
  late List<String> _teams = widget.settings.followedTeams;
  late Stream<List<MatchRecord>> _stream = widget.repository.watchMatches(_teams);

  @override
  void didUpdateWidget(covariant _MatchesStream old) {
    super.didUpdateWidget(old);
    final teams = widget.settings.followedTeams;
    if (teams.join(',') != _teams.join(',')) {
      _teams = teams;
      _stream = widget.repository.watchMatches(teams);
    }
  }

  @override
  Widget build(BuildContext context) => StreamBuilder<List<MatchRecord>>(
    stream: _stream,
    builder: (context, snap) {
      final list = (snap.data ?? const <MatchRecord>[]).where(widget.filter ?? (_) => true).toList();
      return LiveMatchesStrip(
        matches: list,
        onTap: (m) => Navigator.of(context).push(
          MaterialPageRoute<void>(
            builder: (_) => MatchDetailPage(fixtureId: m.fixtureId, repository: widget.repository),
          ),
        ),
      );
    },
  );
}

/// Tek bir sekmenin haber listesi: sonsuz kaydırma (20'şer) + aşağı çekip yenileme.
class ArticleFeed extends StatefulWidget {
  const ArticleFeed({
    super.key,
    required this.repository,
    this.category,
    this.header,
    this.layout = FeedLayout.standard,
  });

  /// Haber dizilimi (Ayarlar → Haber dizilimi).
  final FeedLayout layout;

  final NewsRepository repository;
  final CategoriesRecord? category;

  /// Listenin en üstünde gösterilecek içerik (hava durumu, canlı skorlar).
  final Widget? header;

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
            ?widget.header,
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
        itemCount: _items.length + 2,
        separatorBuilder: (_, i) => i <= 1 ? const SizedBox.shrink() : const Divider(indent: 20, endIndent: 20),
        itemBuilder: (context, index) {
          if (index == 0) return widget.header ?? const SizedBox.shrink();
          final i = index - 1;
          if (i == _items.length) return _footer(s);
          final a = _items[i];
          final layout = widget.layout;
          return ArticleCard(
            article: a,
            fallbackIcon: icon,
            compact: layout == FeedLayout.compact,
            featured:
                a.imageUrl.isNotEmpty && (layout == FeedLayout.large || (layout == FeedLayout.standard && i == 0)),
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

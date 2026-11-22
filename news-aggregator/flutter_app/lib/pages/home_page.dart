import 'dart:async';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:flutter/material.dart';

import '../ads/ad_widgets.dart';
import '../ads/ads_config.dart';
import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/articles_record.dart';
import '../backend/schema/categories_record.dart';
import '../backend/schema/football_records.dart';
import '../services/library_store.dart';
import '../services/weather_service.dart';
import '../widgets/live_matches.dart';
import '../widgets/observance_banner.dart';
import '../widgets/market_panel.dart';
import '../widgets/info_strip.dart';
import '../l10n/app_strings.dart';
import '../widgets/article_actions.dart';
import '../widgets/article_card.dart';
import '../widgets/offline_banner.dart';
import '../widgets/category_icons.dart';
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

/// "Tümü" sekmesinin üstü: tek satır bilgi şeridi (gündem · depremler · hava) + takip edilen takımların canlı maçları.
class _AllHeader extends StatelessWidget {
  const _AllHeader({required this.settings, required this.repository});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const ObservanceBanner(),
        InfoStrip(showWeather: settings.weatherEnabled),
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

/// Günlük akış: yalnızca son [maxAge] içindeki haberler ve en fazla [max] haber (eski haber yığını olmasın).
/// Bu sürede hiç haber yoksa sekme boş kalmasın diye en yeni [fallback] haber gösterilir.
/// Dönen `full` true ise sınıra ulaşıldı (daha fazla sayfa istenmez).
({List<ArticlesRecord> items, bool full}) dailyFeed(
  List<ArticlesRecord> all,
  DateTime now, {
  int max = 30,
  Duration maxAge = const Duration(hours: 24),
  int fallback = 10,
}) {
  final cutoff = now.subtract(maxAge);
  final fresh = all.where((a) => (a.breakingAt ?? a.publishedAt ?? now).isAfter(cutoff)).toList();
  final reachedOld = fresh.length < all.length;
  if (fresh.isEmpty) return (items: all.take(fallback).toList(), full: true);
  if (fresh.length >= max) return (items: fresh.take(max).toList(), full: true);
  return (items: fresh, full: reachedOld);
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
    final first = _cursor == null && _items.isEmpty;
    setState(() {
      _loading = true;
      _error = null;
    });
    if (first) unawaited(_showCachedFirstPage());
    try {
      final page = await widget.repository.fetchArticles(category: widget.category, startAfter: first ? null : _cursor);
      if (!mounted) return;
      setState(() {
        final combined = [if (!first) ..._items, ...page.items]; // önbellekten gösterilen sayfa güncel sayfayla değişir
        final daily = dailyFeed(combined, DateTime.now());
        _items
          ..clear()
          ..addAll(daily.items);
        _cursor = page.lastDocument;
        _hasMore = page.hasMore && !daily.full;
      });
      prefetchArticleImages(context, page.items);
    } catch (e) {
      if (mounted) setState(() => _error = e);
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  /// Açılışta ağ cevabını beklemeden, cihazdaki önbellekte kalan son haberleri hemen gösterir.
  Future<void> _showCachedFirstPage() async {
    try {
      final page = await widget.repository.fetchArticles(category: widget.category, fromCache: true);
      if (!mounted || _cursor != null || _items.isNotEmpty || page.items.isEmpty) return;
      setState(() => _items.addAll(dailyFeed(page.items, DateTime.now()).items));
      prefetchArticleImages(context, page.items);
    } catch (_) {} // önbellek boşsa sorun değil
  }

  /// Aşağı çekip yenileme: listeyi sıfırlayıp ilk sayfayı yeniden çeker.
  Future<void> _refresh() async {
    try {
      final page = await widget.repository.fetchArticles(category: widget.category);
      if (!mounted) return;
      setState(() {
        final daily = dailyFeed(page.items, DateTime.now());
        _items
          ..clear()
          ..addAll(daily.items);
        _cursor = page.lastDocument;
        _hasMore = page.hasMore && !daily.full;
        _error = null;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e);
    }
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

    return ListenableBuilder(
      listenable: LibraryStore.instance,
      builder: (context, _) => _list(context, s, icon),
    );
  }

  Widget _list(BuildContext context, S s, IconData icon) {
    // Kullanıcının gizlediği kaynaklar akışta gösterilmez.
    final items = _items.where(LibraryStore.instance.isVisible).toList();
    // İnternet yokken önbellekteki haberler gösteriliyorsa üstte bilgi şeridi.
    final offline = _error != null;
    return RefreshIndicator(
      onRefresh: _refresh,
      child: ListView.separated(
        controller: _scroll,
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: items.length + 2,
        separatorBuilder: (_, i) => i <= 1 ? const SizedBox.shrink() : const Divider(indent: 20, endIndent: 20),
        itemBuilder: (context, index) {
          if (index == 0) {
            if (!offline) return widget.header ?? const SizedBox.shrink();
            return Column(
              mainAxisSize: MainAxisSize.min,
              children: [OfflineBanner(onRetry: _refresh), ?widget.header],
            );
          }
          final i = index - 1;
          if (i == items.length) return _footer(s);
          final a = items[i];
          final layout = widget.layout;
          final card = ArticleCard(
            article: a,
            fallbackIcon: icon,
            compact: layout == FeedLayout.compact,
            featured:
                a.imageUrl.isNotEmpty && (layout == FeedLayout.large || (layout == FeedLayout.standard && i == 0)),
            onTap: () => openArticle(context, a),
          );
          // Her N haberden sonra akış içi doğal reklam (yüklenemezse yer kaplamaz).
          if ((i + 1) % AdsConfig.nativeEvery != 0) return card;
          return Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              card,
              NativeAdSlot(key: ValueKey('native-ad-$i')),
            ],
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

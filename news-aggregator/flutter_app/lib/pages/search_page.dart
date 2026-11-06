import 'dart:async';

import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/articles_record.dart';
import '../l10n/app_strings.dart';
import '../utils/search_text.dart';
import '../widgets/article_card.dart';
import 'article_webview_page.dart';

/// Haberlerde arama (son 30 günün haberleri, başlık ve kaynak adında).
class SearchPage extends StatefulWidget {
  const SearchPage({super.key, required this.settings, this.repository = const NewsRepository()});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  State<SearchPage> createState() => _SearchPageState();
}

class _SearchPageState extends State<SearchPage> {
  final _controller = TextEditingController();
  Timer? _debounce;
  String _query = '';
  Future<List<ArticlesRecord>>? _results;

  @override
  void dispose() {
    _debounce?.cancel();
    _controller.dispose();
    super.dispose();
  }

  void _onChanged(String v) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 450), () => _run(v));
  }

  void _run(String v, {bool remember = false}) {
    setState(() {
      _query = v.trim();
      _results = searchPlan(_query) == null ? null : widget.repository.searchArticles(_query);
    });
    if (remember && searchPlan(_query) != null) widget.settings.addRecentSearch(_query);
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    return Scaffold(
      appBar: AppBar(title: Text(s.search)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 4, 16, 8),
            child: TextField(
              controller: _controller,
              textInputAction: TextInputAction.search,
              onChanged: _onChanged,
              onSubmitted: (v) => _run(v, remember: true),
              decoration: InputDecoration(
                hintText: s.searchHint,
                suffixIcon: _query.isEmpty
                    ? const Icon(Icons.search_rounded)
                    : IconButton(
                        icon: const Icon(Icons.close_rounded),
                        onPressed: () {
                          _controller.clear();
                          _run('');
                        },
                      ),
                filled: true,
                fillColor: theme.colorScheme.surfaceContainer,
                contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(18), borderSide: BorderSide.none),
              ),
            ),
          ),
          Expanded(child: _body(s, muted)),
        ],
      ),
    );
  }

  Widget _body(S s, Color muted) {
    if (_results == null) {
      if (_query.isNotEmpty) {
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Text(s.searchMinChars, style: TextStyle(color: muted)),
        );
      }
      final recent = widget.settings.recentSearches;
      if (recent.isEmpty) return const SizedBox.shrink();
      return ListView(
        children: [
          ListTile(
            title: Text(s.recentSearches, style: const TextStyle(fontWeight: FontWeight.w700)),
            trailing: TextButton(
              onPressed: () async {
                await widget.settings.clearRecentSearches();
                setState(() {});
              },
              child: Text(s.clear),
            ),
          ),
          for (final q in recent)
            ListTile(
              leading: const Icon(Icons.history_rounded),
              title: Text(q),
              onTap: () {
                _controller.text = q;
                _run(q, remember: true);
              },
            ),
        ],
      );
    }
    return FutureBuilder<List<ArticlesRecord>>(
      future: _results,
      builder: (context, snap) {
        if (snap.connectionState != ConnectionState.done) return const Center(child: CircularProgressIndicator());
        if (snap.hasError) {
          // En olası neden: birleşik dizin henüz oluşturulmadı/oluşuyor.
          return Padding(
            padding: const EdgeInsets.all(24),
            child: Text(s.searchIndexNotReady, style: TextStyle(color: muted)),
          );
        }
        final list = snap.data ?? const [];
        if (list.isEmpty) {
          return Padding(
            padding: const EdgeInsets.all(24),
            child: Text(s.noResults, style: TextStyle(color: muted)),
          );
        }
        return ListView.separated(
          itemCount: list.length,
          separatorBuilder: (_, _) => const Divider(indent: 20, endIndent: 20),
          itemBuilder: (context, i) {
            final a = list[i];
            return ArticleCard(
              article: a,
              onTap: () {
                widget.settings.addRecentSearch(_query);
                Navigator.of(
                  context,
                ).push(ArticleWebViewPage.route(url: a.originalUrl, sourceName: a.sourceName, articleTitle: a.title));
              },
            );
          },
        );
      },
    );
  }
}

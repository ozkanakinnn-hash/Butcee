import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/categories_record.dart';
import '../backend/schema/extra_records.dart';
import '../l10n/app_strings.dart';
import '../utils/time_ago.dart';
import 'article_webview_page.dart';
import 'match_detail_page.dart';
import 'settings_page.dart';

/// Gönderilen bildirimler (yalnızca kullanıcının ilgi alanları ve takımları; son 7 gün).
class NotificationsPage extends StatefulWidget {
  const NotificationsPage({super.key, required this.settings, this.repository = const NewsRepository()});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  State<NotificationsPage> createState() => _NotificationsPageState();
}

class _NotificationsPageState extends State<NotificationsPage> {
  late Set<String> _topics = widget.settings.desiredTopics;
  late Stream<List<NotificationRecord>> _stream = widget.repository.watchNotifications(_topics);
  late final Stream<List<CategoriesRecord>> _categories = widget.repository.watchCategories();

  @override
  void initState() {
    super.initState();
    widget.settings.addListener(_onSettings);
  }

  @override
  void dispose() {
    widget.settings.removeListener(_onSettings);
    super.dispose();
  }

  void _onSettings() {
    final t = widget.settings.desiredTopics;
    if (t.length != _topics.length || !t.containsAll(_topics)) {
      setState(() {
        _topics = t;
        _stream = widget.repository.watchNotifications(t);
      });
    }
  }

  void _open(NotificationRecord n) {
    final nav = Navigator.of(context);
    if (n.isMatch && n.fixtureId != null) {
      nav.push(
        MaterialPageRoute<void>(
          builder: (_) => MatchDetailPage(fixtureId: n.fixtureId!, repository: widget.repository),
        ),
      );
    } else if (n.url.isNotEmpty) {
      nav.push(ArticleWebViewPage.route(url: n.url, sourceName: n.source, articleTitle: n.headline));
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: Text(s.notifications.toLocaleUpperCase(s)),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            tooltip: s.settings,
            onPressed: () =>
                Navigator.of(context)
                    .push(MaterialPageRoute<void>(builder: (_) => SettingsPage(settings: widget.settings))),
          ),
        ],
      ),
      body: StreamBuilder<List<CategoriesRecord>>(
        stream: _categories,
        builder: (context, catSnap) {
          final names = {
            for (final c in catSnap.data ?? const <CategoriesRecord>[]) c.id: c.localizedName(s.isEnglish),
          };
          return StreamBuilder<List<NotificationRecord>>(
            stream: _stream,
            builder: (context, snap) {
              if (_topics.isEmpty) {
                return Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text(s.notificationsOff, style: TextStyle(color: muted)),
                );
              }
              if (!snap.hasData) return const Center(child: CircularProgressIndicator());
              final list = snap.data!;
              if (list.isEmpty) {
                return Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text(s.noNotifications, style: TextStyle(color: muted)),
                );
              }
              return ListView.separated(
                padding: const EdgeInsets.only(top: 8, bottom: 24),
                itemCount: list.length,
                separatorBuilder: (_, _) => const Divider(indent: 24, endIndent: 24),
                itemBuilder: (context, i) {
                  final n = list[i];
                  final label = n.isMatch
                      ? s.matchLabel
                      : n.isBreaking
                      ? s.breakingLabel
                      : (names[n.categoryId] ?? n.categoryId).toLocaleUpperCase(s);
                  return InkWell(
                    onTap: () => _open(n),
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(24, 16, 24, 16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (n.source.isNotEmpty)
                            Padding(
                              padding: const EdgeInsets.only(bottom: 8),
                              child: Text(
                                n.source.toLocaleUpperCase(s),
                                style: theme.textTheme.labelMedium?.copyWith(color: muted, letterSpacing: 0.4),
                              ),
                            ),
                          Text(
                            n.headline,
                            style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800, height: 1.25),
                          ),
                          if (n.isMatch && n.body.isNotEmpty)
                            Padding(
                              padding: const EdgeInsets.only(top: 4),
                              child: Text(n.body, style: theme.textTheme.bodyMedium?.copyWith(color: muted)),
                            ),
                          const SizedBox(height: 10),
                          Row(
                            children: [
                              Text(
                                label,
                                style: theme.textTheme.labelMedium?.copyWith(
                                  color: n.isBreaking ? Colors.red.shade400 : muted,
                                  fontWeight: FontWeight.w700,
                                ),
                              ),
                              Padding(
                                padding: const EdgeInsets.symmetric(horizontal: 8),
                                child: Container(
                                  width: 5,
                                  height: 5,
                                  decoration: BoxDecoration(color: Colors.red.shade400, shape: BoxShape.circle),
                                ),
                              ),
                              Text(timeAgo(n.sentAt, s), style: theme.textTheme.labelMedium?.copyWith(color: muted)),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              );
            },
          );
        },
      ),
    );
  }
}

extension on String {
  /// Türkçe büyük harf (i → İ) — dil İngilizceyse normal büyük harf.
  String toLocaleUpperCase(S s) => s.isEnglish ? toUpperCase() : replaceAll('i', 'İ').toUpperCase();
}

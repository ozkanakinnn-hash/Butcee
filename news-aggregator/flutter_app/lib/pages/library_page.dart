import 'package:flutter/material.dart';

import '../l10n/app_strings.dart';
import '../services/library_store.dart';
import '../widgets/article_actions.dart';
import '../widgets/article_card.dart';

/// Kaydedilenler: sonra okumak için ayrılan haberler (cihazda). Sola/sağa kaydırınca kaldırılır, "Geri al" ile döner.
class SavedPage extends StatelessWidget {
  const SavedPage({super.key});

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final library = LibraryStore.instance;
    return Scaffold(
      appBar: AppBar(title: Text(s.savedArticles)),
      body: ListenableBuilder(
        listenable: library,
        builder: (context, _) {
          final list = library.saved;
          if (list.isEmpty) {
            return _Empty(icon: Icons.bookmark_border_rounded, title: s.savedEmptyTitle, body: s.savedEmptyBody);
          }
          return ListView.separated(
            padding: const EdgeInsets.only(bottom: 24),
            itemCount: list.length + 1,
            separatorBuilder: (_, i) => i == 0 ? const SizedBox.shrink() : const Divider(indent: 20, endIndent: 20),
            itemBuilder: (context, i) {
              if (i == 0) return _Hint(text: s.savedHint(list.length));
              final e = list[i - 1];
              final a = e.toRecord();
              return Dismissible(
                key: ValueKey('saved-${e.id}'),
                background: const _SwipeBackground(alignment: Alignment.centerLeft),
                secondaryBackground: const _SwipeBackground(alignment: Alignment.centerRight),
                onDismissed: (_) {
                  library.removeSaved(e);
                  ScaffoldMessenger.of(context)
                    ..hideCurrentSnackBar()
                    ..showSnackBar(
                      SnackBar(
                        content: Text(s.unsavedToast),
                        behavior: SnackBarBehavior.floating,
                        action: SnackBarAction(label: s.undo, onPressed: () => library.restoreSaved(e, i - 1)),
                      ),
                    );
                },
                child: ArticleCard(article: a, dimWhenRead: false, onTap: () => openArticle(context, a)),
              );
            },
          );
        },
      ),
    );
  }
}

/// Okuma geçmişi: son açılan haberler (en fazla 200), güne göre gruplu.
class HistoryPage extends StatelessWidget {
  const HistoryPage({super.key});

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final library = LibraryStore.instance;
    return Scaffold(
      appBar: AppBar(
        title: Text(s.readingHistory),
        actions: [
          ListenableBuilder(
            listenable: library,
            builder: (context, _) => library.history.isEmpty
                ? const SizedBox.shrink()
                : IconButton(
                    key: const ValueKey('history-clear'),
                    icon: const Icon(Icons.delete_sweep_outlined),
                    tooltip: s.clearHistory,
                    onPressed: () => _confirmClear(context),
                  ),
          ),
        ],
      ),
      body: ListenableBuilder(
        listenable: library,
        builder: (context, _) {
          final list = library.history;
          if (list.isEmpty) {
            return _Empty(icon: Icons.history_rounded, title: s.historyEmptyTitle, body: s.historyEmptyBody);
          }
          final now = DateTime.now();
          final rows = <Widget>[_Hint(text: s.historyHint)];
          String? lastGroup;
          for (final e in list) {
            final group = _groupLabel(s, e.at, now);
            if (group != lastGroup) {
              rows.add(_GroupTitle(group));
              lastGroup = group;
            } else {
              rows.add(const Divider(indent: 20, endIndent: 20));
            }
            final a = e.toRecord();
            rows.add(ArticleCard(article: a, dimWhenRead: false, onTap: () => openArticle(context, a)));
          }
          return ListView(padding: const EdgeInsets.only(bottom: 24), children: rows);
        },
      ),
    );
  }

  static String _groupLabel(S s, DateTime at, DateTime now) {
    final day = DateUtils.dateOnly(at);
    final today = DateUtils.dateOnly(now);
    if (day == today) return s.today;
    if (day == today.subtract(const Duration(days: 1))) return s.yesterdayTitle;
    return s.earlier;
  }

  Future<void> _confirmClear(BuildContext context) async {
    final s = S.of(context);
    final ok = await showDialog<bool>(
      context: context,
      builder: (d) => AlertDialog(
        title: Text(s.clearHistory),
        content: Text(s.clearHistoryConfirm),
        actions: [
          TextButton(onPressed: () => Navigator.of(d).pop(false), child: Text(s.cancel)),
          FilledButton(onPressed: () => Navigator.of(d).pop(true), child: Text(s.clear)),
        ],
      ),
    );
    if (ok == true) await LibraryStore.instance.clearHistory();
  }
}

/// Gizlenen kaynaklar: akışta ve aramada gösterilmeyen yayıncılar; buradan yeniden gösterilir.
class MutedSourcesPage extends StatelessWidget {
  const MutedSourcesPage({super.key});

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final library = LibraryStore.instance;
    return Scaffold(
      appBar: AppBar(title: Text(s.mutedSources)),
      body: ListenableBuilder(
        listenable: library,
        builder: (context, _) {
          final list = library.mutedSources;
          if (list.isEmpty) {
            return _Empty(icon: Icons.visibility_outlined, title: s.mutedEmptyTitle, body: s.mutedEmptyBody);
          }
          return ListView(
            children: [
              _Hint(text: s.mutedHint),
              for (final name in list)
                ListTile(
                  leading: CircleAvatar(
                    backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
                    child: Text(name.characters.first.toUpperCase()),
                  ),
                  title: Text(name),
                  trailing: TextButton(
                    key: ValueKey('unmute-$name'),
                    onPressed: () => library.unmuteSource(name),
                    child: Text(s.show),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}

class _Hint extends StatelessWidget {
  const _Hint({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(20, 12, 20, 4),
    child: Text(
      text,
      style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
    ),
  );
}

class _GroupTitle extends StatelessWidget {
  const _GroupTitle(this.text);

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(20, 16, 20, 2),
    child: Text(
      text,
      style: Theme.of(context).textTheme.titleSmall?.copyWith(
        fontWeight: FontWeight.w800,
        color: Theme.of(context).colorScheme.primary,
      ),
    ),
  );
}

class _SwipeBackground extends StatelessWidget {
  const _SwipeBackground({required this.alignment});

  final Alignment alignment;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Container(
      color: scheme.errorContainer,
      alignment: alignment,
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Icon(Icons.bookmark_remove_outlined, color: scheme.onErrorContainer),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty({required this.icon, required this.title, required this.body});

  final IconData icon;
  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(color: theme.colorScheme.primaryContainer, shape: BoxShape.circle),
              child: Icon(icon, size: 34, color: theme.colorScheme.onPrimaryContainer),
            ),
            const SizedBox(height: 16),
            Text(title, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
            const SizedBox(height: 6),
            Text(
              body,
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}

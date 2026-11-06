import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/categories_record.dart';
import '../l10n/app_strings.dart';
import '../widgets/category_icons.dart';
import 'team_picker_page.dart';

/// İlgi alanları: üstte takip edilenler (sürükleyerek sıralanır, − ile çıkarılır),
/// altta keşfedilecekler (+ ile eklenir). Değişiklikler anında kaydedilir.
///
/// [onboarding] true ise ilk açılış ekranıdır: başlık/açıklama ve "Devam et" düğmesi gösterilir.
class InterestsPage extends StatefulWidget {
  const InterestsPage({
    super.key,
    required this.settings,
    this.onboarding = false,
    this.embedded = false,
    this.onDone,
    this.repository = const NewsRepository(),
  });

  final AppSettings settings;
  final bool onboarding;

  /// Alt menü sekmesi olarak gösteriliyorsa geri düğmesi yok.
  final bool embedded;
  final VoidCallback? onDone;
  final NewsRepository repository;

  @override
  State<InterestsPage> createState() => _InterestsPageState();
}

class _InterestsPageState extends State<InterestsPage> {
  late List<String> _selected = List.of(widget.settings.interests);
  late final Stream<List<CategoriesRecord>> _categories = widget.repository.watchCategories();

  /// Sunucuda var olan kategori kimlikleri (önerilenlerden silinmiş olanlar sayılmaz/kaydedilmez).
  Set<String> _known = const {};

  void _save() {
    // İlk açılışta seçim "Devam et" ile onaylanır; diğer durumlarda anında kaydedilir.
    if (!widget.onboarding) widget.settings.setInterests(_selected);
  }

  void _add(String id) {
    setState(() => _selected.add(id));
    _save();
  }

  void _remove(String id) {
    setState(() => _selected.remove(id));
    _save();
  }

  /// [visible]: ekranda görünen (sunucuda var olan) takip listesi; indeksler ona göredir.
  void _reorder(List<String> visible, int oldIndex, int newIndex) {
    final list = List.of(visible);
    // onReorderItem, çıkarılan öğe için newIndex'i zaten düzeltilmiş verir.
    list.insert(newIndex, list.removeAt(oldIndex));
    setState(() => _selected = [...list, ..._selected.where((id) => !visible.contains(id))]);
    _save();
  }

  Future<void> _finish() async {
    await widget.settings.setInterests(_selected.where(_known.contains).toList());
    widget.onDone?.call();
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);

    return StreamBuilder<List<CategoriesRecord>>(
      stream: _categories,
      builder: (context, snap) {
        final all = snap.data ?? const <CategoriesRecord>[];
        final byId = {for (final c in all) c.id: c};
        _known = byId.keys.toSet();
        // Sunucuda artık olmayan kategoriler listeden düşer.
        final followed = _selected.where(byId.containsKey).toList();
        final others = all.where((c) => !_selected.contains(c.id)).toList();

        return Scaffold(
          appBar: AppBar(
            automaticallyImplyLeading: !widget.onboarding && !widget.embedded,
            title: widget.onboarding ? null : Text(s.interests),
          ),
          body: !snap.hasData
              ? const Center(child: CircularProgressIndicator())
              : CustomScrollView(
                  slivers: [
                    if (widget.onboarding)
                      SliverToBoxAdapter(
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(20, 4, 20, 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                s.chooseInterestsTitle,
                                style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800),
                              ),
                              const SizedBox(height: 8),
                              Text(
                                s.chooseInterestsSubtitle,
                                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                              ),
                            ],
                          ),
                        ),
                      ),
                    _SectionHeader(title: s.following, subtitle: followed.length > 1 ? s.followingHint : null),
                    if (followed.isEmpty)
                      SliverToBoxAdapter(child: _EmptyNote(s.noFollowing))
                    else
                      SliverReorderableList(
                        itemCount: followed.length,
                        onReorderItem: (o, n) => _reorder(followed, o, n),
                        proxyDecorator: (child, _, _) =>
                            Material(color: theme.colorScheme.surfaceContainer, child: child),
                        itemBuilder: (context, i) {
                          final c = byId[followed[i]]!;
                          final name = c.localizedName(s.isEnglish);
                          return _CategoryRow(
                            key: ValueKey(c.id),
                            icon: iconForCategory(c.iconName),
                            title: name,
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                _RoundButton(
                                  icon: Icons.remove_rounded,
                                  color: theme.colorScheme.surfaceContainerHighest,
                                  iconColor: theme.colorScheme.onSurface,
                                  tooltip: s.remove(name),
                                  onTap: () => _remove(c.id),
                                ),
                                const SizedBox(width: 8),
                                ReorderableDragStartListener(
                                  index: i,
                                  child: Padding(
                                    padding: const EdgeInsets.all(8),
                                    child: Icon(
                                      Icons.drag_handle_rounded,
                                      semanticLabel: s.reorder,
                                      color: theme.colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          );
                        },
                      ),
                    if (followed.contains(AppSettings.footballCategoryId))
                      SliverToBoxAdapter(
                        child: ListenableBuilder(
                          listenable: widget.settings,
                          builder: (context, _) {
                            final n = widget.settings.followedTeams.length;
                            return ListTile(
                              contentPadding: const EdgeInsets.fromLTRB(20, 4, 16, 4),
                              leading: Icon(Icons.shield_outlined, color: theme.colorScheme.primary, size: 26),
                              title: Text(
                                n == 0 ? s.chooseTeams : s.myTeams,
                                style: TextStyle(color: theme.colorScheme.primary, fontWeight: FontWeight.w700),
                              ),
                              subtitle: Text(n == 0 ? s.chooseTeamsDesc : s.teamsSelected(n)),
                              trailing: const Icon(Icons.chevron_right_rounded),
                              onTap: () => Navigator.of(context).push(
                                MaterialPageRoute<void>(
                                  builder: (_) =>
                                      TeamPickerPage(settings: widget.settings, repository: widget.repository),
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    _SectionHeader(title: s.discover),
                    if (others.isEmpty)
                      SliverToBoxAdapter(child: _EmptyNote(s.allFollowed))
                    else
                      SliverList.builder(
                        itemCount: others.length,
                        itemBuilder: (context, i) {
                          final c = others[i];
                          final name = c.localizedName(s.isEnglish);
                          return _CategoryRow(
                            key: ValueKey('add-${c.id}'),
                            icon: iconForCategory(c.iconName),
                            title: name,
                            onTap: () => _add(c.id),
                            trailing: _RoundButton(
                              icon: Icons.add_rounded,
                              color: theme.colorScheme.primary,
                              iconColor: Colors.white,
                              tooltip: s.add(name),
                              onTap: () => _add(c.id),
                            ),
                          );
                        },
                      ),
                    const SliverToBoxAdapter(child: SizedBox(height: 96)),
                  ],
                ),
          bottomNavigationBar: widget.onboarding
              ? SafeArea(
                  minimum: const EdgeInsets.fromLTRB(20, 8, 20, 16),
                  child: FilledButton(
                    style: FilledButton.styleFrom(
                      minimumSize: const Size.fromHeight(52),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                    ),
                    onPressed: followed.isEmpty ? null : _finish,
                    child: Text(
                      s.continueWith(followed.length),
                      style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
                    ),
                  ),
                )
              : null,
        );
      },
    );
  }
}

class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.title, this.subtitle});

  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SliverToBoxAdapter(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 6),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title.toUpperCase(),
              style: theme.textTheme.labelMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.8,
              ),
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 2),
              Text(subtitle!, style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
            ],
          ],
        ),
      ),
    );
  }
}

class _EmptyNote extends StatelessWidget {
  const _EmptyNote(this.text);

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
    child: Text(text, style: TextStyle(color: Theme.of(context).colorScheme.onSurfaceVariant)),
  );
}

/// Referans tasarımdaki satır: solda gri ikon, ortada ad, sağda işlem düğmesi.
class _CategoryRow extends StatelessWidget {
  const _CategoryRow({super.key, required this.icon, required this.title, required this.trailing, this.onTap});

  final IconData icon;
  final String title;
  final Widget trailing;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: theme.scaffoldBackgroundColor,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 10, 12, 10),
          child: Row(
            children: [
              Icon(icon, size: 26, color: theme.colorScheme.onSurfaceVariant),
              const SizedBox(width: 20),
              Expanded(
                child: Text(
                  title,
                  style: theme.textTheme.titleMedium?.copyWith(fontSize: 18, fontWeight: FontWeight.w500),
                ),
              ),
              trailing,
            ],
          ),
        ),
      ),
    );
  }
}

class _RoundButton extends StatelessWidget {
  const _RoundButton({
    required this.icon,
    required this.color,
    required this.iconColor,
    required this.tooltip,
    required this.onTap,
  });

  final IconData icon;
  final Color color;
  final Color iconColor;
  final String tooltip;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Tooltip(
    message: tooltip,
    child: Material(
      color: color,
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onTap,
        child: SizedBox(width: 34, height: 34, child: Icon(icon, color: iconColor, size: 24)),
      ),
    ),
  );
}

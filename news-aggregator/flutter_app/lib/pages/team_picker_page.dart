import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import '../utils/search_text.dart';

/// Futbol ilgi alanı için takım seçimi (en fazla [AppSettings.maxTeams]). Seçim anında kaydedilir.
class TeamPickerPage extends StatefulWidget {
  const TeamPickerPage({super.key, required this.settings, this.repository = const NewsRepository()});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  State<TeamPickerPage> createState() => _TeamPickerPageState();
}

class _TeamPickerPageState extends State<TeamPickerPage> {
  late final _teams = widget.repository.watchTeams();
  late final List<String> _selected = List.of(widget.settings.followedTeams);
  String _query = '';

  void _toggle(String id) {
    final s = S.of(context);
    setState(() {
      if (_selected.contains(id)) {
        _selected.remove(id);
      } else if (_selected.length >= AppSettings.maxTeams) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(s.maxTeams(AppSettings.maxTeams))));
      } else {
        _selected.add(id);
      }
    });
    widget.settings.setFollowedTeams(_selected);
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    return Scaffold(
      appBar: AppBar(
        title: Text(s.chooseTeams),
        actions: [TextButton(onPressed: () => Navigator.of(context).pop(), child: Text(s.done))],
      ),
      body: StreamBuilder<List<TeamRecord>>(
        stream: _teams,
        builder: (context, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final q = foldForSearch(_query);
          final teams = snap.data!.where((t) => q.isEmpty || foldForSearch('${t.name} ${t.short}').contains(q)).toList()
            ..sort((a, b) {
              final sa = _selected.contains(a.id), sb = _selected.contains(b.id);
              return sa == sb ? foldForSearch(a.name).compareTo(foldForSearch(b.name)) : (sa ? -1 : 1);
            });
          return CustomScrollView(
            slivers: [
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 8),
                  child: Text(s.chooseTeamsDesc, style: theme.textTheme.bodyMedium?.copyWith(color: muted)),
                ),
              ),
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(16, 4, 16, 8),
                  child: TextField(
                    onChanged: (v) => setState(() => _query = v),
                    decoration: InputDecoration(
                      hintText: s.searchTeam,
                      prefixIcon: const Icon(Icons.search_rounded),
                      filled: true,
                      fillColor: theme.colorScheme.surfaceContainer,
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(14), borderSide: BorderSide.none),
                    ),
                  ),
                ),
              ),
              if (snap.data!.isEmpty)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Text(s.noTeamsYet, style: TextStyle(color: muted)),
                  ),
                ),
              SliverList.builder(
                itemCount: teams.length,
                itemBuilder: (context, i) {
                  final t = teams[i];
                  final selected = _selected.contains(t.id);
                  return InkWell(
                    onTap: () => _toggle(t.id),
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(20, 10, 16, 10),
                      child: Row(
                        children: [
                          TeamCode(code: t.code, width: 52, fontSize: 16),
                          const SizedBox(width: 18),
                          Expanded(
                            child: Text(
                              t.name,
                              style: theme.textTheme.titleMedium?.copyWith(fontSize: 18, fontWeight: FontWeight.w500),
                            ),
                          ),
                          AnimatedSwitcher(
                            duration: const Duration(milliseconds: 150),
                            child: Icon(
                              selected ? Icons.check_circle_rounded : Icons.add_circle_rounded,
                              key: ValueKey(selected),
                              size: 34,
                              color: selected ? Colors.green.shade500 : theme.colorScheme.primary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
              const SliverToBoxAdapter(child: SizedBox(height: 40)),
            ],
          );
        },
      ),
    );
  }
}

/// Takım kısaltması (GS, FB, BJK…) — rozet/logo kullanılmaz.
class TeamCode extends StatelessWidget {
  const TeamCode({super.key, required this.code, this.width = 48, this.fontSize = 15});

  final String code;
  final double width;
  final double fontSize;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SizedBox(
      width: width,
      child: Text(
        code,
        maxLines: 1,
        softWrap: false,
        overflow: TextOverflow.fade,
        style: TextStyle(
          fontSize: fontSize,
          fontWeight: FontWeight.w900,
          letterSpacing: 0.3,
          color: theme.colorScheme.onSurfaceVariant,
          fontFeatures: const [FontFeature.tabularFigures()],
        ),
      ),
    );
  }
}

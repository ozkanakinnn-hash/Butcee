import 'package:cached_network_image/cached_network_image.dart';
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
                          TeamLogo(url: t.logo, short: t.short, colors: t.colors, size: 34),
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

/// Takım rozeti: logo varsa logo, yoksa kulüp renklerinde kısa ad (GS, FB, BJK…).
class TeamLogo extends StatelessWidget {
  const TeamLogo({super.key, required this.url, this.short = '', this.colors = const [], this.size = 28});

  final String url;
  final String short;
  final List<Color> colors;
  final double size;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final badge = colors.isEmpty && short.isEmpty
        ? Icon(Icons.shield_outlined, size: size, color: theme.colorScheme.onSurfaceVariant)
        : _Badge(short: short, colors: colors, size: size);
    if (url.isEmpty) return badge;
    return CachedNetworkImage(
      imageUrl: url,
      width: size,
      height: size,
      fit: BoxFit.contain,
      placeholder: (_, _) => badge,
      errorWidget: (_, _, _) => badge,
    );
  }
}

class _Badge extends StatelessWidget {
  const _Badge({required this.short, required this.colors, required this.size});

  final String short;
  final List<Color> colors;
  final double size;

  @override
  Widget build(BuildContext context) {
    final c1 = colors.isNotEmpty ? colors[0] : Theme.of(context).colorScheme.surfaceContainerHighest;
    final c2 = colors.length > 1 ? colors[1] : c1;
    final text = c1.computeLuminance() > 0.5 ? Colors.black : Colors.white;
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        // Çapraz iki renk (kulüp renkleri)
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [c1, c1, c2, c2],
          stops: const [0, 0.5, 0.5, 1],
        ),
        border: Border.all(color: Colors.white24, width: 1),
      ),
      alignment: Alignment.center,
      child: Padding(
        padding: EdgeInsets.all(size * 0.12),
        child: FittedBox(
          child: Text(
            short,
            style: TextStyle(
              color: text,
              fontWeight: FontWeight.w900,
              shadows: [Shadow(color: c1.computeLuminance() > 0.5 ? Colors.white : Colors.black54, blurRadius: 2)],
            ),
          ),
        ),
      ),
    );
  }
}

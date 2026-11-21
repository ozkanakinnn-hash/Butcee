import 'package:flutter/material.dart';

import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import '../theme.dart';

/// Maç durumu etiketi: "CANLI 67'", "Devre", "MS", "Bugün 20:00".
String matchStatusLabel(MatchRecord m, S s, {DateTime? now}) {
  if (m.status == 'HT') return s.halfTime;
  if (m.isLive) return "${s.live} ${m.elapsed ?? ''}'".trim();
  if (m.isFinished) return s.fullTime;
  final n = now ?? DateTime.now();
  final k = m.kickoff.toLocal();
  String two(int v) => v.toString().padLeft(2, '0');
  final time = '${two(k.hour)}:${two(k.minute)}';
  final days = DateTime(k.year, k.month, k.day).difference(DateTime(n.year, n.month, n.day)).inDays;
  if (days == 0) return '${s.today} $time';
  if (days == 1) return '${s.tomorrow} $time';
  return '${two(k.day)}.${two(k.month)} $time';
}

/// Maç kartları alt alta (canlı olanlar önce); akışla birlikte aşağı kayar.
class LiveMatchesStrip extends StatelessWidget {
  const LiveMatchesStrip({super.key, required this.matches, required this.onTap});

  final List<MatchRecord> matches;
  final void Function(MatchRecord) onTap;

  @override
  Widget build(BuildContext context) {
    if (matches.isEmpty) return const SizedBox.shrink();
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          for (var i = 0; i < matches.length; i++) ...[
            if (i > 0) const SizedBox(height: 10),
            MatchCard(match: matches[i], onTap: () => onTap(matches[i])),
          ],
        ],
      ),
    );
  }
}

/// Tam genişlik maç kartı:
///   [ev sahibi]      CANLI 67'      [deplasman]
///                     2  –  1
///   21' A. Yılmaz                   39' C. Şahin
///   58' M. Arslan (P)
class MatchCard extends StatelessWidget {
  const MatchCard({super.key, required this.match, required this.onTap});

  final MatchRecord match;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = S.of(context);
    final m = match;
    final muted = theme.colorScheme.onSurfaceVariant;
    final scoreStyle = theme.textTheme.headlineMedium?.copyWith(
      fontWeight: FontWeight.w900,
      height: 1.1,
      fontFeatures: const [FontFeature.tabularFigures()],
    );

    final homeGoals = m.goals.where((g) => g.teamId == m.home.id).toList();
    final awayGoals = m.goals.where((g) => g.teamId == m.away.id).toList();

    Widget team(MatchTeam t, CrossAxisAlignment align) => Column(
      crossAxisAlignment: align,
      mainAxisSize: MainAxisSize.min,
      children: [
        _TeamBadge(team: t),
        const SizedBox(height: 6),
        Text(
          t.name,
          maxLines: 2,
          overflow: TextOverflow.ellipsis,
          textAlign: align == CrossAxisAlignment.start ? TextAlign.left : TextAlign.right,
          style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700, height: 1.2),
        ),
      ],
    );

    Widget goals(List<GoalEvent> list, CrossAxisAlignment align) => Column(
      crossAxisAlignment: align,
      mainAxisSize: MainAxisSize.min,
      children: [
        for (final g in list)
          Padding(
            padding: const EdgeInsets.only(top: 3),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              textDirection: align == CrossAxisAlignment.start ? TextDirection.ltr : TextDirection.rtl,
              children: [
                Icon(Icons.sports_soccer, size: 13, color: muted),
                const SizedBox(width: 4),
                Text(
                  g.minuteLabel,
                  style: theme.textTheme.labelMedium?.copyWith(fontWeight: FontWeight.w800, color: muted),
                ),
                const SizedBox(width: 4),
                Flexible(
                  child: Text(
                    [
                      g.player,
                      if (g.detail == 'Penalty') '(P)',
                      if (g.detail == 'Own Goal') '(KK)',
                    ].join(' '),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.labelMedium,
                  ),
                ),
              ],
            ),
          ),
      ],
    );

    final status = Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (m.isLive) ...[const LiveDot(), const SizedBox(width: 6)],
        Text(
          matchStatusLabel(m, s),
          style: theme.textTheme.labelLarge?.copyWith(
            color: m.isLive ? AppTheme.breaking : muted,
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );

    final score = m.notStarted
        ? Text('–', style: scoreStyle?.copyWith(color: muted))
        : Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('${m.homeGoals}', style: scoreStyle),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8),
                child: Text('–', style: scoreStyle?.copyWith(color: muted, fontWeight: FontWeight.w400)),
              ),
              Text('${m.awayGoals}', style: scoreStyle),
            ],
          );

    return Material(
      color: theme.colorScheme.surfaceContainer,
      borderRadius: BorderRadius.circular(18),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: onTap,
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.fromLTRB(14, 12, 14, 12),
          decoration: m.isLive
              ? BoxDecoration(
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(color: AppTheme.breaking.withValues(alpha: 0.6)),
                )
              : null,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(child: team(m.home, CrossAxisAlignment.start)),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 8),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        status,
                        const SizedBox(height: 4),
                        score,
                        if (m.round.isNotEmpty)
                          Text(m.round, style: theme.textTheme.labelSmall?.copyWith(color: muted)),
                      ],
                    ),
                  ),
                  Expanded(child: team(m.away, CrossAxisAlignment.end)),
                ],
              ),
              if (m.goals.isNotEmpty) ...[
                const SizedBox(height: 8),
                Divider(height: 1, color: theme.colorScheme.outlineVariant.withValues(alpha: 0.6)),
                const SizedBox(height: 4),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(child: goals(homeGoals, CrossAxisAlignment.start)),
                    const SizedBox(width: 12),
                    Expanded(child: goals(awayGoals, CrossAxisAlignment.end)),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

/// Takım rozeti: logo yoksa takım renkleriyle kısa kod.
class _TeamBadge extends StatelessWidget {
  const _TeamBadge({required this.team});

  final MatchTeam team;

  @override
  Widget build(BuildContext context) {
    final bg = team.colors.isNotEmpty ? team.colors.first : Theme.of(context).colorScheme.surfaceContainerHighest;
    final fg = team.colors.length > 1 ? team.colors[1] : Theme.of(context).colorScheme.onSurface;
    return Container(
      width: 36,
      height: 36,
      alignment: Alignment.center,
      decoration: BoxDecoration(color: bg, shape: BoxShape.circle),
      child: Text(
        team.code.length > 3 ? team.code.substring(0, 3) : team.code,
        style: TextStyle(color: fg, fontSize: 11, fontWeight: FontWeight.w900, letterSpacing: 0.2),
      ),
    );
  }
}

/// Canlı maç için yanıp sönen kırmızı nokta.
class LiveDot extends StatefulWidget {
  const LiveDot({super.key, this.size = 8});

  final double size;

  @override
  State<LiveDot> createState() => _LiveDotState();
}

class _LiveDotState extends State<LiveDot> with SingleTickerProviderStateMixin {
  late final _c = AnimationController(vsync: this, duration: const Duration(milliseconds: 900))..repeat(reverse: true);

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => FadeTransition(
    opacity: Tween(begin: 0.25, end: 1.0).animate(_c),
    child: Container(
      width: widget.size,
      height: widget.size,
      decoration: const BoxDecoration(color: AppTheme.breaking, shape: BoxShape.circle),
    ),
  );
}

import 'package:flutter/material.dart';

import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import '../pages/team_picker_page.dart';

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

/// Yatay kaydırılan maç kartları (canlı olanlar önce).
class LiveMatchesStrip extends StatelessWidget {
  const LiveMatchesStrip({super.key, required this.matches, required this.onTap});

  final List<MatchRecord> matches;
  final void Function(MatchRecord) onTap;

  @override
  Widget build(BuildContext context) {
    if (matches.isEmpty) return const SizedBox.shrink();
    return SizedBox(
      height: 112,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
        itemCount: matches.length,
        separatorBuilder: (_, _) => const SizedBox(width: 10),
        itemBuilder: (context, i) => MatchCard(match: matches[i], onTap: () => onTap(matches[i])),
      ),
    );
  }
}

class MatchCard extends StatelessWidget {
  const MatchCard({super.key, required this.match, required this.onTap});

  final MatchRecord match;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final s = S.of(context);
    final m = match;
    final scoreStyle = theme.textTheme.titleMedium?.copyWith(
      fontWeight: FontWeight.w800,
      fontFeatures: const [FontFeature.tabularFigures()],
    );

    Widget row(MatchTeam t, int goals) => Row(
      children: [
        TeamLogo(url: t.logo, short: t.short, colors: t.colors, size: 20),
        const SizedBox(width: 8),
        Expanded(
          child: Text(
            t.name,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
          ),
        ),
        if (!m.notStarted) Text('$goals', style: scoreStyle),
      ],
    );

    return Material(
      color: theme.colorScheme.surfaceContainer,
      borderRadius: BorderRadius.circular(16),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Container(
          width: 210,
          padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
          decoration: m.isLive
              ? BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.red.withValues(alpha: 0.6)),
                )
              : null,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  if (m.isLive) ...[const LiveDot(), const SizedBox(width: 6)],
                  Text(
                    matchStatusLabel(m, s),
                    style: theme.textTheme.labelMedium?.copyWith(
                      color: m.isLive ? Colors.red : theme.colorScheme.onSurfaceVariant,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
              row(m.home, m.homeGoals),
              row(m.away, m.awayGoals),
            ],
          ),
        ),
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
      decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle),
    ),
  );
}

import 'package:flutter/material.dart';

import '../backend/news_repository.dart';
import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import '../widgets/live_matches.dart';

/// Maç detayı: büyük skor, durum ve goller — Firestore'dan canlı güncellenir.
class MatchDetailPage extends StatelessWidget {
  const MatchDetailPage({super.key, required this.fixtureId, this.repository = const NewsRepository()});

  final int fixtureId;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(),
      body: StreamBuilder<MatchRecord?>(
        stream: repository.watchMatch(fixtureId),
        builder: (context, snap) {
          final m = snap.data;
          if (m == null) return const Center(child: CircularProgressIndicator());
          Widget team(MatchTeam t) => Expanded(
            child: Column(
              children: [
                Text(t.code, style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.w900)),
                const SizedBox(height: 10),
                Text(
                  t.name,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                ),
              ],
            ),
          );
          return ListView(
            padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
            children: [
              if (m.round.isNotEmpty)
                Text(
                  m.round,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.labelMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                ),
              const SizedBox(height: 16),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  team(m.home),
                  Column(
                    children: [
                      Text(
                        m.notStarted ? '–' : '${m.homeGoals} - ${m.awayGoals}',
                        style: theme.textTheme.displaySmall?.copyWith(
                          fontWeight: FontWeight.w900,
                          fontFeatures: const [FontFeature.tabularFigures()],
                        ),
                      ),
                      const SizedBox(height: 6),
                      Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (m.isLive) ...[const LiveDot(), const SizedBox(width: 6)],
                          Text(
                            matchStatusLabel(m, s),
                            style: theme.textTheme.labelLarge?.copyWith(
                              color: m.isLive ? Colors.red : theme.colorScheme.onSurfaceVariant,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  team(m.away),
                ],
              ),
              const SizedBox(height: 28),
              Text(
                s.goals.toUpperCase(),
                style: theme.textTheme.labelMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 0.8,
                ),
              ),
              const SizedBox(height: 8),
              if (m.goals.isEmpty)
                Text(
                  m.notStarted ? s.matchNotStarted : s.noGoals,
                  style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
                )
              else
                for (final g in m.goals)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 6),
                    child: Row(
                      mainAxisAlignment: g.teamId == m.home.id ? MainAxisAlignment.start : MainAxisAlignment.end,
                      children: [
                        const Icon(Icons.sports_soccer, size: 18),
                        const SizedBox(width: 8),
                        Text(
                          '${g.minuteLabel}  ${g.player}${g.detail == 'Own Goal'
                              ? ' (k.k.)'
                              : g.detail == 'Penalty'
                              ? ' (pen.)'
                              : ''}',
                          style: theme.textTheme.bodyLarge,
                        ),
                      ],
                    ),
                  ),
            ],
          );
        },
      ),
    );
  }
}

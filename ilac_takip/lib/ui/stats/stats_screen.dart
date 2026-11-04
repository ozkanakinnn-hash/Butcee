import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../domain/adherence.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/report_actions.dart';
import '../widgets/visuals.dart';
import '../../services/localized_texts.dart';

class StatsScreen extends ConsumerWidget {
  const StatsScreen({super.key});

  static const _moodEmojis = ['😣', '😕', '😐', '🙂', '😄'];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final stats = ref.watch(statsProvider);
    final plans = ref.watch(plansProvider).value ?? const <SupplementPlan>[];
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(
        title: Text(t.statsTitle, style: const TextStyle(fontWeight: FontWeight.w800)),
        actions: [
          IconButton(
            tooltip: t.toolDoctorReport,
            icon: const Icon(Icons.medical_information_outlined),
            onPressed: () => ReportActions.shareDoctorReport(context, ref),
          ),
        ],
      ),
      body: switch (stats) {
        AsyncValue(:final value?) => ListView(padding: const EdgeInsets.fromLTRB(12, 8, 12, 32), children: [
            Center(
              child: ProgressRing(
                key: const Key('scoreRing'),
                value: value.score.total / 100,
                center: value.month.planned == 0 ? '—' : '${value.score.total}',
                caption: t.outOf100,
                size: 170,
                stroke: 14,
              ),
            ),
            const SizedBox(height: 6),
            Center(child: Text(t.balanceScore, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w800))),
            const SizedBox(height: 6),
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              _scorePart(context, '${value.score.adherencePart}', t.adherencePart),
              Container(width: 1, height: 32, color: Theme.of(context).dividerColor, margin: const EdgeInsets.symmetric(horizontal: 16)),
              _scorePart(context, '${value.score.moodPart}', t.selfReportPart),
            ]),
            const SizedBox(height: 16),
            Row(children: [
              Expanded(child: _tile(context, '${value.streak}', t.streakTile, t.streakSub, const Color(0xFFF57C00))),
              Expanded(child: _tile(context, _pct(value.week), t.thisWeek, t.takenRate, adherenceColor(context, value.week.adherence))),
              Expanded(child: _tile(context, _pct(value.month), t.thisMonth, t.takenRate, adherenceColor(context, value.month.adherence))),
            ]),
            _Card(title: t.last14Days, child: _DayBars(value)),
            _Card(
              title: t.perSupplement,
              child: Column(children: [
                if (plans.isEmpty) Text(t.noDataYet),
                for (final p in plans)
                  Builder(builder: (context) {
                    final s = value.perMedicine[p.medicine.id];
                    final v = s == null || s.planned == 0 ? null : s.adherence;
                    return ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: SupplementAvatar(p.medicine, size: 40),
                      title: Text(p.medicine.displayName),
                      subtitle: v == null
                          ? Text(t.noDataYet)
                          : Padding(
                              padding: const EdgeInsets.only(top: 6),
                              child: LinearProgressIndicator(
                                value: v,
                                minHeight: 6,
                                borderRadius: BorderRadius.circular(3),
                                color: adherenceColor(context, v),
                              ),
                            ),
                      trailing: Text(v == null ? '—' : '${(v * 100).round()}%', style: text.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
                    );
                  }),
              ]),
            ),
            _Card(
              title: t.moodTitle,
              child: value.moods.isEmpty
                  ? Text(t.noDataYet)
                  : Wrap(spacing: 4, runSpacing: 4, children: [
                      for (final d in value.days30)
                        Tooltip(
                          message: DateFormat.MMMd(t.localeName).format(d.day),
                          child: Text(
                            value.moods[dayKey(d.day)] == null ? '·' : _moodEmojis[value.moods[dayKey(d.day)]! - 1],
                            style: const TextStyle(fontSize: 18),
                          ),
                        ),
                    ]),
            ),
            const SizedBox(height: 12),
            FilledButton.tonalIcon(
              icon: const Icon(Icons.picture_as_pdf_outlined),
              label: Text(t.toolDoctorReport),
              onPressed: () => ReportActions.shareDoctorReport(context, ref),
            ),
          ]),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }

  String _pct(AdherenceStats s) => s.planned == 0 ? '—' : '${(s.adherence * 100).round()}%';

  Widget _scorePart(BuildContext context, String v, String label) => Column(children: [
        Text(v, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
        Text(label, style: Theme.of(context).textTheme.bodySmall),
      ]);

  Widget _tile(BuildContext context, String big, String title, String sub, Color color) => Card(
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
          child: Column(children: [
            Text(big, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900, color: color)),
            Text(title, textAlign: TextAlign.center, style: const TextStyle(fontWeight: FontWeight.w700)),
            Text(sub, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodySmall),
          ]),
        ),
      );
}

class _Card extends StatelessWidget {
  const _Card({required this.title, required this.child});
  final String title;
  final Widget child;

  @override
  Widget build(BuildContext context) => Card(
        margin: const EdgeInsets.only(top: 12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(upper(title, context.l10n.localeName),
                style: Theme.of(context).textTheme.labelLarge?.copyWith(
                    letterSpacing: 1.1, fontWeight: FontWeight.w700, color: Theme.of(context).colorScheme.primary)),
            const SizedBox(height: 12),
            child,
          ]),
        ),
      );
}

/// Son 14 günün uyum çubukları.
class _DayBars extends StatelessWidget {
  const _DayBars(this.data);
  final StatsData data;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final days = data.days30.sublist(data.days30.length - 14);
    final cs = Theme.of(context).colorScheme;
    final wd = DateFormat.E(t.localeName);
    return SizedBox(
      height: 110,
      child: Row(crossAxisAlignment: CrossAxisAlignment.end, children: [
        for (final d in days)
          Expanded(
            child: Column(mainAxisAlignment: MainAxisAlignment.end, children: [
              Container(
                height: d.hasPlan ? 8 + 70 * d.stats.adherence : 4,
                margin: const EdgeInsets.symmetric(horizontal: 3),
                decoration: BoxDecoration(
                  color: d.hasPlan ? adherenceColor(context, d.stats.adherence) : cs.outlineVariant,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
              const SizedBox(height: 4),
              Text(wd.format(d.day).characters.first, style: TextStyle(fontSize: 10, color: cs.onSurfaceVariant)),
              Text('${d.day.day}', style: TextStyle(fontSize: 9, color: cs.onSurfaceVariant)),
            ]),
          ),
      ]),
    );
  }
}

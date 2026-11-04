import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import 'pending_panel.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final today = ref.watch(todayDosesProvider);
    final stats = ref.watch(todayStatsProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(t.appTitle),
        actions: [
          IconButton(
            tooltip: t.medicines,
            icon: const Icon(Icons.medication_outlined),
            onPressed: () => context.push('/medicines'),
          ),
          IconButton(
            tooltip: t.settings,
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => context.push('/settings'),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        icon: const Icon(Icons.add),
        label: Text(t.addMedicine),
        onPressed: () => _showAddSheet(context),
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(servicesProvider).planner.reconcile(),
        child: ListView(
          padding: const EdgeInsets.only(bottom: 96),
          children: [
            const PendingPanel(),
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
              child: Wrap(
                alignment: WrapAlignment.spaceBetween,
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 12,
                children: [
                  Text(t.today, style: Theme.of(context).textTheme.titleLarge),
                  if (stats.planned > 0) Text(t.todayProgress(stats.taken, stats.planned)),
                ],
              ),
            ),
            if (stats.planned > 0)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: LinearProgressIndicator(value: stats.adherence, minHeight: 8),
              ),
            ...switch (today) {
              AsyncValue(:final value?) when value.isEmpty => [
                  Padding(padding: const EdgeInsets.all(32), child: Center(child: Text(t.nothingToday))),
                ],
              AsyncValue(:final value?) => value.map((v) => _DoseTile(v)),
              _ => [const Padding(padding: EdgeInsets.all(32), child: Center(child: CircularProgressIndicator()))],
            },
          ],
        ),
      ),
    );
  }

  void _showAddSheet(BuildContext context) {
    final t = context.l10n;
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          ListTile(
            leading: const Icon(Icons.qr_code_scanner),
            title: Text(t.scanBox),
            onTap: () {
              Navigator.pop(ctx);
              context.push('/scan');
            },
          ),
          ListTile(
            leading: const Icon(Icons.edit_outlined),
            title: Text(t.addManually),
            onTap: () {
              Navigator.pop(ctx);
              context.push('/medicines/add');
            },
          ),
        ]),
      ),
    );
  }
}

class _DoseTile extends StatelessWidget {
  const _DoseTile(this.v);
  final DoseView v;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final d = v.dose;
    final cs = Theme.of(context).colorScheme;
    final color = switch (d.status) {
      DoseStatus.taken => cs.primary,
      DoseStatus.missed => cs.error,
      _ => cs.onSurfaceVariant,
    };
    return ListTile(
      onTap: d.isResolved ? null : () => context.push('/alarm/${d.logId}'),
      leading: Text(timeLabel(t, d.scheduledTime), style: Theme.of(context).textTheme.titleMedium),
      title: Text(v.medicine.displayName),
      subtitle: Wrap(spacing: 8, runSpacing: 4, crossAxisAlignment: WrapCrossAlignment.center, children: [
        Text(quantityLabel(t, d.quantity, v.medicine.form)),
        MealBadge(d.meal),
        Text(statusLabel(t, d.status), style: TextStyle(color: color, fontWeight: FontWeight.w600)),
      ]),
      trailing: Icon(statusIcon(d.status), color: color, semanticLabel: statusLabel(t, d.status)),
    );
  }
}

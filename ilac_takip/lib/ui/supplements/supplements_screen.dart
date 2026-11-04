import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../services/localized_texts.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/report_actions.dart';
import '../widgets/visuals.dart';

class SupplementsScreen extends ConsumerWidget {
  const SupplementsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final plans = ref.watch(plansProvider);

    return Scaffold(
      appBar: AppBar(
        title: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(t.mySupplements, style: const TextStyle(fontWeight: FontWeight.w800)),
          if (plans.value != null) Text(t.supplementsCount(plans.value!.length), style: Theme.of(context).textTheme.bodySmall),
        ]),
        actions: [
          IconButton(
            tooltip: t.toolExportPdf,
            icon: const Icon(Icons.picture_as_pdf_outlined),
            onPressed: () => ReportActions.exportPdf(context, ref),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: null, // sekmeler canlı tutulur; varsayılan etiket Hero çakışması yaratır
        tooltip: t.addMedicine,
        onPressed: () => context.push('/catalog'),
        child: const Icon(Icons.add),
      ),
      body: ListView(padding: const EdgeInsets.only(bottom: 96), children: [
        Card(
          margin: const EdgeInsets.fromLTRB(12, 8, 12, 4),
          color: Theme.of(context).colorScheme.secondaryContainer,
          child: ListTile(
            leading: Icon(Icons.auto_awesome_rounded, color: Theme.of(context).colorScheme.onSecondaryContainer),
            title: Text(t.browsePresets, style: const TextStyle(fontWeight: FontWeight.w700)),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push('/presets'),
          ),
        ),
        ...switch (plans) {
          AsyncValue(:final value?) when value.isEmpty => [
              Padding(padding: const EdgeInsets.all(32), child: Center(child: Text(t.noMedicines))),
            ],
          AsyncValue(:final value?) => value.map((p) => _PlanCard(p)),
          _ => [const Padding(padding: EdgeInsets.all(32), child: Center(child: CircularProgressIndicator()))],
        },
      ]),
    );
  }
}

class _PlanCard extends StatelessWidget {
  const _PlanCard(this.p);
  final SupplementPlan p;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final m = p.medicine;
    final slots = p.schedule?.slots ?? const [];
    final when = slots.isNotEmpty
        ? slots.map((s) => slotLabel(t, s)).join(' · ')
        : p.times.map((x) => formatHmText(x.h, x.m)).join(' · ');
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () => context.push('/supplement/${m.id}'),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(children: [
            SupplementAvatar(m, size: 52),
            const SizedBox(width: 14),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(m.displayName, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                Text(p.isAsNeeded ? t.asNeededShort : when, style: Theme.of(context).textTheme.bodyMedium),
                Text(
                  [supplementDoseLabel(t, m, p.rule?.quantity ?? 1), if (p.schedule?.paused ?? false) t.paused].join(' · '),
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ]),
            ),
            if (m.isLowStock && m.totalStock > 0)
              Padding(
                padding: const EdgeInsets.only(right: 4),
                child: Icon(Icons.inventory_2_outlined, color: Theme.of(context).colorScheme.error, semanticLabel: t.lowStockBadge),
              ),
            const Icon(Icons.chevron_right),
          ]),
        ),
      ),
    );
  }
}

String formatHmText(int h, int m) => '${h.toString().padLeft(2, '0')}:${m.toString().padLeft(2, '0')}';

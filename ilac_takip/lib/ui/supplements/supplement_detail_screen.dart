import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../data/supplement_catalog.dart';
import '../../domain/dose_rule.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/visuals.dart';

class SupplementDetailScreen extends ConsumerWidget {
  const SupplementDetailScreen({super.key, required this.medicineId});
  final int medicineId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final plans = ref.watch(plansProvider).value;
    final conflicts = ref.watch(conflictsProvider).value ?? const <Conflict>[];
    final p = plans?.where((x) => x.medicine.id == medicineId).firstOrNull;
    if (plans == null) return const Scaffold(body: Center(child: CircularProgressIndicator()));
    if (p == null) return Scaffold(appBar: AppBar(), body: Center(child: Text(t.alarmNotFound)));

    final m = p.medicine;
    final info = p.info;
    final myIds = {for (final x in plans) x.medicine.catalogId: x.medicine};
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(actions: [
        IconButton(
          tooltip: t.delete,
          icon: const Icon(Icons.delete_outline),
          onPressed: () => _delete(context, ref, m),
        ),
      ]),
      body: ListView(padding: const EdgeInsets.fromLTRB(16, 0, 16, 32), children: [
        Row(children: [
          SupplementAvatar(m, size: 64),
          const SizedBox(width: 16),
          Expanded(
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(m.displayName, style: text.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
              Text(categoryLabel(t, m.category), style: text.bodyMedium),
            ]),
          ),
        ]),
        const SizedBox(height: 16),
        _Section(title: t.dosePlan, children: [
          _kv(context, t.colDose, supplementDoseLabel(t, m, p.rule?.quantity ?? 1)),
          _kv(context, t.colPlan, ruleLabel(t, p.rule)),
          if ((p.schedule?.slots ?? const []).isNotEmpty)
            _kv(context, t.whenToTake, p.schedule!.slots.map((s) => '${slotLabel(t, s)} ${ref.watch(settingsProvider).slotTime(s)}').join(', ')),
          if (p.rule != null && p.rule!.meal != MealRelation.none)
            Padding(padding: const EdgeInsets.only(top: 6), child: Align(alignment: Alignment.centerLeft, child: MealBadge(p.rule!.meal))),
          _kv(context, t.stock, quantityLabel(t, m.totalStock, m.doseUnit == 'capsule' ? 'capsule' : m.form)),
          if (p.rule != null && !p.isAsNeeded) ...[
            const SizedBox(height: 8),
            Text(t.nextDays14, style: text.labelMedium),
            const SizedBox(height: 6),
            Next14Days(rule: p.rule!),
          ],
          const SizedBox(height: 8),
          if (p.schedule != null)
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: Text(p.schedule!.paused ? t.resume : t.pause),
              value: !p.schedule!.paused,
              onChanged: (on) async {
                final s = ref.read(servicesProvider);
                await s.db.upsertSchedule(p.schedule!.copyWith(paused: !on));
                await s.planner.rebuildSchedule(p.schedule!.id);
              },
            ),
        ]),
        if (info != null) ...[
          _Section(title: t.referenceValues, children: [
            if (info.hasReference)
              Text(t.rdaLine(info.isAi ? 'AI' : 'RDA', '${formatQty(info.rdaMale!)} ${info.unit}', '${formatQty(info.rdaFemale!)} ${info.unit}'))
            else
              Text(t.noReference),
            const SizedBox(height: 4),
            Text(info.ul != null ? t.ulLine('${formatQty(info.ul!)} ${info.unit}') : t.noUl),
            if (info.ulNote != null) Text(info.ulNote!.of(t.localeName), style: text.bodySmall),
            ..._ulWarning(context, p, info),
            if (info.note != null) ...[const SizedBox(height: 8), Text(info.note!.of(t.localeName))],
            const SizedBox(height: 8),
            Text(t.sourceNih, style: text.bodySmall?.copyWith(fontStyle: FontStyle.italic)),
          ]),
          ..._compat(context, info, myIds, conflicts),
        ],
      ]),
    );
  }

  List<Widget> _ulWarning(BuildContext context, SupplementPlan p, SupplementInfo info) {
    final t = context.l10n;
    final m = p.medicine;
    if (info.ul == null || m.doseAmount == null || m.doseUnit == null || p.rule == null) return const [];
    final total = dailyTotalInCatalogUnit(info, m.doseAmount! * p.rule!.quantity, m.doseUnit!, p.rule!.averageDailyQuantity() / p.rule!.quantity);
    if (total == null || total <= info.ul!) return const [];
    return [
      const SizedBox(height: 8),
      Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(color: Theme.of(context).colorScheme.errorContainer, borderRadius: BorderRadius.circular(8)),
        child: Text(t.ulWarning('${formatQty(total)} ${info.unit}', '${formatQty(info.ul!)} ${info.unit}'),
            style: TextStyle(color: Theme.of(context).colorScheme.onErrorContainer, fontWeight: FontWeight.w600)),
      ),
    ];
  }

  List<Widget> _compat(BuildContext context, SupplementInfo info, Map<String?, Medicine> mine, List<Conflict> conflicts) {
    final t = context.l10n;
    final rules = rulesFor(info.id);
    if (rules.isEmpty) return const [];
    Widget row(CompatRule r) {
      final otherId = r.other(info.id);
      final other = catalogById(otherId);
      final inList = mine.containsKey(otherId);
      final clash = conflicts.any((c) => c.rule == r);
      return ListTile(
        contentPadding: EdgeInsets.zero,
        leading: Text(other?.emoji ?? '•', style: const TextStyle(fontSize: 24)),
        title: Text.rich(TextSpan(children: [
          TextSpan(text: other?.name.of(t.localeName) ?? otherId, style: const TextStyle(fontWeight: FontWeight.w600)),
          if (inList) TextSpan(text: '  · ${t.inYourList}', style: TextStyle(color: Theme.of(context).colorScheme.primary, fontSize: 12)),
        ])),
        subtitle: Text(r.reason.of(t.localeName)),
        trailing: r.type == CompatType.separate && inList
            ? Icon(clash ? Icons.warning_amber_rounded : Icons.check_circle,
                color: clash ? const Color(0xFFE65100) : Theme.of(context).colorScheme.primary,
                semanticLabel: clash ? t.sameTimeConflict : t.separatedOk)
            : null,
      );
    }

    final groups = {
      CompatType.synergy: t.compatibleWith,
      CompatType.separate: t.takeSeparately,
      CompatType.caution: t.cautionLabel,
    };
    return [
      for (final g in groups.entries)
        if (rules.any((r) => r.type == g.key)) _Section(title: g.value, children: [for (final r in rules.where((r) => r.type == g.key)) row(r)]),
    ];
  }

  Widget _kv(BuildContext context, String k, String v) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
          SizedBox(width: 110, child: Text(k, style: Theme.of(context).textTheme.bodySmall)),
          Expanded(child: Text(v, style: const TextStyle(fontWeight: FontWeight.w600))),
        ]),
      );

  Future<void> _delete(BuildContext context, WidgetRef ref, Medicine m) async {
    final t = context.l10n;
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        content: Text(t.deleteMedicineConfirm(m.displayName)),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: Text(t.cancel)),
          FilledButton(onPressed: () => Navigator.pop(ctx, true), child: Text(t.delete)),
        ],
      ),
    );
    if (ok != true) return;
    final s = ref.read(servicesProvider);
    await s.db.deleteMedicine(m.id!);
    await s.planner.reconcile();
    if (context.mounted) context.pop();
  }
}

class _Section extends StatelessWidget {
  const _Section({required this.title, required this.children});
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) => Card(
        margin: const EdgeInsets.only(top: 12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(upper(title, context.l10n.localeName),
                style: Theme.of(context).textTheme.labelLarge?.copyWith(
                    letterSpacing: 1.1, fontWeight: FontWeight.w700, color: Theme.of(context).colorScheme.primary)),
            const SizedBox(height: 8),
            ...children,
          ]),
        ),
      );
}

/// Sonraki 14 günün doz noktaları (planın nasıl işlediğini görsel olarak doğrulamak için).
class Next14Days extends StatelessWidget {
  const Next14Days({super.key, required this.rule});
  final DoseRule rule;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final today = dateOnly(DateTime.now());
    final days = rule.occurrences(today, DateTime(today.year, today.month, today.day + 14)).map(dateOnly).toSet();
    final cs = Theme.of(context).colorScheme;
    final wd = DateFormat.E(t.localeName);
    return Row(children: [
      for (var i = 0; i < 14; i++)
        Expanded(
          child: Builder(builder: (context) {
            final d = DateTime(today.year, today.month, today.day + i);
            final on = days.contains(d);
            return Column(children: [
              Text(wd.format(d).characters.first, style: TextStyle(fontSize: 10, color: cs.onSurfaceVariant)),
              const SizedBox(height: 3),
              Container(
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: on ? cs.primary : null,
                  border: Border.all(color: on ? cs.primary : cs.outlineVariant, width: 1.5),
                ),
              ),
              const SizedBox(height: 3),
              Text('${d.day}', style: TextStyle(fontSize: 9, color: cs.onSurfaceVariant)),
            ]);
          }),
        ),
    ]);
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../data/supplement_catalog.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../home/pending_panel.dart';
import '../widgets/common.dart';
import '../widgets/dose_ui_actions.dart';
import '../widgets/visuals.dart';

final exactAlarmAllowedProvider = FutureProvider<bool>((ref) async {
  ref.watch(dbTickProvider);
  return ref.watch(servicesProvider).scheduler.canScheduleExact();
});

class TodayScreen extends ConsumerWidget {
  const TodayScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final today = ref.watch(todayDosesProvider).value ?? const <DoseView>[];
    final plans = ref.watch(plansProvider).value ?? const <SupplementPlan>[];
    final conflicts = ref.watch(conflictsProvider).value ?? const <Conflict>[];
    final streak = ref.watch(statsProvider).value?.streak ?? 0;
    final exactOk = ref.watch(exactAlarmAllowedProvider).value ?? true;

    final planned = today.where((v) => !v.dose.isExtra).toList();
    final taken = planned.where((v) => v.dose.status == DoseStatus.taken).length;
    final asNeeded = plans.where((p) => p.isAsNeeded).toList();

    return Scaffold(
      appBar: AppBar(
        title: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(t.navToday, style: const TextStyle(fontWeight: FontWeight.w800)),
          Text(DateFormat.MMMMEEEEd(t.localeName).format(DateTime.now()), style: Theme.of(context).textTheme.bodySmall),
        ]),
        actions: [
          if (streak > 0)
            Padding(
              padding: const EdgeInsets.only(right: 4),
              child: Chip(
                key: const Key('streakChip'),
                avatar: const Icon(Icons.local_fire_department_rounded, color: Color(0xFFF57C00)),
                label: Text('$streak'),
                visualDensity: VisualDensity.compact,
              ),
            ),
          IconButton(tooltip: t.settings, icon: const Icon(Icons.settings_outlined), onPressed: () => context.push('/settings')),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        heroTag: null, // sekmeler canlı tutulur; varsayılan etiket Hero çakışması yaratır
        tooltip: t.addMedicine,
        onPressed: () => context.push('/catalog'),
        child: const Icon(Icons.add),
      ),
      body: plans.isEmpty
          ? const _EmptyToday()
          : RefreshIndicator(
              onRefresh: () => ref.read(servicesProvider).planner.reconcile(),
              child: ListView(padding: const EdgeInsets.only(bottom: 96), children: [
                if (!exactOk)
                  Card(
                    margin: const EdgeInsets.fromLTRB(12, 8, 12, 0),
                    child: ListTile(
                      leading: const Icon(Icons.alarm_off, color: Color(0xFFD84315)),
                      title: Text(t.exactAlarmOff),
                      trailing: TextButton(onPressed: () => context.push('/reliability'), child: Text(t.fix)),
                    ),
                  ),
                const SizedBox(height: 12),
                Center(
                  child: ProgressRing(
                    key: const Key('todayRing'),
                    value: planned.isEmpty ? 0 : taken / planned.length,
                    center: '$taken/${planned.length}',
                    caption: t.takenLabel,
                  ),
                ),
                if (conflicts.isNotEmpty) _ConflictCard(conflicts),
                const PendingPanel(),
                ..._sections(context, ref, today),
                if (asNeeded.isNotEmpty) ...[
                  SectionHeader(t.asNeededShort),
                  for (final p in asNeeded) _AsNeededTile(plan: p, todayCount: today.where((v) => v.medicine.id == p.medicine.id && v.dose.isExtra).length),
                ],
                const _MoodRow(),
              ]),
            ),
    );
  }

  List<Widget> _sections(BuildContext context, WidgetRef ref, List<DoseView> doses) {
    final t = context.l10n;
    final groups = <String, ({String title, String time, List<DoseView> items})>{};
    for (final v in doses.where((v) => !v.dose.isExtra)) {
      final time = timeLabel(t, v.dose.scheduledTime);
      final slot = v.dose.slot;
      final key = slot != null ? '$time$slot' : '${v.dose.scheduledTime.hour.toString().padLeft(2, '0')}${hourGroupLabel(t, v.dose.scheduledTime.hour)}';
      final title = slot != null ? slotLabel(t, slot) : hourGroupLabel(t, v.dose.scheduledTime.hour);
      groups.putIfAbsent(key, () => (title: title, time: slot != null ? time : '', items: <DoseView>[])).items.add(v);
    }
    final keys = groups.keys.toList()..sort();
    return [
      for (final k in keys) ...[
        SectionHeader(groups[k]!.title, trailing: groups[k]!.time),
        for (final v in groups[k]!.items) _DoseCard(v),
      ],
    ];
  }
}

class _DoseCard extends ConsumerWidget {
  const _DoseCard(this.v);
  final DoseView v;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final d = v.dose;
    final cs = Theme.of(context).colorScheme;
    final done = d.status == DoseStatus.taken;
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: () => context.push('/supplement/${v.medicine.id}'),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(children: [
            SupplementAvatar(v.medicine),
            const SizedBox(width: 12),
            Expanded(
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(v.medicine.displayName, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 2),
                Wrap(spacing: 6, runSpacing: 4, crossAxisAlignment: WrapCrossAlignment.center, children: [
                  Text(supplementDoseLabel(t, v.medicine, d.quantity), style: Theme.of(context).textTheme.bodySmall),
                  MealBadge(d.meal),
                  if (d.status == DoseStatus.snoozed || d.status == DoseStatus.missed || d.status == DoseStatus.skipped)
                    Text(statusLabel(t, d.status), style: TextStyle(color: cs.error, fontSize: 12, fontWeight: FontWeight.w600)),
                ]),
              ]),
            ),
            if (d.status == DoseStatus.skipped)
              Icon(Icons.remove_circle_outline, color: cs.outline, semanticLabel: t.statusSkipped)
            else
              IconButton.filledTonal(
                key: Key('take_${d.logId}'),
                tooltip: done ? t.statusTaken : t.take,
                isSelected: done,
                selectedIcon: const Icon(Icons.check),
                style: IconButton.styleFrom(
                  backgroundColor: done ? cs.primary : null,
                  foregroundColor: done ? cs.onPrimary : null,
                ),
                icon: const Icon(Icons.radio_button_unchecked),
                onPressed: done ? null : () => DoseUiActions.take(context, ref, d.logId),
              ),
          ]),
        ),
      ),
    );
  }
}

class _AsNeededTile extends ConsumerWidget {
  const _AsNeededTile({required this.plan, required this.todayCount});
  final SupplementPlan plan;
  final int todayCount;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(12, 10, 12, 4),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Row(children: [
            SupplementAvatar(plan.medicine, size: 40),
            const SizedBox(width: 12),
            Expanded(child: Text(plan.medicine.displayName, style: Theme.of(context).textTheme.titleMedium)),
          ]),
          Row(children: [
            Text('${t.today}: $todayCount', style: Theme.of(context).textTheme.bodySmall),
            const Spacer(),
            Flexible(
              child: TextButton.icon(
                icon: const Icon(Icons.add_circle_outline),
                label: Text(t.addAnotherDose, overflow: TextOverflow.ellipsis),
                onPressed: () async {
                  await ref.read(servicesProvider).actions.logExtra(plan.medicine.id!);
                  if (context.mounted) showSnack(context, t.extraDoseLogged);
                },
              ),
            ),
          ]),
        ]),
      ),
    );
  }
}

class _ConflictCard extends StatelessWidget {
  const _ConflictCard(this.conflicts);
  final List<Conflict> conflicts;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return Card(
      key: const Key('conflictCard'),
      margin: const EdgeInsets.fromLTRB(12, 12, 12, 0),
      color: const Color(0xFFFFF3E0),
      child: Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ExpansionTile(
          leading: const Icon(Icons.warning_amber_rounded, color: Color(0xFFE65100)),
          title: Text(t.interactionsFound(conflicts.length),
              style: const TextStyle(color: Color(0xFF5D2600), fontWeight: FontWeight.w700)),
          iconColor: const Color(0xFF5D2600),
          collapsedIconColor: const Color(0xFF5D2600),
          children: [
            for (final c in conflicts)
              ListTile(
                dense: true,
                title: Text('${c.first.name} ↔ ${c.second.name}', style: const TextStyle(color: Color(0xFF3E1A00), fontWeight: FontWeight.w600)),
                subtitle: Text(c.rule.reason.of(t.localeName), style: const TextStyle(color: Color(0xFF3E1A00))),
                trailing: TextButton(
                  onPressed: () => context.push('/supplement/${c.second.medicineId}'),
                  child: Text(t.editSupplement),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _MoodRow extends ConsumerWidget {
  const _MoodRow();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final mood = ref.watch(todayMoodProvider).value;
    return Card(
      margin: const EdgeInsets.fromLTRB(12, 20, 12, 0),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(children: [
          Text(t.moodQuestion, style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          Row(mainAxisAlignment: MainAxisAlignment.spaceEvenly, children: [
            for (var i = 0; i < moodIcons.length; i++)
              InkWell(
                customBorder: const CircleBorder(),
                onTap: () => ref.read(servicesProvider).db.setMood(DateTime.now(), i + 1),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 200),
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: mood == i + 1 ? Theme.of(context).colorScheme.primaryContainer : null,
                  ),
                  child: Icon(moodIcons[i], size: mood == i + 1 ? 36 : 30, color: mood == i + 1 ? moodColors[i] : Theme.of(context).colorScheme.outline),
                ),
              ),
          ]),
        ]),
      ),
    );
  }
}

class _EmptyToday extends StatelessWidget {
  const _EmptyToday();

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Icon(Icons.spa_outlined, size: 72, color: Theme.of(context).colorScheme.primary),
          const SizedBox(height: 12),
          Text(t.emptyTodayTitle, style: Theme.of(context).textTheme.titleLarge, textAlign: TextAlign.center),
          const SizedBox(height: 8),
          Text(t.emptyTodayBody, textAlign: TextAlign.center),
          const SizedBox(height: 24),
          FilledButton.icon(icon: const Icon(Icons.add), label: Text(t.addMedicine), onPressed: () => context.push('/catalog')),
          const SizedBox(height: 8),
          OutlinedButton.icon(icon: const Icon(Icons.auto_awesome), label: Text(t.browsePresets), onPressed: () => context.push('/presets')),
        ]),
      ),
    );
  }
}

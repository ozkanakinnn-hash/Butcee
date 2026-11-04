import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/dose_rule.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';

/// Hatırlatıcılar: genel anahtar + zaman dilimleri (saat ve açık/kapalı).
/// Dilim saati değişince o dilimi kullanan tüm takviyelerin alarmları yeniden kurulur.
class RemindersScreen extends ConsumerWidget {
  const RemindersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final s = ref.watch(settingsProvider);
    final notifier = ref.read(settingsProvider.notifier);
    final services = ref.read(servicesProvider);

    Future<void> applyAndReconcile() async {
      await services.supplements.applySlotTimes(ref.read(settingsProvider));
      await services.planner.rescheduleAll();
    }

    return Scaffold(
      appBar: AppBar(title: Text(t.remindersTitle)),
      body: ListView(children: [
        SwitchListTile(
          key: const Key('remindersMaster'),
          title: Text(t.enableReminders, style: const TextStyle(fontWeight: FontWeight.w700)),
          subtitle: Text(t.enableRemindersHint),
          value: s.remindersEnabled,
          onChanged: (v) async {
            await notifier.update((x) => x.copyWith(remindersEnabled: v));
            await services.planner.rescheduleAll();
          },
        ),
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
          child: Text(upper(t.reminderSlots, t.localeName),
              style: Theme.of(context).textTheme.labelLarge?.copyWith(letterSpacing: 1.1, fontWeight: FontWeight.w700)),
        ),
        for (final slot in slotOrder)
          ListTile(
            enabled: s.remindersEnabled,
            title: Text(slotLabel(t, slot)),
            trailing: Row(mainAxisSize: MainAxisSize.min, children: [
              TextButton(
                onPressed: !s.remindersEnabled
                    ? null
                    : () async {
                        final cur = parseHm(s.slotTime(slot));
                        final p = await showTimePicker(context: context, initialTime: TimeOfDay(hour: cur.h, minute: cur.m));
                        if (p == null) return;
                        await notifier.update((x) => x.copyWith(
                              slotTimes: {...x.slotTimes, slot: formatHm((h: p.hour, m: p.minute))},
                            ));
                        await applyAndReconcile();
                      },
                child: Text(s.slotTime(slot), style: const TextStyle(fontSize: 18)),
              ),
              Switch(
                value: !s.mutedSlots.contains(slot),
                onChanged: !s.remindersEnabled
                    ? null
                    : (on) async {
                        final muted = {...s.mutedSlots};
                        on ? muted.remove(slot) : muted.add(slot);
                        await notifier.update((x) => x.copyWith(mutedSlots: muted));
                        await services.planner.rescheduleAll();
                      },
              ),
            ]),
          ),
      ]),
    );
  }
}

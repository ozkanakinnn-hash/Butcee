import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/dose_ui_actions.dart';

/// Ertelenen, zamanı gelmiş ve son 24 saatte kaçırılan dozlar. Ana ekranın en üstünde sabit.
class PendingPanel extends ConsumerWidget {
  const PendingPanel({super.key, this.large = false});
  final bool large;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pending = ref.watch(pendingDosesProvider).value ?? const <DoseView>[];
    if (pending.isEmpty) return const SizedBox.shrink();
    final t = context.l10n;
    final cs = Theme.of(context).colorScheme;
    final textStyle = TextStyle(color: cs.onErrorContainer);

    return Card(
      key: const Key('pendingPanel'),
      color: cs.errorContainer,
      margin: const EdgeInsets.all(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          ListTile(
            leading: Icon(Icons.warning_amber_rounded, color: cs.onErrorContainer, size: large ? 36 : 24),
            title: Text(t.pendingTitle(pending.length),
                style: textStyle.copyWith(fontWeight: FontWeight.w900, fontSize: large ? 26 : 18)),
          ),
          for (final v in pending)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
              child: InkWell(
                onTap: () => context.push('/alarm/${v.dose.logId}'),
                child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                  Text(v.medicine.displayName,
                      style: textStyle.copyWith(fontWeight: FontWeight.bold, fontSize: large ? 24 : 16)),
                  Text(_subtitle(context, v), style: textStyle),
                  const SizedBox(height: 6),
                  if (large)
                    FilledButton(
                      onPressed: () => DoseUiActions.take(context, ref, v.dose.logId),
                      child: Text('✓ ${upper(t.takeNow, t.localeName)}'),
                    )
                  else
                    Align(
                      alignment: Alignment.centerRight,
                      child: FilledButton(
                        onPressed: () => DoseUiActions.take(context, ref, v.dose.logId),
                        child: Text(t.take),
                      ),
                    ),
                  const SizedBox(height: 4),
                ]),
              ),
            ),
        ]),
      ),
    );
  }

  String _subtitle(BuildContext context, DoseView v) {
    final t = context.l10n;
    final d = v.dose;
    return switch (d.status) {
      DoseStatus.snoozed => t.snoozedUntil(timeLabel(t, d.snoozedUntil!), d.snoozeCount, v.maxSnoozes),
      DoseStatus.missed => t.missedAt(timeLabel(t, d.scheduledTime)),
      _ => t.dueSince(timeLabel(t, d.scheduledTime)),
    };
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/supplement_catalog.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/visuals.dart';

class PresetsScreen extends ConsumerWidget {
  const PresetsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    return Scaffold(
      appBar: AppBar(title: Text(t.toolPresets)),
      body: ListView(padding: const EdgeInsets.fromLTRB(12, 0, 12, 32), children: [
        Card(
          color: Theme.of(context).colorScheme.surfaceContainerHighest,
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Row(children: [
              const Icon(Icons.info_outline),
              const SizedBox(width: 8),
              Expanded(child: Text(t.presetDisclaimer)),
            ]),
          ),
        ),
        for (final p in supplementPresets)
          Card(
            margin: const EdgeInsets.only(top: 12),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Row(children: [
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.primaryContainer,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Icon(presetIcon(p.id), color: Theme.of(context).colorScheme.onPrimaryContainer, size: 28),
                  ),
                  const SizedBox(width: 12),
                  Expanded(child: Text(p.name.of(t.localeName), style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800))),
                ]),
                const SizedBox(height: 6),
                Text(p.description.of(t.localeName)),
                const SizedBox(height: 10),
                for (final i in p.items)
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 2),
                    child: Row(children: [
                      SupplementIcon(catalogId: i.catalogId, size: 28),
                      const SizedBox(width: 8),
                      Expanded(child: Text(catalogById(i.catalogId)?.name.of(t.localeName) ?? i.catalogId)),
                      Text('${slotLabel(t, i.slot)} ${ref.watch(settingsProvider).slotTime(i.slot)}',
                          style: Theme.of(context).textTheme.bodySmall),
                    ]),
                  ),
                const SizedBox(height: 10),
                Align(
                  alignment: Alignment.centerRight,
                  child: FilledButton.icon(
                    key: Key('preset_${p.id}'),
                    icon: const Icon(Icons.playlist_add),
                    label: Text(t.addPreset),
                    onPressed: () async {
                      final n = await ref
                          .read(servicesProvider)
                          .supplements
                          .applyPreset(p, ref.read(settingsProvider), t.localeName);
                      if (context.mounted) showSnack(context, t.presetAdded(n));
                    },
                  ),
                ),
              ]),
            ),
          ),
      ]),
    );
  }
}

IconData presetIcon(String id) => switch (id) {
      'balanced' => Icons.balance_rounded,
      'bone' => Icons.accessibility_new_rounded,
      'immune' => Icons.health_and_safety_rounded,
      'energy' => Icons.bolt_rounded,
      _ => Icons.auto_awesome_rounded,
    };

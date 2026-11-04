import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/supplement_catalog.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../../services/localized_texts.dart';

/// Kullanıcının listesindeki tüm takviye çiftleri için bilinen etkileşimler.
class InteractionsScreen extends ConsumerWidget {
  const InteractionsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final plans = ref.watch(plansProvider).value ?? const <SupplementPlan>[];
    final conflicts = ref.watch(conflictsProvider).value ?? const <Conflict>[];
    final byCatalog = {for (final p in plans) if (p.medicine.catalogId != null) p.medicine.catalogId!: p};
    final relevant = compatRules.where((r) => byCatalog.containsKey(r.a) && byCatalog.containsKey(r.b)).toList();

    Widget tile(CompatRule r) {
      final a = byCatalog[r.a]!, b = byCatalog[r.b]!;
      final clash = conflicts.any((c) => c.rule == r);
      final (IconData icon, Color color, String status) = switch (r.type) {
        CompatType.separate when clash => (Icons.warning_amber_rounded, const Color(0xFFE65100), t.sameTimeConflict),
        CompatType.separate => (Icons.check_circle, Theme.of(context).colorScheme.primary, t.separatedOk),
        CompatType.synergy => (Icons.favorite, const Color(0xFF00897B), t.compatibleWith),
        CompatType.caution => (Icons.info, const Color(0xFF6D4C41), t.cautionLabel),
      };
      return Card(
        margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
        child: ListTile(
          leading: Icon(icon, color: color, size: 30),
          title: Text('${a.medicine.displayName}  ↔  ${b.medicine.displayName}',
              style: const TextStyle(fontWeight: FontWeight.w700)),
          subtitle: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(status, style: TextStyle(color: color, fontWeight: FontWeight.w600)),
            Text(r.reason.of(t.localeName)),
          ]),
          isThreeLine: true,
          onTap: clash ? () => context.push('/supplement/${b.medicine.id}') : null,
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(title: Text(t.interactionsTitle)),
      body: ListView(padding: const EdgeInsets.only(bottom: 32), children: [
        if (relevant.isEmpty)
          Padding(padding: const EdgeInsets.all(32), child: Center(child: Text(t.noInteractions, textAlign: TextAlign.center)))
        else ...[
          for (final r in relevant.where((r) => r.type != CompatType.synergy)) tile(r),
          if (relevant.any((r) => r.type == CompatType.synergy)) ...[
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 4),
              child: Text(upper(t.synergies, t.localeName),
                  style: Theme.of(context).textTheme.labelLarge?.copyWith(fontWeight: FontWeight.w700, letterSpacing: 1.1)),
            ),
            for (final r in relevant.where((r) => r.type == CompatType.synergy)) tile(r),
          ],
        ],
        Padding(
          padding: const EdgeInsets.all(20),
          child: Text(t.interactionsDisclaimer, style: Theme.of(context).textTheme.bodySmall),
        ),
      ]),
    );
  }
}

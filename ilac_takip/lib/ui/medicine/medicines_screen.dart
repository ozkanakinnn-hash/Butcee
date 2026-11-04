import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';

class MedicinesScreen extends ConsumerWidget {
  const MedicinesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final meds = ref.watch(medicinesProvider);
    final readOnly = ref.watch(settingsProvider).seniorMode;

    return Scaffold(
      appBar: AppBar(title: Text(t.medicines)),
      floatingActionButton: readOnly
          ? null
          : FloatingActionButton(
              tooltip: t.addMedicine,
              onPressed: () => context.push('/medicines/add'),
              child: const Icon(Icons.add),
            ),
      body: switch (meds) {
        AsyncValue(:final value?) when value.isEmpty => Center(child: Text(t.noMedicines)),
        AsyncValue(:final value?) => ListView(children: [
            for (final m in value)
              ListTile(
                leading: const Icon(Icons.medication),
                title: Text(m.displayName),
                subtitle: Text(t.stockLeft(quantityLabel(t, m.totalStock, m.form))),
                trailing: Row(mainAxisSize: MainAxisSize.min, children: [
                  if (m.isLowStock)
                    Chip(
                      label: Text(t.lowStockBadge),
                      backgroundColor: Theme.of(context).colorScheme.errorContainer,
                    ),
                  if (!readOnly)
                    IconButton(
                      tooltip: t.delete,
                      icon: const Icon(Icons.delete_outline),
                      onPressed: () async {
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
                        if (ok == true) {
                          final s = ref.read(servicesProvider);
                          await s.db.deleteMedicine(m.id!);
                          await s.planner.reconcile(); // silinen dozların alarmlarını iptal eder
                        }
                      },
                    ),
                ]),
              ),
          ]),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/models.dart';
import '../../state/providers.dart';
import 'common.dart';

/// UI'dan doz işlemleri: tek giriş noktası `DoseActionService`, geri bildirim burada.
class DoseUiActions {
  DoseUiActions._();

  static Future<void> take(BuildContext context, WidgetRef ref, String logId) async {
    HapticFeedback.heavyImpact();
    final actions = ref.read(servicesProvider).actions;
    final previous = await actions.take(logId);
    if (!context.mounted || previous == null) return;
    showUndoSnack(context, context.l10n.takenToast, onUndo: () => actions.undo(previous));
  }

  static Future<void> snooze(BuildContext context, WidgetRef ref, String logId) async {
    final ok = await ref.read(servicesProvider).actions.snooze(logId);
    if (!context.mounted) return;
    showSnack(context, ok ? context.l10n.snoozedToast : context.l10n.snoozeLimitReached);
  }

  /// Neden sorar (tek soruluk, büyük seçenekler); seçilirse atlar.
  static Future<bool> skip(BuildContext context, WidgetRef ref, String logId) async {
    final t = context.l10n;
    final reason = await showModalBottomSheet<SkipReason>(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
          child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Text(t.skipReasonTitle, style: Theme.of(ctx).textTheme.titleLarge, textAlign: TextAlign.center),
            const SizedBox(height: 12),
            for (final (r, label) in [
              (SkipReason.feelingUnwell, t.reasonFeelingUnwell),
              (SkipReason.outOfStock, t.reasonOutOfStock),
              (SkipReason.doctorAdvice, t.reasonDoctorAdvice),
              (SkipReason.other, t.reasonOther),
            ])
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: OutlinedButton(onPressed: () => Navigator.pop(ctx, r), child: Text(label)),
              ),
          ]),
        ),
      ),
    );
    if (reason == null) return false;
    final actions = ref.read(servicesProvider).actions;
    final previous = await actions.skip(logId, reason);
    if (context.mounted && previous != null) {
      showUndoSnack(context, t.skippedToast, onUndo: () => actions.undo(previous));
    }
    return previous != null;
  }
}

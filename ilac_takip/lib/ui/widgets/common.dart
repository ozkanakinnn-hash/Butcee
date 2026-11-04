import 'package:flutter/material.dart';

import '../../domain/models.dart';
import '../../l10n/app_localizations.dart';
import '../../services/localized_texts.dart';
import '../theme.dart';

extension L10nX on BuildContext {
  AppLocalizations get l10n => AppLocalizations.of(this);
}

class MealBadge extends StatelessWidget {
  const MealBadge(this.meal, {super.key, this.large = false});
  final MealRelation meal;
  final bool large;

  @override
  Widget build(BuildContext context) {
    if (meal == MealRelation.none) return const SizedBox.shrink();
    final label = upper(mealLabel(context.l10n, meal), context.l10n.localeName);
    return Semantics(
      label: label,
      excludeSemantics: true,
      child: Container(
        width: large ? double.infinity : null,
        padding: EdgeInsets.symmetric(vertical: large ? 14 : 4, horizontal: large ? 16 : 8),
        decoration: BoxDecoration(color: mealColor(meal), borderRadius: BorderRadius.circular(large ? 16 : 8)),
        child: Text(
          '${mealIcon(meal)}  $label',
          textAlign: TextAlign.center,
          style: TextStyle(color: Colors.white, fontSize: large ? 26 : 13, fontWeight: FontWeight.w900),
        ),
      ),
    );
  }
}

/// Onay diyaloğu yerine 10 sn "Geri al" çubuğu.
void showUndoSnack(BuildContext context, String message, {VoidCallback? onUndo}) {
  final m = ScaffoldMessenger.of(context);
  m.hideCurrentSnackBar();
  m.showSnackBar(SnackBar(
    content: Text(message),
    duration: const Duration(seconds: 10),
    action: onUndo == null ? null : SnackBarAction(label: context.l10n.undo, onPressed: onUndo),
  ));
}

void showSnack(BuildContext context, String message) {
  ScaffoldMessenger.of(context)
    ..hideCurrentSnackBar()
    ..showSnackBar(SnackBar(content: Text(message)));
}

String statusLabel(AppLocalizations t, DoseStatus s) => switch (s) {
      DoseStatus.pending => t.statusPending,
      DoseStatus.snoozed => t.statusSnoozed,
      DoseStatus.taken => t.statusTaken,
      DoseStatus.skipped => t.statusSkipped,
      DoseStatus.missed => t.statusMissed,
    };


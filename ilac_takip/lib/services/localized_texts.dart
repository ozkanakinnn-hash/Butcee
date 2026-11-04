import 'package:intl/intl.dart';

import '../domain/models.dart';
import '../l10n/app_localizations.dart';
import 'alarm_planner.dart';
import 'local_notification_scheduler.dart';

String mealLabel(AppLocalizations t, MealRelation m) => switch (m) {
      MealRelation.none => t.mealNone,
      MealRelation.afterMeal => t.afterMeal,
      MealRelation.beforeMeal => t.beforeMeal,
      MealRelation.withMeal => t.withMeal,
      MealRelation.emptyStomach => t.emptyStomach,
    };

String formLabel(AppLocalizations t, String form) => switch (form) {
      'tablet' => t.formTablet,
      'capsule' => t.formCapsule,
      'syrup' => t.formSyrup,
      'drops' => t.formDrops,
      'injection' => t.formInjection,
      _ => t.formOther,
    };

String formatQty(double q) => q == q.roundToDouble() ? q.toInt().toString() : q.toString();

String quantityLabel(AppLocalizations t, double qty, String form) =>
    t.doseQuantity(formatQty(qty), formLabel(t, form).toLowerCase());

String timeLabel(AppLocalizations t, DateTime d) => DateFormat.Hm(t.localeName).format(d);

class LocalizedDoseTexts implements DoseTextBuilder {
  LocalizedDoseTexts(this.t);
  final AppLocalizations t;

  @override
  String title(Medicine m, DoseLog d) => t.notifTitle(m.displayName);

  @override
  String repeatTitle(Medicine m, DoseLog d) => t.notifRepeatTitle(m.displayName);

  @override
  String body(Medicine m, DoseLog d) {
    final parts = [
      timeLabel(t, d.scheduledTime),
      quantityLabel(t, d.quantity, m.form),
      if (d.meal != MealRelation.none) mealLabel(t, d.meal).toUpperCase(),
    ];
    return parts.join(' · ');
  }
}

NotificationLabels notificationLabels(AppLocalizations t) => NotificationLabels(
      take: t.take,
      snooze: t.snooze10,
      skip: t.skip,
      criticalChannel: t.channelCritical,
      criticalChannelDesc: t.channelCriticalDesc,
      normalChannel: t.channelNormal,
      infoChannel: t.channelInfo,
    );

({String title, String body}) Function(Medicine) lowStockTexts(AppLocalizations t) =>
    (m) => (title: t.lowStockTitle, body: t.lowStockBody(m.displayName, formatQty(m.totalStock)));

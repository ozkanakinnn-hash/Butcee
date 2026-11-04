import 'package:intl/date_symbol_data_local.dart';
import 'package:intl/intl.dart';

import '../domain/dose_rule.dart';
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
      if (d.meal != MealRelation.none) upper(mealLabel(t, d.meal), t.localeName),
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

// ───────────── Takviye görünüm yardımcıları ─────────────

const slotOrder = ['earlyMorning', 'lateMorning', 'noon', 'afternoon', 'evening', 'lateEvening'];

String slotLabel(AppLocalizations t, String slot) => switch (slot) {
      'earlyMorning' => t.slotEarlyMorning,
      'lateMorning' => t.slotLateMorning,
      'noon' => t.slotNoon,
      'afternoon' => t.slotAfternoon,
      'evening' => t.slotEvening,
      'lateEvening' => t.slotLateEvening,
      _ => slot,
    };

/// Dilimi olmayan dozlar için saat grubuna göre başlık.
String hourGroupLabel(AppLocalizations t, int hour) {
  if (hour < 12) return t.groupMorning;
  if (hour < 17) return t.groupAfternoon;
  if (hour < 21) return t.groupEvening;
  return t.groupNight;
}

String categoryLabel(AppLocalizations t, ItemCategory c) => switch (c) {
      ItemCategory.vitamin => t.catVitamin,
      ItemCategory.mineral => t.catMineral,
      ItemCategory.fattyAcid => t.catFattyAcid,
      ItemCategory.herbal => t.catHerbal,
      ItemCategory.probiotic => t.catProbiotic,
      ItemCategory.protein => t.catProtein,
      ItemCategory.multi => t.catMulti,
      ItemCategory.medicine => t.catMedicine,
      ItemCategory.other => t.catOther,
    };

String categoryEmoji(ItemCategory c) => switch (c) {
      ItemCategory.vitamin => '💊',
      ItemCategory.mineral => '🪨',
      ItemCategory.fattyAcid => '🐟',
      ItemCategory.herbal => '🌿',
      ItemCategory.probiotic => '🦠',
      ItemCategory.protein => '✨',
      ItemCategory.multi => '🌈',
      ItemCategory.medicine => '💊',
      ItemCategory.other => '🔹',
    };

const supplementUnits = ['mg', 'mcg', 'IU', 'g', 'ml', 'tablet', 'capsule', 'drop', 'scoop'];

String unitLabel(AppLocalizations t, String unit) => switch (unit) {
      'tablet' => t.unitTablet,
      'capsule' => t.unitCapsule,
      'drop' => t.unitDrop,
      'scoop' => t.unitScoop,
      _ => unit,
    };

/// "1000 IU", "2 × 500 mg", "1 tablet".
String supplementDoseLabel(AppLocalizations t, Medicine m, double quantity) {
  if (m.doseAmount == null || m.doseUnit == null) return quantityLabel(t, quantity, m.form);
  final amount = '${formatQty(m.doseAmount!)} ${unitLabel(t, m.doseUnit!)}';
  return quantity == 1 ? amount : '${formatQty(quantity)} × $amount';
}

String ruleLabel(AppLocalizations t, DoseRule? r) {
  final wd = DateFormat.E(t.localeName);
  String days(Set<int> d) => (d.toList()..sort()).map((x) => wd.format(DateTime(2024, 1, x))).join(' · ');
  return switch (r) {
    null => '',
    AsNeededRule() => t.asNeededShort,
    IntervalRule(:final intervalHours) => t.everyNHoursShort(intervalHours),
    CyclicRule(:final onDays, :final offDays) => t.cycleLabel(onDays, offDays),
    FixedTimesRule(:final weekdays, :final everyNDays, :final times) => [
        if (weekdays != null && weekdays.length == 2 && weekdays.containsAll([6, 7]))
          t.freqWeekends
        else if (weekdays != null)
          days(weekdays)
        else
          t.everyNDaysLabel(everyNDays),
        t.dosesPerDay(times.length),
      ].join(' · '),
  };
}

/// Dile duyarlı büyük harf: Dart'ın `toUpperCase()`'i Türkçe "i"yi "I" yapar ("BIRLIKTE");
/// Türkçede doğrusu "İ" ("BİRLİKTE").
String upper(String s, String localeName) =>
    localeName.startsWith('tr') ? s.replaceAll('i', 'İ').toUpperCase() : s.toUpperCase();

/// Tarih/saat biçimleri için yerel veriyi yükler. MaterialApp bunu kendi yükler, ancak açılışta
/// `runApp`'ten ÖNCE ve bildirim aksiyonlarının arka plan izolatında `DateFormat.Hm('tr')`
/// çağrısı `LocaleDataException` fırlatır — bu yüzden servisler kurulurken çağrılmalıdır.
Future<void> initIntl() => initializeDateFormatting();

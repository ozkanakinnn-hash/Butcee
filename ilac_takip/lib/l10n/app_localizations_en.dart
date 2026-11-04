// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Medication Tracker';

  @override
  String get today => 'Today';

  @override
  String get nothingToday => 'No medication scheduled today.';

  @override
  String pendingTitle(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count doses waiting',
      one: '1 dose waiting',
    );
    return '$_temp0';
  }

  @override
  String snoozedUntil(String time, int count, int max) {
    return '⏸ Snoozed until $time ($count/$max)';
  }

  @override
  String missedAt(String time) {
    return '✖ Missed at $time';
  }

  @override
  String dueSince(String time) {
    return '⏰ Due since $time';
  }

  @override
  String get take => 'Taken';

  @override
  String get takeBig => 'I TOOK IT';

  @override
  String get takeNow => 'Take now';

  @override
  String get snooze10 => 'In 10 min';

  @override
  String get skip => 'Skip';

  @override
  String get wontTake => 'Won\'t take';

  @override
  String get undo => 'Undo';

  @override
  String get takenToast => 'Saved: taken ✓';

  @override
  String get snoozedToast => 'I\'ll remind you again in 10 minutes';

  @override
  String get skippedToast => 'Dose recorded as skipped';

  @override
  String get snoozeLimitReached => 'No snoozes left';

  @override
  String get addMedicine => 'Add medicine';

  @override
  String get scanBox => 'Scan the box';

  @override
  String get addManually => 'Add manually';

  @override
  String get medicines => 'My medicines';

  @override
  String get settings => 'Settings';

  @override
  String get noMedicines => 'You haven\'t added any medicines yet.';

  @override
  String stockLeft(String amount) {
    return 'Stock: $amount';
  }

  @override
  String get lowStockBadge => 'Low stock';

  @override
  String get delete => 'Delete';

  @override
  String deleteMedicineConfirm(String name) {
    return 'Delete $name and all its schedules?';
  }

  @override
  String get cancel => 'Cancel';

  @override
  String get save => 'Save';

  @override
  String get ok => 'OK';

  @override
  String get mealNone => 'Any time';

  @override
  String get afterMeal => 'After meal';

  @override
  String get beforeMeal => 'Before meal';

  @override
  String get withMeal => 'With meal';

  @override
  String get emptyStomach => 'On an empty stomach';

  @override
  String get statusPending => 'Pending';

  @override
  String get statusSnoozed => 'Snoozed';

  @override
  String get statusTaken => 'Taken';

  @override
  String get statusSkipped => 'Skipped';

  @override
  String get statusMissed => 'Missed';

  @override
  String get medicineName => 'Medicine name';

  @override
  String get nickname => 'Nickname (optional)';

  @override
  String get nicknameHint => 'E.g. Small white pill — heart';

  @override
  String get form => 'Form';

  @override
  String get formTablet => 'Tablet';

  @override
  String get formCapsule => 'Capsule';

  @override
  String get formSyrup => 'Syrup';

  @override
  String get formDrops => 'Drops';

  @override
  String get formInjection => 'Injection';

  @override
  String get formOther => 'Other';

  @override
  String get stock => 'Current stock';

  @override
  String get criticalStock => 'Low-stock warning level';

  @override
  String get packageSize => 'Package size';

  @override
  String get criticalMedicine => 'Critical medicine';

  @override
  String get criticalMedicineHint =>
      'Rings with alarm volume even in silent mode and opens on the lock screen';

  @override
  String get persistentAlarm => 'Persistent alarm';

  @override
  String get persistentAlarmHint =>
      'Repeats every 3 minutes until you tap \"Taken\"';

  @override
  String get frequency => 'How often?';

  @override
  String get freqFixedTimes => 'At set times';

  @override
  String get freqInterval => 'Every N hours';

  @override
  String get freqAsNeeded => 'As needed';

  @override
  String get doseTimes => 'Dose times';

  @override
  String get addTime => 'Add time';

  @override
  String get everyDay => 'Every day';

  @override
  String everyNDaysLabel(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: 'Every $n days',
      two: 'Every other day',
      one: 'Every day',
    );
    return '$_temp0';
  }

  @override
  String get onlyTheseDays => 'Only on these days';

  @override
  String intervalEvery(int n) {
    return 'Every $n hours';
  }

  @override
  String get firstDose => 'First dose time';

  @override
  String get quantityPerDose => 'Amount per dose';

  @override
  String get mealRelation => 'Meal timing';

  @override
  String get maxPerDay => 'Max per day';

  @override
  String get requiredField => 'This field is required';

  @override
  String get atLeastOneTime => 'Add at least one time';

  @override
  String get saved => 'Saved';

  @override
  String get scanTitle => 'Scan medicine';

  @override
  String get scanHint =>
      'Place the box\'s DataMatrix code or barcode inside the frame';

  @override
  String get scanManualEntry => 'Enter barcode manually';

  @override
  String get barcodeDigits => '13-digit barcode';

  @override
  String get duplicatePack => 'This box has already been added';

  @override
  String addStockQuestion(String name, String qty) {
    return '$name is already in your list. Add $qty to stock?';
  }

  @override
  String get addStockDone => 'Stock updated';

  @override
  String get notFoundInCatalog =>
      'We don\'t recognise this medicine yet. You can enter the details manually.';

  @override
  String expiryLabel(String date) {
    return 'Expires: $date';
  }

  @override
  String get invalidCode => 'Could not read the code';

  @override
  String doseQuantity(String qty, String form) {
    return '$qty $form';
  }

  @override
  String get skipReasonTitle => 'Why won\'t you take it?';

  @override
  String get reasonFeelingUnwell => 'I feel unwell';

  @override
  String get reasonOutOfStock => 'I ran out';

  @override
  String get reasonDoctorAdvice => 'My doctor stopped it';

  @override
  String get reasonOther => 'Other';

  @override
  String notifTitle(String name) {
    return '💊 Time for $name';
  }

  @override
  String notifRepeatTitle(String name) {
    return '⏰ Still waiting: $name';
  }

  @override
  String get lowStockTitle => '📦 Running low';

  @override
  String lowStockBody(String name, String amount) {
    return '$amount of $name left. Time to plan a refill.';
  }

  @override
  String get channelCritical => 'Critical medication alarms';

  @override
  String get channelCriticalDesc => 'Alarms that ring until you confirm';

  @override
  String get channelNormal => 'Medication reminders';

  @override
  String get channelInfo => 'Information';

  @override
  String get testAlarmTitle => '🔔 Test alarm';

  @override
  String get testAlarmBody => 'If you can see this, your alarms work.';

  @override
  String get language => 'Language';

  @override
  String get languageSystem => 'System language';

  @override
  String get seniorMode => 'Simple Mode';

  @override
  String get seniorModeHint => 'Large text, high contrast, single screen';

  @override
  String get textScale => 'Text size';

  @override
  String get ttsOnAlarm => 'Read alarms aloud';

  @override
  String get caregiverPhone => 'Caregiver\'s phone';

  @override
  String get seniorPin => 'Settings lock PIN (4 digits)';

  @override
  String get seniorPinHint => 'Required to leave Simple Mode';

  @override
  String get enterPin => 'Enter PIN';

  @override
  String get wrongPin => 'Wrong PIN';

  @override
  String get reliability => 'Alarm reliability';

  @override
  String get reliabilityIntro =>
      'For alarms to always ring, every item below should be green.';

  @override
  String get relNotifications => 'Notification permission';

  @override
  String get relExactAlarms => 'Exact alarm permission';

  @override
  String get relBatteryOpt => 'Battery restriction removed';

  @override
  String get relFullScreen => 'Full-screen alarm on lock screen';

  @override
  String get relOemAutostart => 'Manufacturer setting: autostart / background';

  @override
  String relOemHint(String brand) {
    return '$brand phones may aggressively close apps. Allow this app on the screen that opens.';
  }

  @override
  String get fix => 'Fix';

  @override
  String get open => 'Open';

  @override
  String get testAlarm => 'Set a test alarm (in 1 min)';

  @override
  String get testAlarmScheduled =>
      'The test alarm will ring in 1 minute. Close the app and lock the screen.';

  @override
  String get goodMorning => 'Good morning';

  @override
  String get goodAfternoon => 'Good afternoon';

  @override
  String get goodEvening => 'Good evening';

  @override
  String get callCaregiver => '📞 CALL MY CAREGIVER';

  @override
  String get nextDoses => 'NEXT';

  @override
  String todayProgress(int taken, int total) {
    return 'Today: $taken of $total taken';
  }

  @override
  String get allDoneToday => 'You\'ve taken all of today\'s medicines 👏';

  @override
  String get longPressForSettings => 'Long-press the title for settings';

  @override
  String ttsDose(String time, String name, String meal, String qty) {
    return 'Your $time medicine: $name. $meal. $qty.';
  }

  @override
  String get alarmNotFound => 'Dose not found';
}

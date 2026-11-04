// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get appTitle => 'Vitamin Tracker';

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

  @override
  String get navToday => 'Today';

  @override
  String get navSupplements => 'Supplements';

  @override
  String get navPanel => 'Panel';

  @override
  String get navStats => 'Stats';

  @override
  String get navLearn => 'Learn';

  @override
  String get slotEarlyMorning => 'Early morning';

  @override
  String get slotLateMorning => 'Late morning';

  @override
  String get slotNoon => 'Midday';

  @override
  String get slotAfternoon => 'Afternoon';

  @override
  String get slotEvening => 'Evening';

  @override
  String get slotLateEvening => 'Late evening';

  @override
  String get groupMorning => 'Morning';

  @override
  String get groupAfternoon => 'Afternoon';

  @override
  String get groupEvening => 'Evening';

  @override
  String get groupNight => 'Night';

  @override
  String get takenLabel => 'taken';

  @override
  String streakDays(int n) {
    return '$n days';
  }

  @override
  String interactionsFound(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count interaction warnings',
      one: '1 interaction warning',
    );
    return '$_temp0';
  }

  @override
  String get addAnotherDose => 'Log another dose';

  @override
  String get extraDoseLogged => 'Extra dose logged';

  @override
  String get moodQuestion => 'How do you feel today?';

  @override
  String get emptyTodayTitle => 'You haven\'t added any supplements yet';

  @override
  String get emptyTodayBody =>
      'Add a supplement or start with a ready-made set.';

  @override
  String get mySupplements => 'My supplements';

  @override
  String supplementsCount(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n supplements',
      one: '1 supplement',
    );
    return '$_temp0';
  }

  @override
  String get browsePresets => 'Browse ready-made sets';

  @override
  String get shareProgram => 'Share program as PDF';

  @override
  String get paused => 'Paused';

  @override
  String get searchSupplements => 'Search supplements';

  @override
  String get notInList => 'Not listed? Add it manually';

  @override
  String get catAll => 'All';

  @override
  String get catVitamin => 'Vitamin';

  @override
  String get catMineral => 'Mineral';

  @override
  String get catFattyAcid => 'Fatty acid';

  @override
  String get catHerbal => 'Herbal';

  @override
  String get catProbiotic => 'Probiotic';

  @override
  String get catProtein => 'Protein';

  @override
  String get catMulti => 'Multivitamin';

  @override
  String get catMedicine => 'Medicine';

  @override
  String get catOther => 'Other';

  @override
  String get categoryLabel => 'Category';

  @override
  String get amountLabel => 'Dose (as on label)';

  @override
  String get unitLabel => 'Unit';

  @override
  String get labelDoseHint =>
      'Enter the dose on the product label; the app does not recommend doses.';

  @override
  String get howOften => 'How often?';

  @override
  String get freqDaily => 'Every day';

  @override
  String get freqEveryOtherDay => 'Every other day';

  @override
  String get freqMwf => 'Mon · Wed · Fri';

  @override
  String get freqWeekends => 'Weekends only';

  @override
  String get freqEveryNDays => 'Every N days';

  @override
  String get freqSpecificDays => 'Specific days';

  @override
  String get freqCycle => 'On / off cycle';

  @override
  String get whenToTake => 'When?';

  @override
  String get nextDays14 => 'Next 14 days';

  @override
  String get cycleOnDays => 'On (days)';

  @override
  String get cycleOffDays => 'Off (days)';

  @override
  String get nDaysLabel => 'Every how many days?';

  @override
  String get advanced => 'Advanced';

  @override
  String get pickAtLeastOneSlot => 'Pick at least one time';

  @override
  String conflictWarning(String other, String reason) {
    return 'Close in time to $other: $reason';
  }

  @override
  String ulWarning(String total, String ul) {
    return 'Daily total $total, upper limit (UL) $ul. Talk to your doctor.';
  }

  @override
  String get dosePlan => 'Dose and plan';

  @override
  String get referenceValues => 'Reference values';

  @override
  String rdaLine(String kind, String male, String female) {
    return '$kind: Men $male · Women $female';
  }

  @override
  String ulLine(String ul) {
    return 'Upper limit (UL): $ul';
  }

  @override
  String get noUl => 'No established upper limit';

  @override
  String get noReference => 'No official reference value';

  @override
  String get sourceNih =>
      'Source: NIH ODS, adults 19–50. Values differ in pregnancy, breastfeeding and illness. For information only.';

  @override
  String get compatibleWith => 'Works well with';

  @override
  String get takeSeparately => 'Take separately';

  @override
  String get cautionLabel => 'Caution';

  @override
  String get inYourList => 'in your list';

  @override
  String get pause => 'Pause';

  @override
  String get resume => 'Resume';

  @override
  String dosesPerDay(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n times a day',
      one: 'once a day',
    );
    return '$_temp0';
  }

  @override
  String get panelSubtitle => 'All tools in one place';

  @override
  String get toolScan => 'Scan code / barcode';

  @override
  String get toolCatalog => 'Find a supplement';

  @override
  String get toolInteractions => 'Interaction check';

  @override
  String get toolPresets => 'Ready-made sets';

  @override
  String get toolProgramPdf => 'Program PDF';

  @override
  String get toolDoctorReport => 'Doctor report';

  @override
  String get toolNearbyPharmacy => 'Nearby pharmacies';

  @override
  String get toolDutyPharmacy => 'On-duty pharmacies';

  @override
  String get toolSideEffect => 'Report a side effect';

  @override
  String get toolReminders => 'Reminders';

  @override
  String get sideEffectBody =>
      'If you notice an unexpected effect after a medicine or supplement, stop taking it and talk to your doctor or pharmacist. In Türkiye, medicine side effects can be reported to the national pharmacovigilance centre (TÜFAM) of TİTCK.';

  @override
  String get openTitck => 'Open TİTCK website';

  @override
  String get interactionsTitle => 'Interaction check';

  @override
  String get sameTimeConflict => 'Scheduled close together — separate them';

  @override
  String get separatedOk => 'At different times ✓';

  @override
  String get synergies => 'Good combinations';

  @override
  String get noInteractions => 'No known interactions in your list.';

  @override
  String get interactionsDisclaimer =>
      'This list covers common supplement interactions. Ask your pharmacist about interactions with your medicines.';

  @override
  String get presetDisclaimer =>
      'These are examples, not recommendations. Ask your doctor or pharmacist about doses and needs.';

  @override
  String get addPreset => 'Add set';

  @override
  String presetAdded(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n supplements added',
      one: '1 supplement added',
      zero: 'All supplements in this set are already in your list',
    );
    return '$_temp0';
  }

  @override
  String get tabBasics => 'Basics';

  @override
  String get tabSymptoms => 'Symptoms';

  @override
  String get tabAbsorption => 'Absorption';

  @override
  String get basicsSubtitle =>
      'A short guide to daily targets and percentages.';

  @override
  String get symptomsSubtitle =>
      'Select to see related nutrients. This is not a medical diagnosis.';

  @override
  String get relatedNutrients => 'Related nutrients';

  @override
  String get selectedSummary => 'Nutrients you could discuss with your doctor';

  @override
  String get seeDoctorBanner =>
      'If symptoms persist, see your doctor. Don\'t start supplements without a blood test.';

  @override
  String get absorptionSubtitle =>
      'Practical examples that may increase or decrease absorption.';

  @override
  String get increases => 'Increases';

  @override
  String get decreases => 'Decreases';

  @override
  String get tipLabel => 'Tip';

  @override
  String get statsTitle => 'Statistics';

  @override
  String get balanceScore => 'Balance score';

  @override
  String get outOf100 => 'out of 100';

  @override
  String get adherencePart => 'Adherence';

  @override
  String get selfReportPart => 'Self-report';

  @override
  String get streakTile => 'Day streak';

  @override
  String get streakSub => 'full days in a row';

  @override
  String get thisWeek => 'This week';

  @override
  String get thisMonth => 'This month';

  @override
  String get takenRate => 'doses taken';

  @override
  String get last14Days => 'Last 14 days';

  @override
  String get perSupplement => 'By supplement (30 days)';

  @override
  String get moodTitle => 'Mood';

  @override
  String get noDataYet => 'Not enough data yet';

  @override
  String get reportTitle => 'Supplement Report';

  @override
  String get programTitle => 'Supplement Program';

  @override
  String generatedAt(String date) {
    return 'Generated on $date';
  }

  @override
  String get adherence30 => '30-day adherence';

  @override
  String plannedTaken(int taken, int planned) {
    return '$taken of $planned planned doses taken';
  }

  @override
  String get currentProgram => 'Current program';

  @override
  String get colSupplement => 'Supplement';

  @override
  String get colPlan => 'Plan';

  @override
  String get colDose => 'Dose';

  @override
  String get colAdherence => 'Adherence';

  @override
  String get pdfDisclaimer =>
      'This document is based on the user\'s own records and is not medical advice or a diagnosis.';

  @override
  String get remindersTitle => 'Reminders';

  @override
  String get enableReminders => 'Enable reminders';

  @override
  String get enableRemindersHint =>
      'Get notified when it\'s time for a supplement.';

  @override
  String get reminderSlots => 'Time slots';

  @override
  String get productInfo => 'Product info';

  @override
  String get barcodeLabel => 'Barcode';

  @override
  String get serialLabel => 'Serial no';

  @override
  String get lotLabel => 'Batch no';

  @override
  String get addToMySupplements => 'Add to my supplements';

  @override
  String get findPharmacy => 'Find a pharmacy';

  @override
  String get readLeaflet => 'Patient leaflet';

  @override
  String get crashTitle => 'An error occurred in the previous session';

  @override
  String get crashBody =>
      'You can copy the report and send it to the developer so we can fix it.';

  @override
  String get copyReport => 'Copy report';

  @override
  String get dismiss => 'Close';

  @override
  String get copied => 'Copied';

  @override
  String get startupError => 'The app could not start';

  @override
  String get exactAlarmOff =>
      'Exact alarm permission is off — reminders may be a few minutes late.';

  @override
  String get unitTablet => 'tablet';

  @override
  String get unitCapsule => 'capsule';

  @override
  String get unitDrop => 'drop';

  @override
  String get unitScoop => 'scoop';

  @override
  String get asNeededShort => 'As needed';

  @override
  String cycleLabel(int on, int off) {
    return '$on days on, $off days off';
  }

  @override
  String everyNHoursShort(int n) {
    return 'Every $n h';
  }

  @override
  String get editSupplement => 'Edit';

  @override
  String get details => 'Details';
}

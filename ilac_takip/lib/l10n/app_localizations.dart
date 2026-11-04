import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_tr.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations)!;
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('tr'),
  ];

  /// No description provided for @appTitle.
  ///
  /// In tr, this message translates to:
  /// **'Vitamin Takip'**
  String get appTitle;

  /// No description provided for @today.
  ///
  /// In tr, this message translates to:
  /// **'Bugün'**
  String get today;

  /// No description provided for @nothingToday.
  ///
  /// In tr, this message translates to:
  /// **'Bugün planlanmış ilaç yok.'**
  String get nothingToday;

  /// No description provided for @pendingTitle.
  ///
  /// In tr, this message translates to:
  /// **'{count, plural, =1{1 ilaç bekliyor} other{{count} ilaç bekliyor}}'**
  String pendingTitle(int count);

  /// No description provided for @snoozedUntil.
  ///
  /// In tr, this message translates to:
  /// **'⏸ {time} saatine ertelendi ({count}/{max})'**
  String snoozedUntil(String time, int count, int max);

  /// No description provided for @missedAt.
  ///
  /// In tr, this message translates to:
  /// **'✖ {time} kaçırıldı'**
  String missedAt(String time);

  /// No description provided for @dueSince.
  ///
  /// In tr, this message translates to:
  /// **'⏰ {time}\'dan beri bekliyor'**
  String dueSince(String time);

  /// No description provided for @take.
  ///
  /// In tr, this message translates to:
  /// **'Aldım'**
  String get take;

  /// No description provided for @takeBig.
  ///
  /// In tr, this message translates to:
  /// **'ALDIM'**
  String get takeBig;

  /// No description provided for @takeNow.
  ///
  /// In tr, this message translates to:
  /// **'Şimdi aldım'**
  String get takeNow;

  /// No description provided for @snooze10.
  ///
  /// In tr, this message translates to:
  /// **'10 dk sonra'**
  String get snooze10;

  /// No description provided for @skip.
  ///
  /// In tr, this message translates to:
  /// **'Atla'**
  String get skip;

  /// No description provided for @wontTake.
  ///
  /// In tr, this message translates to:
  /// **'Almayacağım'**
  String get wontTake;

  /// No description provided for @undo.
  ///
  /// In tr, this message translates to:
  /// **'Geri al'**
  String get undo;

  /// No description provided for @takenToast.
  ///
  /// In tr, this message translates to:
  /// **'Kaydedildi: alındı ✓'**
  String get takenToast;

  /// No description provided for @snoozedToast.
  ///
  /// In tr, this message translates to:
  /// **'10 dakika sonra tekrar hatırlatacağım'**
  String get snoozedToast;

  /// No description provided for @skippedToast.
  ///
  /// In tr, this message translates to:
  /// **'Doz atlandı olarak kaydedildi'**
  String get skippedToast;

  /// No description provided for @snoozeLimitReached.
  ///
  /// In tr, this message translates to:
  /// **'Erteleme hakkı doldu'**
  String get snoozeLimitReached;

  /// No description provided for @addMedicine.
  ///
  /// In tr, this message translates to:
  /// **'İlaç ekle'**
  String get addMedicine;

  /// No description provided for @scanBox.
  ///
  /// In tr, this message translates to:
  /// **'Kutuyu tara'**
  String get scanBox;

  /// No description provided for @addManually.
  ///
  /// In tr, this message translates to:
  /// **'Elle ekle'**
  String get addManually;

  /// No description provided for @medicines.
  ///
  /// In tr, this message translates to:
  /// **'İlaçlarım'**
  String get medicines;

  /// No description provided for @settings.
  ///
  /// In tr, this message translates to:
  /// **'Ayarlar'**
  String get settings;

  /// No description provided for @noMedicines.
  ///
  /// In tr, this message translates to:
  /// **'Henüz ilaç eklemediniz.'**
  String get noMedicines;

  /// No description provided for @stockLeft.
  ///
  /// In tr, this message translates to:
  /// **'Stok: {amount}'**
  String stockLeft(String amount);

  /// No description provided for @lowStockBadge.
  ///
  /// In tr, this message translates to:
  /// **'Stok azaldı'**
  String get lowStockBadge;

  /// No description provided for @delete.
  ///
  /// In tr, this message translates to:
  /// **'Sil'**
  String get delete;

  /// No description provided for @deleteMedicineConfirm.
  ///
  /// In tr, this message translates to:
  /// **'{name} ve tüm planları silinsin mi?'**
  String deleteMedicineConfirm(String name);

  /// No description provided for @cancel.
  ///
  /// In tr, this message translates to:
  /// **'Vazgeç'**
  String get cancel;

  /// No description provided for @save.
  ///
  /// In tr, this message translates to:
  /// **'Kaydet'**
  String get save;

  /// No description provided for @ok.
  ///
  /// In tr, this message translates to:
  /// **'Tamam'**
  String get ok;

  /// No description provided for @mealNone.
  ///
  /// In tr, this message translates to:
  /// **'Fark etmez'**
  String get mealNone;

  /// No description provided for @afterMeal.
  ///
  /// In tr, this message translates to:
  /// **'Tok karnına'**
  String get afterMeal;

  /// No description provided for @beforeMeal.
  ///
  /// In tr, this message translates to:
  /// **'Yemekten önce'**
  String get beforeMeal;

  /// No description provided for @withMeal.
  ///
  /// In tr, this message translates to:
  /// **'Yemekle birlikte'**
  String get withMeal;

  /// No description provided for @emptyStomach.
  ///
  /// In tr, this message translates to:
  /// **'Aç karnına'**
  String get emptyStomach;

  /// No description provided for @statusPending.
  ///
  /// In tr, this message translates to:
  /// **'Bekliyor'**
  String get statusPending;

  /// No description provided for @statusSnoozed.
  ///
  /// In tr, this message translates to:
  /// **'Ertelendi'**
  String get statusSnoozed;

  /// No description provided for @statusTaken.
  ///
  /// In tr, this message translates to:
  /// **'Alındı'**
  String get statusTaken;

  /// No description provided for @statusSkipped.
  ///
  /// In tr, this message translates to:
  /// **'Atlandı'**
  String get statusSkipped;

  /// No description provided for @statusMissed.
  ///
  /// In tr, this message translates to:
  /// **'Kaçırıldı'**
  String get statusMissed;

  /// No description provided for @medicineName.
  ///
  /// In tr, this message translates to:
  /// **'İlaç adı'**
  String get medicineName;

  /// No description provided for @nickname.
  ///
  /// In tr, this message translates to:
  /// **'Takma ad (isteğe bağlı)'**
  String get nickname;

  /// No description provided for @nicknameHint.
  ///
  /// In tr, this message translates to:
  /// **'Ör. Beyaz küçük hap — kalp ilacı'**
  String get nicknameHint;

  /// No description provided for @form.
  ///
  /// In tr, this message translates to:
  /// **'Form'**
  String get form;

  /// No description provided for @formTablet.
  ///
  /// In tr, this message translates to:
  /// **'Tablet'**
  String get formTablet;

  /// No description provided for @formCapsule.
  ///
  /// In tr, this message translates to:
  /// **'Kapsül'**
  String get formCapsule;

  /// No description provided for @formSyrup.
  ///
  /// In tr, this message translates to:
  /// **'Şurup'**
  String get formSyrup;

  /// No description provided for @formDrops.
  ///
  /// In tr, this message translates to:
  /// **'Damla'**
  String get formDrops;

  /// No description provided for @formInjection.
  ///
  /// In tr, this message translates to:
  /// **'İğne'**
  String get formInjection;

  /// No description provided for @formOther.
  ///
  /// In tr, this message translates to:
  /// **'Diğer'**
  String get formOther;

  /// No description provided for @stock.
  ///
  /// In tr, this message translates to:
  /// **'Mevcut stok'**
  String get stock;

  /// No description provided for @criticalStock.
  ///
  /// In tr, this message translates to:
  /// **'Uyarı verilecek stok'**
  String get criticalStock;

  /// No description provided for @packageSize.
  ///
  /// In tr, this message translates to:
  /// **'Kutu içeriği'**
  String get packageSize;

  /// No description provided for @criticalMedicine.
  ///
  /// In tr, this message translates to:
  /// **'Kritik ilaç'**
  String get criticalMedicine;

  /// No description provided for @criticalMedicineHint.
  ///
  /// In tr, this message translates to:
  /// **'Sessiz modda da alarm sesiyle çalar, kilit ekranında açılır'**
  String get criticalMedicineHint;

  /// No description provided for @persistentAlarm.
  ///
  /// In tr, this message translates to:
  /// **'Israrcı alarm'**
  String get persistentAlarm;

  /// No description provided for @persistentAlarmHint.
  ///
  /// In tr, this message translates to:
  /// **'\"Aldım\" diyene kadar 3 dakikada bir tekrar çalar'**
  String get persistentAlarmHint;

  /// No description provided for @frequency.
  ///
  /// In tr, this message translates to:
  /// **'Ne sıklıkla?'**
  String get frequency;

  /// No description provided for @freqFixedTimes.
  ///
  /// In tr, this message translates to:
  /// **'Belirli saatlerde'**
  String get freqFixedTimes;

  /// No description provided for @freqInterval.
  ///
  /// In tr, this message translates to:
  /// **'Saat aralığıyla'**
  String get freqInterval;

  /// No description provided for @freqAsNeeded.
  ///
  /// In tr, this message translates to:
  /// **'Gerektiğinde'**
  String get freqAsNeeded;

  /// No description provided for @doseTimes.
  ///
  /// In tr, this message translates to:
  /// **'Doz saatleri'**
  String get doseTimes;

  /// No description provided for @addTime.
  ///
  /// In tr, this message translates to:
  /// **'Saat ekle'**
  String get addTime;

  /// No description provided for @everyDay.
  ///
  /// In tr, this message translates to:
  /// **'Her gün'**
  String get everyDay;

  /// No description provided for @everyNDaysLabel.
  ///
  /// In tr, this message translates to:
  /// **'{n, plural, =1{Her gün} =2{Gün aşırı} other{{n} günde bir}}'**
  String everyNDaysLabel(int n);

  /// No description provided for @onlyTheseDays.
  ///
  /// In tr, this message translates to:
  /// **'Yalnızca şu günler'**
  String get onlyTheseDays;

  /// No description provided for @intervalEvery.
  ///
  /// In tr, this message translates to:
  /// **'{n} saatte bir'**
  String intervalEvery(int n);

  /// No description provided for @firstDose.
  ///
  /// In tr, this message translates to:
  /// **'İlk doz saati'**
  String get firstDose;

  /// No description provided for @quantityPerDose.
  ///
  /// In tr, this message translates to:
  /// **'Doz başına miktar'**
  String get quantityPerDose;

  /// No description provided for @mealRelation.
  ///
  /// In tr, this message translates to:
  /// **'Aç / tok'**
  String get mealRelation;

  /// No description provided for @maxPerDay.
  ///
  /// In tr, this message translates to:
  /// **'Günde en fazla'**
  String get maxPerDay;

  /// No description provided for @requiredField.
  ///
  /// In tr, this message translates to:
  /// **'Bu alan zorunlu'**
  String get requiredField;

  /// No description provided for @atLeastOneTime.
  ///
  /// In tr, this message translates to:
  /// **'En az bir saat ekleyin'**
  String get atLeastOneTime;

  /// No description provided for @saved.
  ///
  /// In tr, this message translates to:
  /// **'Kaydedildi'**
  String get saved;

  /// No description provided for @scanTitle.
  ///
  /// In tr, this message translates to:
  /// **'İlaç tara'**
  String get scanTitle;

  /// No description provided for @scanHint.
  ///
  /// In tr, this message translates to:
  /// **'Kutunun üzerindeki karekodu veya barkodu çerçeveye alın'**
  String get scanHint;

  /// No description provided for @scanManualEntry.
  ///
  /// In tr, this message translates to:
  /// **'Barkodu elle gir'**
  String get scanManualEntry;

  /// No description provided for @barcodeDigits.
  ///
  /// In tr, this message translates to:
  /// **'13 haneli barkod'**
  String get barcodeDigits;

  /// No description provided for @duplicatePack.
  ///
  /// In tr, this message translates to:
  /// **'Bu kutu zaten eklenmiş'**
  String get duplicatePack;

  /// No description provided for @addStockQuestion.
  ///
  /// In tr, this message translates to:
  /// **'{name} listenizde var. Stoğa {qty} eklensin mi?'**
  String addStockQuestion(String name, String qty);

  /// No description provided for @addStockDone.
  ///
  /// In tr, this message translates to:
  /// **'Stok güncellendi'**
  String get addStockDone;

  /// No description provided for @notFoundInCatalog.
  ///
  /// In tr, this message translates to:
  /// **'Bu ilacı henüz tanımıyoruz. Bilgileri elle girebilirsiniz.'**
  String get notFoundInCatalog;

  /// No description provided for @expiryLabel.
  ///
  /// In tr, this message translates to:
  /// **'Son kullanma: {date}'**
  String expiryLabel(String date);

  /// No description provided for @invalidCode.
  ///
  /// In tr, this message translates to:
  /// **'Kod okunamadı'**
  String get invalidCode;

  /// No description provided for @doseQuantity.
  ///
  /// In tr, this message translates to:
  /// **'{qty} {form}'**
  String doseQuantity(String qty, String form);

  /// No description provided for @skipReasonTitle.
  ///
  /// In tr, this message translates to:
  /// **'Neden almayacaksınız?'**
  String get skipReasonTitle;

  /// No description provided for @reasonFeelingUnwell.
  ///
  /// In tr, this message translates to:
  /// **'Kendimi kötü hissediyorum'**
  String get reasonFeelingUnwell;

  /// No description provided for @reasonOutOfStock.
  ///
  /// In tr, this message translates to:
  /// **'İlacım bitti'**
  String get reasonOutOfStock;

  /// No description provided for @reasonDoctorAdvice.
  ///
  /// In tr, this message translates to:
  /// **'Doktorum kesti'**
  String get reasonDoctorAdvice;

  /// No description provided for @reasonOther.
  ///
  /// In tr, this message translates to:
  /// **'Diğer'**
  String get reasonOther;

  /// No description provided for @notifTitle.
  ///
  /// In tr, this message translates to:
  /// **'💊 {name} zamanı'**
  String notifTitle(String name);

  /// No description provided for @notifRepeatTitle.
  ///
  /// In tr, this message translates to:
  /// **'⏰ Hâlâ bekliyor: {name}'**
  String notifRepeatTitle(String name);

  /// No description provided for @lowStockTitle.
  ///
  /// In tr, this message translates to:
  /// **'📦 Stok azaldı'**
  String get lowStockTitle;

  /// No description provided for @lowStockBody.
  ///
  /// In tr, this message translates to:
  /// **'{name} için {amount} kaldı. Reçetenizi ve eczaneyi düşünün.'**
  String lowStockBody(String name, String amount);

  /// No description provided for @channelCritical.
  ///
  /// In tr, this message translates to:
  /// **'Kritik ilaç alarmları'**
  String get channelCritical;

  /// No description provided for @channelCriticalDesc.
  ///
  /// In tr, this message translates to:
  /// **'\"Aldım\" diyene kadar çalan alarmlar'**
  String get channelCriticalDesc;

  /// No description provided for @channelNormal.
  ///
  /// In tr, this message translates to:
  /// **'İlaç hatırlatmaları'**
  String get channelNormal;

  /// No description provided for @channelInfo.
  ///
  /// In tr, this message translates to:
  /// **'Bilgilendirme'**
  String get channelInfo;

  /// No description provided for @testAlarmTitle.
  ///
  /// In tr, this message translates to:
  /// **'🔔 Test alarmı'**
  String get testAlarmTitle;

  /// No description provided for @testAlarmBody.
  ///
  /// In tr, this message translates to:
  /// **'Bu bildirimi gördüyseniz alarmlarınız çalışıyor.'**
  String get testAlarmBody;

  /// No description provided for @language.
  ///
  /// In tr, this message translates to:
  /// **'Dil'**
  String get language;

  /// No description provided for @languageSystem.
  ///
  /// In tr, this message translates to:
  /// **'Sistem dili'**
  String get languageSystem;

  /// No description provided for @seniorMode.
  ///
  /// In tr, this message translates to:
  /// **'Basit Mod'**
  String get seniorMode;

  /// No description provided for @seniorModeHint.
  ///
  /// In tr, this message translates to:
  /// **'Büyük yazı, yüksek kontrast, tek ekran'**
  String get seniorModeHint;

  /// No description provided for @textScale.
  ///
  /// In tr, this message translates to:
  /// **'Yazı büyüklüğü'**
  String get textScale;

  /// No description provided for @ttsOnAlarm.
  ///
  /// In tr, this message translates to:
  /// **'Alarmda sesli oku'**
  String get ttsOnAlarm;

  /// No description provided for @caregiverPhone.
  ///
  /// In tr, this message translates to:
  /// **'Yakınımın telefonu'**
  String get caregiverPhone;

  /// No description provided for @seniorPin.
  ///
  /// In tr, this message translates to:
  /// **'Ayar kilidi PIN\'i (4 hane)'**
  String get seniorPin;

  /// No description provided for @seniorPinHint.
  ///
  /// In tr, this message translates to:
  /// **'Basit Mod\'dan çıkmak için gerekir'**
  String get seniorPinHint;

  /// No description provided for @enterPin.
  ///
  /// In tr, this message translates to:
  /// **'PIN girin'**
  String get enterPin;

  /// No description provided for @wrongPin.
  ///
  /// In tr, this message translates to:
  /// **'PIN hatalı'**
  String get wrongPin;

  /// No description provided for @reliability.
  ///
  /// In tr, this message translates to:
  /// **'Alarm güvenilirliği'**
  String get reliability;

  /// No description provided for @reliabilityIntro.
  ///
  /// In tr, this message translates to:
  /// **'Alarmlarınızın her zaman çalması için aşağıdaki maddelerin hepsi yeşil olmalı.'**
  String get reliabilityIntro;

  /// No description provided for @relNotifications.
  ///
  /// In tr, this message translates to:
  /// **'Bildirim izni'**
  String get relNotifications;

  /// No description provided for @relExactAlarms.
  ///
  /// In tr, this message translates to:
  /// **'Tam zamanlı alarm izni'**
  String get relExactAlarms;

  /// No description provided for @relBatteryOpt.
  ///
  /// In tr, this message translates to:
  /// **'Pil kısıtlaması kaldırıldı'**
  String get relBatteryOpt;

  /// No description provided for @relFullScreen.
  ///
  /// In tr, this message translates to:
  /// **'Kilit ekranında tam ekran alarm'**
  String get relFullScreen;

  /// No description provided for @relOemAutostart.
  ///
  /// In tr, this message translates to:
  /// **'Üretici ayarı: otomatik başlatma / arka plan'**
  String get relOemAutostart;

  /// No description provided for @relOemHint.
  ///
  /// In tr, this message translates to:
  /// **'{brand} telefonlar uygulamaları agresif kapatabilir. Açılan ekranda bu uygulamaya izin verin.'**
  String relOemHint(String brand);

  /// No description provided for @fix.
  ///
  /// In tr, this message translates to:
  /// **'Düzelt'**
  String get fix;

  /// No description provided for @open.
  ///
  /// In tr, this message translates to:
  /// **'Aç'**
  String get open;

  /// No description provided for @testAlarm.
  ///
  /// In tr, this message translates to:
  /// **'Test alarmı kur (1 dk sonra)'**
  String get testAlarm;

  /// No description provided for @testAlarmScheduled.
  ///
  /// In tr, this message translates to:
  /// **'Test alarmı 1 dakika sonra çalacak. Uygulamayı kapatıp ekranı kilitleyin.'**
  String get testAlarmScheduled;

  /// No description provided for @goodMorning.
  ///
  /// In tr, this message translates to:
  /// **'Günaydın'**
  String get goodMorning;

  /// No description provided for @goodAfternoon.
  ///
  /// In tr, this message translates to:
  /// **'İyi günler'**
  String get goodAfternoon;

  /// No description provided for @goodEvening.
  ///
  /// In tr, this message translates to:
  /// **'İyi akşamlar'**
  String get goodEvening;

  /// No description provided for @callCaregiver.
  ///
  /// In tr, this message translates to:
  /// **'📞 YAKINIMI ARA'**
  String get callCaregiver;

  /// No description provided for @nextDoses.
  ///
  /// In tr, this message translates to:
  /// **'SIRADAKİ'**
  String get nextDoses;

  /// No description provided for @todayProgress.
  ///
  /// In tr, this message translates to:
  /// **'Bugün: {total} ilacın {taken} tanesi alındı'**
  String todayProgress(int taken, int total);

  /// No description provided for @allDoneToday.
  ///
  /// In tr, this message translates to:
  /// **'Bugünkü ilaçlarınızın hepsini aldınız 👏'**
  String get allDoneToday;

  /// No description provided for @longPressForSettings.
  ///
  /// In tr, this message translates to:
  /// **'Ayarlar için başlığa uzun basın'**
  String get longPressForSettings;

  /// No description provided for @ttsDose.
  ///
  /// In tr, this message translates to:
  /// **'Saat {time} ilacınız: {name}. {meal}. {qty}.'**
  String ttsDose(String time, String name, String meal, String qty);

  /// No description provided for @alarmNotFound.
  ///
  /// In tr, this message translates to:
  /// **'Bu doz bulunamadı'**
  String get alarmNotFound;

  /// No description provided for @navToday.
  ///
  /// In tr, this message translates to:
  /// **'Bugün'**
  String get navToday;

  /// No description provided for @navSupplements.
  ///
  /// In tr, this message translates to:
  /// **'Takviyeler'**
  String get navSupplements;

  /// No description provided for @navPanel.
  ///
  /// In tr, this message translates to:
  /// **'Panel'**
  String get navPanel;

  /// No description provided for @navStats.
  ///
  /// In tr, this message translates to:
  /// **'İstatistik'**
  String get navStats;

  /// No description provided for @navLearn.
  ///
  /// In tr, this message translates to:
  /// **'Öğren'**
  String get navLearn;

  /// No description provided for @slotEarlyMorning.
  ///
  /// In tr, this message translates to:
  /// **'Erken sabah'**
  String get slotEarlyMorning;

  /// No description provided for @slotLateMorning.
  ///
  /// In tr, this message translates to:
  /// **'Geç sabah'**
  String get slotLateMorning;

  /// No description provided for @slotNoon.
  ///
  /// In tr, this message translates to:
  /// **'Öğle'**
  String get slotNoon;

  /// No description provided for @slotAfternoon.
  ///
  /// In tr, this message translates to:
  /// **'İkindi'**
  String get slotAfternoon;

  /// No description provided for @slotEvening.
  ///
  /// In tr, this message translates to:
  /// **'Akşam'**
  String get slotEvening;

  /// No description provided for @slotLateEvening.
  ///
  /// In tr, this message translates to:
  /// **'Gece'**
  String get slotLateEvening;

  /// No description provided for @groupMorning.
  ///
  /// In tr, this message translates to:
  /// **'Sabah'**
  String get groupMorning;

  /// No description provided for @groupAfternoon.
  ///
  /// In tr, this message translates to:
  /// **'Öğleden sonra'**
  String get groupAfternoon;

  /// No description provided for @groupEvening.
  ///
  /// In tr, this message translates to:
  /// **'Akşam'**
  String get groupEvening;

  /// No description provided for @groupNight.
  ///
  /// In tr, this message translates to:
  /// **'Gece'**
  String get groupNight;

  /// No description provided for @takenLabel.
  ///
  /// In tr, this message translates to:
  /// **'alındı'**
  String get takenLabel;

  /// No description provided for @streakDays.
  ///
  /// In tr, this message translates to:
  /// **'{n} gün'**
  String streakDays(int n);

  /// No description provided for @interactionsFound.
  ///
  /// In tr, this message translates to:
  /// **'{count, plural, =1{1 etkileşim uyarısı} other{{count} etkileşim uyarısı}}'**
  String interactionsFound(int count);

  /// No description provided for @addAnotherDose.
  ///
  /// In tr, this message translates to:
  /// **'Bir doz daha kaydet'**
  String get addAnotherDose;

  /// No description provided for @extraDoseLogged.
  ///
  /// In tr, this message translates to:
  /// **'Ek doz kaydedildi'**
  String get extraDoseLogged;

  /// No description provided for @moodQuestion.
  ///
  /// In tr, this message translates to:
  /// **'Bugün nasıl hissediyorsun?'**
  String get moodQuestion;

  /// No description provided for @emptyTodayTitle.
  ///
  /// In tr, this message translates to:
  /// **'Henüz takviye eklemediniz'**
  String get emptyTodayTitle;

  /// No description provided for @emptyTodayBody.
  ///
  /// In tr, this message translates to:
  /// **'Bir takviye ekleyin ya da hazır bir setle başlayın.'**
  String get emptyTodayBody;

  /// No description provided for @mySupplements.
  ///
  /// In tr, this message translates to:
  /// **'Takviyelerim'**
  String get mySupplements;

  /// No description provided for @supplementsCount.
  ///
  /// In tr, this message translates to:
  /// **'{n, plural, =1{1 takviye} other{{n} takviye}}'**
  String supplementsCount(int n);

  /// No description provided for @browsePresets.
  ///
  /// In tr, this message translates to:
  /// **'Hazır setlere göz at'**
  String get browsePresets;

  /// No description provided for @shareProgram.
  ///
  /// In tr, this message translates to:
  /// **'Programı PDF olarak paylaş'**
  String get shareProgram;

  /// No description provided for @paused.
  ///
  /// In tr, this message translates to:
  /// **'Duraklatıldı'**
  String get paused;

  /// No description provided for @searchSupplements.
  ///
  /// In tr, this message translates to:
  /// **'Takviye ara'**
  String get searchSupplements;

  /// No description provided for @notInList.
  ///
  /// In tr, this message translates to:
  /// **'Listede yok mu? Elle ekleyin'**
  String get notInList;

  /// No description provided for @catAll.
  ///
  /// In tr, this message translates to:
  /// **'Tümü'**
  String get catAll;

  /// No description provided for @catVitamin.
  ///
  /// In tr, this message translates to:
  /// **'Vitamin'**
  String get catVitamin;

  /// No description provided for @catMineral.
  ///
  /// In tr, this message translates to:
  /// **'Mineral'**
  String get catMineral;

  /// No description provided for @catFattyAcid.
  ///
  /// In tr, this message translates to:
  /// **'Yağ asidi'**
  String get catFattyAcid;

  /// No description provided for @catHerbal.
  ///
  /// In tr, this message translates to:
  /// **'Bitkisel'**
  String get catHerbal;

  /// No description provided for @catProbiotic.
  ///
  /// In tr, this message translates to:
  /// **'Probiyotik'**
  String get catProbiotic;

  /// No description provided for @catProtein.
  ///
  /// In tr, this message translates to:
  /// **'Protein'**
  String get catProtein;

  /// No description provided for @catMulti.
  ///
  /// In tr, this message translates to:
  /// **'Multivitamin'**
  String get catMulti;

  /// No description provided for @catMedicine.
  ///
  /// In tr, this message translates to:
  /// **'İlaç'**
  String get catMedicine;

  /// No description provided for @catOther.
  ///
  /// In tr, this message translates to:
  /// **'Diğer'**
  String get catOther;

  /// No description provided for @categoryLabel.
  ///
  /// In tr, this message translates to:
  /// **'Kategori'**
  String get categoryLabel;

  /// No description provided for @amountLabel.
  ///
  /// In tr, this message translates to:
  /// **'Doz (etiketteki)'**
  String get amountLabel;

  /// No description provided for @unitLabel.
  ///
  /// In tr, this message translates to:
  /// **'Birim'**
  String get unitLabel;

  /// No description provided for @labelDoseHint.
  ///
  /// In tr, this message translates to:
  /// **'Ürün etiketindeki dozu girin; uygulama doz önermez.'**
  String get labelDoseHint;

  /// No description provided for @howOften.
  ///
  /// In tr, this message translates to:
  /// **'Ne sıklıkta?'**
  String get howOften;

  /// No description provided for @freqDaily.
  ///
  /// In tr, this message translates to:
  /// **'Her gün'**
  String get freqDaily;

  /// No description provided for @freqEveryOtherDay.
  ///
  /// In tr, this message translates to:
  /// **'Gün aşırı'**
  String get freqEveryOtherDay;

  /// No description provided for @freqMwf.
  ///
  /// In tr, this message translates to:
  /// **'Pzt · Çar · Cum'**
  String get freqMwf;

  /// No description provided for @freqWeekends.
  ///
  /// In tr, this message translates to:
  /// **'Yalnızca hafta sonları'**
  String get freqWeekends;

  /// No description provided for @freqEveryNDays.
  ///
  /// In tr, this message translates to:
  /// **'Her N günde bir'**
  String get freqEveryNDays;

  /// No description provided for @freqSpecificDays.
  ///
  /// In tr, this message translates to:
  /// **'Belirli günler'**
  String get freqSpecificDays;

  /// No description provided for @freqCycle.
  ///
  /// In tr, this message translates to:
  /// **'Kullanım / ara döngüsü'**
  String get freqCycle;

  /// No description provided for @whenToTake.
  ///
  /// In tr, this message translates to:
  /// **'Ne zaman?'**
  String get whenToTake;

  /// No description provided for @nextDays14.
  ///
  /// In tr, this message translates to:
  /// **'Sonraki 14 gün'**
  String get nextDays14;

  /// No description provided for @cycleOnDays.
  ///
  /// In tr, this message translates to:
  /// **'Kullanım (gün)'**
  String get cycleOnDays;

  /// No description provided for @cycleOffDays.
  ///
  /// In tr, this message translates to:
  /// **'Ara (gün)'**
  String get cycleOffDays;

  /// No description provided for @nDaysLabel.
  ///
  /// In tr, this message translates to:
  /// **'Kaç günde bir?'**
  String get nDaysLabel;

  /// No description provided for @advanced.
  ///
  /// In tr, this message translates to:
  /// **'Gelişmiş'**
  String get advanced;

  /// No description provided for @pickAtLeastOneSlot.
  ///
  /// In tr, this message translates to:
  /// **'En az bir zaman seçin'**
  String get pickAtLeastOneSlot;

  /// No description provided for @conflictWarning.
  ///
  /// In tr, this message translates to:
  /// **'{other} ile yakın saatte: {reason}'**
  String conflictWarning(String other, String reason);

  /// No description provided for @ulWarning.
  ///
  /// In tr, this message translates to:
  /// **'Günlük toplam {total}, üst sınır (UL) {ul}. Doktorunuza danışın.'**
  String ulWarning(String total, String ul);

  /// No description provided for @dosePlan.
  ///
  /// In tr, this message translates to:
  /// **'Doz ve plan'**
  String get dosePlan;

  /// No description provided for @referenceValues.
  ///
  /// In tr, this message translates to:
  /// **'Referans değerler'**
  String get referenceValues;

  /// No description provided for @rdaLine.
  ///
  /// In tr, this message translates to:
  /// **'{kind}: Erkek {male} · Kadın {female}'**
  String rdaLine(String kind, String male, String female);

  /// No description provided for @ulLine.
  ///
  /// In tr, this message translates to:
  /// **'Üst sınır (UL): {ul}'**
  String ulLine(String ul);

  /// No description provided for @noUl.
  ///
  /// In tr, this message translates to:
  /// **'Belirlenmiş üst sınır yok'**
  String get noUl;

  /// No description provided for @noReference.
  ///
  /// In tr, this message translates to:
  /// **'Resmî referans değeri yok'**
  String get noReference;

  /// No description provided for @sourceNih.
  ///
  /// In tr, this message translates to:
  /// **'Kaynak: NIH ODS, 19–50 yaş yetişkin değerleri. Gebelik, emzirme ve hastalıklarda farklıdır. Bilgi amaçlıdır.'**
  String get sourceNih;

  /// No description provided for @compatibleWith.
  ///
  /// In tr, this message translates to:
  /// **'Birlikte iyi'**
  String get compatibleWith;

  /// No description provided for @takeSeparately.
  ///
  /// In tr, this message translates to:
  /// **'Ayrı alın'**
  String get takeSeparately;

  /// No description provided for @cautionLabel.
  ///
  /// In tr, this message translates to:
  /// **'Dikkat'**
  String get cautionLabel;

  /// No description provided for @inYourList.
  ///
  /// In tr, this message translates to:
  /// **'listenizde'**
  String get inYourList;

  /// No description provided for @pause.
  ///
  /// In tr, this message translates to:
  /// **'Duraklat'**
  String get pause;

  /// No description provided for @resume.
  ///
  /// In tr, this message translates to:
  /// **'Devam ettir'**
  String get resume;

  /// No description provided for @dosesPerDay.
  ///
  /// In tr, this message translates to:
  /// **'{n, plural, =1{günde 1 kez} other{günde {n} kez}}'**
  String dosesPerDay(int n);

  /// No description provided for @panelSubtitle.
  ///
  /// In tr, this message translates to:
  /// **'Tüm araçlar tek yerde'**
  String get panelSubtitle;

  /// No description provided for @toolScan.
  ///
  /// In tr, this message translates to:
  /// **'Karekod / barkod tara'**
  String get toolScan;

  /// No description provided for @toolCatalog.
  ///
  /// In tr, this message translates to:
  /// **'Takviye ara'**
  String get toolCatalog;

  /// No description provided for @toolInteractions.
  ///
  /// In tr, this message translates to:
  /// **'Etkileşim kontrolü'**
  String get toolInteractions;

  /// No description provided for @toolPresets.
  ///
  /// In tr, this message translates to:
  /// **'Hazır setler'**
  String get toolPresets;

  /// No description provided for @toolProgramPdf.
  ///
  /// In tr, this message translates to:
  /// **'Program PDF'**
  String get toolProgramPdf;

  /// No description provided for @toolDoctorReport.
  ///
  /// In tr, this message translates to:
  /// **'Doktor raporu'**
  String get toolDoctorReport;

  /// No description provided for @toolNearbyPharmacy.
  ///
  /// In tr, this message translates to:
  /// **'Yakındaki eczaneler'**
  String get toolNearbyPharmacy;

  /// No description provided for @toolDutyPharmacy.
  ///
  /// In tr, this message translates to:
  /// **'Nöbetçi eczaneler'**
  String get toolDutyPharmacy;

  /// No description provided for @toolSideEffect.
  ///
  /// In tr, this message translates to:
  /// **'Yan etki bildir'**
  String get toolSideEffect;

  /// No description provided for @toolReminders.
  ///
  /// In tr, this message translates to:
  /// **'Hatırlatıcılar'**
  String get toolReminders;

  /// No description provided for @sideEffectBody.
  ///
  /// In tr, this message translates to:
  /// **'Bir ilaç veya takviyeden sonra beklenmeyen bir etki fark ederseniz kullanmayı bırakıp doktorunuza veya eczacınıza danışın. İlaç yan etkileri Türkiye\'de TİTCK bünyesindeki Türkiye Farmakovijilans Merkezi\'ne (TÜFAM) bildirilebilir.'**
  String get sideEffectBody;

  /// No description provided for @openTitck.
  ///
  /// In tr, this message translates to:
  /// **'TİTCK sitesini aç'**
  String get openTitck;

  /// No description provided for @interactionsTitle.
  ///
  /// In tr, this message translates to:
  /// **'Etkileşim kontrolü'**
  String get interactionsTitle;

  /// No description provided for @sameTimeConflict.
  ///
  /// In tr, this message translates to:
  /// **'Yakın saatte planlı — ayırın'**
  String get sameTimeConflict;

  /// No description provided for @separatedOk.
  ///
  /// In tr, this message translates to:
  /// **'Farklı saatlerde ✓'**
  String get separatedOk;

  /// No description provided for @synergies.
  ///
  /// In tr, this message translates to:
  /// **'Birlikte iyi çalışanlar'**
  String get synergies;

  /// No description provided for @noInteractions.
  ///
  /// In tr, this message translates to:
  /// **'Listenizde bilinen bir etkileşim yok.'**
  String get noInteractions;

  /// No description provided for @interactionsDisclaimer.
  ///
  /// In tr, this message translates to:
  /// **'Bu liste yaygın takviye etkileşimlerini kapsar. İlaçlarınızla etkileşim için eczacınıza danışın.'**
  String get interactionsDisclaimer;

  /// No description provided for @presetDisclaimer.
  ///
  /// In tr, this message translates to:
  /// **'Örnek düzenlerdir, öneri değildir. Doz ve gereksinim için doktorunuza veya eczacınıza danışın.'**
  String get presetDisclaimer;

  /// No description provided for @addPreset.
  ///
  /// In tr, this message translates to:
  /// **'Seti ekle'**
  String get addPreset;

  /// No description provided for @presetAdded.
  ///
  /// In tr, this message translates to:
  /// **'{n, plural, =0{Bu setteki takviyelerin hepsi zaten listenizde} =1{1 takviye eklendi} other{{n} takviye eklendi}}'**
  String presetAdded(int n);

  /// No description provided for @tabBasics.
  ///
  /// In tr, this message translates to:
  /// **'Temel bilgiler'**
  String get tabBasics;

  /// No description provided for @tabSymptoms.
  ///
  /// In tr, this message translates to:
  /// **'Semptomlar'**
  String get tabSymptoms;

  /// No description provided for @tabAbsorption.
  ///
  /// In tr, this message translates to:
  /// **'Emilim'**
  String get tabAbsorption;

  /// No description provided for @basicsSubtitle.
  ///
  /// In tr, this message translates to:
  /// **'Günlük hedefler ve yüzdeler için kısa rehber.'**
  String get basicsSubtitle;

  /// No description provided for @symptomsSubtitle.
  ///
  /// In tr, this message translates to:
  /// **'İlişkili besinleri görmek için seçin. Bu tıbbi bir teşhis değildir.'**
  String get symptomsSubtitle;

  /// No description provided for @relatedNutrients.
  ///
  /// In tr, this message translates to:
  /// **'İlişkili besinler'**
  String get relatedNutrients;

  /// No description provided for @selectedSummary.
  ///
  /// In tr, this message translates to:
  /// **'Doktorunuzla konuşabileceğiniz besinler'**
  String get selectedSummary;

  /// No description provided for @seeDoctorBanner.
  ///
  /// In tr, this message translates to:
  /// **'Belirtileriniz sürüyorsa doktorunuza danışın. Kan testi yapılmadan takviyeye başlamayın.'**
  String get seeDoctorBanner;

  /// No description provided for @absorptionSubtitle.
  ///
  /// In tr, this message translates to:
  /// **'Emilimi artırabilecek veya azaltabilecek pratik örnekler.'**
  String get absorptionSubtitle;

  /// No description provided for @increases.
  ///
  /// In tr, this message translates to:
  /// **'Artırır'**
  String get increases;

  /// No description provided for @decreases.
  ///
  /// In tr, this message translates to:
  /// **'Azaltır'**
  String get decreases;

  /// No description provided for @tipLabel.
  ///
  /// In tr, this message translates to:
  /// **'İpucu'**
  String get tipLabel;

  /// No description provided for @statsTitle.
  ///
  /// In tr, this message translates to:
  /// **'İstatistikler'**
  String get statsTitle;

  /// No description provided for @balanceScore.
  ///
  /// In tr, this message translates to:
  /// **'Denge puanı'**
  String get balanceScore;

  /// No description provided for @outOf100.
  ///
  /// In tr, this message translates to:
  /// **'100 üzerinden'**
  String get outOf100;

  /// No description provided for @adherencePart.
  ///
  /// In tr, this message translates to:
  /// **'Uyum'**
  String get adherencePart;

  /// No description provided for @selfReportPart.
  ///
  /// In tr, this message translates to:
  /// **'Öz bildirim'**
  String get selfReportPart;

  /// No description provided for @streakTile.
  ///
  /// In tr, this message translates to:
  /// **'Günlük seri'**
  String get streakTile;

  /// No description provided for @streakSub.
  ///
  /// In tr, this message translates to:
  /// **'art arda eksiksiz gün'**
  String get streakSub;

  /// No description provided for @thisWeek.
  ///
  /// In tr, this message translates to:
  /// **'Bu hafta'**
  String get thisWeek;

  /// No description provided for @thisMonth.
  ///
  /// In tr, this message translates to:
  /// **'Bu ay'**
  String get thisMonth;

  /// No description provided for @takenRate.
  ///
  /// In tr, this message translates to:
  /// **'alınan doz oranı'**
  String get takenRate;

  /// No description provided for @last14Days.
  ///
  /// In tr, this message translates to:
  /// **'Son 14 gün'**
  String get last14Days;

  /// No description provided for @perSupplement.
  ///
  /// In tr, this message translates to:
  /// **'Takviye bazında (30 gün)'**
  String get perSupplement;

  /// No description provided for @moodTitle.
  ///
  /// In tr, this message translates to:
  /// **'Ruh hâli'**
  String get moodTitle;

  /// No description provided for @noDataYet.
  ///
  /// In tr, this message translates to:
  /// **'Henüz yeterli veri yok'**
  String get noDataYet;

  /// No description provided for @reportTitle.
  ///
  /// In tr, this message translates to:
  /// **'Takviye Raporu'**
  String get reportTitle;

  /// No description provided for @programTitle.
  ///
  /// In tr, this message translates to:
  /// **'Takviye Programı'**
  String get programTitle;

  /// No description provided for @generatedAt.
  ///
  /// In tr, this message translates to:
  /// **'{date} tarihinde oluşturuldu'**
  String generatedAt(String date);

  /// No description provided for @adherence30.
  ///
  /// In tr, this message translates to:
  /// **'30 günlük uyum'**
  String get adherence30;

  /// No description provided for @plannedTaken.
  ///
  /// In tr, this message translates to:
  /// **'Planlanan {planned} dozdan {taken} tanesi alındı'**
  String plannedTaken(int taken, int planned);

  /// No description provided for @currentProgram.
  ///
  /// In tr, this message translates to:
  /// **'Mevcut program'**
  String get currentProgram;

  /// No description provided for @colSupplement.
  ///
  /// In tr, this message translates to:
  /// **'Takviye'**
  String get colSupplement;

  /// No description provided for @colPlan.
  ///
  /// In tr, this message translates to:
  /// **'Plan'**
  String get colPlan;

  /// No description provided for @colDose.
  ///
  /// In tr, this message translates to:
  /// **'Doz'**
  String get colDose;

  /// No description provided for @colAdherence.
  ///
  /// In tr, this message translates to:
  /// **'Uyum'**
  String get colAdherence;

  /// No description provided for @pdfDisclaimer.
  ///
  /// In tr, this message translates to:
  /// **'Bu belge kullanıcının kendi kayıtlarına dayanır; tıbbi tavsiye veya teşhis yerine geçmez.'**
  String get pdfDisclaimer;

  /// No description provided for @remindersTitle.
  ///
  /// In tr, this message translates to:
  /// **'Hatırlatıcılar'**
  String get remindersTitle;

  /// No description provided for @enableReminders.
  ///
  /// In tr, this message translates to:
  /// **'Hatırlatıcıları etkinleştir'**
  String get enableReminders;

  /// No description provided for @enableRemindersHint.
  ///
  /// In tr, this message translates to:
  /// **'Takviye alma zamanı geldiğinde bildirim al.'**
  String get enableRemindersHint;

  /// No description provided for @reminderSlots.
  ///
  /// In tr, this message translates to:
  /// **'Zaman dilimleri'**
  String get reminderSlots;

  /// No description provided for @productInfo.
  ///
  /// In tr, this message translates to:
  /// **'Ürün bilgisi'**
  String get productInfo;

  /// No description provided for @barcodeLabel.
  ///
  /// In tr, this message translates to:
  /// **'Barkod'**
  String get barcodeLabel;

  /// No description provided for @serialLabel.
  ///
  /// In tr, this message translates to:
  /// **'Seri no'**
  String get serialLabel;

  /// No description provided for @lotLabel.
  ///
  /// In tr, this message translates to:
  /// **'Parti no'**
  String get lotLabel;

  /// No description provided for @addToMySupplements.
  ///
  /// In tr, this message translates to:
  /// **'Takviyelerime ekle'**
  String get addToMySupplements;

  /// No description provided for @findPharmacy.
  ///
  /// In tr, this message translates to:
  /// **'Eczane bul'**
  String get findPharmacy;

  /// No description provided for @readLeaflet.
  ///
  /// In tr, this message translates to:
  /// **'Kullanma talimatı'**
  String get readLeaflet;

  /// No description provided for @crashTitle.
  ///
  /// In tr, this message translates to:
  /// **'Önceki oturumda bir hata oluştu'**
  String get crashTitle;

  /// No description provided for @crashBody.
  ///
  /// In tr, this message translates to:
  /// **'Sorunu düzeltebilmemiz için raporu kopyalayıp geliştiriciye gönderebilirsiniz.'**
  String get crashBody;

  /// No description provided for @copyReport.
  ///
  /// In tr, this message translates to:
  /// **'Raporu kopyala'**
  String get copyReport;

  /// No description provided for @dismiss.
  ///
  /// In tr, this message translates to:
  /// **'Kapat'**
  String get dismiss;

  /// No description provided for @copied.
  ///
  /// In tr, this message translates to:
  /// **'Kopyalandı'**
  String get copied;

  /// No description provided for @startupError.
  ///
  /// In tr, this message translates to:
  /// **'Uygulama başlatılamadı'**
  String get startupError;

  /// No description provided for @exactAlarmOff.
  ///
  /// In tr, this message translates to:
  /// **'Tam zamanlı alarm izni kapalı — hatırlatmalar birkaç dakika gecikebilir.'**
  String get exactAlarmOff;

  /// No description provided for @unitTablet.
  ///
  /// In tr, this message translates to:
  /// **'tablet'**
  String get unitTablet;

  /// No description provided for @unitCapsule.
  ///
  /// In tr, this message translates to:
  /// **'kapsül'**
  String get unitCapsule;

  /// No description provided for @unitDrop.
  ///
  /// In tr, this message translates to:
  /// **'damla'**
  String get unitDrop;

  /// No description provided for @unitScoop.
  ///
  /// In tr, this message translates to:
  /// **'ölçek'**
  String get unitScoop;

  /// No description provided for @asNeededShort.
  ///
  /// In tr, this message translates to:
  /// **'Gerektikçe'**
  String get asNeededShort;

  /// No description provided for @cycleLabel.
  ///
  /// In tr, this message translates to:
  /// **'{on} gün kullan, {off} gün ara'**
  String cycleLabel(int on, int off);

  /// No description provided for @everyNHoursShort.
  ///
  /// In tr, this message translates to:
  /// **'{n} saatte bir'**
  String everyNHoursShort(int n);

  /// No description provided for @editSupplement.
  ///
  /// In tr, this message translates to:
  /// **'Düzenle'**
  String get editSupplement;

  /// No description provided for @details.
  ///
  /// In tr, this message translates to:
  /// **'Ayrıntılar'**
  String get details;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'tr'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'tr':
      return AppLocalizationsTr();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}

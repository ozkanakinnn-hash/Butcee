// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Turkish (`tr`).
class AppLocalizationsTr extends AppLocalizations {
  AppLocalizationsTr([String locale = 'tr']) : super(locale);

  @override
  String get appTitle => 'İlaç Takip';

  @override
  String get today => 'Bugün';

  @override
  String get nothingToday => 'Bugün planlanmış ilaç yok.';

  @override
  String pendingTitle(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count ilaç bekliyor',
      one: '1 ilaç bekliyor',
    );
    return '$_temp0';
  }

  @override
  String snoozedUntil(String time, int count, int max) {
    return '⏸ $time saatine ertelendi ($count/$max)';
  }

  @override
  String missedAt(String time) {
    return '✖ $time kaçırıldı';
  }

  @override
  String dueSince(String time) {
    return '⏰ $time\'dan beri bekliyor';
  }

  @override
  String get take => 'Aldım';

  @override
  String get takeBig => 'ALDIM';

  @override
  String get takeNow => 'Şimdi aldım';

  @override
  String get snooze10 => '10 dk sonra';

  @override
  String get skip => 'Atla';

  @override
  String get wontTake => 'Almayacağım';

  @override
  String get undo => 'Geri al';

  @override
  String get takenToast => 'Kaydedildi: alındı ✓';

  @override
  String get snoozedToast => '10 dakika sonra tekrar hatırlatacağım';

  @override
  String get skippedToast => 'Doz atlandı olarak kaydedildi';

  @override
  String get snoozeLimitReached => 'Erteleme hakkı doldu';

  @override
  String get addMedicine => 'İlaç ekle';

  @override
  String get scanBox => 'Kutuyu tara';

  @override
  String get addManually => 'Elle ekle';

  @override
  String get medicines => 'İlaçlarım';

  @override
  String get settings => 'Ayarlar';

  @override
  String get noMedicines => 'Henüz ilaç eklemediniz.';

  @override
  String stockLeft(String amount) {
    return 'Stok: $amount';
  }

  @override
  String get lowStockBadge => 'Stok azaldı';

  @override
  String get delete => 'Sil';

  @override
  String deleteMedicineConfirm(String name) {
    return '$name ve tüm planları silinsin mi?';
  }

  @override
  String get cancel => 'Vazgeç';

  @override
  String get save => 'Kaydet';

  @override
  String get ok => 'Tamam';

  @override
  String get mealNone => 'Fark etmez';

  @override
  String get afterMeal => 'Tok karnına';

  @override
  String get beforeMeal => 'Yemekten önce';

  @override
  String get withMeal => 'Yemekle birlikte';

  @override
  String get emptyStomach => 'Aç karnına';

  @override
  String get statusPending => 'Bekliyor';

  @override
  String get statusSnoozed => 'Ertelendi';

  @override
  String get statusTaken => 'Alındı';

  @override
  String get statusSkipped => 'Atlandı';

  @override
  String get statusMissed => 'Kaçırıldı';

  @override
  String get medicineName => 'İlaç adı';

  @override
  String get nickname => 'Takma ad (isteğe bağlı)';

  @override
  String get nicknameHint => 'Ör. Beyaz küçük hap — kalp ilacı';

  @override
  String get form => 'Form';

  @override
  String get formTablet => 'Tablet';

  @override
  String get formCapsule => 'Kapsül';

  @override
  String get formSyrup => 'Şurup';

  @override
  String get formDrops => 'Damla';

  @override
  String get formInjection => 'İğne';

  @override
  String get formOther => 'Diğer';

  @override
  String get stock => 'Mevcut stok';

  @override
  String get criticalStock => 'Uyarı verilecek stok';

  @override
  String get packageSize => 'Kutu içeriği';

  @override
  String get criticalMedicine => 'Kritik ilaç';

  @override
  String get criticalMedicineHint =>
      'Sessiz modda da alarm sesiyle çalar, kilit ekranında açılır';

  @override
  String get persistentAlarm => 'Israrcı alarm';

  @override
  String get persistentAlarmHint =>
      '\"Aldım\" diyene kadar 3 dakikada bir tekrar çalar';

  @override
  String get frequency => 'Ne sıklıkla?';

  @override
  String get freqFixedTimes => 'Belirli saatlerde';

  @override
  String get freqInterval => 'Saat aralığıyla';

  @override
  String get freqAsNeeded => 'Gerektiğinde';

  @override
  String get doseTimes => 'Doz saatleri';

  @override
  String get addTime => 'Saat ekle';

  @override
  String get everyDay => 'Her gün';

  @override
  String everyNDaysLabel(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n günde bir',
      two: 'Gün aşırı',
      one: 'Her gün',
    );
    return '$_temp0';
  }

  @override
  String get onlyTheseDays => 'Yalnızca şu günler';

  @override
  String intervalEvery(int n) {
    return '$n saatte bir';
  }

  @override
  String get firstDose => 'İlk doz saati';

  @override
  String get quantityPerDose => 'Doz başına miktar';

  @override
  String get mealRelation => 'Aç / tok';

  @override
  String get maxPerDay => 'Günde en fazla';

  @override
  String get requiredField => 'Bu alan zorunlu';

  @override
  String get atLeastOneTime => 'En az bir saat ekleyin';

  @override
  String get saved => 'Kaydedildi';

  @override
  String get scanTitle => 'İlaç tara';

  @override
  String get scanHint =>
      'Kutunun üzerindeki karekodu veya barkodu çerçeveye alın';

  @override
  String get scanManualEntry => 'Barkodu elle gir';

  @override
  String get barcodeDigits => '13 haneli barkod';

  @override
  String get duplicatePack => 'Bu kutu zaten eklenmiş';

  @override
  String addStockQuestion(String name, String qty) {
    return '$name listenizde var. Stoğa $qty eklensin mi?';
  }

  @override
  String get addStockDone => 'Stok güncellendi';

  @override
  String get notFoundInCatalog =>
      'Bu ilacı henüz tanımıyoruz. Bilgileri elle girebilirsiniz.';

  @override
  String expiryLabel(String date) {
    return 'Son kullanma: $date';
  }

  @override
  String get invalidCode => 'Kod okunamadı';

  @override
  String doseQuantity(String qty, String form) {
    return '$qty $form';
  }

  @override
  String get skipReasonTitle => 'Neden almayacaksınız?';

  @override
  String get reasonFeelingUnwell => 'Kendimi kötü hissediyorum';

  @override
  String get reasonOutOfStock => 'İlacım bitti';

  @override
  String get reasonDoctorAdvice => 'Doktorum kesti';

  @override
  String get reasonOther => 'Diğer';

  @override
  String notifTitle(String name) {
    return '💊 $name zamanı';
  }

  @override
  String notifRepeatTitle(String name) {
    return '⏰ Hâlâ bekliyor: $name';
  }

  @override
  String get lowStockTitle => '📦 Stok azaldı';

  @override
  String lowStockBody(String name, String amount) {
    return '$name için $amount kaldı. Reçetenizi ve eczaneyi düşünün.';
  }

  @override
  String get channelCritical => 'Kritik ilaç alarmları';

  @override
  String get channelCriticalDesc => '\"Aldım\" diyene kadar çalan alarmlar';

  @override
  String get channelNormal => 'İlaç hatırlatmaları';

  @override
  String get channelInfo => 'Bilgilendirme';

  @override
  String get testAlarmTitle => '🔔 Test alarmı';

  @override
  String get testAlarmBody =>
      'Bu bildirimi gördüyseniz alarmlarınız çalışıyor.';

  @override
  String get language => 'Dil';

  @override
  String get languageSystem => 'Sistem dili';

  @override
  String get seniorMode => 'Basit Mod';

  @override
  String get seniorModeHint => 'Büyük yazı, yüksek kontrast, tek ekran';

  @override
  String get textScale => 'Yazı büyüklüğü';

  @override
  String get ttsOnAlarm => 'Alarmda sesli oku';

  @override
  String get caregiverPhone => 'Yakınımın telefonu';

  @override
  String get seniorPin => 'Ayar kilidi PIN\'i (4 hane)';

  @override
  String get seniorPinHint => 'Basit Mod\'dan çıkmak için gerekir';

  @override
  String get enterPin => 'PIN girin';

  @override
  String get wrongPin => 'PIN hatalı';

  @override
  String get reliability => 'Alarm güvenilirliği';

  @override
  String get reliabilityIntro =>
      'Alarmlarınızın her zaman çalması için aşağıdaki maddelerin hepsi yeşil olmalı.';

  @override
  String get relNotifications => 'Bildirim izni';

  @override
  String get relExactAlarms => 'Tam zamanlı alarm izni';

  @override
  String get relBatteryOpt => 'Pil kısıtlaması kaldırıldı';

  @override
  String get relFullScreen => 'Kilit ekranında tam ekran alarm';

  @override
  String get relOemAutostart => 'Üretici ayarı: otomatik başlatma / arka plan';

  @override
  String relOemHint(String brand) {
    return '$brand telefonlar uygulamaları agresif kapatabilir. Açılan ekranda bu uygulamaya izin verin.';
  }

  @override
  String get fix => 'Düzelt';

  @override
  String get open => 'Aç';

  @override
  String get testAlarm => 'Test alarmı kur (1 dk sonra)';

  @override
  String get testAlarmScheduled =>
      'Test alarmı 1 dakika sonra çalacak. Uygulamayı kapatıp ekranı kilitleyin.';

  @override
  String get goodMorning => 'Günaydın';

  @override
  String get goodAfternoon => 'İyi günler';

  @override
  String get goodEvening => 'İyi akşamlar';

  @override
  String get callCaregiver => '📞 YAKINIMI ARA';

  @override
  String get nextDoses => 'SIRADAKİ';

  @override
  String todayProgress(int taken, int total) {
    return 'Bugün: $total ilacın $taken tanesi alındı';
  }

  @override
  String get allDoneToday => 'Bugünkü ilaçlarınızın hepsini aldınız 👏';

  @override
  String get longPressForSettings => 'Ayarlar için başlığa uzun basın';

  @override
  String ttsDose(String time, String name, String meal, String qty) {
    return 'Saat $time ilacınız: $name. $meal. $qty.';
  }

  @override
  String get alarmNotFound => 'Bu doz bulunamadı';
}

// ignore: unused_import
import 'package:intl/intl.dart' as intl;

import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for Turkish (`tr`).
class AppLocalizationsTr extends AppLocalizations {
  AppLocalizationsTr([String locale = 'tr']) : super(locale);

  @override
  String get appTitle => 'Vitamin Takip';

  @override
  String get today => 'Bugün';

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
  String get medicines => 'İlaçlarım';

  @override
  String get settings => 'Ayarlar';

  @override
  String get noMedicines => 'Henüz ilaç eklemediniz.';

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
  String get freqInterval => 'Saat aralığıyla';

  @override
  String get freqAsNeeded => 'Gerektiğinde';

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

  @override
  String get navToday => 'Bugün';

  @override
  String get navSupplements => 'Takviyeler';

  @override
  String get navPanel => 'Panel';

  @override
  String get navStats => 'İstatistik';

  @override
  String get navLearn => 'Öğren';

  @override
  String get slotEarlyMorning => 'Erken sabah';

  @override
  String get slotLateMorning => 'Geç sabah';

  @override
  String get slotNoon => 'Öğle';

  @override
  String get slotAfternoon => 'İkindi';

  @override
  String get slotEvening => 'Akşam';

  @override
  String get slotLateEvening => 'Gece';

  @override
  String get groupMorning => 'Sabah';

  @override
  String get groupAfternoon => 'Öğleden sonra';

  @override
  String get groupEvening => 'Akşam';

  @override
  String get groupNight => 'Gece';

  @override
  String get takenLabel => 'alındı';

  @override
  String interactionsFound(int count) {
    String _temp0 = intl.Intl.pluralLogic(
      count,
      locale: localeName,
      other: '$count etkileşim uyarısı',
      one: '1 etkileşim uyarısı',
    );
    return '$_temp0';
  }

  @override
  String get addAnotherDose => 'Bir doz daha kaydet';

  @override
  String get extraDoseLogged => 'Ek doz kaydedildi';

  @override
  String get moodQuestion => 'Bugün nasıl hissediyorsun?';

  @override
  String get emptyTodayTitle => 'Henüz takviye eklemediniz';

  @override
  String get emptyTodayBody =>
      'Bir takviye ekleyin ya da hazır bir setle başlayın.';

  @override
  String get mySupplements => 'Takviyelerim';

  @override
  String supplementsCount(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n takviye',
      one: '1 takviye',
    );
    return '$_temp0';
  }

  @override
  String get browsePresets => 'Hazır setlere göz at';

  @override
  String get shareProgram => 'Programı PDF olarak paylaş';

  @override
  String get paused => 'Duraklatıldı';

  @override
  String get searchSupplements => 'Takviye ara';

  @override
  String get notInList => 'Listede yok mu? Elle ekleyin';

  @override
  String get catAll => 'Tümü';

  @override
  String get catVitamin => 'Vitamin';

  @override
  String get catMineral => 'Mineral';

  @override
  String get catFattyAcid => 'Yağ asidi';

  @override
  String get catHerbal => 'Bitkisel';

  @override
  String get catProbiotic => 'Probiyotik';

  @override
  String get catProtein => 'Protein';

  @override
  String get catMulti => 'Multivitamin';

  @override
  String get catMedicine => 'İlaç';

  @override
  String get catOther => 'Diğer';

  @override
  String get categoryLabel => 'Kategori';

  @override
  String get amountLabel => 'Doz (etiketteki)';

  @override
  String get unitLabel => 'Birim';

  @override
  String get labelDoseHint =>
      'Ürün etiketindeki dozu girin; uygulama doz önermez.';

  @override
  String get howOften => 'Ne sıklıkta?';

  @override
  String get freqDaily => 'Her gün';

  @override
  String get freqEveryOtherDay => 'Gün aşırı';

  @override
  String get freqMwf => 'Pzt · Çar · Cum';

  @override
  String get freqWeekends => 'Yalnızca hafta sonları';

  @override
  String get freqEveryNDays => 'Her N günde bir';

  @override
  String get freqSpecificDays => 'Belirli günler';

  @override
  String get freqCycle => 'Kullanım / ara döngüsü';

  @override
  String get whenToTake => 'Ne zaman?';

  @override
  String get nextDays14 => 'Sonraki 14 gün';

  @override
  String get cycleOnDays => 'Kullanım (gün)';

  @override
  String get cycleOffDays => 'Ara (gün)';

  @override
  String get nDaysLabel => 'Kaç günde bir?';

  @override
  String get advanced => 'Gelişmiş';

  @override
  String get pickAtLeastOneSlot => 'En az bir zaman seçin';

  @override
  String conflictWarning(String other, String reason) {
    return '$other ile yakın saatte: $reason';
  }

  @override
  String ulWarning(String total, String ul) {
    return 'Günlük toplam $total, üst sınır (UL) $ul. Doktorunuza danışın.';
  }

  @override
  String get dosePlan => 'Doz ve plan';

  @override
  String get referenceValues => 'Referans değerler';

  @override
  String rdaLine(String kind, String male, String female) {
    return '$kind: Erkek $male · Kadın $female';
  }

  @override
  String ulLine(String ul) {
    return 'Üst sınır (UL): $ul';
  }

  @override
  String get noUl => 'Belirlenmiş üst sınır yok';

  @override
  String get noReference => 'Resmî referans değeri yok';

  @override
  String get sourceNih =>
      'Kaynak: NIH ODS, 19–50 yaş yetişkin değerleri. Gebelik, emzirme ve hastalıklarda farklıdır. Bilgi amaçlıdır.';

  @override
  String get compatibleWith => 'Birlikte iyi';

  @override
  String get takeSeparately => 'Ayrı alın';

  @override
  String get cautionLabel => 'Dikkat';

  @override
  String get inYourList => 'listenizde';

  @override
  String get pause => 'Duraklat';

  @override
  String get resume => 'Devam ettir';

  @override
  String dosesPerDay(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: 'günde $n kez',
      one: 'günde 1 kez',
    );
    return '$_temp0';
  }

  @override
  String get panelSubtitle => 'Tüm araçlar tek yerde';

  @override
  String get toolScan => 'Karekod / barkod tara';

  @override
  String get toolCatalog => 'Takviye ara';

  @override
  String get toolInteractions => 'Etkileşim kontrolü';

  @override
  String get toolPresets => 'Hazır setler';

  @override
  String get toolProgramPdf => 'Program PDF';

  @override
  String get toolDoctorReport => 'Doktor raporu';

  @override
  String get toolNearbyPharmacy => 'Yakındaki eczaneler';

  @override
  String get toolDutyPharmacy => 'Nöbetçi eczaneler';

  @override
  String get toolSideEffect => 'Yan etki bildir';

  @override
  String get toolReminders => 'Hatırlatıcılar';

  @override
  String get sideEffectBody =>
      'Bir ilaç veya takviyeden sonra beklenmeyen bir etki fark ederseniz kullanmayı bırakıp doktorunuza veya eczacınıza danışın. İlaç yan etkileri Türkiye\'de TİTCK bünyesindeki Türkiye Farmakovijilans Merkezi\'ne (TÜFAM) bildirilebilir.';

  @override
  String get openTitck => 'TİTCK sitesini aç';

  @override
  String get interactionsTitle => 'Etkileşim kontrolü';

  @override
  String get sameTimeConflict => 'Yakın saatte planlı — ayırın';

  @override
  String get separatedOk => 'Farklı saatlerde ✓';

  @override
  String get synergies => 'Birlikte iyi çalışanlar';

  @override
  String get noInteractions => 'Listenizde bilinen bir etkileşim yok.';

  @override
  String get interactionsDisclaimer =>
      'Bu liste yaygın takviye etkileşimlerini kapsar. İlaçlarınızla etkileşim için eczacınıza danışın.';

  @override
  String get presetDisclaimer =>
      'Örnek düzenlerdir, öneri değildir. Doz ve gereksinim için doktorunuza veya eczacınıza danışın.';

  @override
  String get addPreset => 'Seti ekle';

  @override
  String presetAdded(int n) {
    String _temp0 = intl.Intl.pluralLogic(
      n,
      locale: localeName,
      other: '$n takviye eklendi',
      one: '1 takviye eklendi',
      zero: 'Bu setteki takviyelerin hepsi zaten listenizde',
    );
    return '$_temp0';
  }

  @override
  String get tabBasics => 'Temel bilgiler';

  @override
  String get tabSymptoms => 'Semptomlar';

  @override
  String get tabAbsorption => 'Emilim';

  @override
  String get basicsSubtitle => 'Günlük hedefler ve yüzdeler için kısa rehber.';

  @override
  String get symptomsSubtitle =>
      'İlişkili besinleri görmek için seçin. Bu tıbbi bir teşhis değildir.';

  @override
  String get relatedNutrients => 'İlişkili besinler';

  @override
  String get selectedSummary => 'Doktorunuzla konuşabileceğiniz besinler';

  @override
  String get seeDoctorBanner =>
      'Belirtileriniz sürüyorsa doktorunuza danışın. Kan testi yapılmadan takviyeye başlamayın.';

  @override
  String get absorptionSubtitle =>
      'Emilimi artırabilecek veya azaltabilecek pratik örnekler.';

  @override
  String get increases => 'Artırır';

  @override
  String get decreases => 'Azaltır';

  @override
  String get tipLabel => 'İpucu';

  @override
  String get statsTitle => 'İstatistikler';

  @override
  String get balanceScore => 'Denge puanı';

  @override
  String get outOf100 => '100 üzerinden';

  @override
  String get adherencePart => 'Uyum';

  @override
  String get selfReportPart => 'Öz bildirim';

  @override
  String get streakTile => 'Günlük seri';

  @override
  String get streakSub => 'art arda eksiksiz gün';

  @override
  String get thisWeek => 'Bu hafta';

  @override
  String get thisMonth => 'Bu ay';

  @override
  String get takenRate => 'alınan doz oranı';

  @override
  String get last14Days => 'Son 14 gün';

  @override
  String get perSupplement => 'Takviye bazında (30 gün)';

  @override
  String get moodTitle => 'Ruh hâli';

  @override
  String get noDataYet => 'Henüz yeterli veri yok';

  @override
  String get reportTitle => 'Takviye Raporu';

  @override
  String get programTitle => 'Takviye Programı';

  @override
  String generatedAt(String date) {
    return '$date tarihinde oluşturuldu';
  }

  @override
  String get adherence30 => '30 günlük uyum';

  @override
  String plannedTaken(int taken, int planned) {
    return 'Planlanan $planned dozdan $taken tanesi alındı';
  }

  @override
  String get currentProgram => 'Mevcut program';

  @override
  String get colSupplement => 'Takviye';

  @override
  String get colPlan => 'Plan';

  @override
  String get colDose => 'Doz';

  @override
  String get colAdherence => 'Uyum';

  @override
  String get pdfDisclaimer =>
      'Bu belge kullanıcının kendi kayıtlarına dayanır; tıbbi tavsiye veya teşhis yerine geçmez.';

  @override
  String get remindersTitle => 'Hatırlatıcılar';

  @override
  String get enableReminders => 'Hatırlatıcıları etkinleştir';

  @override
  String get enableRemindersHint =>
      'Takviye alma zamanı geldiğinde bildirim al.';

  @override
  String get reminderSlots => 'Zaman dilimleri';

  @override
  String get productInfo => 'Ürün bilgisi';

  @override
  String get barcodeLabel => 'Barkod';

  @override
  String get serialLabel => 'Seri no';

  @override
  String get lotLabel => 'Parti no';

  @override
  String get addToMySupplements => 'Takviyelerime ekle';

  @override
  String get findPharmacy => 'Eczane bul';

  @override
  String get readLeaflet => 'Kullanma talimatı';

  @override
  String get crashTitle => 'Önceki oturumda bir hata oluştu';

  @override
  String get crashBody =>
      'Sorunu düzeltebilmemiz için raporu kopyalayıp geliştiriciye gönderebilirsiniz.';

  @override
  String get copyReport => 'Raporu kopyala';

  @override
  String get dismiss => 'Kapat';

  @override
  String get exactAlarmOff =>
      'Tam zamanlı alarm izni kapalı — hatırlatmalar birkaç dakika gecikebilir.';

  @override
  String get unitTablet => 'tablet';

  @override
  String get unitCapsule => 'kapsül';

  @override
  String get unitDrop => 'damla';

  @override
  String get unitScoop => 'ölçek';

  @override
  String get asNeededShort => 'Gerektikçe';

  @override
  String cycleLabel(int on, int off) {
    return '$on gün kullan, $off gün ara';
  }

  @override
  String everyNHoursShort(int n) {
    return '$n saatte bir';
  }

  @override
  String get editSupplement => 'Düzenle';
}

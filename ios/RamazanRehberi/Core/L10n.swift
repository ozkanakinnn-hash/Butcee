import Foundation

/// Uygulama dili (tr / en). Metinler Türkçe yazılır; İngilizce seçiliyse sözlükten çevrilir.
enum L10n {
  static var current: String = UserDefaults.standard.string(forKey: "key_language") ?? "tr"
  static var isEnglish: Bool { current == "en" }
  private static let prayerWords = ["İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"]

  static func t(_ tr: String) -> String {
    guard isEnglish else { return tr }
    if let v = AppContent.shared.dictionary[tr] ?? extra[tr] { return v }
    if tr.hasSuffix(" Vaktine Kalan") {
      return "Time until " + t(String(tr.dropLast(" Vaktine Kalan".count)))
    }
    for w in prayerWords where tr.hasPrefix(w + " ") {
      return t(w) + tr.dropFirst(w.count)
    }
    return tr
  }

  /// iOS sürümüne özgü ek metinler (Android sözlüğünde olmayanlar).
  static let extra: [String: String] = [
    "Ana Sayfa": "Home",
    "Kur'an-ı Kerim": "Holy Quran",
    "Ramazan Rehberi": "Ramadan Guide",
    "Takvim": "Calendar",
    "Ayet & Dua": "Verse & Dua",
    "Dahası": "More",
    "Merhaba": "Hello",
    "Değerli Kardeşimiz": "Dear Friend",
    "Yakındaki Camiler": "Nearby Mosques",
    "Harita & yol tarifi": "Map & directions",
    "Zikirmatik": "Tasbih Counter",
    "Toplam": "Total",
    "KIBLE": "QIBLA",
    "Sonra": "Next",
    "İmsak": "Imsak", "Güneş": "Sunrise", "Öğle": "Dhuhr", "İkindi": "Asr", "Akşam": "Maghrib", "Yatsı": "Isha",
    "İmsaka Kalan Vakit": "Time until Imsak",
    "İftara Kalan Vakit": "Time until Iftar",
    "Teravihe Kalan Vakit": "Time until Tarawih",
    "İmsak (Sahur)": "Imsak (Suhoor)",
    "Akşam (İftar)": "Maghrib (Iftar)",
    "Yatsı (Teravih)": "Isha (Tarawih)",
    "Sevdiklerinizle Paylaşın": "Share with Loved Ones",
    "Kandil, bayram ve cuma kartları": "Kandil, Eid and Friday cards",
    "Özel Gün Kartları": "Special Day Cards",
    "Sevdiklerinize kart gönderin": "Send cards to your loved ones",
    "Hadis": "Hadith",
    "Ayet": "Verse",
    "Dua": "Dua",
    "Esmâ": "Divine Name",
    "Esmâü'l-Hüsnâ": "Asma ul-Husna",
    "Allah'ın 99 ismi": "99 Names of Allah",
    "Kaza Namazı": "Qada Prayers",
    "Kandil Duaları": "Kandil Prayers",
    "Hatim Takibi": "Khatm Tracker",
    "Bildirim Sihirbazı": "Notification Wizard",
    "Kıble Bulucu": "Qibla Finder",
    "Reklamsız Premium'a geç": "Go Premium, ad-free",
    "Reklamları kaldır, uygulamaya destek ol": "Remove ads and support the app",
    "Orucun Faydaları": "Benefits of Fasting",
    "Dinî Faydalar": "Spiritual Benefits",
    "Tıbbi Faydalar": "Health Benefits",
    "İftar Önerileri": "Iftar Tips",
    "Sahur Önerileri": "Suhoor Tips",
    "Günün İftar Menüsü": "Today's Iftar Menu",
    "Günün Sahur Menüsü": "Today's Suhoor Menu",
    "Çorba": "Soup", "Ana Yemek": "Main Course", "Yardımcı Lezzet": "Side", "Tatlı": "Dessert",
    "SICAK BAŞLANGIÇ": "WARM START", "PROTEİN": "PROTEIN", "TAM TAHIL & SEBZE": "WHOLE GRAIN & VEG", "İÇECEK & MEYVE": "DRINK & FRUIT",
    "Günün": "Today's", "Sahur": "Suhoor", "Menü Önerisi": "Menu Suggestion",
    "İmsaktan önce bitirin": "Finish before imsak",
    "Bol su için, tuzludan kaçının": "Drink plenty of water, avoid salty food",
    "Sureler": "Surahs",
    "Sure adı, cüz no veya anlam ara": "Search surah, juz or meaning",
    "Arapça": "Arabic", "Okunuş": "Transliteration", "Meal": "Translation",
    "Sayfa": "Page", "Cüz": "Juz",
    "Bugün": "Today",
    "Oruç tutacağım": "I will fast",
    "Oruç planını kaldır": "Remove fast plan",
    "Oruç Planla": "Plan a Fast",
    "Bu Yıl Toplam": "This Year",
    "Ramazan": "Ramadan",
    "Kaza Borcu": "Qada Debt",
    "Sıfırla": "Reset",
    "Zikri sıfırlamak istiyor musunuz?": "Reset the counter?",
    "Vazgeç": "Cancel",
    "Ayarlar": "Settings",
    "Dil": "Language",
    "Tema": "Theme",
    "Renk Teması": "Color Theme",
    "Bildirimi Test Et": "Test Notification",
    "Bildirim izni ver": "Allow notifications",
    "Orucun tamamlanan kısmı": "Fast completed",
    "İftara": "Until iftar",
    "Konum Seç": "Choose Location",
    "Ülke": "Country", "İl": "Province", "İlçe": "District",
    "Premium": "Premium",
    "Satın Al": "Buy",
    "Satın Alımları Geri Yükle": "Restore Purchases",
    "Gizlilik Politikası": "Privacy Policy",
    "Kart Paylaş": "Share Card",
    "Ana Ekran Widget'ları": "Home Screen Widgets",
    "Kıble": "Qibla",
    "Reklam gizlilik seçenekleri": "Ad privacy options",
    "📖 Sureler": "📖 Surahs",
    "🏆 Hatim Takibi": "🏆 Khatm Tracker",
    "Oku": "Read",
    "📍 Konumu otomatik takip et (GPS)": "📍 Follow my location automatically (GPS)",
    "Başka şehre gidince namaz vakitleri kendiliğinden güncellenir": "Prayer times update by themselves when you travel",
    "(GPS koordinatına göre)": "(by GPS coordinates)",
    "📍 Konumumu otomatik bul (GPS)": "📍 Find my location automatically (GPS)",
  ]
}

/// Kısayol: Kotlin'deki t() ile aynı.
@inline(__always) func t(_ tr: String) -> String { L10n.t(tr) }

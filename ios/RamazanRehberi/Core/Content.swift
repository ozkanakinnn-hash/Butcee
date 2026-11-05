import Foundation

// Android uygulamasındaki içerik (Kotlin data nesneleri) birebir content.json olarak paketlenir.

struct Bi: Codable, Hashable {
  let tr: String
  let en: String
  var text: String { L10n.isEnglish ? en : tr }
}

struct EsmaName: Codable, Hashable, Identifiable {
  let number: Int
  let arabic: String
  let name: String
  let meaningTr: String
  let meaningEn: String
  var id: Int { number }
  var meaning: String { L10n.isEnglish ? meaningEn : meaningTr }
}

struct Benefit: Codable, Hashable {
  let title: Bi
  let body: Bi
  let source: String
}

struct Dua: Codable, Hashable {
  let title: Bi
  let arabic: String
  let okunus: String
  let meaning: Bi?
  let source: String?
}

struct KandilInfo: Codable, Hashable, Identifiable {
  let key: String
  let name: Bi
  let about: Bi
  let duas: [Dua]
  var id: String { key }
}

struct Country: Codable, Hashable {
  let code: String
  let name: String
  let cities: [String]
}

struct Province: Codable, Hashable {
  let id: Int
  let name: String
  let districts: [String]
}

struct NameSuggestion: Codable, Hashable {
  let name: String
  let meaning: String
}

struct SurahInfo: Codable, Hashable, Identifiable {
  let number: Int
  let nameTurkish: String
  let nameArabic: String
  let meaningTurkish: String
  let totalVerses: Int
  let revelationType: String
  let juzNumber: Int
  let juzRangeStr: String
  var id: Int { number }
  var meaning: String { L10n.isEnglish ? (AppContent.shared.english.surahMeanings[safe: number - 1] ?? meaningTurkish) : meaningTurkish }
}

struct RamadanRange: Codable, Hashable {
  let start: String
  let end: String
}

struct RamadanMenu: Codable, Hashable {
  let day: Int
  let soup: String
  let mainDish: String
  let sideDish: String
  let dessert: String
}

struct SahurMenu: Codable, Hashable {
  let day: Int
  let warm: Bi
  let protein: Bi
  let grain: Bi
  let drink: Bi
}

struct SpiritualItem: Codable, Hashable {
  let day: Int
  var verseArabic: String
  var verseTurkish: String
  var verseSource: String
  var hadithTurkish: String
  var hadithSource: String
  var prayerTurkish: String
  var prayerSource: String
  var esmaArabic: String
  var esmaTurkish: String
  var esmaMeaning: String
}

struct EnglishSpiritual: Codable, Hashable {
  let verse: String
  let verseSource: String
  let hadith: String
  let hadithSource: String
  let prayer: String
  let prayerSource: String
  let esmaMeaning: String
}

private struct RawContent: Decodable {
  struct Daily: Decodable { let messages: [[String]] }
  struct English: Decodable {
    let dishes: [String: String]
    let spiritual: [String: EnglishSpiritual]
    let nameMeanings: [String: String]
    let surahMeanings: [String]
    let BISMILLAH_T: String
    let surahTexts: [String: String]
  }
  struct Esma: Decodable { let names: [EsmaName] }
  struct Fasting: Decodable {
    let religiousBenefits: [Benefit]
    let medicalBenefits: [Benefit]
    let medicalNote: Bi
    let iftarTips: [Bi]
    let sahurTips: [Bi]
    let dailyTips: [Bi]
  }
  struct Kandil: Decodable {
    let general: [Dua]
    let worship: [Bi]
    let note: Bi
    let kandiller: [KandilInfo]
  }
  struct Locations: Decodable { let countries: [Country] }
  struct Names: Decodable { let girls: [NameSuggestion]; let boys: [NameSuggestion] }
  struct Quran: Decodable { let surahs: [SurahInfo]; let BASMALA_AR: String }
  struct Ramadan: Decodable { let ranges: [RamadanRange] }
  struct Menus: Decodable { let menus: [RamadanMenu] }
  struct Sahur: Decodable { let menus: [SahurMenu] }
  struct Spiritual: Decodable { let list: [SpiritualItem]; let generalList: [SpiritualItem] }
  struct Provinces: Decodable { let list: [Province] }
  struct I18n: Decodable { let dictionary: [String: String] }
  struct Motivation: Decodable { let sahur: [String]; let iftar: [String] }

  let DailyMessages: Daily
  let EnglishContent: English
  let EsmaulHusna: Esma
  let FastingKnowledge: Fasting
  let KandilDualari: Kandil
  let LocationsData: Locations
  let NameSuggestions: Names
  let QuranData: Quran
  let RamadanCalendar: Ramadan
  let RamadanMenus: Menus
  let SahurMenus: Sahur
  let SpiritualQuotes: Spiritual
  let TurkeyProvinces: Provinces
  let I18nKt: I18n
  let MotivationMessages: Motivation
}

struct EnglishContentData {
  let dishes: [String: String]
  let spiritual: [Int: EnglishSpiritual]
  let nameMeanings: [String: String]
  let surahMeanings: [String]
  let bismillah: String
  let surahTexts: [Int: String]
  func dish(_ tr: String) -> String { dishes[tr] ?? tr }
}

final class AppContent {
  static let shared = AppContent()

  let dailyMessages: [(String, String)]
  let english: EnglishContentData
  let esma: [EsmaName]
  let religiousBenefits: [Benefit]
  let medicalBenefits: [Benefit]
  let medicalNote: Bi
  let iftarTips: [Bi]
  let sahurTips: [Bi]
  let dailyTips: [Bi]
  let generalDuas: [Dua]
  let kandilWorship: [Bi]
  let kandilNote: Bi
  let kandiller: [KandilInfo]
  let countries: [Country]
  let girls: [NameSuggestion]
  let boys: [NameSuggestion]
  let surahs: [SurahInfo]
  let basmalaArabic: String
  let ramadanRanges: [RamadanRange]
  let iftarMenus: [RamadanMenu]
  let sahurMenus: [SahurMenu]
  let spiritualRamadan: [SpiritualItem]
  let spiritualGeneral: [SpiritualItem]
  let provinces: [Province]
  let dictionary: [String: String]
  let sahurMotivation: [String]
  let iftarMotivation: [String]

  private init() {
    let url = Bundle.main.url(forResource: "content", withExtension: "json")!
    let raw = try! JSONDecoder().decode(RawContent.self, from: Data(contentsOf: url))
    dailyMessages = raw.DailyMessages.messages.map { ($0[0], $0[1]) }
    let e = raw.EnglishContent
    english = EnglishContentData(
      dishes: e.dishes,
      spiritual: Dictionary(uniqueKeysWithValues: e.spiritual.compactMap { k, v in Int(k).map { ($0, v) } }),
      nameMeanings: e.nameMeanings,
      surahMeanings: e.surahMeanings,
      bismillah: e.BISMILLAH_T,
      surahTexts: Dictionary(uniqueKeysWithValues: e.surahTexts.compactMap { k, v in Int(k).map { ($0, v) } })
    )
    esma = raw.EsmaulHusna.names
    religiousBenefits = raw.FastingKnowledge.religiousBenefits
    medicalBenefits = raw.FastingKnowledge.medicalBenefits
    medicalNote = raw.FastingKnowledge.medicalNote
    iftarTips = raw.FastingKnowledge.iftarTips
    sahurTips = raw.FastingKnowledge.sahurTips
    dailyTips = raw.FastingKnowledge.dailyTips
    generalDuas = raw.KandilDualari.general
    kandilWorship = raw.KandilDualari.worship
    kandilNote = raw.KandilDualari.note
    kandiller = raw.KandilDualari.kandiller
    countries = raw.LocationsData.countries
    girls = raw.NameSuggestions.girls
    boys = raw.NameSuggestions.boys
    surahs = raw.QuranData.surahs
    basmalaArabic = raw.QuranData.BASMALA_AR
    ramadanRanges = raw.RamadanCalendar.ranges
    iftarMenus = raw.RamadanMenus.menus
    sahurMenus = raw.SahurMenus.menus
    spiritualRamadan = raw.SpiritualQuotes.list
    spiritualGeneral = raw.SpiritualQuotes.generalList
    provinces = raw.TurkeyProvinces.list
    dictionary = raw.I18nKt.dictionary
    sahurMotivation = raw.MotivationMessages.sahur
    iftarMotivation = raw.MotivationMessages.iftar
  }

  // MARK: - Günlük seçimler (Android ile aynı kurallar)

  func dailyMessage(_ date: Date = Date()) -> String {
    let m = dailyMessages[DateUtil.dayOfYear(date) % dailyMessages.count]
    return L10n.isEnglish ? m.1 : m.0
  }

  func spiritualToday(_ date: Date = Date()) -> SpiritualItem {
    if let d = RamadanCalendar.dayOfRamadan(date) { return spiritualForDay(d) }
    return localized(spiritualGeneral[DateUtil.dayOfYear(date) % spiritualGeneral.count])
  }

  func spiritualForDay(_ day: Int) -> SpiritualItem {
    let n = spiritualRamadan.count
    return localized(spiritualRamadan[((day - 1) % n + n) % n])
  }

  private func localized(_ item: SpiritualItem) -> SpiritualItem {
    guard L10n.isEnglish, let en = english.spiritual[item.day] else { return item }
    var i = item
    i.verseTurkish = en.verse; i.verseSource = en.verseSource
    i.hadithTurkish = en.hadith; i.hadithSource = en.hadithSource
    i.prayerTurkish = en.prayer; i.prayerSource = en.prayerSource
    i.esmaMeaning = en.esmaMeaning
    return i
  }

  func namesToday(_ date: Date = Date()) -> ([NameSuggestion], [NameSuggestion]) {
    let d = DateUtil.dayOfYear(date)
    func pick(_ list: [NameSuggestion]) -> [NameSuggestion] {
      (0..<3).map { list[(d * 3 + $0) % list.count] }.map { n in
        L10n.isEnglish ? NameSuggestion(name: n.name, meaning: english.nameMeanings[n.name] ?? n.meaning) : n
      }
    }
    return (pick(girls), pick(boys))
  }

  func iftarMenu(day: Int) -> RamadanMenu {
    let n = iftarMenus.count
    let m = iftarMenus[((day - 1) % n + n) % n]
    guard L10n.isEnglish else { return m }
    return RamadanMenu(day: m.day, soup: english.dish(m.soup), mainDish: english.dish(m.mainDish), sideDish: english.dish(m.sideDish), dessert: english.dish(m.dessert))
  }

  func sahurMenu(day: Int) -> SahurMenu {
    let n = sahurMenus.count
    return sahurMenus[((day - 1) % n + n) % n]
  }

  func tipForDay(_ day: Int) -> Bi {
    let n = dailyTips.count
    return dailyTips[((day - 1) % n + n) % n]
  }

  func kandilKey(for title: String) -> String? {
    if title.contains("Regaip") { return "regaib" }
    if title.contains("Miraç") { return "mirac" }
    if title.contains("Berat") { return "berat" }
    if title.contains("Kadir") { return "kadir" }
    if title.contains("Mevlid Kandili") { return "mevlid" }
    return nil
  }
}

extension Array {
  subscript(safe i: Int) -> Element? { indices.contains(i) ? self[i] : nil }
}

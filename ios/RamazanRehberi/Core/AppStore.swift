import Foundation
import SwiftUI
import WidgetKit

enum AppThemeMode: String, CaseIterable { case LIGHT, DARK, SYSTEM }
enum AppColorTheme: String, CaseIterable { case SARAY, IZNIK, ZUMRUT
  var title: String {
    switch self { case .SARAY: return t("Saray"); case .IZNIK: return t("İznik Çinisi"); case .ZUMRUT: return t("Zümrüt") }
  }
}

/// Uygulamanın kalıcı durumu (Android'deki AppPreferences + Zikir/Hatim/Kaza depoları).
final class AppData: ObservableObject {
  static let shared = AppData()
  private let d = UserDefaults.standard
  private let s = Shared.defaults

  @Published var onboardingCompleted: Bool { didSet { d.set(onboardingCompleted, forKey: "key_onboarding_completed") } }
  @Published var userName: String { didSet { d.set(userName, forKey: "key_user_name") } }
  @Published var country: String { didSet { s.set(country, forKey: "key_country"); locationChanged() } }
  @Published var province: String { didSet { s.set(province, forKey: "key_province"); locationChanged() } }
  @Published var district: String { didSet { s.set(district, forKey: "key_district"); locationChanged() } }
  @Published var themeMode: AppThemeMode { didSet { s.set(themeMode.rawValue, forKey: "key_theme_mode"); WidgetCenter.shared.reloadAllTimelines() } }
  @Published var colorTheme: AppColorTheme { didSet { s.set(colorTheme.rawValue, forKey: "key_color_theme"); WidgetCenter.shared.reloadAllTimelines() } }
  @Published var language: String { didSet { d.set(language, forKey: "key_language"); s.set(language, forKey: "key_language"); L10n.current = language; WidgetCenter.shared.reloadAllTimelines() } }

  // Bildirimler
  @Published var notificationsEnabled: Bool { didSet { d.set(notificationsEnabled, forKey: "key_notif_enabled"); reschedule() } }
  @Published var notifyImsak: Bool { didSet { d.set(notifyImsak, forKey: "key_notify_imsak"); reschedule() } }
  @Published var notifyOgle: Bool { didSet { d.set(notifyOgle, forKey: "key_notify_ogle"); reschedule() } }
  @Published var notifyIkindi: Bool { didSet { d.set(notifyIkindi, forKey: "key_notify_ikindi"); reschedule() } }
  @Published var notifyIftar: Bool { didSet { d.set(notifyIftar, forKey: "key_notify_iftar"); reschedule() } }
  @Published var notifyYatsi: Bool { didSet { d.set(notifyYatsi, forKey: "key_notify_yatsi"); reschedule() } }
  @Published var remindBeforeImsak: Bool { didSet { d.set(remindBeforeImsak, forKey: "key_remind_before_imsak"); reschedule() } }
  @Published var remindBeforeImsakMinutes: Int { didSet { d.set(remindBeforeImsakMinutes, forKey: "key_remind_before_imsak_min"); reschedule() } }
  @Published var remindBeforeIftar: Bool { didSet { d.set(remindBeforeIftar, forKey: "key_remind_before_iftar"); reschedule() } }
  @Published var remindBeforeIftarMinutes: Int { didSet { d.set(remindBeforeIftarMinutes, forKey: "key_remind_before_iftar_min"); reschedule() } }
  @Published var ezanSound: Bool { didSet { d.set(ezanSound, forKey: "key_ezan_sound"); reschedule() } }

  // Oruç
  @Published var fastingDates: Set<String> { didSet { s.set(Array(fastingDates), forKey: "key_fasting_dates"); reschedule(); WidgetCenter.shared.reloadAllTimelines() } }
  @Published var fastingStatus: [String: String] { didSet { d.set(fastingStatus, forKey: "key_fasting_status") } }
  @Published var notes: [String] { didSet { d.set(notes, forKey: "key_ramadan_notes") } }

  // Zikirmatik
  @Published var zikirCount: Int { didSet { d.set(zikirCount, forKey: "zikir_count") } }
  @Published var zikirMode: String { didSet { d.set(zikirMode, forKey: "zikir_mode") } }
  @Published var zikirTesbihatStep: Int { didSet { d.set(zikirTesbihatStep, forKey: "zikir_tesbihat_step") } }
  @Published var zikirTotal: Int { didSet { d.set(zikirTotal, forKey: "zikir_total") } }
  @Published var zikirSaved: [String] { didSet { d.set(zikirSaved, forKey: "zikir_saved") } }

  // Hatim
  @Published var readJuz: Set<Int> { didSet { d.set(Array(readJuz), forKey: "hatim_juz") } }
  @Published var readSurahs: Set<Int> { didSet { d.set(Array(readSurahs), forKey: "hatim_surah") } }
  @Published var hatimCompleted: Int { didSet { d.set(hatimCompleted, forKey: "hatim_completed") } }
  @Published var lastPage: Int { didSet { d.set(lastPage, forKey: "hatim_last_page") } }
  @Published var bookmarkPage: Int { didSet { d.set(bookmarkPage, forKey: "hatim_bookmark_page") } }
  @Published var readerFontStep: Int { didSet { d.set(readerFontStep, forKey: "hatim_font_step") } }

  // Kaza
  @Published var kazaRemaining: [String: Int] { didSet { d.set(kazaRemaining, forKey: "kaza_kalan") } }
  @Published var kazaPrayed: [String: Int] { didSet { d.set(kazaPrayed, forKey: "kaza_kilinan") } }

  // Premium
  @Published var isPremium: Bool { didSet { d.set(isPremium, forKey: "key_premium") } }

  private init() {
    let d = UserDefaults.standard, s = Shared.defaults
    func b(_ k: String, _ def: Bool) -> Bool { d.object(forKey: k) == nil ? def : d.bool(forKey: k) }
    func i(_ k: String, _ def: Int) -> Int { d.object(forKey: k) == nil ? def : d.integer(forKey: k) }
    onboardingCompleted = d.bool(forKey: "key_onboarding_completed")
    userName = d.string(forKey: "key_user_name") ?? ""
    country = s.string(forKey: "key_country") ?? "Türkiye"
    province = s.string(forKey: "key_province") ?? "İstanbul"
    district = s.string(forKey: "key_district") ?? "Fatih"
    themeMode = AppThemeMode(rawValue: s.string(forKey: "key_theme_mode") ?? "") ?? .SYSTEM
    colorTheme = AppColorTheme(rawValue: s.string(forKey: "key_color_theme") ?? "") ?? .SARAY
    let lang = d.string(forKey: "key_language") ?? (Locale.preferredLanguages.first?.hasPrefix("tr") == false ? "en" : "tr")
    language = lang
    L10n.current = lang
    notificationsEnabled = d.bool(forKey: "key_notif_enabled")
    notifyImsak = b("key_notify_imsak", true)
    notifyOgle = b("key_notify_ogle", true)
    notifyIkindi = b("key_notify_ikindi", true)
    notifyIftar = b("key_notify_iftar", true)
    notifyYatsi = b("key_notify_yatsi", true)
    remindBeforeImsak = b("key_remind_before_imsak", false)
    remindBeforeImsakMinutes = i("key_remind_before_imsak_min", 45)
    remindBeforeIftar = b("key_remind_before_iftar", true)
    remindBeforeIftarMinutes = i("key_remind_before_iftar_min", 15)
    ezanSound = b("key_ezan_sound", true)
    fastingDates = Set((s.array(forKey: "key_fasting_dates") as? [String]) ?? [])
    fastingStatus = (d.dictionary(forKey: "key_fasting_status") as? [String: String]) ?? [:]
    notes = (d.array(forKey: "key_ramadan_notes") as? [String]) ?? []
    zikirCount = d.integer(forKey: "zikir_count")
    zikirMode = d.string(forKey: "zikir_mode") ?? "33"
    zikirTesbihatStep = d.integer(forKey: "zikir_tesbihat_step")
    zikirTotal = d.integer(forKey: "zikir_total")
    zikirSaved = (d.array(forKey: "zikir_saved") as? [String]) ?? []
    readJuz = Set((d.array(forKey: "hatim_juz") as? [Int]) ?? [])
    readSurahs = Set((d.array(forKey: "hatim_surah") as? [Int]) ?? [])
    hatimCompleted = d.integer(forKey: "hatim_completed")
    lastPage = i("hatim_last_page", 1)
    bookmarkPage = d.integer(forKey: "hatim_bookmark_page")
    readerFontStep = i("hatim_font_step", 1)
    kazaRemaining = (d.dictionary(forKey: "kaza_kalan") as? [String: Int]) ?? [:]
    kazaPrayed = (d.dictionary(forKey: "kaza_kilinan") as? [String: Int]) ?? [:]
    isPremium = d.bool(forKey: "key_premium")
    if s.string(forKey: "key_language") == nil { s.set(lang, forKey: "key_language") }
  }

  var locationLabel: String { "\(province) / \(district)" }

  private func locationChanged() {
    PrayerService.shared.reload()
    WidgetCenter.shared.reloadAllTimelines()
  }

  private var rescheduleWork: DispatchWorkItem?
  func reschedule() {
    rescheduleWork?.cancel()
    let w = DispatchWorkItem { NotificationScheduler.rescheduleAll() }
    rescheduleWork = w
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.6, execute: w)
  }

  // MARK: Oruç
  func isFastingDay(_ key: String) -> Bool { RamadanCalendar.isRamadanDay(key) || fastingDates.contains(key) }

  @discardableResult
  func toggleFastingDate(_ key: String) -> Bool {
    if fastingDates.contains(key) {
      fastingDates.remove(key); fastingStatus[key] = nil; return false
    }
    fastingDates.insert(key); fastingStatus[key] = "fasted"; return true
  }

  func setFastingStatus(_ key: String, _ status: String) {
    fastingStatus[key] = status
    if status == "fasted" { fastingDates.insert(key) } else { fastingDates.remove(key) }
  }

  // MARK: Zikir
  func saveZikir(_ entry: String) { zikirSaved = Array(([entry] + zikirSaved).prefix(20)) }

  // MARK: Hatim
  func toggleJuz(_ j: Int) { if readJuz.contains(j) { readJuz.remove(j) } else { readJuz.insert(j) } }
  func toggleSurah(_ n: Int) { if readSurahs.contains(n) { readSurahs.remove(n) } else { readSurahs.insert(n) } }
  func completeHatim() { hatimCompleted += 1; readJuz = []; readSurahs = [] }

  // MARK: Kaza
  func kazaRemaining(_ v: KazaVakit) -> Int { kazaRemaining[v.rawValue] ?? 0 }
  func kazaPrayed(_ v: KazaVakit) -> Int { kazaPrayed[v.rawValue] ?? 0 }
  var kazaTotalRemaining: Int { KazaVakit.allCases.reduce(0) { $0 + kazaRemaining($1) } }
  var kazaTotalPrayed: Int { KazaVakit.allCases.reduce(0) { $0 + kazaPrayed($1) } }
  func kazaMarkPrayed(_ v: KazaVakit) {
    let r = kazaRemaining(v); guard r > 0 else { return }
    kazaRemaining[v.rawValue] = r - 1
    kazaPrayed[v.rawValue] = kazaPrayed(v) + 1
  }
  func kazaAdd(_ v: KazaVakit, _ n: Int) { kazaRemaining[v.rawValue] = max(0, kazaRemaining(v) + n) }
  func kazaAddDays(_ days: Int, includeVitir: Bool) {
    for v in KazaVakit.allCases where v != .vitir || includeVitir { kazaRemaining[v.rawValue] = kazaRemaining(v) + days }
  }
  func kazaReset() { kazaRemaining = [:]; kazaPrayed = [:] }
}

enum KazaVakit: String, CaseIterable, Identifiable {
  case sabah, ogle, ikindi, aksam, yatsi, vitir
  var id: String { rawValue }
  var name: String {
    let tr = ["sabah": "Sabah", "ogle": "Öğle", "ikindi": "İkindi", "aksam": "Akşam", "yatsi": "Yatsı", "vitir": "Vitir"][rawValue]!
    let en = ["sabah": "Fajr", "ogle": "Dhuhr", "ikindi": "Asr", "aksam": "Maghrib", "yatsi": "Isha", "vitir": "Witr"][rawValue]!
    return L10n.isEnglish ? en : tr
  }
  var arabic: String { ["sabah": "الصبح", "ogle": "الظهر", "ikindi": "العصر", "aksam": "المغرب", "yatsi": "العشاء", "vitir": "الوتر"][rawValue]! }
}

import Foundation
import AppIntents
import WidgetKit

/// "Kıldım": vakti girmiş son farz namazı (ya da verilen vakti) bugünün takibine işler.
/// Widget düğmesi (iOS 17), Siri / Kestirmeler ve uygulama aynı kaydı (App Group) kullanır.
@available(iOS 16.0, *)
struct MarkPrayedIntent: AppIntent {
  static var title: LocalizedStringResource = "Namazı kıldım olarak işaretle"
  static var description = IntentDescription("Vakti girmiş son farz namazı Ezan & Vakit takibinde kılındı olarak işaretler.")
  static var openAppWhenRun: Bool = false

  /// FarzVakit sırası (0 Sabah … 4 Yatsı); -1: şu anki vakit
  @Parameter(title: "Vakit", default: -1)
  var vakit: Int

  init() {}
  init(vakit: Int) { self.vakit = vakit }

  func perform() async throws -> some IntentResult & ProvidesDialog {
    let now = Date()
    L10n.current = Shared.defaults.string(forKey: "key_language") ?? "tr"
    let en = L10n.isEnglish
    let times = PrayerCache.timesFor(date: now)
    // Verilen vakit ya da vakti girmiş son farz; imsaktan önce dünün yatsısı
    let target = PrayerLog.markTarget(times, now: now)
    let v = FarzVakit(rawValue: vakit) ?? target.vakit
    let key = FarzVakit(rawValue: vakit) == nil ? target.dateKey : PrayerLog.dateKey(for: v, times: times, now: now)
    PrayerLog.set(v, on: key, prayed: true)
    WidgetCenter.shared.reloadAllTimelines()
    #if !WIDGET
    LiveActivityManager.refresh()
    #endif
    let n = PrayerLog.count(on: key)
    let text = en ? "\(v.name) marked as prayed. Today \(n)/5." : "\(v.name) kılındı olarak işaretlendi. Bugün \(n)/5."
    return .result(dialog: IntentDialog(stringLiteral: text))
  }
}

/// "Sıradaki vakit": sıradaki vakte kalan süre (Diyanet vakitleriyle).
@available(iOS 16.0, *)
struct NextPrayerIntent: AppIntent {
  static var title: LocalizedStringResource = "Sıradaki vakit"
  static var description = IntentDescription("Sıradaki namaz vaktine ne kadar kaldığını söyler.")
  static var openAppWhenRun: Bool = false

  init() {}

  func perform() async throws -> some IntentResult & ProvidesDialog {
    let now = Date()
    L10n.current = Shared.defaults.string(forKey: "key_language") ?? "tr"
    let en = L10n.isEnglish
    let fasting = Set((Shared.defaults.array(forKey: "key_fasting_dates") as? [String]) ?? [])
    let cd = PrayerTimeUtils.countdown(PrayerCache.timesFor(date: now), fastingDates: fasting, now: now)
    let name = cd.targetTimeName.components(separatedBy: " (").first ?? cd.targetTimeName
    let text: String
    if en {
      text = cd.hours > 0 ? "\(cd.hours) h \(cd.minutes) min until \(name) (\(cd.targetTimeString))."
                          : "\(cd.minutes) min until \(name) (\(cd.targetTimeString))."
    } else {
      text = cd.hours > 0 ? "\(name) vaktine \(cd.hours) saat \(cd.minutes) dakika var (\(cd.targetTimeString))."
                          : "\(name) vaktine \(cd.minutes) dakika var (\(cd.targetTimeString))."
    }
    return .result(dialog: IntentDialog(stringLiteral: text))
  }
}

import Foundation
#if canImport(ActivityKit)
import ActivityKit
#endif

/// Kilit ekranı / Dynamic Island geri sayımını başlatır, günceller, kapatır.
/// Uygulama açıldığında, vakitler/işaretler değiştiğinde ve arka plan yenilemesinde çağrılır.
enum LiveActivityManager {
  static func refresh() {
    #if canImport(ActivityKit)
    if #available(iOS 16.2, *) { Task { await refreshAsync() } }
    #endif
  }
}

#if canImport(ActivityKit)
@available(iOS 16.2, *)
extension LiveActivityManager {
  /// Sıradaki vakit (Güneş dahil, uygulamadaki geri sayımla aynı) ve bir önceki vakit.
  static func state(now: Date) -> PrayerActivityAttributes.ContentState {
    let names = PrayerTimes.names.map { t($0) }
    let today = PrayerCache.timesFor(date: now)
    let tomorrowDay = DateUtil.addDays(1, to: now)
    let yesterdayDay = DateUtil.addDays(-1, to: now)
    let tomorrow = PrayerCache.timesFor(date: tomorrowDay)
    let yesterday = PrayerCache.timesFor(date: yesterdayDay)

    var points: [(name: String, time: String, date: Date)] = []
    for (i, s) in today.all.enumerated() { points.append((names[i], s, PrayerTimeUtils.date(s, on: now))) }
    points.append((names[0], tomorrow.imsak, PrayerTimeUtils.date(tomorrow.imsak, on: tomorrowDay)))
    let before = (name: names[5], time: yesterday.yatsi, date: PrayerTimeUtils.date(yesterday.yatsi, on: yesterdayDay))

    let i = points.firstIndex { $0.date > now } ?? (points.count - 1)
    let next = points[i]
    let prev = i > 0 ? points[i - 1] : before
    let prayed = PrayerLog.prayed(on: DateUtil.dateKey(now)).map(\.rawValue).sorted()
    return PrayerActivityAttributes.ContentState(
      name: next.name, time: next.time, start: prev.date, target: next.date,
      prayedCount: prayed.count, prayed: prayed, previousLabel: "\(prev.name) \(prev.time)")
  }

  static func refreshAsync() async {
    let enabled = AppData.shared.liveActivityEnabled && ActivityAuthorizationInfo().areActivitiesEnabled
    let current = Activity<PrayerActivityAttributes>.activities
    guard enabled else {
      for a in current { await a.end(nil, dismissalPolicy: .immediate) }
      return
    }
    let st = state(now: Date())
    // Hedef saat geçince "eski" sayılır; widget bu durumda "Vakit girdi" gösterir.
    let content = ActivityContent(state: st, staleDate: st.target.addingTimeInterval(60))
    if let a = current.first {
      for extra in current.dropFirst() { await extra.end(nil, dismissalPolicy: .immediate) }
      await a.update(content)
    } else {
      _ = try? Activity.request(attributes: PrayerActivityAttributes(place: AppData.shared.locationLabel),
                                content: content, pushType: nil)
    }
  }
}
#endif

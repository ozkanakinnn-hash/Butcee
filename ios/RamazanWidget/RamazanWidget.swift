import WidgetKit
import SwiftUI

struct PrayerEntry: TimelineEntry {
  let date: Date
  let times: PrayerTimes
  let countdown: CountdownResult
  let dark: Bool
  let place: String
}

struct PrayerProvider: TimelineProvider {
  func placeholder(in context: Context) -> PrayerEntry { entry(at: Date()) }
  func getSnapshot(in context: Context, completion: @escaping (PrayerEntry) -> Void) { completion(entry(at: Date())) }

  func getTimeline(in context: Context, completion: @escaping (Timeline<PrayerEntry>) -> Void) {
    let now = Date()
    var entries = [entry(at: now)]
    // Her vakit geçişinde yeni giriş (gün boyu + ertesi gün imsak)
    let today = PrayerCache.timesFor(date: now)
    let tomorrow = PrayerCache.timesFor(date: DateUtil.addDays(1, to: now))
    let changes = today.all.map { PrayerTimeUtils.date($0, on: now) } + [PrayerTimeUtils.date(tomorrow.imsak, on: DateUtil.addDays(1, to: now))]
    for c in changes where c > now { entries.append(entry(at: c.addingTimeInterval(1))) }
    let midnight = DateUtil.startOfDay(DateUtil.addDays(1, to: now)).addingTimeInterval(5)
    entries.append(entry(at: midnight))
    entries.sort { $0.date < $1.date }
    completion(Timeline(entries: entries, policy: .after(midnight.addingTimeInterval(60))))
  }

  private func entry(at d: Date) -> PrayerEntry {
    let s = Shared.defaults
    L10n.current = s.string(forKey: "key_language") ?? "tr"
    let fasting = Set((s.array(forKey: "key_fasting_dates") as? [String]) ?? [])
    let times = PrayerCache.timesFor(date: d)
    let mode = s.string(forKey: "key_theme_mode") ?? "SYSTEM"
    let place = "\(s.string(forKey: "key_province") ?? "İstanbul") / \(s.string(forKey: "key_district") ?? "Fatih")"
    return PrayerEntry(date: d, times: times, countdown: PrayerTimeUtils.countdown(times, fastingDates: fasting, now: d), dark: mode != "LIGHT", place: place)
  }
}

private let gold = Color(red: 0.90, green: 0.72, blue: 0.41)
private let night = LinearGradient(colors: [Color(red: 0.09, green: 0.12, blue: 0.22), Color(red: 0.03, green: 0.04, blue: 0.08)], startPoint: .top, endPoint: .bottom)

extension View {
  @ViewBuilder func widgetBg() -> some View {
    if #available(iOS 17.0, *) { containerBackground(for: .widget) { night } } else { background(night) }
  }
}

struct NextPrayerView: View {
  @Environment(\.widgetFamily) var family
  let e: PrayerEntry
  var body: some View {
    switch family {
    case .accessoryRectangular:
      VStack(alignment: .leading, spacing: 1) {
        Text(e.countdown.targetTimeName).font(.headline)
        Text(e.countdown.targetDate, style: .timer).font(.title3.monospacedDigit())
        Text(e.countdown.targetTimeString).font(.caption)
      }
    case .accessoryInline:
      Text("\(e.countdown.targetTimeName) \(e.countdown.targetTimeString)")
    default:
      VStack(spacing: 4) {
        Text(t("Ramazan Rehberi")).font(.system(size: 11, weight: .semibold)).foregroundStyle(gold).frame(maxWidth: .infinity, alignment: .leading)
        Spacer(minLength: 0)
        Text(L10n.isEnglish ? "Next Prayer" : "Sonraki Namaz").font(.system(size: 11)).foregroundStyle(.white.opacity(0.75))
        Text(e.countdown.targetTimeName).font(.system(size: 20, weight: .bold)).foregroundStyle(.white).lineLimit(1).minimumScaleFactor(0.6)
        Text(e.countdown.targetTimeString).font(.system(size: 13, weight: .bold)).foregroundStyle(gold)
          .padding(.horizontal, 8).padding(.vertical, 2).overlay(Capsule().stroke(gold, lineWidth: 1))
        Text(e.countdown.targetDate, style: .timer).font(.system(size: 26, weight: .bold).monospacedDigit()).foregroundStyle(.white)
          .multilineTextAlignment(.center)
        Spacer(minLength: 0)
      }
      .padding(4)
    }
  }
}

struct FiveTimesView: View {
  let e: PrayerEntry
  var body: some View {
    let c = DateUtil.cal.dateComponents([.hour, .minute], from: e.date)
    let m = c.hour! * 60 + c.minute!
    let mins = e.times.all.map { PrayerTimeUtils.minutes($0) }
    let cur = mins.lastIndex { m >= $0 } ?? 5
    VStack(spacing: 6) {
      HStack {
        Text(e.place).font(.system(size: 12, weight: .semibold)).foregroundStyle(gold).lineLimit(1)
        Spacer()
        Text(e.countdown.title + " ").font(.system(size: 11)).foregroundStyle(.white.opacity(0.7)).lineLimit(1)
        Text(e.countdown.targetDate, style: .timer).font(.system(size: 12, weight: .bold).monospacedDigit()).foregroundStyle(.white).frame(width: 64)
      }
      HStack(spacing: 4) {
        ForEach(0..<6, id: \.self) { i in
          VStack(spacing: 4) {
            Text(t(PrayerTimes.names[i])).font(.system(size: 11, weight: .semibold)).lineLimit(1).minimumScaleFactor(0.7)
            Text(e.times.all[i]).font(.system(size: 14, weight: .bold).monospacedDigit())
          }
          .foregroundStyle(i == cur ? Color.black : Color.white)
          .frame(maxWidth: .infinity).padding(.vertical, 8)
          .background(RoundedRectangle(cornerRadius: 10).fill(i == cur ? gold : Color.white.opacity(0.08)))
        }
      }
    }
    .padding(4)
  }
}

struct SpiritualEntry: TimelineEntry { let date: Date; let item: SpiritualItem }

struct SpiritualProvider: TimelineProvider {
  func placeholder(in context: Context) -> SpiritualEntry { make(Date()) }
  func getSnapshot(in context: Context, completion: @escaping (SpiritualEntry) -> Void) { completion(make(Date())) }
  func getTimeline(in context: Context, completion: @escaping (Timeline<SpiritualEntry>) -> Void) {
    let next = DateUtil.startOfDay(DateUtil.addDays(1, to: Date())).addingTimeInterval(60)
    completion(Timeline(entries: [make(Date())], policy: .after(next)))
  }
  private func make(_ d: Date) -> SpiritualEntry {
    L10n.current = Shared.defaults.string(forKey: "key_language") ?? "tr"
    return SpiritualEntry(date: d, item: AppContent.shared.spiritualToday(d))
  }
}

struct SpiritualWidgetView: View {
  let e: SpiritualEntry
  var body: some View {
    VStack(spacing: 6) {
      Text(t("Günün Manevi İklimi")).font(.system(size: 11, weight: .semibold)).foregroundStyle(gold)
      if !e.item.verseArabic.isEmpty {
        Text(e.item.verseArabic).font(.custom("Amiri-Regular", size: 18)).foregroundStyle(gold).lineLimit(2).minimumScaleFactor(0.6)
      }
      Text("“\(e.item.verseTurkish)”").font(.system(size: 13)).italic().foregroundStyle(.white).multilineTextAlignment(.center).lineLimit(4).minimumScaleFactor(0.7)
      Text("— \(e.item.verseSource)").font(.system(size: 10)).foregroundStyle(.white.opacity(0.7))
    }
    .padding(6)
  }
}

struct NextPrayerWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "NextPrayer", provider: PrayerProvider()) { e in NextPrayerView(e: e).widgetBg() }
      .configurationDisplayName("Sıradaki Namaz")
      .description("Sıradaki vakit ve geri sayım / Next prayer & countdown")
      .supportedFamilies([.systemSmall, .accessoryRectangular, .accessoryInline])
  }
}

struct FiveTimesWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "FiveTimes", provider: PrayerProvider()) { e in FiveTimesView(e: e).widgetBg() }
      .configurationDisplayName("Günün Namaz Vakitleri")
      .description("Bugünün tüm vakitleri / Today's prayer times")
      .supportedFamilies([.systemMedium])
  }
}

struct SpiritualWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "Spiritual", provider: SpiritualProvider()) { e in SpiritualWidgetView(e: e).widgetBg() }
      .configurationDisplayName("Günün Manevi İklimi")
      .description("Günün ayeti / Verse of the day")
      .supportedFamilies([.systemMedium, .systemSmall])
  }
}

@main
struct RamazanWidgetBundle: WidgetBundle {
  var body: some Widget {
    NextPrayerWidget()
    FiveTimesWidget()
    SpiritualWidget()
  }
}

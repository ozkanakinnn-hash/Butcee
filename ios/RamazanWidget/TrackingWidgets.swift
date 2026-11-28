import WidgetKit
import SwiftUI
import AppIntents
#if canImport(ActivityKit)
import ActivityKit
#endif

// MARK: - "Kıldım" widget'ı (iOS 17'de düğme uygulamayı açmadan işaretler)

struct MarkPrayedView: View {
  @Environment(\.widgetFamily) var family
  @Environment(\.colorScheme) var scheme
  let e: PrayerEntry

  var body: some View {
    let p = WPalette.current(scheme)
    let en = L10n.isEnglish
    let prayed = PrayerLog.prayed(on: DateUtil.dateKey(e.date))
    let current = PrayerLog.currentVakit(e.times, now: e.date)
    let done = current.map { prayed.contains($0) } ?? false
    switch family {
    case .accessoryCircular:
      ZStack {
        PrayerRing(prayed: prayed, current: nil, color: .primary, track: .secondary.opacity(0.35), lineWidth: 4, gapDegrees: 20)
        Text("\(prayed.count)").font(.system(size: 16, weight: .heavy))
      }
      .widgetBackground(p)
    default:
      VStack(alignment: .leading, spacing: 6) {
        HStack(spacing: 8) {
          PrayerRing(prayed: prayed, current: done ? nil : current, color: p.accent, track: p.line.opacity(0.45),
                     lineWidth: 5, gapDegrees: 20)
            .frame(width: 46, height: 46)
          VStack(alignment: .leading, spacing: 0) {
            Text((current?.name ?? (en ? "Night" : "Gece")).uppercased())
              .font(.system(size: 10, weight: .heavy)).tracking(1).foregroundStyle(p.muted)
            Text(current.map { e.times.all[$0.timeIndex] } ?? e.times.imsak)
              .font(.system(size: 19, weight: .heavy).monospacedDigit()).foregroundStyle(p.accent)
          }
        }
        Spacer(minLength: 0)
        Text("\(prayed.count)/5 " + (en ? "prayed today" : "bugün kılındı"))
          .font(.system(size: 12, weight: .semibold)).foregroundStyle(p.ink).lineLimit(1).minimumScaleFactor(0.7)
        markButton(p, current: current, done: done, en: en)
      }
      .widgetBackground(p)
    }
  }

  @ViewBuilder
  private func markButton(_ p: WPalette, current: FarzVakit?, done: Bool, en: Bool) -> some View {
    let label = HStack(spacing: 4) {
      Text(done ? (en ? "Prayed" : "Kılındı") : (en ? "Prayed it" : "Kıldım"))
      Image(systemName: "checkmark")
    }
    .font(.system(size: 13, weight: .heavy))
    .foregroundStyle(p.dark ? Color.black : Color.white)
    .frame(maxWidth: .infinity).padding(.vertical, 7)
    .background(Capsule().fill(p.accent.opacity(done || current == nil ? 0.45 : 1)))

    if #available(iOS 17.0, *) {
      if let c = current, !done {
        Button(intent: MarkPrayedIntent(vakit: c.rawValue)) { label }.buttonStyle(.plain)
      } else {
        label
      }
    } else {
      label  // iOS 16: dokununca uygulama açılır
    }
  }
}

struct MarkPrayedWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "MarkPrayed", provider: PrayerProvider()) { e in MarkPrayedView(e: e) }
      .configurationDisplayName("Kıldım")
      .description("Bugün kıldığınız vakitler; vakit girince tek dokunuşla işaretleyin.")
      .supportedFamilies([.systemSmall, .accessoryCircular])
  }
}

// MARK: - Canlı Etkinlik: kilit ekranı ve Dynamic Island

#if canImport(ActivityKit)
private let liveGold = Color(.sRGB, red: 0xE5 / 255, green: 0xB8 / 255, blue: 0x69 / 255, opacity: 1)
private let liveInk = Color(.sRGB, red: 0xF5 / 255, green: 0xEE / 255, blue: 0xDD / 255, opacity: 1)
private let liveMuted = Color(.sRGB, red: 0xF5 / 255, green: 0xEE / 255, blue: 0xDD / 255, opacity: 0.62)
private let liveTrack = Color(.sRGB, red: 0x3A / 255, green: 0x34 / 255, blue: 0x26 / 255, opacity: 1)
private let liveBg = Color(.sRGB, red: 0x17 / 255, green: 0x15 / 255, blue: 0x0F / 255, opacity: 0.92)

@available(iOS 16.1, *)
private func liveRing(_ s: PrayerActivityAttributes.ContentState, width: CGFloat) -> some View {
  PrayerRing(prayed: Set(s.prayed.compactMap(FarzVakit.init(rawValue:))), current: nil,
             color: liveGold, track: liveTrack, lineWidth: width, gapDegrees: 20)
}

@available(iOS 16.1, *)
struct PrayerLiveLockView: View {
  let context: ActivityViewContext<PrayerActivityAttributes>

  var body: some View {
    let s = context.state
    var entered = Date() >= s.target
    if #available(iOS 16.2, *), context.isStale { entered = true }
    let en = Shared.defaults.string(forKey: "key_language") == "en"
    VStack(spacing: 10) {
      HStack(spacing: 12) {
        liveRing(s, width: 5).frame(width: 42, height: 42)
        VStack(alignment: .leading, spacing: 1) {
          Text("Ezan & Vakit · " + (entered ? (en ? "now" : "vakit girdi") : (en ? "next" : "sıradaki")))
            .font(.system(size: 11, weight: .bold)).foregroundStyle(liveMuted)
          Text("\(s.name) \(s.time)").font(.system(size: 19, weight: .heavy)).foregroundStyle(liveInk)
        }
        Spacer()
        if entered {
          Image(systemName: "bell.fill").font(.system(size: 24)).foregroundStyle(liveGold)
        } else {
          Text(timerInterval: s.start...s.target, countsDown: true)
            .font(.system(size: 28, weight: .heavy).monospacedDigit()).foregroundStyle(liveGold)
            .multilineTextAlignment(.trailing).frame(maxWidth: 120)
        }
      }
      ProgressView(timerInterval: s.start...s.target, countsDown: false) { EmptyView() } currentValueLabel: { EmptyView() }
        .tint(liveGold)
      HStack {
        Text(s.previousLabel)
        Spacer()
        Text(en ? "Today \(s.prayedCount)/5" : "Bugün \(s.prayedCount)/5 kılındı")
        Spacer()
        Text(context.attributes.place).lineLimit(1)
      }
      .font(.system(size: 11, weight: .semibold)).foregroundStyle(liveMuted)
    }
    .padding(16)
    .activityBackgroundTint(liveBg)
    .activitySystemActionForegroundColor(liveGold)
  }
}

@available(iOS 16.1, *)
struct PrayerLiveActivityWidget: Widget {
  var body: some WidgetConfiguration {
    ActivityConfiguration(for: PrayerActivityAttributes.self) { context in
      PrayerLiveLockView(context: context)
    } dynamicIsland: { context in
      let s = context.state
      return DynamicIsland {
        DynamicIslandExpandedRegion(.leading) {
          liveRing(s, width: 5).frame(width: 40, height: 40).padding(.leading, 4)
        }
        DynamicIslandExpandedRegion(.center) {
          Text("\(s.name) \(s.time)").font(.system(size: 17, weight: .heavy)).foregroundStyle(liveInk)
        }
        DynamicIslandExpandedRegion(.trailing) {
          Text(timerInterval: s.start...s.target, countsDown: true)
            .font(.system(size: 20, weight: .heavy).monospacedDigit()).foregroundStyle(liveGold)
            .multilineTextAlignment(.trailing).frame(maxWidth: 90)
        }
        DynamicIslandExpandedRegion(.bottom) {
          ProgressView(timerInterval: s.start...s.target, countsDown: false) { EmptyView() } currentValueLabel: { EmptyView() }
            .tint(liveGold)
        }
      } compactLeading: {
        liveRing(s, width: 3).frame(width: 18, height: 18)
      } compactTrailing: {
        Text(timerInterval: s.start...s.target, countsDown: true)
          .font(.system(size: 14, weight: .bold).monospacedDigit()).foregroundStyle(liveGold)
          .frame(maxWidth: 56)
      } minimal: {
        liveRing(s, width: 3).frame(width: 18, height: 18)
      }
      .keylineTint(liveGold)
    }
  }
}
#endif

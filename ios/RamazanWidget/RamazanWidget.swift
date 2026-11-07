import WidgetKit
import SwiftUI

// MARK: - Veri

struct PrayerEntry: TimelineEntry {
  let date: Date
  let times: PrayerTimes
  let countdown: CountdownResult
  let place: String
  let fastingDates: Set<String>
  let periodProgress: Double
}

struct PrayerProvider: TimelineProvider {
  func placeholder(in context: Context) -> PrayerEntry { entry(at: Date()) }
  func getSnapshot(in context: Context, completion: @escaping (PrayerEntry) -> Void) { completion(entry(at: Date())) }

  func getTimeline(in context: Context, completion: @escaping (Timeline<PrayerEntry>) -> Void) {
    let now = Date()
    var entries: [PrayerEntry] = []
    // 15 dakikada bir giriş (halka / çubuk ilerlemesi) + her vakit geçişi
    var t = now
    let end = DateUtil.startOfDay(DateUtil.addDays(1, to: now)).addingTimeInterval(5)
    while t < end && entries.count < 100 { entries.append(entry(at: t)); t = t.addingTimeInterval(15 * 60) }
    let today = PrayerCache.timesFor(date: now)
    for s in today.all {
      let d = PrayerTimeUtils.date(s, on: now).addingTimeInterval(1)
      if d > now { entries.append(entry(at: d)) }
    }
    entries.sort { $0.date < $1.date }
    completion(Timeline(entries: entries, policy: .after(end)))
  }

  private func entry(at d: Date) -> PrayerEntry {
    let s = Shared.defaults
    L10n.current = s.string(forKey: "key_language") ?? "tr"
    let fasting = Set((s.array(forKey: "key_fasting_dates") as? [String]) ?? [])
    let times = PrayerCache.timesFor(date: d)
    let cd = PrayerTimeUtils.countdown(times, fastingDates: fasting, now: d)
    let place = "\(s.string(forKey: "key_province") ?? "İstanbul") / \(s.string(forKey: "key_district") ?? "Fatih")"
    return PrayerEntry(date: d, times: times, countdown: cd, place: place, fastingDates: fasting, periodProgress: cd.progress)
  }
}

// MARK: - Tema

struct WPalette {
  let top: Color, bottom: Color, accent: Color, accentLight: Color, ink: Color, muted: Color, line: Color, dark: Bool

  static func current(_ scheme: ColorScheme) -> WPalette {
    let s = Shared.defaults
    let mode = s.string(forKey: "key_theme_mode") ?? "SYSTEM"
    let dark = mode == "DARK" || (mode == "SYSTEM" && scheme == .dark)
    let theme = s.string(forKey: "key_color_theme") ?? "SARAY"
    func c(_ h: UInt32, _ a: Double = 1) -> Color {
      Color(.sRGB, red: Double((h >> 16) & 0xFF) / 255, green: Double((h >> 8) & 0xFF) / 255, blue: Double(h & 0xFF) / 255, opacity: a)
    }
    if !dark {
      switch theme {
      case "IZNIK": return WPalette(top: c(0xFAFBFE), bottom: c(0xE3EAF2), accent: c(0x1A3F7A), accentLight: c(0x2A8C9C), ink: c(0x1B2230), muted: c(0x1B2230, 0.62), line: c(0x1A3F7A, 0.4), dark: false)
      case "ZUMRUT": return WPalette(top: c(0xF7FBF7), bottom: c(0xDFEAE2), accent: c(0x1E6B4F), accentLight: c(0xA9831B), ink: c(0x1A241F), muted: c(0x1A241F, 0.62), line: c(0x1E6B4F, 0.4), dark: false)
      default: return WPalette(top: c(0xFFFCF6), bottom: c(0xF3EAD8), accent: c(0x9E6B20), accentLight: c(0xC98A2C), ink: c(0x2B2823), muted: c(0x2B2823, 0.62), line: c(0x9E6B20, 0.4), dark: false)
      }
    }
    switch theme {
    case "IZNIK": return WPalette(top: c(0x0F1A33), bottom: c(0x04070F), accent: c(0x8DB1EA), accentLight: c(0xC9DBFA), ink: c(0xEEF3FB), muted: c(0xEEF3FB, 0.62), line: c(0x8DB1EA, 0.45), dark: true)
    case "ZUMRUT": return WPalette(top: c(0x0C2219), bottom: c(0x030806), accent: c(0x6FD3A2), accentLight: c(0xE5C55A), ink: c(0xEEF6F0), muted: c(0xEEF6F0, 0.62), line: c(0x6FD3A2, 0.45), dark: true)
    default: return WPalette(top: c(0x1D170E), bottom: c(0x060504), accent: c(0xE5B869), accentLight: c(0xF8DEA0), ink: c(0xF5EEDD), muted: c(0xF5EEDD, 0.62), line: c(0xE5B869, 0.45), dark: true)
    }
  }
}

// MARK: - Ortak süslemeler

struct StarShape: Shape {
  var inner: CGFloat = 0.72
  func path(in rect: CGRect) -> Path {
    var p = Path()
    let c = CGPoint(x: rect.midX, y: rect.midY), r = min(rect.width, rect.height) / 2
    for k in 0..<16 {
      let a = Double.pi / 8 * Double(k) - Double.pi / 2
      let rr = k % 2 == 0 ? r : r * inner
      let pt = CGPoint(x: c.x + rr * CGFloat(cos(a)), y: c.y + rr * CGFloat(sin(a)))
      if k == 0 { p.move(to: pt) } else { p.addLine(to: pt) }
    }
    p.closeSubpath()
    return p
  }
}

/// Zemin: gradyan + parıltı + sekiz köşeli yıldız deseni.
struct WidgetBackdrop: View {
  let p: WPalette
  var glow: UnitPoint = .topTrailing
  var body: some View {
    ZStack {
      LinearGradient(colors: [p.top, p.bottom], startPoint: .topLeading, endPoint: .bottomTrailing)
      RadialGradient(colors: [p.accent.opacity(p.dark ? 0.22 : 0.14), .clear], center: glow, startRadius: 0, endRadius: 220)
      Canvas { ctx, size in
        let step: CGFloat = 52
        var y: CGFloat = step / 2
        while y < size.height + step {
          var x: CGFloat = step / 2
          while x < size.width + step {
            ctx.stroke(StarShape().path(in: CGRect(x: x - 20, y: y - 20, width: 40, height: 40)), with: .color(p.accent.opacity(0.08)), lineWidth: 0.8)
            x += step
          }
          y += step
        }
      }
    }
  }
}

struct MosqueLine: Shape {
  func path(in r: CGRect) -> Path {
    var p = Path()
    let w = r.width, b = r.maxY, s = r.height / 160
    for x in [w * 0.18, w * 0.82 - 6] {
      p.move(to: CGPoint(x: x, y: b)); p.addLine(to: CGPoint(x: x, y: b - 52 * s))
      p.addLine(to: CGPoint(x: x + 3, y: b - 64 * s)); p.addLine(to: CGPoint(x: x + 6, y: b - 52 * s)); p.addLine(to: CGPoint(x: x + 6, y: b))
    }
    p.move(to: CGPoint(x: w * 0.3, y: b)); p.addLine(to: CGPoint(x: w * 0.3, y: b - 24 * s))
    p.addQuadCurve(to: CGPoint(x: w * 0.5, y: b - 58 * s), control: CGPoint(x: w * 0.3, y: b - 52 * s))
    p.addQuadCurve(to: CGPoint(x: w * 0.7, y: b - 24 * s), control: CGPoint(x: w * 0.7, y: b - 52 * s))
    p.addLine(to: CGPoint(x: w * 0.7, y: b))
    p.move(to: CGPoint(x: w * 0.5, y: b - 58 * s)); p.addLine(to: CGPoint(x: w * 0.5, y: b - 66 * s))
    return p
  }
}

struct CrescentMark: View {
  let p: WPalette
  var body: some View {
    ZStack {
      Circle().fill(p.accent.opacity(0.14)).frame(width: 30, height: 30)
      Image(systemName: "moon.fill").font(.system(size: 15)).foregroundStyle(p.accentLight)
    }
  }
}

extension View {
  @ViewBuilder func widgetBackground(_ p: WPalette, glow: UnitPoint = .topTrailing) -> some View {
    if #available(iOS 17.0, *) { containerBackground(for: .widget) { WidgetBackdrop(p: p, glow: glow) } }
    else { background(WidgetBackdrop(p: p, glow: glow)) }
  }
}

// MARK: - 1) Sıradaki Namaz

struct NextPrayerView: View {
  @Environment(\.widgetFamily) var family
  @Environment(\.colorScheme) var scheme
  let e: PrayerEntry
  var body: some View {
    let p = WPalette.current(scheme)
    switch family {
    case .accessoryRectangular:
      VStack(alignment: .leading, spacing: 1) {
        Text(e.countdown.targetTimeName).font(.headline)
        Text(e.countdown.targetDate, style: .timer).font(.title3.monospacedDigit())
        Text(e.countdown.targetTimeString).font(.caption)
      }
      .widgetBackground(p)
    case .accessoryInline:
      Text("\(e.countdown.targetTimeName) \(e.countdown.targetTimeString)")
        .widgetBackground(p)
    case .accessoryCircular:
      Gauge(value: e.periodProgress) { Text(e.countdown.targetTimeString) }.gaugeStyle(.accessoryCircularCapacity)
        .widgetBackground(p)
    default:
      GeometryReader { g in
        let r = min(g.size.width, g.size.height) / 2 - 14
        ZStack {
          MosqueLine().stroke(p.accent.opacity(0.32), lineWidth: 1.1)
          Circle().stroke(p.line, lineWidth: 5).frame(width: r * 2, height: r * 2)
          Circle().trim(from: 0, to: max(0.02, e.periodProgress))
            .stroke(LinearGradient(colors: [p.accentLight, p.accent], startPoint: .topLeading, endPoint: .bottomTrailing),
                    style: StrokeStyle(lineWidth: 5, lineCap: .round))
            .rotationEffect(.degrees(-90)).frame(width: r * 2, height: r * 2)
            .shadow(color: p.accent.opacity(0.7), radius: 5)
          VStack(spacing: 1) {
            Text(L10n.isEnglish ? "NEXT" : "SIRADAKİ").font(.system(size: 8.5)).tracking(2).foregroundStyle(p.muted)
            Text(e.countdown.targetTimeName.components(separatedBy: " (").first ?? "")
              .font(.custom("PlayfairDisplay-Bold", size: 17)).foregroundStyle(p.ink).lineLimit(1).minimumScaleFactor(0.6)
            Text(e.countdown.targetDate, style: .timer).font(.system(size: 20, weight: .heavy).monospacedDigit())
              .foregroundStyle(p.ink).multilineTextAlignment(.center).frame(width: r * 1.5)
            Text(e.countdown.targetTimeString).font(.system(size: 10.5, weight: .heavy)).foregroundStyle(p.accent)
              .padding(.horizontal, 8).padding(.vertical, 2).overlay(Capsule().stroke(p.accent, lineWidth: 1.1))
          }
          CrescentMark(p: p).position(x: g.size.width - 22, y: 22)
        }
        .frame(width: g.size.width, height: g.size.height)
      }
      .widgetBackground(p)
    }
  }
}

// MARK: - 2) Günün Vakitleri

private let HIJRI_TR = ["Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"]
private let HIJRI_EN = ["Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"]

func hijriText(_ d: Date) -> String {
  if let rd = RamadanCalendar.dayOfRamadan(d) {
    let y = Calendar(identifier: .islamicUmmAlQura).component(.year, from: d)
    return "\(rd) \(L10n.isEnglish ? HIJRI_EN[8] : HIJRI_TR[8]) \(y)"
  }
  let c = Calendar(identifier: .islamicUmmAlQura).dateComponents([.year, .month, .day], from: d)
  let names = L10n.isEnglish ? HIJRI_EN : HIJRI_TR
  return "\(c.day ?? 1) \(names[max(0, min(11, (c.month ?? 1) - 1))]) \(c.year ?? 0)"
}

struct PrayerIcon: View {
  let i: Int
  var body: some View {
    let names = ["moon", "sunrise", "sun.max", "cloud.sun", "sunset", "moon.stars"]
    Image(systemName: names[i]).font(.system(size: 14, weight: .medium))
  }
}

struct FiveTimesView: View {
  @Environment(\.colorScheme) var scheme
  let e: PrayerEntry
  var body: some View {
    let p = WPalette.current(scheme)
    let c = DateUtil.cal.dateComponents([.hour, .minute], from: e.date)
    let m = c.hour! * 60 + c.minute!
    let mins = e.times.all.map { PrayerTimeUtils.minutes($0) }
    let cur = mins.lastIndex { m >= $0 } ?? 5
    let dayProg = min(1, max(0, Double(m - mins[0]) / Double(max(1, mins[5] - mins[0]))))
    VStack(spacing: 8) {
      HStack {
        Text(e.place).font(.system(size: 11, weight: .heavy)).foregroundStyle(p.ink).lineLimit(1)
        Spacer()
        Text(hijriText(e.date)).font(.custom("PlayfairDisplay-Bold", size: 10.5)).foregroundStyle(p.accent).lineLimit(1)
      }
      HStack(spacing: 5) {
        ForEach(0..<6, id: \.self) { i in
          let sel = i == cur
          VStack(spacing: 3) {
            PrayerIcon(i: i).foregroundStyle(sel ? p.bottom : p.accent)
            Text(t(PrayerTimes.names[i])).font(.system(size: 10, weight: sel ? .heavy : .regular)).lineLimit(1).minimumScaleFactor(0.6)
            Text(e.times.all[i]).font(.system(size: 13, weight: .heavy).monospacedDigit()).minimumScaleFactor(0.7)
          }
          .foregroundStyle(sel ? p.bottom : p.ink)
          .frame(maxWidth: .infinity).padding(.vertical, 8)
          .background(
            RoundedRectangle(cornerRadius: 12)
              .fill(sel ? AnyShapeStyle(LinearGradient(colors: [p.accentLight, p.accent], startPoint: .top, endPoint: .bottom)) : AnyShapeStyle(p.ink.opacity(0.05)))
              .shadow(color: sel ? p.accent.opacity(0.5) : .clear, radius: 6)
          )
          .overlay(RoundedRectangle(cornerRadius: 12).stroke(sel ? .clear : p.line, lineWidth: 0.8))
        }
      }
      HStack(spacing: 10) {
        GeometryReader { g in
          ZStack(alignment: .leading) {
            Capsule().fill(p.line).frame(height: 3)
            Capsule().fill(p.accent).frame(width: g.size.width * dayProg, height: 3)
            Circle().fill(p.accentLight).frame(width: 9, height: 9).offset(x: g.size.width * dayProg - 4.5)
          }.frame(maxHeight: .infinity)
        }.frame(height: 10)
        HStack(spacing: 3) {
          Text(e.countdown.targetTimeName.components(separatedBy: " (").first ?? "").font(.system(size: 10)).foregroundStyle(p.muted)
          Text(e.countdown.targetDate, style: .timer).font(.system(size: 10, weight: .heavy).monospacedDigit()).foregroundStyle(p.ink)
            .frame(width: 52, alignment: .leading)
        }
      }
    }
    .padding(2)
    .widgetBackground(p, glow: .top)
  }
}

// MARK: - 3) Günün Manevi İklimi

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

struct ArchShape: Shape {
  func path(in r: CGRect) -> Path {
    var p = Path()
    let l = r.minX, rr = r.maxX, b = r.maxY, t0 = r.minY, mid = r.midX
    p.move(to: CGPoint(x: l, y: b)); p.addLine(to: CGPoint(x: l, y: t0 + 26))
    p.addQuadCurve(to: CGPoint(x: mid - 60, y: t0 + 8), control: CGPoint(x: l, y: t0 + 12))
    p.addQuadCurve(to: CGPoint(x: mid, y: t0 - 2), control: CGPoint(x: mid - 16, y: t0 + 4))
    p.addQuadCurve(to: CGPoint(x: mid + 60, y: t0 + 8), control: CGPoint(x: mid + 16, y: t0 + 4))
    p.addQuadCurve(to: CGPoint(x: rr, y: t0 + 26), control: CGPoint(x: rr, y: t0 + 12))
    p.addLine(to: CGPoint(x: rr, y: b)); p.closeSubpath()
    return p
  }
}

struct SpiritualWidgetView: View {
  @Environment(\.widgetFamily) var family
  @Environment(\.colorScheme) var scheme
  let e: SpiritualEntry
  var body: some View {
    let p = WPalette.current(scheme)
    let verse = !e.item.verseTurkish.isEmpty
    ZStack {
      ArchShape().stroke(p.accent.opacity(0.75), lineWidth: 1.1).padding(.horizontal, 2).padding(.top, 10).padding(.bottom, 2)
      ArchShape().stroke(p.accent.opacity(0.4), lineWidth: 0.6).padding(.horizontal, 6).padding(.top, 14).padding(.bottom, 6)
      StarShape().fill(p.bottom).frame(width: 18, height: 18).overlay(StarShape().stroke(p.accent, lineWidth: 1.1))
        .frame(maxHeight: .infinity, alignment: .top).offset(y: 1)
      VStack(spacing: 4) {
        Text(verse ? (L10n.isEnglish ? "VERSE OF THE DAY" : "GÜNÜN AYETİ") : (L10n.isEnglish ? "HADITH OF THE DAY" : "GÜNÜN HADİSİ"))
          .font(.system(size: 8.5)).tracking(2).foregroundStyle(p.accent)
        if verse && family != .systemSmall {
          Text(e.item.verseArabic).font(.custom("Amiri-Bold", size: 18)).foregroundStyle(p.accentLight).lineLimit(1).minimumScaleFactor(0.5)
        }
        Text("“\(verse ? e.item.verseTurkish : e.item.hadithTurkish)”")
          .font(.custom("PlayfairDisplay-Italic", size: family == .systemSmall ? 11.5 : 13)).foregroundStyle(p.ink)
          .multilineTextAlignment(.center).lineLimit(family == .systemSmall ? 5 : 3).minimumScaleFactor(0.7)
        Text(verse ? e.item.verseSource : e.item.hadithSource).font(.system(size: 8)).tracking(1.5).foregroundStyle(p.muted).lineLimit(1)
      }
      .padding(.horizontal, family == .systemSmall ? 10 : 30).padding(.top, 18)
    }
    .widgetBackground(p, glow: .bottom)
  }
}

// MARK: - 4) Oruç & İftar

struct FastingView: View {
  @Environment(\.colorScheme) var scheme
  let e: PrayerEntry
  var body: some View {
    let p = WPalette.current(scheme)
    let key = DateUtil.dateKey(e.date)
    let ramadanDay = RamadanCalendar.dayOfRamadan(e.date)
    let fastToday = ramadanDay != nil || e.fastingDates.contains(key)
    let imsak = PrayerTimeUtils.date(e.times.imsak, on: e.date)
    let iftar = PrayerTimeUtils.date(e.times.aksam, on: e.date)
    let en = L10n.isEnglish
    let title = ramadanDay.map { en ? "RAMADAN • DAY \($0)" : "RAMAZAN'IN \($0). GÜNÜ" } ?? (fastToday ? (en ? "FASTING DAY" : "ORUÇ GÜNÜ") : (en ? "HOLY DAYS" : "MÜBAREK GÜNLER"))
    ZStack(alignment: .topLeading) {
      Lantern(p: p).frame(width: 34, height: 70).frame(maxWidth: .infinity, alignment: .topTrailing).offset(x: 4, y: -14)
      VStack(alignment: .leading, spacing: 4) {
        Text(title).font(.system(size: 9, weight: .heavy)).foregroundStyle(p.accent).lineLimit(1).minimumScaleFactor(0.7)
        if fastToday && e.date < iftar {
          let toImsak = e.date < imsak
          Text(toImsak ? (en ? "Until suhoor ends" : "İmsaka kalan") : (en ? "Until iftar" : "İftara kalan"))
            .font(.system(size: 11)).foregroundStyle(p.muted).padding(.top, 6)
          Text(toImsak ? imsak : iftar, style: .timer).font(.system(size: 24, weight: .heavy).monospacedDigit()).foregroundStyle(p.ink)
          Spacer(minLength: 0)
          let prog = toImsak ? 0 : min(1, max(0, e.date.timeIntervalSince(imsak) / max(1, iftar.timeIntervalSince(imsak))))
          FastBar(p: p, progress: prog)
          HStack {
            Text((en ? "Imsak " : "İmsak ") + e.times.imsak)
            Spacer()
            Text(en ? "\(Int(prog * 100))%" : "%\(Int(prog * 100))").fontWeight(.heavy).foregroundStyle(p.accent)
            Spacer()
            Text((en ? "Iftar " : "İftar ") + e.times.aksam)
          }.font(.system(size: 9.5)).foregroundStyle(p.muted)
        } else {
          let up = upcoming(after: e.date)
          Text(en ? "Next holy day" : "Sıradaki mübarek gün").font(.system(size: 11)).foregroundStyle(p.muted).padding(.top, 6)
          Text(up.0).font(.system(size: 17, weight: .heavy)).foregroundStyle(p.ink).lineLimit(2).minimumScaleFactor(0.7)
          Text(up.1).font(.system(size: 13, weight: .heavy)).foregroundStyle(p.accent)
          Spacer(minLength: 0)
          FastBar(p: p, progress: nil)
          Text((en ? "Maghrib " : "Akşam ") + e.times.aksam).font(.system(size: 9.5)).foregroundStyle(p.muted)
        }
      }
    }
    .padding(2)
    .widgetBackground(p, glow: .topLeading)
  }

  /// Sıradaki kandil / bayram: (ad, "N gün kaldı")
  private func upcoming(after d: Date) -> (String, String) {
    let key = DateUtil.dateKey(d)
    let y = DateUtil.cal.component(.year, from: d)
    let days = IslamicDays.sortedDays(y) + IslamicDays.sortedDays(y + 1)
    guard let next = days.first(where: { ($0.category == "Mübarek Gece" || ($0.category == "Dini Bayram" && $0.title.contains("1. Gün"))) && $0.dateKey >= key }),
          let date = DateUtil.date(fromKey: next.dateKey) else { return (L10n.isEnglish ? "Blessed days" : "Hayırlı günler", "") }
    let n = DateUtil.cal.dateComponents([.day], from: DateUtil.startOfDay(d), to: DateUtil.startOfDay(date)).day ?? 0
    let name = t(next.title.components(separatedBy: " • ").first?.replacingOccurrences(of: " 1. Gün", with: "") ?? next.title)
    if n == 0 { return (name, L10n.isEnglish ? "Tonight" : "Bu gece") }
    return (name, L10n.isEnglish ? "in \(n) days" : "\(n) gün kaldı")
  }
}

struct FastBar: View {
  let p: WPalette
  let progress: Double?
  var body: some View {
    GeometryReader { g in
      ZStack(alignment: .leading) {
        Capsule().fill(p.line).frame(height: 6)
        if let pr = progress {
          Capsule().fill(LinearGradient(colors: [p.accentLight, p.accent], startPoint: .leading, endPoint: .trailing))
            .frame(width: max(6, g.size.width * pr), height: 6).shadow(color: p.accent.opacity(0.6), radius: 4)
          ZStack {
            Circle().fill(p.accent).frame(width: 18, height: 18)
            Image(systemName: "moon.fill").font(.system(size: 9)).foregroundStyle(p.bottom)
          }.offset(x: max(0, g.size.width * pr - 9))
        }
      }.frame(maxHeight: .infinity)
    }.frame(height: 18)
  }
}

struct Lantern: View {
  let p: WPalette
  var body: some View {
    Canvas { ctx, size in
      let cx = size.width / 2
      var cord = Path(); cord.move(to: CGPoint(x: cx, y: 0)); cord.addLine(to: CGPoint(x: cx, y: 14))
      ctx.stroke(cord, with: .color(p.accent), lineWidth: 1.2)
      ctx.fill(Path(ellipseIn: CGRect(x: cx - 26, y: 10, width: 52, height: 60)), with: .color(p.accentLight.opacity(0.14)))
      ctx.fill(Path(roundedRect: CGRect(x: cx - 7, y: 14, width: 14, height: 4), cornerRadius: 1.5), with: .color(p.accent))
      var body = Path()
      body.move(to: CGPoint(x: cx - 10, y: 18)); body.addQuadCurve(to: CGPoint(x: cx - 10, y: 58), control: CGPoint(x: cx - 15, y: 38))
      body.addLine(to: CGPoint(x: cx + 10, y: 58)); body.addQuadCurve(to: CGPoint(x: cx + 10, y: 18), control: CGPoint(x: cx + 15, y: 38))
      body.closeSubpath()
      ctx.fill(body, with: .linearGradient(Gradient(colors: [Color(red: 1, green: 0.91, blue: 0.66), Color(red: 0.94, green: 0.69, blue: 0.29)]),
                                           startPoint: CGPoint(x: cx, y: 18), endPoint: CGPoint(x: cx, y: 58)))
      ctx.stroke(body, with: .color(p.accent), lineWidth: 1.1)
      ctx.fill(Path(roundedRect: CGRect(x: cx - 6, y: 58, width: 12, height: 4), cornerRadius: 1.5), with: .color(p.accent))
    }
  }
}

// MARK: - Widget tanımları

struct NextPrayerWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "NextPrayer", provider: PrayerProvider()) { e in NextPrayerView(e: e) }
      .configurationDisplayName("Sıradaki Namaz")
      .description("İlerleme halkası ve geri sayım / Next prayer & countdown")
      .supportedFamilies([.systemSmall, .accessoryRectangular, .accessoryInline, .accessoryCircular])
  }
}

struct FiveTimesWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "FiveTimes", provider: PrayerProvider()) { e in FiveTimesView(e: e) }
      .configurationDisplayName("Günün Vakitleri")
      .description("6 vakit, hicri tarih ve günün akışı / Today's prayer times")
      .supportedFamilies([.systemMedium])
  }
}

struct SpiritualWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "Spiritual", provider: SpiritualProvider()) { e in SpiritualWidgetView(e: e) }
      .configurationDisplayName("Günün Manevi İklimi")
      .description("Günün ayeti / Verse of the day")
      .supportedFamilies([.systemMedium, .systemSmall])
  }
}

struct FastingWidget: Widget {
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: "Fasting", provider: PrayerProvider()) { e in FastingView(e: e) }
      .configurationDisplayName("Oruç & İftar")
      .description("Oruç ilerlemesi, iftara kalan süre; diğer günlerde sıradaki kandil / Fast progress & next holy day")
      .supportedFamilies([.systemSmall])
  }
}

@main
struct RamazanWidgetBundle: WidgetBundle {
  var body: some Widget {
    NextPrayerWidget()
    FiveTimesWidget()
    SpiritualWidget()
    FastingWidget()
  }
}

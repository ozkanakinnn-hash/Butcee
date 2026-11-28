import SwiftUI

private func tr(_ a: String, _ b: String) -> String { L10n.isEnglish ? b : a }

/// Takip sekmesi: kılınan vakitler (hafta / ay), seri, vakit bazında oranlar, kaza ve takip ayarları.
struct TrackingView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var prayers: PrayerService
  @State private var mode = 1  // 0 hafta, 1 ay
  @State private var monthOffset = 0

  var body: some View {
    let _ = store.prayerLogVersion
    let now = Date()
    ScrollView {
      VStack(alignment: .leading, spacing: 12) {
        ScreenHeader(title: tr("Takip", "Tracking"), subtitle: store.locationLabel)
        Picker("", selection: $mode) {
          Text(tr("Hafta", "Week")).tag(0)
          Text(tr("Ay", "Month")).tag(1)
        }
        .pickerStyle(.segmented)

        if mode == 0 { weekCard(now: now) } else { monthCard(now: now) }
        kpis(now: now)
        perVakitCard(now: now)
        settingsCard
      }
      .padding(16)
    }
    .screen()
    .toolbar(.hidden, for: .navigationBar)
  }

  // MARK: Hafta

  private func weekCard(now: Date) -> some View {
    let wd = DateUtil.cal.component(.weekday, from: now)  // 1 = Pazar
    let monday = DateUtil.addDays(-((wd + 5) % 7), to: now)
    let days = (0..<7).map { DateUtil.addDays($0, to: monday) }
    let names = L10n.isEnglish ? ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"] : ["Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"]
    let todayKey = DateUtil.dateKey(now)
    return VStack(alignment: .leading, spacing: 12) {
      caption(tr("Bu hafta", "This week"))
      HStack(spacing: 0) {
        ForEach(0..<7, id: \.self) { i in
          let k = DateUtil.dateKey(days[i])
          let prayed = PrayerLog.prayed(on: k)
          VStack(spacing: 6) {
            PrayerRing(prayed: prayed, current: nil,
                       color: prayed.count == 5 ? p.primary : p.primary.opacity(0.75),
                       track: p.surfaceVariant, lineWidth: 5, gapDegrees: 20)
              .frame(width: 40, height: 40)
              .opacity(k > todayKey ? 0.4 : 1)
            Text(names[i]).font(.system(size: 12, weight: k == todayKey ? .heavy : .semibold))
              .foregroundStyle(k == todayKey ? p.primary : p.onSurfaceVariant)
          }
          .frame(maxWidth: .infinity)
        }
      }
    }
    .padding(16).card()
  }

  // MARK: Ay

  private func monthStart(_ now: Date) -> Date {
    let base = DateUtil.cal.date(byAdding: .month, value: monthOffset, to: now) ?? now
    return DateUtil.cal.date(from: DateUtil.cal.dateComponents([.year, .month], from: base)) ?? base
  }

  private func monthCard(now: Date) -> some View {
    let start = monthStart(now)
    let count = DateUtil.cal.range(of: .day, in: .month, for: start)?.count ?? 30
    let lead = (DateUtil.cal.component(.weekday, from: start) + 5) % 7  // Pazartesi başı
    let todayKey = DateUtil.dateKey(now)
    let heads = L10n.isEnglish ? ["M", "T", "W", "T", "F", "S", "S"] : ["Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz"]
    let fmt = DateFormatter()
    fmt.locale = Locale(identifier: L10n.isEnglish ? "en_US" : "tr_TR")
    fmt.dateFormat = "LLLL yyyy"
    let cols = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)
    return VStack(alignment: .leading, spacing: 10) {
      HStack {
        Button { monthOffset -= 1 } label: { Image(systemName: "chevron.left") }
        Spacer()
        Text(fmt.string(from: start).capitalized(with: fmt.locale)).font(.system(size: 16, weight: .bold))
        Spacer()
        Button { monthOffset += 1 } label: { Image(systemName: "chevron.right") }.disabled(monthOffset >= 0)
      }
      .foregroundStyle(p.primary)
      LazyVGrid(columns: cols, spacing: 6) {
        ForEach(0..<7, id: \.self) { i in
          Text(heads[i]).font(.system(size: 10, weight: .bold)).foregroundStyle(p.onSurfaceVariant)
        }
        ForEach(0..<lead, id: \.self) { _ in Color.clear.frame(height: 34) }
        ForEach(1...count, id: \.self) { d in
          let date = DateUtil.addDays(d - 1, to: start)
          let k = DateUtil.dateKey(date)
          let n = PrayerLog.count(on: k)
          let future = k > todayKey
          Text("\(d)")
            .font(.system(size: 12, weight: .bold))
            .foregroundStyle(future ? p.onSurfaceVariant.opacity(0.4) : (n == 5 ? p.onPrimary : p.onBackground))
            .frame(maxWidth: .infinity).frame(height: 34)
            .background(RoundedRectangle(cornerRadius: 9).fill(dayColor(n: n, future: future)))
            .overlay(RoundedRectangle(cornerRadius: 9).stroke(k == todayKey ? p.primary : .clear, lineWidth: 2))
        }
      }
      HStack(spacing: 12) {
        legend(p.primary, "5/5")
        legend(p.primary.opacity(0.45), "3–4")
        legend(p.primary.opacity(0.18), "1–2")
        legend(p.surfaceVariant, tr("işaret yok", "none"))
      }
      .font(.system(size: 11, weight: .semibold)).foregroundStyle(p.onSurfaceVariant)
    }
    .padding(16).card()
  }

  private func dayColor(n: Int, future: Bool) -> Color {
    if future || n == 0 { return p.surfaceVariant.opacity(future ? 0.35 : 0.8) }
    if n == 5 { return p.primary }
    if n >= 3 { return p.primary.opacity(0.45) }
    return p.primary.opacity(0.18)
  }

  private func legend(_ c: Color, _ s: String) -> some View {
    HStack(spacing: 4) { RoundedRectangle(cornerRadius: 3).fill(c).frame(width: 10, height: 10); Text(s) }
  }

  // MARK: Göstergeler

  private func kpis(now: Date) -> some View {
    let start = mode == 0 ? DateUtil.addDays(-((DateUtil.cal.component(.weekday, from: now) + 5) % 7), to: now) : monthStart(now)
    let end = mode == 0 ? DateUtil.addDays(6, to: start)
      : DateUtil.addDays((DateUtil.cal.range(of: .day, in: .month, for: start)?.count ?? 30) - 1, to: start)
    let st = PrayerLog.stats(from: start, to: end, todayTimes: prayers.today, now: now)
    let pct = st.total == 0 ? 0 : Int((Double(st.prayed) / Double(st.total) * 100).rounded())
    return HStack(spacing: 10) {
      kpi(tr("Seri", "Streak"), "\(PrayerLog.streak(today: now))", tr("En uzun \(PrayerLog.longestStreak())", "Best \(PrayerLog.longestStreak())"), accent: true)
      kpi(mode == 0 ? tr("Bu hafta", "This week") : tr("Bu ay", "This month"), "%\(pct)", "\(st.prayed)/\(st.total) \(tr("vakit", "prayers"))")
      NavigationLink(value: Route.kaza) {
        kpi(tr("Kaza", "Missed"), "\(store.kazaTotalRemaining)", tr("kalan", "remaining"))
      }.buttonStyle(.plain)
    }
  }

  private func kpi(_ title: String, _ value: String, _ sub: String, accent: Bool = false) -> some View {
    VStack(alignment: .leading, spacing: 4) {
      caption(title)
      Text(value).font(.system(size: 24, weight: .heavy)).foregroundStyle(accent ? p.primary : p.onBackground)
        .lineLimit(1).minimumScaleFactor(0.6)
      Text(sub).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant).lineLimit(1).minimumScaleFactor(0.7)
    }
    .padding(12).frame(maxWidth: .infinity, alignment: .leading).card(radius: 18)
  }

  // MARK: Vakit bazında

  private func perVakitCard(now: Date) -> some View {
    let st = PrayerLog.stats(from: DateUtil.addDays(-29, to: now), to: now, todayTimes: prayers.today, now: now)
    let rates: [(FarzVakit, Double)] = FarzVakit.allCases.map { v in
      let s = st.perVakit[v] ?? (0, 0)
      return (v, s.total == 0 ? 0 : Double(s.prayed) / Double(s.total))
    }
    let tracked = st.prayed > 0
    let weakest = rates.min { $0.1 < $1.1 }
    return VStack(alignment: .leading, spacing: 10) {
      caption(tr("Son 30 gün · vakit bazında", "Last 30 days · by prayer"))
      ForEach(rates, id: \.0) { item in
        let v = item.0, r = item.1
        HStack(spacing: 10) {
          Text(v.name).font(.system(size: 14, weight: .semibold)).frame(width: 60, alignment: .leading)
          GeometryReader { g in
            ZStack(alignment: .leading) {
              Capsule().fill(p.surfaceVariant)
              Capsule().fill(p.primary).frame(width: max(0, g.size.width * r))
            }
          }
          .frame(height: 8)
          Text("%\(Int((r * 100).rounded()))").font(.system(size: 13, weight: .bold).monospacedDigit()).frame(width: 44, alignment: .trailing)
        }
      }
      if tracked, let w = weakest, w.1 < 0.9 {
        NavigationLink(value: Route.notificationWizard) {
          HStack(alignment: .top, spacing: 8) {
            Image(systemName: "lightbulb").foregroundStyle(p.primary)
            Text(tr("En çok kaçırılan vakit: \(w.0.name). Bu vaktin ezan bildirimini açık tutmak yardımcı olur.",
                    "Most missed: \(w.0.name). Keeping its adhan notification on helps."))
              .font(.system(size: 12)).foregroundStyle(p.onBackground).multilineTextAlignment(.leading)
            Spacer(minLength: 0)
          }
          .padding(12).background(RoundedRectangle(cornerRadius: 14).fill(p.surfaceVariant.opacity(0.7)))
        }.buttonStyle(.plain)
      }
      if !tracked {
        Text(tr("Ana sayfadaki vakit kartında kıldığınız vakte dokunun; widget, bildirim ve Siri ile de işaretleyebilirsiniz.",
                "Tap a prayer on the home screen card; you can also mark from the widget, notifications or Siri."))
          .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
      }
    }
    .padding(16).card()
  }

  // MARK: Ayarlar

  private var settingsCard: some View {
    VStack(alignment: .leading, spacing: 10) {
      caption(tr("Takip ayarları", "Tracking settings"))
      Toggle(isOn: $store.autoKaza) {
        VStack(alignment: .leading, spacing: 2) {
          Text(tr("Kaçırılan vakitleri kazaya ekle", "Add missed prayers to qada")).font(.system(size: 14, weight: .semibold))
          Text(tr("En az bir vakit işaretlediğiniz günlerde işaretlenmeyen vakitler ertesi gün kaza borcuna eklenir.",
                  "On days you marked at least one prayer, unmarked ones are added to your qada list the next day."))
            .font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
      }
      Toggle(isOn: $store.liveActivityEnabled) {
        VStack(alignment: .leading, spacing: 2) {
          Text(tr("Kilit ekranında geri sayım", "Lock screen countdown")).font(.system(size: 14, weight: .semibold))
          Text(tr("Sıradaki vakte kalan süre kilit ekranında ve Dynamic Island'da görünür.",
                  "Time to the next prayer on the Lock Screen and in the Dynamic Island."))
            .font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
      }
    }
    .tint(p.primary)
    .padding(16).card()
  }

  private func caption(_ s: String) -> some View {
    Text(s.uppercased(with: Locale(identifier: L10n.isEnglish ? "en_US" : "tr_TR")))
      .font(.system(size: 11, weight: .bold)).tracking(0.8).foregroundStyle(p.onSurfaceVariant)
  }
}

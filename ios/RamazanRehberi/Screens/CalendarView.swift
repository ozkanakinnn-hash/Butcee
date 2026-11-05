import SwiftUI

struct FastingCalendarView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var month = DateUtil.cal.date(from: DateUtil.cal.dateComponents([.year, .month], from: Date()))!
  @State private var selected: DayItem?
  @State private var showToday = false

  struct DayItem: Identifiable { let key: String; var id: String { key } }

  var year: Int { DateUtil.cal.component(.year, from: Date()) }

  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 14) {
        HStack(spacing: 10) {
          Image(systemName: "calendar").font(.system(size: 22)).foregroundStyle(p.primary)
          Text(t("Takvim")).font(.system(size: 26, weight: .bold))
        }
        Text(L10n.isEnglish ? "Tap a day to plan a fast or mark one you kept; follow kandils, Eids and religious days."
                            : "Bir güne dokunarak oruç planlayın veya tuttuğunuz orucu işaretleyin; kandil, bayram ve dini günleri takip edin.")
          .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
        stats
        Button { showToday = true } label: {
          HStack {
            Text("🤲").font(.system(size: 20))
            Text(L10n.isEnglish ? "Log Today's Fast / Qada Counter" : "Bugünkü Oruç Durumunu Kaydet / Kaza Sayacı").font(.system(size: 14, weight: .semibold))
            Spacer()
            Text(L10n.isEnglish ? "Open ›" : "Aç ›").font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)
          }.padding(14).card(radius: 16)
        }.buttonStyle(.plain)
        monthGrid
        legend
        Text(L10n.isEnglish ? "\(String(year)) Religious Days" : "\(String(year)) Yılı Dini Günler").font(.system(size: 19, weight: .bold)).padding(.top, 6)
        VStack(spacing: 8) {
          ForEach(IslamicDays.sortedDays(year).filter { $0.category != "Dini Hafta" && $0.category != "İslam Büyükleri" }, id: \.dateKey) { d in
            HStack(spacing: 12) {
              Text(d.badgeEmoji).font(.system(size: 22))
              VStack(alignment: .leading, spacing: 2) {
                Text(t(d.title)).font(.system(size: 14, weight: .semibold))
                Text(longDate(d.dateKey) + " • " + t(d.category)).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
              }
              Spacer()
              if d.dateKey < DateUtil.dateKey(Date()) { Image(systemName: "checkmark").foregroundStyle(p.onSurfaceVariant) }
            }
            .padding(12).card(radius: 14)
            .opacity(d.dateKey < DateUtil.dateKey(Date()) ? 0.55 : 1)
          }
        }
      }
      .padding(16)
    }
    .screen()
    .toolbar(.hidden, for: .navigationBar)
    .sheet(item: $selected) { d in DayPlanSheet(key: d.key).presentationDetents([.medium]) }
    .sheet(isPresented: $showToday) { DayPlanSheet(key: DateUtil.dateKey(Date())).presentationDetents([.medium, .large]) }
  }

  private var stats: some View {
    let yearKeys = store.fastingDates.filter { $0.hasPrefix("\(year)-") }
    let ramadanRange = RamadanCalendar.rangeForYear(year)
    var ramadanTotal = 0, ramadanFasted = 0, kaza = 0
    if let r = ramadanRange, var d = DateUtil.date(fromKey: r.start), let end = DateUtil.date(fromKey: r.end) {
      while d <= end {
        let k = DateUtil.dateKey(d)
        ramadanTotal += 1
        if store.fastingStatus[k] == "fasted" { ramadanFasted += 1 }
        if store.fastingStatus[k] == "unfasted" || store.fastingStatus[k] == "broken" { kaza += 1 }
        d = DateUtil.addDays(1, to: d)
      }
    }
    return HStack(spacing: 8) {
      statBox(L10n.isEnglish ? "This Year" : "Bu Yıl Toplam", "\(yearKeys.count) \(t("Gün"))", L10n.isEnglish ? "All fasts" : "Tüm Oruçlar", p.primary)
      statBox(t("Ramazan"), "\(ramadanFasted) / \(ramadanTotal)", L10n.isEnglish ? "Obligatory" : "Farz Oruç", p.secondary)
      statBox(t("Kaza Borcu"), "\(kaza) \(t("Gün"))", L10n.isEnglish ? "Days to make up" : "Kalan Oruç", p.secondary)
    }
  }

  private func statBox(_ title: String, _ value: String, _ sub: String, _ color: Color) -> some View {
    VStack(alignment: .leading, spacing: 6) {
      Text(title).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
      Text(value).font(.system(size: 17, weight: .bold)).foregroundStyle(color)
      Text(sub).font(.system(size: 10)).foregroundStyle(p.onSurfaceVariant)
    }
    .padding(12).frame(maxWidth: .infinity, alignment: .leading).card(radius: 16)
  }

  private var monthGrid: some View {
    let comps = DateUtil.cal.dateComponents([.year, .month], from: month)
    let days = DateUtil.cal.range(of: .day, in: .month, for: month)!.count
    let firstWeekday = (DateUtil.cal.component(.weekday, from: month) + 5) % 7 // Pazartesi = 0
    let todayKey = DateUtil.dateKey(Date())
    return VStack(spacing: 12) {
      HStack {
        Button { month = DateUtil.cal.date(byAdding: .month, value: -1, to: month)! } label: { Image(systemName: "arrow.left") }
        Spacer()
        Text("\(DateUtil.monthName(comps.month!)) \(String(comps.year!))").font(.system(size: 17, weight: .bold))
        Spacer()
        Button { month = DateUtil.cal.date(byAdding: .month, value: 1, to: month)! } label: { Image(systemName: "arrow.right") }
      }
      .foregroundStyle(p.onBackground)
      LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 4), count: 7), spacing: 8) {
        ForEach(DateUtil.weekdaysShort, id: \.self) { Text($0).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant) }
        ForEach(Array(0..<firstWeekday).map { -100 - $0 }, id: \.self) { _ in Color.clear.frame(height: 44) }
        ForEach(1...days, id: \.self) { d in
          let key = String(format: "%04d-%02d-%02d", comps.year!, comps.month!, d)
          let ramadan = RamadanCalendar.isRamadanDay(key)
          let planned = store.fastingDates.contains(key)
          let special = DateUtil.date(fromKey: key).flatMap { IslamicDays.special(on: $0) }
          Button { selected = DayItem(key: key) } label: {
            VStack(spacing: 2) {
              Text("\(d)").font(.system(size: 15, weight: key == todayKey ? .bold : .regular))
                .foregroundStyle(planned ? p.onPrimary : p.onBackground)
              Circle().fill(special.map { dotColor($0.category) } ?? .clear).frame(width: 5, height: 5)
            }
            .frame(maxWidth: .infinity).frame(height: 44)
            .background(RoundedRectangle(cornerRadius: 10).fill(planned ? p.primary : (ramadan ? p.primary.opacity(0.15) : .clear)))
            .overlay(RoundedRectangle(cornerRadius: 10).stroke(key == todayKey ? p.primary : .clear, lineWidth: 1.5))
          }.buttonStyle(.plain)
        }
      }
    }
    .padding(16).card()
  }

  private var legend: some View {
    HStack(spacing: 14) {
      legendItem(p.primary, L10n.isEnglish ? "Fast" : "Oruç")
      legendItem(p.primary.opacity(0.25), t("Ramazan"))
      legendItem(dotColor("Mübarek Gece"), L10n.isEnglish ? "Kandil" : "Kandil")
      legendItem(dotColor("Dini Bayram"), L10n.isEnglish ? "Eid" : "Bayram")
    }.font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
  }

  private func legendItem(_ c: Color, _ s: String) -> some View { HStack(spacing: 4) { Circle().fill(c).frame(width: 8, height: 8); Text(s) } }

  private func dotColor(_ category: String) -> Color {
    switch category {
    case "Mübarek Gece": return Color(hex: 0xF0B04A)
    case "Dini Bayram": return Color(hex: 0x4E9A68)
    case "Resmi Tatil": return Color(hex: 0xD9534F)
    default: return p.secondary
    }
  }

  private func longDate(_ key: String) -> String {
    let p = key.split(separator: "-").compactMap { Int($0) }
    return "\(p[2]) \(DateUtil.monthName(p[1]))"
  }
}

/// Bir gün için oruç planı / durumu.
struct DayPlanSheet: View {
  @Environment(\.palette) var p
  @Environment(\.dismiss) var dismiss
  @EnvironmentObject var store: AppData
  let key: String

  var body: some View {
    let special = DateUtil.date(fromKey: key).flatMap { IslamicDays.special(on: $0) }
    let isPast = key <= DateUtil.dateKey(Date())
    VStack(spacing: 16) {
      Capsule().fill(p.outline).frame(width: 40, height: 4).padding(.top, 8)
      Text(title).font(.system(size: 20, weight: .bold))
      if let s = special { Text("\(s.badgeEmoji) \(t(s.title))").font(.system(size: 14)).foregroundStyle(p.primary).multilineTextAlignment(.center) }
      if RamadanCalendar.isRamadanDay(key) { Text("🌙 " + (L10n.isEnglish ? "Ramadan fast (obligatory)" : "Ramazan orucu (farz)")).font(.system(size: 14)) }
      if isPast {
        VStack(spacing: 10) {
          statusButton("fasted", L10n.isEnglish ? "✅ I fasted" : "✅ Orucumu tuttum")
          statusButton("unfasted", L10n.isEnglish ? "⭕ I didn't fast (qada)" : "⭕ Tutamadım (kaza)")
          statusButton("broken", L10n.isEnglish ? "⚠️ My fast was broken" : "⚠️ Orucum bozuldu")
        }
      } else {
        Button {
          store.toggleFastingDate(key); dismiss()
        } label: {
          Text(store.fastingDates.contains(key) ? t("Oruç planını kaldır") : t("Oruç tutacağım"))
            .font(.headline).foregroundStyle(p.onPrimary).frame(maxWidth: .infinity).padding()
            .background(RoundedRectangle(cornerRadius: 16).fill(p.primary))
        }
        Text(L10n.isEnglish ? "On planned days you'll get suhoor/iftar reminders and the iftar countdown." : "Planlı günlerde sahur/iftar hatırlatıcısı ve iftar sayacı çalışır.")
          .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center)
      }
      Spacer()
    }
    .padding(.horizontal, 24)
    .screen()
  }

  var title: String {
    let parts = key.split(separator: "-").compactMap { Int($0) }
    return "\(parts[2]) \(DateUtil.monthName(parts[1])) \(parts[0])"
  }

  private func statusButton(_ status: String, _ label: String) -> some View {
    let sel = store.fastingStatus[key] == status
    return Button { store.setFastingStatus(key, status); dismiss() } label: {
      Text(label).font(.system(size: 15, weight: .semibold)).foregroundStyle(sel ? p.onPrimary : p.onBackground)
        .frame(maxWidth: .infinity).padding(14)
        .background(RoundedRectangle(cornerRadius: 14).fill(sel ? p.primary : p.surfaceVariant))
    }.buttonStyle(.plain)
  }
}

import SwiftUI

struct HomeView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @EnvironmentObject var prayers: PrayerService
  @StateObject private var ticker = Ticker()
  var selectTab: (Int) -> Void = { _ in }
  @State private var showLocation = false

  var body: some View {
    let now = ticker.now
    let todayKey = DateUtil.dateKey(now)
    let ramadanDay = RamadanCalendar.dayOfRamadan(now)
    let countdown = PrayerTimeUtils.countdown(prayers.today, fastingDates: store.fastingDates, now: now)
    let fastingToday = store.isFastingDay(todayKey)
    let name = store.userName.isEmpty ? t("Değerli Kardeşimiz") : store.userName

    ScrollView {
      VStack(spacing: 14) {
        if ramadanDay != nil { RamadanLanternGarland().padding(.horizontal, -16) }
        header(name: name)
        if let d = ramadanDay {
          VStack(spacing: 8) { RamadanDayBadge(day: d); RamadanMahyaBar(day: d) }
        }
        if let f = Festival.today(now) { FestiveGreetingCard(festival: f, name: name) }
        HStack(spacing: 10) {
          MosqueBanner()
          CountdownCard(result: countdown, ramadanWatermark: ramadanDay != nil)
        }
        .frame(height: 250)
        ShortcutsRow()
        if fastingToday { FastingProgressCard(times: prayers.today, now: now) }
        PrayerTimesCard(times: prayers.today, now: now)
        DailySpiritualCard()
        ShareWithLovedOnesCard()
        HolidayCountdownCard(now: now)
        if fastingToday {
          IftarMenuCard(ramadanDay: ramadanDay)
            .padding(.horizontal, -16)
        }
        NameSuggestionsCard()
        AdBanner()
        PrivacyFooter()
      }
      .padding(.horizontal, 16)
      .padding(.bottom, 20)
    }
    .screen()
    .toolbar(.hidden, for: .navigationBar)
    .sheet(isPresented: $showLocation) { LocationPickerView() }
    .refreshable { prayers.reload() }
  }

  private func header(name: String) -> some View {
    VStack(alignment: .leading, spacing: 10) {
      HStack(spacing: 8) {
        Button { showLocation = true } label: {
          HStack(spacing: 4) {
            Image(systemName: "mappin.and.ellipse").foregroundStyle(p.primary)
            Text(store.locationLabel).font(.system(size: 14, weight: .semibold)).foregroundStyle(p.primary).lineLimit(1)
            Text(t("• Değiştir")).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          }
        }.buttonStyle(.plain)
        Spacer()
        circleLink(.zikirmatik, image: "ic_tasbih")
        circleLink(.qibla, image: "ic_kaaba", original: true)
        circleLink(.mosques, image: "ic_hub_mosque")
        NavigationLink(value: Route.settings) {
          Image(systemName: "gearshape.fill").font(.system(size: 20)).foregroundStyle(p.onBackground).frame(width: 36, height: 36)
        }
      }
      Text("\(t("Merhaba")), \(name) 👋").font(.system(size: 27, weight: .bold))
    }
    .padding(.top, 8)
  }

  private func circleLink(_ r: Route, image: String, original: Bool = false) -> some View {
    NavigationLink(value: r) {
      Image(image).resizable().renderingMode(original ? .original : .template).scaledToFit()
        .foregroundStyle(p.primary).frame(width: 20, height: 20)
        .frame(width: 38, height: 38).background(Circle().fill(p.surfaceVariant))
    }
  }
}

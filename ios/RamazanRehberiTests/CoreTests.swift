import XCTest
import CoreLocation
import UserNotifications
@testable import RamazanRehberi

/// iOS çekirdek mantık testleri: vakit/sayaç, takvim, içerik, kayıtlar, bildirim planlama.
final class CoreTests: XCTestCase {

  override func tearDown() { L10n.current = "tr"; super.tearDown() }

  private func date(_ y: Int, _ m: Int, _ d: Int, _ h: Int = 12, _ min: Int = 0) -> Date {
    DateUtil.cal.date(from: DateComponents(year: y, month: m, day: d, hour: h, minute: min))!
  }

  private let manisa = PrayerTimes(imsak: "06:01", gunes: "07:34", ogle: "12:59", ikindi: "15:48", aksam: "18:13", yatsi: "19:31",
                                   dateStr: "2026-11-05", city: "Manisa", district: "Saruhanlı")

  func testCleanTime() {
    XCTAssertEqual(PrayerTimeUtils.clean("18:30 (+03)"), "18:30")
    XCTAssertEqual(PrayerTimeUtils.clean("5:07"), "05:07")
    XCTAssertEqual(PrayerTimeUtils.clean(nil, fallback: "12:00"), "12:00")
  }

  func testCountdownModes() {
    let normal = PrayerTimeUtils.countdown(manisa, fastingDates: [], now: date(2026, 11, 5, 14))
    XCTAssertEqual(normal.mode, .prayer)
    XCTAssertEqual(normal.targetTimeString, "15:48")
    XCTAssertEqual(normal.hours, 1); XCTAssertEqual(normal.minutes, 48)
    let night = PrayerTimeUtils.countdown(manisa, fastingDates: [], now: date(2026, 11, 5, 22))
    XCTAssertEqual(night.targetTimeString, "06:01"); XCTAssertEqual(night.hours, 8)
    let iftar = PrayerTimeUtils.countdown(manisa, fastingDates: ["2026-11-05"], now: date(2026, 11, 5, 14))
    XCTAssertEqual(iftar.mode, .iftar)
    let sahur = PrayerTimeUtils.countdown(manisa, fastingDates: ["2026-11-06"], now: date(2026, 11, 5, 21))
    XCTAssertEqual(sahur.mode, .sahur)
    let teravih = PrayerTimeUtils.countdown(manisa, fastingDates: [], now: date(2027, 2, 19, 18, 30))
    XCTAssertEqual(teravih.mode, .teravih)
  }

  func testRamadanCalendar() {
    XCTAssertEqual(RamadanCalendar.dayOfRamadan(date(2027, 2, 19)), 12)
    XCTAssertNil(RamadanCalendar.dayOfRamadan(date(2026, 11, 5)))
    XCTAssertTrue(RamadanCalendar.isRamadanDay("2027-03-08"))
    XCTAssertFalse(RamadanCalendar.isRamadanDay("2027-03-09"))
  }

  func testIslamicDays2026() {
    let days = IslamicDays.forYear(2026)
    XCTAssertTrue(days["2026-03-20"]?.title.contains("Ramazan Bayramı 1. Gün") == true, "Ramazan Bayramı tarihi")
    XCTAssertTrue(days["2026-05-27"]?.title.contains("Kurban Bayramı 1. Gün") == true, "Kurban Bayramı tarihi")
    XCTAssertGreaterThanOrEqual(days.values.filter { $0.category == "Mübarek Gece" }.count, 5)
  }

  func testHolidayCountdown() {
    let a = HolidayCountdown.calculate(date(2026, 11, 5, 0))
    XCTAssertTrue(a.title.contains("Ramazan"))
    XCTAssertEqual(a.days, 95)
    let b = HolidayCountdown.calculate(date(2027, 2, 19, 0))
    XCTAssertTrue(b.title.contains("Bayram"))
  }

  func testOfflineCalcCloseToDiyanet() {
    let t = LocalPrayerCalc.times(city: "Manisa", district: "Saruhanlı", date: date(2026, 11, 5))
    for (g, e) in zip(t.all, ["06:10", "07:34", "12:59", "15:48", "18:13", "19:31"]) {
      XCTAssertLessThanOrEqual(abs(PrayerTimeUtils.minutes(g) - PrayerTimeUtils.minutes(e)), 3, "\(g) / \(e)")
    }
    for p in AppContent.shared.provinces { XCTAssertNotNil(LocalPrayerCalc.provinceCoords[p.name], "koordinat yok: \(p.name)") }
  }

  func testMaghribRoundedUpLikeDiyanet() {
    XCTAssertEqual(LocalPrayerCalc.diyanetMaghrib("18:11", lat: 38.733, lon: 27.566, y: 2026, m: 11, d: 6, tzHours: 3), "18:12")
    XCTAssertEqual(LocalPrayerCalc.diyanetMaghrib("18:12", lat: 38.733, lon: 27.566, y: 2026, m: 11, d: 6, tzHours: 3), "18:12")
    XCTAssertEqual(LocalPrayerCalc.diyanetMaghrib("18:09", lat: 38.733, lon: 27.566, y: 2026, m: 11, d: 6, tzHours: 3), "18:09")
  }

  func testQiblaDiyanetMethod() {
    // Diyanet Kâbe koordinatı ve büyük çember açıları (gerçek kuzey)
    XCTAssertEqual(QiblaCalc.kaabaLat, 21.4225, accuracy: 1e-4)
    XCTAssertEqual(QiblaCalc.kaabaLon, 39.8261, accuracy: 1e-4)
    XCTAssertEqual(QiblaCalc.trueBearing(.init(latitude: 41.0082, longitude: 28.9784)), 151.6, accuracy: 0.15)
    XCTAssertEqual(QiblaCalc.trueBearing(.init(latitude: 39.9334, longitude: 32.8597)), 160.2, accuracy: 0.15)
    XCTAssertEqual(QiblaCalc.trueBearing(.init(latitude: 38.7333, longitude: 27.5667)), 145.2, accuracy: 0.15)
    XCTAssertEqual(QiblaCalc.distanceKm(.init(latitude: 41.0082, longitude: 28.9784)), 2405, accuracy: 25)
    // Diyanet biçimi: manyetik, tam derece (Saruhanlı, sapma +5,9°)
    XCTAssertEqual(QiblaCalc.diyanetAngle(trueBearing: 145.2, declination: 5.9), 139)
    // Diyanet'in yayımladığı açılar (2026 sapmaları WMM: Saruhanlı +5,88, İstanbul +6,36, Ankara +6,45)
    let y2026 = DateComponents(calendar: Calendar(identifier: .gregorian), year: 2026, month: 11, day: 7).date!
    XCTAssertEqual(QiblaCalc.diyanetPublishedAngle(trueBearing: 145.19, currentDeclination: 5.88, now: y2026), 141)
    XCTAssertEqual(QiblaCalc.diyanetPublishedAngle(trueBearing: 151.62, currentDeclination: 6.36, now: y2026), 147)
    XCTAssertEqual(QiblaCalc.diyanetPublishedAngle(trueBearing: 160.17, currentDeclination: 6.45, now: y2026), 155)
    // Diyanet tablosu ayrıştırma
    let days = DiyanetSource.parse([["MiladiTarihKisa": "07.11.2026", "Imsak": "06:12", "Gunes": "07:36", "Ogle": "12:58", "Ikindi": "15:46", "Aksam": "18:11", "Yatsi": "19:30", "KibleSaati": "10:46"]])
    XCTAssertEqual(days.first?.times, ["06:12", "07:36", "12:58", "15:46", "18:11", "19:30"])
    XCTAssertEqual(days.first?.dateKey, "2026-11-07")
    XCTAssertEqual(days.first?.kibleSaati, "10:46")
    XCTAssertEqual(DiyanetSource.norm("MANİSA"), "manisa")
    XCTAssertEqual(DiyanetSource.norm("Saruhanlı"), "saruhanli")
    XCTAssertEqual(QiblaCalc.turn(10, 350), 20, accuracy: 1e-9)
    XCTAssertEqual(QiblaCalc.turn(140, 150), -10, accuracy: 1e-9)
  }

  func testQiblaTimeSunPointsToKaaba() {
    let c = CLLocationCoordinate2D(latitude: 38.7333, longitude: 27.5667)
    var cal = Calendar(identifier: .gregorian); cal.timeZone = TimeZone(identifier: "Europe/Istanbul")!
    let day = cal.date(from: DateComponents(year: 2026, month: 11, day: 7, hour: 12))!
    let t = QiblaCalc.qiblaTime(c, day: day, calendar: cal)
    XCTAssertNotNil(t)
    let s = QiblaCalc.sun(c, t!)
    XCTAssertEqual(QiblaCalc.turn(QiblaCalc.trueBearing(c), s.azimuth), 0, accuracy: 0.1)
    XCTAssertEqual(cal.component(.hour, from: t!), 10)   // Android ile aynı: 10:46
  }

  func testHeadingFilterStability() {
    var f = HeadingFilter()
    var g = SystemRandomNumberGenerator()
    var outs: [Double] = []
    for i in 0..<500 {
      let noise = (Double.random(in: -1...1, using: &g) + Double.random(in: -1...1, using: &g)) * 3
      let v = f.update(145 + noise, at: Double(i) * 0.02)
      if i > 150 { outs.append(v) }
    }
    let m = outs.reduce(0, +) / Double(outs.count)
    let sd = sqrt(outs.map { ($0 - m) * ($0 - m) }.reduce(0, +) / Double(outs.count))
    XCTAssertLessThan(sd, 0.8)
    // Kuzeyden geçişte sıçrama yok
    var f2 = HeadingFilter(); var prev = f2.update(0, at: 0)
    for i in 1..<300 { let v = f2.update(QiblaCalc.norm(Double.random(in: -3...3, using: &g)), at: Double(i) * 0.02); XCTAssertLessThan(abs(v - prev), 3); prev = v }
  }

  func testContentAndTranslations() {
    let c = AppContent.shared
    XCTAssertEqual(c.esma.count, 99)
    XCTAssertEqual(c.surahs.count, 114)
    XCTAssertEqual(c.provinces.count, 81)
    XCTAssertEqual(c.kandiller.count, 5)
    for d in [-3, 0, 1, 30, 31, 400] {
      XCTAssertFalse(c.spiritualForDay(d).verseTurkish.isEmpty)
      XCTAssertFalse(c.iftarMenu(day: d).soup.isEmpty)
      XCTAssertFalse(c.sahurMenu(day: d).warm.tr.isEmpty)
      XCTAssertFalse(c.tipForDay(d).tr.isEmpty)
    }
    L10n.current = "en"
    XCTAssertEqual(t("Ayarlar"), "Settings")
    XCTAssertEqual(t("Öğle Vaktine Kalan"), "Time until Dhuhr")
    XCTAssertEqual(t("Bilinmeyen"), "Bilinmeyen")
  }

  func testQuranText() {
    let q = QuranText.shared
    XCTAssertEqual(q.surah(1).count, 7)
    XCTAssertEqual(q.surah(2).count, 286)
    var total = 0
    for s in 1...114 { total += q.surah(s).count }
    XCTAssertEqual(total, 6236)
    XCTAssertFalse(q.page(604).isEmpty)
    XCTAssertEqual(q.firstPage(ofSurah: 2), 2)
  }

  func testGreetingCardsRender() {
    for o in GreetingOccasion.all {
      XCTAssertFalse(o.quote.tr.isEmpty); XCTAssertFalse(o.messages.isEmpty)
      let img = GreetingCardRenderer.render(o, o.messages[0], scale: 0.2)
      XCTAssertEqual(img.size.width, 216, accuracy: 1)
    }
  }

  func testAutoLocationMatching() {
    XCTAssertEqual(AutoLocator.norm("Şanlıurfa"), "sanliurfa")
    XCTAssertEqual(AutoLocator.norm("İstanbul"), AutoLocator.norm("Istanbul"))
  }

  func testStores() {
    let s = AppData.shared
    s.kazaReset()
    s.kazaAddDays(10, includeVitir: false)
    XCTAssertEqual(s.kazaTotalRemaining, 50)
    s.kazaMarkPrayed(.sabah)
    XCTAssertEqual(s.kazaTotalRemaining, 49); XCTAssertEqual(s.kazaTotalPrayed, 1)
    s.kazaMarkPrayed(.vitir)
    XCTAssertEqual(s.kazaRemaining(.vitir), 0)
    s.readJuz = []; s.toggleJuz(3)
    XCTAssertEqual(s.readJuz, [3])
    let before = s.hatimCompleted
    s.completeHatim()
    XCTAssertEqual(s.hatimCompleted, before + 1); XCTAssertTrue(s.readJuz.isEmpty)
    for i in 0..<25 { s.saveZikir("e\(i)") }
    XCTAssertEqual(s.zikirSaved.count, 20)
    XCTAssertTrue(s.toggleFastingDate("2026-12-01"))
    XCTAssertFalse(s.toggleFastingDate("2026-12-01"))
  }

  /// Bildirim planlama: iOS sınırı (64) aşılmaz, ezan bildirimleri gelecekte kurulur.
  func testNotificationScheduling() {
    let s = AppData.shared
    s.notificationsEnabled = true
    NotificationScheduler.rescheduleAll()
    let exp = expectation(description: "pending")
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
      UNUserNotificationCenter.current().getPendingNotificationRequests { reqs in
        XCTAssertLessThanOrEqual(reqs.count, 64)
        // Simülatörde izin verilmemişse liste boş olabilir; varsa hepsi gelecekte olmalı
        for r in reqs {
          if let d = (r.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() { XCTAssertGreaterThan(d, Date()) }
        }
        exp.fulfill()
      }
    }
    wait(for: [exp], timeout: 10)
  }
}

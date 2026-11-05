import Foundation
import UserNotifications

/// iOS'ta uygulama arka planda kod çalıştıramadığı için bildirimler günler öncesinden planlanır.
/// Sistem sınırı 64 bekleyen bildirim: önümüzdeki ~6 gün için vakit + sahur/iftar + günlük Ramazan bilgisi.
enum NotificationScheduler {
  static let center = UNUserNotificationCenter.current()

  static func requestPermission(_ done: ((Bool) -> Void)? = nil) {
    center.requestAuthorization(options: [.alert, .sound, .badge]) { ok, _ in
      DispatchQueue.main.async {
        if ok { AppData.shared.notificationsEnabled = true }
        done?(ok)
      }
    }
  }

  static func rescheduleAll() {
    let st = AppData.shared
    center.removeAllPendingNotificationRequests()
    guard st.notificationsEnabled else { return }
    let en = st.language == "en"
    let place = st.locationLabel
    var requests: [UNNotificationRequest] = []
    let now = Date()

    func add(_ id: String, _ date: Date, _ title: String, _ body: String, sound: String?) {
      guard date > now else { return }
      let c = UNMutableNotificationContent()
      c.title = title
      c.body = body
      c.sound = sound.map { UNNotificationSound(named: UNNotificationSoundName($0)) } ?? .default
      if #available(iOS 15.0, *) { c.interruptionLevel = .timeSensitive }
      let comps = DateUtil.cal.dateComponents([.year, .month, .day, .hour, .minute], from: date)
      requests.append(UNNotificationRequest(identifier: id, content: c, trigger: UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)))
    }

    let ezan = st.ezanSound ? "ezan_aksam.caf" : nil
    for offset in 0..<7 {
      let day = DateUtil.addDays(offset, to: now)
      let key = DateUtil.dateKey(day)
      let pt = PrayerCache.timesFor(date: day)
      let fasting = st.isFastingDay(key)
      func at(_ s: String) -> Date { PrayerTimeUtils.date(s, on: day) }

      if st.notifyImsak {
        add("imsak_\(key)", at(pt.imsak), en ? "🌙 Imsak Time" : "🌙 İmsak Vakti",
            en ? "Imsak time for \(place) (\(pt.imsak))." : "\(place) için imsak vakti (\(pt.imsak)).", sound: ezan)
      }
      if st.notifyOgle {
        add("ogle_\(key)", at(pt.ogle), en ? "🕌 Dhuhr Adhan" : "🕌 Öğle Ezanı",
            en ? "Dhuhr prayer time for \(place) (\(pt.ogle))." : "\(place) için öğle namazı vakti girdi (\(pt.ogle)).", sound: ezan)
      }
      if st.notifyIkindi {
        add("ikindi_\(key)", at(pt.ikindi), en ? "🕌 Asr Adhan" : "🕌 İkindi Ezanı",
            en ? "Asr prayer time for \(place) (\(pt.ikindi))." : "\(place) için ikindi namazı vakti girdi (\(pt.ikindi)).", sound: ezan)
      }
      if st.notifyIftar {
        if fasting {
          add("aksam_\(key)", at(pt.aksam), en ? "🕌 Maghrib Adhan • Iftar Time" : "🕌 Akşam Ezanı • İftar Vakti",
              en ? "It's iftar time for \(place) (\(pt.aksam)). May Allah accept your fast! 🤲" : "\(place) için iftar vakti (\(pt.aksam)). Allah orucunuzu kabul etsin! 🤲", sound: ezan)
        } else {
          add("aksam_\(key)", at(pt.aksam), en ? "🕌 Maghrib Adhan" : "🕌 Akşam Ezanı",
              en ? "Maghrib prayer time for \(place) (\(pt.aksam))." : "\(place) için akşam namazı vakti girdi (\(pt.aksam)).", sound: ezan)
        }
      }
      if st.notifyYatsi {
        add("yatsi_\(key)", at(pt.yatsi), en ? "🕌 Isha Adhan" : "🕌 Yatsı Ezanı",
            en ? "Isha prayer time for \(place) (\(pt.yatsi))." : "\(place) için yatsı namazı vakti girdi (\(pt.yatsi)).", sound: ezan)
      }
      if fasting {
        if st.remindBeforeImsak {
          let m = st.remindBeforeImsakMinutes
          add("sahur_\(key)", at(pt.imsak).addingTimeInterval(TimeInterval(-m * 60)),
              en ? "🌙 \(m) minutes left until suhoor ends" : "🌙 Sahura \(m) dakika kaldı",
              AppContent.shared.sahurMotivation.randomElement() ?? "", sound: "sahur_davul.caf")
        }
        if st.remindBeforeIftar {
          let m = st.remindBeforeIftarMinutes
          add("iftar_\(key)", at(pt.aksam).addingTimeInterval(TimeInterval(-m * 60)),
              en ? "🍽️ \(m) minutes until iftar" : "🍽️ İftara \(m) dakika kaldı",
              AppContent.shared.iftarMotivation.randomElement() ?? "", sound: "bildirim_ney.caf")
        }
      }
      if let rd = RamadanCalendar.dayOfRamadan(day) {
        L10n.current = st.language
        add("tip_\(key)", at("10:30"), en ? "🌙 Ramadan Day \(rd) • Daily Tip" : "🌙 Ramazan'ın \(rd). Günü • Günün Bilgisi",
            AppContent.shared.tipForDay(rd).text, sound: "bildirim_ney.caf")
      }
    }
    requests.sort { a, b in
      let da = (a.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? .distantFuture
      let db = (b.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() ?? .distantFuture
      return da < db
    }
    for r in requests.prefix(62) { center.add(r) }
  }

  /// "Bildirimi test et": 10 saniye sonra ezan sesiyle örnek bildirim.
  static func sendTest() {
    let en = AppData.shared.language == "en"
    let c = UNMutableNotificationContent()
    c.title = en ? "✅ Notification test successful" : "✅ Bildirim testi başarılı"
    c.body = en ? "Adhan and reminders will arrive on time on this phone." : "Ezan ve hatırlatıcılar bu telefonda vaktinde gelecek."
    c.sound = UNNotificationSound(named: UNNotificationSoundName("ezan_aksam.caf"))
    center.add(UNNotificationRequest(identifier: "test", content: c, trigger: UNTimeIntervalNotificationTrigger(timeInterval: 10, repeats: false)))
  }

  static func authorizationStatus(_ done: @escaping (UNAuthorizationStatus) -> Void) {
    center.getNotificationSettings { s in DispatchQueue.main.async { done(s.authorizationStatus) } }
  }
}

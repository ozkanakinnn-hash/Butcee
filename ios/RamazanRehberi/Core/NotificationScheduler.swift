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
    // iOS en fazla 64 bekleyen bildirime izin verir: sınır dolana kadar günleri doldur (en çok 30 gün)
    for offset in 0..<30 {
      if requests.count >= 60 { break }
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
    let scheduled = Array(requests.prefix(60))
    for r in scheduled { center.add(r) }
    // Son planlanan günde: bildirimlerin devam etmesi için uygulamayı açma hatırlatması
    if let last = scheduled.last.flatMap({ ($0.trigger as? UNCalendarNotificationTrigger)?.nextTriggerDate() }) {
      let c = UNMutableNotificationContent()
      c.title = en ? "🔔 Keep adhan notifications on" : "🔔 Ezan bildirimleri devam etsin"
      c.body = en ? "Open Ramazan Rehberi once so the next days' prayer notifications are scheduled." : "Sonraki günlerin vakit bildirimleri için Ramazan Rehberi'ni bir kez açın."
      c.sound = .default
      let at = DateUtil.cal.date(bySettingHour: 12, minute: 0, second: 0, of: last) ?? last
      let comps = DateUtil.cal.dateComponents([.year, .month, .day, .hour, .minute], from: at > now ? at : last.addingTimeInterval(60))
      center.add(UNNotificationRequest(identifier: "refresh_reminder", content: c, trigger: UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)))
    }
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

import SwiftUI
import AVFoundation

/// Kısa sesler (iftar topu, ezan önizleme).
enum SoundPlayer {
  private static var player: AVAudioPlayer?
  static func play(_ name: String) {
    guard let url = Bundle.main.url(forResource: name, withExtension: "caf") else { return }
    try? AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
    player = try? AVAudioPlayer(contentsOf: url)
    player?.play()
  }
  static func stop() { player?.stop() }
}

// MARK: - Fener dizisi

struct RamadanLanternGarland: View {
  @Environment(\.palette) var p
  var body: some View {
    Canvas { ctx, size in
      let w = size.width, h = size.height
      var rnd = SeededRandom(seed: 11)
      for _ in 0..<26 {
        let x = rnd.next() * w, y = rnd.next() * h * 0.9
        ctx.fill(Path(ellipseIn: CGRect(x: x, y: y, width: 2, height: 2)), with: .color(p.primary.opacity(0.35 + rnd.next() * 0.4)))
      }
      var rope = Path()
      rope.move(to: CGPoint(x: 0, y: h * 0.12))
      rope.addQuadCurve(to: CGPoint(x: w, y: h * 0.12), control: CGPoint(x: w / 2, y: h * 0.42))
      ctx.stroke(rope, with: .color(p.primary.opacity(0.7)), lineWidth: 1)
      let xs: [CGFloat] = [0.12, 0.31, 0.5, 0.69, 0.88]
      for (i, fx) in xs.enumerated() {
        let x = w * fx
        let t = fx
        let ropeY = h * 0.12 + (1 - pow(2 * t - 1, 2)) * h * 0.15
        let chain: CGFloat = i == 2 ? 18 : (i % 2 == 0 ? 6 : 12)
        let s: CGFloat = i == 2 ? 1.15 : 0.95
        var c = Path(); c.move(to: CGPoint(x: x, y: ropeY)); c.addLine(to: CGPoint(x: x, y: ropeY + chain))
        ctx.stroke(c, with: .color(p.primary.opacity(0.8)), lineWidth: 1)
        let top = ropeY + chain
        let lw = 12 * s, lh = 22 * s
        ctx.fill(Path(ellipseIn: CGRect(x: x - lw * 1.4, y: top - 4, width: lw * 2.8, height: lh * 1.6)), with: .color(Color(hex: 0xFFD56B).opacity(0.12)))
        ctx.fill(Path(CGRect(x: x - lw * 0.35, y: top, width: lw * 0.7, height: 3 * s)), with: .color(p.primary))
        let body = Path(roundedRect: CGRect(x: x - lw / 2, y: top + 3 * s, width: lw, height: lh), cornerRadius: lw / 2)
        ctx.fill(body, with: .linearGradient(Gradient(colors: [Color(hex: 0xFFE9A8), Color(hex: 0xF0B04A)]), startPoint: CGPoint(x: x, y: top), endPoint: CGPoint(x: x, y: top + lh)))
        ctx.stroke(body, with: .color(p.primary), lineWidth: 1)
        ctx.fill(Path(CGRect(x: x - lw * 0.3, y: top + 3 * s + lh, width: lw * 0.6, height: 3 * s)), with: .color(p.primary))
      }
    }
    .frame(height: 64)
  }
}

struct SeededRandom {
  var state: UInt64
  init(seed: UInt64) { state = seed &* 6364136223846793005 &+ 1442695040888963407 }
  mutating func next() -> CGFloat {
    state = state &* 6364136223846793005 &+ 1442695040888963407
    return CGFloat((state >> 33) % 10000) / 10000
  }
}

struct RamadanDayBadge: View {
  @Environment(\.palette) var p
  let day: Int
  var body: some View {
    Text(L10n.isEnglish ? "🌙 Day \(day) of Ramadan" : "🌙 Ramazan'ın \(day). günü")
      .font(.system(size: 15, weight: .bold)).foregroundStyle(p.onPrimary)
      .padding(.horizontal, 16).padding(.vertical, 9)
      .background(Capsule().fill(p.primary))
      .frame(maxWidth: .infinity, alignment: .leading)
  }
}

struct RamadanMahyaBar: View {
  @Environment(\.palette) var p
  let day: Int
  var totalDays = 30

  var text: String {
    let en = L10n.isEnglish
    if day <= 3 { return en ? "WELCOME, O MONTH OF RAMADAN" : "HOŞ GELDİN YA ŞEHR-İ RAMAZAN" }
    if day >= totalDays - 2 { return en ? "FAREWELL, O MONTH OF RAMADAN" : "ELVEDA YA ŞEHR-İ RAMAZAN" }
    let list = en ? ["SULTAN OF ELEVEN MONTHS", "RAMADAN IS BLESSING", "FASTING IS PATIENCE", "THE MONTH OF THE QURAN", "SHARE AND BE BLESSED"]
                  : ["ON BİR AYIN SULTANI", "RAMAZAN BEREKETTİR", "ORUÇ SABIRDIR", "KUR'AN AYI RAMAZAN", "PAYLAŞTIKÇA BEREKET"]
    return list[day % list.count]
  }

  var body: some View {
    ZStack {
      RoundedRectangle(cornerRadius: 20).fill(p.dark ? Color.black.opacity(0.4) : p.primary.opacity(0.85))
      RoundedRectangle(cornerRadius: 20).stroke(p.primary.opacity(0.8), lineWidth: 1)
      HStack {
        Rectangle().fill(p.primary.opacity(0.7)).frame(width: 2)
        Spacer()
        Rectangle().fill(p.primary.opacity(0.7)).frame(width: 2)
      }.padding(.horizontal, 14).padding(.vertical, 2)
      Text(text)
        .font(.system(size: 16, weight: .heavy)).tracking(3)
        .foregroundStyle(Color(hex: 0xFFE29A))
        .shadow(color: Color(hex: 0xFFD56B).opacity(0.9), radius: 6)
        .lineLimit(1).minimumScaleFactor(0.6).padding(.horizontal, 26)
    }
    .frame(height: 46)
  }
}

/// İftar sayacının köşesinde soluk hilal (yazıları örtmez).
struct CrescentWatermark: View {
  @Environment(\.palette) var p
  var body: some View {
    Canvas { ctx, size in
      let r = min(size.width, size.height) * 0.42
      let c = CGPoint(x: size.width - r * 0.55, y: size.height - r * 0.55)
      var outer = Path(ellipseIn: CGRect(x: c.x - r, y: c.y - r, width: 2 * r, height: 2 * r))
      let ic = CGPoint(x: c.x + r * 0.42, y: c.y - r * 0.24)
      let ir = r * 0.86
      outer.addPath(Path(ellipseIn: CGRect(x: ic.x - ir, y: ic.y - ir, width: 2 * ir, height: 2 * ir)))
      ctx.fill(outer, with: .color(p.primary.opacity(0.08)), style: FillStyle(eoFill: true))
    }
    .allowsHitTesting(false)
  }
}

// MARK: - Oruç ilerleme çubuğu

private let fastingQuotes: [(Bi, String)] = [
  (Bi(tr: "Oruç bir kalkandır.", en: "Fasting is a shield."), "Buhârî, Savm 2"),
  (Bi(tr: "Oruçlu için iki sevinç vardır: iftar ettiği an ve Rabbine kavuştuğu an.", en: "The fasting person has two joys: when he breaks his fast and when he meets his Lord."), "Buhârî, Savm 9"),
  (Bi(tr: "Cennette Reyyân denilen bir kapı vardır; oradan yalnızca oruçlular girer.", en: "In Paradise there is a gate called Rayyan; only those who fast enter through it."), "Buhârî, Savm 4"),
  (Bi(tr: "Oruç sizden öncekilere farz kılındığı gibi size de farz kılındı; umulur ki korunursunuz.", en: "Fasting is prescribed for you as it was for those before you, so that you may be mindful of God."), "Bakara, 2/183"),
  (Bi(tr: "Oruçlunun ağız kokusu Allah katında misk kokusundan daha hoştur.", en: "The breath of the fasting person is sweeter to God than the scent of musk."), "Buhârî, Savm 2"),
  (Bi(tr: "Kim inanarak ve karşılığını Allah'tan bekleyerek Ramazan orucunu tutarsa geçmiş günahları bağışlanır.", en: "Whoever fasts Ramadan with faith and hope of reward, his past sins are forgiven."), "Buhârî, Îmân 28"),
  (Bi(tr: "Sabredenlere mükâfatları hesapsız ödenecektir.", en: "Those who are patient will be given their reward without measure."), "Zümer, 39/10"),
  (Bi(tr: "Allah size kolaylık diler, zorluk dilemez.", en: "God wants ease for you, not hardship."), "Bakara, 2/185"),
  (Bi(tr: "Kullarım sana beni sorduklarında, bilsinler ki ben onlara çok yakınım.", en: "When My servants ask you about Me, I am indeed near."), "Bakara, 2/186"),
  (Bi(tr: "Şüphesiz zorlukla beraber bir kolaylık vardır.", en: "Indeed, with hardship comes ease."), "İnşirâh, 94/6"),
  (Bi(tr: "Kim bir oruçluya iftar ettirirse, onun sevabı kadar sevap kazanır.", en: "Whoever gives iftar to a fasting person earns the same reward."), "Tirmizî, Savm 82"),
]

struct FastingProgressCard: View {
  @Environment(\.palette) var p
  let times: PrayerTimes
  let now: Date

  var body: some View {
    let c = DateUtil.cal.dateComponents([.hour, .minute, .second], from: now)
    let nowSec = c.hour! * 3600 + c.minute! * 60 + c.second!
    let start = PrayerTimeUtils.minutes(times.imsak) * 60
    let end = PrayerTimeUtils.minutes(times.aksam) * 60
    if end > start && nowSec >= start && nowSec < end {
      let progress = Double(nowSec - start) / Double(end - start)
      let remain = end - nowSec
      let pct = Int(progress * 100)
      let idx = (DateUtil.dayOfYear(now) * 4 + min(3, Int(progress * 4))) % fastingQuotes.count
      let q = fastingQuotes[idx]
      let en = L10n.isEnglish
      VStack(spacing: 10) {
        HStack {
          Text(en ? "Fast completed: \(pct)%" : "Orucun tamamlanan kısmı: %\(pct)").font(.system(size: 16, weight: .bold))
          Spacer()
          Text((en ? "Iftar in " : "İftara ") + "\(remain / 3600) \(en ? "h" : "sa") \((remain % 3600) / 60) \(en ? "min" : "dk")")
            .font(.system(size: 13, weight: .bold)).foregroundStyle(p.primary)
        }
        GeometryReader { g in
          let wd = g.size.width - 30
          ZStack(alignment: .leading) {
            Capsule().fill(p.surfaceVariant).frame(width: wd, height: 12)
            Capsule().fill(LinearGradient(colors: [p.primary.mix(.white, 0.25), p.primary], startPoint: .leading, endPoint: .trailing))
              .frame(width: max(12, wd * progress), height: 12)
            ZStack {
              Circle().fill(p.primary.opacity(0.3)).frame(width: 30, height: 30)
              Image(systemName: "moon.fill").font(.system(size: 14)).foregroundStyle(p.onPrimary)
                .padding(4).background(Circle().fill(p.primary))
            }
            .offset(x: max(0, wd * progress - 15))
            Image(systemName: "lamp.ceiling.fill").font(.system(size: 18)).foregroundStyle(Color(hex: 0xF0B04A))
              .offset(x: wd + 6)
          }
          .frame(height: 30)
        }
        .frame(height: 30)
        HStack {
          Text((en ? "Imsak " : "İmsak ") + times.imsak).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
          Spacer()
          Text((en ? "Iftar " : "İftar ") + times.aksam).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
        Text("“\(q.0.text)”").font(.system(size: 14.5, weight: .medium)).multilineTextAlignment(.center).padding(.top, 4)
        Text("— \(q.1)").font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
      }
      .padding(16)
      .card(radius: 20)
    }
  }
}

// MARK: - Mihrap kemerli menü kartları (iftar & sahur)

struct MihrabShape: Shape {
  func path(in rect: CGRect) -> Path {
    var path = Path()
    let w = rect.width, h = rect.height, side = w * 0.42
    path.move(to: CGPoint(x: 0, y: h - 24))
    path.addLine(to: CGPoint(x: 0, y: side))
    path.addCurve(to: CGPoint(x: w / 2, y: 0), control1: CGPoint(x: 0, y: side * 0.45), control2: CGPoint(x: w * 0.34, y: side * 0.18))
    path.addCurve(to: CGPoint(x: w, y: side), control1: CGPoint(x: w * 0.66, y: side * 0.18), control2: CGPoint(x: w, y: side * 0.45))
    path.addLine(to: CGPoint(x: w, y: h - 24))
    path.addQuadCurve(to: CGPoint(x: w - 24, y: h), control: CGPoint(x: w, y: h))
    path.addLine(to: CGPoint(x: 24, y: h))
    path.addQuadCurve(to: CGPoint(x: 0, y: h - 24), control: CGPoint(x: 0, y: h))
    path.closeSubpath()
    return path
  }
}

struct MenuMihrabCard: View {
  @Environment(\.palette) var p
  let heading: String      // "İftar" / "Sahur"
  let ramadanDay: Int?
  let courses: [(String, String)]
  let footer: String
  var night = false

  var body: some View {
    let skyTop = p.primary.mix(.black, p.dark ? 0.55 : 0.62)
    let skyBottom = p.primary.mix(.black, p.dark ? 0.78 : 0.82)
    let gold = p.dark ? p.primary : p.primary.mix(.white, 0.45)
    let ink = Color(hex: 0xF3EEE2)
    VStack(spacing: 0) {
      ZStack {
        Circle().fill(skyBottom.opacity(0.6)).frame(width: 124, height: 124)
        Circle().stroke(gold.opacity(0.8), lineWidth: 1).frame(width: 124, height: 124)
        Circle().stroke(gold.opacity(0.45), lineWidth: 0.5).frame(width: 112, height: 112)
        VStack(spacing: 0) {
          Text(t("Günün")).font(.system(size: 11, weight: .semibold)).tracking(2).foregroundStyle(gold)
          Text(heading).font(.custom("Snell Roundhand", size: 32).bold()).foregroundStyle(ink)
          Text(t("Menü Önerisi") + (ramadanDay.map { L10n.isEnglish ? " • Day \($0)" : " • \($0). Gün" } ?? ""))
            .font(.system(size: 10)).foregroundStyle(ink.opacity(0.72))
        }
      }
      .padding(.top, 34)
      Spacer().frame(height: 16)
      ForEach(courses.indices, id: \.self) { i in
        VStack(spacing: 2) {
          Text(courses[i].0).font(.system(size: 11, weight: .bold)).italic().tracking(1.5).foregroundStyle(gold)
          Text(courses[i].1).font(.system(size: 17, design: .serif)).italic().foregroundStyle(ink).multilineTextAlignment(.center)
        }.padding(.vertical, 6)
      }
      Text(footer).font(.system(size: 11)).foregroundStyle(ink.opacity(0.72)).multilineTextAlignment(.center)
        .padding(.horizontal, 10).padding(.vertical, 6)
        .background(RoundedRectangle(cornerRadius: 10).fill(Color.white.opacity(0.06)))
        .padding(.top, 8).padding(.bottom, 20).padding(.horizontal, 22)
    }
    .frame(maxWidth: .infinity)
    .background(
      ZStack {
        LinearGradient(colors: [skyTop, skyBottom], startPoint: .top, endPoint: .bottom)
        Canvas { ctx, size in
          var rnd = SeededRandom(seed: 27)
          for _ in 0..<38 {
            let x = rnd.next() * size.width, y = rnd.next() * size.height * 0.55, r = 0.8 + rnd.next() * 1.8
            ctx.fill(Path(ellipseIn: CGRect(x: x, y: y, width: r, height: r)), with: .color(ink.opacity(0.25 + rnd.next() * 0.5)))
          }
          let c = CGPoint(x: size.width * 0.84, y: size.width * 0.36), r = size.width * 0.055
          ctx.fill(Path(ellipseIn: CGRect(x: c.x - r * 2.2, y: c.y - r * 2.2, width: r * 4.4, height: r * 4.4)), with: .color(gold.opacity(0.18)))
          var moon = Path(ellipseIn: CGRect(x: c.x - r, y: c.y - r, width: 2 * r, height: 2 * r))
          let ic = CGPoint(x: c.x + r * 0.42, y: c.y - r * 0.18), ir = r * 0.86
          moon.addPath(Path(ellipseIn: CGRect(x: ic.x - ir, y: ic.y - ir, width: 2 * ir, height: 2 * ir)))
          ctx.fill(moon, with: .color(gold), style: FillStyle(eoFill: true))
        }
      }
    )
    .clipShape(MihrabShape())
    .overlay(MihrabShape().stroke(gold.opacity(0.7), lineWidth: 1.5))
    .shadow(color: .black.opacity(0.25), radius: 6, y: 3)
    .padding(.horizontal, 16)
  }
}

struct IftarMenuCard: View {
  let ramadanDay: Int?
  var body: some View {
    let m = AppContent.shared.iftarMenu(day: ramadanDay ?? DateUtil.dayOfYear(Date()))
    MenuMihrabCard(heading: L10n.isEnglish ? "Iftar" : "İftar", ramadanDay: ramadanDay,
                   courses: [(t("ÇORBA"), m.soup), (t("ANA YEMEK"), m.mainDish), (t("YAN YEMEK & SALATA"), m.sideDish), (t("TATLI"), m.dessert)],
                   footer: t("Orucunuzu hurma veya suyla açın • Afiyet olsun"))
  }
}

struct SahurMenuCard: View {
  let ramadanDay: Int?
  let imsak: String
  var body: some View {
    let m = AppContent.shared.sahurMenu(day: ramadanDay ?? DateUtil.dayOfYear(Date()))
    MenuMihrabCard(heading: t("Sahur"), ramadanDay: ramadanDay,
                   courses: [(t("SICAK BAŞLANGIÇ"), m.warm.text), (t("PROTEİN"), m.protein.text), (t("TAM TAHIL & SEBZE"), m.grain.text), (t("İÇECEK & MEYVE"), m.drink.text)],
                   footer: (L10n.isEnglish ? "Finish before imsak (\(imsak))" : "İmsaktan (\(imsak)) önce bitirin") + " • " + t("Bol su için, tuzludan kaçının"),
                   night: true)
  }
}

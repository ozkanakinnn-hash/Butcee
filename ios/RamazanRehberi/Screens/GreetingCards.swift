import SwiftUI
import UIKit

struct CardStyle {
  let ink: UInt32, muted: UInt32, arabic: UInt32, panel: UInt32 // panel: AARRGGBB
  let dark: Bool
  static let saray = CardStyle(ink: 0x2B241C, muted: 0x6F5A3E, arabic: 0x1F4D3A, panel: 0xD1FFFCF5, dark: false)
  static let sarayGece = CardStyle(ink: 0xF5EEDD, muted: 0xC9BC9E, arabic: 0xE9C77E, panel: 0xB8140F1C, dark: true)
  static let zumrut = CardStyle(ink: 0x14261D, muted: 0x3F5E4E, arabic: 0x1E6B4F, panel: 0xD6FAFDF8, dark: false)
  static let zumrutGece = CardStyle(ink: 0xF3EFE2, muted: 0xBFD3C6, arabic: 0xE3C46A, panel: 0xB8061A12, dark: true)
  static let iznik = CardStyle(ink: 0x14203A, muted: 0x4A5874, arabic: 0x1A3F7A, panel: 0xDBFFFFFF, dark: false)
  static let iznikGece = CardStyle(ink: 0xF2F5FB, muted: 0xB8C6E2, arabic: 0x8FD9E2, panel: 0xB8071028, dark: true)
  static let gul = CardStyle(ink: 0x2B1E1C, muted: 0x7A5148, arabic: 0x8E2F42, panel: 0xDBFFFAF7, dark: false)
}

struct GreetingMessage: Hashable { let headline: Bi; let body: Bi }

struct GreetingOccasion: Identifiable, Hashable {
  let id: String
  let label: Bi
  let arabic: String
  let bg: String
  let style: CardStyle
  let messages: [GreetingMessage]
  let quote: Bi
  let source: String
  static func == (a: GreetingOccasion, b: GreetingOccasion) -> Bool { a.id == b.id }
  func hash(into h: inout Hasher) { h.combine(id) }

  private static func m(_ a: String, _ b: String, _ c: String, _ d: String) -> GreetingMessage { GreetingMessage(headline: Bi(tr: a, en: b), body: Bi(tr: c, en: d)) }

  static let all: [GreetingOccasion] = [
    GreetingOccasion(id: "cuma", label: Bi(tr: "Cuma", en: "Friday"), arabic: "جمعة مباركة", bg: "card_bg_cuma", style: .saray, messages: [
      m("Hayırlı Cumalar", "Blessed Friday", "Cumanız mübarek, dualarınız kabul olsun.", "May your Friday be blessed and your prayers accepted."),
      m("Cumanız Mübarek Olsun", "Jumu'ah Mubarak", "Rabbim gönlünüze huzur, evinize bereket versin.", "May God grant peace to your heart and blessings to your home."),
      m("Hayırlı Cumalar", "Blessed Friday", "Bu mübarek günde dualarımız birbirimize olsun.", "On this blessed day, let our prayers be for one another."),
    ], quote: Bi(tr: "Üzerine güneş doğan en hayırlı gün cuma günüdür.", en: "The best day on which the sun rises is Friday."), source: "Müslim, Cum'a 17"),
    GreetingOccasion(id: "regaib", label: Bi(tr: "Regaip", en: "Raghaib"), arabic: "ليلة الرغائب", bg: "card_bg_regaib", style: .zumrutGece, messages: [
      m("Regaip Kandiliniz Mübarek Olsun", "Blessed Night of Raghaib", "Üç ayların ilk müjdecisi gecede dualarınız kabul olsun.", "May your prayers be accepted on this first herald of the three holy months."),
      m("Hayırlı Kandiller", "Blessed Kandil", "Rabbim bu gecenin hürmetine rahmetini üzerimizden eksik etmesin.", "For the sake of this night, may God never withhold His mercy from us."),
    ], quote: Bi(tr: "Allah'ım! Recep ve Şaban'ı bize mübarek kıl ve bizi Ramazan'a ulaştır.", en: "O God, bless us in Rajab and Sha'ban and let us reach Ramadan."), source: "Ahmed b. Hanbel, Müsned"),
    GreetingOccasion(id: "mirac", label: Bi(tr: "Miraç", en: "Mi'raj"), arabic: "ليلة المعراج", bg: "card_bg_mirac", style: .iznikGece, messages: [
      m("Miraç Kandiliniz Mübarek Olsun", "Blessed Night of Mi'raj", "Namazın hediye edildiği bu gecede secdeleriniz daim olsun.", "On the night prayer was gifted to us, may your prostrations be constant."),
      m("Hayırlı Kandiller", "Blessed Kandil", "Gönüllerimiz Miraç'ın nuruyla aydınlansın.", "May our hearts be illuminated by the light of Mi'raj."),
    ], quote: Bi(tr: "Kulunu bir gece Mescid-i Haram'dan Mescid-i Aksâ'ya götüren Allah'ın şanı yücedir.", en: "Glory be to Him who took His servant by night from al-Masjid al-Haram to al-Masjid al-Aqsa."), source: "İsrâ, 17/1"),
    GreetingOccasion(id: "berat", label: Bi(tr: "Berat", en: "Bara'ah"), arabic: "ليلة البراءة", bg: "card_bg_berat", style: .zumrutGece, messages: [
      m("Berat Kandiliniz Mübarek Olsun", "Blessed Night of Bara'ah", "Bu gece affa, rahmete ve berata vesile olsun.", "May this night be a means of forgiveness, mercy and salvation."),
      m("Hayırlı Kandiller", "Blessed Kandil", "Rabbim günahlarımızdan beraatimizi nasip etsin.", "May God grant us release from our sins."),
    ], quote: Bi(tr: "Allah'ın rahmetinden ümidinizi kesmeyin. Şüphesiz Allah bütün günahları bağışlar.", en: "Do not despair of God's mercy. Indeed, God forgives all sins."), source: "Zümer, 39/53"),
    GreetingOccasion(id: "ramazan", label: Bi(tr: "Ramazan", en: "Ramadan"), arabic: "رمضان كريم", bg: "card_bg_ramazan", style: .iznik, messages: [
      m("Hoş Geldin On Bir Ayın Sultanı", "Welcome, Sultan of Eleven Months", "Ramazan-ı Şerif'iniz mübarek olsun.", "May your holy Ramadan be blessed."),
      m("Hayırlı Ramazanlar", "Ramadan Kareem", "Oruçlarınız, dualarınız ve iftar sofralarınız bereketli olsun.", "May your fasts, prayers and iftar tables be full of blessing."),
      m("Ramazan Bereketi Evinize Dolsun", "May Ramadan's Blessings Fill Your Home", "Sabrın, rahmetin ve paylaşmanın ayı hayırlara vesile olsun.", "May the month of patience, mercy and sharing bring all goodness."),
    ], quote: Bi(tr: "Ramazan ayı, insanlara yol gösterici olarak Kur'an'ın indirildiği aydır.", en: "The month of Ramadan is the month in which the Quran was revealed as guidance for people."), source: "Bakara, 2/185"),
    GreetingOccasion(id: "kadir", label: Bi(tr: "Kadir", en: "Qadr"), arabic: "ليلة القدر", bg: "card_bg_kadir", style: .sarayGece, messages: [
      m("Kadir Geceniz Mübarek Olsun", "Blessed Laylat al-Qadr", "Bin aydan hayırlı bu gecede dualarınız kabul olsun.", "On this night better than a thousand months, may your prayers be accepted."),
      m("Hayırlı Kadir Geceleri", "Blessed Night of Power", "Allah'ım, sen affedicisin, affı seversin; bizi affet.", "O God, You are forgiving and love forgiveness; forgive us."),
    ], quote: Bi(tr: "Kadir gecesi bin aydan daha hayırlıdır.", en: "The Night of Decree is better than a thousand months."), source: "Kadr, 97/3"),
    GreetingOccasion(id: "ramazan_bayrami", label: Bi(tr: "Ramazan Bayramı", en: "Eid al-Fitr"), arabic: "عيد مبارك", bg: "card_bg_ramazan_bayrami", style: .zumrut, messages: [
      m("Ramazan Bayramınız Mübarek Olsun", "Eid Mubarak", "Bayramınız sevdiklerinizle birlikte huzur ve sevinç dolu geçsin.", "May your Eid be filled with peace and joy with your loved ones."),
      m("İyi Bayramlar", "Happy Eid", "Allah tuttuğumuz oruçları ve ettiğimiz duaları kabul etsin.", "May God accept our fasts and our prayers."),
    ], quote: Bi(tr: "Oruçlu için iki sevinç vardır: Biri iftar ettiği an, diğeri Rabbine kavuştuğu an.", en: "The fasting person has two joys: when he breaks his fast and when he meets his Lord."), source: "Buhârî, Savm 9"),
    GreetingOccasion(id: "kurban_bayrami", label: Bi(tr: "Kurban Bayramı", en: "Eid al-Adha"), arabic: "عيد الأضحى مبارك", bg: "card_bg_kurban_bayrami", style: .saray, messages: [
      m("Kurban Bayramınız Mübarek Olsun", "Blessed Eid al-Adha", "Kurbanlarınız kabul, bayramınız bereketli olsun.", "May your sacrifices be accepted and your Eid be blessed."),
      m("İyi Bayramlar", "Happy Eid", "Paylaştıkça çoğalan bir bayram dileriz.", "Wishing you an Eid that grows richer as it is shared."),
    ], quote: Bi(tr: "Onların ne etleri ne de kanları Allah'a ulaşır; O'na ulaşacak olan ancak sizin takvanızdır.", en: "Neither their meat nor their blood reaches God; what reaches Him is your devotion."), source: "Hac, 22/37"),
    GreetingOccasion(id: "mevlid", label: Bi(tr: "Mevlid", en: "Mawlid"), arabic: "المولد النبوي الشريف", bg: "card_bg_mevlid", style: .gul, messages: [
      m("Mevlid Kandiliniz Mübarek Olsun", "Blessed Mawlid", "Peygamber Efendimizin (s.a.v.) dünyayı şereflendirdiği gece hayırlara vesile olsun.", "May the night the Prophet (pbuh) honoured the world bring all goodness."),
      m("Hayırlı Kandiller", "Blessed Kandil", "Rabbim bizleri O'nun şefaatine nail eylesin.", "May God make us worthy of his intercession."),
    ], quote: Bi(tr: "Biz seni ancak âlemlere rahmet olarak gönderdik.", en: "We have not sent you except as a mercy to the worlds."), source: "Enbiyâ, 21/107"),
    GreetingOccasion(id: "hicri", label: Bi(tr: "Hicri Yılbaşı", en: "Hijri New Year"), arabic: "سنة هجرية مباركة", bg: "card_bg_hicri_yilbasi", style: .iznik, messages: [
      m("Hicri Yeni Yılınız Mübarek Olsun", "Happy Hijri New Year", "Yeni yıl hayırlara, huzura ve bereketlere vesile olsun.", "May the new year bring goodness, peace and blessings."),
    ], quote: Bi(tr: "Üzülme, şüphesiz Allah bizimle beraberdir.", en: "Do not grieve; indeed God is with us."), source: "Tevbe, 9/40"),
    GreetingOccasion(id: "asure", label: Bi(tr: "Aşure", en: "Ashura"), arabic: "يوم عاشوراء", bg: "card_bg_asure", style: .saray, messages: [
      m("Aşure Gününüz Mübarek Olsun", "Blessed Day of Ashura", "Paylaştıkça bereketlenen aşure gibi gönüllerimiz de birleşsin.", "Like ashura that grows richer when shared, may our hearts come together."),
    ], quote: Bi(tr: "Aşure günü orucunun geçmiş bir yılın günahlarına kefaret olmasını Allah'tan umarım.", en: "I hope from God that fasting on Ashura expiates the sins of the previous year."), source: "Müslim, Sıyâm 196"),
  ]

  static func forToday(_ date: Date = Date()) -> GreetingOccasion? {
    let title = IslamicDays.special(on: date)?.title ?? ""
    let map: [(String, String)] = [("Ramazan Bayramı", "ramazan_bayrami"), ("Kurban Bayramı", "kurban_bayrami"), ("Kadir", "kadir"), ("Regaip", "regaib"),
                                   ("Miraç", "mirac"), ("Berat", "berat"), ("Mevlid Kandili", "mevlid"), ("Hicri Yılbaşı", "hicri"), ("Aşure", "asure")]
    if let id = map.first(where: { title.contains($0.0) })?.1 { return all.first { $0.id == id } }
    if DateUtil.cal.component(.weekday, from: date) == 6 { return all.first { $0.id == "cuma" } }
    if RamadanCalendar.isRamadanDay(date: date) { return all.first { $0.id == "ramazan" } }
    return nil
  }
}

/// 1080x1920 hikâye boyutunda kart (Android GreetingCardRenderer ile aynı yerleşim).
enum GreetingCardRenderer {
  static let W: CGFloat = 1080, H: CGFloat = 1920, K: CGFloat = 2
  private static let gold = UIColor(red: 0xA8 / 255, green: 0x76 / 255, blue: 0x2A / 255, alpha: 1)
  private static let goldLight = UIColor(red: 0xF5 / 255, green: 0xD9 / 255, blue: 0x8F / 255, alpha: 1)

  private static func color(_ rgb: UInt32) -> UIColor {
    UIColor(red: CGFloat((rgb >> 16) & 0xFF) / 255, green: CGFloat((rgb >> 8) & 0xFF) / 255, blue: CGFloat(rgb & 0xFF) / 255, alpha: 1)
  }
  private static func argb(_ v: UInt32) -> UIColor {
    UIColor(red: CGFloat((v >> 16) & 0xFF) / 255, green: CGFloat((v >> 8) & 0xFF) / 255, blue: CGFloat(v & 0xFF) / 255, alpha: CGFloat((v >> 24) & 0xFF) / 255)
  }
  private static func font(_ names: [String], _ size: CGFloat, fallback: UIFont) -> UIFont {
    for n in names { if let f = UIFont(name: n, size: size) { return f } }
    return fallback.withSize(size)
  }

  static func render(_ o: GreetingOccasion, _ msg: GreetingMessage, scale: CGFloat = 1) -> UIImage {
    let fmt = UIGraphicsImageRendererFormat()
    fmt.scale = 1
    return UIGraphicsImageRenderer(size: CGSize(width: W * scale, height: H * scale), format: fmt).image { ctx in
      ctx.cgContext.scaleBy(x: scale, y: scale)
      draw(o, msg)
    }
  }

  private static func para(_ align: NSTextAlignment = .center, _ mult: CGFloat) -> NSMutableParagraphStyle {
    let p = NSMutableParagraphStyle(); p.alignment = align; p.lineHeightMultiple = mult; p.lineBreakMode = .byWordWrapping
    return p
  }

  private static func draw(_ o: GreetingOccasion, _ msg: GreetingMessage) {
    let st = o.style
    let accent = st.dark ? goldLight : gold
    if let bg = BundleImage.ui(o.bg) { bg.draw(in: CGRect(x: 0, y: 0, width: W, height: H)) } else {
      (st.dark ? UIColor(white: 0.05, alpha: 1) : UIColor(red: 0.98, green: 0.96, blue: 0.93, alpha: 1)).setFill()
      UIRectFill(CGRect(x: 0, y: 0, width: W, height: H))
    }
    let side: CGFloat = 60 * K
    let width = W - 2 * side
    var arSize: CGFloat = 40 * K, headSize: CGFloat = 28 * K, bodySize: CGFloat = 15 * K
    let top: CGFloat = 500 * K, limit: CGFloat = (728 - 22) * K
    var ar = NSAttributedString(), hd = NSAttributedString(), bd = NSAttributedString()
    var hA: CGFloat = 0, hH: CGFloat = 0, hB: CGFloat = 0
    while true {
      ar = NSAttributedString(string: o.arabic, attributes: [.font: font(["Amiri-Bold"], arSize, fallback: .boldSystemFont(ofSize: arSize)), .foregroundColor: color(st.arabic), .paragraphStyle: para(.center, 1.1)])
      hd = NSAttributedString(string: msg.headline.text, attributes: [.font: font(["PlayfairDisplay-Bold"], headSize, fallback: .boldSystemFont(ofSize: headSize)), .foregroundColor: color(st.ink), .paragraphStyle: para(.center, 1.05)])
      bd = NSAttributedString(string: msg.body.text, attributes: [.font: font(["Inter-Regular"], bodySize, fallback: .systemFont(ofSize: bodySize)), .foregroundColor: color(st.muted), .paragraphStyle: para(.center, 1.3)])
      hA = ceil(ar.boundingRect(with: CGSize(width: width, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin], context: nil).height)
      hH = ceil(hd.boundingRect(with: CGSize(width: width, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin], context: nil).height)
      hB = ceil(bd.boundingRect(with: CGSize(width: width, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin], context: nil).height)
      if top + hA + 2 * K + hH + 8 * K + hB <= limit || headSize < 18 * K { break }
      arSize *= 0.93; headSize *= 0.93; bodySize *= 0.95
    }
    var y = top
    ar.draw(with: CGRect(x: side, y: y, width: width, height: hA), options: [.usesLineFragmentOrigin], context: nil); y += hA + 2 * K
    hd.draw(with: CGRect(x: side, y: y, width: width, height: hH), options: [.usesLineFragmentOrigin], context: nil); y += hH + 8 * K
    bd.draw(with: CGRect(x: side, y: y, width: width, height: hB), options: [.usesLineFragmentOrigin], context: nil)

    // Ayet / hadis paneli
    let pLeft = 58 * K, pRight = W - 58 * K
    let inner = pRight - pLeft - 44 * K
    var qSize: CGFloat = 16 * K
    let src = NSAttributedString(string: o.source.uppercased(with: Locale(identifier: "tr")), attributes: [
      .font: font(["Inter-ExtraBold"], 10.5 * K, fallback: .boldSystemFont(ofSize: 21)), .foregroundColor: accent, .kern: 10.5 * K * 0.19, .paragraphStyle: para(.center, 1)])
    let hS = ceil(src.boundingRect(with: CGSize(width: inner, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin], context: nil).height)
    var q = NSAttributedString(), hQ: CGFloat = 0
    let panelTop: CGFloat = 728 * K, maxBottom: CGFloat = (912 - 30) * K
    while true {
      q = NSAttributedString(string: "“" + o.quote.text + "”", attributes: [
        .font: font(["PlayfairDisplay-Italic", "PlayfairDisplay-MediumItalic"], qSize, fallback: .italicSystemFont(ofSize: qSize)), .foregroundColor: color(st.ink), .paragraphStyle: para(.center, 1.25)])
      hQ = ceil(q.boundingRect(with: CGSize(width: inner, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin], context: nil).height)
      if panelTop + 20 * K + hQ + 8 * K + hS + 14 * K <= maxBottom || qSize < 11 * K { break }
      qSize *= 0.93
    }
    let panelH = 20 * K + hQ + 8 * K + hS + 14 * K
    let rect = CGRect(x: pLeft, y: panelTop, width: pRight - pLeft, height: panelH)
    let rr = UIBezierPath(roundedRect: rect, cornerRadius: 16 * K)
    argb(st.panel).setFill(); rr.fill()
    gold.setStroke(); rr.lineWidth = 1.2 * K; rr.stroke()
    let star = UIBezierPath()
    let c = CGPoint(x: W / 2, y: panelTop), r = 13 * K
    for k in 0..<16 {
      let a = Double.pi / 8 * Double(k) - Double.pi / 2
      let rad = k % 2 == 0 ? r : r * 0.78
      let pt = CGPoint(x: c.x + rad * CGFloat(cos(a)), y: c.y + rad * CGFloat(sin(a)))
      if k == 0 { star.move(to: pt) } else { star.addLine(to: pt) }
    }
    star.close()
    (st.dark ? UIColor(red: 0.08, green: 0.06, blue: 0.09, alpha: 1) : UIColor(red: 0.98, green: 0.96, blue: 0.93, alpha: 1)).setFill(); star.fill()
    gold.setStroke(); star.lineWidth = 1.6 * K; star.stroke()
    q.draw(with: CGRect(x: pLeft + 22 * K, y: panelTop + 20 * K, width: inner, height: hQ), options: [.usesLineFragmentOrigin], context: nil)
    src.draw(with: CGRect(x: pLeft + 22 * K, y: panelTop + 20 * K + hQ + 8 * K, width: inner, height: hS), options: [.usesLineFragmentOrigin], context: nil)

    // Alt bilgi
    let footer = NSAttributedString(string: "✦  " + "RAMAZAN REHBERİ" + "  ✦", attributes: [
      .font: font(["Inter-ExtraBold"], 11 * K, fallback: .boldSystemFont(ofSize: 22)), .foregroundColor: accent, .kern: 11 * K * 0.36, .paragraphStyle: para(.center, 1)])
    footer.draw(with: CGRect(x: 0, y: 912 * K - 4 * K, width: W, height: 40 * K), options: [.usesLineFragmentOrigin], context: nil)
  }
}

struct GreetingCardsView: View {
  @Environment(\.palette) var p
  @State private var occasion = GreetingOccasion.forToday() ?? GreetingOccasion.all[0]
  @State private var index = 0
  @State private var saved = false

  var body: some View {
    ScrollView {
      VStack(spacing: 14) {
        VStack(spacing: 2) {
          Text(L10n.isEnglish ? "Send cards to" : "Sevdiklerinize").font(AppFont.playfair(32))
          Text(L10n.isEnglish ? "your loved ones" : "kart gönderin").font(AppFont.playfair(32)).foregroundStyle(p.primary)
          Text(t("Kandil, bayram ve cuma kartları")).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant).padding(.top, 4)
        }
        TabView(selection: $index) {
          ForEach(occasion.messages.indices, id: \.self) { i in
            CardPreview(occasion: occasion, message: occasion.messages[i]).tag(i).padding(.horizontal, 30)
          }
        }
        .tabViewStyle(.page(indexDisplayMode: .always))
        .frame(height: 520)
        .id(occasion.id)
        FlowChips(items: GreetingOccasion.all, selected: occasion) { occasion = $0; index = 0 }
        HStack(spacing: 10) {
          Button { share() } label: {
            Label(t("Paylaş"), systemImage: "square.and.arrow.up").font(.system(size: 15, weight: .bold)).foregroundStyle(p.onPrimary)
              .frame(maxWidth: .infinity).padding(14).background(RoundedRectangle(cornerRadius: 16).fill(p.primary))
          }
          Button { save() } label: {
            Label(saved ? (L10n.isEnglish ? "Saved" : "Kaydedildi") : (L10n.isEnglish ? "Save" : "Kaydet"), systemImage: saved ? "checkmark" : "arrow.down.to.line")
              .font(.system(size: 15, weight: .bold)).frame(maxWidth: .infinity).padding(14).card(radius: 16)
          }
        }.buttonStyle(.plain)
      }
      .padding(16)
    }
    .screen()
    .navigationTitle(t("Özel Gün Kartları"))
    .navigationBarTitleDisplayMode(.inline)
  }

  var current: GreetingMessage { occasion.messages[min(index, occasion.messages.count - 1)] }

  private func share() {
    let img = GreetingCardRenderer.render(occasion, current)
    ShareHelper.share(items: [img, current.headline.text])
  }

  private func save() {
    UIImageWriteToSavedPhotosAlbum(GreetingCardRenderer.render(occasion, current), nil, nil, nil)
    saved = true
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { saved = false }
  }
}

private struct CardPreview: View {
  let occasion: GreetingOccasion
  let message: GreetingMessage
  @State private var image: UIImage?
  var body: some View {
    Group {
      if let image { Image(uiImage: image).resizable().scaledToFit() } else { ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity) }
    }
    .clipShape(RoundedRectangle(cornerRadius: 18))
    .shadow(color: .black.opacity(0.3), radius: 8, y: 4)
    .task(id: occasion.id + message.headline.tr + L10n.current) {
      let o = occasion, m = message
      image = await Task.detached(priority: .userInitiated) { GreetingCardRenderer.render(o, m, scale: 0.45) }.value
    }
  }
}

private struct FlowChips: View {
  @Environment(\.palette) var p
  let items: [GreetingOccasion]
  let selected: GreetingOccasion
  let onSelect: (GreetingOccasion) -> Void
  var body: some View {
    let rows = stride(from: 0, to: items.count, by: 3).map { Array(items[$0..<min($0 + 3, items.count)]) }
    VStack(spacing: 8) {
      ForEach(rows.indices, id: \.self) { r in
        HStack(spacing: 8) {
          ForEach(rows[r]) { o in
            Button { onSelect(o) } label: {
              Text(o.label.text).font(.system(size: 14, weight: .semibold)).lineLimit(1).minimumScaleFactor(0.8)
                .padding(.horizontal, 14).padding(.vertical, 10).frame(maxWidth: .infinity)
                .foregroundStyle(o.id == selected.id ? p.onPrimary : p.primary)
                .background(Capsule().fill(o.id == selected.id ? p.primary : p.surfaceVariant))
            }.buttonStyle(.plain)
          }
        }
      }
    }
    .padding(12)
    .background(RoundedRectangle(cornerRadius: 22).stroke(p.outline))
  }
}

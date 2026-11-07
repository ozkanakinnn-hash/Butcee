import SwiftUI

// MARK: - Günün İstanbul camisi (fotoğraf + buzlu cam panelde günün sözü)

struct MosqueBanner: View {
  private static let mosques: [(img: String, name: String)] = [
    ("mosque_fatih", "Fatih Camii"),                          // Pazar (weekday 1)
    ("mosque_sultanahmet", "Sultanahmet Camii"),               // Pazartesi
    ("mosque_ayasofya", "Ayasofya-i Kebîr Câmi-i Şerîfi"),     // Salı
    ("mosque_suleymaniye", "Süleymaniye Camii"),               // Çarşamba
    ("mosque_camlica", "Büyük Çamlıca Camii"),                 // Perşembe
    ("mosque_eyup", "Eyüp Sultan Camii"),                      // Cuma
    ("mosque_ortakoy", "Ortaköy Büyük Mecidiye Camii"),        // Cumartesi
  ]

  var body: some View {
    let m = Self.mosques[DateUtil.cal.component(.weekday, from: Date()) - 1]
    let message = AppContent.shared.dailyMessage()
    GeometryReader { geo in
      ZStack {
        BundleImage.image(m.img).resizable().scaledToFill().frame(width: geo.size.width, height: geo.size.height).clipped()
        LinearGradient(stops: [.init(color: .clear, location: 0), .init(color: .clear, location: 0.6), .init(color: .black.opacity(0.65), location: 1)],
                       startPoint: .top, endPoint: .bottom)
        VStack {
          Spacer().frame(height: geo.size.height * 0.5)
          Text("“\(message)”")
            .font(.system(size: 12.5, weight: .semibold)).italic()
            .foregroundStyle(.white).multilineTextAlignment(.center).lineLimit(4)
            .padding(.horizontal, 10).padding(.vertical, 8)
            .frame(maxWidth: .infinity, maxHeight: geo.size.height * 0.34)
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(.white.opacity(0.28), lineWidth: 1))
            .environment(\.colorScheme, .light)
            .padding(.horizontal, 10)
          Spacer(minLength: 0)
          Text(m.name).font(.system(size: 9.5, weight: .medium)).foregroundStyle(.white.opacity(0.88)).lineLimit(1).padding(.bottom, 7)
        }
      }
    }
    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
  }
}

// MARK: - Geri sayım kartı

struct CountdownCard: View {
  @Environment(\.palette) var p
  let result: CountdownResult
  var ramadanWatermark = false
  @State private var showCannon = false

  var body: some View {
    VStack(spacing: 10) {
      Text(result.title).font(.system(size: 15, weight: .bold)).foregroundStyle(p.onBackground).lineLimit(1).minimumScaleFactor(0.7)
      HStack(spacing: 4) {
        unit(result.hours, t("Saat"))
        Text(":").font(.system(size: 20, weight: .bold)).foregroundStyle(p.primary)
        unit(result.minutes, t("Dakika"))
        Text(":").font(.system(size: 20, weight: .bold)).foregroundStyle(p.primary)
        unit(result.seconds, t("Saniye"))
      }
      Text(result.targetTimeName).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      Text(result.targetTimeString).font(.system(size: 22, weight: .bold)).foregroundStyle(p.primary)
      GeometryReader { g in
        ZStack(alignment: .leading) {
          Capsule().fill(p.surfaceVariant)
          Capsule().fill(p.primary).frame(width: max(6, g.size.width * result.progress))
        }
      }.frame(height: 6)
      if result.mode == .iftar {
        Button { showCannon = true } label: {
          Text(t("💥 Topu Test Et")).font(.system(size: 13, weight: .semibold)).foregroundStyle(p.primary)
            .frame(maxWidth: .infinity).padding(.vertical, 9)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(p.outline, lineWidth: 1))
        }.buttonStyle(.plain)
      } else {
        Text("\(t("Sonra")): \(result.nextLabel)").font(.system(size: 12.5, weight: .semibold))
          .frame(maxWidth: .infinity).padding(.vertical, 9)
          .background(RoundedRectangle(cornerRadius: 12).fill(p.surfaceVariant))
      }
    }
    .padding(12)
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(
      ZStack {
        RoundedRectangle(cornerRadius: 22, style: .continuous).fill(p.surface)
        if ramadanWatermark { CrescentWatermark().clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous)) }
      }
    )
    .overlay(RoundedRectangle(cornerRadius: 22, style: .continuous).stroke(p.outline.opacity(0.6), lineWidth: 1))
    .fullScreenCover(isPresented: $showCannon) { CannonView() }
  }

  private func unit(_ v: Int, _ label: String) -> some View {
    VStack(spacing: 2) {
      Text(String(format: "%02d", v)).font(.system(size: 22, weight: .bold).monospacedDigit()).foregroundStyle(p.primary)
      Text(label).font(.system(size: 9)).foregroundStyle(p.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity).padding(.vertical, 7)
    .background(RoundedRectangle(cornerRadius: 10).fill(p.surfaceVariant.opacity(0.8)))
  }
}

/// İftar topu: ses + konfeti.
struct CannonView: View {
  @Environment(\.dismiss) var dismiss
  @State private var burst = false
  var body: some View {
    ZStack {
      Color.black.opacity(0.85).ignoresSafeArea()
      ForEach(0..<40, id: \.self) { i in
        Circle()
          .fill([Color.yellow, .orange, .red, .white][i % 4])
          .frame(width: 8, height: 8)
          .offset(x: burst ? CGFloat.random(in: -180...180) : 0, y: burst ? CGFloat.random(in: -380...120) : 0)
          .opacity(burst ? 0 : 1)
          .animation(.easeOut(duration: 1.6).delay(Double(i) * 0.01), value: burst)
      }
      VStack(spacing: 16) {
        Text("💥").font(.system(size: 90)).scaleEffect(burst ? 1.2 : 0.6).animation(.spring(), value: burst)
        Text(t("İftar vakti! Allah kabul etsin 🤲")).font(.title2.bold()).foregroundStyle(.white).multilineTextAlignment(.center)
        Button(t("Kapat")) { dismiss() }.buttonStyle(.borderedProminent)
      }
    }
    .onAppear {
      SoundPlayer.play("ezan_aksam")
      burst = true
    }
  }
}

// MARK: - Kısayollar (Yakındaki Camiler • Kıble • Zikirmatik)

struct ShortcutsRow: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData

  var body: some View {
    ZStack {
      HStack(spacing: 0) {
        NavigationLink(value: Route.mosques) {
          VStack(alignment: .leading, spacing: 4) {
            iconChip("ic_hub_mosque")
            Spacer(minLength: 0)
            Text(t("Yakındaki Camiler")).font(.system(size: 15, weight: .bold)).foregroundStyle(p.onBackground).lineLimit(2).minimumScaleFactor(0.7)
            Text(t("Harita & yol tarifi")).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
          }
          .padding(14).frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
          .padding(.trailing, 50)
          .background(RoundedRectangle(cornerRadius: 22).fill(p.surface))
          .overlay(RoundedRectangle(cornerRadius: 22).stroke(p.outline.opacity(0.6)))
        }.buttonStyle(.plain)
        Spacer().frame(width: 8)
        NavigationLink(value: Route.zikirmatik) {
          VStack(alignment: .trailing, spacing: 4) {
            iconChip("ic_hub_tasbih")
            Spacer(minLength: 0)
            Text(t("Zikirmatik")).font(.system(size: 15, weight: .bold)).foregroundStyle(p.onBackground).lineLimit(2).minimumScaleFactor(0.7).multilineTextAlignment(.trailing)
            (Text(t("Toplam") + " ").foregroundColor(p.onSurfaceVariant) + Text("\(store.zikirTotal)").foregroundColor(p.primary).bold())
              .font(.system(size: 12))
          }
          .padding(14).frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .trailing)
          .padding(.leading, 50)
          .background(RoundedRectangle(cornerRadius: 22).fill(p.surface))
          .overlay(RoundedRectangle(cornerRadius: 22).stroke(p.outline.opacity(0.6)))
        }.buttonStyle(.plain)
      }
      NavigationLink(value: Route.qibla) {
        ZStack {
          Circle().fill(p.background).frame(width: 128, height: 128)
          Circle().stroke(p.primary.opacity(0.8), style: StrokeStyle(lineWidth: 1.2, dash: [3, 4])).frame(width: 112, height: 112)
          Circle().fill(RadialGradient(colors: [p.primary.mix(.white, 0.25), p.primary], center: .center, startRadius: 4, endRadius: 50)).frame(width: 96, height: 96)
          VStack(spacing: 2) {
            Image(systemName: "location.north.circle").font(.system(size: 34, weight: .light)).foregroundStyle(p.onPrimary)
            Text(t("KIBLE")).font(.system(size: 12, weight: .heavy)).tracking(1.5).foregroundStyle(p.onPrimary)
          }
        }
      }.buttonStyle(.plain)
    }
    .frame(height: 112)
  }

  private func iconChip(_ name: String) -> some View {
    Image(name).resizable().renderingMode(.template).foregroundStyle(p.secondary)
      .frame(width: 22, height: 22).padding(8)
      .background(RoundedRectangle(cornerRadius: 10).fill(p.surfaceVariant))
  }
}

// MARK: - Günün namaz vakitleri

struct PrayerTimesCard: View {
  @Environment(\.palette) var p
  let times: PrayerTimes
  let now: Date

  private let icons = ["moon.haze", "sunrise", "sun.max", "sun.min", "sunset", "moon"]

  var currentIndex: Int {
    let m = DateUtil.cal.component(.hour, from: now) * 60 + DateUtil.cal.component(.minute, from: now)
    let mins = times.all.map { PrayerTimeUtils.minutes($0) }
    var idx = 5
    for (i, v) in mins.enumerated() where m >= v { idx = i }
    if m < mins[0] { idx = 5 }
    return idx
  }

  var body: some View {
    VStack(spacing: 14) {
      HStack {
        Image(systemName: "clock").foregroundStyle(p.primary)
        Text(t("Günün Namaz Vakitleri")).font(.system(size: 18, weight: .bold))
        Spacer()
        Text(DateUtil.trDate(now)).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      }
      HStack(spacing: 6) {
        ForEach(0..<6, id: \.self) { i in
          let sel = i == currentIndex
          VStack(spacing: 8) {
            Image(systemName: icons[i]).font(.system(size: 16))
            Text(t(PrayerTimes.names[i])).font(.system(size: 12, weight: .semibold)).lineLimit(1).minimumScaleFactor(0.7)
            Text(times.all[i]).font(.system(size: 14, weight: .bold).monospacedDigit())
          }
          .foregroundStyle(sel ? p.onPrimary : p.onBackground)
          .frame(maxWidth: .infinity).padding(.vertical, 12)
          .background(RoundedRectangle(cornerRadius: 14).fill(sel ? p.primary : p.surfaceVariant.opacity(0.7)))
        }
      }
    }
    .padding(16).card()
  }
}

// MARK: - Günün manevi iklimi (Ayet / Hadis / Dua)

struct DailySpiritualCard: View {
  @Environment(\.palette) var p
  @State private var tab = 0
  let item = AppContent.shared.spiritualToday()

  var body: some View {
    VStack(spacing: 14) {
      HStack {
        Image(systemName: "book").foregroundStyle(p.primary)
        Text(t("Günün Manevi İklimi")).font(.system(size: 18, weight: .bold))
        Spacer()
        Button { ShareHelper.share(items: [shareText]) } label: {
          Label(t("Paylaş"), systemImage: "square.and.arrow.up").font(.system(size: 13, weight: .semibold))
            .padding(.horizontal, 10).padding(.vertical, 6)
            .background(RoundedRectangle(cornerRadius: 10).fill(p.surfaceVariant))
        }.buttonStyle(.plain).foregroundStyle(p.primary)
      }
      PillTabs(items: [t("📖 Ayet"), t("💬 Hadis"), t("🤲 Dua")], selection: $tab)
      VStack(spacing: 10) {
        if tab == 0 && !item.verseArabic.isEmpty {
          Text(item.verseArabic).font(AppFont.amiri(22)).foregroundStyle(p.primary).multilineTextAlignment(.center)
        }
        Text("“\(text.0)”").font(.system(size: 15)).italic().multilineTextAlignment(.center)
        Text("— \(text.1)").font(.system(size: 12, weight: .semibold)).foregroundStyle(p.onSurfaceVariant)
      }
      .frame(maxWidth: .infinity)
      .padding(.vertical, 6)
    }
    .padding(16).card()
  }

  var text: (String, String) {
    switch tab {
    case 1: return (item.hadithTurkish, item.hadithSource)
    case 2: return (item.prayerTurkish, item.prayerSource)
    default: return (item.verseTurkish, item.verseSource)
    }
  }

  var shareText: String { "“\(text.0)”\n— \(text.1)\n\n" + t("Ramazan Rehberi") }
}

// MARK: - Sevdiklerinizle paylaşın

struct ShareWithLovedOnesCard: View {
  @Environment(\.palette) var p
  var body: some View {
    NavigationLink(value: Route.greetingCards) {
      HStack(spacing: 14) {
        BundleImage.image("card_bg_cuma").resizable().scaledToFill().frame(width: 46, height: 76)
          .clipShape(RoundedRectangle(cornerRadius: 8))
          .overlay(RoundedRectangle(cornerRadius: 8).stroke(p.primary.opacity(0.5), lineWidth: 1))
        VStack(alignment: .leading, spacing: 5) {
          Text(t("Sevdiklerinizle Paylaşın")).font(AppFont.playfair(20)).foregroundStyle(p.onBackground)
          Text(t("Kandil, bayram ve cuma kartları")).font(.system(size: 13)).foregroundStyle(p.primary)
        }
        Spacer()
        Image(systemName: "chevron.right").foregroundStyle(p.primary)
      }
      .padding(14)
      .background(RoundedRectangle(cornerRadius: 22).fill(p.surface))
      .overlay(RoundedRectangle(cornerRadius: 22).stroke(p.primary.opacity(0.6), lineWidth: 1.2))
    }.buttonStyle(.plain)
  }
}

// MARK: - Bayram / Ramazan geri sayımı

struct HolidayCountdownCard: View {
  @Environment(\.palette) var p
  let now: Date
  var body: some View {
    let info = HolidayCountdown.calculate(now)
    VStack(alignment: .leading, spacing: 12) {
      HStack(spacing: 8) {
        Image(systemName: "calendar").foregroundStyle(p.primary)
        Text(info.title).font(.system(size: 18, weight: .bold))
      }
      Text(info.subtitle).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      HStack(spacing: 8) {
        box(info.days, t("Gün")); box(info.hours, t("Saat")); box(info.minutes, t("Dakika")); box(info.seconds, t("Saniye"))
      }
    }
    .padding(16).card()
  }
  private func box(_ v: Int, _ l: String) -> some View {
    VStack(spacing: 4) {
      Text("\(v)").font(.system(size: 22, weight: .bold).monospacedDigit()).foregroundStyle(p.primary)
      Text(l).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity).padding(.vertical, 12)
    .background(RoundedRectangle(cornerRadius: 14).fill(p.surfaceVariant.opacity(0.7)))
  }
}

// MARK: - İsim önerileri

struct NameSuggestionsCard: View {
  @Environment(\.palette) var p
  var body: some View {
    let names = AppContent.shared.namesToday()
    VStack(alignment: .leading, spacing: 10) {
      Text(t("👶 Bugün Doğanlara İsim Önerileri")).font(.system(size: 18, weight: .bold))
      Text(t("Anlamı güzel, Kur'an ve İslam tarihinde geçen isimler")).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      HStack(alignment: .top, spacing: 10) {
        column(L10n.isEnglish ? "👧 Girl" : "👧 Kız", Color(hex: 0xE8789A), names.0, Color(hex: 0x3A2328))
        column(L10n.isEnglish ? "👦 Boy" : "👦 Erkek", Color(hex: 0x6FA8E8), names.1, Color(hex: 0x1E2A3A))
      }
    }
    .padding(16).card()
  }
  private func column(_ title: String, _ accent: Color, _ list: [NameSuggestion], _ darkBg: Color) -> some View {
    VStack(alignment: .leading, spacing: 10) {
      Text(title).font(.system(size: 14, weight: .bold)).foregroundStyle(accent)
      ForEach(list, id: \.name) { n in
        VStack(alignment: .leading, spacing: 2) {
          Text(n.name).font(.system(size: 16, weight: .bold))
          Text(n.meaning).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
      }
    }
    .padding(12).frame(maxWidth: .infinity, alignment: .leading)
    .background(RoundedRectangle(cornerRadius: 16).fill(p.dark ? darkBg : accent.opacity(0.10)))
  }
}

// MARK: - Bayram tebriği

struct FestiveGreetingCard: View {
  @Environment(\.palette) var p
  let festival: Festival
  let name: String
  var body: some View {
    VStack(spacing: 8) {
      Text(festival.emoji).font(.system(size: 40))
      Text(festival.greeting).font(AppFont.playfair(22)).multilineTextAlignment(.center)
      Text(L10n.isEnglish ? "Dear \(name), may your Eid be blessed." : "Sevgili \(name), bayramınız mübarek olsun.")
        .font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity).padding(20)
    .background(RoundedRectangle(cornerRadius: 22).fill(LinearGradient(colors: [p.primary.opacity(0.25), p.surface], startPoint: .top, endPoint: .bottom)))
    .overlay(RoundedRectangle(cornerRadius: 22).stroke(p.primary.opacity(0.6)))
  }
}

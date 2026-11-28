import SwiftUI

// MARK: - Bugün: kılınan vakitler halkası (takip)

struct TodayTrackCard: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  let times: PrayerTimes
  let now: Date
  /// Takip sekmesine geçer
  var open: () -> Void = {}

  var body: some View {
    let _ = store.prayerLogVersion
    let prayed = PrayerLog.prayed(on: DateUtil.dateKey(now))
    let current = PrayerLog.currentVakit(times, now: now)
    let streak = PrayerLog.streak(today: now)
    Button(action: open) {
      VStack(spacing: 8) {
        HStack {
          Text(t("Bugün")).font(.system(size: 14, weight: .bold)).foregroundStyle(p.onSurfaceVariant)
          Spacer()
          Image(systemName: "chevron.right").font(.system(size: 12, weight: .bold)).foregroundStyle(p.primary)
        }
        ZStack {
          PrayerRing(prayed: prayed, current: current.flatMap { prayed.contains($0) ? nil : $0 },
                     color: p.primary, track: p.surfaceVariant, lineWidth: 11)
          VStack(spacing: 0) {
            (Text("\(prayed.count)").font(.system(size: 30, weight: .heavy)).foregroundColor(p.onBackground)
              + Text("/5").font(.system(size: 16, weight: .bold)).foregroundColor(p.onSurfaceVariant))
            Text(t("kılındı")).font(.system(size: 11, weight: .semibold)).foregroundStyle(p.onSurfaceVariant)
          }
        }
        .frame(maxWidth: 136, maxHeight: 136)
        Text(streak > 0 ? "\(streak) \(t("gün seri"))" : t("Kıldıkça işaretleyin"))
          .font(.system(size: 13, weight: .bold)).foregroundStyle(p.primary).lineLimit(1).minimumScaleFactor(0.7)
      }
      .padding(14).frame(maxWidth: .infinity, maxHeight: .infinity).card()
    }
    .buttonStyle(.plain)
    .accessibilityElement(children: .combine)
    .accessibilityLabel(Text("\(t("Bugün")) \(prayed.count) / 5 \(t("kılındı"))"))
  }
}

// MARK: - Kaza özeti (ana sayfa)

struct KazaSummaryCard: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData

  var body: some View {
    NavigationLink(value: Route.kaza) {
      HStack(spacing: 12) {
        VStack(alignment: .leading, spacing: 3) {
          Text(t("Kaza Namazı")).font(.system(size: 16, weight: .bold)).foregroundStyle(p.onBackground)
          Text(store.autoKaza ? t("Kaçırılan vakitler otomatik eklenir") : t("Kıldıkça işaretleyin"))
            .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
        }
        Spacer()
        VStack(alignment: .trailing, spacing: 0) {
          Text("\(store.kazaTotalRemaining)").font(.system(size: 24, weight: .heavy)).foregroundStyle(p.primary)
          Text(t("kalan")).font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
        Image(systemName: "chevron.right").foregroundStyle(p.primary)
      }
      .padding(16).card()
    }
    .buttonStyle(.plain)
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

// MARK: - Kısayollar (Kaza • Kıble • Zikirmatik)

struct ShortcutsRow: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData

  var body: some View {
    ZStack {
      HStack(spacing: 0) {
        NavigationLink(value: Route.kaza) {
          VStack(alignment: .leading, spacing: 4) {
            iconChip("ic_hub_kaza")
            Spacer(minLength: 0)
            Text(t("Kaza Namazı")).font(.system(size: 15, weight: .bold)).foregroundStyle(p.onBackground).lineLimit(2).minimumScaleFactor(0.7)
            (Text(t("Kalan") + " ").foregroundColor(p.onSurfaceVariant) + Text("\(store.kazaTotalRemaining)").foregroundColor(p.primary).bold())
              .font(.system(size: 12))
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
  @EnvironmentObject var store: AppData
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
      let _ = store.prayerLogVersion
      let todayKey = DateUtil.dateKey(now)
      let prayed = PrayerLog.prayed(on: todayKey)
      let entered = Set(PrayerLog.enteredVakits(times, now: now))
      HStack(spacing: 6) {
        ForEach(0..<6, id: \.self) { i in
          let sel = i == currentIndex
          let farz = FarzVakit.from(timeIndex: i)
          let canMark = farz.map { entered.contains($0) } ?? false
          let isPrayed = farz.map { prayed.contains($0) } ?? false
          Button {
            guard let v = farz, canMark else { return }
            store.togglePrayed(v, on: now)
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
          } label: {
            VStack(spacing: 7) {
              Image(systemName: icons[i]).font(.system(size: 16))
              Text(t(PrayerTimes.names[i])).font(.system(size: 12, weight: .semibold)).lineLimit(1).minimumScaleFactor(0.7)
              Text(times.all[i]).font(.system(size: 14, weight: .bold).monospacedDigit())
              if farz != nil {
                Image(systemName: isPrayed ? "checkmark.circle.fill" : "circle")
                  .font(.system(size: 19, weight: .semibold))
                  .foregroundStyle(isPrayed ? (sel ? p.onPrimary : p.primary) : (sel ? p.onPrimary.opacity(0.8) : p.onSurfaceVariant.opacity(canMark ? 0.9 : 0.35)))
              } else {
                Image(systemName: "circle").font(.system(size: 19)).hidden()
              }
            }
            .foregroundStyle(sel ? p.onPrimary : p.onBackground)
            .frame(maxWidth: .infinity).padding(.vertical, 12)
            .background(RoundedRectangle(cornerRadius: 14).fill(sel ? p.primary : p.surfaceVariant.opacity(0.7)))
          }
          .buttonStyle(.plain)
          .disabled(!canMark)
          .accessibilityLabel(Text("\(t(PrayerTimes.names[i])) \(times.all[i])" + (isPrayed ? " ✓" : "")))
        }
      }
      Text(t("Kıldığınız vakte dokunun"))
        .font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        .frame(maxWidth: .infinity, alignment: .leading)
      // Vaktin kaynağı: resmi Diyanet tablosu değilse kullanıcı açıkça uyarılır (sessizce ±1 dk gösterilmez)
      if times.isOfficial || AppData.shared.useCoordinates {
        if times.isOfficial {
          Label(t("Kaynak: Diyanet İşleri Başkanlığı"), systemImage: "checkmark.seal")
            .font(.system(size: 11, weight: .medium)).foregroundStyle(p.onSurfaceVariant)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
      } else {
        Label(t("Yaklaşık vakit: Diyanet tablosu indirilemedi, internete bağlanınca düzelir"), systemImage: "exclamationmark.triangle")
          .font(.system(size: 11, weight: .semibold)).foregroundStyle(p.primary)
          .frame(maxWidth: .infinity, alignment: .leading)
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

  var shareText: String { "“\(text.0)”\n— \(text.1)\n\n" + "Ezan & Vakit" }
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

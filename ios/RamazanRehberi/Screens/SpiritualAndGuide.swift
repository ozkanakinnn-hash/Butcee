import SwiftUI

// MARK: - Ayet & Dua sekmesi

struct SpiritualView: View {
  @Environment(\.palette) var p
  let item = AppContent.shared.spiritualToday()

  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 14) {
        HStack(spacing: 10) {
          Image(systemName: "book").font(.system(size: 22)).foregroundStyle(p.primary)
          Text(t("Günün Ayet & Hadisi")).font(.system(size: 26, weight: .bold))
        }
        Text(t("Gününüzü aydınlatacak ayet-i kerime, hadis-i şerif ve dua.")).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
        VStack(spacing: 8) {
          Image("ic_quran_rahle").resizable().scaledToFit().frame(height: 60)
          Text(t("Mübarek Kur'an İklimi")).font(AppFont.playfair(20))
          Text(t("“Kur'an okuyunuz; zira o, kıyamet gününde okuyanlarına şefaatçi olarak gelecektir.”"))
            .font(.system(size: 13)).italic().multilineTextAlignment(.center).foregroundStyle(p.onSurfaceVariant)
          Text("— Müslim, Müsâfirîn 252").font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity).padding(18).card()
        block(t("📖 GÜNÜN AYETİ"), arabic: item.verseArabic, text: item.verseTurkish, source: item.verseSource)
        block(t("💬 GÜNÜN HADİSİ"), arabic: nil, text: item.hadithTurkish, source: item.hadithSource)
        block(t("🤲 GÜNÜN DUASI"), arabic: nil, text: item.prayerTurkish, source: item.prayerSource)
        VStack(spacing: 6) {
          Text(L10n.isEnglish ? "✨ DIVINE NAME OF THE DAY" : "✨ GÜNÜN ESMASI").font(.system(size: 12, weight: .bold)).tracking(1).foregroundStyle(p.primary)
          Text(item.esmaArabic).font(AppFont.amiri(30, bold: true)).foregroundStyle(p.primary)
          Text(item.esmaTurkish).font(.system(size: 17, weight: .bold))
          Text(item.esmaMeaning).font(.system(size: 14)).multilineTextAlignment(.center).foregroundStyle(p.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity).padding(18).card()
        HStack(spacing: 10) {
          Button { ShareHelper.share(items: [shareText]) } label: {
            Label(t("Paylaş"), systemImage: "square.and.arrow.up").frame(maxWidth: .infinity).padding(14)
              .foregroundStyle(p.onPrimary).background(RoundedRectangle(cornerRadius: 14).fill(p.primary))
          }
          Button { UIPasteboard.general.string = shareText } label: {
            Label(t("Kopyala"), systemImage: "doc.on.doc").frame(maxWidth: .infinity).padding(14).card(radius: 14)
          }
        }.buttonStyle(.plain)
        AdBanner()
      }
      .padding(16)
    }
    .screen()
    .toolbar(.hidden, for: .navigationBar)
  }

  private func block(_ title: String, arabic: String?, text: String, source: String) -> some View {
    VStack(spacing: 10) {
      Text(title).font(.system(size: 12, weight: .bold)).tracking(1).foregroundStyle(p.primary)
      if let a = arabic, !a.isEmpty { Text(a).font(AppFont.amiri(24)).foregroundStyle(p.primary).multilineTextAlignment(.center) }
      Text("“\(text)”").font(.system(size: 16)).italic().multilineTextAlignment(.center)
      Text("— \(source)").font(.system(size: 12, weight: .semibold)).foregroundStyle(p.onSurfaceVariant)
    }
    .frame(maxWidth: .infinity).padding(18).card()
  }

  var shareText: String {
    "📖 \(item.verseTurkish)\n— \(item.verseSource)\n\n💬 \(item.hadithTurkish)\n— \(item.hadithSource)\n\n🤲 \(item.prayerTurkish)\n— \(item.prayerSource)\n\n" + t("Ramazan Rehberi")
  }
}

// MARK: - Ramazan Rehberi

struct RamadanGuideView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var prayers: PrayerService
  @State private var tab = 0

  var body: some View {
    let ramadanDay = RamadanCalendar.dayOfRamadan()
    ScrollView {
      VStack(alignment: .leading, spacing: 14) {
        if let d = ramadanDay {
          RamadanDayBadge(day: d)
          VStack(alignment: .leading, spacing: 6) {
            Text(L10n.isEnglish ? "🌙 Today's Tip" : "🌙 Günün Bilgisi").font(.system(size: 13, weight: .bold)).foregroundStyle(p.primary)
            Text(AppContent.shared.tipForDay(d).text).font(.system(size: 15))
          }.padding(16).frame(maxWidth: .infinity, alignment: .leading).card()
        }
        Text(t("Orucun Faydaları")).font(.system(size: 20, weight: .bold))
        PillTabs(items: [t("Dinî Faydalar"), t("Tıbbi Faydalar")], selection: $tab)
        ForEach(Array((tab == 0 ? AppContent.shared.religiousBenefits : AppContent.shared.medicalBenefits).enumerated()), id: \.offset) { _, b in
          VStack(alignment: .leading, spacing: 6) {
            Text(b.title.text).font(.system(size: 15, weight: .bold)).foregroundStyle(p.primary)
            Text(b.body.text).font(.system(size: 14))
            if !b.source.isEmpty { Text("— \(b.source)").font(.system(size: 11, weight: .semibold)).foregroundStyle(p.onSurfaceVariant) }
          }.padding(14).frame(maxWidth: .infinity, alignment: .leading).card(radius: 16)
        }
        if tab == 1 {
          Text("⚕️ " + AppContent.shared.medicalNote.text).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
            .padding(12).background(RoundedRectangle(cornerRadius: 12).fill(p.tertiary.opacity(0.12)))
        }
        tips(t("İftar Önerileri"), "🍽️", AppContent.shared.iftarTips)
        tips(t("Sahur Önerileri"), "🌙", AppContent.shared.sahurTips)
        IftarMenuCard(ramadanDay: ramadanDay).padding(.horizontal, -16)
        SahurMenuCard(ramadanDay: ramadanDay, imsak: prayers.today.imsak).padding(.horizontal, -16)
        AdBanner()
        PrivacyFooter()
      }
      .padding(16)
    }
    .screen()
    .navigationTitle(t("Ramazan Rehberi"))
  }

  private func tips(_ title: String, _ emoji: String, _ list: [Bi]) -> some View {
    VStack(alignment: .leading, spacing: 10) {
      Text("\(emoji) \(title)").font(.system(size: 18, weight: .bold))
      ForEach(Array(list.enumerated()), id: \.offset) { i, tip in
        HStack(alignment: .top, spacing: 10) {
          Text("\(i + 1)").font(.system(size: 12, weight: .bold)).foregroundStyle(p.onPrimary)
            .frame(width: 22, height: 22).background(Circle().fill(p.primary))
          Text(tip.text).font(.system(size: 14))
        }
      }
    }
    .padding(16).frame(maxWidth: .infinity, alignment: .leading).card()
  }
}

// MARK: - Dahası

struct MoreHubView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData

  struct Item: Identifiable { let id = UUID(); let route: Route; let icon: String; let title: String; let sub: String }

  var items: [Item] {
    let en = L10n.isEnglish
    return [
      Item(route: .ramadanGuide, icon: "ic_hub_ramadan", title: t("Ramazan Rehberi"), sub: en ? "Benefits of fasting\nIftar & Suhoor" : "Orucun Faydaları\nİftar & Sahur"),
      Item(route: .qibla, icon: "ic_hub_qibla", title: t("Kıble Bulucu"), sub: en ? "Compass\nKaaba direction" : "Pusula Sensörü\nKâbe İstikameti"),
      Item(route: .kaza, icon: "ic_hub_kaza", title: t("Kaza Namazı"), sub: en ? "Count your debt\nMark as you pray" : "Borç hesapla\nKıldıkça işaretle"),
      Item(route: .kandil, icon: "ic_hub_kandil", title: t("Kandil Duaları"), sub: en ? "Blessed nights\nDuas & worship" : "Mübarek geceler\nDua & ibadetler"),
      Item(route: .hatim, icon: "ic_hub_hatim", title: t("Hatim Takibi"), sub: en ? "Juz & Surah\nProgress" : "Cüz & Sure\nİlerleme"),
      Item(route: .notificationWizard, icon: "ic_hub_bell", title: t("Bildirim Sihirbazı"), sub: en ? "No adhan?\nSet up & test" : "Ezan gelmiyor mu?\nAyarla & test et"),
      Item(route: .mosques, icon: "ic_hub_mosque", title: t("Yakındaki Camiler"), sub: store.locationLabel + (en ? "\nMap & Navigation" : "\nHarita & Navigasyon")),
      Item(route: .quranList(tab: 0), icon: "ic_hub_quran", title: t("Kur'an-ı Kerim"), sub: en ? "114 Surahs\nPage by page" : "114 Sure\nSayfa sayfa"),
      Item(route: .zikirmatik, icon: "ic_hub_tasbih", title: t("Zikirmatik"), sub: en ? "Free • 33 • 99\nTasbihat" : "Serbest • 33 • 99\nTesbihat"),
      Item(route: .esma, icon: "ic_hub_esma", title: t("Esmâü'l-Hüsnâ"), sub: en ? "99 Names of Allah\nwith meanings" : "Allah'ın 99 ismi\nAnlamlarıyla"),
      Item(route: .greetingCards, icon: "ic_hub_card", title: t("Özel Gün Kartları"), sub: en ? "Friday • Kandil • Eid\nShare with loved ones" : "Cuma • Kandil • Bayram\nSevdiklerinizle paylaşın"),
      Item(route: .widgets, icon: "ic_hub_widget", title: t("Ana Ekran Widget'ları"), sub: en ? "Prayer times\non home screen" : "Namaz vakitleri\nana ekranda"),
      Item(route: .notes, icon: "ic_hub_notes", title: en ? "Spiritual Notes" : "Manevi Notlar", sub: en ? "Your Ramadan\njournal" : "Ramazan\ngünlüğünüz"),
      Item(route: .settings, icon: "ic_hub_settings", title: t("Ayarlar"), sub: en ? "Theme, language\nNotifications" : "Tema, dil\nBildirimler"),
    ]
  }

  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 12) {
        ScreenHeader(title: t("Dahası & Faydalı Araçlar"), subtitle: t("Kur'an-ı Kerim, Kıble bulucu, camiler ve zengin İslami modüller"))
        let cols = [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]
        LazyVGrid(columns: cols, spacing: 10) {
          ForEach(items.prefix(2)) { hubCard($0) }
        }
        if !store.isPremium {
          NavigationLink(value: Route.premium) {
            HStack(spacing: 14) {
              Text("⭐").font(.system(size: 30))
              VStack(alignment: .leading, spacing: 4) {
                Text(t("Reklamsız Premium'a geç")).font(.system(size: 17, weight: .bold))
                Text(t("Reklamları kaldır, uygulamaya destek ol")).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
              }
              Spacer()
              Image(systemName: "chevron.right").foregroundStyle(p.primary)
            }
            .padding(16)
            .background(RoundedRectangle(cornerRadius: 22).fill(p.surface))
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(p.primary.opacity(0.7), lineWidth: 1.2))
          }.buttonStyle(.plain)
        }
        LazyVGrid(columns: cols, spacing: 10) {
          ForEach(items.dropFirst(2)) { hubCard($0) }
        }
        AdBanner()
      }
      .padding(16)
    }
    .screen()
    .toolbar(.hidden, for: .navigationBar)
  }

  private func hubCard(_ it: Item) -> some View {
    NavigationLink(value: it.route) {
      VStack(spacing: 10) {
        ZStack {
          EightPointStar(inner: 0.82).stroke(p.primary.opacity(0.8), lineWidth: 1).frame(width: 64, height: 64)
          Image(it.icon).resizable().renderingMode(.template).scaledToFit().foregroundStyle(p.primary).frame(width: 28, height: 28)
        }
        Text(it.title).font(.system(size: 16, weight: .bold)).multilineTextAlignment(.center).lineLimit(2)
        Text(it.sub).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant).multilineTextAlignment(.center).lineLimit(2)
      }
      .frame(maxWidth: .infinity, minHeight: 170).padding(12).card()
    }.buttonStyle(.plain)
  }
}

// MARK: - Manevi notlar

struct NotesView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var draft = ""

  var body: some View {
    List {
      Section {
        TextField(L10n.isEnglish ? "Write a note, dua or intention…" : "Bir not, dua veya niyet yazın…", text: $draft, axis: .vertical)
        Button(t("Kaydet")) {
          let n = draft.trimmingCharacters(in: .whitespacesAndNewlines)
          guard !n.isEmpty else { return }
          store.notes.insert(DateUtil.trDate(Date()) + " • " + n, at: 0); draft = ""
        }
      }
      Section {
        ForEach(store.notes.indices, id: \.self) { i in Text(store.notes[i]) }
          .onDelete { store.notes.remove(atOffsets: $0) }
      }
    }
    .scrollContentBackground(.hidden)
    .background(p.background.ignoresSafeArea())
    .navigationTitle(L10n.isEnglish ? "Spiritual Notes" : "Manevi Notlar")
  }
}

// MARK: - Widget bilgi ekranı

struct WidgetsInfoView: View {
  @Environment(\.palette) var p
  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 14) {
        Text(L10n.isEnglish
             ? "Add widgets to your home screen:\n1. Touch and hold an empty area on the home screen.\n2. Tap “+” at the top left.\n3. Search for “Ramazan Rehberi”.\n4. Choose: Next prayer & countdown, Today's 5 prayer times, or Daily Inspiration."
             : "Widget'ları ana ekranınıza ekleyin:\n1. Ana ekranda boş bir alana basılı tutun.\n2. Sol üstteki “+” düğmesine dokunun.\n3. “Ramazan Rehberi”ni arayın.\n4. Seçin: Sıradaki namaz & geri sayım, Günün 5 vakti veya Günün Manevi İklimi.")
          .font(.system(size: 15)).padding(16).card()
        Text(L10n.isEnglish ? "Widgets also work on the lock screen (iOS 16+)." : "Widget'lar kilit ekranında da çalışır (iOS 16+).")
          .font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      }.padding(16)
    }
    .screen()
    .navigationTitle(t("Ana Ekran Widget'ları"))
  }
}

import SwiftUI

// MARK: - Sure listesi + Hatim takibi

struct QuranListView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State var initialTab: Int
  @State private var search = ""

  var filtered: [SurahInfo] {
    let q = search.trimmingCharacters(in: .whitespaces)
    if q.isEmpty { return AppContent.shared.surahs }
    return AppContent.shared.surahs.filter {
      $0.nameTurkish.localizedCaseInsensitiveContains(q) || $0.meaning.localizedCaseInsensitiveContains(q)
        || "\($0.number)" == q || $0.juzRangeStr.localizedCaseInsensitiveContains(q) || $0.nameArabic.contains(q)
    }
  }

  var body: some View {
    ScrollView {
      VStack(alignment: .leading, spacing: 12) {
        HStack(spacing: 10) {
          Image("ic_quran_tab").resizable().renderingMode(.template).foregroundStyle(p.primary).frame(width: 26, height: 26)
          VStack(alignment: .leading, spacing: 2) {
            Text(L10n.isEnglish ? "The Holy Quran" : "Kur'an-ı Kerîm").font(.system(size: 24, weight: .bold))
            Text(L10n.isEnglish ? "114 Surahs • 30 Juz • Original order" : "114 Sure • 30 Cüz • Orijinal Sıralama").font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          }
        }
        PillTabs(items: [t("📖 Sureler"), t("🏆 Hatim Takibi")], selection: $initialTab)
        if initialTab == 0 {
          HStack {
            Image(systemName: "magnifyingglass").foregroundStyle(p.onSurfaceVariant)
            TextField(t("Sure adı, cüz no veya anlam ara"), text: $search)
          }
          .padding(14).card(radius: 16)
          if store.lastPage > 1 {
            NavigationLink(value: Route.quranReader(page: store.lastPage)) {
              Label((L10n.isEnglish ? "Continue reading • Page " : "Okumaya devam et • Sayfa ") + "\(store.lastPage)", systemImage: "book.fill")
                .font(.system(size: 14, weight: .semibold)).foregroundStyle(p.onPrimary)
                .frame(maxWidth: .infinity).padding(12).background(RoundedRectangle(cornerRadius: 14).fill(p.primary))
            }.buttonStyle(.plain)
          }
          Text(L10n.isEnglish ? "Surahs in Quran order (\(filtered.count))" : "Kur'an Sırasına Göre Sureler (\(filtered.count))")
            .font(.system(size: 15, weight: .semibold)).foregroundStyle(p.primary)
          LazyVStack(spacing: 10) {
            ForEach(filtered) { s in
              NavigationLink(value: Route.surah(s.number)) { SurahRow(s: s) }.buttonStyle(.plain)
            }
          }
        } else {
          HatimPanel()
        }
      }
      .padding(16)
    }
    .screen()
    .navigationBarTitleDisplayMode(.inline)
  }
}

struct SurahRow: View {
  @Environment(\.palette) var p
  let s: SurahInfo
  var body: some View {
    HStack(spacing: 12) {
      Text("\(s.number)").font(.system(size: 14, weight: .bold)).foregroundStyle(p.primary)
        .frame(width: 36, height: 36).background(Circle().fill(p.surfaceVariant))
      VStack(alignment: .leading, spacing: 4) {
        HStack(spacing: 8) {
          Text(s.nameTurkish).font(.system(size: 17, weight: .bold))
          Text(L10n.isEnglish ? s.juzRangeStr.replacingOccurrences(of: "Cüz", with: "Juz") : s.juzRangeStr)
            .font(.system(size: 11, weight: .semibold)).foregroundStyle(p.primary)
            .padding(.horizontal, 6).padding(.vertical, 2).background(RoundedRectangle(cornerRadius: 6).fill(p.surfaceVariant))
        }
        Text("\(s.meaning) • \(s.totalVerses) \(L10n.isEnglish ? "verses" : "Ayet") • \(L10n.isEnglish ? (s.revelationType == "Mekkî" ? "Meccan" : "Medinan") : s.revelationType)")
          .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
      }
      Spacer()
      Text(s.nameArabic).font(AppFont.amiri(24, bold: true)).foregroundStyle(p.primary)
    }
    .padding(14).card(radius: 18)
  }
}

struct HatimPanel: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var mode = 0
  @State private var confirmComplete = false

  var nextJuz: Int { (1...30).first { !store.readJuz.contains($0) } ?? 30 }

  var body: some View {
    let pct = Int(Double(store.readJuz.count) / 30 * 100)
    VStack(spacing: 14) {
      HStack(spacing: 16) {
        ZStack {
          EightPointStar().stroke(p.primary, lineWidth: 1.5).frame(width: 70, height: 70)
          Circle().trim(from: 0, to: Double(pct) / 100).stroke(p.primary, style: StrokeStyle(lineWidth: 4, lineCap: .round))
            .rotationEffect(.degrees(-90)).frame(width: 86, height: 86)
          Text("%\(pct)").font(.system(size: 17, weight: .bold))
        }
        VStack(alignment: .leading, spacing: 6) {
          Text(L10n.isEnglish ? "Khatm #\(store.hatimCompleted + 1)" : "\(store.hatimCompleted + 1). Hatim").font(.system(size: 19, weight: .bold))
          Text(L10n.isEnglish ? "Next: Juz \(nextJuz)" : "Sıradaki: \(nextJuz). Cüz").font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          Text((L10n.isEnglish ? "🏆 Completed khatms: " : "🏆 Tamamlanan hatim: ") + "\(store.hatimCompleted)")
            .font(.system(size: 12, weight: .semibold)).foregroundStyle(p.primary)
            .padding(.horizontal, 10).padding(.vertical, 5).background(Capsule().fill(p.surfaceVariant))
        }
        Spacer()
      }
      .padding(16).card()
      HStack(spacing: 0) {
        progressBox(L10n.isEnglish ? "Juz progress" : "Cüz İlerlemesi", store.readJuz.count, 30)
        Divider()
        progressBox(L10n.isEnglish ? "Surah progress" : "Sure İlerlemesi", store.readSurahs.count, 114)
      }
      .card(radius: 18)
      PillTabs(items: [L10n.isEnglish ? "Juz" : "Cüz Takibi", L10n.isEnglish ? "Surahs" : "Sure Takibi"], selection: $mode)
      if mode == 0 {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 5), spacing: 10) {
          ForEach(1...30, id: \.self) { j in
            let read = store.readJuz.contains(j)
            Button { store.toggleJuz(j) } label: {
              ZStack {
                EightPointStar(inner: 0.86).fill(read ? p.primary : p.surface)
                EightPointStar(inner: 0.86).stroke(j == nextJuz ? p.secondary : p.outline, lineWidth: j == nextJuz ? 2 : 1)
                Text("\(j)").font(.system(size: 16, weight: .bold)).foregroundStyle(read ? p.onPrimary : p.onBackground)
              }.frame(height: 62)
            }
            .buttonStyle(.plain)
            .contextMenu {
              NavigationLink(value: Route.quranReader(page: QuranText.shared.firstPage(ofJuz: j))) { Label(t("Oku"), systemImage: "book") }
            }
          }
        }
      } else {
        LazyVStack(spacing: 6) {
          ForEach(AppContent.shared.surahs) { s in
            Button { store.toggleSurah(s.number) } label: {
              HStack {
                Image(systemName: store.readSurahs.contains(s.number) ? "checkmark.circle.fill" : "circle").foregroundStyle(p.primary)
                Text("\(s.number). \(s.nameTurkish)")
                Spacer()
                Text(s.nameArabic).font(AppFont.amiri(18)).foregroundStyle(p.primary)
              }
              .padding(.horizontal, 14).padding(.vertical, 10).card(radius: 14)
            }.buttonStyle(.plain)
          }
        }
      }
      HStack(spacing: 10) {
        NavigationLink(value: Route.quranReader(page: QuranText.shared.firstPage(ofJuz: nextJuz))) {
          Text(L10n.isEnglish ? "📖 Read Juz \(nextJuz)" : "📖 \(nextJuz). Cüzü Oku").font(.system(size: 15, weight: .bold))
            .foregroundStyle(p.onPrimary).frame(maxWidth: .infinity).padding(14).background(RoundedRectangle(cornerRadius: 16).fill(p.primary))
        }.buttonStyle(.plain)
        Button { confirmComplete = true } label: {
          Text(L10n.isEnglish ? "Complete khatm" : "Hatmi Tamamla").font(.system(size: 15, weight: .bold))
            .frame(maxWidth: .infinity).padding(14).card(radius: 16)
        }.buttonStyle(.plain)
      }
      Button { store.readJuz = []; store.readSurahs = [] } label: {
        Text(L10n.isEnglish ? "↻ Reset marks of this khatm" : "↻ Bu hatmin işaretlerini sıfırla").font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      }
    }
    .alert(L10n.isEnglish ? "Complete this khatm?" : "Hatmi tamamladınız mı?", isPresented: $confirmComplete) {
      Button(t("Vazgeç"), role: .cancel) {}
      Button(L10n.isEnglish ? "Complete" : "Tamamla") { store.completeHatim() }
    }
  }

  private func progressBox(_ title: String, _ v: Int, _ total: Int) -> some View {
    VStack(alignment: .leading, spacing: 6) {
      Text(title).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
      Text("\(v)/\(total)").font(.system(size: 24, weight: .bold))
      ProgressView(value: Double(v), total: Double(total)).tint(p.primary)
    }.padding(14).frame(maxWidth: .infinity, alignment: .leading)
  }
}

// MARK: - Sayfa sayfa okuyucu (604 sayfa, sağdan sola)

struct QuranReaderView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  let startPage: Int
  @State private var page: Int = 1
  @State private var mode = 0

  var rows: [QuranText.Row] { QuranText.shared.page(page) }

  var body: some View {
    let first = rows.first
    let surahName = first.flatMap { r in AppContent.shared.surahs[safe: r.surah - 1]?.nameTurkish } ?? ""
    VStack(spacing: 10) {
      PillTabs(items: [t("Arapça"), t("Okunuş"), t("Meal")], selection: $mode).padding(.horizontal, 16)
      TabView(selection: $page) {
        ForEach(1...QuranText.pageCount, id: \.self) { pg in
          QuranPageView(page: pg, mode: mode, fontStep: store.readerFontStep)
            .environment(\.layoutDirection, .leftToRight)
            .tag(pg)
        }
      }
      .tabViewStyle(.page(indexDisplayMode: .never))
      .environment(\.layoutDirection, .rightToLeft)
      ProgressView(value: Double(page), total: Double(QuranText.pageCount)).tint(p.primary).padding(.horizontal, 20).padding(.bottom, 6)
    }
    .screen()
    .navigationTitle("")
    .toolbar {
      ToolbarItem(placement: .principal) {
        VStack(spacing: 1) {
          Text(L10n.isEnglish ? "Surah \(surahName)" : "\(surahName) Sûresi").font(.system(size: 17, weight: .bold))
          Text("\(first?.juz ?? 1). \(t("Cüz")) • \(t("Sayfa")) \(page) / \(QuranText.pageCount)").font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
        }
      }
      ToolbarItemGroup(placement: .navigationBarTrailing) {
        Button { store.bookmarkPage = store.bookmarkPage == page ? 0 : page } label: {
          Image(systemName: store.bookmarkPage == page ? "bookmark.fill" : "bookmark")
        }
        Button { store.readerFontStep = (store.readerFontStep + 1) % 4 } label: { Image(systemName: "textformat.size") }
      }
    }
    .onAppear { page = max(1, min(QuranText.pageCount, startPage)) }
    .onChange(of: page) { v in store.lastPage = v }
  }
}

struct QuranPageView: View {
  @Environment(\.palette) var p
  let page: Int
  let mode: Int
  let fontStep: Int

  var body: some View {
    let rows = QuranText.shared.page(page)
    let scale: CGFloat = [0.85, 1.0, 1.15, 1.3][max(0, min(3, fontStep))]
    ScrollView {
      VStack(spacing: 12) {
        HStack {
          Text("\(QuranText.arabicDigits(rows.first?.juz ?? 1)) الجزء").font(AppFont.amiri(13))
          Spacer()
          Text("سورة " + (AppContent.shared.surahs[safe: (rows.first?.surah ?? 1) - 1]?.nameArabic ?? "")).font(AppFont.amiri(13))
        }
        .foregroundStyle(p.onSurfaceVariant)
        ForEach(groups(rows), id: \.0) { surah, items in
          if items.first?.ayah == 1 { surahHeader(surah) }
          if mode == 0 {
            arabicBlock(items, scale: scale)
          } else {
            VStack(alignment: .leading, spacing: 12) {
              ForEach(items, id: \.ayah) { r in
                HStack(alignment: .top, spacing: 10) {
                  Text("\(r.ayah)").font(.system(size: 11, weight: .bold)).foregroundStyle(p.primary).frame(width: 24)
                  Text(mode == 1 ? (L10n.isEnglish ? r.english : r.pronunciation) : (L10n.isEnglish ? r.english : r.meal))
                    .font(.system(size: 17 * scale)).fixedSize(horizontal: false, vertical: true)
                }
              }
            }
          }
        }
        Text("‹  \(QuranText.arabicDigits(page))  ›").font(AppFont.amiri(16)).foregroundStyle(p.primary).padding(.top, 6)
      }
      .padding(18)
    }
    .background(
      RoundedRectangle(cornerRadius: 18).fill(p.surface)
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(p.primary.opacity(0.7), lineWidth: 1.5))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(p.primary.opacity(0.4), lineWidth: 0.8).padding(5))
    )
    .padding(.horizontal, 14)
  }

  private func groups(_ rows: [QuranText.Row]) -> [(Int, [QuranText.Row])] {
    var out: [(Int, [QuranText.Row])] = []
    for r in rows {
      if let last = out.last, last.0 == r.surah { out[out.count - 1].1.append(r) } else { out.append((r.surah, [r])) }
    }
    return out
  }

  private func surahHeader(_ n: Int) -> some View {
    VStack(spacing: 8) {
      Text("سُورَةُ " + (AppContent.shared.surahs[safe: n - 1]?.nameArabic ?? ""))
        .font(AppFont.amiri(20, bold: true)).foregroundStyle(p.primary)
        .frame(maxWidth: .infinity).padding(.vertical, 8)
        .background(Capsule().fill(p.surfaceVariant))
        .overlay(Capsule().stroke(p.primary, lineWidth: 1.5))
      if n != 1 && n != 9 {
        Text(AppContent.shared.basmalaArabic).font(AppFont.amiri(24)).foregroundStyle(p.primary)
      }
    }
  }

  private func arabicBlock(_ items: [QuranText.Row], scale: CGFloat) -> some View {
    var s = AttributedString()
    for r in items {
      var a = AttributedString(r.arabic + " ")
      a.foregroundColor = p.onBackground
      s += a
      var num = AttributedString("﴿\(QuranText.arabicDigits(r.ayah))﴾ ")
      num.foregroundColor = p.primary
      s += num
    }
    return Text(s)
      .font(AppFont.amiri(25 * scale))
      .lineSpacing(10)
      .multilineTextAlignment(.trailing)
      .frame(maxWidth: .infinity, alignment: .trailing)
  }
}

import SwiftUI

private func tr(_ a: String, _ b: String) -> String { L10n.isEnglish ? b : a }

// MARK: - Zikirmatik

struct ZikirmatikView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var pulse = false
  @State private var confirmReset = false
  @State private var savedToast = false

  private let tesbihat: [(String, String, String)] = [
    ("سُبْحَانَ اللّٰهِ", "Sübhânallah", "SubhanAllah"),
    ("اَلْحَمْدُ لِلّٰهِ", "Elhamdülillâh", "Alhamdulillah"),
    ("اَللّٰهُ اَكْبَرُ", "Allâhu Ekber", "Allahu Akbar"),
  ]

  var target: Int? { ["33": 33, "99": 99, "tesbihat": 33][store.zikirMode] }
  var tesbihatDone: Bool { store.zikirMode == "tesbihat" && store.zikirTesbihatStep >= tesbihat.count }

  var body: some View {
    VStack(spacing: 14) {
      HStack(spacing: 0) {
        ForEach([("free", tr("Serbest", "Free")), ("33", "33"), ("99", "99"), ("tesbihat", tr("Tesbihat", "Tasbihat"))], id: \.0) { key, label in
          Button {
            store.zikirMode = key; store.zikirCount = 0; store.zikirTesbihatStep = 0
          } label: {
            Text(label).font(.system(size: 15, weight: .semibold)).frame(maxWidth: .infinity).padding(.vertical, 12)
              .foregroundStyle(store.zikirMode == key ? p.onPrimary : p.onBackground)
              .background(store.zikirMode == key ? p.primary : Color.clear)
          }.buttonStyle(.plain)
        }
      }
      .clipShape(Capsule()).overlay(Capsule().stroke(p.outline))
      ZStack(alignment: .topTrailing) {
        VStack(spacing: 10) {
          Spacer()
          if tesbihatDone {
            Text("✓").font(.system(size: 64)).foregroundStyle(p.primary)
            Text(tr("Tesbihat tamamlandı", "Tasbihat completed")).font(.system(size: 18, weight: .bold))
            Text(tr("Allah kabul etsin 🤲", "May Allah accept it 🤲")).font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
          } else {
            if store.zikirMode == "tesbihat" {
              let s = tesbihat[store.zikirTesbihatStep]
              Text(s.0).font(AppFont.amiri(30)).foregroundStyle(p.primary)
              Text(L10n.isEnglish ? s.2 : s.1).font(.system(size: 16, weight: .semibold))
              Text("\(store.zikirTesbihatStep + 1) / \(tesbihat.count)").font(.system(size: 11)).foregroundStyle(p.onSurfaceVariant)
            }
            Text("\(store.zikirCount)").font(.system(size: 110, weight: .bold, design: .serif)).scaleEffect(pulse ? 1.06 : 1)
            Text(target.map { "/ \($0)" } ?? tr("Saymak için dokunun", "Tap anywhere to count")).font(.system(size: 18)).foregroundStyle(p.onSurfaceVariant)
          }
          Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(RoundedRectangle(cornerRadius: 28).fill(p.surface))
        .contentShape(Rectangle())
        .onTapGesture { tap() }
        Button { if store.zikirCount > 0 || store.zikirTesbihatStep > 0 { confirmReset = true } } label: {
          Label(tr("Sıfırla", "Reset"), systemImage: "arrow.counterclockwise").font(.system(size: 14, weight: .bold)).foregroundStyle(p.primary)
            .padding(.horizontal, 14).padding(.vertical, 9).background(Capsule().fill(p.surfaceVariant))
        }.padding(14)
      }
      Text(tr("Toplam zikir: ", "Total dhikr: ") + "\(store.zikirTotal)").font(.system(size: 13)).foregroundStyle(p.onSurfaceVariant)
      if !store.zikirSaved.isEmpty {
        ScrollView(.horizontal, showsIndicators: false) {
          HStack {
            ForEach(store.zikirSaved.prefix(10), id: \.self) { e in
              let parts = e.split(separator: "|")
              Text(parts.count == 3 ? "\(parts[2]) • \(parts[0])" : e).font(.system(size: 11)).padding(8).card(radius: 10)
            }
          }
        }
      }
    }
    .padding(16)
    .screen()
    .navigationTitle(t("Zikirmatik"))
    .toolbar {
      ToolbarItem(placement: .navigationBarTrailing) {
        Button { save() } label: { Image(systemName: "bookmark") }
      }
    }
    .alert(tr("Sayaç sıfırlansın mı?", "Reset counter?"), isPresented: $confirmReset) {
      Button(tr("Vazgeç", "Cancel"), role: .cancel) {}
      Button(tr("Sıfırla", "Reset"), role: .destructive) { store.zikirCount = 0; store.zikirTesbihatStep = 0 }
    } message: {
      Text(tr("Mevcut sayı (\(store.zikirCount)) sıfırlanacak. Toplam zikir korunur.", "The current count (\(store.zikirCount)) will be reset. Total dhikr is kept."))
    }
    .overlay(alignment: .bottom) {
      if savedToast {
        Text(tr("Kaydedildi", "Saved")).padding(12).background(Capsule().fill(p.primary)).foregroundStyle(p.onPrimary).padding(.bottom, 40)
      }
    }
  }

  private func tap() {
    guard !tesbihatDone else { return }
    store.zikirCount += 1
    store.zikirTotal += 1
    withAnimation(.easeOut(duration: 0.08)) { pulse = true }
    DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { withAnimation { pulse = false } }
    if let tg = target, store.zikirCount >= tg {
      UINotificationFeedbackGenerator().notificationOccurred(.success)
      if store.zikirMode == "tesbihat" { store.zikirTesbihatStep += 1; store.zikirCount = 0 }
      else { save(); store.zikirCount = 0 }
    } else {
      UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
  }

  private func save() {
    let value = store.zikirCount == 0 ? (target ?? 0) : store.zikirCount
    let f = DateFormatter(); f.dateFormat = "dd.MM.yyyy HH:mm"
    store.saveZikir("\(f.string(from: Date()))|\(store.zikirMode)|\(value)")
    savedToast = true
    DispatchQueue.main.asyncAfter(deadline: .now() + 1.4) { savedToast = false }
  }
}

// MARK: - Kaza namazı

struct KazaView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var showCalc = false
  @State private var showReset = false
  @State private var years = ""
  @State private var days = ""
  @State private var vitir = true

  var body: some View {
    ScrollView {
      VStack(spacing: 12) {
        Text(tr("Borcunu gir, kıldıkça işaretle", "Enter your debt, mark as you pray")).font(.system(size: 14)).foregroundStyle(p.onSurfaceVariant)
          .frame(maxWidth: .infinity, alignment: .leading)
        HStack {
          VStack { Text(tr("Kalan kaza", "Remaining")).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant); Text("\(store.kazaTotalRemaining)").font(.system(size: 30, weight: .heavy)).foregroundStyle(p.primary) }.frame(maxWidth: .infinity)
          VStack { Text(tr("Kılınan", "Prayed")).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant); Text("\(store.kazaTotalPrayed)").font(.system(size: 30, weight: .heavy)) }.frame(maxWidth: .infinity)
        }.padding(16).card()
        HStack(spacing: 10) {
          Button { showCalc = true } label: {
            Text(tr("➕ Borç hesapla / ekle", "➕ Calculate / add")).font(.system(size: 14, weight: .bold)).foregroundStyle(p.onPrimary)
              .frame(maxWidth: .infinity).padding(12).background(RoundedRectangle(cornerRadius: 14).fill(p.primary))
          }
          Button { showReset = true } label: {
            Text(tr("↻ Sıfırla", "↻ Reset")).font(.system(size: 14, weight: .bold)).padding(12).card(radius: 14)
          }
        }.buttonStyle(.plain)
        ForEach(KazaVakit.allCases) { v in
          let r = store.kazaRemaining(v), pr = store.kazaPrayed(v)
          HStack(spacing: 10) {
            VStack(alignment: .leading, spacing: 2) {
              HStack { Text(v.name).font(.system(size: 16, weight: .bold)); Text(v.arabic).font(AppFont.amiri(16)).foregroundStyle(p.primary) }
              Text(tr("Kalan \(r) • Kılınan \(pr)", "Remaining \(r) • Prayed \(pr)")).font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
            }
            Spacer()
            roundBtn("−", enabled: r > 0) { store.kazaAdd(v, -1) }
            roundBtn("+", enabled: true) { store.kazaAdd(v, 1) }
            Button { store.kazaMarkPrayed(v); UINotificationFeedbackGenerator().notificationOccurred(.success) } label: {
              Text(tr("✓ Kıldım", "✓ Prayed")).font(.system(size: 12, weight: .bold)).foregroundStyle(p.onPrimary)
                .padding(.horizontal, 10).padding(.vertical, 8).background(Capsule().fill(r > 0 ? p.primary : p.outline))
            }.disabled(r == 0)
          }
          .padding(14).card(radius: 18)
        }
        Text(tr("ⓘ Kaza borcu, ergenlik çağından itibaren kılınmayan namazlar için yaklaşık hesaplanır. Vitir, Hanefî mezhebine göre vaciptir. Kesin bilgi için müftülüklere danışabilirsiniz.",
                "ⓘ Missed prayers are estimated from the age of maturity. Witr is wajib in the Hanafi school. Consult a local mufti for certainty."))
          .font(.system(size: 12)).foregroundStyle(p.onSurfaceVariant)
      }.padding(16)
    }
    .screen()
    .navigationTitle(tr("Kaza Namazı Takibi", "Missed Prayer Tracker"))
    .alert(tr("Kaza borcu ekle", "Add missed prayers"), isPresented: $showCalc) {
      TextField(tr("Yıl", "Years"), text: $years).keyboardType(.numberPad)
      TextField(tr("Gün", "Days"), text: $days).keyboardType(.numberPad)
      Button(tr("Vazgeç", "Cancel"), role: .cancel) {}
      Button(tr("Vitirsiz ekle", "Add without witr")) { add(vitir: false) }
      Button(tr("Ekle (vitir dahil)", "Add (incl. witr)")) { add(vitir: true) }
    } message: {
      Text(tr("Kılınmayan süreyi yazın; her vakte o kadar gün eklenir.", "Enter the missed period; that many days are added to each prayer."))
    }
    .alert(tr("Sıfırla", "Reset"), isPresented: $showReset) {
      Button(tr("Vazgeç", "Cancel"), role: .cancel) {}
      Button(tr("Sil", "Delete"), role: .destructive) { store.kazaReset() }
    } message: { Text(tr("Tüm kaza kayıtları silinecek.", "All missed-prayer records will be deleted.")) }
  }

  private func add(vitir: Bool) {
    let total = (Int(years) ?? 0) * 365 + (Int(days) ?? 0)
    if total > 0 { store.kazaAddDays(total, includeVitir: vitir) }
    years = ""; days = ""
  }

  private func roundBtn(_ s: String, enabled: Bool, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      Text(s).font(.system(size: 18, weight: .bold)).frame(width: 34, height: 34).background(Circle().fill(p.surfaceVariant))
    }.buttonStyle(.plain).disabled(!enabled).opacity(enabled ? 1 : 0.4)
  }
}

import SwiftUI

// MARK: - Konum seçimi (ülke / il / ilçe)

struct LocationPickerView: View {
  @Environment(\.dismiss) var dismiss
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var country = ""
  @State private var city = ""
  @State private var search = ""
  var onDone: (() -> Void)? = nil

  var isTurkey: Bool { country == "Türkiye" }
  var cities: [String] {
    if isTurkey { return AppContent.shared.provinces.map(\.name) }
    return AppContent.shared.countries.first { $0.name == country }?.cities ?? []
  }

  var body: some View {
    NavigationStack {
      List {
        Section {
          Button {
            store.autoLocation = true
            AutoLocator.shared.refreshIfEnabled()
            dismiss()
          } label: { Label(t("📍 Konumumu otomatik bul (GPS)"), systemImage: "location.fill") }
        }
        Section(t("Ülke")) {
          Picker(t("Ülke"), selection: $country) {
            ForEach(AppContent.shared.countries, id: \.name) { Text($0.name).tag($0.name) }
          }
        }
        if city.isEmpty || !isTurkey {
          Section(t("İl")) {
            ForEach(cities.filter { search.isEmpty || $0.localizedCaseInsensitiveContains(search) }, id: \.self) { c in
              Button {
                if isTurkey { city = c } else { save(city: c, district: c) }
              } label: { row(c, selected: store.province == c) }
            }
          }
        } else {
          Section {
            Button { city = "" } label: { Label(city, systemImage: "chevron.left") }
          }
          Section(t("İlçe")) {
            let districts = AppContent.shared.provinces.first { $0.name == city }?.districts ?? []
            ForEach(districts.filter { search.isEmpty || $0.localizedCaseInsensitiveContains(search) }, id: \.self) { d in
              Button { save(city: city, district: d) } label: { row(d, selected: store.district == d && store.province == city) }
            }
          }
        }
      }
      .searchable(text: $search)
      .navigationTitle(t("Konum Seç"))
      .navigationBarTitleDisplayMode(.inline)
      .toolbar { ToolbarItem(placement: .cancellationAction) { Button(t("Kapat")) { dismiss() } } }
      .onAppear { if country.isEmpty { country = store.country } }
      .onChange(of: country) { _ in city = ""; search = "" }
    }
  }

  private func row(_ s: String, selected: Bool) -> some View {
    HStack { Text(s).foregroundStyle(p.onBackground); Spacer(); if selected { Image(systemName: "checkmark").foregroundStyle(p.primary) } }
  }

  private func save(city: String, district: String) {
    store.applyManualLocation(country: country, province: city, district: district)
    onDone?()
    dismiss()
  }
}

// MARK: - Ayarlar

struct SettingsView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var showLocation = false
  @State private var editingName = false
  @State private var nameDraft = ""

  var body: some View {
    Form {
      Section(t("Kullanıcı Profili")) {
        HStack {
          Image(systemName: "person.fill").foregroundStyle(p.primary)
          if editingName {
            TextField(t("Hitap Edilecek İsim"), text: $nameDraft)
            Button(t("Kaydet")) { store.userName = nameDraft.trimmingCharacters(in: .whitespaces); editingName = false }
          } else {
            VStack(alignment: .leading) {
              Text(store.userName.isEmpty ? t("Hitap Edilecek İsim") : store.userName)
              Text(t("Ana sayfa selamlama hitabı")).font(.caption).foregroundStyle(p.onSurfaceVariant)
            }
            Spacer()
            Button { nameDraft = store.userName; editingName = true } label: { Image(systemName: "pencil") }
          }
        }
      }
      Section(t("Konum")) {
        Button { showLocation = true } label: {
          HStack {
            Image(systemName: "mappin.and.ellipse").foregroundStyle(p.primary)
            VStack(alignment: .leading) {
              Text(store.locationLabel).foregroundStyle(p.onBackground)
              Text(store.country).font(.caption).foregroundStyle(p.onSurfaceVariant)
            }
            Spacer()
            Text(t("Değiştir ›")).foregroundStyle(p.primary)
          }
        }
        Toggle(isOn: $store.autoLocation) {
          VStack(alignment: .leading) {
            Text(t("📍 Konumu otomatik takip et (GPS)"))
            Text(store.useCoordinates ? t("(GPS koordinatına göre)") : t("Başka şehre gidince namaz vakitleri kendiliğinden güncellenir"))
              .font(.caption).foregroundStyle(p.onSurfaceVariant)
          }
        }
      }
      Section {
        NavigationLink(value: Route.premium) { Label(t("Reklamsız Premium"), systemImage: "star.fill") }
        NavigationLink(value: Route.notificationWizard) { Label(t("Bildirim Sihirbazı • Ezan gelmiyor mu?"), systemImage: "bell.badge") }
      }
      Section(t("Namaz Vakitleri & Hatırlatıcı Barları")) {
        Toggle(isOn: Binding(get: { store.notificationsEnabled }, set: { v in
          if v { NotificationScheduler.requestPermission() } else { store.notificationsEnabled = false }
        })) {
          VStack(alignment: .leading) {
            Text(t("Tüm Vakit Bildirimleri"))
            Text(t("Ezan ve iftar bildirimlerini aç / kapat")).font(.caption).foregroundStyle(p.onSurfaceVariant)
          }
        }
        if store.notificationsEnabled {
          Toggle(t("Sahur / İmsak Vakti"), isOn: $store.notifyImsak)
          Toggle(t("Öğle Namazı"), isOn: $store.notifyOgle)
          Toggle(t("İkindi Namazı"), isOn: $store.notifyIkindi)
          Toggle(t("İftar / Akşam Ezanı"), isOn: $store.notifyIftar)
          Toggle(t("Yatsı / Teravih Vakti"), isOn: $store.notifyYatsi)
          Toggle(t("Sahur hatırlatıcısı"), isOn: $store.remindBeforeImsak)
          if store.remindBeforeImsak {
            Stepper("\(t("İmsaktan önce")): \(store.remindBeforeImsakMinutes) \(L10n.isEnglish ? "min" : "dk")", value: $store.remindBeforeImsakMinutes, in: 5...120, step: 5)
          }
          Toggle(t("İftar hatırlatıcısı"), isOn: $store.remindBeforeIftar)
          if store.remindBeforeIftar {
            Stepper("\(t("İftardan önce")): \(store.remindBeforeIftarMinutes) \(L10n.isEnglish ? "min" : "dk")", value: $store.remindBeforeIftarMinutes, in: 5...60, step: 5)
          }
        }
      }
      Section(t("Bildirim Sesi")) {
        Toggle(isOn: $store.ezanSound) {
          VStack(alignment: .leading) {
            Text(t("Ezan Sesi"))
            Text(t("Tüm namaz vakti bildirimlerinde çalar")).font(.caption).foregroundStyle(p.onSurfaceVariant)
          }
        }
        Button(t("▶ Dinle")) { SoundPlayer.play("ezan_aksam") }
      }
      Section(t("Dil / Language")) {
        Picker("", selection: $store.language) {
          Text("🇹🇷 Türkçe").tag("tr")
          Text("🇬🇧 English").tag("en")
        }.pickerStyle(.segmented)
      }
      Section(t("Görünüm & Tema")) {
        Picker(t("Renk Teması"), selection: $store.colorTheme) {
          ForEach(AppColorTheme.allCases, id: \.self) { Text($0.title).tag($0) }
        }.pickerStyle(.segmented)
        Picker(t("Tema"), selection: $store.themeMode) {
          Text(t("☀️ Aydınlık")).tag(AppThemeMode.LIGHT)
          Text(t("🌙 Koyu (Gece Siyahı)")).tag(AppThemeMode.DARK)
          Text(t("📱 Sistem Teması")).tag(AppThemeMode.SYSTEM)
        }
      }
      Section {
        NavigationLink(value: Route.widgets) { Label(t("Ana Ekran Widget'ları"), systemImage: "square.grid.2x2") }
        if Ads.privacyOptionsRequired {
          Button { Ads.showPrivacyOptions() } label: { Label(t("Reklam gizlilik seçenekleri"), systemImage: "hand.raised") }
        }
        Link(t("Gizlilik Politikası"), destination: URL(string: "https://ozkanakinnn-hash.github.io/Butcee/privacy-policy.html")!)
      } footer: {
        Text(t("🔒 Verileriniz yalnızca cihazınızda yerel olarak saklanmaktadır.")).frame(maxWidth: .infinity).padding(.top, 8)
      }
    }
    .scrollContentBackground(.hidden)
    .background(p.background.ignoresSafeArea())
    .navigationTitle(t("Ayarlar & Özelleştirme"))
    .navigationBarTitleDisplayMode(.inline)
    .sheet(isPresented: $showLocation) { LocationPickerView() }
  }
}

// MARK: - İlk açılış

struct OnboardingView: View {
  @Environment(\.palette) var p
  @EnvironmentObject var store: AppData
  @State private var step = 0
  @State private var name = ""
  @State private var showLocation = false

  var body: some View {
    VStack(spacing: 22) {
      Spacer()
      if let icon = UIImage(named: "Logo") {
        Image(uiImage: icon).resizable().frame(width: 110, height: 110).clipShape(RoundedRectangle(cornerRadius: 26))
      } else {
        BundleImage.image("mosque_camlica").resizable().scaledToFill().frame(width: 110, height: 110).clipShape(RoundedRectangle(cornerRadius: 26))
      }
      Text(t("Ramazan Rehberi")).font(AppFont.playfair(32))
      switch step {
      case 0:
        Text(t("Namaz vakitleri, iftar & sahur sayacı, Kur'an-ı Kerim, kıble ve daha fazlası.")).multilineTextAlignment(.center).foregroundStyle(p.onSurfaceVariant)
        Picker("", selection: $store.language) {
          Text("🇹🇷 Türkçe").tag("tr")
          Text("🇬🇧 English").tag("en")
        }.pickerStyle(.segmented).padding(.horizontal, 40)
        primary(t("Başlayalım")) { step = 1 }
      case 1:
        Text(t("Size nasıl hitap edelim?")).font(.headline)
        TextField(t("Hitap Edilecek İsim"), text: $name).textFieldStyle(.roundedBorder).padding(.horizontal, 40)
        primary(t("Devam")) { store.userName = name.trimmingCharacters(in: .whitespaces); step = 2 }
      case 2:
        Text(t("Namaz vakitleri için konumunuzu seçin")).font(.headline).multilineTextAlignment(.center)
        Button { showLocation = true } label: {
          Label(store.locationLabel, systemImage: "mappin.and.ellipse").padding().frame(maxWidth: .infinity).card(radius: 16)
        }.buttonStyle(.plain).padding(.horizontal, 40)
        primary(t("Devam")) { step = 3 }
      default:
        Text(t("Ezan ve iftar vakitlerinde bildirim almak ister misiniz?")).font(.headline).multilineTextAlignment(.center).padding(.horizontal, 30)
        primary(t("Bildirim izni ver")) {
          NotificationScheduler.requestPermission { _ in store.onboardingCompleted = true }
        }
        Button(t("Daha sonra")) { store.onboardingCompleted = true }.foregroundStyle(p.onSurfaceVariant)
      }
      Spacer()
    }
    .padding()
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .screen()
    .sheet(isPresented: $showLocation) { LocationPickerView() }
  }

  private func primary(_ title: String, action: @escaping () -> Void) -> some View {
    Button(action: action) {
      Text(title).font(.headline).foregroundStyle(p.onPrimary).frame(maxWidth: .infinity).padding()
        .background(RoundedRectangle(cornerRadius: 16).fill(p.primary))
    }.padding(.horizontal, 40)
  }
}

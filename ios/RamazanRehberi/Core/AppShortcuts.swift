import AppIntents

/// Siri ve Kestirmeler: "Ezan & Vakit sıradaki vakit", "Ezan & Vakit kıldım".
/// (Yalnızca uygulama hedefinde; widget hedefine eklenmez.)
@available(iOS 16.0, *)
struct EzanVakitShortcuts: AppShortcutsProvider {
  static var appShortcuts: [AppShortcut] {
    AppShortcut(
      intent: NextPrayerIntent(),
      phrases: [
        "\(.applicationName) sıradaki vakit",
        "\(.applicationName) vakte ne kadar var",
        "Next prayer in \(.applicationName)",
      ],
      shortTitle: "Sıradaki vakit",
      systemImageName: "clock"
    )
    AppShortcut(
      intent: MarkPrayedIntent(),
      phrases: [
        "\(.applicationName) kıldım",
        "\(.applicationName) namazı kıldım",
        "Mark prayed in \(.applicationName)",
      ],
      shortTitle: "Kıldım",
      systemImageName: "checkmark.circle"
    )
  }
}

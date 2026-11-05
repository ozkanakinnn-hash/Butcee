import SwiftUI

extension Color {
  init(hex: UInt32, alpha: Double = 1) {
    self.init(.sRGB, red: Double((hex >> 16) & 0xFF) / 255, green: Double((hex >> 8) & 0xFF) / 255, blue: Double(hex & 0xFF) / 255, opacity: alpha)
  }
  /// İki renk arası karışım (Compose lerp karşılığı).
  func mix(_ other: Color, _ t: Double) -> Color {
    let a = UIColor(self), b = UIColor(other)
    var r1: CGFloat = 0, g1: CGFloat = 0, b1: CGFloat = 0, a1: CGFloat = 0
    var r2: CGFloat = 0, g2: CGFloat = 0, b2: CGFloat = 0, a2: CGFloat = 0
    a.getRed(&r1, green: &g1, blue: &b1, alpha: &a1)
    b.getRed(&r2, green: &g2, blue: &b2, alpha: &a2)
    let k = CGFloat(t)
    return Color(.sRGB, red: Double(r1 + (r2 - r1) * k), green: Double(g1 + (g2 - g1) * k), blue: Double(b1 + (b2 - b1) * k), opacity: Double(a1 + (a2 - a1) * k))
  }
}

/// Android'deki Material renk şemalarının birebir karşılığı (Saray, İznik, Zümrüt × açık/koyu).
struct Palette {
  let dark: Bool
  let primary: Color
  let onPrimary: Color
  let secondary: Color
  let tertiary: Color
  let background: Color
  let surface: Color
  let surfaceVariant: Color
  let onBackground: Color
  let onSurfaceVariant: Color
  let outline: Color

  static func of(_ theme: AppColorTheme, dark: Bool) -> Palette {
    if dark {
      let tint: Color, p: Color, s: Color, t3: Color
      switch theme {
      case .SARAY: p = Color(hex: 0xE5B869); s = Color(hex: 0x7CA885); t3 = Color(hex: 0xFFD56B); tint = Color(hex: 0x1A1712)
      case .IZNIK: p = Color(hex: 0x8DB1EA); s = Color(hex: 0x55C6CF); t3 = Color(hex: 0xE8705F); tint = Color(hex: 0x10141C)
      case .ZUMRUT: p = Color(hex: 0x6FD3A2); s = Color(hex: 0xE5B869); t3 = Color(hex: 0xE5C55A); tint = Color(hex: 0x0E1511)
      }
      return Palette(dark: true, primary: p, onPrimary: Color(hex: 0x050505), secondary: s, tertiary: t3,
                     background: Color(hex: 0x050505), surface: Color(hex: 0x0E0E0E).mix(tint, 0.5),
                     surfaceVariant: Color(hex: 0x181818).mix(tint, 0.4), onBackground: Color(hex: 0xEDEBE6),
                     onSurfaceVariant: Color(hex: 0xA6A39C), outline: Color(hex: 0x2A2A2A))
    }
    switch theme {
    case .SARAY:
      return Palette(dark: false, primary: Color(hex: 0x9E6B20), onPrimary: Color(hex: 0xFDFBF7), secondary: Color(hex: 0x3F634B), tertiary: Color(hex: 0xC98A2C),
                     background: Color(hex: 0xFDFBF7), surface: Color(hex: 0xF7F3E9), surfaceVariant: Color(hex: 0xEFE8D8),
                     onBackground: Color(hex: 0x2B2823), onSurfaceVariant: Color(hex: 0x6B665D), outline: Color(hex: 0xE5DDD0))
    case .IZNIK:
      return Palette(dark: false, primary: Color(hex: 0x1A3F7A), onPrimary: Color(hex: 0xFAFAF6), secondary: Color(hex: 0x2A8C9C), tertiary: Color(hex: 0xC0392B),
                     background: Color(hex: 0xFAFAF6), surface: Color(hex: 0xF2F5F8), surfaceVariant: Color(hex: 0xE3EAF2),
                     onBackground: Color(hex: 0x1B2230), onSurfaceVariant: Color(hex: 0x55627A), outline: Color(hex: 0xCBD5E1))
    case .ZUMRUT:
      return Palette(dark: false, primary: Color(hex: 0x1E6B4F), onPrimary: Color(hex: 0xF5F8F3), secondary: Color(hex: 0xA9831B), tertiary: Color(hex: 0xC9A227),
                     background: Color(hex: 0xF5F8F3), surface: Color(hex: 0xEDF3EE), surfaceVariant: Color(hex: 0xDFEAE2),
                     onBackground: Color(hex: 0x1A241F), onSurfaceVariant: Color(hex: 0x52665A), outline: Color(hex: 0xC6D6CB))
    }
  }
}

private struct PaletteKey: EnvironmentKey {
  static let defaultValue = Palette.of(.SARAY, dark: true)
}

extension EnvironmentValues {
  var palette: Palette {
    get { self[PaletteKey.self] }
    set { self[PaletteKey.self] = newValue }
  }
}

enum AppFont {
  static func amiri(_ size: CGFloat, bold: Bool = false) -> Font { .custom(bold ? "Amiri-Bold" : "Amiri-Regular", size: size) }
  static func playfair(_ size: CGFloat) -> Font { .custom("PlayfairDisplay-Bold", size: size) }
  static func playfairItalic(_ size: CGFloat) -> Font {
    for n in ["PlayfairDisplay-Italic", "PlayfairDisplay-MediumItalic", "PlayfairDisplayItalic-Regular"] where UIFont(name: n, size: size) != nil {
      return .custom(n, size: size)
    }
    return .system(size: size, design: .serif).italic()
  }
  static func inter(_ size: CGFloat, bold: Bool = false) -> Font { .custom(bold ? "Inter-ExtraBold" : "Inter-Regular", size: size) }
}

/// Uygulamanın standart kart görünümü.
struct CardBackground: ViewModifier {
  @Environment(\.palette) var p
  var radius: CGFloat = 24
  func body(content: Content) -> some View {
    content
      .background(RoundedRectangle(cornerRadius: radius, style: .continuous).fill(p.surface))
      .overlay(RoundedRectangle(cornerRadius: radius, style: .continuous).stroke(p.outline.opacity(p.dark ? 0.6 : 1), lineWidth: 1))
  }
}

extension View {
  func card(radius: CGFloat = 24) -> some View { modifier(CardBackground(radius: radius)) }
}

/// Sekiz köşeli yıldız (Dahası ikon çerçevesi, hatim rozetleri).
struct EightPointStar: Shape {
  var inner: CGFloat = 0.78
  func path(in rect: CGRect) -> Path {
    var path = Path()
    let c = CGPoint(x: rect.midX, y: rect.midY)
    let r = min(rect.width, rect.height) / 2
    for k in 0..<16 {
      let a = Double.pi / 8 * Double(k) - Double.pi / 2
      let rr = k % 2 == 0 ? r : r * inner
      let pt = CGPoint(x: c.x + rr * CGFloat(cos(a)), y: c.y + rr * CGFloat(sin(a)))
      if k == 0 { path.move(to: pt) } else { path.addLine(to: pt) }
    }
    path.closeSubpath()
    return path
  }
}

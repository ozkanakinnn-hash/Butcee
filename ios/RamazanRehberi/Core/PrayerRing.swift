import SwiftUI

/// Beş vakit halkası: her farz vakit bir yay. Kılınanlar dolu, sıradaki/şimdiki vurgulu, diğerleri soluk.
/// Uygulama, widget ve Canlı Etkinlik aynı çizimi kullanır.
struct PrayerRing: View {
  var prayed: Set<FarzVakit>
  var current: FarzVakit?
  var color: Color
  var track: Color
  var lineWidth: CGFloat = 10
  /// Yaylar arası boşluk (derece)
  var gapDegrees: Double = 14

  var body: some View {
    ZStack {
      ForEach(FarzVakit.allCases) { v in
        let seg = 1.0 / 5.0
        let gap = gapDegrees / 360.0
        let from = Double(v.rawValue) * seg + gap / 2
        let to = Double(v.rawValue + 1) * seg - gap / 2
        Circle()
          .trim(from: from, to: to)
          .stroke(style(for: v), style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
          .rotationEffect(.degrees(-90))
      }
    }
    .padding(lineWidth / 2)
  }

  private func style(for v: FarzVakit) -> Color {
    if prayed.contains(v) { return color }
    if v == current { return color.opacity(0.45) }
    return track
  }
}

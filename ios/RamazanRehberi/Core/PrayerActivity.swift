import Foundation
#if canImport(ActivityKit)
import ActivityKit

/// Canlı Etkinlik (kilit ekranı + Dynamic Island): sıradaki vakte geri sayım. Uygulama ve widget ortak kullanır.
@available(iOS 16.1, *)
struct PrayerActivityAttributes: ActivityAttributes {
  struct ContentState: Codable, Hashable {
    /// Sıradaki vaktin adı (ör. "Akşam")
    var name: String
    /// "HH:mm"
    var time: String
    /// Geri sayımın hedefi ve başlangıcı (ilerleme çubuğu için)
    var start: Date
    var target: Date
    /// Bugün kılınan farz sayısı (0–5) ve kılınanların sırası
    var prayedCount: Int
    var prayed: [Int]
    /// Bir önceki vaktin adı ve saati (ör. "İkindi 15:38")
    var previousLabel: String
  }

  /// "Manisa / Saruhanlı"
  var place: String
}
#endif

import Foundation

/// Kur'an metni: quran.tsv (sure, ayet, Arapça, Türkçe okunuş, İngilizce okunuş, Diyanet meali, sayfa, cüz).
final class QuranText {
  static let shared = QuranText()
  static let pageCount = 604

  struct Row: Hashable {
    let surah: Int, ayah: Int
    let arabic: String, pronunciation: String, english: String, meal: String
    let page: Int, juz: Int
  }

  private var bySurah: [Int: [Row]] = [:]
  private var byPage: [Int: [Row]] = [:]
  private let lock = NSLock()
  private var loaded = false

  func preload() { DispatchQueue.global(qos: .utility).async { _ = self.load() } }

  @discardableResult
  private func load() -> Bool {
    lock.lock(); defer { lock.unlock() }
    if loaded { return true }
    guard let url = Bundle.main.url(forResource: "quran", withExtension: "tsv"),
          let text = try? String(contentsOf: url, encoding: .utf8) else { return false }
    var s: [Int: [Row]] = [:], p: [Int: [Row]] = [:]
    text.enumerateLines { line, _ in
      let f = line.components(separatedBy: "\t")
      guard f.count >= 6, let su = Int(f[0]), let ay = Int(f[1]) else { return }
      let r = Row(surah: su, ayah: ay, arabic: f[2], pronunciation: f[3], english: f[4], meal: f[5],
                  page: f.count > 6 ? Int(f[6]) ?? 1 : 1, juz: f.count > 7 ? Int(f[7]) ?? 1 : 1)
      s[su, default: []].append(r)
      p[r.page, default: []].append(r)
    }
    bySurah = s; byPage = p; loaded = true
    return true
  }

  func surah(_ n: Int) -> [Row] { load(); return bySurah[n] ?? [] }
  func page(_ n: Int) -> [Row] { load(); return byPage[n] ?? [] }
  func firstPage(ofSurah n: Int) -> Int { surah(n).first?.page ?? 1 }
  func firstPage(ofJuz j: Int) -> Int {
    load()
    return byPage.keys.sorted().first { pg in byPage[pg]?.contains { $0.juz == j } == true } ?? 1
  }

  static func arabicDigits(_ n: Int) -> String {
    String(String(n).compactMap { c -> Character? in
      guard let v = c.wholeNumberValue, let u = UnicodeScalar(0x0660 + v) else { return nil }
      return Character(u)
    })
  }
}

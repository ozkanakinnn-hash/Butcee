/// Uygulama genelindeki sabitler. Yayından önce kendi bilgilerinizle değiştirin.
class AppConfig {
  AppConfig._();

  static const appName = 'Haber Akışı';

  /// Ana akışta her sayfada çekilecek haber sayısı (Firestore kuralı en fazla 50'ye izin verir).
  static const pageSize = 20;

  /// Yasal sayfaların yayınlandığı site (Firebase Hosting: firebase/hosting/public).
  static const siteUrl = 'https://haber-68a3c.web.app';

  /// Telif hakkı sahiplerinin içerik kaldırma talebi göndereceği adres ("Uyar-Kaldır").
  /// tool/fill-legal.sh iletişim e-postasıyla doldurur.
  static const copyrightEmail = '[[EPOSTA]]';

  static const privacyPolicyUrl = '$siteUrl/gizlilik';
  static const termsUrl = '$siteUrl/kullanim-kosullari';
  static const kvkkUrl = '$siteUrl/kvkk';
  static const imprintUrl = '$siteUrl/kunye';
}

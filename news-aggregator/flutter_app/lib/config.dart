/// Uygulama genelindeki sabitler. Yayından önce kendi bilgilerinizle değiştirin.
class AppConfig {
  AppConfig._();

  static const appName = 'Haber Akışı';

  /// Ana akışta her sayfada çekilecek haber sayısı (Firestore kuralı en fazla 50'ye izin verir).
  static const pageSize = 20;

  /// Telif hakkı sahiplerinin içerik kaldırma talebi göndereceği adres ("Uyar-Kaldır").
  static const copyrightEmail = 'telif@uygulamaadiniz.com';

  /// GitHub Pages veya kendi sitenizde yayınlanan metinler.
  static const privacyPolicyUrl = 'https://uygulamaadiniz.com/gizlilik';
  static const termsUrl = 'https://uygulamaadiniz.com/kullanim-kosullari';
}

import 'package:flutter/widgets.dart';

/// Türkçe (varsayılan) + İngilizce arayüz metinleri.
/// Kullanım: `S.of(context).close`
class S {
  const S._(this.isEnglish);

  final bool isEnglish;

  static S of(BuildContext context) => S.forLanguage(Localizations.localeOf(context).languageCode);

  /// Context dışında (test, bildirim vb.) kullanım için.
  factory S.forLanguage(String languageCode) => S._(languageCode == 'en');

  String _t(String tr, String en) => isEnglish ? en : tr;

  // Genel
  String get appName => _t('Haber Akışı', 'News Feed');
  String get all => _t('Tümü', 'All');
  String get retry => _t('Tekrar dene', 'Retry');
  String get close => _t('Kapat', 'Close');
  String get settings => _t('Ayarlar', 'Settings');

  // Akış
  String get noArticles => _t('Bu kategoride henüz haber yok.', 'No news in this category yet.');
  String get loadError => _t(
    'Haberler yüklenemedi. İnternet bağlantınızı kontrol edin.',
    'Could not load news. Please check your connection.',
  );
  String get endOfFeed => _t('Hepsi bu kadar', "You're all caught up");

  // Zaman
  String get justNow => _t('az önce', 'just now');
  String minutesAgo(int n) => _t('$n dk önce', '${n}m ago');
  String hoursAgo(int n) => _t('$n sa önce', '${n}h ago');
  String get yesterday => _t('dün', 'yesterday');
  String daysAgo(int n) => _t('$n gün önce', '${n}d ago');

  // WebView
  String get share => _t('Haberi paylaş', 'Share article');
  String get openInBrowser => _t('Tarayıcıda aç', 'Open in browser');
  String get copyLink => _t('Bağlantıyı kopyala', 'Copy link');
  String get linkCopied => _t('Bağlantı kopyalandı', 'Link copied');
  String get reload => _t('Yenile', 'Reload');
  String get secureConnection => _t('Güvenli bağlantı', 'Secure connection');
  String get insecureConnection => _t('Güvenli olmayan bağlantı', 'Not secure');
  String get pageLoadError => _t('Sayfa açılamadı.', 'The page could not be opened.');

  // Ayarlar
  String get language => _t('Dil', 'Language');
  String get systemLanguage => _t('Sistem dili', 'System default');
  String get legal => _t('Yasal', 'Legal');
  String get privacyPolicy => _t('Gizlilik Politikası (KVKK/GDPR)', 'Privacy Policy (GDPR)');
  String get terms => _t('Kullanım Koşulları', 'Terms of Use');
  String get copyrightNotice => _t('Telif Hakkı Bildirimi (Uyar-Kaldır)', 'Copyright Notice (Takedown)');
  String get copyrightNoticeDesc => _t(
    'İçeriğinizin kaldırılmasını istiyorsanız bize e-posta gönderin.',
    'Email us to request removal of your content.',
  );
  String get copyrightMailSubject => _t('Telif hakkı - içerik kaldırma talebi', 'Copyright - takedown request');
  String get copyrightMailBody => _t(
    'Kaldırılmasını istediğiniz haberin bağlantısı:\n\nHak sahibi kurum / kişi:\n\nAçıklama:\n',
    'Link of the article to be removed:\n\nRights holder:\n\nDescription:\n',
  );
  String get aboutContent => _t(
    'Haberler, kaynakların herkese açık RSS yayınlarından alınır ve orijinal sitelerinde açılır. '
        'Tüm içerik hakları ilgili yayıncılara aittir.',
    'Articles come from publishers\' public RSS feeds and open on the original website. '
        'All content rights belong to their respective publishers.',
  );
  String get about => _t('Hakkında', 'About');

  // İlgi alanları
  String get interests => _t('İlgi Alanların', 'Your Interests');
  String get manageInterests => _t('İlgi alanlarını düzenle', 'Edit interests');
  String get following => _t('Takip ettiklerin', 'Following');
  String get discover => _t('Keşfet', 'Discover');
  String get followingHint =>
      _t('Sürükleyerek sırala, sekmeler bu sırayla görünür.', 'Drag to reorder; tabs follow this order.');
  String get noFollowing =>
      _t('Henüz bir ilgi alanı seçmedin. Aşağıdan + ile ekle.', 'No interests yet. Add some with + below.');
  String get allFollowed => _t('Tüm kategorileri takip ediyorsun.', 'You follow every category.');
  String get chooseInterestsTitle => _t('Neleri okumak istersin?', 'What do you want to read?');
  String get chooseInterestsSubtitle => _t(
    'İlgi alanlarını seç; akışın buna göre şekillensin. Sonra Ayarlar\'dan değiştirebilirsin.',
    'Pick your interests to shape your feed. You can change them later in Settings.',
  );
  String continueWith(int n) => _t('Devam et ($n)', 'Continue ($n)');
  String add(String name) => _t('$name ekle', 'Add $name');
  String remove(String name) => _t('$name kaldır', 'Remove $name');
  String get reorder => _t('Sırala', 'Reorder');

  // Görünüm
  String get appearance => _t('Görünüm', 'Appearance');
  String get themeSystem => _t('Sistem', 'System');
  String get themeLight => _t('Açık', 'Light');
  String get themeDark => _t('Koyu', 'Dark');

  // Firebase kurulmamış
  String get setupRequired => _t('Firebase yapılandırması bulunamadı', 'Firebase is not configured');
  String get setupRequiredDesc => _t(
    'android/app/google-services.json dosyasını ekleyip uygulamayı yeniden derleyin. Ayrıntılar README dosyasında.',
    'Add android/app/google-services.json and rebuild the app. See README for details.',
  );
}

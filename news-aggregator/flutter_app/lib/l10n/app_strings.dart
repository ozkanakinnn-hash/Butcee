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

  // Bildirimler ve izinler
  String get notifications => _t('Bildirimler', 'Notifications');
  String get notificationPermission => _t('Bildirim izni', 'Notification permission');
  String get permissionGranted => _t('İzin verildi', 'Allowed');
  String get permissionDenied => _t('İzin verilmedi — dokunarak izin ver', 'Not allowed — tap to allow');
  String get permissionBlocked => _t('Kapalı — sistem ayarlarından aç', 'Blocked — enable in system settings');
  String get newsNotifications => _t('İlgi alanı haberleri', 'Interest news');
  String get newsNotificationsDesc => _t(
    'Her ilgi alanı için en fazla 2 saatte bir özet; Son Dakika hemen.',
    'A digest per interest at most every 2 hours; breaking news instantly.',
  );
  String get matchNotifications => _t('Maç ve gol bildirimleri', 'Match & goal alerts');
  String get matchNotificationsDesc => _t(
    'Takımlarının golleri (sesli), maç başlangıcı, devre ve sonuç.',
    "Your teams' goals (with sound), kick-off, half-time and full-time.",
  );
  String get location => _t('Konum', 'Location');
  String get locationPermission => _t('Konum izni', 'Location permission');
  String get locationDesc => _t(
    'Yalnızca bulunduğun ilin hava durumunu göstermek için, yaklaşık konum kullanılır.',
    'Approximate location is used only to show the weather for your city.',
  );
  String get weatherCard => _t('Hava durumu kartı', 'Weather card');
  String get weatherSource => _t('Hava durumu: MET Norway', 'Weather: MET Norway');

  // Futbol
  String get myTeams => _t('Takımların', 'Your teams');
  String get chooseTeams => _t('Takımını seç', 'Choose your team');
  String get chooseTeamsDesc => _t(
    'Seçtiğin takımların canlı skorunu görürsün, golde sesli bildirim alırsın.',
    'See live scores for your teams and get a sound alert on every goal.',
  );
  String teamsSelected(int n) => _t('$n takım seçildi', '$n teams selected');
  String maxTeams(int n) => _t('En fazla $n takım seçebilirsin.', 'You can pick up to $n teams.');
  String get searchTeam => _t('Takım ara', 'Search team');
  String get noTeamsYet => _t(
    'Takım listesi henüz yüklenmedi. Canlı skor botu ilk çalıştığında gelecek.',
    'Team list is not loaded yet. It arrives when the live-score bot first runs.',
  );
  String get live => _t('CANLI', 'LIVE');
  String get halfTime => _t('Devre', 'HT');
  String get fullTime => _t('MS', 'FT');
  String get today => _t('Bugün', 'Today');
  String get tomorrow => _t('Yarın', 'Tomorrow');
  String get goals => _t('Goller', 'Goals');
  String get noGoals => _t('Henüz gol yok', 'No goals yet');
  String get matchNotStarted => _t('Maç henüz başlamadı', 'Match has not started');
  String get done => _t('Tamam', 'Done');

  // Alt menü, arama, bildirim listesi, piyasa paneli
  String get home => _t('Ana Sayfa', 'Home');
  String get search => _t('Ara', 'Search');
  String get searchHint => _t('Haber, kaynak, konu ara', 'Search news, sources, topics');
  String get searchMinChars => _t('En az 3 harf yaz', 'Type at least 3 letters');
  String get noResults => _t('Sonuç bulunamadı', 'No results');
  String get recentSearches => _t('Son aramalar', 'Recent searches');
  String get clear => _t('Temizle', 'Clear');
  String get searchIndexNotReady => _t(
    'Arama dizini hazırlanıyor, birkaç dakika sonra tekrar dene.',
    'Search index is being prepared, try again in a few minutes.',
  );
  String get noNotifications => _t(
    'Henüz bildirim yok. İlgi alanlarına haber geldikçe burada görünecek.',
    'No notifications yet. News for your interests will appear here.',
  );
  String get notificationsOff =>
      _t('Bildirimler kapalı. Ayarlar\'dan açabilirsin.', 'Notifications are off. Turn them on in Settings.');
  String get breakingLabel => _t('SON DAKİKA', 'BREAKING');
  String get matchLabel => _t('MAÇ', 'MATCH');
  String get markets => _t('Piyasalar', 'Markets');
  String marketUpdated(String t) => _t('Güncelleme: $t', 'Updated: $t');
  String get marketUnavailable => _t('Piyasa verileri henüz yok.', 'Market data not available yet.');
  String get weather => _t('Hava Durumu', 'Weather');
  String get showWeather => _t('Konumundaki hava durumunu göster', 'Show weather for your location');
  String get feedLayout => _t('Haber dizilimi', 'News layout');
  String get layoutStandard => _t('Standart', 'Standard');
  String get layoutLarge => _t('Büyük kart', 'Large cards');
  String get layoutCompact => _t('Sade liste', 'Compact');
  String get menu => _t('Menü', 'Menu');

  // Reklam
  String get ad => _t('Reklam', 'Ad');
  String get adPrivacy => _t('Reklam gizlilik seçenekleri', 'Ad privacy options');

  // Depolama
  String get storage => _t('Depolama', 'Storage');
  String get clearCache => _t('Önbelleği temizle', 'Clear cache');
  String cacheSize(String size) =>
      _t('Önbellekte $size (haber görselleri ve sayfalar)', '$size cached (images and pages)');
  String get cacheCleared => _t('Önbellek temizlendi', 'Cache cleared');

  // Bayram ve anma
  String get observanceNotifications => _t('Bayram, kandil ve anma günleri', 'Holidays, holy nights & commemorations');
  String get observanceNotificationsDesc => _t(
    'Resmi ve dini bayramlarda kutlama, kandillerde tebrik, 10 Kasım gibi günlerde anma bildirimi.',
    'Greetings on national and religious holidays and holy nights, remembrance on days like November 10.',
  );

  // Firebase kurulmamış
  String get setupRequired => _t('Firebase yapılandırması bulunamadı', 'Firebase is not configured');
  String get setupRequiredDesc => _t(
    'android/app/google-services.json dosyasını ekleyip uygulamayı yeniden derleyin. Ayrıntılar README dosyasında.',
    'Add android/app/google-services.json and rebuild the app. See README for details.',
  );
}

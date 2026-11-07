import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

import 'ads_config.dart';

/// Reklamlar: Google UMP onayı (GDPR/KVKK), AdMob başlatma ve geçiş reklamı sıklık sınırı.
///
/// Kurallar (Play Store / AdMob politikaları):
///  - Onay formu gerekiyorsa reklamdan ÖNCE gösterilir; `canRequestAds` false ise hiç reklam istenmez.
///  - Ayarlar'da "Reklam gizlilik seçenekleri" (onay geri alınabilir).
///  - Geçiş reklamı yalnızca doğal bir geçişte (haberi kapatıp akışa dönerken), açılışta/çıkışta asla;
///    her [AdsConfig.interstitialEveryReads] okumada bir, en az [AdsConfig.interstitialMinGap] arayla.
///  - İçerik derecelendirmesi en fazla T (genç); haber uygulaması için uygun.
class AdsService extends ChangeNotifier {
  AdsService._();

  static final instance = AdsService._();

  bool _ready = false;
  bool _privacyOptionsRequired = false;
  InterstitialAd? _interstitial;
  int _reads = 0;
  DateTime? _lastInterstitial;

  /// Reklam istenebilir mi (onay alındı ve AdMob başlatıldı). Testlerde false kalır → reklam alanları boş.
  bool get ready => _ready;

  /// Ayarlar'da "Reklam gizlilik seçenekleri" gösterilmeli mi.
  bool get privacyOptionsRequired => _privacyOptionsRequired;

  /// Uygulama açılışında (ilk kare çizildikten sonra) çağrılır.
  Future<void> init() async {
    if (_ready) return;
    final completer = Completer<void>();
    ConsentInformation.instance.requestConsentInfoUpdate(
      ConsentRequestParameters(),
      () async {
        await ConsentForm.loadAndShowConsentFormIfRequired((FormError? e) {
          if (e != null) debugPrint('Onay formu: ${e.message}');
        });
        completer.complete();
      },
      (FormError e) {
        debugPrint('Onay bilgisi alınamadı: ${e.message}');
        completer.complete(); // önceki oturumdaki onay geçerliyse yine reklam istenebilir
      },
    );
    await completer.future;

    if (!await ConsentInformation.instance.canRequestAds()) return;
    _privacyOptionsRequired =
        await ConsentInformation.instance.getPrivacyOptionsRequirementStatus() ==
        PrivacyOptionsRequirementStatus.required;

    await MobileAds.instance.updateRequestConfiguration(RequestConfiguration(maxAdContentRating: MaxAdContentRating.t));
    await MobileAds.instance.initialize();
    _ready = true;
    notifyListeners();
    _loadInterstitial();
  }

  /// Ayarlar → Reklam gizlilik seçenekleri.
  Future<void> showPrivacyOptions() => ConsentForm.showPrivacyOptionsForm((FormError? e) {
    if (e != null) debugPrint('Gizlilik seçenekleri: ${e.message}');
  });

  void _loadInterstitial() {
    final id = AdsConfig.interstitialId;
    if (!_ready || id == null || _interstitial != null) return;
    InterstitialAd.load(
      adUnitId: id,
      request: const AdRequest(),
      adLoadCallback: InterstitialAdLoadCallback(
        onAdLoaded: (ad) => _interstitial = ad,
        onAdFailedToLoad: (e) => debugPrint('Geçiş reklamı yüklenemedi: ${e.message}'),
      ),
    );
  }

  /// Okunan haber sayacı → gerekirse geçiş reklamı. Haber ekranı kapanıp akışa dönülünce çağrılır.
  void onArticleClosed() {
    if (!_ready) return;
    _reads++;
    final ad = _interstitial;
    final gapOk =
        _lastInterstitial == null || DateTime.now().difference(_lastInterstitial!) >= AdsConfig.interstitialMinGap;
    if (ad == null || !gapOk || _reads % AdsConfig.interstitialEveryReads != 0) {
      _loadInterstitial();
      return;
    }
    _interstitial = null;
    ad.fullScreenContentCallback = FullScreenContentCallback(
      onAdDismissedFullScreenContent: (a) {
        a.dispose();
        _loadInterstitial();
      },
      onAdFailedToShowFullScreenContent: (a, e) {
        a.dispose();
        _loadInterstitial();
      },
    );
    _lastInterstitial = DateTime.now();
    ad.show();
  }
}

import 'package:flutter/foundation.dart';

/// AdMob reklam birimi kimlikleri.
///
/// Debug derlemede Google'ın resmi TEST kimlikleri kullanılır (gerçek reklama tıklamak hesabı kapattırabilir).
/// Mağaza sürümünde gerçek kimlikler derleme sırasında verilir; koda yazılmaz:
///   flutter build appbundle --dart-define=ADMOB_BANNER_ID=ca-app-pub-xxx/yyy \
///     --dart-define=ADMOB_NATIVE_ID=... --dart-define=ADMOB_INTERSTITIAL_ID=...
/// Uygulama kimliği (APPLICATION_ID) AndroidManifest'e ADMOB_APP_ID ortam değişkeninden yazılır (build.gradle.kts).
class AdsConfig {
  AdsConfig._();

  // Google'ın Android test kimlikleri: https://developers.google.com/admob/android/test-ads
  static const _testBanner = 'ca-app-pub-3940256099942544/9214589741';
  static const _testNative = 'ca-app-pub-3940256099942544/2247696110';
  static const _testInterstitial = 'ca-app-pub-3940256099942544/1033173712';

  static const _banner = String.fromEnvironment('ADMOB_BANNER_ID');
  static const _native = String.fromEnvironment('ADMOB_NATIVE_ID');
  static const _interstitial = String.fromEnvironment('ADMOB_INTERSTITIAL_ID');

  static String? _pick(String real, String test) => kReleaseMode ? (real.isEmpty ? null : real) : test;

  /// Kimlik verilmemiş mağaza sürümünde o reklam alanı hiç gösterilmez.
  static String? get bannerId => _pick(_banner, _testBanner);
  static String? get nativeId => _pick(_native, _testNative);
  static String? get interstitialId => _pick(_interstitial, _testInterstitial);

  /// Akışta her kaç haberden sonra bir doğal reklam.
  static const nativeEvery = 6;

  /// Geçiş reklamı: her kaç haber okumasında bir ve iki geçiş reklamı arasında en az ne kadar süre.
  static const interstitialEveryReads = 4;
  static const interstitialMinGap = Duration(minutes: 3);

  /// Alt banner (alt menünün üstünde).
  static const bannerEnabled = true;
}

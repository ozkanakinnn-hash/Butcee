import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

import '../l10n/app_strings.dart';
import 'ads_config.dart';
import 'ads_service.dart';

/// Akış içi doğal reklam: haber kartı genişliğinde, açıkça "Reklam" etiketli (AdMob politikası).
/// Yüklenemezse hiç yer kaplamaz.
class NativeAdSlot extends StatefulWidget {
  const NativeAdSlot({super.key});

  @override
  State<NativeAdSlot> createState() => _NativeAdSlotState();
}

class _NativeAdSlotState extends State<NativeAdSlot> {
  NativeAd? _ad;
  bool _loaded = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final id = AdsConfig.nativeId;
    if (_ad != null || !AdsService.instance.ready || id == null) return;
    final theme = Theme.of(context);
    final scheme = theme.colorScheme;
    _ad = NativeAd(
      adUnitId: id,
      request: const AdRequest(),
      nativeTemplateStyle: NativeTemplateStyle(
        templateType: TemplateType.medium,
        mainBackgroundColor: scheme.surfaceContainer,
        cornerRadius: 14,
        callToActionTextStyle: NativeTemplateTextStyle(
          textColor: Colors.white,
          backgroundColor: scheme.primary,
          style: NativeTemplateFontStyle.bold,
          size: 15,
        ),
        primaryTextStyle: NativeTemplateTextStyle(
          textColor: scheme.onSurface,
          style: NativeTemplateFontStyle.bold,
          size: 16,
        ),
        secondaryTextStyle: NativeTemplateTextStyle(textColor: scheme.onSurfaceVariant, size: 14),
        tertiaryTextStyle: NativeTemplateTextStyle(textColor: scheme.onSurfaceVariant, size: 13),
      ),
      listener: NativeAdListener(
        onAdLoaded: (_) => mounted ? setState(() => _loaded = true) : null,
        onAdFailedToLoad: (ad, e) {
          ad.dispose();
          _ad = null;
        },
      ),
    )..load();
  }

  @override
  void dispose() {
    _ad?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_loaded || _ad == null) return const SizedBox.shrink();
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _AdLabel(text: S.of(context).ad),
          const SizedBox(height: 6),
          ConstrainedBox(
            constraints: const BoxConstraints(minHeight: 320, maxHeight: 360),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(14),
              child: AdWidget(ad: _ad!),
            ),
          ),
          Divider(height: 24, color: theme.dividerColor),
        ],
      ),
    );
  }
}

/// Alt menünün üstündeki uyarlanabilir banner. Yüklenemezse yer kaplamaz.
class BottomBannerAd extends StatefulWidget {
  const BottomBannerAd({super.key});

  @override
  State<BottomBannerAd> createState() => _BottomBannerAdState();
}

class _BottomBannerAdState extends State<BottomBannerAd> {
  BannerAd? _ad;
  bool _loaded = false;
  bool _requested = false;

  @override
  void initState() {
    super.initState();
    AdsService.instance.addListener(_maybeLoad);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _maybeLoad();
  }

  Future<void> _maybeLoad() async {
    final id = AdsConfig.bannerId;
    if (_requested || !mounted || !AdsService.instance.ready || id == null || !AdsConfig.bannerEnabled) return;
    _requested = true;
    final width = MediaQuery.sizeOf(context).width.truncate();
    final size = await AdSize.getLargeAnchoredAdaptiveBannerAdSize(width);
    if (size == null || !mounted) return;
    _ad = BannerAd(
      adUnitId: id,
      size: size,
      request: const AdRequest(),
      listener: BannerAdListener(
        onAdLoaded: (_) => mounted ? setState(() => _loaded = true) : null,
        onAdFailedToLoad: (ad, e) {
          ad.dispose();
          _ad = null;
        },
      ),
    )..load();
  }

  @override
  void dispose() {
    AdsService.instance.removeListener(_maybeLoad);
    _ad?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ad = _ad;
    if (!_loaded || ad == null) return const SizedBox.shrink();
    return SizedBox(
      width: ad.size.width.toDouble(),
      height: ad.size.height.toDouble(),
      child: AdWidget(ad: ad),
    );
  }
}

class _AdLabel extends StatelessWidget {
  const _AdLabel({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(color: const Color(0xFFF5B400), borderRadius: BorderRadius.circular(4)),
      child: Text(
        text,
        style: theme.textTheme.labelSmall?.copyWith(color: Colors.black, fontWeight: FontWeight.w800),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../backend/remote_config.dart';
import '../l10n/app_strings.dart';

/// Uzaktan ayardan gelen şeritler: "güncelle" uyarısı (min_build) ve duyuru (announcement).
/// İkisi de yoksa yer kaplamaz.
class RemoteBanner extends StatefulWidget {
  const RemoteBanner({super.key, this.config, this.buildNumber});

  /// Testler için; boşsa [RemoteConfigService] dinlenir.
  final ValueNotifier<RemoteConfig>? config;

  /// Testler için; boşsa cihazdaki derleme numarası okunur.
  final int? buildNumber;

  @override
  State<RemoteBanner> createState() => _RemoteBannerState();
}

class _RemoteBannerState extends State<RemoteBanner> {
  int _build = 0;

  @override
  void initState() {
    super.initState();
    _build = widget.buildNumber ?? 0;
    if (widget.buildNumber == null) {
      PackageInfo.fromPlatform()
          .then((p) {
            if (mounted) setState(() => _build = int.tryParse(p.buildNumber) ?? 0);
          })
          .catchError((Object _) {});
    }
  }

  Future<void> _open(String url) async {
    final uri = Uri.tryParse(url);
    if (uri != null && uri.hasScheme) await launchUrl(uri, mode: LaunchMode.externalApplication);
  }

  @override
  Widget build(BuildContext context) => ValueListenableBuilder<RemoteConfig>(
    valueListenable: widget.config ?? RemoteConfigService.instance,
    builder: (context, c, _) {
      final s = S.of(context);
      final scheme = Theme.of(context).colorScheme;
      final update = c.needsUpdate(_build);
      return Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (update)
            _Strip(
              icon: Icons.system_update_rounded,
              text: s.updateAvailable,
              action: c.updateUrl.isNotEmpty ? s.updateNow : null,
              color: scheme.errorContainer,
              onColor: scheme.onErrorContainer,
              onTap: c.updateUrl.isNotEmpty ? () => _open(c.updateUrl) : null,
            ),
          if (c.announcement.isNotEmpty)
            _Strip(
              icon: c.announcementWarning ? Icons.warning_amber_rounded : Icons.campaign_outlined,
              text: c.announcement,
              color: c.announcementWarning ? scheme.tertiaryContainer : scheme.secondaryContainer,
              onColor: c.announcementWarning ? scheme.onTertiaryContainer : scheme.onSecondaryContainer,
              onTap: c.announcementUrl.isNotEmpty ? () => _open(c.announcementUrl) : null,
            ),
        ],
      );
    },
  );
}

class _Strip extends StatelessWidget {
  const _Strip({
    required this.icon,
    required this.text,
    required this.color,
    required this.onColor,
    this.action,
    this.onTap,
  });

  final IconData icon;
  final String text;
  final String? action;
  final Color color;
  final Color onColor;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.bodyMedium?.copyWith(color: onColor);
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
      child: Material(
        color: color,
        borderRadius: BorderRadius.circular(14),
        child: InkWell(
          borderRadius: BorderRadius.circular(14),
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            child: Row(
              children: [
                Icon(icon, size: 20, color: onColor),
                const SizedBox(width: 10),
                Expanded(child: Text(text, style: style)),
                if (action != null) ...[
                  const SizedBox(width: 8),
                  Text(action!, style: style?.copyWith(fontWeight: FontWeight.w700)),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}

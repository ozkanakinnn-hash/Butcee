import 'package:flutter/material.dart';

import '../l10n/app_strings.dart';

/// İnternet yokken akışın üstünde: "Çevrimdışısınız — son yüklenen haberler gösteriliyor" + Yenile.
class OfflineBanner extends StatelessWidget {
  const OfflineBanner({super.key, required this.onRetry});

  final Future<void> Function() onRetry;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final scheme = Theme.of(context).colorScheme;
    return Container(
      key: const ValueKey('offline-banner'),
      margin: const EdgeInsets.fromLTRB(16, 8, 16, 4),
      padding: const EdgeInsets.fromLTRB(14, 6, 6, 6),
      decoration: BoxDecoration(color: scheme.secondaryContainer, borderRadius: BorderRadius.circular(14)),
      child: Row(
        children: [
          Icon(Icons.cloud_off_rounded, size: 20, color: scheme.onSecondaryContainer),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  s.offlineTitle,
                  style: Theme.of(
                    context,
                  ).textTheme.labelLarge?.copyWith(color: scheme.onSecondaryContainer, fontWeight: FontWeight.w700),
                ),
                Text(
                  s.offlineBody,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: scheme.onSecondaryContainer),
                ),
              ],
            ),
          ),
          TextButton(onPressed: onRetry, child: Text(s.reload)),
        ],
      ),
    );
  }
}

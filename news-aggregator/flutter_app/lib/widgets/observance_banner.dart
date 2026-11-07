import 'package:flutter/material.dart';

import '../data/observances.dart';
import '../l10n/app_strings.dart';

/// Bayram/anma gününde ana akışın üstünde şerit. Anma günlerinde sade siyah, bayramlarda kırmızı.
class ObservanceBanner extends StatelessWidget {
  const ObservanceBanner({super.key, this.now});

  /// Test için; boşsa bugünün tarihi.
  final DateTime? now;

  @override
  Widget build(BuildContext context) {
    final o = observanceOn(now ?? DateTime.now());
    if (o == null) return const SizedBox.shrink();
    final s = S.of(context);
    final somber = o.observance.isCommemoration;
    final bg = o.observance.color;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: bg,
          borderRadius: BorderRadius.circular(16),
          border: somber ? Border.all(color: Colors.white24) : null,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              o.title(english: s.isEnglish),
              style: const TextStyle(color: Colors.white, fontSize: 17, fontWeight: FontWeight.w800, height: 1.25),
            ),
            const SizedBox(height: 6),
            Text(
              o.body(english: s.isEnglish),
              style: const TextStyle(color: Colors.white70, fontSize: 14, height: 1.35),
            ),
          ],
        ),
      ),
    );
  }
}

import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../data/supplement_catalog.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';

/// Takviyenin emoji simgesi (katalogdan; yoksa kategoriden), yumuşak renkli kare içinde.
class SupplementAvatar extends StatelessWidget {
  const SupplementAvatar(this.medicine, {super.key, this.size = 48});
  final Medicine medicine;
  final double size;

  @override
  Widget build(BuildContext context) {
    final emoji = catalogById(medicine.catalogId)?.emoji ?? categoryEmoji(medicine.category);
    return Container(
      width: size,
      height: size,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: categoryTint(context, medicine.category),
        borderRadius: BorderRadius.circular(size * 0.3),
      ),
      child: Text(emoji, style: TextStyle(fontSize: size * 0.5)),
    );
  }
}

Color categoryTint(BuildContext context, ItemCategory c) {
  final dark = Theme.of(context).brightness == Brightness.dark;
  final base = switch (c) {
    ItemCategory.vitamin => const Color(0xFFFFB300),
    ItemCategory.mineral => const Color(0xFF5C6BC0),
    ItemCategory.fattyAcid => const Color(0xFF29B6F6),
    ItemCategory.herbal => const Color(0xFF66BB6A),
    ItemCategory.probiotic => const Color(0xFFAB47BC),
    ItemCategory.protein => const Color(0xFFEC407A),
    ItemCategory.multi => const Color(0xFF26A69A),
    ItemCategory.medicine => const Color(0xFFEF5350),
    ItemCategory.other => const Color(0xFF78909C),
  };
  return base.withValues(alpha: dark ? 0.28 : 0.16);
}

/// Halka ilerleme göstergesi (ortasında büyük metin).
class ProgressRing extends StatelessWidget {
  const ProgressRing({super.key, required this.value, required this.center, this.caption, this.size = 150, this.stroke = 12});
  final double value;
  final String center;
  final String? caption;
  final double size;
  final double stroke;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    return SizedBox(
      width: size,
      height: size,
      child: CustomPaint(
        painter: _RingPainter(value.clamp(0, 1), cs.primary, cs.surfaceContainerHighest, stroke),
        child: Center(
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            Text(center, style: Theme.of(context).textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.w800)),
            if (caption != null) Text(caption!, style: Theme.of(context).textTheme.bodySmall),
          ]),
        ),
      ),
    );
  }
}

class _RingPainter extends CustomPainter {
  _RingPainter(this.value, this.color, this.track, this.stroke);
  final double value;
  final Color color;
  final Color track;
  final double stroke;

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Offset.zero & size;
    final r = rect.deflate(stroke / 2);
    final p = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = stroke
      ..strokeCap = StrokeCap.round;
    canvas.drawArc(r, 0, math.pi * 2, false, p..color = track);
    if (value > 0) canvas.drawArc(r, -math.pi / 2, math.pi * 2 * value, false, p..color = color);
  }

  @override
  bool shouldRepaint(_RingPainter old) => old.value != value || old.color != color;
}

/// Bölüm başlığı (ör. "ERKEN SABAH   07:00").
class SectionHeader extends StatelessWidget {
  const SectionHeader(this.title, {super.key, this.trailing});
  final String title;
  final String? trailing;

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.labelLarge?.copyWith(
          letterSpacing: 1.1,
          fontWeight: FontWeight.w700,
          color: Theme.of(context).colorScheme.primary,
        );
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 6),
      child: Row(children: [
        Expanded(child: Text(upper(title, Localizations.localeOf(context).languageCode), style: style)),
        if (trailing != null) Text(trailing!, style: style),
      ]),
    );
  }
}

/// Uyum oranına göre renk (kırmızı/yeşil yerine mavi-turuncu ölçek: renk körlüğüne uygun).
Color adherenceColor(BuildContext context, double v) {
  final cs = Theme.of(context).colorScheme;
  if (v >= 0.9) return cs.primary;
  if (v >= 0.6) return const Color(0xFFF9A825);
  return const Color(0xFFD84315);
}

import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../data/supplement_catalog.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';

/// Kategori renkleri: [açık, koyu] degrade çifti. Beyaz metinle en az 4.5:1 kontrast.
List<Color> categoryGradient(ItemCategory c) => switch (c) {
      ItemCategory.vitamin => const [Color(0xFFF59E0B), Color(0xFFC2410C)],
      ItemCategory.mineral => const [Color(0xFF6366F1), Color(0xFF3730A3)],
      ItemCategory.fattyAcid => const [Color(0xFF0EA5E9), Color(0xFF0E7490)],
      ItemCategory.herbal => const [Color(0xFF22C55E), Color(0xFF15803D)],
      ItemCategory.probiotic => const [Color(0xFFA855F7), Color(0xFF6B21A8)],
      ItemCategory.protein => const [Color(0xFFF472B6), Color(0xFFBE185D)],
      ItemCategory.multi => const [Color(0xFF14B8A6), Color(0xFF4F46E5)],
      ItemCategory.medicine => const [Color(0xFFF87171), Color(0xFFB91C1C)],
      ItemCategory.other => const [Color(0xFF64748B), Color(0xFF334155)],
    };

/// Vitamin ailesine göre renk: yağda çözünenler (A, D, E, K) altın, B grubu yakut, C turuncu.
/// Aynı kategori içinde de takviyeler bir bakışta ayırt edilsin diye.
List<Color>? supplementGradient(String? id) => switch (id) {
      'vitA' || 'vitD' || 'vitE' || 'vitK' => const [Color(0xFFF2B01E), Color(0xFFB45309)],
      'vitB1' || 'vitB6' || 'vitB12' || 'folate' || 'biotin' => const [Color(0xFFF43F5E), Color(0xFF9F1239)],
      'vitC' => const [Color(0xFFFB923C), Color(0xFFC2410C)],
      'coq10' => const [Color(0xFFEAB308), Color(0xFF854D0E)],
      _ => null,
    };

IconData categoryIcon(ItemCategory c) => switch (c) {
      ItemCategory.vitamin => Icons.wb_sunny_rounded,
      ItemCategory.mineral => Icons.diamond_rounded,
      ItemCategory.fattyAcid => Icons.water_drop_rounded,
      ItemCategory.herbal => Icons.eco_rounded,
      ItemCategory.probiotic => Icons.bubble_chart_rounded,
      ItemCategory.protein => Icons.spa_rounded,
      ItemCategory.multi => Icons.auto_awesome_rounded,
      ItemCategory.medicine => Icons.medication_rounded,
      ItemCategory.other => Icons.science_rounded,
    };

/// Takviye simgesi: kategori degradeli yuvarlatılmış kare + kod (D3, B12, Fe…) ya da simge.
/// Emojilerin aksine her cihazda aynı görünür ve ölçeklenir.
class SupplementIcon extends StatelessWidget {
  const SupplementIcon({super.key, this.catalogId, this.category = ItemCategory.vitamin, this.name, this.size = 48});

  /// Takviye ([Medicine]) için kısayol.
  SupplementIcon.of(Medicine m, {Key? key, double size = 48})
      : this(key: key, catalogId: m.catalogId, category: m.category, name: m.displayName, size: size);

  final String? catalogId;
  final ItemCategory category;

  /// Katalog dışı takviyelerde baş harfler için.
  final String? name;
  final double size;

  @override
  Widget build(BuildContext context) {
    final info = catalogById(catalogId);
    final cat = info?.category ?? category;
    final colors = supplementGradient(info?.id) ?? categoryGradient(cat);
    final code = info?.code ?? (info == null ? _initials(name) : null);
    final label = info?.name.of(Localizations.localeOf(context).languageCode) ?? name;

    final Widget glyph = code != null
        ? Padding(
            padding: EdgeInsets.symmetric(horizontal: size * 0.1),
            child: FittedBox(
              fit: BoxFit.scaleDown,
              child: Text(
                code,
                maxLines: 1,
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w800,
                  // Kısa kodlar büyük, uzun kodlar (B12, Q10, A–Z) biraz daha küçük.
                  fontSize: size * (code.characters.length <= 2 ? 0.42 : 0.32),
                  letterSpacing: -0.5,
                  height: 1,
                  shadows: const [Shadow(color: Color(0x33000000), offset: Offset(0, 1), blurRadius: 2)],
                ),
              ),
            ),
          )
        : Icon(categoryIcon(cat), color: Colors.white, size: size * 0.52);

    return Semantics(
      label: label,
      excludeSemantics: true,
      child: Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(size * 0.28),
          gradient: LinearGradient(begin: Alignment.topLeft, end: Alignment.bottomRight, colors: colors),
          boxShadow: [BoxShadow(color: colors.last.withValues(alpha: 0.18), blurRadius: size * 0.12, offset: Offset(0, size * 0.04))],
        ),
        child: Stack(alignment: Alignment.center, children: [
          // Üstte ince parlaklık — düz renk yerine hafif "cam" etkisi.
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            height: size * 0.5,
            child: DecoratedBox(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.vertical(top: Radius.circular(size * 0.28)),
                gradient: const LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Color(0x33FFFFFF), Color(0x00FFFFFF)],
                ),
              ),
            ),
          ),
          glyph,
        ]),
      ),
    );
  }

  static String? _initials(String? name) {
    final words = (name ?? '').trim().split(RegExp(r'\s+')).where((w) => w.isNotEmpty).toList();
    if (words.isEmpty) return null;
    final s = words.length == 1 ? words.first.characters.take(2).toString() : '${words[0].characters.first}${words[1].characters.first}';
    return upper(s, 'tr');
  }
}

/// Geriye dönük ad: takviyenin simgesi.
class SupplementAvatar extends StatelessWidget {
  const SupplementAvatar(this.medicine, {super.key, this.size = 48});
  final Medicine medicine;
  final double size;

  @override
  Widget build(BuildContext context) => SupplementIcon.of(medicine, size: size);
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

/// Ruh hâli 1–5 için tutarlı Material simgeleri ve renkleri.
const moodIcons = [
  Icons.sentiment_very_dissatisfied_rounded,
  Icons.sentiment_dissatisfied_rounded,
  Icons.sentiment_neutral_rounded,
  Icons.sentiment_satisfied_rounded,
  Icons.sentiment_very_satisfied_rounded,
];
const moodColors = [Color(0xFFD84315), Color(0xFFF57C00), Color(0xFF9E9D24), Color(0xFF00897B), Color(0xFF00695C)];

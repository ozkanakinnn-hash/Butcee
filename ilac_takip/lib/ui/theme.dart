import 'package:flutter/material.dart';

import '../domain/models.dart';

const _seed = Color(0xFF00696B);

ThemeData normalTheme(Brightness b) => ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: _seed, brightness: b),
    );

/// Basit Mod: büyük yazı, AAA kontrast, geniş dokunma alanları.
ThemeData seniorTheme(Brightness b) {
  final dark = b == Brightness.dark;
  final cs = dark
      ? const ColorScheme.dark(
          primary: Color(0xFFFFD54F),
          onPrimary: Colors.black,
          secondary: Color(0xFF80DEEA),
          onSecondary: Colors.black,
          surface: Colors.black,
          onSurface: Colors.white,
          error: Color(0xFFFF8A80),
          onError: Colors.black,
          errorContainer: Color(0xFF5C0000),
          onErrorContainer: Colors.white,
        )
      : const ColorScheme.light(
          primary: Color(0xFF0B5D1E),
          onPrimary: Colors.white,
          secondary: Color(0xFF0B4F9C),
          onSecondary: Colors.white,
          surface: Colors.white,
          onSurface: Colors.black,
          error: Color(0xFFB00020),
          onError: Colors.white,
          errorContainer: Color(0xFFFFE5E5),
          onErrorContainer: Color(0xFF5C0000),
        );
  // Varsayılan ThemeData.textTheme stillerinde fontSize yoktur (boyutlar sonradan yerelleştirilmiş
  // geometriden gelir); fontSizeFactor ancak boyutu tanımlı stillere uygulanabilir.
  final typography = Typography.material2021();
  final textTheme = typography.englishLike
      .merge(dark ? typography.white : typography.black)
      .apply(fontSizeFactor: 1.25, bodyColor: cs.onSurface, displayColor: cs.onSurface);
  final base = ThemeData(useMaterial3: true, colorScheme: cs, textTheme: textTheme);
  return base.copyWith(
    materialTapTargetSize: MaterialTapTargetSize.padded,
    visualDensity: const VisualDensity(horizontal: 2, vertical: 2),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        minimumSize: const Size.fromHeight(72),
        textStyle: const TextStyle(fontSize: 26, fontWeight: FontWeight.w800),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        minimumSize: const Size.fromHeight(64),
        foregroundColor: cs.onSurface,
        side: BorderSide(color: cs.onSurface, width: 2),
        textStyle: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
    ),
    cardTheme: CardThemeData(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: BorderSide(color: cs.onSurface.withValues(alpha: .5), width: 2),
      ),
    ),
  );
}

/// Aç/tok rozet renkleri — kırmızı/yeşil çiftinden kaçınılır; beyaz yazıyla AAA kontrast.
Color mealColor(MealRelation m) => switch (m) {
      MealRelation.afterMeal => const Color(0xFFB34700),
      MealRelation.beforeMeal || MealRelation.emptyStomach => const Color(0xFF0B4F9C),
      MealRelation.withMeal => const Color(0xFF5B2A86),
      MealRelation.none => Colors.transparent,
    };

String mealIcon(MealRelation m) => switch (m) {
      MealRelation.afterMeal => '🍽️',
      MealRelation.beforeMeal => '⏱️',
      MealRelation.emptyStomach => '⭕',
      MealRelation.withMeal => '🥄',
      MealRelation.none => '',
    };

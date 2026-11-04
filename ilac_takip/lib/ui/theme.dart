import 'package:flutter/material.dart';

import '../domain/models.dart';

const _seed = Color(0xFF00696B);

ThemeData normalTheme(Brightness b) => ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(seedColor: _seed, brightness: b),
    );

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

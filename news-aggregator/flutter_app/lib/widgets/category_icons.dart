import 'package:flutter/material.dart';

/// categories.icon_name → Material ikon eşlemesi. Yeni kategori eklerken buraya da ekleyin.
const Map<String, IconData> categoryIcons = {
  'bolt': Icons.bolt_rounded,
  'newspaper': Icons.newspaper_rounded,
  'show_chart': Icons.show_chart_rounded,
  'trending_up': Icons.show_chart_rounded,
  'smart_toy': Icons.smart_toy_outlined,
  'memory': Icons.smart_toy_outlined,
  'sports_soccer': Icons.sports_soccer_outlined,
  'public': Icons.public_rounded,
  'volunteer_activism': Icons.volunteer_activism_outlined,
  'health': Icons.volunteer_activism_outlined,
  'science': Icons.science_outlined,
  'palette': Icons.palette_outlined,
  'star': Icons.star_outline_rounded,
  'directions_car': Icons.directions_car_outlined,
  'school': Icons.backpack_outlined,
  'spa': Icons.spa_outlined,
  'movie': Icons.movie_outlined,
  'location_city': Icons.location_city_outlined,
};

IconData iconForCategory(String? name) => categoryIcons[name] ?? Icons.article_outlined;

import 'package:flutter/material.dart';

/// categories.icon_name → Material ikon eşlemesi. Yeni kategori eklerken buraya da ekleyin.
const Map<String, IconData> categoryIcons = {
  'newspaper': Icons.newspaper,
  'trending_up': Icons.trending_up,
  'memory': Icons.memory,
  'sports_soccer': Icons.sports_soccer,
  'public': Icons.public,
  'health': Icons.health_and_safety,
  'science': Icons.science,
  'movie': Icons.movie,
  'location_city': Icons.location_city,
};

IconData iconForCategory(String? name) => categoryIcons[name] ?? Icons.article_outlined;

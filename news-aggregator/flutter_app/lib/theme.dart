import 'package:flutter/material.dart';

/// Uygulama teması: sade, yüksek kontrastlı; koyu modda tam siyah (#000000) zemin.
class AppTheme {
  AppTheme._();

  /// Vurgu rengi (seçili sekme, "+" düğmeleri, bağlantılar).
  static const accent = Color(0xFF0A84FF);

  static ThemeData light() => _build(
    brightness: Brightness.light,
    background: Colors.white,
    surfaceHigh: const Color(0xFFF2F2F7),
    onSurface: const Color(0xFF0B0B0C),
    muted: const Color(0xFF6E6E73),
    divider: const Color(0xFFE5E5EA),
  );

  static ThemeData dark() => _build(
    brightness: Brightness.dark,
    background: Colors.black,
    surfaceHigh: const Color(0xFF1C1C1E),
    onSurface: const Color(0xFFF5F5F7),
    muted: const Color(0xFF8E8E93),
    divider: const Color(0xFF1F1F22),
  );

  static ThemeData _build({
    required Brightness brightness,
    required Color background,
    required Color surfaceHigh,
    required Color onSurface,
    required Color muted,
    required Color divider,
  }) {
    final scheme = ColorScheme.fromSeed(seedColor: accent, brightness: brightness).copyWith(
      primary: accent,
      onPrimary: Colors.white,
      surface: background,
      onSurface: onSurface,
      onSurfaceVariant: muted,
      surfaceContainerLowest: background,
      surfaceContainerLow: background,
      surfaceContainer: surfaceHigh,
      surfaceContainerHigh: surfaceHigh,
      surfaceContainerHighest: surfaceHigh,
      outline: muted,
      outlineVariant: divider,
    );

    final base = ThemeData(useMaterial3: true, colorScheme: scheme, brightness: brightness);
    final text = base.textTheme.apply(bodyColor: onSurface, displayColor: onSurface);

    return base.copyWith(
      scaffoldBackgroundColor: background,
      canvasColor: background,
      dividerColor: divider,
      textTheme: text,
      appBarTheme: AppBarTheme(
        backgroundColor: background,
        foregroundColor: onSurface,
        surfaceTintColor: Colors.transparent,
        scrolledUnderElevation: 0,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: text.titleLarge?.copyWith(fontWeight: FontWeight.w800, letterSpacing: -0.3),
      ),
      tabBarTheme: TabBarThemeData(
        labelColor: Colors.white,
        unselectedLabelColor: muted,
        labelStyle: text.labelLarge?.copyWith(fontWeight: FontWeight.w700),
        unselectedLabelStyle: text.labelLarge?.copyWith(fontWeight: FontWeight.w600),
        indicatorSize: TabBarIndicatorSize.tab,
        indicator: BoxDecoration(color: accent, borderRadius: BorderRadius.circular(20)),
        dividerHeight: 0,
        labelPadding: const EdgeInsets.symmetric(horizontal: 14),
        overlayColor: WidgetStateProperty.all(Colors.transparent),
        splashFactory: NoSplash.splashFactory,
      ),
      dividerTheme: DividerThemeData(color: divider, thickness: 1, space: 1),
      listTileTheme: ListTileThemeData(iconColor: muted, textColor: onSurface),
      iconTheme: IconThemeData(color: onSurface),
      progressIndicatorTheme: const ProgressIndicatorThemeData(color: accent),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: surfaceHigh,
        contentTextStyle: text.bodyMedium?.copyWith(color: onSurface),
      ),
      popupMenuTheme: PopupMenuThemeData(color: surfaceHigh, surfaceTintColor: Colors.transparent),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: background,
        surfaceTintColor: Colors.transparent,
        indicatorColor: accent.withValues(alpha: 0.16),
        iconTheme: WidgetStateProperty.resolveWith(
          (states) => IconThemeData(size: 28, color: states.contains(WidgetState.selected) ? accent : onSurface),
        ),
      ),
      drawerTheme: DrawerThemeData(backgroundColor: background, surfaceTintColor: Colors.transparent, width: 320),
      bottomSheetTheme: BottomSheetThemeData(backgroundColor: background, surfaceTintColor: Colors.transparent),
    );
  }
}

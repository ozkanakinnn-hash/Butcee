import 'package:flutter/material.dart';

import '../l10n/app_strings.dart';
import '../pages/briefing_page.dart';
import '../pages/quakes_page.dart';
import '../services/weather_service.dart';

/// Ana akışın üstünde TEK satır bilgi şeridi (eskiden büyük hava kartı haberleri aşağı itiyordu):
/// [☀️ Sabah gündemi] [🔴 Son depremler] [hava °C]. Her çip dokununca ilgili ekranı açar.
class InfoStrip extends StatelessWidget {
  const InfoStrip({super.key, required this.showWeather, this.now});

  final bool showWeather;
  final DateTime? now;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final evening = isEveningSlot(now ?? DateTime.now()); // 19:00–08:30 akşam gündemi (sunucuyla aynı)
    return SizedBox(
      height: 44,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.fromLTRB(12, 6, 12, 2),
        children: [
          _Chip(
            icon: evening ? Icons.nights_stay_outlined : Icons.wb_twilight_outlined,
            label: evening ? s.briefingEvening : s.briefingMorning,
            onTap: () => Navigator.of(context).push(BriefingPage.route()),
          ),
          _Chip(
            icon: Icons.crisis_alert_outlined,
            label: s.recentQuakes,
            onTap: () => Navigator.of(context).push(QuakesPage.route()),
          ),
          if (showWeather) const _WeatherChip(),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  const _Chip({required this.icon, required this.label, this.onTap});

  final IconData icon;
  final String label;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(right: 8),
    child: ActionChip(
      avatar: Icon(icon, size: 18),
      label: Text(label),
      visualDensity: VisualDensity.compact,
      onPressed: onTap,
    ),
  );
}

class _WeatherChip extends StatefulWidget {
  const _WeatherChip();

  @override
  State<_WeatherChip> createState() => _WeatherChipState();
}

class _WeatherChipState extends State<_WeatherChip> {
  Future<Weather?>? _future;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _future ??= WeatherService.shared.current(locale: Localizations.localeOf(context)).catchError((_) => null);
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    return FutureBuilder<Weather?>(
      future: _future,
      builder: (context, snap) {
        final w = snap.data;
        if (w == null) return const SizedBox.shrink();
        final hour = DateTime.now().hour;
        final d = describeWeather(w.baseSymbol, night: hour < 6 || hour >= 19);
        final place = w.place.isEmpty ? (s.isEnglish ? d.en : d.tr) : w.place;
        return Tooltip(
          message: '${s.isEnglish ? d.en : d.tr} · ${s.weatherSource}',
          child: _Chip(icon: d.icon, label: '$place ${w.temperature.round()}°'),
        );
      },
    );
  }
}

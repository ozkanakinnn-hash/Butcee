import 'package:flutter/material.dart';

import '../l10n/app_strings.dart';
import '../services/weather_service.dart';

/// Ana akışın üstündeki hava durumu kartı (konum izni ve ayar açıksa gösterilir).
class WeatherCard extends StatefulWidget {
  const WeatherCard({super.key, required this.service});

  final WeatherService service;

  @override
  State<WeatherCard> createState() => _WeatherCardState();
}

class _WeatherCardState extends State<WeatherCard> {
  Future<Weather?>? _future;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _future ??= widget.service.current(locale: Localizations.localeOf(context)).catchError((_) => null);
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return FutureBuilder<Weather?>(
      future: _future,
      builder: (context, snap) {
        final w = snap.data;
        if (w == null) return const SizedBox.shrink();
        final hour = DateTime.now().hour;
        final d = describeWeather(w.baseSymbol, night: hour < 6 || hour >= 19);
        return Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceContainer,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                Icon(d.icon, size: 30, color: theme.colorScheme.primary),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        w.place.isEmpty ? (s.isEnglish ? d.en : d.tr) : w.place,
                        style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                      ),
                      Text(
                        '${s.isEnglish ? d.en : d.tr} · ${s.weatherSource}',
                        style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                      ),
                    ],
                  ),
                ),
                Text(
                  '${w.temperature.round()}°',
                  style: theme.textTheme.headlineMedium?.copyWith(fontWeight: FontWeight.w800),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

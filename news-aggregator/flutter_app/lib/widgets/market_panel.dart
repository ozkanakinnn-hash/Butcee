import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/extra_records.dart';
import '../l10n/app_strings.dart';
import '../pages/settings_page.dart';
import '../services/weather_service.dart';
import 'weather_card.dart';

/// Sağ üstteki ☰ ile açılan panel: piyasalar (Dolar, Euro, gram/ons altın, BIST 100, Brent) ve hava durumu.
class MarketPanel extends StatelessWidget {
  const MarketPanel({super.key, required this.settings, required this.repository, required this.weather});

  final AppSettings settings;
  final NewsRepository repository;
  final WeatherService weather;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final muted = theme.colorScheme.onSurfaceVariant;

    return Drawer(
      backgroundColor: theme.scaffoldBackgroundColor,
      shape: const RoundedRectangleBorder(),
      child: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 20),
          children: [
            Text(s.markets, style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800)),
            const SizedBox(height: 12),
            StreamBuilder<MarketSnapshot?>(
              stream: repository.watchMarket(),
              builder: (context, snap) {
                final m = snap.data;
                if (snap.connectionState == ConnectionState.waiting) {
                  return const Padding(
                    padding: EdgeInsets.all(24),
                    child: Center(child: CircularProgressIndicator()),
                  );
                }
                if (m == null || m.items.isEmpty) {
                  return Text(s.marketUnavailable, style: TextStyle(color: muted));
                }
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Column(
                      children: [
                        for (var i = 0; i < m.items.length; i += 2)
                          Padding(
                            padding: const EdgeInsets.only(bottom: 10),
                            // Yükseklik içeriğe göre (büyük yazı boyutunda taşmaz), yan yana eşit
                            child: IntrinsicHeight(
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.stretch,
                                children: [
                                  Expanded(child: _MarketTile(item: m.items[i])),
                                  const SizedBox(width: 10),
                                  Expanded(
                                    child: i + 1 < m.items.length
                                        ? _MarketTile(item: m.items[i + 1])
                                        : const SizedBox(),
                                  ),
                                ],
                              ),
                            ),
                          ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    if (m.updatedAt != null)
                      Text(
                        '${s.marketUpdated(_hhmm(m.updatedAt!.toLocal()))} · ${m.source}',
                        style: theme.textTheme.bodySmall?.copyWith(color: muted),
                      ),
                  ],
                );
              },
            ),
            const SizedBox(height: 28),
            Text(s.weather, style: theme.textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
            const SizedBox(height: 4),
            ListenableBuilder(
              listenable: settings,
              builder: (context, _) => settings.weatherEnabled
                  ? WeatherCard(service: weather, padding: EdgeInsets.zero)
                  : Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: OutlinedButton.icon(
                        icon: const Icon(Icons.location_on_outlined),
                        label: Text(s.showWeather),
                        onPressed: () async {
                          final p = await WeatherService.requestPermission();
                          if (WeatherService.isGranted(p)) await settings.setWeatherEnabled(true);
                        },
                      ),
                    ),
            ),
            const SizedBox(height: 24),
            const Divider(),
            ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const Icon(Icons.settings_outlined),
              title: Text(s.settings),
              onTap: () {
                Navigator.of(context).pop();
                Navigator.of(context).push(MaterialPageRoute<void>(builder: (_) => SettingsPage(settings: settings)));
              },
            ),
          ],
        ),
      ),
    );
  }

  static String _hhmm(DateTime d) => '${d.hour.toString().padLeft(2, '0')}:${d.minute.toString().padLeft(2, '0')}';
}

/// Türkçe sayı biçimi: binlik nokta, ondalık virgül (50.131,25).
String formatTr(double v, int decimals) {
  final fixed = v.toStringAsFixed(decimals);
  final parts = fixed.split('.');
  final intPart = parts[0].replaceAllMapped(RegExp(r'\B(?=(\d{3})+(?!\d))'), (_) => '.');
  return parts.length > 1 ? '$intPart,${parts[1]}' : intPart;
}

class _MarketTile extends StatelessWidget {
  const _MarketTile({required this.item});

  final MarketItem item;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final ch = item.changePct;
    final up = (ch ?? 0) > 0;
    final down = (ch ?? 0) < 0;
    final color = up
        ? Colors.green.shade500
        : down
        ? Colors.red.shade400
        : theme.colorScheme.onSurfaceVariant;
    final decimals = item.id == 'usdtry' || item.id == 'eurtry' ? 3 : (item.value >= 1000 ? 0 : 2);
    final value = '${item.unit == '\$' ? '\$' : ''}${formatTr(item.value, decimals)}${item.unit == '₺' ? ' ₺' : ''}';

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(color: theme.colorScheme.surfaceContainer, borderRadius: BorderRadius.circular(14)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(item.label, style: theme.textTheme.labelLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
          const SizedBox(height: 6),
          FittedBox(
            fit: BoxFit.scaleDown,
            alignment: Alignment.centerLeft,
            child: Text(value, style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
          ),
          Row(
            children: [
              Icon(
                up
                    ? Icons.arrow_drop_up
                    : down
                    ? Icons.arrow_drop_down
                    : Icons.remove,
                color: color,
                size: 20,
              ),
              Text(
                ch == null ? '–' : '%${formatTr(ch.abs(), 2)}',
                style: theme.textTheme.labelMedium?.copyWith(color: color, fontWeight: FontWeight.w700),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

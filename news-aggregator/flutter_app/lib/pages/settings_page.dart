import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:url_launcher/url_launcher.dart';

import '../app_settings.dart';
import '../config.dart';
import '../l10n/app_strings.dart';
import '../services/notification_service.dart';
import '../services/weather_service.dart';
import 'interests_page.dart';
import 'team_picker_page.dart';

/// Ayarlar & yasal uyumluluk: dil seçimi, gizlilik/kullanım koşulları ve "Uyar-Kaldır" bildirimi.
class SettingsPage extends StatefulWidget {
  const SettingsPage({super.key, required this.settings});

  final AppSettings settings;

  @override
  State<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends State<SettingsPage> with WidgetsBindingObserver {
  NotificationPermission? _notif;
  LocationPermission? _loc;

  AppSettings get settings => widget.settings;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refreshPermissions();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  /// Sistem ayarlarından dönünce izin durumlarını yenile.
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refreshPermissions();
  }

  Future<void> _refreshPermissions() async {
    final n = await NotificationService.instance.permissionStatus();
    final l = await WeatherService.permission();
    if (!mounted) return;
    setState(() {
      _notif = n;
      _loc = l;
    });
  }

  Future<void> _requestNotifications() async {
    await NotificationService.instance.requestPermission();
    await _refreshPermissions();
  }

  Future<void> _requestLocation({bool enableWeather = false}) async {
    final p = await WeatherService.requestPermission();
    if (enableWeather && WeatherService.isGranted(p)) await settings.setWeatherEnabled(true);
    await _refreshPermissions();
  }

  Future<void> _open(Uri uri) => launchUrl(uri, mode: LaunchMode.externalApplication);

  /// İzin satırı: durum + dokununca izin iste / ayarları aç.
  Widget _permissionTile({
    required IconData icon,
    required String title,
    required bool? granted,
    required bool blocked,
    required VoidCallback onRequest,
  }) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final ok = granted ?? false;
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle: Text(
        granted == null
            ? '…'
            : ok
            ? s.permissionGranted
            : blocked
            ? s.permissionBlocked
            : s.permissionDenied,
      ),
      trailing: granted == null
          ? null
          : ok
          ? Icon(Icons.check_circle_rounded, color: Colors.green.shade500)
          : FilledButton.tonal(
              onPressed: blocked ? NotificationService.openSystemSettings : onRequest,
              child: Text(
                blocked
                    ? s.settings
                    : s.isEnglish
                    ? 'Allow'
                    : 'İzin ver',
              ),
            ),
      onTap: ok ? null : (blocked ? NotificationService.openSystemSettings : onRequest),
      iconColor: ok ? null : theme.colorScheme.primary,
    );
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final notifGranted = _notif == null ? null : _notif == NotificationPermission.granted;
    final locGranted = _loc == null ? null : WeatherService.isGranted(_loc!);

    return Scaffold(
      appBar: AppBar(title: Text(s.settings)),
      body: ListenableBuilder(
        listenable: settings,
        builder: (context, _) => ListView(
          children: [
            ListTile(
              leading: const Icon(Icons.tune_rounded),
              title: Text(s.manageInterests),
              trailing: const Icon(Icons.chevron_right_rounded),
              onTap: () =>
                  Navigator.of(context)
                      .push(MaterialPageRoute<void>(builder: (_) => InterestsPage(settings: settings))),
            ),
            const Divider(),
            _Header(s.notifications),
            _permissionTile(
              icon: Icons.notifications_active_outlined,
              title: s.notificationPermission,
              granted: notifGranted,
              blocked: _notif == NotificationPermission.blocked,
              onRequest: _requestNotifications,
            ),
            SwitchListTile(
              secondary: const Icon(Icons.newspaper_rounded),
              title: Text(s.newsNotifications),
              subtitle: Text(s.newsNotificationsDesc),
              value: settings.newsNotifications,
              onChanged: (v) async {
                await settings.setNewsNotifications(v);
                if (v && notifGranted != true) await _requestNotifications();
              },
            ),
            SwitchListTile(
              secondary: const Icon(Icons.sports_soccer_outlined),
              title: Text(s.matchNotifications),
              subtitle: Text(s.matchNotificationsDesc),
              value: settings.matchNotifications,
              onChanged: (v) async {
                await settings.setMatchNotifications(v);
                if (v && notifGranted != true) await _requestNotifications();
              },
            ),
            if (settings.followsFootball)
              ListTile(
                leading: const Icon(Icons.shield_outlined),
                title: Text(s.myTeams),
                subtitle: Text(
                  settings.followedTeams.isEmpty ? s.chooseTeams : s.teamsSelected(settings.followedTeams.length),
                ),
                trailing: const Icon(Icons.chevron_right_rounded),
                onTap: () =>
                    Navigator.of(context)
                        .push(MaterialPageRoute<void>(builder: (_) => TeamPickerPage(settings: settings))),
              ),
            const Divider(),
            _Header(s.location),
            _permissionTile(
              icon: Icons.location_on_outlined,
              title: s.locationPermission,
              granted: locGranted,
              blocked: _loc == LocationPermission.deniedForever,
              onRequest: _requestLocation,
            ),
            SwitchListTile(
              secondary: const Icon(Icons.wb_sunny_outlined),
              title: Text(s.weatherCard),
              subtitle: Text(s.locationDesc),
              value: settings.weatherEnabled && (locGranted ?? false),
              onChanged: (v) async {
                if (!v) return settings.setWeatherEnabled(false);
                if (locGranted == true) return settings.setWeatherEnabled(true);
                await _requestLocation(enableWeather: true);
              },
            ),
            const Divider(),
            _Header(s.appearance),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: SegmentedButton<ThemeMode>(
                showSelectedIcon: false,
                segments: [
                  ButtonSegment(
                    value: ThemeMode.system,
                    label: Text(s.themeSystem),
                    icon: const Icon(Icons.brightness_auto_outlined),
                  ),
                  ButtonSegment(
                    value: ThemeMode.light,
                    label: Text(s.themeLight),
                    icon: const Icon(Icons.light_mode_outlined),
                  ),
                  ButtonSegment(
                    value: ThemeMode.dark,
                    label: Text(s.themeDark),
                    icon: const Icon(Icons.dark_mode_outlined),
                  ),
                ],
                selected: {settings.themeMode},
                onSelectionChanged: (v) => settings.setThemeMode(v.first),
              ),
            ),
            const Divider(),
            _Header(s.feedLayout),
            RadioGroup<FeedLayout>(
              groupValue: settings.feedLayout,
              onChanged: (v) => settings.setFeedLayout(v ?? FeedLayout.standard),
              child: Column(
                children: [
                  for (final (l, label, icon) in [
                    (FeedLayout.standard, s.layoutStandard, Icons.view_agenda_outlined),
                    (FeedLayout.large, s.layoutLarge, Icons.crop_landscape_outlined),
                    (FeedLayout.compact, s.layoutCompact, Icons.view_headline_rounded),
                  ])
                    RadioListTile<FeedLayout>(value: l, title: Text(label), secondary: Icon(icon)),
                ],
              ),
            ),
            const Divider(),
            _Header(s.language),
            RadioGroup<String>(
              groupValue: settings.locale?.languageCode ?? 'system',
              onChanged: (v) => settings.setLocale(v == null || v == 'system' ? null : Locale(v)),
              child: Column(
                children: [
                  RadioListTile<String>(value: 'system', title: Text(s.systemLanguage)),
                  const RadioListTile<String>(value: 'tr', title: Text('Türkçe')),
                  const RadioListTile<String>(value: 'en', title: Text('English')),
                ],
              ),
            ),
            const Divider(),
            _Header(s.legal),
            ListTile(
              leading: const Icon(Icons.privacy_tip_outlined),
              title: Text(s.privacyPolicy),
              onTap: () => _open(Uri.parse(AppConfig.privacyPolicyUrl)),
            ),
            ListTile(
              leading: const Icon(Icons.description_outlined),
              title: Text(s.terms),
              onTap: () => _open(Uri.parse(AppConfig.termsUrl)),
            ),
            ListTile(
              leading: const Icon(Icons.copyright),
              title: Text(s.copyrightNotice),
              subtitle: Text('${s.copyrightNoticeDesc}\n${AppConfig.copyrightEmail}'),
              isThreeLine: true,
              onTap: () => _open(
                Uri(
                  scheme: 'mailto',
                  path: AppConfig.copyrightEmail,
                  query: _encodeQuery({'subject': s.copyrightMailSubject, 'body': s.copyrightMailBody}),
                ),
              ),
            ),
            const Divider(),
            _Header(s.about),
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
              child: Text(s.aboutContent, style: theme.textTheme.bodySmall),
            ),
          ],
        ),
      ),
    );
  }

  /// mailto: için boşlukları "+" yerine %20 ile kodlar (e-posta uygulamaları "+" gösterir).
  static String _encodeQuery(Map<String, String> params) =>
      params.entries.map((e) => '${Uri.encodeComponent(e.key)}=${Uri.encodeComponent(e.value)}').join('&');
}

class _Header extends StatelessWidget {
  const _Header(this.text);

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.fromLTRB(16, 16, 16, 4),
    child: Text(
      text,
      style: Theme.of(context).textTheme.labelLarge?.copyWith(color: Theme.of(context).colorScheme.primary),
    ),
  );
}

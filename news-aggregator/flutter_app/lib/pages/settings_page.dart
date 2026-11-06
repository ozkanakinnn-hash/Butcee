import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

import '../app_settings.dart';
import '../config.dart';
import '../l10n/app_strings.dart';
import 'interests_page.dart';

/// Ayarlar & yasal uyumluluk: dil seçimi, gizlilik/kullanım koşulları ve "Uyar-Kaldır" bildirimi.
class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key, required this.settings});

  final AppSettings settings;

  Future<void> _open(Uri uri) => launchUrl(uri, mode: LaunchMode.externalApplication);

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);

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

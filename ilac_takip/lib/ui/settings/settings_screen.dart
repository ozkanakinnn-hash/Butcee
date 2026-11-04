import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../state/providers.dart';
import '../widgets/common.dart';

class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final s = ref.watch(settingsProvider);
    final notifier = ref.read(settingsProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: Text(t.settings)),
      body: ListView(children: [
        ListTile(
          leading: const Icon(Icons.language),
          title: Text(t.language),
          trailing: DropdownButton<String?>(
            value: s.languageCode,
            items: [
              DropdownMenuItem(value: null, child: Text(t.languageSystem)),
              const DropdownMenuItem(value: 'tr', child: Text('Türkçe')),
              const DropdownMenuItem(value: 'en', child: Text('English')),
            ],
            onChanged: (v) => notifier.update((x) => x.copyWith(languageCode: () => v)),
          ),
        ),
        ListTile(
          leading: const Icon(Icons.alarm),
          title: Text(t.remindersTitle),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => context.push('/reminders'),
        ),
        ListTile(
          leading: const Icon(Icons.verified_user_outlined),
          title: Text(t.reliability),
          trailing: const Icon(Icons.chevron_right),
          onTap: () => context.push('/reliability'),
        ),
        const Divider(),
        SwitchListTile(
          secondary: const Icon(Icons.record_voice_over_outlined),
          title: Text(t.ttsOnAlarm),
          value: s.ttsOnAlarm,
          onChanged: (v) => notifier.update((x) => x.copyWith(ttsOnAlarm: v)),
        ),
      ]),
    );
  }
}

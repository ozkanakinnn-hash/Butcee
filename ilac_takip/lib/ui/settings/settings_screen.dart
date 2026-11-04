import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../services/settings_store.dart';
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
          secondary: const Icon(Icons.elderly),
          title: Text(t.seniorMode),
          subtitle: Text(t.seniorModeHint),
          value: s.seniorMode,
          onChanged: (v) async {
            await notifier.update((x) => x.copyWith(seniorMode: v));
            if (v && context.mounted) context.go('/');
          },
        ),
        if (s.seniorMode) ...[
          ListTile(
            title: Text(t.textScale),
            subtitle: Slider(
              value: s.textScale,
              min: 1.0,
              max: 1.6,
              divisions: 6,
              label: '${(s.textScale * 100).round()}%',
              onChanged: (v) => notifier.update((x) => x.copyWith(textScale: v)),
            ),
          ),
          ListTile(
            leading: const Icon(Icons.lock_outline),
            title: Text(t.seniorPin),
            subtitle: Text(t.seniorPinHint),
            trailing: s.hasPin ? const Icon(Icons.check) : null,
            onTap: () => _setPin(context, notifier),
          ),
        ],
        SwitchListTile(
          secondary: const Icon(Icons.record_voice_over_outlined),
          title: Text(t.ttsOnAlarm),
          value: s.ttsOnAlarm,
          onChanged: (v) => notifier.update((x) => x.copyWith(ttsOnAlarm: v)),
        ),
        ListTile(
          leading: const Icon(Icons.phone_outlined),
          title: Text(t.caregiverPhone),
          subtitle: Text(s.caregiverPhone ?? '—'),
          onTap: () => _editPhone(context, notifier, s.caregiverPhone),
        ),
      ]),
    );
  }

  Future<void> _setPin(BuildContext context, SettingsNotifier notifier) async {
    final t = context.l10n;
    final c = TextEditingController();
    final pin = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(t.seniorPin),
        content: TextField(
          controller: c,
          autofocus: true,
          obscureText: true,
          maxLength: 4,
          keyboardType: TextInputType.number,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: Text(t.cancel)),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, RegExp(r'^\d{4}$').hasMatch(c.text) ? c.text : null),
            child: Text(t.save),
          ),
        ],
      ),
    );
    c.dispose();
    if (pin != null) await notifier.update((x) => x.copyWith(pinHash: () => SettingsStore.hashPin(pin)));
  }

  Future<void> _editPhone(BuildContext context, SettingsNotifier notifier, String? current) async {
    final t = context.l10n;
    final c = TextEditingController(text: current);
    final phone = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(t.caregiverPhone),
        content: TextField(controller: c, autofocus: true, keyboardType: TextInputType.phone),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: Text(t.cancel)),
          FilledButton(onPressed: () => Navigator.pop(ctx, c.text.trim()), child: Text(t.save)),
        ],
      ),
    );
    c.dispose();
    if (phone != null) {
      await notifier.update((x) => x.copyWith(caregiverPhone: () => phone.isEmpty ? null : phone));
    }
  }
}

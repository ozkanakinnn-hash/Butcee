import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../domain/models.dart';
import '../../l10n/app_localizations.dart';
import '../../services/localized_texts.dart';
import '../../services/settings_store.dart';
import '../../state/providers.dart';
import '../home/pending_panel.dart';
import '../widgets/common.dart';

/// Basit Mod ana ekranı: tek ekran, tek görev — "Şimdi ne yapmalıyım?".
/// Sekme yok; ayarlar başlığa 2 sn uzun basma + PIN ile.
class SeniorHomeScreen extends ConsumerWidget {
  const SeniorHomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final settings = ref.watch(settingsProvider);
    final today = ref.watch(todayDosesProvider).value ?? const <DoseView>[];
    final stats = ref.watch(todayStatsProvider);
    final upcoming = today.where((v) => v.dose.status == DoseStatus.pending && v.dose.scheduledTime.isAfter(DateTime.now())).take(2);
    final text = Theme.of(context).textTheme;

    return Scaffold(
      body: SafeArea(
        child: ListView(padding: const EdgeInsets.all(16), children: [
          GestureDetector(
            onLongPress: () => _openSettings(context, settings),
            child: Semantics(
              hint: t.longPressForSettings,
              child: Text(_greeting(t), style: text.headlineMedium?.copyWith(fontWeight: FontWeight.w900)),
            ),
          ),
          const PendingPanel(large: true),
          if (upcoming.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(t.nextDoses, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
            for (final v in upcoming)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                    Row(children: [
                      Text(timeLabel(t, v.dose.scheduledTime),
                          style: text.headlineMedium?.copyWith(fontWeight: FontWeight.w900)),
                      const SizedBox(width: 16),
                      Expanded(child: Text(v.medicine.displayName, style: text.headlineSmall)),
                    ]),
                    const SizedBox(height: 8),
                    MealBadge(v.dose.meal, large: true),
                  ]),
                ),
              ),
          ],
          const SizedBox(height: 16),
          if (stats.planned > 0)
            Text(
              stats.taken == stats.planned ? t.allDoneToday : t.todayProgress(stats.taken, stats.planned),
              style: text.titleLarge,
              textAlign: TextAlign.center,
            ),
          const SizedBox(height: 24),
          if (settings.caregiverPhone != null && settings.caregiverPhone!.isNotEmpty)
            FilledButton(
              style: FilledButton.styleFrom(backgroundColor: Theme.of(context).colorScheme.secondary),
              onPressed: () => launchUrl(Uri(scheme: 'tel', path: settings.caregiverPhone)),
              child: Text(t.callCaregiver),
            ),
        ]),
      ),
    );
  }

  String _greeting(AppLocalizations t) {
    final h = DateTime.now().hour;
    if (h < 12) return t.goodMorning;
    if (h < 18) return t.goodAfternoon;
    return t.goodEvening;
  }

  Future<void> _openSettings(BuildContext context, AppSettings s) async {
    if (s.pinHash == null) {
      context.push('/settings');
      return;
    }
    final ok = await showDialog<bool>(context: context, builder: (_) => _PinDialog(pinHash: s.pinHash!));
    if (ok == true && context.mounted) context.push('/settings');
  }
}

class _PinDialog extends StatefulWidget {
  const _PinDialog({required this.pinHash});
  final String pinHash;

  @override
  State<_PinDialog> createState() => _PinDialogState();
}

class _PinDialogState extends State<_PinDialog> {
  final _c = TextEditingController();
  String? _error;

  @override
  void dispose() {
    _c.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return AlertDialog(
      title: Text(t.enterPin),
      content: TextField(
        controller: _c,
        autofocus: true,
        obscureText: true,
        maxLength: 4,
        keyboardType: TextInputType.number,
        decoration: InputDecoration(errorText: _error),
      ),
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, false), child: Text(t.cancel)),
        FilledButton(
          onPressed: () {
            if (SettingsStore.verifyPin(_c.text, widget.pinHash)) {
              Navigator.pop(context, true);
            } else {
              setState(() => _error = t.wrongPin);
            }
          },
          child: Text(t.ok),
        ),
      ],
    );
  }
}

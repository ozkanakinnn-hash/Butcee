import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_tts/flutter_tts.dart';
import 'package:go_router/go_router.dart';
import 'package:wakelock_plus/wakelock_plus.dart';

import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/dose_ui_actions.dart';

/// Tam ekran alarm (kilit ekranı üzerinde de açılır). Normal ve Basit Mod'da aynı ekran;
/// boyutlar temadan gelir.
class AlarmScreen extends ConsumerStatefulWidget {
  const AlarmScreen({super.key, required this.logId});
  final String logId;

  @override
  ConsumerState<AlarmScreen> createState() => _AlarmScreenState();
}

class _AlarmScreenState extends ConsumerState<AlarmScreen> {
  final _tts = FlutterTts();
  bool _spoken = false;

  @override
  void initState() {
    super.initState();
    WakelockPlus.enable();
  }

  @override
  void dispose() {
    WakelockPlus.disable();
    _tts.stop();
    super.dispose();
  }

  Future<void> _speak(DoseView v) async {
    if (_spoken || !ref.read(settingsProvider).ttsOnAlarm) return;
    _spoken = true;
    final t = context.l10n;
    await _tts.setLanguage(t.localeName == 'en' ? 'en-US' : 'tr-TR');
    await _tts.speak(t.ttsDose(
      timeLabel(t, v.dose.scheduledTime),
      v.medicine.displayName,
      mealLabel(t, v.dose.meal),
      quantityLabel(t, v.dose.quantity, v.medicine.form),
    ));
  }

  void _close() => context.canPop() ? context.pop() : context.go('/');

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final async = ref.watch(doseViewProvider(widget.logId));
    final text = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(leading: CloseButton(onPressed: _close)),
      body: SafeArea(
        child: switch (async) {
          AsyncValue(:final value?) => () {
              WidgetsBinding.instance.addPostFrameCallback((_) => _speak(value));
              final d = value.dose;
              return ListView(padding: const EdgeInsets.all(20), children: [
                Text(timeLabel(t, d.scheduledTime),
                    textAlign: TextAlign.center,
                    style: text.displayMedium?.copyWith(fontWeight: FontWeight.w900)),
                const SizedBox(height: 16),
                Icon(Icons.medication, size: 120, color: Theme.of(context).colorScheme.primary),
                const SizedBox(height: 12),
                Text(value.medicine.displayName,
                    textAlign: TextAlign.center, style: text.headlineLarge?.copyWith(fontWeight: FontWeight.w900)),
                if (value.medicine.nickname != null && value.medicine.nickname!.isNotEmpty)
                  Text(value.medicine.name, textAlign: TextAlign.center, style: text.titleMedium),
                Text(quantityLabel(t, d.quantity, value.medicine.form),
                    textAlign: TextAlign.center, style: text.titleLarge),
                const SizedBox(height: 16),
                MealBadge(d.meal, large: true),
                const SizedBox(height: 24),
                if (d.isResolved)
                  Center(child: Text(statusLabel(t, d.status), style: text.headlineSmall))
                else ...[
                  FilledButton.icon(
                    key: const Key('alarmTake'),
                    style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(96)),
                    icon: const Icon(Icons.check_circle, size: 40),
                    label: Text(t.takeBig),
                    onPressed: () async {
                      await DoseUiActions.take(context, ref, d.logId);
                      if (mounted) _close();
                    },
                  ),
                  const SizedBox(height: 16),
                  Row(children: [
                    if (value.canSnooze && d.status != DoseStatus.missed) ...[
                      Expanded(
                        child: OutlinedButton(
                          onPressed: () async {
                            await DoseUiActions.snooze(context, ref, d.logId);
                            if (mounted) _close();
                          },
                          child: Text(t.snooze10, textAlign: TextAlign.center),
                        ),
                      ),
                      const SizedBox(width: 16),
                    ],
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () async {
                          if (await DoseUiActions.skip(context, ref, d.logId) && mounted) _close();
                        },
                        child: Text(t.wontTake, textAlign: TextAlign.center),
                      ),
                    ),
                  ]),
                ],
              ]);
            }(),
          AsyncValue(hasValue: true) => Center(child: Text(t.alarmNotFound)),
          _ => const Center(child: CircularProgressIndicator()),
        },
      ),
    );
  }
}

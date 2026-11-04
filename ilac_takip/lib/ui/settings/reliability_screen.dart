import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../services/alarm_scheduler.dart';
import '../../services/reliability_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';

/// "Alarmlarınız güvende mi?" — her satır gerçek durumu okur; ayardan dönünce yeniden kontrol eder.
class ReliabilityScreen extends ConsumerStatefulWidget {
  const ReliabilityScreen({super.key});

  @override
  ConsumerState<ReliabilityScreen> createState() => _ReliabilityScreenState();
}

class _ReliabilityScreenState extends ConsumerState<ReliabilityScreen> with WidgetsBindingObserver {
  ReliabilityStatus? _status;

  ReliabilityService get _svc => ref.read(servicesProvider).reliability;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _refresh();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refresh();
  }

  Future<void> _refresh() async {
    final s = await _svc.check();
    if (mounted) setState(() => _status = s);
  }

  Future<void> _testAlarm() async {
    final t = context.l10n;
    final services = ref.read(servicesProvider);
    await services.scheduler.schedule(PlannedDose(
      intId: AlarmIds.testIntId,
      logId: 'test',
      fireAt: DateTime.now().add(const Duration(minutes: 1)),
      title: t.testAlarmTitle,
      body: t.testAlarmBody,
      repeatTitle: t.testAlarmTitle,
      isCritical: true,
    ));
    if (mounted) showSnack(context, t.testAlarmScheduled);
  }

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final s = _status;
    return Scaffold(
      appBar: AppBar(title: Text(t.reliability)),
      body: s == null
          ? const Center(child: CircularProgressIndicator())
          : ListView(padding: const EdgeInsets.all(16), children: [
              Text(t.reliabilityIntro),
              const SizedBox(height: 12),
              _row(t.relNotifications, s.notifications, _svc.requestNotifications),
              if (s.isAndroid) ...[
                _row(t.relExactAlarms, s.exactAlarms, _svc.requestExactAlarms),
                _row(t.relBatteryOpt, s.batteryUnrestricted, _svc.openBatterySettings),
                _row(t.relFullScreen, s.fullScreen, _svc.requestFullScreen),
                if (s.needsOemGuide)
                  ListTile(
                    leading: const Icon(Icons.warning_amber_rounded, color: Colors.orange),
                    title: Text(t.relOemAutostart),
                    subtitle: Text(t.relOemHint(s.manufacturer.toUpperCase())),
                    trailing: TextButton(onPressed: _svc.openOemAutostart, child: Text(t.open)),
                  ),
              ],
              const SizedBox(height: 24),
              OutlinedButton.icon(
                icon: const Icon(Icons.alarm),
                label: Text(t.testAlarm),
                onPressed: _testAlarm,
              ),
            ]),
    );
  }

  Widget _row(String title, bool ok, Future<void> Function() fix) => ListTile(
        leading: Icon(ok ? Icons.check_circle : Icons.error, color: ok ? Colors.green.shade700 : Colors.orange.shade800),
        title: Text(title),
        trailing: ok ? null : FilledButton.tonal(onPressed: fix, child: Text(context.l10n.fix)),
      );
}

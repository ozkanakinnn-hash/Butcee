import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../domain/adherence.dart';
import '../../services/crash_reporter.dart';
import '../../services/report_service.dart';
import '../../state/providers.dart';
import 'common.dart';

/// PDF paylaşımı ve dış bağlantılar (eczane, TİTCK).
class ReportActions {
  ReportActions._();

  static Future<void> shareProgram(BuildContext context, WidgetRef ref) => _share(context, ref, report: false);
  static Future<void> shareDoctorReport(BuildContext context, WidgetRef ref) => _share(context, ref, report: true);

  static Future<void> _share(BuildContext context, WidgetRef ref, {required bool report}) async {
    final t = context.l10n;
    final s = ref.read(servicesProvider);
    try {
      final plans = await s.supplements.plans();
      AdherenceStats? total;
      Map<int, AdherenceStats>? per;
      if (report) {
        final now = DateTime.now();
        final from = DateTime(now.year, now.month, now.day - 29);
        total = computeAdherence(await s.db.dosesBetween(from, now.add(const Duration(days: 1))),
            from: from, to: now.add(const Duration(days: 1)), now: now);
        per = await s.supplements.perMedicineAdherence();
      }
      final bytes = await ReportService.build(
        t: t,
        plans: plans,
        settings: ref.read(settingsProvider),
        perMedicine: per,
        total: total,
      );
      await ReportService.share(bytes, report ? 'takviye-raporu.pdf' : 'takviye-programi.pdf');
    } on Object catch (e, st) {
      CrashReporter.record(e, st, context: 'PDF');
      if (context.mounted) showSnack(context, '$e');
    }
  }

  static Future<void> openMaps(String query) => launchUrl(
        Uri.https('www.google.com', '/maps/search/', {'api': '1', 'query': query}),
        mode: LaunchMode.externalApplication,
      );

  static Future<void> nearbyPharmacies(BuildContext context) =>
      openMaps(context.l10n.localeName == 'en' ? 'pharmacy' : 'eczane');

  static Future<void> dutyPharmacies(BuildContext context) =>
      openMaps(context.l10n.localeName == 'en' ? 'pharmacy open now' : 'nöbetçi eczane');

  static Future<void> sideEffect(BuildContext context) {
    final t = context.l10n;
    return showDialog<void>(
      context: context,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.report_outlined),
        title: Text(t.toolSideEffect),
        content: Text(t.sideEffectBody),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: Text(t.dismiss)),
          FilledButton(
            onPressed: () => launchUrl(Uri.https('www.titck.gov.tr', '/'), mode: LaunchMode.externalApplication),
            child: Text(t.openTitck),
          ),
        ],
      ),
    );
  }
}

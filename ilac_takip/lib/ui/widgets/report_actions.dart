import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/adherence.dart';
import '../../services/crash_reporter.dart';
import '../../services/report_service.dart';
import '../../state/providers.dart';
import 'common.dart';

/// PDF olarak dışa aktarma.
class ReportActions {
  ReportActions._();

  /// Dışa aktar (PDF): veri varsa 30 günlük uyum özeti + takviye programı.
  static Future<void> exportPdf(BuildContext context, WidgetRef ref) async {
    final t = context.l10n;
    final s = ref.read(servicesProvider);
    try {
      final plans = await s.supplements.plans();
      final now = DateTime.now();
      final from = DateTime(now.year, now.month, now.day - 29);
      final total = computeAdherence(await s.db.dosesBetween(from, now.add(const Duration(days: 1))),
          from: from, to: now.add(const Duration(days: 1)), now: now);
      final per = await s.supplements.perMedicineAdherence();
      final bytes = await ReportService.build(
        t: t,
        plans: plans,
        // Henüz geçmiş yoksa yalnızca program dışa aktarılır.
        perMedicine: total.planned > 0 ? per : null,
        total: total.planned > 0 ? total : null,
      );
      await ReportService.share(bytes, 'takviye-raporu.pdf');
    } on Object catch (e, st) {
      CrashReporter.record(e, st, context: 'PDF');
      if (context.mounted) showSnack(context, '$e');
    }
  }
}

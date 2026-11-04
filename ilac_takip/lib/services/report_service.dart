import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';

import '../domain/adherence.dart';
import '../domain/models.dart';
import '../l10n/app_localizations.dart';
import 'localized_texts.dart';
import 'supplement_service.dart';

/// Dışa aktarılan PDF: (veri varsa) 30 günlük uyum özeti + takviye programı.
class ReportService {
  ReportService._();

  static const _teal = PdfColor.fromInt(0xFF00696B);
  static const _soft = PdfColor.fromInt(0xFFE6F2F2);

  static Future<pw.ThemeData> _theme() async => pw.ThemeData.withFont(
        // Türkçe karakterler (ş, ğ, ı, İ) için gömülü font gerekir; PDF'in varsayılan Helvetica'sı desteklemez.
        base: pw.Font.ttf(await rootBundle.load('assets/fonts/NotoSans-Regular.ttf')),
        bold: pw.Font.ttf(await rootBundle.load('assets/fonts/NotoSans-Bold.ttf')),
      );

  /// Programı dilim (ya da saat) → aç/tok grubu → takviye şeklinde gruplar.
  static List<({String title, String time, Map<MealRelation, List<String>> groups})> programSections(
    AppLocalizations t,
    List<SupplementPlan> plans,
  ) {
    final sections = <String, ({String title, String time, Map<MealRelation, List<String>> groups})>{};
    void add(String key, String title, String time, MealRelation meal, String line) {
      final s = sections.putIfAbsent(key, () => (title: title, time: time, groups: <MealRelation, List<String>>{}));
      s.groups.putIfAbsent(meal, () => []).add(line);
    }

    for (final p in plans) {
      final rule = p.rule;
      if (rule == null || (p.schedule?.paused ?? false)) continue;
      final line = '${p.medicine.displayName} — ${supplementDoseLabel(t, p.medicine, rule.quantity)}';
      if (p.isAsNeeded) {
        add('zz', t.asNeededShort, '', MealRelation.none, line);
        continue;
      }
      final slots = p.schedule!.slots;
      final times = p.times;
      for (var i = 0; i < times.length; i++) {
        final hm = '${times[i].h.toString().padLeft(2, '0')}:${times[i].m.toString().padLeft(2, '0')}';
        final slot = slots.length == times.length ? slots[i] : null;
        if (slot != null) {
          add(hm + slot, slotLabel(t, slot), hm, rule.meal, line);
        } else {
          add(hm, hourGroupLabel(t, times[i].h), hm, rule.meal, line);
        }
      }
    }
    final keys = sections.keys.toList()..sort();
    return [for (final k in keys) sections[k]!];
  }

  static Future<Uint8List> build({
    required AppLocalizations t,
    required List<SupplementPlan> plans,
    Map<int, AdherenceStats>? perMedicine,
    AdherenceStats? total,
  }) async {
    final doc = pw.Document(theme: await _theme());
    final now = DateTime.now();
    final dateFmt = DateFormat.yMMMd(t.localeName);
    final isReport = total != null;
    final sections = programSections(t, plans);

    doc.addPage(pw.MultiPage(
      pageFormat: PdfPageFormat.a4,
      margin: const pw.EdgeInsets.all(32),
      header: (_) => pw.Container(
        padding: const pw.EdgeInsets.only(bottom: 8),
        decoration: const pw.BoxDecoration(border: pw.Border(bottom: pw.BorderSide(color: _teal, width: 2))),
        child: pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceBetween, children: [
          pw.Text(isReport ? t.reportTitle : t.programTitle,
              style: pw.TextStyle(fontSize: 20, fontWeight: pw.FontWeight.bold, color: _teal)),
          pw.Text(t.generatedAt(dateFmt.format(now)), style: const pw.TextStyle(fontSize: 9)),
        ]),
      ),
      footer: (c) => pw.Column(children: [
        pw.Divider(color: PdfColors.grey400),
        pw.Text(t.pdfDisclaimer, style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey700)),
        pw.Align(
          alignment: pw.Alignment.centerRight,
          child: pw.Text('${t.appTitle} · ${c.pageNumber}/${c.pagesCount}',
              style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey600)),
        ),
      ]),
      build: (_) => [
        if (isReport) ...[
          pw.SizedBox(height: 12),
          pw.Text(
            '${dateFmt.format(DateTime(now.year, now.month, now.day - 29))} – ${dateFmt.format(now)}',
            style: const pw.TextStyle(fontSize: 10, color: PdfColors.grey700),
          ),
          pw.SizedBox(height: 8),
          pw.Container(
            padding: const pw.EdgeInsets.all(14),
            decoration: pw.BoxDecoration(color: _soft, borderRadius: pw.BorderRadius.circular(8)),
            child: pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceBetween, children: [
              pw.Column(crossAxisAlignment: pw.CrossAxisAlignment.start, children: [
                pw.Text(t.adherence30, style: pw.TextStyle(fontWeight: pw.FontWeight.bold)),
                pw.Text(t.plannedTaken(total.taken, total.planned), style: const pw.TextStyle(fontSize: 10)),
              ]),
              pw.Text('${(total.adherence * 100).round()}%',
                  style: pw.TextStyle(fontSize: 28, fontWeight: pw.FontWeight.bold, color: _teal)),
            ]),
          ),
          pw.SizedBox(height: 14),
          pw.TableHelper.fromTextArray(
            headerStyle: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: PdfColors.white),
            headerDecoration: const pw.BoxDecoration(color: _teal),
            cellStyle: const pw.TextStyle(fontSize: 10),
            headers: [t.colSupplement, t.colPlan, t.colDose, t.colAdherence],
            data: [
              for (final p in plans)
                [
                  p.medicine.displayName,
                  ruleLabel(t, p.rule),
                  supplementDoseLabel(t, p.medicine, p.rule?.quantity ?? 1),
                  perMedicine?[p.medicine.id] == null || perMedicine![p.medicine.id]!.planned == 0
                      ? '—'
                      : '${(perMedicine[p.medicine.id]!.adherence * 100).round()}%',
                ],
            ],
          ),
          pw.SizedBox(height: 18),
          pw.Text(t.currentProgram, style: pw.TextStyle(fontSize: 14, fontWeight: pw.FontWeight.bold)),
        ],
        pw.SizedBox(height: 8),
        for (final s in sections) ...[
          pw.Container(
            margin: const pw.EdgeInsets.only(top: 8),
            padding: const pw.EdgeInsets.symmetric(horizontal: 8, vertical: 5),
            color: _soft,
            child: pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceBetween, children: [
              pw.Text(upper(s.title, t.localeName), style: pw.TextStyle(fontWeight: pw.FontWeight.bold, color: _teal)),
              pw.Text(s.time, style: const pw.TextStyle(color: _teal)),
            ]),
          ),
          for (final g in s.groups.entries) ...[
            if (g.key != MealRelation.none)
              pw.Padding(
                padding: const pw.EdgeInsets.only(left: 8, top: 4),
                child: pw.Text(upper(mealLabel(t, g.key), t.localeName),
                    style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey700)),
              ),
            for (final line in g.value)
              pw.Padding(
                padding: const pw.EdgeInsets.only(left: 14, top: 2),
                child: pw.Bullet(text: line, style: const pw.TextStyle(fontSize: 11)),
              ),
          ],
        ],
      ],
    ));
    return doc.save();
  }

  static Future<void> share(Uint8List bytes, String filename) =>
      Printing.sharePdf(bytes: bytes, filename: filename);
}

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../data/drug_catalog.dart';
import '../../domain/gs1_parser.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../supplements/add_supplement_screen.dart';
import '../widgets/report_actions.dart';
import '../widgets/common.dart';

/// Karekod (GS1 DataMatrix) + EAN-13 + QR tarayıcı.
class ScanScreen extends ConsumerStatefulWidget {
  const ScanScreen({super.key});

  @override
  ConsumerState<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends ConsumerState<ScanScreen> {
  final _controller = MobileScannerController(
    formats: const [BarcodeFormat.dataMatrix, BarcodeFormat.ean13, BarcodeFormat.qrCode],
    detectionSpeed: DetectionSpeed.noDuplicates,
  );
  bool _busy = false;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _onDetect(BarcodeCapture capture) async {
    final raw = capture.barcodes.isEmpty ? null : capture.barcodes.first.rawValue;
    if (raw == null) return;
    await _handle(raw);
  }

  Future<void> _handle(String raw) async {
    if (_busy) return;
    final code = Gs1Parser.parse(raw);
    if (code == null) {
      showSnack(context, context.l10n.invalidCode);
      return;
    }
    _busy = true;
    HapticFeedback.mediumImpact();
    await _controller.stop();
    try {
      await _route(code);
    } finally {
      _busy = false;
      if (mounted) await _controller.start();
    }
  }

  Future<void> _route(ScannedDrugCode code) async {
    final services = ref.read(servicesProvider);
    final outcome = await services.catalog.resolve(code);
    if (!mounted) return;
    final t = context.l10n;
    switch (outcome) {
      case DuplicatePack():
        showSnack(context, t.duplicatePack);
      case AddStock(:final medicine, :final quantity):
        final qty = quantity > 0 ? quantity.toDouble() : 1.0;
        final ok = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            content: Text(t.addStockQuestion(medicine.displayName, quantityLabel(t, qty, medicine.form))),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx, false), child: Text(t.cancel)),
              FilledButton(onPressed: () => Navigator.pop(ctx, true), child: Text(t.ok)),
            ],
          ),
        );
        if (ok == true) {
          final added = await services.db.addScannedPack(medicine.id!, code, qty, DateTime.now());
          if (mounted) showSnack(context, added ? t.addStockDone : t.duplicatePack);
        }
      case NewFromCatalog(:final entry):
        await _productSheet(code, entry);
      case NotInCatalog():
        await _productSheet(code, null);
    }
  }

  /// Ürün bilgisi: barkod, SKT, seri/parti no; ekle, eczane bul, kullanma talimatı.
  Future<void> _productSheet(ScannedDrugCode code, DrugCatalogEntry? entry) {
    final t = context.l10n;
    final df = DateFormat.yMMMd(t.localeName);
    return showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 16),
          child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            Text(upper(t.productInfo, t.localeName),
                style: Theme.of(ctx).textTheme.labelLarge?.copyWith(letterSpacing: 1.1, color: Theme.of(ctx).colorScheme.primary)),
            const SizedBox(height: 6),
            Text(entry?.name ?? t.notFoundInCatalog, style: Theme.of(ctx).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
            const SizedBox(height: 12),
            _row(ctx, Icons.qr_code_2, t.barcodeLabel, code.barcode13),
            if (code.expiry != null) _row(ctx, Icons.event_available, t.expiryLabel('').replaceAll(':', '').trim(), df.format(code.expiry!)),
            if (code.serial != null) _row(ctx, Icons.tag, t.serialLabel, code.serial!),
            if (code.lot != null) _row(ctx, Icons.inventory_2_outlined, t.lotLabel, code.lot!),
            const SizedBox(height: 16),
            FilledButton.icon(
              icon: const Icon(Icons.add),
              label: Text(t.addToMySupplements),
              onPressed: () {
                Navigator.pop(ctx);
                context.push('/supplements/add', extra: AddSupplementArgs(code: code, drugEntry: entry));
              },
            ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              icon: const Icon(Icons.local_pharmacy_outlined),
              label: Text(t.findPharmacy),
              onPressed: () => ReportActions.nearbyPharmacies(context),
            ),
            if (entry?.leafletUrl != null) ...[
              const SizedBox(height: 8),
              OutlinedButton.icon(
                icon: const Icon(Icons.description_outlined),
                label: Text(t.readLeaflet),
                onPressed: () => launchUrl(Uri.parse(entry!.leafletUrl!), mode: LaunchMode.externalApplication),
              ),
            ],
          ]),
        ),
      ),
    );
  }

  Widget _row(BuildContext ctx, IconData icon, String k, String v) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(children: [
          Icon(icon, size: 20, color: Theme.of(ctx).colorScheme.primary),
          const SizedBox(width: 10),
          SizedBox(width: 120, child: Text(k)),
          Expanded(child: SelectableText(v, style: const TextStyle(fontWeight: FontWeight.w700))),
        ]),
      );

  Future<void> _manualEntry() async {
    final t = context.l10n;
    final c = TextEditingController();
    final value = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(t.scanManualEntry),
        content: TextField(
          controller: c,
          autofocus: true,
          maxLength: 13,
          keyboardType: TextInputType.number,
          inputFormatters: [FilteringTextInputFormatter.digitsOnly],
          decoration: InputDecoration(hintText: t.barcodeDigits),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: Text(t.cancel)),
          FilledButton(onPressed: () => Navigator.pop(ctx, c.text), child: Text(t.ok)),
        ],
      ),
    );
    c.dispose();
    if (value != null && value.isNotEmpty) await _handle(value);
  }

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return Scaffold(
      appBar: AppBar(title: Text(t.scanTitle), actions: [
        IconButton(icon: const Icon(Icons.flash_on), onPressed: _controller.toggleTorch),
        IconButton(tooltip: t.scanManualEntry, icon: const Icon(Icons.keyboard), onPressed: _manualEntry),
      ]),
      body: Stack(children: [
        MobileScanner(
          controller: _controller,
          onDetect: _onDetect,
          errorBuilder: (context, error) => Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(mainAxisSize: MainAxisSize.min, children: [
                const Icon(Icons.no_photography, size: 64),
                const SizedBox(height: 12),
                Text(error.errorDetails?.message ?? error.errorCode.name, textAlign: TextAlign.center),
                const SizedBox(height: 12),
                FilledButton(onPressed: _manualEntry, child: Text(t.scanManualEntry)),
              ]),
            ),
          ),
        ),
        Center(
          child: Container(
            width: 240,
            height: 240,
            decoration: BoxDecoration(
              border: Border.all(color: Colors.white, width: 3),
              borderRadius: BorderRadius.circular(16),
            ),
          ),
        ),
        Positioned(
          left: 16,
          right: 16,
          bottom: 32,
          child: Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(color: Colors.black54, borderRadius: BorderRadius.circular(12)),
            child: Text(t.scanHint, textAlign: TextAlign.center, style: const TextStyle(color: Colors.white)),
          ),
        ),
      ]),
    );
  }
}

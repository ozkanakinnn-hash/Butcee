import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../data/drug_catalog.dart';
import '../../domain/gs1_parser.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../medicine/add_medicine_screen.dart';
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
        context.push('/medicines/add', extra: AddMedicineArgs(code: code, entry: entry));
      case NotInCatalog():
        showSnack(context, t.notFoundInCatalog);
        context.push('/medicines/add', extra: AddMedicineArgs(code: code));
    }
  }

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

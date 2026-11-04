import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../data/supplement_catalog.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../widgets/common.dart';
import 'add_supplement_screen.dart';

/// Takviye seç: arama + kategori filtresi; listede yoksa elle ekle ya da kutuyu tara.
class CatalogPickerScreen extends StatefulWidget {
  const CatalogPickerScreen({super.key});

  @override
  State<CatalogPickerScreen> createState() => _CatalogPickerScreenState();
}

class _CatalogPickerScreenState extends State<CatalogPickerScreen> {
  String _q = '';
  ItemCategory? _cat;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final results = searchCatalog(_q, t.localeName).where((s) => _cat == null || s.category == _cat).toList();
    final cats = {for (final s in supplementCatalog) s.category}.toList();

    return Scaffold(
      appBar: AppBar(
        title: Text(t.addMedicine),
        actions: [
          IconButton(tooltip: t.toolScan, icon: const Icon(Icons.qr_code_scanner), onPressed: () => context.push('/scan')),
        ],
      ),
      body: Column(children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(12, 4, 12, 8),
          child: SearchBar(
            key: const Key('catalogSearch'),
            hintText: t.searchSupplements,
            leading: const Icon(Icons.search),
            onChanged: (v) => setState(() => _q = v),
          ),
        ),
        SizedBox(
          height: 44,
          child: ListView(scrollDirection: Axis.horizontal, padding: const EdgeInsets.symmetric(horizontal: 12), children: [
            Padding(
              padding: const EdgeInsets.only(right: 6),
              child: ChoiceChip(label: Text(t.catAll), selected: _cat == null, onSelected: (_) => setState(() => _cat = null)),
            ),
            for (final c in cats)
              Padding(
                padding: const EdgeInsets.only(right: 6),
                child: ChoiceChip(label: Text(categoryLabel(t, c)), selected: _cat == c, onSelected: (_) => setState(() => _cat = c)),
              ),
          ]),
        ),
        Expanded(
          child: ListView(children: [
            for (final s in results)
              ListTile(
                leading: Text(s.emoji, style: const TextStyle(fontSize: 28)),
                title: Text(s.name.of(t.localeName)),
                subtitle: Text(categoryLabel(t, s.category)),
                trailing: const Icon(Icons.add_circle_outline),
                onTap: () => context.push('/supplements/add', extra: AddSupplementArgs(catalogId: s.id)),
              ),
            ListTile(
              leading: const Icon(Icons.edit_outlined),
              title: Text(t.notInList),
              onTap: () => context.push('/supplements/add', extra: AddSupplementArgs(customName: _q.trim())),
            ),
          ]),
        ),
      ]),
    );
  }
}

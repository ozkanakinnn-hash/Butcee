import 'package:flutter/material.dart';

import '../../data/supplement_catalog.dart';
import '../widgets/common.dart';
import '../../services/localized_texts.dart';

/// Öğren: Temel bilgiler · Semptomlar · Emilim.
class LearnScreen extends StatelessWidget {
  const LearnScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: Text(t.navLearn, style: const TextStyle(fontWeight: FontWeight.w800)),
          bottom: TabBar(tabs: [Tab(text: t.tabBasics), Tab(text: t.tabSymptoms), Tab(text: t.tabAbsorption)]),
        ),
        body: const TabBarView(children: [_Basics(), _Symptoms(), _Absorption()]),
      ),
    );
  }
}

class _Intro extends StatelessWidget {
  const _Intro(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
        child: Text(text, style: Theme.of(context).textTheme.bodyMedium),
      );
}

class _Basics extends StatelessWidget {
  const _Basics();

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    return ListView(padding: const EdgeInsets.only(bottom: 24), children: [
      _Intro(t.basicsSubtitle),
      for (final c in basicsCards)
        Card(
          margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(c.title.of(t.localeName), style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 6),
              Text(c.body.of(t.localeName)),
            ]),
          ),
        ),
    ]);
  }
}

class _Symptoms extends StatefulWidget {
  const _Symptoms();
  @override
  State<_Symptoms> createState() => _SymptomsState();
}

class _SymptomsState extends State<_Symptoms> {
  final _selected = <int>{};

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    String names(List<String> ids) => ids.map((id) => catalogById(id)?.name.of(t.localeName) ?? id).join(', ');
    final picked = {for (final i in _selected) ...symptomList[i].nutrients}.toList();
    final cs = Theme.of(context).colorScheme;

    return ListView(padding: const EdgeInsets.only(bottom: 24), children: [
      _Intro(t.symptomsSubtitle),
      for (var i = 0; i < symptomList.length; i++)
        Card(
          margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          child: CheckboxListTile(
            value: _selected.contains(i),
            onChanged: (v) => setState(() => v == true ? _selected.add(i) : _selected.remove(i)),
            title: Text(symptomList[i].title.of(t.localeName), style: const TextStyle(fontWeight: FontWeight.w700)),
            subtitle: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(symptomList[i].detail.of(t.localeName)),
              const SizedBox(height: 2),
              Text('${t.relatedNutrients}: ${names(symptomList[i].nutrients)}',
                  style: TextStyle(color: cs.primary, fontStyle: FontStyle.italic, fontSize: 12)),
            ]),
          ),
        ),
      if (picked.isNotEmpty)
        Card(
          margin: const EdgeInsets.fromLTRB(12, 12, 12, 0),
          color: cs.primaryContainer,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(t.selectedSummary, style: TextStyle(fontWeight: FontWeight.w700, color: cs.onPrimaryContainer)),
              const SizedBox(height: 8),
              Wrap(spacing: 6, runSpacing: 6, children: [
                for (final id in picked) Chip(avatar: Text(catalogById(id)?.emoji ?? ''), label: Text(catalogById(id)?.name.of(t.localeName) ?? id)),
              ]),
            ]),
          ),
        ),
      Card(
        margin: const EdgeInsets.fromLTRB(12, 12, 12, 0),
        color: cs.errorContainer,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(children: [
            Icon(Icons.medical_services_outlined, color: cs.onErrorContainer),
            const SizedBox(width: 8),
            Expanded(child: Text(t.seeDoctorBanner, style: TextStyle(color: cs.onErrorContainer))),
          ]),
        ),
      ),
    ]);
  }
}

class _Absorption extends StatefulWidget {
  const _Absorption();
  @override
  State<_Absorption> createState() => _AbsorptionState();
}

class _AbsorptionState extends State<_Absorption> {
  TipEffect? _filter;

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final tips = absorptionTips.where((x) => _filter == null || x.effect == _filter).toList();
    return ListView(padding: const EdgeInsets.only(bottom: 24), children: [
      _Intro(t.absorptionSubtitle),
      Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12),
        child: Wrap(spacing: 8, children: [
          ChoiceChip(label: Text(t.catAll), selected: _filter == null, onSelected: (_) => setState(() => _filter = null)),
          ChoiceChip(label: Text(t.increases), selected: _filter == TipEffect.increases, onSelected: (_) => setState(() => _filter = TipEffect.increases)),
          ChoiceChip(label: Text(t.decreases), selected: _filter == TipEffect.decreases, onSelected: (_) => setState(() => _filter = TipEffect.decreases)),
        ]),
      ),
      for (final tip in tips)
        Card(
          margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                  decoration: BoxDecoration(
                    color: tip.effect == TipEffect.increases ? const Color(0xFF00695C) : const Color(0xFFBF360C),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(upper(tip.effect == TipEffect.increases ? t.increases : t.decreases, t.localeName),
                      style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w800)),
                ),
                const SizedBox(width: 8),
                Expanded(child: Text(tip.title.of(t.localeName), style: const TextStyle(fontWeight: FontWeight.w700))),
              ]),
              const SizedBox(height: 8),
              Text(tip.body.of(t.localeName)),
              const SizedBox(height: 8),
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(color: const Color(0xFFFFF8E1), borderRadius: BorderRadius.circular(8)),
                child: Text.rich(TextSpan(children: [
                  TextSpan(text: '${t.tipLabel}: ', style: const TextStyle(fontWeight: FontWeight.w700, color: Color(0xFF8D4A00))),
                  TextSpan(text: tip.tip.of(t.localeName), style: const TextStyle(color: Color(0xFF5D3000))),
                ])),
              ),
            ]),
          ),
        ),
    ]);
  }
}

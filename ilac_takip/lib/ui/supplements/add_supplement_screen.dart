import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../data/drug_catalog.dart';
import '../../data/supplement_catalog.dart';
import '../../domain/dose_rule.dart';
import '../../domain/gs1_parser.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../services/settings_store.dart';
import '../../services/supplement_service.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';
import 'supplement_detail_screen.dart';
import '../widgets/visuals.dart';

/// Ekleme ekranına ön doldurma verisi: katalog takviyesi, taranan kutu veya elle ad.
class AddSupplementArgs {
  const AddSupplementArgs({this.catalogId, this.code, this.drugEntry, this.customName});
  final String? catalogId;
  final ScannedDrugCode? code;
  final DrugCatalogEntry? drugEntry;
  final String? customName;
}

enum Freq { daily, everyOtherDay, mwf, weekends, everyN, specificDays, cycle, interval, asNeeded }

class AddSupplementScreen extends ConsumerStatefulWidget {
  const AddSupplementScreen({super.key, this.args = const AddSupplementArgs()});
  final AddSupplementArgs args;

  @override
  ConsumerState<AddSupplementScreen> createState() => _AddSupplementScreenState();
}

class _AddSupplementScreenState extends ConsumerState<AddSupplementScreen> {
  final _formKey = GlobalKey<FormState>();
  late final SupplementInfo? _info = catalogById(widget.args.catalogId);
  late final _name = TextEditingController();
  late final _amount = TextEditingController(text: _info?.defaultAmount == null ? '' : formatQty(_info!.defaultAmount!));
  final _qty = TextEditingController(text: '1');
  late final _stock = TextEditingController(text: '${widget.args.drugEntry?.packageSize ?? ''}');
  final _critical = TextEditingController(text: '5');

  late ItemCategory _category = _info?.category ?? (widget.args.drugEntry != null ? ItemCategory.medicine : ItemCategory.vitamin);
  late String _unit = _info?.unit ?? 'mg';
  late MealRelation _meal = _info?.meal ?? MealRelation.none;
  late final Set<String> _slots = {_info?.defaultSlot ?? 'earlyMorning'};
  Freq _freq = Freq.daily;
  int _everyN = 3;
  final Set<int> _weekdays = {};
  int _cycleOn = 21, _cycleOff = 7;
  TimeOfDayHM _firstDose = (h: 8, m: 0);
  int _intervalHours = 8;
  bool _isCritical = false, _persistent = false, _saving = false, _nameInit = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_nameInit) return;
    _nameInit = true;
    _name.text = _info?.name.of(context.l10n.localeName) ?? widget.args.drugEntry?.name ?? widget.args.customName ?? '';
  }

  @override
  void dispose() {
    for (final c in [_name, _amount, _qty, _stock, _critical]) {
      c.dispose();
    }
    super.dispose();
  }

  double? _num(TextEditingController c) => double.tryParse(c.text.replaceAll(',', '.'));

  List<String> get _orderedSlots => slotOrder.where(_slots.contains).toList();

  DoseRule _rule(AppSettings settings) {
    final start = dateOnly(DateTime.now());
    final qty = _num(_qty) ?? 1;
    FixedTimesRule fixed({int everyN = 1, Set<int>? weekdays}) => SupplementService.ruleFromSlots(
          slots: _orderedSlots, settings: settings, start: start, quantity: qty, meal: _meal,
          everyNDays: everyN, weekdays: weekdays) as FixedTimesRule;
    return switch (_freq) {
      Freq.daily => fixed(),
      Freq.everyOtherDay => fixed(everyN: 2),
      Freq.mwf => fixed(weekdays: {1, 3, 5}),
      Freq.weekends => fixed(weekdays: {6, 7}),
      Freq.everyN => fixed(everyN: _everyN),
      Freq.specificDays => fixed(weekdays: _weekdays.isEmpty ? {1, 2, 3, 4, 5, 6, 7} : Set.of(_weekdays)),
      Freq.cycle => CyclicRule(startDate: start, quantity: qty, meal: _meal, inner: fixed(), onDays: _cycleOn, offDays: _cycleOff),
      Freq.interval => IntervalRule(startDate: start, quantity: qty, meal: _meal, firstDose: _firstDose, intervalHours: _intervalHours),
      Freq.asNeeded => AsNeededRule(startDate: start, quantity: qty, maxPerDay: 3, minIntervalHours: 4),
    };
  }

  bool get _usesSlots => _freq != Freq.interval && _freq != Freq.asNeeded;

  Future<void> _save() async {
    final t = context.l10n;
    if (!_formKey.currentState!.validate()) return;
    if (_usesSlots && _slots.isEmpty) {
      showSnack(context, t.pickAtLeastOneSlot);
      return;
    }
    setState(() => _saving = true);
    final settings = ref.read(settingsProvider);
    final services = ref.read(servicesProvider);
    try {
      await services.supplements.addSupplement(
        medicine: Medicine(
          name: _name.text.trim(),
          category: _category,
          catalogId: _info?.id,
          doseAmount: _num(_amount),
          doseUnit: _unit,
          form: _unit == 'capsule' ? 'capsule' : 'tablet',
          barcode: widget.args.code?.barcode13,
          packageSize: widget.args.drugEntry?.packageSize,
          criticalStockLevel: _num(_critical) ?? 5,
          isCritical: _isCritical,
        ),
        rule: _rule(settings),
        slots: _usesSlots ? _orderedSlots : const [],
        initialStock: _num(_stock) ?? 0,
        scanned: widget.args.code,
        persistent: _persistent,
      );
      if (!mounted) return;
      showSnack(context, t.saved);
      context.go('/supplements');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final settings = ref.watch(settingsProvider);
    final plans = ref.watch(plansProvider).value ?? const <SupplementPlan>[];
    final rule = _rule(settings);
    final text = Theme.of(context).textTheme;
    final wd = DateFormat.E(t.localeName);
    String? required(String? v) => (v == null || v.trim().isEmpty) ? t.requiredField : null;

    // Canlı çakışma kontrolü: mevcut plan + bu aday.
    final hours = switch (rule) {
      FixedTimesRule(:final times) => times.map((x) => x.h).toSet(),
      CyclicRule(:final inner) => inner.times.map((x) => x.h).toSet(),
      _ => <int>{},
    };
    final conflicts = _info == null
        ? const <Conflict>[]
        : findConflicts([
            for (final p in plans)
              if (p.schedule != null && !p.isAsNeeded)
                PlannedItem(medicineId: p.medicine.id!, catalogId: p.medicine.catalogId, name: p.medicine.displayName, hours: p.times.map((x) => x.h).toSet()),
            PlannedItem(medicineId: -1, catalogId: _info.id, name: _name.text, hours: hours),
          ]).where((c) => c.first.medicineId == -1 || c.second.medicineId == -1).toList();

    final amount = _num(_amount);
    final ulTotal = (_info?.ul != null && amount != null && rule is! AsNeededRule)
        ? dailyTotalInCatalogUnit(_info!, amount * rule.quantity, _unit, rule.averageDailyQuantity() / rule.quantity)
        : null;
    // Diğer takviyelerden gelen aynı besin (ör. iki ayrı D vitamini) de toplanır.
    final otherSame = plans.where((p) => p.medicine.catalogId == _info?.id && p.medicine.doseAmount != null && p.rule != null && p.medicine.doseUnit != null).fold<double>(0, (sum, p) {
      final v = dailyTotalInCatalogUnit(_info!, p.medicine.doseAmount! * p.rule!.quantity, p.medicine.doseUnit!, p.rule!.averageDailyQuantity() / p.rule!.quantity);
      return sum + (v ?? 0);
    });
    final overUl = ulTotal != null && ulTotal + otherSame > _info!.ul!;

    Widget chip(Freq f, String label) => ChoiceChip(label: Text(label), selected: _freq == f, onSelected: (_) => setState(() => _freq = f));

    return Scaffold(
      appBar: AppBar(title: Text(_info?.name.of(t.localeName) ?? t.addMedicine)),
      body: Form(
        key: _formKey,
        child: ListView(padding: const EdgeInsets.fromLTRB(16, 8, 16, 32), children: [
          if (_info != null) Padding(padding: const EdgeInsets.only(bottom: 8), child: Center(child: SupplementIcon(catalogId: _info.id, size: 72))),
          TextFormField(controller: _name, decoration: InputDecoration(labelText: t.medicineName), validator: required),
          const SizedBox(height: 12),
          DropdownButtonFormField<ItemCategory>(
            initialValue: _category,
            decoration: InputDecoration(labelText: t.categoryLabel),
            items: [for (final c in ItemCategory.values) DropdownMenuItem(value: c, child: Text(categoryLabel(t, c)))],
            onChanged: (v) => setState(() => _category = v ?? ItemCategory.vitamin),
          ),
          const SizedBox(height: 12),
          Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Expanded(flex: 3, child: _numberField(_amount, t.amountLabel, onChanged: (_) => setState(() {}))),
            const SizedBox(width: 12),
            Expanded(
              flex: 2,
              child: DropdownButtonFormField<String>(
                initialValue: _unit,
                decoration: InputDecoration(labelText: t.unitLabel),
                items: [for (final u in supplementUnits) DropdownMenuItem(value: u, child: Text(unitLabel(t, u)))],
                onChanged: (v) => setState(() => _unit = v ?? 'mg'),
              ),
            ),
          ]),
          Padding(padding: const EdgeInsets.only(top: 4), child: Text(t.labelDoseHint, style: text.bodySmall)),
          const SizedBox(height: 12),
          _numberField(_qty, t.quantityPerDose, onChanged: (_) => setState(() {})),
          if (overUl) ...[
            const SizedBox(height: 8),
            _warn(context, t.ulWarning('${formatQty(ulTotal + otherSame)} ${_info.unit}', '${formatQty(_info.ul!)} ${_info.unit}')),
          ],
          const Divider(height: 32),
          Text(t.howOften, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
          const SizedBox(height: 8),
          Wrap(spacing: 8, runSpacing: 4, children: [
            chip(Freq.daily, t.freqDaily),
            chip(Freq.everyOtherDay, t.freqEveryOtherDay),
            chip(Freq.mwf, t.freqMwf),
            chip(Freq.weekends, t.freqWeekends),
            chip(Freq.everyN, t.freqEveryNDays),
            chip(Freq.specificDays, t.freqSpecificDays),
            chip(Freq.cycle, t.freqCycle),
            chip(Freq.interval, t.freqInterval),
            chip(Freq.asNeeded, t.freqAsNeeded),
          ]),
          const SizedBox(height: 8),
          ...switch (_freq) {
            Freq.everyN => [
                DropdownButtonFormField<int>(
                  initialValue: _everyN,
                  decoration: InputDecoration(labelText: t.nDaysLabel),
                  items: [for (final n in [3, 4, 5, 7, 10, 14]) DropdownMenuItem(value: n, child: Text(t.everyNDaysLabel(n)))],
                  onChanged: (v) => setState(() => _everyN = v ?? 3),
                ),
              ],
            Freq.specificDays => [
                Wrap(spacing: 6, children: [
                  for (var d = 1; d <= 7; d++)
                    FilterChip(
                      label: Text(wd.format(DateTime(2024, 1, d))),
                      selected: _weekdays.contains(d),
                      onSelected: (on) => setState(() => on ? _weekdays.add(d) : _weekdays.remove(d)),
                    ),
                ]),
              ],
            Freq.cycle => [
                Row(children: [
                  Expanded(child: _intDropdown(t.cycleOnDays, _cycleOn, [5, 7, 14, 21, 28, 56], (v) => _cycleOn = v)),
                  const SizedBox(width: 12),
                  Expanded(child: _intDropdown(t.cycleOffDays, _cycleOff, [2, 3, 7, 14, 28], (v) => _cycleOff = v)),
                ]),
              ],
            Freq.interval => [
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(t.firstDose),
                  trailing: Text(formatHm(_firstDose), style: text.titleLarge),
                  onTap: () async {
                    final p = await showTimePicker(context: context, initialTime: TimeOfDay(hour: _firstDose.h, minute: _firstDose.m));
                    if (p != null) setState(() => _firstDose = (h: p.hour, m: p.minute));
                  },
                ),
                _intDropdown(t.freqInterval, _intervalHours, [4, 6, 8, 12], (v) => _intervalHours = v, label: t.everyNHoursShort),
              ],
            _ => const <Widget>[],
          },
          if (_usesSlots) ...[
            const SizedBox(height: 16),
            Text(t.whenToTake, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            Wrap(spacing: 8, runSpacing: 4, children: [
              for (final s in slotOrder)
                FilterChip(
                  key: Key('slot_$s'),
                  label: Text('${slotLabel(t, s)} ${settings.slotTime(s)}'),
                  selected: _slots.contains(s),
                  onSelected: (on) => setState(() => on ? _slots.add(s) : _slots.remove(s)),
                ),
            ]),
          ],
          for (final c in conflicts) ...[
            const SizedBox(height: 8),
            _warn(context, t.conflictWarning(c.first.medicineId == -1 ? c.second.name : c.first.name, c.rule.reason.of(t.localeName))),
          ],
          if (_freq != Freq.asNeeded) ...[
            const SizedBox(height: 16),
            Text(t.mealRelation, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 8),
            Wrap(spacing: 8, runSpacing: 4, children: [
              for (final m in MealRelation.values)
                ChoiceChip(label: Text(mealLabel(t, m)), selected: _meal == m, onSelected: (_) => setState(() => _meal = m)),
            ]),
            if (_info?.note != null) Padding(padding: const EdgeInsets.only(top: 6), child: Text(_info!.note!.of(t.localeName), style: text.bodySmall)),
            const SizedBox(height: 16),
            Text(t.nextDays14, style: text.labelLarge),
            const SizedBox(height: 6),
            if (!_usesSlots || _slots.isNotEmpty) Next14Days(rule: rule),
          ],
          const Divider(height: 32),
          Row(children: [
            Expanded(child: _numberField(_stock, t.stock)),
            const SizedBox(width: 12),
            Expanded(child: _numberField(_critical, t.criticalStock)),
          ]),
          ExpansionTile(
            tilePadding: EdgeInsets.zero,
            title: Text(t.advanced),
            children: [
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: Text(t.criticalMedicine),
                subtitle: Text(t.criticalMedicineHint),
                value: _isCritical,
                onChanged: (v) => setState(() => _isCritical = v),
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: Text(t.persistentAlarm),
                subtitle: Text(t.persistentAlarmHint),
                value: _persistent,
                onChanged: (v) => setState(() => _persistent = v),
              ),
            ],
          ),
          const SizedBox(height: 16),
          FilledButton(key: const Key('saveSupplement'), onPressed: _saving ? null : _save, child: Text(t.save)),
        ]),
      ),
    );
  }

  Widget _warn(BuildContext context, String msg) => Container(
        padding: const EdgeInsets.all(10),
        decoration: BoxDecoration(color: const Color(0xFFFFF3E0), borderRadius: BorderRadius.circular(8)),
        child: Row(children: [
          const Icon(Icons.warning_amber_rounded, color: Color(0xFFE65100)),
          const SizedBox(width: 8),
          Expanded(child: Text(msg, style: const TextStyle(color: Color(0xFF3E1A00)))),
        ]),
      );

  Widget _intDropdown(String title, int value, List<int> options, void Function(int) set, {String Function(int)? label}) =>
      DropdownButtonFormField<int>(
        initialValue: options.contains(value) ? value : options.first,
        decoration: InputDecoration(labelText: title),
        items: [for (final o in options) DropdownMenuItem(value: o, child: Text(label?.call(o) ?? '$o'))],
        onChanged: (v) => setState(() => set(v ?? options.first)),
      );

  Widget _numberField(TextEditingController c, String label, {ValueChanged<String>? onChanged}) => TextFormField(
        controller: c,
        onChanged: onChanged,
        decoration: InputDecoration(labelText: label),
        keyboardType: const TextInputType.numberWithOptions(decimal: true),
        inputFormatters: [FilteringTextInputFormatter.allow(RegExp(r'[0-9.,]'))],
      );
}

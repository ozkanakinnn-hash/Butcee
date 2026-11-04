import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';

import '../../data/drug_catalog.dart';
import '../../domain/dose_rule.dart';
import '../../domain/gs1_parser.dart';
import '../../domain/models.dart';
import '../../services/localized_texts.dart';
import '../../state/providers.dart';
import '../widgets/common.dart';

/// Tarama veya elle girişten gelen ön doldurma verisi.
class AddMedicineArgs {
  const AddMedicineArgs({this.code, this.entry});
  final ScannedDrugCode? code;
  final DrugCatalogEntry? entry;
}

enum _Freq { fixedTimes, interval, asNeeded }

class AddMedicineScreen extends ConsumerStatefulWidget {
  const AddMedicineScreen({super.key, this.args = const AddMedicineArgs()});
  final AddMedicineArgs args;

  @override
  ConsumerState<AddMedicineScreen> createState() => _AddMedicineScreenState();
}

class _AddMedicineScreenState extends ConsumerState<AddMedicineScreen> {
  final _formKey = GlobalKey<FormState>();
  late final _name = TextEditingController(text: widget.args.entry?.name ?? '');
  final _nickname = TextEditingController();
  late final _stock = TextEditingController(text: '${widget.args.entry?.packageSize ?? ''}');
  final _critical = TextEditingController(text: '5');
  final _qty = TextEditingController(text: '1');
  late String _form = widget.args.entry?.form ?? 'tablet';

  _Freq _freq = _Freq.fixedTimes;
  final List<TimeOfDayHM> _times = [(h: 9, m: 0)];
  int _everyNDays = 1;
  final Set<int> _weekdays = {};
  TimeOfDayHM _firstDose = (h: 8, m: 0);
  int _intervalHours = 8;
  int _maxPerDay = 3;
  MealRelation _meal = MealRelation.none;
  bool _isCritical = false;
  bool _persistent = false;
  bool _saving = false;

  static const _forms = ['tablet', 'capsule', 'syrup', 'drops', 'injection', 'other'];

  @override
  void dispose() {
    for (final c in [_name, _nickname, _stock, _critical, _qty]) {
      c.dispose();
    }
    super.dispose();
  }

  double? _num(TextEditingController c) => double.tryParse(c.text.replaceAll(',', '.'));

  Future<void> _pickTime(int? index) async {
    final initial = index == null ? const TimeOfDay(hour: 12, minute: 0) : TimeOfDay(hour: _times[index].h, minute: _times[index].m);
    final picked = await showTimePicker(context: context, initialTime: initial);
    if (picked == null) return;
    setState(() {
      final v = (h: picked.hour, m: picked.minute);
      if (index == null) {
        if (!_times.contains(v)) _times.add(v);
      } else {
        _times[index] = v;
      }
      _times.sort((a, b) => (a.h * 60 + a.m).compareTo(b.h * 60 + b.m));
    });
  }

  DoseRule _buildRule() {
    final today = dateOnly(DateTime.now());
    final qty = _num(_qty) ?? 1;
    return switch (_freq) {
      _Freq.fixedTimes => FixedTimesRule(
          startDate: today,
          quantity: qty,
          meal: _meal,
          times: List.of(_times),
          everyNDays: _everyNDays,
          weekdays: _weekdays.isEmpty ? null : Set.of(_weekdays),
        ),
      _Freq.interval => IntervalRule(
          startDate: today,
          quantity: qty,
          meal: _meal,
          firstDose: _firstDose,
          intervalHours: _intervalHours,
        ),
      _Freq.asNeeded => AsNeededRule(startDate: today, quantity: qty, maxPerDay: _maxPerDay, minIntervalHours: 4),
    };
  }

  Future<void> _save() async {
    final t = context.l10n;
    if (!_formKey.currentState!.validate()) return;
    if (_freq == _Freq.fixedTimes && _times.isEmpty) {
      showSnack(context, t.atLeastOneTime);
      return;
    }
    setState(() => _saving = true);
    final services = ref.read(servicesProvider);
    final code = widget.args.code;
    final medicineId = await services.db.insertMedicine(Medicine(
      name: _name.text.trim(),
      nickname: _nickname.text.trim().isEmpty ? null : _nickname.text.trim(),
      form: _form,
      barcode: code?.barcode13,
      totalStock: 0,
      criticalStockLevel: _num(_critical) ?? 5,
      packageSize: widget.args.entry?.packageSize,
      isCritical: _isCritical,
    ));
    final stock = _num(_stock) ?? 0;
    if (code != null) {
      await services.db.addScannedPack(medicineId, code, stock, DateTime.now());
    } else if (stock > 0) {
      await services.db.adjustStock(medicineId, stock);
    }
    await services.db.upsertSchedule(Schedule(
      id: 'sch_${DateTime.now().microsecondsSinceEpoch}',
      medicineId: medicineId,
      rule: _buildRule(),
      isCritical: _isCritical,
      persistent: PersistentAlarm(enabled: _persistent),
      graceMinutes: _isCritical ? 30 : 60,
    ));
    await services.planner.reconcile();
    if (!mounted) return;
    showSnack(context, t.saved);
    context.go('/');
  }

  @override
  Widget build(BuildContext context) {
    final t = context.l10n;
    final code = widget.args.code;
    String? required(String? v) => (v == null || v.trim().isEmpty) ? t.requiredField : null;
    final weekdayFmt = DateFormat.E(t.localeName);

    return Scaffold(
      appBar: AppBar(title: Text(t.addMedicine)),
      body: Form(
        key: _formKey,
        child: ListView(padding: const EdgeInsets.all(16), children: [
          if (code?.expiry != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Chip(
                avatar: const Icon(Icons.event_available),
                label: Text(t.expiryLabel(DateFormat.yMd(t.localeName).format(code!.expiry!))),
              ),
            ),
          TextFormField(controller: _name, decoration: InputDecoration(labelText: t.medicineName), validator: required),
          const SizedBox(height: 12),
          TextFormField(
            controller: _nickname,
            decoration: InputDecoration(labelText: t.nickname, hintText: t.nicknameHint),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            initialValue: _forms.contains(_form) ? _form : 'other',
            decoration: InputDecoration(labelText: t.form),
            items: [for (final f in _forms) DropdownMenuItem(value: f, child: Text(formLabel(t, f)))],
            onChanged: (v) => setState(() => _form = v ?? 'tablet'),
          ),
          const SizedBox(height: 12),
          Row(children: [
            Expanded(child: _numberField(_stock, t.stock)),
            const SizedBox(width: 12),
            Expanded(child: _numberField(_critical, t.criticalStock)),
          ]),
          const Divider(height: 32),
          Text(t.frequency, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          SegmentedButton<_Freq>(
            segments: [
              ButtonSegment(value: _Freq.fixedTimes, label: Text(t.freqFixedTimes)),
              ButtonSegment(value: _Freq.interval, label: Text(t.freqInterval)),
              ButtonSegment(value: _Freq.asNeeded, label: Text(t.freqAsNeeded)),
            ],
            selected: {_freq},
            onSelectionChanged: (s) => setState(() => _freq = s.first),
          ),
          const SizedBox(height: 12),
          ...switch (_freq) {
            _Freq.fixedTimes => [
                Text(t.doseTimes),
                Wrap(spacing: 8, children: [
                  for (var i = 0; i < _times.length; i++)
                    InputChip(
                      label: Text(formatHm(_times[i])),
                      onPressed: () => _pickTime(i),
                      onDeleted: () => setState(() => _times.removeAt(i)),
                    ),
                  ActionChip(avatar: const Icon(Icons.add), label: Text(t.addTime), onPressed: () => _pickTime(null)),
                ]),
                const SizedBox(height: 12),
                DropdownButtonFormField<int>(
                  initialValue: _everyNDays,
                  items: [for (final n in [1, 2, 3, 7]) DropdownMenuItem(value: n, child: Text(t.everyNDaysLabel(n)))],
                  onChanged: (v) => setState(() => _everyNDays = v ?? 1),
                ),
                const SizedBox(height: 12),
                Text(t.onlyTheseDays),
                Wrap(spacing: 6, children: [
                  for (var wd = DateTime.monday; wd <= DateTime.sunday; wd++)
                    FilterChip(
                      label: Text(weekdayFmt.format(DateTime(2024, 1, wd))), // 1 Ocak 2024 = Pazartesi
                      selected: _weekdays.contains(wd),
                      onSelected: (on) => setState(() => on ? _weekdays.add(wd) : _weekdays.remove(wd)),
                    ),
                ]),
              ],
            _Freq.interval => [
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  title: Text(t.firstDose),
                  trailing: Text(formatHm(_firstDose), style: Theme.of(context).textTheme.titleLarge),
                  onTap: () async {
                    final p = await showTimePicker(
                        context: context, initialTime: TimeOfDay(hour: _firstDose.h, minute: _firstDose.m));
                    if (p != null) setState(() => _firstDose = (h: p.hour, m: p.minute));
                  },
                ),
                DropdownButtonFormField<int>(
                  initialValue: _intervalHours,
                  items: [for (final h in [4, 6, 8, 12, 24, 48]) DropdownMenuItem(value: h, child: Text(t.intervalEvery(h)))],
                  onChanged: (v) => setState(() => _intervalHours = v ?? 8),
                ),
              ],
            _Freq.asNeeded => [
                DropdownButtonFormField<int>(
                  initialValue: _maxPerDay,
                  decoration: InputDecoration(labelText: t.maxPerDay),
                  items: [for (final n in [1, 2, 3, 4, 6]) DropdownMenuItem(value: n, child: Text('$n'))],
                  onChanged: (v) => setState(() => _maxPerDay = v ?? 3),
                ),
              ],
          },
          const SizedBox(height: 12),
          _numberField(_qty, t.quantityPerDose),
          const SizedBox(height: 12),
          if (_freq != _Freq.asNeeded)
            DropdownButtonFormField<MealRelation>(
              initialValue: _meal,
              decoration: InputDecoration(labelText: t.mealRelation),
              items: [for (final m in MealRelation.values) DropdownMenuItem(value: m, child: Text(mealLabel(t, m)))],
              onChanged: (v) => setState(() => _meal = v ?? MealRelation.none),
            ),
          const Divider(height: 32),
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
          const SizedBox(height: 16),
          FilledButton(onPressed: _saving ? null : _save, child: Text(t.save)),
        ]),
      ),
    );
  }

  Widget _numberField(TextEditingController c, String label) => TextFormField(
        controller: c,
        decoration: InputDecoration(labelText: label),
        keyboardType: const TextInputType.numberWithOptions(decimal: true),
        inputFormatters: [FilteringTextInputFormatter.allow(RegExp(r'[0-9.,]'))],
      );
}

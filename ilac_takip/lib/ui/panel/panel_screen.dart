import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../state/providers.dart';
import '../widgets/common.dart';
import '../widgets/report_actions.dart';
import '../../services/localized_texts.dart';

/// Tüm araçların bir arada olduğu panel.
class PanelScreen extends ConsumerWidget {
  const PanelScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final t = context.l10n;
    final conflicts = ref.watch(conflictsProvider).value?.length ?? 0;
    final cs = Theme.of(context).colorScheme;

    final groups = <(String?, List<_Tool>)>[
      (null, [
        _Tool(Icons.qr_code_scanner, t.toolScan, const Color(0xFF5C6BC0), () => context.push('/scan')),
        _Tool(Icons.search, t.toolCatalog, const Color(0xFF26A69A), () => context.push('/catalog')),
        _Tool(Icons.compare_arrows, t.toolInteractions, const Color(0xFFEF6C00), () => context.push('/interactions'), badge: conflicts),
        _Tool(Icons.auto_awesome, t.toolPresets, const Color(0xFFAB47BC), () => context.push('/presets')),
      ]),
      (t.toolDoctorReport, [
        _Tool(Icons.picture_as_pdf_outlined, t.toolProgramPdf, const Color(0xFF00897B), () => ReportActions.shareProgram(context, ref)),
        _Tool(Icons.medical_information_outlined, t.toolDoctorReport, const Color(0xFF1E88E5), () => ReportActions.shareDoctorReport(context, ref)),
      ]),
      (t.findPharmacy, [
        _Tool(Icons.local_pharmacy_outlined, t.toolNearbyPharmacy, const Color(0xFF00ACC1), () => ReportActions.nearbyPharmacies(context)),
        _Tool(Icons.nightlight_outlined, t.toolDutyPharmacy, const Color(0xFFD81B60), () => ReportActions.dutyPharmacies(context)),
        _Tool(Icons.report_outlined, t.toolSideEffect, const Color(0xFF8E24AA), () => ReportActions.sideEffect(context)),
      ]),
      (t.settings, [
        _Tool(Icons.alarm, t.toolReminders, const Color(0xFFF9A825), () => context.push('/reminders')),
        _Tool(Icons.verified_user_outlined, t.reliability, const Color(0xFF43A047), () => context.push('/reliability')),
        _Tool(Icons.elderly, t.seniorMode, const Color(0xFF6D4C41), () => context.push('/settings')),
        _Tool(Icons.settings_outlined, t.settings, const Color(0xFF546E7A), () => context.push('/settings')),
      ]),
    ];

    return Scaffold(
      body: CustomScrollView(slivers: [
        SliverAppBar.medium(
          title: Text(t.appTitle, style: const TextStyle(fontWeight: FontWeight.w800)),
          backgroundColor: cs.primaryContainer,
          foregroundColor: cs.onPrimaryContainer,
        ),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 12, 20, 0),
            child: Text(t.panelSubtitle, style: Theme.of(context).textTheme.titleMedium),
          ),
        ),
        for (final (title, tools) in groups) ...[
          if (title != null)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 4),
                child: Text(upper(title, t.localeName),
                    style: Theme.of(context).textTheme.labelLarge?.copyWith(letterSpacing: 1.1, fontWeight: FontWeight.w700, color: cs.primary)),
              ),
            ),
          SliverPadding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            sliver: SliverGrid.count(
              crossAxisCount: 2,
              childAspectRatio: 1.15,
              mainAxisSpacing: 8,
              crossAxisSpacing: 8,
              children: [for (final tool in tools) _ToolCard(tool)],
            ),
          ),
        ],
        const SliverToBoxAdapter(child: SizedBox(height: 24)),
      ]),
    );
  }
}

class _Tool {
  const _Tool(this.icon, this.label, this.color, this.onTap, {this.badge = 0});
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  final int badge;
}

class _ToolCard extends StatelessWidget {
  const _ToolCard(this.tool);
  final _Tool tool;

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: tool.onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
          child: Column(mainAxisAlignment: MainAxisAlignment.center, mainAxisSize: MainAxisSize.min, children: [
            Badge(
              isLabelVisible: tool.badge > 0,
              label: Text('${tool.badge}'),
              child: Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(color: tool.color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(14)),
                child: Icon(tool.icon, color: tool.color, size: 30),
              ),
            ),
            const SizedBox(height: 8),
            Flexible(
              child: Text(tool.label, textAlign: TextAlign.center, maxLines: 2, overflow: TextOverflow.ellipsis,
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
            ),
          ]),
        ),
      ),
    );
  }
}

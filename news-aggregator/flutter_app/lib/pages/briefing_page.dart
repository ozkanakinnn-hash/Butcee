import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/categories_record.dart';
import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import 'article_webview_page.dart';
import 'match_detail_page.dart';

/// Şu an geçerli gündem dilimi: 08:30–19:00 sabah, 19:00–08:30 akşam (sunucuyla aynı kural).
bool isEveningSlot(DateTime t) {
  final m = t.hour * 60 + t.minute;
  return m >= 19 * 60 || m < 8 * 60 + 30;
}

/// "5 dakikada gündem" (sabah 08:30 / akşam 19:00): sunucu `briefings/latest` belgesini otomatik hazırlar.
/// Bölümler: 1) Gündem — en çok yayıncının verdiği haberler, 2) İlgi alanların — seçtiğin kategorilerden kısa başlıklar,
/// 3) Futbol seçiliyse günün maçları (takip edilen takımlar).
class BriefingPage extends StatefulWidget {
  const BriefingPage({super.key, this.id, this.repository = const NewsRepository()});

  final String? id;
  final NewsRepository repository;

  static Route<void> route({String? id}) => MaterialPageRoute<void>(builder: (_) => BriefingPage(id: id));

  @override
  State<BriefingPage> createState() => _BriefingPageState();
}

class _BriefingPageState extends State<BriefingPage> {
  late final Stream<Map<String, dynamic>?> _briefing = widget.repository.watchBriefing(widget.id);
  late final Stream<List<CategoriesRecord>> _categories = widget.repository.watchCategories();
  AppSettings? _settings;
  Stream<List<MatchRecord>>? _matches;

  @override
  void initState() {
    super.initState();
    AppSettings.load().then((s) {
      if (!mounted) return;
      setState(() {
        _settings = s;
        if (s.followsFootball && s.followedTeams.isNotEmpty) _matches = widget.repository.watchMatches(s.followedTeams);
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    return StreamBuilder<Map<String, dynamic>?>(
      stream: _briefing,
      builder: (context, snap) {
        final data = snap.data;
        // Başlık: belgenin dilimi; belge yoksa şu anki saate göre (eskiden boşken hep "Sabah" yazıyordu)
        final evening = data != null ? data['slot'] == 'pm' : isEveningSlot(DateTime.now());
        final items = ((data?['items'] as List?) ?? const []).whereType<Map>().toList();
        final byCategory = (data?['by_category'] as Map?) ?? const {};
        return Scaffold(
          appBar: AppBar(title: Text(evening ? s.briefingEvening : s.briefingMorning)),
          body: snap.connectionState == ConnectionState.waiting && data == null
              ? const Center(child: CircularProgressIndicator())
              : StreamBuilder<List<CategoriesRecord>>(
                  stream: _categories,
                  builder: (context, cats) {
                    final names = {for (final c in cats.data ?? const <CategoriesRecord>[]) c.id: c.localizedName(s.isEnglish)};
                    final shown = items.map((e) => '${e['id']}').toSet();
                    final interestSections = <Widget>[
                      for (final id in _settings?.interests ?? const <String>[])
                        if (byCategory[id] is List)
                          ...() {
                            final list = (byCategory[id] as List).whereType<Map>().where((e) => !shown.contains('${e['id']}')).toList();
                            if (list.isEmpty) return const <Widget>[];
                            return [
                              _SectionTitle(names[id] ?? id),
                              for (final e in list) _StoryRow(item: e, compact: true),
                            ];
                          }(),
                    ];
                    return ListView(
                      padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
                      children: [
                        Text(
                          '${evening ? '🌙' : '☀️'} ${s.briefingSubtitle}${data?['day'] != null ? ' · ${data!['day']}' : ''}',
                          style: Theme.of(context).textTheme.titleSmall?.copyWith(
                            color: Theme.of(context).colorScheme.onSurfaceVariant,
                          ),
                        ),
                        if (items.isEmpty)
                          Padding(padding: const EdgeInsets.symmetric(vertical: 32), child: Text(s.briefingEmpty, textAlign: TextAlign.center))
                        else ...[
                          _SectionTitle(s.briefingHeadlines),
                          for (var i = 0; i < items.length && i < 5; i++) _StoryRow(item: items[i], index: i + 1),
                        ],
                        if (interestSections.isNotEmpty) ...[
                          _SectionTitle(s.briefingInterests, big: true),
                          ...interestSections,
                        ],
                        if (_matches != null) _TodayMatches(stream: _matches!, repository: widget.repository),
                      ],
                    );
                  },
                ),
        );
      },
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.text, {this.big = false});
  final String text;
  final bool big;

  @override
  Widget build(BuildContext context) => Padding(
    padding: EdgeInsets.only(top: big ? 24 : 16, bottom: 6),
    child: Text(
      text,
      style: (big ? Theme.of(context).textTheme.titleLarge : Theme.of(context).textTheme.titleMedium)
          ?.copyWith(fontWeight: FontWeight.w800),
    ),
  );
}

class _StoryRow extends StatelessWidget {
  const _StoryRow({required this.item, this.index, this.compact = false});
  final Map item;
  final int? index;
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final sources = ((item['sources'] as List?) ?? const []).whereType<String>().toList();
    final title = '${item['title'] ?? ''}';
    final url = '${item['url'] ?? ''}';
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      onTap: url.isEmpty
          ? null
          : () => Navigator.of(context).push(
              ArticleWebViewPage.route(url: url, sourceName: '${item['source'] ?? ''}', articleTitle: title),
            ),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (index != null) ...[
              CircleAvatar(
                radius: 13,
                backgroundColor: theme.colorScheme.primaryContainer,
                child: Text('$index', style: TextStyle(color: theme.colorScheme.onPrimaryContainer, fontWeight: FontWeight.w800)),
              ),
              const SizedBox(width: 12),
            ] else
              const Padding(padding: EdgeInsets.only(right: 10, top: 2), child: Text('•')),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: (compact ? theme.textTheme.bodyLarge : theme.textTheme.titleMedium)?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    sources.length >= 2 ? '${s.inSources(sources.length)}: ${sources.take(4).join(', ')}' : '${item['source'] ?? ''}',
                    style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Takip edilen takımların bugünkü maçları (saat ya da canlı skor).
class _TodayMatches extends StatelessWidget {
  const _TodayMatches({required this.stream, required this.repository});
  final Stream<List<MatchRecord>> stream;
  final NewsRepository repository;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return StreamBuilder<List<MatchRecord>>(
      stream: stream,
      builder: (context, snap) {
        final today = DateTime.now();
        final list = (snap.data ?? const <MatchRecord>[])
            .where((m) => DateUtils.isSameDay(m.kickoff.toLocal(), today))
            .toList()
          ..sort((a, b) => a.kickoff.compareTo(b.kickoff));
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _SectionTitle(s.briefingMatches, big: true),
            if (list.isEmpty)
              Text(s.briefingNoMatches, style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant))
            else
              for (final m in list)
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.sports_soccer),
                  title: Text('${m.home.name} – ${m.away.name}'),
                  trailing: Text(
                    m.notStarted
                        ? '${m.kickoff.toLocal().hour.toString().padLeft(2, '0')}:${m.kickoff.toLocal().minute.toString().padLeft(2, '0')}'
                        : '${m.homeGoals}-${m.awayGoals}${m.isLive ? " · ${m.elapsed ?? ''}'" : ''}',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                      color: m.isLive ? const Color(0xFFD62828) : null,
                    ),
                  ),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute<void>(builder: (_) => MatchDetailPage(fixtureId: m.fixtureId, repository: repository)),
                  ),
                ),
          ],
        );
      },
    );
  }
}

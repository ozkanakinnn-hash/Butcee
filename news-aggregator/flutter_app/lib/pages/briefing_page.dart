import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../backend/schema/categories_record.dart';
import '../backend/schema/football_records.dart';
import '../l10n/app_strings.dart';
import '../theme.dart';
import '../utils/briefing_slot.dart';
import '../utils/storage.dart';
import 'article_webview_page.dart';
import 'match_detail_page.dart';

/// Şu an geçerli gündem dilimi: 08:30–19:00 sabah, 19:00–08:30 akşam (sunucuyla aynı kural).
bool isEveningSlot(DateTime t) {
  final m = t.hour * 60 + t.minute;
  return m >= 19 * 60 || m < 8 * 60 + 30;
}

/// "5 dakikada gündem" (sabah 08:30 / akşam 19:00) — gazete düzeni:
/// künye (logo, baskı, tarih) → manşet (görsel + spot) → günün özeti (AI, varsa) → kısa kısa (iki sütun)
/// → gündem → ilgi alanların (bölümler) → bugünün maçları. Haber satırlarında görsel SAĞDA.
///
/// Bildirimden açılınca [id] o bültenin kimliğidir. [id] yoksa son bülten açılır; şu anki dilime ait değilse
/// (sunucu henüz yazmadıysa) dünkü bülten "bugünkü" gibi GÖSTERİLMEZ — "hazırlanıyor" yazar.
class BriefingPage extends StatefulWidget {
  const BriefingPage({super.key, this.id, this.repository = const NewsRepository(), this.now});

  final String? id;
  final NewsRepository repository;

  /// Testler için sabit saat.
  final DateTime? now;

  static Route<void> route({String? id}) => MaterialPageRoute<void>(builder: (_) => BriefingPage(id: id));

  @override
  State<BriefingPage> createState() => _BriefingPageState();
}

class _BriefingPageState extends State<BriefingPage> {
  late final Stream<Map<String, dynamic>?> _briefing = widget.repository.watchBriefing(widget.id);
  late final Stream<List<CategoriesRecord>> _categories = widget.repository.watchCategories();
  AppSettings? _settings;
  Stream<List<MatchRecord>>? _matches;
  bool _showPrevious = false;

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
    final now = widget.now ?? DateTime.now();
    return Scaffold(
      appBar: AppBar(toolbarHeight: 44),
      body: StreamBuilder<Map<String, dynamic>?>(
        stream: _briefing,
        builder: (context, snap) {
          final raw = snap.data;
          if (snap.connectionState == ConnectionState.waiting && raw == null) {
            return const Center(child: CircularProgressIndicator());
          }
          // Belirli bir bülten istenmediyse yalnızca şu anki dilimin bülteni gösterilir.
          final stale = widget.id == null && raw != null && !isCurrentBriefing(raw, now);
          final data = stale && !_showPrevious ? null : raw;
          final evening = data != null ? data['slot'] == 'pm' : isEveningSlot(istanbulTime(now));
          final items = ((data?['items'] as List?) ?? const []).whereType<Map>().toList();
          final byCategory = (data?['by_category'] as Map?) ?? const {};
          final aiSummary = '${data?['ai_summary'] ?? ''}'.trim();
          final day = _parseDay(data?['day']) ?? istanbulTime(now);

          return StreamBuilder<List<CategoriesRecord>>(
            stream: _categories,
            builder: (context, cats) {
              final names = {for (final c in cats.data ?? const <CategoriesRecord>[]) c.id: c.localizedName(s.isEnglish)};
              final shown = items.map((e) => '${e['id']}').toSet();
              final interestSections = <Widget>[];
              for (final id in _settings?.interests ?? const <String>[]) {
                if (byCategory[id] is! List) continue;
                final list = (byCategory[id] as List).whereType<Map>().where((e) => !shown.contains('${e['id']}')).toList();
                if (list.isEmpty) continue;
                interestSections
                  ..add(_SectionHeader(title: names[id] ?? id, count: list.length))
                  ..addAll([for (final e in list) _PaperRow(item: e)]);
              }

              return ListView(
                // Uçtan uca ekranda son satır gezinme çubuğunun altında kalmasın
                padding: EdgeInsets.fromLTRB(18, 0, 18, 32 + MediaQuery.paddingOf(context).bottom),
                children: [
                  _Masthead(evening: evening, day: day),
                  if (data == null) ...[
                    _Preparing(text: stale ? s.briefingPreparing : s.briefingEmpty),
                    if (stale)
                      Center(
                        child: TextButton(
                          onPressed: () => setState(() => _showPrevious = true),
                          child: Text(s.briefingShowPrevious),
                        ),
                      ),
                  ] else if (items.isEmpty)
                    _Preparing(text: s.briefingEmpty)
                  else ...[
                    _Lead(item: items.first, categoryName: names['${items.first['category_id']}']),
                    if (aiSummary.isNotEmpty) _DaySummary(text: aiSummary),
                    if (items.length >= 3) _Briefs(left: items[1], right: items[2], names: names),
                    if (items.length > 3) ...[
                      _SectionHeader(title: s.briefingHeadlines, count: items.length - 3),
                      for (final e in items.skip(3)) _PaperRow(item: e, kicker: names['${e['category_id']}']),
                    ] else if (items.length == 2)
                      _PaperRow(item: items[1], kicker: names['${items[1]['category_id']}']),
                  ],
                  if (data != null) ...interestSections,
                  if (_matches != null) _TodayMatches(stream: _matches!, repository: widget.repository),
                  const _Colophon(),
                ],
              );
            },
          );
        },
      ),
    );
  }

  static DateTime? _parseDay(Object? v) => v is String ? DateTime.tryParse(v) : null;
}

/// Türkçe büyük harf: i → İ, ı → I (Dart'ın toUpperCase'i "CUMARTESI", "EĞITIM" yazıyordu).
String _upper(BuildContext c, String text) =>
    S.of(c).isEnglish ? text.toUpperCase() : text.replaceAll('i', 'İ').replaceAll('ı', 'I').toUpperCase();

/// Gazete renkleri (uygulama temasından): çizgiler açıkta lacivert, koyuda açık lacivert.
Color _ink(BuildContext c) => Theme.of(c).brightness == Brightness.dark ? AppTheme.accentDark : AppTheme.navy;
Color _accent(BuildContext c) => Theme.of(c).colorScheme.primary;
Color _muted(BuildContext c) => Theme.of(c).colorScheme.onSurfaceVariant;
Color _hair(BuildContext c) => Theme.of(c).colorScheme.outlineVariant;

TextStyle? _kickerStyle(BuildContext c, {Color? color}) => Theme.of(c).textTheme.labelSmall?.copyWith(
  color: color ?? _accent(c),
  fontWeight: FontWeight.w800,
  letterSpacing: 1.6,
);

/// Çift çizgi (kalın + ince), gazete bölüm ayracı.
class _DoubleRule extends StatelessWidget {
  const _DoubleRule({this.top = true});
  final bool top;

  @override
  Widget build(BuildContext context) {
    final c = _ink(context);
    final thick = Container(height: 3, color: c);
    final thin = Container(height: 1, color: c);
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: top ? [thick, const SizedBox(height: 2), thin] : [thin, const SizedBox(height: 2), thick],
    );
  }
}

/// Künye: baskı adı · logo + "Haber Akışı" · çift çizgi arasında tarih ve okuma süresi.
class _Masthead extends StatelessWidget {
  const _Masthead({required this.evening, required this.day});
  final bool evening;
  final DateTime day;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final edition = s.briefingEdition(evening);
    final sep = edition.indexOf(' ');
    final small = theme.textTheme.labelSmall?.copyWith(letterSpacing: 1.4, fontWeight: FontWeight.w600);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text.rich(
          TextSpan(
            children: [
              TextSpan(
                text: _upper(context, edition.substring(0, sep)),
                style: TextStyle(color: _accent(context), fontWeight: FontWeight.w800),
              ),
              TextSpan(text: _upper(context, edition.substring(sep)), style: TextStyle(color: _muted(context))),
            ],
          ),
          style: small,
        ),
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.asset('assets/brand/logo.png', width: 48, height: 48),
              ),
              const SizedBox(width: 12),
              Flexible(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      s.appName,
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.w900,
                        letterSpacing: -0.8,
                        height: 1.05,
                      ),
                    ),
                    Text(s.briefingSubtitle, style: theme.textTheme.bodySmall?.copyWith(color: _muted(context))),
                  ],
                ),
              ),
            ],
          ),
        ),
        const _DoubleRule(),
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 5),
          child: Row(
            children: [
              Expanded(child: Text(_upper(context, s.longDate(day)), style: small)),
              Text(_upper(context, s.briefingReadTime), style: small),
            ],
          ),
        ),
        const _DoubleRule(top: false),
        const SizedBox(height: 14),
      ],
    );
  }
}

/// Haber görseli: yuvarlak köşeli; görsel yoksa yayıncının baş harfleri.
class _Picture extends StatelessWidget {
  const _Picture({required this.url, required this.source, this.aspect = 4 / 3, this.radius = 10});
  final String url;
  final String source;
  final double aspect;
  final double radius;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // "Motorsport.com" → "M", "Bloomberg HT" → "BH"
    final initials = source
        .replaceAll(RegExp(r'\.(com|net|org|tv|tr|info)\b.*', caseSensitive: false), '')
        .split(RegExp(r'\s+'))
        .where((w) => w.isNotEmpty)
        .take(2)
        .map((w) => w.characters.first.toUpperCase())
        .join();
    final placeholder = ColoredBox(
      color: theme.colorScheme.surfaceContainer,
      child: Center(
        child: Text(
          initials.isEmpty ? '•' : initials,
          style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900, color: _muted(context)),
        ),
      ),
    );
    return ClipRRect(
      borderRadius: BorderRadius.circular(radius),
      child: AspectRatio(
        aspectRatio: aspect,
        child: url.isEmpty
            ? placeholder
            : CachedNetworkImage(
                cacheManager: AppStorage.images,
                imageUrl: url,
                fit: BoxFit.cover,
                fadeInDuration: const Duration(milliseconds: 120),
                placeholder: (_, _) => placeholder,
                errorWidget: (_, _, _) => placeholder,
              ),
      ),
    );
  }
}

void _open(BuildContext context, Map item) {
  final url = '${item['url'] ?? ''}';
  if (url.isEmpty) return;
  Navigator.of(context).push(
    ArticleWebViewPage.route(url: url, sourceName: '${item['source'] ?? ''}', articleTitle: '${item['title'] ?? ''}'),
  );
}

int _sourceCount(Map item) =>
    (item['source_count'] as num?)?.toInt() ?? ((item['sources'] as List?) ?? const []).length;

bool _isBreaking(Map item) => item['category_id'] == 'son-dakika';

/// "KAYNAK  ·  N kaynakta"
class _Byline extends StatelessWidget {
  const _Byline({required this.item});
  final Map item;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final n = _sourceCount(item);
    final base = Theme.of(context).textTheme.labelSmall?.copyWith(letterSpacing: 1.1, color: _muted(context));
    return Text.rich(
      TextSpan(
        children: [
          TextSpan(
            text: _upper(context, '${item['source'] ?? ''}'),
            style: TextStyle(fontWeight: FontWeight.w700, color: Theme.of(context).colorScheme.onSurface),
          ),
          if (n >= 2) TextSpan(text: '  ·  ${s.inSources(n)}', style: TextStyle(color: _accent(context))),
        ],
      ),
      style: base,
    );
  }
}

/// Üst etiket: kategori adı (lacivert) ya da "● SON DAKİKA" (kırmızı — kırmızı yalnızca burada).
class _Kicker extends StatelessWidget {
  const _Kicker({required this.item, this.text});
  final Map item;
  final String? text;

  @override
  Widget build(BuildContext context) {
    if (_isBreaking(item)) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const _Dot(),
          Text(_upper(context, S.of(context).briefingBreaking), style: _kickerStyle(context, color: AppTheme.breaking)),
        ],
      );
    }
    final t = text;
    if (t == null || t.isEmpty) return const SizedBox.shrink();
    return Text(_upper(context, t), style: _kickerStyle(context));
  }
}

/// Son dakika noktası (yazı tipinde "●" olmayabilir; çizilir).
class _Dot extends StatelessWidget {
  const _Dot();

  @override
  Widget build(BuildContext context) => Container(
    width: 7,
    height: 7,
    margin: const EdgeInsets.only(right: 6),
    decoration: const BoxDecoration(color: AppTheme.breaking, shape: BoxShape.circle),
  );
}

/// Manşet: etiket · büyük başlık · spot · görsel · künye.
class _Lead extends StatelessWidget {
  const _Lead({required this.item, this.categoryName});
  final Map item;
  final String? categoryName;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final summary = '${item['summary'] ?? ''}'.trim();
    final image = '${item['image'] ?? ''}';
    return InkWell(
      onTap: () => _open(context, item),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text.rich(
            TextSpan(
              children: [
                if (_isBreaking(item)) ...[
                  const WidgetSpan(alignment: PlaceholderAlignment.middle, child: _Dot()),
                  TextSpan(text: _upper(context, '${s.briefingBreaking}  ·  '), style: const TextStyle(color: AppTheme.breaking)),
                ]
                else if (categoryName != null)
                  TextSpan(text: _upper(context, '$categoryName  ·  ')),
                TextSpan(text: _upper(context, s.briefingLead)),
              ],
            ),
            style: _kickerStyle(context),
          ),
          const SizedBox(height: 4),
          Text(
            '${item['title'] ?? ''}',
            style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w800, height: 1.18, letterSpacing: -0.4),
          ),
          if (summary.isNotEmpty) ...[
            const SizedBox(height: 8),
            Text(
              summary,
              style: theme.textTheme.bodyLarge?.copyWith(
                height: 1.4,
                color: theme.colorScheme.onSurface.withValues(alpha: 0.85),
              ),
            ),
          ],
          if (image.isNotEmpty) ...[
            const SizedBox(height: 12),
            _Picture(url: image, source: '${item['source'] ?? ''}', aspect: 16 / 9, radius: 12),
          ],
          const SizedBox(height: 8),
          _Byline(item: item),
        ],
      ),
    );
  }
}

/// Günün özeti (yapay zekâ): büyük ilk harfli başyazı.
class _DaySummary extends StatelessWidget {
  const _DaySummary({required this.text});
  final String text;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final first = text.characters.first;
    final rest = text.characters.skip(1).toString();
    return Container(
      margin: const EdgeInsets.only(top: 16),
      padding: const EdgeInsets.symmetric(vertical: 10),
      decoration: BoxDecoration(
        border: Border.symmetric(horizontal: BorderSide(color: _ink(context))),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.auto_awesome_rounded, size: 14, color: _accent(context)),
              const SizedBox(width: 6),
              Expanded(
                child: Text(
                  _upper(context, s.briefingDaySummary),
                  style: _kickerStyle(context, color: theme.colorScheme.onSurface),
                ),
              ),
              Text(
                _upper(context, s.briefingAiNote),
                style: theme.textTheme.labelSmall?.copyWith(fontSize: 9.5, letterSpacing: 1, color: _muted(context)),
              ),
            ],
          ),
          const SizedBox(height: 6),
          Text.rich(
            TextSpan(
              children: [
                TextSpan(
                  text: first,
                  style: theme.textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.w900,
                    color: _accent(context),
                    height: 1,
                  ),
                ),
                TextSpan(text: rest),
              ],
            ),
            style: theme.textTheme.bodyLarge?.copyWith(height: 1.5),
          ),
        ],
      ),
    );
  }
}

/// Kısa kısa: iki haber, aralarında sütun çizgisi.
class _Briefs extends StatelessWidget {
  const _Briefs({required this.left, required this.right, required this.names});
  final Map left;
  final Map right;
  final Map<String, String> names;

  @override
  Widget build(BuildContext context) {
    Widget col(Map e) => InkWell(
      onTap: () => _open(context, e),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _Kicker(item: e, text: names['${e['category_id']}']),
          const SizedBox(height: 3),
          Text(
            '${e['title'] ?? ''}',
            maxLines: 4,
            overflow: TextOverflow.ellipsis,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700, height: 1.28),
          ),
          const SizedBox(height: 4),
          _Byline(item: e),
        ],
      ),
    );
    return Padding(
      padding: const EdgeInsets.only(top: 14),
      child: IntrinsicHeight(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Expanded(child: col(left)),
            VerticalDivider(width: 21, thickness: 1, color: _hair(context)),
            Expanded(child: col(right)),
          ],
        ),
      ),
    );
  }
}

/// Bölüm başlığı: çift çizgi + BÜYÜK HARF ad + haber sayısı.
class _SectionHeader extends StatelessWidget {
  const _SectionHeader({required this.title, this.count});
  final String title;
  final int? count;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(top: 20, bottom: 2),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const _DoubleRule(),
          const SizedBox(height: 6),
          Row(
            children: [
              Expanded(
                child: Text(
                  _upper(context, title),
                  style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w800, letterSpacing: 2.2),
                ),
              ),
              if (count != null)
                Text(s.newsCount(count!), style: theme.textTheme.bodySmall?.copyWith(color: _muted(context))),
            ],
          ),
        ],
      ),
    );
  }
}

/// Haber satırı: solda etiket + başlık + kaynak, SAĞDA görsel.
class _PaperRow extends StatelessWidget {
  const _PaperRow({required this.item, this.kicker});
  final Map item;
  final String? kicker;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final hasKicker = (kicker != null && kicker!.isNotEmpty) || _isBreaking(item);
    return InkWell(
      onTap: () => _open(context, item),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(border: Border(bottom: BorderSide(color: _hair(context)))),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (hasKicker) ...[_Kicker(item: item, text: kicker), const SizedBox(height: 3)],
                  Text(
                    '${item['title'] ?? ''}',
                    style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700, height: 1.28),
                  ),
                  const SizedBox(height: 6),
                  _Byline(item: item),
                ],
              ),
            ),
            const SizedBox(width: 12),
            SizedBox(width: 112, child: _Picture(url: '${item['image'] ?? ''}', source: '${item['source'] ?? ''}')),
          ],
        ),
      ),
    );
  }
}

class _Preparing extends StatelessWidget {
  const _Preparing({required this.text});
  final String text;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 32),
    child: Column(
      children: [
        Icon(Icons.hourglass_top_rounded, color: _muted(context)),
        const SizedBox(height: 10),
        Text(text, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodyLarge),
      ],
    ),
  );
}

class _Colophon extends StatelessWidget {
  const _Colophon();

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(top: 24),
    child: Center(
      child: Opacity(
        opacity: 0.5,
        child: ClipRRect(
          borderRadius: BorderRadius.circular(7),
          child: Image.asset('assets/brand/logo.png', width: 26, height: 26),
        ),
      ),
    ),
  );
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
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            _SectionHeader(title: s.briefingMatches, count: list.isEmpty ? null : list.length),
            if (list.isEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(s.briefingNoMatches, style: theme.textTheme.bodyMedium?.copyWith(color: _muted(context))),
              )
            else
              for (final m in list)
                ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.sports_soccer),
                  title: Text('${m.home.name} – ${m.away.name}', style: const TextStyle(fontWeight: FontWeight.w700)),
                  trailing: Text(
                    m.notStarted
                        ? '${m.kickoff.toLocal().hour.toString().padLeft(2, '0')}:${m.kickoff.toLocal().minute.toString().padLeft(2, '0')}'
                        : '${m.homeGoals}-${m.awayGoals}${m.isLive ? " · ${m.elapsed ?? ''}'" : ''}',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w800,
                      color: m.isLive ? AppTheme.breaking : null,
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

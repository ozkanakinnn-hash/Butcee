import 'dart:ui' show Color;

import 'package:cloud_firestore/cloud_firestore.dart';

/// "#A90432" → Color.
Color? parseHexColor(Object? v) {
  if (v is! String) return null;
  final h = v.replaceFirst('#', '');
  final n = int.tryParse(h.length == 6 ? 'FF$h' : h, radix: 16);
  return n == null ? null : Color(n);
}

List<Color> _colors(Object? v) => (v is List ? v : const []).map(parseHexColor).whereType<Color>().toList();

/// `football_teams/{kimlik}` — kimlik kendi kısa adımızdır (`galatasaray`), API sayısı değil.
class TeamRecord {
  TeamRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap)
    : id = snap.id,
      name = snap.data()?['name'] as String? ?? snap.id,
      short = snap.data()?['short'] as String? ?? '',
      colors = _colors(snap.data()?['colors']),
      logo = snap.data()?['logo'] as String? ?? '',
      active = snap.data()?['active'] as bool? ?? true;

  final String id;
  final String name;
  final String short;
  final List<Color> colors;
  final String logo;

  /// Bu sezon ligde mi (küme düşen takımlar false).
  final bool active;

  /// Gösterilen kısaltma (GS, FB, BJK…); yoksa adın ilk 3 harfi.
  String get code => teamCode(short, name);
}

/// Kısaltma; tanımlı değilse adın ilk 3 harfi (Türkçe büyük harf).
String teamCode(String short, String name) {
  if (short.isNotEmpty) return short;
  final n = name.replaceAll('i', 'İ').replaceAll('ı', 'I').toUpperCase().replaceAll(RegExp(r'[^A-ZÇĞİÖŞÜ0-9]'), '');
  return n.length <= 3 ? n : n.substring(0, 3);
}

class MatchTeam {
  MatchTeam.fromMap(Map<String, dynamic>? m)
    : id = '${m?['id'] ?? ''}',
      name = m?['name'] as String? ?? '',
      short = m?['short'] as String? ?? '',
      colors = _colors(m?['colors']),
      logo = m?['logo'] as String? ?? '';

  final String id;
  final String name;
  final String short;
  final List<Color> colors;
  final String logo;

  String get code => teamCode(short, name);
}

class GoalEvent {
  GoalEvent.fromMap(Map<String, dynamic> m)
    : minute = (m['minute'] as num?)?.toInt() ?? 0,
      extra = (m['extra'] as num?)?.toInt() ?? 0,
      teamId = '${m['team_id'] ?? ''}',
      player = m['player'] as String? ?? '',
      detail = m['detail'] as String? ?? '';

  final int minute;
  final int extra;
  final String teamId;
  final String player;
  final String detail;

  String get minuteLabel => extra > 0 ? "$minute+$extra'" : "$minute'";
}

/// `matches/{fixtureId}` — canlı skor; bot maç süresince günceller, uygulama canlı dinler.
class MatchRecord {
  MatchRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap) : this.fromMap(snap.data() ?? const {});

  MatchRecord.fromMap(Map<String, dynamic> d)
    : fixtureId = (d['fixture_id'] as num?)?.toInt() ?? 0,
      kickoff = (d['kickoff'] as Timestamp?)?.toDate() ?? DateTime.fromMillisecondsSinceEpoch(0),
      status = (d['status'] as Map?)?['short'] as String? ?? 'NS',
      elapsed = ((d['status'] as Map?)?['elapsed'] as num?)?.toInt(),
      home = MatchTeam.fromMap((d['home'] as Map?)?.cast<String, dynamic>()),
      away = MatchTeam.fromMap((d['away'] as Map?)?.cast<String, dynamic>()),
      homeGoals = ((d['goals'] as Map?)?['home'] as num?)?.toInt() ?? 0,
      awayGoals = ((d['goals'] as Map?)?['away'] as num?)?.toInt() ?? 0,
      round = (d['league'] as Map?)?['round'] as String? ?? '',
      isLive = d['is_live'] as bool? ?? false,
      isFinished = d['is_finished'] as bool? ?? false,
      goals = ((d['goal_events'] as List?) ?? const [])
          .map((e) => GoalEvent.fromMap(Map<String, dynamic>.from(e as Map)))
          .toList();

  final int fixtureId;
  final DateTime kickoff;
  final String status;
  final int? elapsed;
  final MatchTeam home;
  final MatchTeam away;
  final int homeGoals;
  final int awayGoals;
  final String round;
  final bool isLive;
  final bool isFinished;
  final List<GoalEvent> goals;

  bool get notStarted => !isLive && !isFinished;
}

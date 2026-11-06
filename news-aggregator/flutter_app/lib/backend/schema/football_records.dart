import 'package:cloud_firestore/cloud_firestore.dart';

/// `football_teams/{teamId}` — bot haftada bir API-Football'dan yeniler.
class TeamRecord {
  TeamRecord.fromSnapshot(DocumentSnapshot<Map<String, dynamic>> snap)
    : id = int.tryParse(snap.id) ?? 0,
      name = snap.data()?['name'] as String? ?? '',
      logo = snap.data()?['logo'] as String? ?? '';

  final int id;
  final String name;
  final String logo;
}

class MatchTeam {
  MatchTeam.fromMap(Map<String, dynamic>? m)
    : id = (m?['id'] as num?)?.toInt() ?? 0,
      name = m?['name'] as String? ?? '',
      logo = m?['logo'] as String? ?? '';

  final int id;
  final String name;
  final String logo;
}

class GoalEvent {
  GoalEvent.fromMap(Map<String, dynamic> m)
    : minute = (m['minute'] as num?)?.toInt() ?? 0,
      extra = (m['extra'] as num?)?.toInt() ?? 0,
      teamId = (m['team_id'] as num?)?.toInt() ?? 0,
      player = m['player'] as String? ?? '',
      detail = m['detail'] as String? ?? '';

  final int minute;
  final int extra;
  final int teamId;
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

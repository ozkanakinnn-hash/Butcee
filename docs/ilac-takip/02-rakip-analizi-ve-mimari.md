# Mobil İlaç Takip Uygulaması — Rakip Analizi, Stratejik Kararlar ve Alarm Mimarisi

> **Girdi:** Rakip uygulamaların mağaza yorumlarında öne çıkan 5 şikâyet (barkod uyumsuzluğu, arka planda çalmayan alarm, kaybolan ertelemeler, karmaşık arayüz, esnek olmayan dozaj periyotları)
> **Çıktı:** (1) Alarm/erteleme servis katmanı mimarisi ve örnek kod, (2) "Basit Mod" UI/UX akışı
> **Tarih:** 2026-11-04

---

## 1. Şikâyet → Kök neden → Çözüm matrisi

| # | Şikâyet | Teknik kök neden | Bizim çözümümüz | Bölüm |
|---|---|---|---|---|
| 1 | "İlaç bulunamadı" | DataMatrix okunmuyor, GTIN-14→13 normalize edilmiyor, yerel katalog yok | GS1 ayrıştırıcı + yerel TİTCK tabanlı katalog + manuel/OCR yedek | `01` §1 |
| 2 | Alarm geç çalıyor / hiç çalmıyor | `AlarmManager.set()` / inexact alarmlar Doze'da ertelenir; Android 12+ exact alarm izni; Android 14'te izin varsayılan kapalı; OEM "uygulama öldürücüleri"; yeniden başlatmada alarmlar silinir; iOS 64 bekleyen bildirim sınırı | `setAlarmClock` modu, izin sağlık kontrolü, BOOT receiver, kayan pencere planlama, OEM rehberi | §3, §4 |
| 3 | Ertelenen ilaç kayboluyor | Erteleme yalnızca bir bildirim olarak yaşıyor, veri modelinde durumu yok | Ertelemenin **birinci sınıf durum** (`snoozed`) olduğu doz durum makinesi + "Bekleyenler" paneli | §3.4, §3.6 |
| 4 | Karmaşık arayüz | Her şey tek arayüzde; küçük dokunma alanları | Ayrı "Basit Mod" görünüm katmanı, kilitlenebilir ayarlar | §5 |
| 5 | "8 saatte bir", "gün aşırı" girilemiyor | Plan = "günde şu saatler" varsayımı | Kural tabanlı `DoseRule` modeli (aralık, N günde bir, haftanın günleri, döngüsel, gerektiğinde, aç/tok ofseti) | §3.2 |

> **Önemli düzeltme:** Pil sorunu için `flutter_background_service` **önerilmez**. Sürekli çalışan bir ön plan servisi pili tüketir, Android 14+'ta servis türü beyanı ve Play Console onayı ister ve alarmı daha güvenilir yapmaz. Doğru araç, sistemin alarm saatinde uygulamayı kendisinin uyandırdığı **`AlarmManager.setAlarmClock()`**'tur (`flutter_local_notifications` içinde `AndroidScheduleMode.alarmClock`). Pil muafiyeti yalnızca agresif OEM'lere karşı **ek sigortadır**.

---

## 2. Mimari genel bakış

```
┌──────────────────────────── Sunum (Flutter UI) ─────────────────────────────┐
│ HomeScreen · PendingPanel · AlarmScreen · SeniorHome · OnboardingReliability │
└───────────────────────────────┬─────────────────────────────────────────────┘
                                │ Riverpod
┌──────────────────────────── Alan (saf Dart) ────────────────────────────────┐
│ DoseRule.occurrences()  · DoseStateMachine · AdherenceCalculator            │
└───────────────────────────────┬─────────────────────────────────────────────┘
┌──────────────────────────── Uygulama servisleri ────────────────────────────┐
│ DoseActionService (take/snooze/skip)  ·  AlarmPlanner (kayan pencere)        │
│ ReliabilityService (izin & pil sağlık kontrolü)                              │
└──────┬───────────────────────┬────────────────────────────┬─────────────────┘
       │                       │                            │
┌──────▼──────┐   ┌────────────▼────────────┐   ┌───────────▼────────────┐
│ drift (yerel│   │ AlarmService            │   │ SyncService            │
│ doğruluk    │   │ flutter_local_notif.    │   │ Firestore ⇄ drift      │
│ kaynağı)    │   │ + Android native kanal  │   │ (outbox kuyruğu)       │
└─────────────┘   └─────────────────────────┘   └────────────────────────┘
```

**Altın kurallar**
1. Alarm kararı **yalnızca yerel veritabanından** verilir. Ağ yoksa da çalar.
2. Her doz örneği (`DoseLog`) deterministik kimliklidir: `scheduleId_yyyyMMddTHHmm`.
3. Bir bildirimin kaybolması veriyi kaybettirmez: durum veritabanındadır; ana ekran veritabanından çizilir.
4. Her kullanıcı eylemi (aldım/ertele/atla) → (a) yerel DB güncelle, (b) ilgili bildirimleri iptal/yeniden kur, (c) outbox'a Firestore senkronu ekle.

**Paketler**

| Amaç | Paket |
|---|---|
| Bildirim/alarm | `flutter_local_notifications` (^19), `timezone`, `flutter_timezone` |
| Yerel DB | `drift`, `sqlite3_flutter_libs`, `path_provider` |
| Periyodik bakım (pencereyi uzat, stok kontrolü) | `workmanager` |
| İzinler | `permission_handler` |
| Pil/OEM ayarları | Kendi `MethodChannel`'ımız (aşağıda) — alternatif: `disable_battery_optimization` |
| Basit Mod sesli okuma | `flutter_tts` |
| Ekran parlaklığı / uyanık tutma | `screen_brightness`, `wakelock_plus` |

---

## 3. Alarm ve erteleme servis katmanı (örnek kod)

### 3.1 Doz durum makinesi

```
                  ┌────────── snooze(10dk) ──────────┐
                  ▼                                   │
 [pending] ──zamanı geldi──▶ [due/ringing] ──────────┤
    │                          │   │                  │
    │                          │   └── skip(neden) ──▶ [skipped] (son)
    │                          └────── take() ──────▶ [taken]   (son)
    │                                                     ▲
 [snoozed(until)] ──until geldi──▶ [due/ringing] ─────────┘
    │
    └── graceUntil geçti & işlem yok ──▶ [missed]  (sunucu veya cihaz)
           └── kullanıcı sonradan "aslında aldım" ──▶ [taken] (late=true)
```

- `due/ringing` veritabanında ayrı bir durum değildir; `status == pending && scheduledTime <= now` olarak **türetilir**.
- `snoozed` durumunda `snoozedUntil` ve `snoozeCount` saklanır; maksimum erteleme sayısı (varsayılan 3) aşılınca "Ertele" düğmesi kalkar.
- `graceUntil` = `scheduledTime + graceMinutes` (varsayılan 60 dk; kritik ilaçta 30 dk). Ertelemede `graceUntil = max(graceUntil, snoozedUntil + 15dk)`.

### 3.2 Esnek dozaj kuralları (`DoseRule`)

```dart
enum MealRelation { none, beforeMeal, afterMeal, withMeal, emptyStomach }

sealed class DoseRule {
  const DoseRule({required this.startDate, this.endDate, this.quantity = 1, this.meal = MealRelation.none});
  final DateTime startDate;         // yerel tarih (saat yok)
  final DateTime? endDate;
  final double quantity;            // 1 tablet, 0.5 tablet, 5 ml…
  final MealRelation meal;

  /// [from, to) aralığındaki yerel doz zamanları.
  Iterable<DateTime> occurrences(DateTime from, DateTime to);
  double averageDailyQuantity();
}

/// "Günde 3 kez: 08:00, 14:00, 20:00" — isteğe bağlı haftanın günleri ve "N günde bir" ile
class FixedTimesRule extends DoseRule {
  const FixedTimesRule({required super.startDate, super.endDate, super.quantity, super.meal,
      required this.times, this.weekdays, this.everyNDays = 1});
  final List<({int h, int m})> times;
  final Set<int>? weekdays;          // DateTime.monday..sunday; null = her gün
  final int everyNDays;              // 2 = gün aşırı

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) sync* {
    var day = DateTime(from.year, from.month, from.day);
    while (day.isBefore(to)) {
      final dayIndex = _daysBetween(startDate, day);
      final active = dayIndex >= 0 &&
          (endDate == null || !day.isAfter(endDate!)) &&
          dayIndex % everyNDays == 0 &&
          (weekdays == null || weekdays!.contains(day.weekday));
      if (active) {
        for (final t in times) {
          final dt = DateTime(day.year, day.month, day.day, t.h, t.m);
          if (!dt.isBefore(from) && dt.isBefore(to)) yield dt;
        }
      }
      day = DateTime(day.year, day.month, day.day + 1); // DST güvenli gün artışı
    }
  }

  @override
  double averageDailyQuantity() =>
      quantity * times.length / everyNDays * ((weekdays?.length ?? 7) / 7);
}

/// "8 saatte bir, ilk doz 07:00" — gece yarısını geçen döngüler doğru hesaplanır
class IntervalRule extends DoseRule {
  const IntervalRule({required super.startDate, super.endDate, super.quantity, super.meal,
      required this.firstDose, required this.intervalHours});
  final ({int h, int m}) firstDose;
  final int intervalHours;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) sync* {
    var t = DateTime(startDate.year, startDate.month, startDate.day, firstDose.h, firstDose.m);
    // Başlangıçtan 'from'a hızlı atla
    if (t.isBefore(from)) {
      final steps = from.difference(t).inMinutes ~/ (intervalHours * 60);
      t = t.add(Duration(hours: steps * intervalHours));
    }
    while (t.isBefore(to)) {
      if (endDate != null && t.isAfter(endDate!.add(const Duration(days: 1)))) return;
      if (!t.isBefore(from)) yield t;
      t = t.add(Duration(hours: intervalHours)); // gerçek süre (8 saat = 8 saat; DST'de saat kayar, doğru davranış)
    }
  }

  @override
  double averageDailyQuantity() => quantity * 24 / intervalHours;
}

/// "21 gün kullan, 7 gün ara ver" (ör. doğum kontrol, bazı onkoloji ilaçları)
class CyclicRule extends DoseRule {
  const CyclicRule({required super.startDate, super.endDate, super.quantity, super.meal,
      required this.inner, required this.onDays, required this.offDays});
  final FixedTimesRule inner;
  final int onDays, offDays;

  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) => inner
      .occurrences(from, to)
      .where((dt) => _daysBetween(startDate, dt) % (onDays + offDays) < onDays);

  @override
  double averageDailyQuantity() => inner.averageDailyQuantity() * onDays / (onDays + offDays);
}

/// "Ağrı olduğunda, günde en fazla 3" — alarm kurulmaz, günlük limit uyarısı verilir
class AsNeededRule extends DoseRule {
  const AsNeededRule({required super.startDate, super.endDate, super.quantity,
      required this.maxPerDay, required this.minIntervalHours});
  final int maxPerDay;
  final int minIntervalHours;
  @override
  Iterable<DateTime> occurrences(DateTime from, DateTime to) => const [];
  @override
  double averageDailyQuantity() => 0;
}

int _daysBetween(DateTime a, DateTime b) =>
    DateTime.utc(b.year, b.month, b.day).difference(DateTime.utc(a.year, a.month, a.day)).inDays;
```

**Aç/tok karnına bağlantısı:** Kullanıcı öğün saatlerini bir kez girer (kahvaltı 08:00, öğle 13:00, akşam 19:00). "Kahvaltıdan 30 dk önce" seçimi `FixedTimesRule.times`'a 07:30 olarak çözülür ve `meal = beforeMeal` alarm ekranında rozet olarak gösterilir. Öğün saati değişince ilgili tüm planlar yeniden hesaplanır.

Firestore'daki karşılığı (`users/{uid}/schedules/{scheduleId}`):

```json
{
  "scheduleId": "sch_111",
  "medicineId": "med_abc123",
  "rule": { "type": "interval", "firstDose": "07:00", "intervalHours": 8 },
  "quantity": 1,
  "meal": "afterMeal",
  "mealOffsetMinutes": 0,
  "startDate": "2026-11-04",
  "endDate": null,
  "isCritical": true,
  "persistentAlarm": { "enabled": true, "repeatEveryMinutes": 3, "maxRepeats": 10 },
  "graceMinutes": 30,
  "maxSnoozes": 3,
  "timeZone": "Europe/Istanbul",
  "paused": false,
  "updatedAt": "2026-11-04T10:00:00Z"
}
```

Diğer `rule.type` örnekleri: `{ "type": "fixedTimes", "times": ["08:00","20:00"], "everyNDays": 2 }`, `{ "type": "fixedTimes", "times": ["09:00"], "weekdays": [1,3,5] }`, `{ "type": "cyclic", "onDays": 21, "offDays": 7, "inner": {…} }`, `{ "type": "asNeeded", "maxPerDay": 3, "minIntervalHours": 6 }`.

### 3.3 `AlarmService` — güvenilir zamanlama + ısrarcı alarm

```dart
import 'dart:typed_data';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_timezone/flutter_timezone.dart';
import 'package:timezone/data/latest_all.dart' as tzdata;
import 'package:timezone/timezone.dart' as tz;

class AlarmIds {
  // Her doz için 32 bitlik taban kimlik: yerel DB'deki otomatik artan int id.
  // Ana alarm: base*16, ısrarcı tekrarlar: base*16 + 1..15
  static int main(int doseIntId) => doseIntId * 16;
  static int repeat(int doseIntId, int k) => doseIntId * 16 + k; // 1 ≤ k ≤ 15
}

class AlarmService {
  AlarmService(this._plugin);
  final FlutterLocalNotificationsPlugin _plugin;

  static const _flagInsistent = 4; // Android Notification.FLAG_INSISTENT: kullanıcı dokunana kadar ses döngüde

  Future<void> init({
    required void Function(NotificationResponse) onForegroundAction,
    required void Function(NotificationResponse) onBackgroundAction, // top-level + @pragma('vm:entry-point')
  }) async {
    tzdata.initializeTimeZones();
    final tzInfo = await FlutterTimezone.getLocalTimezone(); // flutter_timezone ^4 → TimezoneInfo
    tz.setLocalLocation(tz.getLocation(tzInfo.identifier));

    await _plugin.initialize(
      InitializationSettings(
        android: const AndroidInitializationSettings('@drawable/ic_stat_pill'),
        iOS: DarwinInitializationSettings(
          requestAlertPermission: false, requestSoundPermission: false, requestBadgePermission: false,
          notificationCategories: [
            DarwinNotificationCategory('dose', actions: [
              DarwinNotificationAction.plain('take', 'Aldım ✓'),
              DarwinNotificationAction.plain('snooze', '10 dk ertele'),
              DarwinNotificationAction.plain('skip', 'Atla', options: {DarwinNotificationActionOption.destructive}),
            ]),
          ],
        ),
      ),
      onDidReceiveNotificationResponse: onForegroundAction,
      onDidReceiveBackgroundNotificationResponse: onBackgroundAction,
    );

    final android = _plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    await android?.createNotificationChannel(const AndroidNotificationChannel(
      'dose_critical_v1', 'Kritik ilaç alarmları',
      description: 'Siz "Aldım" diyene kadar çalan alarmlar',
      importance: Importance.max,
      sound: RawResourceAndroidNotificationSound('alarm_pill'),
      audioAttributesUsage: AudioAttributesUsage.alarm, // sessiz modda bile alarm ses seviyesinden çalar
      enableVibration: true,
    ));
    await android?.createNotificationChannel(const AndroidNotificationChannel(
      'dose_normal_v1', 'İlaç hatırlatmaları',
      importance: Importance.high,
    ));
  }

  /// Bir doz için ana alarmı (+ ısrarcı tekrarları) kurar.
  Future<void> scheduleDose(PlannedDose d) async {
    final when = tz.TZDateTime.from(d.fireAt, tz.local);
    if (when.isBefore(tz.TZDateTime.now(tz.local))) return;

    await _plugin.zonedSchedule(
      AlarmIds.main(d.intId), d.title, d.body, when, _details(d),
      androidScheduleMode: d.isCritical ? AndroidScheduleMode.alarmClock : AndroidScheduleMode.exactAllowWhileIdle,
      payload: d.logId,
    );

    // Israrcı mod: "Aldım" denene kadar her N dk yeniden çal (önceden zincir olarak kurulur)
    if (d.persistent) {
      for (var k = 1; k <= d.maxRepeats && k <= 15; k++) {
        await _plugin.zonedSchedule(
          AlarmIds.repeat(d.intId, k),
          '⏰ ${d.title}', d.repeatBody(k),
          when.add(Duration(minutes: d.repeatEveryMinutes * k)),
          _details(d),
          androidScheduleMode: AndroidScheduleMode.alarmClock,
          payload: d.logId,
        );
      }
    }
  }

  Future<void> cancelDose(int doseIntId) async {
    for (var k = 0; k <= 15; k++) {
      await _plugin.cancel(doseIntId * 16 + k);
    }
  }

  NotificationDetails _details(PlannedDose d) => NotificationDetails(
        android: AndroidNotificationDetails(
          d.isCritical ? 'dose_critical_v1' : 'dose_normal_v1',
          d.isCritical ? 'Kritik ilaç alarmları' : 'İlaç hatırlatmaları',
          importance: d.isCritical ? Importance.max : Importance.high,
          priority: Priority.max,
          category: AndroidNotificationCategory.alarm,
          fullScreenIntent: d.isCritical,      // kilit ekranında AlarmScreen açılır
          ongoing: d.isCritical,               // kaydırarak kapatılamaz
          autoCancel: false,
          visibility: d.hideNameOnLockScreen ? NotificationVisibility.private : NotificationVisibility.public,
          additionalFlags: d.isCritical ? Int32List.fromList([_flagInsistent]) : null,
          styleInformation: BigTextStyleInformation(d.body), // "🔴 TOK KARNINA · 1 tablet"
          actions: [
            const AndroidNotificationAction('take', 'Aldım ✓', cancelNotification: true),
            if (d.canSnooze) const AndroidNotificationAction('snooze', '10 dk ertele', cancelNotification: true),
            const AndroidNotificationAction('skip', 'Atla', cancelNotification: true),
          ],
        ),
        iOS: DarwinNotificationDetails(
          categoryIdentifier: 'dose',
          sound: 'alarm_pill.caf',            // ≤30 sn, uygulama paketinde
          interruptionLevel: d.isCritical ? InterruptionLevel.timeSensitive : InterruptionLevel.active,
          threadIdentifier: 'dose',
        ),
      );
}
```

`PlannedDose` = `DoseLog` + medicine bilgisinden türetilen görünüm modeli (`intId`, `logId`, `fireAt` = `snoozedUntil ?? scheduledTime`, `title`, `body`, `isCritical`, `persistent`, `repeatEveryMinutes`, `maxRepeats`, `canSnooze`).

**Neden zincir + `FLAG_INSISTENT` birlikte?**
- `FLAG_INSISTENT` sesi kullanıcı bildirimle etkileşene kadar **sürekli** çalar (kritik ilaç için en güçlü sinyal).
- Kullanıcı bildirimi kaydırıp kapatırsa (`ongoing` olmayan kanallarda) veya ses kapatılırsa, **3 dk sonra zincirdeki bir sonraki alarm** yeniden gelir. "Aldım" denince `cancelDose` zincirin tamamını siler.

### 3.4 Bildirim aksiyonları (arka plan izolatı)

```dart
// lib/alarm/notification_entry.dart
@pragma('vm:entry-point')
void onBackgroundNotificationAction(NotificationResponse r) async {
  // Uygulama kapalıyken ayrı bir izolatta çalışır: Firebase'e değil yerel DB'ye yazarız.
  DartPluginRegistrant.ensureInitialized();
  final container = await bootstrapHeadless(); // drift + AlarmService (UI'siz)
  await container.read(doseActionServiceProvider).handleNotificationAction(r.actionId, r.payload);
}

void onForegroundNotificationAction(NotificationResponse r) {
  if (r.actionId == null) {
    router.push('/alarm/${r.payload}'); // bildirime dokunuldu → tam ekran alarm
  } else {
    globalContainer.read(doseActionServiceProvider).handleNotificationAction(r.actionId, r.payload);
  }
}
```

### 3.5 `DoseActionService` — tek doğruluk noktası

```dart
class DoseActionService {
  DoseActionService(this._db, this._alarms, this._outbox, this._clock);
  final AppDatabase _db;
  final AlarmService _alarms;
  final SyncOutbox _outbox;
  final Clock _clock;

  Future<void> handleNotificationAction(String? actionId, String? logId) async {
    if (logId == null) return;
    switch (actionId) {
      case 'take':   await take(logId);
      case 'snooze': await snooze(logId, const Duration(minutes: 10));
      case 'skip':   await skip(logId, reason: SkipReason.unspecified);
    }
  }

  Future<void> take(String logId, {DateTime? at, String? note}) async {
    final now = at ?? _clock.now();
    await _db.transaction(() async {
      final log = await _db.doseLogs.byId(logId);
      if (log.status == DoseStatus.taken) return;      // çift dokunma koruması
      await _db.doseLogs.update(log.copyWith(
        status: DoseStatus.taken, actionTime: now, snoozedUntil: null, note: note,
      ));
      await _db.medicines.decrementStock(log.medicineId, log.quantity); // stok otomatik düşer
      await _outbox.enqueueDoseLog(logId);
    });
    await _alarms.cancelDose((await _db.doseLogs.byId(logId)).intId);
    await _checkLowStock(logId);
  }

  Future<void> snooze(String logId, Duration by) async {
    final log = await _db.doseLogs.byId(logId);
    final schedule = await _db.schedules.byId(log.scheduleId);
    if (log.snoozeCount >= schedule.maxSnoozes) {
      // Erteleme hakkı bitti: alarm çalmaya devam eder, panelde kırmızı kalır
      return;
    }
    final until = _clock.now().add(by);
    final updated = log.copyWith(
      status: DoseStatus.snoozed,
      snoozedUntil: until,
      snoozeCount: log.snoozeCount + 1,
      graceUntil: _max(log.graceUntil, until.add(const Duration(minutes: 15))),
    );
    await _db.doseLogs.update(updated);
    await _outbox.enqueueDoseLog(logId);              // bakım veren "ertelendi" görür, sunucu missed'i geciktirir
    await _alarms.cancelDose(log.intId);
    await _alarms.scheduleDose(await _db.plannedDose(updated)); // fireAt = snoozedUntil
  }

  Future<void> skip(String logId, {required SkipReason reason}) async {
    final log = await _db.doseLogs.byId(logId);
    await _db.doseLogs.update(log.copyWith(
      status: DoseStatus.skipped, actionTime: _clock.now(), skipReason: reason, snoozedUntil: null,
    ));
    await _outbox.enqueueDoseLog(logId);
    await _alarms.cancelDose(log.intId);
  }

  /// Kullanıcı kaçırılan bir dozu sonradan "aslında aldım" diye işaretler.
  Future<void> markTakenLate(String logId, DateTime actualTime) => take(logId, at: actualTime);

  Future<void> _checkLowStock(String logId) async { /* criticalStockLevel → yerel bildirim */ }
  DateTime _max(DateTime a, DateTime b) => a.isAfter(b) ? a : b;
}
```

### 3.6 Bekleyen / Kaçırılanlar paneli

Panel **bildirimlerden değil veritabanından** beslenir — bu yüzden bir erteleme asla "kaybolmaz".

```dart
// drift sorgusu: şu an aksiyon bekleyen tüm dozlar
Stream<List<PendingDose>> watchPending(DateTime now) => (select(doseLogs)
      ..where((l) =>
          // zamanı gelmiş ama işlem yapılmamış
          (l.status.equals(DoseStatus.pending.name) & l.scheduledTime.isSmallerOrEqualValue(now)) |
          // ertelenmiş (zamanı gelmemiş olsa bile görünür → "09:10'a ertelendi")
          l.status.equals(DoseStatus.snoozed.name) |
          // son 24 saatte kaçırılan (sonradan "aldım" denebilsin)
          (l.status.equals(DoseStatus.missed.name) &
              l.scheduledTime.isBiggerOrEqualValue(now.subtract(const Duration(hours: 24)))))
      ..orderBy([(l) => OrderingTerm.asc(l.scheduledTime)]))
    .join([innerJoin(medicines, medicines.id.equalsExp(doseLogs.medicineId))])
    .watch()
    .map((rows) => rows.map(PendingDose.fromRow).toList());
```

```dart
class PendingPanel extends ConsumerWidget {
  const PendingPanel({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pending = ref.watch(pendingDosesProvider).valueOrNull ?? const [];
    if (pending.isEmpty) return const SizedBox.shrink();
    final cs = Theme.of(context).colorScheme;
    return Card(
      color: cs.errorContainer,
      margin: const EdgeInsets.all(12),
      child: Column(children: [
        ListTile(
          leading: Icon(Icons.warning_amber_rounded, color: cs.onErrorContainer),
          title: Text(context.l10n.pendingTitle(pending.length), // "3 ilaç bekliyor"
              style: TextStyle(color: cs.onErrorContainer, fontWeight: FontWeight.bold)),
        ),
        for (final p in pending)
          ListTile(
            title: Text(p.medicineName),
            subtitle: Text(switch (p.status) {
              DoseStatus.snoozed => context.l10n.snoozedUntil(p.snoozedUntil!),   // "⏸ 09:10'a ertelendi (1/3)"
              DoseStatus.missed  => context.l10n.missedAt(p.scheduledTime),       // "✖ 09:00 kaçırıldı"
              _                  => context.l10n.dueSince(p.scheduledTime),       // "⏰ 09:00'dan beri bekliyor"
            }),
            trailing: FilledButton(
              onPressed: () => ref.read(doseActionServiceProvider).take(p.logId),
              child: Text(context.l10n.take),
            ),
            onTap: () => context.push('/alarm/${p.logId}'),
          ),
      ]),
    );
  }
}
```

Ana ekranın en üstünde sabittir; uygulama simgesinde rozet sayısı (`badge`) aynı sayıyı gösterir.

### 3.7 `AlarmPlanner` — kayan pencere ve yeniden senkronizasyon

| Tetikleyici | Yapılan |
|---|---|
| Uygulama açılışı / ön plana gelme | `reconcile()` |
| Plan ekleme/düzenleme/silme | `reconcile(scheduleId)` |
| Cihaz yeniden başlatma, uygulama güncellemesi | Eklentinin `ScheduledNotificationBootReceiver`'ı kayıtlı alarmları geri yükler; ardından ilk açılışta `reconcile()` |
| Saat dilimi / saat değişimi | `reconcile()` (yerel saat 08:00 → yeni dilimde 08:00) |
| Günlük `workmanager` görevi (~03:00) | Pencereyi ileri kaydır, Firestore'a 7 günlük `pending` örnekleri yaz |

```dart
class AlarmPlanner {
  static const window = Duration(hours: 48);   // Android'de rahat; iOS sınırı için aşağıya bakın

  Future<void> reconcile() async {
    final now = clock.now();
    // 1) Doz örneklerini üret (idempotent upsert, deterministik logId)
    for (final s in await db.schedules.active()) {
      for (final t in s.rule.occurrences(now.subtract(const Duration(hours: 24)), now.add(const Duration(days: 7)))) {
        await db.doseLogs.insertIfAbsent(DoseLog.planned(s, t));
      }
    }
    // 2) Pencere içindekileri (iOS'ta bütçe dahilinde) planla
    final upcoming = await db.doseLogs.alarmable(now, now.add(window)); // pending|snoozed, en yakından
    final budget = Platform.isIOS ? 60 : 500;   // iOS: aynı anda en fazla 64 bekleyen yerel bildirim
    final pending = await plugin.pendingNotificationRequests();
    final wanted = <int>{};
    var used = 0;
    for (final d in upcoming) {
      final cost = 1 + (d.persistent ? (Platform.isIOS ? 3 : d.maxRepeats) : 0);
      if (used + cost > budget) break;
      used += cost;
      wanted.addAll(List.generate(cost, (k) => d.intId * 16 + k));
      if (!pending.any((p) => p.id == AlarmIds.main(d.intId))) await alarms.scheduleDose(d);
    }
    // 3) Artık istenmeyenleri iptal et (silinen plan, değişen saat)
    for (final p in pending) {
      if (!wanted.contains(p.id)) await plugin.cancel(p.id);
    }
  }
}
```

### 3.8 Android yapılandırması

`android/app/src/main/AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>          <!-- Android 13+ -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>        <!-- Android 12+ (14'te varsayılan kapalı) -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>      <!-- Android 14: Play beyanı gerekir -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/> <!-- isteğe bağlı, bkz. §4 -->

<application …>
  <activity android:name=".MainActivity"
      android:showWhenLocked="true"
      android:turnScreenOn="true"
      android:launchMode="singleTop" …/>

  <receiver android:exported="false"
      android:name="com.dexterous.flutterlocalnotifications.ActionBroadcastReceiver"/>
  <receiver android:exported="false"
      android:name="com.dexterous.flutterlocalnotifications.ScheduledNotificationReceiver"/>
  <receiver android:exported="false"
      android:name="com.dexterous.flutterlocalnotifications.ScheduledNotificationBootReceiver">
    <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED"/>
      <action android:name="android.intent.action.MY_PACKAGE_REPLACED"/>
      <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
      <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
    </intent-filter>
  </receiver>
</application>
```

**Play Store politika notları**
- `USE_EXACT_ALARM` (otomatik verilen) yalnızca çekirdek işlevi **çalar saat veya takvim** olan uygulamalara izinlidir; ilaç hatırlatıcısı için risklidir. `SCHEDULE_EXACT_ALARM` kullanıp izni kullanıcıdan isteyin (aşağıdaki sağlık kontrolü).
- `USE_FULL_SCREEN_INTENT` Android 14+'ta yalnızca çağrı/alarm uygulamalarına varsayılan verilir; Play Console'da beyan edin, verilmezse uygulama yüksek öncelikli bildirime düşer (alarm yine çalar).
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` için Play Console gerekçesi istenebilir; politika riskini azaltmak için doğrudan izin diyaloğu yerine **ayar listesini açan** `ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` tercih edilebilir.

### 3.9 iOS notları
- iOS'ta arka planda uygulama kodu alarm anında **çalışmaz**; her şey önceden kurulmuş yerel bildirimdir → ısrarcı mod iOS'ta en fazla birkaç tekrar bildirimi (bütçe nedeniyle 3) ile sınırlıdır.
- **Time Sensitive** bildirimler için Xcode'da "Time Sensitive Notifications" yeteneğini ekleyin; Odak modlarını delebilir.
- **Critical Alerts** (sessiz modu delip çalma) Apple'dan özel yetki (`com.apple.developer.usernotifications.critical-alerts`) gerektirir; sağlık uygulamaları başvurabilir — onaylanırsa kritik ilaçlar için kullanın.
- Bildirim sesi ≤ 30 sn olmalıdır.

---

## 4. Güvenilirlik (pil ve izin) rehberi

### 4.1 Akış

```
İlk açılış → Hoş geldin → Dil (TR/EN) → Basit Mod sorusu → İlk ilaç
   └──▶ "Alarmlarınız güvende mi?" kontrol ekranı (ilk ilaç kaydedildikten sonra — bağlam varken)

┌────────────────────────────────────────┐
│  Alarmlarınızın her zaman çalması için │
│                                        │
│  ✅ Bildirim izni                      │
│  ⚠ Tam zamanlı alarm izni   [AÇ]      │
│  ⚠ Pil kısıtlaması          [KALDIR]  │
│  ⚠ Xiaomi: Otomatik başlatma [AÇ]     │
│  ✅ Tam ekran alarm                    │
│                                        │
│  [ Test alarmı kur (1 dk sonra) ]      │
│  [ Sonra hatırlat ]                    │
└────────────────────────────────────────┘
```

- Her satır gerçek durumu okur (statik metin değil); ayardan dönüldüğünde (`AppLifecycleState.resumed`) yeniden kontrol edilir.
- **Test alarmı**: kullanıcı uygulamayı kapatır, ekranı kilitler; alarm 1 dk sonra çalmazsa sonraki açılışta "Alarm çalmadı mı?" sorusu → üreticiye özel rehber.
- Ayarlar'da kalıcı "Alarm güvenilirliği" kartı: herhangi bir madde kırmızıya dönerse ana ekranda uyarı şeridi.
- Üretici rehberi metinleri (Xiaomi/MIUI-HyperOS, Huawei/EMUI, Samsung One UI "Uyuyan uygulamalar", Oppo/Realme/ColorOS, Vivo) uzaktan yapılandırılabilir (Firebase Remote Config) ve ekran görüntülü olmalıdır; kaynak olarak dontkillmyapp.com güncel tutulur.

### 4.2 Native köprü (Kotlin)

```kotlin
// android/app/src/main/kotlin/.../ReliabilityChannel.kt
class ReliabilityChannel(private val activity: Activity) : MethodChannel.MethodCallHandler {
  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
    val am = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    when (call.method) {
      "status" -> result.success(mapOf(
        "ignoringBatteryOptimizations" to pm.isIgnoringBatteryOptimizations(activity.packageName),
        "canScheduleExactAlarms" to (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()),
        "canUseFullScreenIntent" to (Build.VERSION.SDK_INT < 34 ||
            (activity.getSystemService(NotificationManager::class.java)).canUseFullScreenIntent()),
        "manufacturer" to Build.MANUFACTURER.lowercase(),
        "sdk" to Build.VERSION.SDK_INT,
      ))
      "openBatterySettings" -> {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:${activity.packageName}"))
        val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        result.success(tryStart(direct) || tryStart(list))
      }
      "openExactAlarmSettings" -> result.success(
        Build.VERSION.SDK_INT >= 31 &&
        tryStart(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${activity.packageName}"))))
      "openOemAutostart" -> result.success(oemIntents().any { tryStart(it) })
      else -> result.notImplemented()
    }
  }

  private fun oemIntents() = listOf(
    Intent().setComponent(ComponentName("com.miui.securitycenter",
        "com.miui.permcenter.autostart.AutoStartManagementActivity")),
    Intent().setComponent(ComponentName("com.huawei.systemmanager",
        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
    Intent().setComponent(ComponentName("com.coloros.safecenter",
        "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
    Intent().setComponent(ComponentName("com.vivo.permissionmanager",
        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}")),
  )

  private fun tryStart(i: Intent): Boolean = try {
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); activity.startActivity(i); true
  } catch (e: Exception) { false }
}
```

> OEM bileşen adları sürümden sürüme değişir; her biri `try/catch` içindedir ve en sonda genel "Uygulama bilgisi" ekranına düşülür.

### 4.3 Dart tarafı

```dart
class ReliabilityStatus {
  final bool notifications, exactAlarms, batteryUnrestricted, fullScreen;
  final String manufacturer;
  const ReliabilityStatus(this.notifications, this.exactAlarms, this.batteryUnrestricted,
      this.fullScreen, this.manufacturer);
  bool get allGood => notifications && exactAlarms && batteryUnrestricted;
  bool get needsOemGuide => const {'xiaomi', 'huawei', 'honor', 'oppo', 'realme', 'vivo', 'oneplus', 'samsung'}
      .contains(manufacturer);
}

class ReliabilityService {
  static const _ch = MethodChannel('app/reliability');
  ReliabilityService(this._plugin);
  final FlutterLocalNotificationsPlugin _plugin;

  AndroidFlutterLocalNotificationsPlugin? get _android =>
      _plugin.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();

  Future<ReliabilityStatus> check() async {
    if (!Platform.isAndroid) {
      final ios = _plugin.resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>();
      final granted = await ios?.checkPermissions();
      return ReliabilityStatus(granted?.isEnabled ?? false, true, true, true, 'apple');
    }
    final s = Map<String, dynamic>.from(await _ch.invokeMethod('status'));
    return ReliabilityStatus(
      await _android?.areNotificationsEnabled() ?? false,
      s['canScheduleExactAlarms'] as bool,
      s['ignoringBatteryOptimizations'] as bool,
      s['canUseFullScreenIntent'] as bool,
      s['manufacturer'] as String,
    );
  }

  Future<void> requestNotifications() async {
    await _android?.requestNotificationsPermission();
    await _plugin.resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>()
        ?.requestPermissions(alert: true, badge: true, sound: true);
  }
  Future<void> requestExactAlarms() async => await _android?.requestExactAlarmsPermission();
  Future<void> requestFullScreen() async => await _android?.requestFullScreenIntentPermission();
  Future<void> openBatterySettings() => _ch.invokeMethod('openBatterySettings');
  Future<void> openOemAutostart() => _ch.invokeMethod('openOemAutostart');
}
```

> Exact alarm izni olmasa bile `alarmClock` modu Android'de alarm saati API'sini kullandığı için genellikle izinsiz de kurulabilir; yine de izin durumunu izleyin ve izin yoksa `inexactAllowWhileIdle`'a düşüp kullanıcıyı uyarın — **sessizce başarısız olmayın**.

---

## 5. "Basit Mod / Senior Mode" UI/UX akışı

### 5.1 Tasarım ilkeleri

| İlke | Uygulama |
|---|---|
| **Tek ekran, tek görev** | Ana ekran yalnızca "Şimdi ne yapmalıyım?" sorusunu yanıtlar. Sekme çubuğu yok. |
| **Dev dokunma alanları** | Birincil düğmeler ≥ 72 dp yükseklik, tüm genişlik; ikincil ≥ 56 dp; aralarında ≥ 16 dp boşluk. |
| **Okunabilirlik** | Gövde ≥ 22 sp, ilaç adı ≥ 32 sp; sistem yazı ölçeği üstüne ek çarpan (1.0–1.6, kullanıcı seçer); kalın yazı tipi (Atkinson Hyperlegible / Noto Sans). |
| **Yüksek kontrast** | WCAG AAA (≥ 7:1) metin kontrastı; durum yalnızca renkle değil ikon + kelime ile de anlatılır (renk körlüğü). |
| **Tanıma > hatırlama** | İlacın kutu/hap fotoğrafı her kartta; "Coraspin" yerine "Beyaz küçük hap — kalp ilacı" takma adı. |
| **Geri alınabilirlik** | Onay diyaloğu yerine 10 sn "Geri Al" çubuğu (yanlış dokunma korkusu azalır). |
| **Ses ve dokunsal geri bildirim** | `flutter_tts` ile "Saat 9 ilacınız: Coraspin, tok karnına, 1 tablet" okunur; "Aldım"da güçlü titreşim + "Aferin!" sesi. |
| **Kilitli ayarlar** | Plan düzenleme ve ayarlar 4 haneli PIN veya bakım veren uzaktan yönetir (`canManageSeniorMode`). |
| **Yardım bir dokunuş uzakta** | Her ekranda "📞 Yakınımı Ara" düğmesi (birincil bakım veren). |

### 5.2 Ekran akışı

```
                    ┌──────────────────────────┐
  Alarm çalar ────▶ │ ① ALARM (tam ekran)       │
                    └────────────┬─────────────┘
                                 │ Aldım / 10 dk sonra / Almayacağım
                                 ▼
┌──────────────────────────┐   ┌──────────────────────────┐   ┌──────────────────────────┐
│ ② ANA EKRAN "Bugün"      │──▶│ ③ İLAÇLARIM (salt okunur)│   │ ④ YARDIM                 │
│  şimdiki doz kartı       │   │  foto + takma ad + stok  │   │  📞 Yakınımı ara         │
│  sıradaki dozlar         │   └──────────────────────────┘   │  🏥 Eczane bul           │
│  [📞 Yakınımı Ara]       │──────────────────────────────────▶│  🔊 Ekranı sesli oku     │
│  ⚙ (uzun bas + PIN)      │                                   └──────────────────────────┘
└────────────┬─────────────┘
             │ uzun bas 2 sn + PIN
             ▼
   ⑤ Tam (normal) mod ayarları — yalnızca hasta yakını / PIN ile
```

#### ① Alarm ekranı (kilit ekranı üzerinde)

```
┌──────────────────────────────────┐
│            09:00                 │  ← 56 sp
│  ┌────────────────────────────┐  │
│  │      [ hap fotoğrafı ]     │  │  ← 160 dp
│  └────────────────────────────┘  │
│        CORASPIN 100 mg           │  ← 34 sp kalın
│        Kalp ilacı · 1 tablet     │  ← 24 sp
│  ┌────────────────────────────┐  │
│  │ 🔴  TOK KARNINA             │  │  ← tam genişlik rozet, 28 sp, kırmızı zemin beyaz yazı
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │        ✓  ALDIM             │  │  ← 96 dp, yeşil, ekranın alt yarısı
│  └────────────────────────────┘  │
│  ┌─────────────┐ ┌────────────┐  │
│  │ ⏰ 10 dk     │ │ ✖ Almayacağım│ │  ← 64 dp, ikincil
│  └─────────────┘ └────────────┘  │
└──────────────────────────────────┘
```

- "Almayacağım" → tek soruluk büyük seçenekli ekran (Kendimi kötü hissediyorum / İlacım bitti / Doktor kesti / Diğer). "İlacım bitti" → doğrudan Eczane ekranı + yakına bildirim.
- Açılışta TTS otomatik okur (ayarla kapatılabilir), ses alarm kanalından çalar.
- Ekran 60 sn boyunca açık kalır (`wakelock_plus`), parlaklık yükselir.

**Aç/tok rozet sözlüğü** (renk + ikon + kelime birlikte):

| Durum | Rozet | Renk |
|---|---|---|
| Tok karnına (yemekten sonra) | 🍽️ TOK KARNINA | Turuncu zemin `#B34700` / beyaz yazı |
| Aç karnına | ⭕ AÇ KARNINA | Mavi zemin `#0B4F9C` / beyaz yazı |
| Yemekle birlikte | 🥄 YEMEKLE BİRLİKTE | Mor zemin `#5B2A86` / beyaz yazı |
| Yemekten 30 dk önce | ⏱️ YEMEKTEN 30 DK ÖNCE | Mavi zemin + süre |

> Kullanıcının örneğindeki 🔴/🟢 eşlemesi yerine kırmızı-yeşil çiftinden kaçındık: kırmızı/yeşil renk körlüğü erkeklerin yaklaşık %8'inde görülür ve kırmızı "tehlike/yapma" çağrışımı yapar. Renkler beyaz yazıyla AAA kontrast sağlar.

#### ② Ana ekran "Bugün"

```
┌──────────────────────────────────┐
│ Günaydın Fatma Hanım      4 Kasım│  ← 26 sp
│ ┌──────────────────────────────┐ │
│ │ ⚠ 1 İLAÇ BEKLİYOR            │ │  ← Bekleyenler paneli (kırmızı kart)
│ │ Coraspin · 09:00              │ │
│ │ [      ✓ ŞİMDİ ALDIM       ]  │ │  ← 72 dp
│ └──────────────────────────────┘ │
│  SIRADAKİ                        │
│ ┌──────────────────────────────┐ │
│ │ [foto] 13:00  Glifor          │ │
│ │        🍽️ TOK KARNINA         │ │
│ └──────────────────────────────┘ │
│ ┌──────────────────────────────┐ │
│ │ [foto] 20:00  Glifor          │ │
│ └──────────────────────────────┘ │
│  Bugün: ●●○○  2/4 alındı         │  ← büyük ilerleme noktaları
│ ┌──────────────────────────────┐ │
│ │      📞  KIZIMI ARA          │ │  ← 72 dp
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

- Kartlara dokunmak ilaç fotoğrafını tam ekran büyütür ("Bu hap mı?").
- Kaydırma minimumda: en fazla "şimdi + sıradaki 2 doz" gösterilir, kalanı "Tüm gün" düğmesiyle.
- Gece (22:00 sonrası) koyu, yine yüksek kontrastlı tema (siyah zemin, sarı/beyaz yazı).

#### ⑤ Basit Mod'a geçiş ve yönetim
- **İlk açılışta**: "Uygulamayı kim kullanacak?" → "Kendim için, sade ve büyük yazılı olsun" seçeneği Basit Mod'u açar.
- **Bakım verenden**: Bakım veren kendi telefonundan hastanın planını kurar, "Basit Mod'u aç ve kilitle" der → hasta cihazı `users/{uid}.seniorMode` değişikliğini dinler.
- **Çıkış**: Ana ekran başlığına 2 sn uzun bas → PIN. Yanlışlıkla çıkış mümkün değil.

```json
// users/{uid} — ilgili alanlar
{
  "seniorMode": {
    "enabled": true,
    "textScale": 1.4,
    "highContrast": true,
    "ttsOnAlarm": true,
    "lockedBy": "user_caretaker456",
    "pinHash": "argon2id$…",
    "primaryContact": { "name": "Ayşe", "phone": "+905000000000" }
  }
}
```

### 5.3 Flutter uygulaması

```dart
/// Basit Mod teması: büyük, yüksek kontrastlı, geniş dokunma alanları.
ThemeData seniorTheme({required bool dark}) {
  final cs = dark
      ? const ColorScheme.dark(
          primary: Color(0xFFFFD54F), onPrimary: Colors.black,
          surface: Colors.black, onSurface: Colors.white,
          error: Color(0xFFFF8A80), onError: Colors.black,
          errorContainer: Color(0xFF5C0000), onErrorContainer: Colors.white,
        )
      : const ColorScheme.light(
          primary: Color(0xFF0B5D1E), onPrimary: Colors.white,   // koyu yeşil — AAA
          surface: Colors.white, onSurface: Colors.black,
          error: Color(0xFFB00020), onError: Colors.white,
          errorContainer: Color(0xFFFFE5E5), onErrorContainer: Color(0xFF5C0000),
        );
  final base = ThemeData(useMaterial3: true, colorScheme: cs, fontFamily: 'AtkinsonHyperlegible');
  return base.copyWith(
    textTheme: base.textTheme.apply(fontSizeFactor: 1.35, bodyColor: cs.onSurface, displayColor: cs.onSurface),
    materialTapTargetSize: MaterialTapTargetSize.padded,
    visualDensity: const VisualDensity(horizontal: 2, vertical: 2),
    filledButtonTheme: FilledButtonThemeData(style: FilledButton.styleFrom(
      minimumSize: const Size.fromHeight(72),
      textStyle: const TextStyle(fontSize: 26, fontWeight: FontWeight.w800),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
    )),
    outlinedButtonTheme: OutlinedButtonThemeData(style: OutlinedButton.styleFrom(
      minimumSize: const Size.fromHeight(64),
      side: BorderSide(color: cs.onSurface, width: 2),
      textStyle: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700),
    )),
    cardTheme: CardThemeData(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
        side: BorderSide(color: cs.onSurface.withValues(alpha: .4), width: 2),
      ),
    ),
  );
}

/// Kök: kullanıcının ek yazı çarpanını sistem ayarının üstüne uygular.
class SeniorScope extends StatelessWidget {
  const SeniorScope({super.key, required this.textScale, required this.child});
  final double textScale;
  final Widget child;
  @override
  Widget build(BuildContext context) {
    final mq = MediaQuery.of(context);
    final system = mq.textScaler.scale(1);
    return MediaQuery(
      data: mq.copyWith(textScaler: TextScaler.linear((system * textScale).clamp(1.0, 2.2))),
      child: child,
    );
  }
}

class MealBadge extends StatelessWidget {
  const MealBadge(this.meal, {super.key});
  final MealRelation meal;
  @override
  Widget build(BuildContext context) {
    final (icon, label, color) = switch (meal) {
      MealRelation.afterMeal    => ('🍽️', context.l10n.afterMeal,  const Color(0xFFB34700)),
      MealRelation.beforeMeal   => ('⏱️', context.l10n.beforeMeal, const Color(0xFF0B4F9C)),
      MealRelation.emptyStomach => ('⭕', context.l10n.emptyStomach, const Color(0xFF0B4F9C)),
      MealRelation.withMeal     => ('🥄', context.l10n.withMeal,   const Color(0xFF5B2A86)),
      MealRelation.none         => ('', '', Colors.transparent),
    };
    if (meal == MealRelation.none) return const SizedBox.shrink();
    return Semantics(
      label: label,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
        decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(16)),
        child: Text('$icon  ${label.toUpperCase()}', textAlign: TextAlign.center,
            style: const TextStyle(color: Colors.white, fontSize: 26, fontWeight: FontWeight.w900)),
      ),
    );
  }
}

class SeniorAlarmScreen extends ConsumerStatefulWidget {
  const SeniorAlarmScreen({super.key, required this.logId});
  final String logId;
  @override
  ConsumerState<SeniorAlarmScreen> createState() => _SeniorAlarmScreenState();
}

class _SeniorAlarmScreenState extends ConsumerState<SeniorAlarmScreen> {
  @override
  void initState() {
    super.initState();
    WakelockPlus.enable();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      final d = await ref.read(pendingDoseProvider(widget.logId).future);
      if (ref.read(seniorSettingsProvider).ttsOnAlarm) {
        await ref.read(ttsProvider).speak(context.l10n.ttsDose(d.timeLabel, d.displayName, d.mealLabel, d.quantityLabel));
      }
    });
  }

  @override
  void dispose() { WakelockPlus.disable(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    final dose = ref.watch(pendingDoseProvider(widget.logId));
    final actions = ref.read(doseActionServiceProvider);
    return Scaffold(
      body: SafeArea(
        child: dose.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (_, __) => Center(child: Text(context.l10n.genericError)),
          data: (d) => Padding(
            padding: const EdgeInsets.all(20),
            child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
              Text(d.timeLabel, textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.displayMedium?.copyWith(fontWeight: FontWeight.w900)),
              const SizedBox(height: 12),
              Expanded(child: ClipRRect(borderRadius: BorderRadius.circular(20),
                  child: d.photo != null ? Image.file(d.photo!, fit: BoxFit.contain) : const Icon(Icons.medication, size: 120))),
              const SizedBox(height: 12),
              Text(d.displayName, textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.headlineLarge?.copyWith(fontWeight: FontWeight.w900)),
              Text(d.quantityLabel, textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 12),
              MealBadge(d.meal),
              const SizedBox(height: 20),
              FilledButton.icon(
                style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(96)),
                icon: const Icon(Icons.check_circle, size: 40),
                label: Text(context.l10n.takeBig), // "ALDIM"
                onPressed: () async {
                  HapticFeedback.heavyImpact();
                  await actions.take(d.logId);
                  if (!context.mounted) return;
                  context.pop();
                  showUndoBar(context, onUndo: () => actions.undoTake(d.logId)); // 10 sn geri al
                },
              ),
              const SizedBox(height: 16),
              Row(children: [
                if (d.canSnooze)
                  Expanded(child: OutlinedButton(
                    onPressed: () async { await actions.snooze(d.logId, const Duration(minutes: 10)); if (context.mounted) context.pop(); },
                    child: Text(context.l10n.snooze10))),
                if (d.canSnooze) const SizedBox(width: 16),
                Expanded(child: OutlinedButton(
                  onPressed: () => context.push('/senior/skip/${d.logId}'),
                  child: Text(context.l10n.wontTake))),
              ]),
            ]),
          ),
        ),
      ),
    );
  }
}
```

### 5.4 Basit Mod kabul kriterleri (test listesi)
- [ ] Tüm birincil eylemler ≤ 1 dokunuş (alarm → "Aldım").
- [ ] Sistem yazı boyutu en büyük + uygulama çarpanı 1.6'da hiçbir metin taşmaz/kesilmez (golden testler, 320 dp genişlik).
- [ ] TalkBack/VoiceOver ile tüm düğmeler anlamlı okunur (`Semantics`).
- [ ] Kontrast ≥ 7:1 (otomatik test: renk çiftleri listesi).
- [ ] Yanlışlıkla Basit Mod'dan çıkılamaz; PIN'siz plan değiştirilemez.
- [ ] 65+ yaş en az 5 kullanıcıyla moderasyonlu kullanılabilirlik testi: "ilacı aldım işaretle", "ertelenen ilacı bul", "yakınını ara" görevleri ≤ 10 sn.

---

## 6. Doğrulama planı (alarm güvenilirliği)

| Test | Yöntem |
|---|---|
| Doze modu | `adb shell dumpsys deviceidle force-idle` → alarm zamanında çalmalı |
| Uygulama zorla durduruldu | Ayarlar → Zorla durdur sonrası alarm (Android'de force-stop alarmları siler → ilk açılışta uyarı + `reconcile`) |
| Yeniden başlatma | Alarm kur → cihazı yeniden başlat → alarm çalmalı (BOOT receiver) |
| Saat dilimi değişimi | İstanbul → Berlin: 08:00 alarmı yerel 08:00'de çalmalı |
| Ertele ×3 | Dördüncü alarmda "Ertele" düğmesi olmamalı; panelde "3/3" |
| Çevrimdışı | Uçak modu: aldım → stok düşer → ağ gelince Firestore'a yazılır, bakım verene bildirim gider |
| iOS 64 sınırı | 10 ilaç × 4 doz × ısrarcı → `pendingNotificationRequests().length ≤ 64` |
| OEM cihaz matrisi | Xiaomi, Samsung, Huawei/Honor, Oppo/Realme, Pixel — Firebase Test Lab + gerçek cihaz; gece boyunca 8 alarm, gecikme ≤ 1 dk |
| Kaçırılan doz (sunucu) | Telefon kapalı → `graceUntil` + 5 dk içinde bakım verene "kaçırıldı" bildirimi |

**Telemetri:** Her alarm için `scheduledTime` ile bildirimin gösterildiği / uygulamanın açıldığı zaman arasındaki fark (yerelde kaydedilip anonim olarak gönderilir) → üretici/sürüm bazında "alarm gecikmesi" panosu. Rakiplerin göremediği sorunu biz ölçeriz.

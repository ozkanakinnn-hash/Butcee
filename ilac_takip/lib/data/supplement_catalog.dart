// Takviye kataloğu, etkileşim kuralları ve eğitim içeriği.
//
// Referans değerler: ABD NIH Office of Dietary Supplements (ODS) — 19–50 yaş yetişkin
// RDA/AI ve UL (Tolerable Upper Intake Level). Bilgi amaçlıdır, tıbbi tavsiye değildir;
// gebelik, emzirme, kronik hastalık ve ilaç kullanımında değerler farklıdır.
import '../domain/models.dart';

/// İki dilli metin.
class L {
  const L(this.tr, this.en);
  final String tr;
  final String en;
  String of(String locale) => locale.startsWith('en') ? en : tr;
}

class SupplementInfo {
  const SupplementInfo({
    required this.id,
    required this.name,
    required this.category,
    this.code,
    this.unit = 'mg',
    this.defaultAmount,
    this.meal = MealRelation.none,
    this.defaultSlot = 'lateMorning',
    this.rdaMale,
    this.rdaFemale,
    this.isAi = false,
    this.ul,
    this.ulNote,
    this.note,
    this.keywords = const [],
  });

  final String id;
  final L name;
  final ItemCategory category;
  /// Simgede gösterilen kısa kod: vitamin harfi ya da kimyasal sembol (D3, B12, Fe, Mg…).
  /// null → kategori simgesi kullanılır.
  final String? code;
  final String unit;

  /// Formu önceden doldurmak için yaygın etiket dozu — öneri DEĞİLDİR, kullanıcı etiketten girer.
  final double? defaultAmount;
  final MealRelation meal;
  final String defaultSlot;
  final double? rdaMale;
  final double? rdaFemale;

  /// true → RDA yerine AI (yeterli alım) değeri.
  final bool isAi;

  /// Günlük üst sınır, [unit] cinsinden. null → belirlenmiş UL yok.
  final double? ul;
  final L? ulNote;
  final L? note;
  final List<String> keywords;

  bool get hasReference => rdaMale != null || rdaFemale != null;
}

const _fatSoluble = L(
  'Yağda çözünür; yağ içeren bir öğünle alındığında daha iyi emilir.',
  'Fat-soluble; better absorbed with a meal that contains fat.',
);

const supplementCatalog = <SupplementInfo>[
  SupplementInfo(
    id: 'vitD', name: L('D Vitamini', 'Vitamin D'), category: ItemCategory.vitamin, code: 'D3',
    unit: 'IU', defaultAmount: 1000, meal: MealRelation.withMeal, defaultSlot: 'earlyMorning',
    rdaMale: 600, rdaFemale: 600, ul: 4000, note: L('1 mcg = 40 IU. Yağda çözünür; yemekle alın.', '1 mcg = 40 IU. Fat-soluble; take with a meal.'),
    keywords: ['d3', 'kolekalsiferol', 'cholecalciferol'],
  ),
  SupplementInfo(
    id: 'vitC', name: L('C Vitamini', 'Vitamin C'), category: ItemCategory.vitamin, code: 'C',
    defaultAmount: 500, defaultSlot: 'earlyMorning', rdaMale: 90, rdaFemale: 75, ul: 2000,
    note: L('Bitkisel kaynaklı demirin emilimini artırabilir.', 'May increase absorption of plant-based iron.'),
    keywords: ['askorbik', 'ascorbic'],
  ),
  SupplementInfo(
    id: 'vitB12', name: L('B12 Vitamini', 'Vitamin B12'), category: ItemCategory.vitamin, code: 'B12',
    unit: 'mcg', defaultAmount: 1000, defaultSlot: 'earlyMorning', rdaMale: 2.4, rdaFemale: 2.4,
    note: L('Belirlenmiş üst sınırı yoktur. Vegan beslenmede ve ileri yaşta eksiklik daha sık görülür.',
        'No established upper limit. Deficiency is more common with vegan diets and older age.'),
    keywords: ['kobalamin', 'cobalamin', 'b 12'],
  ),
  SupplementInfo(
    id: 'folate', name: L('Folat (B9)', 'Folate (B9)'), category: ItemCategory.vitamin, code: 'B9',
    unit: 'mcg', defaultAmount: 400, defaultSlot: 'earlyMorning', rdaMale: 400, rdaFemale: 400, ul: 1000,
    ulNote: L('Üst sınır yalnızca takviye/folik asit içindir.', 'Upper limit applies to supplemental folic acid only.'),
    keywords: ['folik asit', 'folic acid', 'b9'],
  ),
  SupplementInfo(
    id: 'vitB1', name: L('B1 Vitamini (Tiamin)', 'Vitamin B1 (Thiamine)'), category: ItemCategory.vitamin, code: 'B1',
    defaultAmount: 100, rdaMale: 1.2, rdaFemale: 1.1, keywords: ['tiamin', 'thiamine'],
  ),
  SupplementInfo(
    id: 'vitB6', name: L('B6 Vitamini', 'Vitamin B6'), category: ItemCategory.vitamin, code: 'B6',
    defaultAmount: 25, rdaMale: 1.3, rdaFemale: 1.3, ul: 100,
    note: L('Uzun süreli yüksek doz sinir hasarına yol açabilir.', 'Long-term high doses can cause nerve damage.'),
    keywords: ['piridoksin', 'pyridoxine'],
  ),
  SupplementInfo(
    id: 'biotin', name: L('Biyotin (B7)', 'Biotin (B7)'), category: ItemCategory.vitamin, code: 'B7',
    unit: 'mcg', defaultAmount: 5000, rdaMale: 30, rdaFemale: 30, isAi: true,
    note: L('Yüksek doz biyotin bazı kan testlerinin sonucunu bozabilir; test öncesi doktorunuza söyleyin.',
        'High-dose biotin can interfere with some blood tests; tell your doctor before testing.'),
  ),
  SupplementInfo(
    id: 'vitA', name: L('A Vitamini', 'Vitamin A'), category: ItemCategory.vitamin, code: 'A',
    unit: 'mcg', defaultAmount: 750, meal: MealRelation.withMeal, rdaMale: 900, rdaFemale: 700, ul: 3000,
    ulNote: L('Üst sınır hazır (retinol) A vitamini içindir. Gebelikte fazlası zararlıdır.',
        'Upper limit applies to preformed vitamin A (retinol). Excess is harmful in pregnancy.'),
    note: _fatSoluble, keywords: ['retinol'],
  ),
  SupplementInfo(
    id: 'vitE', name: L('E Vitamini', 'Vitamin E'), category: ItemCategory.vitamin, code: 'E',
    defaultAmount: 15, meal: MealRelation.withMeal, rdaMale: 15, rdaFemale: 15, ul: 1000,
    note: _fatSoluble, keywords: ['tokoferol', 'tocopherol'],
  ),
  SupplementInfo(
    id: 'vitK', name: L('K Vitamini (K2)', 'Vitamin K (K2)'), category: ItemCategory.vitamin, code: 'K2',
    unit: 'mcg', defaultAmount: 100, meal: MealRelation.withMeal, defaultSlot: 'earlyMorning',
    rdaMale: 120, rdaFemale: 90, isAi: true,
    note: L('Kan sulandırıcı (ör. varfarin) kullanıyorsanız doktorunuza danışmadan başlamayın.',
        'If you take a blood thinner (e.g. warfarin), talk to your doctor before starting.'),
    keywords: ['mk7', 'mk-7', 'menakinon'],
  ),
  SupplementInfo(
    id: 'calcium', name: L('Kalsiyum', 'Calcium'), category: ItemCategory.mineral, code: 'Ca',
    defaultAmount: 500, meal: MealRelation.withMeal, defaultSlot: 'noon', rdaMale: 1000, rdaFemale: 1000, ul: 2500,
    note: L('Bir seferde en fazla ~500 mg daha iyi emilir. Demir ve tiroid ilacından ayrı saatte alın.',
        'Absorbed best in doses of ~500 mg or less. Take apart from iron and thyroid medicine.'),
  ),
  SupplementInfo(
    id: 'magnesium', name: L('Magnezyum', 'Magnesium'), category: ItemCategory.mineral, code: 'Mg',
    defaultAmount: 200, defaultSlot: 'evening', rdaMale: 420, rdaFemale: 320, ul: 350,
    ulNote: L('Üst sınır yalnızca takviyeden alınan magnezyum içindir; fazlası ishal yapabilir.',
        'Upper limit applies to supplemental magnesium only; excess may cause diarrhoea.'),
  ),
  SupplementInfo(
    id: 'iron', name: L('Demir', 'Iron'), category: ItemCategory.mineral, code: 'Fe',
    defaultAmount: 18, meal: MealRelation.emptyStomach, defaultSlot: 'lateMorning',
    rdaMale: 8, rdaFemale: 18, ul: 45,
    note: L('Aç karnına daha iyi emilir; mideyi rahatsız ederse az bir yemekle alınabilir. Kahve/çay ve kalsiyumdan ayrı alın.',
        'Absorbed best on an empty stomach; take with a little food if it upsets your stomach. Keep apart from coffee/tea and calcium.'),
  ),
  SupplementInfo(
    id: 'zinc', name: L('Çinko', 'Zinc'), category: ItemCategory.mineral, code: 'Zn',
    defaultAmount: 15, meal: MealRelation.withMeal, defaultSlot: 'noon', rdaMale: 11, rdaFemale: 8, ul: 40,
    note: L('Aç karnına mide bulantısı yapabilir. Uzun süre yüksek doz bakır eksikliğine yol açabilir.',
        'May cause nausea on an empty stomach. Long-term high doses can lead to copper deficiency.'),
  ),
  SupplementInfo(
    id: 'selenium', name: L('Selenyum', 'Selenium'), category: ItemCategory.mineral, code: 'Se',
    unit: 'mcg', defaultAmount: 55, meal: MealRelation.withMeal, rdaMale: 55, rdaFemale: 55, ul: 400,
  ),
  SupplementInfo(
    id: 'iodine', name: L('İyot', 'Iodine'), category: ItemCategory.mineral, code: 'I',
    unit: 'mcg', defaultAmount: 150, rdaMale: 150, rdaFemale: 150, ul: 1100,
    note: L('Tiroid hastalığınız varsa doktorunuza danışın.', 'Talk to your doctor if you have a thyroid condition.'),
  ),
  SupplementInfo(
    id: 'copper', name: L('Bakır', 'Copper'), category: ItemCategory.mineral, code: 'Cu',
    unit: 'mcg', defaultAmount: 900, meal: MealRelation.withMeal, rdaMale: 900, rdaFemale: 900, ul: 10000,
  ),
  SupplementInfo(
    id: 'potassium', name: L('Potasyum', 'Potassium'), category: ItemCategory.mineral, code: 'K',
    defaultAmount: 99, meal: MealRelation.withMeal, rdaMale: 3400, rdaFemale: 2600, isAi: true,
    note: L('Böbrek hastalığında ve bazı tansiyon ilaçlarıyla tehlikeli olabilir; doktorunuza danışın.',
        'Can be dangerous with kidney disease and some blood-pressure medicines; ask your doctor.'),
  ),
  SupplementInfo(
    id: 'omega3', name: L('Omega-3 (Balık yağı)', 'Omega-3 (Fish oil)'), category: ItemCategory.fattyAcid, code: 'Ω3',
    defaultAmount: 1000, meal: MealRelation.withMeal, defaultSlot: 'earlyMorning',
    note: L('EPA/DHA için resmî RDA yoktur. Yemekle almak balık tadı geri dönüşünü azaltır. Kan sulandırıcı kullanıyorsanız doktorunuza danışın.',
        'There is no official RDA for EPA/DHA. Taking it with food reduces fishy burps. Ask your doctor if you take blood thinners.'),
    keywords: ['epa', 'dha', 'balık yağı', 'fish oil'],
  ),
  SupplementInfo(
    id: 'multi', name: L('Multivitamin', 'Multivitamin'), category: ItemCategory.multi, code: 'A–Z',
    unit: 'tablet', defaultAmount: 1, meal: MealRelation.withMeal, defaultSlot: 'earlyMorning',
    note: L('İçeriğindeki vitamin ve mineraller diğer takviyelerinizle toplanır; üst sınırları kontrol edin.',
        'Its vitamins and minerals add up with your other supplements; check upper limits.'),
  ),
  SupplementInfo(
    id: 'probiotic', name: L('Probiyotik', 'Probiotic'), category: ItemCategory.probiotic,
    unit: 'capsule', defaultAmount: 1, defaultSlot: 'earlyMorning',
  ),
  SupplementInfo(
    id: 'collagen', name: L('Kolajen', 'Collagen'), category: ItemCategory.protein,
    unit: 'g', defaultAmount: 10, defaultSlot: 'earlyMorning',
  ),
  SupplementInfo(
    id: 'coq10', name: L('Koenzim Q10', 'Coenzyme Q10'), category: ItemCategory.other, code: 'Q10',
    defaultAmount: 100, meal: MealRelation.withMeal, note: _fatSoluble, keywords: ['q10', 'ubikinon'],
  ),
  SupplementInfo(
    id: 'ashwagandha', name: L('Ashwagandha', 'Ashwagandha'), category: ItemCategory.herbal,
    defaultAmount: 300, defaultSlot: 'evening',
    note: L('Bitkisel üründür; gebelikte, tiroid ve otoimmün hastalıklarda ve ilaç kullanıyorsanız doktorunuza danışın.',
        'Herbal product; ask your doctor if pregnant, if you have thyroid or autoimmune disease, or take medicines.'),
  ),
];

SupplementInfo? catalogById(String? id) {
  if (id == null) return null;
  for (final s in supplementCatalog) {
    if (s.id == id) return s;
  }
  return null;
}

List<SupplementInfo> searchCatalog(String query, String locale) {
  final q = query.trim().toLowerCase();
  if (q.isEmpty) return supplementCatalog;
  return supplementCatalog
      .where((s) =>
          s.name.tr.toLowerCase().contains(q) ||
          s.name.en.toLowerCase().contains(q) ||
          s.keywords.any((k) => k.contains(q)))
      .toList();
}

// ───────────────────────── Etkileşim / uyumluluk ─────────────────────────

enum CompatType {
  /// Farklı saatlerde alın.
  separate,

  /// Birlikte iyi çalışır.
  synergy,

  /// Uzun süreli/yüksek dozda dikkat (aynı saatte olmasa da).
  caution,
}

class CompatRule {
  const CompatRule(this.a, this.b, this.type, this.reason, {this.gapHours});
  final String a;
  final String b;
  final CompatType type;
  final L reason;
  final int? gapHours;

  bool involves(String id) => a == id || b == id;
  String other(String id) => a == id ? b : a;
}

const compatRules = <CompatRule>[
  CompatRule('calcium', 'iron', CompatType.separate,
      L('Kalsiyum demir emilimini azaltabilir; en az 2 saat ara verin.', 'Calcium can reduce iron absorption; keep at least 2 hours apart.'),
      gapHours: 2),
  CompatRule('zinc', 'iron', CompatType.separate,
      L('Birlikte alındığında birbirlerinin emilimini azaltabilirler.', 'Taken together they can reduce each other\'s absorption.'),
      gapHours: 2),
  CompatRule('calcium', 'zinc', CompatType.separate,
      L('Yüksek doz kalsiyum çinko emilimini azaltabilir.', 'High-dose calcium can reduce zinc absorption.'),
      gapHours: 2),
  CompatRule('zinc', 'copper', CompatType.caution,
      L('Uzun süreli yüksek doz çinko bakır emilimini azaltıp eksikliğe yol açabilir.',
          'Long-term high-dose zinc can reduce copper absorption and cause deficiency.')),
  CompatRule('iron', 'vitC', CompatType.synergy,
      L('C vitamini bitkisel kaynaklı demirin emilimini artırabilir.', 'Vitamin C can increase absorption of plant-based iron.')),
  CompatRule('vitD', 'calcium', CompatType.synergy,
      L('D vitamini bağırsaktan kalsiyum emilimini destekler.', 'Vitamin D supports calcium absorption in the gut.')),
  CompatRule('vitD', 'magnesium', CompatType.synergy,
      L('Magnezyum, D vitamininin vücutta işlenmesinde görev alır.', 'Magnesium is involved in processing vitamin D in the body.')),
  CompatRule('vitD', 'vitK', CompatType.synergy,
      L('İkisi de kalsiyum metabolizmasında rol alır ve yağda çözünür; birlikte yemekle alınabilir.',
          'Both play a role in calcium metabolism and are fat-soluble; can be taken together with a meal.')),
  CompatRule('vitB12', 'folate', CompatType.synergy,
      L('Birlikte çalışırlar. Yüksek folat B12 eksikliğini gizleyebilir; B12 düzeyinizi kontrol ettirin.',
          'They work together. High folate can mask B12 deficiency; have your B12 checked.')),
  CompatRule('vitD', 'omega3', CompatType.synergy,
      L('Yağ içeren omega-3 ile aynı öğünde alınan D vitamini daha iyi emilebilir.',
          'Vitamin D taken in the same meal as fat-containing omega-3 may be absorbed better.')),
];

List<CompatRule> rulesFor(String id) => compatRules.where((r) => r.involves(id)).toList();

/// Kullanıcının planındaki bir takviye ve aldığı dilimler/saatler.
class PlannedItem {
  const PlannedItem({required this.medicineId, required this.catalogId, required this.name, required this.hours});
  final int medicineId;
  final String? catalogId;
  final String name;

  /// Dozların alındığı saatler (0–23).
  final Set<int> hours;
}

class Conflict {
  const Conflict(this.first, this.second, this.rule);
  final PlannedItem first;
  final PlannedItem second;
  final CompatRule rule;
}

/// "Ayrı alın" kuralı olan iki takviye, [CompatRule.gapHours] içinde bir saatte planlandıysa çakışır.
List<Conflict> findConflicts(List<PlannedItem> items) {
  final out = <Conflict>[];
  for (var i = 0; i < items.length; i++) {
    for (var j = i + 1; j < items.length; j++) {
      final a = items[i], b = items[j];
      if (a.catalogId == null || b.catalogId == null) continue;
      for (final r in compatRules) {
        if (r.type != CompatType.separate) continue;
        final match = (r.a == a.catalogId && r.b == b.catalogId) || (r.b == a.catalogId && r.a == b.catalogId);
        if (!match) continue;
        final gap = r.gapHours ?? 2;
        final close = a.hours.any((h1) => b.hours.any((h2) => (h1 - h2).abs() < gap));
        if (close) out.add(Conflict(a, b, r));
      }
    }
  }
  return out;
}

/// Günlük toplam dozu üst sınırla karşılaştırır (birimler eşleşmiyorsa null).
/// D vitamini için IU ↔ mcg dönüşümü yapılır (1 mcg = 40 IU).
double? dailyTotalInCatalogUnit(SupplementInfo info, double amountPerDose, String unit, double dosesPerDay) {
  var amount = amountPerDose;
  if (unit != info.unit) {
    if (info.id == 'vitD' && unit == 'mcg' && info.unit == 'IU') {
      amount = amountPerDose * 40;
    } else if (info.id == 'vitD' && unit == 'IU' && info.unit == 'mcg') {
      amount = amountPerDose / 40;
    } else if (unit == 'g' && info.unit == 'mg') {
      amount = amountPerDose * 1000;
    } else if (unit == 'mg' && info.unit == 'mcg') {
      amount = amountPerDose * 1000;
    } else {
      return null;
    }
  }
  return amount * dosesPerDay;
}

// ───────────────────────── Hazır setler ─────────────────────────

class PresetItem {
  const PresetItem(this.catalogId, this.slot);
  final String catalogId;
  final String slot;
}

class SupplementPreset {
  const SupplementPreset({required this.id, required this.name, required this.description, required this.items});
  final String id;
  final L name;
  final L description;
  final List<PresetItem> items;
}

/// Örnek düzenler — "ayrı alın" kurallarına uyacak şekilde dilimlere dağıtılmıştır.
const supplementPresets = <SupplementPreset>[
  SupplementPreset(
    id: 'balanced',
    name: L('Dengeli başlangıç', 'Balanced start'),
    description: L('Multivitamin, D vitamini, omega-3 ve akşam magnezyumu.', 'Multivitamin, vitamin D, omega-3 and evening magnesium.'),
    items: [PresetItem('multi', 'earlyMorning'), PresetItem('vitD', 'earlyMorning'), PresetItem('omega3', 'earlyMorning'), PresetItem('magnesium', 'evening')],
  ),
  SupplementPreset(
    id: 'bone',
    name: L('Kemik sağlığı', 'Bone health'),
    description: L('Kalsiyum öğle yemeğiyle; D ve K vitamini sabah; magnezyum akşam.', 'Calcium with lunch; vitamins D and K in the morning; magnesium in the evening.'),
    items: [PresetItem('vitD', 'earlyMorning'), PresetItem('vitK', 'earlyMorning'), PresetItem('calcium', 'noon'), PresetItem('magnesium', 'evening')],
  ),
  SupplementPreset(
    id: 'immune',
    name: L('Bağışıklık', 'Immune support'),
    description: L('C ve D vitamini sabah, çinko öğle yemeğiyle.', 'Vitamins C and D in the morning, zinc with lunch.'),
    items: [PresetItem('vitC', 'earlyMorning'), PresetItem('vitD', 'earlyMorning'), PresetItem('zinc', 'noon')],
  ),
  SupplementPreset(
    id: 'energy',
    name: L('Enerji (B12 + demir)', 'Energy (B12 + iron)'),
    description: L('B12 ve folat sabah; demir C vitaminiyle geç sabah, aç karnına.', 'B12 and folate in the morning; iron with vitamin C late morning, on an empty stomach.'),
    items: [PresetItem('vitB12', 'earlyMorning'), PresetItem('folate', 'earlyMorning'), PresetItem('iron', 'lateMorning'), PresetItem('vitC', 'lateMorning')],
  ),
];

// ───────────────────────── Öğren ─────────────────────────

class InfoCard {
  const InfoCard(this.title, this.body);
  final L title;
  final L body;
}

const basicsCards = <InfoCard>[
  InfoCard(L('RDA, AI ve UL farkı', 'RDA, AI and UL'),
      L('RDA, sağlıklı kişilerin neredeyse tamamının ihtiyacını karşılayan günlük miktardır. Yeterli veri yoksa AI (yeterli alım) kullanılır. UL ise çoğu kişide yan etki beklenmeyen en yüksek günlük miktardır — hedef değil, sınırdır.',
          'The RDA is the daily amount that meets the needs of nearly all healthy people. When data are limited, an AI (adequate intake) is used. The UL is the highest daily amount unlikely to cause harm — a limit, not a target.')),
  InfoCard(L('Etiketteki yüzdenin anlamı', 'What the label percentage means'),
      L('Etiketteki %, ürünün bir dozunun referans değere oranıdır. %100\'ü aşmak her zaman zararlı değildir, ama üst sınıra (UL) yaklaşmamaya dikkat edin. Uygulama günlük toplamınızı UL ile karşılaştırır.',
          'The % on the label compares one serving with a reference value. Going over 100% is not always harmful, but stay well below the upper limit (UL). The app compares your daily total with the UL.')),
  InfoCard(L('Birimler', 'Units'),
      L('1 g = 1000 mg, 1 mg = 1000 mcg (µg). D vitamini için 1 mcg = 40 IU. Etiketteki birimi aynen girin.',
          '1 g = 1000 mg, 1 mg = 1000 mcg (µg). For vitamin D, 1 mcg = 40 IU. Enter the unit exactly as on the label.')),
  InfoCard(L('Eğilimleri takip edin', 'Follow the trend'),
      L('Tek bir günü kaçırmak önemli değildir; düzenlilik önemlidir. İstatistikler ekranı son 7 ve 30 günü gösterir.',
          'Missing a single day is not a big deal; consistency is. The Statistics screen shows the last 7 and 30 days.')),
  InfoCard(L('Önce besinler', 'Food first'),
      L('Takviyeler dengeli beslenmenin yerini tutmaz. Yüksek dozlu takviyelere karşı dikkatli olun.',
          'Supplements do not replace a balanced diet. Be careful with high-dose products.')),
  InfoCard(L('Ne zaman doktora danışmalı?', 'When to see a doctor'),
      L('Sürekli şikâyetleriniz varsa, gebe veya emziriyorsanız, kronik hastalığınız varsa ya da ilaç kullanıyorsanız yeni bir takviyeye başlamadan önce doktorunuza veya eczacınıza danışın.',
          'If you have persistent symptoms, are pregnant or breastfeeding, have a chronic condition or take medicines, talk to your doctor or pharmacist before starting a new supplement.')),
];

class SymptomInfo {
  const SymptomInfo(this.title, this.detail, this.nutrients);
  final L title;
  final L detail;
  final List<String> nutrients;
}

const symptomList = <SymptomInfo>[
  SymptomInfo(L('Yorgunluk ve düşük enerji', 'Fatigue and low energy'),
      L('Sürekli yorgunluk, solgunluk, odaklanma güçlüğü', 'Ongoing tiredness, pale skin, trouble concentrating'),
      ['iron', 'vitB12', 'folate', 'vitD', 'magnesium']),
  SymptomInfo(L('Kuru cilt ve gözler', 'Dry skin and eyes'),
      L('Ciltte pullanma, göz kuruluğu', 'Flaky skin, dry eyes'), ['vitA', 'omega3', 'vitE']),
  SymptomInfo(L('Diş eti kanaması, kolay morarma', 'Bleeding gums, easy bruising'),
      L('Fırçalarken kanama, yaraların yavaş iyileşmesi', 'Bleeding when brushing, slow wound healing'), ['vitC', 'vitK']),
  SymptomInfo(L('Kas krampları ve seğirmeler', 'Muscle cramps and twitches'),
      L('Gece bacak krampları, göz kapağı seğirmesi', 'Night-time leg cramps, eyelid twitching'), ['magnesium', 'potassium', 'calcium']),
  SymptomInfo(L('Kırılgan tırnak, saç dökülmesi', 'Brittle nails, hair loss'),
      L('Tırnakların kolay kırılması, saçlarda incelme', 'Nails that break easily, thinning hair'), ['iron', 'zinc', 'biotin']),
  SymptomInfo(L('Sık enfeksiyon', 'Frequent infections'),
      L('Sık soğuk algınlığı, geç iyileşme', 'Frequent colds, slow recovery'), ['vitD', 'vitC', 'zinc']),
  SymptomInfo(L('El ve ayaklarda karıncalanma', 'Tingling in hands and feet'),
      L('Uyuşma, iğnelenme hissi', 'Numbness, pins and needles'), ['vitB12', 'folate']),
];

enum TipEffect { increases, decreases }

class AbsorptionTip {
  const AbsorptionTip(this.effect, this.title, this.body, this.tip);
  final TipEffect effect;
  final L title;
  final L body;
  final L tip;
}

const absorptionTips = <AbsorptionTip>[
  AbsorptionTip(TipEffect.increases, L('C vitamini + bitkisel demir', 'Vitamin C + plant-based iron'),
      L('C vitamini, bitkisel gıdalardaki demirin emilimini artırabilir.', 'Vitamin C can increase absorption of iron from plant foods.'),
      L('Mercimek, fasulye veya yeşil yapraklılara biber, turunçgil ya da domates ekleyin.', 'Add peppers, citrus or tomatoes to lentils, beans or leafy greens.')),
  AbsorptionTip(TipEffect.increases, L('D vitamini + kalsiyum', 'Vitamin D + calcium'),
      L('Yeterli D vitamini bağırsakta kalsiyum emilimini destekler.', 'Enough vitamin D supports calcium absorption in the gut.'),
      L('Kalsiyumdan zengin besinleri günlük beslenmenize yayın.', 'Spread calcium-rich foods across your day.')),
  AbsorptionTip(TipEffect.increases, L('Yağda çözünen vitaminler + yemek', 'Fat-soluble vitamins + a meal'),
      L('A, D, E ve K vitaminleri yağ içeren bir öğünle daha iyi emilir.', 'Vitamins A, D, E and K are absorbed better with a meal that contains fat.'),
      L('Bu takviyeleri kahvaltı veya akşam yemeğiyle alın.', 'Take them with breakfast or dinner.')),
  AbsorptionTip(TipEffect.decreases, L('Kahve / çay + demir', 'Coffee / tea + iron'),
      L('Kahve ve çaydaki polifenoller demir emilimini azaltabilir.', 'Polyphenols in coffee and tea can reduce iron absorption.'),
      L('Demiri kahve ve çaydan en az 1 saat ayrı alın.', 'Take iron at least 1 hour apart from coffee and tea.')),
  AbsorptionTip(TipEffect.decreases, L('Kalsiyum / süt ürünleri + demir', 'Calcium / dairy + iron'),
      L('Kalsiyum, aynı anda alınan demirin emilimini azaltabilir.', 'Calcium can reduce absorption of iron taken at the same time.'),
      L('Demir ile kalsiyum takviyesini farklı saatlere koyun.', 'Schedule iron and calcium supplements at different times.')),
  AbsorptionTip(TipEffect.decreases, L('Yüksek doz çinko + bakır', 'High-dose zinc + copper'),
      L('Uzun süre yüksek doz çinko bakır emilimini azaltabilir.', 'Long-term high-dose zinc can reduce copper absorption.'),
      L('Üst sınırı (40 mg) aşmayın.', 'Stay below the upper limit (40 mg).')),
  AbsorptionTip(TipEffect.decreases, L('Kepek / tam tahıl + mineraller', 'Bran / whole grains + minerals'),
      L('Fitatlar demir, çinko ve kalsiyum emilimini azaltabilir.', 'Phytates can reduce absorption of iron, zinc and calcium.'),
      L('Mineral takviyelerini kepekli kahvaltıdan ayrı alabilirsiniz.', 'You can take mineral supplements apart from a high-bran breakfast.')),
  AbsorptionTip(TipEffect.decreases, L('Kalsiyum / demir + tiroid ilacı', 'Calcium / iron + thyroid medicine'),
      L('Kalsiyum ve demir, levotiroksin gibi tiroid ilaçlarının emilimini azaltabilir.', 'Calcium and iron can reduce absorption of thyroid medicines such as levothyroxine.'),
      L('Genellikle en az 4 saat ara önerilir; doktorunuza veya eczacınıza danışın.', 'A gap of at least 4 hours is usually advised; ask your doctor or pharmacist.')),
];

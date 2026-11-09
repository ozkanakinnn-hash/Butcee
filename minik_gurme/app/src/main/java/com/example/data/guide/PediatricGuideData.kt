package com.example.data.guide

data class MonthGuide(
    val month: Int,
    val label: String,
    val title: String,
    val summary: String,
    val goals: List<String>,
    val textures: List<String>,
    val recommendedFoods: List<String>,
    val warnings: List<String>,
    val sampleMealRoutine: String,
    val doctorNote: String = ""
)

data class StarterWeekPlan(
    val weekNumber: Int,
    val title: String,
    val subtitle: String,
    val focusFoods: List<FocusFood>,
    val dailyPlan: List<DailyMealIntro>,
    val keyGoals: List<String>,
    val cautions: List<String>
)

data class FocusFood(
    val name: String,
    val emoji: String,
    val prepTip: String,
    val benefits: String
)

data class DailyMealIntro(
    val dayNumber: Int,
    val dayName: String,
    val mealTitle: String,
    val description: String,
    val mealType: String = "Öğle"
)

data class AllergenInfo(
    val name: String,
    val icon: String,
    val introductionTip: String,
    val waitPeriod: String,
    val riskLevel: String,
    val howToPrepare: String = ""
)

data class SafetyTip(
    val foodName: String,
    val hazard: String,
    val safePreparation: String
)

data class GaggingVsChoking(
    val symptom: String,
    val gaggingFeature: String,
    val chokingFeature: String
)

data class CommonMistake(
    val title: String,
    val wrongAction: String,
    val correctAction: String,
    val icon: String
)

data class NutrientGuide(
    val nutrientName: String,
    val importance: String,
    val sources: List<String>,
    val absorptionTip: String
)

data class ReadinessItem(
    val title: String,
    val description: String,
    val isCrucial: Boolean = true
)

object PediatricGuideData {

    // 1. Ek Gıdaya Hazır Oluş Belirtileri
    val readinessChecklist = listOf(
        ReadinessItem(
            title = "Baş ve Gövde Kontrolü",
            description = "Destekli veya mama sandalyesinde dik oturabiliyor, başını dik tutabiliyor.",
            isCrucial = true
        ),
        ReadinessItem(
            title = "Yiyeceklere İlgi Gösterme",
            description = "Sofradaki yiyeceklere dikkatle bakıyor, uzanıp tutmaya çalışıyor.",
            isCrucial = true
        ),
        ReadinessItem(
            title = "Dil İtme Refleksinin Azalması",
            description = "Ağzına verilen kaşığı veya yumuşak gıdayı diliyle dışarı fırlatmak yerine ağız arkasına yönlendirip yutabiliyor.",
            isCrucial = true
        ),
        ReadinessItem(
            title = "Keşfetmeye İstekli ve Meraklı",
            description = "Yiyecekleri koklama, dokunma ve tatma konusunda hevesli davranıyor.",
            isCrucial = true
        ),
        ReadinessItem(
            title = "Açlık ve Tokluk Belirtileri",
            description = "Yiyeceği görünce ağzını açıyor; doyduğunda başını çevirip ilgisini kaybediyor.",
            isCrucial = false
        )
    )

    val readinessMythBusters = listOf(
        "❌ Diş çıkması ek gıdaya başlamak için şart DEĞİLDİR! Bebekler diş etleriyle püre ve yumuşak gıdaları rahatlıkla ezebilir.",
        "❌ Belli bir kilo hedefi yoktur; önemli olan bebeğin nörolojik ve sindirimsel olgunluğudur.",
        "❌ Sadece anne sütü veya formül mama alan bebeklerin 6 aydan önce ek gıdaya erken başlatılması sindirim ve alerji riskini artırır."
    )

    // 2. Ay Ay Pediatrik Rehber (Dr. Eda Tezgel rehberiyle tam uyumlu)
    val monthlyGuides = listOf(
        MonthGuide(
            month = 6,
            label = "6. Ay",
            title = "6. Ay: İlk Adımlar ve Tanışma",
            summary = "Tatlarla tanışma ve demirden zengin besinleri beslenmeye ekleme dönemi. Ek gıda anne sütü veya mamanın yerine geçmez; tamamlar.",
            goals = listOf(
                "Ek gıdaya yavaş ve keyifli bir başlangıç yapmak",
                "Demirden zengin besinlerle tanışmak (et, baklagil, yumurta)",
                "Farklı tat ve pürüzsüz dokulara alışmaya başlamak",
                "Açlık ve tokluk sinyallerine saygı göstermek"
            ),
            textures = listOf(
                "Yumuşak Ezme / Püre (Homojen, akışkan ama çok sulu olmayan)",
                "Çatalla Ezilmiş Yumuşak (Kademeli geçiş)"
            ),
            recommendedFoods = listOf(
                "İyi pişmiş dana kıyma / didiklenmiş kuzu eti",
                "İyi pişmiş yumurta sarısı (1/4 ile başlayarak)",
                "Sade tam yağlı pastörize ev yoğurdu",
                "Buharda kabak, havuç, tatlı patates, brokoli ezmesi",
                "Cam rendede elma / armut püresi, avokado ezmesi",
                "İyi haşlanmış kırmızı mercimek püresi"
            ),
            warnings = listOf(
                "12 aydan önce KESİNLİKLE BAL VERMEYİN (Bebek botulizmi hayati risk taşır)",
                "Yemeklere tuz, ilave şeker veya salça kesinlikle eklemeyin",
                "Meyve suyu yerine meyvenin lifli ezmesini tercih edin",
                "İnek sütü doğrudan içecek olarak verilmez (yoğurt/lor uygundur)"
            ),
            sampleMealRoutine = "Sabah: Anne sütü/mama -> Gün Ortası: 1-2 tatlı kaşığı kıymalı kabak ezmesi -> İkindi: Birkaç kaşık sade yoğurt -> Akşam: Anne sütü/mama",
            doctorNote = "Öğünler örnektir; miktar zorunlu değil, bebeğinizin isteği ve hazır oluşuna göre kademeli artırılmalıdır."
        ),
        MonthGuide(
            month = 7,
            label = "7. Ay",
            title = "7. Ay: Çeşitliliği Artıralım",
            summary = "Günde 2-3 tamamlayıcı öğüne doğru ilerlenir. Çatalla ezilmiş yumuşak dokular sunulur ve çeşitlilik yavaş yavaş artırılır.",
            goals = listOf(
                "Günde 2-3 tamamlayıcı öğüne ilerlemek",
                "Demirden zengin besinleri düzenli sürdürmek",
                "Çatalla ezilmiş pütürlü kıvama geçişi pekiştirmek",
                "Öğünlerle birlikte küçük yudumlarla su alıştırması"
            ),
            textures = listOf(
                "Çatalla Ezilmiş Yumuşak (Pütürlü doku)",
                "Pütürlü Ezme (Minik yumuşak tanecikler)"
            ),
            recommendedFoods = listOf(
                "Mercimek, nohut, kuru fasulye ezmeleri",
                "Didiklenmiş tavuk eti & kıymalı sebze ezmeleri",
                "İyi pişmiş yumurta + avokado ezmesi",
                "Mevsim meyveleri (şeftali, kayısı, muz)",
                "Bebek irmiği ve yulaf lapaları"
            ),
            warnings = listOf(
                "Her besini aynı gün vermek gerekmez, acele etmeyin",
                "Bebeği beslerken mutlaka dik oturtun ve gözetim altında tutun",
                "Tuz, şeker ve bal kısıtlaması kararlılıkla devam eder"
            ),
            sampleMealRoutine = "Sabah: Anne sütü/mama -> Kuşluk: Yoğurt + meyve ezmesi veya yumurta sarısı -> Öğle: Kıymalı sebze ezmesi -> İkindi: Anne sütü -> Akşam: Küçük yoğurt/sebze denemesi",
            doctorNote = "Açlık: Yiyeceğe uzanma, ağzını açma. Tokluk: Başını çevirme, ağzını kapatma, kaşığı itme. Tokluk sinyallerini zorlamayın!"
        ),
        MonthGuide(
            month = 8,
            label = "8. Ay",
            title = "8. Ay: Kıvamı İlerletelim & Parmak Gıdalar",
            summary = "Pütürlü kıvamlara tam alışma ve bebeğin kendi kendine beslenmesini (BLW) destekleme zamanı.",
            goals = listOf(
                "Pütürlü ezme ve yumuşak parmak gıdalara alışmak",
                "Bebeğin kendi kendine kavrayıp yemesine alan açmak",
                "Demir ve çinko kaynaklarını çeşitlendirerek sürdürmek",
                "Çiğneme ve dil-damak koordinasyonunu geliştirmek"
            ),
            textures = listOf(
                "Pütürlü Ezme (Daha yoğun, pütürlü)",
                "Yumuşak Parmak Gıdalar (İki parmak arasında ezilebilen güvenli lokmalar)"
            ),
            recommendedFoods = listOf(
                "İyi pişmiş sebze çubukları (havuç, brokoli, kabak)",
                "Yumuşak köfte içi (baharatsız, az soğanlı)",
                "Omlet şeritleri & avokado/muz dilimleri",
                "Kılçıksız taze beyaz etli balık (mezgit, somon)",
                "Ezilmiş baklagiller ve tam yağlı yoğurt"
            ),
            warnings = listOf(
                "Yuvarlak, sert ve kaygan gıdalardan kaçının (boğulma tehlikesi)",
                "Diş çıkmasa bile diş etleri bu yumuşak gıdaları rahatça ezer",
                "Yemek sırasında bebeğin yanında olun, araba/puset hareket halindeyken yedirmeyin"
            ),
            sampleMealRoutine = "Sabah: Anne sütü/mama -> Kahvaltı: İyi pişmiş yumurta + avokado ezmesi -> Öğle: Etli/tavuklu sebze ezmesi -> İkindi: Sade yoğurt + meyve -> Akşam: Anne sütü/mama",
            doctorNote = "Kıvam ilerlemesi bir yarış değildir; her bebek kıvama farklı hızda adapte olur."
        ),
        MonthGuide(
            month = 9,
            label = "9. Ay",
            title = "9. Ay: Sofra Benzeri Besinler & Çiğneme",
            summary = "Üç ana öğüne yaklaşılır. Ezmeden çok küçük yumuşak parçalı, çatalla ezilmiş gıdalar verilir.",
            goals = listOf(
                "Günde 3 ana öğün + gerekirse 1 küçük ara öğün",
                "Yumuşak parçalı sofra benzeri besinleri artırmak",
                "Açık bardakla su içme denemeleri",
                "Kendi kaşığını tutmasına fırsat tanımak"
            ),
            textures = listOf(
                "Küçük yumuşak parçalı, çatalla ezilmiş gıdalar",
                "Kendi kendine tutabileceği yumuşak lokmalar"
            ),
            recommendedFoods = listOf(
                "Peynirli omlet parçaları (domatessiz kahvaltı)",
                "Sebzeli kıymalı / tavuklu ev yemeği",
                "Mercimekli / bulgurlu yumuşak yemekler",
                "Meyveli yoğurt (muz, çilek, elma)",
                "Kılçıksız buğulama balık"
            ),
            warnings = listOf(
                "Tuz eklemeden aile tenceresinden bebeğe ayırın",
                "Öğünleri aileyle aynı masada sunarak sosyal yeme modelini destekleyin",
                "Dağınıklık normal ve gelişimsel açıdan gereklidir"
            ),
            sampleMealRoutine = "Sabah: Peynirli omlet parçaları -> Öğle: Sebzeli kıymalı ev yemeği -> İkindi: Yoğurt + meyve -> Akşam: Mercimekli/bulgurlu sebze yemeği",
            doctorNote = "Bebeğinize açık bardak sunun. Döke saça öğrenecektir, bardağı kavraması motor gelişimini hızlandırır."
        ),
        MonthGuide(
            month = 10,
            label = "10. Ay",
            title = "10. Ay: Sofra Beslenmesine Geçiş",
            summary = "Bebeği aile sofrasına yaklaştırmak, ev yemeklerini bebeğe uygun hale getirmek ve çeşitliliği pekiştirmek.",
            goals = listOf(
                "3 ana öğün + 1-2 ara deneme",
                "Aile sofrası yemeklerini tuzsuz/az baharatlı uyarlamak",
                "Kendi kendine beslenme bağımsızlığını desteklemek",
                "Farklı doku kombinasyonlarını keşfetmek"
            ),
            textures = listOf(
                "Yumuşak sofra yemeği parçaları",
                "Pilav, makarna, köfte gibi çatalla kolay ezilen yapılar"
            ),
            recommendedFoods = listOf(
                "Yumuşak fırın köfte parçaları",
                "İyi pişmiş sebze yemekleri",
                "Sebzeli bulgur veya pirinç pilavı",
                "Yumurta veya kılçıksız balık",
                "Ev yapımı kefir ve yoğurt"
            ),
            warnings = listOf(
                "Kızartma ve acı baharatlı yemeklerden kesinlikle kaçının",
                "İri ve sert parçaları lokma boyutuna küçültün",
                "Tuzsuz pişirip bebeğe ayırdıktan sonra ailenin yemeğine tuz ekleyin"
            ),
            sampleMealRoutine = "Sabah: Yumurta + peynir kahvaltı tabağı -> Öğle: Yumuşak köfte + buharda sebze -> İkindi: Yoğurt + meyve -> Akşam: Bulgurlu sebze tabağı",
            doctorNote = "Aile sofrasından besinleri bebeğinize uyarlayarak sunun. Birlikte yemek yemek iştahı olumlu etkiler."
        ),
        MonthGuide(
            month = 11,
            label = "11. Ay",
            title = "11. Ay: Bağımsız Beslenme ve Çeşitlilik",
            summary = "Bağımsız beslenmeyi desteklemek, çeşitli aile yemeklerini sunmak ve bebeğin işaretlerine duyarlı kalmak.",
            goals = listOf(
                "Kendi kaşığını tutma ve koordinasyon",
                "Aile sofrasının aktif bir üyesi olma",
                "Çeşitli sebze, bakliyat, tahıl ve et kombinasyonları",
                "İştah dalgalanmalarına karşı sabırlı olmak"
            ),
            textures = listOf(
                "Yumuşak parmak gıdalar ve aile sofrası dokuları"
            ),
            recommendedFoods = listOf(
                "Sebzeli tavuk ve etli ev yemekleri",
                "Sebzeli tahıllı öğünler (mercimek, bulgur, erişte)",
                "Mevsim meyveleriyle zenginleştirilmiş yoğurtlar",
                "Haşlanmış yumurta ve tam tahıllı bebek ekmeği"
            ),
            warnings = listOf(
                "İştah günlere göre değişebilir, zorlamadan keyifli bir sofra ortamı sağlayın",
                "Yemek masasında dikkat dağıtıcı ekran (TV/telefon) açmayın"
            ),
            sampleMealRoutine = "Sabah: Yumurta / peynir -> Öğle: Sebzeli tavuk yemeği -> İkindi: Yoğurt + meyve -> Akşam: Sebzeli bulgur pilavı & mercimek",
            doctorNote = "Küçük porsiyonlarla başlayın, tekrar tekrar sunun. Bir besini ilk seferde reddetse bile birkaç gün sonra farklı şekilde tekrar deneyin."
        ),
        MonthGuide(
            month = 12,
            label = "12. Ay+",
            title = "12. Ay+: Aile Sofrasına Tam Uyum",
            summary = "1 yaş ile birlikte beslenme tabularının çoğu kalkar. Çocuk aileyle aynı masada, aynı menüyü paylaşabilir.",
            goals = listOf(
                "3 Ana Öğün + 1-2 Ara Öğün",
                "Aile sofrasına tam katılım",
                "Bardakla su içmeyi pekiştirmek",
                "Sürdürülebilir ve dengeli aile beslenmesi"
            ),
            textures = listOf(
                "Aile sofrası kıvamında (az baharatlı, tuzsuz/az tuzlu)"
            ),
            recommendedFoods = listOf(
                "Tam yumurta (sarısı ve beyazı birlikte)",
                "Tüm ev yemekleri",
                "Peynir, yoğurt, kefir ve uygun miktarda tam yağlı inek sütü",
                "Bal (12 aydan sonra güvenle verilebilir)",
                "Tüm mevsim sebze, meyve ve baklagilleri"
            ),
            warnings = listOf(
                "12 aydan sonra bal serbesttir ancak tuz ve ilave şeker yine de minimize edilmelidir",
                "Bütün sert kuruyemişler 4-5 yaşına kadar boğulma riski nedeniyle bütün verilmemelidir (toz/ezme olarak sunulabilir)",
                "Şekerli ve gazlı içeceklerden kesinlikle kaçının"
            ),
            sampleMealRoutine = "Sabah: Klasik kahvaltı tabağı -> Öğle: Ev yemeği + yoğurt -> Ara: Meyve & yoğurt -> Akşam: Aile akşam yemeği uygun lokmalarda",
            doctorNote = "Amaç her öğünde mükemmellik değil; çeşitlilik, denge ve sürdürülebilir sağlıklı sofra alışkanlığıdır."
        )
    )

    // 3. 4 Haftalık 6. Ay Adım Adım Başlangıç Planı (Dr. Eda Tezgel infografiği)
    val fourWeekStarterPlan = listOf(
        StarterWeekPlan(
            weekNumber = 1,
            title = "1. Hafta: İlk Tatlar, Sebzeler ve Dana Eti",
            subtitle = "Yumuşak ve kolay sindirilen besinlerle başlıyoruz; özellikle demirden zengin gıdalara (dana eti) yer veriyoruz.",
            focusFoods = listOf(
                FocusFood("Kabak", "🥒", "İyi pişirilip püre kıvamında verilir.", "Ek gıdaya başlamak için hafif ve iyi tolere edilen bir sebzedir."),
                FocusFood("Brokoli", "🥦", "İyi pişirilip püre kıvamında sunulur.", "Vitamin ve mineralden zengin, lifli bir sebzedir."),
                FocusFood("Dana Eti", "🥩", "Yağsız taze dana eti iyice pişirilip püre haline getirilerek verilir.", "Bebeğiniz için en önemli demir ve çinko kaynaklarındandır.")
            ),
            dailyPlan = listOf(
                DailyMealIntro(1, "1. Gün (Pzt)", "Kabak Püresi (İlk deneme)", "1-2 tatlı kaşığı pürüzsüz kabak püresi"),
                DailyMealIntro(2, "2. Gün (Sal)", "Kabak Püresi (Tekrar)", "Miktar tolere edildikçe 2-3 tatlı kaşığına çıkarılabilir"),
                DailyMealIntro(3, "3. Gün (Çar)", "Brokoli Püresi (İlk deneme)", "1-2 tatlı kaşığı buharda pişmiş brokoli püresi"),
                DailyMealIntro(4, "4. Gün (Per)", "Brokoli Püresi (Tekrar)", "2-3 tatlı kaşığı brokoli püresi"),
                DailyMealIntro(5, "5. Gün (Cum)", "Dana Eti Püresi (İlk deneme)", "İyice haşlanıp püre edilmiş dana eti (1 tatlı kaşığı)"),
                DailyMealIntro(6, "6. Gün (Cmt)", "Dana Eti Püresi (Tekrar)", "Dana eti püresi sebzeyle karıştırılarak verilebilir"),
                DailyMealIntro(7, "7. Gün (Paz)", "Tolere Edilen Besinleri Tekrar Sun", "Kabak, brokoli veya et ile kombine 1 öğün")
            ),
            keyGoals = listOf(
                "Günde 1 küçük ek gıda denemesi yeterlidir",
                "Çok az miktarla (1-2 tatlı kaşığı) başlayın",
                "Kıvam: Pürüzsüz ezme veya püre olmalıdır",
                "Anne sütü veya formül süt ana besin olarak devam eder"
            ),
            cautions = listOf(
                "Tuz, şeker ve bal eklemeyin",
                "Her yeni besinden sonra 2-3 gün gözlemleyin",
                "Besinlerin sabit bir sırayla verilmesi zorunlu değildir; bu plan rehberdir"
            )
        ),
        StarterWeekPlan(
            weekNumber = 2,
            title = "2. Hafta: Yumurta Sarısı, Sade Yoğurt ve Mercimek",
            subtitle = "Demir ve protein açısından son derece değerli besinleri beslenmeye ekliyoruz.",
            focusFoods = listOf(
                FocusFood("Yumurta", "🥚", "İyi pişmiş (katı) yumurta sarısı ile başlayın. İlk denemede 1/4 sarı, tolere edildikçe artırın.", "Zengin demir, protein ve kolin kaynağı."),
                FocusFood("Sade Yoğurt", "🥛", "Şekersiz, katkısız, tam yağlı sade pastörize yoğurt (1-2 tatlı kaşığı ile başlayarak).", "Doğal kalsiyum ve probiyotik deposu."),
                FocusFood("Kırmızı Mercimek", "🍲", "İyi pişmiş ve pürüzsüz mercimek püresi (1-2 tatlı kaşığı ile başlayarak).", "Bitkisel demir ve lif kaynağı.")
            ),
            dailyPlan = listOf(
                DailyMealIntro(8, "8. Gün (Pzt)", "Yumurta Sarısı (1/4)", "Sabah 1/4 iyi pişmiş yumurta sarısı + Öğle havuç püresi"),
                DailyMealIntro(9, "9. Gün (Sal)", "Yumurta Sarısı (1/2)", "Sabah 1/2 yumurta sarısı + Öğle kabak püresi"),
                DailyMealIntro(10, "10. Gün (Çar)", "Sade Yoğurt (İlk deneme)", "1-2 tatlı kaşığı sade yoğurt + Kırmızı mercimek püresi"),
                DailyMealIntro(11, "11. Gün (Per)", "Sade Yoğurt & Sebzeli Mercimek", "2-3 tatlı kaşığı yoğurt + Mercimek kabak ezmesi"),
                DailyMealIntro(12, "12. Gün (Cum)", "Yumurta Sarısı (1/2 - 1 tam)", "Kahvaltıda yumurta sarısı + Patates mercimek püresi"),
                DailyMealIntro(13, "13. Gün (Cmt)", "Sade Yoğurt & Sebzeli Mercimek", "Sebzeli mercimek püresi (kabak+havuç) + 3-4 tatlı kaşığı yoğurt"),
                DailyMealIntro(14, "14. Gün (Paz)", "Tolere Edilen Besin Karışımı", "Sebze + mercimek + yumurta ve yoğurt ile besleyici gün")
            ),
            keyGoals = listOf(
                "Yumurta, yoğurt ve mercimeği güvenli şekilde tanıtmak",
                "Miktarı küçük başlayıp tolere edildikçe kademeli artırmak",
                "Farklı sebzelerle çeşitlendirmek"
            ),
            cautions = listOf(
                "Yumurta sarısı MUTLAKA tam pişmiş (katı) olmalıdır (çiğ/kayısı olmamalı)",
                "Yoğurt katkısız ve şekersiz olmalıdır",
                "Yeni bir besini tek başına verip alerji yönünden gözlemleyin"
            )
        ),
        StarterWeekPlan(
            weekNumber = 3,
            title = "3. Hafta: Alerjenlerin Güvenli Tanıtımı",
            subtitle = "Yer fıstığı, balık ve buğdayı tek tek ve kontrollü olarak tanıştırıyoruz.",
            focusFoods = listOf(
                FocusFood("Yer Fıstığı Ezmesi", "🥜", "Pürüzsüz, şekersiz, tuzsuz ezme. 1/4 çay kaşığı su veya anne sütü ile açılarak püreye karıştırılır.", "Erken tanıtım fıstık alerjisi riskini azaltır."),
                FocusFood("Balık", "🐟", "Beyaz etli, az cıvalı balıklar (mezgit, levrek, çipura, somon). İyi pişmiş, kemiksiz, püre şeklinde.", "Zengin Omega-3 ve kaliteli protein."),
                FocusFood("Buğday / İrmik", "🌾", "Tam buğday unu muhallebisi, bebek irmiği, yulaf veya iyi pişmiş makarna.", "Enerji, B vitaminleri ve lif desteği.")
            ),
            dailyPlan = listOf(
                DailyMealIntro(15, "15. Gün", "Yer Fıstığı Tanıtımı (Çok az)", "1/4 çay kaşığı seyreltilmiş fıstık ezmesi (meyve/yoğurda karıştırılarak)"),
                DailyMealIntro(16, "16. Gün", "Yer Fıstığı Miktarını Artır", "1/2 - 1 çay kaşığı fıstık ezmesi (gözlemleyin)"),
                DailyMealIntro(17, "17. Gün", "Balık Tanıtımı (İlk deneme)", "1-2 tatlı kaşığı kılçıksız somon/mezgit püresi"),
                DailyMealIntro(18, "18. Gün", "Balık Miktarını Artır", "2-3 tatlı kaşığı balık sebze ezmesi"),
                DailyMealIntro(19, "19. Gün", "Buğday / İrmik Tanıtımı", "İrmikli/yulaflı meyve püresi"),
                DailyMealIntro(20, "20. Gün", "Buğday Miktarını Artır", "2-3 tatlı kaşığı tahıllı sebze çorbası"),
                DailyMealIntro(21, "21. Gün", "Tolere Edilen Besin Tekrarı", "Bebeğin en sevdiği kıvamda güvenli gıda sunumu")
            ),
            keyGoals = listOf(
                "Alerjenleri tek tek ve küçük miktarla sunmak",
                "Yeni besinden sonra 2-3 gün gözlemlemek",
                "Aynı gün birden fazla yeni alerjen denememek"
            ),
            cautions = listOf(
                "Döküntü, dudak-göz şişliği, kusma, hırıltı olursa hekiminize başvurun",
                "Ağır egzaması veya yumurta alerjisi olan bebeklerde fıstık tanıtımını çocuk doktorunuzla planlayın"
            )
        ),
        StarterWeekPlan(
            weekNumber = 4,
            title = "4. Hafta: Çeşitlilik, Kıvam İlerlemesi ve Birleştirme",
            subtitle = "Önceki haftalarda tanıştırdığımız besinleri birleştirerek daha çeşitli, biraz daha kıvamlı öğünler hazırlıyoruz.",
            focusFoods = listOf(
                FocusFood("Çeşitlilik", "🥗", "Sebze, meyve, tahıl, baklagil, et/tavuk/balık, yumurta ve yoğurdu dengeli kullanın.", "Besin öğesi zenginliği sağlar."),
                FocusFood("Kıvam İlerlemesi", "🥣", "Pürüzsüz pürelerden pütürlü ezmelere, çatalla ezilmiş parçalara geçin.", "Çiğneme refleksini geliştirir."),
                FocusFood("Doku Deneyimi (BLW)", "🥕", "Yumuşak haşlanmış havuç/muz dilimleri ile kendi kendine yeme fırsatı verin.", "El-göz koordinasyonunu destekler.")
            ),
            dailyPlan = listOf(
                DailyMealIntro(22, "22. Gün", "Muz Püresi + Yoğurt", "Öğle: Kırmızı mercimek çorbası (az taneli)"),
                DailyMealIntro(23, "23. Gün", "Elma-Armut Yulaf Lapası", "Öğle: Kabak + patates + tavuk püresi"),
                DailyMealIntro(24, "24. Gün", "Yoğurt + Şeftali Püresi", "Öğle: Kılçıksız balık + havuç + pirinç püresi"),
                DailyMealIntro(25, "25. Gün", "Muz + Yulaf Lapası", "Öğle: Ispanaklı kıymalı sebze püresi (iyice pişmiş)"),
                DailyMealIntro(26, "26. Gün", "Armut Püresi + Yoğurt", "Öğle: Nohut püresi + havuç (pürüzlü püre)"),
                DailyMealIntro(27, "27. Gün", "Haşlanmış Elma Dilimleri (Parmak Gıda)", "Öğle: Brokoli + patates + yumurta ezmesi"),
                DailyMealIntro(28, "28. Gün", "Muz & Avokado Ezmesi", "Öğle: Sebzeli bulgur pilavı (pütürlü)")
            ),
            keyGoals = listOf(
                "Besin çeşitliliğini artırmak",
                "Kıvamı biraz daha pütürlü hale getirmek",
                "Kendi yemesi için yumuşak parmak gıdalara fırsat tanımak"
            ),
            cautions = listOf(
                "Tuz, şeker ve bal kuralına uymaya devam edin",
                "Alerjik reaksiyon bulgularını (döküntü, kusma, ishal) izleyin"
            )
        )
    )

    // 4. Öğürme (Gagging) vs. Boğulma (Choking) Farkı (Hayati Güvenlik Bilgisi)
    val gaggingVsChokingList = listOf(
        GaggingVsChoking(
            symptom = "Ses Durumu",
            gaggingFeature = "SESLİDİR. Bebek öksürür, öğürür, 'hıh' sesi çıkarır.",
            chokingFeature = "SESSİZDİR! Ağlayamaz, öksüremez, ses çıkaramaz."
        ),
        GaggingVsChoking(
            symptom = "Hava Akışı ve Solunum",
            gaggingFeature = "Hava yolu açıktır, nefes almaya devam eder.",
            chokingFeature = "Hava yolu tamamen veya kısmen tıkalıdır, hava alamaz."
        ),
        GaggingVsChoking(
            symptom = "Yüz Rengi",
            gaggingFeature = "Yüzü kızarabilir, gözleri sulanabilir.",
            chokingFeature = "Yüzü morumsu, grimsi veya soluk renge dönebilir."
        ),
        GaggingVsChoking(
            symptom = "Bebeğin Hareketi",
            gaggingFeature = "Dilini öne çıkarıp besini dışarı itmeye çalışır (Doğal savunma refleksi).",
            chokingFeature = "Çırpınabilir, gözlerinde panik ifadesi vardır, hareketsizleşebilir."
        ),
        GaggingVsChoking(
            symptom = "Ne Yapılmalı?",
            gaggingFeature = "Sakin kalın! Panikle ağzına parmak sokmayın. Bebeğe zaman tanıyın, kendi kendine toparlar.",
            chokingFeature = "DERHAL ACİL MÜDAHALE! Bebeği dizinize yatırıp sırt vuruşu ve göğüs basısı uygulayın, 112'yi arayın."
        )
    )

    // 5. Ek Gıdada En Sık Yapılan 6 Hata (Dr. Eda Tezgel infografiği)
    val commonMistakesList = listOf(
        CommonMistake(
            title = "1. Zorla Yedirmek",
            wrongAction = "'Bir kaşık daha!' diyerek doyduğu halde zorlamak, kaşığı peşinden koşturmak.",
            correctAction = "Bebeğin açlık ve tokluk işaretlerine saygı gösterin. Kafasını çevirdiğinde ısrar etmeyin.",
            icon = "🥄"
        ),
        CommonMistake(
            title = "2. Aylarca Sadece Püre Vermek",
            wrongAction = "Hep pürüzsüz, blenderize edilmiş tek kıvamda takılı kalmak.",
            correctAction = "Bebeğin gelişimine göre 7-8. aylarda çatalla ezilmiş pütürlü ve yumuşak parmak gıdalara geçin.",
            icon = "🥣"
        ),
        CommonMistake(
            title = "3. Demiri Unutmak",
            wrongAction = "Sadece meyve ve sebze püreleriyle ilerleyip protein/demiri geciktirmek.",
            correctAction = "Erken dönemden itibaren et, kıyma, tavuk, yumurta ve baklagil gibi demir kaynaklarına yer verin.",
            icon = "🥩"
        ),
        CommonMistake(
            title = "4. Gereksiz Alerjen Yasağı",
            wrongAction = "Alerji korkusuyla tüm alerjenleri 1 yaş sonrasına ertelemek.",
            correctAction = "Alerjenleri zamanında (~6. aydan itibaren), tek tek ve 3 gün kuralı ile tanıtın.",
            icon = "🥚"
        ),
        CommonMistake(
            title = "5. Uygunsuz Kesim Yapmak",
            wrongAction = "Bütün üzüm, kiraz domates, sert havuç veya kuruyemiş gibi boğulma riski yüksek gıdaları olduğu gibi sunmak.",
            correctAction = "Üzüm ve kiraz domatesi uzunlamasına 4'e bölün, sert sebzeleri buharda yumuşatın.",
            icon = "🔪"
        ),
        CommonMistake(
            title = "6. Tuz, Şeker ve Bal Eklemek",
            wrongAction = "'Tatsız olmasın' diyerek tuz veya şeker eklemek, tatlandırmak için bal sürmek.",
            correctAction = "12 aydan önce bal KESİNLİKLE YASAKTIR. Tuz böbrekleri yorar, şeker tat alışkanlığını bozar.",
            icon = "🧂"
        )
    )

    // 6. Demir, Protein ve Kalsiyum Rehberi
    val nutrientGuides = listOf(
        NutrientGuide(
            nutrientName = "Demir (Fe)",
            importance = "Bebeklerin beyin gelişimi, bilişsel fonksiyonlar ve bağışıklık sistemi için ilk yılda en kritik mineraldir.",
            sources = listOf("İyi pişmiş kırmızı et / kıyma", "Yumurta sarısı", "Kırmızı/yeşil mercimek ve nohut", "Koyu yeşil yapraklı sebzeler", "Demirle zenginleştirilmiş tahıllar"),
            absorptionTip = "💡 Bitkisel demir kaynaklarını (mercimek, nohut) C vitamini içeren gıdalarla (portakal, mandalina, brokoli, kırmızı biber) birlikte sunarak emilimi 3-4 kat artırabilirsiniz!"
        ),
        NutrientGuide(
            nutrientName = "Protein",
            importance = "Hücre büyümesi, kas gelişimi ve güçlü bağışıklık yapısı için temel yapı taşıdır.",
            sources = listOf("Dana ve kuzu eti", "Tavuk but eti", "Az cıvalı beyaz balıklar (somon, levrek)", "Tam yumurta", "Sade yoğurt ve lor peyniri", "Kuru baklagiller"),
            absorptionTip = "💡 Tek bir mucize besin yoktur; asıl hedef protein kaynaklarını haftalık olarak dönüştürerek dengeli çeşitlilik sağlamaktır."
        ),
        NutrientGuide(
            nutrientName = "Kalsiyum",
            importance = "Bebeğin kemik yoğunluğu, diş gelişimi ve sinir sistemi fonksiyonlarını destekler.",
            sources = listOf("Tam yağlı sade yoğurt", "Tuzsuz lor peyniri & labne", "Kefir", "Tahin (küçük miktarda püreye)", "Yeşil sebzeler (ıspanak, brokoli)"),
            absorptionTip = "💡 Mayalanmış süt ürünleri (yoğurt, kefir) sindirimi kolaylaştırır ve bağırsak mikrobiyotasını zenginleştirir."
        )
    )

    // 7. En Yaygın Alerjenler ve Hazırlık Rehberi
    val topAllergens = listOf(
        AllergenInfo(
            name = "Yumurta (Sarısı & Beyazı)",
            icon = "🥚",
            introductionTip = "İyi pişmiş katı yumurta sarısı ile (1/4 çeyrek) başlayın. 8. aydan itibaren beyazı da iyi pişmiş olarak ekleyin.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Yüksek",
            howToPrepare = "En az 10-12 dakika tam haşlanmış olmalıdır. Çiğ, cıvık veya kayısı kıvamda asla vermeyin."
        ),
        AllergenInfo(
            name = "Yer Fıstığı Ezmesi",
            icon = "🥜",
            introductionTip = "Erken dönemde (~6. ay) pürüzsüz ve şekersiz ezme halinde anne sütü/suyla açılarak meyve püresine karıştırılmalıdır.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Yüksek",
            howToPrepare = "Asla bütün veya parça fıstık vermeyin (boğulma tehlikesi). 1/4 çay kaşığı cıvıtılmış ezme ile başlayın."
        ),
        AllergenInfo(
            name = "İnek Sütü & Süt Ürünleri",
            icon = "🥛",
            introductionTip = "Sıvı inek sütü 12 aydan önce ana içecek olamaz. Ancak pastörize sade yoğurt ve tuzsuz lor peyniri 6. aydan itibaren serbesttir.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Yüksek",
            howToPrepare = "Şekersiz, katkısız, pastörize tam yağlı yoğurt veya ev yoğurdu olarak birkaç tatlı kaşığı sunun."
        ),
        AllergenInfo(
            name = "Balık & Deniz Ürünleri",
            icon = "🐟",
            introductionTip = "Az cıvalı taze somon, levrek, mezgit gibi beyaz ve yağlı balıklar iyi pişmiş ve kılçıkları didiklenmiş sunulmalıdır.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Orta-Yüksek",
            howToPrepare = "Buharda veya fırında az zeytinyağı ile pişirin. Parmaklarınızla tüm kılçıkları kontrol edip ezin."
        ),
        AllergenInfo(
            name = "Buğday / Gluten",
            icon = "🌾",
            introductionTip = "Tam buğday unu muhallebisi, bebek irmiği veya bebek yulaf ezmesi ile 6. aydan itibaren tek başına tanıştırılır.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Orta",
            howToPrepare = "İyice pişirilmiş muhallebi veya çorba kıvamında 1-2 tatlı kaşığı ile başlayın."
        ),
        AllergenInfo(
            name = "Susam & Tahin",
            icon = "🌱",
            introductionTip = "Tahin şeklinde, meyve püresine veya yoğurda çay kaşığının ucuyla karıştırarak tadım yaptırın.",
            waitPeriod = "3 Gün Kuralı",
            riskLevel = "Orta",
            howToPrepare = "Katkısız çifte kavrulmamış sade tahini yoğurt veya püre içine 1/4 çay kaşığı ekleyin."
        )
    )

    // 8. Boğulma Riski ve Güvenli Hazırlama Rehberi
    val safetyTips = listOf(
        SafetyTip(
            foodName = "Bütün Üzüm & Kiraz Domates",
            hazard = "Yuvarlak ve kaygan yapısı soluk borusuna tam oturarak tam tıkanmaya yol açar.",
            safePreparation = "Asla bütün vermeyin! Daima uzunlamasına ikiye, ardından tekrar dörde bölerek sunun."
        ),
        SafetyTip(
            foodName = "Sert Havuç & Çiğ Elma",
            hazard = "Sert parçalar ısırıldığında kopup hava yoluna kaçabilir.",
            safePreparation = "Buharda parmaklarınızla ezilecek yumuşaklığa gelene kadar haşlayın veya ince cam rendede rendeleyin."
        ),
        SafetyTip(
            foodName = "Bütün Kuruyemişler (Ceviz, Fındık, Badem)",
            hazard = "Sert ve kuru taneler 4-5 yaşına kadar en sık rastlanan boğulma sebebidir.",
            safePreparation = "Asla tane olarak vermeyin. Toz haline getirip yoğurda serpin veya pürüzsüz ezme halinde inceltin."
        ),
        SafetyTip(
            foodName = "Sosis, Salam ve Yuvarlak Etler",
            hazard = "Tekerlek şeklinde kesilen yuvarlak silindirik parçalar soluk borusunu vakum gibi kapatır.",
            safePreparation = "Tekerlek şeklinde kesmeyin! Daima uzunlamasına şeritler halinde ve yumuşak liflerine ayırarak sunun."
        ),
        SafetyTip(
            foodName = "Kaşık Dolusu Yoğun Fıstık Ezmesi",
            hazard = "Yapışkan yoğun kıvam damak ve boğaza yapışarak solunumu engelleyebilir.",
            safePreparation = "Kaşık dolusu yoğun vermeyin! Yoğurt, anne sütü veya meyve püresi ile cıvıtarak ince tabaka sürün."
        )
    )
}

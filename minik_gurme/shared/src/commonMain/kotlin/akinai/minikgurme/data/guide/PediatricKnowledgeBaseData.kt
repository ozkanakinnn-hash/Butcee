package akinai.minikgurme.data.guide

data class WeeklyPediatricGuide(
    val id: String,
    val month: Int,
    val weekNumber: Int,
    val title: String,
    val subtitle: String,
    val ageLabel: String,
    val developmentalGoal: String,
    val textureMilestone: String,
    val dailyRoutine: String,
    val pediatricNotes: String,
    val weeklyFoods: List<KnowledgeBaseFoodItem>,
    val goldenRules: List<String>,
    val safetyAndAlerts: List<String>,
    val faqs: List<KnowledgeBaseFAQ>,
    val medicalSource: String = "WHO 2023 Infant Feeding Guidelines & ESPGHAN Complementary Feeding Committee Raporu"
)

data class KnowledgeBaseFoodItem(
    val name: String,
    val category: String,
    val emoji: String,
    val prepInstructions: String,
    val portionGuide: String,
    val pediatricBenefit: String,
    val allergenLevel: String
)

data class KnowledgeBaseFAQ(
    val question: String,
    val answer: String,
    val expertBadge: String = "Pediatrik Gastroenteroloji & Beslenme"
)

object PediatricKnowledgeBaseData {

    val allWeeklyGuides: List<WeeklyPediatricGuide> = listOf(
        // ==========================================
        // 6. AY (EK GIDAYA İLK ADIM & TANIŞMA)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_6_w1",
            month = 6,
            weekNumber = 1,
            title = "6. Ay 1. Hafta: İlk Kaşık & Kök Sebzeler",
            subtitle = "Tek bileşenli püreler, dil kontrolü ve 3 gün kuralı ile ek gıdaya güvenli başlangıç",
            ageLabel = "6. Ay • 1. Hafta (24-25. Hafta)",
            developmentalGoal = "Dil itme refleksinin gerilemesini gözlemlemek, kaşıktan beslenmeyi öğrenmek ve tekil sebze aromalarına alışmak.",
            textureMilestone = "İpeksi, akışkan ve tamamen pürüzsüz püre (anne sütü veya formül mama ile kıvamı açılmış).",
            dailyRoutine = "Sabah & Ara: Anne sütü / Formül mama\nÖğle (11:00-12:00): 1-2 çay kaşığı havuç veya kabak püresi + ardından anne sütü/mama\nAkşam & Gece: Anne sütü / Formül mama",
            pediatricNotes = "Ek gıdanın ilk haftasında amaç bebeğin doyması değil, tat ve dokuyla tanışmasıdır. Bebeğin ana besin kaynağı %90 oranında anne sütü veya formül mamadır. ASLA zorlanmamalıdır.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Balkabağı",
                    category = "Sebze",
                    emoji = "🎃",
                    prepInstructions = "Buharda 15 dk yumuşayana kadar pişirip çatalla ezin veya blenderdan geçirip anne sütüyle inceltin.",
                    portionGuide = "İlk gün 1 çay kaşığı, 2. gün 1 tatlı kaşığı, 3. gün 2 tatlı kaşığı.",
                    pediatricBenefit = "A vitamini ve beta-karoten zengini, sindirimi son derece kolay ve mideyi yormaz.",
                    allergenLevel = "Çok Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Havuç",
                    category = "Sebze",
                    emoji = "🥕",
                    prepInstructions = "İyice yıkayıp kabuğunu soyun. Buharda yumuşacık olana kadar pişirin ve ipeksi püre yapın.",
                    portionGuide = "3 gün kuralına göre 1-2 tatlı kaşığı.",
                    pediatricBenefit = "Göz ve cilt sağlığı için A vitamini deposudur, doğal tatlı aromasıyla bebekler kolay benimser.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Bal Kabağı & Havuç Karışımı",
                    category = "Kombinasyon",
                    emoji = "🥣",
                    prepInstructions = "İkisi de ayrı ayrı 3 gün denendikten sonra birlikte buharda pişirilip püre yapılabilir.",
                    portionGuide = "2-3 tatlı kaşığı.",
                    pediatricBenefit = "Zengin lif ve vitamin kombinasyonu sağlar.",
                    allergenLevel = "Test Edilmiş"
                )
            ),
            goldenRules = listOf(
                "Tuz, şeker, bal, salça ve baharat KESİNLİKLE eklenmez.",
                "Her yeni besin sabah veya öğle saatlerinde verilerek olası alerjik reaksiyonlar gün ışığında takip edilir.",
                "Bebek tok veya aşırı aç değilken, keyifli bir anında ilk kaşık teklif edilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Mama sandalyesinde mutlaka 90 derece dik oturtulmalıdır, yatar pozisyonda ASLA beslenmez.",
                "Ciltte kızarıklık, dudak çevresinde şişme, kusma veya ishal durumunda besin kesilir ve hekime danışılır."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğim ilk kaşığı diliyle dışarı itiyor, ek gıdaya hazır değil mi?",
                    answer = "Bu durum 'dil itme refleksi'nin henüz tamamen sönmediğini gösterebilir. 3-4 gün ara verip tekrar nazikçe deneyin, zorlamayın."
                ),
                KnowledgeBaseFAQ(
                    question = "Sebze pürelerine zeytinyağı ne zaman eklenmeli?",
                    answer = "İlk 1-2 haftada tekil sebze tadını alması için sade verilebilir. 2. haftadan itibaren kabızlığı önlemek ve yağda eriyen vitaminleri emmek için 1 çay kaşığı soğuk sıkım sızma zeytinyağı eklenebilir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_6_w2",
            month = 6,
            weekNumber = 2,
            title = "6. Ay 2. Hafta: Yeşil Sebzeler & İlk Sağlıklı Yağlar",
            subtitle = "Brokoli, tatlı kabak ve zeytinyağı ile sindirim sistemini destekleme",
            ageLabel = "6. Ay • 2. Hafta (25-26. Hafta)",
            developmentalGoal = "Tatlı olmayan hafif buruk yeşil sebze aromalarına alışmak ve bağırsak mikrobiyotasını lifle tanıştırmak.",
            textureMilestone = "Pürüzsüz püre, ancak ilk haftaya göre bir tık daha kıvamlı.",
            dailyRoutine = "Sabah: Anne sütü/mama\nÖğle (12:00): 2-3 tatlı kaşığı brokoli veya sakız kabağı püresi + 1 çay kaşığı sızma zeytinyağı\nÖğleden sonra & Gece: Anne sütü/mama",
            pediatricNotes = "Brokoli gibi gaz yapma potansiyeli olan sebzeleri pişirirken kimyon tohumu ile buğulayabilir ya da az miktarda başlayabilirsiniz.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Sakız Kabağı",
                    category = "Sebze",
                    emoji = "🥒",
                    prepInstructions = "Çekirdeksiz kısımlarını buharda pişirin, çatalla ezip zeytinyağı gezdirin.",
                    portionGuide = "2-4 tatlı kaşığı.",
                    pediatricBenefit = "Yüksek su ve lif içeriği sayesinde kabızlığı önler, çok hafif ve hipoalerjeniktir.",
                    allergenLevel = "Çok Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Brokoli Çiçekleri",
                    category = "Sebze",
                    emoji = "🥦",
                    prepInstructions = "Saplarını ayırıp yalnızca yumuşak tepe çiçeklerini buharda 10 dk haşlayın ve püreleyin.",
                    portionGuide = "1-2 tatlı kaşığı.",
                    pediatricBenefit = "C vitamini, folat, demir ve kalsiyum açısından en zengin sebzelerdendir.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Soğuk Sıkım Sızma Zeytinyağı",
                    category = "Sağlıklı Yağ",
                    emoji = "🫒",
                    prepInstructions = "Yemek pişerken değil, piştikten sonra pürenin üzerine çiğ olarak 1 çay kaşığı eklenir.",
                    portionGuide = "Günde 1 çay kaşığı.",
                    pediatricBenefit = "Beyin gelişimi için elzem esansiyel yağ asitleri içerir, bağırsak hareketlerini yumuşatır.",
                    allergenLevel = "Hipoalerjenik"
                )
            ),
            goldenRules = listOf(
                "Yeşil sebzeleri bebeğiniz ilk denemede buruşturarak tepki verebilir, bu normaldir; pes etmeyin, birkaç gün sonra tekrar deneyin.",
                "Zeytinyağı mutlaka asit oranı %0.8 altı soğuk sıkım sızma tercih edilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Gaz sancısı yaşanırsa brokoli miktarı azaltılıp sakız kabağına ağırlık verilmelidir.",
                "Püreye kesinlikle tuz veya bulyon katılmamalıdır; bebeğin böbrekleri tuzu süzemez."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğim yeşil sebzeleri sevmedi, elma püresiyle karıştırayım mı?",
                    answer = "İlk etapta sebzelerin kendi doğal tadına alışması tercih edilir. Eğer çok direnirse tatlı patates veya havuçla karıştırarak yumuşatabilirsiniz."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_6_w3",
            month = 6,
            weekNumber = 3,
            title = "6. Ay 3. Hafta: Buharda Meyveler & C Vitamini",
            subtitle = "Elma, armut ve avokado ile vitamin çeşitliliği",
            ageLabel = "6. Ay • 3. Hafta (26-27. Hafta)",
            developmentalGoal = "Doğal meyve şekerini tanıma, C vitamini ile demir emilim zeminini hazırlama.",
            textureMilestone = "Buharda yumuşatılmış pürüzsüz meyve püresi veya ezilmiş olgun avokado.",
            dailyRoutine = "Sabah: Anne sütü/mama\nAra Öğün (10:30): 1-2 tatlı kaşığı buharda armut veya elma püresi\nÖğle (13:00): Sebze püresi (havuç + kabak + zeytinyağı)\nAkşam: Anne sütü/mama",
            pediatricNotes = "Meyveler ilk haftalarda çiğ yerine buharda hafif pişirilerek verilirse mide asidi ve sindirim rahatlığı açısından daha tolere edilebilir olur.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Amasya / Tatlı Elma",
                    category = "Meyve",
                    emoji = "🍎",
                    prepInstructions = "Kabuğunu soyup cam rendede rendeleyin veya buharda 8 dk yumuşatıp ezin.",
                    portionGuide = "2-3 tatlı kaşığı.",
                    pediatricBenefit = "Pektin lifi bağırsak florasını besler, C vitamini bağışıklığı güçlendirir.",
                    allergenLevel = "Çok Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Deveci / Santa Maria Armut",
                    category = "Meyve",
                    emoji = "🍐",
                    prepInstructions = "Olgun armudu cam rende ile püreleyin. Sertse buharda buğulayın.",
                    portionGuide = "2-3 tatlı kaşığı.",
                    pediatricBenefit = "Kabızlık için en etkili doğal çözümdür, su oranı çok yüksektir.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Hass Avokado",
                    category = "Meyve / Sağlıklı Yağ",
                    emoji = "🥑",
                    prepInstructions = "Olgunlaşmış yumuşak avokadoyu çatalla ezin, birkaç damla anne sütüyle pürüzsüzleştirin.",
                    portionGuide = "1 tatlı kaşığı.",
                    pediatricBenefit = "Anne sütüne yakın sağlıklı tekli doymamış yağlar ve folat deposudur.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Meyve suyu ASLA verilmemelidir; lifi alınmış meyve suyu kan şekerini aniden fırlatır ve obezite riskini artırır.",
                "Meyveleri tatlı ödül olarak değil, dengeli beslenmenin bir parçası olarak ikindi veya sabah ara öğününde sunun."
            ),
            safetyAndAlerts = listOf(
                "Sert meyve parçaları boğulma riski yaratır; armut ve elma mutlaka yumuşatılmalıdır."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Cam rende kullanmak gerçekten şart mı?",
                    answer = "Cam rende meyvenin vitamin değerlerinin oksitlenmeden korunmasını sağlar ve püre kıvamını bebek için ideal pütürde tutar. Metal bıçak yerine cam rende önerilir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_6_w4",
            month = 6,
            weekNumber = 4,
            title = "6. Ay 4. Hafta: Tahıllar, Yoğurt & 1. Ay Değerlendirmesi",
            subtitle = "Glutensiz yulaf, pirinç unu ve ev yapımı probiyotik yoğurt",
            ageLabel = "6. Ay • 4. Hafta (27-28. Hafta)",
            developmentalGoal = "Tahılların doyuruculuğu ve ev yapımı yoğurdun probiyotik gücüyle besin profilini zenginleştirmek.",
            textureMilestone = "Hafif pütürlü muhallebi kıvamı ve kremsi ev yoğurdu dokusu.",
            dailyRoutine = "Sabah: Anne sütü/mama\nÖğle: Sebze püresi + zeytinyağı\nİkindi (15:30): 2 tatlı kaşığı ev yoğurdu veya yulaf lapası\nAkşam: Anne sütü/mama",
            pediatricNotes = "İnek sütü 1 yaşına kadar içecek olarak yasaktır; ancak fermente edilmiş yoğurt ve peynir laktoz ve protein yapısı değiştiği için 6. aydan itibaren güvenle verilebilir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Ev Yapımı Probiyotik Yoğurt",
                    category = "Süt Ürünü",
                    emoji = "🥛",
                    prepInstructions = "Pastörize günlük sütten probiyotik maya ile mayalanmış taze yoğurdun tatlı kısmından verilir.",
                    portionGuide = "2-4 tatlı kaşığı.",
                    pediatricBenefit = "Kalsiyum, B12 vitamini ve bağırsak dostu probiyotik bakteriler sunar.",
                    allergenLevel = "Süt Alerjeni Gözlemi (3 Gün Kuralı)"
                ),
                KnowledgeBaseFoodItem(
                    name = "Organik İnce Öğütülmüş Yulaf Unu",
                    category = "Tahıl",
                    emoji = "🌾",
                    prepInstructions = "1 yemek kaşığı yulaf ununu 1 çay bardağı su ile kısık ateşte 5 dk pişirin, ılıyınca anne sütü veya elma püresiyle tatlandırın.",
                    portionGuide = "2-3 yemek kaşığı.",
                    pediatricBenefit = "Beta-glukan lifi, B kompleks vitaminleri ve bitkisel demir sağlar.",
                    allergenLevel = "Düşük Risk (Gluten Kontrolü)"
                )
            ),
            goldenRules = listOf(
                "Yoğurt ilk kez verilirken mutlaka 3 gün kuralı uygulanmalı; ciltte döküntü veya mukuslu kaka takip edilmelidir.",
                "Tahıllar rafine beyaz un yerine tam tahıl (yulaf, pirinç unu) olarak seçilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Marketten alınan şekerli veya meyveli bebek yoğurtları KESİNLİKLE kullanılmamalıdır."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğim ek gıdaya başlayınca kabız oldu, ne yapmalıyım?",
                    answer = "Ek gıdaya başlandığında bağırsakların yeni besinleri tanıması birkaç hafta sürebilir. Pürelerine zeytinyağı, haşlanmış armut veya kuru erik püresi ekleyin ve her öğün sonrası birkaç yudum kaynatılmış ılıtılmış su teklif edin."
                )
            )
        ),

        // ==========================================
        // 7. AY (PÜTÜRLÜYE GEÇİŞ & İLK PROTEİNLER)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_7_w1",
            month = 7,
            weekNumber = 1,
            title = "7. Ay 1. Hafta: Pütürlüye Geçiş & Çatalla Ezme",
            subtitle = "Blenderı bırakma zamanı! Çiğneme refleksini tetikleyen hafif pütürlü dokular",
            ageLabel = "7. Ay • 1. Hafta (28-29. Hafta)",
            developmentalGoal = "Öğürme (gag) refleksi ile boğulma arasındaki farkı anlamak ve bebeğin dilini damağına bastırarak pütürleri ezmesini sağlamak.",
            textureMilestone = "Çatalla ezilmiş, mercimek tanesi büyüklüğünde yumuşak pütürler içeren dokular.",
            dailyRoutine = "Sabah: Anne sütü/mama\nKuşluk: 2 tatlı kaşığı ev yoğurdu\nÖğle (12:30): Çatalla ezilmiş patates & kabak püresi + zeytinyağı\nAkşam: Anne sütü/mama",
            pediatricNotes = "7-9. aylar arasında pütürlü gıdaya geçilmezse, bebek ileriki yaşlarda çiğneme tembelliği ve yemek seçme sorunları yaşayabilir. Blender kullanımını bu hafta kademeli olarak sonlandırın.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Buharda Patates & Havuç (Çatalla Ezilmiş)",
                    category = "Sebze",
                    emoji = "🥔",
                    prepInstructions = "Sebzeleri çok iyi haşlayıp blender yerine çatalla ezin. Taneleri bezelyeden küçük olsun.",
                    portionGuide = "3-4 yemek kaşığı.",
                    pediatricBenefit = "Kompleks karbonhidrat ve potasyum ile enerji sağlar.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Kuru Erik Püresi",
                    category = "Meyve",
                    emoji = "🫐",
                    prepInstructions = "Güneşte kurutulmuş gün kurusu eriği sıcak suda 20 dk bekletip çatalla püreleyin.",
                    portionGuide = "1-2 tatlı kaşığı.",
                    pediatricBenefit = "Pütürlüye geçişte artan kabızlık riskini ortadan kaldırır, zengin demir içerir.",
                    allergenLevel = "Çok Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Bebek pütürlü gıda yerken öğürürse panik yapmayın; bu boğulma değil, dilin güvenlik mekanizmasıdır.",
                "Pütürlü yemek yerken bebeğin dikkatini dağıtacak ekran (TV/tablet) ASLA açılmamalıdır."
            ),
            safetyAndAlerts = listOf(
                "Sessiz kalma, morarma ve nefes alamama BOĞULMA belirtisidir; acil ilk yardım gerektirir. Öğürme ise seslidir ve bebek kendisi besini öne iter."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğimin henüz dişi yok, pütürlü yiyebilir mi?",
                    answer = "Evet! Bebekler dişleriyle değil, son derece güçlü olan çene kemikleri ve sert damaklarıyla yumuşak pütürleri rahatlıkla ezebilirler."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_7_w2",
            month = 7,
            weekNumber = 2,
            title = "7. Ay 2. Hafta: Yumurta Sarısı & Alerjen Yönetimi",
            subtitle = "Beyin gelişimi için kolin deposu: 1/8 dilimden tam yumurta sarısına",
            ageLabel = "7. Ay • 2. Hafta (29-30. Hafta)",
            developmentalGoal = "Yumurta sarısındaki kolin, lutein ve demir ile beyin ve sinir sistemi gelişimini desteklemek.",
            textureMilestone = "İyi haşlanmış yumurta sarısı anne sütü, yoğurt veya zeytinyağı ile ezilerek verilir.",
            dailyRoutine = "Sabah (Kahvaltı Başlangıcı): 1/4 haşlanmış yumurta sarısı + 1 çay kaşığı labne + anne sütü/mama\nÖğle: Sebze püresi\nAkşam: Anne sütü/mama",
            pediatricNotes = "Yumurta beyazı 1 yaşına kadar alerjenik proteinler nedeniyle önerilmez. Sarısı ise tam pişmiş (katı) olmalı; cıvık veya rafadan Salmonella riski nedeniyle ASLA verilmemelidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Organik Köy Yumurtası Sarısı (Katı Haşlanmış)",
                    category = "Protein",
                    emoji = "🍳",
                    prepInstructions = "Kaynar suda 10-12 dakika tam katılaşana kadar haşlayın. Sarısını ayırıp 1 çay kaşığı zeytinyağı ile ezin.",
                    portionGuide = "1. gün 1/8 dilim, 2. gün 1/4 dilim, 3. gün 1/2 dilim, hafta sonu tam sarı.",
                    pediatricBenefit = "Beyin gelişiminin en kritik besini kolin, demir, D vitamini ve çinko içerir.",
                    allergenLevel = "Yüksek Alerjen Potansiyeli (Gözlem Şart)"
                ),
                KnowledgeBaseFoodItem(
                    name = "Tuzsuz Bebek Labnesi",
                    category = "Süt Ürünü",
                    emoji = "🧀",
                    prepInstructions = "Kaynatılmış sütten evde mayalanmış tuzsuz taze labne.",
                    portionGuide = "1 tatlı kaşığı.",
                    pediatricBenefit = "Yumuşak dokusuyla yumurta sarısını yutmayı kolaylaştırır, kalsiyum sağlar.",
                    allergenLevel = "Süt Alerjeni Takibi"
                )
            ),
            goldenRules = listOf(
                "Yumurta sarısı denerken başka hiçbir yeni besin menüye sokulmaz.",
                "Yumurta sarısı sabah kahvaltısında verilmeli, gün boyunca egzama, kızarıklık veya kusma gözlenmelidir."
            ),
            safetyAndAlerts = listOf(
                "Yumurta sarısının etrafındaki yeşil halka aşırı pişme göstergesidir; sindirimi zorlaştırabilir, 10-12 dk idealdir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Yumurta sarısı boğazına yapışıyor, nasıl vermeliyim?",
                    answer = "Kuru şekilde tek başına vermeyin. Anne sütü, formül mama, zeytinyağı veya 1 tatlı kaşığı yoğurtla karıştırarak kremsi macun kıvamına getirin."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_7_w3",
            month = 7,
            weekNumber = 3,
            title = "7. Ay 3. Hafta: İlk Kıymalı Çorba & Hayvansal Demir",
            subtitle = "Azalan demir depolarını takviye etme: İki kez çekilmiş kuzu kıyma",
            ageLabel = "7. Ay • 3. Hafta (30-31. Hafta)",
            developmentalGoal = "Doğumda gelen ve 6. aydan sonra tükenen demir depolarını hayvansal hem demir ile desteklemek.",
            textureMilestone = "İki kez çekilmiş yağsız kıymayla pişmiş, çatalla ezilmiş sebze çorbası.",
            dailyRoutine = "Sabah: Yumurta sarısı + labne peyniri\nÖğle: Kıymalı sebze çorbası (1 tatlı kaşığı kıyma ile pişmiş)\nİkindi: Meyve püresi veya yoğurt\nAkşam: Anne sütü/mama",
            pediatricNotes = "Bebeklerin 6-12 ay arasında demir ihtiyacı erişkin bir erkekten daha fazladır. Kuzu kıyma, sindirimi danaya göre daha kolay ve yumuşak olduğu için ilk tercihtir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Çift Çekilmiş Kuzu Kıyma",
                    category = "Et & Protein",
                    emoji = "🥩",
                    prepInstructions = "Yağsız kuzu but veya koldan iki kez çektirin. Sebze çorbasına 1 ceviz büyüklüğünde ekleyip iyice pişirin.",
                    portionGuide = "1 yemek kaşığı (yaklaşık 20-30 gr).",
                    pediatricBenefit = "En yüksek biyoyararlanımlı hem demir, B12 vitamini ve çinko kaynağıdır.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Balkabaklı Kıymalı İlikli Çorba",
                    category = "Çorba",
                    emoji = "🍲",
                    prepInstructions = "Balkabağı, havuç, kıyma ve az su ile pişirip çatalla ezin.",
                    portionGuide = "1 küçük kase (100-120 ml).",
                    pediatricBenefit = "Kolajen ve demir dengesi sağlar.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Demir emilimini azaltmaması için etli öğünlerin hemen öncesinde veya sonrasında yoğurt verilmemelidir; yanında C vitamini içeren brokoli/havuç tercih edilir.",
                "Kıymayı topaklanmaması için tencereye soğuk su ile karıştırarak ilave edin."
            ),
            safetyAndAlerts = listOf(
                "İşlenmiş etler (sosis, salam, sucuk) sodyum ve nitrit içeriği nedeniyle bebek beslenmesinde KESİNLİKLE yasaktır."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Kırmızı et yerine tavuk veya hindi ile başlayabilir miyim?",
                    answer = "Organik hindi kıyması veya tavuk but da uygundur; ancak kırmızı et demir içeriği bakımından beyaz etten yaklaşık 3-4 kat daha zengindir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_7_w4",
            month = 7,
            weekNumber = 4,
            title = "7. Ay 4. Hafta: Düzenli İki Öğün Rutini",
            subtitle = "Kahvaltı ve öğle yemeği dengesi, su içme alışkanlığı",
            ageLabel = "7. Ay • 4. Hafta (31-32. Hafta)",
            developmentalGoal = "Günlük beslenme ritmi oluşturmak, aile sofrası saatlerine uyum sağlamak.",
            textureMilestone = "Yumuşak pütürlü sebze yemekleri, püre kıvamında kahvaltılıklar.",
            dailyRoutine = "08:30 Sabah Kahvaltısı: Yumurta sarısı, labne, zeytinyağlı yulaf ezmesi\n12:30 Öğle Yemeği: Kıymalı sebze çorbası / patates yemeği\n16:00 İkindi: Ev yoğurdu + cam rendede armut\nGece: Anne sütü/mama",
            pediatricNotes = "7. ayın sonunda bebek artık günde 2 ana ek gıda öğününe rahatlıkla uyum sağlayabilir. Her ek gıda sonrası 30-50 ml kaynatılmış ılıtılmış su teklif edilmelidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Bebek İrmiği",
                    category = "Tahıl",
                    emoji = "🌾",
                    prepInstructions = "1 tatlı kaşığı bebek irmiğini 1 çay bardağı suda 5 dk haşlayıp meyve püresine ekleyin.",
                    portionGuide = "2-3 yemek kaşığı.",
                    pediatricBenefit = "Kolay sindirilir, tokluk sağlar, mineral zenginidir.",
                    allergenLevel = "Gluten İçerir"
                )
            ),
            goldenRules = listOf(
                "Yemek saatleri mümkün olduğunca her gün aynı saat aralığında olmalıdır.",
                "Porsiyon miktarı bebeğin iştahına göre ayarlanmalı, kaseyi bitirmesi için baskı kurulmamalıdır."
            ),
            safetyAndAlerts = listOf(
                "Su içerken mutlaka dik oturtulmalı, alıştırma bardağı veya açık küçük bardak tercih edilmelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Günde ne kadar su içmeli?",
                    answer = "7. ayda ek gıda öğünlerinin ardından günde toplam yaklaşık 100-150 ml su yeterlidir. Anne sütü alan bebekler su ihtiyacının çoğunu sütten karşılar."
                )
            )
        ),

        // ==========================================
        // 8. AY (BLW, PARMAK GIDALAR & BAKLAGİLLER)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_8_w1",
            month = 8,
            weekNumber = 1,
            title = "8. Ay 1. Hafta: BLW & Yumuşak Parmak Gıdalar",
            subtitle = "Kendi kendine beslenme (Baby-Led Weaning) ve kavrama becerileri",
            ageLabel = "8. Ay • 1. Hafta (32-33. Hafta)",
            developmentalGoal = "Avuç içi kavrama (palmar grasp) ile yiyeceği eline alıp ağzına götürme ve el-göz-ağız koordinasyonu.",
            textureMilestone = "Yetişkinin baş ve işaret parmağı arasında kolayca ezilebilecek yumuşaklıkta, işaret parmağı boyunda dilimler.",
            dailyRoutine = "Kahvaltı: Yumurta sarısı + parmak şeklinde muz veya avokado dilimi\nÖğle: Kıymalı sebze yemeği + bebeğin eline buharda havuç çubuğu\nİkindi: Yoğurt\nAkşam: Anne sütü/mama",
            pediatricNotes = "BLW yaklaşımı bebekte özgüven ve iştah regülasyonu kazandırır. Başlangıçta besinlerin çoğu yere dökülebilir; bu bir oyun değil, çok önemli bir duyusal öğrenme sürecidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Buharda Havuç & Kabak Çubukları",
                    category = "Parmak Gıda",
                    emoji = "🥕",
                    prepInstructions = "İşaret parmağı kalınlığında doğrayıp buharda iyice yumuşayana kadar (parmakla ezilecek kıvamda) pişirin.",
                    portionGuide = "Bebeğin tepsisine 2-3 çubuk bırakın.",
                    pediatricBenefit = "Çiğneme kaslarını güçlendirir ve ince motor becerilerini tetikler.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Avokado Dilimleri (Ruşeym veya İrmik Kaplı)",
                    category = "Parmak Gıda",
                    emoji = "🥑",
                    prepInstructions = "Kaygan olmaması için avokado dilimlerini hafifçe buharda pişmiş irmik veya yulaf kepeğine bulayın.",
                    portionGuide = "2-3 dilim.",
                    pediatricBenefit = "Kolay kavranır, sağlıklı yağlar sağlar.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Parmak gıdalar verilirken bebek ASLA tek başına bırakılmaz; göz teması mesafesinde kalınmalıdır.",
                "Yiyecekler yuvarlak (fındık, üzüm gibi) değil, mutlaka uzunlamasına şeritler halinde kesilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Çiğ elma, çiğ havuç, bütün ceviz ve sosis en sık boğulmaya yol açan tehlikeli gıdalardır; 8. ayda KESİNLİKLE çiğ sert gıda verilmez."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebek parmak gıdayı ağzına çok büyük parça olarak atarsa ne yapmalıyım?",
                    answer = "Ağzına parmağınızı sokup zorla çıkarmaya çalışmayın; bu besini daha arkaya itip boğulmaya yol açabilir. Dik oturtup hafifçe öne eğilmesini destekleyin; öğürme refleksiyle kendisi tükürecektir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_8_w2",
            month = 8,
            weekNumber = 2,
            title = "8. Ay 2. Hafta: Baklagillerle Tanışma & Bitkisel Protein",
            subtitle = "Kırmızı ve sarı mercimek ile lif ve demir takviyesi",
            ageLabel = "8. Ay • 2. Hafta (33-34. Hafta)",
            developmentalGoal = "Bitkisel protein ve çözünür liflerle bağırsak florasını zenginleştirmek, gaz yapmayan baklagil pişirme teknikleri.",
            textureMilestone = "Pütürlü, yoğun kıvamlı mercimek çorbası.",
            dailyRoutine = "Kahvaltı: Yumurtalı omlet lokmaları + labne\nÖğle: Kırmızı mercimekli sebze çorbası + 1 tatlı kaşığı zeytinyağı\nİkindi: Meyveli yoğurt\nAkşam: Anne sütü/mama",
            pediatricNotes = "Baklagiller bebeklerde gaz yapmaması için pişirilmeden önce mutlaka en az 8-12 saat suda bekletilmeli ve ilk köpüğü alınmalıdır. Kabuksuz kırmızı mercimek başlangıç için idealdir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Kabuksuz Kırmızı Mercimek",
                    category = "Baklagil",
                    emoji = "🥣",
                    prepInstructions = "Suda bekletip yıkayın. Havuç ve patatesle kaynatıp çatalla ezerek yoğun çorba kıvamına getirin.",
                    portionGuide = "1 küçük kase (100 ml).",
                    pediatricBenefit = "Bitkisel demir, folik asit, çinko ve yüksek lif deposudur.",
                    allergenLevel = "Düşük Risk (Gaz Gözlemi)"
                )
            ),
            goldenRules = listOf(
                "Mercimek çorbasına kimyon tohumu veya bir tutam nane eklemek gaz sancısını önler.",
                "Demir emilimini artırmak için mercimek çorbasının içine birkaç damla limon suyu sıkılabilir."
            ),
            safetyAndAlerts = listOf(
                "Kuru fasulye ve nohut gibi kalın kabuklu baklagiller 9. aydan önce kabuğu soyulmadan verilmemelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Baklagiller bebeğe gaz yapar mı?",
                    answer = "İyi ıslatılıp pişirildiğinde kırmızı mercimek nadiren gaz yapar. Öğle öğününde verilirse akşam uykusuna kadar gaz tolere edilmiş olur."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_8_w3",
            month = 8,
            weekNumber = 3,
            title = "8. Ay 3. Hafta: Balık & Omega-3 Tanıtımı",
            subtitle = "Beyin ve göz gelişimi için haftada 1-2 kez kılçıksız taze balık",
            ageLabel = "8. Ay • 3. Hafta (34-35. Hafta)",
            developmentalGoal = "DHA ve EPA yağ asitleriyle bilişsel gelişimi desteklemek, balık proteini alerji gözlemi.",
            textureMilestone = "Buharda veya fırında pişmiş, parmaklarla kılçığı titizlikle kontrol edilip didiklenmiş balık eti.",
            dailyRoutine = "Kahvaltı: Yumurta sarısı + lor peyniri\nÖğle: Buharda pişmiş somon/levrek didiği + fırın patates\nİkindi: Yoğurt\nAkşam: Anne sütü/mama",
            pediatricNotes = "Bebekler için derin deniz dip balıkları (midye, kalkan, kılıç, mezgit) ağır metal ve cıva riski nedeniyle yasaktır. Somon, levrek, çipura gibi yüzey balıkları tercih edilmelidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Norveç / Karadeniz Somonu",
                    category = "Balık & Deniz Ürünü",
                    emoji = "🐟",
                    prepInstructions = "Buharda veya fırında yağlı kağıtta 15 dk pişirin. Parmaklarınızla 3 kez kılçık kontrolü yapıp didikleyin.",
                    portionGuide = "1-2 yemek kaşığı (30-40 gr).",
                    pediatricBenefit = "Beyin gelişiminin yapı taşı olan DHA Omega-3, D vitamini ve kaliteli protein kaynağıdır.",
                    allergenLevel = "Alerjenik Gıda (3 Gün Kuralı)"
                )
            ),
            goldenRules = listOf(
                "Balık mutlaka taze olmalı, aynı gün tüketilmelidir; dondurulup tekrar çözdürülmüş balık bebek için uygun değildir.",
                "Balık ilk denendiğinde mutlaka 3 gün kuralı ile takip edilir."
            ),
            safetyAndAlerts = listOf(
                "KILÇIK KONTROLÜ EN KRİTİK ADIMDIR; çatal yerine mutlaka parmak uçlarıyla hissederek kontrol edilmelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Balıkla yoğurt aynı gün yenir mi?",
                    answer = "Balık taze olduğu sürece yoğurtla birlikte tüketilmesinde hiçbir tıbbi sakınca yoktur; zehirlenme efsanesi bayat balıktaki histamin artışından kaynaklanır.",
                    expertBadge = "Pediatrik Toksikoloji"
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_8_w4",
            month = 8,
            weekNumber = 4,
            title = "8. Ay 4. Hafta: Kahvaltı Çeşitliliği & Lor Peyniri",
            subtitle = "Tuzsuz ev yapımı lor, yulaf ezmesi ve zenginleştirilmiş sabah menüsü",
            ageLabel = "8. Ay • 4. Hafta (35-36. Hafta)",
            developmentalGoal = "Sabah kahvaltısını bir aile ritüeli haline getirmek, kalsiyum ve protein dengesini kurmak.",
            textureMilestone = "Yumuşak lokmalar, çatalla ezilmiş lor ve parmak gıdalar.",
            dailyRoutine = "08:30 Kahvaltı: 1 tam yumurta sarısı + 1 yemek kaşığı lor peyniri + 1 çay kaşığı pekmez + zeytinyağı\n12:30 Öğle: Kıymalı mercimek yemeği\n16:00 İkindi: Meyveli yoğurt\n20:00 Gece: Anne sütü/mama",
            pediatricNotes = "Pekmez kalsiyum içeren süt ürünleriyle karıştırılırsa demir emilimi azalır. Pekmez verilecekse meyve püresine veya yulafa eklenmesi daha uygundur.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Tuzsuz Ev Loru",
                    category = "Süt Ürünü",
                    emoji = "🧀",
                    prepInstructions = "Süt kaynarken içine birkaç damla limon veya yoğurt suyu ekleyip kestirin; tülbentte süzün.",
                    portionGuide = "1-2 yemek kaşığı.",
                    pediatricBenefit = "Kazein proteini ve kalsiyum açısından son derece zengin, sindirimi çok kolaydır.",
                    allergenLevel = "Süt Ürünü"
                )
            ),
            goldenRules = listOf(
                "Market peynirleri suda bekletilse dahi bebek böbrekleri için fazla tuzlu olabilir; ev yapımı lor veya bebek labnesi seçilmelidir.",
                "Kahvaltı tabağı renkli tutularak bebeğin ilgisi canlı tutulmalıdır."
            ),
            safetyAndAlerts = listOf(
                "Sert ve yapışkan gıdalar lokma halinde verilmemelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğime pekmez verebilir miyim?",
                    answer = "Soğuk sıkım organik dut veya keçiboynuzu özü 8. aydan sonra günde en fazla 1 çay kaşığı olarak C vitamini içeren gıdalarla (örneğin portakal/mandalina suyu eklenmiş yulaf) verilebilir."
                )
            )
        ),

        // ==========================================
        // 9. AY (PİNCER GRASP & AİLE SOFRASI)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_9_w1",
            month = 9,
            weekNumber = 1,
            title = "9. Ay 1. Hafta: Pincer Grasp (Cımbız Kavrama) & Minik Lokmalar",
            subtitle = "Baş ve işaret parmağıyla küçük yiyecekleri tutma miladına geçiş",
            ageLabel = "9. Ay • 1. Hafta (36-37. Hafta)",
            developmentalGoal = "İnce motor becerisi olan cımbız kavrama (pincer grasp) ile bezelye büyüklüğündeki yumuşak gıdaları kendi başına yakalama.",
            textureMilestone = "Yumuşak nohut veya bezelye büyüklüğünde lokmalar, iyi pişmiş makarna parçaları.",
            dailyRoutine = "Kahvaltı: Yumurta sarısı + lor peyniri + tam buğday ekmek içi lokmaları\nÖğle: Minik köfteli sebze yemeği\nİkindi: Yoğurt + şeftali küpleri\nAkşam: Aile sofrasında hafif sebze çorbası",
            pediatricNotes = "9. ayda bebek artık sofrada aktif bir katılımcıdır. Kendi tabağı ve vantuzlu mama kasesi ile önüne konan lokmaları parmaklarıyla almasına izin verilmelidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Buharda Pişmiş Bezelye & Havuç Küpleri",
                    category = "Parmak Gıda",
                    emoji = "🥕",
                    prepInstructions = "Bezelye büyüklüğünde doğrayıp buharda yumuşayana kadar haşlayın. Hafifçe parmakla ezerek tepsiye koyun.",
                    portionGuide = "1 avuç içi kadar.",
                    pediatricBenefit = "İnce motor koordinasyonu ve göz-el uyumunu geliştirir.",
                    allergenLevel = "Düşük Risk"
                ),
                KnowledgeBaseFoodItem(
                    name = "Tam Buğday Bebek Makarnası",
                    category = "Tahıl",
                    emoji = "🍝",
                    prepInstructions = "Burgu veya burgu makarna şeklinde iyice yumuşayana kadar haşlayıp zeytinyağı ile harmanlayın.",
                    portionGuide = "2-3 yemek kaşığı.",
                    pediatricBenefit = "Kolay kavranır, çiğneme kaslarını çalıştırır.",
                    allergenLevel = "Gluten İçerir"
                )
            ),
            goldenRules = listOf(
                "Lokmaların boğulma riski yaratmaması için sert olmamasına dikkat edilmeli; dudak arasında ezilebilmelidir.",
                "Yere atılan yiyeceklere kızılmamalı, bu süreç sabırla yönetilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Bütün üzüm, kiraz, zeytin gibi yuvarlak gıdalar KESİNLİKLE dörde bölünerek verilmelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Yemeğin çoğunu yere atıyor, karnı doyuyor mu?",
                    answer = "Bu ayda bebeklerin keşfetme arzusu çok yüksektir. Siz kaşıkla ana öğünü desteklerken, önüne kendi yiyebileceği lokmalar koyarak hem doymasını hem de bağımsızlaşmasını sağlayabilirsiniz."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_9_w2",
            month = 9,
            weekNumber = 2,
            title = "9. Ay 2. Hafta: Aile Sofrasına Entegrasyon",
            subtitle = "Tuzsuz ve baharatsız ev yemeklerine geçiş: Birlikte yemek yeme psikolojisi",
            ageLabel = "9. Ay • 2. Hafta (37-38. Hafta)",
            developmentalGoal = "Ailenin yediği yemekleri bebeğe uyarlayarak sofra kültürünü ve sosyal beslenmeyi pekiştirmek.",
            textureMilestone = "Çatalla ezilmiş tencere yemekleri, pirinçli/bulgurlu sulu yemekler.",
            dailyRoutine = "Kahvaltı: Aile kahvaltısı (yumurta, peynir, ekmek içi, salatalık içi)\nÖğle: Kıymalı kabak kalye veya taze fasulye\nİkindi: Kefir veya yoğurt\nAkşam: Aile sofrasında mercimek çorbası veya bulgur pilavı",
            pediatricNotes = "Tüm ailenin tükettiği yemek pişerken tuz ve salça eklenmeden önce bebeğin porsiyonu ayrılmalı; ailenin kalan yemeğine tuz sonradan ilave edilmelidir.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Kıymalı Taze Fasulye (Tuzsuz)",
                    category = "Tencere Yemeği",
                    emoji = "🍲",
                    prepInstructions = "Kılçıksız taze fasulye, domates rendesi (alerji testi yapılmışsa) ve kıyma ile zeytinyağında pişirin; çatalla ezin.",
                    portionGuide = "3-4 yemek kaşığı.",
                    pediatricBenefit = "Lif ve potasyum dengesi sağlar, aile sofrasına alıştırır.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Bebeğin tabağı yetişkin tabağıyla benzer görünmeli, aile bireylerinin yediğini görmesi iştahını artırır.",
                "Yemek sırasında telefon, video veya oyuncakla dikkat dağıtılmamalıdır."
            ),
            safetyAndAlerts = listOf(
                "Acı biber, karabiber ve aşırı baharatlar mide mukozasını tahriş edebilir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Salça kullanabilir miyim?",
                    answer = "Market salçaları yüksek tuz ve koruyucu içerir. Ev yapımı güneşte kurutulmuş tuzsuz domates veya kapya biber püresi 9. aydan sonra az miktarda lezzetlendirme için kullanılabilir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_9_w3",
            month = 9,
            weekNumber = 3,
            title = "9. Ay 3. Hafta: 3 Ana Öğün Rutinine Geçiş",
            subtitle = "Kahvaltı, Öğle ve Akşam Yemeği Düzeni",
            ageLabel = "9. Ay • 3. Hafta (38-39. Hafta)",
            developmentalGoal = "Yetişkin öğün ritmine yaklaşmak, gece beslenmesini kademeli olarak azaltmak.",
            textureMilestone = "Pütürlü yemekler, köfteler ve yoğurtlu garnitürler.",
            dailyRoutine = "08:30 Kahvaltı: Tam yumurtalı omlet + lor + zeytin ezmesi (tuzsuz)\n12:30 Öğle: Kıymalı sebze yemeği + bulgur\n16:00 Ara: Meyveli yoğurt veya kefir\n19:00 Akşam: Çorba + fırın sebze\nGece: Anne sütü/mama",
            pediatricNotes = "9. ayın sonunda ek gıdalar bebeğin kalori ihtiyacının yaklaşık %50'sini karşılamaya başlar. Geri kalan %50 hala anne sütü veya formül mamadır.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Bebek Köftesi (Fırında)",
                    category = "Et & Protein",
                    emoji = "🧆",
                    prepInstructions = "Kuzu kıyma, rendelenmiş soğan, ekmek içi ve kimyon ile yoğurup fırında 15 dk kurutmadan pişirin.",
                    portionGuide = "1-2 küçük köfte.",
                    pediatricBenefit = "Yüksek kaliteli protein, hem demir ve çinko.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Akşam yemeği yatmadan en az 1.5 - 2 saat önce bitirilmelidir; reflü ve uyku bölünmeleri önlenir.",
                "Porsiyonlar küçük tutulmalı, bebek istedikçe ilave edilmelidir."
            ),
            safetyAndAlerts = listOf(
                "Köfteler çok kuru olmamalıdır; kuru köfte yutulurken boğaza takılabilir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Kefir ne zaman ve nasıl başlanmalı?",
                    answer = "Kefir güçlü bir probiyotiktir. 8-9. aylardan itibaren günde 2-3 yemek kaşığı ile başlanabilir, sindirimi rahatlatır ve bağışıklığı güçlendirir."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_9_w4",
            month = 9,
            weekNumber = 4,
            title = "9. Ay 4. Hafta: Demir Emilimi ve Besin Eşleşmeleri",
            subtitle = "Etin yanına C vitamini, yoğurdun ayrılması: Pediatrik beslenme biyokimyası",
            ageLabel = "9. Ay • 4. Hafta (39-40. Hafta)",
            developmentalGoal = "Demir eksikliği anemisini önlemek için akılcı besin kombinasyonlarını öğrenmek.",
            textureMilestone = "Çatalla ezilmiş tencere yemekleri, yumuşak parmak köfteler.",
            dailyRoutine = "Kahvaltı: Yumurta sarısı + kapya biber püresi (C vitamini desteği)\nÖğle: Kıymalı mercimek yemeği (yoğurtsuz, limonlu)\nİkindi: Ev yoğurdu veya kefir (kalsiyum öğünü ayrı)\nAkşam: Sebzeli somon balığı",
            pediatricNotes = "Demir minerali kalsiyum ile yarışır; bu nedenle kıymalı veya etli yemeklerin yanında yoğurt yerine C vitamini zengini brokoli, havuç veya biber verilmesi demir emilimini %300 artırır.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Fırınlanmış Kapya Biber Püresi",
                    category = "Sebze",
                    emoji = "🫑",
                    prepInstructions = "Fırında közleyip kabuğunu soyun, çatalla ezip zeytinyağı ile etli yemeklerin yanına sos yapın.",
                    portionGuide = "1-2 yemek kaşığı.",
                    pediatricBenefit = "Limon ve portakaldan daha yüksek C vitamini içerir, demir emilimini katlar.",
                    allergenLevel = "Düşük Risk"
                )
            ),
            goldenRules = listOf(
                "Kırmızı et veya baklagil öğünlerinin yanında yoğurt veya peynir vermeyin; kalsiyumu ikindi ara öğününe kaydırın.",
                "Yemeklere taze sıkılmış birkaç damla limon suyu ekleyin."
            ),
            safetyAndAlerts = listOf(
                "Demir takviyesi damlası kullanılıyorsa süt veya mama ile aynı anda verilmemelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Bebeğimin demir seviyesi yeterli mi, nasıl anlarım?",
                    answer = "Genellikle 9. veya 12. ay rutin kontrolünde çocuk hekimi tam kan sayımı ve ferritin testi yapar. Yorgunluk, iştahsızlık veya ciltte solukluk demir eksikliği işareti olabilir."
                )
            )
        ),

        // ==========================================
        // 10-11. AY (ÇİĞNEME & ÇEŞİTLİLİK)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_10_w1",
            month = 10,
            weekNumber = 1,
            title = "10. Ay 1. Hafta: Çiğneme Refleksinin Olgunlaşması",
            subtitle = "Daha taneli yemekler, sebzeli pilavlar ve çorba terbiyeleri",
            ageLabel = "10. Ay • 1. Hafta (40-41. Hafta)",
            developmentalGoal = "Yanak ve dil kaslarının tam uyumu, dil ile lokmayı ağız içinde döndürerek çiğneme.",
            textureMilestone = "Çatalla ezilmemiş yumuşak taneler, minik bulgur ve pirinç pilavları.",
            dailyRoutine = "Kahvaltı: Yumurtalı krep / pankek lokmaları + peynir\nÖğle: Sebzeli mercimekli bulgur pilavı + ayran\nİkindi: Meyve salatası küpleri\nAkşam: Tavuklu şehriye çorbası",
            pediatricNotes = "10. ayda bebeğe hala pürüzsüz püre vermek çiğneme refleksinin gelişimini geciktirir. Yiyecekler artık normal ev yemeği kıvamına çok yakın olmalıdır.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Bebek Pankeki (Yumurtalı & Muzlu)",
                    category = "Kahvaltılık",
                    emoji = "🥞",
                    prepInstructions = "1 yumurta sarısı, 1/2 ezilmiş muz ve 2 yemek kaşığı yulaf ununu tavada yağsız pişirin; şeritler halinde kesin.",
                    portionGuide = "1 adet küçük pankek.",
                    pediatricBenefit = "Parmak gıda olarak harika bir kahvaltı alternatifi, enerji ve potasyum sağlar.",
                    allergenLevel = "Yumurta Sarısı"
                )
            ),
            goldenRules = listOf(
                "Pankek ve kreplerde şeker veya bal KESİNLİKLE kullanılmaz; tatlandırma için muz veya elma püresi yeterlidir.",
                "Bebeğin kendi kendine çiğnemesi için bol zaman tanıyın, acele ettirmeyin."
            ),
            safetyAndAlerts = listOf(
                "Pankek lokmaları çok kalın olmamalı, yumuşak piştiğinden emin olunmalıdır."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Pankek yaparken yumurtanın beyazını kullanabilir miyim?",
                    answer = "1 yaşına kadar yumurta beyazı önerilmez. Pankekleri sadece yumurta sarısı ile hazırlayınız."
                )
            )
        ),

        WeeklyPediatricGuide(
            id = "guide_10_w2",
            month = 10,
            weekNumber = 2,
            title = "10. Ay 2. Hafta: Narenciye & Asitli Meyveler",
            subtitle = "Mandalina, portakal ve domates ile asit tolerans kontrolü",
            ageLabel = "10. Ay • 2. Hafta (41-42. Hafta)",
            developmentalGoal = "C vitamini zengini asitli meyveleri denemek, pişik ve reflü belirtilerini gözlemlemek.",
            textureMilestone = "Zarları soyulmuş mandalina dilimleri veya rendelenmiş domatesli soslar.",
            dailyRoutine = "Kahvaltı: Pankek + lor\nÖğle: Domates soslu kıymalı makarna\nİkindi: 2-3 dilim zarı soyulmuş mandalina\nAkşam: Sebze çorbası",
            pediatricNotes = "Narenciye ve domates asidik yapıları nedeniyle hassas bebeklerde ağız çevresinde geçici kızarıklık veya bez bölgesinde pişiğe yol açabilir; bu gerçek bir alerji değil, asit reaksiyonudur.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Zarı Soyulmuş Tatlı Mandalina",
                    category = "Meyve",
                    emoji = "🍊",
                    prepInstructions = "Mandalinanın dış şeffaf zarını tamamen soyup sadece yumuşak iç tanelerini lokma lokma verin.",
                    portionGuide = "2-3 dilim.",
                    pediatricBenefit = "Yüksek C vitamini, bağışıklık ve sıvı desteği.",
                    allergenLevel = "Asit Hassasiyeti Takibi"
                )
            ),
            goldenRules = listOf(
                "Mandalina zarı boğulma riski yaratabileceğinden zar mutlaka soyulmalıdır.",
                "Pişik oluşursa birkaç gün ara verilmeli ve çinko oksitli pişik kremi uygulanmalıdır."
            ),
            safetyAndAlerts = listOf(
                "Çekirdekler kesinlikle temizlenmelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "Ağız çevresinde oluşan kızarıklık alerji midir?",
                    answer = "Domates ve narenciye asidinin cilde temasıyla oluşan kızarıklık çoğunlukla kontakt dermatittir. Yemekten önce dudak çevresine ince bir tabaka zeytinyağı veya bariyer krem sürmek bunu önler."
                )
            )
        ),

        // ==========================================
        // 12+ AY (1 YAŞ BARAJINI AŞMAK)
        // ==========================================
        WeeklyPediatricGuide(
            id = "guide_12_w1",
            month = 12,
            weekNumber = 1,
            title = "12. Ay 1. Hafta: 1 Yaş Yasaklarının Kalkışı",
            subtitle = "Bal, inek sütü ve tam yumurta beyazına kontrollü geçiş",
            ageLabel = "12. Ay • 1. Hafta (1 Yaş Başlangıcı)",
            developmentalGoal = "1 yaş yasaklarının kalkmasıyla birlikte aile sofrasının tam bir üyesi olmak, botulizm riskinin sona ermesi.",
            textureMilestone = "Yetişkin ev yemekleri kıvamında, hafif tuzlu olmayan normal sofralık yemekler.",
            dailyRoutine = "Sabah Kahvaltısı: Tam yumurta (sarısı+beyazı) omlet + bal (1 çay kaşığı) + peynir\nÖğle: Ev yemekleri (kuru fasulye & bulgur pilavı)\nİkindi: Meyve + 1 bardak inek sütü veya kefir\nAkşam: Izgara köfte / balık + salata",
            pediatricNotes = "Bal botulizm toksini riski nedeniyle 1 yaşına kadar kesinlikle yasaktı. 1 yaşından sonra bağırsak florası olgunlaştığı için güvenle başlanabilir; ancak şeker alışkanlığı yaratmamak için ölçülü olunmalıdır.",
            weeklyFoods = listOf(
                KnowledgeBaseFoodItem(
                    name = "Hakiki Çiçek / Çam Balı",
                    category = "Doğal Tatlandırıcı",
                    emoji = "🍯",
                    prepInstructions = "Günde en fazla 1 çay kaşığı, ılık süte veya yulafa karıştırılarak verilir.",
                    portionGuide = "1 çay kaşığı.",
                    pediatricBenefit = "Doğal antimikrobiyal ve enerji kaynağıdır.",
                    allergenLevel = "1 Yaş Sonrası Güvenli"
                ),
                KnowledgeBaseFoodItem(
                    name = "Tam Yumurta Beyazı",
                    category = "Protein",
                    emoji = "🥚",
                    prepInstructions = "İyi pişmiş haşlama yumurta veya omlet şeklinde beyazı ile birlikte verilir.",
                    portionGuide = "1 tam yumurta.",
                    pediatricBenefit = "En yüksek biyolojik değere sahip albümin proteini.",
                    allergenLevel = "Alerjen Gözlemi (3 Gün)"
                ),
                KnowledgeBaseFoodItem(
                    name = "Pastörize Tam Yağlı İnek Sütü",
                    category = "Süt",
                    emoji = "🥛",
                    prepInstructions = "Günlük pastörize süt ılık olarak bardaktan içirilir.",
                    portionGuide = "Günde en fazla 200-300 ml.",
                    pediatricBenefit = "Kalsiyum ve D vitamini takviyesi.",
                    allergenLevel = "Laktoz ve Süt Proteini"
                )
            ),
            goldenRules = listOf(
                "İnek sütü günde 400 ml'den fazla tüketilirse demir eksikliği anemisine ve iştahsızlığa yol açabilir; sınır 1-2 bardaktır.",
                "Bal 1 yaşından sonra güvenli olsa da şeker içeriği nedeniyle abartılmamalıdır."
            ),
            safetyAndAlerts = listOf(
                "Pastörize edilmemiş çiğ süt (sokak sütü) brusella riski nedeniyle ASLA tüketilmemelidir."
            ),
            faqs = listOf(
                KnowledgeBaseFAQ(
                    question = "1 yaşından sonra anne sütünü bırakmalı mıyım?",
                    answer = "Kesinlikle hayır! Dünya Sağlık Örgütü (WHO) anne sütünün 2 yaşına kadar ve sonrasında anne ve bebek istediği sürece devam ettirilmesini güçlü bir şekilde önerir."
                ),
                KnowledgeBaseFAQ(
                    question = "Bebeğim artık yemekleri reddediyor, neden?",
                    answer = "1 yaşından sonra büyüme hızı ilk yıla göre belirgin şekilde yavaşlar; bu da fizyolojik bir iştah azalmasına yol açar. Porsiyonları küçültün ve asla zorlamayın."
                )
            )
        )
    )

    fun getGuidesForMonth(month: Int): List<WeeklyPediatricGuide> {
        val filtered = allWeeklyGuides.filter { it.month == month }
        return if (filtered.isNotEmpty()) filtered else {
            // En yakın ayı bul
            val closestMonth = allWeeklyGuides.minByOrNull { kotlin.math.abs(it.month - month) }?.month ?: 6
            allWeeklyGuides.filter { it.month == closestMonth }
        }
    }

    fun searchGuides(query: String): List<WeeklyPediatricGuide> {
        if (query.isBlank()) return allWeeklyGuides
        val q = query.trim().lowercase()
        return allWeeklyGuides.filter { guide ->
            guide.title.lowercase().contains(q) ||
            guide.subtitle.lowercase().contains(q) ||
            guide.developmentalGoal.lowercase().contains(q) ||
            guide.textureMilestone.lowercase().contains(q) ||
            guide.pediatricNotes.lowercase().contains(q) ||
            guide.weeklyFoods.any { food ->
                food.name.lowercase().contains(q) ||
                food.pediatricBenefit.lowercase().contains(q) ||
                food.prepInstructions.lowercase().contains(q)
            } ||
            guide.goldenRules.any { it.lowercase().contains(q) } ||
            guide.faqs.any { it.question.lowercase().contains(q) || it.answer.lowercase().contains(q) }
        }
    }

    fun getGuideForBaby(ageMonths: Int, weekNumber: Int = 1): WeeklyPediatricGuide {
        val monthGuides = getGuidesForMonth(ageMonths)
        return monthGuides.find { it.weekNumber == weekNumber }
            ?: monthGuides.firstOrNull()
            ?: allWeeklyGuides.first()
    }
}

package com.example.data.guide

data class FoodStartRecommendation(
    val id: String,
    val name: String,
    val emoji: String,
    val category: String, // "Sebze", "Meyve", "Protein & Et", "Süt Ürünü", "Tahıl & Bakliyat", "Sağlıklı Yağlar"
    val startMonth: Int, // 6, 7, 8, 9, 10, 11, 12
    val startMonthLabel: String, // "6. Ay+", "7. Ay+", "8. Ay+", "9-10. Ay+", "11-12. Ay+", "12+ Ay (1 Yaş)"
    val servingTip: String,
    val texture: String,
    val whyIntroduce: String,
    val isAllergen: Boolean = false,
    val isChokingHazard: Boolean = false,
    val specialNotes: String = ""
)

data class ForbiddenFoodItem(
    val name: String,
    val emoji: String,
    val reason: String,
    val risk: String,
    val whenAllowed: String,
    val severityLevel: String = "Yüksek Risk"
)

object FoodStartMonthData {

    val allRecommendations: List<FoodStartRecommendation> = listOf(
        // ==================== 6. AY (İLK BAŞLANGIÇ) ====================
        FoodStartRecommendation(
            id = "avokado",
            name = "Avokado",
            emoji = "🥑",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Olgun avokado çatalla ezilerek püre kıvamında veya BLW için kalın parmak dilimler halinde sunulur.",
            texture = "Pürüzsüz Püre / Yumuşak Dilim",
            whyIntroduce = "Beyin gelişimini hızlandıran tekli doymamış sağlıklı yağlar, lif ve E vitamini deposudur.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Yumuşak ve sindirimi çok naziktir, ilk haftalarda güvenle başlanabilir."
        ),
        FoodStartRecommendation(
            id = "kabak",
            name = "Yeşil Kabak",
            emoji = "🥒",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Buharda iyice yumuşayana kadar pişirilir. Çatalla ezilerek veya yumuşak kalın çubuklar halinde verilir.",
            texture = "Püre / Yumuşak Parmak Gıda",
            whyIntroduce = "Yüksek su ve lif içeriğiyle bağırsakları çalıştırır, sindirimi en kolay başlangıç sebzesidir.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "İlk sebze tadımlarında en çok tavsiye edilen sebzedir."
        ),
        FoodStartRecommendation(
            id = "havuc",
            name = "Havuç",
            emoji = "🥕",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Buharda veya suda çatalla ezilecek kadar yumuşayana kadar pişirilir. Asla çiğ verilmez!",
            texture = "Yumuşak Püre / İyice Pişmiş Çubuk",
            whyIntroduce = "Göz ve bağışıklık sağlığı için zengin A vitamini (beta-karoten) kaynağıdır.",
            isAllergen = false,
            isChokingHazard = true,
            specialNotes = "Çiğ havuç tehlikeli bir boğulma riskidir; mutlaka çatalla kolay ezilecek yumuşaklıkta olmalıdır."
        ),
        FoodStartRecommendation(
            id = "tatli_patates",
            name = "Tatlı Patates",
            emoji = "🍠",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Buharda pişirilip zeytinyağı ilavesiyle ezilir veya fırınlanıp yumuşak parmak dilim yapılır.",
            texture = "Çatalla Ezilmiş Püre",
            whyIntroduce = "Hafif tatlı aroması sayesinde bebeklerin kolay kabul ettiği, lif ve C vitamini zengini bir karbonhidrattır.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "patates",
            name = "Patates",
            emoji = "🥔",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Haşlanıp anne sütü veya zeytinyağı ile açılarak çatalla ezilir. Çok yapışkan olmaması için miksere vurulmaz.",
            texture = "Yumuşak Ezme",
            whyIntroduce = "Yüksek enerji, potasyum ve B6 vitamini sağlar; bebeğin artan kalori ihtiyacını destekler.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "balkabagi",
            name = "Balkabağı",
            emoji = "🎃",
            category = "Sebze",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Buharda 15 dk pişirilip çatalla ezilir. Yulaf lapasına veya yoğurda karıştırılabilir.",
            texture = "Pürüzsüz Püre",
            whyIntroduce = "A, C ve E vitaminleri ile sindirimi düzenleyen lif açısından zengindir.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "elma",
            name = "Elma",
            emoji = "🍎",
            category = "Meyve",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Cam rendede rendelenerek veya buharda hafif yumuşatılıp püre yapılarak verilir. Asla sert dilim verilmez!",
            texture = "Cam Rende / Pişmiş Püre",
            whyIntroduce = "Sindirim sağlığını destekleyen pektin lifi ve antioksidanlar içerir.",
            isAllergen = false,
            isChokingHazard = true,
            specialNotes = "Çiğ sert elma dilimleri boğulma riski oluşturur. Buharda yumuşatılmalı veya rendelenmelidir."
        ),
        FoodStartRecommendation(
            id = "armut",
            name = "Armut",
            emoji = "🍐",
            category = "Meyve",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "İyice olgunlaşmış ve yumuşamış armut cam rendede veya çatalla ezilerek sunulur.",
            texture = "Yumuşak Ezme / Cam Rende",
            whyIntroduce = "Kabızlığı önleyici ve bağırsak hareketlerini yumuşatıcı nazik doğal lif kaynağıdır.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "muz",
            name = "Muz",
            emoji = "🍌",
            category = "Meyve",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Olgun muz çatalla ezilir veya kabuğu alttan tutulacak şekilde parmak gıda olarak verilir.",
            texture = "Çatalla Ezilmiş / Yumuşak Parça",
            whyIntroduce = "Potasyum, B6 vitamini ve enerji sağlar; çiğneme kaslarını güçlendirir.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Aşırı miktarda verilmesi kabızlık yapabilir, ölçülü sunulmalıdır."
        ),
        FoodStartRecommendation(
            id = "yumurta_sarisi",
            name = "Yumurta Sarısı",
            emoji = "🍳",
            category = "Protein & Et",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Tam katı haşlanır. İlk gün 1/4 (çeyrek) miktar anne sütü veya zeytinyağı ile ezilerek başlanır.",
            texture = "Ezilmiş Püre",
            whyIntroduce = "Beyin gelişiminde kilit rol oynayan kolin, demir, lutein ve kaliteli protein içerir.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Alerjen gıdadır; 3 gün kuralı ile sabah saatlerinde denenmelidir. Asla cıvık/çiğ bırakılmaz."
        ),
        FoodStartRecommendation(
            id = "yogurt",
            name = "Doğal Ev Yoğurdu",
            emoji = "🥛",
            category = "Süt Ürünü",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Tam yağlı pastörize sütle mayalanmış doğal yoğurt, oda sıcaklığına getirilerek küçük porsiyonla sunulur.",
            texture = "Pürüzsüz Kaşık Kıvamı",
            whyIntroduce = "Kemik gelişimi için kalsiyum, fosfor ve bağırsak mikrobiyotası için canlı probiyotikler içerir.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "İnek sütü içecek olarak yasak olsa da fermente yoğurdun protein yapısı sindirilebilir hale gelir."
        ),
        FoodStartRecommendation(
            id = "yulaf",
            name = "Bebek Yulafı",
            emoji = "🥣",
            category = "Tahıl & Bakliyat",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Su veya anne sütü ile pişirilerek yumuşacık lapa yapılır; meyve püresi ile tatlandırılabilir.",
            texture = "Yumuşak Lapa",
            whyIntroduce = "Beta-glukan lifi, demir, çinko ve uzun süreli tokluk sağlayan kompleks karbonhidrat sunar.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "zeytinyagi",
            name = "Soğuk Sıkım Zeytinyağı",
            emoji = "🫒",
            category = "Sağlıklı Yağlar",
            startMonth = 6,
            startMonthLabel = "6. Ay+",
            servingTip = "Pişmiş sebze ve çorbalara tabağa alındıktan sonra 1 çay kaşığı çiğ olarak ilave edilir.",
            texture = "Sıvı İlave",
            whyIntroduce = "Hızlı büyüyen bebek beyni için sağlıklı yağlar sağlar, yağda eriyen A-D-E-K vitaminlerinin emilimini artırır.",
            isAllergen = false,
            isChokingHazard = false
        ),

        // ==================== 7. AY (ÇEŞİTLİLİK & DEMİR) ====================
        FoodStartRecommendation(
            id = "dana_kiyma",
            name = "Dana Kıyma (2 Kez Çekilmiş)",
            emoji = "🥩",
            category = "Protein & Et",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "İki kez çekilmiş az yağlı kıyma sebze püreleri içine iyice pişirilerek karıştırılır veya yumuşak minik köfte yapılır.",
            texture = "Ezilmiş Sebzeyle Karışık / Yumuşak Köfte İçi",
            whyIntroduce = "6. aydan sonra tükenen demir depolarını dolduran en zengin emilimli 'hem demir' ve çinko kaynağıdır.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Haftada 2-3 kez tüketilmesi demir eksikliği anemisine karşı en güçlü kalkandır."
        ),
        FoodStartRecommendation(
            id = "kirmizi_mercimek",
            name = "Kırmızı Mercimek",
            emoji = "🥣",
            category = "Tahıl & Bakliyat",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "İyice haşlanıp kabak ve havuçla birlikte çorba veya çatalla ezilmiş püre şeklinde sunulur.",
            texture = "Pütürlü Çorba / Ezme",
            whyIntroduce = "Bitkisel demir, bitkisel protein ve bağırsak dostu lif sağlar.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Gaz yapmaması için pişirmeden önce suda bekletilmeli ve pişerken köpüğü alınmalıdır."
        ),
        FoodStartRecommendation(
            id = "brokoli",
            name = "Brokoli",
            emoji = "🥦",
            category = "Sebze",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "Buharda sap kısmı yumuşayana kadar haşlanır; çiçek kısımları çatalla ezilir veya parmak gıda tutturulur.",
            texture = "Yumuşak Çiçek / Çatalla Ezilmiş",
            whyIntroduce = "Yüksek C vitamini, folat ve kalsiyum içerir. Acımsı tatlara alışmayı destekler.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "bebek_loru",
            name = "Tuzsuz Bebek Loru",
            emoji = "🧀",
            category = "Süt Ürünü",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "Evde süt kestirilerek yapılan tuzsuz lor, kahvaltıda yumurta sarısıyla veya sebzeyle karıştırılarak verilir.",
            texture = "Yumuşak Pütürlü",
            whyIntroduce = "Yüksek kaliteli peynir altı suyu proteini ve konsantre kalsiyum sağlar.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Tuzsuz olması şarttır; piyasadaki tuzlu salamura peynirler bebek böbrekleri için zararlıdır."
        ),
        FoodStartRecommendation(
            id = "kemik_suyu",
            name = "İlikli Kemik Suyu",
            emoji = "🍲",
            category = "Protein & Et",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "Kısık ateşte uzun süre kaynatılmış kemik suyu çorbalara ve sebze ezmelerine lezzet/besin için eklenir.",
            texture = "Sıvı Pişirme Bazı",
            whyIntroduce = "Bağırsak astarını koruyan kolajen, jelatin ve mineraller sunar.",
            isAllergen = false,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "seftali",
            name = "Şeftali / Kayısı",
            emoji = "🍑",
            category = "Meyve",
            startMonth = 7,
            startMonthLabel = "7. Ay+",
            servingTip = "Kabukları soyulup çatalla ezilir veya olgun yumuşak dilim halinde bebeğin eline verilir.",
            texture = "Çatalla Ezilmiş Meyve",
            whyIntroduce = "A ve C vitamini deposudur; sindirimi hızlandırır ve kabızlığı önler.",
            isAllergen = false,
            isChokingHazard = false
        ),

        // ==================== 8. AY (PÜTÜRLÜ & PARMAK GIDA) ====================
        FoodStartRecommendation(
            id = "tavuk_eti",
            name = "Organik Tavuk / Hindi Eti",
            emoji = "🍗",
            category = "Protein & Et",
            startMonth = 8,
            startMonthLabel = "8. Ay+",
            servingTip = "But kısmından iyice haşlanıp lif lif çok ince didiklenir veya sebzeyle çatalla ezilir.",
            texture = "İnce Didiklenmiş / Pütürlü Ezme",
            whyIntroduce = "Büyüme hormonları, B vitaminleri (özellikle niasin) ve sindirimi kolay yağsız protein sunar.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Göğüs eti kuru kalabileceğinden daha sulu olan but kısmı tercih edilmelidir."
        ),
        FoodStartRecommendation(
            id = "ispanak",
            name = "Ispanak",
            emoji = "🥬",
            category = "Sebze",
            startMonth = 8,
            startMonthLabel = "8. Ay+",
            servingTip = "Taze pişirilir, kıymayla veya pirinçle ezilir. Asla bekletilip tekrar ısıtılmaz (nitrit riski).",
            texture = "Yumuşak Ezme",
            whyIntroduce = "Folat, demir, magnezyum ve A vitamini deposudur.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "Ispanak pişirildiği öğünde tüketilmeli, buzdolabında bekletilip yeniden ısıtılmamalıdır."
        ),
        FoodStartRecommendation(
            id = "bezelye",
            name = "Bezelye",
            emoji = "🟢",
            category = "Sebze",
            startMonth = 8,
            startMonthLabel = "8. Ay+",
            servingTip = "Haşlandıktan sonra kabukları çatalla patlatılarak ezilir. Tane olarak yuvarlak verilmez!",
            texture = "Patlatılmış & Ezilmiş",
            whyIntroduce = "Protein oranı yüksek bir sebzedir; demir, çinko ve tatlı lif içerir.",
            isAllergen = false,
            isChokingHazard = true,
            specialNotes = "Bütün tane bezelye soluk borusuna kaçabilir; mutlaka çatalla yassılaştırılmalıdır."
        ),
        FoodStartRecommendation(
            id = "bebek_makarnasi",
            name = "Bebek Makarnası / İnce Şehriye",
            emoji = "🍝",
            category = "Tahıl & Bakliyat",
            startMonth = 8,
            startMonthLabel = "8. Ay+",
            servingTip = "İyice yumuşayana kadar haşlanır; zeytinyağı ve lor peyniri ile karıştırılarak verilir.",
            texture = "Yumuşak Taneli",
            whyIntroduce = "Çiğneme ve dil koordinasyonunu geliştirir, enerji sağlar.",
            isAllergen = true,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "labne",
            name = "Ev Yapımı Labne Peyniri",
            emoji = "🧀",
            category = "Süt Ürünü",
            startMonth = 8,
            startMonthLabel = "8. Ay+",
            servingTip = "Süzme yoğurt kıvamındaki tuzsuz labne, parmak sebzelerin üzerine sürülerek verilebilir.",
            texture = "Sürülebilir Krema",
            whyIntroduce = "Yüksek kalsiyum ve sağlıklı yağ ile doyurucu kahvaltı seçeneğidir.",
            isAllergen = true,
            isChokingHazard = false
        ),

        // ==================== 9. - 10. AY (BALIK & BAKLİYAT & KISKAÇ TUTUŞU) ====================
        FoodStartRecommendation(
            id = "balik",
            name = "Kılçıksız Beyaz Balık (Somon / Mezgit)",
            emoji = "🐟",
            category = "Protein & Et",
            startMonth = 9,
            startMonthLabel = "9-10. Ay+",
            servingTip = "Buharda veya fırında pişirilir. Kılçıkları iki kez elle dikkatle kontrol edilerek didiklenir.",
            texture = "Didiklenmiş Lokmalar",
            whyIntroduce = "Beyin ve göz retinası gelişimi için eşsiz Omega-3 (DHA & EPA) ve D vitamini kaynağıdır.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Büyük yırtıcı balıklar (ton balığı, kılıç) cıva riski nedeniyle verilmez. Somon, mezgit, levrek tercih edilir."
        ),
        FoodStartRecommendation(
            id = "nohut",
            name = "Nohut / Kuru Fasulye",
            emoji = "🧆",
            category = "Tahıl & Bakliyat",
            startMonth = 9,
            startMonthLabel = "9-10. Ay+",
            servingTip = "İyice yumuşayana kadar haşlanır, dış zarları kolay sindirim için soyulur ve çatalla ezilir (humus gibi).",
            texture = "Ezilmiş Püre / Humus",
            whyIntroduce = "Zengin lif, bitkisel protein, çinko ve demir kombinasyonudur.",
            isAllergen = false,
            isChokingHazard = true,
            specialNotes = "Tane nohut sert ve yuvarlak olduğu için boğulma riskidir; mutlaka iki parmakla ezilmelidir."
        ),
        FoodStartRecommendation(
            id = "bulgur",
            name = "Bulgur / Siyez Bulguru",
            emoji = "🌾",
            category = "Tahıl & Bakliyat",
            startMonth = 9,
            startMonthLabel = "9-10. Ay+",
            servingTip = "Sebzeli yumuşak pilav veya çorba içinde kıymayla pişirilerek kıskaç tutuşuna sunulur.",
            texture = "Yumuşak Taneli Pilav",
            whyIntroduce = "B grubu vitaminleri, lif ve tokluk hissi sağlar.",
            isAllergen = true,
            isChokingHazard = false
        ),
        FoodStartRecommendation(
            id = "kivi",
            name = "Kivi",
            emoji = "🥝",
            category = "Meyve",
            startMonth = 9,
            startMonthLabel = "9-10. Ay+",
            servingTip = "İyice olgunlaşmış tatlı kivi minik küpler halinde kesilir. Asitli olduğu için ağız kenarı kızarabilir.",
            texture = "Yumuşak Minik Küp",
            whyIntroduce = "Portakaldan bile yüksek C vitamini içerir; demir emilimini maksimuma çıkarır.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Hafif asidiktir; ilk denemede küçük miktar verilip dudak çevresi takip edilmelidir."
        ),
        FoodStartRecommendation(
            id = "mandalina",
            name = "Mandalina / Portakal",
            emoji = "🍊",
            category = "Meyve",
            startMonth = 9,
            startMonthLabel = "9-10. Ay+",
            servingTip = "Beyaz zarları ve çekirdekleri tamamen soyulmuş tatlı dilim parçaları olarak verilir.",
            texture = "Zarsız Yumuşak Dilim",
            whyIntroduce = "C vitamini ve bağışıklık desteği sağlar.",
            isAllergen = false,
            isChokingHazard = true,
            specialNotes = "Zarı soyulmamış dilim boğulma riski yaratabilir; zarı mutlaka temizlenmelidir."
        ),

        // ==================== 11. - 12. AY (AİLE SOFRASI) ====================
        FoodStartRecommendation(
            id = "yumurta_beyazi",
            name = "Yumurta Beyazı",
            emoji = "🥚",
            category = "Protein & Et",
            startMonth = 11,
            startMonthLabel = "11-12. Ay+",
            servingTip = "İyi pişmiş haşlanmış yumurta beyazı veya omlet içinde küçük parçalarla başlanır.",
            texture = "Küçük Yumuşak Parçalar",
            whyIntroduce = "Biyoyararlanımı en yüksek tam protein kaynağıdır.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Yumurta beyazı sarısına göre daha yüksek alerjen potansiyeline sahiptir; 3 gün kuralı uygulanmalıdır."
        ),
        FoodStartRecommendation(
            id = "cilek",
            name = "Çilek / Domates",
            emoji = "🍓",
            category = "Meyve",
            startMonth = 11,
            startMonthLabel = "11-12. Ay+",
            servingTip = "Boyuna 4'e bölünerek minik parçalara ayrılır. Pişmiş domates sosu olarak yemeklerde kullanılır.",
            texture = "Minik Yumuşak Lokmalar",
            whyIntroduce = "Likopen, C vitamini ve güçlü antioksidan deposudur.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "Histamin salgılatıcı özelliği nedeniyle hassas ciltlerde döküntü yapabilir, kontrollü denenmelidir."
        ),

        // ==================== 12+ AY (1 YAŞ SONRASI GÜVENLİ OLANLAR) ====================
        FoodStartRecommendation(
            id = "bal",
            name = "Bal (1 Yaş Sonrası)",
            emoji = "🍯",
            category = "Meyve",
            startMonth = 12,
            startMonthLabel = "12+ Ay (1 Yaş)",
            servingTip = "1 yaşını doldurduktan sonra tatlandırıcı olarak az miktarda yulaf veya pankeke sürülebilir.",
            texture = "Akışkan / Sürülebilir",
            whyIntroduce = "1 yaşından önce ölümcül botulizm riski nedeniyle kesinlikle yasaktır; 1 yaştan sonra güvenlidir.",
            isAllergen = false,
            isChokingHazard = false,
            specialNotes = "12 aydan önce ASLA verilmemelidir! 1 yaşından sonra doğal ve güvenlidir."
        ),
        FoodStartRecommendation(
            id = "inek_sutu",
            name = "İnek Sütü (İçecek Olarak)",
            emoji = "🥛",
            category = "Süt Ürünü",
            startMonth = 12,
            startMonthLabel = "12+ Ay (1 Yaş)",
            servingTip = "1 yaştan sonra bardakla günde maksimum 400-500 ml tam yağlı pastörize süt olarak tüketilebilir.",
            texture = "Sıvı İçecek",
            whyIntroduce = "1 yaştan önce böbrek yükü ve bağırsak kanaması riski taşır; 1 yaştan sonra kalsiyum desteğidir.",
            isAllergen = true,
            isChokingHazard = false,
            specialNotes = "1 yaştan önce doğrudan içilmez, sadece yoğurt/peynir içinde fermente tüketilebilir."
        ),
        FoodStartRecommendation(
            id = "patlican",
            name = "Patlıcan",
            emoji = "🍆",
            category = "Sebze",
            startMonth = 12,
            startMonthLabel = "12+ Ay (1 Yaş)",
            servingTip = "Fırınlanıp kabuğu soyularak sebze yemekleri içinde az miktarda sunulur.",
            texture = "Pişmiş Yumuşak",
            whyIntroduce = "Doğal nikotin içerdiği ve besin değeri düşük olduğu için 1 yaşından sonra önerilir.",
            isAllergen = false,
            isChokingHazard = false
        )
    )

    // 1 Yaşından Önce KESİNLİKLE Yasak Olan Gıdalar
    val forbiddenFoodsBeforeOneYear: List<ForbiddenFoodItem> = listOf(
        ForbiddenFoodItem(
            name = "Bal (Tüm Çeşitleri)",
            emoji = "🍯",
            reason = "Bal içinde 'Clostridium botulinum' bakteri sporları bulunabilir. Bebeğin henüz olgunlaşmamış bağırsak florası bu sporları nötralize edemez.",
            risk = "Ölümcül seyredebilecek 'Bebek Botulizmi' (kas felci, solunum yetmezliği ve beslenememe).",
            whenAllowed = "12 ayı (1 yaşını) tamamladıktan sonra güvenle başlanabilir.",
            severityLevel = "HAYATİ TEHLİKE"
        ),
        ForbiddenFoodItem(
            name = "İnek Sütü (Doğrudan İçecek)",
            emoji = "🥛",
            reason = "Yüksek protein ve mineral yükü henüz gelişmekte olan bebek böbreklerini aşırı yorar. Ayrıca demir emilimini engeller.",
            risk = "Mikroskobik bağırsak kanamaları, şiddetli demir eksikliği anemisi ve böbrek zorlanması.",
            whenAllowed = "1 yaşından sonra günde en fazla 400-500 ml bardakla sunulabilir (Yoğurt ve peynir 6. aydan itibaren serbesttir).",
            severityLevel = "YÜKSEK RİSK"
        ),
        ForbiddenFoodItem(
            name = "Tuz & Kaya Tuzu",
            emoji = "🧂",
            reason = "Bebek böbrekleri tuzu süzme kapasitesine sahip değildir. Anne sütü ve sebzelerdeki doğal sodyum bebeğe fazlasıyla yeterlidir.",
            risk = "Böbrek hasarı, hipertansiyon yatkınlığı ve yapay tuz bağımlılığı.",
            whenAllowed = "1 yaşından sonra aile yemeklerinde çok az miktarda kullanılabilir; 2 yaşına kadar tuzsuz tercih edilmelidir.",
            severityLevel = "BÖBREK HASARI"
        ),
        ForbiddenFoodItem(
            name = "İlave Şeker, Pekmez & Şuruplar",
            emoji = "🍬",
            reason = "Boş kalori kaynağıdır, damak tadını bozar ve sağlıklı sebzeleri reddetmesine yol açar.",
            risk = "Erken çocukluk çağı obezitesi, diş çürükleri ve iştahsızlık.",
            whenAllowed = "Mümkünse 2 yaşına kadar ilave şekerden tamamen uzak durulmalıdır.",
            severityLevel = "METABOLİK RİSK"
        ),
        ForbiddenFoodItem(
            name = "Bütün Kuruyemişler & Yuvarlak Gıdalar",
            emoji = "🥜",
            reason = "Fındık, ceviz, bütün üzüm, leblebi ve zeytin gibi sert veya yuvarlak gıdalar tam olarak soluk borusunun çapına uygundur.",
            risk = "Soluk borusunu tıkayarak dakikalar içinde boğulmaya yol açabilir.",
            whenAllowed = "Kuruyemişler 6. aydan itibaren toz halinde veya pürüzsüz ezme olarak verilir; bütün halleri 4-5 yaşa kadar verilmez!",
            severityLevel = "BOĞULMA TEHLİKESİ"
        ),
        ForbiddenFoodItem(
            name = "Bitki Çayları, Siyah Çay & Rezene",
            emoji = "🫖",
            reason = "Çaylardaki tanenler besinlerdeki demiri bağlayarak emilimi %80 oranında engeller. Rezene gibi bitkiler estragol toksini içerebilir.",
            risk = "Ağır demir eksikliği, nörolojik yan etkiler ve yetersiz beslenme.",
            whenAllowed = "Bebekler için su ve anne sütü/mama tek sıvı kaynağıdır; çaylar 2 yaş öncesi önerilmez.",
            severityLevel = "TOKSİK & ANEMİ RİSKİ"
        ),
        ForbiddenFoodItem(
            name = "Şarküteri & İşlenmiş Etler (Sosis, Salam, Sucuk)",
            emoji = "🌭",
            reason = "Aşırı tuz, doymuş yağ, nitrit/nitrat koruyucuları ve baharat içerir.",
            risk = "Kanserojen madde yükü, sindirim bozukluğu ve böbrek yükü.",
            whenAllowed = "Erken çocuklukta kesinlikle önerilmez; temiz taze et tercih edilmelidir.",
            severityLevel = "YÜKSEK KATKI MADDESİ"
        ),
        ForbiddenFoodItem(
            name = "Çiğ veya Az Pişmiş Yumurta / Et / Süt",
            emoji = "🍳",
            reason = "Pastörize edilmemiş süt veya cıvık yumurtada salmonella ve listeria bakterileri yaşayabilir.",
            risk = "Ağır bakteriyel enfeksiyon, hastaneye yatış gerektiren zehirlenmeler.",
            whenAllowed = "Yumurta ve etler bebeklikte mutlaka tam katı ve iyi pişmiş sunulmalıdır.",
            severityLevel = "BAKTERİYEL ENFEKSİYON"
        )
    )
}

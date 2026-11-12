package akinai.minikgurme.data.guide

import akinai.minikgurme.data.model.BabyRecipe
import akinai.minikgurme.data.model.RecipeIngredient

object PediatricRecipesData {

    val allRecipes: List<BabyRecipe> = listOf(
        // === 6+ AY: İLK EK GIDA & İPEKSİ PÜRELER ===
        BabyRecipe(
            id = "avokado_muz_ipek",
            title = "Avokadolu & Muzlu İpeksi İlk Kaşık Püresi",
            ageMonths = 6,
            ageLabel = "6+ Ay",
            prepTime = "5 dk",
            servings = "1 porsiyon",
            mealType = "Kahvaltı / Ara Öğün",
            textureStyle = "İpeksi Püre",
            tags = listOf("Hızlı & Kolay", "Pişirme Yok", "Sağlıklı Yağ", "Potasyum", "Glutensiz"),
            ingredients = listOf(
                RecipeIngredient("Olgunlaşmış yumuşak avokado", "1/4 adet", "🥑"),
                RecipeIngredient("Tatlı olgun muz", "1/2 adet", "🍌"),
                RecipeIngredient("Anne sütü veya ılık su (açmak için)", "1-2 tatlı kaşığı", "💧")
            ),
            instructions = listOf(
                "Olgun avokadonun iç etli kısmını kaşıkla küçük bir cam kaseye çıkarın.",
                "Muzu dilimleyip avokado ile birlikte çatalla tamamen pürüzsüz ve ipeksi kıvama gelene kadar ezin.",
                "Bebeğin yutmasını kolaylaştırmak için kıvamı 1 tatlı kaşığı anne sütü veya ılık su ile yumuşatın.",
                "Bekletmeden taze olarak bebeğinize sunun."
            ),
            doctorTip = "İpucu: Avokado beyin gelişimini destekleyen sağlıklı tekli doymamış yağ asitleri açısından doğadaki en zengin ilk besindir. Pişirme gerektirmediği için tüm vitaminlerini %100 korur.",
            emoji = "🥑"
        ),
        BabyRecipe(
            id = "buharda_havuc_elma",
            title = "Buharda Pişen Havuç & Elma Püresi",
            ageMonths = 6,
            ageLabel = "6+ Ay",
            prepTime = "15 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "Pürüzsüz Püre",
            tags = listOf("A Vitamini", "C Vitamini", "Sindirime Yardımcı", "Glutensiz", "Doğal Tatlı"),
            ingredients = listOf(
                RecipeIngredient("Taze tatlı havuç", "1 adet", "🥕"),
                RecipeIngredient("Tatlı kırmızı veya sarı elma", "1/2 adet", "🍎"),
                RecipeIngredient("Soğuk sıkım sızma zeytinyağı", "1 tatlı kaşığı", "🫒"),
                RecipeIngredient("Haşlama sebze suyu", "2 yemek kaşığı", "💧")
            ),
            instructions = listOf(
                "Havuç ve elmanın kabuğunu soyup ince halkalar halinde doğrayın.",
                "Buhar sepetinde havuçlar çatalla kolayca ezilene kadar yaklaşık 10-12 dakika pişirin.",
                "Buharda yumuşayan elma ve havuçları çatalla veya cam rende ile pürüzsüz hale getirin.",
                "Ilındıktan sonra A vitamininin vücutta tam emilmesi için 1 tatlı kaşığı zeytinyağını ekleyip karıştırın."
            ),
            doctorTip = "İpucu: Havuçtaki beta-karoten yağda çözünen bir vitamindir. Zeytinyağı eklemek bebeğin görme ve bağışıklık gelişimini en üst düzeye çıkarır.",
            emoji = "🥕"
        ),
        BabyRecipe(
            id = "kabak_sandal",
            title = "Kıymalı & Yoğurtlu Kabak Sandal",
            ageMonths = 6,
            ageLabel = "6+ Ay",
            prepTime = "30 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "BLW / Çatalla Ezilebilir",
            tags = listOf("Öğle Yemeği", "Glutensiz", "Demir Deposu", "BLW Uygun", "Protein"),
            ingredients = listOf(
                RecipeIngredient("Taze küçük sakız kabağı", "1 adet", "🥒"),
                RecipeIngredient("Dana kıyma (2 kez çekilmiş)", "50g", "🥩"),
                RecipeIngredient("Sızma zeytinyağı", "1 yemek kaşığı", "🫒"),
                RecipeIngredient("Taze nane yaprağı", "1-2 yaprak", "🌿"),
                RecipeIngredient("Doğal ev yoğurdu", "2 yemek kaşığı", "🥣")
            ),
            instructions = listOf(
                "Kabağı yıkayıp boylamasına ikiye kesin. İç kısmını tatlı kaşığı ile hafifçe oyup sandal şekli verin.",
                "Küçük tavada dana kıymayı 2 yemek kaşığı su ile kısık ateşte pembeleşene kadar pişirin.",
                "Kıymaya incecik kıyılmış taze nane ve zeytinyağı ekleyin.",
                "Hazırladığınız harcı kabak sandallarına paylaştırın.",
                "Tencerenin tabanına az su ekleyip kapağını kapatın. Kısık ateşte kabaklar lokum kıvamına gelene kadar 20 dakika pişirin.",
                "Ilındıktan sonra üzerine taze ev yoğurdu gezdirerek bebeğinize verin."
            ),
            doctorTip = "İpucu: Kıyma 6. aydan itibaren tükenen anne sütü demir depolarını doldurmak için en kritik besindir. Kabak sindirimi çok kolaylaştırır.",
            emoji = "🥒"
        ),
        BabyRecipe(
            id = "tatli_patates_kiyma",
            title = "Kıymalı Tatlı Patates Püresi",
            ageMonths = 6,
            ageLabel = "6+ Ay",
            prepTime = "20 dk",
            servings = "1 porsiyon",
            mealType = "Akşam yemeği",
            textureStyle = "Pütürlü Ezme",
            tags = listOf("Akşam Yemeği", "A Vitamini", "Demir Deposu", "Glutensiz", "Uzun Tokluk"),
            ingredients = listOf(
                RecipeIngredient("Tatlı patates (küçük)", "1/2 adet", "🍠"),
                RecipeIngredient("Çift çekim dana kıyma", "1 yemek kaşığı", "🥩"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒"),
                RecipeIngredient("Ilık kemik suyu veya su", "3 yemek kaşığı", "🍲")
            ),
            instructions = listOf(
                "Tatlı patatesi soyup küçük küpler halinde doğrayın.",
                "Buharda veya az suda patatesleri yumuşayana kadar haşlayın.",
                "Ayrı bir tavada kıymayı az suyla kısık ateşte kavurun.",
                "Yumuşayan tatlı patatesi ve kıymayı kasede çatalla ezin.",
                "Ilık kemik suyunu ve zeytinyağını ekleyerek kremamsı kıvama getirin."
            ),
            doctorTip = "İpucu: Tatlı patatesin doğal lezzeti kıymanın tadını yumuşatır ve bebeklerin eti severek yemesini sağlar.",
            emoji = "🍠"
        ),
        BabyRecipe(
            id = "balkabagi_irmik",
            title = "Balkabaklı & İrmikli İpeksi Bebek Çorbası",
            ageMonths = 6,
            ageLabel = "6+ Ay",
            prepTime = "15 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "Püre Çorba",
            tags = listOf("Beta-Karoten", "Bağışıklık", "Yumuşak Kıvam", "Hafif"),
            ingredients = listOf(
                RecipeIngredient("Balkabağı dilimi", "1 ince dilim (100g)", "🎃"),
                RecipeIngredient("Bebek irmiği", "1 tatlı kaşığı", "🌾"),
                RecipeIngredient("Su veya sebze suyu", "1 su bardağı", "💧"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Balkabağını küp küp doğrayın ve su ile tencereye alın.",
                "Kaynamaya başlayınca bebek irmiğini ilave edip kısık ateşte 10 dakika pişirin.",
                "Balkabakları yumuşayınca çatalla veya blenderla pürüzsüzleştirin.",
                "Ocaktan alıp ılıtın, son olarak zeytinyağını karıştırıp servis edin."
            ),
            doctorTip = "İpucu: Balkabağı yüksek lif oranıyla kabızlığı önler. Bebek irmiği ise çorbaya doyurucu, yumuşacık bir doku katar.",
            emoji = "🎃"
        ),

        // === 7+ AY: PÜTÜRLERE ALIŞMA & BESLEYİCİ ÇORBALAR ===
        BabyRecipe(
            id = "sutlu_brokoli_corbasi",
            title = "Sütlü & Brokolili Bebek Çorbası",
            ageMonths = 7,
            ageLabel = "7+ Ay",
            prepTime = "20 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "Püre Çorba",
            tags = listOf("Çorba", "C Vitamini", "Kalsiyum", "Bağışıklık Güçlendirici"),
            ingredients = listOf(
                RecipeIngredient("Brokoli çiçeği", "3-4 küçük dal", "🥦"),
                RecipeIngredient("Patates (küçük)", "1/2 adet", "🥔"),
                RecipeIngredient("Anne sütü veya formül mama", "1/2 çay bardağı", "🍼"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Brokoli ve patatesi buharda yumuşayana kadar haşlayın.",
                "Çatalla püre haline getirin veya pürüzsüz kıvam için ezin.",
                "Ocaktan aldıktan sonra çorbanın ılınmasını bekleyin.",
                "Ilınan karışıma taze hazırlanmış anne sütünü veya formül mamayı ekleyin.",
                "Zeytinyağını ekleyip karıştırarak ılık servis yapın."
            ),
            doctorTip = "İpucu: Anne sütü veya formül mamayı asla ocakta kaynatmayın; vitaminlerini korumak için ılındıktan sonra ilave edin.",
            emoji = "🥦"
        ),
        BabyRecipe(
            id = "kirmizi_mercimek_havuc",
            title = "Kabaklı & Havuçlu Kırmızı Mercimek Püresi",
            ageMonths = 7,
            ageLabel = "7+ Ay",
            prepTime = "20 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "Pütürlü Ezme",
            tags = listOf("Bitkisel Protein", "Demir", "Zengin Lif", "Doyurucu"),
            ingredients = listOf(
                RecipeIngredient("Kırmızı mercimek (yıkanmış)", "1 yemek kaşığı", "🫘"),
                RecipeIngredient("Bal kabağı veya taze kabak", "1/3 adet", "🥒"),
                RecipeIngredient("Havuç (küçük)", "1/2 adet", "🥕"),
                RecipeIngredient("Kimyon (gaz önleyici)", "1 çimdik", "🌿"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Mercimek, rendelenmiş havuç ve doğranmış kabağı 1 su bardağı suyla tencereye alın.",
                "Mercimekler iyice açılıp sebzeler yumuşayana kadar 15-20 dakika haşlayın.",
                "Gaz yapmaması için 1 çimdik kimyon ekleyin.",
                "Çatalla pütürlü şekilde ezin ve zeytinyağını ilave edip ılıtın."
            ),
            doctorTip = "İpucu: Kırmızı mercimek ilk baklagil başlangıcı için en idealdir. 1 çimdik kimyon sindirimi rahatlatır ve gaz sancısını önler.",
            emoji = "🫘"
        ),
        BabyRecipe(
            id = "kiymali_bebek_corbasi",
            title = "Kıymalı & Havuçlu Bebek Çorbası",
            ageMonths = 7,
            ageLabel = "7+ Ay",
            prepTime = "25 dk",
            servings = "2 porsiyon",
            mealType = "Akşam yemeği",
            textureStyle = "Yumuşak Taneli Çorba",
            tags = listOf("Demir Deposu", "Kemik Suyu", "Doyurucu", "Akşam Yemeği"),
            ingredients = listOf(
                RecipeIngredient("Dana kıyma (çift çekim)", "1 yemek kaşığı (30g)", "🥩"),
                RecipeIngredient("Havuç (ince rendelenmiş)", "1/2 adet", "🥕"),
                RecipeIngredient("Bebek pirinci veya irmik", "1 tatlı kaşığı", "🌾"),
                RecipeIngredient("Ev yapımı ilikli kemik suyu", "1 çay bardağı", "🍲"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Kıymayı tencerede 2 kaşık suyla kavurun.",
                "Rendelenmiş havuç ve pirinci ekleyin.",
                "Kemik suyu ve 1 çay bardağı sıcak suyu ekleyip kısık ateşte pirinçler yumuşayana kadar pişirin.",
                "Çatalla hafifçe ezerek taneli kıvamda sunun."
            ),
            doctorTip = "İpucu: İlikli kemik suyu bebeğin bağırsak florasını güçlendirir ve minerallerle zenginleştirir.",
            emoji = "🍲"
        ),
        BabyRecipe(
            id = "elmali_yulaf_gece",
            title = "Yulaflı & Elmalı Gece Muhallebisi",
            ageMonths = 7,
            ageLabel = "7+ Ay",
            prepTime = "10 dk",
            servings = "1 porsiyon",
            mealType = "Akşam yemeği",
            textureStyle = "Yumuşak Lapa",
            tags = listOf("Uzun Tokluk", "Rahat Uyku", "Zengin Lif", "Tatlı Krizine Sağlıklı"),
            ingredients = listOf(
                RecipeIngredient("İnce öğütülmüş bebek yulafı", "2 yemek kaşığı", "🥣"),
                RecipeIngredient("Cam rendeden elma püresi", "2 yemek kaşığı", "🍎"),
                RecipeIngredient("Su veya papatya çayı", "1/2 su bardağı", "💧"),
                RecipeIngredient("Toz tarçın (alerji testi yapılmışsa)", "1 çimdik", "🪵")
            ),
            instructions = listOf(
                "Yulafı su ile küçük cezvede kısık ateşte 5 dakika pişirin.",
                "Koyulaşan lapayı kaseye alıp ılıtın.",
                "Üzerine taze rendelenmiş elmayı ve 1 çimdik tarçını ekleyip karıştırın."
            ),
            doctorTip = "İpucu: Yulafın içerdiği triptofan ve melatonin bebeğin gece uykusuna geçişini kolaylaştırır ve uzun süre tok tutar.",
            emoji = "🍎"
        ),

        // === 8+ AY: PARMAK BESİNLER & BLW ===
        BabyRecipe(
            id = "avokado_tost",
            title = "Yumurta & Avokado Bebek Tostu",
            ageMonths = 8,
            ageLabel = "8+ Ay",
            prepTime = "10 dk",
            servings = "1 porsiyon",
            mealType = "Kahvaltı",
            textureStyle = "BLW (Parmak Besin)",
            tags = listOf("Kahvaltı", "Sağlıklı Yağ", "Kolin", "Parmak Besin", "BLW"),
            ingredients = listOf(
                RecipeIngredient("Tam buğday ekmeği", "1 ince dilim", "🍞"),
                RecipeIngredient("Olgun avokado", "1/4 adet", "🥑"),
                RecipeIngredient("Tam haşlanmış yumurta sarısı", "1 adet", "🍳"),
                RecipeIngredient("Sızma zeytinyağı", "1 çay kaşığı", "🫒")
            ),
            instructions = listOf(
                "Ekmeği tavada yağsız şekilde hafifçe yumuşak tost kıvamına getirin.",
                "Avokado ve yumurta sarısını kasede çatalla püre yapın.",
                "Zeytinyağını ekleyip ekmeğin üzerine sürün.",
                "Bebeğin minik elleriyle tutabilmesi için parmak kalınlığında şeritler halinde kesin."
            ),
            doctorTip = "İpucu: Yumurta sarısındaki kolin beyin ve sinir hücresi gelişiminin temel taşıdır. Parmak şeritler bebeklerin kavramasını geliştirir.",
            emoji = "🥑"
        ),
        BabyRecipe(
            id = "bebek_pankeki",
            title = "Muzlu & Yulaflı Bebek Pankeki",
            ageMonths = 8,
            ageLabel = "8+ Ay",
            prepTime = "15 dk",
            servings = "4 mini pankek",
            mealType = "Kahvaltı",
            textureStyle = "BLW (Parmak Besin)",
            tags = listOf("Kahvaltı", "Ara Öğün", "BLW", "Şekersiz", "Hızlı"),
            ingredients = listOf(
                RecipeIngredient("Olgun tatlı muz", "1/2 adet", "🍌"),
                RecipeIngredient("Yumurta sarısı", "1 adet", "🍳"),
                RecipeIngredient("Bebek yulaf unu", "2 yemek kaşığı", "🌾"),
                RecipeIngredient("Sızma zeytinyağı (tava için)", "1 çay kaşığı", "🫒")
            ),
            instructions = listOf(
                "Muzu kasede çatalla iyice ezin.",
                "Yumurta sarısını ve yulaf ununu ekleyip akışkan krep harcı yapın.",
                "Tavayı zeytinyağı ile hafifçe yağlayın.",
                "Kaşıkla döküp arkalı önlü kısık ateşte pembeleşene kadar 2'şer dakika pişirin."
            ),
            doctorTip = "İpucu: Dişleri henüz çıkmamış bebekler bile bu yumuşacık pankekleri damaklarıyla çok rahat çiğneyebilir.",
            emoji = "🥞"
        ),
        BabyRecipe(
            id = "somon_patates",
            title = "Somonlu & Patatesli Buharda Lokmalar",
            ageMonths = 8,
            ageLabel = "8+ Ay",
            prepTime = "20 dk",
            servings = "1 porsiyon",
            mealType = "Akşam yemeği",
            textureStyle = "Yumuşak Ezme / BLW",
            tags = listOf("Akşam Yemeği", "Omega-3", "Beyin Gelişimi", "Balık"),
            ingredients = listOf(
                RecipeIngredient("Taze somon fileto (kılçıksız)", "50g", "🐟"),
                RecipeIngredient("Patates (orta boy)", "1/2 adet", "🥔"),
                RecipeIngredient("Taze sıkılmış limon suyu", "2 damla", "🍋"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Somon filetosunu parmaklarınızla 3 kez kontrol edin, hiç kılçık olmadığından emin olun.",
                "Somon ve küp patatesi buharda 15 dakika haşlayın.",
                "Patatesi çatalla ezin, somonu lokmalık parçalara didin.",
                "Üzerine 2 damla limon ve zeytinyağı gezdirip harmanlayın."
            ),
            doctorTip = "İpucu: Haftada 1-2 porsiyon somon veya mezgit balığı bebeğin retina ve bilişsel zeka gelişimi için vazgeçilmezdir.",
            emoji = "🐟"
        ),
        BabyRecipe(
            id = "firinda_bebek_mucveri",
            title = "Fırında Yumuşak Kabak & Havuç Mücveri",
            ageMonths = 8,
            ageLabel = "8+ Ay",
            prepTime = "25 dk",
            servings = "4 çubuk",
            mealType = "Öğle yemeği",
            textureStyle = "BLW (Parmak Besin)",
            tags = listOf("BLW", "Sebze Sevdiren", "Kalsiyum", "Fırında Sağlıklı"),
            ingredients = listOf(
                RecipeIngredient("Rendelenmiş sakız kabağı", "1/2 adet", "🥒"),
                RecipeIngredient("Rendelenmiş havuç", "1/2 adet", "🥕"),
                RecipeIngredient("Tuzsuz bebek lor peyniri", "1 yemek kaşığı", "🧀"),
                RecipeIngredient("Yumurta sarısı", "1 adet", "🍳"),
                RecipeIngredient("Tam buğday unu", "1.5 yemek kaşığı", "🌾"),
                RecipeIngredient("Zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Kabak ve havucun suyunu elinizle iyice sıkın.",
                "Tüm malzemeleri kasede yoğurun.",
                "Yağlı kağıt serili fırın tepsisine uzun parmak şekilleri vererek dizin.",
                "180 derece fırında 15 dakika yumuşak kalacak şekilde fırınlayın."
            ),
            doctorTip = "İpucu: Sebze yemeyi reddeden bebeklerin sebzeleri kendi kendilerine eğlenerek tüketmesini sağlayan harika bir BLW tarifidir.",
            emoji = "🧆"
        ),

        // === 9+ AY: MİNİ KÖFTELER, GELENEKSEL LEZZETLER & AİLE SOFRASI ===
        BabyRecipe(
            id = "sebzeli_bebek_koftesi",
            title = "Buharda Yumuşak Sebzeli Bebek Köftesi",
            ageMonths = 9,
            ageLabel = "9+ Ay",
            prepTime = "20 dk",
            servings = "4 mini köfte",
            mealType = "Öğle yemeği",
            textureStyle = "BLW Parmak Besin",
            tags = listOf("Öğle Yemeği", "Demir Deposu", "Çinko", "BLW", "Protein"),
            ingredients = listOf(
                RecipeIngredient("Dana/kuzu kıyma (çift çekim)", "80g", "🥩"),
                RecipeIngredient("Havuç ve kabak rendesi", "2 yemek kaşığı", "🥕"),
                RecipeIngredient("Yulaf unu veya ekmek içi", "1 yemek kaşığı", "🥣"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Sebzelerin suyunu iyice sıkın.",
                "Kıymayı sebzeler ve yulaf unuyla 3 dakika yoğurun.",
                "Yassı parmak köfteler yapın.",
                "Buhar sepetinde kapağı kapalı 12-15 dakika yumuşacık pişirin."
            ),
            doctorTip = "İpucu: Buharda pişen köfte sertleşmez, lokum gibi yumuşacık kalır ve boğulma riskini en aza indirir.",
            emoji = "🥩"
        ),
        BabyRecipe(
            id = "bebek_tarhanasi",
            title = "Ev Yapımı Kıymalı Bebek Tarhanası",
            ageMonths = 9,
            ageLabel = "9+ Ay",
            prepTime = "15 dk",
            servings = "2 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "Kıvamlı Çorba",
            tags = listOf("Doğal Probiyotik", "Geleneksel", "Demir", "Bağışıklık"),
            ingredients = listOf(
                RecipeIngredient("Bebek tarhanası (tuzsuz)", "1 yemek kaşığı", "🥣"),
                RecipeIngredient("Dana kıyma", "1 tatlı kaşığı", "🥩"),
                RecipeIngredient("Kemik suyu veya su", "1 su bardağı", "🍲"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒"),
                RecipeIngredient("Taze nane", "1 çimdik", "🌿")
            ),
            instructions = listOf(
                "Tarhanayı yarım çay bardağı soğuk su ile kasede çözdürün.",
                "Küçük tencerede kıymayı hafifçe çevirin.",
                "Üzerine kemik suyunu ve çözülmüş tarhanayı dökün.",
                "Sürekli karıştırarak kaynayana kadar 8 dakika pişirin, ılınınca zeytinyağı ekleyin."
            ),
            doctorTip = "İpucu: Tarhana fermente bir besindir; bebeğin bağırsak bağışıklığını kış aylarında hastalıklara karşı korur.",
            emoji = "🥣"
        ),
        BabyRecipe(
            id = "avokado_makarna",
            title = "Avokado Soslu Mini Bebek Makarnası",
            ageMonths = 9,
            ageLabel = "9+ Ay",
            prepTime = "15 dk",
            servings = "1 porsiyon",
            mealType = "Öğle yemeği",
            textureStyle = "BLW / Parmak Besin",
            tags = listOf("B Vitamini", "Enerji", "Parmak Besin", "Hızlı"),
            ingredients = listOf(
                RecipeIngredient("Bebek mini burgu makarnası", "1 avuç (30g)", "🍝"),
                RecipeIngredient("Olgun avokado", "1/4 adet", "🥑"),
                RecipeIngredient("Ev yoğurdu veya lor peyniri", "1 yemek kaşığı", "🧀"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Makarnayı bol suda iyice yumuşayana kadar haşlayın (al dente değil, yumuşak olmalı).",
                "Ayrı kasede avokado, yoğurt ve zeytinyağını çatalla ezip sos yapın.",
                "Süzülen ılık makarnayı sosla harmanlayıp bebeğinizin tabağına koyun."
            ),
            doctorTip = "İpucu: Burgu makarna bebeklerin baş ve işaret parmağı ile cımbız tutuşunu (pincer grasp) geliştirmesi için mükemmeldir.",
            emoji = "🍝"
        ),
        BabyRecipe(
            id = "bebek_menemeni",
            title = "Yumuşacık Labneli Bebek Menemeni",
            ageMonths = 9,
            ageLabel = "9+ Ay",
            prepTime = "12 dk",
            servings = "1 porsiyon",
            mealType = "Kahvaltı",
            textureStyle = "Yumuşak Ezme",
            tags = listOf("Kolin", "A Vitamini", "Kalsiyum", "Zengin Kahvaltı"),
            ingredients = listOf(
                RecipeIngredient("Kabuğu soyulmuş tatlı domates", "1/2 adet", "🍅"),
                RecipeIngredient("Tatlı köy biberi (çekirdeksiz ince doğranmış)", "1/3 adet", "🫑"),
                RecipeIngredient("Yumurta sarısı", "1 adet", "🍳"),
                RecipeIngredient("Tuzsuz labne peyniri", "1 tatlı kaşığı", "🧀"),
                RecipeIngredient("Zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Küp doğranmış domates ve biberi zeytinyağı ile kısık ateşte yumuşayana kadar soteleyin.",
                "Yumurta sarısını çırpıp sebzelerin üzerine dökün ve karıştırarak pişirin.",
                "Ocaktan aldıktan sonra ılıtın ve labne peynirini karıştırarak kremamsı doku verin."
            ),
            doctorTip = "İpucu: Pişmiş domatesteki likopen çok güçlü bir antioksidandır. Labne menemenin asiditesini kırar ve mideyi korur.",
            emoji = "🍳"
        ),

        // === 10-12+ AY: GELİŞMİŞ ÇİĞNEME & ZENGİN TABAKLAR ===
        BabyRecipe(
            id = "tavuklu_sebze_yahni",
            title = "Didilmiş Tavuklu & Bezelyeli Bebek Yahnisi",
            ageMonths = 10,
            ageLabel = "10+ Ay",
            prepTime = "25 dk",
            servings = "2 porsiyon",
            mealType = "Akşam yemeği",
            textureStyle = "Lokma Büyüklüğünde Parçalar",
            tags = listOf("Yüksek Protein", "Çiğneme Refleksi", "Demir", "Aile Sofrası"),
            ingredients = listOf(
                RecipeIngredient("Tavuk göğsü veya but eti", "50g", "🍗"),
                RecipeIngredient("Taze bezelye (haşlanmış hafif ezilmiş)", "1 yemek kaşığı", "🟢"),
                RecipeIngredient("Havuç ve patates küpleri", "2 yemek kaşığı", "🥕"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Tavuk etini suda lokum gibi pişene kadar haşlayıp minik lifler halinde didikleyin.",
                "Havuç, patates ve bezelyeyi buharda pişirin.",
                "Tüm malzemeleri az tavuk suyu ile 5 dakika harmanlayın, ılık servis edin."
            ),
            doctorTip = "İpucu: Didiklenmiş tavuk lifleri 10. aydan itibaren çiğneme kaslarını güçlendirir ve katı gıda geçişini tamamlar.",
            emoji = "🍗"
        ),
        BabyRecipe(
            id = "lorlu_bebek_omleti",
            title = "Ispanaklı & Lor Peynirli Bebek Omleti",
            ageMonths = 10,
            ageLabel = "10+ Ay",
            prepTime = "10 dk",
            servings = "1 porsiyon",
            mealType = "Kahvaltı",
            textureStyle = "BLW (Parmak Besin)",
            tags = listOf("Demir", "Kalsiyum", "Hızlı & Pratik", "BLW"),
            ingredients = listOf(
                RecipeIngredient("Taze bebek ıspanak yaprağı (ince kıyılmış)", "3-4 yaprak", "🥬"),
                RecipeIngredient("Yumurta", "1 adet", "🍳"),
                RecipeIngredient("Tuzsuz bebek lor peyniri", "1 yemek kaşığı", "🧀"),
                RecipeIngredient("Sızma zeytinyağı", "1 tatlı kaşığı", "🫒")
            ),
            instructions = listOf(
                "Ispanakları tavada zeytinyağı ile 1 dakika söndürün.",
                "Kasede çırpılmış yumurta ve lor peynirini tavaya dökün.",
                "Kısık ateşte arkalı önlü pişirin, parmak dilimler halinde kesin."
            ),
            doctorTip = "İpucu: Ispanaktaki demir ile yumurtadaki protein mükemmel bir kahvaltı kombinasyonudur.",
            emoji = "🥬"
        )
    )

    fun getDailyMenu(): Map<String, BabyRecipe> {
        return mapOf(
            "Kahvaltı" to (allRecipes.firstOrNull { it.id == "avokado_tost" } ?: allRecipes[0]),
            "Öğle" to (allRecipes.firstOrNull { it.id == "kabak_sandal" } ?: allRecipes[2]),
            "İkindi" to (allRecipes.firstOrNull { it.id == "sutlu_brokoli_corbasi" } ?: allRecipes[5]),
            "Akşam" to (allRecipes.firstOrNull { it.id == "tatli_patates_kiyma" } ?: allRecipes[3])
        )
    }

    fun shuffleMenu(): Map<String, BabyRecipe> {
        val breakfasts = allRecipes.filter { it.mealType.contains("Kahvaltı") }.shuffled()
        val lunches = allRecipes.filter { it.mealType.contains("Öğle") }.shuffled()
        val others = allRecipes.filter { !it.mealType.contains("Kahvaltı") && !it.mealType.contains("Öğle") }.shuffled()

        return mapOf(
            "Kahvaltı" to (breakfasts.firstOrNull() ?: allRecipes[0]),
            "Öğle" to (lunches.firstOrNull() ?: allRecipes[2]),
            "İkindi" to (others.firstOrNull() ?: allRecipes[1]),
            "Akşam" to (others.getOrNull(1) ?: allRecipes[3])
        )
    }
}

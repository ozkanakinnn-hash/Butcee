package com.example.data

data class SurahInfo(
  val number: Int,
  val nameTurkish: String,
  val nameArabic: String,
  val meaningTurkish: String,
  val totalVerses: Int,
  val revelationType: String, // Mekkî / Medenî
  val juzNumber: Int,         // 1 - 30
  val juzRangeStr: String     // Örn: "1. Cüz" veya "1 - 3. Cüz"
)

data class AyahItem(
  val numberInSurah: Int,
  val arabicText: String,
  val turkishMeal: String
)

data class SurahDetailContent(
  val fullArabic: String,
  val fullPronunciation: String,
  val fullTranslation: String
)

object QuranData {

  val surahs: List<SurahInfo> = listOf(
    SurahInfo(1, "Fâtiha", "الفاتحة", "Açılış", 7, "Mekkî", 1, "1. Cüz"),
    SurahInfo(2, "Bakara", "البقرة", "Boğa / Sığır", 286, "Medenî", 1, "1 - 3. Cüz"),
    SurahInfo(3, "Âl-i İmrân", "آل عمران", "İmran Ailesi", 200, "Medenî", 3, "3 - 4. Cüz"),
    SurahInfo(4, "Nisâ", "النساء", "Kadınlar", 176, "Medenî", 4, "4 - 6. Cüz"),
    SurahInfo(5, "Mâide", "المائدة", "Sofra", 120, "Medenî", 6, "6 - 7. Cüz"),
    SurahInfo(6, "En'âm", "الأنعام", "Koyun / Sığır", 165, "Mekkî", 7, "7 - 8. Cüz"),
    SurahInfo(7, "A'râf", "الأعراف", "Yüksek Yerler", 206, "Mekkî", 8, "8 - 9. Cüz"),
    SurahInfo(8, "Enfâl", "الأنفال", "Ganimetler", 75, "Medenî", 9, "9 - 10. Cüz"),
    SurahInfo(9, "Tevbe", "التوبة", "Tövbe", 129, "Medenî", 10, "10 - 11. Cüz"),
    SurahInfo(10, "Yûnus", "يونس", "Yunus Peygamber", 109, "Mekkî", 11, "11. Cüz"),
    SurahInfo(11, "Hûd", "هود", "Hud Peygamber", 123, "Mekkî", 11, "11 - 12. Cüz"),
    SurahInfo(12, "Yûsuf", "يوسف", "Yusuf Peygamber", 111, "Mekkî", 12, "12 - 13. Cüz"),
    SurahInfo(13, "Ra'd", "الرعد", "Gök Gürültüsü", 43, "Medenî", 13, "13. Cüz"),
    SurahInfo(14, "İbrâhîm", "إبراهيم", "İbrahim Peygamber", 52, "Mekkî", 13, "13. Cüz"),
    SurahInfo(15, "Hicr", "الحجر", "Taşlı Vadi", 99, "Mekkî", 14, "14. Cüz"),
    SurahInfo(16, "Nahl", "النحل", "Bal Arısı", 128, "Mekkî", 14, "14. Cüz"),
    SurahInfo(17, "İsrâ", "الإسراء", "Gece Yürüyüşü", 111, "Mekkî", 15, "15. Cüz"),
    SurahInfo(18, "Kehf", "الكهف", "Mağara", 110, "Mekkî", 15, "15 - 16. Cüz"),
    SurahInfo(19, "Meryem", "مريم", "Meryem", 98, "Mekkî", 16, "16. Cüz"),
    SurahInfo(20, "Tâhâ", "طه", "Taha", 135, "Mekkî", 16, "16. Cüz"),
    SurahInfo(21, "Enbiyâ", "الأنبياء", "Peygamberler", 112, "Mekkî", 17, "17. Cüz"),
    SurahInfo(22, "Hac", "الحج", "Hac İbadeti", 78, "Medenî", 17, "17. Cüz"),
    SurahInfo(23, "Mü'minûn", "المؤمنون", "İnananlar", 118, "Mekkî", 18, "18. Cüz"),
    SurahInfo(24, "Nûr", "النور", "İlahi Işık", 64, "Medenî", 18, "18. Cüz"),
    SurahInfo(25, "Furkân", "الفرقان", "Hak ile Batılı Ayıran", 77, "Mekkî", 18, "18 - 19. Cüz"),
    SurahInfo(26, "Şuarâ", "الشعراء", "Şairler", 227, "Mekkî", 19, "19. Cüz"),
    SurahInfo(27, "Neml", "النمل", "Karınca", 93, "Mekkî", 19, "19 - 20. Cüz"),
    SurahInfo(28, "Kasas", "القصص", "Tarihi Kıssalar", 88, "Mekkî", 20, "20. Cüz"),
    SurahInfo(29, "Ankebût", "العنكبوت", "Örümcek", 69, "Mekkî", 20, "20 - 21. Cüz"),
    SurahInfo(30, "Rûm", "الروم", "Romalılar", 60, "Mekkî", 21, "21. Cüz"),
    SurahInfo(31, "Lokmân", "لقمان", "Lokman Hekim", 34, "Mekkî", 21, "21. Cüz"),
    SurahInfo(32, "Secde", "السجدة", "Secde", 30, "Mekkî", 21, "21. Cüz"),
    SurahInfo(33, "Ahzâb", "الأحزاب", "Gruplar / Fırkalar", 73, "Medenî", 21, "21 - 22. Cüz"),
    SurahInfo(34, "Sebe'", "سبإ", "Sebe Halkı", 54, "Mekkî", 22, "22. Cüz"),
    SurahInfo(35, "Fâtır", "فاطر", "Yaratan", 45, "Mekkî", 22, "22. Cüz"),
    SurahInfo(36, "Yâsîn", "يس", "Yasin (Kur'an'ın Kalbi)", 83, "Mekkî", 22, "22 - 23. Cüz"),
    SurahInfo(37, "Sâffât", "الصافات", "Sıra Sıra Duranlar", 182, "Mekkî", 23, "23. Cüz"),
    SurahInfo(38, "Sâd", "ص", "Sad", 88, "Mekkî", 23, "23. Cüz"),
    SurahInfo(39, "Zümer", "الزمر", "Zümreler / Gruplar", 75, "Mekkî", 23, "23 - 24. Cüz"),
    SurahInfo(40, "Mü'min (Gâfir)", "غافر", "Bağışlayan", 85, "Mekkî", 24, "24. Cüz"),
    SurahInfo(41, "Fussilet", "فصلت", "Açıklanmış", 54, "Mekkî", 24, "24 - 25. Cüz"),
    SurahInfo(42, "Şûrâ", "الشورى", "Danışma", 53, "Mekkî", 25, "25. Cüz"),
    SurahInfo(43, "Zuhruf", "الزخرف", "Altın Süsler", 89, "Mekkî", 25, "25. Cüz"),
    SurahInfo(44, "Duhân", "الدخان", "Duman", 59, "Mekkî", 25, "25. Cüz"),
    SurahInfo(45, "Câsiye", "الجاثية", "Diz Üstü Çökenler", 37, "Mekkî", 25, "25. Cüz"),
    SurahInfo(46, "Ahkâf", "الأحقاف", "Kum Tepeleri", 35, "Mekkî", 26, "26. Cüz"),
    SurahInfo(47, "Muhammed", "محمد", "Hz. Muhammed (s.a.v.)", 38, "Medenî", 26, "26. Cüz"),
    SurahInfo(48, "Fetih", "الفتح", "Büyük Zafer", 29, "Medenî", 26, "26. Cüz"),
    SurahInfo(49, "Hucurât", "الحجرات", "Odalar", 18, "Medenî", 26, "26. Cüz"),
    SurahInfo(50, "Kâf", "ق", "Kaf", 45, "Mekkî", 26, "26. Cüz"),
    SurahInfo(51, "Zâriyât", "الذاريات", "Rüzgarlar", 60, "Mekkî", 26, "26 - 27. Cüz"),
    SurahInfo(52, "Tûr", "الطور", "Tur Dağı", 49, "Mekkî", 27, "27. Cüz"),
    SurahInfo(53, "Necm", "النجم", "Kayan Yıldız", 62, "Mekkî", 27, "27. Cüz"),
    SurahInfo(54, "Kamer", "القمر", "Ay", 55, "Mekkî", 27, "27. Cüz"),
    SurahInfo(55, "Rahmân", "الرحمن", "Sonsuz Merhametli", 78, "Medenî", 27, "27. Cüz"),
    SurahInfo(56, "Vâkıa", "الواقعة", "Kıyamet Olayı", 96, "Mekkî", 27, "27. Cüz"),
    SurahInfo(57, "Hadîd", "الحديد", "Demir", 29, "Medenî", 27, "27. Cüz"),
    SurahInfo(58, "Mücâdele", "المجادلة", "Tartışan Kadın", 22, "Medenî", 28, "28. Cüz"),
    SurahInfo(59, "Haşr", "الحشر", "Sürgün ve Toplanma", 24, "Medenî", 28, "28. Cüz"),
    SurahInfo(60, "Mümtehine", "الممتحنة", "İmtihan Edilen Kadın", 13, "Medenî", 28, "28. Cüz"),
    SurahInfo(61, "Saff", "الصف", "Sıra Halinde", 14, "Medenî", 28, "28. Cüz"),
    SurahInfo(62, "Cum'a", "الجمعة", "Cuma Günü", 11, "Medenî", 28, "28. Cüz"),
    SurahInfo(63, "Münâfikûn", "المنافقون", "İki Yüzlüler", 11, "Medenî", 28, "28. Cüz"),
    SurahInfo(64, "Teğâbün", "التغابن", "Aldanma Günü", 18, "Medenî", 28, "28. Cüz"),
    SurahInfo(65, "Talâk", "الطلاق", "Boşanma Hükümleri", 12, "Medenî", 28, "28. Cüz"),
    SurahInfo(66, "Tahrîm", "التحريم", "Haram Kılma", 12, "Medenî", 28, "28. Cüz"),
    SurahInfo(67, "Mülk (Tebâreke)", "الملك", "Egemenlik ve Mülk", 30, "Mekkî", 29, "29. Cüz (Tebâreke)"),
    SurahInfo(68, "Kalem", "القلم", "Kalem", 52, "Mekkî", 29, "29. Cüz"),
    SurahInfo(69, "Hâkka", "الحاقة", "Kaçınılmaz Gerçek", 52, "Mekkî", 29, "29. Cüz"),
    SurahInfo(70, "Meâric", "المعارج", "Yükseliş Dereceleri", 44, "Mekkî", 29, "29. Cüz"),
    SurahInfo(71, "Nûh", "نوح", "Nuh Peygamber", 28, "Mekkî", 29, "29. Cüz"),
    SurahInfo(72, "Cin", "الجن", "Görünmeyen Varlıklar", 28, "Mekkî", 29, "29. Cüz"),
    SurahInfo(73, "Müzzemmil", "المزمل", "Örtünen", 20, "Mekkî", 29, "29. Cüz"),
    SurahInfo(74, "Müddessir", "المدثر", "Bürünen", 56, "Mekkî", 29, "29. Cüz"),
    SurahInfo(75, "Kıyâme", "القيامة", "Diriliş Günü", 40, "Mekkî", 29, "29. Cüz"),
    SurahInfo(76, "İnsân", "الإنسان", "İnsanoğlu", 31, "Medenî", 29, "29. Cüz"),
    SurahInfo(77, "Mürselât", "المرسلات", "Gönderilen Melekler", 50, "Mekkî", 29, "29. Cüz"),
    SurahInfo(78, "Nebe' (Amme)", "النبإ", "Büyük Haber", 40, "Mekkî", 30, "30. Cüz (Amme)"),
    SurahInfo(79, "Nâziât", "النازعات", "Söküp Çıkaranlar", 46, "Mekkî", 30, "30. Cüz"),
    SurahInfo(80, "Abese", "عبس", "Yüzünü Ekşitti", 42, "Mekkî", 30, "30. Cüz"),
    SurahInfo(81, "Tekvîr", "التكوير", "Dürülme", 29, "Mekkî", 30, "30. Cüz"),
    SurahInfo(82, "İnfitâr", "الانفطار", "Yarılma", 19, "Mekkî", 30, "30. Cüz"),
    SurahInfo(83, "Mutaffifîn", "المطففين", "Ölçüde Hile Yapanlar", 36, "Mekkî", 30, "30. Cüz"),
    SurahInfo(84, "İnşikâk", "الانشقاق", "Parçalanma", 25, "Mekkî", 30, "30. Cüz"),
    SurahInfo(85, "Bürûc", "البروج", "Burçlar", 22, "Mekkî", 30, "30. Cüz"),
    SurahInfo(86, "Târık", "الطارق", "Gece Gelen Yıldız", 17, "Mekkî", 30, "30. Cüz"),
    SurahInfo(87, "A'lâ", "الأعلى", "En Yüce Olan", 19, "Mekkî", 30, "30. Cüz"),
    SurahInfo(88, "Gâşiye", "الغاشية", "Her Şeyi Kuşatan", 26, "Mekkî", 30, "30. Cüz"),
    SurahInfo(89, "Fecr", "الفجر", "Tan Vakti", 30, "Mekkî", 30, "30. Cüz"),
    SurahInfo(90, "Beled", "البلد", "Kutsal Şehir (Mekke)", 20, "Mekkî", 30, "30. Cüz"),
    SurahInfo(91, "Şems", "الشمس", "Güneş", 15, "Mekkî", 30, "30. Cüz"),
    SurahInfo(92, "Leyl", "الليل", "Gece", 21, "Mekkî", 30, "30. Cüz"),
    SurahInfo(93, "Duhâ", "الضحى", "Kuşluk Vakti", 11, "Mekkî", 30, "30. Cüz"),
    SurahInfo(94, "İnşirâh", "الشرح", "Gönül Ferahlığı", 8, "Mekkî", 30, "30. Cüz"),
    SurahInfo(95, "Tîn", "التين", "İncir", 8, "Mekkî", 30, "30. Cüz"),
    SurahInfo(96, "Alak", "العلق", "Embriyo / Asılan Şey", 19, "Mekkî", 30, "30. Cüz"),
    SurahInfo(97, "Kadr", "القدر", "Kadir Gecesi", 5, "Mekkî", 30, "30. Cüz"),
    SurahInfo(98, "Beyyine", "البينة", "Açık Delil", 8, "Medenî", 30, "30. Cüz"),
    SurahInfo(99, "Zilzâl", "الزلزلة", "Büyük Deprem", 8, "Medenî", 30, "30. Cüz"),
    SurahInfo(100, "Âdiyât", "العاديات", "Koşan Atlar", 11, "Mekkî", 30, "30. Cüz"),
    SurahInfo(101, "Kâria", "القارعة", "Çarpan Kıyamet", 11, "Mekkî", 30, "30. Cüz"),
    SurahInfo(102, "Tekâsür", "التكاثر", "Çoklukla Övünme", 8, "Mekkî", 30, "30. Cüz"),
    SurahInfo(103, "Asr", "العصر", "Zaman / Çağ", 3, "Mekkî", 30, "30. Cüz"),
    SurahInfo(104, "Hümeze", "الهمزة", "Arkadan Çekiştiren", 9, "Mekkî", 30, "30. Cüz"),
    SurahInfo(105, "Fîl", "الفيل", "Fil Hadisesi", 5, "Mekkî", 30, "30. Cüz"),
    SurahInfo(106, "Kureyş", "قريش", "Kureyş Kabilesi", 4, "Mekkî", 30, "30. Cüz"),
    SurahInfo(107, "Mâûn", "الماعون", "Küçük Yardım", 7, "Mekkî", 30, "30. Cüz"),
    SurahInfo(108, "Kevser", "الكوثر", "Bol Nimet ve Havuz", 3, "Mekkî", 30, "30. Cüz"),
    SurahInfo(109, "Kâfirûn", "الكافرون", "İnkarcılar", 6, "Mekkî", 30, "30. Cüz"),
    SurahInfo(110, "Nasr", "النصر", "İlahi Yardım", 3, "Medenî", 30, "30. Cüz"),
    SurahInfo(111, "Tebbet (Mesed)", "المسد", "Kurusun / Lif", 5, "Mekkî", 30, "30. Cüz"),
    SurahInfo(112, "İhlâs", "الإخلاص", "Samimiyet ve Tevhid", 4, "Mekkî", 30, "30. Cüz"),
    SurahInfo(113, "Felak", "الفلق", "Sabahın Aydınlığı", 5, "Mekkî", 30, "30. Cüz"),
    SurahInfo(114, "Nâs", "الناس", "İnsanlar", 6, "Mekkî", 30, "30. Cüz")
  )

  // Pre-cached authentic verses with Diyanet-aligned translations for essential and frequently recited surahs
  fun getVersesForSurah(surahNumber: Int): List<AyahItem> {
    return when (surahNumber) {
      1 -> listOf(
        AyahItem(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Rahmân ve Rahîm olan Allah'ın adıyla."),
        AyahItem(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Hamd, âlemlerin Rabbi olan Allah'a mahsustur."),
        AyahItem(3, "الرَّحْمَٰنِ الرَّحِيمِ", "O, Rahmândır ve Rahîmdir."),
        AyahItem(4, "مَالِكِ يَوْمِ الدِّينِ", "Din (hesap ve ceza) gününün tek sahibidir."),
        AyahItem(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "(Rabbimiz!) Yalnız sana kulluk eder ve yalnız senden yardım dileriz."),
        AyahItem(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Bizi dosdoğru yola ilet."),
        AyahItem(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Kendilerine lütufta bulunduğun kimselerin yoluna; gazaba uğramışların ve sapmışların yoluna değil. (Âmin)")
      )
      93 -> listOf(
        AyahItem(1, "وَالضُّحَىٰ", "Kuşluk vaktine andolsun,"),
        AyahItem(2, "وَاللَّيْلِ إِذَا سَجَىٰ", "Karanlığı çöktüğü zaman geceye andolsun ki,"),
        AyahItem(3, "مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ", "Rabbin seni ne terk etti, ne de sana darıldı."),
        AyahItem(4, "وَلَلْآخِرَةُ خَيْرٌ لَّكَ مِنَ الْأُولَىٰ", "Elbette ahiret senin için dünyadan çok daha hayırlıdır."),
        AyahItem(5, "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ", "Rabbin sana verecek ve sen hoşnut olacaksın."),
        AyahItem(6, "أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ", "O seni yetim bulup barındırmadı mı?"),
        AyahItem(7, "وَوَجَدَكَ ضَالًّا فَهَدَىٰ", "Seni yolunu ararken bulup hidayete erdirmedi mi?"),
        AyahItem(8, "وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ", "Seni yoksul bulup zengin kılmadı mı?"),
        AyahItem(9, "فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ", "Öyleyse sakın yetimi ezme!"),
        AyahItem(10, "وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ", "Bir şey isteyeni asla azarlama!"),
        AyahItem(11, "وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ", "Ve Rabbinin nimetini şükranla anlat!")
      )
      94 -> listOf(
        AyahItem(1, "أَلَمْ نَشْرَحْ لَكَ صَدْرَكَ", "Biz senin göğsünü yarıp genişletmedik mi?"),
        AyahItem(2, "وَوَضَعْنَا عَنكَ وِزْرَكَ", "Belini büken ağır yükünü üzerinden indirmedik mi?"),
        AyahItem(3, "الَّذِي أَنقَضَ ظَهْرَكَ", "O yük ki senin belini bükmüştü."),
        AyahItem(4, "وَرَفَعْنَا لَكَ ذِكْرَكَ", "Senin şanını ve namını yüceltmedik mi?"),
        AyahItem(5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "Şüphesiz her zorlukla beraber bir kolaylık vardır."),
        AyahItem(6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Evet, muhakkak her zorlukla beraber bir kolaylık vardır."),
        AyahItem(7, "فَإِذَا فَرَغْتَ فَانصَبْ", "Öyleyse bir işi bitirince diğerine koyul."),
        AyahItem(8, "وَإِلَىٰ رَبِّكَ فَارْغَب", "Ve yalnız Rabbine yönel, O'na rağbet et.")
      )
      97 -> listOf(
        AyahItem(1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", "Şüphesiz biz onu (Kur'an'ı) Kadir Gecesi'nde indirdik."),
        AyahItem(2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", "Kadir Gecesi'nin ne olduğunu sen nereden bileceksin?"),
        AyahItem(3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "Kadir Gecesi, bin aydan daha hayırlıdır."),
        AyahItem(4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", "Melekler ve Ruh (Cebrail), Rablerinin izniyle her türlü iş için o gece iner de iner."),
        AyahItem(5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", "O gece, tan yeri ağarıncaya kadar tam bir esenlik ve selamettir.")
      )
      103 -> listOf(
        AyahItem(1, "وَالْعَصْرِ", "Asra (zamana) andolsun ki,"),
        AyahItem(2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "İnsan gerçekten büyük bir ziyan içindedir."),
        AyahItem(3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Ancak iman edip salih ameller işleyenler, birbirlerine hakkı ve sabrı tavsiye edenler müstesnadır.")
      )
      108 -> listOf(
        AyahItem(1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", "Şüphesiz biz sana bol nimet ve Kevser'i verdik."),
        AyahItem(2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", "Şimdi sen yalnız Rabbin için namaz kıl ve kurban kes."),
        AyahItem(3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", "Asıl soyu kesik olan, sana buğzeden (düşmanlık eden) kimsedir.")
      )
      112 -> listOf(
        AyahItem(1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "De ki: O Allah tektir."),
        AyahItem(2, "اللَّهُ الصَّمَدُ", "Allah Samed'dir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir)."),
        AyahItem(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "O doğurmamış ve doğmamıştır."),
        AyahItem(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Hiçbir şey O'na denk ve benzer olamaz.")
      )
      113 -> listOf(
        AyahItem(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "De ki: Sabahın Rabbine sığınırım,"),
        AyahItem(2, "مِن شَرِّ مَا خَلَقَ", "Yarattığı şeylerin şerrinden,"),
        AyahItem(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Karanlığı çöktüğü zaman gecenin şerrinden,"),
        AyahItem(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "Düğümlere üfleyen büyücülerin şerrinden,"),
        AyahItem(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Ve haset ettiği vakit hasetçinin şerrinden.")
      )
      114 -> listOf(
        AyahItem(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "De ki: İnsanların Rabbine sığınırım,"),
        AyahItem(2, "مَلِكِ النَّاسِ", "İnsanların yegâne Hükümdarına,"),
        AyahItem(3, "إِلَٰهِ النَّاسِ", "İnsanların gerçek İlahına,"),
        AyahItem(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "O sinsi ve gizlice fısıldayan vesvesecinin şerrinden,"),
        AyahItem(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "O ki insanların kalplerine vesvese sokar;"),
        AyahItem(6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "Gerek cinlerden gerekse insanlardan olanların şerrinden.")
      )
      67 -> listOf(
        AyahItem(1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Hükümranlık elinde olan Allah ne yücedir! O, her şeye hakkıyla kadirdir."),
        AyahItem(2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", "O, hanginizin daha güzel amel yapacağını sınamak için ölümü ve hayatı yaratandır. O mutlak güç sahibidir, çok bağışlayandır."),
        AyahItem(3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ", "O, yedi göğü birbiriyle tam bir uyum içinde yaratandır. Rahmân'ın yaratışında hiçbir düzensizlik göremezsin."),
        AyahItem(4, "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Gözünü bir kez daha çevir bak; göz sana yorulmuş ve aradığını bulamamış olarak geri dönecektir.")
      )
      36 -> listOf(
        AyahItem(1, "يس", "Yâsîn."),
        AyahItem(2, "وَالْقُرْآنِ الْحَكِيمِ", "Hikmet dolu Kur'an'a andolsun ki,"),
        AyahItem(3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", "Sen şüphesiz peygamberlerdensin,"),
        AyahItem(4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", "Dosdoğru bir yol üzerindesin."),
        AyahItem(5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", "Bu Kur'an, mutlak güç ve engin merhamet sahibi Allah tarafından indirilmiştir.")
      )
      else -> listOf(
        AyahItem(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Rahmân ve Rahîm olan Allah'ın adıyla."),
        AyahItem(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Hamd, âlemlerin Rabbi olan Allah'a mahsustur."),
        AyahItem(3, "إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ", "Şüphesiz bu Kur'an, insanları en doğru ve sağlam yola iletir.")
      )
    }
  }

  fun getSurahDetailContent(surahNumber: Int, surahName: String): SurahDetailContent {
    return when (surahNumber) {
      1 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾ الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾ مَالِكِ يَوْمِ الدِّينِ ﴿٤﴾ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٥﴾ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٦﴾ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ ﴿٧﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Elhamdü lillâhi rabbil'âlemîn.
          2. Errahmânir'rahîm.
          3. Mâliki yevmiddîn.
          4. İyyâke na'büdü ve iyyâke neste'în.
          5. İhdinâs-sırâtel müstekîm.
          6. Sırâtallezîne en'amte aleyhim.
          7. Ğayrilmağdûbi aleyhim veleddâllîn. (Âmîn)
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Hamd, âlemlerin Rabbi olan Allah'a mahsustur.
          2. O, Rahmândır ve Rahîmdir.
          3. Din ve hesap gününün yegâne sahibidir.
          4. (Rabbimiz!) Yalnız sana ibadet eder ve yalnız senden yardım dileriz.
          5. Bizi dosdoğru yola ilet.
          6. Kendilerine lütuf ve nimet verdiğin kimselerin yoluna;
          7. Gazaba uğramışların ve sapmışların yoluna değil. (Âmin)
        """.trimIndent()
      )

      112 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ هُوَ اللَّهُ أَحَدٌ ﴿١﴾ اللَّهُ الصَّمَدُ ﴿٢﴾ لَمْ يَلِدْ وَلَمْ يُولَدْ ﴿٣﴾ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ ﴿٤﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Kul hüvellâhü ehad.
          2. Allâhüs-samed.
          3. Lem yelid ve lem yûled.
          4. Ve lem yekün lehû küfüven ehad.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. De ki: O Allah tektir (eşi ve benzeri yoktur).
          2. Allah Samed'dir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir).
          3. O doğurmamış ve doğmamıştır.
          4. Hiçbir şey O'na denk ve benzer olamaz.
        """.trimIndent()
      )

      113 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ﴿١﴾ مِن شَرِّ مَا خَلَقَ ﴿٢﴾ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ﴿٣﴾ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ﴿٤﴾ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ ﴿٥﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Kul e'ûzü birabbil-felak.
          2. Min şerri mâ halak.
          3. Ve min şerri ğâsikın izâ vekab.
          4. Ve min şerrin-neffâsâti fil-'ukad.
          5. Ve min şerri hâsidin izâ hased.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. De ki: Sabahın Rabbine sığınırım,
          2. Yarattığı şeylerin şerrinden,
          3. Karanlığı çöktüğü zaman gecenin şerrinden,
          4. Düğümlere üfleyen büyücülerin şerrinden,
          5. Ve haset ettiği vakit hasetçinin şerrinden.
        """.trimIndent()
      )

      114 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ أَعُوذُ بِرَبِّ النَّاسِ ﴿١﴾ مَلِكِ النَّاسِ ﴿٢﴾ إِلَٰهِ النَّاسِ ﴿٣﴾ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ﴿٤﴾ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ﴿٥﴾ مِنَ الْجِنَّةِ وَالنَّاسِ ﴿٦﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Kul e'ûzü birabbin-nâs.
          2. Melikin-nâs.
          3. İlâhin-nâs.
          4. Min şerril-vesvâsil-hannâs.
          5. Ellezî yüvesvisü fî sudûrin-nâs.
          6. Minel-cinneti ven-nâs.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. De ki: İnsanların Rabbine sığınırım,
          2. İnsanların mutlak Hükümdarına,
          3. İnsanların gerçek İlahına,
          4. O sinsi vesvesecinin şerrinden,
          5. O ki insanların kalplerine vesvese sokar;
          6. Gerek cinlerden gerekse insanlardan olanların şerrinden.
        """.trimIndent()
      )

      108 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nإِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ﴿١﴾ فَصَلِّ لِرَبِّكَ وَانْحَرْ ﴿٢﴾ إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ ﴿٣﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. İnnâ a'taynâkel-kevser.
          2. Fesalli lirabbike venhar.
          3. İnne şânieke hüvel-ebter.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Şüphesiz biz sana bol hayır ve Kevser'i verdik.
          2. Şimdi sen yalnız Rabbin için namaz kıl ve kurban kes.
          3. Asıl soyu kesik olan, sana buğzedip düşmanlık besleyendir.
        """.trimIndent()
      )

      103 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nوَالْعَصْرِ ﴿١﴾ إِنَّ الْإِنسَانَ لَفِي خُسْرٍ ﴿٢﴾ إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ ﴿٣﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Vel-'asr.
          2. İnnel-insâne lefî husr.
          3. İllellezîne âmenû ve 'amilus-sâlihâti ve tevâsav bil-hakkı ve tevâsav bis-sabr.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Asra (zamana) andolsun ki,
          2. İnsan gerçekten büyük bir ziyan içindedir.
          3. Ancak iman edip salih ameller işleyenler, birbirlerine hakkı ve sabrı tavsiye edenler müstesnadır.
        """.trimIndent()
      )

      97 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nإِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ ﴿١﴾ وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ ﴿٢﴾ لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ ﴿٣﴾ تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ ﴿٤﴾ سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ ﴿٥﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. İnnâ enzelnâhü fî leyletil-kadr.
          2. Ve mâ edrâke mâ leyletül-kadr.
          3. Leyletül-kadri hayrum-min elfi şehr.
          4. Tenezzelül-melâiketü ver-rûhu fîhâ bi-izni rabbihim min külli emr.
          5. Selâmün hiye hattâ matle'il-fecr.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Şüphesiz biz onu (Kur'an'ı) Kadir Gecesi'nde indirdik.
          2. Kadir Gecesi'nin ne olduğunu sen nereden bileceksin?
          3. Kadir Gecesi, bin aydan daha hayırlıdır.
          4. Melekler ve Ruh (Cebrail), Rablerinin izniyle her türlü iş için o gece iner de iner.
          5. O gece, tan yeri ağarıncaya kadar tam bir esenlik ve selamettir.
        """.trimIndent()
      )

      94 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nأَلَمْ نَشْرَحْ لَكَ صَدْرَكَ ﴿١﴾ وَوَضَعْنَا عَنكَ وِزْرَكَ ﴿٢﴾ الَّذِي أَنقَضَ ظَهْرَكَ ﴿٣﴾ وَرَفَعْنَا لَكَ ذِكْرَكَ ﴿٤﴾ فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ﴿٥﴾ إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴿٦﴾ فَإِذَا فَرَغْتَ فَانصَبْ ﴿٧﴾ وَإِلَىٰ رَبِّكَ فَارْغَب ﴿٨﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Elem neşrah leke sadrak.
          2. Ve veda'nâ 'anke vizrak.
          3. Ellezî enkada zahrak.
          4. Ve refa'nâ leke zikrak.
          5. Fe-inne me'al-'usri yusrâ.
          6. İnne me'al-'usri yusrâ.
          7. Fe-izâ ferağte fensab.
          8. Ve ilâ rabbike ferğab.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Biz senin göğsünü açıp genişletmedik mi?
          2. Belini büken ağır yükünü üzerinden indirmedik mi?
          3. O yük ki senin belini bükmüştü.
          4. Senin şanını ve namını yüceltmedik mi?
          5. Şüphesiz her zorlukla beraber bir kolaylık vardır.
          6. Evet, muhakkak her zorlukla beraber bir kolaylık vardır.
          7. Öyleyse bir işi bitirince hemen diğerine koyul;
          8. Ve yalnız Rabbine yönel, O'na rağbet et.
        """.trimIndent()
      )

      93 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nوَالضُّحَىٰ ﴿١﴾ وَاللَّيْلِ إِذَا سَجَىٰ ﴿٢﴾ مَا وَدَّعَكَ رَبُّكَ وَمَا قَلَىٰ ﴿٣﴾ وَلَلْآخِرَةُ خَيْرٌ لَّكَ مِنَ الْأُولَىٰ ﴿٤﴾ وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ ﴿٥﴾ أَلَمْ يَجِدْكَ يَتِيمًا فَآوَىٰ ﴿٦﴾ وَوَجَدَكَ ضَالًّا فَهَدَىٰ ﴿٧﴾ وَوَجَدَكَ عَائِلًا فَأَغْنَىٰ ﴿٨﴾ فَأَمَّا الْيَتِيمَ فَلَا تَقْهَرْ ﴿٩﴾ وَأَمَّا السَّائِلَ فَلَا تَنْهَرْ ﴿١٠﴾ وَأَمَّا بِنِعْمَةِ رَبِّكَ فَحَدِّثْ ﴿١١﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Ved-duhâ. 2. Vel-leyli izâ secâ. 3. Mâ vedde'ake rabbüke ve mâ kalâ. 4. Ve lel-âhiratü hayrul-leke minel-ûlâ. 5. Ve lesevfe yu'tîke rabbüke feterdâ. 6. Elem yecidke yetîmen fe-âvâ. 7. Ve vecedeke dâllen fe-hedâ. 8. Ve vecedeke 'âilen fe-ağnâ. 9. Fe-emmel-yetîme felâ takher. 10. Ve emmes-sâile felâ tenher. 11. Ve emmâ bini'meti rabbike fehaddis.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Kuşluk vaktine andolsun, 2. Karanlığı çöktüğü zaman geceye andolsun ki, 3. Rabbin seni terk etmedi ve sana darılmadı. 4. Elbette ahiret senin için dünyadan hayırlıdır. 5. Rabbin sana verecek ve sen hoşnut olacaksın. 6. O seni yetim bulup barındırmadı mı? 7. Yolunu ararken bulup hidayete erdirmedi mi? 8. Yoksul bulup zengin kılmadı mı? 9. Öyleyse sakın yetimi ezme! 10. Bir şey isteyeni asla azarlama! 11. Ve Rabbinin nimetini şükranla anlat!
        """.trimIndent()
      )

      109 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nقُلْ يَا أَيُّهَا الْكَافِرُونَ ﴿١﴾ لَا أَعْبُدُ مَا تَعْبُدُونَ ﴿٢﴾ وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٣﴾ وَلَا أَنَا عَابِدٌ مَّا عَبَدتُّمْ ﴿٤﴾ وَلَا أَنتُمْ عَابِدُونَ مَا أَعْبُدُ ﴿٥﴾ لَكُمْ دِينُكُمْ وَلِيَ دِينِ ﴿٦﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Kul yâ eyyühel-kâfirûn.
          2. Lâ a'büdü mâ ta'büdûn.
          3. Ve lâ entüm 'âbidûne mâ a'büd.
          4. Ve lâ ene 'âbidüm-mâ 'abettüm.
          5. Ve lâ entüm 'âbidûne mâ a'büd.
          6. Leküm dînüküm veliye dîn.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. De ki: Ey inkârcılar!
          2. Ben sizin taptıklarınıza tapmam.
          3. Siz de benim taptığıma tapacak değilsiniz.
          4. Ben sizin taptıklarınıza asla tapacak değilim.
          5. Siz de benim taptığıma tapacak değilsiniz.
          6. Sizin dininiz size, benim dinim banadır.
        """.trimIndent()
      )

      106 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nلِإِيلَافِ قُرَيْشٍ ﴿١﴾ إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ ﴿٢﴾ فَلْيَعْبُدُوا رَبَّ هَٰذَا الْبَيْتِ ﴿٣﴾ الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ ﴿٤﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Li-îlâfi kurayş.
          2. Îlâfihim rıhleteş-şitâi ves-sayf.
          3. Fel-ya'büdû rabbe hâzel-beyt.
          4. Ellezî et'amehüm min cû'in ve âmenehüm min havf.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Kureyş'in güven ve uzlaşmasını sağladığı için,
          2. Kış ve yaz yolculuklarında onları güvenli kıldığı için,
          3. Artık bu Beyt'in (Kâbe'nin) Rabbine kulluk etsinler;
          4. O ki, onları açlıktan doyurmuş ve her türlü korkudan emin kılmıştır.
        """.trimIndent()
      )

      105 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nأَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ ﴿١﴾ أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ ﴿٢﴾ وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ ﴿٣﴾ تَرْمِيهِم بِحِجَارَةٍ مِّن سِجِّيلٍ ﴿٤﴾ فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ ﴿٥﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Elem tera keyfe fe'ale rabbüke bi-eshâbil-fîl.
          2. Elem yec'al keydehüm fî tadlîl.
          3. Ve ersele 'aleyhim tayran ebâbîl.
          4. Termîhim bi-hicâratim-min siccîl.
          5. Fece'alehüm ke-'asfim-me'kûl.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Görmedin mi Rabbin fil sahiplerine ne yaptı?
          2. Onların tuzaklarını boşa çıkarmadı mı?
          3. Üzerlerine sürü sürü Ebabil kuşlarını saldı.
          4. Onlara pişmiş çamurdan taşlar atıyorlardı.
          5. Nihayet onları çiğnenmiş ekin yaprağı gibi kıldı.
        """.trimIndent()
      )

      36 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nيس ﴿١﴾ وَالْقُرْآنِ الْحَكِيمِ ﴿٢﴾ إِنَّكَ لَمِنَ الْمُرْسَلِينَ ﴿٣﴾ عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ ﴿٤﴾ تَنزِيلَ الْعَزِيزِ الرَّحِيمِ ﴿٥﴾ لِتُنذِرَ قَوْمًا مَّا أُنذِرَ آبَاؤُهُمْ فَهُمْ غَافِلُونَ ﴿٦﴾ لَقَدْ حَقَّ الْقَوْلُ عَلَىٰ أَكْثَرِهِمْ فَهُمْ لَا يُؤْمِنُونَ ﴿٧﴾ إِنَّا جَعَلْنَا فِي أَعْنَاقِهِمْ أَغْلَالًا فَهِيَ إِلَى الْأَذْقَانِ فَهُم مُّقْمَحُونَ ﴿٨﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Yâsîn.
          2. Vel-kur'ânil-hakîm.
          3. İnneke leminel-mürselîn.
          4. 'Alâ sırâtım-müstekîm.
          5. Tenzîlel-'azîzir-rahîm.
          6. Li-tünzira kavmem-mâ ünzira âbâühüm fehüm ğâfilûn.
          7. Lekad hakkal-kavlü 'alâ ekserihim fehüm lâ yü'minûn.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Yâsîn.
          2. Hikmet dolu Kur'an'a andolsun ki,
          3. Sen şüphesiz gönderilmiş peygamberlerdensin.
          4. Dosdoğru bir yol üzerindesin.
          5. Bu Kur'an, mutlak güç ve engin merhamet sahibi Allah tarafından indirilmiştir.
          6. Ataları uyarılmamış, bu yüzden kendileri de gaflet içinde kalmış bir toplumu uyarman için gönderildin.
          7. Andolsun ki onların çoğu üzerine o azap hükmü hak olmuştur.
        """.trimIndent()
      )

      67 -> SurahDetailContent(
        fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\nتَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ﴿١﴾ الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ ﴿٢﴾ الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ ﴿٣﴾ ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ ﴿٤﴾",
        fullPronunciation = """
          Bismillâhirrahmânirrahîm.
          1. Tebârakellezî bi-yedihil-mülkü ve hüve 'alâ külli şey'in kadîr.
          2. Ellezî halakal-mevte vel-hayâte li-yeblüveküm eyyüküm ahsenü 'amelâ, ve hüvel-'azîzül-ğafûr.
          3. Ellezî halaka seb'a semâvâtin tıbâkâ, mâ terâ fî halkır-rahmâni min tefâvüt, ferci'ıl-basara hel terâ min fütûr.
          4. Sümmerci'ıl-basara kerrateyni yenkalib ileykel-basaru hâsi-ev-ve hüve hasîr.
        """.trimIndent(),
        fullTranslation = """
          Rahmân ve Rahîm olan Allah'ın adıyla.
          1. Hükümranlık elinde olan Allah yüceler yücesidir; O, her şeye hakkıyla kadirdir.
          2. O, hanginizin daha güzel amel yapacağını sınamak için ölümü ve hayatı yaratandır. O, mutlak güç sahibidir, çok bağışlayandır.
          3. O, yedi göğü birbiriyle tam bir uyum içinde yaratandır. Rahmân'ın yaratışında hiçbir düzensizlik göremezsin. Gözünü çevir de bak, bir çatlak veya kusur görebilir misin?
          4. Sonra gözünü tekrar tekrar çevir bak; göz sana yorulmuş ve aradığını bulamamış olarak geri dönecektir.
        """.trimIndent()
      )

      else -> {
        val ayahs = getVersesForSurah(surahNumber)
        val arabicCombined = StringBuilder()
        if (surahNumber != 9) {
          arabicCombined.append("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n")
        }
        val pronunciationCombined = StringBuilder()
        pronunciationCombined.append("Bismillâhirrahmânirrahîm.\n")
        val translationCombined = StringBuilder()
        translationCombined.append("Rahmân ve Rahîm olan Allah'ın adıyla.\n\n")

        ayahs.forEach { ayah ->
          arabicCombined.append("${ayah.arabicText} ﴿${ayah.numberInSurah}﴾ ")
          pronunciationCombined.append("${ayah.numberInSurah}. ${ayah.turkishMeal.take(30)}... [Arapça tilâvet]\n")
          translationCombined.append("${ayah.numberInSurah}. ${ayah.turkishMeal}\n")
        }

        SurahDetailContent(
          fullArabic = arabicCombined.toString().trim(),
          fullPronunciation = pronunciationCombined.toString().trim(),
          fullTranslation = translationCombined.toString().trim()
        )
      }
    }
  }
}

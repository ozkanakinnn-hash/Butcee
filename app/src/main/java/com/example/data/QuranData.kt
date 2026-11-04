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

  /** Surenin tüm ayetleri: Arapça metin + Diyanet İşleri meali (assets/quran/quran.tsv). */
  fun getVersesForSurah(surahNumber: Int): List<AyahItem> =
    QuranText.surah(surahNumber).map { AyahItem(it.ayah, it.arabic, it.meal) }

  fun getSurahDetailContent(surahNumber: Int, surahName: String): SurahDetailContent {
    val tr = getSurahDetailContentTr(surahNumber, surahName)
    if (!com.example.i18n.AppLanguage.isEnglish) return tr
    // İngilizce meal gösterilmez; yalnızca Arapça metin + Latin harfli okunuş.
    return tr.copy(fullPronunciation = englishTransliteration(surahNumber), fullTranslation = "")
  }

  /** "1 - 3. Cüz" -> İngilizcede "Juz 1 - 3" */
  fun juzLabel(surah: SurahInfo): String =
    if (com.example.i18n.AppLanguage.isEnglish) "Juz " + surah.juzRangeStr.removeSuffix(". Cüz") else surah.juzRangeStr

  fun revelationLabel(surah: SurahInfo): String =
    if (com.example.i18n.AppLanguage.isEnglish) (if (surah.revelationType.startsWith("Mek")) "Meccan" else "Medinan") else surah.revelationType

  /** İngilizce sure adı anlamı (dil English ise), aksi halde Türkçe anlam. */
  fun meaningOf(surah: SurahInfo): String =
    if (com.example.i18n.AppLanguage.isEnglish) EnglishContent.surahMeaning(surah.number) ?: surah.meaningTurkish else surah.meaningTurkish

  private fun getSurahDetailContentTr(surahNumber: Int, @Suppress("UNUSED_PARAMETER") surahName: String): SurahDetailContent {
    val rows = QuranText.surah(surahNumber)
    if (rows.isEmpty()) {
      return SurahDetailContent("", "Sure metni yüklenemedi.", "")
    }
    // Fâtiha'da besmele 1. ayettir; Tevbe'de besmele okunmaz; diğerlerinde başta ayrıca yazılır.
    val withBasmala = surahNumber != 1 && surahNumber != 9
    val arabic = StringBuilder()
    val pron = StringBuilder()
    val meal = StringBuilder()
    if (withBasmala) {
      arabic.append(BASMALA_AR).append("\n")
      pron.append("Bismillâhirrahmânirrahîm.\n")
      meal.append("Rahmân ve Rahîm olan Allah'ın adıyla.\n")
    }
    rows.forEach { r ->
      arabic.append(r.arabic).append(" ﴿").append(toArabicDigits(r.ayah)).append("﴾ ")
      pron.append(r.ayah).append(". ").append(r.pronunciation).append("\n")
      // Eski Diyanet baskısında birkaç ayete tek meal yazılmıştır; tekrarlar boş bırakıldı.
      if (r.meal.isNotBlank()) meal.append(r.ayah).append(". ").append(r.meal).append("\n")
    }
    return SurahDetailContent(
      fullArabic = arabic.toString().trim(),
      fullPronunciation = pron.toString().trim(),
      fullTranslation = meal.toString().trim()
    )
  }

  /** İngilizce modda: Latin harfli (İngilizce) okunuş. */
  fun englishTransliteration(surahNumber: Int): String {
    val rows = QuranText.surah(surahNumber)
    val sb = StringBuilder()
    if (surahNumber != 1 && surahNumber != 9) sb.append("Bismi Allahi alrrahmani alrraheemi\n")
    rows.forEach { sb.append(it.ayah).append(". ").append(it.english).append("\n") }
    return sb.toString().trim()
  }

  private const val BASMALA_AR = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"

  private fun toArabicDigits(n: Int): String =
    n.toString().map { c -> '\u0660' + (c - '0') }.joinToString("")
}

/** Kur'an metni: assets/quran/quran.tsv (sure, ayet, Arapça, Türkçe okunuş, İngilizce okunuş, Diyanet meali). */
object QuranText {
  data class Row(val surah: Int, val ayah: Int, val arabic: String, val pronunciation: String, val english: String, val meal: String, val page: Int = 1, val juz: Int = 1)

  @Volatile private var bySurah: Map<Int, List<Row>>? = null

  fun surah(n: Int): List<Row> = load()[n].orEmpty()

  const val PAGE_COUNT = 604

  @Volatile private var byPage: Map<Int, List<Row>>? = null

  /** Medine mushafı sayfa düzeni (1-604). */
  fun page(p: Int): List<Row> {
    val m = byPage ?: load().values.flatten().sortedWith(compareBy<Row>({ it.surah }, { it.ayah })).groupBy { it.page }.also { byPage = it }
    return m[p].orEmpty()
  }

  fun firstPageOfSurah(n: Int): Int = surah(n).firstOrNull()?.page ?: 1

  fun firstPageOfJuz(j: Int): Int =
    load().values.asSequence().flatten().filter { it.juz == j }.minOfOrNull { it.page } ?: 1

  /** Uygulama açılışında arka planda çağrılır; ilk sure açılışında bekleme olmaz. */
  fun preload(context: android.content.Context) { load(context) }

  @Synchronized
  private fun load(context: android.content.Context? = null): Map<Int, List<Row>> {
    bySurah?.let { return it }
    val ctx = context ?: com.example.AppHolder.context
    val map = HashMap<Int, MutableList<Row>>(128)
    try {
      ctx.assets.open("quran/quran.tsv").bufferedReader(Charsets.UTF_8).useLines { lines ->
        lines.forEach { line ->
          val f = line.split('\t')
          if (f.size >= 6) {
            val r = Row(f[0].toInt(), f[1].toInt(), f[2], f[3], f[4], f[5], f.getOrNull(6)?.toIntOrNull() ?: 1, f.getOrNull(7)?.toIntOrNull() ?: 1)
            map.getOrPut(r.surah) { ArrayList() }.add(r)
          }
        }
      }
    } catch (_: Exception) {
      return emptyMap()
    }
    return map.also { bySurah = it }
  }
}

package com.example.data

data class SpiritualItem(
  val day: Int,
  val verseArabic: String,
  val verseTurkish: String,
  val verseSource: String,
  val hadithTurkish: String,
  val hadithSource: String,
  val prayerTurkish: String,
  val prayerSource: String,
  val esmaArabic: String,
  val esmaTurkish: String,
  val esmaMeaning: String
)

object SpiritualQuotes {
  val list: List<SpiritualItem> = listOf(
    SpiritualItem(
      day = 1,
      verseArabic = "شَهْرُ رَمَضَانَ الَّذِي أُنزِلَ فِيهِ الْقُرْآنُ هُدًى لِّلنَّاسِ",
      verseTurkish = "Ramazan ayı, insanlara yol gösterici, doğrunun ve doğruyu eğriden ayırmanın açık delilleri olarak Kur'an'ın indirildiği aydır.",
      verseSource = "Bakara Suresi, 185. Ayet",
      hadithTurkish = "Kim inanarak ve sevabını yalnız Allah'tan bekleyerek Ramazan orucunu tutarsa, geçmiş günahları bağışlanır.",
      hadithSource = "Buhârî, Savm 6; Müslim, Müsâfirîn 175",
      prayerTurkish = "Allah'ım! Sen affedicisin, affetmeyi seversin, beni ve sevdiklerimi de bağışla.",
      prayerSource = "Tirmizî, Daavât 84",
      esmaArabic = "الرَّحْمَنُ",
      esmaTurkish = "Er-Rahmân",
      esmaMeaning = "Dünyada inanan veya inanmayan bütün mahlukata merhamet eden, rızık veren."
    ),
    SpiritualItem(
      day = 2,
      verseArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا كُتِبَ عَلَيْكُمُ الصِّيَامُ كَمَا كُتِبَ عَلَى الَّذِينَ مِن قَبْلِكُمْ لَعَلَّكُمْ تَتَّقُونَ",
      verseTurkish = "Ey iman edenler! Allah'a karşı gelmekten sakınasınız diye, oruç sizden öncekilere farz kılındığı gibi size de farz kılındı.",
      verseSource = "Bakara Suresi, 183. Ayet",
      hadithTurkish = "Oruç bir kalkandır. Oruçlu kimse kötü söz söylemesin ve cahillik etmesin. Biri kendisiyle dövüşecek veya sövecek olursa 'Ben oruçluyum' desin.",
      hadithSource = "Buhârî, Savm 9",
      prayerTurkish = "Allah'ım! Senin rızan için oruç tuttum, Sana inandım, Sana güvendim ve Senin rızkınla iftar ettim.",
      prayerSource = "Ebû Dâvûd, Savm 22",
      esmaArabic = "الرَّحِيمُ",
      esmaTurkish = "Er-Rahîm",
      esmaMeaning = "Ahirette sadece mümin kullarına hususi merhamet gösteren."
    ),
    SpiritualItem(
      day = 3,
      verseArabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
      verseTurkish = "Kullarım sana beni sorduklarında bilsinler ki şüphesiz ben onlara çok yakınım. Bana dua edenin duasına icabet ederim.",
      verseSource = "Bakara Suresi, 186. Ayet",
      hadithTurkish = "Üç kimsenin duası geri çevrilmez: İftar edinceye kadar oruçlunun duası, adil devlet başkanının duası ve mazlumun duası.",
      hadithSource = "Tirmizî, Daavât 48",
      prayerTurkish = "Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi cehennem azabından koru.",
      prayerSource = "Bakara Suresi, 201. Ayet",
      esmaArabic = "الْغَفَّارُ",
      esmaTurkish = "El-Gaffâr",
      esmaMeaning = "Kullarının günahlarını örten, bağışlayan ve ayıplarını gizleyen."
    ),
    SpiritualItem(
      day = 4,
      verseArabic = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ",
      verseTurkish = "Sabır ve namaz ile Allah'tan yardım isteyin. Şüphesiz bu, saygıyla boyun eğenlerden başkasına ağır gelir.",
      verseSource = "Bakara Suresi, 45. Ayet",
      hadithTurkish = "Sahur yapınız, zira sahurda bereket vardır.",
      hadithSource = "Buhârî, Savm 20; Müslim, Sıyâm 45",
      prayerTurkish = "Allah'ım! Kalplerimizi dinin üzere sabit kıl, bizi doğru yoldan ayırma.",
      prayerSource = "Tirmizî, Kader 7",
      esmaArabic = "الْكَرِيمُ",
      esmaTurkish = "El-Kerîm",
      esmaMeaning = "Lütuf ve ihsanı bol olan, karşılıksız veren, sonsuz cömertlik sahibi."
    ),
    SpiritualItem(
      day = 5,
      verseArabic = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
      verseTurkish = "Şüphesiz zorluğun yanında muhakkak bir kolaylık vardır.",
      verseSource = "İnşirah Suresi, 6. Ayet",
      hadithTurkish = "Oruçlunun iki sevinci vardır: Biri iftar ettiği vakit, diğeri Rabbine kavuştuğu vakittir.",
      hadithSource = "Müslim, Sıyâm 163",
      prayerTurkish = "Allah'ım! Faydasız ilimden, korkmayan kalpten, doymayan nefisten ve kabul olunmayan duadan Sana sığınırım.",
      prayerSource = "Müslim, Zikir 73",
      esmaArabic = "الْمُجِيبُ",
      esmaTurkish = "El-Mucîb",
      esmaMeaning = "Kendisine yalvaranların, dua edenlerin dualarına icabet eden."
    ),
    SpiritualItem(
      day = 6,
      verseArabic = "وَقُل رَّبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
      verseTurkish = "Ve de ki: Rabbim! Onlar beni küçükken nasıl özenle yetiştirdilerse, sen de onlara öyle merhamet et.",
      verseSource = "İsrâ Suresi, 24. Ayet",
      hadithTurkish = "İnsanlara merhamet etmeyene Allah da merhamet etmez.",
      hadithSource = "Buhârî, Tevhîd 2",
      prayerTurkish = "Rabbimiz! Bizi ve bizden önce iman etmiş kardeşlerimizi bağışla; kalplerimizde iman edenlere karşı en ufak bir kin bırakma.",
      prayerSource = "Haşr Suresi, 10. Ayet",
      esmaArabic = "الْوَاسِعُ",
      esmaTurkish = "El-Vâsi",
      esmaMeaning = "İlmi, rahmeti, kudreti ve ihsanı her şeyi kuşatan, sonsuz genişlik sahibi."
    ),
    SpiritualItem(
      day = 7,
      verseArabic = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      verseTurkish = "Onlar iman eden ve gönülleri Allah'ın zikriyle huzura kavuşanlardır. Bilesiniz ki kalpler ancak Allah'ı anmakla huzur bulur.",
      verseSource = "Ra'd Suresi, 28. Ayet",
      hadithTurkish = "Cennette 'Reyyân' denilen özel bir kapı vardır ki, kıyamet gününde oradan sadece oruç tutanlar girer.",
      hadithSource = "Buhârî, Savm 4; Müslim, Sıyâm 166",
      prayerTurkish = "Allah'ım! Bize hidayet, takva, iffet ve gönül zenginliği nasip eyle.",
      prayerSource = "Müslim, Zikir 72",
      esmaArabic = "السَّلَامُ",
      esmaTurkish = "Es-Selâm",
      esmaMeaning = "Her türlü eksiklikten uzak, selamete ve esenliğe erdiren."
    ),
    SpiritualItem(
      day = 8,
      verseArabic = "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
      verseTurkish = "Kim Allah'a karşı gelmekten sakınırsa, Allah ona bir çıkış yolu açar ve onu hiç beklemediği yerden rızıklandırır.",
      verseSource = "Talâk Suresi, 2-3. Ayet",
      hadithTurkish = "Kim bir oruçluya iftar verirse, oruçlunun sevabından hiçbir şey eksilmeksizin onun kadar sevap kazanır.",
      hadithSource = "Tirmizî, Savm 82",
      prayerTurkish = "Allah'ım! Helalinden vererek beni haramlardan koru; lütfunla beni Senden başkasına muhtaç etme.",
      prayerSource = "Tirmizî, Daavât 111",
      esmaArabic = "الرَّزَّاقُ",
      esmaTurkish = "Er-Rezzâk",
      esmaMeaning = "Bütün varlıkların rızkını veren ve onların ihtiyaçlarını karşılayan."
    ),
    SpiritualItem(
      day = 9,
      verseArabic = "وَأَحْسِنُوا إِنَّ اللَّهَ يُحِبُّ الْمُحْسِنِينَ",
      verseTurkish = "İyilik edin; şüphesiz Allah iyilik edenleri ve ihsan sahiplerini sever.",
      verseSource = "Bakara Suresi, 195. Ayet",
      hadithTurkish = "Ramazan ayı girdiğinde cennet kapıları açılır, cehennem kapıları kapanır ve şeytanlar zincire vurulur.",
      hadithSource = "Buhârî, Bed'ü'l-halk 11",
      prayerTurkish = "Rabbim! Beni ve neslimi namazı dosdoğru kılanlardan eyle. Rabbimiz! Duamı kabul buyur.",
      prayerSource = "İbrâhim Suresi, 40. Ayet",
      esmaArabic = "الْوَدُودُ",
      esmaTurkish = "El-Vedûd",
      esmaMeaning = "Kullarını çok seven, sevilmeye en layık olan ve sevgiyi yaratan."
    ),
    SpiritualItem(
      day = 10,
      verseArabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
      verseTurkish = "Öyleyse yalnız beni anın ki ben de sizi anayım. Bana şükredin, sakın nankörlük etmeyin.",
      verseSource = "Bakara Suresi, 152. Ayet",
      hadithTurkish = "Ramazan'ın ilk on günü rahmet, ortası mağfiret, sonu ise cehennemden kurtuluştur.",
      hadithSource = "İbn Ebî Şeybe, Musannef 2/429",
      prayerTurkish = "Allah'ım! Günahlarımızın küçüğünü büyüğünü, ilkini sonunu, açığını ve gizlisini bağışla.",
      prayerSource = "Müslim, Salât 216",
      esmaArabic = "الْحَلِيمُ",
      esmaTurkish = "El-Halîm",
      esmaMeaning = "Kullarına ceza vermede acele etmeyen, son derece şefkatli ve müsamahakar."
    ),
    SpiritualItem(
      day = 15,
      verseArabic = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَى أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ",
      verseTurkish = "De ki: Ey nefislerine karşı haddi aşmış kullarım! Allah'ın rahmetinden ümidinizi kesmeyin. Muhakkak ki Allah bütün günahları bağışlar.",
      verseSource = "Zümer Suresi, 53. Ayet",
      hadithTurkish = "Her kim yalan söylemeyi ve yalanla iş görmeyi bırakmazsa, Allah'ın onun yemesini ve içmesini terk etmesine ihtiyacı yoktur.",
      hadithSource = "Buhârî, Savm 8",
      prayerTurkish = "Rabbimiz! Bize katından bir rahmet ver ve şu işimizde bize bir kurtuluş ve muvaffakiyet hazırla.",
      prayerSource = "Kehf Suresi, 10. Ayet",
      esmaArabic = "التَّوَّابُ",
      esmaTurkish = "Et-Tevvâb",
      esmaMeaning = "Tövbeleri çokça kabul eden, pişmanlıkla dönen kullarına kapısını açan."
    ),
    SpiritualItem(
      day = 27,
      verseArabic = "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ",
      verseTurkish = "Şüphesiz biz onu Kadir gecesinde indirdik. Kadir gecesinin ne olduğunu sen ne bileceksin! Kadir gecesi bin aydan daha hayırlıdır.",
      verseSource = "Kadr Suresi, 1-3. Ayet",
      hadithTurkish = "Kadir gecesini Ramazan'ın son on gününün tek gecelerinde arayınız.",
      hadithSource = "Buhârî, Fadlu Leyleti'l-Kadr 3",
      prayerTurkish = "Allah'ım! Şüphesiz Sen çok affedicisin, cömertsin, affetmeyi seversin; bizi de affeyle.",
      prayerSource = "Tirmizî, Daavât 84 (Hz. Âişe r.a.)",
      esmaArabic = "الْعَفُوُّ",
      esmaTurkish = "El-Afüvv",
      esmaMeaning = "Affı sınırsız olan, günahların izini silip tamamen yok eden."
    ),
    SpiritualItem(
      day = 30,
      verseArabic = "وَلِتُكْمِلُوا الْعِدَّةَ وَلِتُكَبِّرُوا اللَّهَ عَلَى مَا هَدَاكُمْ وَلَعَلَّكُمْ تَشْكُرُونَ",
      verseTurkish = "Sayılı günleri tamamlayasınız ve size doğru yolu gösterdiğinden dolayı Allah'ı tekbirlerle yüceltesiniz, şükredesiniz diye...",
      verseSource = "Bakara Suresi, 185. Ayet",
      hadithTurkish = "Oruç tutunuz ki sıhhat bulasınız.",
      hadithSource = "Taberânî, el-Mu'cemü'l-Evsat 8312",
      prayerTurkish = "Allah'ım! Tuttuğumuz oruçları, kıldığımız namazları, verdiğimiz sadakaları dergâh-ı izzetinde kabul buyur. Bizi bayrama affedilmiş olarak ulaştır.",
      prayerSource = "Geleneksel Ramazan Hatim Duası",
      esmaArabic = "الشَّكُورُ",
      esmaTurkish = "Eş-Şekûr",
      esmaMeaning = "Az amele çok mükafat veren, şükreden kullarının mükâfatını kat kat artıran."
    )
  )

  /** Ramazan dışında gösterilen genel (Ramazan'a özel olmayan) ayet, hadis ve dualar. */
  val generalList: List<SpiritualItem> = listOf(
    SpiritualItem(101, "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
      "Bilesiniz ki kalpler ancak Allah'ı anmakla huzur bulur.", "Ra'd Suresi, 28. Ayet",
      "Kolaylaştırınız, zorlaştırmayınız; müjdeleyiniz, nefret ettirmeyiniz.", "Buhârî, İlim 11",
      "Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi cehennem azabından koru.", "Bakara Suresi, 201. Ayet",
      "السَّلَامُ", "Es-Selâm", "Kullarını her türlü tehlikeden selamete çıkaran."),
    SpiritualItem(102, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا",
      "Şüphesiz zorlukla beraber bir kolaylık vardır.", "İnşirâh Suresi, 5. Ayet",
      "Müslüman, elinden ve dilinden Müslümanların emin olduğu kimsedir.", "Buhârî, Îmân 4",
      "Rabbim! İlmimi artır.", "Tâhâ Suresi, 114. Ayet",
      "الْفَتَّاحُ", "El-Fettâh", "Her türlü zorluğu çözen, hayır kapılarını açan."),
    SpiritualItem(103, "فَاذْكُرُونِي أَذْكُرْكُمْ",
      "Öyleyse yalnız beni anın ki ben de sizi anayım.", "Bakara Suresi, 152. Ayet",
      "Sizin en hayırlınız, ahlakı en güzel olanınızdır.", "Buhârî, Edeb 39",
      "Rabbimiz! Bizi doğru yola ilettikten sonra kalplerimizi eğriltme.", "Âl-i İmrân Suresi, 8. Ayet",
      "الْوَدُودُ", "El-Vedûd", "Kullarını çok seven ve sevilmeye en layık olan."),
    SpiritualItem(104, "وَمَنْ يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
      "Kim Allah'a tevekkül ederse, O ona yeter.", "Talâk Suresi, 3. Ayet",
      "Ameller niyetlere göredir.", "Buhârî, Bed'ü'l-vahy 1",
      "Rabbim! Göğsüme genişlik ver, işimi bana kolaylaştır.", "Tâhâ Suresi, 25-26. Ayetler",
      "الْوَكِيلُ", "El-Vekîl", "Kendisine güvenip dayananların işlerini en güzel şekilde yoluna koyan."),
    SpiritualItem(105, "إِنَّمَا الْمُؤْمِنُونَ إِخْوَةٌ",
      "Müminler ancak kardeştirler.", "Hucurât Suresi, 10. Ayet",
      "Sizden biriniz, kendisi için istediğini kardeşi için de istemedikçe (gerçek anlamda) iman etmiş olmaz.", "Buhârî, Îmân 7",
      "Rabbimiz! Bizi ve bizden önce iman etmiş kardeşlerimizi bağışla.", "Haşr Suresi, 10. Ayet",
      "الْغَفُورُ", "El-Gafûr", "Günahları çokça bağışlayan."),
    SpiritualItem(106, "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
      "Allah hiç kimseye gücünün yeteceğinden fazlasını yüklemez.", "Bakara Suresi, 286. Ayet",
      "Kardeşine tebessüm etmen sadakadır.", "Tirmizî, Birr 36",
      "Rabbimiz! Unutur veya yanılırsak bizi sorumlu tutma.", "Bakara Suresi, 286. Ayet",
      "الرَّحِيمُ", "Er-Rahîm", "Müminlere ahirette sonsuz merhamet eden."),
    SpiritualItem(107, "وَهُوَ مَعَكُمْ أَيْنَ مَا كُنْتُمْ",
      "Nerede olursanız olun, O sizinle beraberdir.", "Hadîd Suresi, 4. Ayet",
      "Temizlik imanın yarısıdır.", "Müslim, Tahâret 1",
      "Allah'ım! Senden faydalı ilim, helal rızık ve kabul edilmiş amel isterim.", "İbn Mâce, İkâmetü's-salât 32",
      "الرَّزَّاقُ", "Er-Rezzâk", "Bütün canlıların rızkını veren."),
    SpiritualItem(108, "وَقَضَىٰ رَبُّكَ أَلَّا تَعْبُدُوا إِلَّا إِيَّاهُ وَبِالْوَالِدَيْنِ إِحْسَانًا",
      "Rabbin, yalnız kendisine ibadet etmenizi ve anne babaya iyi davranmanızı kesin olarak emretti.", "İsrâ Suresi, 23. Ayet",
      "Allah'ın rızası anne babanın rızasındadır.", "Tirmizî, Birr 3",
      "Rabbim! Onlar beni küçükken nasıl yetiştirdilerse, sen de onlara öyle merhamet et.", "İsrâ Suresi, 24. Ayet",
      "الْبَرُّ", "El-Berr", "İyiliği ve ihsanı bol olan."),
    SpiritualItem(109, "إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنْكَرِ",
      "Şüphesiz namaz, hayâsızlıktan ve kötülükten alıkoyar.", "Ankebût Suresi, 45. Ayet",
      "Beş vakit namaz, aralarında işlenen (küçük) günahlara kefarettir.", "Müslim, Tahâret 16",
      "Rabbim! Beni ve soyumdan gelecekleri namazı devamlı kılanlardan eyle.", "İbrâhîm Suresi, 40. Ayet",
      "الْمُجِيبُ", "El-Mücîb", "Dualara karşılık veren."),
    SpiritualItem(110, "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
      "Şüphesiz Allah sabredenlerle beraberdir.", "Bakara Suresi, 153. Ayet",
      "Gerçek güçlü kimse, öfkelendiği zaman kendine hâkim olandır.", "Buhârî, Edeb 76",
      "Rabbimiz! Üzerimize sabır yağdır, ayaklarımızı sabit kıl.", "Bakara Suresi, 250. Ayet",
      "الصَّبُورُ", "Es-Sabûr", "Çok sabırlı olan, cezalandırmada acele etmeyen.")
  )

  /** Bugünün içeriği: Ramazan'da Ramazan'ın gününe göre, Ramazan dışında genel listeden. */
  fun forToday(cal: java.util.Calendar = java.util.Calendar.getInstance()): SpiritualItem {
    val ramadanDay = RamadanCalendar.dayOfRamadan(cal)
    return if (ramadanDay != null) getForDay(ramadanDay)
    else localized(generalList[cal.get(java.util.Calendar.DAY_OF_YEAR) % generalList.size])
  }

  /** Uygulama dili İngilizce ise metinleri İngilizce karşılıklarıyla değiştirir. */
  fun localized(item: SpiritualItem): SpiritualItem {
    if (!com.example.i18n.AppLanguage.isEnglish) return item
    val en = EnglishContent.spiritual[item.day] ?: return item
    return item.copy(
      verseTurkish = en.verse, verseSource = en.verseSource,
      hadithTurkish = en.hadith, hadithSource = en.hadithSource,
      prayerTurkish = en.prayer, prayerSource = en.prayerSource,
      esmaMeaning = en.esmaMeaning
    )
  }

  fun getForDay(dayIndex: Int): SpiritualItem {
    val safeDay = ((dayIndex - 1) % list.size + list.size) % list.size
    return localized(list[safeDay])
  }
}

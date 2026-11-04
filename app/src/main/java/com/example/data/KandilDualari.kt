package com.example.data

/** Kandil gecelerinde okunabilecek dualar ve yapılabilecek ibadetler. */
data class Dua(val title: Bi, val arabic: String, val okunus: String, val meaning: Bi, val source: String)

data class KandilInfo(val key: String, val name: Bi, val about: Bi, val duas: List<Dua>)

object KandilDualari {

  private val seyyidulIstigfar = Dua(
    Bi("Seyyidü'l-İstiğfar (tövbe duası)", "Sayyid al-Istighfar (master of forgiveness)"),
    "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
    "Allâhümme ente rabbî, lâ ilâhe illâ ente, halaktenî ve ene abdük, ve ene alâ ahdike ve va'dike mesteta'tü, eûzü bike min şerri mâ sana'tü, ebûü leke bi-ni'metike aleyye, ve ebûü bi-zenbî fağfir lî, fe innehû lâ yağfirüz-zünûbe illâ ente.",
    Bi(
      "Allah'ım! Sen benim Rabbimsin. Senden başka ilah yoktur. Beni Sen yarattın, ben Senin kulunum. Gücüm yettiğince Sana verdiğim söze ve ahde bağlıyım. İşlediklerimin şerrinden Sana sığınırım. Bana verdiğin nimetleri ve günahımı itiraf ediyorum; beni bağışla. Çünkü günahları ancak Sen bağışlarsın.",
      "O God, You are my Lord; there is no god but You. You created me and I am Your servant. I keep Your covenant and promise as much as I can. I seek refuge in You from the evil I have done. I acknowledge Your blessings upon me and I confess my sins; so forgive me, for none forgives sins but You."
    ),
    "Buhârî, Deavât 2"
  )

  private val salavat = Dua(
    Bi("Salli – Bârik duaları (salavat)", "Salawat (Salli & Barik)"),
    "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ. اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
    "Allâhümme salli alâ Muhammedin ve alâ âli Muhammed, kemâ salleyte alâ İbrâhîme ve alâ âli İbrâhîm, inneke hamîdün mecîd. Allâhümme bârik alâ Muhammedin ve alâ âli Muhammed, kemâ bârekte alâ İbrâhîme ve alâ âli İbrâhîm, inneke hamîdün mecîd.",
    Bi(
      "Allah'ım! İbrahim'e ve ailesine rahmet ettiğin gibi Muhammed'e ve ailesine de rahmet et; İbrahim'i ve ailesini mübarek kıldığın gibi Muhammed'i ve ailesini de mübarek kıl. Şüphesiz Sen övülmeye layık ve yücesin.",
      "O God, send mercy upon Muhammad and his family as You sent mercy upon Abraham and his family, and bless Muhammad and his family as You blessed Abraham and his family. You are Praiseworthy, Glorious."
    ),
    "Buhârî, Enbiyâ 10"
  )

  private val rabbenaAtina = Dua(
    Bi("Rabbenâ âtinâ", "Rabbana atina"),
    "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
    "Rabbenâ âtinâ fid-dünyâ haseneten ve fil-âhirati haseneten ve kınâ azâben-nâr.",
    Bi("Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi ateş azabından koru.", "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire."),
    "Bakara, 2/201"
  )

  val general = listOf(seyyidulIstigfar, salavat, rabbenaAtina)

  /** Kandil gecesi yapılabilecek ibadetler. */
  val worship = listOf(
    Bi("Kaza ve nafile namaz kılmak", "Praying missed (qada) and voluntary prayers"),
    Bi("Kur'an-ı Kerim okumak (Yâsîn, Mülk, Fetih vb.)", "Reciting the Quran (Ya-Sin, Al-Mulk, Al-Fath etc.)"),
    Bi("Tövbe ve istiğfar etmek", "Repentance and seeking forgiveness"),
    Bi("Peygamber Efendimize salavat getirmek", "Sending salawat upon the Prophet (pbuh)"),
    Bi("Tesbihat ve zikir", "Tasbih and dhikr"),
    Bi("Anne-baba, aile ve tüm ümmet için dua etmek", "Praying for parents, family and the whole ummah"),
    Bi("Sadaka vermek, akrabaları aramak, küsleri barıştırmak", "Giving charity, calling relatives, reconciling")
  )

  val note = Bi(
    "Not: Kandil geceleri için sahih kaynaklarda belirlenmiş özel bir namaz şekli yoktur; bu gecelerde dileyen nafile ve kaza namazı kılabilir, dua ve zikirle meşgul olabilir.",
    "Note: There is no specially prescribed prayer form for these nights in authentic sources; one may pray voluntary or missed prayers and engage in dua and dhikr."
  )

  val kandiller = listOf(
    KandilInfo(
      "regaib", Bi("Regaip Kandili", "Night of Raghaib"),
      Bi("Recep ayının ilk cuma gecesi. Üç ayların müjdecisidir.", "The first Friday night of Rajab, herald of the three holy months."),
      listOf(
        Dua(
          Bi("Üç aylar duası", "Dua for the three months"),
          "اللَّهُمَّ بَارِكْ لَنَا فِي رَجَبٍ وَشَعْبَانَ وَبَلِّغْنَا رَمَضَانَ",
          "Allâhümme bârik lenâ fî Recebe ve Şa'bân ve belliğnâ Ramazân.",
          Bi("Allah'ım! Recep ve Şaban'ı bize mübarek kıl ve bizi Ramazan'a ulaştır.", "O God, bless us in Rajab and Sha'ban and let us reach Ramadan."),
          "Ahmed b. Hanbel, Müsned (senedinde zayıflık vardır)"
        )
      )
    ),
    KandilInfo(
      "mirac", Bi("Miraç Kandili", "Night of Mi'raj"),
      Bi("Peygamberimizin (s.a.v.) göğe yükseldiği gece. Beş vakit namaz ve Bakara suresinin son ayetleri bu gecede hediye edilmiştir.", "The night of the Prophet's (pbuh) ascension; the five daily prayers and the last verses of Al-Baqarah were gifted on this night."),
      listOf(
        Dua(
          Bi("Bakara suresi son ayeti (Âmenerrasûlü'den)", "Last verse of Al-Baqarah"),
          "رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَسِينَا أَوْ أَخْطَأْنَا، رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِنْ قَبْلِنَا، رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ، وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا، أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
          "Rabbenâ lâ tüâhiznâ in nesînâ ev ahta'nâ, rabbenâ ve lâ tahmil aleynâ ısran kemâ hameltehû alellezîne min kablinâ, rabbenâ ve lâ tühammilnâ mâ lâ tâkate lenâ bih, va'fü annâ, vağfir lenâ, verhamnâ, ente mevlânâ fensurnâ alel-kavmil-kâfirîn.",
          Bi("Rabbimiz! Unutur veya yanılırsak bizi sorumlu tutma. Rabbimiz! Bizden öncekilere yüklediğin gibi bize ağır yük yükleme. Gücümüzün yetmediğini bize taşıtma. Bizi affet, bağışla, bize merhamet et. Sen bizim Mevlâmızsın.", "Our Lord, do not hold us accountable if we forget or err. Do not lay upon us a burden like that laid on those before us, nor burden us beyond our strength. Pardon us, forgive us and have mercy on us. You are our Protector."),
          "Bakara, 2/286; Müslim, Îmân 279"
        )
      )
    ),
    KandilInfo(
      "berat", Bi("Berat Kandili", "Night of Bara'ah"),
      Bi("Şaban ayının 15. gecesi. Af, arınma ve kurtuluş gecesi olarak bilinir.", "The 15th night of Sha'ban, known as the night of forgiveness and salvation."),
      listOf(
        Dua(
          Bi("Anne-baba ve müminler için af duası", "Forgiveness for parents and believers"),
          "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
          "Rabbenağfir lî ve li-vâlideyye ve lil-mü'minîne yevme yekûmül-hisâb.",
          Bi("Rabbimiz! Hesap gününde beni, anne-babamı ve bütün müminleri bağışla.", "Our Lord, forgive me, my parents and the believers on the Day the account is established."),
          "İbrâhîm, 14/41"
        )
      )
    ),
    KandilInfo(
      "kadir", Bi("Kadir Gecesi", "Laylat al-Qadr"),
      Bi("Kur'an'ın indirildiği, bin aydan hayırlı gece. Ramazan'ın son on gününde aranır.", "The night the Quran was revealed, better than a thousand months; sought in the last ten nights of Ramadan."),
      listOf(
        Dua(
          Bi("Kadir Gecesi duası", "Dua of Laylat al-Qadr"),
          "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
          "Allâhümme inneke afüvvün tuhibbül-afve fa'fu annî.",
          Bi("Allah'ım! Sen affedicisin, affetmeyi seversin; beni de affet.", "O God, You are Pardoning and love to pardon, so pardon me."),
          "Tirmizî, Deavât 84; İbn Mâce, Dua 5"
        )
      )
    ),
    KandilInfo(
      "mevlid", Bi("Mevlid Kandili", "Mawlid"),
      Bi("Peygamber Efendimizin (s.a.v.) dünyaya geldiği gece.", "The night the Prophet (pbuh) was born."),
      listOf(
        Dua(
          Bi("Salavat ayeti", "Verse of salawat"),
          "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ، يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا",
          "İnnallâhe ve melâiketehû yusallûne alen-nebiyy, yâ eyyühellezîne âmenû sallû aleyhi ve sellimû teslîmâ.",
          Bi("Şüphesiz Allah ve melekleri Peygamber'e salat ederler. Ey iman edenler! Siz de ona salat edin ve tam bir teslimiyetle selam verin.", "Indeed God and His angels send blessings upon the Prophet. O you who believe, send blessings upon him and greet him with peace."),
          "Ahzâb, 33/56"
        )
      )
    )
  )

  /** Bugüne/sonraki güne denk gelen kandilin anahtarı (takvimden). */
  fun keyFor(title: String): String? = when {
    title.contains("Regaip") -> "regaib"
    title.contains("Miraç") -> "mirac"
    title.contains("Berat") -> "berat"
    title.contains("Kadir") -> "kadir"
    title.contains("Mevlid Kandili") -> "mevlid"
    else -> null
  }
}

package com.example.data

import com.example.R
import java.util.Calendar

data class GreetingMessage(val headline: Bi, val body: Bi)

/**
 * Kart renk paleti (arka plan görseli card_bg_*.webp ile uyumlu; yerleşim GreetingCardRenderer'da sabit).
 * panel: ayet/hadis panelinin yarı saydam zemini (ARGB).
 */
data class CardStyle(val ink: Long, val muted: Long, val arabic: Long, val panel: Long, val dark: Boolean)

private val SARAY = CardStyle(0xFF2B241C, 0xFF6F5A3E, 0xFF1F4D3A, 0xD1FFFCF5, false)
private val SARAY_GECE = CardStyle(0xFFF5EEDD, 0xFFC9BC9E, 0xFFE9C77E, 0xB8140F1C, true)
private val ZUMRUT = CardStyle(0xFF14261D, 0xFF3F5E4E, 0xFF1E6B4F, 0xD6FAFDF8, false)
private val ZUMRUT_GECE = CardStyle(0xFFF3EFE2, 0xFFBFD3C6, 0xFFE3C46A, 0xB8061A12, true)
private val IZNIK = CardStyle(0xFF14203A, 0xFF4A5874, 0xFF1A3F7A, 0xDBFFFFFF, false)
private val IZNIK_GECE = CardStyle(0xFFF2F5FB, 0xFFB8C6E2, 0xFF8FD9E2, 0xB8071028, true)
private val GUL = CardStyle(0xFF2B1E1C, 0xFF7A5148, 0xFF8E2F42, 0xDBFFFAF7, false)

/** Kartın altındaki, günün anlamına uygun ayet / hadis alıntısı. */
data class CardQuote(val text: Bi, val source: String)

/** Paylaşılabilir özel gün kartı türü. */
enum class GreetingOccasion(
  val label: Bi,
  val arabic: String,
  val bgRes: Int,
  val style: CardStyle,
  val messages: List<GreetingMessage>
) {
  CUMA(
    Bi("Cuma", "Friday"), "جمعة مباركة", R.drawable.card_bg_cuma, SARAY,
    listOf(
      GreetingMessage(Bi("Hayırlı Cumalar", "Blessed Friday"), Bi("Cumanız mübarek, dualarınız kabul olsun.", "May your Friday be blessed and your prayers accepted.")),
      GreetingMessage(Bi("Cumanız Mübarek Olsun", "Jumu'ah Mubarak"), Bi("Rabbim gönlünüze huzur, evinize bereket versin.", "May God grant peace to your heart and blessings to your home.")),
      GreetingMessage(Bi("Hayırlı Cumalar", "Blessed Friday"), Bi("Bu mübarek günde dualarımız birbirimize olsun.", "On this blessed day, let our prayers be for one another."))
    )
  ),
  REGAIB(
    Bi("Regaip", "Raghaib"), "ليلة الرغائب", R.drawable.card_bg_regaib, ZUMRUT_GECE,
    listOf(
      GreetingMessage(Bi("Regaip Kandiliniz Mübarek Olsun", "Blessed Night of Raghaib"), Bi("Üç ayların ilk müjdecisi gecede dualarınız kabul olsun.", "May your prayers be accepted on this first herald of the three holy months.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim bu gecenin hürmetine rahmetini üzerimizden eksik etmesin.", "For the sake of this night, may God never withhold His mercy from us."))
    )
  ),
  MIRAC(
    Bi("Miraç", "Mi'raj"), "ليلة المعراج", R.drawable.card_bg_mirac, IZNIK_GECE,
    listOf(
      GreetingMessage(Bi("Miraç Kandiliniz Mübarek Olsun", "Blessed Night of Mi'raj"), Bi("Namazın hediye edildiği bu gecede secdeleriniz daim olsun.", "On the night prayer was gifted to us, may your prostrations be constant.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Gönüllerimiz Miraç'ın nuruyla aydınlansın.", "May our hearts be illuminated by the light of Mi'raj."))
    )
  ),
  BERAT(
    Bi("Berat", "Bara'ah"), "ليلة البراءة", R.drawable.card_bg_berat, ZUMRUT_GECE,
    listOf(
      GreetingMessage(Bi("Berat Kandiliniz Mübarek Olsun", "Blessed Night of Bara'ah"), Bi("Bu gece affa, rahmete ve berata vesile olsun.", "May this night be a means of forgiveness, mercy and salvation.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim günahlarımızdan beraatimizi nasip etsin.", "May God grant us release from our sins."))
    )
  ),
  RAMAZAN(
    Bi("Ramazan", "Ramadan"), "رمضان كريم", R.drawable.card_bg_ramazan, IZNIK,
    listOf(
      GreetingMessage(Bi("Hoş Geldin On Bir Ayın Sultanı", "Welcome, Sultan of Eleven Months"), Bi("Ramazan-ı Şerif'iniz mübarek olsun.", "May your holy Ramadan be blessed.")),
      GreetingMessage(Bi("Hayırlı Ramazanlar", "Ramadan Kareem"), Bi("Oruçlarınız, dualarınız ve iftar sofralarınız bereketli olsun.", "May your fasts, prayers and iftar tables be full of blessing.")),
      GreetingMessage(Bi("Ramazan Bereketi Evinize Dolsun", "May Ramadan's Blessings Fill Your Home"), Bi("Sabrın, rahmetin ve paylaşmanın ayı hayırlara vesile olsun.", "May the month of patience, mercy and sharing bring all goodness."))
    )
  ),
  KADIR(
    Bi("Kadir", "Qadr"), "ليلة القدر", R.drawable.card_bg_kadir, SARAY_GECE,
    listOf(
      GreetingMessage(Bi("Kadir Geceniz Mübarek Olsun", "Blessed Laylat al-Qadr"), Bi("Bin aydan hayırlı bu gecede dualarınız kabul olsun.", "On this night better than a thousand months, may your prayers be accepted.")),
      GreetingMessage(Bi("Hayırlı Kadir Geceleri", "Blessed Night of Power"), Bi("Allah'ım, sen affedicisin, affı seversin; bizi affet.", "O God, You are forgiving and love forgiveness; forgive us."))
    )
  ),
  RAMAZAN_BAYRAMI(
    Bi("Ramazan Bayramı", "Eid al-Fitr"), "عيد مبارك", R.drawable.card_bg_ramazan_bayrami, ZUMRUT,
    listOf(
      GreetingMessage(Bi("Ramazan Bayramınız Mübarek Olsun", "Eid Mubarak"), Bi("Bayramınız sevdiklerinizle birlikte huzur ve sevinç dolu geçsin.", "May your Eid be filled with peace and joy with your loved ones.")),
      GreetingMessage(Bi("İyi Bayramlar", "Happy Eid"), Bi("Allah tuttuğumuz oruçları ve ettiğimiz duaları kabul etsin.", "May God accept our fasts and our prayers."))
    )
  ),
  KURBAN_BAYRAMI(
    Bi("Kurban Bayramı", "Eid al-Adha"), "عيد الأضحى مبارك", R.drawable.card_bg_kurban_bayrami, SARAY,
    listOf(
      GreetingMessage(Bi("Kurban Bayramınız Mübarek Olsun", "Blessed Eid al-Adha"), Bi("Kurbanlarınız kabul, bayramınız bereketli olsun.", "May your sacrifices be accepted and your Eid be blessed.")),
      GreetingMessage(Bi("İyi Bayramlar", "Happy Eid"), Bi("Paylaştıkça çoğalan bir bayram dileriz.", "Wishing you an Eid that grows richer as it is shared."))
    )
  ),
  MEVLID(
    Bi("Mevlid", "Mawlid"), "المولد النبوي الشريف", R.drawable.card_bg_mevlid, GUL,
    listOf(
      GreetingMessage(Bi("Mevlid Kandiliniz Mübarek Olsun", "Blessed Mawlid"), Bi("Peygamber Efendimizin (s.a.v.) dünyayı şereflendirdiği gece hayırlara vesile olsun.", "May the night the Prophet (pbuh) honoured the world bring all goodness.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim bizleri O'nun şefaatine nail eylesin.", "May God make us worthy of his intercession."))
    )
  ),
  HICRI_YILBASI(
    Bi("Hicri Yılbaşı", "Hijri New Year"), "سنة هجرية مباركة", R.drawable.card_bg_hicri_yilbasi, IZNIK,
    listOf(
      GreetingMessage(Bi("Hicri Yeni Yılınız Mübarek Olsun", "Happy Hijri New Year"), Bi("Yeni yıl hayırlara, huzura ve bereketlere vesile olsun.", "May the new year bring goodness, peace and blessings."))
    )
  ),
  ASURE(
    Bi("Aşure", "Ashura"), "يوم عاشوراء", R.drawable.card_bg_asure, SARAY,
    listOf(
      GreetingMessage(Bi("Aşure Gününüz Mübarek Olsun", "Blessed Day of Ashura"), Bi("Paylaştıkça bereketlenen aşure gibi gönüllerimiz de birleşsin.", "Like ashura that grows richer when shared, may our hearts come together."))
    )
  );

  companion object {
    /** Bugüne uygun kart türü: dini gün / kandil / bayram, yoksa Ramazan veya Cuma; hiçbiri değilse null. */
    fun forToday(cal: Calendar = Calendar.getInstance()): GreetingOccasion? {
      val title = IslamicDays.forYear(cal.get(Calendar.YEAR))[RamadanCalendar.dateKey(cal)]?.title.orEmpty()
      val byTitle = when {
        title.contains("Ramazan Bayramı") -> RAMAZAN_BAYRAMI
        title.contains("Kurban Bayramı") -> KURBAN_BAYRAMI
        title.contains("Kadir") -> KADIR
        title.contains("Regaip") -> REGAIB
        title.contains("Miraç") -> MIRAC
        title.contains("Berat") -> BERAT
        title.contains("Mevlid Kandili") -> MEVLID
        title.contains("Hicri Yılbaşı") -> HICRI_YILBASI
        title.contains("Aşure") -> ASURE
        else -> null
      }
      return byTitle
        ?: if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) CUMA
        else if (RamadanCalendar.isRamadanDay(cal)) RAMAZAN
        else null
    }
  }
}

// lazy: enum ile dosya düzeyindeki paletlerin başlatma sırası döngüsünde anahtarlar null kalmasın
private val CARD_QUOTES: Map<GreetingOccasion, CardQuote> by lazy { mapOf(
  GreetingOccasion.CUMA to CardQuote(Bi("Üzerine güneş doğan en hayırlı gün cuma günüdür.", "The best day on which the sun rises is Friday."), "Müslim, Cum'a 17"),
  GreetingOccasion.REGAIB to CardQuote(Bi("Allah'ım! Recep ve Şaban'ı bize mübarek kıl ve bizi Ramazan'a ulaştır.", "O God, bless us in Rajab and Sha'ban and let us reach Ramadan."), "Ahmed b. Hanbel, Müsned"),
  GreetingOccasion.MIRAC to CardQuote(Bi("Kulunu bir gece Mescid-i Haram'dan Mescid-i Aksâ'ya götüren Allah'ın şanı yücedir.", "Glory be to Him who took His servant by night from al-Masjid al-Haram to al-Masjid al-Aqsa."), "İsrâ, 17/1"),
  GreetingOccasion.BERAT to CardQuote(Bi("Allah'ın rahmetinden ümidinizi kesmeyin. Şüphesiz Allah bütün günahları bağışlar.", "Do not despair of God's mercy. Indeed, God forgives all sins."), "Zümer, 39/53"),
  GreetingOccasion.RAMAZAN to CardQuote(Bi("Ramazan ayı, insanlara yol gösterici olarak Kur'an'ın indirildiği aydır.", "The month of Ramadan is the month in which the Quran was revealed as guidance for people."), "Bakara, 2/185"),
  GreetingOccasion.KADIR to CardQuote(Bi("Kadir gecesi bin aydan daha hayırlıdır.", "The Night of Decree is better than a thousand months."), "Kadr, 97/3"),
  GreetingOccasion.RAMAZAN_BAYRAMI to CardQuote(Bi("Oruçlu için iki sevinç vardır: Biri iftar ettiği an, diğeri Rabbine kavuştuğu an.", "The fasting person has two joys: when he breaks his fast and when he meets his Lord."), "Buhârî, Savm 9"),
  GreetingOccasion.KURBAN_BAYRAMI to CardQuote(Bi("Onların ne etleri ne de kanları Allah'a ulaşır; O'na ulaşacak olan ancak sizin takvanızdır.", "Neither their meat nor their blood reaches God; what reaches Him is your devotion."), "Hac, 22/37"),
  GreetingOccasion.MEVLID to CardQuote(Bi("Biz seni ancak âlemlere rahmet olarak gönderdik.", "We have not sent you except as a mercy to the worlds."), "Enbiyâ, 21/107"),
  GreetingOccasion.HICRI_YILBASI to CardQuote(Bi("Üzülme, şüphesiz Allah bizimle beraberdir.", "Do not grieve; indeed God is with us."), "Tevbe, 9/40"),
  GreetingOccasion.ASURE to CardQuote(Bi("Aşure günü orucunun geçmiş bir yılın günahlarına kefaret olmasını Allah'tan umarım.", "I hope from God that fasting on Ashura expiates the sins of the previous year."), "Müslim, Sıyâm 196")
) }

val GreetingOccasion.quote: CardQuote get() = CARD_QUOTES.getValue(this)

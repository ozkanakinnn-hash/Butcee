package com.example.data

import com.example.R
import java.util.Calendar

data class GreetingMessage(val headline: Bi, val body: Bi)

/** Paylaşılabilir özel gün kartı türü. */
enum class GreetingOccasion(
  val label: Bi,
  val arabic: String,
  val photoRes: Int,
  val messages: List<GreetingMessage>
) {
  CUMA(
    Bi("Cuma", "Friday"), "جمعة مباركة", R.drawable.mosque_sultanahmet,
    listOf(
      GreetingMessage(Bi("Hayırlı Cumalar", "Blessed Friday"), Bi("Cumanız mübarek, dualarınız kabul olsun.", "May your Friday be blessed and your prayers accepted.")),
      GreetingMessage(Bi("Cumanız Mübarek Olsun", "Jumu'ah Mubarak"), Bi("Rabbim gönlünüze huzur, evinize bereket versin.", "May God grant peace to your heart and blessings to your home.")),
      GreetingMessage(Bi("Hayırlı Cumalar", "Blessed Friday"), Bi("Bu mübarek günde dualarımız birbirimize olsun.", "On this blessed day, let our prayers be for one another."))
    )
  ),
  REGAIB(
    Bi("Regaip", "Raghaib"), "ليلة الرغائب", R.drawable.mosque_suleymaniye,
    listOf(
      GreetingMessage(Bi("Regaip Kandiliniz Mübarek Olsun", "Blessed Night of Raghaib"), Bi("Üç ayların ilk müjdecisi gecede dualarınız kabul olsun.", "May your prayers be accepted on this first herald of the three holy months.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim bu gecenin hürmetine rahmetini üzerimizden eksik etmesin.", "For the sake of this night, may God never withhold His mercy from us."))
    )
  ),
  MIRAC(
    Bi("Miraç", "Mi'raj"), "ليلة المعراج", R.drawable.mosque_camlica,
    listOf(
      GreetingMessage(Bi("Miraç Kandiliniz Mübarek Olsun", "Blessed Night of Mi'raj"), Bi("Namazın hediye edildiği bu gecede secdeleriniz daim olsun.", "On the night prayer was gifted to us, may your prostrations be constant.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Gönüllerimiz Miraç'ın nuruyla aydınlansın.", "May our hearts be illuminated by the light of Mi'raj."))
    )
  ),
  BERAT(
    Bi("Berat", "Bara'ah"), "ليلة البراءة", R.drawable.mosque_ayasofya,
    listOf(
      GreetingMessage(Bi("Berat Kandiliniz Mübarek Olsun", "Blessed Night of Bara'ah"), Bi("Bu gece affa, rahmete ve berata vesile olsun.", "May this night be a means of forgiveness, mercy and salvation.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim günahlarımızdan beraatimizi nasip etsin.", "May God grant us release from our sins."))
    )
  ),
  RAMAZAN(
    Bi("Ramazan", "Ramadan"), "رمضان كريم", R.drawable.mosque_fatih,
    listOf(
      GreetingMessage(Bi("Hoş Geldin On Bir Ayın Sultanı", "Welcome, Sultan of Eleven Months"), Bi("Ramazan-ı Şerif'iniz mübarek olsun.", "May your holy Ramadan be blessed.")),
      GreetingMessage(Bi("Hayırlı Ramazanlar", "Ramadan Kareem"), Bi("Oruçlarınız, dualarınız ve iftar sofralarınız bereketli olsun.", "May your fasts, prayers and iftar tables be full of blessing.")),
      GreetingMessage(Bi("Ramazan Bereketi Evinize Dolsun", "May Ramadan's Blessings Fill Your Home"), Bi("Sabrın, rahmetin ve paylaşmanın ayı hayırlara vesile olsun.", "May the month of patience, mercy and sharing bring all goodness."))
    )
  ),
  KADIR(
    Bi("Kadir", "Qadr"), "ليلة القدر", R.drawable.mosque_eyup,
    listOf(
      GreetingMessage(Bi("Kadir Geceniz Mübarek Olsun", "Blessed Laylat al-Qadr"), Bi("Bin aydan hayırlı bu gecede dualarınız kabul olsun.", "On this night better than a thousand months, may your prayers be accepted.")),
      GreetingMessage(Bi("Hayırlı Kadir Geceleri", "Blessed Night of Power"), Bi("Allah'ım, sen affedicisin, affı seversin; bizi affet.", "O God, You are forgiving and love forgiveness; forgive us."))
    )
  ),
  RAMAZAN_BAYRAMI(
    Bi("Ramazan Bayramı", "Eid al-Fitr"), "عيد مبارك", R.drawable.mosque_ortakoy,
    listOf(
      GreetingMessage(Bi("Ramazan Bayramınız Mübarek Olsun", "Eid Mubarak"), Bi("Bayramınız sevdiklerinizle birlikte huzur ve sevinç dolu geçsin.", "May your Eid be filled with peace and joy with your loved ones.")),
      GreetingMessage(Bi("İyi Bayramlar", "Happy Eid"), Bi("Allah tuttuğumuz oruçları ve ettiğimiz duaları kabul etsin.", "May God accept our fasts and our prayers."))
    )
  ),
  KURBAN_BAYRAMI(
    Bi("Kurban Bayramı", "Eid al-Adha"), "عيد الأضحى مبارك", R.drawable.mosque_suleymaniye,
    listOf(
      GreetingMessage(Bi("Kurban Bayramınız Mübarek Olsun", "Blessed Eid al-Adha"), Bi("Kurbanlarınız kabul, bayramınız bereketli olsun.", "May your sacrifices be accepted and your Eid be blessed.")),
      GreetingMessage(Bi("İyi Bayramlar", "Happy Eid"), Bi("Paylaştıkça çoğalan bir bayram dileriz.", "Wishing you an Eid that grows richer as it is shared."))
    )
  ),
  MEVLID(
    Bi("Mevlid", "Mawlid"), "المولد النبوي الشريف", R.drawable.mosque_eyup,
    listOf(
      GreetingMessage(Bi("Mevlid Kandiliniz Mübarek Olsun", "Blessed Mawlid"), Bi("Peygamber Efendimizin (s.a.v.) dünyayı şereflendirdiği gece hayırlara vesile olsun.", "May the night the Prophet (pbuh) honoured the world bring all goodness.")),
      GreetingMessage(Bi("Hayırlı Kandiller", "Blessed Kandil"), Bi("Rabbim bizleri O'nun şefaatine nail eylesin.", "May God make us worthy of his intercession."))
    )
  ),
  HICRI_YILBASI(
    Bi("Hicri Yılbaşı", "Hijri New Year"), "سنة هجرية مباركة", R.drawable.mosque_sultanahmet,
    listOf(
      GreetingMessage(Bi("Hicri Yeni Yılınız Mübarek Olsun", "Happy Hijri New Year"), Bi("Yeni yıl hayırlara, huzura ve bereketlere vesile olsun.", "May the new year bring goodness, peace and blessings."))
    )
  ),
  ASURE(
    Bi("Aşure", "Ashura"), "يوم عاشوراء", R.drawable.mosque_fatih,
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

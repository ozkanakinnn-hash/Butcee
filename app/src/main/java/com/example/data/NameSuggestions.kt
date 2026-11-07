package com.example.data

import java.util.Calendar

data class NameSuggestion(val name: String, val meaning: String)

/**
 * "Bugün doğanlara" isim önerileri: Kur'an'da geçen, peygamberlerin, sahabelerin ve İslam
 * büyüklerinin adları ile anlamı güzel isimler. Her gün 3 kız + 3 erkek ismi döner.
 * NOT: Bu liste uygulama için derlenmiştir; resmi bir Diyanet takvimi listesi değildir.
 */
object NameSuggestions {

  val girls = listOf(
    NameSuggestion("Zeynep", "Değerli taş; Peygamberimizin kızının adı"),
    NameSuggestion("Fatma", "Hz. Peygamber'in kızı Hz. Fatıma'nın adı"),
    NameSuggestion("Ayşe", "Yaşayan, hayat dolu; Hz. Ayşe'nin adı"),
    NameSuggestion("Hatice", "Hz. Peygamber'in ilk eşi Hz. Hatice'nin adı"),
    NameSuggestion("Meryem", "Hz. İsa'nın annesi; Kur'an'da adıyla anılan tek kadın"),
    NameSuggestion("Elif", "Arap alfabesinin ilk harfi; doğruluk, incelik"),
    NameSuggestion("Zehra", "Parlak, aydınlık; Hz. Fatıma'nın lakabı"),
    NameSuggestion("Esma", "Yüce, isimler; Esmâ-i Hüsnâ'dan"),
    NameSuggestion("Rabia", "Dördüncü; büyük veli Rabiatü'l-Adeviyye'nin adı"),
    NameSuggestion("Hümeyra", "Kırmızı yanaklı; Hz. Ayşe'nin lakabı"),
    NameSuggestion("Sümeyye", "Yüce; İslam'ın ilk kadın şehidinin adı"),
    NameSuggestion("Havva", "Hayat veren; ilk insan Hz. Âdem'in eşi"),
    NameSuggestion("Nur", "Işık, aydınlık"),
    NameSuggestion("Hafsa", "Hz. Peygamber'in eşi Hz. Hafsa'nın adı"),
    NameSuggestion("Saliha", "İyi, dürüst, hayırlı işler yapan"),
    NameSuggestion("Büşra", "Müjde, sevinçli haber"),
    NameSuggestion("Şeyma", "Hz. Peygamber'in süt kardeşinin adı"),
    NameSuggestion("Kevser", "Bolluk; cennette bir ırmak ve Kur'an suresi"),
    NameSuggestion("Asiye", "Firavun'un mümin eşinin adı"),
    NameSuggestion("Beyza", "Beyaz, ak, temiz"),
    NameSuggestion("Selma", "Esen, huzurlu, güvende olan"),
    NameSuggestion("Hilal", "Yeni ay"),
    NameSuggestion("Sena", "Övgü, övme"),
    NameSuggestion("Zübeyde", "Öz, seçkin, değerli"),
    NameSuggestion("Meva", "Sığınak, barınak; cennet bahçelerinden"),
    NameSuggestion("Tuba", "Ne mutlu; cennette bir ağaç"),
    NameSuggestion("Feyza", "Bolluk, bereket, feyiz"),
    NameSuggestion("Ravza", "Bahçe; Peygamber Mescidi'ndeki Ravza"),
    NameSuggestion("Safiye", "Saf, temiz, içten"),
    NameSuggestion("Rukiye", "Yükselen; Peygamberimizin kızının adı"),
    NameSuggestion("Sare", "Sevindiren; Hz. İbrahim'in eşi"),
    NameSuggestion("İkra", "Oku; Kur'an'ın ilk emri")
  )

  val boys = listOf(
    NameSuggestion("Muhammed", "Övülmüş; Peygamberimizin adı"),
    NameSuggestion("Ahmet", "Çok övülen; Peygamberimizin adlarından"),
    NameSuggestion("Mustafa", "Seçilmiş, seçkin; Peygamberimizin lakabı"),
    NameSuggestion("Ali", "Yüce, yüksek; dördüncü halifenin adı"),
    NameSuggestion("Ömer", "Uzun ömürlü; ikinci halifenin adı"),
    NameSuggestion("Osman", "Üçüncü halife Hz. Osman'ın adı"),
    NameSuggestion("Hasan", "Güzel, iyi; Peygamberimizin torunu"),
    NameSuggestion("Hüseyin", "Küçük güzel; Peygamberimizin torunu"),
    NameSuggestion("Yusuf", "Hz. Yusuf peygamberin adı"),
    NameSuggestion("İbrahim", "Hz. İbrahim; peygamberlerin atası"),
    NameSuggestion("İsmail", "Allah işitti; Hz. İsmail peygamber"),
    NameSuggestion("Yahya", "Yaşayan; Hz. Yahya peygamber"),
    NameSuggestion("Enes", "Yakın dost, sevimli; sahabi adı"),
    NameSuggestion("Hamza", "Güçlü, keskin; Peygamberimizin amcası"),
    NameSuggestion("Bilal", "Ferahlık veren; İslam'ın ilk müezzini"),
    NameSuggestion("Salih", "İyi, dürüst; Hz. Salih peygamber"),
    NameSuggestion("Abdullah", "Allah'ın kulu"),
    NameSuggestion("Yunus", "Hz. Yunus peygamber; güvercin"),
    NameSuggestion("Musa", "Hz. Musa peygamber"),
    NameSuggestion("Taha", "Kur'an suresi; Peygamberimizin isimlerinden"),
    NameSuggestion("Yasin", "Kur'an'ın kalbi sayılan sure"),
    NameSuggestion("Furkan", "Hakkı batıldan ayıran; Kur'an'ın adlarından"),
    NameSuggestion("Bekir", "İlk doğan; Hz. Ebubekir'in adından"),
    NameSuggestion("Halil", "Samimi dost; Hz. İbrahim'in lakabı"),
    NameSuggestion("Eymen", "Uğurlu, bereketli, sağ taraf"),
    NameSuggestion("Muaz", "Korunan, sığınılan; sahabi adı"),
    NameSuggestion("Said", "Mutlu, mesut"),
    NameSuggestion("Emin", "Güvenilir; Peygamberimizin lakabı"),
    NameSuggestion("Selim", "Sağlam, kusursuz, esen"),
    NameSuggestion("Harun", "Hz. Harun peygamber"),
    NameSuggestion("Süleyman", "Esenlik içinde; Hz. Süleyman peygamber"),
    NameSuggestion("Davud", "Sevgili; Hz. Davud peygamber")
  )

  /** Bugünün önerileri: 3 kız, 3 erkek (yılın gününe göre döner). */
  fun forToday(cal: Calendar = Calendar.getInstance()): Pair<List<NameSuggestion>, List<NameSuggestion>> {
    val d = cal.get(Calendar.DAY_OF_YEAR)
    fun pick(list: List<NameSuggestion>) = (0 until 3).map { list[(d * 3 + it) % list.size] }.map { n ->
      if (com.example.i18n.AppLanguage.isEnglish) n.copy(meaning = EnglishContent.nameMeanings[n.name] ?: n.meaning) else n
    }
    return pick(girls) to pick(boys)
  }
}

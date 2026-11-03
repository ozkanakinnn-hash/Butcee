package com.example.data

data class RamadanMenu(
  val day: Int,
  val soup: String,
  val mainDish: String,
  val sideDish: String,
  val dessert: String
)

object RamadanMenus {
  val menus: List<RamadanMenu> = listOf(
    RamadanMenu(1, "Süzme Mercimek Çorbası", "Hünkar Beğendi", "Mevsim Salatası & Pirinç Pilavı", "Fıstıklı Güllaç"),
    RamadanMenu(2, "Yayla Çorbası (Naneli)", "Fırında Sebzeli Tavuk İncik", "Arpa Şehriyeli Bulgur Pilavı", "Sütlaç"),
    RamadanMenu(3, "Ezogelin Çorbası", "Karnıyarık", "Cacık & Şehriyeli Pirinç Pilavı", "İncir Uyutması"),
    RamadanMenu(4, "Tarhana Çorbası", "Dalyan Köfte & Fırın Patates", "Havuç Tarator & Gavurdağı Salata", "Kemalpaşa Tatlısı"),
    RamadanMenu(5, "Domates Çorbası (Kaşarlı)", "Tas Kebabı", "Tereyağlı Nohutlu Pilav", "Cevizli Baklava"),
    RamadanMenu(6, "Şehriye Çorbası", "Fırında Kıymalı Patates Oturtma", "Yoğurtlu Semizotu Salatası", "Kazandibi"),
    RamadanMenu(7, "Tutmaç Çorbası", "İzmir Köfte", "Garnitürlü Pirinç Pilavı", "Fırın Helva"),
    RamadanMenu(8, "Mercimek Çorbası", "Kuzu Gerdan Haşlama", "Zeytinyağlı Taze Fasulye", "Revani"),
    RamadanMenu(9, "Kremalı Mantar Çorbası", "Tavuk Sote & Köz Biber", "Meyhane Pilavı", "Tavukgöğsü"),
    RamadanMenu(10, "Lebeniye Çorbası", "Orman Kebabı", "Zeytinyağlı Enginar & Cacık", "Trileçe"),
    RamadanMenu(11, "Ezogelin Çorbası", "Fırında Beşamel Soslu Kıymalı Karnabahar", "Domatesli Bulgur Pilavı", "Kadayıf"),
    RamadanMenu(12, "Düğün Çorbası", "Etli Güveç", "Cevizli Roka Salatası & Şehriyeli Pilav", "Etimek Tatlısı"),
    RamadanMenu(13, "Arpa Şehriye Çorbası", "Hasanpaşa Köftesi", "Köz Patlıcan Salatası", "Şekerpare"),
    RamadanMenu(14, "Yayla Çorbası", "Etli Biber Dolması", "Sarımsaklı Yoğurt & Karışık Salata", "Muhallebi"),
    RamadanMenu(15, "Sarı Mercimek Çorbası", "Ali Nazik Kebabı", "Fırınlanmış Baharatlı Patates", "Gül Suyu Aromalı Güllaç"),
    RamadanMenu(16, "Mahluta Çorbası", "Kuru Fasulye (Etli)", "Turşu & Tereyağlı Pirinç Pilavı", "Kalburabastı"),
    RamadanMenu(17, "Terbiyeli Tavuk Çorbası", "Kıymalı Tepsi Kebabı", "Ezme & Haydari & Lavaş", "Supangle"),
    RamadanMenu(18, "Köylü Çorbası", "Tavuk Fajita (Türk Usulü)", "Fırın Makarna & Yeşil Salata", "Tulumba Tatlısı"),
    RamadanMenu(19, "Kırmızı Mercimek Çorbası", "Kuzu Güveç (Sebzeli)", "Bademli Pirinç Pilavı", "Kaymaklı Ekmek Kadayıfı"),
    RamadanMenu(20, "Yoğurtlu Buğday Çorbası", "Çeltik Kebabı", "Bostana Salatası", "Magnolia"),
    RamadanMenu(21, "Ezogelin Çorbası", "Fırında Somon & Sebzeler", "Deniz Börülcesi & Roka Salatası", "Sıcak Tahin Helvası"),
    RamadanMenu(22, "Tarhana Çorbası", "Kıymalı Bezelye Yemeği", "Zeytinyağlı Yaprak Sarma", "Cevizli İrmik Helvası"),
    RamadanMenu(23, "Terbiyeli Şehriye Çorbası", "İslim Kebabı (Kürdan Kebabı)", "Soslu Makarna & Cacık", "Kıbrıs Tatlısı"),
    RamadanMenu(24, "Brokoli Çorbası", "Fırında Piliç Topkapı", "Kuru Domatesli Akdeniz Salatası", "Profiterol"),
    RamadanMenu(25, "Analı Kızlı Çorbası", "Etli Yaprak Sarma", "Közlenmiş Kırmızı Biber Salatası", "Fıstıklı Katmer"),
    RamadanMenu(26, "Mercimek Çorbası", "Kilis Tava", "Közleme Salata & Ayran", "Hira Tatlısı"),
    RamadanMenu(27, "Yayla Çorbası", "Kadir Gecesi Özel Tas Kebabı", "Safranlı Pirinç Pilavı & Komposto", "Saray Helvası & Güllaç"),
    RamadanMenu(28, "Domates Çorbası", "Fırında Sebzeli Köfte", "Piyaz & Fırın Patates", "Limonlu Cheesecake"),
    RamadanMenu(29, "Ezogelin Çorbası", "Tavuklu Sultan Kebabı", "Köz Patlıcanlı Yoğurtlu Meze", "Künefe"),
    RamadanMenu(30, "Arefe Çorbası (Yayık Ayranlı)", "Bayram Öncesi Karışık Izgara", "Zeytinyağlı Barbunya & Çoban Salata", "Ev Yapımı Bayram Baklavası")
  )

  fun getMenuForDay(dayIndex: Int): RamadanMenu {
    val safeDay = ((dayIndex - 1) % menus.size + menus.size) % menus.size
    return menus[safeDay]
  }
}

package com.example.data

/**
 * Sahur menü önerileri (30 gün). Sahur önerilerine uygun: protein ağırlıklı, yavaş sindirilen
 * karbonhidrat, su oranı yüksek sebze/meyve; tuzlu, kızartma ve şerbetliden uzak.
 */
data class SahurMenu(
  val day: Int,
  val warm: Bi,       // Sıcak başlangıç
  val protein: Bi,    // Protein
  val grain: Bi,      // Tam tahıl & sebze
  val drink: Bi       // İçecek & meyve
)

object SahurMenus {
  private fun m(d: Int, w: Pair<String, String>, p: Pair<String, String>, g: Pair<String, String>, dr: Pair<String, String>) =
    SahurMenu(d, Bi(w.first, w.second), Bi(p.first, p.second), Bi(g.first, g.second), Bi(dr.first, dr.second))

  private val menus = listOf(
    m(1, "Mercimek Çorbası" to "Lentil Soup", "Haşlanmış Yumurta & Beyaz Peynir" to "Boiled Egg & White Cheese", "Tam Buğday Ekmeği, Domates-Salatalık" to "Wholewheat Bread, Tomato & Cucumber", "Ayran & Muz" to "Ayran & Banana"),
    m(2, "Yulaf Lapası (sütlü, tarçınlı)" to "Oatmeal with Milk & Cinnamon", "Lor Peyniri & Ceviz" to "Curd Cheese & Walnuts", "Çavdar Ekmeği, Yeşillik" to "Rye Bread & Greens", "Su & Elma" to "Water & Apple"),
    m(3, "Ezogelin Çorbası" to "Ezogelin Soup", "Sebzeli Omlet" to "Vegetable Omelette", "Tam Tahıllı Ekmek, Roka" to "Wholegrain Bread & Rocket", "Süt & Kayısı Kurusu" to "Milk & Dried Apricots"),
    m(4, "Tarhana Çorbası" to "Tarhana Soup", "Menemen (az tuzlu)" to "Menemen (low salt)", "Bulgur Ekmeği, Salatalık" to "Bulgur Bread & Cucumber", "Ayran & Portakal" to "Ayran & Orange"),
    m(5, "Sebze Çorbası" to "Vegetable Soup", "Yoğurtlu Haşlanmış Patates" to "Boiled Potato with Yogurt", "Tam Buğday Lavaş, Domates" to "Wholewheat Wrap & Tomato", "Su & Karpuz" to "Water & Watermelon"),
    m(6, "Yayla Çorbası" to "Yogurt Soup (Yayla)", "Haşlanmış Yumurta & Avokado" to "Boiled Egg & Avocado", "Çavdar Ekmeği, Biber" to "Rye Bread & Peppers", "Süt & Hurma" to "Milk & Dates"),
    m(7, "Yulaf Çorbası" to "Oat Soup", "Kaşar & Zeytin (az)" to "Kashar Cheese & a Few Olives", "Tam Tahıllı Simit, Yeşillik" to "Wholegrain Simit & Greens", "Ayran & Elma" to "Ayran & Apple"),
    m(8, "Mercimek Çorbası" to "Lentil Soup", "Tavuklu Sandviç (tam buğday)" to "Chicken Sandwich (wholewheat)", "Marul, Domates" to "Lettuce & Tomato", "Su & Muz" to "Water & Banana"),
    m(9, "Sütlü Yulaf" to "Oats with Milk", "Fıstık Ezmesi & Lor" to "Peanut Butter & Curd", "Tam Buğday Ekmeği" to "Wholewheat Bread", "Süt & Çilek" to "Milk & Strawberries"),
    m(10, "Domates Çorbası" to "Tomato Soup", "Peynirli Omlet" to "Cheese Omelette", "Çavdar Ekmeği, Salatalık" to "Rye Bread & Cucumber", "Ayran & Kayısı" to "Ayran & Apricots"),
    m(11, "Ezogelin Çorbası" to "Ezogelin Soup", "Haşlanmış Nohut & Yoğurt" to "Chickpeas with Yogurt", "Bulgur Pilavı (az)" to "Small Bulgur Pilaf", "Su & Armut" to "Water & Pear"),
    m(12, "Tavuk Suyu Çorba" to "Chicken Broth Soup", "Haşlanmış Yumurta & Beyaz Peynir" to "Boiled Egg & White Cheese", "Tam Tahıllı Ekmek, Roka" to "Wholegrain Bread & Rocket", "Süt & Muz" to "Milk & Banana"),
    m(13, "Yayla Çorbası" to "Yogurt Soup (Yayla)", "Sebzeli Menemen" to "Vegetable Menemen", "Tam Buğday Lavaş" to "Wholewheat Wrap", "Ayran & Karpuz" to "Ayran & Watermelon"),
    m(14, "Yulaf Lapası (meyveli)" to "Fruit Oatmeal", "Yoğurt & Ceviz" to "Yogurt & Walnuts", "Tam Tahıllı Gevrek" to "Wholegrain Cereal", "Su & Elma" to "Water & Apple"),
    m(15, "Mercimek Çorbası" to "Lentil Soup", "Peynirli Tam Buğday Tost" to "Wholewheat Cheese Toast", "Domates-Salatalık Söğüş" to "Sliced Tomato & Cucumber", "Süt & Hurma" to "Milk & Dates"),
    m(16, "Sebze Çorbası" to "Vegetable Soup", "Haşlanmış Yumurta & Avokado" to "Boiled Egg & Avocado", "Çavdar Ekmeği" to "Rye Bread", "Ayran & Portakal" to "Ayran & Orange"),
    m(17, "Tarhana Çorbası" to "Tarhana Soup", "Lor Peynirli Omlet" to "Curd Cheese Omelette", "Tam Tahıllı Ekmek, Yeşillik" to "Wholegrain Bread & Greens", "Su & Kavun" to "Water & Melon"),
    m(18, "Sütlü Yulaf" to "Oats with Milk", "Kaşar & Ceviz" to "Kashar Cheese & Walnuts", "Tam Buğday Ekmeği, Biber" to "Wholewheat Bread & Peppers", "Süt & Muz" to "Milk & Banana"),
    m(19, "Ezogelin Çorbası" to "Ezogelin Soup", "Fırında Patates & Yoğurt" to "Baked Potato & Yogurt", "Marul Salatası" to "Lettuce Salad", "Ayran & Elma" to "Ayran & Apple"),
    m(20, "Domates Çorbası" to "Tomato Soup", "Haşlanmış Yumurta & Beyaz Peynir" to "Boiled Egg & White Cheese", "Bulgur Ekmeği, Salatalık" to "Bulgur Bread & Cucumber", "Su & Üzüm" to "Water & Grapes"),
    m(21, "Mercimek Çorbası" to "Lentil Soup", "Tavuklu Tam Buğday Dürüm" to "Chicken Wholewheat Wrap", "Roka, Domates" to "Rocket & Tomato", "Ayran & Kayısı" to "Ayran & Apricots"),
    m(22, "Yulaf Lapası (cevizli)" to "Oatmeal with Walnuts", "Lor Peyniri & Bal (az)" to "Curd Cheese & a Little Honey", "Çavdar Ekmeği" to "Rye Bread", "Süt & Çilek" to "Milk & Strawberries"),
    m(23, "Yayla Çorbası" to "Yogurt Soup (Yayla)", "Sebzeli Omlet" to "Vegetable Omelette", "Tam Tahıllı Ekmek, Salatalık" to "Wholegrain Bread & Cucumber", "Su & Karpuz" to "Water & Watermelon"),
    m(24, "Sebze Çorbası" to "Vegetable Soup", "Haşlanmış Nohut & Yoğurt" to "Chickpeas with Yogurt", "Tam Buğday Lavaş, Yeşillik" to "Wholewheat Wrap & Greens", "Ayran & Muz" to "Ayran & Banana"),
    m(25, "Tavuk Suyu Çorba" to "Chicken Broth Soup", "Peynirli Omlet" to "Cheese Omelette", "Domates-Salatalık Söğüş" to "Sliced Tomato & Cucumber", "Süt & Hurma" to "Milk & Dates"),
    m(26, "Tarhana Çorbası" to "Tarhana Soup", "Haşlanmış Yumurta & Avokado" to "Boiled Egg & Avocado", "Tam Tahıllı Ekmek, Roka" to "Wholegrain Bread & Rocket", "Su & Armut" to "Water & Pear"),
    m(27, "Sütlü Yulaf (muzlu)" to "Oats with Milk & Banana", "Yoğurt & Fındık" to "Yogurt & Hazelnuts", "Tam Tahıllı Gevrek" to "Wholegrain Cereal", "Ayran & Elma" to "Ayran & Apple"),
    m(28, "Ezogelin Çorbası" to "Ezogelin Soup", "Menemen (az tuzlu)" to "Menemen (low salt)", "Çavdar Ekmeği, Biber" to "Rye Bread & Peppers", "Süt & Kayısı Kurusu" to "Milk & Dried Apricots"),
    m(29, "Mercimek Çorbası" to "Lentil Soup", "Haşlanmış Yumurta & Beyaz Peynir" to "Boiled Egg & White Cheese", "Tam Buğday Ekmeği, Domates" to "Wholewheat Bread & Tomato", "Su & Kavun" to "Water & Melon"),
    m(30, "Domates Çorbası" to "Tomato Soup", "Lor Peynirli Tam Buğday Tost" to "Wholewheat Curd Cheese Toast", "Salatalık, Yeşillik" to "Cucumber & Greens", "Ayran & Hurma" to "Ayran & Dates")
  )

  fun forDay(day: Int): SahurMenu = menus[((day - 1) % menus.size + menus.size) % menus.size]
}

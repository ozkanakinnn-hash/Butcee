package com.example.reminder

/** Hatırlatıcı bildirimlerinde kullanılan motive edici sözler. */
object MotivationMessages {

  private val sahur = listOf(
    "Sahur yemeğinde bereket vardır. Kalkın, niyetinizi tazeleyin. 🤲",
    "Seher vakti duaların kabul vaktidir; sofraya otururken bir dua da sizden olsun.",
    "Bir yudum su, bir hurma… Sahur sünnetini ihmal etmeyin, gün boyu güç versin.",
    "Günün en bereketli saatlerindesiniz. Hayırlı sahurlar, kolay gelsin!",
    "Niyet ettim Allah rızası için yarınki orucu tutmaya… Hayırlı sahurlar.",
    "Bol su için, hafif yiyin; bugünkü orucunuz kolay ve sağlıklı geçsin."
  )

  private val iftar = listOf(
    "Sabrın sonu selamettir; iftara az kaldı. Oruçlunun iftar anındaki duası geri çevrilmez. 🤲",
    "Sofranızı hazırlarken dualarınızı da hazırlayın. Allah kabul etsin!",
    "Bir hurma ve bir yudum suyla açmak sünnettir. Hayırlı iftarlar şimdiden.",
    "Bugünkü emeğiniz boşa gitmedi; iftara dakikalar kaldı, sabrınız bereket olsun.",
    "Komşunuzu, yakınınızı hatırlayın; paylaşılan iftar daha bereketlidir.",
    "Az kaldı! Oruçluya iki sevinç vardır: biri iftar anı, diğeri Rabbine kavuşma anı."
  )

  fun randomSahur(): String = sahur.random()
  fun randomIftar(): String = iftar.random()
}

package com.example.reminder

/** Cuma hatırlatıcısında gösterilen kısa mesajlar (ayet/hadis meali kaynaklarıyla). */
object CumaMessages {
  private val tr = listOf(
    "Ey iman edenler! Cuma günü namaz için çağrı yapıldığında Allah'ı anmaya koşun. (Cuma, 9)",
    "Güneşin doğduğu en hayırlı gün Cuma günüdür. (Müslim, Cuma 17)",
    "Cuma günü yıkanıp temizlenen, güzel kokular sürünüp namaza giden kişinin iki Cuma arasındaki günahları bağışlanır. (Buhârî, Cuma 6)",
    "Cuma gününde öyle bir an vardır ki, o ana denk gelip Allah'tan hayır dileyen kula Allah dilediğini verir. (Buhârî, Cuma 37)",
    "Cuma günü bana çokça salavat getirin. (Ebû Dâvûd, Salât 207)",
    "Hayırlı Cumalar! Abdestinizi alıp camiye erken gitmeyi unutmayın.",
  )
  private val en = listOf(
    "O you who believe! When the call is made for prayer on Friday, hasten to the remembrance of Allah. (Al-Jumu'ah 62:9)",
    "The best day on which the sun rises is Friday. (Muslim)",
    "Whoever bathes on Friday, purifies himself and goes to the prayer, his sins between the two Fridays are forgiven. (Bukhari)",
    "There is an hour on Friday in which no Muslim asks Allah for good but He gives it to him. (Bukhari)",
    "Send abundant blessings upon me on Friday. (Abu Dawud)",
    "Blessed Friday! Remember to make wudu and go to the mosque early.",
  )

  fun random(english: Boolean): String = (if (english) en else tr).random()
}

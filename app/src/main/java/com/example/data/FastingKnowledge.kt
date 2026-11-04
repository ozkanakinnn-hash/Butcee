package com.example.data

import com.example.i18n.AppLanguage

/** Türkçe + İngilizce metin çifti; seçili dile göre döner. */
data class Bi(val tr: String, val en: String) {
  val text: String get() = if (AppLanguage.isEnglish) en else tr
}

data class Benefit(val title: Bi, val body: Bi, val source: String = "")

/**
 * Ramazan Rehberi içerikleri: orucun dini ve tıbbi faydaları, iftar ve sahur önerileri,
 * Ramazan boyunca her gün farklı gönderilen faydalı bilgi bildirimleri.
 * Tıbbi bilgiler genel bilgilendirme amaçlıdır; hekim tavsiyesinin yerini tutmaz.
 */
object FastingKnowledge {

  val religiousBenefits = listOf(
    Benefit(
      Bi("Takvâ: Allah'a karşı duyarlılık", "Taqwa: God-consciousness"),
      Bi("Oruç, kulun Allah'ı her an hatırlamasını ve haramlardan sakınmasını sağlar.",
        "Fasting keeps the servant mindful of God at every moment and guards against the forbidden."),
      "Bakara, 2/183"
    ),
    Benefit(
      Bi("Geçmiş günahların bağışlanması", "Forgiveness of past sins"),
      Bi("\"Kim inanarak ve karşılığını Allah'tan bekleyerek Ramazan orucunu tutarsa geçmiş günahları bağışlanır.\"",
        "\"Whoever fasts Ramadan with faith and hoping for reward, his past sins are forgiven.\""),
      "Buhârî, Îmân 28"
    ),
    Benefit(
      Bi("Oruç bir kalkandır", "Fasting is a shield"),
      Bi("Oruç; dili, gözü ve kalbi kötü söz ve davranışlardan koruyan manevi bir kalkandır.",
        "Fasting is a spiritual shield that protects the tongue, eyes and heart from evil words and deeds."),
      "Buhârî, Savm 2; Müslim, Sıyâm 162"
    ),
    Benefit(
      Bi("Reyyân kapısı", "The gate of Rayyan"),
      Bi("Cennette \"Reyyân\" adlı bir kapı vardır; oradan yalnızca oruç tutanlar girer.",
        "In Paradise there is a gate called Rayyan; only those who fast will enter through it."),
      "Buhârî, Savm 4"
    ),
    Benefit(
      Bi("Sabır ve irade terbiyesi", "Training of patience and willpower"),
      Bi("Helal olan yeme içmeden bile Allah için uzak durmak, nefse hâkim olmayı ve sabrı öğretir.",
        "Refraining even from what is lawful for God's sake teaches self-control and patience."),
    ),
    Benefit(
      Bi("Şükür ve merhamet", "Gratitude and compassion"),
      Bi("Açlığı tatmak nimetlerin kıymetini bildirir; ihtiyaç sahiplerinin hâlini anlamayı ve paylaşmayı artırır.",
        "Tasting hunger reveals the value of blessings and deepens empathy and sharing with those in need."),
    ),
    Benefit(
      Bi("İki sevinç", "Two joys"),
      Bi("\"Oruçlu için iki sevinç vardır: Biri iftar ettiği an, diğeri Rabbine kavuştuğu an.\"",
        "\"The fasting person has two joys: one when he breaks his fast, and one when he meets his Lord.\""),
      "Buhârî, Savm 9; Müslim, Sıyâm 163"
    ),
    Benefit(
      Bi("Duaların kabul vakti", "A time when prayers are accepted"),
      Bi("Oruçlunun iftar anındaki duası geri çevrilmeyen dualardandır.",
        "The supplication of the fasting person at the time of iftar is among those not turned away."),
      "Tirmizî, Deavât 128"
    )
  )

  val medicalBenefits = listOf(
    Benefit(
      Bi("Sindirim sistemine dinlenme", "Rest for the digestive system"),
      Bi("Gün boyu süren ara, mide ve bağırsaklara dinlenme fırsatı verir.",
        "The daytime break gives the stomach and intestines a chance to rest.")
    ),
    Benefit(
      Bi("Kan şekeri ve insülin dengesi", "Blood sugar and insulin balance"),
      Bi("Aralıklı açlık dönemlerinin insülin duyarlılığını artırabildiği araştırmalarda gösterilmiştir.",
        "Studies show that periods of intermittent fasting can improve insulin sensitivity.")
    ),
    Benefit(
      Bi("Hücresel yenilenme", "Cellular renewal"),
      Bi("Açlık süresince vücudun hücre içi temizlik ve yenilenme süreçleri (otofaji) desteklenebilir.",
        "During fasting the body's cellular clean-up and renewal processes (autophagy) may be supported.")
    ),
    Benefit(
      Bi("Kilo ve kolesterol kontrolü", "Weight and cholesterol control"),
      Bi("Dengeli iftar ve sahurla birlikte kilo, bel çevresi ve kan yağları üzerinde olumlu etkiler görülebilir.",
        "Together with balanced iftar and suhoor meals, positive effects on weight, waist and blood lipids may be seen.")
    ),
    Benefit(
      Bi("Kötü alışkanlıklardan uzaklaşma", "Breaking bad habits"),
      Bi("Sigara, aşırı çay-kahve ve sık atıştırmayı azaltmak için güçlü bir fırsattır.",
        "It is a strong opportunity to cut down on smoking, excess tea/coffee and frequent snacking.")
    ),
    Benefit(
      Bi("Zihinsel berraklık ve öz denetim", "Mental clarity and self-control"),
      Bi("Düzenli öğün saatleri ve irade egzersizi, odaklanmayı ve öz denetimi güçlendirebilir.",
        "Regular meal times and practising willpower can strengthen focus and self-control.")
    )
  )

  val medicalNote = Bi(
    "Kronik hastalığı olanlar, hamileler, emzirenler ve düzenli ilaç kullananlar oruç tutmadan önce hekimine danışmalıdır. Bu bilgiler genel bilgilendirme amaçlıdır.",
    "People with chronic illnesses, pregnant or breastfeeding women and those on regular medication should consult their doctor before fasting. This information is for general guidance only."
  )

  val iftarTips = listOf(
    Bi("Orucunuzu hurma, zeytin veya ılık suyla açın.", "Break your fast with dates, olives or warm water."),
    Bi("Ana yemeğe geçmeden önce 10-15 dakika ara verin; bu arada akşam namazını kılabilirsiniz.", "Pause 10-15 minutes before the main meal; you can pray maghrib in the meantime."),
    Bi("Ilık bir çorba ile başlayın; mideyi yemeğe hazırlar.", "Start with a warm soup; it prepares the stomach for the meal."),
    Bi("Yavaş yiyin ve iyice çiğneyin; tokluk hissi yaklaşık 20 dakikada oluşur.", "Eat slowly and chew well; the feeling of fullness takes about 20 minutes."),
    Bi("Porsiyonları küçük tutun, tabağın yarısını sebze ve salataya ayırın.", "Keep portions small and fill half your plate with vegetables and salad."),
    Bi("Kızartma ve çok yağlı yemekler yerine haşlama, fırın veya ızgarayı tercih edin.", "Prefer boiled, baked or grilled dishes over fried and very fatty food."),
    Bi("Tatlıyı iftardan 1-2 saat sonra yiyin; şerbetli yerine sütlü tatlı veya meyve seçin.", "Have dessert 1-2 hours after iftar; choose milk-based desserts or fruit over syrupy ones."),
    Bi("İftar ile sahur arasında 2-2,5 litre suyu yudum yudum yayarak için.", "Drink 2-2.5 litres of water spread out between iftar and suhoor."),
    Bi("Çay ve kahveyi sınırlayın; vücuttan su kaybını artırır.", "Limit tea and coffee; they increase water loss."),
    Bi("Yemekten hemen sonra uzanmayın; 1-2 saat sonra hafif bir yürüyüş yapın.", "Don't lie down right after eating; take a light walk 1-2 hours later."),
    Bi("İftar sofrasında dua etmeyi ve paylaşmayı unutmayın.", "Don't forget to make dua and share at the iftar table.")
  )

  val sahurTips = listOf(
    Bi("Sahuru atlamayın; sahur yemeğinde bereket vardır.", "Don't skip suhoor; there is blessing in it."),
    Bi("Sahuru imsak vaktine yakın yapın; gün daha kolay geçer.", "Eat suhoor close to imsak time; the day will be easier."),
    Bi("Yumurta, peynir, yoğurt gibi protein kaynakları uzun süre tok tutar.", "Protein sources like eggs, cheese and yogurt keep you full longer."),
    Bi("Tam tahıllı ekmek, yulaf ve bulgur gibi yavaş sindirilen karbonhidratları seçin.", "Choose slowly digested carbs such as wholegrain bread, oats and bulgur."),
    Bi("Salatalık, domates ve meyve gibi su oranı yüksek besinler tüketin.", "Eat foods with high water content like cucumber, tomato and fruit."),
    Bi("Tuzlu, baharatlı, turşu ve salamura yiyeceklerden kaçının; susuzluğu artırır.", "Avoid salty, spicy, pickled and brined food; they increase thirst."),
    Bi("Şekerli ve hamur işi ağırlıklı yiyecekler çabuk acıktırır, sınırlayın.", "Sugary foods and pastries make you hungry quickly; limit them."),
    Bi("Yeterince su için, ancak bir anda çok fazla su içmeyin.", "Drink enough water, but not too much at once."),
    Bi("Çay ve kahveyi azaltın; ayran veya süt tercih edin.", "Cut down on tea and coffee; prefer ayran or milk."),
    Bi("Aşırı yemekten kaçının; sahur doyurmalı, ağırlaştırmamalıdır.", "Avoid overeating; suhoor should satisfy, not weigh you down."),
    Bi("Niyetinizi tazeleyin ve seher vaktinin bereketinden dua ile faydalanın.", "Renew your intention and benefit from the blessed pre-dawn hour with dua.")
  )

  /** Ramazan boyunca her gün farklı gönderilen faydalı oruç bilgisi (1-30). */
  val dailyTips = listOf(
    Bi("Ramazan'ın ilk günü: Niyetinizi tazeleyin; bu ay sabrın, rahmetin ve paylaşmanın ayıdır. Hayırlı Ramazanlar!", "First day of Ramadan: renew your intention; this is the month of patience, mercy and sharing. Blessed Ramadan!"),
    Bi("Su ihtiyacınızı iftar ile sahur arasına yayın: her saat 1-2 bardak su içmeyi hedefleyin.", "Spread your water intake between iftar and suhoor: aim for 1-2 glasses every hour."),
    Bi("\"Oruç bir kalkandır.\" (Buhârî) Bugün dilinizi kırıcı sözlerden de koruyun.", "\"Fasting is a shield.\" (Bukhari) Guard your tongue from hurtful words today too."),
    Bi("Sahurda yumurta ve peynir gibi proteinler gün boyu tok tutar.", "Proteins like eggs and cheese at suhoor keep you full all day."),
    Bi("İftarı hurma ve su ile açmak sünnettir; kan şekerini yumuşakça yükseltir.", "Breaking the fast with dates and water is sunnah; it raises blood sugar gently."),
    Bi("Bugün bir ihtiyaç sahibine iftar ikram etmeye ne dersiniz? Oruçluya iftar ettirene onun sevabı kadar sevap vardır.", "How about offering iftar to someone in need today? Whoever feeds a fasting person earns the same reward."),
    Bi("Tuzlu ve baharatlı yiyecekler susuzluğu artırır; sahurda bunlardan kaçının.", "Salty and spicy food increases thirst; avoid them at suhoor."),
    Bi("Kur'an Ramazan ayında indirildi. Bugün birkaç sayfa okumaya vakit ayırın.", "The Quran was revealed in Ramadan. Set aside time to read a few pages today."),
    Bi("İftarda yavaş yiyin: tokluk hissi yaklaşık 20 dakikada oluşur.", "Eat slowly at iftar: the feeling of fullness takes about 20 minutes."),
    Bi("Çay ve kahve su kaybını artırır; iftardan sonra en fazla 2-3 bardakla sınırlayın.", "Tea and coffee increase water loss; limit them to 2-3 cups after iftar."),
    Bi("Sabır imanın yarısıdır. Açlık anlarında sabrınızı dua ile taçlandırın.", "Patience is half of faith. Crown your patience with dua in moments of hunger."),
    Bi("İftardan 1-2 saat sonra yapılan 20-30 dakikalık hafif yürüyüş sindirimi kolaylaştırır.", "A light 20-30 minute walk 1-2 hours after iftar helps digestion."),
    Bi("Fitre ve zekâtınızı bayramdan önce ihtiyaç sahiplerine ulaştırmayı planlayın.", "Plan to deliver your fitra and zakat to those in need before Eid."),
    Bi("Sahurda tam tahıllı ekmek veya yulaf tercih edin; enerji yavaş ve dengeli salınır.", "Choose wholegrain bread or oats at suhoor; energy is released slowly and evenly."),
    Bi("Ramazan'ın yarısına geldiniz. Allah tuttuğunuz oruçları kabul etsin!", "You've reached the middle of Ramadan. May God accept your fasts!"),
    Bi("Tatlıyı iftardan 1-2 saat sonra yiyin; sütlü tatlılar şerbetlilere göre daha hafiftir.", "Have dessert 1-2 hours after iftar; milk desserts are lighter than syrupy ones."),
    Bi("Oruç sadece mideyi değil; gözü, kulağı ve kalbi de kötülüklerden korumaktır.", "Fasting is not only of the stomach; it is guarding the eyes, ears and heart from evil."),
    Bi("Salatalık, domates ve karpuz gibi su oranı yüksek besinler susuzluğa iyi gelir.", "Foods with high water content like cucumber, tomato and watermelon help with thirst."),
    Bi("Akrabalarınızı arayın; sıla-i rahim Ramazan'da daha da kıymetlidir.", "Call your relatives; keeping family ties is even more precious in Ramadan."),
    Bi("Kızartma yerine fırın veya ızgara tercih etmek iftar sonrası ağırlığı azaltır.", "Choosing baked or grilled food instead of fried reduces heaviness after iftar."),
    Bi("Son on gün başladı. Kadir Gecesi'ni bu gecelerde arayın; ibadet ve duaya ağırlık verin.", "The last ten days have begun. Seek Laylat al-Qadr in these nights; focus on worship and dua."),
    Bi("Sahuru imsaka yakın yapmak sünnettir ve gün boyu açlık hissini azaltır.", "Delaying suhoor close to imsak is sunnah and reduces hunger during the day."),
    Bi("İtikâf, son on günde Peygamberimizin (s.a.v.) sünnetidir; imkânınız varsa değerlendirin.", "I'tikaf in the last ten days is a sunnah of the Prophet (pbuh); consider it if you can."),
    Bi("Ani tansiyon düşüklüğü hissederseniz iftarda bir bardak ayran iyi gelebilir.", "If you feel low blood pressure, a glass of ayran at iftar may help."),
    Bi("Kadir Gecesi bin aydan hayırlıdır. (Kadr, 97/3) Bu geceler için bir dua listesi hazırlayın.", "Laylat al-Qadr is better than a thousand months. (Qadr 97:3) Prepare a dua list for these nights."),
    Bi("Yemeği uyumadan en az 2 saat önce bitirmek reflü ve hazımsızlığı önler.", "Finishing food at least 2 hours before sleep helps prevent reflux and indigestion."),
    Bi("Bu gece Kadir Gecesi olabilir: \"Allah'ım, sen affedicisin, affı seversin, beni affet.\"", "Tonight may be Laylat al-Qadr: \"O God, You are forgiving and love forgiveness, so forgive me.\""),
    Bi("Bayram öncesi beslenme düzeninizi yavaş yavaş normale döndürmeyi planlayın.", "Plan to gradually return your eating pattern to normal before Eid."),
    Bi("Ramazan'ın son günlerindeyiz. Bu ayda kazandığınız güzel alışkanlıkları sürdürmeye niyet edin.", "We are in the last days of Ramadan. Intend to keep the good habits you gained this month."),
    Bi("Bayramda tatlıyı ölçülü tüketin, bol su için. Allah oruçlarınızı kabul etsin, bayramınız mübarek olsun!", "Enjoy sweets moderately at Eid and drink plenty of water. May God accept your fasts, Eid Mubarak!")
  )

  fun tipForDay(day: Int): Bi = dailyTips[(day - 1).mod(dailyTips.size)]
}

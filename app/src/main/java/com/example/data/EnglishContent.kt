package com.example.data

/**
 * İngilizce içerik (uygulama dili English iken kullanılır).
 * Kur'an meâlleri, telifli bir çeviriden alınmamıştır; anlamı esas alan sade İngilizce
 * karşılıklardır. Yayından önce bir uzmana kontrol ettirilmesi önerilir.
 */
object EnglishContent {

  // ─────────────────────────── Yemek adları ───────────────────────────
  val dishes: Map<String, String> = mapOf(
    "Süzme Mercimek Çorbası" to "Strained Lentil Soup", "Hünkar Beğendi" to "Hünkar Beğendi (Lamb on Smoky Eggplant Purée)",
    "Mevsim Salatası & Pirinç Pilavı" to "Seasonal Salad & Rice Pilaf", "Fıstıklı Güllaç" to "Güllaç with Pistachios",
    "Yayla Çorbası (Naneli)" to "Yogurt Soup with Mint", "Fırında Sebzeli Tavuk İncik" to "Oven-Baked Chicken Drumsticks with Vegetables",
    "Arpa Şehriyeli Bulgur Pilavı" to "Bulgur Pilaf with Orzo", "Sütlaç" to "Rice Pudding",
    "Ezogelin Çorbası" to "Ezogelin (Red Lentil & Bulgur) Soup", "Karnıyarık" to "Stuffed Eggplant with Minced Meat",
    "Cacık & Şehriyeli Pirinç Pilavı" to "Cacık (Yogurt & Cucumber) & Rice Pilaf with Vermicelli", "İncir Uyutması" to "Fig & Milk Pudding",
    "Tarhana Çorbası" to "Tarhana Soup", "Dalyan Köfte & Fırın Patates" to "Dalyan Meatloaf & Roast Potatoes",
    "Havuç Tarator & Gavurdağı Salata" to "Carrot Tarator & Gavurdağı Salad", "Kemalpaşa Tatlısı" to "Kemalpaşa Syrup Dumplings",
    "Domates Çorbası (Kaşarlı)" to "Tomato Soup with Cheese", "Tas Kebabı" to "Tas Kebab (Braised Beef Stew)",
    "Tereyağlı Nohutlu Pilav" to "Buttered Rice Pilaf with Chickpeas", "Cevizli Baklava" to "Walnut Baklava",
    "Şehriye Çorbası" to "Vermicelli Soup", "Fırında Kıymalı Patates Oturtma" to "Baked Potatoes with Minced Meat",
    "Yoğurtlu Semizotu Salatası" to "Purslane Salad with Yogurt", "Kazandibi" to "Kazandibi (Caramelized Milk Pudding)",
    "Tutmaç Çorbası" to "Tutmaç (Noodle & Lentil) Soup", "İzmir Köfte" to "İzmir Meatballs in Tomato Sauce",
    "Garnitürlü Pirinç Pilavı" to "Rice Pilaf with Vegetables", "Fırın Helva" to "Baked Halva",
    "Mercimek Çorbası" to "Lentil Soup", "Kuzu Gerdan Haşlama" to "Boiled Lamb Neck", "Zeytinyağlı Taze Fasulye" to "Green Beans in Olive Oil", "Revani" to "Revani (Semolina Cake)",
    "Kremalı Mantar Çorbası" to "Cream of Mushroom Soup", "Tavuk Sote & Köz Biber" to "Chicken Sauté & Roasted Peppers", "Meyhane Pilavı" to "Bulgur Pilaf with Tomatoes & Peppers", "Tavukgöğsü" to "Tavukgöğsü (Chicken Breast Pudding)",
    "Lebeniye Çorbası" to "Lebeniye (Yogurt & Chickpea) Soup", "Orman Kebabı" to "Forest Kebab (Meat & Vegetable Stew)", "Zeytinyağlı Enginar & Cacık" to "Artichokes in Olive Oil & Cacık", "Trileçe" to "Trileçe (Milk Cake)",
    "Fırında Beşamel Soslu Kıymalı Karnabahar" to "Baked Cauliflower with Minced Meat & Béchamel", "Domatesli Bulgur Pilavı" to "Tomato Bulgur Pilaf", "Kadayıf" to "Kadayıf",
    "Düğün Çorbası" to "Wedding Soup", "Etli Güveç" to "Meat Casserole", "Cevizli Roka Salatası & Şehriyeli Pilav" to "Rocket Salad with Walnuts & Rice Pilaf", "Etimek Tatlısı" to "Etimek (Rusk) Dessert",
    "Arpa Şehriye Çorbası" to "Orzo Soup", "Hasanpaşa Köftesi" to "Hasanpaşa Meatballs", "Köz Patlıcan Salatası" to "Smoky Eggplant Salad", "Şekerpare" to "Şekerpare (Syrup Cookies)",
    "Yayla Çorbası" to "Yogurt Soup", "Etli Biber Dolması" to "Peppers Stuffed with Meat", "Sarımsaklı Yoğurt & Karışık Salata" to "Garlic Yogurt & Mixed Salad", "Muhallebi" to "Milk Pudding",
    "Sarı Mercimek Çorbası" to "Yellow Lentil Soup", "Ali Nazik Kebabı" to "Ali Nazik Kebab", "Fırınlanmış Baharatlı Patates" to "Spiced Roast Potatoes", "Gül Suyu Aromalı Güllaç" to "Rosewater Güllaç",
    "Mahluta Çorbası" to "Mahluta (Lentil & Rice) Soup", "Kuru Fasulye (Etli)" to "White Bean Stew with Meat", "Turşu & Tereyağlı Pirinç Pilavı" to "Pickles & Buttered Rice Pilaf", "Kalburabastı" to "Kalburabastı (Syrup Pastries)",
    "Terbiyeli Tavuk Çorbası" to "Chicken Soup with Egg & Lemon", "Kıymalı Tepsi Kebabı" to "Minced Meat Tray Kebab", "Ezme & Haydari & Lavaş" to "Ezme, Haydari & Lavash", "Supangle" to "Chocolate Pudding",
    "Köylü Çorbası" to "Village Soup", "Tavuk Fajita (Türk Usulü)" to "Turkish-Style Chicken Fajita", "Fırın Makarna & Yeşil Salata" to "Baked Pasta & Green Salad", "Tulumba Tatlısı" to "Tulumba (Fried Syrup Dough)",
    "Kırmızı Mercimek Çorbası" to "Red Lentil Soup", "Kuzu Güveç (Sebzeli)" to "Lamb & Vegetable Casserole", "Bademli Pirinç Pilavı" to "Rice Pilaf with Almonds", "Kaymaklı Ekmek Kadayıfı" to "Bread Kadayıf with Clotted Cream",
    "Yoğurtlu Buğday Çorbası" to "Wheat & Yogurt Soup", "Çeltik Kebabı" to "Çeltik Kebab", "Bostana Salatası" to "Bostana Salad", "Magnolia" to "Magnolia (Cream & Biscuit Dessert)",
    "Fırında Somon & Sebzeler" to "Baked Salmon & Vegetables", "Deniz Börülcesi & Roka Salatası" to "Samphire & Rocket Salad", "Sıcak Tahin Helvası" to "Warm Tahini Halva",
    "Kıymalı Bezelye Yemeği" to "Peas with Minced Meat", "Zeytinyağlı Yaprak Sarma" to "Stuffed Vine Leaves in Olive Oil", "Cevizli İrmik Helvası" to "Semolina Halva with Walnuts",
    "Terbiyeli Şehriye Çorbası" to "Vermicelli Soup with Egg & Lemon", "İslim Kebabı (Kürdan Kebabı)" to "İslim Kebab (Eggplant-Wrapped Meatballs)", "Soslu Makarna & Cacık" to "Pasta with Sauce & Cacık", "Kıbrıs Tatlısı" to "Cyprus Dessert",
    "Brokoli Çorbası" to "Broccoli Soup", "Fırında Piliç Topkapı" to "Topkapı Baked Chicken", "Kuru Domatesli Akdeniz Salatası" to "Mediterranean Salad with Sun-Dried Tomatoes", "Profiterol" to "Profiteroles",
    "Analı Kızlı Çorbası" to "Analı Kızlı (Bulgur Dumpling) Soup", "Etli Yaprak Sarma" to "Vine Leaves Stuffed with Meat", "Közlenmiş Kırmızı Biber Salatası" to "Roasted Red Pepper Salad", "Fıstıklı Katmer" to "Katmer with Pistachios",
    "Kilis Tava" to "Kilis Tava (Baked Meat & Vegetables)", "Közleme Salata & Ayran" to "Roasted Vegetable Salad & Ayran", "Hira Tatlısı" to "Hira Dessert",
    "Kadir Gecesi Özel Tas Kebabı" to "Night of Decree Special Tas Kebab", "Safranlı Pirinç Pilavı & Komposto" to "Saffron Rice Pilaf & Fruit Compote", "Saray Helvası & Güllaç" to "Palace Halva & Güllaç",
    "Domates Çorbası" to "Tomato Soup", "Fırında Sebzeli Köfte" to "Baked Meatballs with Vegetables", "Piyaz & Fırın Patates" to "Bean Salad (Piyaz) & Roast Potatoes", "Limonlu Cheesecake" to "Lemon Cheesecake",
    "Tavuklu Sultan Kebabı" to "Chicken Sultan Kebab", "Köz Patlıcanlı Yoğurtlu Meze" to "Smoky Eggplant & Yogurt Meze", "Künefe" to "Künefe",
    "Arefe Çorbası (Yayık Ayranlı)" to "Eve-of-Eid Soup (with Churned Ayran)", "Bayram Öncesi Karışık Izgara" to "Pre-Eid Mixed Grill", "Zeytinyağlı Barbunya & Çoban Salata" to "Borlotti Beans in Olive Oil & Shepherd's Salad", "Ev Yapımı Bayram Baklavası" to "Homemade Eid Baklava"
  )

  fun dish(tr: String): String = dishes[tr] ?: tr

  // ─────────────────────── Günün ayet / hadis / duası ───────────────────────
  data class EnSpiritual(
    val verse: String, val verseSource: String,
    val hadith: String, val hadithSource: String,
    val prayer: String, val prayerSource: String,
    val esmaMeaning: String
  )

  val spiritual: Map<Int, EnSpiritual> = mapOf(
    1 to EnSpiritual(
      "The month of Ramadan is the month in which the Quran was revealed as guidance for people, with clear proofs of guidance and of the criterion between right and wrong.", "Surah Al-Baqarah 2:185",
      "Whoever fasts Ramadan out of faith and hoping for reward from Allah alone, his past sins will be forgiven.", "Bukhari, Sawm 6; Muslim, Musafirin 175",
      "O Allah! You are Most Forgiving and You love to forgive, so forgive me and those I love.", "Tirmidhi, Da'awat 84",
      "The Most Gracious, who shows mercy and gives provision to all creation in this world."),
    2 to EnSpiritual(
      "O you who believe! Fasting has been prescribed for you as it was prescribed for those before you, so that you may attain God-consciousness.", "Surah Al-Baqarah 2:183",
      "Fasting is a shield. The one who fasts should not speak ill or behave foolishly; if someone fights or insults him, let him say: 'I am fasting.'", "Bukhari, Sawm 9",
      "O Allah! For Your sake I fasted, in You I believe, upon You I rely, and with Your provision I break my fast.", "Abu Dawud, Sawm 22",
      "The Most Merciful, who shows special mercy to the believers in the Hereafter."),
    3 to EnSpiritual(
      "When My servants ask you about Me, I am indeed near. I answer the call of the one who calls upon Me.", "Surah Al-Baqarah 2:186",
      "Three prayers are not rejected: the prayer of the fasting person until he breaks his fast, the prayer of a just ruler, and the prayer of the oppressed.", "Tirmidhi, Da'awat 48",
      "Our Lord! Give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.", "Surah Al-Baqarah 2:201",
      "The Ever-Forgiving, who covers, pardons and conceals the faults of His servants."),
    4 to EnSpiritual(
      "Seek help through patience and prayer. Indeed, it is hard except for the humble.", "Surah Al-Baqarah 2:45",
      "Eat suhoor, for there is blessing in suhoor.", "Bukhari, Sawm 20; Muslim, Siyam 45",
      "O Allah! Keep our hearts firm upon Your religion and do not let us stray from the straight path.", "Tirmidhi, Qadar 7",
      "The Most Generous, abundant in kindness and giving without expecting return."),
    5 to EnSpiritual(
      "Surely, with hardship comes ease.", "Surah Ash-Sharh 94:6",
      "The fasting person has two joys: one when he breaks his fast, and one when he meets his Lord.", "Muslim, Siyam 163",
      "O Allah! I seek refuge in You from knowledge that does not benefit, from a heart that is not humble, from a soul that is never satisfied, and from a prayer that is not answered.", "Muslim, Dhikr 73",
      "The Responsive, who answers those who call upon Him."),
    6 to EnSpiritual(
      "And say: My Lord, have mercy on them as they raised me when I was small.", "Surah Al-Isra 17:24",
      "Whoever does not show mercy to people, Allah will not show mercy to him.", "Bukhari, Tawhid 2",
      "Our Lord! Forgive us and our brothers who preceded us in faith, and leave no resentment in our hearts toward those who believe.", "Surah Al-Hashr 59:10",
      "The All-Encompassing, whose knowledge, mercy, power and kindness embrace everything."),
    7 to EnSpiritual(
      "Those who believe and whose hearts find peace in the remembrance of Allah. Surely, in the remembrance of Allah hearts find peace.", "Surah Ar-Ra'd 13:28",
      "In Paradise there is a gate called Ar-Rayyan; on the Day of Judgment only those who fasted will enter through it.", "Bukhari, Sawm 4; Muslim, Siyam 166",
      "O Allah! Grant us guidance, piety, chastity and contentment of heart.", "Muslim, Dhikr 72",
      "The Source of Peace, free from all imperfection, who grants safety and well-being."),
    8 to EnSpiritual(
      "Whoever is mindful of Allah, He will make a way out for him and provide for him from where he does not expect.", "Surah At-Talaq 65:2-3",
      "Whoever provides iftar for a fasting person earns the same reward, without anything being taken from the reward of the fasting person.", "Tirmidhi, Sawm 82",
      "O Allah! Suffice me with what You have made lawful against what You have forbidden, and by Your grace make me independent of all besides You.", "Tirmidhi, Da'awat 111",
      "The Provider, who gives sustenance to all beings and meets their needs."),
    9 to EnSpiritual(
      "Do good; indeed Allah loves those who do good.", "Surah Al-Baqarah 2:195",
      "When Ramadan begins, the gates of Paradise are opened, the gates of Hell are closed and the devils are chained.", "Bukhari, Bad' al-Khalq 11",
      "My Lord! Make me and my descendants steadfast in prayer. Our Lord, accept my prayer.", "Surah Ibrahim 14:40",
      "The Most Loving, who loves His servants and is most worthy of love."),
    10 to EnSpiritual(
      "So remember Me; I will remember you. Be grateful to Me and do not be ungrateful.", "Surah Al-Baqarah 2:152",
      "The first ten days of Ramadan are mercy, the middle ten forgiveness, and the last ten freedom from the Fire.", "Ibn Abi Shaybah, Musannaf 2/429",
      "O Allah! Forgive all my sins, the small and the great, the first and the last, the open and the hidden.", "Muslim, Salat 216",
      "The Forbearing, who is not hasty in punishing and is most gentle and tolerant."),
    15 to EnSpiritual(
      "Say: O My servants who have wronged themselves, do not despair of the mercy of Allah. Indeed, Allah forgives all sins.", "Surah Az-Zumar 39:53",
      "Whoever does not give up false speech and acting upon it, Allah has no need of him giving up his food and drink.", "Bukhari, Sawm 8",
      "Our Lord! Grant us mercy from Yourself and guide us rightly in our affair.", "Surah Al-Kahf 18:10",
      "The Accepter of Repentance, who opens His door to those who return in regret."),
    27 to EnSpiritual(
      "Indeed, We sent it down on the Night of Decree. And what will make you know what the Night of Decree is? The Night of Decree is better than a thousand months.", "Surah Al-Qadr 97:1-3",
      "Seek the Night of Decree in the odd nights of the last ten days of Ramadan.", "Bukhari, Fadl Laylat al-Qadr 3",
      "O Allah! You are Most Forgiving and Generous, and You love to forgive, so forgive us.", "Tirmidhi, Da'awat 84 (Aisha r.a.)",
      "The Pardoner, whose pardon is limitless and who erases every trace of sin."),
    30 to EnSpiritual(
      "So that you may complete the prescribed days and glorify Allah for guiding you, and so that you may be grateful.", "Surah Al-Baqarah 2:185",
      "Fast and you will find health.", "Tabarani, al-Mu'jam al-Awsat 8312",
      "O Allah! Accept our fasts, our prayers and our charity, and let us reach the Eid as those who are forgiven.", "Traditional end-of-Ramadan prayer",
      "The Most Appreciative, who greatly multiplies the reward of the grateful."),
    // Ramazan dışı genel liste
    101 to EnSpiritual("Surely, in the remembrance of Allah hearts find peace.", "Surah Ar-Ra'd 13:28",
      "Make things easy and do not make them difficult; give good news and do not drive people away.", "Bukhari, 'Ilm 11",
      "Our Lord! Give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.", "Surah Al-Baqarah 2:201",
      "The Source of Peace, who keeps His servants safe from every harm."),
    102 to EnSpiritual("Surely, with hardship comes ease.", "Surah Ash-Sharh 94:5",
      "A Muslim is the one from whose tongue and hand other Muslims are safe.", "Bukhari, Iman 4",
      "My Lord! Increase me in knowledge.", "Surah Ta-Ha 20:114",
      "The Opener, who resolves every difficulty and opens the doors of goodness."),
    103 to EnSpiritual("So remember Me; I will remember you.", "Surah Al-Baqarah 2:152",
      "The best of you are those with the best character.", "Bukhari, Adab 39",
      "Our Lord! Do not let our hearts deviate after You have guided us.", "Surah Al-i Imran 3:8",
      "The Most Loving, who loves His servants and is most worthy of love."),
    104 to EnSpiritual("Whoever puts their trust in Allah, He is sufficient for them.", "Surah At-Talaq 65:3",
      "Actions are judged by intentions.", "Bukhari, Bad' al-Wahy 1",
      "My Lord! Expand my chest for me and ease my task for me.", "Surah Ta-Ha 20:25-26",
      "The Trustee, who best arranges the affairs of those who rely on Him."),
    105 to EnSpiritual("The believers are but brothers.", "Surah Al-Hujurat 49:10",
      "None of you truly believes until he wishes for his brother what he wishes for himself.", "Bukhari, Iman 7",
      "Our Lord! Forgive us and our brothers who preceded us in faith.", "Surah Al-Hashr 59:10",
      "The All-Forgiving, who forgives abundantly."),
    106 to EnSpiritual("Allah does not burden any soul beyond what it can bear.", "Surah Al-Baqarah 2:286",
      "Your smile in the face of your brother is charity.", "Tirmidhi, Birr 36",
      "Our Lord! Do not hold us accountable if we forget or make a mistake.", "Surah Al-Baqarah 2:286",
      "The Most Merciful, who shows endless mercy to the believers in the Hereafter."),
    107 to EnSpiritual("He is with you wherever you are.", "Surah Al-Hadid 57:4",
      "Cleanliness is half of faith.", "Muslim, Taharah 1",
      "O Allah! I ask You for beneficial knowledge, lawful provision and accepted deeds.", "Ibn Majah, Iqamat as-Salat 32",
      "The Provider, who gives sustenance to every living being."),
    108 to EnSpiritual("Your Lord has decreed that you worship none but Him, and that you be good to your parents.", "Surah Al-Isra 17:23",
      "The pleasure of Allah lies in the pleasure of the parents.", "Tirmidhi, Birr 3",
      "My Lord! Have mercy on them as they raised me when I was small.", "Surah Al-Isra 17:24",
      "The Source of Goodness, abundant in kindness and generosity."),
    109 to EnSpiritual("Indeed, prayer keeps one away from indecency and wrongdoing.", "Surah Al-'Ankabut 29:45",
      "The five daily prayers are an expiation for the (minor) sins committed between them.", "Muslim, Taharah 16",
      "My Lord! Make me and my descendants steadfast in prayer.", "Surah Ibrahim 14:40",
      "The Responsive, who answers prayers."),
    110 to EnSpiritual("Indeed, Allah is with those who are patient.", "Surah Al-Baqarah 2:153",
      "The strong one is the one who controls himself when angry.", "Bukhari, Adab 76",
      "Our Lord! Pour patience upon us and make our steps firm.", "Surah Al-Baqarah 2:250",
      "The Most Patient, who is never hasty in punishment.")
  )

  // ─────────────────────────── İsim anlamları ───────────────────────────
  val nameMeanings: Map<String, String> = mapOf(
    "Zeynep" to "Precious stone; name of the Prophet's daughter", "Fatma" to "Name of the Prophet's daughter Fatima",
    "Ayşe" to "Living, full of life; name of Aisha (r.a.)", "Hatice" to "Name of Khadija, the Prophet's first wife",
    "Meryem" to "Mary, mother of Jesus; the only woman named in the Quran", "Elif" to "First letter of the Arabic alphabet; uprightness",
    "Zehra" to "Radiant, bright; a title of Fatima", "Esma" to "Exalted; names (from Allah's Beautiful Names)",
    "Rabia" to "The fourth; name of the saint Rabia al-Adawiyya", "Hümeyra" to "Rosy-cheeked; a nickname of Aisha (r.a.)",
    "Sümeyye" to "Exalted; name of the first female martyr of Islam", "Havva" to "Eve, the wife of Adam; giver of life",
    "Nur" to "Light, radiance", "Hafsa" to "Name of Hafsa, wife of the Prophet", "Saliha" to "Righteous, good, doer of good deeds",
    "Büşra" to "Good news, glad tidings", "Şeyma" to "Name of the Prophet's foster sister", "Kevser" to "Abundance; a river in Paradise and a surah",
    "Asiye" to "Name of the believing wife of Pharaoh", "Beyza" to "White, pure", "Selma" to "Safe, peaceful, secure", "Hilal" to "Crescent moon",
    "Sena" to "Praise", "Zübeyde" to "Essence, the choicest", "Meva" to "Refuge, shelter; a garden of Paradise",
    "Tuba" to "Blessedness; a tree in Paradise", "Feyza" to "Abundance, blessing", "Ravza" to "Garden; the Rawdah in the Prophet's Mosque",
    "Safiye" to "Pure, sincere", "Rukiye" to "Rising; name of the Prophet's daughter", "Sare" to "Sarah, wife of Abraham; one who brings joy",
    "İkra" to "Read; the first command of the Quran",
    "Muhammed" to "The praised one; name of the Prophet", "Ahmet" to "Most praised; a name of the Prophet",
    "Mustafa" to "The chosen one; a title of the Prophet", "Ali" to "Exalted, high; name of the fourth caliph",
    "Ömer" to "Long-lived; name of the second caliph", "Osman" to "Name of Uthman, the third caliph",
    "Hasan" to "Good, beautiful; the Prophet's grandson", "Hüseyin" to "Little beautiful one; the Prophet's grandson",
    "Yusuf" to "Prophet Joseph", "İbrahim" to "Prophet Abraham, father of the prophets", "İsmail" to "God has heard; Prophet Ishmael",
    "Yahya" to "Living; Prophet John", "Enes" to "Close friend, affectionate; a companion's name", "Hamza" to "Strong, steadfast; the Prophet's uncle",
    "Bilal" to "Refreshing; the first muezzin of Islam", "Salih" to "Righteous; Prophet Salih", "Abdullah" to "Servant of Allah",
    "Yunus" to "Prophet Jonah; dove", "Musa" to "Prophet Moses", "Taha" to "A surah of the Quran; a name of the Prophet",
    "Yasin" to "A surah known as the heart of the Quran", "Furkan" to "The criterion between truth and falsehood; a name of the Quran",
    "Bekir" to "Firstborn; from the name of Abu Bakr", "Halil" to "Close friend; a title of Abraham", "Eymen" to "Blessed, fortunate",
    "Muaz" to "Protected, sheltered; a companion's name", "Said" to "Happy, fortunate", "Emin" to "Trustworthy; a title of the Prophet",
    "Selim" to "Sound, flawless, safe", "Harun" to "Prophet Aaron", "Süleyman" to "One who lives in peace; Prophet Solomon", "Davud" to "Beloved; Prophet David"
  )

  // ─────────────────────────── Sure adları (anlam) ───────────────────────────
  val surahMeanings: List<String> = listOf(
    "The Opening", "The Cow", "The Family of Imran", "Women", "The Table Spread", "Cattle", "The Heights", "The Spoils of War", "Repentance", "Jonah",
    "Hud", "Joseph", "Thunder", "Abraham", "The Rocky Tract", "The Bee", "The Night Journey", "The Cave", "Mary", "Ta-Ha",
    "The Prophets", "The Pilgrimage", "The Believers", "The Light", "The Criterion", "The Poets", "The Ant", "The Stories", "The Spider", "The Romans",
    "Luqman", "The Prostration", "The Confederates", "Sheba", "The Originator", "Ya-Sin", "Those Ranged in Ranks", "Sad", "The Groups", "The Forgiver",
    "Explained in Detail", "Consultation", "Ornaments of Gold", "The Smoke", "The Kneeling", "The Sand Dunes", "Muhammad", "The Victory", "The Private Chambers", "Qaf",
    "The Scattering Winds", "The Mount", "The Star", "The Moon", "The Most Merciful", "The Inevitable Event", "Iron", "The Pleading Woman", "The Exile", "She Who Is Examined",
    "The Ranks", "Friday", "The Hypocrites", "Mutual Loss and Gain", "Divorce", "The Prohibition", "Sovereignty", "The Pen", "The Reality", "The Ascending Stairways",
    "Noah", "The Jinn", "The Enwrapped One", "The Cloaked One", "The Resurrection", "Man", "Those Sent Forth", "The Great News", "Those Who Pull Out", "He Frowned",
    "The Folding Up", "The Cleaving", "Those Who Give Short Measure", "The Splitting Open", "The Constellations", "The Night Visitor", "The Most High", "The Overwhelming Event", "The Dawn", "The City",
    "The Sun", "The Night", "The Morning Light", "The Relief", "The Fig", "The Clot", "The Night of Decree", "The Clear Proof", "The Earthquake", "The Chargers",
    "The Striking Calamity", "Competition in Increase", "Time", "The Slanderer", "The Elephant", "Quraysh", "Small Kindnesses", "Abundance", "The Disbelievers", "Divine Help",
    "The Palm Fibre", "Sincerity", "The Daybreak", "Mankind"
  )

  fun surahMeaning(number: Int): String? = surahMeanings.getOrNull(number - 1)

  // ───────────────────── Sure metinleri: okunuş + anlam ─────────────────────
  private const val BISMILLAH_T = "Bismillahir-Rahmanir-Rahim."
  private const val BISMILLAH_M = "In the name of Allah, the Most Gracious, the Most Merciful."

  private fun lines(vararg l: String) = l.mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")

  /** surahNumber -> (İngilizce okunuş, İngilizce anlam) */
  val surahTexts: Map<Int, Pair<String, String>> = mapOf(
    1 to (BISMILLAH_T + "\n" + lines(
      "Al-hamdu lillahi Rabbil-'alamin.", "Ar-Rahmanir-Rahim.", "Maliki yawmid-din.", "Iyyaka na'budu wa iyyaka nasta'in.",
      "Ihdinas-siratal-mustaqim.", "Siratal-ladhina an'amta 'alayhim.", "Ghayril-maghdubi 'alayhim wa lad-dallin. (Amin)"
    ) to BISMILLAH_M + "\n" + lines(
      "All praise is due to Allah, Lord of all the worlds.", "The Most Gracious, the Most Merciful.", "Master of the Day of Judgment.",
      "You alone we worship, and You alone we ask for help.", "Guide us to the straight path,", "the path of those You have blessed,",
      "not of those who have earned Your anger, nor of those who have gone astray. (Amin)"
    )),
    112 to (BISMILLAH_T + "\n" + lines("Qul huwallahu ahad.", "Allahus-samad.", "Lam yalid wa lam yulad.", "Wa lam yakul-lahu kufuwan ahad.")
      to BISMILLAH_M + "\n" + lines("Say: He is Allah, the One.", "Allah, the Eternal Refuge, on whom all depend.", "He neither begets nor is born,", "and there is none comparable to Him.")),
    113 to (BISMILLAH_T + "\n" + lines("Qul a'udhu bi-rabbil-falaq.", "Min sharri ma khalaq.", "Wa min sharri ghasiqin idha waqab.", "Wa min sharrin-naffathati fil-'uqad.", "Wa min sharri hasidin idha hasad.")
      to BISMILLAH_M + "\n" + lines("Say: I seek refuge in the Lord of the daybreak", "from the evil of what He has created,", "and from the evil of the darkness when it settles,", "and from the evil of those who blow on knots,", "and from the evil of the envier when he envies.")),
    114 to (BISMILLAH_T + "\n" + lines("Qul a'udhu bi-rabbin-nas.", "Malikin-nas.", "Ilahin-nas.", "Min sharril-waswasil-khannas.", "Alladhi yuwaswisu fi sudurin-nas.", "Minal-jinnati wan-nas.")
      to BISMILLAH_M + "\n" + lines("Say: I seek refuge in the Lord of mankind,", "the King of mankind,", "the God of mankind,", "from the evil of the whisperer who withdraws,", "who whispers into the hearts of mankind,", "from among the jinn and mankind.")),
    108 to (BISMILLAH_T + "\n" + lines("Inna a'taynakal-kawthar.", "Fasalli li-rabbika wanhar.", "Inna shani'aka huwal-abtar.")
      to BISMILLAH_M + "\n" + lines("Indeed, We have granted you abundance (al-Kawthar).", "So pray to your Lord and sacrifice.", "Surely, it is your enemy who will be cut off.")),
    103 to (BISMILLAH_T + "\n" + lines("Wal-'asr.", "Innal-insana lafi khusr.", "Illal-ladhina amanu wa 'amilus-salihati wa tawasaw bil-haqqi wa tawasaw bis-sabr.")
      to BISMILLAH_M + "\n" + lines("By time,", "surely mankind is in loss,", "except those who believe, do good deeds, and urge one another to truth and to patience.")),
    97 to (BISMILLAH_T + "\n" + lines("Inna anzalnahu fi laylatil-qadr.", "Wa ma adraka ma laylatul-qadr.", "Laylatul-qadri khayrum-min alfi shahr.", "Tanazzalul-mala'ikatu war-ruhu fiha bi-idhni rabbihim min kulli amr.", "Salamun hiya hatta matla'il-fajr.")
      to BISMILLAH_M + "\n" + lines("Indeed, We sent it (the Quran) down on the Night of Decree.", "And what will make you know what the Night of Decree is?", "The Night of Decree is better than a thousand months.", "The angels and the Spirit descend in it by the permission of their Lord for every matter.", "Peace it is, until the break of dawn.")),
    94 to (BISMILLAH_T + "\n" + lines("Alam nashrah laka sadrak.", "Wa wada'na 'anka wizrak.", "Alladhi anqada zahrak.", "Wa rafa'na laka dhikrak.", "Fa-inna ma'al-'usri yusra.", "Inna ma'al-'usri yusra.", "Fa-idha faraghta fansab.", "Wa ila rabbika farghab.")
      to BISMILLAH_M + "\n" + lines("Have We not expanded your chest for you,", "and removed from you your burden", "which weighed down your back,", "and raised high your mention?", "So surely, with hardship comes ease.", "Surely, with hardship comes ease.", "So when you have finished, strive on,", "and to your Lord turn with longing.")),
    93 to (BISMILLAH_T + "\n" + lines("Wad-duha.", "Wal-layli idha saja.", "Ma wadda'aka rabbuka wa ma qala.", "Wa lal-akhiratu khayrul-laka minal-ula.", "Wa la-sawfa yu'tika rabbuka fatarda.", "Alam yajidka yatiman fa-awa.", "Wa wajadaka dallan fahada.", "Wa wajadaka 'a'ilan fa-aghna.", "Fa-ammal-yatima fala taqhar.", "Wa ammas-sa'ila fala tanhar.", "Wa amma bi-ni'mati rabbika fahaddith.")
      to BISMILLAH_M + "\n" + lines("By the morning light,", "and by the night when it grows still,", "your Lord has not abandoned you, nor is He displeased.", "And the Hereafter is better for you than this life.", "And your Lord will give you, and you will be pleased.", "Did He not find you an orphan and shelter you?", "Did He not find you searching and guide you?", "Did He not find you in need and make you self-sufficient?", "So do not oppress the orphan,", "and do not turn away the one who asks,", "and proclaim the blessings of your Lord.")),
    109 to (BISMILLAH_T + "\n" + lines("Qul ya ayyuhal-kafirun.", "La a'budu ma ta'budun.", "Wa la antum 'abiduna ma a'bud.", "Wa la ana 'abidum-ma 'abadtum.", "Wa la antum 'abiduna ma a'bud.", "Lakum dinukum wa liya din.")
      to BISMILLAH_M + "\n" + lines("Say: O disbelievers,", "I do not worship what you worship,", "nor do you worship what I worship,", "nor will I worship what you worship,", "nor will you worship what I worship.", "To you your religion, and to me mine.")),
    106 to (BISMILLAH_T + "\n" + lines("Li-ilafi Quraysh.", "Ilafihim rihlatash-shita'i was-sayf.", "Fal-ya'budu rabba hadhal-bayt.", "Alladhi at'amahum min ju'in wa amanahum min khawf.")
      to BISMILLAH_M + "\n" + lines("For the security of the Quraysh,", "their security in the journeys of winter and summer,", "let them worship the Lord of this House,", "who fed them against hunger and made them safe from fear.")),
    105 to (BISMILLAH_T + "\n" + lines("Alam tara kayfa fa'ala rabbuka bi-ashabil-fil.", "Alam yaj'al kaydahum fi tadlil.", "Wa arsala 'alayhim tayran ababil.", "Tarmihim bi-hijaratim-min sijjil.", "Fa-ja'alahum ka-'asfim-ma'kul.")
      to BISMILLAH_M + "\n" + lines("Have you not seen how your Lord dealt with the people of the elephant?", "Did He not make their plan go astray?", "And He sent against them birds in flocks,", "striking them with stones of baked clay,", "and made them like chewed-up straw.")),
    36 to (BISMILLAH_T + "\n" + lines("Ya-Sin.", "Wal-Qur'anil-hakim.", "Innaka laminal-mursalin.", "'Ala siratim-mustaqim.", "Tanzilal-'azizir-rahim.", "Li-tundhira qawmam-ma undhira aba'uhum fahum ghafilun.", "Laqad haqqal-qawlu 'ala aktharihim fahum la yu'minun.")
      to BISMILLAH_M + "\n" + lines("Ya-Sin.", "By the Quran, full of wisdom,", "you are indeed one of the messengers,", "on a straight path.", "A revelation from the Almighty, the Most Merciful,", "so that you may warn a people whose forefathers were not warned, and so they are heedless.", "The word has certainly come true for most of them, for they do not believe.")),
    67 to (BISMILLAH_T + "\n" + lines("Tabarakal-ladhi bi-yadihil-mulku wa huwa 'ala kulli shay'in qadir.", "Alladhi khalaqal-mawta wal-hayata li-yabluwakum ayyukum ahsanu 'amala, wa huwal-'azizul-ghafur.", "Alladhi khalaqa sab'a samawatin tibaqa, ma tara fi khalqir-rahmani min tafawut, farji'il-basara hal tara min futur.", "Thummar-ji'il-basara karratayni yanqalib ilaykal-basaru khasi'aw-wa huwa hasir.")
      to BISMILLAH_M + "\n" + lines("Blessed is He in whose hand is all sovereignty, and He has power over all things.", "He who created death and life to test which of you is best in deeds; He is the Almighty, the Most Forgiving.", "He who created seven heavens in layers; you will not see any flaw in the creation of the Most Merciful. So look again: do you see any rift?", "Then look again and again; your sight will return to you humbled and weary."))
  )
}

package akinai.minikgurme.util

/**
 * Gizlilik politikası, kullanım koşulları ve açık kaynak lisansları (TR + EN).
 * YAYIN ÖNCESİ: [CONTACT_EMAIL] doldurulmalı ve aynı metin bir web adresinde yayımlanmalı (Play Console için).
 * Bu metinler genel bir taslaktır; hukuki görüş değildir.
 */
object LegalTexts {
    /** Kullanıcıların ulaşacağı iletişim adresi — yayından önce doldurun. */
    const val CONTACT_EMAIL = ""
    const val UPDATED = "12.11.2026"

    private val contact get() = CONTACT_EMAIL.ifBlank { "[iletişim e-postası]" }
    private val contactEn get() = CONTACT_EMAIL.ifBlank { "[contact e-mail]" }

    fun privacy(lang: String): List<Pair<String, String>> = if (lang == "en") listOf(
        "Privacy Policy" to "Last updated: $UPDATED. This policy explains how the Minik Gurme app (“the app”) handles your information.",
        "Data stays on your phone" to "Everything you enter — your baby's name, age, sex, weight, height, meals, reactions, allergen trials and notes — is stored only on your device. The app has no account system, no servers, no analytics and no advertising. We (the developer) never receive this data.",
        "Health data" to "Weight, height, allergy and reaction records are health-related data. Because they never leave your device, they are not shared with us or any third party by the app.",
        "Backups and files" to "If Android's own backup is enabled on your phone, Google may include the app's data in your device backup under Google's privacy policy. When you use “Export data” or “Doctor report”, a file is created only where you choose and is shared only with whom you choose.",
        "Purchases" to "Minik Gurme Plus subscriptions are processed by Google Play. The app only receives whether a subscription is active; payment details are handled by Google under its own terms.",
        "Notifications" to "Reminders are scheduled locally on your device; no data is sent anywhere.",
        "Children" to "The app is intended for parents and caregivers (18+), not for use by children.",
        "Your rights" to "You can delete all data at any time from Settings (reset/clear) or by uninstalling the app. Under the Turkish Personal Data Protection Law (KVKK) and GDPR you may contact us with any request.",
        "Contact" to contactEn,
    ) else listOf(
        "Gizlilik Politikası" to "Son güncelleme: $UPDATED. Bu politika, Minik Gurme uygulamasının (“uygulama”) bilgilerinizi nasıl işlediğini açıklar.",
        "Veriler telefonunuzda kalır" to "Girdiğiniz her şey — bebeğinizin adı, yaşı, cinsiyeti, kilo ve boyu, öğünleri, tepkileri, alerjen denemeleri ve notlar — yalnızca cihazınızda saklanır. Uygulamada hesap sistemi, sunucu, analiz ve reklam yoktur. Geliştirici olarak bu verileri hiçbir zaman almayız.",
        "Sağlık verileri" to "Kilo, boy, alerji ve tepki kayıtları sağlıkla ilgili (özel nitelikli) verilerdir. Cihazınızdan çıkmadıkları için uygulama tarafından bize veya üçüncü kişilere aktarılmazlar.",
        "Yedekler ve dosyalar" to "Telefonunuzda Android yedeklemesi açıksa Google, uygulama verilerini kendi gizlilik politikası kapsamında cihaz yedeğinize ekleyebilir. “Verileri dışa aktar” veya “Doktor raporu” kullandığınızda dosya yalnızca sizin seçtiğiniz yere kaydedilir ve yalnızca sizin seçtiğiniz kişiyle paylaşılır.",
        "Satın almalar" to "Minik Gurme Plus abonelikleri Google Play tarafından işlenir. Uygulama yalnızca aboneliğin etkin olup olmadığını öğrenir; ödeme bilgileri Google tarafından kendi koşulları altında işlenir.",
        "Bildirimler" to "Hatırlatıcılar cihazınızda yerel olarak kurulur; hiçbir veri gönderilmez.",
        "Çocuklar" to "Uygulama ebeveynler ve bakım verenler (18 yaş üstü) içindir; çocukların kullanımına yönelik değildir.",
        "Haklarınız" to "Tüm verileri dilediğiniz zaman Ayarlar'dan (sıfırla/temizle) veya uygulamayı kaldırarak silebilirsiniz. 6698 sayılı KVKK ve GDPR kapsamındaki talepleriniz için bize ulaşabilirsiniz.",
        "İletişim" to contact,
    )

    fun terms(lang: String): List<Pair<String, String>> = if (lang == "en") listOf(
        "Terms of Use" to "Last updated: $UPDATED. By using the app you accept these terms.",
        "Not medical advice" to "The app provides general information on starting solid foods. It is not medical advice and does not diagnose, treat or prevent any condition. Always consult your paediatrician; in an emergency (difficulty breathing, swelling of the face or throat) call emergency services immediately.",
        "Your responsibility" to "You are responsible for the accuracy of the data you enter and for decisions about your baby's feeding. Food textures, portions and allergen introduction must be adapted to your baby with your doctor.",
        "Minik Gurme Plus" to "Plus is an auto-renewing subscription billed through Google Play. It renews at the end of each period unless cancelled at least 24 hours before. You can cancel anytime in Google Play › Subscriptions; access continues until the end of the paid period. Refunds follow Google Play's policies. Prices are shown in the app before purchase.",
        "Content" to "Recipes and guides are prepared from public sources (e.g. WHO, ESPGHAN) and may be updated. The app does not claim endorsement by any organisation.",
        "Liability" to "To the extent permitted by law, the developer is not liable for damages arising from the use of the app. These terms are governed by the laws of the Republic of Türkiye.",
        "Contact" to contactEn,
    ) else listOf(
        "Kullanım Koşulları" to "Son güncelleme: $UPDATED. Uygulamayı kullanarak bu koşulları kabul etmiş olursunuz.",
        "Tıbbi tavsiye değildir" to "Uygulama ek gıdaya geçiş hakkında genel bilgi sunar. Tıbbi tavsiye değildir; hiçbir durumu teşhis, tedavi veya önleme amacı taşımaz. Her zaman çocuk doktorunuza danışın; acil durumda (nefes darlığı, yüzde veya boğazda şişlik) hemen 112'yi arayın.",
        "Sorumluluğunuz" to "Girdiğiniz verilerin doğruluğundan ve bebeğinizin beslenmesine ilişkin kararlardan siz sorumlusunuz. Kıvam, porsiyon ve alerjen tanıtımı doktorunuzla birlikte bebeğinize göre uyarlanmalıdır.",
        "Minik Gurme Plus" to "Plus, Google Play üzerinden faturalanan, otomatik yenilenen bir aboneliktir. Dönem bitiminden en az 24 saat önce iptal edilmezse yenilenir. Google Play › Abonelikler'den istediğiniz zaman iptal edebilirsiniz; erişim ödenen dönemin sonuna kadar sürer. İadeler Google Play politikalarına tabidir. Fiyatlar satın almadan önce uygulamada gösterilir.",
        "İçerik" to "Tarif ve rehberler kamuya açık kaynaklardan (ör. WHO, ESPGHAN) yararlanılarak hazırlanmıştır ve güncellenebilir. Uygulama herhangi bir kuruluş tarafından onaylandığını iddia etmez.",
        "Sorumluluk" to "Yasaların izin verdiği ölçüde geliştirici, uygulamanın kullanımından doğan zararlardan sorumlu tutulamaz. Bu koşullar Türkiye Cumhuriyeti hukukuna tabidir.",
        "İletişim" to contact,
    )

    fun licenses(lang: String): List<Pair<String, String>> = listOf(
        "Plus Jakarta Sans" to "Copyright 2020 The Plus Jakarta Sans Project Authors. SIL Open Font License 1.1 — https://openfontlicense.org",
        "Fluent Emoji (3D)" to "Copyright (c) Microsoft Corporation. MIT License — https://github.com/microsoft/fluentui-emoji",
        "AndroidX, Jetpack Compose, Room" to "Copyright The Android Open Source Project. Apache License 2.0 — https://www.apache.org/licenses/LICENSE-2.0",
        "Kotlin, kotlinx.coroutines" to "Copyright JetBrains s.r.o. and contributors. Apache License 2.0",
        "Google Play Billing Library" to (if (lang == "en") "Used under the Android Software Development Kit License Agreement." else "Android Yazılım Geliştirme Kiti Lisans Sözleşmesi kapsamında kullanılır."),
        (if (lang == "en") "WHO Child Growth Standards" else "WHO Çocuk Büyüme Standartları") to (if (lang == "en") "Median values (weight-for-age, length-for-age) © World Health Organization; used for reference only, no endorsement implied." else "Medyan değerler (yaşa göre kilo ve boy) © Dünya Sağlık Örgütü; yalnızca karşılaştırma amacıyla kullanılır, onay anlamı taşımaz."),
    )
}

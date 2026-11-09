package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bebeklerin yediği besinleri, tarih/saat, porsiyon, kıvam,
 * iştah durumu ve gözlemlenen reaksiyonları saklayan Beslenme Günlüğü veri modeli.
 */
@Entity(tableName = "feeding_diary")
data class FeedingDiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD formatında tarih
    val time: String, // HH:mm formatında saat (örn: "09:30", "13:00")
    val mealType: String, // "Kahvaltı / Sabah", "Öğle Yemeği", "İkindi / Ara Öğün", "Akşam Yemeği", "Tadım Denemesi"
    val foodItems: String, // Yenen besinler (örn: "Avokado ezmesi, haşlanmış yumurta sarısı")
    val portionAmount: String = "1 Porsiyon", // Porsiyon miktarı (örn: "3 tatlı kaşığı", "Yarım kase", "BLW - 2 çubuk")
    val texture: String = "Çatalla Ezilmiş", // "Pürüzsüz Püre", "Çatalla Ezilmiş", "Pütürlü Ezme", "Parmak Gıda (BLW)"
    val appetite: String = "İştahla Bitirdi 😋", // "İştahla Bitirdi 😋", "Severek Yedi 😊", "Az Tadına Baktı 🤏", "Reddetti / İstemedi 🙅‍♂️"
    val reaction: String = "Sorunsuz / Keyifli ✨", // "Sorunsuz / Keyifli ✨", "Hafif Gaz / Huysuzluk 💨", "Kızarıklık / Döküntü ⚠️", "Reflü / Tükürme 🚨"
    val reactionNotes: String = "", // Varsa reaksiyon notu veya gözlem
    val notes: String = "", // Ebeveyn hazırlama notları, kullanılan zeytinyağı, pişirme şekli vb.
    val timestamp: Long = System.currentTimeMillis()
)

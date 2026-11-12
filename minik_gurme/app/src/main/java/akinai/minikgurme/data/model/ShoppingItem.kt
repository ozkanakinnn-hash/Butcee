package akinai.minikgurme.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Alışveriş listesi kalemi. [amount] tarifteki temel miktardır (Türkçe kaynak metin);
 * ekranda [factor] ile ölçeklenip seçili dile çevrilerek gösterilir.
 */
@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: String = "",
    val factor: Float = 1f,
    val emoji: String = "🛒",
    val source: String = "",
    val checked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

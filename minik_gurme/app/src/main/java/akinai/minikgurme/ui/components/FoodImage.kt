package akinai.minikgurme.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.R

/**
 * Emoji → 3D çizim (Microsoft Fluent Emoji, MIT lisansı; drawable-nodpi/emoji_*.webp).
 * Telefonun emoji yazı tipi yerine her cihazda aynı görünen görseller kullanılır.
 * Bu dosyadaki eşleme tool ile üretilir (README'ye bakın).
 */
private val emojiArt: Map<String, Int> = mapOf(
    "☀️" to R.drawable.emoji_2600,
    "⚠" to R.drawable.emoji_26a0,
    "🌈" to R.drawable.emoji_1f308,
    "🌙" to R.drawable.emoji_1f319,
    "🌤️" to R.drawable.emoji_1f324,
    "🌭" to R.drawable.emoji_1f32d,
    "🌰" to R.drawable.emoji_1f330,
    "🌱" to R.drawable.emoji_1f331,
    "🌾" to R.drawable.emoji_1f33e,
    "🌿" to R.drawable.emoji_1f33f,
    "🍅" to R.drawable.emoji_1f345,
    "🍆" to R.drawable.emoji_1f346,
    "🍊" to R.drawable.emoji_1f34a,
    "🍋" to R.drawable.emoji_1f34b,
    "🍌" to R.drawable.emoji_1f34c,
    "🍎" to R.drawable.emoji_1f34e,
    "🍏" to R.drawable.emoji_1f34f,
    "🍐" to R.drawable.emoji_1f350,
    "🍑" to R.drawable.emoji_1f351,
    "🍓" to R.drawable.emoji_1f353,
    "🍗" to R.drawable.emoji_1f357,
    "🍝" to R.drawable.emoji_1f35d,
    "🍞" to R.drawable.emoji_1f35e,
    "🍠" to R.drawable.emoji_1f360,
    "🍬" to R.drawable.emoji_1f36c,
    "🍯" to R.drawable.emoji_1f36f,
    "🍲" to R.drawable.emoji_1f372,
    "🍳" to R.drawable.emoji_1f373,
    "🍼" to R.drawable.emoji_1f37c,
    "🍽️" to R.drawable.emoji_1f37d,
    "🎃" to R.drawable.emoji_1f383,
    "🎯" to R.drawable.emoji_1f3af,
    "🐟" to R.drawable.emoji_1f41f,
    "💡" to R.drawable.emoji_1f4a1,
    "💧" to R.drawable.emoji_1f4a7,
    "📊" to R.drawable.emoji_1f4ca,
    "📏" to R.drawable.emoji_1f4cf,
    "📚" to R.drawable.emoji_1f4da,
    "🔍" to R.drawable.emoji_1f50d,
    "🔪" to R.drawable.emoji_1f52a,
    "🛒" to R.drawable.emoji_1f6d2,
    "🛡️" to R.drawable.emoji_1f6e1,
    "🟢" to R.drawable.emoji_1f7e2,
    "🥄" to R.drawable.emoji_1f944,
    "🥑" to R.drawable.emoji_1f951,
    "🥒" to R.drawable.emoji_1f952,
    "🥔" to R.drawable.emoji_1f954,
    "🥕" to R.drawable.emoji_1f955,
    "🥗" to R.drawable.emoji_1f957,
    "🥚" to R.drawable.emoji_1f95a,
    "🥛" to R.drawable.emoji_1f95b,
    "🥜" to R.drawable.emoji_1f95c,
    "🥝" to R.drawable.emoji_1f95d,
    "🥞" to R.drawable.emoji_1f95e,
    "🥣" to R.drawable.emoji_1f963,
    "🥦" to R.drawable.emoji_1f966,
    "🥩" to R.drawable.emoji_1f969,
    "🥬" to R.drawable.emoji_1f96c,
    "🥯" to R.drawable.emoji_1f96f,
    "🧀" to R.drawable.emoji_1f9c0,
    "🧂" to R.drawable.emoji_1f9c2,
    "🧆" to R.drawable.emoji_1f9c6,
    "🧪" to R.drawable.emoji_1f9ea,
    "🧺" to R.drawable.emoji_1f9fa,
    "🩺" to R.drawable.emoji_1fa7a,
    "🪵" to R.drawable.emoji_1fab5,
    "🫐" to R.drawable.emoji_1fad0,
    "🫑" to R.drawable.emoji_1fad1,
    "🫒" to R.drawable.emoji_1fad2,
    "🫖" to R.drawable.emoji_1fad6,
    "🫘" to R.drawable.emoji_1fad8,
)

/** Emojinin 3D çizimi; eşleşme yoksa null. */
fun emojiArtFor(emoji: String): Int? = emojiArt[emoji] ?: emojiArt[emoji.replace("\uFE0F", "")]

/** Besin/tarif görseli: emojinin 3D çizimi, yoksa emojinin kendisi. */
@Composable
fun FoodImage(emoji: String, size: Dp, modifier: Modifier = Modifier) {
    val res = emojiArtFor(emoji)
    if (res != null) {
        Image(painter = painterResource(res), contentDescription = null, modifier = modifier.size(size))
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = (size.value * 0.72f).sp)
        }
    }
}

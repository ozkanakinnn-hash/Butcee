package akinai.minikgurme.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import akinai.minikgurme.resources.Res
import akinai.minikgurme.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Emoji → 3D çizim (Microsoft Fluent Emoji, MIT lisansı; drawable-nodpi/emoji_*.webp).
 * İstisnalar: 🥒 (emoji_1f952) sakız kabağı ve emoji_cucumber salatalık görselleri kullanıcı tarafından sağlandı.
 * food_*.webp: aynı emojiyi paylaşan besinler için aynı çizimin besin rengine göre yeniden renklendirilmiş hâli
 * (hindi: Fluent "Turkey", tarçın: Fluent "Wood"; ikisi de MIT).
 * Telefonun emoji yazı tipi yerine her cihazda aynı görünen görseller kullanılır.
 * Bu dosyadaki eşleme tool ile üretilir (README'ye bakın).
 */
private val emojiArt: Map<String, DrawableResource> = mapOf(
    // Aynı emojiyi paylaşan besinler: emoji + görünmez varyasyon seçici (U+FE00…) → yeniden renklendirilmiş çizim (food_*.webp)
    "🍠\uFE00" to Res.drawable.food_pancar,
    "🥦\uFE00" to Res.drawable.food_karnabahar,
    "🥬\uFE00" to Res.drawable.food_pirasa,
    "🥬\uFE01" to Res.drawable.food_kereviz,
    "🥬\uFE02" to Res.drawable.food_lahana,
    "🥬\uFE03" to Res.drawable.food_bruksel,
    "🥬\uFE04" to Res.drawable.food_pazi,
    "🍐\uFE00" to Res.drawable.food_ayva,
    "🍑\uFE00" to Res.drawable.food_kayisi,
    "🍑\uFE01" to Res.drawable.food_erik,
    "🫐\uFE00" to Res.drawable.food_bogurtlen,
    "🥩\uFE00" to Res.drawable.food_dana_eti,
    "🐟\uFE00" to Res.drawable.food_somon,
    "🐟\uFE01" to Res.drawable.food_levrek,
    "🐟\uFE02" to Res.drawable.food_cipura,
    "🐟\uFE03" to Res.drawable.food_mezgit,
    "🍗\uFE00" to Res.drawable.food_hindi,
    "🥣\uFE00" to Res.drawable.food_yogurt,
    "🧀\uFE00" to Res.drawable.food_bebek_loru,
    "🧀\uFE01" to Res.drawable.food_beyaz_peynir,
    "🌰\uFE00" to Res.drawable.food_ceviz,
    "🌰\uFE01" to Res.drawable.food_badem,
    "🌰\uFE02" to Res.drawable.food_kaju,
    "🌰\uFE03" to Res.drawable.food_kabak_cekirdegi,
    "🫛\uFE00" to Res.drawable.food_bamya,
    "🌱\uFE00" to Res.drawable.food_chia,
    "🌱\uFE01" to Res.drawable.food_keten,
    "🍓\uFE00" to Res.drawable.food_ahududu,
    "🍊\uFE00" to Res.drawable.food_portakal,
    "🍚\uFE00" to Res.drawable.food_kuskus,
    "🌾\uFE00" to Res.drawable.food_irmik,
    "🌾\uFE01" to Res.drawable.food_kinoa,
    "🌾\uFE02" to Res.drawable.food_karabugday,
    "🌾\uFE03" to Res.drawable.food_bulgur,
    "🫘\uFE00" to Res.drawable.food_kuru_fasulye,
    "🫘\uFE01" to Res.drawable.food_yesil_mercimek,
    "🍖\uFE00" to Res.drawable.food_ciger,
    "🧈\uFE00" to Res.drawable.food_tofu,
    "🥛\uFE00" to Res.drawable.food_keci_yogurdu,
    "🥛\uFE01" to Res.drawable.food_suzme_yogurt,
    "🌿\uFE00" to Res.drawable.food_dereotu,
    "🌿\uFE01" to Res.drawable.food_nane,
    "🌿\uFE02" to Res.drawable.food_tarcin,
    // Salatalık: 🥒 + metin seçici (U+FE0E) — 🥒 tek başına sakız kabağı çizimini gösterir
    "\uD83E\uDD52\uFE0E" to Res.drawable.emoji_cucumber,
    "🫛" to Res.drawable.emoji_1fadb,
    "🍄" to Res.drawable.emoji_1f344,
    "🌽" to Res.drawable.emoji_1f33d,
    "🧅" to Res.drawable.emoji_1f9c5,
    "🧄" to Res.drawable.emoji_1f9c4,
    "🍒" to Res.drawable.emoji_1f352,
    "🍇" to Res.drawable.emoji_1f347,
    "🍉" to Res.drawable.emoji_1f349,
    "🍈" to Res.drawable.emoji_1f348,
    "🥭" to Res.drawable.emoji_1f96d,
    "🍍" to Res.drawable.emoji_1f34d,
    "🍚" to Res.drawable.emoji_1f35a,
    "🍖" to Res.drawable.emoji_1f356,
    "🧈" to Res.drawable.emoji_1f9c8,
    "🦐" to Res.drawable.emoji_1f990,
    "☀️" to Res.drawable.emoji_2600,
    "⚠" to Res.drawable.emoji_26a0,
    "🌈" to Res.drawable.emoji_1f308,
    "🌙" to Res.drawable.emoji_1f319,
    "🌤️" to Res.drawable.emoji_1f324,
    "🌭" to Res.drawable.emoji_1f32d,
    "🌰" to Res.drawable.emoji_1f330,
    "🌱" to Res.drawable.emoji_1f331,
    "🌾" to Res.drawable.emoji_1f33e,
    "🌿" to Res.drawable.emoji_1f33f,
    "🍅" to Res.drawable.emoji_1f345,
    "🍆" to Res.drawable.emoji_1f346,
    "🍊" to Res.drawable.emoji_1f34a,
    "🍋" to Res.drawable.emoji_1f34b,
    "🍌" to Res.drawable.emoji_1f34c,
    "🍎" to Res.drawable.emoji_1f34e,
    "🍏" to Res.drawable.emoji_1f34f,
    "🍐" to Res.drawable.emoji_1f350,
    "🍑" to Res.drawable.emoji_1f351,
    "🍓" to Res.drawable.emoji_1f353,
    "🍗" to Res.drawable.emoji_1f357,
    "🍝" to Res.drawable.emoji_1f35d,
    "🍞" to Res.drawable.emoji_1f35e,
    "🍠" to Res.drawable.emoji_1f360,
    "🍬" to Res.drawable.emoji_1f36c,
    "🍯" to Res.drawable.emoji_1f36f,
    "🍲" to Res.drawable.emoji_1f372,
    "🍳" to Res.drawable.emoji_1f373,
    "🍼" to Res.drawable.emoji_1f37c,
    "🍽️" to Res.drawable.emoji_1f37d,
    "🎃" to Res.drawable.emoji_1f383,
    "🎯" to Res.drawable.emoji_1f3af,
    "🐟" to Res.drawable.emoji_1f41f,
    "💡" to Res.drawable.emoji_1f4a1,
    "💧" to Res.drawable.emoji_1f4a7,
    "📊" to Res.drawable.emoji_1f4ca,
    "📏" to Res.drawable.emoji_1f4cf,
    "📚" to Res.drawable.emoji_1f4da,
    "🔍" to Res.drawable.emoji_1f50d,
    "🔪" to Res.drawable.emoji_1f52a,
    "🛒" to Res.drawable.emoji_1f6d2,
    "🛡️" to Res.drawable.emoji_1f6e1,
    "🟢" to Res.drawable.emoji_1f7e2,
    "🥄" to Res.drawable.emoji_1f944,
    "🥑" to Res.drawable.emoji_1f951,
    "🥒" to Res.drawable.emoji_1f952,
    "🥔" to Res.drawable.emoji_1f954,
    "🥕" to Res.drawable.emoji_1f955,
    "🥗" to Res.drawable.emoji_1f957,
    "🥚" to Res.drawable.emoji_1f95a,
    "🥛" to Res.drawable.emoji_1f95b,
    "🥜" to Res.drawable.emoji_1f95c,
    "🥝" to Res.drawable.emoji_1f95d,
    "🥞" to Res.drawable.emoji_1f95e,
    "🥣" to Res.drawable.emoji_1f963,
    "🥦" to Res.drawable.emoji_1f966,
    "🥩" to Res.drawable.emoji_1f969,
    "🥬" to Res.drawable.emoji_1f96c,
    "🥯" to Res.drawable.emoji_1f96f,
    "🧀" to Res.drawable.emoji_1f9c0,
    "🧂" to Res.drawable.emoji_1f9c2,
    "🧆" to Res.drawable.emoji_1f9c6,
    "🧪" to Res.drawable.emoji_1f9ea,
    "🧺" to Res.drawable.emoji_1f9fa,
    "🩺" to Res.drawable.emoji_1fa7a,
    "🪵" to Res.drawable.emoji_1fab5,
    "🫐" to Res.drawable.emoji_1fad0,
    "🫑" to Res.drawable.emoji_1fad1,
    "🫒" to Res.drawable.emoji_1fad2,
    "🫖" to Res.drawable.emoji_1fad6,
    "🫘" to Res.drawable.emoji_1fad8,
)

/** Emojinin 3D çizimi; eşleşme yoksa null. */
fun emojiArtFor(emoji: String): DrawableResource? = emojiArt[emoji] ?: emojiArt[emoji.replace("\uFE0F", "")]
    ?: emojiArt[emoji.replace(Regex("[\uFE00-\uFE0D]"), "")]

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

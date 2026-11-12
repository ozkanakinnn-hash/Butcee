package akinai.minikgurme.ui.platform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import akinai.minikgurme.i18n.Text
import kotlinx.coroutines.delay

/** Android'deki Toast'ın ortak karşılığı (iOS'ta kullanılır): ekranın altında kısa süre görünen mesaj. */
object InAppMessage {
    var current by mutableStateOf<String?>(null)
        private set
    private var serial = 0
    var shownSerial by mutableStateOf(0)
        private set

    fun show(message: String) { current = message; shownSerial = ++serial }
}

@Composable
fun InAppMessageHost() {
    val msg = InAppMessage.current
    val serial = InAppMessage.shownSerial
    var visible by androidx.compose.runtime.remember { mutableStateOf(false) }
    LaunchedEffect(serial) {
        if (msg == null) return@LaunchedEffect
        visible = true; delay(3000); visible = false
    }
    Box(Modifier.fillMaxSize().navigationBarsPadding().padding(bottom = 96.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible && msg != null, enter = fadeIn(), exit = fadeOut()) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.inverseSurface, shadowElevation = 6.dp) {
                Text(msg ?: "", color = MaterialTheme.colorScheme.inverseOnSurface,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
            }
        }
    }
}

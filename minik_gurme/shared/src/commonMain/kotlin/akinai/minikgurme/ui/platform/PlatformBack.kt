package akinai.minikgurme.ui.platform

import androidx.compose.runtime.Composable

/** Sistem geri hareketi (Android geri tuşu/hareketi; iOS'ta ekranlardaki geri okları kullanılır). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/** UI testlerinde testTag'leri kimlik olarak gösterir (Android UiAutomator); iOS'ta etkisiz. */
expect fun androidx.compose.ui.Modifier.testTagsAsIds(): androidx.compose.ui.Modifier

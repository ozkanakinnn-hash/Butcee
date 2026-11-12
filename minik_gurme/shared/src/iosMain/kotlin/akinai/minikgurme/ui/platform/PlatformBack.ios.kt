package akinai.minikgurme.ui.platform

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

actual fun androidx.compose.ui.Modifier.testTagsAsIds(): androidx.compose.ui.Modifier = this

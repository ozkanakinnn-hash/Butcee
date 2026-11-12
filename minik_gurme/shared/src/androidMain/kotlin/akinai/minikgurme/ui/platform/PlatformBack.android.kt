package akinai.minikgurme.ui.platform

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
actual fun androidx.compose.ui.Modifier.testTagsAsIds(): androidx.compose.ui.Modifier =
    this.semantics { testTagsAsResourceId = true }

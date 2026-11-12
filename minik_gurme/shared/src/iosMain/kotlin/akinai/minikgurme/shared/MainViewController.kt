package akinai.minikgurme.shared

import androidx.compose.ui.window.ComposeUIViewController

/** iPhone uygulamasının giriş noktası (Swift tarafı bunu çağırır). */
fun MainViewController() = ComposeUIViewController { SharedHello() }

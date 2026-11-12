package akinai.minikgurme.shared

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual val platformName: String = "ios"

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

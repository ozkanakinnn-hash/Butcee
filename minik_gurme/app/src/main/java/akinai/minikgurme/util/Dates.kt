package akinai.minikgurme.util

import akinai.minikgurme.i18n.AppLanguage
import java.text.SimpleDateFormat
import java.util.Locale

/** "2026-11-09" → "9 Kas 2026" / "9 Nov 2026". Ayrıştırılamazsa metni aynen döndürür. */
fun prettyDate(iso: String): String = try {
    val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(iso.trim())
    if (d == null) iso else SimpleDateFormat("d MMM yyyy", Locale(AppLanguage.current)).format(d)
} catch (_: Exception) { iso }

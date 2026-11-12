package akinai.minikgurme.shared

import platform.Foundation.NSUserDefaults

/** Her depo ayrı bir NSUserDefaults "suite"i (ör. akinai.minikgurme.app_prefs). */
actual class KeyValueStore actual constructor(name: String) {
    private val d = NSUserDefaults(suiteName = "akinai.minikgurme.$name")

    actual fun getString(key: String, default: String?): String? = d.stringForKey(key) ?: default
    actual fun putString(key: String, value: String?) { if (value == null) d.removeObjectForKey(key) else d.setObject(value, key) }
    actual fun getBoolean(key: String, default: Boolean): Boolean = if (contains(key)) d.boolForKey(key) else default
    actual fun putBoolean(key: String, value: Boolean) { d.setBool(value, key) }
    actual fun getLong(key: String, default: Long): Long = if (contains(key)) d.integerForKey(key) else default
    actual fun putLong(key: String, value: Long) { d.setInteger(value, key) }
    actual fun getInt(key: String, default: Int): Int = getLong(key, default.toLong()).toInt()
    actual fun putInt(key: String, value: Int) { putLong(key, value.toLong()) }
    actual fun getStringSet(key: String): Set<String> =
        (d.arrayForKey(key) ?: emptyList<Any?>()).filterIsInstance<String>().toSet()
    actual fun putStringSet(key: String, value: Set<String>) { d.setObject(value.toList(), key) }
    actual fun contains(key: String): Boolean = d.objectForKey(key) != null
    actual fun remove(key: String) { d.removeObjectForKey(key) }
    actual fun clear() { d.dictionaryRepresentation().keys.forEach { d.removeObjectForKey(it as String) } }
}

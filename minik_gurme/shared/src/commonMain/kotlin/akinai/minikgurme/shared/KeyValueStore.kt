package akinai.minikgurme.shared

/**
 * Basit kalıcı ayar deposu (Android: SharedPreferences, iOS: NSUserDefaults).
 * Android'de [name] eski SharedPreferences dosya adıyla aynıdır; güncellemede ayarlar korunur.
 */
expect class KeyValueStore(name: String) {
    fun getString(key: String, default: String? = null): String?
    fun putString(key: String, value: String?)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getLong(key: String, default: Long = 0L): Long
    fun putLong(key: String, value: Long)
    fun getInt(key: String, default: Int = 0): Int
    fun putInt(key: String, value: Int)
    fun getStringSet(key: String): Set<String>
    fun putStringSet(key: String, value: Set<String>)
    fun contains(key: String): Boolean
    fun remove(key: String)
    fun clear()
}

package akinai.minikgurme.reminders

import akinai.minikgurme.shared.KeyValueStore

/**
 * Hatırlatıcı ayarları (ortak). Bildirimleri kurmak platforma özeldir:
 * Android → AlarmManager, iOS → UNUserNotificationCenter. Platform [scheduler]'ı kurar.
 */
object Reminders {
    const val KEY_ALLERGEN = "reminder_allergen"
    const val KEY_MEALS = "reminder_meals"

    fun interface Scheduler { fun reschedule() }

    var scheduler: Scheduler = Scheduler { }

    private val prefs by lazy { KeyValueStore("app_prefs") }

    fun allergenOn() = prefs.getBoolean(KEY_ALLERGEN, false)
    fun mealsOn() = prefs.getBoolean(KEY_MEALS, false)
    fun setAllergen(on: Boolean) { prefs.putBoolean(KEY_ALLERGEN, on); scheduler.reschedule() }
    fun setMeals(on: Boolean) { prefs.putBoolean(KEY_MEALS, on); scheduler.reschedule() }
}

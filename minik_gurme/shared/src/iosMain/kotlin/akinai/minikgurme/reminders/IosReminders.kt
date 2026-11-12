package akinai.minikgurme.reminders

import akinai.minikgurme.billing.PlusManager
import akinai.minikgurme.i18n.t
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS yerel hatırlatıcıları (internet gerekmez): her gün tekrarlanan takvim bildirimleri.
 *  - Alerjen kontrolü (ücretsiz): her akşam 19:00
 *  - Öğün hatırlatıcıları (Plus): 08:30, 12:30, 18:00
 * Android'dekiyle aynı saatler ve metinler.
 */
object IosReminders {
    private data class Slot(val id: String, val hour: Long, val minute: Long, val title: String, val body: String)

    private fun allergen() = Slot("allergen", 19, 0, t("3 gün kuralı: belirtileri kontrol edin"),
        t("Devam eden bir alerjen denemeniz varsa kızarıklık, kusma veya ishal oldu mu kontrol edin ve günü kaydedin."))
    private fun meals() = listOf(
        Slot("meal_breakfast", 8, 30, t("Kahvaltı zamanı"), t("Bugünün kahvaltısını kaydetmeyi unutmayın.")),
        Slot("meal_lunch", 12, 30, t("Öğle yemeği zamanı"), t("Öğle öğününü kaydetmeyi unutmayın.")),
        Slot("meal_dinner", 18, 0, t("Akşam yemeği zamanı"), t("Akşam öğününü kaydetmeyi unutmayın."))
    )
    private val allIds = listOf("allergen", "meal_breakfast", "meal_lunch", "meal_dinner")

    /** Ortak [Reminders] ayarı değişince bildirimleri yeniden kurar. */
    fun install() {
        Reminders.scheduler = Reminders.Scheduler { reschedule() }
    }

    fun reschedule() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(allIds)
        val wanted = buildList {
            if (Reminders.allergenOn()) add(allergen())
            if (Reminders.mealsOn() && PlusManager.isPlus) addAll(meals())
        }
        wanted.forEach { s ->
            val content = UNMutableNotificationContent().apply {
                setTitle(s.title); setBody(s.body); setSound(UNNotificationSound.defaultSound)
            }
            val time = NSDateComponents().apply { hour = s.hour; minute = s.minute }
            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(time, repeats = true)
            center.addNotificationRequest(UNNotificationRequest.requestWithIdentifier(s.id, content, trigger), null)
        }
    }
}

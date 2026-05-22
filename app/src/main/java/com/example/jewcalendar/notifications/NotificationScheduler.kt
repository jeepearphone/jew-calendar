package com.example.jewcalendar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.EventsProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"
    private const val PREFS = "notification_scheduler"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
    private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

    fun scheduleAll(context: Context, lat: Double, lon: Double) {
        saveLocation(context, lat, lon)
        scheduleShabbatNotifications(context, lat, lon)
        scheduleHolidayNotifications(context, lat, lon)
    }

    fun rescheduleFromSavedLocation(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LAT) || !prefs.contains(KEY_LON)) return

        scheduleAll(
            context = context,
            lat = prefs.getFloat(KEY_LAT, 31.7683f).toDouble(),
            lon = prefs.getFloat(KEY_LON, 35.2137f).toDouble()
        )
    }

    private fun saveLocation(context: Context, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LON, lon.toFloat())
            .apply()
    }

    private fun scheduleShabbatNotifications(context: Context, lat: Double, lon: Double) {
        val now = System.currentTimeMillis()
        var friday = LocalDate.now()

        while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
            friday = friday.plusDays(1)
        }

        var sunsetMillis = sunsetMillis(lat, lon, friday) ?: return
        if (sunsetMillis <= now) {
            friday = friday.plusWeeks(1)
            sunsetMillis = sunsetMillis(lat, lon, friday) ?: return
        }

        scheduleNotification(
            context = context,
            triggerMillis = sunsetMillis - MILLIS_IN_DAY,
            type = "shabbat_soon",
            message = "Завтра начинается Шаббат",
            id = stableId("shabbat_soon", friday)
        )
        scheduleNotification(
            context = context,
            triggerMillis = sunsetMillis,
            type = "shabbat_start",
            message = "Шаббат Шалом! Шаббат начался",
            id = stableId("shabbat_start", friday)
        )
    }

    private fun scheduleHolidayNotifications(context: Context, lat: Double, lon: Double) {
        val today = LocalDate.now()
        val scheduledEventIds = mutableSetOf<String>()

        for (i in 0..90) {
            val holidayDate = today.plusDays(i.toLong())
            val jc = Calendar.jewishCalendarFromLocalDate(holidayDate)
            val jewishEvent = EventsProvider.getJewishEventsForDay(jc, holidayDate)
            val gregorianEvent = EventsProvider.getGregorianEventForDay(holidayDate)

            jewishEvent?.let { event ->
                if (scheduledEventIds.add(event.id)) {
                    scheduleJewishHoliday(context, lat, lon, holidayDate, event.id, event.nameRu)
                }
            }

            gregorianEvent?.let { event ->
                val triggerMillis = holidayDate
                    .atTime(9, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                scheduleNotification(
                    context = context,
                    triggerMillis = triggerMillis - MILLIS_IN_DAY,
                    type = "holiday_soon",
                    message = "Завтра: ${event.nameRu}",
                    id = stableId("gregorian_holiday_soon_${event.id}", holidayDate)
                )
                scheduleNotification(
                    context = context,
                    triggerMillis = triggerMillis,
                    type = "holiday_start",
                    message = "Сегодня: ${event.nameRu}",
                    id = stableId("gregorian_holiday_start_${event.id}", holidayDate)
                )
            }
        }
    }

    private fun scheduleJewishHoliday(
        context: Context,
        lat: Double,
        lon: Double,
        holidayDate: LocalDate,
        eventId: String,
        eventName: String
    ) {
        val startDate = holidayDate.minusDays(1)
        val startMillis = sunsetMillis(lat, lon, startDate) ?: return

        scheduleNotification(
            context = context,
            triggerMillis = startMillis - MILLIS_IN_DAY,
            type = "holiday_soon",
            message = "Завтра вечером начинается $eventName",
            id = stableId("holiday_soon_$eventId", holidayDate)
        )
        scheduleNotification(
            context = context,
            triggerMillis = startMillis,
            type = "holiday_start",
            message = "$eventName начался",
            id = stableId("holiday_start_$eventId", holidayDate)
        )
    }

    private fun sunsetMillis(lat: Double, lon: Double, date: LocalDate): Long? {
        return Calendar.getSunset(lat, lon, date)
            ?.let { sunset ->
                date.atTime(sunset)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
    }

    private fun scheduleNotification(
        context: Context,
        triggerMillis: Long,
        type: String,
        message: String,
        id: Int
    ) {
        if (triggerMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ShabbatNotificationReceiver::class.java).apply {
            putExtra("notification_type", type)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun stableId(type: String, date: LocalDate): Int {
        return 10_000 + kotlin.math.abs("$type-$date".hashCode() % 80_000)
    }
}

package com.example.jewcalendar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.EventsProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

object NotificationScheduler {

    fun scheduleAll(context: Context, lat: Double, lon: Double) {
        scheduleShabbatNotifications(context, lat, lon)
        scheduleHolidayNotifications(context)
    }

    private fun scheduleShabbatNotifications(context: Context, lat: Double, lon: Double) {
        var friday = LocalDate.now()
        while (friday.dayOfWeek != DayOfWeek.FRIDAY) {
            friday = friday.plusDays(1)
        }

        val sunset = Calendar.getSunset(lat, lon, friday) ?: return
        val sunsetMillis = friday
            .atTime(sunset)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        scheduleNotification(
            context = context,
            triggerMillis = sunsetMillis - 24 * 60 * 60 * 1000L,
            type = "shabbat_soon",
            message = "Завтра начинается Шаббат",
            id = 1001
        )
        scheduleNotification(
            context = context,
            triggerMillis = sunsetMillis,
            type = "shabbat_start",
            message = "Шаббат Шалом! Шаббат начался",
            id = 1002
        )
    }

    private fun scheduleHolidayNotifications(context: Context) {
        val today = LocalDate.now()
        for (i in 1..30) {
            val date = today.plusDays(i.toLong())
            val jc = Calendar.jewishCalendarFromLocalDate(date)
            val event = EventsProvider.getJewishEventsForDay(jc, date) ?: continue

            val triggerMillis = date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            scheduleNotification(
                context = context,
                triggerMillis = triggerMillis - 24 * 60 * 60 * 1000L,
                type = "holiday",
                message = "Завтра: ${event.nameRu} — ${event.shortDesc}",
                id = (2000 + i)
            )
        }
    }

    private fun scheduleNotification(
        context: Context,
        triggerMillis: Long,
        type: String,
        message: String,
        id: Int
    ) {
        if (triggerMillis < System.currentTimeMillis()) return

        val intent = Intent(context, ShabbatNotificationReceiver::class.java).apply {
            putExtra("notification_type", type)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }
}
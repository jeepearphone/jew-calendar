package com.example.jewcalendar.data

import com.example.jewcalendar.data.Calendar.jewishCalendarFromLocalDate
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.time.LocalDate


object EventsProvider {



    fun getJewishEventsForDay(jc: JewishCalendar, date: LocalDate): JewishEventsInfo? {
        var result : JewishEventsInfo? = null
        val currentDate = jewishCalendarFromLocalDate(date)
        HOLIDAYS_BY_HEBREW_DAY.forEach { (dateThis, event) ->
            if (dateThis == currentDate) {
                result = event
            }
        }

        return result
    }
    fun getById(id: String) = HOLIDAYS_BY_HEBREW_DAY.values.firstOrNull { it.id == id }
    fun getAll() = HOLIDAYS_BY_HEBREW_DAY.values.toList()
    val HOLIDAYS_BY_HEBREW_DAY: Map<Date, JewishEventsInfo> = mapOf(
        Date(
            hebrewDayOfMonth = 1,
            hebrewMonthName = "Тишрей"
        ) to JewishEventsInfo(
            id = "rosh_hashana_1",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Рош а-Шана",
            nameHebrew = "ראש השנה",
            shortDesc = "Еврейский Новый год",
            fullDesc = "fullDesc",
            prohibitions = emptyList(),
            durationDays = 2,
        ),
        Date(
            hebrewDayOfMonth = 2,
            hebrewMonthName = "Тишрей"
        ) to JewishEventsInfo(
            id = "rosh_hashana_1",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Рош а-Шана",
            nameHebrew = "ראש השנה",
            shortDesc = "Еврейский Новый год",
            fullDesc = "fullDesc",
            prohibitions = emptyList(),
            durationDays = 2,
        ),
    )


}
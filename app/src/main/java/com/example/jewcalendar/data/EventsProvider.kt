package com.example.jewcalendar.data

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar


object EventsProvider {




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
package com.example.jewcalendar.data


import androidx.compose.runtime.*
import com.example.jewcalendar.data.Calendar.daysInHebrewMonth
import com.example.jewcalendar.data.Calendar.getHebrewYearType
import com.example.jewcalendar.data.Calendar.jewishCalendarFromLocalDate
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.time.LocalDate


object EventsProvider {


    fun getById(id: String) = HOLIDAYS_BY_HEBREW_DAY.values.firstOrNull { it.id == id }
    fun getAll() = HOLIDAYS_BY_HEBREW_DAY.values.toList()
    fun getJewishEventsForDay(jc: JewishCalendar, date: LocalDate): JewishEventsInfo? {
        val hebrewDate = jc.jewishDayOfMonth
        val hebrewMonth = Calendar.getHebrewMonthName(jc.jewishMonth, Calendar.isHebrewLeapYear(jc.jewishYear))
        if (isHanukkah(jc, hebrewDate, hebrewMonth)) return Hanukah
        if (isPurim(jc, hebrewDate, hebrewMonth)) return Purim
        return HOLIDAYS_BY_HEBREW_DAY[Date(hebrewDate, hebrewMonth)]
    }

    val Hanukah = JewishEventsInfo(
        id = "hanukah",
        type = EventType.RELIGIOUS_CULTURAL,
        nameRu = "Ханука",
        nameHebrew = "חנוכה",
        shortDesc = "Праздник огней",
        fullDesc = "",
        durationDays = 8
    )
    val Purim = JewishEventsInfo(
        id = "purim",
        type = EventType.RELIGIOUS_CULTURAL,
        nameRu = "Пурим",
        nameHebrew = "פורים",
        shortDesc = "Праздник спасения еврейского народа",
        fullDesc = "",
        durationDays = 1
    )
    val yomHaZikaron = JewishEventsInfo(
        id = "yom_hazikaron",
        type = EventType.HISTORICAL,
        nameRu = "Йом а-Зикарон",
        nameHebrew = "יום הזיכרון",
        shortDesc = "День памяти павших воинов",
        fullDesc = "",
        durationDays = 1
    )
    val yomHaAtzmaut = JewishEventsInfo(
        id = "yom_haatzmaut",
        type = EventType.HISTORICAL,
        nameRu = "Йом а-Ацмаут",
        nameHebrew = "יום העצמאות",
        shortDesc = "День независимости Израиля",
        fullDesc = "",
        durationDays = 1
    )
    private fun isPurim(jc: JewishCalendar, day: Int, month: String): Boolean {
        val leap = Calendar.isHebrewLeapYear(jc.jewishYear)

        return if (leap) {
            day == 14 && month == "Адар бет"
        } else {
            day == 14 && month == "Адар"
        }
    }
    private fun isHanukkah(jc: JewishCalendar, day: Int, month: String): Boolean {
        if (month == "Кислев" && day >= 25) return true
        if (month == "Тевет") {
            val kislevDays = daysInHebrewMonth(jc.jewishYear, 9)
            return if (kislevDays == 29) {
                day in 1..3
            }
            else {
                day in 1..2
            }
        }
        return false
    }
    private fun getIsraeliIndependenceDays(
        jc: JewishCalendar,
        day: Int,
        month: String
    ): JewishEventsInfo? {
        if (month != "Ияр") return null
        val year = jc.jewishYear
        val iyar5 = JewishCalendar(year, JewishCalendar.IYAR, 5)
        val dayOfWeek = iyar5.dayOfWeek

        val atzmautDay = when (dayOfWeek) {
            6 -> 3
            7 -> 3
            2 -> 6
            else -> 5
        }

        val zikaronDay = atzmautDay - 1

        return when (day) {
            zikaronDay -> yomHaZikaron
            atzmautDay -> yomHaAtzmaut
            else -> null
        }
    }


    private fun multiDay(
        startDay: Int,
        monthName: String,
        info: JewishEventsInfo
    ) : List<Pair<Date, JewishEventsInfo>> =
        (0 until info.durationDays).map {
            offset ->
            Date(startDay + offset, monthName) to info
        }



    val HOLIDAYS_BY_HEBREW_DAY: Map<Date, JewishEventsInfo> = buildMap {

        val Rosh_Ha_Shana = JewishEventsInfo(
            id = "rosh_hashana",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Рош ха-Шана",
            nameHebrew = "ראש השנה",
            shortDesc = "Еврейский Новый год",
            fullDesc = "fullDesc",
            prohibitions = emptyList(),
            durationDays = 2,
        )
        multiDay(1, "Тишрей", Rosh_Ha_Shana).forEach {
            put(it.first, it.second)
        }

        put(
            Date(3, "Тишрей"), JewishEventsInfo(
                id = "tzom_gedaliah",
                type = EventType.RELIGIOUS_CULTURAL,
                nameRu = "Пост Гедалии",
                nameHebrew = "צום גדליה",
                shortDesc = "Пост в память о Гедалии бен Ахикаме",
                fullDesc = "",
                durationDays = 1
            )
        )

        put(
            Date(10, "Тишрей"), JewishEventsInfo(
                id = "yom_kippur",
                type = EventType.RELIGIOUS_CULTURAL,
                nameRu = "Йом Кипур",
                nameHebrew = "יום כיפור",
                shortDesc = "День Искупления",
                fullDesc = "",
                durationDays = 1
            )
        )

        val Sukkot = JewishEventsInfo(
            id = "sukkot",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Суккот",
            nameHebrew = "סוכות",
            shortDesc = "Праздник Кущей",
            fullDesc = "",
            durationDays = 7
        )
        multiDay(15, "Тишрей", Sukkot).forEach {
            put(it.first, it.second)
        }

        put(Date(22, "Тишрей"), JewishEventsInfo(
            id = "simchat_torah",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Симхат Тора",
            nameHebrew = "שמחת תורה",
            shortDesc = "Радость Торы",
            fullDesc = "",
            durationDays = 1
        ))


        put(Date(15, "Шват"), JewishEventsInfo(
            id = "tu_bishvat",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Ту би-Шват",
            nameHebrew = "ט״ו בשבט",
            shortDesc = "Новый год деревьев",
            fullDesc = "",
            durationDays = 1
        ))

        val Pesach = JewishEventsInfo(
            id = "pesach",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Песах",
            nameHebrew = "פסח",
            shortDesc = "Праздник исхода из Египта",
            fullDesc = "",
            durationDays = 7
        )
        multiDay(15, "Нисан", Pesach).forEach {
            put(it.first, it.second)
        }

        put(Date(27, "Нисан"), JewishEventsInfo(
            id = "yom_hashoah",
            type = EventType.HISTORICAL,
            nameRu = "Йом а-Шоа",
            nameHebrew = "יום השואה",
            shortDesc = "День памяти жертв Шоа",
            fullDesc = "",
            durationDays = 1
        ))

        put(Date(20, "Сиван"), JewishEventsInfo(
            id = "yom_shichrur",
            type = EventType.HISTORICAL,
            nameRu = "Йом Шихрур ве-Ацала",
            nameHebrew = "יום השחרור וההצלה",
            shortDesc = "День освобождения и спасения",
            fullDesc = "",
            durationDays = 1
        ))

        put(Date(28, "Ияр"), JewishEventsInfo(
            id = "yom_yerushalayim",
            type = EventType.HISTORICAL,
            nameRu = "Йом Иерушалаим",
            nameHebrew = "יום ירושלים",
            shortDesc = "День Иерусалима",
            fullDesc = "",
            durationDays = 1
        ))

        val Shavuot = JewishEventsInfo(
            id = "shavuot",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Шавуот",
            nameHebrew = "שבועות",
            shortDesc = "Праздник дарования Торы",
            fullDesc = "",
            durationDays = 2
        )
        multiDay(6, "Сиван", Shavuot).forEach {
            put(it.first, it.second)
        }

        put(Date(9, "Ав"), JewishEventsInfo(
            id = "tisha_beav",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Тиша бе-Ав",
            nameHebrew = "תשעה באב",
            shortDesc = "Пост в память о разрушении Храма",
            fullDesc = "",
            durationDays = 1
        ))

        put(Date(15, "Ав"), JewishEventsInfo(
            id = "tu_beav",
            type = EventType.RELIGIOUS_CULTURAL,
            nameRu = "Ту бе-Ав",
            nameHebrew = "ט״ו באב",
            shortDesc = "День любви",
            fullDesc = "",
            durationDays = 1
        ))



    }



}
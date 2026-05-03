package com.example.jewcalendar.data

import com.example.jewcalendar.data.EventsProvider.getJewishEventsForDay
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.*



object Calendar {
    enum class HebrewYearType {
        DEFICIENT_COMMON,
        REGULAR_COMMON,
        COMPLETE_COMMON,
        DEFICIENT_LEAP,
        REGULAR_LEAP,
        COMPLETE_LEAP
    }

    fun getMonthDays(month: YearMonth): List<HebrewDay> =
        (1..month.lengthOfMonth()).map { getHebrewDay(month.atDay(it)) }

    fun getSunset(lat: Double, lon: Double, date: LocalDate): LocalTime? {
        val tz  = TimeZone.getDefault()
        val geo = GeoLocation("pt", lat, lon, 0.0, tz)
        val czc = ComplexZmanimCalendar(geo)
        czc.calendar = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()))
        val sunset = czc.sunset ?: return null
        return sunset.toInstant().atZone(ZoneId.systemDefault()).toLocalTime()
    }
    fun getHebrewDay(date: LocalDate): HebrewDay {
        val jc = jewishCalendarFromLocalDate(date)
        return HebrewDay(
            gregorianDate = date,
            hebrewDayOfMonth = jc.jewishDayOfMonth,
            hebrewMonthName = getHebrewMonthName(jc.jewishMonth, isHebrewLeapYear(jc.jewishYear)),
            hebrewYear = jc.jewishYear,
            sunsetTime = null, //okak
            events = getJewishEventsForDay(jc, date),
            userEvents = null, //okak
            isShabbat = false,
            isToday = false
        )
    }
    fun jewishCalendarFromLocalDate(date: LocalDate): JewishCalendar {
        val gc = GregorianCalendar(date.year, date.monthValue - 1, date.dayOfMonth)
        return JewishCalendar(gc)
    }
    fun getHebrewMonthName(month: Int, isLeap: Boolean): String = when (month) {
        1  -> "Нисан"
        2  -> "Ияр"
        3  -> "Сиван"
        4  -> "Тамуз"
        5  -> "Ав"
        6  -> "Элул"
        7  -> "Тишрей"
        8  -> "Хешван"
        9  -> "Кислев"
        10 -> "Тевет"
        11 -> "Шват"
        12 -> if (isLeap) "Адар алеф" else "Адар"
        13 -> "Адар бет"
        else -> "Неизвестный месяц"
    }

    fun getHebrewYearType(hebrewYear: Int): HebrewYearType {
        val isLeap = isHebrewLeapYear(hebrewYear)
        val daysInYear = getDaysInHebrewYear(hebrewYear)

        return when {
            !isLeap && daysInYear == 353 -> HebrewYearType.DEFICIENT_COMMON
            !isLeap && daysInYear == 354 -> HebrewYearType.REGULAR_COMMON
            !isLeap && daysInYear == 355 -> HebrewYearType.COMPLETE_COMMON

            isLeap && daysInYear == 383 -> HebrewYearType.DEFICIENT_LEAP
            isLeap && daysInYear == 384 -> HebrewYearType.REGULAR_LEAP
            isLeap && daysInYear == 385 -> HebrewYearType.COMPLETE_LEAP

            else -> error("Некорректное количество дней в еврейском году: $daysInYear")
        }
    }
    fun isHebrewLeapYear(hebrewYear: Int): Boolean {
        return ((7 * hebrewYear + 1) % 19) < 7
    }
    fun getDaysInHebrewYear(hebrewYear: Int): Int {
        val start = absoluteFromHebrew(hebrewYear, 7, 1)
        val nextStart = absoluteFromHebrew(hebrewYear + 1, 7, 1)
        return nextStart - start
    }
    private fun absoluteFromHebrew(year: Int, month: Int, day: Int): Int {
        return hebrewCalendarElapsedDays(year) + daysBeforeMonth(year, month) + day - 1
    }
    private fun hebrewCalendarElapsedDays(year: Int): Int {
        val monthsElapsed =
            235 * ((year - 1) / 19) +
                    12 * ((year - 1) % 19) +
                    ((7 * ((year - 1) % 19) + 1) / 19)

        val partsElapsed = 204 + 793 * (monthsElapsed % 1080)
        val hoursElapsed =
            5 + 12 * monthsElapsed + 793 * (monthsElapsed / 1080) + (partsElapsed / 1080)

        var day = 1 + 29 * monthsElapsed + (hoursElapsed / 24)
        val parts = (hoursElapsed % 24) * 1080 + (partsElapsed % 1080)
        if (parts >= 19440 ||
            ((day % 7) == 2 && parts >= 9924 && !isHebrewLeapYear(year)) ||
            ((day % 7) == 1 && parts >= 16789 && isHebrewLeapYear(year - 1))
        ) {
            day += 1
        }
        if ((day % 7) == 0 || (day % 7) == 3 || (day % 7) == 5) {
            day += 1
        }
        return day
    }
    private fun daysBeforeMonth(year: Int, month: Int): Int {
        var days = 0

        if (month >= 7) {
            for (m in 7 until month) {
                days += daysInHebrewMonth(year, m)
            }
        } else {
            for (m in 7..lastMonthOfHebrewYear(year)) {
                days += daysInHebrewMonth(year, m)
            }
            for (m in 1 until month) {
                days += daysInHebrewMonth(year, m)
            }
        }

        return days
    }
    private fun lastMonthOfHebrewYear(year: Int): Int =
        if (isHebrewLeapYear(year)) 13 else 12
    fun daysInHebrewMonth(year: Int, month: Int): Int {
        return when (month) {
            1 -> 30
            2 -> 29
            3 -> 30
            4 -> 29
            5 -> 30
            6 -> 29
            7 -> 30
            8 -> if (isLongCheshvan(year)) 30 else 29
            9 -> if (isShortKislev(year)) 29 else 30
            10 -> 29
            11 -> 30
            12 -> if (isHebrewLeapYear(year)) 30 else 29
            13 -> 29
            else -> error("Некорректный номер еврейского месяца: $month")
        }
    }
    private fun isLongCheshvan(year: Int): Boolean {
        val daysInYear = getDaysInHebrewYear(year)
        return daysInYear == 355 || daysInYear == 385
    }

    private fun isShortKislev(year: Int): Boolean {
        val daysInYear = getDaysInHebrewYear(year)
        return daysInYear == 353 || daysInYear == 383
    }
    fun getHebrewMonthDays(hebrewYear: Int, hebrewMonth: Int): List<HebrewDay> {
        val startJC = com.kosherjava.zmanim.hebrewcalendar.JewishCalendar(hebrewYear, hebrewMonth, 1)
        val gc = startJC.gregorianCalendar
        val startDate = LocalDate.of(gc.get(java.util.Calendar.YEAR), gc.get(java.util.Calendar.MONTH) + 1, gc.get(java.util.Calendar.DAY_OF_MONTH))

        val daysInMonth = daysInHebrewMonth(hebrewYear, hebrewMonth)
        return (0 until daysInMonth).map { offset ->
            getHebrewDay(startDate.plusDays(offset.toLong()))
        }
    }
}

package com.example.jewcalendar.data

import java.time.LocalDate
import java.time.LocalTime

data class HebrewDay(
    val gregorianDate: LocalDate,
    val hebrewDayOfMonth: Int,
    val hebrewMonthName: String,
    val hebrewYear: Int,
    val sunsetTime: LocalTime? = null,
    val events: JewishEventsInfo? = null,
    val userEvents: UserEvent? = null,
    val isShabbat: Boolean = false,
    val isToday: Boolean = false
)
package com.example.jewcalendar.domain.usecase

import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.JewishEventsInfo
import java.time.LocalDate
import java.time.LocalTime

class GetSunsetForLocationUseCase {
    data class Result(
        val sunset: LocalTime?,
        val todayEvents: JewishEventsInfo?
    )
    operator fun invoke(lat: Double, lon: Double, date: LocalDate): Result {
        val sunset = Calendar.getSunset(lat, lon, date)
        val day = Calendar.getHebrewDay(date)
        return Result(sunset, day.events)
    }
}
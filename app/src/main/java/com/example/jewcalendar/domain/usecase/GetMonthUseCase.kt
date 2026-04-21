package com.example.jewcalendar.domain.usecase


import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.HebrewDay
import java.time.YearMonth

class GetMonthUseCase {
    operator fun invoke(month: YearMonth): List<HebrewDay> =
        Calendar.getMonthDays(month)
}

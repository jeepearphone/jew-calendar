package com.example.jewcalendar.data

import java.time.LocalDate

data class UserEvent(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val isRecurringYearly: Boolean = true
)
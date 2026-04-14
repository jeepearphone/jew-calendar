package com.example.jewcalendar.data

enum class EventType {
    RELIGIOUS_CULTURAL, HISTORICAL, USER_DEFINED
}

data class JewishEventsInfo(
    val id: String,
    val nameRu: String,
    val nameHebrew: String,
    val type: EventType,
    val shortDesc: String,
    val fullDesc: String,
    val prohibitions: List<String> = emptyList(),
    val durationDays: Int = 1
)
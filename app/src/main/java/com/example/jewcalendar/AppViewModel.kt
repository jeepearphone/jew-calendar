package com.example.jewcalendar

import androidx.lifecycle.ViewModel
import com.example.jewcalendar.data.UserEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CalendarDisplayMode {
    GREGORIAN,
    HEBREW
}

class AppViewModel : ViewModel() {

    private val _userEvents = MutableStateFlow<List<UserEvent>>(emptyList())
    val userEvents: StateFlow<List<UserEvent>> = _userEvents.asStateFlow()

    private val _calendarMode = MutableStateFlow(CalendarDisplayMode.HEBREW)
    val calendarMode: StateFlow<CalendarDisplayMode> = _calendarMode.asStateFlow()

    fun addUserEvent(event: UserEvent) {
        _userEvents.value = _userEvents.value + event.copy(id = (_userEvents.value.maxOfOrNull { it.id } ?: 0) + 1)
    }

    fun removeUserEvent(event: UserEvent) {
        _userEvents.value = _userEvents.value - event
    }

    fun setCalendarMode(mode: CalendarDisplayMode) {
        _calendarMode.value = mode
    }
}
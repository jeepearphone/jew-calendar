package com.example.jewcalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.UserEvent
import com.example.jewcalendar.data.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

enum class CalendarDisplayMode {
    GREGORIAN,
    HEBREW
}

class AppViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _todaySunset = MutableStateFlow<LocalTime?>(null)
    val todaySunset: StateFlow<LocalTime?> = _todaySunset.asStateFlow()



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
    fun onLocationReceived(lat: Double, lon: Double) {
        viewModelScope.launch {
            _userLocation.value = UserLocation(lat, lon)
            _todaySunset.value = Calendar.getSunset(lat, lon, LocalDate.now())
        }
    }
    fun getSunsetForDate(date: LocalDate): LocalTime? {
        val loc = _userLocation.value ?: return null
        return Calendar.getSunset(loc.latitude, loc.longitude, date)
    }
}
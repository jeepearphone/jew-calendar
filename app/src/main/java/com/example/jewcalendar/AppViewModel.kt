package com.example.jewcalendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
import java.time.ZoneId
import java.time.Instant

enum class CalendarDisplayMode {
    GREGORIAN,
    HEBREW
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _userEvents = MutableStateFlow<List<UserEvent>>(emptyList())
    val userEvents: StateFlow<List<UserEvent>> = _userEvents.asStateFlow()

    private val _calendarMode = MutableStateFlow(CalendarDisplayMode.HEBREW)
    val calendarMode: StateFlow<CalendarDisplayMode> = _calendarMode.asStateFlow()

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    private val _todaySunset = MutableStateFlow<LocalTime?>(null)
    val todaySunset: StateFlow<LocalTime?> = _todaySunset.asStateFlow()

    private val _isAfterSunset = MutableStateFlow(false)
    val isAfterSunset: StateFlow<Boolean> = _isAfterSunset.asStateFlow()

    private var sunsetWatcherStarted = false

    fun addUserEvent(event: UserEvent) {
        _userEvents.value = _userEvents.value + event.copy(
            id = (_userEvents.value.maxOfOrNull { it.id } ?: 0) + 1
        )
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
            recalculateSunsetAndDay(lat, lon)
            if (!sunsetWatcherStarted) {
                startSunsetWatcher()
                sunsetWatcherStarted = true
            }
        }
    }

    private val _sunsetVersion = MutableStateFlow(0)
    val sunsetVersion: StateFlow<Int> = _sunsetVersion.asStateFlow()
    private val _effectiveToday = MutableStateFlow(LocalDate.now())
    val effectiveToday: StateFlow<LocalDate> = _effectiveToday.asStateFlow()
    private fun recalculateSunsetAndDay(lat: Double, lon: Double) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val nowInstant = Instant.now()

        val sunsetDate = Calendar.getSunsetDate(lat, lon, today)

        val sunsetToday = sunsetDate
            ?.toInstant()
            ?.atZone(zone)
            ?.toLocalTime()
            ?.withSecond(0)
            ?.withNano(0)

        val isAfter = sunsetDate
            ?.toInstant()
            ?.let { sunsetInstant ->
                !nowInstant.isBefore(sunsetInstant)
            } ?: false

        val effective = if (isAfter) today.plusDays(1) else today

        android.util.Log.d(
            "SUNSET_DEBUG",
            "zone=$zone, today=$today, lat=$lat, lon=$lon, " +
                    "sunsetToday=$sunsetToday, now=${nowInstant.atZone(zone).toLocalTime()}, " +
                    "isAfter=$isAfter, effectiveToday=$effective"
        )

        _todaySunset.value = sunsetToday
        _isAfterSunset.value = isAfter
        _effectiveToday.value = effective
        _sunsetVersion.value = _sunsetVersion.value + 1
    }

    private fun startSunsetWatcher() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                val loc = _userLocation.value ?: continue
                recalculateSunsetAndDay(loc.latitude, loc.longitude)
            }
        }
    }


    fun getSunsetForDate(date: LocalDate): LocalTime? {
        val loc = _userLocation.value ?: return null
        return Calendar.getSunset(loc.latitude, loc.longitude, date)
            ?.let { LocalTime.of(it.hour, it.minute) }
    }
}
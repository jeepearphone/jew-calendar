package com.example.jewcalendar.ui.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jewcalendar.AppViewModel
import com.example.jewcalendar.CalendarDisplayMode
import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.Calendar.getHebrewMonthName
import com.example.jewcalendar.data.Calendar.isHebrewLeapYear
import com.example.jewcalendar.data.HebrewDay
import com.example.jewcalendar.data.JewishEventsInfo
import com.example.jewcalendar.ui.calendar.components.MonthGrid
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


data class HebrewMonthKey(
    val hebrewYear: Int,
    val hebrewMonth: Int,
    val monthName: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    appViewModel : AppViewModel,
    onDayClick: (HebrewDay) -> Unit = {},
    onHolidayClick: (String) -> Unit = {}
) {
    val calendarMode by appViewModel.calendarMode.collectAsState()
    val userEvents by appViewModel.userEvents.collectAsState()
    var currentGregorianMonth by remember { mutableStateOf(YearMonth.now()) }
    var currentHebrewMonthKey by remember {
        val jc = Calendar.jewishCalendarFromLocalDate(LocalDate.now())
        mutableStateOf(
            HebrewMonthKey(
                hebrewYear = jc.jewishYear,
                hebrewMonth = jc.jewishMonth,
                monthName = getHebrewMonthName(jc.jewishMonth, isHebrewLeapYear(jc.jewishYear))
            )
        )
    }
    var days by remember { mutableStateOf<List<HebrewDay>>(emptyList()) }
    var selectedDay by remember { mutableStateOf<HebrewDay?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val now = LocalDate.now()
    val currentTime = LocalTime.now() //  заменить на реальные координаты из LocationProvider
    val sunsetTime: LocalTime? = remember { null } // будет заполнено через GetSunsetForLocationUseCase

    val effectiveToday: LocalDate = remember(sunsetTime) {
        if (sunsetTime != null && currentTime.isAfter(sunsetTime)) {
            now.plusDays(1)
        } else {
            now
        }
    }
    LaunchedEffect(currentGregorianMonth, currentHebrewMonthKey, calendarMode, userEvents) {
        val rawDays = if (calendarMode == CalendarDisplayMode.GREGORIAN) {
            Calendar.getMonthDays(currentGregorianMonth)
        } else {
            Calendar.getHebrewMonthDays(currentHebrewMonthKey.hebrewYear, currentHebrewMonthKey.hebrewMonth)
        }
        days = rawDays.map { day ->
            val userEvent = userEvents.firstOrNull { ev ->
                if (ev.isRecurringYearly) {
                    ev.date.dayOfMonth == day.gregorianDate.dayOfMonth &&
                            ev.date.monthValue == day.gregorianDate.monthValue
                } else {
                    ev.date == day.gregorianDate
                }
            }
            day.copy(isToday = day.gregorianDate == effectiveToday, userEvents = userEvent)
        }
    }
    val titleMain: String
    val titleSub: String
    if (calendarMode == CalendarDisplayMode.GREGORIAN) {
        titleMain = currentGregorianMonth.month
            .getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale("ru"))
            .replaceFirstChar { it.uppercase() }
        titleSub = currentGregorianMonth.year.toString()
    } else {
        titleMain = currentHebrewMonthKey.monthName
        titleSub = currentHebrewMonthKey.hebrewYear.toString()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                if (calendarMode == CalendarDisplayMode.GREGORIAN) {
                    currentGregorianMonth = currentGregorianMonth.minusMonths(1)
                } else {
                    currentHebrewMonthKey = prevHebrewMonth(currentHebrewMonthKey)
                }
            }) {
                Image(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                    contentDescription = "Пред. месяц"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = titleMain,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    titleSub,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = {
                if (calendarMode == CalendarDisplayMode.GREGORIAN) {
                    currentGregorianMonth = currentGregorianMonth.plusMonths(1)
                } else {
                    currentHebrewMonthKey = nextHebrewMonth(currentHebrewMonthKey)
                }
            }) {
                Image(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowForward),
                    contentDescription = "След. месяц"
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            listOf("Вс","Пн","Вт","Ср","Чт","Пт","Сб").forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (d == "Сб") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        MonthGrid(
            days = days,
            firstDayOverride = if (calendarMode == CalendarDisplayMode.HEBREW && days.isNotEmpty())
                days.first().gregorianDate.dayOfWeek
            else null,
            currentMonth = currentGregorianMonth,
            isHebrewMode = calendarMode == CalendarDisplayMode.HEBREW,
            onDayClick = { day -> selectedDay = day }
        )
    }
    selectedDay?.let { day ->
        ModalBottomSheet(
            onDismissRequest = { selectedDay = null },
            sheetState = sheetState
        ) {
            DayBottomSheetContent(
                day = day,
                onHolidayClick = { id ->
                    selectedDay = null
                    onHolidayClick(id)
                }
            )
        }
    }
}

fun nextHebrewMonth(key: HebrewMonthKey): HebrewMonthKey {
    val isLeap = Calendar.isHebrewLeapYear(key.hebrewYear)
    val lastMonth = if (isLeap) 13 else 12
    val newMonth = if (key.hebrewMonth >= lastMonth) 1 else key.hebrewMonth + 1
    val newYear = if (key.hebrewMonth >= lastMonth) key.hebrewYear + 1 else key.hebrewYear
    val newIsLeap = Calendar.isHebrewLeapYear(newYear)
    return HebrewMonthKey(newYear, newMonth, getHebrewMonthName(newMonth, newIsLeap))
}

fun prevHebrewMonth(key: HebrewMonthKey): HebrewMonthKey {
    val newMonth = if (key.hebrewMonth <= 1) {
        val prevYear = key.hebrewYear - 1
        if (Calendar.isHebrewLeapYear(prevYear)) 13 else 12
    } else key.hebrewMonth - 1
    val newYear = if (key.hebrewMonth <= 1) key.hebrewYear - 1 else key.hebrewYear
    val newIsLeap = Calendar.isHebrewLeapYear(newYear)
    return HebrewMonthKey(newYear, newMonth, getHebrewMonthName(newMonth, newIsLeap))
}

@Composable
private fun DayBottomSheetContent(
    day: HebrewDay,
    onHolidayClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = day.gregorianDate.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "${day.hebrewDayOfMonth} ${day.hebrewMonthName} ${day.hebrewYear}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (day.sunsetTime != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "🌅 Закат: ${day.sunsetTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))




        if (day.events != null) {
            Spacer(Modifier.height(16.dp))
            HolidayCard(event = day.events, onClick = { onHolidayClick(day.events.id) })
            Spacer(Modifier.height(8.dp))

        }

        if (day.userEvents != null) {
            Spacer(Modifier.height(16.dp))
            Text("• ${day.userEvents.title}", style = MaterialTheme.typography.bodyMedium)

        }
    }
}

@Composable
private fun HolidayCard(event: JewishEventsInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(event.nameRu, fontWeight = FontWeight.SemiBold)
                Text(event.shortDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onClick) { Text("Подробнее →") }
        }
    }
}
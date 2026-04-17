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
import com.example.jewcalendar.data.Calendar
import com.example.jewcalendar.data.Calendar.getHebrewMonthName
import com.example.jewcalendar.data.Calendar.isHebrewLeapYear
import com.example.jewcalendar.data.HebrewDay
import com.example.jewcalendar.data.JewishEventsInfo
import com.example.jewcalendar.ui.calendar.components.MonthGrid
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onDayClick: (HebrewDay) -> Unit = {},
    onHolidayClick: (String) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val jc = Calendar.jewishCalendarFromLocalDate(currentMonth.atDay(1))
    val monthsName: String = getHebrewMonthName(jc.jewishMonth, isHebrewLeapYear(jc.jewishYear))
    var days by remember { mutableStateOf(Calendar.getMonthDays(currentMonth)) }
    var selectedDay by remember { mutableStateOf<HebrewDay?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(currentMonth) {
        days = Calendar.getMonthDays(currentMonth)
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
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Image(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowBack),
                    contentDescription = "Пред. месяц"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentMonth.month
                        .getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, Locale("ru"))
                        .replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text  = currentMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
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
            currentMonth = currentMonth,
            onDayClick  = { day -> selectedDay = day }
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
            text  = "${day.hebrewMonthName} ${day.hebrewYear}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        // Закат



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
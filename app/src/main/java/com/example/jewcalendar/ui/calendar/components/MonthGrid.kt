package com.example.jewcalendar.ui.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jewcalendar.data.HebrewDay
import java.time.DayOfWeek
import java.time.YearMonth

@Composable
fun MonthGrid(
    days: List<HebrewDay>,
    currentMonth: YearMonth,
    onDayClick: (HebrewDay) -> Unit
) {
    val offset = when (currentMonth.atDay(1).dayOfWeek) {
        DayOfWeek.SUNDAY    -> 0
        DayOfWeek.MONDAY    -> 1
        DayOfWeek.TUESDAY   -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY  -> 4
        DayOfWeek.FRIDAY    -> 5
        DayOfWeek.SATURDAY  -> 6
    }
    val total = offset + days.size
    val weeks = (total + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        repeat(weeks) { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(7) { col ->
                    val index = week * 7 + col - offset
                    val day = days.getOrNull(index)
                    Box(Modifier.weight(1f)) {
                        if (day != null) {
                            DayCell(day = day, onClick = { onDayClick(day) })
                        }
                    }
                }
            }
        }
    }
}
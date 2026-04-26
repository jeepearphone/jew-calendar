package com.example.jewcalendar.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jewcalendar.data.EventType
import com.example.jewcalendar.data.HebrewDay
import com.example.jewcalendar.data.JewishEventsInfo
import com.example.jewcalendar.data.UserEvent
import java.time.LocalDate

@Composable
fun DayCell(day: HebrewDay, onClick: () -> Unit) {
    val isToday  = day.gregorianDate == LocalDate.now()
    val hasJewishEvent: JewishEventsInfo? = day.events
    val hasUserEvent: UserEvent? = day.userEvents
    val borderMod = if (isToday)
        Modifier.border(1.5.dp, Color.Red, RoundedCornerShape(8.dp))
    else Modifier

    Box(
        modifier = Modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(8.dp))
            .then(borderMod)
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text  = day.gregorianDate.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            )
            Text(
                text = day.hebrewDayOfMonth.toString(),
                fontSize = 12.sp,
                lineHeight = 8.sp,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.height(6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hasJewishEvent != null) {
                    val dotColor = when (hasJewishEvent.type) {
                        EventType.RELIGIOUS_CULTURAL    -> Color(0xFFFFB300)
                        EventType.HISTORICAL -> Color(0xFF4CAF50)
                        else -> Color.Transparent
                    }
                    Box(Modifier.size(5.dp).clip(CircleShape).background(dotColor))
                }
                if (hasUserEvent != null) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF9C27B0)))
                }
            }
        }
    }
}
package com.example.jewcalendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jewcalendar.data.EventsProvider
import com.example.jewcalendar.data.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(eventId: String, onBack: () -> Unit) {
    val event = EventsProvider.getById(eventId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(event?.nameRu ?: "Праздник") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { pad ->
        if (event == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                Text("Праздник не найден")
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(event.nameHebrew,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(event.nameRu, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            if (event.durationDays > 1) {
                Spacer(Modifier.height(4.dp))
                Text("${event.durationDays} дней",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("О празднике", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(event.fullDesc, style = MaterialTheme.typography.bodyMedium)
            if (event.prohibitions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                InfoCard("Ограничения", event.prohibitions, MaterialTheme.colorScheme.errorContainer)
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, items: List<String>, bg: androidx.compose.ui.graphics.Color) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            items.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp)) }
        }
    }
}


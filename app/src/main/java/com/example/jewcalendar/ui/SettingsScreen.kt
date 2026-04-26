package com.example.jewcalendar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jewcalendar.data.EventsProvider
import com.example.jewcalendar.data.UserEvent
import java.time.LocalDate

@Composable
fun SettingsScreen(onHolidayClick: (String) -> Unit = {}) {
    var userEvents by remember { mutableStateOf(listOf<UserEvent>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var shabbatMode by remember { mutableStateOf(false) }
    val allHolidays = remember { EventsProvider.getAll() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Режим Шаббата", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Switch(checked = shabbatMode, onCheckedChange = { shabbatMode = it })
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Мои события", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                FilledTonalIconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "Добавить")
                }
            }
        }
        if (userEvents.isEmpty()) {
            item { Text("Нет личных событий", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(userEvents) { ev ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(ev.title, fontWeight = FontWeight.SemiBold)
                            Text(ev.date.toString(), style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { userEvents = userEvents - ev }) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Особые даты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(allHolidays) { h ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onHolidayClick(h.id) }) {
                Row(Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(h.nameRu, fontWeight = FontWeight.SemiBold)
                        Text("${h.nameHebrew} · ${h.shortDesc}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                }
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { ev -> userEvents = userEvents + ev; showAddDialog = false }
        )
    }
}

@Composable
private fun AddEventDialog(onDismiss: () -> Unit, onAdd: (UserEvent) -> Unit) {
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().toString()) }
    var recurring by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новое событие") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Название*") }, singleLine = true)
                OutlinedTextField(dateStr, { dateStr = it; err = false },
                    label = { Text("Дата (гггг-мм-дд)") }, isError = err, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(recurring, { recurring = it })
                    Text("Ежегодно")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val date = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                if (title.isBlank() || date == null) { err = date == null; return@TextButton }
                onAdd(UserEvent(title = title, date = date, isRecurringYearly = recurring))
            }) { Text("Добавить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
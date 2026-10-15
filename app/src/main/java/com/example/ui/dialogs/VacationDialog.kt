package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.VacationPeriodEntity
import com.example.ui.theme.VacationTeal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationDialog(
    vacations: List<VacationPeriodEntity>,
    onDismiss: () -> Unit,
    onSaveVacation: (startDate: LocalDate, endDate: LocalDate, title: String, notes: String?) -> Unit,
    onDeleteVacation: (VacationPeriodEntity) -> Unit
) {
    val today = LocalDate.now()
    var startDate by remember { mutableStateOf(today.plusDays(7)) }
    var endDate by remember { mutableStateOf(today.plusDays(36)) } // default 30 days
    var title by remember { mutableStateOf("Férias") }
    var notes by remember { mutableStateOf("") }

    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    val daysCount = (ChronoUnit.DAYS.between(startDate, endDate) + 1).coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(VacationTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = VacationTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Marcar Mês ou Período de Férias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Defina datas completas ou quebradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Presets: Mês Inteiro vs Período Quebrado
                Text(
                    text = "Atalhos Rápidos de Férias:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Next month full
                    val nextMonth = YearMonth.now().plusMonths(1)
                    val nextMonthName = nextMonth.format(DateTimeFormatter.ofPattern("MMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
                    FilterChip(
                        selected = startDate == nextMonth.atDay(1) && endDate == nextMonth.atEndOfMonth(),
                        onClick = {
                            startDate = nextMonth.atDay(1)
                            endDate = nextMonth.atEndOfMonth()
                            title = "Férias de $nextMonthName"
                        },
                        label = { Text("Mês de $nextMonthName") },
                        modifier = Modifier.weight(1f)
                    )

                    // 30 days starting from custom
                    FilterChip(
                        selected = daysCount == 30L,
                        onClick = {
                            endDate = startDate.plusDays(29)
                        },
                        label = { Text("Fixar 30 dias") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Selected Range Cards
                Text(
                    text = "Quando começam e quando terminam as férias?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingStart = true }
                            .testTag("vacation_start_date_picker"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = VacationTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Início", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VacationTeal
                            )
                            Text(
                                text = startDate.format(DateTimeFormatter.ofPattern("EEEE", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { pickingEnd = true }
                            .testTag("vacation_end_date_picker"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = VacationTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Término", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = VacationTeal
                            )
                            Text(
                                text = endDate.format(DateTimeFormatter.ofPattern("EEEE", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Summary of vacation days
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VacationTeal.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏖️ Duração total:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$daysCount dias livres",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VacationTeal
                        )
                    }
                }

                // Title and notes
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome / Motivo") },
                    placeholder = { Text("Ex: Férias Anuais, Viagem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    placeholder = { Text("Ex: Telefone substituto, viagem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Already scheduled vacations list
                if (vacations.isNotEmpty()) {
                    Text(
                        text = "Férias Agendadas:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        vacations.forEach { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = VacationTeal.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${item.startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} até ${item.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                            fontSize = 12.sp,
                                            color = VacationTeal,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!item.notes.isNullOrBlank()) {
                                            Text(text = item.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = { onDeleteVacation(item) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir período",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!endDate.isBefore(startDate)) {
                        onSaveVacation(startDate, endDate, title, notes.ifBlank { null })
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VacationTeal),
                modifier = Modifier.testTag("save_vacation_button")
            ) {
                Text("Confirmar Férias", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )

    // Start Date Picker
    if (pickingStart) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        startDate = picked
                        if (endDate.isBefore(picked)) {
                            endDate = picked.plusDays(29)
                        }
                    }
                    pickingStart = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingStart = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker
    if (pickingEnd) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        if (!picked.isBefore(startDate)) {
                            endDate = picked
                        }
                    }
                    pickingEnd = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pickingEnd = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

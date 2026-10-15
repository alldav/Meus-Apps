package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStatus
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.ScheduleEngine
import com.example.ui.components.DayCell
import com.example.ui.theme.FeriadoGold
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.theme.VacationTeal
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState,
    onOpenVacationDialog: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val currentMonth = uiState.currentMonth
    val today = LocalDate.now()
    var selectedFilter by remember { mutableStateOf("Todos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Month Navigation Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Escala ${uiState.config.scheduleType.title} • ${uiState.config.selectedUf}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês")
                }
            }
        }

        // Quick Actions Row: Férias Button + Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onOpenVacationDialog,
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = VacationTeal.copy(alpha = 0.15f), contentColor = VacationTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_vacation_dialog_button")
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Marcar Férias", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            listOf("Todos", "Folgas", "Plantões", "Férias", "FTs", "Feriados").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 11.sp) },
                    modifier = Modifier.testTag("filter_chip_${filter}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Weekday Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").forEachIndexed { idx, dayName ->
                Text(
                    text = dayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (idx == 0 || idx == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Calendar Grid
        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekFirst = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0, Monday = 1, ...
        val daysInMonth = currentMonth.lengthOfMonth()

        val prevMonth = currentMonth.minusMonths(1)
        val daysInPrevMonth = prevMonth.lengthOfMonth()

        val totalCells = ((dayOfWeekFirst + daysInMonth + 6) / 7) * 7

        val monthHolidays = BrazilianHolidayProvider.getHolidaysForYear(currentMonth.year, uiState.config.selectedUf)
            .associateBy { it.date }

        Column(modifier = Modifier.weight(1f)) {
            var cellIndex = 0
            while (cellIndex < totalCells) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (col in 0..6) {
                        if (cellIndex < totalCells) {
                            val (date, isCurMonth) = when {
                                cellIndex < dayOfWeekFirst -> {
                                    val day = daysInPrevMonth - dayOfWeekFirst + cellIndex + 1
                                    Pair(prevMonth.atDay(day), false)
                                }
                                cellIndex < dayOfWeekFirst + daysInMonth -> {
                                    val day = cellIndex - dayOfWeekFirst + 1
                                    Pair(currentMonth.atDay(day), true)
                                }
                                else -> {
                                    val day = cellIndex - (dayOfWeekFirst + daysInMonth) + 1
                                    Pair(currentMonth.plusMonths(1).atDay(day), false)
                                }
                            }

                            val effectiveStatus = ScheduleEngine.calculateEffectiveStatus(
                                date,
                                uiState.config,
                                uiState.overrides[date],
                                uiState.vacations
                            )

                            val holiday = monthHolidays[date]
                            val hasAppointments = uiState.appointments.any { it.date == date }

                            val matchesFilter = when (selectedFilter) {
                                "Folgas" -> effectiveStatus == DayStatus.OFF
                                "Plantões" -> effectiveStatus == DayStatus.WORK
                                "Férias" -> effectiveStatus == DayStatus.VACATION
                                "FTs" -> effectiveStatus == DayStatus.FT
                                "Feriados" -> holiday != null
                                else -> true
                            }

                            DayCell(
                                date = date,
                                isCurrentMonth = isCurMonth && matchesFilter,
                                isToday = date == today,
                                isSelected = date == uiState.selectedDate,
                                status = effectiveStatus,
                                holiday = holiday,
                                hasAppointments = hasAppointments,
                                onSelectDate = onSelectDate,
                                modifier = Modifier.weight(1f)
                            )
                            cellIndex++
                        }
                    }
                }
            }
        }

        // Bottom Tactical Legend
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator(color = FolgaGreen, label = "Folga")
                LegendIndicator(color = PlantaoBlue, label = "Plantão")
                LegendIndicator(color = VacationTeal, label = "Férias")
                LegendIndicator(color = FtAmber, label = "FT")
                LegendIndicator(color = TrocaPurple, label = "Troca")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FeriadoGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Feriado", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

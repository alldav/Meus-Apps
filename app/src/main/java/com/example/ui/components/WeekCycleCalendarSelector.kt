package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.PlantaoBlue
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * An interactive, accessible calendar cycle selector for multi-day schedules (e.g. 4x2, 6x1, 5x2, Custom).
 * Instead of abstract "Dia 1, Dia 2", it displays real calendar dates with day names,
 * marks "HOJE" with high visibility, and allows one-tap toggling between Plantão and Folga.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeekCycleCalendarSelector(
    startDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    cycleLength: Int,
    pattern: List<Boolean>,
    onPatternChange: (List<Boolean>) -> Unit,
    modifier: Modifier = Modifier,
    scaleTitle: String = "4 por 2",
    workCount: Int = 4,
    offCount: Int = 2
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val mondayThisWeek = remember { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }

    // Ensure pattern has exact cycleLength items
    val safePattern = remember(pattern, cycleLength) {
        if (pattern.size == cycleLength) pattern
        else {
            val list = mutableListOf<Boolean>()
            repeat(workCount) { list.add(true) }
            repeat(offCount) { list.add(false) }
            while (list.size < cycleLength) list.add(false)
            list.take(cycleLength)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date anchor card with real calendar information
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .testTag("cycle_calendar_start_date_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendário",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Data de Início do Ciclo ($scaleTitle)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = startDate.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (startDate == today) {
                        Text(
                            text = "⭐ Começando no dia de hoje",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FolgaGreen
                        )
                    }
                }

                TextButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.testTag("btn_change_calendar_date")
                ) {
                    Text("Alterar", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick alignment buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = startDate == today,
                onClick = { onStartDateChange(today) },
                label = { Text("Hoje (${today.format(DateTimeFormatter.ofPattern("dd/MM"))})") },
                leadingIcon = {
                    if (startDate == today) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    else Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = startDate == mondayThisWeek,
                onClick = { onStartDateChange(mondayThisWeek) },
                label = { Text("Segunda (${mondayThisWeek.format(DateTimeFormatter.ofPattern("dd/MM"))})") },
                leadingIcon = {
                    if (startDate == mondayThisWeek) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    else Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Instructional callout
        Text(
            text = "Toque em qualquer dia abaixo para alternar entre Plantão e Folga:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Real Calendar Day Cards (Displayed in pairs or responsive 2-column grid for high readability)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until cycleLength step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // First day of row
                    val day1 = startDate.plusDays(i.toLong())
                    val isWork1 = safePattern.getOrElse(i) { true }
                    CalendarCycleDayCard(
                        date = day1,
                        cycleDayIndex = i + 1,
                        isWork = isWork1,
                        isToday = day1 == today,
                        onToggle = {
                            val mutable = safePattern.toMutableList()
                            mutable[i] = !isWork1
                            onPatternChange(mutable)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Second day of row (if exists)
                    if (i + 1 < cycleLength) {
                        val day2 = startDate.plusDays((i + 1).toLong())
                        val isWork2 = safePattern.getOrElse(i + 1) { false }
                        CalendarCycleDayCard(
                            date = day2,
                            cycleDayIndex = i + 2,
                            isWork = isWork2,
                            isToday = day2 == today,
                            onToggle = {
                                val mutable = safePattern.toMutableList()
                                mutable[i + 1] = !isWork2
                                onPatternChange(mutable)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty spacer if odd count
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Live cycle summary box
        val workDaysCalculated = safePattern.count { it }
        val offDaysCalculated = safePattern.count { !it }
        val nextCycleDate = startDate.plusDays(cycleLength.toLong())

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📋 Resumo do Ciclo Selecionado",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Restore preset button
                    TextButton(
                        onClick = {
                            val reset = mutableListOf<Boolean>()
                            repeat(workCount) { reset.add(true) }
                            repeat(offCount) { reset.add(false) }
                            while (reset.size < cycleLength) reset.add(false)
                            onPatternChange(reset.take(cycleLength))
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurar Padrão", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PlantaoBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$workDaysCalculated dias de Plantão",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(FolgaGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$offDaysCalculated dias de Folga",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A partir de ${nextCycleDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}, a mesma sequência se repetirá continuamente.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // DatePicker Dialog for selecting custom start date
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onStartDateChange(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Individual Calendar Day Card showing real date, day of week, "HOJE" badge,
 * and high-contrast Plantão/Folga status.
 */
@Composable
private fun CalendarCycleDayCard(
    date: LocalDate,
    cycleDayIndex: Int,
    isWork: Boolean,
    isToday: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfWeekName = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR"))
        .replace(".", "")
        .uppercase()

    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM"))
    val targetBgColor = if (isWork) PlantaoBlue.copy(alpha = 0.12f) else FolgaGreen.copy(alpha = 0.12f)
    val targetBorderColor = if (isWork) PlantaoBlue else FolgaGreen

    val animatedBg by animateColorAsState(targetValue = targetBgColor, label = "bg")
    val animatedBorder by animateColorAsState(targetValue = targetBorderColor, label = "border")

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .testTag("cycle_day_card_$cycleDayIndex"),
        colors = CardDefaults.cardColors(containerColor = animatedBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isToday) 2.5.dp else 1.5.dp,
            color = if (isToday) MaterialTheme.colorScheme.primary else animatedBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top row: Day of week + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$dayOfWeekName $formattedDate",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isToday) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "HOJE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Dia $cycleDayIndex",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status chip button inside card
            Surface(
                color = if (isWork) PlantaoBlue else FolgaGreen,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWork) Icons.Default.Work else Icons.Default.Weekend,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWork) "PLANTÃO" else "FOLGA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Toque para alternar",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

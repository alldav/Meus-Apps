package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.DayStatus
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.ScheduleEngine
import com.example.ui.theme.FeriadoGold
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.VacationTeal
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCheckScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState,
    onSelectDateForAction: (LocalDate) -> Unit
) {
    var checkDate by remember { mutableStateOf(uiState.quickCheckDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val status = ScheduleEngine.calculateEffectiveStatus(
        checkDate,
        uiState.config,
        uiState.overrides[checkDate],
        uiState.vacations
    )
    val holiday = BrazilianHolidayProvider.getHolidayOn(checkDate, uiState.config.selectedUf)

    val isOff = status == DayStatus.OFF
    val isVacation = status == DayStatus.VACATION
    val themeColor = when (status) {
        DayStatus.VACATION -> VacationTeal
        DayStatus.OFF -> FolgaGreen
        else -> PlantaoBlue
    }

    val formattedDate = checkDate.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")))
        .replaceFirstChar { it.uppercase() }

    val nextWeekendOff = uiState.nextWeekendOff

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Heading
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Simulador: Estarei Livre?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calcule qualquer data futura sem precisar fazer contas mentais.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Selector Trigger Button
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_date_picker_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher Data para Simular: ${checkDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                }
            }
        }

        // Prominent Result Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("simulation_result_card"),
            colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, themeColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(themeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isVacation -> Icons.Default.FlightTakeoff
                            isOff -> Icons.Default.Celebration
                            else -> Icons.Default.Shield
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        isVacation -> "VOCÊ ESTARÁ DE FÉRIAS!"
                        isOff -> "VOCÊ ESTARÁ DE FOLGA!"
                        else -> "VOCÊ ESTARÁ DE PLANTÃO!"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        isVacation -> "🏖️ Período de férias cadastrado. Dia 100% livre de obrigações de escala para viajar e aproveitar!"
                        isOff -> "🎉 Dia 100% livre na sua escala ${uiState.config.scheduleType.title}. Pode agendar viagens, churrascos e compromissos com segurança!"
                        else -> "🛡️ Você estará escalado no turno ${ScheduleEngine.getShiftHours(checkDate, uiState.config)}. Evite marcar eventos pessoais neste dia ou organize uma troca de plantão."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                if (holiday != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FeriadoGold.copy(alpha = 0.2f))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = FeriadoGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Feriado: ${holiday.name} (${if (holiday.isNational) "Nacional" else "Estadual - ${holiday.stateUf}"})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { onSelectDateForAction(checkDate) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Abrir Ações para este Dia (Agendar / Troca)")
                }
            }
        }

        // Quick Intelligence Cards
        Text(
            text = "Consultas Inteligentes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Weekend Off Shortcut
        if (nextWeekendOff != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        checkDate = nextWeekendOff.first
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FolgaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Weekend, contentDescription = null, tint = FolgaGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Próximo Fim de Semana 100% Livre",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${nextWeekendOff.first.format(DateTimeFormatter.ofPattern("dd/MM"))} e ${nextWeekendOff.second.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                fontSize = 12.sp,
                                color = FolgaGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Christmas & New Year Quick Checks
        val christmasDate = LocalDate.of(LocalDate.now().year, 12, 25).let { if (it.isBefore(LocalDate.now())) it.plusYears(1) else it }
        val newYearDate = LocalDate.of(LocalDate.now().year, 12, 31).let { if (it.isBefore(LocalDate.now())) it.plusYears(1) else it }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chrisStatus = ScheduleEngine.calculateEffectiveStatus(christmasDate, uiState.config, uiState.overrides[christmasDate], uiState.vacations)
            val chrisColor = when (chrisStatus) {
                DayStatus.VACATION -> VacationTeal
                DayStatus.OFF -> FolgaGreen
                else -> PlantaoBlue
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { checkDate = christmasDate },
                colors = CardDefaults.cardColors(containerColor = chrisColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, chrisColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Natal (${christmasDate.year})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (chrisStatus == DayStatus.OFF || chrisStatus == DayStatus.VACATION) "FOLGA 🎉" else "PLANTÃO 🛡️",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = chrisColor
                    )
                }
            }

            val nyStatus = ScheduleEngine.calculateEffectiveStatus(newYearDate, uiState.config, uiState.overrides[newYearDate], uiState.vacations)
            val nyColor = when (nyStatus) {
                DayStatus.VACATION -> VacationTeal
                DayStatus.OFF -> FolgaGreen
                else -> PlantaoBlue
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { checkDate = newYearDate },
                colors = CardDefaults.cardColors(containerColor = nyColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, nyColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Virada (${newYearDate.year})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (nyStatus == DayStatus.OFF || nyStatus == DayStatus.VACATION) "FOLGA 🎉" else "PLANTÃO 🛡️",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = nyColor
                    )
                }
            }
        }
    }

    // Material 3 DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = checkDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        checkDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

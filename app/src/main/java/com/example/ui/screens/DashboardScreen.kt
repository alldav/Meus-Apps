package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Weekend
import com.example.data.update.UpdateCheckState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStatus
import com.example.domain.ScheduleEngine
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.theme.VacationTeal
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQuickCheck: () -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenVacationDialog: () -> Unit,
    onOpenOnboardingWizard: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onNavigateToCalendarWithDate: (LocalDate) -> Unit = { onNavigateToCalendar() }
) {
    val scrollState = rememberScrollState()
    val today = LocalDate.now()
    val daysUntilNextOff = ChronoUnit.DAYS.between(today, uiState.nextOffDate).coerceAtLeast(0)

    val (statusColor, statusTitle, statusSubtitle) = when (uiState.todayStatus) {
        DayStatus.OFF -> Triple(FolgaGreen, "FOLGA HOJE", "Dia livre para descanso")
        DayStatus.WORK -> Triple(PlantaoBlue, "PLANTÃO HOJE", "Turno: ${uiState.todayHoursLabel}")
        DayStatus.FT -> Triple(FtAmber, "FOLGA TRABALHADA", "Turno extra de FT registrado")
        DayStatus.SWAP -> Triple(TrocaPurple, "TROCA DE PLANTÃO", "Permuta de serviço ativa")
        DayStatus.VACATION -> Triple(VacationTeal, "EM FÉRIAS 🏖️", "Aproveite seu descanso merecido!")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Editorial Loose Header with Personalized Greeting
            Column {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Olá, ${uiState.config.userName}!",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { onOpenOnboardingWizard() }
                            .testTag("reconfigure_scale_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Escala ${uiState.config.scheduleType.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // In-app Update Notification Banner
            if (uiState.updateCheckState is UpdateCheckState.Available) {
                val release = (uiState.updateCheckState as UpdateCheckState.Available).release
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.checkForUpdates(showDialogIfAvailable = true) }
                        .testTag("dashboard_update_banner"),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nova versão ${release.versionName} disponível!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Toque para ver as novidades e atualizar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Quick Actions Bar: Marcar Férias & Assistente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenVacationDialog() }
                        .testTag("dashboard_vacation_button"),
                    color = VacationTeal.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VacationTeal.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = VacationTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Férias", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = VacationTeal)
                            Text(
                                text = if (uiState.vacations.isEmpty()) "Marcar período" else "${uiState.vacations.size} agendada(s)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToQuickCheck() },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Simulador", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Checar data futura", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Big, High-Legibility Accessible Hero Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onSelectDate(today) }
                    .testTag("today_hero_card"),
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "STATUS DE HOJE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 1.sp
                            )
                        }

                        Icon(
                            imageVector = if (uiState.todayStatus == DayStatus.OFF || uiState.todayStatus == DayStatus.VACATION) Icons.Default.EventAvailable else Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 36.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = statusColor.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Next Folga indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.todayStatus == DayStatus.OFF) "Próximo plantão amanhã" else "Próxima folga em $daysUntilNextOff dia(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.nextOffDate.format(DateTimeFormatter.ofPattern("dd/MM (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FolgaGreen
                        )
                    }
                }
            }

            // 1. Vacation Active or Upcoming Section
            val currentVacation = uiState.vacations.firstOrNull {
                !today.isBefore(it.startDate) && !today.isAfter(it.endDate)
            }
            val upcomingVacation = uiState.vacations
                .filter { it.startDate.isAfter(today) && (it.startDate.month == today.month || ChronoUnit.DAYS.between(today, it.startDate) <= 30) }
                .minByOrNull { it.startDate }

            if (currentVacation != null) {
                val daysLeft = ChronoUnit.DAYS.between(today, currentVacation.endDate).toInt() + 1
                val returnToWorkDate = ScheduleEngine.findNextWorkDay(
                    currentVacation.endDate.plusDays(1),
                    uiState.config,
                    uiState.overrides,
                    uiState.vacations
                )
                val isLastDay = today == currentVacation.endDate

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("dashboard_vacation_active_card"),
                    color = VacationTeal.copy(alpha = 0.14f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VacationTeal)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(VacationTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.BeachAccess, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Você está de férias, parabéns! 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VacationTeal
                                )
                                Text(
                                    text = "Aproveite seu descanso e recarregue as energias!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vacation Days Counter
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🏖️", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isLastDay) "Hoje é o seu último dia de férias!" else "Você ainda tem $daysLeft dias de férias",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isLastDay) MaterialTheme.colorScheme.error else VacationTeal
                                    )
                                    Text(
                                        text = "Período: ${currentVacation.startDate.format(DateTimeFormatter.ofPattern("dd/MM"))} até ${currentVacation.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Return Date Reminder
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLastDay) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (isLastDay) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isLastDay) "Lembrete: Seu Retorno é Amanhã!" else "Lembrete de Retorno",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isLastDay) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Você volta a trabalhar dia ${returnToWorkDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }}.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onNavigateToCalendarWithDate(returnToWorkDate) }) {
                                    Text("Ver Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else if (upcomingVacation != null) {
                val daysUntilVacation = ChronoUnit.DAYS.between(today, upcomingVacation.startDate).toInt()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToCalendarWithDate(upcomingVacation.startDate) }
                        .testTag("dashboard_upcoming_vacation_card"),
                    color = VacationTeal.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VacationTeal.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VacationTeal.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = VacationTeal, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mês de Férias se Aproximando! 🏖️",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = VacationTeal
                            )
                            Text(
                                text = "Faltam $daysUntilVacation dia(s) para iniciar suas férias (${upcomingVacation.startDate.format(DateTimeFormatter.ofPattern("dd/MM"))} a ${upcomingVacation.endDate.format(DateTimeFormatter.ofPattern("dd/MM"))}).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = VacationTeal, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // 2. Pending Shift Swap Repayments Reminders
            val pendingSwapsToPay = uiState.overrides.values
                .filter { it.status == DayStatus.SWAP && it.paybackDate != null && !it.paybackCompleted }
                .sortedBy { it.paybackDate }

            if (pendingSwapsToPay.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pendingSwapsToPay.forEach { swap ->
                        val daysToPay = ChronoUnit.DAYS.between(today, swap.paybackDate).toInt()
                        val isTodayPay = daysToPay == 0
                        val isOverdue = daysToPay < 0
                        val isTomorrow = daysToPay == 1

                        val cardBg = when {
                            isTodayPay || isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                            isTomorrow -> FtAmber.copy(alpha = 0.15f)
                            else -> TrocaPurple.copy(alpha = 0.12f)
                        }
                        val cardBorderColor = when {
                            isTodayPay || isOverdue -> MaterialTheme.colorScheme.error
                            isTomorrow -> FtAmber
                            else -> TrocaPurple.copy(alpha = 0.5f)
                        }
                        val statusHeadline = when {
                            isTodayPay -> "⚠️ HOJE é o dia de pagar plantão!"
                            isOverdue -> "⚠️ VENCIDO há ${-daysToPay} dia(s)!"
                            isTomorrow -> "🔔 Amanhã você paga o plantão!"
                            else -> "Faltam $daysToPay dias para pagar"
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .testTag("swap_repay_reminder_card_${swap.date}"),
                            color = cardBg,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, cardBorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isTodayPay || isOverdue) MaterialTheme.colorScheme.error else TrocaPurple),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.SwapHoriz,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Lembrete: Devolver Plantão",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isTodayPay || isOverdue) MaterialTheme.colorScheme.error else TrocaPurple
                                            )
                                            Text(
                                                text = "Para: ${swap.partnerName ?: "Colega"}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = (if (isTodayPay || isOverdue) MaterialTheme.colorScheme.error else TrocaPurple).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = statusHeadline,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isTodayPay || isOverdue) MaterialTheme.colorScheme.error else TrocaPurple,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Data marcada: ${swap.paybackDate!!.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!swap.notes.isNullOrBlank()) {
                                    Text(
                                        text = "Obs: ${swap.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { onNavigateToCalendarWithDate(swap.paybackDate!!) },
                                        modifier = Modifier.testTag("swap_repay_view_calendar_${swap.date}")
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ver no Calendário", fontSize = 12.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.setSwapPaybackCompleted(swap.date, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = FolgaGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("swap_repay_mark_paid_${swap.date}")
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pago ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Next Weekend Off Card (Click navigates directly to calendar with that date selected!)
            uiState.nextWeekendOff?.let { weekend ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToCalendarWithDate(weekend.first) }
                        .padding(16.dp)
                        .testTag("dashboard_next_weekend_card"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FolgaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Weekend, contentDescription = null, tint = FolgaGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Próximo Fim de Semana 100% Livre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sábado ${weekend.first.format(DateTimeFormatter.ofPattern("dd/MM"))} e Domingo ${weekend.second.format(DateTimeFormatter.ofPattern("dd/MM"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FolgaGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Toque para ver no calendário completo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Ver no calendário", tint = FolgaGreen)
                }
            }

            // Horizontal Next 7 Days Timeline
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximos Dias",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ver Mês Completo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToCalendar() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 0..6) {
                        val futureDate = today.plusDays(i.toLong())
                        val futureStatus = ScheduleEngine.calculateEffectiveStatus(
                            futureDate,
                            uiState.config,
                            uiState.overrides[futureDate],
                            uiState.vacations
                        )
                        val color = when (futureStatus) {
                            DayStatus.OFF -> FolgaGreen
                            DayStatus.WORK -> PlantaoBlue
                            DayStatus.FT -> FtAmber
                            DayStatus.SWAP -> TrocaPurple
                            DayStatus.VACATION -> VacationTeal
                        }

                        Surface(
                            modifier = Modifier
                                .width(84.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onSelectDate(futureDate) },
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (i == 0) 2.dp else 1.dp,
                                color = if (i == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = futureDate.format(DateTimeFormatter.ofPattern("EEE", Locale("pt", "BR"))).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = futureDate.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(color)
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = futureStatus.label.take(6),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Monthly Productivity Numbers
            uiState.monthSummary?.let { summary ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Neste Mês (${uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = summary.totalWorkDays.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = PlantaoBlue
                            )
                            Text("Plantões", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = summary.totalOffDays.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = FolgaGreen
                            )
                            Text("Folgas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (summary.totalVacationDays > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = summary.totalVacationDays.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = VacationTeal
                                )
                                Text("Férias", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = summary.totalFtDays.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FtAmber
                                )
                                Text("FTs", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = summary.totalSwapDays.toString(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = TrocaPurple
                            )
                            Text("Trocas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Floating Action Button for the Assistant
        ExtendedFloatingActionButton(
            onClick = onOpenAssistant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("floating_assistant_button"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Perguntar data")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Consultar Data",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

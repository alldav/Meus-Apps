package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
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
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.model.BrazilianHoliday
import com.example.data.model.DayStatus
import com.example.data.model.ShiftPeriod
import com.example.ui.theme.FeriadoGold
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.theme.VacationTeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailBottomSheet(
    sheetState: SheetState,
    date: LocalDate,
    status: DayStatus,
    period: ShiftPeriod,
    holiday: BrazilianHoliday?,
    override: DayOverrideEntity?,
    appointments: List<AppointmentEntity>,
    onDismiss: () -> Unit,
    onAddAppointment: () -> Unit,
    onMarkFt: () -> Unit,
    onMarkSwap: () -> Unit,
    onRestoreScale: () -> Unit,
    onDeleteAppointment: (AppointmentEntity) -> Unit
) {
    val dateFull = date.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")))
        .replaceFirstChar { it.uppercase() }

    val (statusColor, statusTitle, statusIcon) = when (status) {
        DayStatus.OFF -> Triple(FolgaGreen, "FOLGA GARANTIDA 🎉", Icons.Default.EventAvailable)
        DayStatus.WORK -> Triple(PlantaoBlue, "PLANTÃO DE ESCALA 🛡️", Icons.Default.EventBusy)
        DayStatus.FT -> Triple(FtAmber, "FOLGA TRABALHADA (FT) 💼", Icons.Default.Paid)
        DayStatus.SWAP -> Triple(TrocaPurple, "TROCA DE PLANTÃO 🔄", Icons.Default.SwapHoriz)
        DayStatus.VACATION -> Triple(VacationTeal, "EM PERÍODO DE FÉRIAS 🏖️", Icons.Default.EventAvailable)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("day_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFull,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusTitle,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (status == DayStatus.WORK) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Holiday Highlight Card if applicable
            if (holiday != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FeriadoGold.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = FeriadoGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (holiday.isNational) "Feriado Nacional do Brasil" else "Feriado Estadual (${holiday.stateUf})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FeriadoGold
                            )
                            Text(
                                text = holiday.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Override Details if FT or Swap
            if (override != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = (if (override.status == DayStatus.FT) FtAmber else TrocaPurple).copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (override.status == DayStatus.FT) "Detalhes da Folga Trabalhada" else "Detalhes da Troca de Plantão",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (override.status == DayStatus.FT) FtAmber else TrocaPurple
                        )
                        if (!override.partnerName.isNullOrBlank()) {
                            Text(
                                text = "Colega parceiro: ${override.partnerName}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!override.extraValueOrLocation.isNullOrBlank()) {
                            Text(
                                text = "Info / Valor: ${override.extraValueOrLocation}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (override.paybackDate != null) {
                            Text(
                                text = "Pagar em: ${override.paybackDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", java.util.Locale("pt", "BR")))} ${if (override.paybackCompleted) "✓ (Pago)" else "(Pendente)"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (override.paybackCompleted) FolgaGreen else TrocaPurple
                            )
                        }
                        if (!override.notes.isNullOrBlank()) {
                            Text(
                                text = "Notas: ${override.notes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onRestoreScale,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restaurar para Escala Original", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Text(
                text = "Ações para este dia",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddAppointment,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_appointment_sheet_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compromisso", fontSize = 12.sp)
                }

                if (status == DayStatus.OFF) {
                    FilledTonalButton(
                        onClick = onMarkFt,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_ft_sheet_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = FtAmber.copy(alpha = 0.2f), contentColor = FtAmber)
                    ) {
                        Icon(Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fazer FT", fontSize = 12.sp)
                    }
                } else if (status == DayStatus.WORK) {
                    FilledTonalButton(
                        onClick = onMarkSwap,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mark_swap_sheet_button"),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = TrocaPurple.copy(alpha = 0.2f), contentColor = TrocaPurple)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trocar", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Existing Appointments & Observations
            Text(
                text = "Compromissos e Lembretes (${appointments.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum compromisso marcado para este dia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(appointments, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (item.time != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    if (!item.notes.isNullOrBlank()) {
                                        Text(
                                            text = item.notes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteAppointment(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir compromisso",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.dialogs

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import com.example.data.model.DayStatus
import com.example.domain.ScheduleEngine
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.theme.TrocaWorkCyan
import com.example.ui.theme.VacationTeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DayOverrideDialog(
    date: LocalDate,
    targetStatus: DayStatus, // FT, SWAP, or SWAP_WORK
    config: ScheduleConfigEntity = ScheduleConfigEntity(),
    overrides: Map<LocalDate, DayOverrideEntity> = emptyMap(),
    vacations: List<VacationPeriodEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (partner: String?, valueOrLocation: String?, notes: String?, paybackDate: LocalDate?) -> Unit
) {
    val context = LocalContext.current
    var partner by remember { mutableStateOf("") }
    var valueOrLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paybackDate by remember { mutableStateOf<LocalDate?>(null) }
    var confirmDespiteConflict by remember { mutableStateOf(false) }

    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }
    val isFt = targetStatus == DayStatus.FT
    val isSwapWork = targetStatus == DayStatus.SWAP_WORK

    val title = when (targetStatus) {
        DayStatus.FT -> "Marcar Folga Trabalhada (FT)"
        DayStatus.SWAP_WORK -> "Trocar Folga por Plantão"
        else -> "Trocar Plantão por Folga"
    }
    val actionColor = when (targetStatus) {
        DayStatus.FT -> FtAmber
        DayStatus.SWAP_WORK -> TrocaWorkCyan
        else -> TrocaPurple
    }

    // Status evaluation for the compensation/repayment date
    val paybackStatus = paybackDate?.let {
        ScheduleEngine.calculateEffectiveStatus(it, config, overrides[it], vacations)
    }

    // Conflict detection:
    // If isSwapWork: I'm working on my day off; the partner should work for me on my regular work day!
    // So conflict occurs if paybackDate is already an OFF or VACATION day.
    // If normal SWAP: I'm taking a day off from work; I should work for the partner on my regular day off!
    // So conflict occurs if paybackDate is already a WORK day.
    val hasConflict = when {
        paybackStatus == null -> false
        isSwapWork -> paybackStatus == DayStatus.OFF || paybackStatus == DayStatus.VACATION
        else -> paybackStatus == DayStatus.WORK || paybackStatus == DayStatus.FT || paybackStatus == DayStatus.SWAP_WORK
    }

    val openDatePicker = {
        val initial = paybackDate ?: (if (isSwapWork) ScheduleEngine.findNextWorkDay(date.plusDays(1), config, overrides, vacations) else ScheduleEngine.findNextOffDay(date, config, overrides, vacations))
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                paybackDate = LocalDate.of(year, month + 1, dayOfMonth)
                confirmDespiteConflict = false
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        )
        dpd.show()
    }

    val canConfirm = if (isFt) {
        true
    } else {
        partner.isNotBlank() && (!hasConflict || confirmDespiteConflict)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(actionColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFt) Icons.Default.Event else Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = actionColor,
                        fontWeight = FontWeight.SemiBold
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
                if (isFt) {
                    Text(
                        text = "Você marcará este dia que originalmente era de folga como trabalhado (FT/hora extra). Isso será contabilizado no seu relatório mensal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = valueOrLocation,
                        onValueChange = { valueOrLocation = it },
                        label = { Text("Posto / Valor da Diária (Opcional)") },
                        placeholder = { Text("Ex: Posto Central - R$ 250,00") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ft_value_input"),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = if (isSwapWork) {
                            "Você vai cobrir o plantão de um colega no seu dia de folga (plantão atípico). Indique o colega e selecione quando ele devolverá o plantão trabalhando para você."
                        } else {
                            "Registre com qual colega você fez a permuta de plantão e agende o dia para pagar o plantão no calendário."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = partner,
                        onValueChange = { partner = it },
                        label = { Text("Nome do Colega de Troca *") },
                        placeholder = { Text("Ex: Cabo Ribeiro, Vigilante Souza, Enf. Paula") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("swap_partner_input"),
                        singleLine = true
                    )

                    // Section: Seletor de Data de Pagamento / Compensação
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = actionColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSwapWork) "Quando o Colega Trabalhará para Você" else "Dia a Pagar ao Colega",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                if (paybackDate != null) {
                                    IconButton(
                                        onClick = {
                                            paybackDate = null
                                            confirmDespiteConflict = false
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Limpar data",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (paybackDate == null) {
                                Text(
                                    text = if (isSwapWork) {
                                        "Escolha a data em que você teria PLANTÃO para o colega trabalhar no seu lugar (este dia virará folga para você)."
                                    } else {
                                        "Nenhuma data definida ainda. Escolha no calendário para o app avisar se coincide com folga ou trabalho."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { openDatePicker() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pick_payback_date_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Escolher Data no Calendário", fontWeight = FontWeight.SemiBold)
                                }

                                // Quick presets
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSwapWork) {
                                        val nextWork = remember(date, config) {
                                            ScheduleEngine.findNextWorkDay(date.plusDays(1), config, overrides, vacations)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                paybackDate = nextWork
                                                confirmDespiteConflict = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                        ) {
                                            Text("Próx. Plantão (${nextWork.format(DateTimeFormatter.ofPattern("dd/MM"))})", fontSize = 11.sp)
                                        }
                                    } else {
                                        val nextOff = remember(date, config) {
                                            ScheduleEngine.findNextOffDay(date, config, overrides, vacations)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                paybackDate = nextOff
                                                confirmDespiteConflict = false
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                        ) {
                                            Text("Próx. Folga (${nextOff.format(DateTimeFormatter.ofPattern("dd/MM"))})", fontSize = 11.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            paybackDate = date.plusDays(7)
                                            confirmDespiteConflict = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                                    ) {
                                        Text("+7 dias", fontSize = 11.sp)
                                    }
                                }
                            } else {
                                // Date is selected: Display date card
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { openDatePicker() },
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, actionColor.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = paybackDate!!.format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Toque para alterar a data",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = actionColor)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Real-time conflict feedback
                                if (isSwapWork) {
                                    // I'm working today; colega should cover me when I have a shift!
                                    when (paybackStatus) {
                                        DayStatus.WORK, DayStatus.FT -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = FolgaGreen.copy(alpha = 0.12f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, FolgaGreen)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = FolgaGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Plantão na sua Escala (Ideal)!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = FolgaGreen
                                                        )
                                                        Text(
                                                            text = "Neste dia você trabalharia. O colega cobrirá para você e este dia virará sua folga por troca!",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        DayStatus.OFF -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Warning,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Atenção: Você já estaria de Folga!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Nesta data sua escala já é de folga. Para compensar o dia trabalhado, escolha um dia em que você teria plantão.",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { confirmDespiteConflict = !confirmDespiteConflict }
                                                    ) {
                                                        Checkbox(
                                                            checked = confirmDespiteConflict,
                                                            onCheckedChange = { confirmDespiteConflict = it },
                                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error),
                                                            modifier = Modifier.testTag("confirm_conflict_checkbox")
                                                        )
                                                        Text(
                                                            text = "Confirmar esta data mesmo assim",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        DayStatus.VACATION -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = VacationTeal.copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, VacationTeal)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.BeachAccess,
                                                        contentDescription = null,
                                                        tint = VacationTeal,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Período de Férias!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = VacationTeal
                                                        )
                                                        Text(
                                                            text = "Nesta data você já estará de férias.",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                } else {
                                    // Normal swap: I'm off today; I will pay back on my free day
                                    when (paybackStatus) {
                                        DayStatus.WORK, DayStatus.FT -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Warning,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Atenção: Dia de Trabalho!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Nesta data você já estará trabalhando (Plantão na sua escala). Normalmente não seria possível pagar o plantão pois você já tem serviço.",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { confirmDespiteConflict = !confirmDespiteConflict }
                                                    ) {
                                                        Checkbox(
                                                            checked = confirmDespiteConflict,
                                                            onCheckedChange = { confirmDespiteConflict = it },
                                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error),
                                                            modifier = Modifier.testTag("confirm_conflict_checkbox")
                                                        )
                                                        Text(
                                                            text = "Desejo mesmo assim confirmar esta data para troca (Dobra/Autorizado)",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        DayStatus.OFF -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = FolgaGreen.copy(alpha = 0.12f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, FolgaGreen)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = FolgaGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Folga Livre na sua Escala!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = FolgaGreen
                                                        )
                                                        Text(
                                                            text = "Você estará de folga neste dia. Data ideal para pagar o plantão.",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        DayStatus.VACATION -> {
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                color = VacationTeal.copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, VacationTeal)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.BeachAccess,
                                                        contentDescription = null,
                                                        tint = VacationTeal,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "Período de Férias!",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = VacationTeal
                                                        )
                                                        Text(
                                                            text = "Nesta data você estará no seu período de férias agendado.",
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    placeholder = { Text("Turno, setor, autorização da chefia...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("override_notes_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        partner.trim().ifBlank { null },
                        valueOrLocation.trim().ifBlank { null },
                        notes.trim().ifBlank { null },
                        paybackDate
                    )
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                modifier = Modifier.testTag("confirm_override_button")
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

package com.example.ui.dialogs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStatus
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDialog(
    date: LocalDate,
    dayStatus: DayStatus,
    onDismiss: () -> Unit,
    onSave: (title: String, time: LocalTime?, notes: String?, reminderMinutes: Int, isShiftSwap: Boolean, partnerName: String?) -> Unit,
    onRequestSwapFromCollision: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var hourString by remember { mutableStateOf("14:00") }
    var notes by remember { mutableStateOf("") }
    var reminderMinutes by remember { mutableStateOf(60) } // default 1 hour before
    var isShiftSwap by remember { mutableStateOf(false) }
    var swapPartner by remember { mutableStateOf("") }

    val formattedDate = date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale("pt", "BR")))

    val reminderOptions = listOf(
        0 to "No horário do evento",
        15 to "15 minutos antes",
        30 to "30 minutos antes",
        60 to "1 hora antes",
        120 to "2 horas antes",
        1440 to "1 dia antes (véspera)"
    )
    var reminderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Novo Compromisso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Shift Collision Warning Banner
                if (dayStatus == DayStatus.WORK) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FtAmber.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alerta de Plantão",
                                    tint = FtAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conflito: Dia de Plantão!",
                                    fontWeight = FontWeight.Bold,
                                    color = FtAmber,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Você está escalado para trabalhar neste dia. Deseja registrar este compromisso como uma Troca de Plantão?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    isShiftSwap = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("convert_to_swap_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TrocaPurple)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vincular a Troca de Plantão", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Compromisso *") },
                    placeholder = { Text("Ex: Consulta médica, Viagem, Churrasco") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_title_input"),
                    singleLine = true
                )

                // Time input
                OutlinedTextField(
                    value = hourString,
                    onValueChange = { hourString = it },
                    label = { Text("Horário (HH:mm)") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_time_input"),
                    singleLine = true
                )

                // Shift swap partner (if toggled)
                if (isShiftSwap) {
                    OutlinedTextField(
                        value = swapPartner,
                        onValueChange = { swapPartner = it },
                        label = { Text("Nome do Colega que vai cobrir") },
                        placeholder = { Text("Ex: Sgt. Silva, Vigilante Marcos") },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = TrocaPurple) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appointment_swap_partner_input"),
                        singleLine = true
                    )
                }

                // Advance Reminder Selector
                ExposedDropdownMenuBox(
                    expanded = reminderExpanded,
                    onExpandedChange = { reminderExpanded = !reminderExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = reminderOptions.find { it.first == reminderMinutes }?.second ?: "1 hora antes",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Lembrete / Alarme") },
                        leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = reminderExpanded,
                        onDismissRequest = { reminderExpanded = false }
                    ) {
                        reminderOptions.forEach { (mins, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    reminderMinutes = mins
                                    reminderExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações / Detalhes") },
                    placeholder = { Text("Endereço, documentos ou anotações...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_notes_input"),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val parsedTime = try {
                            val parts = hourString.trim().split(":")
                            if (parts.size >= 2) {
                                LocalTime.of(parts[0].toInt(), parts[1].toInt())
                            } else null
                        } catch (_: Exception) {
                            null
                        }
                        onSave(
                            title.trim(),
                            parsedTime,
                            notes.trim().ifBlank { null },
                            reminderMinutes,
                            isShiftSwap,
                            swapPartner.trim().ifBlank { null }
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_appointment_button")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

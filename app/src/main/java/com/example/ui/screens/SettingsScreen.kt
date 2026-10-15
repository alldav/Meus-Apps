package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppThemeMode
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.notification.NotificationHelper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.example.data.update.UpdateCheckState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.ScheduleEngine
import com.example.ui.components.WeekCycleCalendarSelector
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.PlantaoBlue
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState,
    onOpenWizard: () -> Unit,
    onSaved: () -> Unit
) {
    val config = uiState.config

    var userName by remember(config) { mutableStateOf(config.userName) }
    var selectedType by remember(config) { mutableStateOf(config.scheduleType) }
    var refDate by remember(config) { mutableStateOf(config.referenceDate) }
    var refIsWork by remember(config) { mutableStateOf(config.referenceIsWork) }
    var period by remember(config) { mutableStateOf(config.period) }
    var startTime by remember(config) { mutableStateOf(config.customStartTime) }
    var endTime by remember(config) { mutableStateOf(config.customEndTime) }
    var selectedUf by remember(config) { mutableStateOf(config.selectedUf) }

    // Custom cycle parameters (e.g. 3x2)
    var customWorkCount by remember(config) { mutableIntStateOf(config.workDaysCount) }
    var customOffCount by remember(config) { mutableIntStateOf(config.offDaysCount) }

    // Initial custom pattern array
    var customPatternList by remember(config) {
        val initial = ScheduleEngine.parseCustomPattern(config.customCyclePattern, config.workDaysCount, config.offDaysCount)
        mutableStateOf(initial)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var ufDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action: Assistente Passo a Passo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenWizard() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Assistente Passo a Passo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Refaça as perguntas iniciais para recalibrar sua escala rapidamente.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Section: Identificação
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Identificação",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Nome ou como prefere ser chamado") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Section: Tipo de Escala
        Text(
            text = "Tipo de Escala",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val scheduleOptions = listOf(
            ScheduleType.TWELVE_THIRTY_SIX,
            ScheduleType.SIX_ONE,
            ScheduleType.FIVE_TWO,
            ScheduleType.FOUR_TWO,
            ScheduleType.CUSTOM
        )

        scheduleOptions.forEach { type ->
            val isSelected = selectedType == type
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedType = type }
                    .testTag("schedule_type_${type.name}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 2.dp else 0.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = type.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = type.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section: Configuração do Ciclo e Ponto de Partida
        if (selectedType == ScheduleType.TWELVE_THIRTY_SIX) {
            // Section: Ponto de Referência (Data Âncora) para 12x36
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ponto de Partida da Escala (12 por 36)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escolha um dia conhecido para o app calcular o ciclo alternado (dia sim, dia não).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = refDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data de Referência") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Escolher data")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .testTag("ref_date_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Nesta data de referência (${refDate.format(DateTimeFormatter.ofPattern("dd/MM"))}), você estava:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = refIsWork,
                            onClick = { refIsWork = true },
                            label = { Text("Trabalhando (Plantão)", fontWeight = FontWeight.Bold) },
                            leadingIcon = { if (refIsWork) Icon(Icons.Default.Check, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ref_is_work_chip")
                        )

                        FilterChip(
                            selected = !refIsWork,
                            onClick = { refIsWork = false },
                            label = { Text("De Folga", fontWeight = FontWeight.Bold) },
                            leadingIcon = { if (!refIsWork) Icon(Icons.Default.Check, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ref_is_off_chip")
                        )
                    }
                }
            }
        } else {
            // Scales: 4x2, 6x1, 5x2, or Custom -> Use WeekCycleCalendarSelector
            val scaleName = when (selectedType) {
                ScheduleType.SIX_ONE -> "6 por 1"
                ScheduleType.FIVE_TWO -> "5 por 2"
                ScheduleType.FOUR_TWO -> "4 por 2"
                else -> "Personalizada"
            }
            val wCount = when (selectedType) {
                ScheduleType.SIX_ONE -> 6
                ScheduleType.FIVE_TWO -> 5
                ScheduleType.FOUR_TWO -> 4
                else -> customWorkCount
            }
            val oCount = when (selectedType) {
                ScheduleType.SIX_ONE -> 1
                ScheduleType.FIVE_TWO -> 2
                ScheduleType.FOUR_TWO -> 2
                else -> customOffCount
            }
            val cLength = when (selectedType) {
                ScheduleType.SIX_ONE -> 7
                ScheduleType.FIVE_TWO -> 7
                ScheduleType.FOUR_TWO -> 6
                else -> customWorkCount + customOffCount
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("multi_day_cycle_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calendário do Ciclo ($scaleName)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selecione no calendário a semana ou data de início e marque os dias exatos em que você está de plantão ou de folga:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (selectedType == ScheduleType.CUSTOM) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customWorkCount.toString(),
                                onValueChange = {
                                    val n = it.toIntOrNull()?.coerceIn(1, 15) ?: 1
                                    customWorkCount = n
                                    val list = mutableListOf<Boolean>()
                                    repeat(n) { list.add(true) }
                                    repeat(customOffCount) { list.add(false) }
                                    customPatternList = list
                                    refIsWork = list.firstOrNull() ?: true
                                },
                                label = { Text("Dias Trab.") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = customOffCount.toString(),
                                onValueChange = {
                                    val n = it.toIntOrNull()?.coerceIn(1, 15) ?: 1
                                    customOffCount = n
                                    val list = mutableListOf<Boolean>()
                                    repeat(customWorkCount) { list.add(true) }
                                    repeat(n) { list.add(false) }
                                    customPatternList = list
                                    refIsWork = list.firstOrNull() ?: true
                                },
                                label = { Text("Dias Folga") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WeekCycleCalendarSelector(
                        startDate = refDate,
                        onStartDateChange = { refDate = it },
                        cycleLength = cLength,
                        pattern = customPatternList,
                        onPatternChange = {
                            customPatternList = it
                            refIsWork = it.firstOrNull() ?: true
                        },
                        scaleTitle = scaleName,
                        workCount = wCount,
                        offCount = oCount
                    )
                }
            }
        }

        // Section: Turno e Horários (Diurno, Noturno, Misto, Personalizado)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Horários e Turno de Trabalho",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Troque o horário padrão ou digite horários personalizados:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                val periodList = listOf(
                    ShiftPeriod.DAY to "Diurno (07:00 às 19:00)",
                    ShiftPeriod.DAY_EARLY to "Diurno Cedo (06:00 às 18:00)",
                    ShiftPeriod.NIGHT to "Noturno (19:00 às 07:00)",
                    ShiftPeriod.NIGHT_EARLY to "Noturno Cedo (18:00 às 06:00)",
                    ShiftPeriod.FULL_DAY to "Plantão 24 Horas",
                    ShiftPeriod.MIXED to "Misto (2 Diurnos / 2 Noturnos)",
                    ShiftPeriod.CUSTOM_HOURS to "Personalizado (Entrada / Saída)"
                )

                periodList.forEach { (p, label) ->
                    val isSel = period == p
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                period = p
                                when (p) {
                                    ShiftPeriod.DAY -> { startTime = "07:00"; endTime = "19:00" }
                                    ShiftPeriod.DAY_EARLY -> { startTime = "06:00"; endTime = "18:00" }
                                    ShiftPeriod.NIGHT -> { startTime = "19:00"; endTime = "07:00" }
                                    ShiftPeriod.NIGHT_EARLY -> { startTime = "18:00"; endTime = "06:00" }
                                    ShiftPeriod.FULL_DAY -> { startTime = "07:00"; endTime = "07:00" }
                                    else -> {}
                                }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = label, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                if (period == ShiftPeriod.CUSTOM_HOURS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Início (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Fim (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Section: Seleção de Estado (UF) para Feriados Regionais
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Estado / UF para Feriados Regionais",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Carrega os feriados estaduais específicos da sua região no Brasil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = ufDropdownExpanded,
                    onExpandedChange = { ufDropdownExpanded = !ufDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val ufName = BrazilianHolidayProvider.BRAZILIAN_STATES.find { it.first == selectedUf }?.second ?: selectedUf
                    OutlinedTextField(
                        value = "$selectedUf - $ufName",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecione o Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ufDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("uf_selector_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = ufDropdownExpanded,
                        onDismissRequest = { ufDropdownExpanded = false }
                    ) {
                        BrazilianHolidayProvider.BRAZILIAN_STATES.forEach { (uf, name) ->
                            DropdownMenuItem(
                                text = { Text("$uf - $name") },
                                onClick = {
                                    selectedUf = uf
                                    ufDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val customPatternString = if (selectedType == ScheduleType.CUSTOM || selectedType == ScheduleType.SIX_ONE || selectedType == ScheduleType.FIVE_TWO || selectedType == ScheduleType.FOUR_TWO) {
                    customPatternList.joinToString(",") { if (it) "1" else "0" }
                } else "1,0"

                viewModel.saveScheduleConfig(
                    userName = userName.trim().ifBlank { "Plantonista" },
                    type = selectedType,
                    refDate = refDate,
                    refIsWork = if (selectedType == ScheduleType.TWELVE_THIRTY_SIX) refIsWork else (customPatternList.firstOrNull() ?: true),
                    period = period,
                    startTime = startTime.trim().ifBlank { "07:00" },
                    endTime = endTime.trim().ifBlank { "19:00" },
                    uf = selectedUf,
                    workCount = customWorkCount,
                    offCount = customOffCount,
                    customPattern = customPatternString
                )
                onSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_schedule_config_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salvar Configurações da Escala", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        // Section: Atualizações do Aplicativo via Internet
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_updates_section_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Atualizações do Aplicativo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Versão Atual: v${viewModel.updateManager.currentVersionName} (Build ${viewModel.updateManager.currentVersionCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status message based on current check state
                when (val updateState = uiState.updateCheckState) {
                    is UpdateCheckState.Checking -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Buscando novas versões na internet...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    is UpdateCheckState.Available -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FolgaGreen.copy(alpha = 0.15f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = FolgaGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nova versão v${updateState.release.versionName} disponível!",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FolgaGreen
                                )
                                Text(
                                    text = "Toque abaixo para abrir o instalador.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is UpdateCheckState.UpToDate -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FolgaGreen.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = FolgaGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Você está com a versão mais recente instalada!",
                                style = MaterialTheme.typography.bodySmall,
                                color = FolgaGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    is UpdateCheckState.Error -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = updateState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    UpdateCheckState.Idle -> Unit
                }

                // Check Now Button
                Button(
                    onClick = { viewModel.checkForUpdates(showDialogIfAvailable = true, silentOnUpToDate = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("check_updates_now_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificar Atualizações Agora", fontWeight = FontWeight.SemiBold)
                }

                // Test / Simulation Button
                OutlinedButton(
                    onClick = { viewModel.simulateUpdateForTesting() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_update_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testar Tela de Atualização (Simulação)")
                }

                // Auto-check Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Checagem automática",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Avisa ao abrir o app sempre que uma nova versão for publicada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isAutoCheckEnabled,
                        onCheckedChange = { viewModel.setAutoCheckEnabled(it) },
                        modifier = Modifier.testTag("auto_check_updates_switch")
                    )
                }

                // Safe update educational tip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Como funcionam as atualizações:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ao atualizar o aplicativo por um novo APK, o sistema Android instala a nova versão preservando 100% dos seus dados (escalas, histórico de plantões, trocas FT e férias continuam salvos).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Aparência & Tema (Dark & Natural)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_settings_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Aparência",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aparência & Tema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Alterne entre o tema Dark e o tema Natural",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionItem(
                        title = "Dark",
                        subtitle = "Noturno",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentTheme == AppThemeMode.DARK,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(AppThemeMode.DARK) }
                    )

                    ThemeOptionItem(
                        title = "Natural",
                        subtitle = "Claro",
                        icon = Icons.Default.LightMode,
                        isSelected = currentTheme == AppThemeMode.NATURAL,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(AppThemeMode.NATURAL) }
                    )

                    ThemeOptionItem(
                        title = "Sistema",
                        subtitle = "Auto",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = currentTheme == AppThemeMode.SYSTEM,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (currentTheme) {
                        AppThemeMode.DARK -> "🌙 Tema Dark: fundo escuro tático, alto contraste e economia de bateria."
                        AppThemeMode.NATURAL -> "☀️ Tema Natural: tons claros orgânicos e leitura confortável para o dia."
                        AppThemeMode.SYSTEM -> "⚙️ Sistema: segue automaticamente o modo claro/escuro do seu celular."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card: Notificações & Alarmes do Sistema
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_settings_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            val context = LocalContext.current
            val hasPermission = NotificationHelper.hasNotificationPermission(context)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notificações",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Notificações & Lembretes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasPermission) FolgaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasPermission) Icons.Default.CheckCircle else Icons.Default.Alarm,
                        contentDescription = null,
                        tint = if (hasPermission) FolgaGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPermission) "Notificações Ativas no Android" else "Permissão de Notificação Pendente",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasPermission) FolgaGreen else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Esquemas de Notificação do App:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🔔", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compromissos: Alarme sonoro e vibração no horário marcado ou com antecedência (15m, 30m, 1h, 1 dia).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🔄", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Trocas de Plantão: Alerta às 08:30 da manhã na data combinada de devolução do plantão com o parceiro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.Top) {
                        Text("⏳", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retorno da Folga: Card de aviso destacado na tela inicial na véspera do seu retorno ao plantão.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.sendTestNotification()
                        Toast.makeText(context, "Notificação de teste enviada!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_notification_button")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disparar Notificação de Teste")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Material 3 DatePickerDialog for Reference Date
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = refDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        refDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}


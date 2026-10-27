package com.example.ui.dialogs

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import com.example.domain.ProfessionSpecialDayProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import com.example.domain.BrazilianHolidayProvider
import com.example.ui.components.WeekCycleCalendarSelector
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.PlantaoBlue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingWizardDialog(
    initialName: String = "",
    initialProfession: String = "",
    onDismiss: () -> Unit,
    onSkipWithDefault: () -> Unit = onDismiss,
    onComplete: (
        name: String,
        type: ScheduleType,
        refDate: LocalDate,
        refIsWork: Boolean,
        period: ShiftPeriod,
        startTime: String,
        endTime: String,
        uf: String,
        workCount: Int,
        offCount: Int,
        customPattern: String,
        profession: String
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Nome & Profissão, 2: Escala, 3: Ciclo & Calendário, 4: Horários, 5: UF

    var userName by remember { mutableStateOf(if (initialName.isNotBlank() && initialName != "Plantonista") initialName else "") }
    var profession by remember { mutableStateOf(if (initialProfession.isNotBlank() && initialProfession != "Geral") initialProfession else "") }
    var professionDropdownExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(ScheduleType.TWELVE_THIRTY_SIX) }
    var refDate by remember { mutableStateOf(LocalDate.now()) }
    var refIsWork by remember { mutableStateOf(true) }

    // Period & custom hours
    var selectedPeriod by remember { mutableStateOf(ShiftPeriod.DAY) }
    var startTime by remember { mutableStateOf("07:00") }
    var endTime by remember { mutableStateOf("19:00") }

    var selectedUf by remember { mutableStateOf("SP") }
    var ufDropdownExpanded by remember { mutableStateOf(false) }

    var customWorkCount by remember { mutableIntStateOf(3) }
    var customOffCount by remember { mutableIntStateOf(2) }
    var customPatternList by remember {
        mutableStateOf(listOf(true, true, true, false, false)) // 3x2 default
    }

    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Modal Wizard requires completion or explicit action */ },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .testTag("onboarding_wizard_dialog"),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Entendendo sua Escala",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Etapa $step de 5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..5) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                when (step) {
                    // ETAPA 1: NOME / COMO GOSTARIA DE SER CHAMADO
                    1 -> {
                        Text(
                            text = "Bem-vindo! Vamos calibrar seus plantões e folgas 👋",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Primeira coisa: qual é o seu nome ou como você gostaria de ser chamado?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Seu Nome ou Apelido") },
                            placeholder = { Text("Ex: Rogério, Sgt. Silva, Carlos") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = professionDropdownExpanded,
                            onExpandedChange = { professionDropdownExpanded = !professionDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = profession,
                                onValueChange = { profession = it },
                                label = { Text("Sua Profissão (Enfermagem, Vigilante, Médico...)") },
                                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = professionDropdownExpanded,
                                onDismissRequest = { professionDropdownExpanded = false }
                            ) {
                                ProfessionSpecialDayProvider.RECOGNIZED_PROFESSIONS.forEach { prof ->
                                    DropdownMenuItem(
                                        text = { Text(prof) },
                                        onClick = {
                                            if (prof.contains("Outra profissão")) {
                                                profession = ""
                                            } else {
                                                profession = prof
                                            }
                                            professionDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Você pode escolher uma das profissões sugeridas acima ou digitar livremente a sua profissão.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSkipWithDefault() }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Quer começar rápido?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Pular e usar escala padrão 12x36 diurno", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("Pular", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                        }
                    }

                    // ETAPA 2: QUAL A SUA ESCALA?
                    2 -> {
                        Text(
                            text = if (userName.isNotBlank()) "Legal, $userName! Qual a sua escala?" else "Qual a sua escala de trabalho?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Escolha o formato em que você está escalado atualmente:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val scaleList = listOf(
                            ScheduleType.TWELVE_THIRTY_SIX to "12 por 36" to "Dia sim, dia não direto (Plantão de 12h e 36h de folga)",
                            ScheduleType.SIX_ONE to "6 por 1" to "Trabalha 6 dias seguidos e folga 1 dia",
                            ScheduleType.FIVE_TWO to "5 por 2" to "Trabalha 5 dias seguidos e folga 2 dias",
                            ScheduleType.FOUR_TWO to "4 por 2" to "Trabalha 4 dias seguidos e folga 2 dias",
                            ScheduleType.CUSTOM to "Escala Personalizada" to "Defina sua proporção (ex: 3 por 2, 2 por 2, etc.)"
                        )

                        scaleList.forEach { (pair, desc) ->
                            val (type, title) = pair
                            val isSel = selectedType == type
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedType = type
                                        if (type == ScheduleType.SIX_ONE) {
                                            customWorkCount = 6
                                            customOffCount = 1
                                            customPatternList = listOf(true, true, true, true, true, true, false)
                                        } else if (type == ScheduleType.FIVE_TWO) {
                                            customWorkCount = 5
                                            customOffCount = 2
                                            customPatternList = listOf(true, true, true, true, true, false, false)
                                        } else if (type == ScheduleType.FOUR_TWO) {
                                            customWorkCount = 4
                                            customOffCount = 2
                                            customPatternList = listOf(true, true, true, true, false, false)
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
                                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ETAPA 3: MARCAR DIAS DE TRABALHO E FOLGA NO CALENDÁRIO
                    3 -> {
                        when (selectedType) {
                            ScheduleType.TWELVE_THIRTY_SIX -> {
                                Text(
                                    text = "Sincronizando seu 12 por 36 📅",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No 12 por 36, o ciclo é de 2 dias (dia sim, dia não). Selecione no calendário a data de referência:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                // Date selector
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Data no Calendário:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = refDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Neste dia ${refDate.format(DateTimeFormatter.ofPattern("dd/MM"))}, você estará:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = refIsWork,
                                        onClick = { refIsWork = true },
                                        label = { Text("Plantão (Trabalhando)", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { if (refIsWork) Icon(Icons.Default.Check, contentDescription = null) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = !refIsWork,
                                        onClick = { refIsWork = false },
                                        label = { Text("Folga (Descanso)", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { if (!refIsWork) Icon(Icons.Default.Check, contentDescription = null) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Visual 2-day sequence preview
                                Text("Confirmação dos 2 dias do seu ciclo:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val day1Date = refDate
                                    val day2Date = refDate.plusDays(1)
                                    val isDay1Work = refIsWork
                                    val isDay2Work = !refIsWork

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = if (isDay1Work) PlantaoBlue.copy(alpha = 0.15f) else FolgaGreen.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isDay1Work) PlantaoBlue else FolgaGreen)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(day1Date.format(DateTimeFormatter.ofPattern("dd/MM (EEE)", Locale("pt", "BR"))).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(if (isDay1Work) "PLANTÃO" else "FOLGA", fontWeight = FontWeight.ExtraBold, color = if (isDay1Work) PlantaoBlue else FolgaGreen, fontSize = 14.sp)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = if (isDay2Work) PlantaoBlue.copy(alpha = 0.15f) else FolgaGreen.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isDay2Work) PlantaoBlue else FolgaGreen)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(day2Date.format(DateTimeFormatter.ofPattern("dd/MM (EEE)", Locale("pt", "BR"))).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(if (isDay2Work) "PLANTÃO" else "FOLGA", fontWeight = FontWeight.ExtraBold, color = if (isDay2Work) PlantaoBlue else FolgaGreen, fontSize = 14.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Tudo certo! A partir deste ponto, o aplicativo entende todo o seu futuro dia sim, dia não.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            ScheduleType.SIX_ONE, ScheduleType.FIVE_TWO, ScheduleType.FOUR_TWO, ScheduleType.CUSTOM -> {
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

                                Text(
                                    text = "Sincronizando escala $scaleName 📅",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Confira a semana atual ou a data de início da sua escala. Toque em qualquer dia para indicar onde você está no ciclo (Plantão ou Folga):",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (selectedType == ScheduleType.CUSTOM) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                    // ETAPA 4: PERSONALIZAÇÃO DE HORÁRIOS (DIURNO, NOTURNO, MISTO, PERSONALIZADO)
                    4 -> {
                        Text(
                            text = "Horário do seu Plantão ⏰",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Escolha seu turno de trabalho ou personalize o horário exato de entrada e saída:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val periodOptions = listOf(
                            ShiftPeriod.DAY to "Diurno Padrão" to "07:00 às 19:00",
                            ShiftPeriod.DAY_EARLY to "Diurno Cedo" to "06:00 às 18:00",
                            ShiftPeriod.NIGHT to "Noturno Padrão" to "19:00 às 07:00",
                            ShiftPeriod.NIGHT_EARLY to "Noturno Cedo" to "18:00 às 06:00",
                            ShiftPeriod.FULL_DAY to "Plantão 24 Horas" to "24 Horas Ininterruptas",
                            ShiftPeriod.MIXED to "Misto / Revezamento" to "2 dias Diurno (07-19h) / 2 dias Noturno (19-07h)",
                            ShiftPeriod.CUSTOM_HOURS to "Outro Horário Personalizado" to "Digite seu horário de entrada e saída"
                        )

                        periodOptions.forEach { (pair, desc) ->
                            val (p, label) = pair
                            val isSel = selectedPeriod == p
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedPeriod = p
                                        when (p) {
                                            ShiftPeriod.DAY -> { startTime = "07:00"; endTime = "19:00" }
                                            ShiftPeriod.DAY_EARLY -> { startTime = "06:00"; endTime = "18:00" }
                                            ShiftPeriod.NIGHT -> { startTime = "19:00"; endTime = "07:00" }
                                            ShiftPeriod.NIGHT_EARLY -> { startTime = "18:00"; endTime = "06:00" }
                                            ShiftPeriod.FULL_DAY -> { startTime = "07:00"; endTime = "07:00" }
                                            else -> {}
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = label, fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (selectedPeriod == ShiftPeriod.CUSTOM_HOURS) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Defina as horas do seu plantão:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = startTime,
                                    onValueChange = { startTime = it },
                                    label = { Text("Entrada (ex: 06:30)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = endTime,
                                    onValueChange = { endTime = it },
                                    label = { Text("Saída (ex: 18:30)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // ETAPA 5: FERIADOS REGIONAIS DO BRASIL (UF)
                    5 -> {
                        Text(
                            text = "Feriados do seu Estado 🇧🇷",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Além de todos os feriados nacionais do Brasil (como Natal, Ano Novo, Tiradentes), selecione sua UF para carregar também os feriados estaduais:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

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
                                label = { Text("Selecione seu Estado") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ufDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
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

                        Spacer(modifier = Modifier.height(18.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "🎉 Pronto, ${if (userName.isNotBlank()) userName else "Plantonista"}! Sua escala ficará perfeitamente calibrada com os cálculos automáticos de folgas, plantões, horários e feriados.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step < 5) {
                Button(
                    onClick = { step++ },
                    modifier = Modifier.testTag("wizard_next_button")
                ) {
                    Text("Avançar")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = {
                        val patternString = if (selectedType == ScheduleType.CUSTOM || selectedType == ScheduleType.SIX_ONE || selectedType == ScheduleType.FIVE_TWO || selectedType == ScheduleType.FOUR_TWO) {
                            customPatternList.joinToString(",") { if (it) "1" else "0" }
                        } else "1,0"

                        onComplete(
                            if (userName.isBlank()) "Plantonista" else userName.trim(),
                            selectedType,
                            refDate,
                            if (selectedType == ScheduleType.TWELVE_THIRTY_SIX) refIsWork else (customPatternList.firstOrNull() ?: true),
                            selectedPeriod,
                            startTime,
                            endTime,
                            selectedUf,
                            customWorkCount,
                            customOffCount,
                            patternString,
                            profession
                        )
                        onDismiss()
                    },
                    modifier = Modifier.testTag("wizard_finish_button")
                ) {
                    Text("Concluir e Ver Escala", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Voltar")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Configurar Depois")
                }
            }
        }
    )

    // Reference Date Picker
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
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

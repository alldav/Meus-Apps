package com.example.data.model

import java.time.LocalDate
import java.time.LocalTime

enum class ScheduleType(val title: String, val description: String) {
    TWELVE_THIRTY_SIX("12x36", "Dia sim, dia não (1 dia trabalho / 1 dia folga)"),
    SIX_ONE("6x1", "Trabalha 6 dias seguidos e folga 1 dia"),
    FIVE_TWO("5x2", "Trabalha 5 dias seguidos e folga 2 dias"),
    FOUR_TWO("4x2", "Trabalha 4 dias e folga 2 dias seguidos"),
    CUSTOM("Personalizada", "Defina a sua proporção (ex: 3x2, 2x2) e marque o ciclo inicial")
}

enum class DayStatus(val label: String) {
    WORK("Plantão"),
    OFF("Folga"),
    FT("Folga Trabalhada (FT)"),
    SWAP("Troca de Plantão"),
    VACATION("Férias")
}

enum class ShiftPeriod(val label: String, val defaultHours: String) {
    DAY("Diurno (07h às 19h)", "07:00 às 19:00"),
    NIGHT("Noturno (19h às 07h)", "19:00 às 07:00"),
    DAY_EARLY("Diurno (06h às 18h)", "06:00 às 18:00"),
    NIGHT_EARLY("Noturno (18h às 06h)", "18:00 às 06:00"),
    FULL_DAY("24 Horas", "Plantão 24h"),
    CUSTOM_HOURS("Horário Personalizado", "Personalizado"),
    MIXED("Misto (Diurno / Noturno)", "Alternado")
}

data class BrazilianHoliday(
    val date: LocalDate,
    val name: String,
    val isNational: Boolean,
    val stateUf: String? = null
)

data class MonthSummary(
    val yearMonth: java.time.YearMonth,
    val totalWorkDays: Int,
    val totalOffDays: Int,
    val totalFtDays: Int,
    val totalSwapDays: Int,
    val totalVacationDays: Int,
    val weekendOffDays: Int
)

data class QueryResult(
    val targetDate: LocalDate,
    val status: DayStatus,
    val periodLabel: String,
    val holiday: BrazilianHoliday?,
    val isHoliday: Boolean,
    val message: String
)

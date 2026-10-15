package com.example.domain

import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import com.example.data.model.DayStatus
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object ScheduleEngine {

    /**
     * Resolves the effective status for a date, factoring in Vacations and DayOverrides (FT or Swap).
     */
    fun calculateEffectiveStatus(
        date: LocalDate,
        config: ScheduleConfigEntity,
        override: DayOverrideEntity?,
        vacations: List<VacationPeriodEntity> = emptyList()
    ): DayStatus {
        // Priority 1: Vacation period takes precedence over normal schedule
        val isVacation = vacations.any { !date.isBefore(it.startDate) && !date.isAfter(it.endDate) }
        if (isVacation) {
            return DayStatus.VACATION
        }

        // Priority 2: Manual override (FT or Swap)
        if (override != null) {
            return override.status
        }

        // Priority 3: Baseline mathematical calculation
        return calculateBaseStatus(date, config)
    }

    /**
     * Calculates the baseline status (WORK or OFF) from the mathematical scale cycle.
     */
    fun calculateBaseStatus(
        date: LocalDate,
        config: ScheduleConfigEntity
    ): DayStatus {
        val daysDiff = ChronoUnit.DAYS.between(config.referenceDate, date)

        val cyclePattern: List<Boolean> = when (config.scheduleType) {
            ScheduleType.TWELVE_THIRTY_SIX -> {
                if (config.referenceIsWork) listOf(true, false) else listOf(false, true)
            }
            ScheduleType.SIX_ONE -> {
                if (config.customCyclePattern.isNotBlank()) {
                    parseCustomPattern(config.customCyclePattern, 6, 1)
                } else {
                    generatePattern(6, 1, config.referenceIsWork)
                }
            }
            ScheduleType.FIVE_TWO -> {
                if (config.customCyclePattern.isNotBlank()) {
                    parseCustomPattern(config.customCyclePattern, 5, 2)
                } else {
                    generatePattern(5, 2, config.referenceIsWork)
                }
            }
            ScheduleType.FOUR_TWO -> {
                if (config.customCyclePattern.isNotBlank()) {
                    parseCustomPattern(config.customCyclePattern, 4, 2)
                } else {
                    generatePattern(4, 2, config.referenceIsWork)
                }
            }
            ScheduleType.CUSTOM -> {
                parseCustomPattern(config.customCyclePattern, config.workDaysCount, config.offDaysCount)
            }
        }

        val cycleLength = cyclePattern.size.coerceAtLeast(1)
        val modIndex = (((daysDiff % cycleLength) + cycleLength) % cycleLength).toInt()
        val isWork = cyclePattern[modIndex]

        return if (isWork) DayStatus.WORK else DayStatus.OFF
    }

    /**
     * Returns formatted shift hours for a date based on configuration.
     */
    fun getShiftHours(date: LocalDate, config: ScheduleConfigEntity): String {
        return when (config.period) {
            ShiftPeriod.CUSTOM_HOURS -> "${config.customStartTime} às ${config.customEndTime}"
            ShiftPeriod.DAY_EARLY -> "06:00 às 18:00"
            ShiftPeriod.NIGHT_EARLY -> "18:00 às 06:00"
            ShiftPeriod.DAY -> "07:00 às 19:00"
            ShiftPeriod.NIGHT -> "19:00 às 07:00"
            ShiftPeriod.FULL_DAY -> "Plantão 24h"
            ShiftPeriod.MIXED -> {
                // Alternating mixed shifts (e.g. 2 days diurnal, 2 days nocturnal)
                val daysDiff = ChronoUnit.DAYS.between(config.referenceDate, date)
                val isFirstHalf = (((daysDiff / 2) % 2 + 2) % 2) == 0L
                if (isFirstHalf) "07:00 às 19:00 (Diurno)" else "19:00 às 07:00 (Noturno)"
            }
        }
    }

    private fun generatePattern(workDays: Int, offDays: Int, referenceIsWork: Boolean): List<Boolean> {
        val list = mutableListOf<Boolean>()
        if (referenceIsWork) {
            repeat(workDays) { list.add(true) }
            repeat(offDays) { list.add(false) }
        } else {
            repeat(offDays) { list.add(false) }
            repeat(workDays) { list.add(true) }
        }
        return list
    }

    fun parseCustomPattern(patternString: String, fallbackWork: Int, fallbackOff: Int): List<Boolean> {
        if (patternString.isNotBlank()) {
            val parts = patternString.split(",").mapNotNull { it.trim().toIntOrNull() }
            if (parts.isNotEmpty()) {
                return parts.map { it == 1 }
            }
        }
        val list = mutableListOf<Boolean>()
        repeat(fallbackWork.coerceAtLeast(1)) { list.add(true) }
        repeat(fallbackOff.coerceAtLeast(1)) { list.add(false) }
        return list
    }

    /**
     * Finds the next upcoming OFF day starting after the given date.
     */
    fun findNextOffDay(
        startDate: LocalDate,
        config: ScheduleConfigEntity,
        overrides: Map<LocalDate, DayOverrideEntity>,
        vacations: List<VacationPeriodEntity> = emptyList()
    ): LocalDate {
        var cursor = startDate.plusDays(1)
        var count = 0
        while (count < 60) {
            val status = calculateEffectiveStatus(cursor, config, overrides[cursor], vacations)
            if (status == DayStatus.OFF || status == DayStatus.VACATION) {
                return cursor
            }
            cursor = cursor.plusDays(1)
            count++
        }
        return startDate.plusDays(1)
    }

    /**
     * Finds the next consecutive weekend off (both Saturday and Sunday off or on vacation).
     */
    fun findNextWeekendOff(
        startDate: LocalDate,
        config: ScheduleConfigEntity,
        overrides: Map<LocalDate, DayOverrideEntity>,
        vacations: List<VacationPeriodEntity> = emptyList()
    ): Pair<LocalDate, LocalDate>? {
        var cursor = startDate
        var attempts = 0
        while (attempts < 52) { // up to 1 year
            cursor = cursor.plusDays(1)
            if (cursor.dayOfWeek == java.time.DayOfWeek.SATURDAY) {
                val sunday = cursor.plusDays(1)
                val satStatus = calculateEffectiveStatus(cursor, config, overrides[cursor], vacations)
                val sunStatus = calculateEffectiveStatus(sunday, config, overrides[sunday], vacations)
                if ((satStatus == DayStatus.OFF || satStatus == DayStatus.VACATION) &&
                    (sunStatus == DayStatus.OFF || sunStatus == DayStatus.VACATION)
                ) {
                    return Pair(cursor, sunday)
                }
            }
            attempts++
        }
        return null
    }

    /**
     * Finds the next WORK (plantão) day on or after the given date.
     */
    fun findNextWorkDay(
        startDate: LocalDate,
        config: ScheduleConfigEntity,
        overrides: Map<LocalDate, DayOverrideEntity> = emptyMap(),
        vacations: List<VacationPeriodEntity> = emptyList()
    ): LocalDate {
        var cursor = startDate
        var attempts = 0
        while (attempts < 120) {
            val status = calculateEffectiveStatus(cursor, config, overrides[cursor], vacations)
            if (status == DayStatus.WORK || status == DayStatus.FT) {
                return cursor
            }
            cursor = cursor.plusDays(1)
            attempts++
        }
        return startDate
    }
}

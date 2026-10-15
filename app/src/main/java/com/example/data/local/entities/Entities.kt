package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DayStatus
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "schedule_config")
data class ScheduleConfigEntity(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Plantonista",
    val isConfigured: Boolean = false,
    val scheduleType: ScheduleType = ScheduleType.TWELVE_THIRTY_SIX,
    val referenceDate: LocalDate = LocalDate.now(),
    val referenceIsWork: Boolean = true,
    val period: ShiftPeriod = ShiftPeriod.DAY,
    val customStartTime: String = "07:00",
    val customEndTime: String = "19:00",
    val selectedUf: String = "SP",
    val workDaysCount: Int = 1,
    val offDaysCount: Int = 1,
    // Comma-separated string of 1 (work) and 0 (off) representing the full cycle
    // e.g. for 3x2: "1,1,1,0,0"
    val customCyclePattern: String = "1,0",
    // For mixed schedules (e.g. 2 day shifts, 2 night shifts)
    val mixedShiftSequence: String = "DAY,DAY,NIGHT,NIGHT"
)

@Entity(tableName = "day_overrides")
data class DayOverrideEntity(
    @PrimaryKey val date: LocalDate,
    val status: DayStatus, // FT (Folga Trabalhada) or SWAP (Troca de Plantão)
    val partnerName: String? = null,
    val extraValueOrLocation: String? = null,
    val notes: String? = null,
    val paybackDate: LocalDate? = null,
    val paybackCompleted: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime? = null,
    val title: String,
    val notes: String? = null,
    val reminderMinutesBefore: Int = 60, // 0 = at time, 60 = 1h before, 1440 = 1 day before
    val isShiftSwap: Boolean = false,
    val swapPartnerName: String? = null
)

@Entity(tableName = "vacation_periods")
data class VacationPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val title: String = "Férias",
    val notes: String? = null,
    val registeredAt: Long = System.currentTimeMillis()
)

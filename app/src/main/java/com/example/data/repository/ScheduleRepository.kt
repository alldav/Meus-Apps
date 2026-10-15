package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import com.example.data.model.DayStatus
import com.example.data.model.MonthSummary
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import com.example.domain.ScheduleEngine
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class ScheduleRepository(private val database: AppDatabase) {

    private val scheduleDao = database.scheduleDao()
    private val dayOverrideDao = database.dayOverrideDao()
    private val appointmentDao = database.appointmentDao()
    private val vacationDao = database.vacationDao()

    val scheduleConfigFlow: Flow<ScheduleConfigEntity?> = scheduleDao.getScheduleConfig()
    val allOverridesFlow: Flow<List<DayOverrideEntity>> = dayOverrideDao.getAllOverrides()
    val allAppointmentsFlow: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()
    val allVacationsFlow: Flow<List<VacationPeriodEntity>> = vacationDao.getAllVacations()

    suspend fun getOrCreateDefaultConfig(): ScheduleConfigEntity {
        val existing = scheduleDao.getScheduleConfigSync()
        if (existing != null) return existing

        val defaultConfig = ScheduleConfigEntity(
            id = 1,
            userName = "Plantonista",
            isConfigured = false,
            scheduleType = ScheduleType.TWELVE_THIRTY_SIX,
            referenceDate = LocalDate.now(),
            referenceIsWork = true,
            period = ShiftPeriod.DAY,
            customStartTime = "07:00",
            customEndTime = "19:00",
            selectedUf = "SP",
            workDaysCount = 1,
            offDaysCount = 1,
            customCyclePattern = "1,0"
        )
        scheduleDao.saveScheduleConfig(defaultConfig)
        return defaultConfig
    }

    suspend fun saveScheduleConfig(config: ScheduleConfigEntity) {
        scheduleDao.saveScheduleConfig(config)
    }

    suspend fun saveDayOverride(override: DayOverrideEntity) {
        dayOverrideDao.saveOverride(override)
    }

    suspend fun removeDayOverride(date: LocalDate) {
        dayOverrideDao.deleteOverrideForDate(date)
    }

    suspend fun getOverrideForDate(date: LocalDate): DayOverrideEntity? {
        return dayOverrideDao.getOverrideForDate(date)
    }

    suspend fun updateSwapPaybackStatus(date: LocalDate, completed: Boolean) {
        dayOverrideDao.updatePaybackStatus(date, completed)
    }

    suspend fun insertAppointment(appointment: AppointmentEntity): Long {
        return appointmentDao.insertAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.deleteAppointment(appointment)
    }

    suspend fun deleteAppointmentById(id: Long) {
        appointmentDao.deleteAppointmentById(id)
    }

    suspend fun insertVacation(vacation: VacationPeriodEntity): Long {
        return vacationDao.insertVacation(vacation)
    }

    suspend fun deleteVacation(vacation: VacationPeriodEntity) {
        vacationDao.deleteVacation(vacation)
    }

    suspend fun deleteVacationById(id: Long) {
        vacationDao.deleteVacationById(id)
    }

    fun getAppointmentsForMonth(yearMonth: YearMonth): Flow<List<AppointmentEntity>> {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        return appointmentDao.getAppointmentsBetween(start, end)
    }

    fun calculateMonthSummary(
        yearMonth: YearMonth,
        config: ScheduleConfigEntity,
        overrides: Map<LocalDate, DayOverrideEntity>,
        vacations: List<VacationPeriodEntity>
    ): MonthSummary {
        val daysInMonth = yearMonth.lengthOfMonth()
        var work = 0
        var off = 0
        var ft = 0
        var swap = 0
        var vacationDays = 0
        var weekendOff = 0

        for (day in 1..daysInMonth) {
            val date = yearMonth.atDay(day)
            val status = ScheduleEngine.calculateEffectiveStatus(date, config, overrides[date], vacations)

            when (status) {
                DayStatus.WORK -> work++
                DayStatus.OFF -> {
                    off++
                    if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        weekendOff++
                    }
                }
                DayStatus.FT -> ft++
                DayStatus.SWAP -> swap++
                DayStatus.VACATION -> {
                    vacationDays++
                    if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) {
                        weekendOff++
                    }
                }
            }
        }

        return MonthSummary(
            yearMonth = yearMonth,
            totalWorkDays = work,
            totalOffDays = off,
            totalFtDays = ft,
            totalSwapDays = swap,
            totalVacationDays = vacationDays,
            weekendOffDays = weekendOff
        )
    }
}

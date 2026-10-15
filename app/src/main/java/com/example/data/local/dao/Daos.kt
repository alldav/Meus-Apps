package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_config WHERE id = 1 LIMIT 1")
    fun getScheduleConfig(): Flow<ScheduleConfigEntity?>

    @Query("SELECT * FROM schedule_config WHERE id = 1 LIMIT 1")
    suspend fun getScheduleConfigSync(): ScheduleConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScheduleConfig(config: ScheduleConfigEntity)
}

@Dao
interface DayOverrideDao {
    @Query("SELECT * FROM day_overrides")
    fun getAllOverrides(): Flow<List<DayOverrideEntity>>

    @Query("SELECT * FROM day_overrides WHERE date = :date LIMIT 1")
    suspend fun getOverrideForDate(date: LocalDate): DayOverrideEntity?

    @Query("SELECT * FROM day_overrides WHERE date BETWEEN :startDate AND :endDate")
    fun getOverridesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<DayOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOverride(override: DayOverrideEntity)

    @Delete
    suspend fun deleteOverride(override: DayOverrideEntity)

    @Query("DELETE FROM day_overrides WHERE date = :date")
    suspend fun deleteOverrideForDate(date: LocalDate)

    @Query("UPDATE day_overrides SET paybackCompleted = :completed WHERE date = :date")
    suspend fun updatePaybackStatus(date: LocalDate, completed: Boolean)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY time ASC")
    fun getAppointmentsForDate(date: LocalDate): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC")
    fun getAppointmentsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)
}

@Dao
interface VacationDao {
    @Query("SELECT * FROM vacation_periods ORDER BY startDate ASC")
    fun getAllVacations(): Flow<List<VacationPeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVacation(vacation: VacationPeriodEntity): Long

    @Delete
    suspend fun deleteVacation(vacation: VacationPeriodEntity)

    @Query("DELETE FROM vacation_periods WHERE id = :id")
    suspend fun deleteVacationById(id: Long)
}

package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DayStatus
import com.example.data.model.ScheduleType
import com.example.data.model.ShiftPeriod
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.format(dateFormatter)

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }

    @TypeConverter
    fun fromDayStatus(status: DayStatus?): String? = status?.name

    @TypeConverter
    fun toDayStatus(value: String?): DayStatus? = value?.let { DayStatus.valueOf(it) }

    @TypeConverter
    fun fromScheduleType(type: ScheduleType?): String? = type?.name

    @TypeConverter
    fun toScheduleType(value: String?): ScheduleType? = value?.let { ScheduleType.valueOf(it) }

    @TypeConverter
    fun fromShiftPeriod(period: ShiftPeriod?): String? = period?.name

    @TypeConverter
    fun toShiftPeriod(value: String?): ShiftPeriod? = value?.let { ShiftPeriod.valueOf(it) }
}

package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AppointmentDao
import com.example.data.local.dao.DayOverrideDao
import com.example.data.local.dao.ScheduleDao
import com.example.data.local.dao.VacationDao
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity

@Database(
    entities = [
        ScheduleConfigEntity::class,
        DayOverrideEntity::class,
        AppointmentEntity::class,
        VacationPeriodEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun dayOverrideDao(): DayOverrideDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun vacationDao(): VacationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plantao_folga_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

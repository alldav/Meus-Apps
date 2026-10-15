package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.model.DayStatus
import com.example.data.model.ScheduleType
import com.example.domain.BrazilianHolidayProvider
import com.example.domain.ScheduleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Escala Medina", appName)
    }

    @Test
    fun `test 12x36 schedule calculation`() {
        val baseDate = LocalDate.of(2026, 10, 13)
        val config = ScheduleConfigEntity(
            scheduleType = ScheduleType.TWELVE_THIRTY_SIX,
            referenceDate = baseDate,
            referenceIsWork = true
        )

        // Base date is WORK
        val statusDay0 = ScheduleEngine.calculateBaseStatus(baseDate, config)
        assertEquals(DayStatus.WORK, statusDay0)

        // Next day is OFF
        val statusDay1 = ScheduleEngine.calculateBaseStatus(baseDate.plusDays(1), config)
        assertEquals(DayStatus.OFF, statusDay1)

        // Day 2 is WORK
        val statusDay2 = ScheduleEngine.calculateBaseStatus(baseDate.plusDays(2), config)
        assertEquals(DayStatus.WORK, statusDay2)
    }

    @Test
    fun `test 4x2 schedule calculation with calendar pattern`() {
        // Wednesday Oct 14: 4 work, 2 off sequence
        val wednesday = LocalDate.of(2026, 10, 14)
        val config = ScheduleConfigEntity(
            scheduleType = ScheduleType.FOUR_TWO,
            referenceDate = wednesday,
            referenceIsWork = true,
            customCyclePattern = "1,1,1,1,0,0" // 4 work, 2 off
        )

        // Day 0 (Wed 14): WORK
        assertEquals(DayStatus.WORK, ScheduleEngine.calculateBaseStatus(wednesday, config))
        // Day 1 (Thu 15): WORK
        assertEquals(DayStatus.WORK, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(1), config))
        // Day 2 (Fri 16): WORK
        assertEquals(DayStatus.WORK, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(2), config))
        // Day 3 (Sat 17): WORK
        assertEquals(DayStatus.WORK, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(3), config))
        // Day 4 (Sun 18): OFF
        assertEquals(DayStatus.OFF, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(4), config))
        // Day 5 (Mon 19): OFF
        assertEquals(DayStatus.OFF, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(5), config))
        // Day 6 (Tue 20): Cycle repeats -> WORK
        assertEquals(DayStatus.WORK, ScheduleEngine.calculateBaseStatus(wednesday.plusDays(6), config))
    }

    @Test
    fun `test brazilian holidays loading`() {
        val holidays2026 = BrazilianHolidayProvider.getHolidaysForYear(2026, "SP")
        assertTrue(holidays2026.isNotEmpty())

        val christmas = holidays2026.find { it.date == LocalDate.of(2026, 12, 25) }
        assertNotNull(christmas)
        assertEquals("Natal", christmas?.name)

        // SP state holiday: 9 de Julho
        val spHoliday = holidays2026.find { it.date == LocalDate.of(2026, 7, 9) }
        assertNotNull(spHoliday)
    }

    @Test
    fun `test app update manager simulated release`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val updateManager = com.example.data.update.AppUpdateManager(context)
        val release = updateManager.createSimulatedUpdate()

        assertNotNull(release)
        assertTrue(release.versionCode > 0)
        assertEquals("1.1.0", release.versionName)
        assertTrue(release.releaseNotes.isNotEmpty())
    }

    @Test
    fun `test return to work date calculation after vacation`() {
        val config = ScheduleConfigEntity(
            scheduleType = ScheduleType.TWELVE_THIRTY_SIX,
            referenceDate = LocalDate.of(2026, 10, 1),
            referenceIsWork = true
        )
        // Vacation from Oct 10 to Oct 20
        val vacations = listOf(
            com.example.data.local.entities.VacationPeriodEntity(
                startDate = LocalDate.of(2026, 10, 10),
                endDate = LocalDate.of(2026, 10, 20),
                title = "Férias Anuais"
            )
        )

        // Day after vacation is Oct 21
        // Oct 1 (day 0) is WORK, Oct 2 is OFF ...
        // Day 20 (Oct 21) -> 20 days difference -> 20 % 2 == 0 -> WORK!
        val returnWorkDate = ScheduleEngine.findNextWorkDay(
            startDate = LocalDate.of(2026, 10, 21),
            config = config,
            vacations = vacations
        )
        assertEquals(LocalDate.of(2026, 10, 21), returnWorkDate)
    }
}

package com.example.domain

import com.example.data.local.entities.DayOverrideEntity
import com.example.data.local.entities.ScheduleConfigEntity
import com.example.data.local.entities.VacationPeriodEntity
import com.example.data.model.DayStatus
import com.example.data.model.QueryResult
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object QuickQueryParser {

    /**
     * Parses user query (typed or spoken) and returns a detailed QueryResult with diagnosis.
     */
    fun parseAndEvaluate(
        rawQuery: String,
        currentDate: LocalDate = LocalDate.now(),
        config: ScheduleConfigEntity,
        overrides: Map<LocalDate, DayOverrideEntity>,
        vacations: List<VacationPeriodEntity> = emptyList()
    ): QueryResult? {
        val query = rawQuery.trim().lowercase(Locale.ROOT)
        if (query.isBlank()) return null

        val targetDate = extractDateFromQuery(query, currentDate, config.selectedUf) ?: return null
        val status = ScheduleEngine.calculateEffectiveStatus(targetDate, config, overrides[targetDate], vacations)
        val holiday = BrazilianHolidayProvider.getHolidayOn(targetDate, config.selectedUf)
        val shiftHours = ScheduleEngine.getShiftHours(targetDate, config)

        val dateFormatted = targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR")))
            .replaceFirstChar { it.uppercase() }

        val message = when (status) {
            DayStatus.VACATION -> {
                val vac = vacations.find { !targetDate.isBefore(it.startDate) && !targetDate.isAfter(it.endDate) }
                val periodText = if (vac != null) " (de ${vac.startDate.format(DateTimeFormatter.ofPattern("dd/MM"))} a ${vac.endDate.format(DateTimeFormatter.ofPattern("dd/MM"))})" else ""
                "🏖️ VOCÊ ESTARÁ DE FÉRIAS! No dia $dateFormatted você estará em período oficial de descanso$periodText. Aproveite bastante!"
            }
            DayStatus.OFF -> {
                if (holiday != null) {
                    "🎉 FOLGA no Feriado! No dia $dateFormatted você estará 100% DE FOLGA (${holiday.name}). Pode planejar suas viagens e comemorações sem preocupação!"
                } else {
                    "🎉 VOCÊ ESTARÁ DE FOLGA! No dia $dateFormatted você estará livre da escala. Ótimo dia para marcar compromissos, consultas e descansar!"
                }
            }
            DayStatus.WORK -> {
                if (holiday != null) {
                    "🛡️ ATENÇÃO: PLANTÃO EM FERIADO! No dia $dateFormatted é ${holiday.name} e você estará DE PLANTÃO ($shiftHours). Se precisar folgar, solicite uma Troca de Plantão no app!"
                } else {
                    "🛡️ VOCÊ ESTARÁ DE PLANTÃO! No dia $dateFormatted você estará escalado para trabalhar ($shiftHours). Evite marcar compromissos ou registre uma troca antecipada!"
                }
            }
            DayStatus.FT -> {
                "💼 FOLGA TRABALHADA (FT)! No dia $dateFormatted você registrou que irá trabalhar como FT/extra no seu dia de folga."
            }
            DayStatus.SWAP -> {
                val partner = overrides[targetDate]?.partnerName
                val partnerInfo = if (!partner.isNullOrBlank()) " coberta pelo colega $partner" else ""
                "🎉 FOLGA POR TROCA! No dia $dateFormatted você estará livre da escala$partnerInfo. Bom descanso!"
            }
            DayStatus.SWAP_WORK -> {
                val partner = overrides[targetDate]?.partnerName
                val partnerInfo = if (!partner.isNullOrBlank()) " no lugar do colega $partner" else ""
                "🔄 PLANTÃO POR TROCA! No dia $dateFormatted você estará trabalhando$partnerInfo (troca acordada na folga)."
            }
        }

        return QueryResult(
            targetDate = targetDate,
            status = status,
            periodLabel = shiftHours,
            holiday = holiday,
            isHoliday = holiday != null,
            message = message
        )
    }

    private fun extractDateFromQuery(query: String, today: LocalDate, selectedUf: String): LocalDate? {
        val currentYear = today.year

        // Special relative days
        if (query.contains("hoje")) return today
        if (query.contains("amanhã") || query.contains("amanha")) return today.plusDays(1)
        if (query.contains("depois de amanhã") || query.contains("depois de amanha")) return today.plusDays(2)

        // Special celebrated events
        if (query.contains("natal")) {
            val date = LocalDate.of(currentYear, 12, 25)
            return if (date.isBefore(today)) date.plusYears(1) else date
        }
        if (query.contains("ano novo") || query.contains("reveillon") || query.contains("réveillon") || query.contains("virada")) {
            val date = LocalDate.of(currentYear, 12, 31)
            return if (date.isBefore(today)) date.plusYears(1) else date
        }
        if (query.contains("carnaval")) {
            val holidaysThisYear = BrazilianHolidayProvider.getHolidaysForYear(currentYear, selectedUf)
            val carnival = holidaysThisYear.find { it.name == "Carnaval" }?.date
            if (carnival != null && !carnival.isBefore(today)) return carnival
            return BrazilianHolidayProvider.getHolidaysForYear(currentYear + 1, selectedUf).find { it.name == "Carnaval" }?.date
        }
        if (query.contains("páscoa") || query.contains("pascoa")) {
            val holidaysThisYear = BrazilianHolidayProvider.getHolidaysForYear(currentYear, selectedUf)
            val easter = holidaysThisYear.find { it.name == "Páscoa" }?.date
            if (easter != null && !easter.isBefore(today)) return easter
            return BrazilianHolidayProvider.getHolidaysForYear(currentYear + 1, selectedUf).find { it.name == "Páscoa" }?.date
        }
        if (query.contains("tiradentes")) return LocalDate.of(currentYear, 4, 21)
        if (query.contains("independência") || query.contains("independencia") || query.contains("7 de setembro")) return LocalDate.of(currentYear, 9, 7)
        if (query.contains("aparecida") || query.contains("12 de outubro")) return LocalDate.of(currentYear, 10, 12)
        if (query.contains("finados")) return LocalDate.of(currentYear, 11, 2)
        if (query.contains("república") || query.contains("republica") || query.contains("15 de novembro")) return LocalDate.of(currentYear, 11, 15)
        if (query.contains("consciência negra") || query.contains("consciencia negra") || query.contains("20 de novembro")) return LocalDate.of(currentYear, 11, 20)

        // Day of week queries (e.g. "próximo sábado", "domingo que vem", "sexta")
        val dayOfWeek = when {
            query.contains("segunda") -> DayOfWeek.MONDAY
            query.contains("terça") || query.contains("terca") -> DayOfWeek.TUESDAY
            query.contains("quarta") -> DayOfWeek.WEDNESDAY
            query.contains("quinta") -> DayOfWeek.THURSDAY
            query.contains("sexta") -> DayOfWeek.FRIDAY
            query.contains("sábado") || query.contains("sabado") -> DayOfWeek.SATURDAY
            query.contains("domingo") -> DayOfWeek.SUNDAY
            else -> null
        }
        if (dayOfWeek != null) {
            var cursor = today.plusDays(1)
            while (cursor.dayOfWeek != dayOfWeek) {
                cursor = cursor.plusDays(1)
            }
            return cursor
        }

        // Direct dates formats: dd/MM/yyyy or dd/MM
        val slashRegex = Regex("""(\d{1,2})[/.-](\d{1,2})(?:[/.-](\d{2,4}))?""")
        val matchSlash = slashRegex.find(query)
        if (matchSlash != null) {
            val day = matchSlash.groupValues[1].toIntOrNull() ?: 1
            val month = matchSlash.groupValues[2].toIntOrNull() ?: 1
            val rawYear = matchSlash.groupValues.getOrNull(3)?.toIntOrNull()
            val year = when {
                rawYear == null -> if (LocalDate.of(currentYear, month, day).isBefore(today)) currentYear + 1 else currentYear
                rawYear < 100 -> 2000 + rawYear
                else -> rawYear
            }
            return try {
                LocalDate.of(year, month, day)
            } catch (_: Exception) {
                null
            }
        }

        // Month names e.g. "15 de dezembro", "20 de janeiro de 2027"
        val monthNames = mapOf(
            "janeiro" to 1, "jan" to 1,
            "fevereiro" to 2, "fev" to 2,
            "março" to 3, "marco" to 3, "mar" to 3,
            "abril" to 4, "abr" to 4,
            "maio" to 5, "mai" to 5,
            "junho" to 6, "jun" to 6,
            "julho" to 7, "jul" to 7,
            "agosto" to 8, "ago" to 8,
            "setembro" to 9, "set" to 9,
            "outubro" to 10, "out" to 10,
            "novembro" to 11, "nov" to 11,
            "dezembro" to 12, "dez" to 12
        )

        for ((mName, mNum) in monthNames) {
            if (query.contains(mName)) {
                val dayRegex = Regex("""(?:dia\s+)?(\d{1,2})""")
                val dayMatch = dayRegex.find(query)
                val day = dayMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val yearRegex = Regex("""\b(20\d{2})\b""")
                val yearMatch = yearRegex.find(query)
                val year = yearMatch?.groupValues?.get(1)?.toIntOrNull() ?: currentYear
                return try {
                    LocalDate.of(year, mNum, day)
                } catch (_: Exception) {
                    null
                }
            }
        }

        // Just "dia 25" e.g.
        val justDayRegex = Regex("""dia\s+(\d{1,2})\b""")
        val justDayMatch = justDayRegex.find(query)
        if (justDayMatch != null) {
            val day = justDayMatch.groupValues[1].toIntOrNull()
            if (day != null && day in 1..31) {
                return try {
                    val dateThisMonth = today.withDayOfMonth(day)
                    if (dateThisMonth.isBefore(today)) today.plusMonths(1).withDayOfMonth(day) else dateThisMonth
                } catch (_: Exception) {
                    null
                }
            }
        }

        return null
    }
}

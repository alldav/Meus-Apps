package com.example.domain

import com.example.data.model.BrazilianHoliday
import java.time.LocalDate

object BrazilianHolidayProvider {

    val BRAZILIAN_STATES = listOf(
        "AC" to "Acre",
        "AL" to "Alagoas",
        "AP" to "Amapá",
        "AM" to "Amazonas",
        "BA" to "Bahia",
        "CE" to "Ceará",
        "DF" to "Distrito Federal",
        "ES" to "Espírito Santo",
        "GO" to "Goiás",
        "MA" to "Maranhão",
        "MT" to "Mato Grosso",
        "MS" to "Mato Grosso do Sul",
        "MG" to "Minas Gerais",
        "PA" to "Pará",
        "PB" to "Paraíba",
        "PR" to "Paraná",
        "PE" to "Pernambuco",
        "PI" to "Piauí",
        "RJ" to "Rio de Janeiro",
        "RN" to "Rio Grande do Norte",
        "RS" to "Rio Grande do Sul",
        "RO" to "Rondônia",
        "RR" to "Roraima",
        "SC" to "Santa Catarina",
        "SP" to "São Paulo",
        "SE" to "Sergipe",
        "TO" to "Tocantins"
    )

    /**
     * Calculates all national holidays and state-specific holidays for a given year and UF.
     */
    fun getHolidaysForYear(year: Int, selectedUf: String): List<BrazilianHoliday> {
        val list = mutableListOf<BrazilianHoliday>()

        // Fixed National Holidays
        list.add(BrazilianHoliday(LocalDate.of(year, 1, 1), "Confraternização Universal", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 4, 21), "Tiradentes", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 5, 1), "Dia do Trabalho", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 9, 7), "Independência do Brasil", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 10, 12), "Nossa Senhora Aparecida", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 11, 2), "Finados", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 11, 15), "Proclamação da República", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 11, 20), "Dia da Consciência Negra", isNational = true))
        list.add(BrazilianHoliday(LocalDate.of(year, 12, 25), "Natal", isNational = true))

        // Mobile Christian Holidays based on Easter (Meeus/Jones algorithm)
        val easter = calculateEaster(year)
        val carnival = easter.minusDays(47)
        val goodFriday = easter.minusDays(2)
        val corpusChristi = easter.plusDays(60)

        list.add(BrazilianHoliday(carnival, "Carnaval", isNational = true))
        list.add(BrazilianHoliday(goodFriday, "Sexta-feira Santa", isNational = true))
        list.add(BrazilianHoliday(easter, "Páscoa", isNational = true))
        list.add(BrazilianHoliday(corpusChristi, "Corpus Christi", isNational = true))

        // State Holidays for the chosen UF
        getStateHolidays(year, selectedUf.uppercase()).forEach { stateHoliday ->
            list.add(stateHoliday)
        }

        return list.sortedBy { it.date }
    }

    fun getHolidayOn(date: LocalDate, selectedUf: String): BrazilianHoliday? {
        val holidays = getHolidaysForYear(date.year, selectedUf)
        return holidays.find { it.date == date }
    }

    /**
     * Anonymous Gregorian algorithm for Easter calculation.
     */
    private fun calculateEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }

    private fun getStateHolidays(year: Int, uf: String): List<BrazilianHoliday> {
        val holidays = mutableListOf<BrazilianHoliday>()
        when (uf) {
            "AC" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 1, 23), "Dia do Evangélico (AC)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 6, 15), "Aniversário do Estado (AC)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 8, 6), "Início da Revolução Acreana", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 11, 17), "Tratado de Petrópolis (AC)", false, uf))
            }
            "AL" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 6, 24), "São João (AL)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 6, 29), "São Pedro (AL)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 9, 16), "Emancipação Política de Alagoas", false, uf))
            }
            "AP" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 3, 19), "Dia de São José (AP)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 9, 13), "Criação do Território Federal (AP)", false, uf))
            }
            "AM" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 9, 5), "Elevação do Amazonas à Categoria de Província", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 24), "Aniversário de Manaus / AM", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 12, 8), "Nossa Senhora da Conceição (AM)", false, uf))
            }
            "BA" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 7, 2), "Independência da Bahia", false, uf))
            }
            "CE" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 3, 19), "Dia de São José (CE)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 3, 25), "Data Magna do Ceará (Libertação dos Escravos)", false, uf))
            }
            "DF" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 4, 21), "Fundação de Brasília", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 11, 30), "Dia do Evangélico (DF)", false, uf))
            }
            "ES" -> {
                // Nossa Senhora da Penha (oitava de Páscoa)
                val easter = calculateEaster(year)
                holidays.add(BrazilianHoliday(easter.plusDays(8), "Nossa Senhora da Penha (ES)", false, uf))
            }
            "GO" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 24), "Pedra Fundamental de Goiânia", false, uf))
            }
            "MA" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 7, 28), "Adesão do Maranhão à Independência", false, uf))
            }
            "MT" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 11, 20), "Consciência Negra / Zumbi (MT)", false, uf))
            }
            "MS" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 11), "Criação do Estado do Mato Grosso do Sul", false, uf))
            }
            "MG" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 4, 21), "Data Magna de Minas Gerais", false, uf))
            }
            "PA" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 8, 15), "Adesão do Grão-Pará à Independência", false, uf))
            }
            "PB" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 7, 26), "Homenagem à Memória de João Pessoa", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 8, 5), "Fundação do Estado da Paraíba", false, uf))
            }
            "PR" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 12, 19), "Emancipação Política do Paraná", false, uf))
            }
            "PE" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 3, 6), "Revolução Pernambucana de 1817", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 6, 24), "São João (PE)", false, uf))
            }
            "PI" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 19), "Dia do Piauí", false, uf))
            }
            "RJ" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 4, 23), "Dia de São Jorge (RJ)", false, uf))
            }
            "RN" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 3), "Mártires de Cunhaú e Uruuaçu (RN)", false, uf))
            }
            "RS" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 9, 20), "Revolução Farroupilha (Dia do Gaúcho)", false, uf))
            }
            "RO" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 1, 4), "Criação do Estado de Rondônia", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 6, 18), "Dia do Evangélico (RO)", false, uf))
            }
            "RR" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 5), "Criação do Estado de Roraima", false, uf))
            }
            "SC" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 8, 11), "Data Magna do Estado de Santa Catarina", false, uf))
            }
            "SP" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 7, 9), "Revolução Constitucionalista de 1932 (SP)", false, uf))
            }
            "SE" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 7, 8), "Emancipação Política de Sergipe", false, uf))
            }
            "TO" -> {
                holidays.add(BrazilianHoliday(LocalDate.of(year, 9, 8), "Nossa Senhora da Natividade (TO)", false, uf))
                holidays.add(BrazilianHoliday(LocalDate.of(year, 10, 5), "Criação do Estado do Tocantins", false, uf))
            }
        }
        return holidays
    }
}

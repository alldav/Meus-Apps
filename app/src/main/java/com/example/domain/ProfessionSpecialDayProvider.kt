package com.example.domain

import java.time.LocalDate

data class ProfessionDayCelebration(
    val title: String,
    val message: String,
    val iconEmoji: String
)

data class PlantonistaTip(
    val category: String,
    val title: String,
    val content: String,
    val iconEmoji: String
)

object ProfessionSpecialDayProvider {

    val RECOGNIZED_PROFESSIONS = listOf(
        "Vigilante / Segurança Privada",
        "Porteiro / Portaria",
        "Controlador(a) de Acesso",
        "Enfermagem (Técnico / Enfermeiro)",
        "Médico(a)",
        "Policial / Segurança Pública",
        "Bombeiro(a) / Brigadista",
        "Socorrista / SAMU / Resgate",
        "Operador(a) de Monitoramento / CFTV",
        "Agente de Trânsito / Guarda Municipal",
        "Recepcionista / Atendimento",
        "Condutor(a) / Motorista de Plantão",
        "Farmacêutico(a)",
        "Fisioterapeuta",
        "Técnico(a) em Radiologia",
        "Auxiliar de Limpeza / Conservação",
        "Outra profissão (digite sua profissão)"
    )

    fun checkCelebrationForToday(profession: String, date: LocalDate): ProfessionDayCelebration? {
        val month = date.monthValue
        val day = date.dayOfMonth

        val normalized = profession.trim().lowercase()

        return when {
            normalized.contains("enferm") -> {
                when {
                    month == 5 && day == 12 -> ProfessionDayCelebration(
                        title = "🎉 Parabéns pelo seu dia!",
                        message = "Hoje é o Dia Internacional da Enfermagem! O Escala Medina agradece sua dedicação e cuidado que salvam vidas todos os dias.",
                        iconEmoji = "🩺"
                    )
                    month == 5 && day == 20 -> ProfessionDayCelebration(
                        title = "🎉 Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Técnico e Auxiliar de Enfermagem! Força, competência e amor no cuidado dos pacientes.",
                        iconEmoji = "💉"
                    )
                    else -> null
                }
            }
            normalized.contains("vigilan") || normalized.contains("seguran") -> {
                if (month == 6 && day == 20) {
                    ProfessionDayCelebration(
                        title = "🛡️ Parabéns, Guerreiro!",
                        message = "Hoje é o Dia Nacional do Vigilante! Honra e respeito a quem zela pela segurança e tranquilidade de todos.",
                        iconEmoji = "👮"
                    )
                } else null
            }
            normalized.contains("médic") || normalized.contains("medic") -> {
                if (month == 10 && day == 18) {
                    ProfessionDayCelebration(
                        title = "🎉 Parabéns, Doutor(a)!",
                        message = "Hoje é o Dia do Médico! Obrigado pelo compromisso incansável com a saúde e a vida dos seus pacientes.",
                        iconEmoji = "⚕️"
                    )
                } else null
            }
            normalized.contains("polic") -> {
                when {
                    month == 4 && day == 21 -> ProfessionDayCelebration(
                        title = "⭐ Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Policial (Tiradentes)! Homenagem a quem dedica a vida à proteção da sociedade.",
                        iconEmoji = "🚓"
                    )
                    month == 12 && day == 4 -> ProfessionDayCelebration(
                        title = "⭐ Dia do Policial Penal!",
                        message = "Parabéns aos profissionais de segurança penal pela disciplina e dedicação contínua.",
                        iconEmoji = "🛡️"
                    )
                    else -> null
                }
            }
            normalized.contains("bombeir") -> {
                if (month == 7 && day == 2) {
                    ProfessionDayCelebration(
                        title = "🚒 Parabéns, Herói(na)!",
                        message = "Hoje é o Dia do Bombeiro Brasileiro! Vidas alheias e riquezas salvar: nossa homenagem pela sua coragem!",
                        iconEmoji = "🚒"
                    )
                } else null
            }
            normalized.contains("socorr") || normalized.contains("samu") -> {
                if (month == 7 && day == 11) {
                    ProfessionDayCelebration(
                        title = "🚑 Parabéns, Socorrista!",
                        message = "Hoje é o Dia do Socorrista! A rapidez e bravura de quem chega primeiro quando a vida mais precisa.",
                        iconEmoji = "🚑"
                    )
                } else null
            }
            normalized.contains("farmac") -> {
                if (month == 1 && day == 20) {
                    ProfessionDayCelebration(
                        title = "💊 Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Farmacêutico! Conhecimento e precisão que garantem a cura e o bem-estar.",
                        iconEmoji = "💊"
                    )
                } else null
            }
            normalized.contains("fisio") -> {
                if (month == 10 && day == 13) {
                    ProfessionDayCelebration(
                        title = "🏃 Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Fisioterapeuta! Devolver o movimento, o alívio e a independência a quem precisa.",
                        iconEmoji = "👐"
                    )
                } else null
            }
            normalized.contains("motorist") || normalized.contains("condutor") -> {
                if (month == 7 && day == 25) {
                    ProfessionDayCelebration(
                        title = "🚗 Parabéns, Motorista!",
                        message = "Hoje é o Dia do Motorista! Condução defensiva e agilidade no trânsito transportando vidas e equipes.",
                        iconEmoji = "🚐"
                    )
                } else null
            }
            normalized.contains("porteir") || normalized.contains("acesso") -> {
                if (month == 6 && day == 9) {
                    ProfessionDayCelebration(
                        title = "🏢 Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Porteiro! Atenção, cordialidade e proteção no primeiro ponto de contato.",
                        iconEmoji = "🔑"
                    )
                } else null
            }
            normalized.contains("radio") -> {
                if (month == 11 && day == 8) {
                    ProfessionDayCelebration(
                        title = "☢️ Parabéns pelo seu dia!",
                        message = "Hoje é o Dia do Profissional de Radiologia! Enxergando o invisível para diagnósticos precisos.",
                        iconEmoji = "🩻"
                    )
                } else null
            }
            month == 5 && day == 1 -> {
                ProfessionDayCelebration(
                    title = "🎉 Feliz Dia do Trabalhador!",
                    message = "Homenagem especial a quem não para nem nos feriados e mantém o Brasil funcionando nos plantões!",
                    iconEmoji = "⭐"
                )
            }
            else -> null
        }
    }

    fun getTipsForProfession(profession: String, sector: String = ""): List<PlantonistaTip> {
        val list = mutableListOf<PlantonistaTip>()
        val normalized = profession.trim().lowercase()

        when {
            normalized.contains("enferm") -> {
                list.add(PlantonistaTip(
                    category = "Saúde na Enfermagem",
                    title = "Alívio nas Pernas em 12 Horas",
                    content = "Use meias de compressão elástica durante o plantão para melhorar o retorno venoso e evitar dores e inchaço ao final das 12h.",
                    iconEmoji = "🩺"
                ))
                list.add(PlantonistaTip(
                    category = "Dica de Plantão",
                    title = "Passagem de Plantão Organizada",
                    content = "Anote as intercorrências no bloco de anotações do Escala Medina para não esquecer detalhes cruciais na troca de turno.",
                    iconEmoji = "📋"
                ))
            }
            normalized.contains("vigilan") || normalized.contains("seguran") -> {
                list.add(PlantonistaTip(
                    category = "Atenção Tática",
                    title = "Combate à Sonolência das 03h às 05h",
                    content = "Nesse horário o ciclo circadiano tem pico de sonolência. Faça pausas ativas com caminhada curta, água fria no rosto e alongamentos.",
                    iconEmoji = "🛡️"
                ))
                list.add(PlantonistaTip(
                    category = "Segurança do Posto",
                    title = "Comunicação e Ronda",
                    content = "Mantenha o rádio e bateria do celular cheios. Se fizer FT extra, garanta que suas folgas de descanso estejam registradas no app.",
                    iconEmoji = "🔦"
                ))
            }
            normalized.contains("porteir") || normalized.contains("acesso") -> {
                list.add(PlantonistaTip(
                    category = "Portaria & Controle de Acesso",
                    title = "Postura e Campo de Visão",
                    content = "Mantenha a tela de CFTV e o interfone na linha dos olhos. Evite inclinar o pescoço para baixo por horas seguidas para prevenir dores na cervical.",
                    iconEmoji = "🏢"
                ))
                list.add(PlantonistaTip(
                    category = "Atenção no Acesso",
                    title = "Pausas de Movimentação",
                    content = "Levante-se da guarita a cada 45 minutos para caminhar alguns passos, esticar a coluna e renovar o estado de alerta.",
                    iconEmoji = "🚶"
                ))
            }
            normalized.contains("médic") || normalized.contains("socorr") -> {
                list.add(PlantonistaTip(
                    category = "Bem-Estar Médico",
                    title = "Micro-pausas Estratégicas",
                    content = "5 minutos de descompressão respiratória entre atendimentos pesados renovam o foco diagnóstico e evitam burnout.",
                    iconEmoji = "⚕️"
                ))
            }
            else -> {
                list.add(PlantonistaTip(
                    category = "Higiene do Sono",
                    title = "Pós-Plantão Noturno Eficiente",
                    content = "Na volta para casa após a noite, use óculos escuros e mantenha o quarto 100% escuro e fresco para estimular o sono reparador.",
                    iconEmoji = "🌙"
                ))
            }
        }

        // Sector-specific tip if available
        if (sector.isNotBlank()) {
            list.add(PlantonistaTip(
                category = "Setor: $sector",
                title = "Alinhamento com a Equipe",
                content = "Em setores movimentados como '$sector', ter suas trocas combinadas salvas no app evita ruídos e faltas acidentais.",
                iconEmoji = "📍"
            ))
        }

        // Ergonomia para Profissões em Pé e Plantões Longos (Vigilantes, Enfermagem, Portaria, etc.)
        list.add(PlantonistaTip(
            category = "Ergonomia em Pé",
            title = "Distribuição de Peso e Joelhos Destravados",
            content = "Evite descarregar todo o peso em uma só perna. Alterne o apoio dos pés a cada 15 minutos e mantenha os joelhos levemente destravados para aliviar a lombar.",
            iconEmoji = "🚶"
        ))

        list.add(PlantonistaTip(
            category = "Ergonomia & Calçados",
            title = "Calçados e Palmilhas com Amortecimento",
            content = "Trabalhar 12h em pé exige solado com absorção e palmilha de gel ou anatômica, reduzindo até 40% a sobrecarga nos joelhos, calcanhares e coluna.",
            iconEmoji = "👟"
        ))

        list.add(PlantonistaTip(
            category = "Circulação no Posto",
            title = "Bomba da Panturrilha (Exercício Rápido)",
            content = "A cada 1 ou 2 horas, fique na ponta dos pés por 15 vezes seguidas. Esse movimento simples bombeia o sangue venoso de volta ao coração e previne varizes e inchaço.",
            iconEmoji = "🦵"
        ))

        list.add(PlantonistaTip(
            category = "Descanso para os Pés",
            title = "Apoio Elevado de Pés",
            content = "Se o seu posto permitir, coloque uma pequena barra ou apoio no chão e alterne um dos pés sobre ele. Isso muda a inclinação pélvica e zera a tensão na lombar.",
            iconEmoji = "🦶"
        ))

        list.add(PlantonistaTip(
            category = "Coluna & Movimentação",
            title = "Flexione os Joelhos, Não a Coluna",
            content = "Ao levantar materiais, caixas ou apoiar pessoas, agache dobrando os joelhos e mantenha o peso colado ao tronco para não lesionar a coluna lombar.",
            iconEmoji = "🏋️"
        ))

        list.add(PlantonistaTip(
            category = "Alongamento Rápido",
            title = "Descompressão da Coluna Lombar",
            content = "Apoie as mãos na cintura e incline o tronco suavemente para trás por 5 segundos, repetindo 3 vezes. Esse movimento contrai a musculatura e alivia a pressão dos discos.",
            iconEmoji = "🧘"
        ))

        list.add(PlantonistaTip(
            category = "Meta de Hidratação",
            title = "Água Contínua no Turno",
            content = "Beba água regularmente mesmo sem sede aparente. Manter a garrafinha de 500 ml por perto e repor 4 vezes garante sua meta de 2000 ml no plantão.",
            iconEmoji = "💧"
        ))

        list.add(PlantonistaTip(
            category = "Descanso Circulatório",
            title = "Elevação de Pernas na Pausa",
            content = "No seu intervalo regulamentar de refeição ou descanso, sente-se e mantenha as pernas elevadas por 10 a 15 minutos para zerar a pressão nos membros inferiores.",
            iconEmoji = "🛋️"
        ))

        // Universal Shift Tips
        list.add(PlantonistaTip(
            category = "Organização de Trocas",
            title = "Compensação Garantida",
            content = "Ao fazer troca no dia de folga ou plantão, use o Escala Medina para registrar a data em que o colega devolverá o serviço.",
            iconEmoji = "🔄"
        ))

        return list
    }

    fun getCelebrationInMonth(profession: String, yearMonth: java.time.YearMonth): Pair<LocalDate, ProfessionDayCelebration>? {
        if (profession.isBlank() || profession == "Geral / Plantonista") return null
        for (day in 1..yearMonth.lengthOfMonth()) {
            val date = yearMonth.atDay(day)
            val celebration = checkCelebrationForToday(profession, date)
            if (celebration != null) {
                return Pair(date, celebration)
            }
        }
        return null
    }
}

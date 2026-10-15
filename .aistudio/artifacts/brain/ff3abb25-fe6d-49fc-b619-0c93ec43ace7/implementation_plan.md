# Plantão & Folga: Gestão Inteligente de Escalas, FT e Compromissos

Aplicativo tático e inteligente projetado para profissionais de segurança e plantonistas (12x36, 6x1, 5x2, 4x2 e escalas personalizadas). Permite calcular folgas futuras com antecedência ilimitada, gerenciar Folgas Trabalhadas (FT), registrar Trocas de Plantão com colegas, consultar feriados do Brasil (nacionais e estaduais por UF) e tirar dúvidas rápidas por texto ou voz sobre folgas em datas específicas.

## User Review & Critical Decisions

> [!IMPORTANT]
> Revisões incorporadas com base no seu feedback:

- **Construtor Visual de Escala Personalizada**: O usuário define a proporção de trabalho e folga (ex: "3 por 2", "2 por 2") e em seguida marca visualmente no calendário os dias que compõem o primeiro ciclo completo. O motor de cálculo assume a repetição automática a partir desse padrão.
- **Registro de Folga Trabalhada (FT) & Trocas**:
  - Distinção clara entre:
    - 🛡️ **Plantão Ordinário**: Turno normal de escala.
    - 🟢 **Folga Real**: Dia de descanso garantido.
    - 💼 **Folga Trabalhada (FT)**: Dia originalmente de folga em que o profissional foi escalado ou aceitou fazer hora extra/FT (com valor ou anotação).
    - 🔄 **Troca de Plantão**: Dia trocado com outro colega (com nome do colega e dia a pagar/receber).
  - **Relatório Mensal de Produtividade**: Resumo com total de FTs realizadas, trocas efetuadas e plantões cumpridos no mês.
- **Assistente Rápido de Dúvidas ("Estou de Folga no Dia Tal?")**:
  - Campo de busca em linguagem natural ou atalho rápido (ex: "15 de novembro estou de folga?", "natal", ou via reconhecimento de voz por microfone) que responde imediatamente com status, feriado e compromissos existentes.
- **Feriados do Brasil (Nacionais + Estaduais por UF)**: Carregamento seletivo pelo Estado do usuário.
- **Persistência Confiável Local (Room Database)**: Dados seguros no aparelho, funcionando 100% offline em guaritas e postos sem sinal.

---

## 1. Overview & Core Concept

- **O que faz**:
  - Responde instantaneamente à pergunta clássica do plantonista: *"Em tal data estarei de folga para viajar ou marcar consulta?"*
  - Controla o ciclo exato da escala (12x36 dia sim/dia não, 6x1, 5x2, 4x2 ou padrão visual customizado como 3x2).
  - Dá total visibilidade sobre **Folgas Trabalhadas (FT)** para controle de rendimento e escalas extras.
  - Oferece atalho de voz e texto para perguntar por datas diretamente sem navegar página a página.
- **Público-Alvo**:
  - Vigilantes, porteiros, policiais, guardas municipais, bombeiros, enfermeiros e agentes de segurança privada.
- **Diferencial**:
  - Design inteligente, tático e limpo com alívio visual nas folgas e clareza absoluta nos plantões.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Configuração de Escala (Presets ou Construtor Customizado 3x2, etc.)**:
   - Presets imediatos: **12x36** (dia sim, dia não), **6x1**, **5x2**, **4x2**.
   - **Modo Personalizado**:
     - O usuário digita a fórmula (ex: 3 dias de trabalho x 2 de folga).
     - Abre um seletor visual de 5 dias no calendário onde a pessoa clica para marcar: Dia 1 (Trabalho), Dia 2 (Trabalho), Dia 3 (Trabalho), Dia 4 (Folga), Dia 5 (Folga).
     - O app valida o ciclo e projeta a escala infinita para trás e para frente.
2. **Dashboard & Calendário Mensal**:
   - Topo: **Card Herói de Hoje** ("Hoje: PLANTÃO NOTURNO - Próxima folga amanhã!").
   - Calendário M3 com códigos visuais:
     - 🟢 Verde: Folga
     - 🔵 Azul Escuro / Grafite: Plantão Normal
     - 🟠 Âmbar / Laranja: Folga Trabalhada (FT)
     - 🟣 Roxo: Troca de Plantão
     - 🇧🇷 Ponto Amarelo: Feriado Nacional/Estadual
     - 📌 Marcador: Compromisso agendado
3. **Assistente Rápido de Dúvidas ("Estou Livre?")**:
   - Barra interativa com botão de microfone e sugestões rápidas ("Próximo Feriado", "Natal", "Carnaval", ou data específica).
   - Card de resposta dinâmico e amigável:
     - *"No dia 15/11/2026 (Proclamação da República), você estará de **FOLGA**! 🎉 Aproveite!"*
     - *"No dia 25/12/2026, você estará de **PLANTÃO**! ⚠️ Caso queira, você pode marcar uma Troca ou registrar como FT."*
4. **Registro de FT (Folga Trabalhada) e Trocas**:
   - Ao tocar em qualquer dia de folga no calendário:
     - Opção: *Transformar em Folga Trabalhada (FT)* (com anotação de valor/posto).
     - Opção: *Registrar Troca de Plantão* (informando nome do parceiro da troca).
5. **Relatório & Resumo Mensal**:
   - Aba ou gaveta de resumo: Total de plantões no mês, total de folgas reais, número de FTs feitas e trocas pendentes.
6. **Compromissos & Alertas com Antecedência**:
   - Agendamento com hora e seleção de alerta (ex: 1 hora antes, véspera).
   - Se coincidir com plantão, sugere automaticamente registrar a troca de plantão.

### Identidade Visual & Tema
- **Vibe**: Tático, sofisticado, moderno (estilo equipamento de alta precisão).
- **Paleta**:
  - Superfícies: Dark Slate (`#0B1120`, `#1E293B`) com suporte a Light Clean.
  - Primária: Tactical Cobalt Blue (`#2563EB`) para ações e plantão diurno.
  - Folga: Vibrant Emerald (`#10B981`) para folgas bem aproveitadas.
  - FT (Folga Trabalhada): Deep Amber / Gold (`#F59E0B`).
  - Troca: Electric Violet (`#8B5CF6`).

---

## 3. Key Product Decisions & Trade-Offs

- **Motor Modular de Ciclos (Cycle Engine)**:
  - O cálculo aceita qualquer padrão arbitrário de $N$ dias de trabalho e $M$ dias de folga a partir de um array booleano de estados `[true, true, true, false, false]` ancorado em uma data base. Cálculo instantâneo $O(1)$.
- **Sobrescritas Pontuais (Overlays de FT e Trocas)**:
  - O status de um dia é composto: `Status Base (da escala)` + `Sobrescrita (se houver FT ou Troca registrada para aquela data específica)`.
  - Isso garante que a regra matemática da escala continue intacta para o resto do ano, mesmo que o usuário faça 3 FTs neste mês.
- **Pesquisa em Linguagem Natural Local (Regex + Date Parser)**:
  - O assistente de dúvidas interpreta termos comuns ("amanhã", "depois de amanhã", "domingo que vem", "15/11", "natal", "carnaval") e datas por voz/texto de forma 100% offline, sem custos nem latência de APIs externas.
- **Banco de Dados Room**:
  - Tabelas: `ScheduleConfig` (definição da escala e UF), `DayOverride` (FTs e Trocas de plantão), `Appointment` (compromissos e notas).

---

## 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────────┐
│                      Compose UI Screens                          │
│                                                                  │
│  ┌────────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │ DashboardScreen    │  │  CalendarScreen  │  │ Voice/Quick   │ │
│  │ (Status + Resumo)  │  │ (Grid + Filtros) │  │ Question Bar  │ │
│  └─────────┬──────────┘  └────────┬─────────┘  └───────┬───────┘ │
│            │                      │                    │         │
│  ┌─────────┴──────────────────────┴────────────────────┴───────┐ │
│  │ CustomCycleBuilder / DayActionSheet (FT/Troca) / NewEvent   │ │
│  └────────────────────────────────┬────────────────────────────┘ │
└───────────────────────────────────┼──────────────────────────────┘
                                    ▼
┌──────────────────────────────────────────────────────────────────┐
│                 ShiftViewModel & ScheduleEngine                  │
│                                                                  │
│  - getEffectiveDayStatus(date): ShiftStatus                      │
│    (BaseCycle + DayOverrides [FT / Trocas])                     │
│  - parseNaturalDateQuery(text): DateResolutionResult             │
│  - calculateMonthSummary(yearMonth): MonthlyReport               │
│  - brazilianHolidaysProvider.getHolidaysForYear(year, uf)        │
└───────────────────────────────────┬──────────────────────────────┘
                                    ▼
┌──────────────────────────────────────────────────────────────────┐
│                   Room Database (AppDatabase)                    │
│                                                                  │
│  - ScheduleConfigEntity (tipo de escala, ciclo de dias, UF)      │
│  - DayOverrideEntity (data, tipo: FT | TROCA, parceiro, notas)   │
│  - AppointmentEntity (data, horário, título, lembrete)           │
└──────────────────────────────────────────────────────────────────┘
```

---

## 5. Implementation Steps

1. **Identidade da Aplicação**:
   - `metadata.json`: Atualizar para *"Plantão & Folga"* com descrição focada no gestor de escalas e FTs.
   - `res/values/strings.xml`: Nome da aplicação e strings das ações.
2. **Motor de Cálculo com Suporte a FT, Trocas e Ciclos Customizados**:
   - `ScheduleEngine`: Suporte a escalas pré-definidas (12x36, 6x1, 5x2, 4x2) e ciclo arbitrário configurado visualmente.
   - `BrazilianHolidayProvider`: Feriados nacionais móveis e fixos + feriados de todas as 27 UFs do Brasil.
   - `QuickQueryParser`: Interpretador inteligente de datas por texto/voz.
3. **Camada de Persistência (Room Database)**:
   - `ScheduleConfigEntity` (armazenando o padrão do ciclo e UF).
   - `DayOverrideEntity` (para registrar Folgas Trabalhadas FT e Trocas de Plantão).
   - `AppointmentEntity` (compromissos e observações).
   - DAOs e `AppDatabase` com TypeConverters.
4. **Interface Jetpack Compose**:
   - **Construtor Visual de Escala Customizada**: Escolha de $N$ de trabalho e $M$ de folga, com mini-calendário para clicar e preencher a sequência.
   - **Tela Principal (Dashboard & Calendário Completo)**:
     - Card de hoje e próxima folga.
     - Calendário mensal com cores táteis para Plantão, Folga, FT, Troca e Feriados.
   - **Assistente "Estou de Folga?" (Texto & Voz)**:
     - Atalho de voz via `SpeechRecognizer` nativo do Android e campo de texto com respostas instantâneas.
   - **Bottom Sheet de Ação do Dia**:
     - Ver detalhes do dia (se é feriado estadual/nacional, plantão, folga).
     - Botão *Registrar FT* ou *Registrar Troca de Plantão*.
     - Botão *Adicionar Compromisso / Observação*.
   - **Painel de Resumo / Relatório do Mês**:
     - Total de plantões realizados, total de FTs feitas e folgas.
5. **Verificação & Compilação**:
   - Compilação limpa com `compile_applet`.

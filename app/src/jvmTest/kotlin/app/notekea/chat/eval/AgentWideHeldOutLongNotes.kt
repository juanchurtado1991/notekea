package app.notekea.chat.eval

/** Held-out wider tasks over the newer long notes of `Agent Eval` ([AgentWideHeldOutCases]). */
internal object AgentWideHeldOutLongNotes {
    private val READ = listOf("outline", "read", "answer")
    private val LOOK_UP = listOf("search", "read", "answer")
    private val SUM = listOf("search", "read", "calculate", "answer")
    private val ASK_NOTE = listOf("list", "ask_user")
    private val ASK_NAME = listOf("search", "ask_user")

    // The statistics course's lecture notes, week by week.
    val course = listOf(
        AgentWideCase(
            task = "in my statistics lecture notes, what z value do we use for a 95 % confidence " +
                "interval?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("1.96")
        ),
        AgentWideCase(
            task = "how many points are all the homeworks in my statistics notes worth together?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: 3 × 10 + 3 × 15 + 3 × 20 + 3 × 25
            computed = listOf("210")
        ),
        AgentWideCase(
            task = "when and where is the final exam of my statistics course, and what may I " +
                "bring?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("B-204", "A4")
        ),
        AgentWideCase(
            task = "write a note Regression cheat sheet from the regression weeks of my " +
                "statistics notes",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Regression cheat sheet")
        ),
        AgentWideCase(
            task = "what p-value did the coffee-tasting example in my statistics notes give?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("0.03")
        ),
        AgentWideCase(
            task = "what grade do I need to pass my statistics course, per my lecture notes?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("search", "answer"),
            ends = AgentWideCase.End.ANSWER,
            lacks = listOf("70")
        ),
        AgentWideCase(
            task = "en mis apuntes de estadística, ¿con qué datos trabajamos la semana de ANOVA?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Coto Brus")
        )
    )

    // The Japan journal, day by day.
    val journal = listOf(
        AgentWideCase(
            task = "where did I lose my umbrella in Japan?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Ginza line")
        ),
        AgentWideCase(
            task = "what do the daily Spent figures of my Japan journal add up to?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: the 14 days' figures, 6,400 + 9,800 + … + 3,600 yen
            computed = listOf("124,900")
        ),
        AgentWideCase(
            task = "what train did we take from Tokyo to Kyoto, and how long was the ride?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Nozomi")
        ),
        AgentWideCase(
            task = "write a note Japan restaurants with every restaurant in my Japan journal and " +
                "its city",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Japan restaurants")
        ),
        AgentWideCase(
            task = "en mi diario de Japón, ¿desde dónde vimos por fin el Fuji?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Oishi")
        ),
        AgentWideCase(
            task = "how far did we cycle around Lake Kawaguchi, per my Japan journal?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("18")
        ),
        AgentWideCase(
            task = "what did we do on the last morning before the airport, per my Japan journal?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Yanaka")
        ),
        AgentWideCase(
            task = "who paid the ryokan in Kyoto, and how much does Tomás owe for it, per my " +
                "journal?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("45,000")
        )
    )

    // The chess club's minutes of the year, in Spanish.
    val minutes = listOf(
        AgentWideCase(
            task = "en las actas del club de ajedrez, ¿quién ganó el torneo interno de junio?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Rodrigo Chacón")
        ),
        AgentWideCase(
            task = "según la última acta del club de ajedrez, ¿cuánto dinero quedó en caja?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("412")
        ),
        AgentWideCase(
            task = "¿cuántos socios nuevos entraron al club de ajedrez en 2026, según las actas?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: 2 + 1 + 0 + 3 + 1 + 2 + 4 + 0 + 1 + 2, January to October
            computed = listOf("16")
        ),
        AgentWideCase(
            task = "haz una nota Acuerdos del club 2026 con los acuerdos de cada acta del club " +
                "de ajedrez",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Acuerdos del club 2026")
        ),
        AgentWideCase(
            task = "¿en qué acta se acordó subir la cuota anual del club de ajedrez, y a cuánto?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("septiembre", "60")
        ),
        AgentWideCase(
            task = "¿cuánto gastó el club de ajedrez en tableros y relojes en 2026, según las " +
                "actas?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: 4 × 18 boards + 3 × 35 clocks + 2 × 35 clocks USD
            computed = listOf("247")
        ),
        AgentWideCase(
            task = "in the chess club minutes, which session had the most members present, and " +
                "how many?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("23")
        )
    )
}

package app.notekea.chat.eval

/** Held-out wider tasks over the newer long documents of `Agent Eval` ([AgentWideHeldOutCases]). */
internal object AgentWideHeldOutDocuments {
    private val READ = listOf("outline", "read", "answer")
    private val LOOK_UP = listOf("search", "read", "answer")
    private val SUM = listOf("search", "read", "calculate", "answer")
    private val ASK_NOTE = listOf("list", "ask_user")
    private val ASK_NAME = listOf("search", "ask_user")

    // The flat's lease, in Spanish, read in English and in Spanish.
    val lease = listOf(
        AgentWideCase(
            task = "¿cuánto es la renta del apartamento de Barreal y en qué plazo se paga cada " +
                "mes?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("650")
        ),
        AgentWideCase(
            task = "según el contrato de arrendamiento, ¿cuánto puede pesar una mascota y cuánto " +
                "es su depósito?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("10", "150")
        ),
        AgentWideCase(
            task = "si pago la renta de marzo el día 12, ¿cuánto recargo me cobran según el " +
                "contrato de arrendamiento?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("10"),
            // worked out: 10 USD a day from day 6 to day 12, both counted: 7 × 10, under 100
            computed = listOf("70")
        ),
        AgentWideCase(
            task = "¿cuánto será la renta mensual en el segundo año del contrato de arrendamiento?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: 650 × 1.04
            computed = listOf("676")
        ),
        AgentWideCase(
            task = "haz una nota Inventario del apartamento con la tabla del Anexo A del " +
                "contrato de arrendamiento",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Inventario del apartamento")
        ),
        AgentWideCase(
            task = "under the lease of my flat, which repairs are mine to pay, and how fast must " +
                "the landlady fix an urgent one?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("75", "24")
        ),
        AgentWideCase(
            task = "what fine does the lease of my flat set for losing the keys?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "answer"),
            ends = AgentWideCase.End.ANSWER,
            lacks = listOf("fine of")
        ),
        // Loose but clear: Rosa is the landlady of one contract only.
        AgentWideCase(
            task = "until when does my contract with Rosa run?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("2030"),
            lacks = listOf("2028")
        )
    )

    // The shelter's volunteer handbook.
    val handbook = listOf(
        AgentWideCase(
            task = "how old must a volunteer at the shelter be, and what does one under 18 need?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("16", "consent")
        ),
        AgentWideCase(
            task = "I drove 180 km for the shelter in October and 260 km in November: what can I " +
                "claim for each month under the volunteer handbook?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("60"),
            // worked out: 180 × 0.30 = 54; 260 × 0.30 = 78, held to the 60 a month
            computed = listOf("54")
        ),
        AgentWideCase(
            task = "if I foster two cats for three months, how much food money does the " +
                "shelter's handbook say I get?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: 25 USD × 2 animals × 3 months
            computed = listOf("150")
        ),
        AgentWideCase(
            task = "make a note Shelter contacts with the contacts table of the volunteer handbook",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Shelter contacts")
        ),
        AgentWideCase(
            task = "what does the volunteer handbook give volunteers for meals on a shift?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "answer"),
            ends = AgentWideCase.End.ANSWER,
            lacks = listOf("per meal")
        ),
        AgentWideCase(
            task = "según el manual de voluntarios del refugio, ¿qué día y a qué hora son las " +
                "ferias de adopción?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("9:00", "14:00")
        )
    )

    // The water audit, with its tables and annexes.
    val audit = listOf(
        AgentWideCase(
            task = "which sampling point had the highest turbidity in the water audit, and what " +
                "was it?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("P-09", "4.8")
        ),
        AgentWideCase(
            task = "how much water can the three tanks of the water audit store together?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: 250 + 120 + 60 m³
            computed = listOf("430")
        ),
        AgentWideCase(
            task = "per the water audit, how much water was produced but not billed from March " +
                "to August in all?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: 196,000 produced − 135,300 billed m³
            computed = listOf("60,700")
        ),
        AgentWideCase(
            task = "what would all the recommendations of the water audit cost together?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            // worked out: 2,800 + 41,000 + 6,450 + 3,900 + 1,200 + 350 + 0 USD
            computed = listOf("55,700")
        ),
        AgentWideCase(
            task = "write a note Audit findings listing each finding of the water audit with its " +
                "severity",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Audit findings")
        ),
        AgentWideCase(
            task = "what lead levels did the water audit measure?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "answer"),
            ends = AgentWideCase.End.ANSWER,
            lacks = listOf("µg/L", "ppb")
        ),
        // Loose but clear: the vault has one report.
        AgentWideCase(
            task = "¿qué recomienda el informe hacer primero?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Tanque Alto")
        ),
        AgentWideCase(
            task = "según la auditoría del agua, ¿en qué calle se repararon más fugas?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Calle El Cedro")
        )
    )
}

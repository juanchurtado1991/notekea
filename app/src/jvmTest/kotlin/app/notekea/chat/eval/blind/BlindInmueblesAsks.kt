package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesAsks {
    private val asks = listOf(
        ask(
            task = "cuánto suma el presupuesto 2027?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Finanzas", "Mantenimiento")
        ),
        ask(
            task = "mete 300 € de imprevistos en el presupuesto 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Finanzas", "Mantenimiento")
        ),
        ask(
            task = "qué honorarios le cobro a Javier?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ortega", "Lozano")
        ),
        ask(
            task = "añade una tarea para llamar a Javier el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ortega", "Lozano")
        ),
        ask(
            task = "cuándo se le acaba el contrato a Laura?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Pons", "Benítez")
        ),
        ask(
            task = "Laura ya ha pagado noviembre, apúntalo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Pons", "Benítez")
        ),
        ask(
            task = "cambia la visita de Jaén 9 a las 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hugo Marín", "Sofía Llorente", "Bruno Esteve")
        ),
        ask(
            task = "cierra la incidencia de Toledo 14",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Humedad", "armario")
        ),
        ask(
            task = "a qué hora es la visita de Bravo Murillo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Adriana Coll", "Martín Quiroga")
        )
    )

    private val traps = listOf(
        answer(
            task = "qué honorarios le cobro a Javier Lozano?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("95"),
            lacks = listOf("70")
        ),
        answer(
            task = "cuál es la partida más cara del presupuesto 2027 de mantenimiento?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("caldera", "2300"),
            lacks = listOf("autónomos")
        ),
        change(
            task = "cambia la visita del sábado de Jaén 9 a las 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Visitas Jaén 9", "18:00")
        ),
        answer(
            task = "cuándo se acaban los contratos de las dos Lauras?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Pons", "Benítez", "2029", "2028")
        ),
        change(
            task = "Laura Benítez me ha pagado hoy noviembre, apúntalo en cobros",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Cobros 2026", "Laura Benítez"),
            lacks = listOf("Laura Pons")
        )
    )

    val all = asks + traps
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesAsks {
    private val asks = listOf(
        AgentWideCase(
            task = "cuánto suma el presupuesto 2027?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Finanzas", "Mantenimiento")
        ),
        AgentWideCase(
            task = "mete 300 € de imprevistos en el presupuesto 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Finanzas", "Mantenimiento")
        ),
        AgentWideCase(
            task = "qué honorarios le cobro a Javier?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ortega", "Lozano")
        ),
        AgentWideCase(
            task = "añade una tarea para llamar a Javier el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ortega", "Lozano")
        ),
        AgentWideCase(
            task = "cuándo se le acaba el contrato a Laura?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pons", "Benítez")
        ),
        AgentWideCase(
            task = "Laura ya ha pagado noviembre, apúntalo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pons", "Benítez")
        ),
        AgentWideCase(
            task = "cambia la visita de Jaén 9 a las 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hugo Marín", "Sofía Llorente", "Bruno Esteve")
        ),
        AgentWideCase(
            task = "cierra la incidencia de Toledo 14",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Humedad", "armario")
        ),
        AgentWideCase(
            task = "a qué hora es la visita de Bravo Murillo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Adriana Coll", "Martín Quiroga")
        )
    )

    private val traps = listOf(
        AgentWideCase(
            task = "qué honorarios le cobro a Javier Lozano?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("95"),
            lacks = listOf("70")
        ),
        AgentWideCase(
            task = "cuál es la partida más cara del presupuesto 2027 de mantenimiento?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("caldera", "2300"),
            lacks = listOf("autónomos")
        ),
        AgentWideCase(
            task = "cambia la visita del sábado de Jaén 9 a las 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Visitas Jaén 9", "18:00")
        ),
        AgentWideCase(
            task = "cuándo se acaban los contratos de las dos Lauras?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Pons", "Benítez", "2029", "2028")
        ),
        AgentWideCase(
            task = "Laura Benítez me ha pagado hoy noviembre, apúntalo en cobros",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cobros 2026", "Laura Benítez"),
            lacks = listOf("Laura Pons")
        )
    )

    val all = asks + traps
}

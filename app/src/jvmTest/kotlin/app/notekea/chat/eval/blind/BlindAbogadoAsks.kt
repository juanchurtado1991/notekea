package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAbogadoAsks {
    val all = listOf(
        AgentWideCase(
            task = "renombra la nota Octubre a Octubre 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gastos", "Honorarios")
        ),
        AgentWideCase(
            task = "ponle #prioridad a la nota de Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Mario", "Lucía")
        ),
        AgentWideCase(
            task = "pásame la audiencia de Mario al 26 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("preliminar", "testimonial", "juicio")
        ),
        AgentWideCase(
            task = "agrégame una tarea: llamarle a Jorge el lunes por lo del convenio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Treviño", "Villarreal")
        ),
        AgentWideCase(
            task = "marca como cobrado el pagaré de Arista",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("P-01", "P-02", "P-03")
        ),
        AgentWideCase(
            task = "cambia a las 12:00 la audiencia de la señora Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("convivencia", "alimentos")
        ),
        AgentWideCase(
            task = "ponle la etiqueta #cerrado a la nota de octubre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gastos", "Honorarios")
        ),
        AgentWideCase(
            task = "anota en el escrito de Lucía que ya salió el acuerdo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("pruebas", "alimentos")
        ),
        // Traps: they look ambiguous, but their words settle them.
        AgentWideCase(
            task = "cuánto cobré de honorarios en octubre, ya con el reembolso?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("37500")
        ),
        AgentWideCase(
            task = "mueve la audiencia preliminar de Mario Fernández al 2026-11-26 a las 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mario Fernández", "2026-11-26")
        ),
        AgentWideCase(
            task = "ponle #prioridad a los dos Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mario Fernández", "Lucía Fernández", "prioridad")
        ),
        AgentWideCase(
            task = "Héctor ya me pagó lo que debía, márcalo como pagado en Facturas 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("A-109"),
            lacks = listOf("A-105", "A-108")
        ),
        AgentWideCase(
            task = "renombra la nota Gastos/Octubre a Octubre 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gastos", "Octubre 2026"),
            lacks = listOf("Honorarios/Octubre")
        )
    )
}

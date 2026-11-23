package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAbogadoAsks {
    val all = listOf(
        ask(
            task = "renombra la nota Octubre a Octubre 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Gastos", "Honorarios")
        ),
        ask(
            task = "ponle #prioridad a la nota de Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Mario", "Lucía")
        ),
        ask(
            task = "pásame la audiencia de Mario al 26 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("preliminar", "testimonial", "juicio")
        ),
        ask(
            task = "agrégame una tarea: llamarle a Jorge el lunes por lo del convenio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Treviño", "Villarreal")
        ),
        ask(
            task = "marca como cobrado el pagaré de Arista",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("P-01", "P-02", "P-03")
        ),
        ask(
            task = "cambia a las 12:00 la audiencia de la señora Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("convivencia", "alimentos")
        ),
        ask(
            task = "ponle la etiqueta #cerrado a la nota de octubre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Gastos", "Honorarios")
        ),
        ask(
            task = "anota en el escrito de Lucía que ya salió el acuerdo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("pruebas", "alimentos")
        ),
        // Traps: they look ambiguous, but their words settle them.
        answer(
            task = "cuánto cobré de honorarios en octubre, ya con el reembolso?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("37500")
        ),
        change(
            task = "mueve la audiencia preliminar de Mario Fernández al 2026-11-26 a las 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Mario Fernández", "2026-11-26")
        ),
        change(
            task = "ponle #prioridad a los dos Fernández",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("Mario Fernández", "Lucía Fernández", "prioridad")
        ),
        change(
            task = "Héctor ya me pagó lo que debía, márcalo como pagado en Facturas 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("A-109"),
            lacks = listOf("A-105", "A-108")
        ),
        change(
            task = "renombra la nota Gastos/Octubre a Octubre 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Gastos", "Octubre 2026"),
            lacks = listOf("Honorarios/Octubre")
        )
    )
}

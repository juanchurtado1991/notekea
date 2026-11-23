package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Streamer notes cannot settle, then look-alikes whose words do settle them. */
internal object BlindStreamerAsks {
    val all = listOf(
        ask(
            task = "mueve el collab con Santi al sábado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Morales", "Herrera")
        ),
        ask(
            task = "add Santi as a mod in the chat rules",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Morales", "Herrera")
        ),
        ask(
            task = "en el presupuesto cambia el disponible a 300.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("equipo")
        ),
        ask(
            task = "agrega maratón de juegos de terror a mis ideas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("contenido")
        ),
        ask(
            task = "cancela el pedido de Sonar Audio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("brazo", "XLR")
        ),
        ask(
            task = "en el calendario de contenido pasa el video pendiente al 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hollow Lantern", "Orbe", "setup")
        ),
        ask(
            task = "mark the pending invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Nébula", "Orbe")
        ),
        ask(
            task = "anota en la nota de Laura que cumple años el 3 de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pineda", "Quintero")
        ),
        change(
            task = "mueve el collab con Santi Herrera al sábado 28",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Santi Herrera", "Collabs pendientes")
        ),
        change(
            task = "marca como pagada la factura de Teclados Orbe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("FAC-025", "pagada")
        ),
        change(
            task = "en el presupuesto de equipo sube el tope a 1.800.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("1.800.000")
        ),
        change(
            task = "add speedrun relay to my collab ideas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "propose"),
            holds = listOf("speedrun relay")
        ),
        answer(
            task = "what time are the collabs with both Santis?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("20:00", "16:00")
        )
    )
}

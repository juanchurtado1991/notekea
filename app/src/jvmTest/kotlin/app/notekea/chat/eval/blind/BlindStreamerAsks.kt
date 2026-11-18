package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Streamer notes cannot settle, then look-alikes whose words do settle them. */
internal object BlindStreamerAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve el collab con Santi al sábado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Herrera")
        ),
        AgentWideCase(
            task = "add Santi as a mod in the chat rules",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Herrera")
        ),
        AgentWideCase(
            task = "en el presupuesto cambia el disponible a 300.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("equipo")
        ),
        AgentWideCase(
            task = "agrega maratón de juegos de terror a mis ideas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("contenido")
        ),
        AgentWideCase(
            task = "cancela el pedido de Sonar Audio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("brazo", "XLR")
        ),
        AgentWideCase(
            task = "en el calendario de contenido pasa el video pendiente al 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hollow Lantern", "Orbe", "setup")
        ),
        AgentWideCase(
            task = "mark the pending invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Nébula", "Orbe")
        ),
        AgentWideCase(
            task = "anota en la nota de Laura que cumple años el 3 de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pineda", "Quintero")
        ),
        AgentWideCase(
            task = "mueve el collab con Santi Herrera al sábado 28",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Santi Herrera", "Collabs pendientes")
        ),
        AgentWideCase(
            task = "marca como pagada la factura de Teclados Orbe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("FAC-025", "pagada")
        ),
        AgentWideCase(
            task = "en el presupuesto de equipo sube el tope a 1.800.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("1.800.000")
        ),
        AgentWideCase(
            task = "add speedrun relay to my collab ideas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("speedrun relay")
        ),
        AgentWideCase(
            task = "what time are the collabs with both Santis?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20:00", "16:00")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Streamer notes, each one clear from its words and the notes. */
internal object BlindStreamerChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrega una tarea en Metas 2026: comprar micrófono de respaldo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Metas 2026", "micrófono de respaldo")
        ),
        AgentWideCase(
            task = "ya me pagaron la FAC-023, márcala como pagada",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("FAC-023", "pagada")
        ),
        AgentWideCase(
            task = "set the status of the Nébula Sillas contract to cerrado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Patrocinio Nébula Sillas", "cerrado")
        ),
        AgentWideCase(
            task = "ponle la etiqueta cobro-pendiente a cada contrato de patrocinio que tenga " +
                "una factura sin pagar",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Nébula Sillas", "Teclados Orbe", "cobro-pendiente")
        ),
        AgentWideCase(
            task = "agrega al registro de noviembre un stream de hoy: 2 h de Just Chatting, " +
                "41 de pico",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Registro de streams noviembre", "41")
        ),
        AgentWideCase(
            task = "crea una nota Resumen sponsors 2026 con cada patrocinador y cuánto me paga",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Resumen sponsors 2026",
                "Café Altamira",
                "Nébula Sillas",
                "Teclados Orbe",
                "800.000"
            ),
            lacks = listOf("750.000")
        ),
        AgentWideCase(
            task = "renombra Clips para editar a Clips Mateo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Clips para editar", "Clips Mateo")
        ),
        AgentWideCase(
            task = "move the Maratón benéfica note into the Stream folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Maratón benéfica", "Stream")
        ),
        AgentWideCase(
            task = "en el calendario de contenido pasa el video Top 5 sustos de Hollow Lantern " +
                "al 2026-11-24",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Top 5 sustos de Hollow Lantern", "2026-11-24")
        ),
        AgentWideCase(
            task = "ponle rating 4 a la nota de la serie de Hollow Lantern",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Serie Hollow Lantern", "rating")
        ),
        AgentWideCase(
            task = "in my pending collabs, add the task elegir juego under every collab " +
                "that has no game yet",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Herrera", "Natalia Vega", "elegir juego")
        ),
        AgentWideCase(
            task = "me llegó hoy el brazo del micrófono, márcalo como entregado en pedidos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("brazo articulado", "entregado")
        )
    )
}

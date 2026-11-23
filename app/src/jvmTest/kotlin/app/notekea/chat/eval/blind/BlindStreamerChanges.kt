package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Streamer notes, each one clear from its words and the notes. */
internal object BlindStreamerChanges {
    val all = listOf(
        change(
            task = "agrega una tarea en Metas 2026: comprar micrófono de respaldo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Metas 2026", "micrófono de respaldo")
        ),
        change(
            task = "ya me pagaron la FAC-023, márcala como pagada",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("FAC-023", "pagada")
        ),
        change(
            task = "set the status of the Nébula Sillas contract to cerrado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Patrocinio Nébula Sillas", "cerrado")
        ),
        change(
            task = "ponle la etiqueta cobro-pendiente a cada contrato de patrocinio que tenga " +
                "una factura sin pagar",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "add_tag", "propose"),
            holds = listOf("Nébula Sillas", "Teclados Orbe", "cobro-pendiente")
        ),
        change(
            task = "agrega al registro de noviembre un stream de hoy: 2 h de Just Chatting, " +
                "41 de pico",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Registro de streams noviembre", "41")
        ),
        change(
            task = "crea una nota Resumen sponsors 2026 con cada patrocinador y cuánto me paga",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Resumen sponsors 2026",
                "Café Altamira",
                "Nébula Sillas",
                "Teclados Orbe",
                "800.000"
            ),
            lacks = listOf("750.000")
        ),
        change(
            task = "renombra Clips para editar a Clips Mateo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Clips para editar", "Clips Mateo")
        ),
        change(
            task = "move the Maratón benéfica note into the Stream folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Maratón benéfica", "Stream")
        ),
        change(
            task = "en el calendario de contenido pasa el video Top 5 sustos de Hollow Lantern " +
                "al 2026-11-24",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Top 5 sustos de Hollow Lantern", "2026-11-24")
        ),
        change(
            task = "ponle rating 4 a la nota de la serie de Hollow Lantern",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Serie Hollow Lantern", "rating")
        ),
        change(
            task = "in my pending collabs, add the task elegir juego under every collab " +
                "that has no game yet",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Herrera", "Natalia Vega", "elegir juego")
        ),
        change(
            task = "me llegó hoy el brazo del micrófono, márcalo como entregado en pedidos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("brazo articulado", "entregado")
        )
    )
}

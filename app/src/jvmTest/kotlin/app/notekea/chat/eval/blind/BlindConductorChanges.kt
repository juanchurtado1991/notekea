package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorChanges {
    val all = listOf(
        change(
            task = "agrégame en pendientes de la casa: pagar la luz antes del 20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Pendientes", "pagar la luz")
        ),
        change(
            task = "marca como pagada la multa de la vuelta prohibida, la pagué hoy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Multas", "vuelta prohibida")
        ),
        change(
            task = "ponle a la bitácora del 10 la propiedad clima: lluvia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("2026-11-10", "lluvia")
        ),
        change(
            task = "etiqueta la nota de los términos de Jalo con #contrato",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Términos Jalo", "contrato")
        ),
        change(
            task = "apunta en la nota de Raite que desde hoy el bono es por 45 viajes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Raite", "45 viajes")
        ),
        change(
            task = "hazme una nota Resumen mecánicos con el teléfono de cada Toño",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Resumen mecánicos", "33 1842 7730", "33 2107 5584")
        ),
        change(
            task = "mueve la nota de Incidentes a la carpeta Coche",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Incidentes", "Coche")
        ),
        change(
            task = "renombra Viajes largos a Viajes foráneos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Viajes foráneos")
        ),
        change(
            task = "la cita de la alineación pásala al 21, misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Citas taller", "alineación", "16:30")
        ),
        change(
            task = "ponle #debo a la nota de cada mecánico al que le deba dinero",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Toño Villaseñor", "debo"),
            lacks = listOf("Personas/Toño Ramírez")
        ),
        change(
            task = "agrega al mantenimiento el cambio de aceite de hoy con Toño Ramírez, $1,200",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Mantenimiento", "1,200")
        ),
        change(
            task = "en el crédito del carro marca la mensualidad de noviembre como pagada",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Crédito auto", "pagad")
        )
    )
}

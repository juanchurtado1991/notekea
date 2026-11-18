package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrégame en pendientes de la casa: pagar la luz antes del 20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pendientes", "pagar la luz")
        ),
        AgentWideCase(
            task = "marca como pagada la multa de la vuelta prohibida, la pagué hoy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Multas", "vuelta prohibida")
        ),
        AgentWideCase(
            task = "ponle a la bitácora del 10 la propiedad clima: lluvia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "lluvia")
        ),
        AgentWideCase(
            task = "etiqueta la nota de los términos de Jalo con #contrato",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Términos Jalo", "contrato")
        ),
        AgentWideCase(
            task = "apunta en la nota de Raite que desde hoy el bono es por 45 viajes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Raite", "45 viajes")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen mecánicos con el teléfono de cada Toño",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Resumen mecánicos", "33 1842 7730", "33 2107 5584")
        ),
        AgentWideCase(
            task = "mueve la nota de Incidentes a la carpeta Coche",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Incidentes", "Coche")
        ),
        AgentWideCase(
            task = "renombra Viajes largos a Viajes foráneos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Viajes foráneos")
        ),
        AgentWideCase(
            task = "la cita de la alineación pásala al 21, misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Citas taller", "alineación", "16:30")
        ),
        AgentWideCase(
            task = "ponle #debo a la nota de cada mecánico al que le deba dinero",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Toño Villaseñor", "debo"),
            lacks = listOf("Personas/Toño Ramírez")
        ),
        AgentWideCase(
            task = "agrega al mantenimiento el cambio de aceite de hoy con Toño Ramírez, $1,200",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mantenimiento", "1,200")
        ),
        AgentWideCase(
            task = "en el crédito del carro marca la mensualidad de noviembre como pagada",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Crédito auto", "pagad")
        )
    )
}

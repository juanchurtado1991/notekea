package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesChanges {
    val all = listOf(
        AgentWideCase(
            task = "tarea en Bravo Murillo 212: revisar el inventario con Paula antes del 15/01",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bravo Murillo 212", "inventario")
        ),
        AgentWideCase(
            task = "pon Jaén 9 como reservado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jaén 9", "reservado")
        ),
        AgentWideCase(
            task = "etiqueta Delicias 40 con #moroso",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Delicias 40", "moroso")
        ),
        AgentWideCase(
            task = "añade a proveedores el antenista: Antenas Sol, Iván Sol, 622 310 845",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Proveedores", "Iván Sol")
        ),
        AgentWideCase(
            task = "hazme una nota con los pisos de Javier Ortega, su inquilino y su renta",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Toledo 14", "Ercilla 22", "Laura Pons", "Diego Salas", "1150", "890")
        ),
        AgentWideCase(
            task = "mueve Renovaciones y IPC a la carpeta Finanzas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Renovaciones y IPC", "Finanzas")
        ),
        AgentWideCase(
            task = "renombra Candidatos a Candidatos Jaén 9",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Candidatos Jaén 9")
        ),
        AgentWideCase(
            task = "la visita de Hugo Marín pásala al viernes 13 a las 17:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Visitas Jaén 9", "17:30")
        ),
        AgentWideCase(
            task = "pon #pago-pendiente en los pisos que todavía no han pagado noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("pago-pendiente", "Toledo 14", "Fuencarral 88", "Delicias 40"),
            lacks = listOf(
                "Ercilla 22",
                "Alonso Cano 5",
                "Arganzuela 3",
                "Bravo Murillo 212",
                "Jaén 9"
            )
        ),
        AgentWideCase(
            task = "a los propietarios con más de un piso ponles honorarios_revisados: 2027",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "set_property",
                "set_property",
                "set_property",
                "propose"
            ),
            ends = End.PROPOSE,
            holds = listOf("Javier Ortega", "Carmen Vidal", "Rosa Puig"),
            lacks = listOf("Javier Lozano", "Andrés Carrasco")
        ),
        AgentWideCase(
            task = "cierra la incidencia del termo de Ercilla 22",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Incidencias abiertas", "termo")
        ),
        AgentWideCase(
            task = "enlaza la nota de Jaén 9 con sus visitas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jaén 9", "Visitas Jaén 9")
        )
    )
}

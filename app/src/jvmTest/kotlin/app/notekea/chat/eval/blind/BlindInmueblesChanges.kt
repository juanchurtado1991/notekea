package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesChanges {
    val all = listOf(
        change(
            task = "tarea en Bravo Murillo 212: revisar el inventario con Paula antes del 15/01",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Bravo Murillo 212", "inventario")
        ),
        change(
            task = "pon Jaén 9 como reservado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Jaén 9", "reservado")
        ),
        change(
            task = "etiqueta Delicias 40 con #moroso",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Delicias 40", "moroso")
        ),
        change(
            task = "añade a proveedores el antenista: Antenas Sol, Iván Sol, 622 310 845",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Proveedores", "Iván Sol")
        ),
        change(
            task = "hazme una nota con los pisos de Javier Ortega, su inquilino y su renta",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Toledo 14", "Ercilla 22", "Laura Pons", "Diego Salas", "1150", "890")
        ),
        change(
            task = "mueve Renovaciones y IPC a la carpeta Finanzas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Renovaciones y IPC", "Finanzas")
        ),
        change(
            task = "renombra Candidatos a Candidatos Jaén 9",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Candidatos Jaén 9")
        ),
        change(
            task = "la visita de Hugo Marín pásala al viernes 13 a las 17:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Visitas Jaén 9", "17:30")
        ),
        change(
            task = "pon #pago-pendiente en los pisos que todavía no han pagado noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf("pago-pendiente", "Toledo 14", "Fuencarral 88", "Delicias 40"),
            lacks = listOf(
                "Ercilla 22",
                "Alonso Cano 5",
                "Arganzuela 3",
                "Bravo Murillo 212",
                "Jaén 9"
            )
        ),
        change(
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
            holds = listOf("Javier Ortega", "Carmen Vidal", "Rosa Puig"),
            lacks = listOf("Javier Lozano", "Andrés Carrasco")
        ),
        change(
            task = "cierra la incidencia del termo de Ercilla 22",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Incidencias abiertas", "termo")
        ),
        change(
            task = "enlaza la nota de Jaén 9 con sus visitas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Jaén 9", "Visitas Jaén 9")
        )
    )
}

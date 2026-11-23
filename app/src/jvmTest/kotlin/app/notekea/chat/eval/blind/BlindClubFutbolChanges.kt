package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the club treasurer's notes, each one settled by the words and the notes. */
internal object BlindClubFutbolChanges {
    val all = listOf(
        change(
            task = "agregá a mis pendientes: cobrarle a Matías Cabrera setiembre, " +
                "octubre y noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Pendientes", "Matías Cabrera")
        ),
        change(
            task = "ponele status cerrado a Gastos octubre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Gastos octubre", "cerrado")
        ),
        change(
            task = "agregale el tag #moroso a la nota de Rodrigo Pereira",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Rodrigo Pereira", "moroso")
        ),
        change(
            task = "anotá en caja chica: 2026-11-11 cal para marcar la cancha −$480",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("Caja chica", "480")
        ),
        change(
            task = "haceme una nota Deudores noviembre con los socios que deben noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf(
                "Deudores noviembre",
                "Rodrigo Suárez",
                "Matías Cabrera",
                "Andrés Larrosa",
                "Joaquín Silveira"
            )
        ),
        change(
            task = "mové la nota de Néstor Píriz a Personas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Néstor Píriz", "Personas")
        ),
        change(
            task = "renombrá Rifa fin de año a Rifa Navidad 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Rifa Navidad 2026")
        ),
        change(
            task = "las camisetas ahora llegan el 2026-11-28, cambialo en pedidos",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Pedidos", "2026-11-28")
        ),
        change(
            task = "poné el tag #debe-rifa en las notas de los vendedores que no rindieron todo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "calculate", "search", "add_tag", "add_tag", "propose"),
            holds = listOf("Valeria Rodríguez", "Rodrigo Pereira", "debe-rifa")
        ),
        change(
            task = "en alquiler de cancha agregá una tarea: pagarle a Néstor la seña " +
                "del amistoso",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Alquiler de cancha", "seña")
        ),
        change(
            task = "en plantel primera linkeá a Rodrigo Suárez a su nota",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "link", "propose"),
            holds = listOf("Plantel primera", "Rodrigo Suárez")
        ),
        change(
            task = "escribime una nota Resumen rifa para la comisión: cuántos números " +
                "se vendieron, cuánto se rindió y quién debe",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Resumen rifa", "Valeria Rodríguez", "Rodrigo Pereira"),
            computed = listOf("135", "23200", "3800")
        )
    )
}

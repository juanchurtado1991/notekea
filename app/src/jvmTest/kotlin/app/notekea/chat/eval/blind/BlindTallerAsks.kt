package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps that only look ambiguous. */
internal object BlindTallerAsks {
    val all = listOf(
        ask(
            task = "agrega a las facturas la de hoy por 350.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("client", "proveedor")
        ),
        ask(
            task = "agrégame una tarea para llamar a Ospina el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Carlos", "Lucía")
        ),
        ask(
            task = "corre la cita de Ospina para las 11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Carlos", "Lucía")
        ),
        ask(
            task = "cancela la cita de Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Swift", "Spark")
        ),
        ask(
            task = "cancela el pedido de Autopartes Laureles",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("repartición", "Batería")
        ),
        ask(
            task = "pasa la revisión del elevador una semana después, misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("mangueras", "aceite")
        ),
        ask(
            task = "ponle una tarea a Andrés de revisar el ruido del elevador",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Gómez", "Villa")
        ),
        ask(
            task = "agrega a la orden de Marta una nota: pide factura electrónica",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Spark", "Swift")
        ),
        ask(
            task = "renombra Facturas a Facturas 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("client", "proveedor")
        ),
        answer(
            task = "¿cuánto me deben los clientes en facturas pendientes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("787")
        ),
        change(
            task = "corre la cita de Lucía Ospina para las 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Lucía Ospina", "16:00"),
            lacks = listOf("carlos ospina")
        ),
        answer(
            task = "¿en qué barrio vive el Ospina del Mazda?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Belén"),
            lacks = listOf("envigado")
        ),
        change(
            task = "agrega al final de las dos notas de facturas: revisado el 2026-11-11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            holds = listOf("client", "proveedor", "revisado")
        ),
        change(
            task = "mueve la revisión de mangueras del elevador al 19 de noviembre, misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Elevador hidráulico", "mangueras", "09:00")
        )
    )
}

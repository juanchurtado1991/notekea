package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps that only look ambiguous. */
internal object BlindTallerAsks {
    val all = listOf(
        AgentWideCase(
            task = "agrega a las facturas la de hoy por 350.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("client", "proveedor")
        ),
        AgentWideCase(
            task = "agrégame una tarea para llamar a Ospina el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Carlos", "Lucía")
        ),
        AgentWideCase(
            task = "corre la cita de Ospina para las 11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Carlos", "Lucía")
        ),
        AgentWideCase(
            task = "cancela la cita de Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Swift", "Spark")
        ),
        AgentWideCase(
            task = "cancela el pedido de Autopartes Laureles",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("repartición", "Batería")
        ),
        AgentWideCase(
            task = "pasa la revisión del elevador una semana después, misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("mangueras", "aceite")
        ),
        AgentWideCase(
            task = "ponle una tarea a Andrés de revisar el ruido del elevador",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gómez", "Villa")
        ),
        AgentWideCase(
            task = "agrega a la orden de Marta una nota: pide factura electrónica",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Spark", "Swift")
        ),
        AgentWideCase(
            task = "renombra Facturas a Facturas 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("client", "proveedor")
        ),
        AgentWideCase(
            task = "¿cuánto me deben los clientes en facturas pendientes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("787")
        ),
        AgentWideCase(
            task = "corre la cita de Lucía Ospina para las 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucía Ospina", "16:00"),
            lacks = listOf("carlos ospina")
        ),
        AgentWideCase(
            task = "¿en qué barrio vive el Ospina del Mazda?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Belén"),
            lacks = listOf("envigado")
        ),
        AgentWideCase(
            task = "agrega al final de las dos notas de facturas: revisado el 2026-11-11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("client", "proveedor", "revisado")
        ),
        AgentWideCase(
            task = "mueve la revisión de mangueras del elevador al 19 de noviembre, misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Elevador hidráulico", "mangueras", "09:00")
        )
    )
}

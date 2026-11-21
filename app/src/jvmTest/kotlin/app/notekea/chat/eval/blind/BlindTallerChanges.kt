package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Taller notes that the words make clear. */
internal object BlindTallerChanges {
    val all = listOf(
        AgentWideCase(
            task = "en la orden del Logan agrega la tarea: llamar a Lucía cuando llegue el kit",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("OT-102", "llamar a Lucía")
        ),
        AgentWideCase(
            task = "Marta ya aprobó lo del Swift, ponle estado aprobada a la orden",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("OT-107", "aprobada")
        ),
        AgentWideCase(
            task = "ponle la etiqueta #credito a Transportes La Ladera",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Transportes La Ladera", "credito")
        ),
        AgentWideCase(
            task = "agrega al inventario de herramientas una pistola de " +
                "impacto neumática, 1, nueva, en el banco 2",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Inventario de herramientas", "pistola de impacto")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen Lucía Ospina con sus " +
                "dos carros, sus órdenes y lo que debe",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Resumen Lucía Ospina", "Renault Logan", "Kia Picanto", "FV-2211")
        ),
        AgentWideCase(
            task = "mueve la nota de Transportes La Ladera " +
                "a una carpeta nueva que se llame Empresas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Transportes La Ladera", "Empresas")
        ),
        AgentWideCase(
            task = "cámbiale el nombre a la nómina por Nómina primera quincena noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Nómina primera quincena noviembre")
        ),
        AgentWideCase(
            task = "la cita de Jorge pásala para el 2026-11-20 a la misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jorge Arango", "2026-11-20", "14:00")
        ),
        AgentWideCase(
            task = "ponle #debe a todos los clientes que tengan factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marta Henao", "Lucía Ospina", "debe"),
            lacks = listOf("carlos ospina", "jorge arango")
        ),
        AgentWideCase(
            task = "ya pagué la LA-0917, actualízala",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("LA-0917", "pagada")
        ),
        AgentWideCase(
            task = "crea la orden OT-108 para el Mazda de Carlos, cambio de aceite, entra mañana",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("OT-108", "Carlos Ospina")
        ),
        AgentWideCase(
            task = "en las facturas de proveedores agrega la AL-5650 " +
                "de Autopartes Laureles por 290.000, vence 2026-12-09, pendiente",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Proveedores", "AL-5650", "290.000")
        )
    )
}

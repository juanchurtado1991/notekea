package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes for the bakery vault: tasks, properties, tags, appends, new notes, moves and renames. */
internal object BlindPanaderiaChanges {
    val all = listOf(
        change(
            task = "agrega una tarea en la nota de Julio Quispe: recoger las 12 jabas el viernes",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Julio Quispe", "jabas")
        ),
        change(
            task = "ponle status inactivo al Colegio Nuevo Horizonte",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Colegio Nuevo Horizonte", "inactivo")
        ),
        change(
            task = "ponle el tag #moroso a todos los clientes que tienen factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            holds = listOf(
                "Hostal Los Tunales",
                "Marleny Quispe",
                "Julio Quispe",
                "moroso"
            )
        ),
        change(
            task = "anota en la producción de hoy que se malogró la selladora a las 10",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("2026-11-11", "selladora")
        ),
        change(
            task = "hazme una nota Cobranza con lo que me debe cada cliente y sus días de crédito",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Cobranza", "702", "385", "512", "15 días", "30 días")
        ),
        change(
            task = "mueve la receta del panetón a una carpeta nueva Campaña Navidad",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Panetón", "Campaña Navidad")
        ),
        change(
            task = "renombra Agenda técnica a Visitas técnicas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Agenda técnica", "Visitas técnicas")
        ),
        change(
            task = "ya llegó la levadura, marca el P-121 como recibido",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("P-121", "recibido")
        ),
        change(
            task = "la cita de la cámara de frío cámbiala a las 11:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Cámara de frío", "11:30")
        ),
        change(
            task = "a Luis Condori agrégale la tarea renovar carnet de sanidad antes del 30",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Luis Condori", "carnet de sanidad")
        ),
        change(
            task = "ponle rating 5 a la receta de pan de tres puntas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Pan de tres puntas", "rating")
        ),
        change(
            task = "enlaza la nota de Marleny Quispe con Rutas de reparto",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Marleny Quispe", "Rutas de reparto")
        )
    )
}

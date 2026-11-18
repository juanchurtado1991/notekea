package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes for the bakery vault: tasks, properties, tags, appends, new notes, moves and renames. */
internal object BlindPanaderiaChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrega una tarea en la nota de Julio Quispe: recoger las 12 jabas el viernes",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Julio Quispe", "jabas")
        ),
        AgentWideCase(
            task = "ponle status inactivo al Colegio Nuevo Horizonte",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Colegio Nuevo Horizonte", "inactivo")
        ),
        AgentWideCase(
            task = "ponle el tag #moroso a todos los clientes que tienen factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Hostal Los Tunales",
                "Marleny Quispe",
                "Julio Quispe",
                "moroso"
            )
        ),
        AgentWideCase(
            task = "anota en la producción de hoy que se malogró la selladora a las 10",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-11", "selladora")
        ),
        AgentWideCase(
            task = "hazme una nota Cobranza con lo que me debe cada cliente y sus días de crédito",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cobranza", "702", "385", "512", "15 días", "30 días")
        ),
        AgentWideCase(
            task = "mueve la receta del panetón a una carpeta nueva Campaña Navidad",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Panetón", "Campaña Navidad")
        ),
        AgentWideCase(
            task = "renombra Agenda técnica a Visitas técnicas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda técnica", "Visitas técnicas")
        ),
        AgentWideCase(
            task = "ya llegó la levadura, marca el P-121 como recibido",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("P-121", "recibido")
        ),
        AgentWideCase(
            task = "la cita de la cámara de frío cámbiala a las 11:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cámara de frío", "11:30")
        ),
        AgentWideCase(
            task = "a Luis Condori agrégale la tarea renovar carnet de sanidad antes del 30",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Luis Condori", "carnet de sanidad")
        ),
        AgentWideCase(
            task = "ponle rating 5 a la receta de pan de tres puntas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pan de tres puntas", "rating")
        ),
        AgentWideCase(
            task = "enlaza la nota de Marleny Quispe con Rutas de reparto",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marleny Quispe", "Rutas de reparto")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Arquitecta notes, each settled by the words of the task and the notes. */
internal object BlindArquitectaChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrega una tarea a Casa La Reina: revisar impermeabilización de la terraza " +
                "antes del 20 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Casa La Reina", "impermeabilización")
        ),
        AgentWideCase(
            task = "cambia el status de Remodelación Providencia a cerrado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Remodelación Providencia", "cerrado")
        ),
        AgentWideCase(
            task = "ponle el tag #urgente al permiso de Ñuñoa",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Permiso Ñuñoa", "urgente")
        ),
        AgentWideCase(
            task = "en la visita de ayer a Ñuñoa agrega que Pizarro confirmó el inicio de obra " +
                "para el 23 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Visita Ñuñoa", "inicio de obra")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen cobranza con las cuotas vencidas de todos los proyectos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Resumen cobranza",
                "Patricia Muñoz",
                "Ignacio Vera",
                "1.800.000",
                "4.140.000"
            )
        ),
        AgentWideCase(
            task = "mueve la cotización de estructura de Peñalolén a la carpeta Proyectos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cotización estructura Peñalolén", "Proyectos")
        ),
        AgentWideCase(
            task = "renombra Agenda noviembre a Agenda noviembre 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda noviembre 2026")
        ),
        AgentWideCase(
            task = "marca como pagada la cuota 2 de Patricia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Honorarios", "Cuota 2")
        ),
        AgentWideCase(
            task = "ponle el tag #moroso a las notas de los clientes que tienen alguna " +
                "cuota vencida",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "date", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Patricia Muñoz", "Ignacio Vera", "moroso"),
            lacks = listOf("Rodrigo Muñoz", "Daniela Soto")
        ),
        AgentWideCase(
            task = "pasa la reunión con Daniela a las 11:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda noviembre", "11:00")
        ),
        AgentWideCase(
            task = "enlaza la visita del 4 de noviembre con la cotización de estructura",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Visita Peñalolén", "Cotización estructura Peñalolén")
        ),
        AgentWideCase(
            task = "crea una carpeta Archivo y mueve ahí Remodelación Providencia",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Archivo", "Remodelación Providencia")
        )
    )
}

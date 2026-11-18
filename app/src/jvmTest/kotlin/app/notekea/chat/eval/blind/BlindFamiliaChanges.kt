package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Familia notes, each clear from its words. */
internal object BlindFamiliaChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrégale a Valentina la tarea: plan de mejoramiento de química " +
                "para el 20 de nov",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Valentina - Colegio", "plan de mejoramiento")
        ),
        AgentWideCase(
            task = "Add insect repellent to the packing list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lista de equipaje")
        ),
        AgentWideCase(
            task = "Valentina ya entregó el laboratorio de química, márcalo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "tasks", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("laboratorio de química")
        ),
        AgentWideCase(
            task = "ya pagué la pensión de noviembre de Lucía, actualízalo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pensiones", "pagado")
        ),
        AgentWideCase(
            task = "ponle a la nota del abuelo Hernando la propiedad proxima_cita: 2026-11-27",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Abuelo Hernando", "2026-11-27")
        ),
        AgentWideCase(
            task = "ponle el tag #reprobó a cada boletín que tenga una materia por debajo de 65",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boletín Valentina 3er periodo"),
            lacks = listOf("Boletín Samuel")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen salud Samuel con su alergia, sus medicamentos " +
                "y sus próximas citas de pediatra y odontólogo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Resumen salud Samuel", "maní", "2026-11-26", "2026-11-14")
        ),
        AgentWideCase(
            task = "mueve la nota de Gloria Pineda a la carpeta Abuelos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gloria Pineda", "Abuelos")
        ),
        AgentWideCase(
            task = "Rename the Cartagena trip plan to Cartagena 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Plan Cartagena diciembre", "Cartagena 2026")
        ),
        AgentWideCase(
            task = "la cita de odontología de Lucía pásala al 2026-12-05 a la misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Citas odontólogo", "2026-12-05", "10:20")
        ),
        AgentWideCase(
            task = "anota en el diario de hoy que Samuel ganó el partido de fútbol 3 a 1",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Diario", "3 a 1"),
            computed = listOf("2026-11-11")
        ),
        AgentWideCase(
            task = "Add a to-do in Pendientes de la casa to replace Samuel's adrenaline " +
                "autoinjector before it expires, with the expiry date",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pendientes de la casa", "2027")
        )
    )
}

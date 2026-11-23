package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Familia notes, each clear from its words. */
internal object BlindFamiliaChanges {
    val all = listOf(
        change(
            task = "agrégale a Valentina la tarea: plan de mejoramiento de química " +
                "para el 20 de nov",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Valentina - Colegio", "plan de mejoramiento")
        ),
        change(
            task = "Add insect repellent to the packing list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Lista de equipaje")
        ),
        change(
            task = "Valentina ya entregó el laboratorio de química, márcalo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "tasks", "edit_note", "propose"),
            holds = listOf("laboratorio de química")
        ),
        change(
            task = "ya pagué la pensión de noviembre de Lucía, actualízalo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Pensiones", "pagado")
        ),
        change(
            task = "ponle a la nota del abuelo Hernando la propiedad proxima_cita: 2026-11-27",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Abuelo Hernando", "2026-11-27")
        ),
        change(
            task = "ponle el tag #reprobó a cada boletín que tenga una materia por debajo de 65",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_tag", "propose"),
            holds = listOf("Boletín Valentina 3er periodo"),
            lacks = listOf("Boletín Samuel")
        ),
        change(
            task = "hazme una nota Resumen salud Samuel con su alergia, sus medicamentos " +
                "y sus próximas citas de pediatra y odontólogo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Resumen salud Samuel", "maní", "2026-11-26", "2026-11-14")
        ),
        change(
            task = "mueve la nota de Gloria Pineda a la carpeta Abuelos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Gloria Pineda", "Abuelos")
        ),
        change(
            task = "Rename the Cartagena trip plan to Cartagena 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Plan Cartagena diciembre", "Cartagena 2026")
        ),
        change(
            task = "la cita de odontología de Lucía pásala al 2026-12-05 a la misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Citas odontólogo", "2026-12-05", "10:20")
        ),
        change(
            task = "anota en el diario de hoy que Samuel ganó el partido de fútbol 3 a 1",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "date", "write_note", "propose"),
            holds = listOf("Diario", "3 a 1"),
            computed = listOf("2026-11-11")
        ),
        change(
            task = "Add a to-do in Pendientes de la casa to replace Samuel's adrenaline " +
                "autoinjector before it expires, with the expiry date",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Pendientes de la casa", "2027")
        )
    )
}

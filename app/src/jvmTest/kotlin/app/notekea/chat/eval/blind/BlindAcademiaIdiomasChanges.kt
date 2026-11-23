package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the academy vault, each settled by its words and the notes. */
internal object BlindAcademiaIdiomasChanges {
    val all = listOf(
        change(
            task = "añade en la semana cultural una tarea: confirmar la licencia del cine fórum " +
                "antes del 1 de diciembre",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Semana cultural", "cine fórum")
        ),
        change(
            task = "pon el italiano como cancelado",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Italiano A1", "cancelado")
        ),
        change(
            task = "etiqueta la ficha de Elena Castro con #examen-oficial",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            holds = listOf("Elena Castro", "examen-oficial")
        ),
        change(
            task = "apunta en el diario de hoy que Tomás Vega ha llamado para confirmar su prueba",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            holds = listOf("2026-11-11", "Tomás Vega")
        ),
        change(
            task = "hazme una nota 'Deudas pendientes' con quién me debe noviembre y cuánto",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            holds = listOf("Deudas pendientes", "Marta Giménez", "Sofía Marín", "Kenji Arai"),
            lacks = listOf("Andrés Molina")
        ),
        change(
            task = "mueve la jornada de puertas abiertas a Gestión",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            holds = listOf("Jornada de puertas abiertas", "Gestión")
        ),
        change(
            task = "renombra 'Español para extranjeros B2' a 'Español B2'",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            holds = listOf("Español para extranjeros B2", "Español B2")
        ),
        change(
            task = "el pedido de rotuladores al final son 42 €, cámbialo",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Pedidos de material", "42")
        ),
        change(
            task = "ponle #pago-pendiente a la ficha de cada alumno que me deba noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("pago-pendiente", "Marta Giménez", "Sofía Marín", "Kenji Arai"),
            lacks = listOf("Javier Ruiz", "Elena Castro")
        ),
        change(
            task = "a cada profe con más de 15 horas semanales ponle una tarea: revisar horario " +
                "del segundo trimestre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Lucía Moreno", "Daniel Ortega", "segundo trimestre"),
            lacks = listOf("Carmen Vidal", "Markus Feld", "Lucía Herrera")
        ),
        change(
            task = "crea una nota 'Aula 3' con lo que hay en el aula 3 cada día",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            holds = listOf("Aula 3", "Francés A2", "Alemán A1", "DELF")
        ),
        change(
            task = "apunta en la nota de Markus quién le sustituye durante la baja",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Markus Feld", "Inés Navarro")
        )
    )
}

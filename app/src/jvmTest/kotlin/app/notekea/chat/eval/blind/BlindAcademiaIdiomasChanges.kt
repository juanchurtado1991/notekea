package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the academy vault, each settled by its words and the notes. */
internal object BlindAcademiaIdiomasChanges {
    val all = listOf(
        AgentWideCase(
            task = "añade en la semana cultural una tarea: confirmar la licencia del cine fórum " +
                "antes del 1 de diciembre",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Semana cultural", "cine fórum")
        ),
        AgentWideCase(
            task = "pon el italiano como cancelado",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Italiano A1", "cancelado")
        ),
        AgentWideCase(
            task = "etiqueta la ficha de Elena Castro con #examen-oficial",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Elena Castro", "examen-oficial")
        ),
        AgentWideCase(
            task = "apunta en el diario de hoy que Tomás Vega ha llamado para confirmar su prueba",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-11", "Tomás Vega")
        ),
        AgentWideCase(
            task = "hazme una nota 'Deudas pendientes' con quién me debe noviembre y cuánto",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Deudas pendientes", "Marta Giménez", "Sofía Marín", "Kenji Arai"),
            lacks = listOf("Andrés Molina")
        ),
        AgentWideCase(
            task = "mueve la jornada de puertas abiertas a Gestión",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jornada de puertas abiertas", "Gestión")
        ),
        AgentWideCase(
            task = "renombra 'Español para extranjeros B2' a 'Español B2'",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Español para extranjeros B2", "Español B2")
        ),
        AgentWideCase(
            task = "el pedido de rotuladores al final son 42 €, cámbialo",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pedidos de material", "42")
        ),
        AgentWideCase(
            task = "ponle #pago-pendiente a la ficha de cada alumno que me deba noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("pago-pendiente", "Marta Giménez", "Sofía Marín", "Kenji Arai"),
            lacks = listOf("Javier Ruiz", "Elena Castro")
        ),
        AgentWideCase(
            task = "a cada profe con más de 15 horas semanales ponle una tarea: revisar horario " +
                "del segundo trimestre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucía Moreno", "Daniel Ortega", "segundo trimestre"),
            lacks = listOf("Carmen Vidal", "Markus Feld", "Lucía Herrera")
        ),
        AgentWideCase(
            task = "crea una nota 'Aula 3' con lo que hay en el aula 3 cada día",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Aula 3", "Francés A2", "Alemán A1", "DELF")
        ),
        AgentWideCase(
            task = "apunta en la nota de Markus quién le sustituye durante la baja",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Markus Feld", "Inés Navarro")
        )
    )
}

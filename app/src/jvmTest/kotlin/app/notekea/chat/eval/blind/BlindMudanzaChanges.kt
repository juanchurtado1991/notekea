package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Mudanza vault that the words and the notes make clear. */
internal object BlindMudanzaChanges {
    val all = listOf(
        AgentWideCase(
            task = "marca como hecha la traducción jurada del libro de familia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("libro de familia")
        ),
        AgentWideCase(
            task = "marca el piso de High Park como descartado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("High Park", "descartado")
        ),
        AgentWideCase(
            task = "tag my documents note with #urgent",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Documentos", "urgent")
        ),
        AgentWideCase(
            task = "crea la nota del diario de hoy: he llamado al Riverdale Heights y nos piden " +
                "el contrato de alquiler",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Riverdale Heights"),
            computed = listOf("2026-11-11")
        ),
        AgentWideCase(
            task = "Write a new note comparing the three apartments: rent, bedrooms and rating",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Leslieville", "Davisville", "High Park")
        ),
        AgentWideCase(
            task = "mueve la nota de Jordi Vidal a una carpeta nueva llamada Contactos Toronto",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordi Vidal", "Contactos Toronto")
        ),
        AgentWideCase(
            task = "rename my Gastos trámites note to Tasas IRCC",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tasas IRCC")
        ),
        AgentWideCase(
            task = "cambia la hora del examen médico a las 11:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("11:30", "médico")
        ),
        AgentWideCase(
            task = "añade la etiqueta #con-garaje a los pisos que ofrecen plaza de garaje",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Davisville", "High Park")
        ),
        AgentWideCase(
            task = "pon el nivel de inglés de Pau en B2",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pau", "B2")
        ),
        AgentWideCase(
            task = "añade al inventario que la bici de Martina va en el contenedor",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Inventario", "Martina")
        ),
        AgentWideCase(
            task = "add a task to Marc's job offer note: send the signed relocation form by Nov 20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marc", "relocation")
        ),
        AgentWideCase(
            task = "etiqueta con #toronto a todos los contactos que viven en Toronto",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kowalski", "Jordi")
        )
    )
}

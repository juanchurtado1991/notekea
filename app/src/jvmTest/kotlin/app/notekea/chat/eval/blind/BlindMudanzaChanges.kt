package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Mudanza vault that the words and the notes make clear. */
internal object BlindMudanzaChanges {
    val all = listOf(
        change(
            task = "marca como hecha la traducción jurada del libro de familia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("libro de familia")
        ),
        change(
            task = "marca el piso de High Park como descartado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("High Park", "descartado")
        ),
        change(
            task = "tag my documents note with #urgent",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Documentos", "urgent")
        ),
        change(
            task = "crea la nota del diario de hoy: he llamado al Riverdale Heights y nos piden " +
                "el contrato de alquiler",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "write_note", "propose"),
            holds = listOf("Riverdale Heights"),
            computed = listOf("2026-11-11")
        ),
        change(
            task = "Write a new note comparing the three apartments: rent, bedrooms and rating",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Leslieville", "Davisville", "High Park")
        ),
        change(
            task = "mueve la nota de Jordi Vidal a una carpeta nueva llamada Contactos Toronto",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Jordi Vidal", "Contactos Toronto")
        ),
        change(
            task = "rename my Gastos trámites note to Tasas IRCC",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Tasas IRCC")
        ),
        change(
            task = "cambia la hora del examen médico a las 11:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("11:30", "médico")
        ),
        change(
            task = "añade la etiqueta #con-garaje a los pisos que ofrecen plaza de garaje",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Davisville", "High Park")
        ),
        change(
            task = "pon el nivel de inglés de Pau en B2",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Pau", "B2")
        ),
        change(
            task = "añade al inventario que la bici de Martina va en el contenedor",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Inventario", "Martina")
        ),
        change(
            task = "add a task to Marc's job offer note: send the signed relocation form by Nov 20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Marc", "relocation")
        ),
        change(
            task = "etiqueta con #toronto a todos los contactos que viven en Toronto",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Kowalski", "Jordi")
        )
    )
}

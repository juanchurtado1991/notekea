package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindBodasChanges {
    val all = listOf(
        change(
            task = "agregá al cronograma de Martina y Tomás la tarea de confirmar el menú " +
                "infantil con el catering",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Paz-Gómez", "menú infantil")
        ),
        change(
            task = "Camila y Diego ya eligieron El Ombú Viejo, poné la propiedad salon en su nota",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Camila y Diego", "El Ombú Viejo")
        ),
        change(
            task = "agregale el tag #vip a la nota de Pastelería Dulce Abril",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Dulce Abril", "vip")
        ),
        change(
            task = "anotá en la nota de Sofía y Federico que Federico pidió sumar 6 invitados " +
                "más, amigos del trabajo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Sofía y Federico", "6 invitados")
        ),
        change(
            task = "armame una nota Pagos diciembre con todos los saldos pendientes que vencen " +
                "en diciembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Pagos diciembre", "Ferreyra", "Benítez", "Dulce Abril"),
            lacks = listOf("Quinta Los Aromos", "Palermo Alto")
        ),
        change(
            task = "haceme una nota Contactos Martina y Tomás con el teléfono de cada " +
                "proveedor de su boda",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Contactos Martina y Tomás", "4862-3390", "6140-2295", "4419-8806"),
            lacks = listOf("4866-1207", "3301-5584")
        ),
        change(
            task = "mové la nota de la degustación de Sofía y Federico a la carpeta de su boda",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "move", "propose"),
            holds = listOf("Degustación Luna-Ibarra", "Bodas/Luna-Ibarra")
        ),
        change(
            task = "renombrá Búsqueda de salón a Salones Rossi-Méndez",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Salones Rossi-Méndez")
        ),
        change(
            task = "pasá la reunión con Camila del 19 a las 18:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("18:00", "Camila")
        ),
        change(
            task = "poneles el tag #seña-pendiente a las notas de los proveedores que tienen " +
                "una seña sin pagar",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Catering Almagro", "Lucía Ferreyra"),
            lacks = listOf("Almagro Sur", "Benítez", "Palermo Alto")
        ),
        change(
            task = "Sofía y Federico ya tienen 64 confirmados, actualizá invitados_confirmados",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("invitados_confirmados", "64")
        ),
        change(
            task = "en el diario del 10 de noviembre agregá la tarea llamar a Bruno Carrizo " +
                "por el álbum",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Bruno Carrizo", "2026-11-10")
        )
    )
}

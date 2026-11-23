package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaChanges {
    val all = listOf(
        change(
            task = "en la nota de Droguería Pacífico Sur agrégame la tarea de llamar a Valentina " +
                "por la devolución del omeprazol",
            kind = Kind.DOCUMENT,
            tools = listOf("add_task", "propose"),
            holds = listOf("Droguería Pacífico Sur", "Valentina")
        ),
        change(
            task = "en la nota de la inspección de la Seremi cambia el status a agendada",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Inspección", "agendada")
        ),
        change(
            task = "ponle el tag #pagar-noviembre a los proveedores a los que les debo " +
                "una factura que vence este mes",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            holds = listOf("Droguería Pacífico Sur", "Laboratorio Austral Genéricos"),
            lacks = listOf("Droguería del Pacífico")
        ),
        change(
            task = "anota en Cadena de frío la temperatura de hoy en la mañana: 4,2, firma CR",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Cadena de frío", "4,2")
        ),
        change(
            task = "hazme una nota Resumen proveedores con el plazo de pago y el pedido mínimo " +
                "de cada proveedor",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Resumen proveedores",
                "del Pacífico",
                "Pacífico Sur",
                "Austral",
                "45 días",
                "60 días",
                "200"
            )
        ),
        change(
            task = "crea una carpeta Archivo y mueve ahí Ventas septiembre 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            holds = listOf("Ventas septiembre 2026", "Archivo")
        ),
        change(
            task = "renombra la nota del reglamento a Apuntes reglamento farmacias",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Apuntes reglamento farmacias")
        ),
        change(
            task = "el P-1192 se atrasa, cambia la llegada al 2026-11-20",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("P-1192", "2026-11-20")
        ),
        change(
            task = "en Stock general deja el salbutamol en 15 unidades",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Salbutamol", "15")
        ),
        change(
            task = "agrégale el tag #controlado a la nota de Camila Fuentes",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            holds = listOf("Camila Fuentes", "controlado")
        ),
        change(
            task = "crea la nota de hoy en Bitácora: llegó un reclamo por demora en una receta",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "write_note", "propose"),
            holds = listOf("Bitácora", "reclamo")
        ),
        change(
            task = "en Laboratorio Austral Genéricos cambia el plazo de pago a 90 días",
            kind = Kind.DOCUMENT,
            tools = listOf("set_property", "propose"),
            holds = listOf("Laboratorio Austral Genéricos", "90 días")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaChanges {
    val all = listOf(
        AgentWideCase(
            task = "en la nota de Droguería Pacífico Sur agrégame la tarea de llamar a Valentina " +
                "por la devolución del omeprazol",
            kind = Kind.DOCUMENT,
            tools = listOf("add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Droguería Pacífico Sur", "Valentina")
        ),
        AgentWideCase(
            task = "en la nota de la inspección de la Seremi cambia el status a agendada",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Inspección", "agendada")
        ),
        AgentWideCase(
            task = "ponle el tag #pagar-noviembre a los proveedores a los que les debo " +
                "una factura que vence este mes",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Droguería Pacífico Sur", "Laboratorio Austral Genéricos"),
            lacks = listOf("Droguería del Pacífico")
        ),
        AgentWideCase(
            task = "anota en Cadena de frío la temperatura de hoy en la mañana: 4,2, firma CR",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cadena de frío", "4,2")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen proveedores con el plazo de pago y el pedido mínimo " +
                "de cada proveedor",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
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
        AgentWideCase(
            task = "crea una carpeta Archivo y mueve ahí Ventas septiembre 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ventas septiembre 2026", "Archivo")
        ),
        AgentWideCase(
            task = "renombra la nota del reglamento a Apuntes reglamento farmacias",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Apuntes reglamento farmacias")
        ),
        AgentWideCase(
            task = "el P-1192 se atrasa, cambia la llegada al 2026-11-20",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("P-1192", "2026-11-20")
        ),
        AgentWideCase(
            task = "en Stock general deja el salbutamol en 15 unidades",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Salbutamol", "15")
        ),
        AgentWideCase(
            task = "agrégale el tag #controlado a la nota de Camila Fuentes",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Camila Fuentes", "controlado")
        ),
        AgentWideCase(
            task = "crea la nota de hoy en Bitácora: llegó un reclamo por demora en una receta",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bitácora", "reclamo")
        ),
        AgentWideCase(
            task = "en Laboratorio Austral Genéricos cambia el plazo de pago a 90 días",
            kind = Kind.DOCUMENT,
            tools = listOf("set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Laboratorio Austral Genéricos", "90 días")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindRestauranteChanges {
    val all = listOf(
        change(
            task = "agrega una tarea en Pedidos pendientes: confirmar con Abarrotes Sánchez " +
                "la entrega del viernes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Pedidos pendientes", "Abarrotes Sánchez")
        ),
        change(
            task = "marca la boda Hernández Ruvalcaba como confirmada",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Boda Hernández Ruvalcaba", "confirmad")
        ),
        change(
            task = "anota en la bitácora de noviembre que hoy vino el técnico y dejó lista " +
                "la plancha del truck",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Bitácora noviembre", "plancha")
        ),
        change(
            task = "hazme una nota Resumen de anticipos con lo que ha pagado cada evento " +
                "y lo que falta",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Resumen de anticipos", "20,000", "30,000", "Treviño", "Arreola")
        ),
        change(
            task = "mueve la receta del agua de jamaica a una carpeta nueva Bebidas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Agua de jamaica", "Bebidas")
        ),
        change(
            task = "renombra Turnos semana 46 a Turnos 9 al 15 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Turnos semana 46", "Turnos 9 al 15 de noviembre")
        ),
        change(
            task = "en Pedidos pendientes cambia el pedido del gas a \$1,600",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Pedidos pendientes", "1,600")
        ),
        change(
            task = "ponle #anticipo-pendiente a los eventos que todavía no pagan el anticipo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Bautizo Sofía Treviño", "Posada Despacho Contable Arreola"),
            lacks = listOf("XV años Sofía Márquez", "Boda Hernández Ruvalcaba")
        ),
        change(
            task = "ponle #por-pagar a cada proveedor que tenga facturas pendientes",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Carnicería Hermanos Ibarra", "Abarrotes Sánchez"),
            lacks = listOf("Tortillería La Esperanza", "Gas Occidente Express")
        ),
        change(
            task = "sube el precio de los tacos al pastor a \$90",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Tacos al pastor", "90")
        ),
        change(
            task = "en la nota de Luis Ochoa pon un enlace a Ruta y permisos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Luis Ochoa", "Ruta y permisos")
        ),
        change(
            task = "ponle a Diego Partida la propiedad puesto: taquero",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Diego Partida", "taquero")
        )
    )
}

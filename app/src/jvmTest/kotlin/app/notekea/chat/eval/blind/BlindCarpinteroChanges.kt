package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCarpinteroChanges {
    val all = listOf(
        change(
            task = "pon la etiqueta #impago a todos los clientes que tengan alguna " +
                "factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("impago", "Iñaki Larrea", "Begoña Sáez", "Txoko Berria"),
            lacks = listOf("Amaia Uriarte", "Koldo Arrieta", "Iñaki Zubiaurre")
        ),
        change(
            task = "añade a la mesa de Zubiaurre una tarea: comprar cola de poliuretano",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Mesa comedor Zubiaurre", "cola de poliuretano")
        ),
        change(
            task = "pon el armario de Amaia en estado montaje",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Armario empotrado Uriarte", "montaje")
        ),
        change(
            task = "etiqueta con #urgente el encargo sin entregar que tenga la " +
                "entrega más cercana",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Armario empotrado Uriarte", "urgente"),
            lacks = listOf("Banco jardín Arrieta")
        ),
        change(
            task = "añade al diario de ayer: Amaia confirma el montaje el día 27",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "read", "append", "propose"),
            holds = listOf("2026-11-10", "montaje")
        ),
        change(
            task = "hazme una nota Entregas diciembre con los encargos que entrego " +
                "en diciembre y su fecha",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "write_note", "propose"),
            holds = listOf("Entregas diciembre", "Zubiaurre", "Larrea", "Sáez"),
            lacks = listOf("Uriarte")
        ),
        change(
            task = "mueve Ideas de diseño a una carpeta nueva que se llame Ideas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Ideas de diseño")
        ),
        change(
            task = "renombra Pedido maderas octubre a Pedido maderas 2026-10",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Pedido maderas 2026-10")
        ),
        change(
            task = "la ITV cámbiala a las 10:30",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("ITV", "10:30")
        ),
        change(
            task = "en la ficha de Begoña Sáez pon la propiedad telefono: 600 123 987",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Begoña Sáez", "600 123 987")
        ),
        change(
            task = "añade a las normas de seguridad, en lo de la tupí: " +
                "nunca usarla sin el protector",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Normas de seguridad", "protector")
        ),
        change(
            task = "pon #nogal a todos los encargos que lleven nogal",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Librería Larrea", "Barra Txoko Berria"),
            lacks = listOf(
                "Mesa comedor Zubiaurre",
                "Cuna Sáez",
                "Armario empotrado Uriarte"
            )
        )
    )
}

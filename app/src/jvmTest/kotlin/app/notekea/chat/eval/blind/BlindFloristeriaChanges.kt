package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Floristeria notes that the words and the notes settle. */
internal object BlindFloristeriaChanges {
    val all = listOf(
        change(
            task = "añade una tarea a la boda Casals-Ortega: mandar a Elena muestras de color " +
                "de peonías",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Boda Casals-Ortega", "peonías")
        ),
        change(
            task = "marca la boda Puig-Serra como status confirmada",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Boda Puig-Serra", "confirmada")
        ),
        change(
            task = "ponle la etiqueta proveedor-principal al acuerdo con " +
                "Viveros del Maresme",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Acuerdo Viveros del Maresme", "proveedor-principal")
        ),
        change(
            task = "apunta en el diario del día 10 que Teresa Mas vuelve el viernes a por " +
                "otro ramo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("2026-11-10", "Teresa Mas", "viernes")
        ),
        change(
            task = "hazme una nota 'Señales por cobrar' con los eventos aceptados que todavía no " +
                "han pagado la señal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Señales por cobrar", "705", "156")
        ),
        change(
            task = "mueve las ideas de Sant Jordi a la carpeta Eventos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Ideas Sant Jordi 2027", "Eventos")
        ),
        change(
            task = "renombra Gasolina y peajes a Gastos furgoneta",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Gastos furgoneta")
        ),
        change(
            task = "pasa la cita de los neumáticos al 2026-12-09, misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("neumáticos", "2026-12-09", "10:15")
        ),
        change(
            task = "etiqueta con #moroso todas las notas de clientes que tengan alguna factura " +
                "pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("La Barra Blava", "Montserrat Vidal", "moroso")
        ),
        change(
            task = "en la nota de Jordi Roca pon la propiedad carnet: renovado",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Jordi Roca", "renovado")
        ),
        change(
            task = "hazme una nota 'Llegadas antes del 15' con los pedidos pendientes que llegan " +
                "antes del 15 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Llegadas antes del 15", "rosas rojas", "tulipanes blancos")
        ),
        change(
            task = "pon la etiqueta #boda en todas las notas de bodas",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "list", "add_tag", "add_tag", "propose"),
            holds = listOf("Boda Puig-Serra", "Boda Casals-Ortega")
        )
    )
}

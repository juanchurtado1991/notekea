package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Floristeria notes that the words and the notes settle. */
internal object BlindFloristeriaChanges {
    val all = listOf(
        AgentWideCase(
            task = "añade una tarea a la boda Casals-Ortega: mandar a Elena muestras de color " +
                "de peonías",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boda Casals-Ortega", "peonías")
        ),
        AgentWideCase(
            task = "marca la boda Puig-Serra como status confirmada",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boda Puig-Serra", "confirmada")
        ),
        AgentWideCase(
            task = "ponle la etiqueta proveedor-principal al acuerdo con " +
                "Viveros del Maresme",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Acuerdo Viveros del Maresme", "proveedor-principal")
        ),
        AgentWideCase(
            task = "apunta en el diario del día 10 que Teresa Mas vuelve el viernes a por " +
                "otro ramo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "Teresa Mas", "viernes")
        ),
        AgentWideCase(
            task = "hazme una nota 'Señales por cobrar' con los eventos aceptados que todavía no " +
                "han pagado la señal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Señales por cobrar", "705", "156")
        ),
        AgentWideCase(
            task = "mueve las ideas de Sant Jordi a la carpeta Eventos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ideas Sant Jordi 2027", "Eventos")
        ),
        AgentWideCase(
            task = "renombra Gasolina y peajes a Gastos furgoneta",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gastos furgoneta")
        ),
        AgentWideCase(
            task = "pasa la cita de los neumáticos al 2026-12-09, misma hora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("neumáticos", "2026-12-09", "10:15")
        ),
        AgentWideCase(
            task = "etiqueta con #moroso todas las notas de clientes que tengan alguna factura " +
                "pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("La Barra Blava", "Montserrat Vidal", "moroso")
        ),
        AgentWideCase(
            task = "en la nota de Jordi Roca pon la propiedad carnet: renovado",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordi Roca", "renovado")
        ),
        AgentWideCase(
            task = "hazme una nota 'Llegadas antes del 15' con los pedidos pendientes que llegan " +
                "antes del 15 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Llegadas antes del 15", "rosas rojas", "tulipanes blancos")
        ),
        AgentWideCase(
            task = "pon la etiqueta #boda en todas las notas de bodas",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "list", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boda Puig-Serra", "Boda Casals-Ortega")
        )
    )
}

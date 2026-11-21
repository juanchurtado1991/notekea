package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Hostal notes that the words and the notes make clear. */
internal object BlindHostalChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrega a Pedidos una tarea: llamar a Colchones Andinos para confirmar la " +
                "entrega",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pedidos", "Colchones Andinos")
        ),
        AgentWideCase(
            task = "Valeria Rojas ya pagó, ponle pago: pagado en su nota",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Valeria Rojas", "pagado")
        ),
        AgentWideCase(
            task = "tag Klaus Becker's note #repeat",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Klaus Becker", "repeat")
        ),
        AgentWideCase(
            task = "en el diario del 10 de noviembre anota que Valeria pidió toallas extra",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "toallas extra")
        ),
        AgentWideCase(
            task = "hazme una nota 'Llegadas próximas' con las reservas que llegan entre el 12 " +
                "y el 20 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Llegadas próximas", "Emily Smith", "Hana Sato")
        ),
        AgentWideCase(
            task = "mueve Objetos olvidados a la carpeta Operaciones",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Objetos olvidados", "Operaciones")
        ),
        AgentWideCase(
            task = "rename the Montaña de Colores note to Rainbow Mountain",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rainbow Mountain")
        ),
        AgentWideCase(
            task = "la factura F-0662 de la lavandería ya está pagada, actualízala",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("F-0662", "pagada")
        ),
        AgentWideCase(
            task = "ponle #debe a las notas de los huéspedes que todavía no pagan su reserva de " +
                "noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "list", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Valeria Rojas", "Emily Smith", "debe")
        ),
        AgentWideCase(
            task = "add #machu-picchu to the note of every guest who booked a Machu Picchu tour",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "list", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("John Smith", "Emily Smith", "machu-picchu")
        ),
        AgentWideCase(
            task = "en Tarifas 2026 sube la temporada alta de la 101 a 220",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tarifas 2026", "220")
        ),
        AgentWideCase(
            task = "crea una nota 'Comisiones por cobrar' con las comisiones de tours que me " +
                "faltan cobrar y el total",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Comisiones por cobrar", "John Smith", "Emily Smith"),
            computed = listOf("193")
        )
    )
}

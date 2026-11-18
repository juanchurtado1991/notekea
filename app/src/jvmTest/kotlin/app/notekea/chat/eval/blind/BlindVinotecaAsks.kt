package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle what looks ambiguous. */
internal object BlindVinotecaAsks {
    val all = listOf(
        AgentWideCase(
            task = "agregá al presupuesto 2027 una línea de $ 150.000 para imprevistos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("club")
        ),
        AgentWideCase(
            task = "anotá en la nota de Martín que en diciembre paga por transferencia",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gálvez", "Ferreyra")
        ),
        AgentWideCase(
            task = "mové la degustación a las 21 hs",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Luján", "Blancos")
        ),
        AgentWideCase(
            task = "cambiá la entrega del pedido pendiente al 30 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Cerro Azul", "Vientos del Este")
        ),
        AgentWideCase(
            task = "bajá a 30 el stock del Malbec Reserva",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Cerro Azul", "Cerro Alto")
        ),
        AgentWideCase(
            task = "agregame una tarea para llamar a Lucía el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pereyra", "Bustos")
        ),
        AgentWideCase(
            task = "marcá como pagado el pedido de la clínica",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("del Parque", "San Roque")
        ),
        AgentWideCase(
            task = "marcá como hecho lo del técnico",
            kind = Kind.AMBIGUOUS,
            tools = listOf("tasks", "ask_user"),
            ends = End.ASK,
            holds = listOf("cámara de frío", "aire acondicionado")
        ),
        AgentWideCase(
            task = "pasá la degustación de Blancos de verano a las 21:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Blancos de verano", "21:00")
        ),
        AgentWideCase(
            task = "anotale a Martín Ferreyra que en enero pausa la caja",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Martín Ferreyra", "enero")
        ),
        AgentWideCase(
            task = "en el presupuesto 2027 del club, ¿cuánto tengo para envíos?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("640.000"),
            lacks = listOf("720.000")
        ),
        AgentWideCase(
            task = "bajá a 30 el stock del Malbec Reserva de Cerro Alto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cerro Alto", "30")
        ),
        AgentWideCase(
            task = "marcá como hechas las dos llamadas al técnico",
            kind = Kind.AMBIGUOUS,
            tools = listOf("tasks", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("cámara de frío", "aire acondicionado")
        )
    )
}

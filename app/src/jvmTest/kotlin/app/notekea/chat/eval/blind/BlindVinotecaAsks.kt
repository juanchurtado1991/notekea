package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle what looks ambiguous. */
internal object BlindVinotecaAsks {
    val all = listOf(
        ask(
            task = "agregá al presupuesto 2027 una línea de $ 150.000 para imprevistos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("club")
        ),
        ask(
            task = "anotá en la nota de Martín que en diciembre paga por transferencia",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Gálvez", "Ferreyra")
        ),
        ask(
            task = "mové la degustación a las 21 hs",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Luján", "Blancos")
        ),
        ask(
            task = "cambiá la entrega del pedido pendiente al 30 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Cerro Azul", "Vientos del Este")
        ),
        ask(
            task = "bajá a 30 el stock del Malbec Reserva",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Cerro Azul", "Cerro Alto")
        ),
        ask(
            task = "agregame una tarea para llamar a Lucía el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pereyra", "Bustos")
        ),
        ask(
            task = "marcá como pagado el pedido de la clínica",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("del Parque", "San Roque")
        ),
        ask(
            task = "marcá como hecho lo del técnico",
            kind = Kind.AMBIGUOUS,
            tools = listOf("tasks", "ask_user"),
            holds = listOf("cámara de frío", "aire acondicionado")
        ),
        change(
            task = "pasá la degustación de Blancos de verano a las 21:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Blancos de verano", "21:00")
        ),
        change(
            task = "anotale a Martín Ferreyra que en enero pausa la caja",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            holds = listOf("Martín Ferreyra", "enero")
        ),
        answer(
            task = "en el presupuesto 2027 del club, ¿cuánto tengo para envíos?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("640.000"),
            lacks = listOf("720.000")
        ),
        change(
            task = "bajá a 30 el stock del Malbec Reserva de Cerro Alto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Cerro Alto", "30")
        ),
        change(
            task = "marcá como hechas las dos llamadas al técnico",
            kind = Kind.AMBIGUOUS,
            tools = listOf("tasks", "edit_note", "propose"),
            holds = listOf("cámara de frío", "aire acondicionado")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the treasurer's notes cannot settle, and look-alikes whose words do settle them. */
internal object BlindClubFutbolAsks {
    val all = listOf(
        ask(
            task = "marcá como paga la cuota de noviembre de Rodrigo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Pereira", "Suárez")
        ),
        ask(
            task = "agregale al presupuesto una línea de imprevistos de $1000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Rifa", "2027")
        ),
        ask(
            task = "pasá la reserva de cancha para las 20:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "marcá el pedido como entregado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("camiseta", "pelota", "redes")
        ),
        ask(
            task = "anotá en la nota de Rodrigo que ya trajo el carné de salud",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pereira", "Suárez")
        ),
        ask(
            task = "marcá como pagado lo del árbitro",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "pasá el entrenamiento de la semana que viene al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "cambiá el premio de la rifa por una tablet",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Bicicleta", "Canasta", "Camiseta")
        ),
        change(
            task = "marcá como paga la cuota de octubre de Rodrigo Pereira",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Rodrigo Pereira", "Cuotas 2026")
        ),
        change(
            task = "marcá como pagado el árbitro del partido con Juventud del Cerrito",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Árbitros", "Juventud del Cerrito")
        ),
        answer(
            task = "cuánto suman los gastos del presupuesto de la rifa?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("5970")
        ),
        change(
            task = "pasá la reserva del amistoso con los veteranos para las 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Alquiler de cancha", "11:00")
        ),
        answer(
            task = "cuántos números vendieron entre los dos Rodrigos?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("38")
        ),
        change(
            task = "agregale el tag #finanzas a los dos presupuestos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("Presupuesto", "Rifa", "finanzas")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the treasurer's notes cannot settle, and look-alikes whose words do settle them. */
internal object BlindClubFutbolAsks {
    val all = listOf(
        AgentWideCase(
            task = "marcá como paga la cuota de noviembre de Rodrigo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pereira", "Suárez")
        ),
        AgentWideCase(
            task = "agregale al presupuesto una línea de imprevistos de $1000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Rifa", "2027")
        ),
        AgentWideCase(
            task = "pasá la reserva de cancha para las 20:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "marcá el pedido como entregado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("camiseta", "pelota", "redes")
        ),
        AgentWideCase(
            task = "anotá en la nota de Rodrigo que ya trajo el carné de salud",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pereira", "Suárez")
        ),
        AgentWideCase(
            task = "marcá como pagado lo del árbitro",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "pasá el entrenamiento de la semana que viene al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "cambiá el premio de la rifa por una tablet",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Bicicleta", "Canasta", "Camiseta")
        ),
        AgentWideCase(
            task = "marcá como paga la cuota de octubre de Rodrigo Pereira",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rodrigo Pereira", "Cuotas 2026")
        ),
        AgentWideCase(
            task = "marcá como pagado el árbitro del partido con Juventud del Cerrito",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Árbitros", "Juventud del Cerrito")
        ),
        AgentWideCase(
            task = "cuánto suman los gastos del presupuesto de la rifa?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5970")
        ),
        AgentWideCase(
            task = "pasá la reserva del amistoso con los veteranos para las 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alquiler de cancha", "11:00")
        ),
        AgentWideCase(
            task = "cuántos números vendieron entre los dos Rodrigos?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("38")
        ),
        AgentWideCase(
            task = "agregale el tag #finanzas a los dos presupuestos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Presupuesto", "Rifa", "finanzas")
        )
    )
}

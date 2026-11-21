package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Mudanza vault cannot settle, then traps whose words do settle them. */
internal object BlindMudanzaAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve la cita al 26 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("biometría", "médico", "notaría")
        ),
        AgentWideCase(
            task = "cambia la visita del 14 a las 12:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Leslieville", "High Park")
        ),
        AgentWideCase(
            task = "añade 'llevar fotocopia de los pasaportes' al checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("trámites")
        ),
        AgentWideCase(
            task = "añade al presupuesto una partida de 200 para imprevistos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("mudanza")
        ),
        AgentWideCase(
            task = "apunta en la nota de Elena que la llamo el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Prats", "Kowalski")
        ),
        AgentWideCase(
            task = "marca el piso como descartado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Leslieville", "Davisville", "High Park")
        ),
        AgentWideCase(
            task = "add 'buy a school uniform' to my kid's note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pau", "Martina")
        ),
        AgentWideCase(
            task = "mark the moving quote as accepted",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Turia", "Oceanline")
        ),
        AgentWideCase(
            task = "¿a qué hora es la cita de biometría?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10:15")
        ),
        AgentWideCase(
            task = "cambia la visita de Davisville al 22 de noviembre, a la misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Davisville", "11:00")
        ),
        AgentWideCase(
            task = "¿cuánto nos queda por pagar del presupuesto de la mudanza?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("925")
        ),
        AgentWideCase(
            task = "¿qué teléfono tiene Elena Kowalski?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("555-0142"),
            lacks = listOf("418 275")
        ),
        AgentWideCase(
            task = "add 'buy winter boots' to both kids' notes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pau", "Martina")
        )
    )
}

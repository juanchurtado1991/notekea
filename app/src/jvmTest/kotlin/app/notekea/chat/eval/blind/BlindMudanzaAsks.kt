package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Mudanza vault cannot settle, then traps whose words do settle them. */
internal object BlindMudanzaAsks {
    val all = listOf(
        ask(
            task = "mueve la cita al 26 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("biometría", "médico", "notaría")
        ),
        ask(
            task = "cambia la visita del 14 a las 12:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Leslieville", "High Park")
        ),
        ask(
            task = "añade 'llevar fotocopia de los pasaportes' al checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("trámites")
        ),
        ask(
            task = "añade al presupuesto una partida de 200 para imprevistos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("mudanza")
        ),
        ask(
            task = "apunta en la nota de Elena que la llamo el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Prats", "Kowalski")
        ),
        ask(
            task = "marca el piso como descartado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Leslieville", "Davisville", "High Park")
        ),
        ask(
            task = "add 'buy a school uniform' to my kid's note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pau", "Martina")
        ),
        ask(
            task = "mark the moving quote as accepted",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Turia", "Oceanline")
        ),
        answer(
            task = "¿a qué hora es la cita de biometría?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("10:15")
        ),
        change(
            task = "cambia la visita de Davisville al 22 de noviembre, a la misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Davisville", "11:00")
        ),
        answer(
            task = "¿cuánto nos queda por pagar del presupuesto de la mudanza?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("925")
        ),
        answer(
            task = "¿qué teléfono tiene Elena Kowalski?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("555-0142"),
            lacks = listOf("418 275")
        ),
        change(
            task = "add 'buy winter boots' to both kids' notes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            holds = listOf("Pau", "Martina")
        )
    )
}

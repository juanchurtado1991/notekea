package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaAsks {
    val all = listOf(
        ask(
            task = "mueve la entrega programada de doña Rosa al 22 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hernández", "Méndez")
        ),
        ask(
            task = "agrega en la nota de doña Rosa que ya entregó copia de su INE",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hernández", "Méndez")
        ),
        ask(
            task = "pasa la cita con la destilería al viernes 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "agrega 3000 de imprevistos al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("camioneta")
        ),
        ask(
            task = "agrega al final de la nota de octubre: cerrado el 2026-11-11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Entregas", "Pagos")
        ),
        ask(
            task = "cambia la entrega a la tortillería a 7 toneladas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("lote")
        ),
        ask(
            task = "cancela la entrega del 28",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hernández", "Celestino")
        ),
        ask(
            task = "pasa la cita a las 9 de la mañana",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("destilería", "tortillería")
        ),
        ask(
            task = "ponle status renovar al contrato",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("agave", "maíz")
        ),
        answer(
            task = "cuántos días faltan para la firma de la adenda?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("14 días")
        ),
        answer(
            task = "cuántos kilos entregó doña Rosa Méndez en octubre en total?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1390")
        ),
        change(
            task = "en el presupuesto de la camioneta sube combustible a 32000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Camioneta", "32000")
        ),
        change(
            task = "mueve las entregas programadas de las dos doñas Rosas al 22 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Rosa Hernández", "Rosa Méndez")
        ),
        change(
            task = "en la nota de entregas de octubre agrega que Marcelina vuelve a entregar " +
                "en enero",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Entregas", "Marcelina", "enero")
        )
    )
}

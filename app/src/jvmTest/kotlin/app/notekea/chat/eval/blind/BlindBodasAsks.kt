package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindBodasAsks {
    val all = listOf(
        ask(
            task = "agregá al cronograma la tarea: mandar a todos los proveedores el horario final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Martina", "Sofía")
        ),
        ask(
            task = "poné rating 8 en la nota de la degustación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Paz-Gómez", "Luna-Ibarra")
        ),
        ask(
            task = "pasá la reunión con Sofía a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("13", "24")
        ),
        ask(
            task = "actualizá el teléfono del catering, ahora es 11 3987-4410",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Almagro Sur")
        ),
        ask(
            task = "cambiá la partida de flores del presupuesto a $ 1.300.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Martina", "Sofía", "Camila")
        ),
        ask(
            task = "marcá como pagado el saldo de Lucía en lo de Martina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Ferreyra", "Benítez")
        ),
        ask(
            task = "cancelá la visita pendiente de Camila y Diego",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("El Ombú Viejo", "Quinta Los Aromos")
        ),
        ask(
            task = "agregale a la nota del salón que pidieron el plano de mesas actualizado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Quinta Los Aromos", "Palermo Alto")
        ),
        // Traps: they look ambiguous, but the words settle them.
        change(
            task = "agregá al cronograma de Sofía y Federico la tarea: mandar a todos los " +
                "proveedores el horario final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Luna-Ibarra", "horario final"),
            lacks = listOf("Paz-Gómez")
        ),
        change(
            task = "marcá como pagado el saldo de Lucía la florista en lo de Martina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Ferreyra", "600.000")
        ),
        answer(
            task = "cuánto sale el cubierto del menú Clásico Sur?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("68.000"),
            lacks = listOf("74.500", "63.000")
        ),
        change(
            task = "cancelá las dos visitas pendientes de Camila y Diego",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("El Ombú Viejo", "Quinta Los Aromos")
        ),
        change(
            task = "pasá la reunión de presupuesto con Sofía a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("17:00", "presupuesto")
        )
    )
}

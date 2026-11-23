package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle them. */
internal object BlindAcademiaIdiomasAsks {
    val all = listOf(
        ask(
            task = "pon el presupuesto como aprobado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("presupuesto")
        ),
        ask(
            task = "mete en el presupuesto una partida de 150 € para otros gastos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("presupuesto")
        ),
        ask(
            task = "añádeme una tarea para revisar con Lucía sus horas de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Moreno", "Herrera")
        ),
        ask(
            task = "apunta en la ficha de Lucía que el lunes 16 no puede venir",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Moreno", "Herrera")
        ),
        ask(
            task = "pasa la prueba de nivel al lunes 16 a la misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Irene", "Tomás", "Nuria")
        ),
        ask(
            task = "cambia el grupo de B1 al aula 4",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("mañana", "tarde")
        ),
        ask(
            task = "marca como cobrado el pago pendiente de Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("octubre", "noviembre")
        ),
        ask(
            task = "marca como recibido el pedido de Sonotec",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Proyector", "Auriculares")
        ),
        change(
            task = "marca como aprobado el presupuesto de eventos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("presupuesto", "aprobado"),
            lacks = listOf("gestión")
        ),
        answer(
            task = "¿cuántas horas a la semana tiene Lucía Herrera?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("9 horas")
        ),
        change(
            task = "sube a 98 € la cuota de los dos grupos de B1",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("mañana", "tarde", "98")
        ),
        change(
            task = "marca como cobrados los dos pagos pendientes de Marta Giménez",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Marta Giménez", "octubre", "noviembre")
        ),
        answer(
            task = "¿cuántas horas sumaron las dos Lucías en octubre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("122")
        )
    )
}

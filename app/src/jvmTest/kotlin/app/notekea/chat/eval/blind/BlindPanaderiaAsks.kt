package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the bakery vault cannot settle (ask back), and look-alikes whose words settle them. */
internal object BlindPanaderiaAsks {
    val all = listOf(
        ask(
            task = "ya pagaron la factura pendiente de Quispe, márcala como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Julio", "Marleny")
        ),
        ask(
            task = "cambia el turno del sábado de Luis a la tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Condori", "Apaza")
        ),
        ask(
            task = "cancela el pedido de Valle Sur",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("mantequilla", "azúcar")
        ),
        ask(
            task = "pasa la cita del sábado con Saúl a las 11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("horno", "amasadora")
        ),
        ask(
            task = "en Presupuesto ponle status aprobado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("insumos", "planilla")
        ),
        ask(
            task = "agrega a Pendientes: revisar todo el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pedidos", "Clientes")
        ),
        ask(
            task = "a Quispe súbele el crédito a 45 días",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Julio", "Marleny")
        ),
        ask(
            task = "adelanta el pedido pendiente al jueves",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("harina", "azúcar")
        ),
        ask(
            task = "corrige el monto de la factura del Hostal Los Tunales a 480",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("512", "448")
        ),
        change(
            task = "Marleny ya me pagó su factura pendiente, márcala como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("0233", "pagada")
        ),
        change(
            task = "pasa la entrega de la mantequilla al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("P-119", "mantequilla")
        ),
        answer(
            task = "cuánto suma el presupuesto de insumos de noviembre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("9210")
        ),
        change(
            task = "en los pendientes de clientes agrega: cotizar 50 panetones para Julio Quispe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Clientes", "cotizar 50 panetones")
        ),
        answer(
            task = "qué día descansa Luis el repartidor?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("martes")
        )
    )
}

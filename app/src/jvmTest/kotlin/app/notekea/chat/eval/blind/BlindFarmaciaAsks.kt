package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaAsks {
    val all = listOf(
        ask(
            task = "agrega 'revisar extintor' al checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("checklist")
        ),
        ask(
            task = "en Precios deja el ibuprofeno en $1.590",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("venta")
        ),
        ask(
            task = "marca como recibido el pedido de Pacífico Sur",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "saca a Camila del turno del 29, la reemplaza Javiera",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Rojas", "Fuentes")
        ),
        ask(
            task = "Camila cambió de celular, ponle el nuevo en su nota: +56 9 4410 2277",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Rojas", "Fuentes")
        ),
        ask(
            task = "pasa la visita del representante al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Austral", "del Pacífico", "Pacífico Sur")
        ),
        ask(
            task = "en la nota de Droguería Pacífico agrega la tarea: pedir lista de precios 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("del Pacífico", "Pacífico Sur")
        ),
        ask(
            task = "ya pagué la factura de Austral, márcala pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        // Traps: they look ambiguous, but their words settle them.
        change(
            task = "marca como recibido el pedido de la insulina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("P-1190"),
            lacks = listOf("P-1195")
        ),
        change(
            task = "agrega 'revisar extintor' al checklist de apertura",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("revisar extintor"),
            lacks = listOf("Normativa")
        ),
        change(
            task = "Camila Fuentes no puede el domingo 29, ponle a Matías en su lugar",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Turnos", "Matías")
        ),
        change(
            task = "cambia la visita de Valentina a las 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Agenda", "15:00")
        ),
        change(
            task = "la A-1534 la pagué hoy, márcala pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("A-1534", "pagada"),
            lacks = listOf("A-1561")
        ),
        answer(
            task = "cuál es el celular de Camila Rojas?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("5531 0982"),
            lacks = listOf("6274 1135")
        )
    )
}

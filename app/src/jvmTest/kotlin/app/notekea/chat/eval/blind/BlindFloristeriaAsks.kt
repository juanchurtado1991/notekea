package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Floristeria requests that must be asked back, and traps whose words settle them. */
internal object BlindFloristeriaAsks {
    val all = listOf(
        ask(
            task = "mueve la cita de la furgoneta al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("ITV", "neumáticos")
        ),
        ask(
            task = "añade 10 jarrones al inventario",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Tienda", "Eventos")
        ),
        ask(
            task = "pon en la nota de Montserrat que prefiere que le escriban por WhatsApp",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Puig", "Vidal")
        ),
        ask(
            task = "marca el seguro como renovado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Tienda", "Furgoneta")
        ),
        ask(
            task = "pasa el pedido de Viveros del Maresme al lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("rosas", "hortensias")
        ),
        ask(
            task = "cambia la fecha de la boda al sábado siguiente",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Puig", "Casals")
        ),
        ask(
            task = "sube la suscripción semanal a 45 €",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("La Barra Blava", "Miramar", "Ribas")
        ),
        ask(
            task = "pasa la cita con Montserrat Puig a las 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("ramo", "finca")
        ),
        answer(
            task = "¿a qué hora es la ITV de la furgo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("8:30")
        ),
        answer(
            task = "¿cuánto ha pagado de señal Montserrat Puig?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("960")
        ),
        change(
            task = "en el inventario de la tienda deja la cinta de raso en 2 rollos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("cinta de raso")
        ),
        change(
            task = "añade a la nota de Montserrat Vidal la tarea de confirmar el número de " +
                "centros de mesa",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Montserrat Vidal", "centros de mesa")
        ),
        answer(
            task = "¿cuánto pagamos al año entre los dos seguros?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("955")
        )
    )
}

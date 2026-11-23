package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCarpinteroAsks {
    val all = listOf(
        ask(
            task = "añade una tarea para llamar a Iñaki el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Zubiaurre", "Larrea")
        ),
        ask(
            task = "cambia la entrega del encargo de Iñaki al 15 de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Zubiaurre", "Larrea")
        ),
        ask(
            task = "pon en mantenimiento una tarea para el lunes: comprar repuestos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("máquinas", "furgoneta")
        ),
        ask(
            task = "añade la etiqueta #revisado a mi nota de mantenimiento",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("máquinas", "furgoneta")
        ),
        ask(
            task = "pasa la cita al miércoles que viene",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("médico", "gestoría", "ITV")
        ),
        ask(
            task = "marca el pedido como recibido",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("tiradores", "guías", "tableros")
        ),
        ask(
            task = "apunta en pedidos pendientes que he pedido 10 tablones a Maderas, " +
                "llegan el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Nervión", "Txorierri")
        ),
        ask(
            task = "cambia a 450 € la factura de compra que tengo pendiente",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Deusto", "Nervión")
        ),
        change(
            task = "en la nota de Iñaki Larrea añade una tarea: cobrar la factura de las sillas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Iñaki Larrea", "sillas"),
            lacks = listOf("Iñaki Zubiaurre")
        ),
        answer(
            task = "¿de qué madera es la librería de Iñaki?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("nogal")
        ),
        change(
            task = "en Personal/Mantenimiento apunta que hay que cambiar los neumáticos de la " +
                "furgoneta antes de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            holds = listOf("neumáticos"),
            lacks = listOf("máquinas")
        ),
        change(
            task = "mueve la cita de la gestoría al viernes 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("gestoría", "20")
        ),
        change(
            task = "marca como recibidos los dos pedidos de Herrajes Deusto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("tiradores", "guías"),
            lacks = listOf("8 tableros")
        )
    )
}

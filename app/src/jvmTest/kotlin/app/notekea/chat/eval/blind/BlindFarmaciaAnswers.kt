package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaAnswers {
    val all = listOf(
        answer(
            task = "cuántos días le quedan a la amoxicilina antes de vencer?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("39 días")
        ),
        answer(
            task = "cuánto suman los pedidos pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("853"),
            lacks = listOf("951")
        ),
        answer(
            task = "cuánto le debo en total a los proveedores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("606"),
            lacks = listOf("1169", "1.169")
        ),
        answer(
            task = "de las facturas que no he pagado, cuál vence primero?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("A-1534")
        ),
        answer(
            task = "hay algún pedido pendiente que no llegue al mínimo del proveedor?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "answer"),
            holds = listOf("P-1195")
        ),
        answer(
            task = "cuánto llevo perdido en mermas este año, sin contar las devoluciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("281"),
            lacks = listOf("302")
        ),
        answer(
            task = "cuánto más vendí en octubre que en septiembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("530")
        ),
        answer(
            task = "quién firmó el registro del refri el día que pasó de 8 grados?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Camila Rojas"),
            lacks = listOf("Camila Fuentes")
        ),
        answer(
            task = "quién tiene la llave de los controlados los sábados?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Camila Fuentes")
        ),
        answer(
            task = "según mis apuntes del reglamento, qué plazo tengo para responder un reclamo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15", "hábiles")
        ),
        answer(
            task = "cuál es el número del acta de la inspección de la Seremi?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2026-VAL-0884")
        ),
        answer(
            task = "cuántos días faltan para la reinspección?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("22 días")
        ),
        answer(
            task = "cuando llegue el pedido de paracetamol, con cuántas cajas quedo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("206")
        ),
        answer(
            task = "qué médico firmó más recetas retenidas en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "count", "answer"),
            holds = listOf("Ortiz")
        ),
        answer(
            task = "en octubre, cuál es el promedio de boletas de las cuatro semanas de la tabla?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("834")
        ),
        answer(
            task = "cuál es el RUT de Pacífico Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("76.418.220-3")
        ),
        answer(
            task = "cuánto le pago a Matías por hora?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("68.000")
        ),
        answer(
            task = "a qué hora llega el pedido de Austral?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("11:30", "12:00")
        )
    )
}

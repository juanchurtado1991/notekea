package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuántos días le quedan a la amoxicilina antes de vencer?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 días")
        ),
        AgentWideCase(
            task = "cuánto suman los pedidos pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("853"),
            lacks = listOf("951")
        ),
        AgentWideCase(
            task = "cuánto le debo en total a los proveedores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("606"),
            lacks = listOf("1169", "1.169")
        ),
        AgentWideCase(
            task = "de las facturas que no he pagado, cuál vence primero?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("A-1534")
        ),
        AgentWideCase(
            task = "hay algún pedido pendiente que no llegue al mínimo del proveedor?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("P-1195")
        ),
        AgentWideCase(
            task = "cuánto llevo perdido en mermas este año, sin contar las devoluciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("281"),
            lacks = listOf("302")
        ),
        AgentWideCase(
            task = "cuánto más vendí en octubre que en septiembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("530")
        ),
        AgentWideCase(
            task = "quién firmó el registro del refri el día que pasó de 8 grados?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Camila Rojas"),
            lacks = listOf("Camila Fuentes")
        ),
        AgentWideCase(
            task = "quién tiene la llave de los controlados los sábados?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Camila Fuentes")
        ),
        AgentWideCase(
            task = "según mis apuntes del reglamento, qué plazo tengo para responder un reclamo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15", "hábiles")
        ),
        AgentWideCase(
            task = "cuál es el número del acta de la inspección de la Seremi?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2026-VAL-0884")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la reinspección?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 días")
        ),
        AgentWideCase(
            task = "cuando llegue el pedido de paracetamol, con cuántas cajas quedo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("206")
        ),
        AgentWideCase(
            task = "qué médico firmó más recetas retenidas en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "count", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ortiz")
        ),
        AgentWideCase(
            task = "en octubre, cuál es el promedio de boletas de las cuatro semanas de la tabla?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("834")
        ),
        AgentWideCase(
            task = "cuál es el RUT de Pacífico Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("76.418.220-3")
        ),
        AgentWideCase(
            task = "cuánto le pago a Matías por hora?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("68.000")
        ),
        AgentWideCase(
            task = "a qué hora llega el pedido de Austral?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("11:30", "12:00")
        )
    )
}

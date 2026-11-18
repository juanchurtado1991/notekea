package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCarpinteroAsks {
    val all = listOf(
        AgentWideCase(
            task = "añade una tarea para llamar a Iñaki el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Zubiaurre", "Larrea")
        ),
        AgentWideCase(
            task = "cambia la entrega del encargo de Iñaki al 15 de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Zubiaurre", "Larrea")
        ),
        AgentWideCase(
            task = "pon en mantenimiento una tarea para el lunes: comprar repuestos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("máquinas", "furgoneta")
        ),
        AgentWideCase(
            task = "añade la etiqueta #revisado a mi nota de mantenimiento",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("máquinas", "furgoneta")
        ),
        AgentWideCase(
            task = "pasa la cita al miércoles que viene",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("médico", "gestoría", "ITV")
        ),
        AgentWideCase(
            task = "marca el pedido como recibido",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("tiradores", "guías", "tableros")
        ),
        AgentWideCase(
            task = "apunta en pedidos pendientes que he pedido 10 tablones a Maderas, " +
                "llegan el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Nervión", "Txorierri")
        ),
        AgentWideCase(
            task = "cambia a 450 € la factura de compra que tengo pendiente",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Deusto", "Nervión")
        ),
        AgentWideCase(
            task = "en la nota de Iñaki Larrea añade una tarea: cobrar la factura de las sillas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Iñaki Larrea", "sillas"),
            lacks = listOf("Iñaki Zubiaurre")
        ),
        AgentWideCase(
            task = "¿de qué madera es la librería de Iñaki?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("nogal")
        ),
        AgentWideCase(
            task = "en Personal/Mantenimiento apunta que hay que cambiar los neumáticos de la " +
                "furgoneta antes de diciembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("neumáticos"),
            lacks = listOf("máquinas")
        ),
        AgentWideCase(
            task = "mueve la cita de la gestoría al viernes 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("gestoría", "20")
        ),
        AgentWideCase(
            task = "marca como recibidos los dos pedidos de Herrajes Deusto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("tiradores", "guías"),
            lacks = listOf("8 tableros")
        )
    )
}

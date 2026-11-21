package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the bakery vault cannot settle (ask back), and look-alikes whose words settle them. */
internal object BlindPanaderiaAsks {
    val all = listOf(
        AgentWideCase(
            task = "ya pagaron la factura pendiente de Quispe, márcala como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Julio", "Marleny")
        ),
        AgentWideCase(
            task = "cambia el turno del sábado de Luis a la tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Condori", "Apaza")
        ),
        AgentWideCase(
            task = "cancela el pedido de Valle Sur",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("mantequilla", "azúcar")
        ),
        AgentWideCase(
            task = "pasa la cita del sábado con Saúl a las 11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("horno", "amasadora")
        ),
        AgentWideCase(
            task = "en Presupuesto ponle status aprobado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("insumos", "planilla")
        ),
        AgentWideCase(
            task = "agrega a Pendientes: revisar todo el lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pedidos", "Clientes")
        ),
        AgentWideCase(
            task = "a Quispe súbele el crédito a 45 días",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Julio", "Marleny")
        ),
        AgentWideCase(
            task = "adelanta el pedido pendiente al jueves",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("harina", "azúcar")
        ),
        AgentWideCase(
            task = "corrige el monto de la factura del Hostal Los Tunales a 480",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("512", "448")
        ),
        AgentWideCase(
            task = "Marleny ya me pagó su factura pendiente, márcala como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("0233", "pagada")
        ),
        AgentWideCase(
            task = "pasa la entrega de la mantequilla al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("P-119", "mantequilla")
        ),
        AgentWideCase(
            task = "cuánto suma el presupuesto de insumos de noviembre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9210")
        ),
        AgentWideCase(
            task = "en los pendientes de clientes agrega: cotizar 50 panetones para Julio Quispe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Clientes", "cotizar 50 panetones")
        ),
        AgentWideCase(
            task = "qué día descansa Luis el repartidor?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("martes")
        )
    )
}

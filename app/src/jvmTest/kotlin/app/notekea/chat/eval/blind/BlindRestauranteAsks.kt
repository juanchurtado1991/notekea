package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindRestauranteAsks {
    val all = listOf(
        // Two notes named Mantenimiento: Restaurante/ and Food truck/.
        AgentWideCase(
            task = "agrega a mantenimiento: revisar extintores",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Restaurante", "Food truck")
        ),
        // Two notes named Inventario semanal, both list aceite vegetal.
        AgentWideCase(
            task = "actualiza el inventario semanal, ya no hay aceite",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Restaurante", "Food truck")
        ),
        // Luis Ramírez and Luis Ochoa.
        AgentWideCase(
            task = "cuánto le pago a Luis por turno?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ramírez", "Ochoa")
        ),
        // Both Luis work on Sunday 15.
        AgentWideCase(
            task = "dale el domingo libre a Luis",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ramírez", "Ochoa")
        ),
        // XV años Sofía Márquez and Bautizo Sofía Treviño.
        AgentWideCase(
            task = "agrega al evento de Sofía que quieren mesa de postres",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Márquez", "Treviño")
        ),
        // Four pending orders in Pedidos pendientes.
        AgentWideCase(
            task = "cambia la entrega del pedido al lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Doña Licha", "Abarrotes Sánchez")
        ),
        // Three appointments in Food truck/Mantenimiento.
        AgentWideCase(
            task = "pasa la cita del food truck una hora más tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Afinación", "llantas")
        ),
        // Two pending invoices of the butcher: F-1034 and F-1047.
        AgentWideCase(
            task = "pásale a pagada la factura pendiente de la carnicería",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("F-1034", "F-1047")
        ),
        // Traps: the words settle them.
        AgentWideCase(
            task = "qué me falta resurtir en el inventario del food truck?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("tortilla", "pastor", "piña"),
            lacks = listOf("guajillo")
        ),
        AgentWideCase(
            task = "ponle #urgente a la nota de mantenimiento del food truck",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("urgente", "Food truck")
        ),
        AgentWideCase(
            task = "cuánto gana Luis Ochoa por turno?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("420"),
            lacks = listOf("450")
        ),
        AgentWideCase(
            task = "agrega al evento de Sofía Treviño que quieren mesa de postres",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bautizo Sofía Treviño", "mesa de postres"),
            lacks = listOf("Márquez")
        ),
        AgentWideCase(
            task = "marca como pagadas las dos facturas pendientes de la carnicería",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("F-1034", "F-1047")
        )
    )
}

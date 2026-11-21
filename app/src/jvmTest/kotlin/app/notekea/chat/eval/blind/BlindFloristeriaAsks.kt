package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Floristeria requests that must be asked back, and traps whose words settle them. */
internal object BlindFloristeriaAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve la cita de la furgoneta al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("ITV", "neumáticos")
        ),
        AgentWideCase(
            task = "añade 10 jarrones al inventario",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Tienda", "Eventos")
        ),
        AgentWideCase(
            task = "pon en la nota de Montserrat que prefiere que le escriban por WhatsApp",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Puig", "Vidal")
        ),
        AgentWideCase(
            task = "marca el seguro como renovado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Tienda", "Furgoneta")
        ),
        AgentWideCase(
            task = "pasa el pedido de Viveros del Maresme al lunes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("rosas", "hortensias")
        ),
        AgentWideCase(
            task = "cambia la fecha de la boda al sábado siguiente",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Puig", "Casals")
        ),
        AgentWideCase(
            task = "sube la suscripción semanal a 45 €",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("La Barra Blava", "Miramar", "Ribas")
        ),
        AgentWideCase(
            task = "pasa la cita con Montserrat Puig a las 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("ramo", "finca")
        ),
        AgentWideCase(
            task = "¿a qué hora es la ITV de la furgo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("8:30")
        ),
        AgentWideCase(
            task = "¿cuánto ha pagado de señal Montserrat Puig?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("960")
        ),
        AgentWideCase(
            task = "en el inventario de la tienda deja la cinta de raso en 2 rollos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("cinta de raso")
        ),
        AgentWideCase(
            task = "añade a la nota de Montserrat Vidal la tarea de confirmar el número de " +
                "centros de mesa",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Montserrat Vidal", "centros de mesa")
        ),
        AgentWideCase(
            task = "¿cuánto pagamos al año entre los dos seguros?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("955")
        )
    )
}

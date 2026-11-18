package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindBodasAsks {
    val all = listOf(
        AgentWideCase(
            task = "agregá al cronograma la tarea: mandar a todos los proveedores el horario final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Martina", "Sofía")
        ),
        AgentWideCase(
            task = "poné rating 8 en la nota de la degustación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Paz-Gómez", "Luna-Ibarra")
        ),
        AgentWideCase(
            task = "pasá la reunión con Sofía a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("13", "24")
        ),
        AgentWideCase(
            task = "actualizá el teléfono del catering, ahora es 11 3987-4410",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Almagro Sur")
        ),
        AgentWideCase(
            task = "cambiá la partida de flores del presupuesto a $ 1.300.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Martina", "Sofía", "Camila")
        ),
        AgentWideCase(
            task = "marcá como pagado el saldo de Lucía en lo de Martina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ferreyra", "Benítez")
        ),
        AgentWideCase(
            task = "cancelá la visita pendiente de Camila y Diego",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("El Ombú Viejo", "Quinta Los Aromos")
        ),
        AgentWideCase(
            task = "agregale a la nota del salón que pidieron el plano de mesas actualizado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Quinta Los Aromos", "Palermo Alto")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "agregá al cronograma de Sofía y Federico la tarea: mandar a todos los " +
                "proveedores el horario final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Luna-Ibarra", "horario final"),
            lacks = listOf("Paz-Gómez")
        ),
        AgentWideCase(
            task = "marcá como pagado el saldo de Lucía la florista en lo de Martina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ferreyra", "600.000")
        ),
        AgentWideCase(
            task = "cuánto sale el cubierto del menú Clásico Sur?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("68.000"),
            lacks = listOf("74.500", "63.000")
        ),
        AgentWideCase(
            task = "cancelá las dos visitas pendientes de Camila y Diego",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("El Ombú Viejo", "Quinta Los Aromos")
        ),
        AgentWideCase(
            task = "pasá la reunión de presupuesto con Sofía a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("17:00", "presupuesto")
        )
    )
}

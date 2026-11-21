package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle them. */
internal object BlindAcademiaIdiomasAsks {
    val all = listOf(
        AgentWideCase(
            task = "pon el presupuesto como aprobado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("presupuesto")
        ),
        AgentWideCase(
            task = "mete en el presupuesto una partida de 150 € para otros gastos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("presupuesto")
        ),
        AgentWideCase(
            task = "añádeme una tarea para revisar con Lucía sus horas de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moreno", "Herrera")
        ),
        AgentWideCase(
            task = "apunta en la ficha de Lucía que el lunes 16 no puede venir",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moreno", "Herrera")
        ),
        AgentWideCase(
            task = "pasa la prueba de nivel al lunes 16 a la misma hora",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Irene", "Tomás", "Nuria")
        ),
        AgentWideCase(
            task = "cambia el grupo de B1 al aula 4",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("mañana", "tarde")
        ),
        AgentWideCase(
            task = "marca como cobrado el pago pendiente de Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("octubre", "noviembre")
        ),
        AgentWideCase(
            task = "marca como recibido el pedido de Sonotec",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Proyector", "Auriculares")
        ),
        AgentWideCase(
            task = "marca como aprobado el presupuesto de eventos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("presupuesto", "aprobado"),
            lacks = listOf("gestión")
        ),
        AgentWideCase(
            task = "¿cuántas horas a la semana tiene Lucía Herrera?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("9 horas")
        ),
        AgentWideCase(
            task = "sube a 98 € la cuota de los dos grupos de B1",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("mañana", "tarde", "98")
        ),
        AgentWideCase(
            task = "marca como cobrados los dos pagos pendientes de Marta Giménez",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marta Giménez", "octubre", "noviembre")
        ),
        AgentWideCase(
            task = "¿cuántas horas sumaron las dos Lucías en octubre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("122")
        )
    )
}

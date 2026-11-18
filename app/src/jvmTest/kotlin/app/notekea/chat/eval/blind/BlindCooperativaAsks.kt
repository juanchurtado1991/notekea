package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve la entrega programada de doña Rosa al 22 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hernández", "Méndez")
        ),
        AgentWideCase(
            task = "agrega en la nota de doña Rosa que ya entregó copia de su INE",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hernández", "Méndez")
        ),
        AgentWideCase(
            task = "pasa la cita con la destilería al viernes 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "agrega 3000 de imprevistos al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("camioneta")
        ),
        AgentWideCase(
            task = "agrega al final de la nota de octubre: cerrado el 2026-11-11",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Entregas", "Pagos")
        ),
        AgentWideCase(
            task = "cambia la entrega a la tortillería a 7 toneladas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("lote")
        ),
        AgentWideCase(
            task = "cancela la entrega del 28",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hernández", "Celestino")
        ),
        AgentWideCase(
            task = "pasa la cita a las 9 de la mañana",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("destilería", "tortillería")
        ),
        AgentWideCase(
            task = "ponle status renovar al contrato",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("agave", "maíz")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la firma de la adenda?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("14 días")
        ),
        AgentWideCase(
            task = "cuántos kilos entregó doña Rosa Méndez en octubre en total?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1390")
        ),
        AgentWideCase(
            task = "en el presupuesto de la camioneta sube combustible a 32000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Camioneta", "32000")
        ),
        AgentWideCase(
            task = "mueve las entregas programadas de las dos doñas Rosas al 22 de noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rosa Hernández", "Rosa Méndez")
        ),
        AgentWideCase(
            task = "en la nota de entregas de octubre agrega que Marcelina vuelve a entregar " +
                "en enero",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Entregas", "Marcelina", "enero")
        )
    )
}

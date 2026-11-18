package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve la cita del taller al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "agrega a la nota de Toño que cierra los domingos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ramírez", "Villaseñor")
        ),
        AgentWideCase(
            task = "agrega una tarea en la nota del seguro: cotizar la renovación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "agrega al presupuesto un renglón de imprevistos de $1,000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("casa")
        ),
        AgentWideCase(
            task = "cambia la cita de Toño Villaseñor para el 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("alineación", "rótulas")
        ),
        AgentWideCase(
            task = "marca la multa como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("vuelta prohibida")
        ),
        AgentWideCase(
            task = "en viajes largos cámbiale la tarifa al viaje del aeropuerto, fueron $520",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Chapala", "Zapopan")
        ),
        AgentWideCase(
            task = "marca como pagado el pago pendiente de la escuela de Sofi, ya lo hice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("colegiatura", "material")
        ),
        AgentWideCase(
            task = "cuánto le he pagado a Toño Ramírez este año? sin el alternador que devolvió",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4650")
        ),
        AgentWideCase(
            task = "cambia la cita con Toño Ramírez a las 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Citas taller", "11:00")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que venza el seguro del carro?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("65 días")
        ),
        AgentWideCase(
            task = "en el presupuesto del carro sube la gasolina a $8,500",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Presupuesto", "8,500")
        ),
        AgentWideCase(
            task = "agrega a las notas de los dos Toños que ya no abren en domingo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Toño Ramírez", "Toño Villaseñor")
        )
    )
}

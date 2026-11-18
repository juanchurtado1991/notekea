package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCafetalAsks {
    val all = listOf(
        AgentWideCase(
            task = "agregá una línea de imprevistos de ₡50.000 al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("finca", "casa")
        ),
        AgentWideCase(
            task = "pasame la cita para el 2026-12-02",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("dentista", "Solano")
        ),
        AgentWideCase(
            task = "cancelá el pedido del agroservicio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("canastos", "fungicida")
        ),
        AgentWideCase(
            task = "anotá en la nota de Rodríguez que cambió de número de teléfono",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Esteban", "Mauricio")
        ),
        AgentWideCase(
            task = "en la nota de José poné que mañana llega tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Quirós", "Vindas")
        ),
        AgentWideCase(
            task = "marcá como hecha la tarea pendiente de la certificación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("bitácora", "rótulos")
        ),
        AgentWideCase(
            task = "poné el contrato de Mauricio como cancelado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Quebrada")
        ),
        AgentWideCase(
            task = "poné el status del lote como terminado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Higuerón", "Quebrada")
        ),
        // Traps: they look ambiguous but the words settle them.
        AgentWideCase(
            task = "agregá una línea de imprevistos de ₡50.000 al presupuesto de la casa",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("imprevistos", "50.000")
        ),
        AgentWideCase(
            task = "pasame la cita del dentista para el 2026-12-02",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("dentista", "2026-12-02")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Esteban Rodríguez?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2260-1184"),
            lacks = listOf("2541-0937")
        ),
        AgentWideCase(
            task = "anotá en las notas de los dos José que el sábado no se coge",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("José Quirós", "José Vindas")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCafetalAsks {
    val all = listOf(
        ask(
            task = "agregá una línea de imprevistos de ₡50.000 al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("finca", "casa")
        ),
        ask(
            task = "pasame la cita para el 2026-12-02",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("dentista", "Solano")
        ),
        ask(
            task = "cancelá el pedido del agroservicio",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("canastos", "fungicida")
        ),
        ask(
            task = "anotá en la nota de Rodríguez que cambió de número de teléfono",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Esteban", "Mauricio")
        ),
        ask(
            task = "en la nota de José poné que mañana llega tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Quirós", "Vindas")
        ),
        ask(
            task = "marcá como hecha la tarea pendiente de la certificación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("bitácora", "rótulos")
        ),
        ask(
            task = "poné el contrato de Mauricio como cancelado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Quebrada")
        ),
        ask(
            task = "poné el status del lote como terminado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Higuerón", "Quebrada")
        ),
        // Traps: they look ambiguous but the words settle them.
        change(
            task = "agregá una línea de imprevistos de ₡50.000 al presupuesto de la casa",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("imprevistos", "50.000")
        ),
        change(
            task = "pasame la cita del dentista para el 2026-12-02",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("dentista", "2026-12-02")
        ),
        answer(
            task = "cuál es el teléfono de Esteban Rodríguez?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2260-1184"),
            lacks = listOf("2541-0937")
        ),
        change(
            task = "anotá en las notas de los dos José que el sábado no se coge",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            holds = listOf("José Quirós", "José Vindas")
        )
    )
}

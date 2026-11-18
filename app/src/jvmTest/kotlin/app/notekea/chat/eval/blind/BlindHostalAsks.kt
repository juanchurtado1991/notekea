package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Hostal requests that must be asked back, and traps whose words settle them. */
internal object BlindHostalAsks {
    val all = listOf(
        AgentWideCase(
            task = "mueve la visita del gasfitero al lunes 16",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("14", "26")
        ),
        AgentWideCase(
            task = "cambia la entrega del pedido para el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("toallas", "café")
        ),
        AgentWideCase(
            task = "agrega al presupuesto una línea de pintura por S/ 400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "ponle la etiqueta #cerrado a la nota Noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Reservas", "Tours")
        ),
        AgentWideCase(
            task = "pasa el turno del sábado de Lucía al domingo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Huamán", "Condori")
        ),
        AgentWideCase(
            task = "al huésped Smith ponle la etiqueta #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("John", "Emily")
        ),
        AgentWideCase(
            task = "move the Smith booking in november to room 201",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("John", "Emily")
        ),
        AgentWideCase(
            task = "reschedule the maintenance visit to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("14", "19", "26")
        ),
        AgentWideCase(
            task = "pasa la visita del electricista a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mantenimiento", "17:00")
        ),
        AgentWideCase(
            task = "en el presupuesto de 2027 sube marketing a 3500",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("marketing", "3500")
        ),
        AgentWideCase(
            task = "cuánto suman las reservas de noviembre de los dos Smith?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1360")
        ),
        AgentWideCase(
            task = "a Lucía Condori agrégale la tarea 'entregar llaves del depósito' antes de " +
                "sus vacaciones",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucía Condori", "entregar llaves del depósito")
        ),
        AgentWideCase(
            task = "in Tours/Noviembre add a row: 2026-11-26, Valle Sagrado, Pierre Lambert, " +
                "Qosqo Andes Travel, 1 person, 120",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pierre Lambert", "Valle Sagrado")
        )
    )
}

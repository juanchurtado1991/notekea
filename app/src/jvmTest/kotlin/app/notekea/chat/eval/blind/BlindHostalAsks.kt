package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Hostal requests that must be asked back, and traps whose words settle them. */
internal object BlindHostalAsks {
    val all = listOf(
        ask(
            task = "mueve la visita del gasfitero al lunes 16",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("14", "26")
        ),
        ask(
            task = "cambia la entrega del pedido para el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("toallas", "café")
        ),
        ask(
            task = "agrega al presupuesto una línea de pintura por S/ 400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "ponle la etiqueta #cerrado a la nota Noviembre",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Reservas", "Tours")
        ),
        ask(
            task = "pasa el turno del sábado de Lucía al domingo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Huamán", "Condori")
        ),
        ask(
            task = "al huésped Smith ponle la etiqueta #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("John", "Emily")
        ),
        ask(
            task = "move the Smith booking in november to room 201",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("John", "Emily")
        ),
        ask(
            task = "reschedule the maintenance visit to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("14", "19", "26")
        ),
        change(
            task = "pasa la visita del electricista a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Mantenimiento", "17:00")
        ),
        change(
            task = "en el presupuesto de 2027 sube marketing a 3500",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("marketing", "3500")
        ),
        answer(
            task = "cuánto suman las reservas de noviembre de los dos Smith?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1360")
        ),
        change(
            task = "a Lucía Condori agrégale la tarea 'entregar llaves del depósito' antes de " +
                "sus vacaciones",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Lucía Condori", "entregar llaves del depósito")
        ),
        change(
            task = "in Tours/Noviembre add a row: 2026-11-26, Valle Sagrado, Pierre Lambert, " +
                "Qosqo Andes Travel, 1 person, 120",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Pierre Lambert", "Valle Sagrado")
        )
    )
}

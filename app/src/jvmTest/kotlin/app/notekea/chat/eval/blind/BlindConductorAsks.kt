package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorAsks {
    val all = listOf(
        ask(
            task = "mueve la cita del taller al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "agrega a la nota de Toño que cierra los domingos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ramírez", "Villaseñor")
        ),
        ask(
            task = "agrega una tarea en la nota del seguro: cotizar la renovación",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "agrega al presupuesto un renglón de imprevistos de $1,000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("casa")
        ),
        ask(
            task = "cambia la cita de Toño Villaseñor para el 20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("alineación", "rótulas")
        ),
        ask(
            task = "marca la multa como pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("vuelta prohibida")
        ),
        ask(
            task = "en viajes largos cámbiale la tarifa al viaje del aeropuerto, fueron $520",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Chapala", "Zapopan")
        ),
        ask(
            task = "marca como pagado el pago pendiente de la escuela de Sofi, ya lo hice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("colegiatura", "material")
        ),
        answer(
            task = "cuánto le he pagado a Toño Ramírez este año? sin el alternador que devolvió",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4650")
        ),
        change(
            task = "cambia la cita con Toño Ramírez a las 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Citas taller", "11:00")
        ),
        answer(
            task = "cuántos días faltan para que venza el seguro del carro?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("65 días")
        ),
        change(
            task = "en el presupuesto del carro sube la gasolina a $8,500",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Presupuesto", "8,500")
        ),
        change(
            task = "agrega a las notas de los dos Toños que ya no abren en domingo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "append", "propose"),
            holds = listOf("Toño Ramírez", "Toño Villaseñor")
        )
    )
}

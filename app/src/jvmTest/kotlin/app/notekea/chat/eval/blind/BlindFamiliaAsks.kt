package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Familia notes cannot settle, then traps whose words do settle them. */
internal object BlindFamiliaAsks {
    val all = listOf(
        ask(
            task = "ponme un recordatorio para la reunión con la profe Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Gil", "Quintero")
        ),
        ask(
            task = "cambia la cita del pediatra para el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Lucía", "Samuel")
        ),
        ask(
            task = "Move Lucía's pediatrician appointment one week later",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("19")
        ),
        ask(
            task = "agrega una línea de imprevistos de 200 mil al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Viaje", "Casa")
        ),
        ask(
            task = "añade ibuprofeno a la nota de medicamentos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("abuelos")
        ),
        ask(
            task = "What's Dr. Felipe's copay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Arango", "Cárdenas")
        ),
        ask(
            task = "cancela la cita del odontólogo del sábado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Valentina", "Samuel")
        ),
        ask(
            task = "Samuel ya terminó la tarea, márcala como hecha",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "tasks", "ask_user"),
            holds = listOf("maqueta", "geometría")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "a qué hora es la reunión con Marta Quintero?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("12:30")
        ),
        change(
            task = "Move Samuel's pediatrician appointment to 2026-12-02, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Citas pediatra", "2026-12-02", "3:00")
        ),
        change(
            task = "agrega 150 mil de parqueadero del aeropuerto al presupuesto del viaje",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Viaje", "parqueadero"),
            lacks = listOf("Casa/Presupuesto")
        ),
        answer(
            task = "cuánto es el copago con cada uno de los dos doctores Felipe?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Arango", "Cárdenas", "38", "25")
        ),
        answer(
            task = "en qué categoría del presupuesto de la casa nos pasamos en noviembre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Transporte")
        ),
        change(
            task = "Add vitamin D 1000 IU in the morning to grandpa Hernando's medicines",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Hernando", "vitamin")
        )
    )
}

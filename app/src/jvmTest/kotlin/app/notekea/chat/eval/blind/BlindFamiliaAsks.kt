package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Familia notes cannot settle, then traps whose words do settle them. */
internal object BlindFamiliaAsks {
    val all = listOf(
        AgentWideCase(
            task = "ponme un recordatorio para la reunión con la profe Marta",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gil", "Quintero")
        ),
        AgentWideCase(
            task = "cambia la cita del pediatra para el viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Lucía", "Samuel")
        ),
        AgentWideCase(
            task = "Move Lucía's pediatrician appointment one week later",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("19")
        ),
        AgentWideCase(
            task = "agrega una línea de imprevistos de 200 mil al presupuesto",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Viaje", "Casa")
        ),
        AgentWideCase(
            task = "añade ibuprofeno a la nota de medicamentos",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("abuelos")
        ),
        AgentWideCase(
            task = "What's Dr. Felipe's copay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Arango", "Cárdenas")
        ),
        AgentWideCase(
            task = "cancela la cita del odontólogo del sábado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Valentina", "Samuel")
        ),
        AgentWideCase(
            task = "Samuel ya terminó la tarea, márcala como hecha",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "tasks", "ask_user"),
            ends = End.ASK,
            holds = listOf("maqueta", "geometría")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "a qué hora es la reunión con Marta Quintero?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("12:30")
        ),
        AgentWideCase(
            task = "Move Samuel's pediatrician appointment to 2026-12-02, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Citas pediatra", "2026-12-02", "3:00")
        ),
        AgentWideCase(
            task = "agrega 150 mil de parqueadero del aeropuerto al presupuesto del viaje",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Viaje", "parqueadero"),
            lacks = listOf("Casa/Presupuesto")
        ),
        AgentWideCase(
            task = "cuánto es el copago con cada uno de los dos doctores Felipe?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Arango", "Cárdenas", "38", "25")
        ),
        AgentWideCase(
            task = "en qué categoría del presupuesto de la casa nos pasamos en noviembre?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Transporte")
        ),
        AgentWideCase(
            task = "Add vitamin D 1000 IU in the morning to grandpa Hernando's medicines",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hernando", "vitamin")
        )
    )
}

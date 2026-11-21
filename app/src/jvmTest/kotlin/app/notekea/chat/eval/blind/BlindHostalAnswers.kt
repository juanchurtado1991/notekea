package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Hostal notes, three of them not answered by the notes. */
internal object BlindHostalAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto le debo a la lavandería?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("425")
        ),
        AgentWideCase(
            task = "cuánto llevo cobrado en comisiones de tours este mes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("123")
        ),
        AgentWideCase(
            task = "cuánto me falta cobrar de las reservas de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2120")
        ),
        AgentWideCase(
            task = "what do my november Booking reservations add up to, minus the cancelled one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1480")
        ),
        AgentWideCase(
            task = "cuánto le toca a Booking de comisión por la reserva de Emily Smith?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("96")
        ),
        AgentWideCase(
            task = "how much did I spend on supplies in november, not counting breakfast stuff?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("329")
        ),
        AgentWideCase(
            task = "average score of my Booking and Hostelworld reviews?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("8.5")
        ),
        AgentWideCase(
            task = "how many days till the Ortiz family arrives?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 days")
        ),
        AgentWideCase(
            task = "how many days between Klaus Becker's two check-ins?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("54 days")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que venza el certificado ITSE?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("105 días")
        ),
        AgentWideCase(
            task = "qué observación dejó pendiente el inspector de Defensa Civil?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("luz de emergencia")
        ),
        AgentWideCase(
            task = "a qué hora se cierra la puerta principal?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("23:30")
        ),
        AgentWideCase(
            task = "hasta qué hora se puede pedir el box breakfast?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20:00")
        ),
        AgentWideCase(
            task = "Emily Smith tiene alguna alergia?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("maní")
        ),
        AgentWideCase(
            task = "de quién es el cargador olvidado y cuándo vuelve al hostal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Klaus Becker", "28")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Emily Smith?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el teléfono de emily smith es")
        ),
        AgentWideCase(
            task = "qué puntaje le dio Valeria Rojas al hostal?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("valeria rojas le dio un", "valeria rojas le puso un")
        ),
        AgentWideCase(
            task = "what time does the train leave for Emily's Machu Picchu tour?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf(
                "train leaves at 04:30",
                "train leaves at 4:30",
                "train departs at 04:30"
            )
        )
    )
}

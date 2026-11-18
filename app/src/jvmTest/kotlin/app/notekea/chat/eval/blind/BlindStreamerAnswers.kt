package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Streamer notes, three of them not answered by the notes. */
internal object BlindStreamerAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuántas horas transmití en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("39")
        ),
        AgentWideCase(
            task = "cuál fue el promedio de viewers pico en los streams de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("53")
        ),
        AgentWideCase(
            task = "cuánto gané en subs en el tercer trimestre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("397")
        ),
        AgentWideCase(
            task = "which month before November had the most donations?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("83")
        ),
        AgentWideCase(
            task = "cuánto llevo de donaciones este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("89"),
            lacks = listOf("llevas 129")
        ),
        AgentWideCase(
            task = "hasta cuándo tiene garantía el micrófono?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2028")
        ),
        AgentWideCase(
            task = "con cuánto aviso puedo terminar el contrato con Kinetiq?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("45")
        ),
        AgentWideCase(
            task = "how many hours do I need to stream to get the Kinetiq year-end bonus?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("120")
        ),
        AgentWideCase(
            task = "a qué fundación va la plata del maratón?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Patitas del Sumapaz"),
            lacks = listOf("va para la Fundación Lectores del Barrio")
        ),
        AgentWideCase(
            task = "puedo pasar el maratón también por YouTube al mismo tiempo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que termine la vigencia del acuerdo con Kinetiq?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("81 días")
        ),
        AgentWideCase(
            task = "how many days has Café Altamira been sponsoring me?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("163 days")
        ),
        AgentWideCase(
            task = "cuál de los dos Santis tiene más seguidores?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Morales")
        ),
        AgentWideCase(
            task = "cuánto le debo a Mateo por los clips?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("54")
        ),
        AgentWideCase(
            task = "who are my chat mods?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Laura Pineda", "trueno_bog")
        ),
        AgentWideCase(
            task = "a qué hora es la reunión con Daniela?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10:30")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Santi Morales?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("su teléfono es", "el número es")
        ),
        AgentWideCase(
            task = "cuánto gané en subs en noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("en noviembre ganaste", "en noviembre llevas")
        ),
        AgentWideCase(
            task = "when does the warranty on my ring light end?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("ring light's warranty ends", "ring light is covered until")
        )
    )
}

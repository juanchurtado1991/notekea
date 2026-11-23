package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Streamer notes, three of them not answered by the notes. */
internal object BlindStreamerAnswers {
    val all = listOf(
        answer(
            task = "cuántas horas transmití en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("39")
        ),
        answer(
            task = "cuál fue el promedio de viewers pico en los streams de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("53")
        ),
        answer(
            task = "cuánto gané en subs en el tercer trimestre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("397")
        ),
        answer(
            task = "which month before November had the most donations?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("83")
        ),
        answer(
            task = "cuánto llevo de donaciones este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("89"),
            lacks = listOf("llevas 129")
        ),
        answer(
            task = "hasta cuándo tiene garantía el micrófono?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("2028")
        ),
        answer(
            task = "con cuánto aviso puedo terminar el contrato con Kinetiq?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("45")
        ),
        answer(
            task = "how many hours do I need to stream to get the Kinetiq year-end bonus?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("120")
        ),
        answer(
            task = "a qué fundación va la plata del maratón?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Patitas del Sumapaz"),
            lacks = listOf("va para la Fundación Lectores del Barrio")
        ),
        answer(
            task = "puedo pasar el maratón también por YouTube al mismo tiempo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("15")
        ),
        answer(
            task = "cuántos días faltan para que termine la vigencia del acuerdo con Kinetiq?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("81 días")
        ),
        answer(
            task = "how many days has Café Altamira been sponsoring me?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("163 days")
        ),
        answer(
            task = "cuál de los dos Santis tiene más seguidores?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Morales")
        ),
        answer(
            task = "cuánto le debo a Mateo por los clips?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("54")
        ),
        answer(
            task = "who are my chat mods?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Laura Pineda", "trueno_bog")
        ),
        answer(
            task = "a qué hora es la reunión con Daniela?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("10:30")
        ),
        answer(
            task = "cuál es el teléfono de Santi Morales?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("su teléfono es", "el número es")
        ),
        answer(
            task = "cuánto gané en subs en noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("en noviembre ganaste", "en noviembre llevas")
        ),
        answer(
            task = "when does the warranty on my ring light end?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("ring light's warranty ends", "ring light is covered until")
        )
    )
}

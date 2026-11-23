package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorAnswers {
    val all = listOf(
        answer(
            task = "cuánto saqué con Raite en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("9500")
        ),
        answer(
            task = "cuánto llevo de Jalo en noviembre sin contar propinas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("7260")
        ),
        answer(
            task = "cuánto he juntado de propinas este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("470")
        ),
        answer(
            task = "cuánto llevo de mi bolsa en gasolina en noviembre? lo de los vales no cuenta",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4127")
        ),
        answer(
            task = "cuánto le he pagado a Toño Villaseñor este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("6000")
        ),
        answer(
            task = "cuánto debo de multas?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1955")
        ),
        answer(
            task = "cuántos días faltan para la verificación?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("23 días")
        ),
        answer(
            task = "según el último estado de cuenta, cuánto debo todavía del carro?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("171,480")
        ),
        answer(
            task = "qué calificación mínima pide Jalo para no darte de baja?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("4.6")
        ),
        answer(
            task = "cuánto me va a cobrar Jalo de comisión en diciembre?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("28")
        ),
        answer(
            task = "cuánto pagué de deducible cuando me chocaron?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("9,800")
        ),
        answer(
            task = "cuánto me retiene Raite de ISR?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("2.1")
        ),
        answer(
            task = "cuántos km hice el martes 10?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("217")
        ),
        answer(
            task = "cuánto me falta para la meta del enganche?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("19300")
        ),
        answer(
            task = "quién me puso el alternador y cuánta garantía da en eso?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Ramírez", "6 meses")
        ),
        answer(
            task = "cuántos días pasaron del choque de septiembre a que me entregaron el carro?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("15 días")
        ),
        answer(
            task = "pásame el cel de Don Chuy",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("el número de don chuy es", "su número es", "su cel es")
        ),
        answer(
            task = "cuánto saqué con Raite en septiembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("en septiembre sacaste", "en septiembre ganaste")
        ),
        answer(
            task = "cuánto me va a cobrar Toño Villaseñor por la alineación?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("la alineación cuesta", "la alineación te cuesta")
        )
    )
}

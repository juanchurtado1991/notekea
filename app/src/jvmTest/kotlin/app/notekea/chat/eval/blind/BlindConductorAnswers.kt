package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindConductorAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto saqué con Raite en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9500")
        ),
        AgentWideCase(
            task = "cuánto llevo de Jalo en noviembre sin contar propinas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7260")
        ),
        AgentWideCase(
            task = "cuánto he juntado de propinas este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("470")
        ),
        AgentWideCase(
            task = "cuánto llevo de mi bolsa en gasolina en noviembre? lo de los vales no cuenta",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4127")
        ),
        AgentWideCase(
            task = "cuánto le he pagado a Toño Villaseñor este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6000")
        ),
        AgentWideCase(
            task = "cuánto debo de multas?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1955")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la verificación?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("23 días")
        ),
        AgentWideCase(
            task = "según el último estado de cuenta, cuánto debo todavía del carro?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("171,480")
        ),
        AgentWideCase(
            task = "qué calificación mínima pide Jalo para no darte de baja?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4.6")
        ),
        AgentWideCase(
            task = "cuánto me va a cobrar Jalo de comisión en diciembre?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("28")
        ),
        AgentWideCase(
            task = "cuánto pagué de deducible cuando me chocaron?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("9,800")
        ),
        AgentWideCase(
            task = "cuánto me retiene Raite de ISR?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2.1")
        ),
        AgentWideCase(
            task = "cuántos km hice el martes 10?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("217")
        ),
        AgentWideCase(
            task = "cuánto me falta para la meta del enganche?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("19300")
        ),
        AgentWideCase(
            task = "quién me puso el alternador y cuánta garantía da en eso?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ramírez", "6 meses")
        ),
        AgentWideCase(
            task = "cuántos días pasaron del choque de septiembre a que me entregaron el carro?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("15 días")
        ),
        AgentWideCase(
            task = "pásame el cel de Don Chuy",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el número de don chuy es", "su número es", "su cel es")
        ),
        AgentWideCase(
            task = "cuánto saqué con Raite en septiembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("en septiembre sacaste", "en septiembre ganaste")
        ),
        AgentWideCase(
            task = "cuánto me va a cobrar Toño Villaseñor por la alineación?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("la alineación cuesta", "la alineación te cuesta")
        )
    )
}

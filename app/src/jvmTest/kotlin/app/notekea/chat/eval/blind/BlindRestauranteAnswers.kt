package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindRestauranteAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto le debo todavía a la carnicería?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("11700"),
            lacks = listOf("20750")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la boda Hernández Ruvalcaba?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("24 días")
        ),
        AgentWideCase(
            task = "cuánto me queda por orden de birria " +
                "ya quitando lo que cuestan los ingredientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("54")
        ),
        AgentWideCase(
            task = "qué me deja más ganancia por orden, la torta ahogada " +
                "o los tacos al pastor? y cuánto me queda en la que gana?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("torta ahogada"),
            computed = listOf("56.5")
        ),
        AgentWideCase(
            task = "cuánto les voy a pagar esta semana a Carmen y a Diego " +
                "sin contar propinas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4000")
        ),
        AgentWideCase(
            task = "cuánto suman los pedidos que todavía no me llegan?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9185"),
            lacks = listOf("18115")
        ),
        AgentWideCase(
            task = "cuánto vendí en octubre sin contar eventos y ya quitando las devoluciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("276000"),
            lacks = listOf("324000")
        ),
        AgentWideCase(
            task = "el food truck vendió más en septiembre o en octubre? " +
                "de cuánto fue la diferencia?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("septiembre"),
            computed = listOf("5550")
        ),
        AgentWideCase(
            task = "cuándo se le vence la licencia a Luis Ochoa?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("18 de febrero")
        ),
        AgentWideCase(
            task = "según el reglamento, hasta cuándo tengo para pagar el aguinaldo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15 de diciembre")
        ),
        AgentWideCase(
            task = "de cuánto fue la multa de la inspección sanitaria del año pasado?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4,350")
        ),
        AgentWideCase(
            task = "qué día se descompuso la plancha del truck?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6 de noviembre")
        ),
        AgentWideCase(
            task = "cuántos días llevamos desde la última fumigación del restaurante?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("44 días")
        ),
        AgentWideCase(
            task = "cuánto llevo cobrado en anticipos de los eventos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("50000"),
            lacks = listOf("61000")
        ),
        AgentWideCase(
            task = "cuánto me falta por cobrar de los eventos de diciembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("84800")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Doña Licha?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("3615 2290", "3826 4417", "3637 1945", "3150 8806")
        ),
        AgentWideCase(
            task = "cuánto vendió el food truck en agosto?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "list", "answer"),
            ends = End.ANSWER,
            lacks = listOf("98300", "92750")
        ),
        AgentWideCase(
            task = "cuándo fue la última inspección sanitaria del food truck?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindRestauranteAnswers {
    val all = listOf(
        answer(
            task = "cuánto le debo todavía a la carnicería?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("11700"),
            lacks = listOf("20750")
        ),
        answer(
            task = "cuántos días faltan para la boda Hernández Ruvalcaba?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("24 días")
        ),
        answer(
            task = "cuánto me queda por orden de birria " +
                "ya quitando lo que cuestan los ingredientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("54")
        ),
        answer(
            task = "qué me deja más ganancia por orden, la torta ahogada " +
                "o los tacos al pastor? y cuánto me queda en la que gana?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("torta ahogada"),
            computed = listOf("56.5")
        ),
        answer(
            task = "cuánto les voy a pagar esta semana a Carmen y a Diego " +
                "sin contar propinas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("4000")
        ),
        answer(
            task = "cuánto suman los pedidos que todavía no me llegan?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("9185"),
            lacks = listOf("18115")
        ),
        answer(
            task = "cuánto vendí en octubre sin contar eventos y ya quitando las devoluciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("276000"),
            lacks = listOf("324000")
        ),
        answer(
            task = "el food truck vendió más en septiembre o en octubre? " +
                "de cuánto fue la diferencia?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("septiembre"),
            computed = listOf("5550")
        ),
        answer(
            task = "cuándo se le vence la licencia a Luis Ochoa?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("18 de febrero")
        ),
        answer(
            task = "según el reglamento, hasta cuándo tengo para pagar el aguinaldo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15 de diciembre")
        ),
        answer(
            task = "de cuánto fue la multa de la inspección sanitaria del año pasado?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("4,350")
        ),
        answer(
            task = "qué día se descompuso la plancha del truck?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6 de noviembre")
        ),
        answer(
            task = "cuántos días llevamos desde la última fumigación del restaurante?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("44 días")
        ),
        answer(
            task = "cuánto llevo cobrado en anticipos de los eventos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("50000"),
            lacks = listOf("61000")
        ),
        answer(
            task = "cuánto me falta por cobrar de los eventos de diciembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("84800")
        ),
        answer(
            task = "cuál es el teléfono de Doña Licha?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("3615 2290", "3826 4417", "3637 1945", "3150 8806")
        ),
        answer(
            task = "cuánto vendió el food truck en agosto?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "list", "answer"),
            lacks = listOf("98300", "92750")
        ),
        answer(
            task = "cuándo fue la última inspección sanitaria del food truck?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer")
        )
    )
}

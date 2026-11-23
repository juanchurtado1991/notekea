package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaAnswers {
    val all = listOf(
        answer(
            task = "cuántos kilos de espadín recibimos en octubre sin contar lo rechazado?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("7990")
        ),
        answer(
            task = "cuánto llevamos pagado por las entregas de octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("63495")
        ),
        answer(
            task = "cuánto le tengo que pagar a Aurelio ya con su anticipo descontado?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("11175")
        ),
        answer(
            task = "cuánto gastamos de diésel en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4630")
        ),
        answer(
            task = "cuánto nos queda del presupuesto de reparaciones de la camioneta?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("1050")
        ),
        answer(
            task = "cuánto se cobra ahorita por usar la camioneta para algo personal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("300")
        ),
        answer(
            task = "cuántos días faltan para que se venza el seguro de la camioneta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("109 días")
        ),
        answer(
            task = "por qué le rechazaron el agave a Marcelina y cuándo puede volver a entregar?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("enero")
        ),
        answer(
            task = "qué dice el estatuto si alguien falta a tres asambleas seguidas?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "answer"),
            holds = listOf("voto")
        ),
        answer(
            task = "si le entregamos a la destilería un lote de 100 mil pesos con dos semanas " +
                "de retraso, de cuánto es la pena?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "calculate", "answer"),
            computed = listOf("6000")
        ),
        answer(
            task = "quién va a fumigar la bodega del maíz?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "answer"),
            holds = listOf("Fumigaciones del Valle")
        ),
        answer(
            task = "cuántos socios activos tenemos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "count", "answer"),
            computed = listOf("13 socios")
        ),
        answer(
            task = "cuánto le falta a Bartolo para completar su aportación?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "find", "calculate", "answer"),
            computed = listOf("1300")
        ),
        answer(
            task = "doña Rosa Hernández entregó más agave en octubre o en noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("1,840", "1,260", "octubre")
        ),
        answer(
            task = "cuántos días lleva Bartolo en la coop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("100 días")
        ),
        answer(
            task = "cuál es el teléfono de Fermín?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el teléfono de fermín es", "su teléfono es")
        ),
        answer(
            task = "cuánto pagamos de seguro de la camioneta en 2025?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("en 2025 pagamos", "en 2025 se pagaron")
        ),
        answer(
            task = "a cómo nos paga la tortillería el maíz amarillo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("amarillo a \$8", "amarillo a 8 pesos", "amarillo también a")
        )
    )
}

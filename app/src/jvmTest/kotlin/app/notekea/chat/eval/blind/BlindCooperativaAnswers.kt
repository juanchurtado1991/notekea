package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuántos kilos de espadín recibimos en octubre sin contar lo rechazado?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7990")
        ),
        AgentWideCase(
            task = "cuánto llevamos pagado por las entregas de octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("63495")
        ),
        AgentWideCase(
            task = "cuánto le tengo que pagar a Aurelio ya con su anticipo descontado?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("11175")
        ),
        AgentWideCase(
            task = "cuánto gastamos de diésel en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4630")
        ),
        AgentWideCase(
            task = "cuánto nos queda del presupuesto de reparaciones de la camioneta?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1050")
        ),
        AgentWideCase(
            task = "cuánto se cobra ahorita por usar la camioneta para algo personal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("300")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que se venza el seguro de la camioneta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("109 días")
        ),
        AgentWideCase(
            task = "por qué le rechazaron el agave a Marcelina y cuándo puede volver a entregar?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("enero")
        ),
        AgentWideCase(
            task = "qué dice el estatuto si alguien falta a tres asambleas seguidas?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "answer"),
            ends = End.ANSWER,
            holds = listOf("voto")
        ),
        AgentWideCase(
            task = "si le entregamos a la destilería un lote de 100 mil pesos con dos semanas " +
                "de retraso, de cuánto es la pena?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6000")
        ),
        AgentWideCase(
            task = "quién va a fumigar la bodega del maíz?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "find", "answer"),
            ends = End.ANSWER,
            holds = listOf("Fumigaciones del Valle")
        ),
        AgentWideCase(
            task = "cuántos socios activos tenemos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "count", "answer"),
            ends = End.ANSWER,
            computed = listOf("13 socios")
        ),
        AgentWideCase(
            task = "cuánto le falta a Bartolo para completar su aportación?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "find", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1300")
        ),
        AgentWideCase(
            task = "doña Rosa Hernández entregó más agave en octubre o en noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1,840", "1,260", "octubre")
        ),
        AgentWideCase(
            task = "cuántos días lleva Bartolo en la coop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("100 días")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Fermín?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el teléfono de fermín es", "su teléfono es")
        ),
        AgentWideCase(
            task = "cuánto pagamos de seguro de la camioneta en 2025?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("en 2025 pagamos", "en 2025 se pagaron")
        ),
        AgentWideCase(
            task = "a cómo nos paga la tortillería el maíz amarillo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("amarillo a \$8", "amarillo a 8 pesos", "amarillo también a")
        )
    )
}

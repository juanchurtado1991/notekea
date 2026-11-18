package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the Blind Taller notes answer, and three they do not. */
internal object BlindTallerAnswers {
    val all = listOf(
        AgentWideCase(
            task = "¿cuánto le debo a Lubricantes Antioquia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("176"),
            lacks = listOf("409")
        ),
        AgentWideCase(
            task = "cuánto le debo a proveedores sin contar lo de Frenos y Embragues",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("588"),
            lacks = listOf("1478", "1.478")
        ),
        AgentWideCase(
            task = "¿cuántos días faltan para que se venza la AL-5602?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("9 días")
        ),
        AgentWideCase(
            task = "¿en cuánto salieron los repuestos del Spark de Marta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("193"),
            lacks = listOf("289")
        ),
        AgentWideCase(
            task = "¿cuánto gasté en servicios públicos en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("635")
        ),
        AgentWideCase(
            task = "cuánto suman las comisiones de esta quincena",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("438")
        ),
        AgentWideCase(
            task = "¿cuántos días faltan para pagar la quincena?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("2 días")
        ),
        AgentWideCase(
            task = "¿cuánto lleva el Logan de Lucía en el taller?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("6 días")
        ),
        AgentWideCase(
            task = "¿qué citas tengo mañana?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("Carlos Ospina", "Lucía Ospina", "08:00", "15:00"),
            lacks = listOf("marta henao")
        ),
        AgentWideCase(
            task = "¿cuántos días le quedan a la garantía de los amortiguadores del Picanto?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("125 días")
        ),
        AgentWideCase(
            task = "si el cliente trae su propio repuesto, ¿qué garantía le doy?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("mano de obra", "30 días")
        ),
        AgentWideCase(
            task = "¿cuántos meses de garantía tiene la caja de la Fortuner?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("12 meses")
        ),
        AgentWideCase(
            task = "¿lubricantes antioquia va a subir los precios?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("diciembre")
        ),
        AgentWideCase(
            task = "¿cómo está el compresor?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("válvula")
        ),
        AgentWideCase(
            task = "¿cuál de los mecánicos tiene el salario base más alto?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Wilson Cardona")
        ),
        AgentWideCase(
            task = "pásame el teléfono de Jorge Arango",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("412 7781", "teléfono es")
        ),
        AgentWideCase(
            task = "¿cuánto cuesta la certificación anual del elevador?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("cuesta 280", "cuesta $280", "cuesta $ 280")
        ),
        AgentWideCase(
            task = "¿a qué hora llega el kit de embrague de la Frontier?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("llega a las")
        )
    )
}

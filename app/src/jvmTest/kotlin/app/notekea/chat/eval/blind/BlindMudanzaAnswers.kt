package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions about the Blind Mudanza vault, three of which the notes do not answer. */
internal object BlindMudanzaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "¿cuánto hemos cobrado ya por los muebles?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("720")
        ),
        AgentWideCase(
            task = "How much have we paid so far in Canadian immigration fees?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("585")
        ),
        AgentWideCase(
            task = "¿cuántos días faltan para que caduque el pasaporte de Martina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("37 días")
        ),
        AgentWideCase(
            task = "how many days until our flight?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("70 days")
        ),
        AgentWideCase(
            task = "¿a qué hora llegamos a Toronto?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("14:55")
        ),
        AgentWideCase(
            task = "¿qué formulario hay que presentar en el aeropuerto para declarar " +
                "lo que llega en el contenedor?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("BSF186")
        ),
        AgentWideCase(
            task = "¿quién va a ser el jefe de Marc en Toronto?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Priya Raman")
        ),
        AgentWideCase(
            task = "¿cuál de los pisos tiene el alquiler más barato?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("High Park")
        ),
        AgentWideCase(
            task = "¿cuánta diferencia de alquiler hay entre el piso más caro y el más barato?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("450")
        ),
        AgentWideCase(
            task = "¿qué colegio queda más cerca si cogemos el piso de Davisville?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Riverdale Heights")
        ),
        AgentWideCase(
            task = "al mes en Toronto, ¿cuánto suman los gastos sin contar el alquiler " +
                "ni la comida?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("945")
        ),
        AgentWideCase(
            task = "si cambiamos 600 euros, ¿cuántos dólares canadienses son con nuestro " +
                "tipo de cambio?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("900")
        ),
        AgentWideCase(
            task = "Where do we get the SIN number once we land?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Service Canada")
        ),
        AgentWideCase(
            task = "¿quién les va a hacer la evaluación de inglés a los niños?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Okafor")
        ),
        AgentWideCase(
            task = "¿Marc puede canjear el carnet de conducir español en Ontario " +
                "sin hacer examen?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("G1")
        ),
        AgentWideCase(
            task = "¿cuál es el email de Jordi?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("harbourline", "pratsllorens")
        ),
        AgentWideCase(
            task = "Who's the landlord of the Davisville apartment?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("landlord is elena", "landlord is kowalski")
        ),
        AgentWideCase(
            task = "¿qué teléfono tiene la doctora del examen médico?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("418 275", "220 913", "555-0142")
        )
    )
}

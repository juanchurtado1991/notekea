package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Familia notes, three of them not answered by the notes. */
internal object BlindFamiliaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "en qué materia va perdiendo Valentina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Química", "61")
        ),
        AgentWideCase(
            task = "quién sacó mejor promedio en el tercer periodo, Valentina o Samuel?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Valentina"),
            computed = listOf("86")
        ),
        AgentWideCase(
            task = "a qué hora sale el vuelo de regreso de Cartagena?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("7:45")
        ),
        AgentWideCase(
            task = "What's the hotel booking code for the Cartagena trip?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("CM-48217")
        ),
        AgentWideCase(
            task = "cuántos días faltan para el viaje a Cartagena?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("38 días")
        ),
        AgentWideCase(
            task = "cuántos días faltan para el cumple de Andrés?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("14 días")
        ),
        AgentWideCase(
            task = "hay alguien de nosotros que no pueda comer mariscos en Cartagena?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Andrés")
        ),
        AgentWideCase(
            task = "How many mg of losartan does grandma Rosa take per dose?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("50")
        ),
        AgentWideCase(
            task = "cuál es el promedio de las glucometrías del abuelo en noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("130")
        ),
        AgentWideCase(
            task = "quién tiene las llaves de repuesto del apartamento de la abuela Rosa?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Efraín")
        ),
        AgentWideCase(
            task = "a qué es alérgico el abuelo Hernando?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("dipirona")
        ),
        AgentWideCase(
            task = "cuánto hemos pagado de nuestro bolsillo este año en gastos médicos " +
                "de los niños?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("457"),
            lacks = listOf("777", "517", "837")
        ),
        AgentWideCase(
            task = "cuánto me falta pagar del tour de islas y los traslados del viaje?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("930")
        ),
        AgentWideCase(
            task = "qué refuerzo de vacuna le falta a Lucía y en cuántos días es?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("DPT"),
            computed = listOf("29 días")
        ),
        AgentWideCase(
            task = "Where is Lucía's pediatrician appointment on the 19th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Calle 127", "504")
        ),
        AgentWideCase(
            task = "qué nota sacó Samuel en química?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("61")
        ),
        AgentWideCase(
            task = "What time does the school bus pick up Valentina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("6:25")
        ),
        AgentWideCase(
            task = "a qué hora es la cita del cardiólogo del abuelo Hernando?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("10:00")
        )
    )
}

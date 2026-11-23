package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Familia notes, three of them not answered by the notes. */
internal object BlindFamiliaAnswers {
    val all = listOf(
        answer(
            task = "en qué materia va perdiendo Valentina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Química", "61")
        ),
        answer(
            task = "quién sacó mejor promedio en el tercer periodo, Valentina o Samuel?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("Valentina"),
            computed = listOf("86")
        ),
        answer(
            task = "a qué hora sale el vuelo de regreso de Cartagena?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("7:45")
        ),
        answer(
            task = "What's the hotel booking code for the Cartagena trip?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("CM-48217")
        ),
        answer(
            task = "cuántos días faltan para el viaje a Cartagena?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("38 días")
        ),
        answer(
            task = "cuántos días faltan para el cumple de Andrés?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("14 días")
        ),
        answer(
            task = "hay alguien de nosotros que no pueda comer mariscos en Cartagena?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Andrés")
        ),
        answer(
            task = "How many mg of losartan does grandma Rosa take per dose?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("50")
        ),
        answer(
            task = "cuál es el promedio de las glucometrías del abuelo en noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("130")
        ),
        answer(
            task = "quién tiene las llaves de repuesto del apartamento de la abuela Rosa?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Efraín")
        ),
        answer(
            task = "a qué es alérgico el abuelo Hernando?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("dipirona")
        ),
        answer(
            task = "cuánto hemos pagado de nuestro bolsillo este año en gastos médicos " +
                "de los niños?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("457"),
            lacks = listOf("777", "517", "837")
        ),
        answer(
            task = "cuánto me falta pagar del tour de islas y los traslados del viaje?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("930")
        ),
        answer(
            task = "qué refuerzo de vacuna le falta a Lucía y en cuántos días es?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            holds = listOf("DPT"),
            computed = listOf("29 días")
        ),
        answer(
            task = "Where is Lucía's pediatrician appointment on the 19th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Calle 127", "504")
        ),
        answer(
            task = "qué nota sacó Samuel en química?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("61")
        ),
        answer(
            task = "What time does the school bus pick up Valentina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("6:25")
        ),
        answer(
            task = "a qué hora es la cita del cardiólogo del abuelo Hernando?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("10:00")
        )
    )
}

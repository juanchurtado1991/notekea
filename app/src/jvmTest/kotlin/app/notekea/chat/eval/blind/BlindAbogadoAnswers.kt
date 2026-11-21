package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAbogadoAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto me deben en total de facturas pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("54000"),
            lacks = listOf("63500")
        ),
        AgentWideCase(
            task = "cuánto gasté en el despacho en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("19589"),
            lacks = listOf("24489", "21939", "26839")
        ),
        AgentWideCase(
            task = "cuántos días pasaron entre que corrieron a Mario y que presentamos la demanda?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 días"),
            lacks = listOf("32 días")
        ),
        AgentWideCase(
            task = "qué interés moratorio trae mi carta de encargo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1.5"),
            lacks = listOf("3 %")
        ),
        AgentWideCase(
            task = "con cuánto mínimo puedo cerrar convenio con el ferretero sin preguntarle " +
                "a Beatriz?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("350000")
        ),
        AgentWideCase(
            task = "cuál es la última oferta de la empresa en lo de Mario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("120000")
        ),
        AgentWideCase(
            task = "quién es ahora el abogado del ferretero en lo de Arista?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Mónica Esparza")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la audiencia preliminar de Mario?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("8 días")
        ),
        AgentWideCase(
            task = "cuánto pago al año de suscripciones con los precios de ahorita?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("16416")
        ),
        AgentWideCase(
            task = "qué diferencia hay entre la pensión que pide Lucía y la que ofrece Óscar?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4200"),
            lacks = listOf("2200")
        ),
        AgentWideCase(
            task = "cuántos días me quedan para que se venza el contrato de renta de la oficina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("140 días")
        ),
        AgentWideCase(
            task = "qué testigo ofrecí en el caso de Mario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rubén Salinas")
        ),
        AgentWideCase(
            task = "cuánto gasto en promedio al mes de papelería? toma octubre y noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1400")
        ),
        AgentWideCase(
            task = "qué cliente me ha pagado más este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Constructora Arista")
        ),
        AgentWideCase(
            task = "qué audiencias tengo la semana del 23 al 27 de noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Lucía Fernández", "Héctor Galindo"),
            lacks = listOf("preliminar")
        ),
        AgentWideCase(
            task = "cuál es el número de expediente de Patricia Ibarra?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("FAM-2290", "JL-1187", "CIV-1563")
        ),
        AgentWideCase(
            task = "pásame la cédula profesional de Jorge Villarreal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("8823415")
        ),
        AgentWideCase(
            task = "cuánto me va a subir la renta de la oficina el año que entra?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("8%")
        )
    )
}

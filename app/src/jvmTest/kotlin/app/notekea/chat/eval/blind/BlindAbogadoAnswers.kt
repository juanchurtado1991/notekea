package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAbogadoAnswers {
    val all = listOf(
        answer(
            task = "cuánto me deben en total de facturas pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("54000"),
            lacks = listOf("63500")
        ),
        answer(
            task = "cuánto gasté en el despacho en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("19589"),
            lacks = listOf("24489", "21939", "26839")
        ),
        answer(
            task = "cuántos días pasaron entre que corrieron a Mario y que presentamos la demanda?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("39 días"),
            lacks = listOf("32 días")
        ),
        answer(
            task = "qué interés moratorio trae mi carta de encargo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1.5"),
            lacks = listOf("3 %")
        ),
        answer(
            task = "con cuánto mínimo puedo cerrar convenio con el ferretero sin preguntarle " +
                "a Beatriz?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("350000")
        ),
        answer(
            task = "cuál es la última oferta de la empresa en lo de Mario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("120000")
        ),
        answer(
            task = "quién es ahora el abogado del ferretero en lo de Arista?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Mónica Esparza")
        ),
        answer(
            task = "cuántos días faltan para la audiencia preliminar de Mario?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("8 días")
        ),
        answer(
            task = "cuánto pago al año de suscripciones con los precios de ahorita?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("16416")
        ),
        answer(
            task = "qué diferencia hay entre la pensión que pide Lucía y la que ofrece Óscar?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4200"),
            lacks = listOf("2200")
        ),
        answer(
            task = "cuántos días me quedan para que se venza el contrato de renta de la oficina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("140 días")
        ),
        answer(
            task = "qué testigo ofrecí en el caso de Mario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Rubén Salinas")
        ),
        answer(
            task = "cuánto gasto en promedio al mes de papelería? toma octubre y noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("1400")
        ),
        answer(
            task = "qué cliente me ha pagado más este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Constructora Arista")
        ),
        answer(
            task = "qué audiencias tengo la semana del 23 al 27 de noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            holds = listOf("Lucía Fernández", "Héctor Galindo"),
            lacks = listOf("preliminar")
        ),
        answer(
            task = "cuál es el número de expediente de Patricia Ibarra?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("FAM-2290", "JL-1187", "CIV-1563")
        ),
        answer(
            task = "pásame la cédula profesional de Jorge Villarreal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("8823415")
        ),
        answer(
            task = "cuánto me va a subir la renta de la oficina el año que entra?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("8%")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the wine shop owner asks about her notes; three the notes do not answer. */
internal object BlindVinotecaAnswers {
    val all = listOf(
        answer(
            task = "¿cuántas botellas de tinto de Cerro Alto tengo en stock?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("55")
        ),
        answer(
            task = "¿cuántas botellas me entran en total con los pedidos pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("168")
        ),
        answer(
            task = "¿cuánto le debo a Cerro Azul?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("1.020.000"),
            lacks = listOf("1.865.000")
        ),
        answer(
            task = "¿en qué mes de este año vendí más vino?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Septiembre")
        ),
        answer(
            task = "en total, contando también las que ya entregué, " +
                "¿cuántas botellas llevan las cajas del club de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("37")
        ),
        answer(
            task = "¿cuántos días faltan para la degustación de espumantes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("31 días")
        ),
        answer(
            task = "¿cuántos días pasan entre que le hice el pedido a Cerro Azul " +
                "y la entrega estimada?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("21 días")
        ),
        answer(
            task = "¿hay algún problema con el Blend de Altura 2019?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("corcho")
        ),
        answer(
            task = "¿a qué dirección le mando la caja del club a Valeria?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Rioja 1182")
        ),
        answer(
            task = "según el reglamento de importación, " +
                "¿cuántas botellas de muestra puedo traer por envío?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("18 botellas")
        ),
        answer(
            task = "¿quién va a ser mi contacto en Cerro Azul desde enero?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Inés Calvo")
        ),
        answer(
            task = "¿cuántas cajas regalo corporativas tengo comprometidas para fin de año?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("97")
        ),
        answer(
            task = "¿cuánta gente vino en total a las tres degustaciones que hice este año?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "calculate", "answer"),
            computed = listOf("39")
        ),
        answer(
            task = "¿cuántas botellas de Vientos del Este tengo entre tintos, " +
                "blancos y espumantes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("117")
        ),
        answer(
            task = "¿cuántas botellas vendí por mes en promedio en julio, agosto y septiembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("513")
        ),
        answer(
            task = "¿cuál es el pedido mínimo de Cerro Alto?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("4 cajas")
        ),
        answer(
            task = "pasame el teléfono de Martín Ferreyra",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("el teléfono de martín ferreyra es")
        ),
        answer(
            task = "¿cuántas botellas vendí en noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("en noviembre vendiste")
        ),
        answer(
            task = "¿qué puntaje le puse al Petit Verdot de Cerro Alto?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el petit verdot tiene un puntaje")
        )
    )
}

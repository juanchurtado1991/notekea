package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Floristeria notes, three of them not answered by any note. */
internal object BlindFloristeriaAnswers {
    val all = listOf(
        answer(
            task = "¿cuánto hicimos de media en mostrador del 2 al 7 de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("452")
        ),
        answer(
            task = "cuánto sacamos en total el martes 10, ya quitando la devolución?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("403")
        ),
        answer(
            task = "suma los gastos variables de octubre de la tienda, sin lo personal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("383"),
            lacks = listOf("478")
        ),
        answer(
            task = "¿cuánto nos gastamos en gasolina en octubre? solo gasolina, sin peajes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("285"),
            lacks = listOf("308")
        ),
        answer(
            task = "¿cuánto me falta por cobrar de señales de los presupuestos aceptados?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("861"),
            lacks = listOf("991")
        ),
        answer(
            task = "¿cuánto me deben los clientes en facturas pendientes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("La Barra Blava"),
            computed = listOf("660")
        ),
        answer(
            task = "¿cuánto suman los pedidos pendientes a Viveros del Maresme?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("676"),
            lacks = listOf("794")
        ),
        answer(
            task = "cuántos días faltan para la ITV de la furgo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("13 días")
        ),
        answer(
            task = "¿cuántos días quedan para la boda de Montserrat Puig?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("185 días")
        ),
        answer(
            task = "cuántos días hay entre que llegan las hortensias y la gala del hotel?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("22 días")
        ),
        answer(
            task = "¿qué día les llevamos las flores a La Barra Blava?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("jueves")
        ),
        answer(
            task = "¿para cuántos invitados es al final la gala del Miramar?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("200")
        ),
        answer(
            task = "¿a qué flor es alérgico el novio de la boda Puig-Serra?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("lirio")
        ),
        answer(
            task = "si la cámara deja de enfriar a quién llamo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Escolà", "Ramon")
        ),
        answer(
            task = "¿con cuánta antelación tengo que cancelar un pedido grande de eventos a " +
                "Viveros del Maresme?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("10 días")
        ),
        answer(
            task = "¿quién lleva más tiempo en la tienda, Núria o Jordi?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            holds = listOf("Núria")
        ),
        answer(
            task = "¿cuánto cobra Pau por hora?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("pau cobra 13", "pau cobra 12")
        ),
        answer(
            task = "¿cuándo fue la última ITV de la furgoneta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("la última itv fue el")
        ),
        answer(
            task = "pásame el NIF del Hotel Miramar Port",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el nif del hotel es")
        )
    )
}

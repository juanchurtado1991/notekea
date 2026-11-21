package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Floristeria notes, three of them not answered by any note. */
internal object BlindFloristeriaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "¿cuánto hicimos de media en mostrador del 2 al 7 de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("452")
        ),
        AgentWideCase(
            task = "cuánto sacamos en total el martes 10, ya quitando la devolución?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("403")
        ),
        AgentWideCase(
            task = "suma los gastos variables de octubre de la tienda, sin lo personal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("383"),
            lacks = listOf("478")
        ),
        AgentWideCase(
            task = "¿cuánto nos gastamos en gasolina en octubre? solo gasolina, sin peajes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("285"),
            lacks = listOf("308")
        ),
        AgentWideCase(
            task = "¿cuánto me falta por cobrar de señales de los presupuestos aceptados?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("861"),
            lacks = listOf("991")
        ),
        AgentWideCase(
            task = "¿cuánto me deben los clientes en facturas pendientes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("La Barra Blava"),
            computed = listOf("660")
        ),
        AgentWideCase(
            task = "¿cuánto suman los pedidos pendientes a Viveros del Maresme?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("676"),
            lacks = listOf("794")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la ITV de la furgo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("13 días")
        ),
        AgentWideCase(
            task = "¿cuántos días quedan para la boda de Montserrat Puig?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("185 días")
        ),
        AgentWideCase(
            task = "cuántos días hay entre que llegan las hortensias y la gala del hotel?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 días")
        ),
        AgentWideCase(
            task = "¿qué día les llevamos las flores a La Barra Blava?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("jueves")
        ),
        AgentWideCase(
            task = "¿para cuántos invitados es al final la gala del Miramar?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("200")
        ),
        AgentWideCase(
            task = "¿a qué flor es alérgico el novio de la boda Puig-Serra?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("lirio")
        ),
        AgentWideCase(
            task = "si la cámara deja de enfriar a quién llamo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Escolà", "Ramon")
        ),
        AgentWideCase(
            task = "¿con cuánta antelación tengo que cancelar un pedido grande de eventos a " +
                "Viveros del Maresme?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10 días")
        ),
        AgentWideCase(
            task = "¿quién lleva más tiempo en la tienda, Núria o Jordi?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Núria")
        ),
        AgentWideCase(
            task = "¿cuánto cobra Pau por hora?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("pau cobra 13", "pau cobra 12")
        ),
        AgentWideCase(
            task = "¿cuándo fue la última ITV de la furgoneta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("la última itv fue el")
        ),
        AgentWideCase(
            task = "pásame el NIF del Hotel Miramar Port",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el nif del hotel es")
        )
    )
}

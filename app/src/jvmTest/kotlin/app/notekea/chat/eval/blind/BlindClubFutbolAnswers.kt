package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the club treasurer's notes, three of them not answered by the notes. */
internal object BlindClubFutbolAnswers {
    val all = listOf(
        answer(
            task = "cuánto nos deben de cuotas en total, sin contar a los menores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("6450")
        ),
        answer(
            task = "cuánto suman los gastos de la nota Gastos octubre? descontá lo que nos " +
                "devolvieron",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("9780")
        ),
        answer(
            task = "cuánto le debemos a los árbitros de los partidos que ya se jugaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("5050")
        ),
        answer(
            task = "cuánta plata de la rifa me falta que me rindan los vendedores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("3800")
        ),
        answer(
            task = "de los 300 números de la rifa cuántos quedan sin vender, contando los " +
                "talonarios sin repartir?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("165")
        ),
        answer(
            task = "cuántos puntos llevamos en el Apertura?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("10 puntos")
        ),
        answer(
            task = "cuántos días faltan para el sorteo de la rifa?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("39 días")
        ),
        answer(
            task = "cuántos días hay entre la asamblea y el sorteo de la rifa?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("63 días")
        ),
        answer(
            task = "cuánto falta pagar de los pedidos que todavía no llegaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("18100")
        ),
        answer(
            task = "hasta cuánto puedo gastar yo solo sin pedirle permiso a la comisión?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6000")
        ),
        answer(
            task = "para sacar plata de la cuenta del club quién tiene que firmar conmigo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Lucía Fernández")
        ),
        answer(
            task = "a cuánto queda la cuota de jugador desde enero?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("680")
        ),
        answer(
            task = "quién atiende la cantina en el partido contra Racing Las Palmas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Valeria")
        ),
        answer(
            task = "cuánto va a salir la hora de cancha desde enero?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("1800")
        ),
        answer(
            task = "pasame el teléfono de Rodrigo Suárez",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el teléfono de rodrigo suárez es")
        ),
        answer(
            task = "a qué hora jugamos contra Estrella del Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("es a las 10:00", "es a las 15:30")
        ),
        answer(
            task = "cuánto cuesta la inscripción a la liga para 2027?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("cuesta $7500", "sale $7500", "es de $7500")
        ),
        answer(
            task = "contra quién jugamos el próximo partido y en qué cancha?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            holds = listOf("Defensores del Arroyo", "Parque de los Aromos")
        )
    )
}

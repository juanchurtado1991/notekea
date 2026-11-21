package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the club treasurer's notes, three of them not answered by the notes. */
internal object BlindClubFutbolAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto nos deben de cuotas en total, sin contar a los menores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6450")
        ),
        AgentWideCase(
            task = "cuánto suman los gastos de la nota Gastos octubre? descontá lo que nos " +
                "devolvieron",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9780")
        ),
        AgentWideCase(
            task = "cuánto le debemos a los árbitros de los partidos que ya se jugaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5050")
        ),
        AgentWideCase(
            task = "cuánta plata de la rifa me falta que me rindan los vendedores?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3800")
        ),
        AgentWideCase(
            task = "de los 300 números de la rifa cuántos quedan sin vender, contando los " +
                "talonarios sin repartir?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("165")
        ),
        AgentWideCase(
            task = "cuántos puntos llevamos en el Apertura?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("10 puntos")
        ),
        AgentWideCase(
            task = "cuántos días faltan para el sorteo de la rifa?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 días")
        ),
        AgentWideCase(
            task = "cuántos días hay entre la asamblea y el sorteo de la rifa?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("63 días")
        ),
        AgentWideCase(
            task = "cuánto falta pagar de los pedidos que todavía no llegaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("18100")
        ),
        AgentWideCase(
            task = "hasta cuánto puedo gastar yo solo sin pedirle permiso a la comisión?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6000")
        ),
        AgentWideCase(
            task = "para sacar plata de la cuenta del club quién tiene que firmar conmigo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Lucía Fernández")
        ),
        AgentWideCase(
            task = "a cuánto queda la cuota de jugador desde enero?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("680")
        ),
        AgentWideCase(
            task = "quién atiende la cantina en el partido contra Racing Las Palmas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Valeria")
        ),
        AgentWideCase(
            task = "cuánto va a salir la hora de cancha desde enero?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1800")
        ),
        AgentWideCase(
            task = "pasame el teléfono de Rodrigo Suárez",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el teléfono de rodrigo suárez es")
        ),
        AgentWideCase(
            task = "a qué hora jugamos contra Estrella del Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("es a las 10:00", "es a las 15:30")
        ),
        AgentWideCase(
            task = "cuánto cuesta la inscripción a la liga para 2027?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("cuesta $7500", "sale $7500", "es de $7500")
        ),
        AgentWideCase(
            task = "contra quién jugamos el próximo partido y en qué cancha?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("Defensores del Arroyo", "Parque de los Aromos")
        )
    )
}

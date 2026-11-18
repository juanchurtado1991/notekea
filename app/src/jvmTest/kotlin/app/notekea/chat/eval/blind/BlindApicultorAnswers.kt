package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Apicultor vault, three of them not answered by the notes. */
internal object BlindApicultorAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto me he gastado este año en varroa en el apiario de Órgiva?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("117"),
            lacks = listOf("269")
        ),
        AgentWideCase(
            task = "cuántos días pasaron entre el amitraz y el sublimado de Lanjarón?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("55 días")
        ),
        AgentWideCase(
            task = "cuántos kg de miel envasé este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("462")
        ),
        AgentWideCase(
            task = "cuántos kilos de castaño extraje este año más que en 2025?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("49")
        ),
        AgentWideCase(
            task = "quién me dio la reina nueva de la L3?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Paco Jiménez"),
            lacks = listOf("Olmedo")
        ),
        AgentWideCase(
            task = "cuánto vendí el sábado en el mercado de Órgiva, quitando la devolución?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("348")
        ),
        AgentWideCase(
            task = "cuál fue la media de ventas por mercado en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("377"),
            lacks = listOf("301")
        ),
        AgentWideCase(
            task = "cuánto suman los pedidos online pendientes de envío que ya están pagados?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("102")
        ),
        AgentWideCase(
            task = "cuánto me deben las tiendas y restaurantes en facturas sin cobrar?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("281")
        ),
        AgentWideCase(
            task = "a qué distancia mínima de una casa puedo tener colmenas?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("400")
        ),
        AgentWideCase(
            task = "cuántos días me quedan hasta el último día para presentar la " +
                "declaración censal?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("50 días")
        ),
        AgentWideCase(
            task = "cuánto pago de alquiler por el terreno del apiario de Órgiva?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("60")
        ),
        AgentWideCase(
            task = "necesito 500 tarros de 1 kg con tapa: con qué proveedor me salen más " +
                "baratos y cuánto pagaría en total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Vidrios del Sur"),
            computed = listOf("370"),
            lacks = listOf("345")
        ),
        AgentWideCase(
            task = "qué teléfono tiene Inés Quesada?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("617", "659")
        ),
        AgentWideCase(
            task = "cuántas colmenas tiene Paco Olmedo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("40")
        ),
        AgentWideCase(
            task = "cuándo me caduca el seguro de responsabilidad civil?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("2027-03-15")
        ),
        AgentWideCase(
            task = "quién es ahora el tesorero de la cooperativa?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rocío Aranda")
        ),
        AgentWideCase(
            task = "cuántos días lleva huérfana la L8?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("18 días")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions for the bakery vault: facts, sums, dates, long notes and three it cannot answer. */
internal object BlindPanaderiaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto me deben en total por las facturas de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1599"),
            lacks = listOf("1895")
        ),
        AgentWideCase(
            task = "cuánto me debe Marleny Quispe?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("385"),
            lacks = listOf("681")
        ),
        AgentWideCase(
            task = "cuánto ya me pagaron de las facturas de este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2268")
        ),
        AgentWideCase(
            task = "a qué hora le dejamos el pan a Marleny?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("7:15")
        ),
        AgentWideCase(
            task = "a cuánto está ahora el saco de harina?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("115")
        ),
        AgentWideCase(
            task = "cuánto sumaron los pedidos de octubre, descontando la devolución?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5770")
        ),
        AgentWideCase(
            task = "cuántos días van desde el último mantenimiento del horno?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("84 días")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que vengan a ver la cámara de frío?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("9 días")
        ),
        AgentWideCase(
            task = "a cuánto vendo el panetón al por mayor?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("24")
        ),
        AgentWideCase(
            task = "a qué temperatura se hornea el panetón?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("165")
        ),
        AgentWideCase(
            task = "cuánto le voy a cobrar a Marleny por sus panetones de navidad?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1440")
        ),
        AgentWideCase(
            task = "con cuánta anticipación me tienen que pedir vacaciones?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20 días")
        ),
        AgentWideCase(
            task = "cuánto fue la planilla de octubre sin contar el bono?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6300")
        ),
        AgentWideCase(
            task = "cuántos panes franceses hicimos entre el lunes 9 y el martes 10?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3550")
        ),
        AgentWideCase(
            task = "promedio diario de pan de tres puntas producido del 9 al 11 de noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("630")
        ),
        AgentWideCase(
            task = "cuál es el RUC de Marleny Quispe?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("ruc de marleny quispe es 10294718365")
        ),
        AgentWideCase(
            task = "cuánto me costó el arreglo de la amasadora?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("amasadora costó s/ 450", "amasadora costó 450")
        ),
        AgentWideCase(
            task = "cuándo es el cumpleaños de Luis Apaza?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("luis apaza cumple el 27")
        )
    )
}

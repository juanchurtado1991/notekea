package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuánto paga Diego de alquiler?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("890")
        ),
        AgentWideCase(
            task = "de quién es el piso de Arganzuela 3?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rosa Puig")
        ),
        AgentWideCase(
            task = "quién administra la comunidad de Delicias 40?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Elena Robles")
        ),
        AgentWideCase(
            task = "en el contrato tipo, hasta cuánto pagan los inquilinos de reparaciones?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("150")
        ),
        AgentWideCase(
            task = "los inquilinos pueden tener perro? qué pone el contrato tipo",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15 kg")
        ),
        AgentWideCase(
            task = "Fuencarral 88 tiene trastero?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("T-7")
        ),
        AgentWideCase(
            task = "cuánto han pagado los propietarios en reparaciones este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("935"),
            lacks = listOf("1710")
        ),
        AgentWideCase(
            task = "cuánto suman al mes las cuotas de comunidad de todos los pisos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("544")
        ),
        AgentWideCase(
            task = "cuánto dinero cobré con retraso en septiembre y octubre, y de qué pisos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Delicias 40", "Ercilla 22"),
            computed = listOf("2810")
        ),
        AgentWideCase(
            task = "con cuántos días de antelación me avisó Paula de que se va?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("74 días")
        ),
        AgentWideCase(
            task = "cuántos días le quedan al contrato de Samuel?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("354")
        ),
        AgentWideCase(
            task = "qué propietario me deja más honorarios al mes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rosa Puig", "170")
        ),
        AgentWideCase(
            task = "cuánto pagará Rosa en 2027 por Arganzuela 3 sumando comunidad y derrama?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1908")
        ),
        AgentWideCase(
            task = "cuántas visitas tengo esta semana y con quién?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("Hugo Marín", "Adriana Coll", "Sofía Llorente"),
            lacks = listOf("Bruno Esteve", "Martín Quiroga")
        ),
        AgentWideCase(
            task = "cuánto le tengo que transferir a Andrés de la liquidación de octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("805")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Irene Castaño?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("611 482 907")
        ),
        AgentWideCase(
            task = "cuánto me cobró el pintor por pintar Jaén 9?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("540")
        ),
        AgentWideCase(
            task = "en qué año compró Rosa el piso de Delicias 40?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("compró en 2022", "comprado en 2022", "compró el piso en 2022")
        )
    )
}

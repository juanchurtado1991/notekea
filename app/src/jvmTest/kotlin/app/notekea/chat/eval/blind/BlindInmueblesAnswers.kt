package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindInmueblesAnswers {
    val all = listOf(
        answer(
            task = "cuánto paga Diego de alquiler?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("890")
        ),
        answer(
            task = "de quién es el piso de Arganzuela 3?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Rosa Puig")
        ),
        answer(
            task = "quién administra la comunidad de Delicias 40?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Elena Robles")
        ),
        answer(
            task = "en el contrato tipo, hasta cuánto pagan los inquilinos de reparaciones?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("150")
        ),
        answer(
            task = "los inquilinos pueden tener perro? qué pone el contrato tipo",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15 kg")
        ),
        answer(
            task = "Fuencarral 88 tiene trastero?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("T-7")
        ),
        answer(
            task = "cuánto han pagado los propietarios en reparaciones este año?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("935"),
            lacks = listOf("1710")
        ),
        answer(
            task = "cuánto suman al mes las cuotas de comunidad de todos los pisos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("544")
        ),
        answer(
            task = "cuánto dinero cobré con retraso en septiembre y octubre, y de qué pisos?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Delicias 40", "Ercilla 22"),
            computed = listOf("2810")
        ),
        answer(
            task = "con cuántos días de antelación me avisó Paula de que se va?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("74 días")
        ),
        answer(
            task = "cuántos días le quedan al contrato de Samuel?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("354")
        ),
        answer(
            task = "qué propietario me deja más honorarios al mes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            holds = listOf("Rosa Puig", "170")
        ),
        answer(
            task = "cuánto pagará Rosa en 2027 por Arganzuela 3 sumando comunidad y derrama?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("1908")
        ),
        answer(
            task = "cuántas visitas tengo esta semana y con quién?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            holds = listOf("Hugo Marín", "Adriana Coll", "Sofía Llorente"),
            lacks = listOf("Bruno Esteve", "Martín Quiroga")
        ),
        answer(
            task = "cuánto le tengo que transferir a Andrés de la liquidación de octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("805")
        ),
        answer(
            task = "cuál es el teléfono de Irene Castaño?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("611 482 907")
        ),
        answer(
            task = "cuánto me cobró el pintor por pintar Jaén 9?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("540")
        ),
        answer(
            task = "en qué año compró Rosa el piso de Delicias 40?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("compró en 2022", "comprado en 2022", "compró el piso en 2022")
        )
    )
}

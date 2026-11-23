package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindBodasAnswers {
    val all = listOf(
        answer(
            task = "cuántos invitados confirmados tiene la boda de Martina y Tomás?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("128")
        ),
        answer(
            task = "qué plato principal eligieron Sofía y Federico para su boda?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("ojo de bife"),
            lacks = listOf("lomo al malbec")
        ),
        answer(
            task = "cuántos días faltan para la boda de Martina y Tomás?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("24 días")
        ),
        answer(
            task = "cuántos días hay entre la degustación de Sofía y Federico y su boda?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("108 días")
        ),
        answer(
            task = "según el contrato, cuánto cobro por cada hora extra el día de la boda?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("380.000"),
            lacks = listOf("85.000")
        ),
        answer(
            task = "Martina tiene alguna alergia que tenga que avisarle al catering?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("frutos secos")
        ),
        answer(
            task = "sumando la boda de Martina y la de Sofía, cuántos invitados confirmados tengo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("189")
        ),
        answer(
            task = "cuál es el promedio de invitados en lista entre mis tres bodas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("155")
        ),
        answer(
            task = "qué catering me dio la mejor degustación, el de octubre o el de noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Almagro Sur")
        ),
        answer(
            task = "qué facturas tengo sin cobrar?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("F-0043", "F-0046", "F-0048"),
            lacks = listOf("F-0041", "F-0044")
        ),
        answer(
            task = "cuántas horas facturables le dediqué a la boda de Sofía y Federico en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("18 horas")
        ),
        answer(
            task = "qué saldos de lo de Martina y Tomás vencen antes del 30 de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Quinta Los Aromos", "Almagro Sur"),
            lacks = listOf("Ferreyra", "Benítez", "Dulce Abril")
        ),
        answer(
            task = "qué tema eligieron Sofía y Federico para el vals?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Luz de Diciembre")
        ),
        answer(
            task = "cuántos días pasaron desde que Camila y Diego firmaron el contrato conmigo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("76 días")
        ),
        answer(
            task = "cuál es el CUIT de Catering Almagro Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("30-71458823-6")
        ),
        answer(
            task = "cuánto pagaron Camila y Diego de seña a la Quinta Los Aromos?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("3.500.000")
        ),
        answer(
            task = "a qué hora llega el fotógrafo a la boda de Sofía y Federico?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("13:00")
        ),
        answer(
            task = "qué salón tenía problemas de estacionamiento?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("El Ombú Viejo")
        )
    )
}

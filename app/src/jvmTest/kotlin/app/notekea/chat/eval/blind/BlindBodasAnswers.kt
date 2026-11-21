package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindBodasAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuántos invitados confirmados tiene la boda de Martina y Tomás?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("128")
        ),
        AgentWideCase(
            task = "qué plato principal eligieron Sofía y Federico para su boda?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("ojo de bife"),
            lacks = listOf("lomo al malbec")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la boda de Martina y Tomás?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("24 días")
        ),
        AgentWideCase(
            task = "cuántos días hay entre la degustación de Sofía y Federico y su boda?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("108 días")
        ),
        AgentWideCase(
            task = "según el contrato, cuánto cobro por cada hora extra el día de la boda?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("380.000"),
            lacks = listOf("85.000")
        ),
        AgentWideCase(
            task = "Martina tiene alguna alergia que tenga que avisarle al catering?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("frutos secos")
        ),
        AgentWideCase(
            task = "sumando la boda de Martina y la de Sofía, cuántos invitados confirmados tengo?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("189")
        ),
        AgentWideCase(
            task = "cuál es el promedio de invitados en lista entre mis tres bodas?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("155")
        ),
        AgentWideCase(
            task = "qué catering me dio la mejor degustación, el de octubre o el de noviembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Almagro Sur")
        ),
        AgentWideCase(
            task = "qué facturas tengo sin cobrar?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("F-0043", "F-0046", "F-0048"),
            lacks = listOf("F-0041", "F-0044")
        ),
        AgentWideCase(
            task = "cuántas horas facturables le dediqué a la boda de Sofía y Federico en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("18 horas")
        ),
        AgentWideCase(
            task = "qué saldos de lo de Martina y Tomás vencen antes del 30 de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Quinta Los Aromos", "Almagro Sur"),
            lacks = listOf("Ferreyra", "Benítez", "Dulce Abril")
        ),
        AgentWideCase(
            task = "qué tema eligieron Sofía y Federico para el vals?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Luz de Diciembre")
        ),
        AgentWideCase(
            task = "cuántos días pasaron desde que Camila y Diego firmaron el contrato conmigo?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("76 días")
        ),
        AgentWideCase(
            task = "cuál es el CUIT de Catering Almagro Sur?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("30-71458823-6")
        ),
        AgentWideCase(
            task = "cuánto pagaron Camila y Diego de seña a la Quinta Los Aromos?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("3.500.000")
        ),
        AgentWideCase(
            task = "a qué hora llega el fotógrafo a la boda de Sofía y Federico?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("13:00")
        ),
        AgentWideCase(
            task = "qué salón tenía problemas de estacionamiento?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("El Ombú Viejo")
        )
    )
}

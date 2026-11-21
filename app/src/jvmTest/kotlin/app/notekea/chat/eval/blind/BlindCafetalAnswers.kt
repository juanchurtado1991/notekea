package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCafetalAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuántos kg de fruta llevo cogidos en El Higuerón?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("830")
        ),
        AgentWideCase(
            task = "cuánto entregué de La Quebrada sin contar lo que rechazaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("490")
        ),
        AgentWideCase(
            task = "qué variedad hay sembrada en La Quebrada?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Obatá")
        ),
        AgentWideCase(
            task = "cuántas cajuelas se cogieron en total la semana 45?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("179")
        ),
        AgentWideCase(
            task = "a quién le debo todavía de la planilla de la semana 45?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("José Vindas", "Kendall Ortiz", "Rosa Zúñiga")
        ),
        AgentWideCase(
            task = "cuánta plata me deben en facturas pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1515"),
            lacks = listOf("2475")
        ),
        AgentWideCase(
            task = "cuántos días lleva sin pagar la factura pendiente de Doña Lidia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("43 días")
        ),
        AgentWideCase(
            task = "cuántos quintales tengo firmados en contratos esta cosecha?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("74")
        ),
        AgentWideCase(
            task = "cuál fue el promedio de puntaje de las cuatro muestras de la catación?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("86")
        ),
        AgentWideCase(
            task = "el Geisha llega al puntaje que pide Mauricio para el contrato?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("87", "89")
        ),
        AgentWideCase(
            task = "cómo se llama la que viene a hacer la auditoría del sello?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Priscilla Chaves")
        ),
        AgentWideCase(
            task = "cuántos días faltan para la auditoría del sello?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("69 días")
        ),
        AgentWideCase(
            task = "si tengo un problema con la liquidación del beneficio, con quién hablo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Olman Mena")
        ),
        AgentWideCase(
            task = "cuántos metros de zona de protección pide el sello para la quebrada?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10 m")
        ),
        AgentWideCase(
            task = "cuánto gasté en la finca en octubre, sin contar lo de la casa?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("733")
        ),
        AgentWideCase(
            task = "cuántos kg de oro dio Alto Palmira la cosecha pasada?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("714")
        ),
        AgentWideCase(
            task = "cuántos quintales le vendo a Esteban este año?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("60")
        ),
        // The notes do not answer the next three.
        AgentWideCase(
            task = "qué puntaje sacó El Higuerón en la catación del año pasado?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "a cuánto me vende el fungicida el agroservicio?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "cuál fue el rendimiento de Alto Palmira en la cosecha 2024-25?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        )
    )
}

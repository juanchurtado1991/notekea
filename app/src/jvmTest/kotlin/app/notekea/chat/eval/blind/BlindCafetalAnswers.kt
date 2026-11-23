package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCafetalAnswers {
    val all = listOf(
        answer(
            task = "cuántos kg de fruta llevo cogidos en El Higuerón?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("830")
        ),
        answer(
            task = "cuánto entregué de La Quebrada sin contar lo que rechazaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("490")
        ),
        answer(
            task = "qué variedad hay sembrada en La Quebrada?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Obatá")
        ),
        answer(
            task = "cuántas cajuelas se cogieron en total la semana 45?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("179")
        ),
        answer(
            task = "a quién le debo todavía de la planilla de la semana 45?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("José Vindas", "Kendall Ortiz", "Rosa Zúñiga")
        ),
        answer(
            task = "cuánta plata me deben en facturas pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1515"),
            lacks = listOf("2475")
        ),
        answer(
            task = "cuántos días lleva sin pagar la factura pendiente de Doña Lidia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("43 días")
        ),
        answer(
            task = "cuántos quintales tengo firmados en contratos esta cosecha?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("74")
        ),
        answer(
            task = "cuál fue el promedio de puntaje de las cuatro muestras de la catación?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("86")
        ),
        answer(
            task = "el Geisha llega al puntaje que pide Mauricio para el contrato?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("87", "89")
        ),
        answer(
            task = "cómo se llama la que viene a hacer la auditoría del sello?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Priscilla Chaves")
        ),
        answer(
            task = "cuántos días faltan para la auditoría del sello?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("69 días")
        ),
        answer(
            task = "si tengo un problema con la liquidación del beneficio, con quién hablo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Olman Mena")
        ),
        answer(
            task = "cuántos metros de zona de protección pide el sello para la quebrada?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("10 m")
        ),
        answer(
            task = "cuánto gasté en la finca en octubre, sin contar lo de la casa?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("733")
        ),
        answer(
            task = "cuántos kg de oro dio Alto Palmira la cosecha pasada?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("714")
        ),
        answer(
            task = "cuántos quintales le vendo a Esteban este año?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("60")
        ),
        // The notes do not answer the next three.
        answer(
            task = "qué puntaje sacó El Higuerón en la catación del año pasado?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer")
        ),
        answer(
            task = "a cuánto me vende el fungicida el agroservicio?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer")
        ),
        answer(
            task = "cuál fue el rendimiento de Alto Palmira en la cosecha 2024-25?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer")
        )
    )
}

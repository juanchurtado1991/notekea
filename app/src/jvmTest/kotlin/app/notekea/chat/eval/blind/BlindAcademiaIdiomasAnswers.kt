package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the academy vault, three it cannot answer. */
internal object BlindAcademiaIdiomasAnswers {
    val all = listOf(
        answer(
            task = "¿cuánto llevo cobrado en la lista de pagos de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("581")
        ),
        answer(
            task = "¿cuánto suman los pedidos de material que siguen pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("905")
        ),
        answer(
            task = "¿cuánto le cuesta de verdad a la academia la semana cultural?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("785"),
            lacks = listOf("cuesta 1.235")
        ),
        answer(
            task = "¿cuánto le tengo que pagar a Lucía Herrera por octubre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("912")
        ),
        answer(
            task = "¿cuántas horas dio el profesorado en total en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("272")
        ),
        answer(
            task = "¿qué puntuación media sacaron las pruebas de nivel que ya están hechas?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("76")
        ),
        answer(
            task = "¿quién va a cubrir el alemán mientras Markus esté de baja?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Inés Navarro")
        ),
        answer(
            task = "¿quién nos hace la auditoría externa de calidad?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Patricia Lozano", "Certifica Sur")
        ),
        answer(
            task = "¿cuántos días quedan para la auditoría externa?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("97 días")
        ),
        answer(
            task = "¿cuánto es la tasa del examen oficial del C1?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("212")
        ),
        answer(
            task = "¿cuándo nos sube el alquiler del local?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("marzo")
        ),
        answer(
            task = "¿qué grupo tiene más alumnos, el de alemán o el de francés?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("alemán", "8")
        ),
        answer(
            task = "¿quién le da clase a Kenji?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            holds = listOf("Carmen Vidal")
        ),
        answer(
            task = "pásame el teléfono de Lucía Herrera",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("677 845 102")
        ),
        answer(
            task = "¿cuántos días pasaron entre la matrícula de Elena Castro y la de Kenji?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("43 días")
        ),
        answer(
            task = "¿cuántos días faltan para que empiece la semana cultural?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("33 días")
        ),
        answer(
            task = "¿quién va a dar el italiano?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf(
                "lo dará carmen vidal",
                "lo impartirá carmen vidal",
                "carmen vidal dará"
            )
        ),
        answer(
            task = "pásame el teléfono de Markus",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("su teléfono es", "el teléfono de markus es")
        ),
        answer(
            task = "¿cuánto costó el proyector del aula 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el proyector del aula 1 costó", "el del aula 1 costó")
        )
    )
}

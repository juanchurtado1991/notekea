package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the academy vault, three it cannot answer. */
internal object BlindAcademiaIdiomasAnswers {
    val all = listOf(
        AgentWideCase(
            task = "¿cuánto llevo cobrado en la lista de pagos de noviembre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("581")
        ),
        AgentWideCase(
            task = "¿cuánto suman los pedidos de material que siguen pendientes?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("905")
        ),
        AgentWideCase(
            task = "¿cuánto le cuesta de verdad a la academia la semana cultural?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("785"),
            lacks = listOf("cuesta 1.235")
        ),
        AgentWideCase(
            task = "¿cuánto le tengo que pagar a Lucía Herrera por octubre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("912")
        ),
        AgentWideCase(
            task = "¿cuántas horas dio el profesorado en total en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("272")
        ),
        AgentWideCase(
            task = "¿qué puntuación media sacaron las pruebas de nivel que ya están hechas?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("76")
        ),
        AgentWideCase(
            task = "¿quién va a cubrir el alemán mientras Markus esté de baja?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Inés Navarro")
        ),
        AgentWideCase(
            task = "¿quién nos hace la auditoría externa de calidad?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Patricia Lozano", "Certifica Sur")
        ),
        AgentWideCase(
            task = "¿cuántos días quedan para la auditoría externa?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("97 días")
        ),
        AgentWideCase(
            task = "¿cuánto es la tasa del examen oficial del C1?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("212")
        ),
        AgentWideCase(
            task = "¿cuándo nos sube el alquiler del local?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("marzo")
        ),
        AgentWideCase(
            task = "¿qué grupo tiene más alumnos, el de alemán o el de francés?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("alemán", "8")
        ),
        AgentWideCase(
            task = "¿quién le da clase a Kenji?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Carmen Vidal")
        ),
        AgentWideCase(
            task = "pásame el teléfono de Lucía Herrera",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("677 845 102")
        ),
        AgentWideCase(
            task = "¿cuántos días pasaron entre la matrícula de Elena Castro y la de Kenji?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("43 días")
        ),
        AgentWideCase(
            task = "¿cuántos días faltan para que empiece la semana cultural?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("33 días")
        ),
        AgentWideCase(
            task = "¿quién va a dar el italiano?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf(
                "lo dará carmen vidal",
                "lo impartirá carmen vidal",
                "carmen vidal dará"
            )
        ),
        AgentWideCase(
            task = "pásame el teléfono de Markus",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("su teléfono es", "el teléfono de markus es")
        ),
        AgentWideCase(
            task = "¿cuánto costó el proyector del aula 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el proyector del aula 1 costó", "el del aula 1 costó")
        )
    )
}

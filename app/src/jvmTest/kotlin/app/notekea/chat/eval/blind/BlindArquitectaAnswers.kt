package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Arquitecta notes, three of them not answered by any note. */
internal object BlindArquitectaAnswers {
    val all = listOf(
        AgentWideCase(
            task = "cuál es el número del permiso de la casa la reina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("PE-2026-0418")
        ),
        AgentWideCase(
            task = "qué porcentaje del honorario de La Reina ya me pagaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("60")
        ),
        AgentWideCase(
            task = "cuántos m2 suman mis cuatro proyectos?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("471")
        ),
        AgentWideCase(
            task = "cuál cotización de ventanas de La Reina sale más barata con la instalación " +
                "incluida?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Termopanel Sur", "6.210.000"),
            lacks = listOf("la más barata es aluminios")
        ),
        AgentWideCase(
            task = "cuántos días pasaron entre que ingresé el permiso de Ñuñoa y el acta de " +
                "observaciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("38 días")
        ),
        AgentWideCase(
            task = "según mis apuntes de la ordenanza, qué vidrio tiene que llevar una baranda " +
                "de vidrio?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("laminado")
        ),
        AgentWideCase(
            task = "de qué madera va a ser el deck de la terraza de Rodrigo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("lenga")
        ),
        AgentWideCase(
            task = "quién hace el cálculo estructural de Peñalolén?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Marcela Oyarce")
        ),
        AgentWideCase(
            task = "cuánto me debe Patricia de la cuota vencida?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1.800.000")
        ),
        AgentWideCase(
            task = "cuánto duraron en promedio las reuniones con clientes que ya tuve este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("70")
        ),
        AgentWideCase(
            task = "qué proyecto me paga más honorario por m2?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ñuñoa")
        ),
        AgentWideCase(
            task = "cuántos días faltan para que venza la cuota 4 de La Reina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("19 días")
        ),
        AgentWideCase(
            task = "cuántas horas facturables le hice a Peñalolén en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("35")
        ),
        AgentWideCase(
            task = "cuántos días se demoró la DOM en aprobar el permiso de La Reina desde el " +
                "ingreso?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("106 días")
        ),
        AgentWideCase(
            task = "de qué color quiere Patricia la fachada?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("gris perla")
        ),
        AgentWideCase(
            task = "cuál es el teléfono de Jorge Salinas?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el teléfono de jorge salinas es")
        ),
        AgentWideCase(
            task = "cuánto costó al final la obra de Providencia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("costó $31.400.000", "el costo final fue")
        ),
        AgentWideCase(
            task = "cuántos m2 tiene el terreno de Peñalolén?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("el terreno tiene 142")
        )
    )
}

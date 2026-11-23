package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Arquitecta notes, three of them not answered by any note. */
internal object BlindArquitectaAnswers {
    val all = listOf(
        answer(
            task = "cuál es el número del permiso de la casa la reina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("PE-2026-0418")
        ),
        answer(
            task = "qué porcentaje del honorario de La Reina ya me pagaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("60")
        ),
        answer(
            task = "cuántos m2 suman mis cuatro proyectos?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "calculate", "answer"),
            computed = listOf("471")
        ),
        answer(
            task = "cuál cotización de ventanas de La Reina sale más barata con la instalación " +
                "incluida?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Termopanel Sur", "6.210.000"),
            lacks = listOf("la más barata es aluminios")
        ),
        answer(
            task = "cuántos días pasaron entre que ingresé el permiso de Ñuñoa y el acta de " +
                "observaciones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("38 días")
        ),
        answer(
            task = "según mis apuntes de la ordenanza, qué vidrio tiene que llevar una baranda " +
                "de vidrio?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("laminado")
        ),
        answer(
            task = "de qué madera va a ser el deck de la terraza de Rodrigo?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("lenga")
        ),
        answer(
            task = "quién hace el cálculo estructural de Peñalolén?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Marcela Oyarce")
        ),
        answer(
            task = "cuánto me debe Patricia de la cuota vencida?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1.800.000")
        ),
        answer(
            task = "cuánto duraron en promedio las reuniones con clientes que ya tuve este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("70")
        ),
        answer(
            task = "qué proyecto me paga más honorario por m2?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("Ñuñoa")
        ),
        answer(
            task = "cuántos días faltan para que venza la cuota 4 de La Reina?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("19 días")
        ),
        answer(
            task = "cuántas horas facturables le hice a Peñalolén en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("35")
        ),
        answer(
            task = "cuántos días se demoró la DOM en aprobar el permiso de La Reina desde el " +
                "ingreso?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("106 días")
        ),
        answer(
            task = "de qué color quiere Patricia la fachada?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("gris perla")
        ),
        answer(
            task = "cuál es el teléfono de Jorge Salinas?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el teléfono de jorge salinas es")
        ),
        answer(
            task = "cuánto costó al final la obra de Providencia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("costó $31.400.000", "el costo final fue")
        ),
        answer(
            task = "cuántos m2 tiene el terreno de Peñalolén?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("el terreno tiene 142")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCarpinteroAnswers {
    val all = listOf(
        answer(
            task = "¿cuántos días pasaron entre la señal de Iñaki Zubiaurre y la de Iñaki Larrea?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("17 días")
        ),
        answer(
            task = "¿cuánto me ha costado de media cada visita al taller de la furgoneta? " +
                "cuenta todas las que tengo apuntadas",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("250")
        ),
        answer(
            task = "¿cuánto gasté en octubre en compras que no fueran madera? " +
                "sin contar lo devuelto",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("376")
        ),
        answer(
            task = "¿cuánto les debo todavía a los proveedores?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("506")
        ),
        answer(
            task = "¿cuántas horas llevo apuntadas este mes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("55")
        ),
        answer(
            task = "¿cuál fue mi media de horas por semana en octubre?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("39")
        ),
        answer(
            task = "¿cuántos días me quedan para entregar la barra del Txoko?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("70 días")
        ),
        answer(
            task = "¿cuánto llevo gastado en casa este mes sin contar el alquiler?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("492")
        ),
        answer(
            task = "¿qué encargos tengo que entregar en diciembre?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "answer"),
            holds = listOf("Zubiaurre", "Larrea", "Sáez")
        ),
        answer(
            task = "¿qué acabado va a llevar la cuna de Begoña?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("cera dura")
        ),
        answer(
            task = "¿con quién hablo en el Txoko para lo de la barra? y su teléfono",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Maite", "944 216 803")
        ),
        answer(
            task = "¿qué mascarilla tengo que ponerme para cortar MDF?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("FFP3")
        ),
        answer(
            task = "¿cada cuánto le cambio el filtro al aspirador de virutas?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("40 horas")
        ),
        answer(
            task = "¿a qué altura va al final la barra del Txoko?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("108")
        ),
        answer(
            task = "¿a qué hora tengo el médico?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("9:15")
        ),
        answer(
            task = "¿qué le devolví a Herrajes Deusto y cuánto me abonaron?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("correderas", "214")
        ),
        answer(
            task = "¿cuánto cobro yo por hora?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("cobras 35", "tu tarifa es de 35")
        ),
        answer(
            task = "¿qué teléfono tiene Koldo Arrieta?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf(
                "su teléfono es",
                "634 718 260",
                "677 210 584",
                "688 412 907",
                "944 216 803"
            )
        ),
        answer(
            task = "¿cuánto me costó la ingletadora?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("la ingletadora costó", "la ingletadora te costó")
        )
    )
}

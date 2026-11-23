package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Arquitecta requests that must be asked back, then traps whose words settle them. */
internal object BlindArquitectaAsks {
    val all = listOf(
        ask(
            task = "sube el arriendo del presupuesto a $800.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("estudio", "personal")
        ),
        ask(
            task = "agrega una tarea a la nota de Muñoz: enviar planos firmados",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Rodrigo", "Patricia")
        ),
        ask(
            task = "cambia el correo de Jorge en su nota a jorge@obrasnuevas.cl",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Salinas", "Pizarro")
        ),
        ask(
            task = "corre la reunión con Rodrigo una hora más tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("13", "19", "26")
        ),
        ask(
            task = "en la cotización de ventanas anota que piden 50% de anticipo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Ventanas Andes", "Aluminios del Maipo", "Termopanel Sur")
        ),
        ask(
            task = "renombra Presupuesto a Presupuesto 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("estudio", "personal")
        ),
        ask(
            task = "cambia el status del proyecto de Muñoz a pausado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("La Reina", "Ñuñoa")
        ),
        ask(
            task = "marca la observación de la DOM como subsanada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("rasante", "estacionamiento")
        ),
        ask(
            task = "cambia el monto de la cotización de Jorge a $5.900.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Salinas", "Pizarro")
        ),
        change(
            task = "sube el arriendo del presupuesto del estudio a $950.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("950.000")
        ),
        change(
            task = "agrega una tarea a Patricia Muñoz: enviar acta firmada a la DOM",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Patricia Muñoz", "acta firmada"),
            lacks = listOf("Rodrigo Muñoz")
        ),
        answer(
            task = "cuál es el correo de Jorge Pizarro?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("jpizarro.obras@correo.cl")
        ),
        change(
            task = "mueve la reunión con Rodrigo del 19 a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Agenda noviembre", "17:00")
        ),
        answer(
            task = "cuánto cotizaron los dos Jorge para la ampliación de Ñuñoa?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6.100.000", "5.750.000")
        )
    )
}

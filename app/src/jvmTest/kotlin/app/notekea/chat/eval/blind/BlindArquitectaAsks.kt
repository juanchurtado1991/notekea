package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Arquitecta requests that must be asked back, then traps whose words settle them. */
internal object BlindArquitectaAsks {
    val all = listOf(
        AgentWideCase(
            task = "sube el arriendo del presupuesto a $800.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("estudio", "personal")
        ),
        AgentWideCase(
            task = "agrega una tarea a la nota de Muñoz: enviar planos firmados",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Rodrigo", "Patricia")
        ),
        AgentWideCase(
            task = "cambia el correo de Jorge en su nota a jorge@obrasnuevas.cl",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Salinas", "Pizarro")
        ),
        AgentWideCase(
            task = "corre la reunión con Rodrigo una hora más tarde",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("13", "19", "26")
        ),
        AgentWideCase(
            task = "en la cotización de ventanas anota que piden 50% de anticipo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ventanas Andes", "Aluminios del Maipo", "Termopanel Sur")
        ),
        AgentWideCase(
            task = "renombra Presupuesto a Presupuesto 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("estudio", "personal")
        ),
        AgentWideCase(
            task = "cambia el status del proyecto de Muñoz a pausado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("La Reina", "Ñuñoa")
        ),
        AgentWideCase(
            task = "marca la observación de la DOM como subsanada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("rasante", "estacionamiento")
        ),
        AgentWideCase(
            task = "cambia el monto de la cotización de Jorge a $5.900.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Salinas", "Pizarro")
        ),
        AgentWideCase(
            task = "sube el arriendo del presupuesto del estudio a $950.000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("950.000")
        ),
        AgentWideCase(
            task = "agrega una tarea a Patricia Muñoz: enviar acta firmada a la DOM",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Patricia Muñoz", "acta firmada"),
            lacks = listOf("Rodrigo Muñoz")
        ),
        AgentWideCase(
            task = "cuál es el correo de Jorge Pizarro?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("jpizarro.obras@correo.cl")
        ),
        AgentWideCase(
            task = "mueve la reunión con Rodrigo del 19 a las 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda noviembre", "17:00")
        ),
        AgentWideCase(
            task = "cuánto cotizaron los dos Jorge para la ampliación de Ñuñoa?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6.100.000", "5.750.000")
        )
    )
}

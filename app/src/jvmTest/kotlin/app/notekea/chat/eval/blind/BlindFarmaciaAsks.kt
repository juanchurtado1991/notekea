package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFarmaciaAsks {
    val all = listOf(
        AgentWideCase(
            task = "agrega 'revisar extintor' al checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("checklist")
        ),
        AgentWideCase(
            task = "en Precios deja el ibuprofeno en $1.590",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("venta")
        ),
        AgentWideCase(
            task = "marca como recibido el pedido de Pacífico Sur",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "saca a Camila del turno del 29, la reemplaza Javiera",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Rojas", "Fuentes")
        ),
        AgentWideCase(
            task = "Camila cambió de celular, ponle el nuevo en su nota: +56 9 4410 2277",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Rojas", "Fuentes")
        ),
        AgentWideCase(
            task = "pasa la visita del representante al viernes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Austral", "del Pacífico", "Pacífico Sur")
        ),
        AgentWideCase(
            task = "en la nota de Droguería Pacífico agrega la tarea: pedir lista de precios 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("del Pacífico", "Pacífico Sur")
        ),
        AgentWideCase(
            task = "ya pagué la factura de Austral, márcala pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        // Traps: they look ambiguous, but their words settle them.
        AgentWideCase(
            task = "marca como recibido el pedido de la insulina",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("P-1190"),
            lacks = listOf("P-1195")
        ),
        AgentWideCase(
            task = "agrega 'revisar extintor' al checklist de apertura",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("revisar extintor"),
            lacks = listOf("Normativa")
        ),
        AgentWideCase(
            task = "Camila Fuentes no puede el domingo 29, ponle a Matías en su lugar",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Turnos", "Matías")
        ),
        AgentWideCase(
            task = "cambia la visita de Valentina a las 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda", "15:00")
        ),
        AgentWideCase(
            task = "la A-1534 la pagué hoy, márcala pagada",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("A-1534", "pagada"),
            lacks = listOf("A-1561")
        ),
        AgentWideCase(
            task = "cuál es el celular de Camila Rojas?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("5531 0982"),
            lacks = listOf("6274 1135")
        )
    )
}

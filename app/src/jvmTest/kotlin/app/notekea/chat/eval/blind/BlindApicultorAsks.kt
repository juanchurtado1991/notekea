package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Apicultor vault cannot settle, and look-alikes whose words do settle them. */
internal object BlindApicultorAsks {
    val all = listOf(
        AgentWideCase(
            task = "añade al presupuesto 2027 una partida de 150 € para una báscula",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Colmenas", "Ventas")
        ),
        AgentWideCase(
            task = "apunta en la nota de Paco que ya ha pagado la cuota de 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Jiménez", "Olmedo")
        ),
        AgentWideCase(
            task = "ha subido 5 € la cuota del mercado, cámbiala en la nota",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Granada", "Motril")
        ),
        AgentWideCase(
            task = "marca el pedido pendiente como enviado",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Vílchez", "Ortega")
        ),
        AgentWideCase(
            task = "pasa la cita de la OCA a las 13:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("19")
        ),
        AgentWideCase(
            task = "añade en la nota de Paco una tarea: devolverle las cajas de transporte",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Jiménez", "Olmedo")
        ),
        AgentWideCase(
            task = "apunta en el tratamiento de Lanjarón que hubo que repetir la dosis",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("amitraz", "sublimado")
        ),
        AgentWideCase(
            task = "marca como pagada la factura pendiente",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Alacena", "Herboristería")
        ),
        AgentWideCase(
            task = "apunta en la nota de Paco Olmedo que ya ha pagado la cuota de 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Paco Olmedo"),
            lacks = listOf("Paco Jiménez")
        ),
        AgentWideCase(
            task = "cuánto tengo para combustible en el presupuesto 2027 de ventas?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("310"),
            lacks = listOf("450")
        ),
        AgentWideCase(
            task = "el mercadillo de Motril empieza a las 18:00, cámbialo",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Motril", "18:00")
        ),
        AgentWideCase(
            task = "marca como pagadas las dos facturas pendientes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "edit_note", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("La Alacena de Lola", "Herboristería Romero")
        ),
        AgentWideCase(
            task = "en qué pueblo vive cada uno de los dos Pacos?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Pampaneira", "Lanjarón")
        )
    )
}

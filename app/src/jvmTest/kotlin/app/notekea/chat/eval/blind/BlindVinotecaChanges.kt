package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes the wine shop owner asks for; each is settled by her words and her notes. */
internal object BlindVinotecaChanges {
    val all = listOf(
        AgentWideCase(
            task = "agregá a pendientes del local: reclamar a Cerro Azul las botellas con corcho",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pendientes del local", "corcho")
        ),
        AgentWideCase(
            task = "poné status agotado en la nota del Blend de Altura",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Blend de Altura 2019", "agotado")
        ),
        AgentWideCase(
            task = "poneles el tag #deuda a todas las bodegas " +
                "a las que les tengo una factura impaga",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("deuda", "Cerro Alto", "Vientos del Este", "Cerro Azul"),
            lacks = listOf("Las Tunas")
        ),
        AgentWideCase(
            task = "anotá en la nota de Vientos del Este " +
                "que el pedido 315 llegó con 2 botellas rotas",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Vientos del Este", "rotas")
        ),
        AgentWideCase(
            task = "armame una nota 'Resumen club noviembre' con los socios activos, " +
                "su plan y cuántas botellas lleva cada uno",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Resumen club noviembre", "Paula Ibarra", "Nicolás Pardo"),
            lacks = listOf("Hernán Toledo")
        ),
        AgentWideCase(
            task = "mové la nota de la degustación de Torrontés a una carpeta Archivo dentro de " +
                "Degustaciones",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Degustación Torrontés 2026-08-22", "Archivo")
        ),
        AgentWideCase(
            task = "renombrá la nota de bajas y roturas a 'Roturas y bajas 2026'",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Roturas y bajas 2026")
        ),
        AgentWideCase(
            task = "en pedidos pendientes cambiá la entrega del pedido 312 al 2026-11-24",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pedidos pendientes", "312")
        ),
        AgentWideCase(
            task = "a las notas de socios que tienen plan Reserva agregales el tag #reserva",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Martín Gálvez", "Valeria Quiroga"),
            lacks = listOf("Lucía Bustos")
        ),
        AgentWideCase(
            task = "en la nota de Martín Gálvez poné plan: Gran Reserva",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Martín Gálvez", "Gran Reserva")
        ),
        AgentWideCase(
            task = "sumá una tarea en la agenda de degustaciones: " +
                "comprar copas para la de espumantes",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Agenda de degustaciones", "copas")
        ),
        AgentWideCase(
            task = "haceme una nota 'Deudas con bodegas' con las facturas que todavía no pagué",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Deudas con bodegas", "1.020.000", "412.000", "290.000")
        )
    )
}

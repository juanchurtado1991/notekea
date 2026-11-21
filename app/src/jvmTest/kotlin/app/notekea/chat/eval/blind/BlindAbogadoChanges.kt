package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAbogadoChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrégale a Lucía Fernández una tarea: pedir la constancia de ingresos " +
                "de Óscar antes del 20 de noviembre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucía Fernández", "constancia de ingresos")
        ),
        AgentWideCase(
            task = "cámbiale el status a Ramiro Olvera a archivado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ramiro Olvera", "archivado")
        ),
        AgentWideCase(
            task = "ponle #adeudo a cada cliente que tenga una factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Constructora Arista", "Lucía Fernández", "Héctor Galindo"),
            lacks = listOf("Mario Fernández", "Ramiro Olvera", "Patricia Ibarra")
        ),
        AgentWideCase(
            task = "hazme una nota Audiencias pendientes con todas las audiencias que vienen, " +
                "ordenadas por fecha",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Audiencias pendientes",
                "Mario Fernández",
                "Lucía Fernández",
                "Héctor Galindo"
            ),
            lacks = listOf("constitucional")
        ),
        AgentWideCase(
            task = "mueve a Ramiro Olvera a una carpeta Archivo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ramiro Olvera", "Archivo")
        ),
        AgentWideCase(
            task = "en la carta de encargo sube el anticipo del 40% al 50%",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Carta de encargo", "50%")
        ),
        AgentWideCase(
            task = "anota en la nota de Mario que la empresa subió su oferta a $120,000",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mario Fernández", "120000")
        ),
        AgentWideCase(
            task = "crea en Contactos una nota para la Lic. Mónica Esparza, la nueva abogada del " +
                "ferretero en lo de Arista",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mónica Esparza", "Contactos")
        ),
        AgentWideCase(
            task = "la audiencia de alimentos de Lucía se pasó al 2026-12-16 a las 13:00, " +
                "actualízala",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucía Fernández", "2026-12-16")
        ),
        AgentWideCase(
            task = "ponle a Patricia Ibarra la propiedad expediente: SUC-0412/2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Patricia Ibarra", "SUC-0412/2026")
        ),
        AgentWideCase(
            task = "hazme una nota en Honorarios con lo que me debe cada cliente",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Honorarios", "24000", "19000", "11000"),
            lacks = listOf("9500")
        ),
        AgentWideCase(
            task = "agrega a mi bitácora de ayer que hablé con Beatriz sobre el embargo de cuentas",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "Beatriz")
        )
    )
}

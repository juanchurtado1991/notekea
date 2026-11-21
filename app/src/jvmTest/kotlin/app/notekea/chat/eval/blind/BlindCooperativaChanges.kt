package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaChanges {
    val all = listOf(
        AgentWideCase(
            task = "agrega a mis pendientes de la semana: comprar costales para el maíz",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pendientes de la semana", "costales")
        ),
        AgentWideCase(
            task = "marca como pagado lo de Teodora en los pagos pendientes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pendientes", "Teodora")
        ),
        AgentWideCase(
            task = "ponle status firmado al contrato de maíz",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Contrato de maíz", "firmado")
        ),
        AgentWideCase(
            task = "etiqueta con #adeudo a todos los socios a los que les debemos un pago",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("adeudo", "Aurelio", "Teodora", "Rosa Méndez"),
            lacks = listOf("Bartolo García")
        ),
        AgentWideCase(
            task = "haz una nota Adeudos a socios con lo que le debemos a cada uno, ya con " +
                "los anticipos descontados",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Adeudos a socios", "7,260", "9,460", "4,320"),
            computed = listOf("11175")
        ),
        AgentWideCase(
            task = "crea una carpeta Contratos y mete ahí los dos contratos",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "create_folder", "move", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Contratos", "Contrato de agave 2026-2027", "Contrato de maíz")
        ),
        AgentWideCase(
            task = "renombra la nota del 2026-11-06 a 2026-11-06 visita a Teodora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("visita a Teodora")
        ),
        AgentWideCase(
            task = "cambia la cita con la tortillería a las 17:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("17:00", "Citas con compradores")
        ),
        AgentWideCase(
            task = "anota la carga de diésel de hoy: 40 litros, 990 pesos, km 185120, Aurelio",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bitácora de combustible", "990", "185120")
        ),
        AgentWideCase(
            task = "ponle la etiqueta #jima a doña Rosa Hernández",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rosa Hernández", "jima"),
            lacks = listOf("Rosa Méndez.md")
        ),
        AgentWideCase(
            task = "en el padrón pon que Bartolo ya completó su aportación",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Padrón de socios", "Bartolo", "completa")
        ),
        AgentWideCase(
            task = "haz la nota de la asamblea del 6 de diciembre con el orden del día que " +
                "quedó en el acta de octubre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("suplente", "2027")
        )
    )
}

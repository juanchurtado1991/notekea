package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCooperativaChanges {
    val all = listOf(
        change(
            task = "agrega a mis pendientes de la semana: comprar costales para el maíz",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Pendientes de la semana", "costales")
        ),
        change(
            task = "marca como pagado lo de Teodora en los pagos pendientes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Pendientes", "Teodora")
        ),
        change(
            task = "ponle status firmado al contrato de maíz",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Contrato de maíz", "firmado")
        ),
        change(
            task = "etiqueta con #adeudo a todos los socios a los que les debemos un pago",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf("adeudo", "Aurelio", "Teodora", "Rosa Méndez"),
            lacks = listOf("Bartolo García")
        ),
        change(
            task = "haz una nota Adeudos a socios con lo que le debemos a cada uno, ya con " +
                "los anticipos descontados",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Adeudos a socios", "7,260", "9,460", "4,320"),
            computed = listOf("11175")
        ),
        change(
            task = "crea una carpeta Contratos y mete ahí los dos contratos",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "create_folder", "move", "move", "propose"),
            holds = listOf("Contratos", "Contrato de agave 2026-2027", "Contrato de maíz")
        ),
        change(
            task = "renombra la nota del 2026-11-06 a 2026-11-06 visita a Teodora",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("visita a Teodora")
        ),
        change(
            task = "cambia la cita con la tortillería a las 17:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("17:00", "Citas con compradores")
        ),
        change(
            task = "anota la carga de diésel de hoy: 40 litros, 990 pesos, km 185120, Aurelio",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Bitácora de combustible", "990", "185120")
        ),
        change(
            task = "ponle la etiqueta #jima a doña Rosa Hernández",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Rosa Hernández", "jima"),
            lacks = listOf("Rosa Méndez.md")
        ),
        change(
            task = "en el padrón pon que Bartolo ya completó su aportación",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Padrón de socios", "Bartolo", "completa")
        ),
        change(
            task = "haz la nota de la asamblea del 6 de diciembre con el orden del día que " +
                "quedó en el acta de octubre",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("suplente", "2027")
        )
    )
}

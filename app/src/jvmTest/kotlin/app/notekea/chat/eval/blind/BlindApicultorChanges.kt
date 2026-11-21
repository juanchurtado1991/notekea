package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes proposed in the Blind Apicultor vault. */
internal object BlindApicultorChanges {
    val all = listOf(
        AgentWideCase(
            task = "añade en Reinas una tarea: marcar en azul la reina nueva de la L8",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reinas", "L8")
        ),
        AgentWideCase(
            task = "marca el pedido de Marta Vílchez como enviado hoy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marta Vílchez", "enviado")
        ),
        AgentWideCase(
            task = "en Apiario Órgiva pon la propiedad colmenas a 7, que he hecho un enjambre",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Apiario Órgiva", "7")
        ),
        AgentWideCase(
            task = "pon la etiqueta #moroso a los clientes que tengan alguna factura sin pagar",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "read",
                "read",
                "read",
                "add_tag",
                "add_tag",
                "propose"
            ),
            ends = End.PROPOSE,
            holds = listOf("moroso", "La Alacena de Lola", "Herboristería Romero"),
            lacks = listOf("Hotel Cerro Azul", "Restaurante El Aljibe")
        ),
        AgentWideCase(
            task = "añade a Tratamientos varroa una fila: 2026-11-12, Lanjarón, timol, " +
                "2 colmenas, 18 €",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tratamientos varroa", "timol")
        ),
        AgentWideCase(
            task = "hazme una nota Resumen cosecha con los kg totales de 2025 y de 2026",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Resumen cosecha"),
            computed = listOf("367", "468")
        ),
        AgentWideCase(
            task = "mueve la Reunión 2026-10-20 a una carpeta Actas dentro de Cooperativa",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reunión 2026-10-20", "Actas")
        ),
        AgentWideCase(
            task = "renombra Citas OCA a Citas oficina comarcal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Citas oficina comarcal")
        ),
        AgentWideCase(
            task = "el mercado ecológico de Granada sube la cuota a 30 €, cámbialo en la nota",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mercado ecológico de Granada", "30")
        ),
        AgentWideCase(
            task = "en Inspecciones noviembre añade una tarea de tratar cada colmena con más de " +
                "10 ácaros en el conteo",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("L3", "L6", "O4"),
            lacks = listOf("O5")
        ),
        AgentWideCase(
            task = "crea una nota Pedido Envases Alpujarra: 400 tarros de 250 g con tapa",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Envases Alpujarra", "400")
        ),
        AgentWideCase(
            task = "en Apiario Lanjarón, en la fila de la L3, pon que la reina es de " +
                "[[Paco Jiménez]]",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Apiario Lanjarón", "Paco Jiménez"),
            lacks = listOf("Olmedo")
        )
    )
}

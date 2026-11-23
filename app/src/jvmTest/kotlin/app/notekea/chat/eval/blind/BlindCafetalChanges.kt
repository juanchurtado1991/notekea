package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindCafetalChanges {
    val all = listOf(
        change(
            task = "agregá a El Higuerón la tarea de chapear la calle de abajo",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("El Higuerón", "chapear")
        ),
        change(
            task = "poné el status de Alto Palmira como en cosecha",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Alto Palmira", "en cosecha")
        ),
        change(
            task = "ponele la etiqueta #cobrar a cada comprador que tenga una factura pendiente",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Hotel Las Nubes", "Mauricio Rodríguez", "cobrar"),
            lacks = listOf("Esteban")
        ),
        change(
            task = "agregale a la nota de José Vindas que hoy pidió un adelanto de ₡15.000",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("José Vindas", "15.000")
        ),
        change(
            task = "haceme una nota que se llame Pendientes de cobro con las facturas que me deben",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Pendientes de cobro", "F-0124", "F-0127", "F-0131"),
            lacks = listOf("960")
        ),
        change(
            task = "mové Renovación de lotes a la carpeta Cosecha",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Renovación de lotes", "Cosecha")
        ),
        change(
            task = "cambiale el nombre a Catación noviembre por Catación 2026-11-05",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Catación 2026-11-05")
        ),
        change(
            task = "en la planilla de la semana 45 poné a Kendall Ortiz como pagado",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Kendall Ortiz", "semana 45")
        ),
        change(
            task = "ponele #revisar-broca a los lotes que están arriba de 1500 metros",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("La Quebrada", "Alto Palmira", "revisar-broca"),
            lacks = listOf("El Higuerón")
        ),
        change(
            task = "en los pedidos del agroservicio marcá el de los canastos como recibido hoy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("canastos", "recibido")
        ),
        change(
            task = "en la nota del hotel cambiá el contacto, ahora es Marco Calderón",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Hotel Las Nubes", "Marco Calderón")
        ),
        change(
            task = "haceme una nota nueva Cosecha a la fecha con los kg de fruta de cada lote " +
                "hasta hoy, sin contar lo rechazado en el recibidor",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Cosecha a la fecha"),
            computed = listOf("830", "490")
        )
    )
}

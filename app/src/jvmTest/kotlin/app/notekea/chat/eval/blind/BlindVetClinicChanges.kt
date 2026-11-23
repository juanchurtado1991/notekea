package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Vet Clinic vault, each one clear from the words and the notes. */
internal object BlindVetClinicChanges {
    val all = listOf(
        change(
            task = "add a task to vaccine stock to reorder canine influenza",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Vaccine Stock", "canine influenza")
        ),
        change(
            task = "set Rook's weight to 5.2 kg",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Rook", "5.2")
        ),
        change(
            task = "tag Clover's note #exotic",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            holds = listOf("Clover", "exotic")
        ),
        change(
            task = "add to my nov 9 journal that the autoclave passed its spore test",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            holds = listOf("2026-11-09", "spore test")
        ),
        change(
            task = "make a note called Unpaid Invoices listing who owes me what",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            holds = listOf(
                "Unpaid Invoices", "Dana Pruitt", "Dana Whitcomb", "Ellis Thibault", "Marta Ashby",
                "415", "128", "74", "187"
            ),
            lacks = listOf("Victor Delacroix", "Ruth Hollins")
        ),
        change(
            task = "move my reading list into the Conference folder",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            holds = listOf("Reading List", "Conference")
        ),
        change(
            task = "rename Payroll October to Payroll 2026-10",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            holds = listOf("Payroll 2026-10")
        ),
        change(
            task = "move Biscuit's dental surgery to 2026-11-24",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Surgeries", "Biscuit", "2026-11-24")
        ),
        change(
            task = "tag every owner with an unpaid invoice #overdue",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf(
                "Dana Pruitt",
                "Dana Whitcomb",
                "Ellis Thibault",
                "Marta Ashby",
                "overdue"
            ),
            lacks = listOf("Ruth Hollins", "Victor Delacroix")
        ),
        change(
            task = "tag every patient whose rabies is due before the end of the year #rabies-due",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf("Max Hollins", "Biscuit", "Rook", "rabies-due"),
            lacks = listOf("Max Thibault", "Pepper")
        )
    )
}

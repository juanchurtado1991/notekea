package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Vet Clinic vault, each one clear from the words and the notes. */
internal object BlindVetClinicChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to vaccine stock to reorder canine influenza",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Vaccine Stock", "canine influenza")
        ),
        AgentWideCase(
            task = "set Rook's weight to 5.2 kg",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rook", "5.2")
        ),
        AgentWideCase(
            task = "tag Clover's note #exotic",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Clover", "exotic")
        ),
        AgentWideCase(
            task = "add to my nov 9 journal that the autoclave passed its spore test",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-09", "spore test")
        ),
        AgentWideCase(
            task = "make a note called Unpaid Invoices listing who owes me what",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Unpaid Invoices", "Dana Pruitt", "Dana Whitcomb", "Ellis Thibault", "Marta Ashby",
                "415", "128", "74", "187"
            ),
            lacks = listOf("Victor Delacroix", "Ruth Hollins")
        ),
        AgentWideCase(
            task = "move my reading list into the Conference folder",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reading List", "Conference")
        ),
        AgentWideCase(
            task = "rename Payroll October to Payroll 2026-10",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Payroll 2026-10")
        ),
        AgentWideCase(
            task = "move Biscuit's dental surgery to 2026-11-24",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Surgeries", "Biscuit", "2026-11-24")
        ),
        AgentWideCase(
            task = "tag every owner with an unpaid invoice #overdue",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Dana Pruitt",
                "Dana Whitcomb",
                "Ellis Thibault",
                "Marta Ashby",
                "overdue"
            ),
            lacks = listOf("Ruth Hollins", "Victor Delacroix")
        ),
        AgentWideCase(
            task = "tag every patient whose rabies is due before the end of the year #rabies-due",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Max Hollins", "Biscuit", "Rook", "rabies-due"),
            lacks = listOf("Max Thibault", "Pepper")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Translator vault that the words and the notes settle. */
internal object BlindTranslatorChanges {
    val all = listOf(
        change(
            task = "add a task to the leaflet job: ask Tom about the dosage table",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("leaflet", "dosage table")
        ),
        change(
            task = "set the status of the wine labels job to delivered",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("wine labels", "delivered")
        ),
        change(
            task = "tag the Northlight NDA note #legal",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("NDA Northlight", "legal")
        ),
        change(
            task = "add to yesterday's journal: called Rita about the label sizes",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            holds = listOf("2026-11-10", "label sizes")
        ),
        change(
            task = "write a note called Overdue invoices listing each overdue one " +
                "with client and amount",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf(
                "Overdue invoices",
                "INV-2026-036",
                "INV-2026-039",
                "INV-2026-040",
                "935",
                "1320",
                "405"
            ),
            lacks = listOf("INV-2026-041", "INV-2026-042", "INV-2026-043")
        ),
        change(
            task = "move the 2026-10-28 journal note into a new Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("2026-10-28", "Archive")
        ),
        change(
            task = "rename the Software UI EN-PT glossary to UI strings EN-PT",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Software UI EN-PT", "UI strings EN-PT")
        ),
        change(
            task = "change the due date of INV-2026-043 to 2026-12-20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("INV-2026-043", "2026-12-20")
        ),
        change(
            task = "tag every client with an overdue invoice #chase",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            holds = listOf(
                "Verba Global",
                "Northlight Localization",
                "Verba Translations",
                "chase"
            ),
            lacks = listOf("Okafor Studio", "Quinta do Vale Azul")
        ),
        change(
            task = "set priority: high on every job due before 2026-11-21",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Patient leaflet", "UI strings", "high"),
            lacks = listOf("wine labels", "game dialogue", "museum brochure")
        ),
        change(
            task = "link the leaflet job to the pharma glossary",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "search", "link", "propose"),
            holds = listOf("Pharma EN-PT", "leaflet")
        ),
        change(
            task = "make a Rates summary note with every client's per-word rate",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "read",
                "read",
                "read",
                "read",
                "write_note",
                "propose"
            ),
            holds = listOf("Rates summary", "0.09", "0.11", "0.10", "0.14", "0.12")
        )
    )
}

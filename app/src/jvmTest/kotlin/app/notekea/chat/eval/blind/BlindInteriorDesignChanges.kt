package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes proposed to the Blind Interior Design vault. */
internal object BlindInteriorDesignChanges {
    val all = listOf(
        change(
            task = "add a task to the clapham schedule: book the signage fitter",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Clapham Cafe", "signage fitter")
        ),
        change(
            task = "hannah lowe signed today, set her status to client",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Hannah Lowe", "status", "client")
        ),
        change(
            task = "tag every client with an unpaid invoice #owes",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "propose"),
            holds = listOf("Ravi Patel", "Meera Patel", "Hannah Lowe", "owes"),
            lacks = listOf("Tom Ashby")
        ),
        change(
            task = "start today's journal note with: Linden confirmed the Moss velvet reorder",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "write_note", "propose"),
            holds = listOf("journal", "Moss velvet reorder")
        ),
        change(
            task = "make a note Opening checklist in the clapham cafe folder " +
                "with the clapham orders still pending",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Opening checklist", "Dome", "Teal", "Perch"),
            lacks = listOf("Bistro")
        ),
        change(
            task = "the hampstead linen came today, mark it delivered",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Oat", "delivered")
        ),
        change(
            task = "move Mood board - Hampstead into the hampstead townhouse project folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Mood board - Hampstead", "Hampstead Townhouse")
        ),
        change(
            task = "rename Colour palettes to Colour palettes 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Colour palettes 2026")
        ),
        change(
            task = "give oakbarrow a rating of 3",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Oakbarrow", "rating")
        ),
        change(
            task = "push sofia's islington slot to 2026-11-25",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Sofia", "2026-11-25")
        ),
        change(
            task = "write me a note Studio/Owed listing every unpaid invoice " +
                "with client and amount",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("INV-2026-044", "INV-2026-047", "INV-2026-050"),
            lacks = listOf("INV-2026-049")
        ),
        change(
            task = "update the hampstead schedule, the alcove shelves are smoked " +
                "sage paint now not oiled",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Smoked Sage", "alcove")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastChanges {
    val all = listOf(
        change(
            task = "set ep 144's status to edited",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("144", "edited")
        ),
        change(
            task = "tag every sponsor that still has an unpaid ad read with #overdue",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Northfield Coffee", "Quillstack", "overdue")
        ),
        change(
            task = "add a task to Lucia's guest note: send her the studio address",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Lucia Ferraro", "studio address")
        ),
        change(
            task = "make a note for ep 146 from Dev's guest note and the pipeline",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Dev Raman", "Night-shift nurses")
        ),
        change(
            task = "rename Guest Wishlist to Guest Prospects",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Guest Prospects")
        ),
        change(
            task = "move the show notes template into a new Templates folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Show Notes Template", "Templates")
        ),
        change(
            task = "change Marguerite's recording to 17:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Marguerite Bell", "17:00")
        ),
        change(
            task = "add to yesterday's journal: Northfield wants a live read at the Dundas café " +
                "in December",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            holds = listOf("2026-11-10", "live read")
        ),
        change(
            task = "mic 2 is back from the shop today, it cost $85. note it in repairs",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Repairs", "85")
        ),
        change(
            task = "put a task in Quillstack's note to chase their unpaid invoice, with the " +
                "invoice number",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Quillstack", "INV-2026-031")
        ),
        change(
            task = "update the promo code in Harbourline's note to the new one Rob gave me",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "set_property", "edit_note", "propose"),
            holds = listOf("Harbourline", "LONGTABLE15")
        ),
        change(
            task = "write me a note with what each sponsor still owes me from the ad reads log",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "calculate", "write_note", "propose"),
            holds = listOf("Northfield", "Quillstack", "735"),
            computed = listOf("850")
        )
    )
}

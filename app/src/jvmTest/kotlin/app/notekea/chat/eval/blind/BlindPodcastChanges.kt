package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastChanges {
    val all = listOf(
        AgentWideCase(
            task = "set ep 144's status to edited",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("144", "edited")
        ),
        AgentWideCase(
            task = "tag every sponsor that still has an unpaid ad read with #overdue",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Northfield Coffee", "Quillstack", "overdue")
        ),
        AgentWideCase(
            task = "add a task to Lucia's guest note: send her the studio address",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucia Ferraro", "studio address")
        ),
        AgentWideCase(
            task = "make a note for ep 146 from Dev's guest note and the pipeline",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Dev Raman", "Night-shift nurses")
        ),
        AgentWideCase(
            task = "rename Guest Wishlist to Guest Prospects",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Guest Prospects")
        ),
        AgentWideCase(
            task = "move the show notes template into a new Templates folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Show Notes Template", "Templates")
        ),
        AgentWideCase(
            task = "change Marguerite's recording to 17:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marguerite Bell", "17:00")
        ),
        AgentWideCase(
            task = "add to yesterday's journal: Northfield wants a live read at the Dundas café " +
                "in December",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "live read")
        ),
        AgentWideCase(
            task = "mic 2 is back from the shop today, it cost $85. note it in repairs",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Repairs", "85")
        ),
        AgentWideCase(
            task = "put a task in Quillstack's note to chase their unpaid invoice, with the " +
                "invoice number",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Quillstack", "INV-2026-031")
        ),
        AgentWideCase(
            task = "update the promo code in Harbourline's note to the new one Rob gave me",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "set_property", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Harbourline", "LONGTABLE15")
        ),
        AgentWideCase(
            task = "write me a note with what each sponsor still owes me from the ad reads log",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Northfield", "Quillstack", "735"),
            computed = listOf("850")
        )
    )
}

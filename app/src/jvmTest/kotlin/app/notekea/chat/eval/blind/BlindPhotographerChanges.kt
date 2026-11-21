package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPhotographerChanges {
    val all = listOf(
        AgentWideCase(
            task = "tag every client with an unpaid invoice #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hollis Family", "Jordan Ellis", "Teresa Vance", "unpaid"),
            lacks = listOf("Jordan Pike", "Priya Raman")
        ),
        AgentWideCase(
            task = "add a task to marcus's note to get his W-9",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marcus Bell", "W-9")
        ),
        AgentWideCase(
            task = "priya gave me 5 stars, set the rating on her client note to 5",
            kind = Kind.DOCUMENT,
            tools = listOf("set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya Raman", "rating")
        ),
        AgentWideCase(
            task = "add to jordan pike's note that the gala dress code is black tie",
            kind = Kind.DOCUMENT,
            tools = listOf("append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordan Pike", "black tie")
        ),
        AgentWideCase(
            task = "make a note Ellis Wedding Brief with the date, venue, where to park " +
                "and who's second shooting",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ellis Wedding Brief", "Magnolia Hall", "lot C", "Marcus Bell")
        ),
        AgentWideCase(
            task = "move calder reyes into an Archive folder, he cancelled",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Calder Reyes", "Archive")
        ),
        AgentWideCase(
            task = "rename my wishlist to Gear Wishlist 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gear Wishlist 2027")
        ),
        AgentWideCase(
            task = "brightline headshots now start at 08:30, fix the schedule",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brightline", "08:30")
        ),
        AgentWideCase(
            task = "add color checker card to my packing list",
            kind = Kind.DOCUMENT,
            tools = listOf("add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Packing List", "color checker")
        ),
        AgentWideCase(
            task = "teresa just paid her deposit, mark it paid in my invoices",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Teresa", "paid")
        ),
        AgentWideCase(
            task = "add a 'confirm final headcount' task to every client whose booking " +
                "in Bookings 2026 is still to come this month and not cancelled",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brightline Dental", "Jordan Pike", "confirm final headcount"),
            lacks = listOf("Calder Reyes", "Jordan Ellis")
        ),
        AgentWideCase(
            task = "finished 120 more hollis edits, update the backlog",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hollis"),
            computed = listOf("300")
        )
    )
}

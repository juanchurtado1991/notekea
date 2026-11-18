package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the gym owner's notes that the words and the notes make clear. */
internal object BlindGymOwnerChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to my wishlist to get quotes for a third ski erg",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Wishlist", "ski erg")
        ),
        AgentWideCase(
            task = "marcus is back early, set his status to active",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marcus Bell", "active")
        ),
        AgentWideCase(
            task = "tag leah's note #competitor",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Leah Whitford", "competitor")
        ),
        AgentWideCase(
            task = "add to yesterday's journal: ordered the flywheel part for rower #3",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "flywheel")
        ),
        AgentWideCase(
            task = "make a note called October P&L with revenue after refunds, operating " +
                "expenses and profit for october",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("October P&L"),
            computed = listOf("14019", "12506", "1513")
        ),
        AgentWideCase(
            task = "move the volunteers note into the admin folder",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Volunteers", "Admin")
        ),
        AgentWideCase(
            task = "rename trial signups to Leads",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Trial Signups", "Leads")
        ),
        AgentWideCase(
            task = "change hannah cole's plan to unlimited in the roster",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Member Roster", "Hannah Cole", "Unlimited")
        ),
        AgentWideCase(
            task = "tag every coach whose cpr card runs out this year with #renew-cpr",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ryan Delgado", "Jess Amari", "renew-cpr")
        ),
        AgentWideCase(
            task = "add a task in sponsors to chase each sponsor that hasn't paid yet",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rocky Ridge Coffee", "Mesa Brewing")
        ),
        AgentWideCase(
            task = "set the throwdown note's status to confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mile High Throwdown 2026", "confirmed")
        ),
        AgentWideCase(
            task = "link tomás's note to the liability waiver",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tomás Reyes", "Liability Waiver")
        )
    )
}

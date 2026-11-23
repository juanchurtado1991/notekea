package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the gym owner's notes that the words and the notes make clear. */
internal object BlindGymOwnerChanges {
    val all = listOf(
        change(
            task = "add a task to my wishlist to get quotes for a third ski erg",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Wishlist", "ski erg")
        ),
        change(
            task = "marcus is back early, set his status to active",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Marcus Bell", "active")
        ),
        change(
            task = "tag leah's note #competitor",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("Leah Whitford", "competitor")
        ),
        change(
            task = "add to yesterday's journal: ordered the flywheel part for rower #3",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            holds = listOf("2026-11-10", "flywheel")
        ),
        change(
            task = "make a note called October P&L with revenue after refunds, operating " +
                "expenses and profit for october",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            holds = listOf("October P&L"),
            computed = listOf("14019", "12506", "1513")
        ),
        change(
            task = "move the volunteers note into the admin folder",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "move", "propose"),
            holds = listOf("Volunteers", "Admin")
        ),
        change(
            task = "rename trial signups to Leads",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "rename", "propose"),
            holds = listOf("Trial Signups", "Leads")
        ),
        change(
            task = "change hannah cole's plan to unlimited in the roster",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Member Roster", "Hannah Cole", "Unlimited")
        ),
        change(
            task = "tag every coach whose cpr card runs out this year with #renew-cpr",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            holds = listOf("Ryan Delgado", "Jess Amari", "renew-cpr")
        ),
        change(
            task = "add a task in sponsors to chase each sponsor that hasn't paid yet",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Rocky Ridge Coffee", "Mesa Brewing")
        ),
        change(
            task = "set the throwdown note's status to confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Mile High Throwdown 2026", "confirmed")
        ),
        change(
            task = "link tomás's note to the liability waiver",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "link", "propose"),
            holds = listOf("Tomás Reyes", "Liability Waiver")
        )
    )
}

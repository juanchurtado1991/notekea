package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHealthLogChanges {
    val all = listOf(
        change(
            task = "tag everyone I still owe money with #owed",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Sarah Whitlock", "Priya Raman", "Mike Osei", "owed")
        ),
        change(
            task = "add a task to my supplements note to restock anything with fewer than " +
                "10 servings left",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Supplements", "Iron", "Collagen"),
            lacks = listOf("Fish oil")
        ),
        change(
            task = "write a new note Upcoming Appointments with all my upcoming doctor, " +
                "physio and dietitian appointments",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Upcoming Appointments",
                "2026-11-13",
                "2026-11-19",
                "2026-11-24",
                "2026-12-03"
            )
        ),
        change(
            task = "log today's run in my november log: 4.0 mi in 41:20, pace 10:20, knee pain 1",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("November 2026", "41:20")
        ),
        change(
            task = "set the status of my front range marathon note to training",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Front Range Marathon", "training")
        ),
        change(
            task = "rename my hydration note to Hydration Plan",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Hydration Plan")
        ),
        change(
            task = "move the strength routine note into the Health folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Strength Routine", "Health")
        ),
        change(
            task = "tag my sleep log #recovery",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Sleep Log", "recovery")
        ),
        change(
            task = "add a task to my front range marathon note: book a hotel near the start",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Front Range Marathon", "hotel")
        ),
        change(
            task = "add 'ice after long runs' to my home exercises as a task",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Home Exercises", "ice after long runs")
        ),
        change(
            task = "link my lab results from the supplements note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Supplements", "Lab Results")
        ),
        change(
            task = "the winter tights came today, mark that order delivered",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("4480", "delivered")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the food bank vault that the words and the notes make clear. */
internal object BlindFoodBankChanges {
    val all = listOf(
        change(
            task = "add a todo to my Nov 10 journal: order 2 cases of hairnets",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("2026-11-10", "hairnets")
        ),
        change(
            task = "Luis moved to Pittsburgh, set his status to inactive",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Luis Ortega", "inactive")
        ),
        change(
            task = "tag Riverside Grocers as #top-donor",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Riverside Grocers", "top-donor")
        ),
        change(
            task = "log this morning's freezer reading: 07:50, freezer -3, cooler 37, by Nadia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Freezer log", "-3", "07:50")
        ),
        change(
            task = "make a note November donors so far, each donor and their pounds this month",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "write_note", "propose"),
            holds = listOf("November donors so far", "Harlan", "730"),
            computed = listOf("1390", "1360", "403")
        ),
        change(
            task = "move the Orientation note into Operations",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Orientation", "Operations")
        ),
        change(
            task = "rename Mobile pantry to Mobile pantry routes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Mobile pantry routes")
        ),
        change(
            task = "Hunting Park stop is moving to 11:00–13:00, update the mobile pantry",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Hunting Park", "11:00")
        ),
        change(
            task = "tag volunteers whose food handler cert runs out this year with #cert-renewal",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Grace Kim", "Marcus Bell", "cert-renewal")
        ),
        change(
            task = "tag every donor that hasn't given anything in November with #lapsed",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "propose"),
            holds = listOf("Ostrowski Deli", "lapsed")
        ),
        change(
            task = "I just recounted the warehouse, set the inventory's last count to 2026-11-11",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Inventory", "2026-11-11")
        ),
        change(
            task = "link the grant report draft to Distribution days October",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Grant report draft", "Distribution days October")
        )
    )
}

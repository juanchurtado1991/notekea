package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the food bank vault that the words and the notes make clear. */
internal object BlindFoodBankChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a todo to my Nov 10 journal: order 2 cases of hairnets",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "hairnets")
        ),
        AgentWideCase(
            task = "Luis moved to Pittsburgh, set his status to inactive",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Luis Ortega", "inactive")
        ),
        AgentWideCase(
            task = "tag Riverside Grocers as #top-donor",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Riverside Grocers", "top-donor")
        ),
        AgentWideCase(
            task = "log this morning's freezer reading: 07:50, freezer -3, cooler 37, by Nadia",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Freezer log", "-3", "07:50")
        ),
        AgentWideCase(
            task = "make a note November donors so far, each donor and their pounds this month",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("November donors so far", "Harlan", "730"),
            computed = listOf("1390", "1360", "403")
        ),
        AgentWideCase(
            task = "move the Orientation note into Operations",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Orientation", "Operations")
        ),
        AgentWideCase(
            task = "rename Mobile pantry to Mobile pantry routes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mobile pantry routes")
        ),
        AgentWideCase(
            task = "Hunting Park stop is moving to 11:00–13:00, update the mobile pantry",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hunting Park", "11:00")
        ),
        AgentWideCase(
            task = "tag volunteers whose food handler cert runs out this year with #cert-renewal",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Grace Kim", "Marcus Bell", "cert-renewal")
        ),
        AgentWideCase(
            task = "tag every donor that hasn't given anything in November with #lapsed",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ostrowski Deli", "lapsed")
        ),
        AgentWideCase(
            task = "I just recounted the warehouse, set the inventory's last count to 2026-11-11",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Inventory", "2026-11-11")
        ),
        AgentWideCase(
            task = "link the grant report draft to Distribution days October",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Grant report draft", "Distribution days October")
        )
    )
}

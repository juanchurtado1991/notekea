package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHobbiesChanges {
    val all = listOf(
        AgentWideCase(
            task = "add rain jacket to my packing list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Packing list", "rain jacket")
        ),
        AgentWideCase(
            task = "set the status of the scotland itinerary to booked",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Itinerary", "booked")
        ),
        AgentWideCase(
            task = "tag the salt orchard note with #favourite",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("The Salt Orchard", "favourite")
        ),
        AgentWideCase(
            task = "add eki (station) to my travel vocab",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Vocabulary - Travel", "eki")
        ),
        AgentWideCase(
            task = "finished a house of tides today, 4 stars. put it in my reading log",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reading log", "A House of Tides", "Clara Moniz", "356")
        ),
        AgentWideCase(
            task = "write me a new note with what my two past trips cost in total, each one",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kyoto", "Azores"),
            computed = listOf("3834", "1232")
        ),
        AgentWideCase(
            task = "move the packing list into the scotland 2027 folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Packing list", "Scotland 2027")
        ),
        AgentWideCase(
            task = "the onion sets arrived, mark that order as delivered",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("GS-2305", "delivered")
        ),
        AgentWideCase(
            task = "tag every friend who is in the book club with #bookclub",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rita Almeida", "Inês Carvalho", "Tomás Pereira", "bookclub"),
            lacks = listOf("Rita Fonseca")
        ),
        AgentWideCase(
            task = "paid kenji today, mark the unpaid lessons in my lesson notes as paid",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lesson notes", "2026-10-28", "2026-11-04")
        ),
        AgentWideCase(
            task = "rename my photo walks note to Lisbon photo walks",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lisbon photo walks")
        ),
        AgentWideCase(
            task = "picked the first winter lettuce today, 0.8 kg. log it in the garden log",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Garden log 2026", "0.8")
        )
    )
}

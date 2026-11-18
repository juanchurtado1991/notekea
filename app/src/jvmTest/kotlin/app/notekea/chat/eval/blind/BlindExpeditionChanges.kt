package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindExpeditionChanges {
    val all = listOf(
        AgentWideCase(
            task = "add 'buy spare headlamp batteries' to my packing checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Packing Checklist", "headlamp batteries")
        ),
        AgentWideCase(
            task = "set the insurance policy status to active",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Insurance Policy", "active")
        ),
        AgentWideCase(
            task = "tag everyone on the team who hasn't sent their medical form " +
                "with #missing-form",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alex Kowalski", "Priya Raman", "missing-form"),
            lacks = listOf("Alex Moreno")
        ),
        AgentWideCase(
            task = "log today's hike in my training log: Green Mountain, 2450 ft gain, " +
                "5.1 mi, 2h50",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Training Log", "Green Mountain", "2450")
        ),
        AgentWideCase(
            task = "write a note called Outstanding Payments with everything I still have to pay " +
                "from the expedition budget and the permits",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Outstanding Payments", "Outfitter balance", "Kathmandu hotel", "TIMS")
        ),
        AgentWideCase(
            task = "move Vaccinations into the Expedition folder",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Vaccinations", "Expedition")
        ),
        AgentWideCase(
            task = "rename the Elbert note to Mount Elbert 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mount Elbert 2026")
        ),
        AgentWideCase(
            task = "tag the itinerary with #acclimatization",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Itinerary", "acclimatization")
        ),
        AgentWideCase(
            task = "give the Longs Peak note a rating of 5",
            kind = Kind.DOCUMENT,
            tools = listOf("set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Longs Peak", "rating")
        ),
        AgentWideCase(
            task = "link the gear list from Alex Moreno's note, he's lending me the ice axe",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alex Moreno", "Gear List")
        ),
        AgentWideCase(
            task = "paid the TIMS card today, mark it paid",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Permits and Fees", "TIMS")
        ),
        AgentWideCase(
            task = "for each gear item I still need to buy, add a task to buy it in my " +
                "packing checklist",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sleeping pad", "Glacier glasses", "Summit mittens"),
            lacks = listOf("Gaiters")
        )
    )
}

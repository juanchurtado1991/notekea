package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindExpeditionChanges {
    val all = listOf(
        change(
            task = "add 'buy spare headlamp batteries' to my packing checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Packing Checklist", "headlamp batteries")
        ),
        change(
            task = "set the insurance policy status to active",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Insurance Policy", "active")
        ),
        change(
            task = "tag everyone on the team who hasn't sent their medical form " +
                "with #missing-form",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            holds = listOf("Alex Kowalski", "Priya Raman", "missing-form"),
            lacks = listOf("Alex Moreno")
        ),
        change(
            task = "log today's hike in my training log: Green Mountain, 2450 ft gain, " +
                "5.1 mi, 2h50",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Training Log", "Green Mountain", "2450")
        ),
        change(
            task = "write a note called Outstanding Payments with everything I still have to pay " +
                "from the expedition budget and the permits",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            holds = listOf("Outstanding Payments", "Outfitter balance", "Kathmandu hotel", "TIMS")
        ),
        change(
            task = "move Vaccinations into the Expedition folder",
            kind = Kind.DOCUMENT,
            tools = listOf("move", "propose"),
            holds = listOf("Vaccinations", "Expedition")
        ),
        change(
            task = "rename the Elbert note to Mount Elbert 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Mount Elbert 2026")
        ),
        change(
            task = "tag the itinerary with #acclimatization",
            kind = Kind.DOCUMENT,
            tools = listOf("add_tag", "propose"),
            holds = listOf("Itinerary", "acclimatization")
        ),
        change(
            task = "give the Longs Peak note a rating of 5",
            kind = Kind.DOCUMENT,
            tools = listOf("set_property", "propose"),
            holds = listOf("Longs Peak", "rating")
        ),
        change(
            task = "link the gear list from Alex Moreno's note, he's lending me the ice axe",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "link", "propose"),
            holds = listOf("Alex Moreno", "Gear List")
        ),
        change(
            task = "paid the TIMS card today, mark it paid",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Permits and Fees", "TIMS")
        ),
        change(
            task = "for each gear item I still need to buy, add a task to buy it in my " +
                "packing checklist",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_task", "propose"),
            holds = listOf("Sleeping pad", "Glacier glasses", "Summit mittens"),
            lacks = listOf("Gaiters")
        )
    )
}

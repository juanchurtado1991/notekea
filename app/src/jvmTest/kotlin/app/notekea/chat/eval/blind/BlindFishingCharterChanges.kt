package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Fishing Charter vault, each one settled by its words and the notes. */
internal object BlindFishingCharterChanges {
    val all = listOf(
        AgentWideCase(
            task = "add buy 10 bags of ice for the 14th to my to do list",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("To do", "10 bags of ice")
        ),
        AgentWideCase(
            task = "pelican grove paid invoice PG-1108, mark their balance as paid",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pelican Grove Realty", "balance")
        ),
        AgentWideCase(
            task = "tag marisol's note #rebooked",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marisol Vega", "rebooked")
        ),
        AgentWideCase(
            task = "make a new note in Clients called Bill Ostrander trips with all his " +
                "booked trips from now through next may, date and trip type",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bill Ostrander trips", "offshore", "tarpon"),
            lacks = listOf("Bill Hanley")
        ),
        AgentWideCase(
            task = "move the waitlist into Admin",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Waitlist", "Admin")
        ),
        AgentWideCase(
            task = "rename Tarpon season 2027 to Tarpon season May 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tarpon season May 2027")
        ),
        AgentWideCase(
            task = "Bill Ostrander just paid the 400 deposit for the 19th, " +
                "update the november bookings",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("November 2026", "400")
        ),
        AgentWideCase(
            task = "tag every client note whose balance is unpaid with #balance-due",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pelican Grove Realty", "Whitcomb", "balance-due"),
            lacks = listOf("Bill Hanley", "Marisol Vega")
        ),
        AgentWideCase(
            task = "renewed my CPR cert, new expiry is 2028-12-03, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Licenses", "2028-12-03")
        ),
        AgentWideCase(
            task = "kenny's working the 14th, add a row for him in november crew pay " +
                "at his rate, not paid yet",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Crew pay November", "200"),
            lacks = listOf("$180")
        ),
        AgentWideCase(
            task = "give the 11-08 catch log a rating of 4",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-08", "rating")
        ),
        AgentWideCase(
            task = "bookmark the insurance policy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "bookmark", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Insurance policy")
        )
    )
}

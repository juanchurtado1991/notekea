package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAstronomyAsks {
    val all = listOf(
        ask(
            task = "set the Budget note's status to final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("club", "gear")
        ),
        ask(
            task = "tick off everything on the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("checklist")
        ),
        ask(
            task = "add a task to invite Dave to the Winter Solstice star party",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Kowalski", "Lindqvist")
        ),
        ask(
            task = "change Dave's phone to 520-555-0199",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Kowalski", "Lindqvist")
        ),
        ask(
            task = "mark the Night Sky Supply order as cancelled",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Barlow", "collimation cap")
        ),
        ask(
            task = "change the school visit to 7 pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Desert View", "Mesquite")
        ),
        ask(
            task = "move the Ortiz Ranch star party to the following Saturday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Fall Finale", "New Year")
        ),
        ask(
            task = "add 'Jupiter's moons all in a row' to the backyard log",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("2026-10-03", "2026-11-07")
        ),
        ask(
            task = "the eyepiece order slipped, set its expected date to 2026-12-09",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("6 mm", "24 mm")
        ),
        answer(
            task = "what's Dave Lindqvist's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("520-555-0178"),
            lacks = listOf("520-555-0143")
        ),
        answer(
            task = "what's still unticked on my gear checklist?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("Red flashlight", "Hand warmers", "Warm jacket"),
            lacks = listOf("Sign-in sheet", "Projector")
        ),
        change(
            task = "add Dave Kowalski as a volunteer for the Dec 10 library event",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Outreach", "Kowalski")
        ),
        change(
            task = "change both school visits to start at 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Desert View", "Mesquite", "19:00")
        ),
        change(
            task = "set status final on the club budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("club", "final"),
            lacks = listOf("Gear/Budget")
        )
    )
}

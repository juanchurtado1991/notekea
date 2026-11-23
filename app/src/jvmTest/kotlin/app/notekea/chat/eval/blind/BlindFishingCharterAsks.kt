package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, then traps that only look ambiguous (Blind Fishing Charter). */
internal object BlindFishingCharterAsks {
    val all = listOf(
        ask(
            task = "log the bilge pump replacement in the maintenance note, 85 bucks",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "move bill's trip to the 21st",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hanley", "Ostrander")
        ),
        ask(
            task = "tag bill's note #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hanley", "Ostrander")
        ),
        ask(
            task = "add a fill-up to the fuel log: 40 gallons at $4.80",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "move the inspection to next friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("extinguisher", "life raft")
        ),
        ask(
            task = "push the big boat's appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("engine")
        ),
        ask(
            task = "change the deposit on the reef trip to $250",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "ask_user"),
            holds = listOf("Hanley", "Marisol")
        ),
        ask(
            task = "switch the whitcomb trip to the skiff",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "ask_user"),
            holds = listOf("offshore", "reef")
        ),
        change(
            task = "add to the skiff's maintenance note: replaced the bilge pump today, $85",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            holds = listOf("Skiff", "bilge pump", "85")
        ),
        change(
            task = "tag Bill Ostrander's note #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("Bill Ostrander", "vip")
        ),
        change(
            task = "push the salt ledger engine service back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Maintenance", "Engine service"),
            computed = listOf("2026-11-23")
        ),
        answer(
            task = "when are both bills' next trips?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Hanley", "Ostrander", "14", "19")
        ),
        change(
            task = "move the life raft repack to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Inspections", "2026-12-01")
        )
    )
}

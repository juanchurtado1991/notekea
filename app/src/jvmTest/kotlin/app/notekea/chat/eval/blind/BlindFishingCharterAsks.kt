package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, then traps that only look ambiguous (Blind Fishing Charter). */
internal object BlindFishingCharterAsks {
    val all = listOf(
        AgentWideCase(
            task = "log the bilge pump replacement in the maintenance note, 85 bucks",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "move bill's trip to the 21st",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hanley", "Ostrander")
        ),
        AgentWideCase(
            task = "tag bill's note #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hanley", "Ostrander")
        ),
        AgentWideCase(
            task = "add a fill-up to the fuel log: 40 gallons at $4.80",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "move the inspection to next friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("extinguisher", "life raft")
        ),
        AgentWideCase(
            task = "push the big boat's appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("engine")
        ),
        AgentWideCase(
            task = "change the deposit on the reef trip to $250",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hanley", "Marisol")
        ),
        AgentWideCase(
            task = "switch the whitcomb trip to the skiff",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("offshore", "reef")
        ),
        AgentWideCase(
            task = "add to the skiff's maintenance note: replaced the bilge pump today, $85",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Skiff", "bilge pump", "85")
        ),
        AgentWideCase(
            task = "tag Bill Ostrander's note #vip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bill Ostrander", "vip")
        ),
        AgentWideCase(
            task = "push the salt ledger engine service back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Maintenance", "Engine service"),
            computed = listOf("2026-11-23")
        ),
        AgentWideCase(
            task = "when are both bills' next trips?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Hanley", "Ostrander", "14", "19")
        ),
        AgentWideCase(
            task = "move the life raft repack to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Inspections", "2026-12-01")
        )
    )
}

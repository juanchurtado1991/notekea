package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindExpeditionAsks {
    val all = listOf(
        // Asks: the words name one thing and the vault has several that fit equally.
        AgentWideCase(
            task = "add a line to the budget: misc, 50 dollars",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("expedition")
        ),
        AgentWideCase(
            task = "add a task to Alex's note: send passport copy",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moreno", "Kowalski")
        ),
        AgentWideCase(
            task = "move the appointment to next friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("dentist", "blood")
        ),
        AgentWideCase(
            task = "set Alex's role to rope lead",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moreno", "Kowalski")
        ),
        AgentWideCase(
            task = "mark the Lukla flight as booked",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            // "21" is in KA 211, KA 214 and the return date (Apr 21): any way to tell them apart.
            holds = listOf("21")
        ),
        AgentWideCase(
            task = "rename my budget note to Budget 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("expedition")
        ),
        AgentWideCase(
            task = "add Alex as the emergency contact in my insurance note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moreno", "Kowalski")
        ),
        AgentWideCase(
            task = "add a reminder task for the day before the appointment",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("dentist", "blood")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "add a line to my household budget: expedition fund transfer, 400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Personal", "400"),
            lacks = listOf("Expedition/Budget")
        ),
        AgentWideCase(
            task = "add a task to Alex Kowalski's note: send passport copy",
            kind = Kind.AMBIGUOUS,
            tools = listOf("add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alex Kowalski", "passport")
        ),
        AgentWideCase(
            task = "which Alex is the doctor?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Kowalski")
        ),
        AgentWideCase(
            task = "move the dentist appointment to Dec 3 at 9:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Appointments", "Dentist", "9:00")
        ),
        AgentWideCase(
            task = "what does each of my two budgets add up to?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9297", "3382")
        ),
        AgentWideCase(
            task = "mark both Lukla flights as booked",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("KA 211", "KA 214")
        )
    )
}

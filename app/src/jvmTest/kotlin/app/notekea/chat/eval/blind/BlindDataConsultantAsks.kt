package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Data Consultant vault cannot settle, and look-alikes the words settle. */
internal object BlindDataConsultantAsks {
    val all = listOf(
        AgentWideCase(
            task = "mark the Müller invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Logistik", "Kliniken")
        ),
        AgentWideCase(
            task = "mark the Kliniken invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("MK-2026-04", "MK-2026-05")
        ),
        AgentWideCase(
            task = "add a task to call Jonas back next week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Weber", "Albrecht")
        ),
        AgentWideCase(
            task = "set the budget note to final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("talk")
        ),
        AgentWideCase(
            task = "move my Müller Logistik call to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("review", "demo")
        ),
        AgentWideCase(
            task = "tag the Müller contract with #renewal",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Logistik", "Kliniken")
        ),
        AgentWideCase(
            task = "mark the course as finished in my learning plan",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Bayesian", "Causal")
        ),
        AgentWideCase(
            task = "mark the tax prepayment as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("income tax", "VAT")
        ),
        AgentWideCase(
            task = "add 3 hours for Müller to this week's timesheet",
            kind = Kind.AMBIGUOUS,
            tools = listOf("date", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Logistik", "Kliniken")
        ),
        AgentWideCase(
            task = "mark the Müller Logistik invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("ML-2026-09", "paid"),
            lacks = listOf("MK-2026")
        ),
        AgentWideCase(
            task = "add a task to send Jonas Albrecht the updated DPIA by 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Albrecht", "DPIA")
        ),
        AgentWideCase(
            task = "move the Nordlicht call to 2026-11-20, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Meetings", "2026-11-20", "16:00")
        ),
        AgentWideCase(
            task = "how much is left in my talk budget after everything planned?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("225")
        ),
        AgentWideCase(
            task = "add #renewal to both Müller contracts",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Müller Logistik", "Müller Kliniken", "renewal")
        ),
        AgentWideCase(
            task = "what's Jonas Weber's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("23846610"),
            lacks = listOf("55210874")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back about, and look-alikes whose words settle them. */
internal object BlindResidentDoctorAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task to my schedule: print it and stick it on the fridge",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("rotation")
        ),
        AgentWideCase(
            task = "change the food line in my budget to 450",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "update Dr. Kim's pager to 4-2290",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Daniel", "Grace")
        ),
        AgentWideCase(
            task = "move my overnight call to 2026-11-29",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("CCU", "Wards")
        ),
        AgentWideCase(
            task = "mark my moonlighting shift as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Quincy", "Somerville", "Telehealth")
        ),
        AgentWideCase(
            task = "set the interest rate on my federal loan to 6.1%",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Unsubsidized", "Grad PLUS")
        ),
        AgentWideCase(
            task = "push the conference deadline to 2026-11-30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("grant", "poster")
        ),
        AgentWideCase(
            task = "tag my hyponatremia case #teaching",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("SIADH", "potomania")
        ),
        AgentWideCase(
            task = "what did the nephro Dr. Kim say about my abstract?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("sodium curve")
        ),
        AgentWideCase(
            task = "add redo missed cardiology Qs to my boards schedule",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boards", "redo missed cardiology qs")
        ),
        AgentWideCase(
            task = "tag both hyponatremia cases #renal",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("SIADH", "potomania", "renal")
        ),
        AgentWideCase(
            task = "the Quincy shift finally got paid, update it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Quincy", "paid")
        ),
        AgentWideCase(
            task = "how much of my conference money haven't I spent yet?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1270"),
            lacks = listOf("1175")
        )
    )
}

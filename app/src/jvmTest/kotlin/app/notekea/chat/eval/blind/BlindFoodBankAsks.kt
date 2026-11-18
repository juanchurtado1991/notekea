package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the food bank vault cannot settle, then traps whose words do settle them. */
internal object BlindFoodBankAsks {
    val all = listOf(
        AgentWideCase(
            task = "set Grace's status to on-leave",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kim", "Mensah")
        ),
        AgentWideCase(
            task = "add a line to the budget: new hand truck, $240",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("grant", "operat")
        ),
        AgentWideCase(
            task = "add 'check the fire extinguisher tags' to the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("distribution")
        ),
        AgentWideCase(
            task = "move the pickup to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Fairmount", "Riverside", "Brigid")
        ),
        AgentWideCase(
            task = "take Grace off the Nov 22 shift",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kim", "Mensah")
        ),
        AgentWideCase(
            task = "change the mobile pantry stop to start at 4pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Fishtown", "Hunting Park", "Eastwick")
        ),
        AgentWideCase(
            task = "push the training back to Dec 3",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("forklift")
        ),
        AgentWideCase(
            task = "mark the turkey pledge as fully received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Riverside", "Ostrowski")
        ),
        AgentWideCase(
            task = "how many hours did Grace Mensah put in in October?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("13"),
            lacks = listOf("21 hours")
        ),
        AgentWideCase(
            task = "add 'restock nitrile gloves' to the distribution checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Distribution", "restock nitrile gloves")
        ),
        AgentWideCase(
            task = "push the Fairmount pickup to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pickups", "Fairmount")
        ),
        AgentWideCase(
            task = "what's the total planned spend across both budgets?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("23360")
        ),
        AgentWideCase(
            task = "tag both Graces with #saturday-crew",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Grace Kim", "Grace Mensah", "saturday-crew")
        )
    )
}

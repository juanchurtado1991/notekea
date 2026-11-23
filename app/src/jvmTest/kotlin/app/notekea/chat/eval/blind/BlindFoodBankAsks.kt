package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the food bank vault cannot settle, then traps whose words do settle them. */
internal object BlindFoodBankAsks {
    val all = listOf(
        ask(
            task = "set Grace's status to on-leave",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Kim", "Mensah")
        ),
        ask(
            task = "add a line to the budget: new hand truck, $240",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("grant", "operat")
        ),
        ask(
            task = "add 'check the fire extinguisher tags' to the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("distribution")
        ),
        ask(
            task = "move the pickup to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Fairmount", "Riverside", "Brigid")
        ),
        ask(
            task = "take Grace off the Nov 22 shift",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kim", "Mensah")
        ),
        ask(
            task = "change the mobile pantry stop to start at 4pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Fishtown", "Hunting Park", "Eastwick")
        ),
        ask(
            task = "push the training back to Dec 3",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("forklift")
        ),
        ask(
            task = "mark the turkey pledge as fully received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Riverside", "Ostrowski")
        ),
        answer(
            task = "how many hours did Grace Mensah put in in October?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("13"),
            lacks = listOf("21 hours")
        ),
        change(
            task = "add 'restock nitrile gloves' to the distribution checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Distribution", "restock nitrile gloves")
        ),
        change(
            task = "push the Fairmount pickup to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Pickups", "Fairmount")
        ),
        answer(
            task = "what's the total planned spend across both budgets?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("23360")
        ),
        change(
            task = "tag both Graces with #saturday-crew",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("Grace Kim", "Grace Mensah", "saturday-crew")
        )
    )
}

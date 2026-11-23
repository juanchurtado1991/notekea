package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back about, and look-alikes whose words settle them. */
internal object BlindResidentDoctorAsks {
    val all = listOf(
        ask(
            task = "add a task to my schedule: print it and stick it on the fridge",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("rotation")
        ),
        ask(
            task = "change the food line in my budget to 450",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user")
        ),
        ask(
            task = "update Dr. Kim's pager to 4-2290",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Daniel", "Grace")
        ),
        ask(
            task = "move my overnight call to 2026-11-29",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("CCU", "Wards")
        ),
        ask(
            task = "mark my moonlighting shift as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Quincy", "Somerville", "Telehealth")
        ),
        ask(
            task = "set the interest rate on my federal loan to 6.1%",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Unsubsidized", "Grad PLUS")
        ),
        ask(
            task = "push the conference deadline to 2026-11-30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("grant", "poster")
        ),
        ask(
            task = "tag my hyponatremia case #teaching",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("SIADH", "potomania")
        ),
        answer(
            task = "what did the nephro Dr. Kim say about my abstract?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("sodium curve")
        ),
        change(
            task = "add redo missed cardiology Qs to my boards schedule",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Boards", "redo missed cardiology qs")
        ),
        change(
            task = "tag both hyponatremia cases #renal",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("SIADH", "potomania", "renal")
        ),
        change(
            task = "the Quincy shift finally got paid, update it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Quincy", "paid")
        ),
        answer(
            task = "how much of my conference money haven't I spent yet?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1270"),
            lacks = listOf("1175")
        )
    )
}

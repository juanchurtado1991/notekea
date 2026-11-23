package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Grad Student vault cannot settle, and traps whose words do settle them. */
internal object BlindGradStudentAsks {
    val all = listOf(
        ask(
            task = "what's the total in my budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Lab", "Travel")
        ),
        ask(
            task = "add a task in Tom's note: call him about the March trip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Tom Ellery", "Tom Kessler")
        ),
        ask(
            task = "put a 90 for Jordan on lab report 3",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Jordan Price", "Jordan Webb")
        ),
        ask(
            task = "cancel my confocal booking",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("2026-11-13", "2026-11-18", "2026-11-20")
        ),
        ask(
            task = "mark the LabWare order as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("ORD-2304", "ORD-2322")
        ),
        ask(
            task = "move the echinoderm dissection lab to the 18th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("2026-11-17", "2026-11-19")
        ),
        ask(
            task = "change the fertilisation rate of the spawning with 3 females to 90%",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("2026-09-08", "2026-11-02")
        ),
        ask(
            task = "mark the pending conference cost as reimbursed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hotel", "Poster printing")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "what's the field station Tom's phone number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("555 0142"),
            lacks = listOf("555 0187")
        ),
        change(
            task = "give Jordan from section 04 an 85 on lab report 3",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Jordan Webb", "85"),
            lacks = listOf("Jordan Price")
        ),
        change(
            task = "cancel the confocal backup slot",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("2026-11-20")
        ),
        change(
            task = "add a 45 USD line for bait bags to my travel budget, fieldwork category",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("bait bags", "45")
        ),
        change(
            task = "mark both LabWare orders as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("ORD-2304", "ORD-2322", "delivered")
        )
    )
}

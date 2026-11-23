package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Fishing Charter vault, three of them not answered by it. */
internal object BlindFishingCharterAnswers {
    val all = listOf(
        answer(
            task = "what did the november trips I've already run gross? skip the cancelled one",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("3500"),
            lacks = listOf("4000")
        ),
        answer(
            task = "how much deposit money am I actually holding for the rest of november's trips",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("350")
        ),
        answer(
            task = "if marisol cancels her trip on the 22nd today, how much of her deposit " +
                "do I give back",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "calculate", "answer"),
            computed = listOf("125")
        ),
        answer(
            task = "how many days till my master license expires",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("70 days")
        ),
        answer(
            task = "how long ago was the salt ledger's last haul-out, in days",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("207 days")
        ),
        answer(
            task = "if a named storm damages the hull what's my deductible in dollars",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("9250")
        ),
        answer(
            task = "how much is each insurance installment",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1710")
        ),
        answer(
            task = "thinking of a dry tortugas run with the bills, anything I gotta do " +
                "for insurance first?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("Renata Solis")
        ),
        answer(
            task = "do I need to add a fuel surcharge to Bill Ostrander's trip on the 19th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            holds = listOf("75")
        ),
        answer(
            task = "what's kenny's day rate now",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("200")
        ),
        answer(
            task = "how many gallons of diesel did the big boat take in october",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("435")
        ),
        answer(
            task = "how much more have I spent on maintenance this year on the salt ledger " +
                "than on the skiff",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("5500")
        ),
        answer(
            task = "how much do I still owe the crew for november",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("720")
        ),
        answer(
            task = "how many fish did we release in total across this month's catch logs",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "calculate", "answer"),
            computed = listOf("29")
        ),
        answer(
            task = "how much deposit money did I refund for weather this year",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("900"),
            lacks = listOf("1300")
        ),
        answer(
            task = "biggest fish we kept this month and who caught it?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "answer"),
            holds = listOf("wahoo", "Bill Hanley")
        ),
        answer(
            task = "what's Bill Ostrander's phone number",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("352-555-0187")
        ),
        answer(
            task = "how much did the new outriggers cost me",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("outriggers cost $", "they cost $", "you paid $")
        ),
        answer(
            task = "when does the skiff's insurance renew",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("skiff renews on", "skiff's policy renews on", "little ledger renews on")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Fishing Charter vault, three of them not answered by it. */
internal object BlindFishingCharterAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what did the november trips I've already run gross? skip the cancelled one",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3500"),
            lacks = listOf("4000")
        ),
        AgentWideCase(
            task = "how much deposit money am I actually holding for the rest of november's trips",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("350")
        ),
        AgentWideCase(
            task = "if marisol cancels her trip on the 22nd today, how much of her deposit " +
                "do I give back",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("125")
        ),
        AgentWideCase(
            task = "how many days till my master license expires",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("70 days")
        ),
        AgentWideCase(
            task = "how long ago was the salt ledger's last haul-out, in days",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("207 days")
        ),
        AgentWideCase(
            task = "if a named storm damages the hull what's my deductible in dollars",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("9250")
        ),
        AgentWideCase(
            task = "how much is each insurance installment",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1710")
        ),
        AgentWideCase(
            task = "thinking of a dry tortugas run with the bills, anything I gotta do " +
                "for insurance first?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Renata Solis")
        ),
        AgentWideCase(
            task = "do I need to add a fuel surcharge to Bill Ostrander's trip on the 19th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("75")
        ),
        AgentWideCase(
            task = "what's kenny's day rate now",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("200")
        ),
        AgentWideCase(
            task = "how many gallons of diesel did the big boat take in october",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("435")
        ),
        AgentWideCase(
            task = "how much more have I spent on maintenance this year on the salt ledger " +
                "than on the skiff",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5500")
        ),
        AgentWideCase(
            task = "how much do I still owe the crew for november",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("720")
        ),
        AgentWideCase(
            task = "how many fish did we release in total across this month's catch logs",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("29")
        ),
        AgentWideCase(
            task = "how much deposit money did I refund for weather this year",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("900"),
            lacks = listOf("1300")
        ),
        AgentWideCase(
            task = "biggest fish we kept this month and who caught it?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("wahoo", "Bill Hanley")
        ),
        AgentWideCase(
            task = "what's Bill Ostrander's phone number",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("352-555-0187")
        ),
        AgentWideCase(
            task = "how much did the new outriggers cost me",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("outriggers cost $", "they cost $", "you paid $")
        ),
        AgentWideCase(
            task = "when does the skiff's insurance renew",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("skiff renews on", "skiff's policy renews on", "little ledger renews on")
        )
    )
}

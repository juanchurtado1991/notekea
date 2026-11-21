package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Bike Shop vault, three of them not answered by the notes. */
internal object BlindBikeShopAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much did Femke end up paying for her service after the bar tape refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("190")
        ),
        AgentWideCase(
            task = "how many days until the christmas ride?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 days")
        ),
        AgentWideCase(
            task = "how many days was Noor's wheel in the shop before it was ready?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 days")
        ),
        AgentWideCase(
            task = "how much is still unpaid across all my invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("411.50")
        ),
        AgentWideCase(
            task = "how many people are in the ride group?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "count", "answer"),
            ends = End.ANSWER,
            computed = listOf("19")
        ),
        AgentWideCase(
            task = "whats the average number of riders on the autumn rides so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("13.5")
        ),
        AgentWideCase(
            task = "how long are e-bike batteries under warranty?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("18 months")
        ),
        AgentWideCase(
            task = "how long does a customer have to report a defect?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("30 days")
        ),
        AgentWideCase(
            task = "what day do the old tyres get picked up?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Thursday")
        ),
        AgentWideCase(
            task = "what time do we start the gooi ride?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("9:00")
        ),
        AgentWideCase(
            task = "how many km longer is the gooi ride than the christmas ride?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("46")
        ),
        AgentWideCase(
            task = "who's supplying the pads for Lars de Wit's cargo bike?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Remwerk")
        ),
        AgentWideCase(
            task = "what do my monthly costs come to without the loan repayment?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1864")
        ),
        AgentWideCase(
            task = "how much rental money do I get from the november bookings, not counting " +
                "the cancelled ones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("460")
        ),
        AgentWideCase(
            task = "which repair tickets are still open?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("T-1038", "T-1052", "T-1053")
        ),
        AgentWideCase(
            task = "what's Lars de Wit's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("email is lars")
        ),
        AgentWideCase(
            task = "how much is the deposit for a tandem?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("tandem deposit is €", "for a tandem is €")
        ),
        AgentWideCase(
            task = "what time does the christmas ride start?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("christmas ride starts at 0", "christmas ride starts at 1")
        )
    )
}

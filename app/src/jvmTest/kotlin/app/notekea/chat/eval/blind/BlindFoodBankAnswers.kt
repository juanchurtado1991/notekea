package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the food bank vault. */
internal object BlindFoodBankAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how many pounds did Riverside give us in October?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3075"),
            lacks = listOf("4180")
        ),
        AgentWideCase(
            task = "total pounds donated in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5067"),
            lacks = listOf("6172")
        ),
        AgentWideCase(
            task = "how many households did we serve on the October distribution days?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("764")
        ),
        AgentWideCase(
            task = "what was our average households per distribution day in oct",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("191")
        ),
        AgentWideCase(
            task = "how many days do I have left before the grant report is due?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("34 days")
        ),
        AgentWideCase(
            task = "how long can thawed meat sit in the walk-in before we toss it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("48 hours")
        ),
        AgentWideCase(
            task = "who's our program officer at the Schuylkill fund again?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Renata Vos")
        ),
        AgentWideCase(
            task = "who do we ask for when we pick up at Fairmount Bakery?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Teodoro")
        ),
        AgentWideCase(
            task = "how big is Harlan's last delivery of the season, and what is it?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("900", "sweet potatoes")
        ),
        AgentWideCase(
            task = "october hours for individual volunteers only, leave out the school group",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("76")
        ),
        AgentWideCase(
            task = "how many cases of canned food can we hand out right now? skip anything on hold",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("163")
        ),
        AgentWideCase(
            task = "which donor has given us the most pounds so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Brigid"),
            computed = listOf("1390")
        ),
        AgentWideCase(
            task = "how many days between Luis's orientation and his first shift?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("27 days")
        ),
        AgentWideCase(
            task = "did we serve more households on Nov 8 than on our best October day?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("223", "211")
        ),
        AgentWideCase(
            task = "how many turkeys are we still waiting on for thanksgiving?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("125")
        ),
        AgentWideCase(
            task = "what's Marcus Bell's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("215-555-0148", "267-555-0192")
        ),
        AgentWideCase(
            task = "how much is the chest freezer going to cost us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("freezer costs $3200", "freezer is $3200")
        ),
        AgentWideCase(
            task = "when's Grace Mensah's birthday?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("march 3")
        )
    )
}

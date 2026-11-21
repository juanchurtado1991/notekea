package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what's the average birth weight of juniper's litter?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("389")
        ),
        AgentWideCase(
            task = "how much in puppy deposits am I actually holding right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1750"),
            lacks = listOf("2250", "2750")
        ),
        AgentWideCase(
            task = "how much does Priya still owe me at pickup for her pup?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2050")
        ),
        AgentWideCase(
            task = "how many days between the puppies' first DHPP and their rabies shot?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("70 days")
        ),
        AgentWideCase(
            task = "how long is the health guarantee in my puppy contract?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("26 months")
        ),
        AgentWideCase(
            task = "if a buyer picks up late, what's the boarding fee per day in the contract?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("35")
        ),
        AgentWideCase(
            task = "how much is still owed to me on invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1780"),
            lacks = listOf("1875")
        ),
        AgentWideCase(
            task = "what did the business really spend in october, litter included? " +
                "leave out my personal stuff and the refunded pen",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("708"),
            lacks = listOf("838", "950")
        ),
        AgentWideCase(
            task = "did the litter cost me more in october or november so far? " +
                "don't count the refunded pen",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("October"),
            computed = listOf("353", "293")
        ),
        AgentWideCase(
            task = "is Megan Ashby's puppy healthy? anything I have to tell her?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("hernia")
        ),
        AgentWideCase(
            task = "who's doing the temperament test and what time?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Joanne Ruiz", "10:00")
        ),
        AgentWideCase(
            task = "how old are the puppies today in days?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("52 days")
        ),
        AgentWideCase(
            task = "what med is Ranger on for vet visits?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("trazodone")
        ),
        AgentWideCase(
            task = "who still hasn't paid for puppy basics?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ines Varga", "Ray Colton")
        ),
        AgentWideCase(
            task = "how much has the fall puppy basics class actually brought in? paid only",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "count", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1050"),
            lacks = listOf("1680", "1260")
        ),
        AgentWideCase(
            task = "what am I charging per dog for the reactive dog class in january?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("210")
        ),
        AgentWideCase(
            task = "how much does Ranger weigh?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("18 kg")
        ),
        AgentWideCase(
            task = "how much did juniper's progesterone testing cost me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("240")
        )
    )
}

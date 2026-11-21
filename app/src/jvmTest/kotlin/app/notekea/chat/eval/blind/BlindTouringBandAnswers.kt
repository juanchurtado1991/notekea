package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTouringBandAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much have the venues actually paid us so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3650"),
            lacks = listOf("4000")
        ),
        AgentWideCase(
            task = "how much do venues still owe us for shows we've already played?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1350")
        ),
        AgentWideCase(
            task = "what did merch actually bring in on leg one after refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3900"),
            lacks = listOf("3970")
        ),
        AgentWideCase(
            task = "how much have I spent on fuel itself? not parking or tolls",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("411"),
            lacks = listOf("460")
        ),
        AgentWideCase(
            task = "how many days till we start recording at greywater?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("33 days")
        ),
        AgentWideCase(
            task = "how many days between the saltmarsh reply deadline and the festival itself?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("207 days")
        ),
        AgentWideCase(
            task = "what's ollie sato's day rate?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("240")
        ),
        AgentWideCase(
            task = "what commission does the agency take on our shows?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("12%")
        ),
        AgentWideCase(
            task = "how much notice do we have to give to get out of the harbourline deal?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("90 days")
        ),
        AgentWideCase(
            task = "which songs is mags playing on at greywater?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Lowtide Hymn", "Saltlight"),
            lacks = listOf("Paper Saints")
        ),
        AgentWideCase(
            task = "what's the capacity of the cardiff venue?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("520")
        ),
        AgentWideCase(
            task = "how many songs are in the headline set, encore included?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "count", "answer"),
            ends = End.ANSWER,
            computed = listOf("15 songs")
        ),
        AgentWideCase(
            task = "who's drumming in cardiff?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ollie Sato")
        ),
        AgentWideCase(
            task = "what's our average guarantee across all eight shows?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("975")
        ),
        AgentWideCase(
            task = "how many tees did we sell on leg one, not counting the refunded one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("106")
        ),
        AgentWideCase(
            task = "how much is saltmarsh paying us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("1200")
        ),
        AgentWideCase(
            task = "what's mags ferreira's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("07700")
        ),
        AgentWideCase(
            task = "what time is load-in at the hollow arch?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("15:30", "16:00", "16:30")
        )
    )
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHistoryThesisChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to chapter 4: check the 1852 Dundee flax prices",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Chapter 4 Flax and linen", "1852")
        ),
        AgentWideCase(
            task = "set chapter 3's status to revised",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Chapter 3 Timber duties", "revised")
        ),
        AgentWideCase(
            task = "tag the Bergen workshop note #presented",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("North Sea Trade Workshop Bergen", "presented")
        ),
        AgentWideCase(
            task = "log today in my writing log: chapter 4, 980 words",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Writing log", "980")
        ),
        AgentWideCase(
            task = "make a note called Pending claims listing the expense claims I'm " +
                "still waiting on",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pending claims", "Hull train", "Utrecht registration"),
            lacks = listOf("Bergen flight")
        ),
        AgentWideCase(
            task = "move the annual review note into the Thesis folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Annual review", "Thesis")
        ),
        AgentWideCase(
            task = "rename the Utrecht conference note to Utrecht 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Utrecht 2027")
        ),
        AgentWideCase(
            task = "move my NRS visit on 2026-11-19 to 2026-11-26",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("National Records of Scotland", "2026-11-26")
        ),
        AgentWideCase(
            task = "tag every book note I rated 4 or more with #core",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Merchants of the Northern Sea",
                "Timber and Empire",
                "Ports and Paperwork"
            ),
            lacks = listOf("The Flax Frontier", "Baltic Credit Networks")
        ),
        AgentWideCase(
            task = "tag the chapters that are still under their target word count #behind",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Chapter 1 Introduction",
                "Chapter 3 Timber duties",
                "Chapter 4 Flax and linen"
            ),
            lacks = listOf("Chapter 2 Leith merchants")
        ),
        AgentWideCase(
            task = "add to the NRS note that CE57/1/12 is back from conservation",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("National Records of Scotland", "conservation")
        ),
        AgentWideCase(
            task = "link chapter 3 to Timber and Empire",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Chapter 3 Timber duties", "Timber and Empire")
        )
    )
}

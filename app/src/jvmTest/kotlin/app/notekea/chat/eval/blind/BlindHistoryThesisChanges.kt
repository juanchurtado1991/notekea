package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHistoryThesisChanges {
    val all = listOf(
        change(
            task = "add a task to chapter 4: check the 1852 Dundee flax prices",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Chapter 4 Flax and linen", "1852")
        ),
        change(
            task = "set chapter 3's status to revised",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Chapter 3 Timber duties", "revised")
        ),
        change(
            task = "tag the Bergen workshop note #presented",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("North Sea Trade Workshop Bergen", "presented")
        ),
        change(
            task = "log today in my writing log: chapter 4, 980 words",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            holds = listOf("Writing log", "980")
        ),
        change(
            task = "make a note called Pending claims listing the expense claims I'm " +
                "still waiting on",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            holds = listOf("Pending claims", "Hull train", "Utrecht registration"),
            lacks = listOf("Bergen flight")
        ),
        change(
            task = "move the annual review note into the Thesis folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Annual review", "Thesis")
        ),
        change(
            task = "rename the Utrecht conference note to Utrecht 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Utrecht 2027")
        ),
        change(
            task = "move my NRS visit on 2026-11-19 to 2026-11-26",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("National Records of Scotland", "2026-11-26")
        ),
        change(
            task = "tag every book note I rated 4 or more with #core",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            holds = listOf(
                "Merchants of the Northern Sea",
                "Timber and Empire",
                "Ports and Paperwork"
            ),
            lacks = listOf("The Flax Frontier", "Baltic Credit Networks")
        ),
        change(
            task = "tag the chapters that are still under their target word count #behind",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_tag", "propose"),
            holds = listOf(
                "Chapter 1 Introduction",
                "Chapter 3 Timber duties",
                "Chapter 4 Flax and linen"
            ),
            lacks = listOf("Chapter 2 Leith merchants")
        ),
        change(
            task = "add to the NRS note that CE57/1/12 is back from conservation",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("National Records of Scotland", "conservation")
        ),
        change(
            task = "link chapter 3 to Timber and Empire",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Chapter 3 Timber duties", "Timber and Empire")
        )
    )
}

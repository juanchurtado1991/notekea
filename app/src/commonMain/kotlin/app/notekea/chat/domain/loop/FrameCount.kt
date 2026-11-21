package app.notekea.chat.domain.loop

/** How many things a task means, as the frame's model writes it. */
enum class FrameCount(val written: String) {
    /** One thing: the appointment, Ana's phone, my budget. */
    ONE(written = "one"),

    /** The several it names: both, my two budgets, Ana and Luis. */
    SEVERAL(written = "several"),

    /** Every one a condition picks out: the clients with an unpaid invoice, my recipes. */
    EVERY(written = "every"),

    /** No particular note: a new note from nothing, the app, the web. */
    NONE(written = "none")
}

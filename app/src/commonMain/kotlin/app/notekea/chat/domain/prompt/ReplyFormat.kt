package app.notekea.chat.domain.prompt

/**
 * How a reply of the chat is shaped (the developer's request of 2026-11-13: well structured, in Markdown when it
 * helps), which the chat then draws: the shape follows the content, and a simple answer stays a plain sentence.
 */
object ReplyFormat {
    const val MARKDOWN =
        "Shape the reply to its content with Markdown: steps in order as a numbered list, one step a line; " +
            "several things as a bulleted list; **bold** for the key name or value; `code` for commands, file " +
            "names and shortcuts. A simple answer stays one plain sentence: no heading, no list of one item."
}

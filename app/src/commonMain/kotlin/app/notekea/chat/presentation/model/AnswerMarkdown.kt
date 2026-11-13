package app.notekea.chat.presentation.model

import app.notekea.core.presentation.designsystem.components.markdown.InlineLinks

/**
 * An answer's text as the Markdown the chat draws (the developer's smoke of 2026-11-13: the model writes Markdown,
 * which showed raw): each `[n]` of a source it has becomes that citation's inline content, a wiki link its name in
 * bold, and while the model writes a caret stands where it writes next.
 */
object AnswerMarkdown {
    const val CITATION = "citation:"
    const val CARET = "caret"

    // Not a link the model wrote itself, "[1](https://…)", nor a task's box.
    private val CITED = Regex(pattern = """\[(\d{1,3})](?!\()""")
    private val WIKI_LINK = Regex(pattern = """\[\[(?:[^\]|]*\|)?([^\]]+)]]""")
    private val FENCE = Regex(pattern = """^\s*(```|~~~)""")

    fun of(
        text: String,
        numbers: Set<Int>,
        isWriting: Boolean
    ): String {
        var isCode = false

        val lines = text.lines().map { line ->
            val isFence = FENCE.containsMatchIn(input = line)
            if (isFence) isCode = !isCode

            if (isCode || isFence) {
                line
            } else {
                shown(
                    line = line,
                    numbers = numbers
                )
            }
        }

        val shown = lines.joinToString(separator = "\n")
        // Inside an open block of code a caret would be read as code.
        if (!isWriting || isCode) return shown

        return shown.trimEnd() + InlineLinks.of(
            id = CARET,
            text = "\u200B"
        )
    }

    private fun shown(
        line: String,
        numbers: Set<Int>
    ): String {
        val named = WIKI_LINK.replace(input = line) { "**${it.groupValues[1]}**" }

        return CITED
            .replace(input = named) { match ->
                val number = match.groupValues[1].toInt()

                if (number in numbers) {
                    InlineLinks.of(
                        id = CITATION + number,
                        text = "\\[$number\\]"
                    )
                } else {
                    match.value
                }
            }
    }
}

package app.notekea.shell.data.chat

import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * A note's text with text added at its end, in the note's own line endings: after a blank line, after one line break
 * when a list item follows a list, with nothing before it in a blank note, and never inside a code block left open,
 * which is closed first.
 */
internal object AppendedText {
    private const val FENCE = "```"
    private val LINE_END = Regex(pattern = "\\r?\\n")
    private val LIST_ITEM = Regex(pattern = "^\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s")

    fun of(
        before: String,
        added: String
    ): String {
        // A line feed alone in a CRLF note left its tasks unable to be ticked.
        val end = if ("\r\n" in before) "\r\n" else "\n"

        val text = added.trim().replace(
            regex = LINE_END,
            replacement = end
        )

        if (before.isBlank()) return text + end

        val kept = before.trimEnd()

        val last = kept
            .lines()
            .last()

        val fence = openFenceOf(text = kept)

        // A list item under a list joins it; any other text after one would continue its last item.
        val isListed =
            LIST_ITEM.containsMatchIn(input = last) && LIST_ITEM.containsMatchIn(input = text)

        val gap = when {
            fence != null -> end + fence + end + end
            isListed -> end
            else -> end + end
        }

        return kept + gap + text + end
    }

    // The fence that opened a code block still open at the text's end, to close it with; null when none is.
    private fun openFenceOf(text: String): String? {
        val fences = FenceTracker()
        var opened: String? = null

        text.lines().forEach { line ->
            val wasOpen = fences.isOpen
            fences.read(text = line)

            if (!wasOpen && fences.isOpen) {
                opened = MarkdownSyntax
                    .FENCE_LINE
                    .find(input = line.trimStart(' ', '>'))
                    ?.groupValues
                    ?.get(index = 1)
            }
        }

        if (!fences.isOpen) return null

        return opened ?: FENCE
    }
}

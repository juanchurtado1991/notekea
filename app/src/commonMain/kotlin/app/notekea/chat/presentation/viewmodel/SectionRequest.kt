package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.plan.ClipWords
import app.notekea.chat.domain.plan.SectionAsked

/**
 * The answer to the question of a page's links ([SectionAsked]) as the request the chat then acts on, in the words
 * [ClipWords] reads: a deep choice is counted first, and clipped once the person confirms the count.
 */
internal object SectionRequest {
    fun of(
        section: SectionAsked,
        isWhole: Boolean,
        isEvery: Boolean,
        depth: Int
    ): String {
        val url = section.url
        val isFull = depth >= ClipWords.FULL_DEPTH
        // Counted already: this is the yes to it.
        val count = if (section.isCounted) "" else ", ${ClipWords.COUNT_FIRST}"

        return when {
            !isWhole -> "clip only $url"
            isFull -> "clip $url with its linked pages, the whole documentation$count"
            depth > 1 -> "clip $url with its linked pages, $depth levels deep$count"
            isEvery -> "clip $url with all its linked pages"
            else -> "clip $url with its linked pages"
        }
    }
}

package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * [this] conversation for the model with the person's [language] said at the end of its last message, so the reply
 * is in it even when the notes, or the plan's rewording of the request, are in another; unchanged when unknown. A
 * [kept] reply is written as it is: a NOT_FOUND translated was taken for an answer (the chat probe, 2026-11-11).
 */
internal fun List<ChatMessage>.inLanguage(
    language: String?,
    kept: String? = null
): List<ChatMessage> {
    val last = indexOfLast {
        it.role == ChatRole.USER
    }

    val isKept = language == null || last < 0
    if (isKept) return this

    val keeping = kept?.let { " When your reply is $it, write $it as it is." }.orEmpty()
    val said = "Write your reply in $language, every word of it.$keeping"

    return mapIndexed { index, message ->
        if (index == last) message.copy(text = "${message.text}\n\n$said") else message
    }
}

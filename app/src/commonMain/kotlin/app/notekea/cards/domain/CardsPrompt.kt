package app.notekea.cards.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to make flashcards of a note (4.6): the note, and how to write them: one fact each, a short
 * answer, the note's own sentence it comes from copied exactly, in the note's language. The reply is JSON ([SCHEMA]).
 */
object CardsPrompt {
    const val MAX_CARDS = 8
    private const val MAX_NOTE_CHARS = 8_000

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"cards\":{\"type\":\"array\",\"maxItems\":$MAX_CARDS,\"items\":" +
            "{\"type\":\"object\",\"properties\":{\"question\":{\"type\":\"string\"},\"answer\":{\"type\":\"string\"}," +
            "\"sentence\":{\"type\":\"string\"}},\"required\":[\"question\",\"answer\",\"sentence\"]}}}," +
            "\"required\":[\"cards\"]}"

    private const val INSTRUCTIONS =
        "You make flashcards to study a note. Each card asks about one fact the note states, with a short answer " +
            "of a few words. Copy into \"sentence\" the note's own sentence the fact comes from, exactly as written. " +
            "Write the questions and answers in the note's language. Make up to $MAX_CARDS cards, fewer for a " +
            "short note, and none about facts the note does not state."

    fun messages(
        title: String,
        text: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Note \"$title\":\n${text.take(n = MAX_NOTE_CHARS)}"
        )
    )
}

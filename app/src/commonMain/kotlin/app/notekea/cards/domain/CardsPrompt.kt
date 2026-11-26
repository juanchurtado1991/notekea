package app.notekea.cards.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to make flashcards of a note (4.6): the note, and how to write them: the note's sentence
 * first, copied exactly, then a question it answers and a short answer from it. The reply is JSON ([SCHEMA]).
 */
object CardsPrompt {
    const val MAX_CARDS = 8
    const val MAX_NOTE_CHARS = 8_000

    // The note's language first, named by the model: asked for the sentence first, the E4B wrote Spanish
    // questions for English notes (the cards' set, 2026-11-26).
    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"language\":{\"type\":\"string\"},\"cards\":{\"type\":\"array\"," +
            "\"maxItems\":$MAX_CARDS,\"items\":{\"type\":\"object\",\"properties\":{\"sentence\":{\"type\":\"string\"}," +
            "\"question\":{\"type\":\"string\"},\"answer\":{\"type\":\"string\"}}," +
            "\"required\":[\"sentence\",\"question\",\"answer\"]}}},\"required\":[\"language\",\"cards\"]}"

    private const val INSTRUCTIONS =
        "You make flashcards to study a note. First write in \"language\" the language the note is written " +
            "in. Then, for each card, copy into \"sentence\" one sentence of the note exactly as written, then " +
            "write a question that sentence answers and its short answer of a few words, taken from that " +
            "sentence, both in that language. Make up to $MAX_CARDS cards, fewer for a short note, each from a " +
            "different sentence."

    /** [text] is the note, or the part of it at [place] ("part 2 of 5") when it is cut into several. */
    fun messages(
        title: String,
        text: String,
        place: String? = null
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Note \"$title\"${place?.let { ", $it" }.orEmpty()}:\n${text.take(n = MAX_NOTE_CHARS)}"
        )
    )
}

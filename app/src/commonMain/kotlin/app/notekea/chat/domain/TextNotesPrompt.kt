package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to turn a pasted text into notes (`notes_from_text`, 3.9): one part of the text at a time,
 * a note per topic, each rewritten in its own words and nothing invented; the reply is JSON shaped by [SCHEMA].
 */
object TextNotesPrompt {
    const val MAX_NOTES_PER_PART = 6

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"notes\":{\"type\":\"array\",\"minItems\":1," +
            "\"maxItems\":$MAX_NOTES_PER_PART,\"items\":{\"type\":\"object\",\"properties\":{\"title\":" +
            "{\"type\":\"string\"},\"text\":{\"type\":\"string\"}},\"required\":[\"title\",\"text\"]}}}," +
            "\"required\":[\"notes\"]}"

    private const val INSTRUCTIONS =
        "You split a text into notes for a notes app, one note per topic, in the language of the text. Each note " +
            "has a short title (a few words, no slashes or colons) and a Markdown text that keeps every fact, name, " +
            "number and date of its part, organized with short headings and lists, in your own words; never invent. " +
            "Make at most $MAX_NOTES_PER_PART notes; a short text is one or two notes. Link related notes with " +
            "[[Title]]. ${WritePrompt.FORMATTING} Titles already used: %s."

    fun messages(
        request: String,
        part: String,
        usedTitles: List<String>
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS.replace("%s", usedTitles.ifEmpty { listOf("none") }.joinToString())
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Request: ${request.lines().first().take(n = REQUEST_LINE)}\n\nText:\n$part"
        )
    )

    private const val REQUEST_LINE = 200
}

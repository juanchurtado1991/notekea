package app.notekea.chat.domain.prompt

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to write a note's body for an action (3.9): the request, what to write, and the notes to
 * write it from, if any; the length asked for grows with the notes read ([wordsFor]), so a summary of 30 notes is
 * longer than one of 3, never endless.
 */
object WritePrompt {
    /**
     * The Markdown the editor draws, the formatting toolbar's, so a written note reads like one a person made with it;
     * the shape follows the content (steps numbered, a comparison as a table), never every format at once.
     */
    const val FORMATTING =
        "Format it with the Markdown the app draws, choosing what fits the content: ## and ### headings, **bold** " +
            "for key words, *italic*, - bulleted lists, 1. numbered lists for steps, - [ ] tasks for things to do, " +
            "> quotes, callouts like > [!note] or > [!tip] for what stands out, | tables | for comparisons and data, " +
            "[[Note name]] links to notes of the vault and #tags. Do not use every format at once."

    private const val FROM_NOTES =
        "You write the body of a Markdown note for a notes app, in the language of the request. Use only what the " +
            "notes below say; never invent. Write about %d words, with short headings and lists where they help, " +
            "in your own words (do not paste whole paragraphs). After each point, name the note it comes from as a " +
            "link, like [[Note name]]. Do not write a title. $FORMATTING"

    private const val FROM_REQUEST =
        "You write the body of a Markdown note for a notes app, in the language of the request: useful, concrete " +
            "and well organized. Write at most %d words. Do not write a title. $FORMATTING"

    private const val DIGEST =
        "Write in two or three sentences what this note says that matters for the request, in the language of the " +
            "request. Keep names, numbers and dates."

    private const val BASE_WORDS = 120
    private const val WORDS_PER_NOTE = 20
    private const val MAX_WORDS = 600
    private const val FREE_WORDS = 250

    /** How long a note written from [notes] notes should be: 120 words, 20 more per note, at most 600. */
    fun wordsFor(notes: Int): Int =
        (BASE_WORDS + WORDS_PER_NOTE * notes).coerceAtMost(maximumValue = MAX_WORDS)

    /** A body written from [sources] (each a note's name and its text, or a digest of it) for [what]. */
    fun fromNotes(
        request: String,
        what: String,
        sources: List<Pair<String, String>>
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = FROM_NOTES.replace("%d", wordsFor(notes = sources.size).toString())
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Request: $request\nWhat to write: $what\n\nNotes:\n" +
                sources.joinToString(separator = "\n\n") { (name, text) -> "### [[$name]]\n$text" }
        )
    )

    /** A body written from the request alone (a note "with a simple recipe"). */
    fun fromRequest(
        request: String,
        what: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = FROM_REQUEST.replace("%d", FREE_WORDS.toString())
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Request: $request\nWhat to write: $what"
        )
    )

    /** A short digest of one note, when the notes together are too long to be read at once. */
    fun digest(
        request: String,
        name: String,
        text: String
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = DIGEST
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Request: $request\n\n### $name\n$text"
        )
    )
}

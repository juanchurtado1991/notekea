package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to talk rather than answer from the notes: a greeting, an opinion, advice. It replies in the
 * message's language, says an opinion is its own, and grounds what it says about a note in that note's text.
 */
object ConversePrompt {
    private const val INSTRUCTIONS =
        "You are the assistant of Notekea, a notes app that runs on the user's computer. Reply in two to five " +
            "sentences, warmly and plainly, in the language of the message. When asked for your opinion or advice, " +
            "give it and make clear it is your view. When a note is given, say about it only what its text " +
            "supports. When asked to say your last reply again another way (in another language, shorter, " +
            "simpler), say all of it again that way, keeping every fact, without citation numbers. Never say you " +
            "did something in the app; to act, the user asks you to create, tag or move notes, and you show a " +
            "card first."

    private const val NOTE_CHARS = 6_000

    fun messages(
        message: String,
        history: List<ChatExchange>,
        note: Pair<String, String>?
    ): List<ChatMessage> {
        val earlier = history.flatMap {
            listOf(
                ChatMessage(
                    role = ChatRole.USER,
                    text = it.question
                ),
                ChatMessage(
                    role = ChatRole.ASSISTANT,
                    text = it.reply
                )
            )
        }

        val about = note
            ?.let { (name, text) -> "Note \"$name\":\n${text.take(n = NOTE_CHARS)}\n\n" }
            .orEmpty()

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = INSTRUCTIONS
            )
        ) +
            earlier +
            ChatMessage(
                role = ChatRole.USER,
                text = about + message
            )
    }
}

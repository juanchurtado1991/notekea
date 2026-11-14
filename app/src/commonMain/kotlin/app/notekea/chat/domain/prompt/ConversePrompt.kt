package app.notekea.chat.domain.prompt

import app.notekea.chat.domain.ChatExchange
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to talk rather than answer from the notes: a greeting, an opinion, advice. It replies in the
 * message's language, says an opinion is its own, and grounds what it says about a note in that note's text.
 */
object ConversePrompt {
    private const val INSTRUCTIONS =
        "You are the assistant of Notekea, a notes app that runs on the user's computer, and you can read the " +
            "user's notes. Reply plainly and briefly (one to four sentences, or a short list) in the language of the message. " +
            "${ReplyFormat.MARKDOWN} Only " +
            "when asked for your opinion or advice, give it and say it is your view. A general question (a sum, a " +
            "fact everyone knows) you answer directly. When a note is given, say about it only what its text " +
            "supports. When asked to say your last reply again another way (in another language, shorter, " +
            "simpler), say all of it again that way, directly (never that you will), keeping every fact and adding " +
            "nothing, without citation " +
            "numbers. A message you cannot make sense of: say you did not understand it and ask what they mean. A " +
            "complaint about Notekea: take it calmly and ask what went wrong, to help with it. Never say you " +
            "cannot read the notes or reach the computer; what changed today is said after the date. What you did earlier " +
            "in this chat is written in it as a record of what you did: asked about it (why you wrote something, " +
            "whether you made it up, whether it is real data), answer from that record and honestly: a text the " +
            "record says you wrote without reading a note was your own example and not the person's data, so say " +
            "so plainly, never defend it, and offer to write it again reading the note they name; a text the " +
            "record says you wrote from notes you read was taken from them, so say which notes you read and " +
            "that it was not made up, even when the person says it was, and invite them to check it. Beyond that " +
            "record, never say you " +
            "did something in the app; to act, the user asks you to create, tag or move notes, and you show a " +
            "card first."

    private const val NOTE_CHARS = 6_000

    fun messages(
        message: String,
        history: List<ChatExchange>,
        note: Pair<String, String>?,
        today: String? = null,
        working: String? = null
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
                text = INSTRUCTIONS + today?.let { " Today is $it." }.orEmpty()
            )
        ) +
            earlier +
            ChatMessage(
                role = ChatRole.USER,
                text = about + working?.let { "$it\n\n" }.orEmpty() + message
            )
    }
}

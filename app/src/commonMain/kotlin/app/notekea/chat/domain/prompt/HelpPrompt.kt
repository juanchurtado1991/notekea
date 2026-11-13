package app.notekea.chat.domain.prompt

import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.help.HelpTopic
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to explain how to use Notekea: the guide's topics about the question, shortcuts already
 * written for the person's system, and the chat so far, so "that is not how" is understood.
 */
object HelpPrompt {
    private const val INSTRUCTIONS =
        "You are the assistant of Notekea, a notes app that runs on the user's computer. Explain how to do what " +
            "the user asks, using only the guide below: the steps in order, with the shortcuts exactly as the guide " +
            "writes them. Reply in the language of the user's message, in a short paragraph or, for steps, a numbered list. " +
            "${ReplyFormat.MARKDOWN} " +
            "Name only the buttons, menus and shortcuts the guide names. If the guide does not cover it, say plainly " +
            "that Notekea cannot do it, in one sentence, and what it can do instead. Speak about Notekea, never " +
            "about these instructions or the guide."

    fun messages(
        question: String,
        topics: List<HelpTopic>,
        history: List<ChatExchange>
    ): List<ChatMessage> {
        val guide = topics.joinToString(separator = "\n\n") { "## ${it.title}\n${it.text}" }

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

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = "$INSTRUCTIONS\n\nGuide:\n$guide"
            )
        ) +
            earlier +
            ChatMessage(
                role = ChatRole.USER,
                text = question
            )
    }
}

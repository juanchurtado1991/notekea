package app.notekea.cards.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to write a hard card anew (4.6): the note's sentence it comes from and the card as it is,
 * and up to [VERSIONS] other wordings of it from that sentence only. The reply is the cards' JSON ([SCHEMA]).
 */
object RewritePrompt {
    const val VERSIONS = 3

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"cards\":{\"type\":\"array\",\"minItems\":1,\"maxItems\":$VERSIONS," +
            "\"items\":{\"type\":\"object\",\"properties\":{\"question\":{\"type\":\"string\"}," +
            "\"answer\":{\"type\":\"string\"}},\"required\":[\"question\",\"answer\"]}}},\"required\":[\"cards\"]}"

    private const val INSTRUCTIONS =
        "You rewrite a flashcard its reader keeps getting wrong. Write up to $VERSIONS new versions of it, each a " +
            "question the sentence answers, asked another way than the old question and clearer, and its short " +
            "answer of a few words, taken from the sentence. Use only what the sentence says, in its language."

    fun messages(
        sentence: String,
        card: StudyCard
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Sentence: $sentence\nOld question: ${card.question}\nOld answer: ${card.answer}"
        )
    )
}

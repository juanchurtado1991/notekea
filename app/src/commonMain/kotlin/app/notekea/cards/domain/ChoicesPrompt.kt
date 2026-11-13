package app.notekea.cards.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to make a card a multiple choice question (the developer's design of 2026-11-13): three
 * wrong answers that look like the right one, so choosing still needs remembering. The reply is JSON ([SCHEMA]).
 */
object ChoicesPrompt {
    const val WRONG_ANSWERS = 3

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"wrong\":{\"type\":\"array\",\"minItems\":$WRONG_ANSWERS," +
            "\"maxItems\":$WRONG_ANSWERS,\"items\":{\"type\":\"string\"}}},\"required\":[\"wrong\"]}"

    private const val INSTRUCTIONS =
        "You write the wrong options of a multiple choice question made from a flashcard. Write $WRONG_ANSWERS " +
            "answers that are wrong but believable: the same kind of thing as the right answer, about as long, in " +
            "its language and style. Never the right answer, a synonym of it, or a joke."

    fun messages(card: StudyCard): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Question: ${card.question}\nRight answer: ${card.answer}"
        )
    )
}

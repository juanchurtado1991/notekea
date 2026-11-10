package app.notekea.chat.eval

import app.notekea.chat.domain.AnswerSource

/**
 * How one [question] went: the [answer] (null when the notes did not answer it), the passages [read] and [cited],
 * the milliseconds until the answer began to show ([firstMillis], null when nothing was written) and until it ended.
 */
data class AnsweredQuestion(
    val question: AnswerQuestion,
    val read: List<AnswerSource>,
    val cited: List<AnswerSource>,
    val totalMillis: Long,
    val answer: String?,
    val firstMillis: Long?,
    val failure: String? = null
) {
    val isRight: Boolean
        get() = AnswerGrade.isRight(
            question = question,
            excerpts = read.map { it.text },
            answer = answer
        )

    /** A right answer whose cited passages hold its facts: what a person checking the citations would find. */
    val isCitedRight: Boolean
        get() =
            isRight &&
                !question.isTrap &&
                AnswerGrade.hasFacts(
                    question = question,
                    text = cited.joinToString(separator = " ") { it.text }
                )
}

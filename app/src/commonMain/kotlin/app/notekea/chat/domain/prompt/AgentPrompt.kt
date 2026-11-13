package app.notekea.chat.domain.prompt

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given at each step of the agent (`AgentAnswerUseCase`): the question, the vault's note names, and
 * what it found so far; it answers with one move as JSON shaped by [SCHEMA].
 */
object AgentPrompt {
    const val SEARCH = "search"
    const val READ = "read"
    const val ANSWER = "answer"
    const val GIVE_UP = "not_found"

    val SCHEMA =
        "{\"type\":\"object\",\"properties\":{\"move\":{\"type\":\"string\",\"enum\":[\"$SEARCH\",\"$READ\"," +
            "\"$ANSWER\",\"$GIVE_UP\"]},\"query\":{\"type\":\"string\"},\"note\":{\"type\":\"string\"}}," +
            "\"required\":[\"move\"]}"

    private const val INSTRUCTIONS =
        """A first search of the user's notes did not answer the question. Find the answer in a few moves, as JSON:
- search: query, other words for what the question asks (a synonym, the other language, a name it implies).
- read: note, the name of a note that likely holds the answer, to read it whole.
- answer: what was found answers the question.
- not_found: the notes do not hold the answer; say so rather than guess.
Never read the same note twice or search the same words twice."""

    fun messages(
        question: String,
        notes: List<String>,
        found: List<String>
    ): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Notes: ${notes.joinToString()}\n\nFound so far:\n" +
                found.ifEmpty { listOf("nothing") }.joinToString(separator = "\n") +
                "\n\nQuestion: $question"
        )
    )
}

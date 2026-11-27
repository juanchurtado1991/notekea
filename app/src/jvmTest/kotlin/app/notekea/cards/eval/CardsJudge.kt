package app.notekea.cards.eval

import app.notekea.chat.data.GhostModelJson
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.ReplyText

/**
 * The study cards' judge (the developer's rules of 2026-11-26): a card passes when it asks what one would want to
 * remember, cannot be guessed, is no sentence with a gap, has one right answer and is understood without the note;
 * and which sentences of a note are worth a card at all, for the cover.
 */
internal class CardsJudge(private val engine: LlmEngine) {
    private val json = GhostModelJson()

    /** The rules [card] fails, by name; none when it passes. Null when the judge's reply cannot be read. */
    suspend fun failedOf(card: MadeCard): List<String>? {
        val reply = ReplyText.of(
            llmEngine = engine,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = RULES
                ),
                ChatMessage(
                    role = ChatRole.USER,
                    text = "Sentence: ${card.sentence}\nQuestion: ${card.question}\nAnswer: ${card.answer}"
                )
            ),
            options = GenerationOptions(
                maxTokens = CARD_TOKENS,
                temperature = 0f,
                jsonSchema = CARD_SCHEMA
            )
        )

        val fields = json.fields(json = reply) ?: return null

        return RULE_NAMES.filter { (name, good) ->
            fields[name] != good
        }.map { it.first }
    }

    /** Which of [sentences] are worth a study card, by their place; null when the reply cannot be read. */
    suspend fun worthOf(sentences: List<String>): Set<Int>? {
        val ids = sentences.indices.map { "S${it + 1}" }

        val listed = sentences
            .mapIndexed { index, sentence -> "${ids[index]}: $sentence" }
            .joinToString(separator = "\n")

        val schema = "{\"type\":\"object\",\"properties\":{" +
            ids.joinToString(separator = ",") { "\"$it\":{\"enum\":[\"yes\",\"no\"]}" } +
            "},\"required\":[" + ids.joinToString(separator = ",") { "\"$it\"" } + "]}"

        val reply = ReplyText.of(
            llmEngine = engine,
            messages = listOf(
                ChatMessage(
                    role = ChatRole.SYSTEM,
                    text = WORTH
                ),
                ChatMessage(
                    role = ChatRole.USER,
                    text = listed
                )
            ),
            options = GenerationOptions(
                maxTokens = WORTH_TOKENS * ids.size + WORTH_TOKENS,
                temperature = 0f,
                jsonSchema = schema
            )
        )

        val fields = json.fields(json = reply) ?: return null

        return ids
            .indices
            .filter {
                fields[ids[it]] == "yes"
            }
            .toSet()
    }

    private companion object {
        const val CARD_TOKENS = 120

        // A sentence's line of the reply, "S12": "yes", as the model indents it: about ten tokens.
        const val WORTH_TOKENS = 16

        // Each rule and the reply that passes it.
        val RULE_NAMES = listOf(
            "memorable" to "yes",
            "guessable" to "no",
            "gap" to "no",
            "one_answer" to "yes",
            "standalone" to "yes"
        )

        val CARD_SCHEMA = "{\"type\":\"object\",\"properties\":{" +
            RULE_NAMES.joinToString(separator = ",") { "\"${it.first}\":{\"enum\":[\"yes\",\"no\"]}" } +
            "},\"required\":[" + RULE_NAMES.joinToString(separator = ",") { "\"${it.first}\"" } + "]}"

        const val RULES =
            "You judge a study flashcard made from a sentence of someone's notes. Reply with JSON, yes or no for " +
                "each:\n- memorable: it asks the sentence's main point, something a person would want to remember " +
                "(a fact, a figure, a name, a date, a definition, a rule), not a filler detail.\n- guessable: it can " +
                "be answered without having learned it: a yes or no question, the answer written in the question, " +
                "or only one sensible answer.\n- gap: it is the sentence copied with a word left out.\n" +
                "- one_answer: it has one right answer, the card's.\n- standalone: it is understood without seeing " +
                "the note (\"how much does it cost?\" is not; \"how much is the gas order?\" is)."

        const val WORTH =
            "You read the sentences of someone's note (S1, S2…). Say yes for a sentence worth a study flashcard: it " +
                "holds a fact, a figure, a name, a date, a definition or a rule the person would want to remember. " +
                "Say no for filler, a passing remark, a reminder to self, a link or a title. Reply with JSON: each " +
                "id with yes or no."
    }
}

/** A card as the cards' set made it: where it is from and what it asks. */
internal data class MadeCard(
    val vault: String,
    val note: String,
    val sentence: String,
    val question: String,
    val answer: String
) {
    fun line(): String = listOf(vault, note, sentence, question, answer)
        .joinToString(separator = "\t") { clean(it) }

    companion object {
        fun of(line: String): MadeCard? = line
            .split('\t')
            .takeIf {
                it.size == FIELDS
            }
            ?.let { MadeCard(it[0], it[1], it[2], it[3], it[4]) }

        private const val FIELDS = 5

        private fun clean(text: String): String = text.replace('\t', ' ').replace('\n', ' ')
    }
}

package app.notekea.chat.eval

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * `smoke-data/eval/answers.json`: the questions of the answer evaluation (3.4), started from the 504 of the model
 * tournament (`docs/spikes/2026-11-08-model-tournament.md`). Ghost generates serializers for the app's code only.
 */
object AnswerEvalSet {
    private const val SET_FILE = "../smoke-data/eval/answers.json"

    fun read(): List<AnswerQuestion> = ObjectMapper()
        .readTree(File(SET_FILE))
        .get("questions")
        .map { node ->
            AnswerQuestion(
                vault = node.get("vault").asText(),
                question = node.get("question").asText(),
                notes = node.get("notes").map { it.asText() },
                facts = node.get("facts").map { fact ->
                    fact.map { it.asText() }
                }
            )
        }
}

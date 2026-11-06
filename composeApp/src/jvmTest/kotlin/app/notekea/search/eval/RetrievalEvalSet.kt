package app.notekea.search.eval

import java.io.File

/**
 * `smoke-data/eval/retrieval.json` (`ROADMAP.md` 2.8): the questions a search must answer on the sample vaults, one
 * object a line. Ghost generates serializers for the app's code only, so these lines are read here.
 */
data class RetrievalEvalSet(
    val questions: List<RetrievalQuestion>
) {
    companion object {
        const val SAMPLE_VAULT = "Sample Vault"
        const val GHOST_VAULT = "Ghost Serializer"
        private const val SET_FILE = "../smoke-data/eval/retrieval.json"
        private const val VAULTS_FOLDER = "../smoke-data/vaults"
        private const val QUESTION_START = "  {"
        private val TEXT = Regex(pattern = "\"((?:[^\"\\\\]|\\\\.)*)\"")

        fun read(): RetrievalEvalSet = RetrievalEvalSet(
            questions = File(SET_FILE).readLines().filter { it.startsWith(prefix = QUESTION_START) }.map(::questionOf)
        )

        fun folderOf(
            vault: String
        ): File = File(VAULTS_FOLDER, vault).canonicalFile

        private fun questionOf(
            line: String
        ) = RetrievalQuestion(
            vault = textsOf(line = line, key = "vault", end = ", \"question\"").single(),
            question = textsOf(line = line, key = "question", end = ", \"notes\"").single(),
            notes = textsOf(line = line, key = "notes", end = ", \"heading\""),
            heading = textsOf(line = line, key = "heading", end = "}").singleOrNull()
        )

        private fun textsOf(
            line: String,
            key: String,
            end: String
        ): List<String> = TEXT.findAll(input = line.substringAfter("\"$key\":").substringBefore(end))
            .map { it.groupValues[1].replace(oldValue = "\\\"", newValue = "\"") }
            .toList()
    }
}

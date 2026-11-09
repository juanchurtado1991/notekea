package app.notekea.search.eval

import app.notekea.core.notes.domain.OutlineFinder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** The evaluation set stays true to the vaults it asks about, on every check, with no model involved. */
class RetrievalEvalSetTest {
    private val questions = RetrievalEvalSet.read().questions

    @Test
    fun `it holds 50 questions on the Sample Vault and 30 on the Ghost Serializer vault`() {
        assertEquals(
            expected = mapOf(
                RetrievalEvalSet.SAMPLE_VAULT to 50,
                RetrievalEvalSet.GHOST_VAULT to 30
            ),
            actual = questions.groupingBy { it.vault }.eachCount()
        )
    }

    @Test
    fun `every question is different, and every expected note exists`() {
        val missing = questions.flatMap { question ->
            question.notes.filterNot {
                noteOf(
                    question = question,
                    path = it
                ).isFile
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = missing
        )

        assertEquals(
            expected = questions.size,
            actual = questions
                .map { it.question }
                .distinct()
                .size
        )
    }

    @Test
    fun `every expected heading is in its note`() {
        val missing = questions.filter { question ->
            val heading = question.heading

            heading != null && question.notes.none { path ->
                val text = noteOf(
                    question = question,
                    path = path
                ).readText()

                OutlineFinder().headings(text = text).any {
                    it.text == heading
                }
            }
        }

        assertEquals(
            expected = emptyList(),
            actual = missing
        )
    }

    private fun noteOf(
        question: RetrievalQuestion,
        path: String
    ) = File(RetrievalEvalSet.folderOf(vault = question.vault), path)
}

package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCases
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** GUIDELINES §8: every expected value of the blind exams is checked against the vault it runs on. */
class BlindExamsTest {
    // The nine first exams and the 50 of the pool, by id, vault and cases.
    private val everyExam = BlindExams
        .all
        .map { Triple(it.id, it.vault, it.cases) }
        .map { (id, vault, cases) ->
            BlindPool.Exam(
                id = id,
                vault = vault,
                cases = cases,
                isSealed = false
            )
        } + BlindPool.all

    private fun textOf(vault: String): String = File("../smoke-data/vaults/$vault")
        .walkTopDown()
        .filter {
            it.extension == "md"
        }
        .joinToString(separator = "\n") { "${it.path}\n${it.readText()}" }
        .replace(
            oldValue = ",",
            newValue = ""
        )
        .lowercase()

    // As the check reads an answer: commas out and lower-cased, so "4,480" is "4480".
    private fun plain(text: String): String = text
        .replace(
            oldValue = ",",
            newValue = ""
        )
        .lowercase()

    @Test
    fun `what a blind task's answer must hold is written in its vault or in the task`() {
        assertEquals(
            expected = emptyList(),
            actual = everyExam.flatMap { exam ->
                val vault = textOf(vault = exam.vault)

                exam.cases.flatMap { case ->
                    case
                        .holds
                        .map {
                            it.replace(
                                oldValue = ",",
                                newValue = ""
                            ).lowercase()
                        }
                        .filterNot {
                            it in vault || it in plain(text = case.task)
                        }
                        .map { "${exam.id}: \"${case.task}\" holds \"$it\"" }
                }
            }
        )
    }

    @Test
    fun `every blind exam names tools of the design, asks back, and has every kind of task`() {
        assertEquals(
            expected = emptyList(),
            actual = everyExam.flatMap { exam ->
                val tools = exam.cases.flatMap { it.tools } - AgentWideCases.TOOLS

                val kinds = AgentWideCase.Kind.entries - exam
                    .cases
                    .map { it.kind }
                    .toSet()

                val ends = AgentWideCase.End.entries - exam
                    .cases
                    .map { it.ends }
                    .toSet()

                (tools + kinds + ends).map { "${exam.id}: $it" }
            }
        )
    }
}

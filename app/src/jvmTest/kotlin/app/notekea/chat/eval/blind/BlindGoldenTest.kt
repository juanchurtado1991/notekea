package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * The blind exams as the logged runs measured them: every exam's id, vault and seal, and each case's fields, in order
 * (`docs/code-review-2026-11-22.md`, block 7). `BlindPool.sampleOf` shuffles the list by its seed, so a case moved or
 * changed makes the runs no longer comparable: this file is the guard for any rewrite of their source.
 */
class BlindGoldenTest {
    private val golden = File("src/jvmTest/resources/eval/blind-exams.txt")

    private fun written(): String = buildString {
        (BlindExams.all + BlindPool.all).forEach {
            exam(
                id = it.id,
                vault = it.vault,
                isSealed = it.isSealed,
                cases = it.cases
            )
        }
    }

    private fun StringBuilder.exam(
        id: String,
        vault: String,
        isSealed: Boolean,
        cases: List<AgentWideCase>
    ) {
        appendLine("exam $id | $vault | sealed=$isSealed")

        cases.forEach {
            appendLine("  task=${it.task}")
            appendLine("    kind=${it.kind} ends=${it.ends} tools=${it.tools}")
            appendLine("    holds=${it.holds} computed=${it.computed} lacks=${it.lacks}")
        }
    }

    @Test
    fun `the blind exams are the ones the logged runs measured, case by case and in order`() {
        // Written once, from the exams as they were, and committed: a missing file is written and fails the run.
        if (!golden.exists()) {
            golden.parentFile.mkdirs()
            golden.writeText(written())
            fail(message = "${golden.path} was written: check it and commit it")
        }

        assertEquals(
            expected = golden.readText(),
            actual = written()
        )
    }
}

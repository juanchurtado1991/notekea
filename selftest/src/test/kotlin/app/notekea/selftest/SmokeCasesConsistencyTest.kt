package app.notekea.selftest

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Keeps `MVP_SMOKE_TEST.md` and the Self-Test together (`qa/SELF_TEST.md`): every case of the file is written in the
 * suite as its **Checked by** says, or still listed in `pending-cases.txt`; the suite has no case the file lacks.
 */
class SmokeCasesConsistencyTest {
    private val cases = SmokeCaseList.read(file = File(SMOKE_TEST_FILE))
    private val pending = File(PENDING_FILE).readLines().map(String::trim).filter {
        it.isNotEmpty() &&
            !it.startsWith("#")
    }
    private val suite = NotekeaSuite.suite
    private val automated = suite.automated.map { it.id }.toSet()
    private val manual = suite.manual.associate { it.id to "Manual (${it.reason.label})" }

    @Test
    fun `every case is written as its Checked by says, or still pending`() {
        val wrong = cases.filterNot { case ->
            val isWritten = if (case.checkedBy == SELF_TEST) case.id in automated else manual[case.id] == case.checkedBy
            isWritten || case.id in pending
        }
        assertEquals(emptyList(), wrong.map { "${it.id} (${it.checkedBy})" })
        println("Self-Test: ${cases.count { it.checkedBy == SELF_TEST }} of ${cases.size} cases are automatic")
    }

    @Test
    fun `the suite and the pending list name only cases of the file`() {
        val known = cases.map { it.id }.toSet()
        assertEquals(emptySet(), (automated + manual.keys + pending) - known)
    }

    @Test
    fun `a written case leaves the pending list`() {
        assertEquals(emptySet(), pending.toSet().intersect(automated + manual.keys))
        assertTrue(pending.size == pending.toSet().size, "pending-cases.txt names a case twice")
    }

    private companion object {
        const val SMOKE_TEST_FILE = "../MVP_SMOKE_TEST.md"
        const val PENDING_FILE = "pending-cases.txt"
        const val SELF_TEST = "Self-Test"
    }
}

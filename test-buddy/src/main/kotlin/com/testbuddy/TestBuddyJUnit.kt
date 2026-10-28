package com.testbuddy

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files

/**
 * Runs a suite's automated cases as JUnit 5 tests, one per case named after its ID, so CI runs the cases the Self-Test
 * app runs: `class NotekeaSuiteTest : TestBuddyJUnit(notekeaSuite)`.
 */
public abstract class TestBuddyJUnit(private val suite: Suite) {
    @TestFactory
    public fun automatedCases(): List<DynamicTest> {
        val runner = SuiteRunner(suite = suite, workFolder = Files.createTempDirectory(WORK_FOLDER_PREFIX))
        return suite.automated.map { case ->
            DynamicTest.dynamicTest("${case.id} ${case.title}") {
                val result = runner.run(case = case)
                if (result is CaseResult.Failed) throw AssertionError(failureMessage(result = result))
            }
        }
    }

    private fun failureMessage(result: CaseResult.Failed): String =
        result.message + (result.screenshot?.let { " (screenshot: $it)" } ?: "")

    private companion object {
        const val WORK_FOLDER_PREFIX = "test-buddy"
    }
}

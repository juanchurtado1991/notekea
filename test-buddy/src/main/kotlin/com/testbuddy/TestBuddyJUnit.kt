package com.testbuddy

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * Runs a suite's automated cases as JUnit 5 tests, one per case named after its ID, so CI runs the cases the Self-Test
 * app runs: `class NotekeaSuiteTest : TestBuddyJUnit(notekeaSuite)`.
 */
public abstract class TestBuddyJUnit(private val suite: Suite) {
    @TestFactory
    public fun automatedCases(): List<DynamicTest> {
        val runner = SuiteRunner(suite = suite, workFolder = workFolder())
        return suite.automated.map { case ->
            DynamicTest.dynamicTest("${case.id} ${case.title}") {
                val result = runner.run(case = case)
                if (result is CaseResult.Failed) throw AssertionError(failureMessage(result = result))
            }
        }
    }

    // `testbuddy.workFolder` keeps the work, screenshots of failures included, where CI uploads its reports.
    private fun workFolder(): Path =
        System.getProperty(WORK_FOLDER_PROPERTY)?.let { Files.createDirectories(Path.of(it)) }
            ?: Files.createTempDirectory(WORK_FOLDER_PREFIX)

    private fun failureMessage(result: CaseResult.Failed): String =
        result.message + (result.screenshot?.let { " (screenshot: $it)" } ?: "")

    private companion object {
        const val WORK_FOLDER_PREFIX = "test-buddy"
        const val WORK_FOLDER_PROPERTY = "testbuddy.workFolder"
    }
}

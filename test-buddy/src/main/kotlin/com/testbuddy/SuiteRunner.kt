package com.testbuddy

import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.time.TimeSource

/**
 * Runs a suite's automated cases one at a time, each on fresh folders under [workFolder] and in its own app, and
 * never stops at the first failure. A failed case keeps a screenshot in `screenshots/` of [workFolder]. Each
 * expectation waits up to [waitMillis] for the app, long enough for a slow machine.
 */
public class SuiteRunner(
    private val suite: Suite,
    private val workFolder: Path,
    private val system: HostSystem = HostSystem.current(),
    private val waitMillis: Long = DEFAULT_WAIT_MILLIS
) {
    private val workspace = CaseWorkspace(workFolder = workFolder, dataSources = suite.dataSources)

    /** Runs [cases] in order, telling [onResult] about each; once [shouldStop] says so, the rest are skipped. */
    public fun run(
        cases: List<AutomatedCase> = suite.automated,
        shouldStop: () -> Boolean = { false },
        onResult: (CaseResult) -> Unit = {}
    ): List<CaseResult> = cases.map { case ->
        val result = if (shouldStop()) CaseResult.Skipped(id = case.id) else run(case = case)
        result.also(onResult)
    }

    public fun run(case: AutomatedCase): CaseResult {
        val folders = workspace.prepare(caseId = case.id)
        val started = TimeSource.Monotonic.markNow()
        var screenshot: Path? = null
        val failure = runCatching {
            runDesktopComposeUiTest(
                width = suite.window.width,
                height = suite.window.height,
                testTimeout = case.timeout
            ) {
                setContent { suite.content(folders) }
                try {
                    case.body(CaseScope(ui = this, folders = folders, system = system, waitMillis = waitMillis))
                } catch (failure: Throwable) {
                    screenshot = capture(caseId = case.id)
                    throw failure
                }
            }
        }.exceptionOrNull()
        workspace.clean(caseId = case.id)
        val duration = started.elapsedNow()
        return if (failure == null) {
            CaseResult.Passed(id = case.id, duration = duration)
        } else {
            CaseResult.Failed(
                id = case.id,
                message = describe(failure = failure),
                duration = duration,
                screenshot = screenshot
            )
        }
    }

    private fun ComposeUiTest.capture(caseId: String): Path? = runCatching {
        val image = onAllNodes(matcher = isRoot()).onFirst().captureToImage().toAwtImage()
        workspace.screenshotFile(caseId = caseId).also { ImageIO.write(image, PNG, it.toFile()) }
    }.getOrNull()

    private fun describe(failure: Throwable): String = when (failure) {
        is CaseFailure -> failure.message.orEmpty()
        else -> "${failure::class.simpleName}: ${failure.message.orEmpty()}".trimEnd(':', ' ')
    }

    private companion object {
        const val PNG = "png"
        const val DEFAULT_WAIT_MILLIS = 10_000L
    }
}

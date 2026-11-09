package com.testbuddy

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import com.testbuddy.app.AppProcess
import com.testbuddy.app.AutoTestFolders
import com.testbuddy.app.ManagerSession
import com.testbuddy.app.ManagerWindow
import com.testbuddy.app.OutputCapture
import com.testbuddy.app.runAutomatedCases
import kotlin.system.exitProcess

/**
 * The `main` of an app's QA build (`test-buddy/README.md`). It runs the app as always ([runApp]) with the Test Buddy
 * manager as one more window. When the manager starts the automatic cases, the app starts again in a process of its own
 * with a flag, and this runs the cases there instead. What the tester does and the report are kept in [folders].
 */
public fun testBuddyMain(
    args: Array<String>,
    suite: Suite,
    folders: AutoTestFolders = AutoTestFolders.default(appName = suite.appName),
    runApp: (manager: @Composable ApplicationScope.() -> Unit) -> Unit
) {
    if (AppProcess.RUNNER_FLAG in args) {
        runAutomatedCases(
            suite = suite,
            workFolder = folders.work.resolve(RUNS)
        )

        exitProcess(status = 0)
    }

    folders.create()
    OutputCapture.start(file = folders.report.resolve(OUTPUT))

    val session = ManagerSession(
        suite = suite,
        folders = folders
    )

    runApp {
        ManagerWindow(
            suite = suite,
            session = session,
            folders = folders
        )
    }
}

private const val RUNS = "runs"
private const val OUTPUT = "output.txt"

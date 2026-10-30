package com.testbuddy

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import com.testbuddy.app.AppProcess
import com.testbuddy.app.ManagerSession
import com.testbuddy.app.ManagerWindow
import com.testbuddy.app.OutputCapture
import com.testbuddy.app.SelfTestFolders
import com.testbuddy.app.runAutomatedCases
import kotlin.system.exitProcess

/**
 * The `main` of an app's QA build (`test-buddy/README.md`): `fun main(args: Array<String>) = testBuddyMain(args,
 * suite) { manager -> runMyApp(extraWindows = manager) }`. It runs the app as always ([runApp]) with the Test Buddy
 * manager as one more window, which the app puts inside its `application { }`. When the manager starts the automatic
 * cases, the app starts again in a process of its own with a flag, and then this runs the cases there instead of the
 * app. What the tester does, the test data and the report are kept in [folders]; what the app prints is copied to
 * the Report folder's `output.txt`.
 */
public fun testBuddyMain(
    args: Array<String>,
    suite: Suite,
    folders: SelfTestFolders = SelfTestFolders.default(appName = suite.appName),
    runApp: (manager: @Composable ApplicationScope.() -> Unit) -> Unit
) {
    if (AppProcess.RUNNER_FLAG in args) {
        runAutomatedCases(suite = suite, workFolder = folders.work.resolve(RUNS))
        exitProcess(status = 0)
    }
    folders.create()
    OutputCapture.start(file = folders.report.resolve(OUTPUT))
    val session = ManagerSession(suite = suite, folders = folders)
    runApp { ManagerWindow(suite = suite, session = session, folders = folders) }
}

private const val RUNS = "runs"
private const val OUTPUT = "output.txt"

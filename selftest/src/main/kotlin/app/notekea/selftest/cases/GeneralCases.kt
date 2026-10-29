package app.notekea.selftest.cases

import app.notekea.selftest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_NOTEKEA = "Click the Open Notekea button"
private const val PROCESS_LIST =
    "the list of running apps (Windows: Task Manager, Ctrl+Shift+Esc; macOS: Activity Monitor, in Applications › " +
        "Utilities; Linux: System Monitor)"

/** The General cases of `MVP_SMOKE_TEST.md`: the installed app, the system and privacy. */
fun SuiteBuilder.generalCases() {
    installAndLaunch()
    cleanOutput()
    processExits()
    worksOffline()
    noTraffic()
    followsSystemTheme()
    crashNotice()
    launcherAndUpdate()
}

/** The last case of the Self-Test (G-09): free use, after every other case, to catch what no case thought of. */
fun SuiteBuilder.freeUseCase() {
    manual(id = "G-09", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Use Notekea freely for 15 minutes")
        answers(passed = "Nothing odd", failed = "I found something")
        steps(
            text = "1. $OPEN_NOTEKEA and open the Playground Vault from the Test data folder (Open folder…).\n2. For " +
                "15 minutes, use Notekea as you really would: write notes, link them, search, open and close notes " +
                "and tabs, open another vault and come back.\n3. Whenever something looks wrong, surprising or slow, " +
                "write down what you did and what you saw."
        )
        expected(
            text = "Nothing odd happens: no error, no lost text, nothing that looks wrong. If something did, choose " +
                "I found something and describe it, with a screenshot if you can."
        )
    }
}

private fun SuiteBuilder.installAndLaunch() {
    manual(id = "G-01", reason = ManualReason.PACKAGED) {
        title(text = "Notekea installs and opens like any app")
        steps(
            text = "1. Install Notekea from the file you were sent (msi on Windows, dmg on macOS, deb on Linux).\n2. " +
                "Open it from the system's launcher (the Start menu, Launchpad or the applications menu)."
        )
        expected(
            text = "Notekea opens. The system asks at most its usual question for an app opened the first time, and " +
                "nothing else."
        )
    }
}

private fun SuiteBuilder.cleanOutput() {
    manual(id = "G-02", reason = ManualReason.PACKAGED) {
        prepare(PLAYGROUND)
        title(text = "Notekea starts without errors")
        steps(
            text = "1. $OPEN_NOTEKEA. Open the Playground Vault from the Test data folder (Open folder…) and a " +
                "note.\n2. Close Notekea's window.\n3. In your Documents folder, open Notekea Self-Test, then " +
                "Report, and open the file G-02 output.txt."
        )
        expected(
            text = "The file has no line with \"Exception\" and no \"WARNING: A restricted method\". An empty file " +
                "is fine."
        )
    }
}

private fun SuiteBuilder.processExits() {
    manual(id = "G-03", reason = ManualReason.PACKAGED) {
        title(text = "Closing the window quits Notekea")
        steps(
            text = "1. $OPEN_NOTEKEA, then close its window.\n2. A few seconds later, look for Notekea in " +
                "$PROCESS_LIST."
        )
        expected(text = "Notekea is not in the list: it quit completely.")
    }
}

private fun SuiteBuilder.worksOffline() {
    manual(id = "G-04", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "Notekea works without the internet")
        steps(
            text = "1. Turn off Wi-Fi and unplug the network cable.\n2. $OPEN_NOTEKEA, open the Playground Vault " +
                "from the Test data folder (Open folder…), edit a note and search.\n3. Turn the network back on."
        )
        expected(text = "Everything works as with the internet; nothing waits or shows an error about the network.")
    }
}

private fun SuiteBuilder.noTraffic() {
    manual(id = "G-05", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "Notekea sends nothing over the network")
        steps(
            text = "1. Open an app that shows which apps use the network (Windows: Resource Monitor, Network tab; " +
                "macOS: Activity Monitor, Network tab; Linux: System Monitor or your firewall).\n2. $OPEN_NOTEKEA, " +
                "open the Playground Vault from the Test data folder and use it for 10 minutes."
        )
        expected(text = "Notekea sends and receives nothing: its row stays at zero.")
    }
}

private fun SuiteBuilder.followsSystemTheme() {
    manual(id = "G-06", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "Notekea follows the system's light and dark mode")
        steps(
            text = "1. $OPEN_NOTEKEA and open the Playground Vault from the Test data folder (Open folder…).\n2. In " +
                "the system's settings, switch between light and dark mode while Notekea is open."
        )
        expected(text = "Notekea turns light or dark at once, with the system, and every text reads well.")
    }
}

private fun SuiteBuilder.crashNotice() {
    manual(id = "G-07", reason = ManualReason.PACKAGED) {
        prepare(PLAYGROUND)
        title(text = "A crash shows a notice and keeps what you typed")
        launchWith(name = "NOTEKEA_CRASH_TEST", value = "1")
        steps(
            text = "1. $OPEN_NOTEKEA: this time it starts in a test mode that fails on purpose.\n2. Quickly open the " +
                "Playground Vault from the Test data folder, open a note and type a word, then wait.\n3. In the " +
                "window that appears, click Copy details, then Report on GitHub, then Close Notekea.\n4. Open " +
                "Notekea from the system's launcher and open the same note."
        )
        expected(
            text = "About 3 seconds after Notekea opens, a window \"Notekea needs to close\" in Notekea's style " +
                "names a report file. Copy details says \"Copied to the clipboard\"; Report on GitHub opens a Crash " +
                "report form in the browser; Close Notekea closes it. The word you typed is in the note."
        )
    }
}

private fun SuiteBuilder.launcherAndUpdate() {
    manual(id = "G-08", reason = ManualReason.PACKAGED) {
        prepare(PLAYGROUND)
        title(text = "Notekea is in the launcher, and installing again updates it")
        steps(
            text = "1. Look for Notekea in the system's launcher and open it from there.\n2. Open the Playground " +
                "Vault from the Test data folder (Open folder…), open a note and search for \"plan\" (the magnifier " +
                "on the left).\n3. On Windows only: close Notekea and run the installer again."
        )
        expected(
            text = "Notekea is in the launcher with its logo (Windows: the Start menu and a desktop shortcut; macOS: " +
                "Applications and Launchpad; Linux: the applications menu, under Office). The vault opens and the " +
                "search lists results, with no crash notice. On Windows, installing again leaves one Notekea in " +
                "Installed apps."
        )
    }
}

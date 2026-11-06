package app.notekea.selftest.cases.m2

import app.notekea.selftest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val OPEN_TASKS =
    "1. In Notekea, open the Sample Vault (see TEST DATA above).\n2. Click Tasks in the rail on the left."

/** The cases of 2.11, vault tasks (`MVP_SMOKE_TEST.md`): the Tasks tab. */
fun SuiteBuilder.vaultTaskCases() {
    tasksTab()
    groupsAndLimits()
    checkingATask()
    changedOutside()
    openingATask()
}

private fun SuiteBuilder.tasksTab() {
    automated(id = "2.11-01", title = "The Tasks tab lists the vault's tasks by note") {
        openVault(name = SAMPLE, shownText = "Home")
        clickIcon(label = "Tasks")
        expectText(text = "By note")
        expectText(text = "Record a demo video")
    }
}

private fun SuiteBuilder.groupsAndLimits() {
    manual(id = "2.11-02", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Tasks group by date and are limited by folder and done")
        steps(text = "$OPEN_TASKS\n3. Click By date.\n4. Click Show done.\n5. Click Folder and choose Projects.")
        expected(
            text = "Step 3 groups the tasks by date, with Write the announcement and Book the flights dated, and No " +
                "date holding the rest. Step 4 adds a Done group at the end. Step 5 keeps only the tasks of " +
                "Projects, and the foot counts them."
        )
    }
}

private fun SuiteBuilder.checkingATask() {
    manual(id = "2.11-03", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A task checked in the tab or in its note shows in the other")
        steps(
            text = "$OPEN_TASKS\n3. Check Record a demo video.\n4. Open Projects/Notekea launch.\n5. Uncheck that " +
                "task there, and go back to the Tasks tab."
        )
        expected(text = "It leaves the list at once and its note shows it checked. After step 5 it is listed again.")
    }
}

private fun SuiteBuilder.changedOutside() {
    manual(id = "2.11-04", reason = ManualReason.SYSTEM) {
        prepare(SAMPLE)
        title(text = "A task changed outside Notekea updates in the tab")
        steps(text = "$OPEN_TASKS\n3. Change a task's text in another editor, and save.")
        expected(text = "The tab shows the new text within a second.")
    }
}

private fun SuiteBuilder.openingATask() {
    manual(id = "2.11-05", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "A task's text opens its note at the task")
        steps(text = "$OPEN_TASKS\n3. Click the text of Find a flat near the river.")
        expected(text = "Projects/Trip to Lisbon opens with that task selected.")
    }
}

package app.notekea.autotest.cases.m2

import app.notekea.autotest.clickLast
import app.notekea.autotest.expectLineSelected
import app.notekea.autotest.expectOrderOnScreen
import app.notekea.autotest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val TASKS = "Tasks"
private const val DEMO_TASK = "Record a demo video"
private const val FLAT_TASK = "Find a flat near the river"
private const val LAUNCH_FILE = "$SAMPLE/Projects/Notekea launch.md"
private const val OPEN_TASKS =
    "1. In Notekea, open the Sample Vault (see TEST DATA above).\n2. Click Tasks in the rail on the left."

/** The cases of 2.11, vault tasks (`MVP_SMOKE_TEST.md`): the Tasks tab. */
fun SuiteBuilder.vaultTaskCases() {
    tasksTab()
    groupsAndLimits()
    checkingATask()
    changedOutside()
    openingATask()
    cardsLook()
}

private fun SuiteBuilder.tasksTab() {
    automated(
        id = "2.11-01",
        title = "The Tasks tab lists the vault's tasks by note"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = TASKS)
        expectText(text = "By note")
        // The notes are cards in a lazy list: the one further down is made once the list is scrolled to it.
        scrollTo(text = "Record a demo video")
        expectText(text = "Record a demo video")
    }
}

private fun SuiteBuilder.groupsAndLimits() {
    automated(
        id = "2.11-02",
        title = "Tasks group by date and are limited by folder and done"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = TASKS)
        click(text = "By date")

        // Write the announcement is dated 2026-10-30, so it stays overdue; Book the flights moves with today.
        expectOrderOnScreen(
            texts = listOf("Overdue", "Write the announcement 2026-10-30", "No date")
        )

        expectNoText(text = "A done task")
        click(text = "Show done")
        scrollTo(text = "A done task")
        expectText(text = "Done")
        click(text = "Folder")
        clickLast(text = "Projects")
        expectText(text = "4 open of 5 tasks")
        expectText(text = "Finish the editor")
        expectNoText(text = "An open task")
    }
}

private fun SuiteBuilder.checkingATask() {
    automated(
        id = "2.11-03",
        title = "A task checked or unchecked in the tab is written to its note"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = TASKS)
        // Only its folder, so its card is on screen whatever the vault holds above it.
        click(text = "Folder")
        clickLast(text = "Projects")
        clickIcon(label = "Task: $DEMO_TASK")
        expectNoText(text = DEMO_TASK)

        expectFile(
            path = LAUNCH_FILE,
            containing = "- [x] $DEMO_TASK"
        )

        click(text = "Show done")
        clickIcon(label = "Task: $DEMO_TASK")

        expectFile(
            path = LAUNCH_FILE,
            containing = "- [ ] $DEMO_TASK"
        )

        click(text = "Hide done")
        expectText(text = DEMO_TASK)
    }
}

private fun SuiteBuilder.changedOutside() {
    manual(
        id = "2.11-04",
        reason = ManualReason.SYSTEM
    ) {
        prepare(SAMPLE)
        title(text = "A task changed outside Notekea updates in the tab")
        steps(text = "$OPEN_TASKS\n3. Change a task's text in another editor, and save.")
        expected(text = "The tab shows the new text within a second.")
    }
}

private fun SuiteBuilder.openingATask() {
    automated(
        id = "2.11-05",
        title = "A task's text opens its note at the task"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = TASKS)
        scrollTo(text = FLAT_TASK)
        click(text = FLAT_TASK)
        expectLineSelected(line = FLAT_TASK)
    }
}

private fun SuiteBuilder.cardsLook() {
    manual(
        id = "2.11-06",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "The Tasks tab shows notes and dates as cards")

        steps(
            text =
            "1. In Notekea, open the Sample Vault (see TEST DATA above).\n2. Click Tasks in the rail on the left " +
                "and look at the tab.\n3. Click Show done, then By date."
        )

        expected(
            text =
            "Each note is a card on a soft background: its icon in its folder's color, its name, its folder " +
                "and, at the card's end, a bar with \"N of M done\"; under it its tasks, a late date in red. By " +
                "date each group is a card with its count: Overdue in red, Today in blue, and each task names " +
                "its note."
        )
    }
}

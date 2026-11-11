package app.notekea.autotest.cases.m2

import app.notekea.autotest.clickLast
import app.notekea.autotest.expectLineSelected
import app.notekea.autotest.expectOrderOnScreen
import app.notekea.autotest.openVault
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val SAMPLE = "Sample Vault"
private const val HOME = "Home"
private const val TASKS = "Tasks"
private const val DEMO_TASK = "Record a demo video"
private const val EDITED_TASK = "Record a short demo video"
private const val SCROLL_TRIES = 10
private const val SCROLL_WAIT_MILLIS = 500L
private const val FLAT_TASK = "Find a flat near the river"
private const val LAUNCH_FILE = "$SAMPLE/Projects/Notekea launch.md"
private const val OPEN_TASKS =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba).\n2. Haz clic en Tareas en la barra de la izquierda."

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
    automated(
        id = "2.11-04",
        title = "A task changed outside Notekea shows its new text in the Tasks tab"
    ) {
        openVault(
            name = SAMPLE,
            shownText = HOME
        )

        clickIcon(label = TASKS)
        scrollTo(text = DEMO_TASK)

        // Another editor saves the note: the tab follows the vault's files without a click.
        val launch = file(path = LAUNCH_FILE)
        Files.writeString(launch, Files.readString(launch).replace(DEMO_TASK, EDITED_TASK))

        // The vault's watcher tells the tab a moment after the file is written.
        awaitScrolledTo(text = EDITED_TASK)
        expectNoText(text = DEMO_TASK)
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
        title(text = "La pestaña Tareas muestra notas y fechas como tarjetas")

        steps(
            text =
            "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba).\n2. Haz clic " +
                "en Tareas en la barra de la izquierda y mira la pestaña.\n3. Haz clic en " +
                "Mostrar terminadas y luego en Por fecha."
        )

        expected(
            text =
            "Cada nota es una tarjeta sobre un fondo suave: su icono en el color de su " +
                "carpeta, su nombre, su carpeta y, al final de la tarjeta, una barra con \"N de " +
                "M terminadas\"; debajo, sus tareas, con una fecha vencida en rojo. Por fecha, " +
                "cada grupo es una tarjeta con su cuenta: Vencidas en rojo, Hoy en azul, y cada " +
                "tarea nombra su nota."
        )
    }
}

/** Scrolls the tab to [text], trying again while the change that brings it is on its way. */
private fun CaseScope.awaitScrolledTo(text: String) {
    repeat(times = SCROLL_TRIES - 1) {
        try {
            return scrollTo(text = text)
        } catch (_: AssertionError) {
            settle(millis = SCROLL_WAIT_MILLIS)
        }
    }

    scrollTo(text = text)
}

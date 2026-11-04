package app.notekea.selftest.cases.m1

import app.notekea.selftest.notekea
import com.testbuddy.CaseFailure
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val EXIT_IN_FILE = "File ends with Settings… and Exit, and Help with About Notekea."
private const val APP_MENU =
    "The Notekea menu has About Notekea, Settings… and Quit Notekea, and each opens or does what it says, the About " +
        "and Settings of the tool rail; File has no Exit, and Help has Keyboard shortcuts."

/** The cases of 1.1 about the menu bar (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.menuCases() {
    menuCommands()
    menuBars()
}

private fun SuiteBuilder.menuCommands() {
    automated(id = "1.1-08", title = "Every command of the menu bar works with a note open") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_FLOUR)
        ui.runOnIdle {
            val app = notekea()
            val missing = app.menuCommands().filterNot { app.shortcuts.isAvailable(shortcut = it.shortcut) }
            if (missing.isNotEmpty()) throw CaseFailure(message = "no screen offers ${missing.map { it.shortcut }}")
        }
    }
}

private fun SuiteBuilder.menuBars() {
    menuBar(id = "1.1-09", system = "Windows", bar = "in the window, under its title", exit = EXIT_IN_FILE)
    menuBar(id = "1.1-10", system = "macOS", bar = "at the top of the screen", exit = APP_MENU)
    menuBar(id = "1.1-11", system = "Linux", bar = "in the window, under its title", exit = EXIT_IN_FILE)
}

private fun SuiteBuilder.menuBar(
    id: String,
    system: String,
    bar: String,
    exit: String
) {
    manual(id = id, reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "The menu bar on $system")
        steps(
            text = "Only on $system; on another system mark it passed.\n1. In Notekea, open the Playground Vault " +
                "(see TEST DATA above) and the note Welcome.\n2. Open each menu of the bar $bar.\n3. Click File → " +
                "New note, then View → Zoom in and View → Actual size.\n4. Open the last menu, Help, and click its " +
                "entry; press Esc.\n5. Open another vault with the tool rail and look at the menus on the welcome " +
                "screen."
        )
        expected(
            text = "The bar has File, Edit, View, Window and Help; each entry shows its shortcut as this system " +
                "writes it. $exit Step 3 creates a note and zooms the window and back, once each. Help opens About " +
                "Notekea. On the welcome screen the commands of a note or a vault are dimmed and do nothing, and " +
                "Open folder… and the zoom work."
        )
    }
}

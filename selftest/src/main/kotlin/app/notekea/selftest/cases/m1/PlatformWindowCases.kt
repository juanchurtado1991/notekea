package app.notekea.selftest.cases.m1

import app.notekea.selftest.notekea
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val TITLE_WAIT_MILLIS = 10_000L

/** The cases of 1.1 about the window itself (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.windowCases() {
    windowTitle()
    windowKept()
}

private fun SuiteBuilder.windowTitle() {
    automated(id = "1.1-12", title = "The window's title names the active note and the vault") {
        openPlaygroundNote(name = "Arepas", shownText = AREPAS_FLOUR)
        ui.waitUntil(timeoutMillis = TITLE_WAIT_MILLIS) {
            notekea().windowTitle == "Arepas — $PLAYGROUND — Notekea"
        }
    }
}

private fun SuiteBuilder.windowKept() {
    manual(id = "1.1-13", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "The window comes back as it was left, and narrows well")
        steps(
            text = "1. In Notekea, open the Playground Vault (see TEST DATA above) and the note Welcome.\n2. Make " +
                "the window as narrow and as short as it goes.\n3. Make it about 700 wide; show Files with the top " +
                "icon of the narrow bar if it is hidden.\n4. Move it to a corner, quit Notekea and start it again " +
                "from Notekea QA.\n5. Maximize it, quit and start it again, then unmaximize it."
        )
        expected(
            text = "In step 2 the window stops at a size where a note still fits. In step 3 Files floats over the " +
                "note, with a shadow, instead of squeezing it. After step 4 the window is in the same place at the " +
                "same size. After step 5 it starts maximized and goes back to that size. The title always starts " +
                "with the note's name."
        )
    }
}

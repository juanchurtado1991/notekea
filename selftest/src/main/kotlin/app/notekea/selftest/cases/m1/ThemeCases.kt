package app.notekea.selftest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open another vault at the bottom left if a vault is open)."

/** The cases of 1.2, the theme and the design system (`MVP_SMOKE_TEST.md`): all of them need a person's eyes. */
fun SuiteBuilder.themeCases() {
    fonts()
    lightAndDark()
    roundedCorners()
    keyboardFocus()
    screenReader()
    noDeveloperTools()
    narrowWindow()
}

private fun SuiteBuilder.fonts() {
    manual(id = "1.2-01", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "The app's fonts")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Open the note Editor › Read mode and look at its text and at its code blocks."
        )
        expected(
            text = "The text uses Notekea's own clean font (Inter), not your system's usual font; the code uses a " +
                "monospaced font where every letter has the same width (JetBrains Mono)."
        )
    }
}

private fun SuiteBuilder.lightAndDark() {
    manual(id = "1.2-02", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Colors in light and dark mode")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Look around, then switch your system to dark mode (or light, if it is " +
                "dark) and look again."
        )
        expected(
            text = "Blue is the main color and orange marks small details. In both modes every text is easy to read."
        )
    }
}

private fun SuiteBuilder.roundedCorners() {
    manual(id = "1.2-03", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Rounded corners")
        steps(text = "$OPEN_PLAYGROUND\n2. Look at the buttons, the text fields and the rows of the list on the left.")
        expected(text = "They have small rounded corners, not round pill shapes.")
    }
}

private fun SuiteBuilder.keyboardFocus() {
    manual(id = "1.2-04", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Moving through the window with the keyboard")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Without the mouse, press Tab several times, then Shift+Tab, and press " +
                "Enter or Space on a button."
        )
        expected(text = "Each time, a clear ring shows which element has the keyboard, and Enter or Space uses it.")
    }
}

private fun SuiteBuilder.screenReader() {
    manual(id = "1.2-05", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND)
        title(text = "A screen reader reads the window")
        steps(
            text = "1. Turn on your system's screen reader: Narrator (Ctrl+Win+Enter) on Windows, VoiceOver (Cmd+F5) " +
                "on macOS, Orca on Linux.\n2. Open the Playground Vault in Notekea and move through it with Tab.\n" +
                "3. Turn the screen reader off."
        )
        expected(
            text = "Buttons, tabs and rows are read aloud with their name and what they are (\"button\", \"tab\")."
        )
    }
}

private fun SuiteBuilder.noDeveloperTools() {
    manual(id = "1.2-06", reason = ManualReason.PACKAGED) {
        title(text = "No developer tools in the installed app")
        steps(text = "1. In Notekea, look through About, Settings and the right-click menus.")
        expected(text = "Nothing offers a design gallery or a developer screen.")
    }
}

private fun SuiteBuilder.narrowWindow() {
    manual(id = "1.2-07", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "A very narrow window")
        steps(text = "$OPEN_PLAYGROUND\n2. Open a few notes, then make the window as narrow as it goes.")
        expected(
            text = "The labels of the bottom bar and of the tabs stay on one line and end with … when they do not " +
                "fit; numbers are never cut in two; nothing is cut off."
        )
    }
}

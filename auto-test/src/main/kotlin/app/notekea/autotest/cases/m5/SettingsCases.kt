package app.notekea.autotest.cases.m5

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val LANGUAGE = "Language"
private const val OPEN_LAST = "Open the last vault at startup"

/**
 * The cases of 5.2, Settings as macOS's System Settings (`MVP_SMOKE_TEST.md`, the developer's choice of 2026-11-11):
 * Mod+, opens it, General's last vault, Files written to the vault's `.obsidian`, and the look, by eye.
 */
fun SuiteBuilder.settingsCases() {
    lastVault()
    filesInObsidian()
    look()
}

private fun SuiteBuilder.lastVault() {
    automated(
        id = "5.2-01",
        title = "Mod+, opens Settings on General, and turning off the last vault starts on the welcome screen"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        pressInWindow(shortcut = Mod + Key.Comma)
        expectText(text = LANGUAGE)
        clickIcon(label = OPEN_LAST)
        relaunch()
        expectText(text = "Open folder…")
        expectNoText(text = WELCOME)
    }
}

private fun SuiteBuilder.filesInObsidian() {
    automated(
        id = "5.2-02",
        title = "Settings → Files writes the daily note's date format to the vault's .obsidian, as Obsidian keeps it"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        pressInWindow(shortcut = Mod + Key.Comma)
        click(text = "Files")
        click(text = "YYYY-MM-DD")
        type(text = "DD-MM-YYYY")

        expectFile(
            path = "$PLAYGROUND/.obsidian/daily-notes.json",
            containing = "\"format\": \"DD-MM-YYYY\""
        )
    }
}

private fun SuiteBuilder.look() {
    manual(
        id = "5.2-03",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Settings looks like macOS's System Settings, and the editor follows Text size and Line width")

        steps(
            text =
            "1. Open the Playground Vault and a long note; press Ctrl+, (⌘, on macOS).\n2. Look at every section " +
                "in the sidebar.\n3. In Editor choose Text size 20 pt and Line width Readable, then Full width; look " +
                "at the note behind.\n4. Set Text size back to 16 pt and Line width to Wide."
        )

        expected(
            text =
            "Step 2: a sidebar with a colored square icon per section; each section's settings in rounded groups, " +
                "the name on the left and the control on the right, a short note under some groups. Step 3: the " +
                "note's text grows, headings and code with it, and its lines go to a narrow column, then the whole " +
                "pane. Step 4 brings it back as it was."
        )
    }
}

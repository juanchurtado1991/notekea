package app.notekea.autotest.cases.m2

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import app.notekea.autotest.relaunch
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "1. In Notekea, open the Playground Vault (see TEST DATA above)."
private const val OPEN_APPEARANCE =
    "Click Settings (the gear at the bottom of the narrow bar on the far left)"
private const val THEME = "Sage"
private const val SELECTED_MILLIS = 10_000L

/** The cases of 2.12, color themes (`MVP_SMOKE_TEST.md`): Settings → Appearance's mode and themes. */
fun SuiteBuilder.appearanceCases() {
    keptChoice()
    startingChoice()
    themesLook()
    systemMode()
    reduceMotion()
}

private fun SuiteBuilder.keptChoice() {
    automated(
        id = "2.12-01",
        title = "A color theme and a mode chosen in Settings are kept after a relaunch"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        scrollTo(text = THEME)
        click(text = THEME)
        click(text = "Dark")
        expectChosen(text = THEME)
        press(key = Key.Escape)
        relaunch()
        expectText(text = WELCOME)
        clickIcon(label = "Settings")
        expectChosen(text = THEME)
        expectChosen(text = "Dark")
    }
}

private fun SuiteBuilder.startingChoice() {
    automated(
        id = "2.12-04",
        title = "Before anything is chosen Notekea is in Workspace Blue, following the system"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        expectChosen(text = "Workspace Blue")
        expectChosen(text = "System")
    }
}

private fun SuiteBuilder.reduceMotion() {
    manual(
        id = "2.12-05",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Reduce motion")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE, tick Reduce motion and press Esc.\n3. Press the sidebar's " +
                "shortcut (Ctrl+\\, Cmd+\\ on macOS) twice, open a folder in Files and switch the side panel between " +
                "Inspector and Chat.\n4. $OPEN_APPEARANCE, untick Reduce motion and do step 3 again.\n5. Turn on the " +
                "system's own setting (Reduce motion on macOS, Animation effects off on Windows, Animations off on " +
                "GNOME), start Notekea again and do step 3."
        )

        expected(
            text =
            "In step 3 everything changes at once, nothing slides or fades. In step 4 panels slide and rows " +
                "fade as usual. In step 5 they change at once again, with the box in Settings unticked; on Linux " +
                "desktops that do not share this setting they slide."
        )
    }
}

private fun SuiteBuilder.themesLook() {
    manual(
        id = "2.12-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Every color theme in light and dark")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE.\n3. Under Mode click Light, then click each theme's card " +
                "and look at the window behind the dialog.\n4. Under Mode click Dark and click each card again.\n5. " +
                "Press Tab until a card has a ring around it and press Enter.\n6. Choose Workspace Blue and System again."
        )

        expected(
            text =
            "Each click changes the whole window at once (the list on the left, the tabs, the note and the " +
                "logo) to the colors of the card, and every text stays easy to read. The chosen card is outlined and " +
                "checked. Enter chooses the card with the ring, as a click does."
        )
    }
}

private fun SuiteBuilder.systemMode() {
    manual(
        id = "2.12-03",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "System follows the computer's light or dark setting")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE and choose a theme other than Workspace Blue.\n3. With Mode on " +
                "System, switch your computer between light and dark mode.\n4. Click Light under Mode and switch " +
                "your computer again.\n5. Choose Workspace Blue and System again, and set your computer back."
        )

        expected(
            text =
            "On System, Notekea turns light or dark with the computer, in the chosen theme. On Light it stays " +
                "light whatever the computer does."
        )
    }
}

/** Waits until the card or segment showing [text] is the chosen one. */
private fun CaseScope.expectChosen(text: String) {
    ui
        .waitUntil(timeoutMillis = SELECTED_MILLIS) {
            ui
                .onAllNodes(
                    matcher =
                    isSelected() and
                        (hasText(text = text) or hasAnyDescendant(matcher = hasText(text = text)))
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
}

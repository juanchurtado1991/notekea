package app.notekea.autotest.cases.m5

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.StandInDocsSite
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val NOTE = "$PLAYGROUND/Web sources/Page 4.md"

/**
 * Settings → General, Web sources (the developer's requests of 2026-11-13 and 2026-11-14): chosen every week, a
 * clipped note whose page changed is rewritten on its own the next time the vault opens, with no dialog; Update now
 * does it at once.
 */
internal fun SuiteBuilder.webUpdateSettingCases() {
    automated(
        id = "5.2-06",
        title = "Update web sources every week, in Settings, updates a clipped note when the vault opens again"
    ) {
        val page = StandInDocsSite.ADDRESS.replace(
            oldValue = "intro",
            newValue = "page-4"
        )

        Files.createDirectories(file(path = "$PLAYGROUND/Web sources"))

        Files.writeString(
            file(path = NOTE),
            "---\nsource: \"$page\"\nclipped: 2026-10-01\n---\n\n# Page 4\n\n## Original text\n\nAs it was.\n"
        )

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        pressInWindow(shortcut = Mod + Key.Comma)
        expectText(text = "Update on their own")
        click(text = "Never")
        click(text = "Every week")
        press(key = Key.Escape)
        relaunch()

        expectFile(
            path = NOTE,
            containing = "Page 4 explains one part of the guide"
        )

        expectNoText(text = "Reading page")
    }

    automated(
        id = "5.2-07",
        title = "Update now, in Settings, updates the web sources at once and says when they were last updated"
    ) {
        val page = StandInDocsSite.ADDRESS.replace(
            oldValue = "intro",
            newValue = "page-4"
        )

        Files.createDirectories(file(path = "$PLAYGROUND/Web sources"))

        Files.writeString(
            file(path = NOTE),
            "---\nsource: \"$page\"\nclipped: 2026-10-01\n---\n\n# Page 4\n\n## Original text\n\nAs it was.\n"
        )

        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        pressInWindow(shortcut = Mod + Key.Comma)
        expectText(text = "Web sources")
        expectText(text = "Never")
        click(text = "Update now")

        expectFile(
            path = NOTE,
            containing = "Page 4 explains one part of the guide"
        )

        expectText(text = "Today")
    }
}

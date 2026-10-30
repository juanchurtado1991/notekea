package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.platform.domain.SystemEnvironment
import app.notekea.selftest.openVault
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files
import java.nio.file.Path

/** The cases of 1.1, platform conventions (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.platformCases() {
    quitShortcut()
    shortcutModifier()
    dialogButtonOrder()
    appDataFolder()
    xdgDataHome()
}

private fun SuiteBuilder.quitShortcut() {
    manual(id = "1.1-01", reason = ManualReason.PACKAGED) {
        title(text = "Quitting Notekea with the system's shortcut")
        steps(
            text =
            "1. Click in Notekea's window.\n2. Press the quit shortcut of your system: Alt+F4 on Windows, Cmd+Q on " +
                "macOS, Ctrl+Q on Linux.\n3. Open Notekea QA again from the system's launcher; this window comes back " +
                "on this case.\n4. On macOS, open the Notekea menu at the top left of the screen."
        )
        expected(
            text = "Notekea closes at once, with this window. On macOS the Notekea menu ends with \"Quit Notekea\" and ⌘Q."
        )
    }
}

private fun SuiteBuilder.shortcutModifier() {
    automated(id = "1.1-02", title = "Shortcuts use Cmd on macOS and Ctrl elsewhere") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        click(text = WELCOME)
        press(shortcut = Mod + Key.N)
        expectFile(path = "$PERSONAL_NOTES/Untitled.md")
    }
}

private fun SuiteBuilder.dialogButtonOrder() {
    automated(id = "1.1-03", title = "Dialog buttons in the system's order") {
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        click(text = WELCOME)
        pressDelete()
        expectText(text = "Delete note")
        val isPrimaryFirst = left(text = "Delete note") < left(text = "Cancel")
        if (isPrimaryFirst != (system == HostSystem.WINDOWS)) {
            throw CaseFailure(
                message = "expected Delete note ${if (system == HostSystem.WINDOWS) "before" else "after"} Cancel"
            )
        }
        click(text = "Cancel")
        expectFile(path = "$PERSONAL_NOTES/Welcome.md")
    }
}

private fun SuiteBuilder.appDataFolder() {
    automated(id = "1.1-04", title = "The app's data goes to the system's app data folder") {
        val expected = when (system) {
            HostSystem.MACOS -> "/Library/Application Support/Notekea"
            HostSystem.WINDOWS -> "\\Notekea"
            HostSystem.LINUX -> "/notekea"
        }
        val resolved = ResolvePlatformConventionsUseCase(systemEnvironment = JvmSystemEnvironment())().appDataDirectory
        if (!resolved.endsWith(
                expected
            )
        ) {
            throw CaseFailure(message = "expected the app data folder to end with $expected, it is $resolved")
        }
        val before = filesIn(folder = folders.data.resolve(PERSONAL_NOTES))
        openVault(name = PERSONAL_NOTES, shownText = WELCOME)
        expectAppDataFile()
        if (filesIn(folder = folders.data.resolve(PERSONAL_NOTES)) != before) {
            throw CaseFailure(message = "expected nothing written inside the vault")
        }
    }
}

private fun SuiteBuilder.xdgDataHome() {
    automated(id = "1.1-05", title = "On Linux, XDG_DATA_HOME moves the app data folder") {
        val environment = XdgEnvironment(xdgDataHome = "/tmp/xdg-test")
        val resolved = ResolvePlatformConventionsUseCase(systemEnvironment = environment)().appDataDirectory
        if (system == HostSystem.LINUX && resolved != "/tmp/xdg-test/notekea") {
            throw CaseFailure(message = "expected /tmp/xdg-test/notekea, it is $resolved")
        }
    }
}

private fun CaseScope.pressDelete() {
    if (system == HostSystem.MACOS) press(shortcut = Mod + Key.Backspace) else press(key = Key.Delete)
}

private fun CaseScope.left(text: String): Float =
    ui.onAllNodes(matcher = hasText(text = text)).onFirst().fetchSemanticsNode().boundsInRoot.left

private fun CaseScope.expectAppDataFile() {
    ui.waitUntil(timeoutMillis = 10_000) { Files.list(folders.appData).use { it.findAny().isPresent } }
}

private fun filesIn(folder: Path): Set<String> = Files.walk(folder).use { paths ->
    paths.map { folder.relativize(it).toString() }.toList().toSet()
}

/** The machine as JvmSystemEnvironment reads it, with XDG_DATA_HOME set. */
private class XdgEnvironment(private val xdgDataHome: String) : SystemEnvironment by JvmSystemEnvironment() {
    override val operatingSystemName: String = "Linux"

    override fun environmentVariable(name: String): String? =
        if (name == "XDG_DATA_HOME") xdgDataHome else JvmSystemEnvironment().environmentVariable(name = name)
}

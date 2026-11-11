package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import app.notekea.autotest.closeWindowWith
import app.notekea.autotest.openVault
import app.notekea.autotest.typeAtEnd
import app.notekea.core.platform.data.JvmSystemEnvironment
import app.notekea.core.platform.domain.ResolvePlatformConventionsUseCase
import app.notekea.core.platform.domain.SystemEnvironment
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Alt
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import java.nio.file.Files
import java.nio.file.Path

/** The cases of 1.1, platform conventions (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.platformCases() {
    quitShortcut()
    quitSavesTheNote()
    closeWindowShortcuts()
    menuCases()
    windowCases()
    shortcutModifier()
    dialogButtonOrder()
    appDataFolder()
    xdgDataHome()
}

private fun SuiteBuilder.quitShortcut() {
    manual(
        id = "1.1-01",
        reason = ManualReason.PACKAGED
    ) {
        title(text = "Salir de Notekea con el atajo del sistema")

        steps(
            text =
            "1. Haz clic en la ventana de Notekea.\n2. Pulsa el atajo para salir de tu " +
                "sistema: Alt+F4 en Windows, Cmd+Q en macOS, Ctrl+Q en Linux.\n3. Abre de nuevo " +
                "Notekea QA desde el lanzador del sistema; esta ventana vuelve a este caso.\n4. " +
                "En macOS, abre el menú Notekea arriba a la izquierda de la pantalla."
        )

        expected(
            text =
            "Notekea se cierra al instante, con esta ventana. En macOS el menú Notekea " +
                "termina con la opción para salir de Notekea y ⌘Q."
        )
    }
}

private fun SuiteBuilder.quitSavesTheNote() {
    manual(
        id = "1.1-06",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Salir desde el sistema conserva lo que se acaba de escribir")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba) y la nota " +
                "Welcome.\n2. Escribe unas palabras al final de la nota y, en menos de un " +
                "segundo, termina Notekea: en macOS pulsa Cmd+Q; en Windows y Linux termínalo " +
                "desde una terminal (taskkill /PID <su id>, o kill <su id>), sin cerrar antes su " +
                "ventana.\n3. Abre de nuevo Notekea QA desde el lanzador del sistema; esta " +
                "ventana vuelve a este caso.\n4. Abre el Welcome.md de la Playground Vault en un " +
                "editor de texto."
        )

        expected(
            text =
            "Notekea se cierra. Las palabras escritas en el paso 2 están en Welcome.md, " +
                "ninguna se perdió."
        )
    }
}

private fun SuiteBuilder.closeWindowShortcuts() {
    automated(
        id = "1.1-07",
        title = "Close window and Quit save the note and close the window"
    ) {
        openPlaygroundNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )

        typeAtEnd(
            part = AREPAS_FLOUR,
            text = "\nClosed with the keyboard."
        )

        closeWindowWith(shortcut = Mod + Shift + Key.W)

        expectFile(
            path = "$PLAYGROUND/Recipes/Arepas.md",
            containing = "Closed with the keyboard."
        )

        typeAtEnd(
            part = AREPAS_FLOUR,
            text = "\nQuit with the keyboard."
        )

        closeWindowWith(shortcut = if (system == HostSystem.WINDOWS) Alt + Key.F4 else Mod + Key.Q)

        expectFile(
            path = "$PLAYGROUND/Recipes/Arepas.md",
            containing = "Quit with the keyboard."
        )
    }
}

private fun SuiteBuilder.shortcutModifier() {
    automated(
        id = "1.1-02",
        title = "Shortcuts use Cmd on macOS and Ctrl elsewhere"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        click(text = WELCOME)
        press(shortcut = Mod + Key.N)
        expectFile(path = "$PERSONAL_NOTES/Untitled.md")
    }
}

private fun SuiteBuilder.dialogButtonOrder() {
    automated(
        id = "1.1-03",
        title = "Dialog buttons in the system's order"
    ) {
        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

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
    automated(
        id = "1.1-04",
        title = "The app's data goes to the system's app data folder"
    ) {
        val expected = when (system) {
            HostSystem.MACOS -> "/Library/Application Support/Notekea"
            HostSystem.WINDOWS -> "\\Notekea"
            HostSystem.LINUX -> "/notekea"
        }

        val resolved = ResolvePlatformConventionsUseCase(
            systemEnvironment = JvmSystemEnvironment()
        )().appDataDirectory

        if (!resolved.endsWith(expected)) {
            throw CaseFailure(
                message = "expected the app data folder to end with $expected, it is $resolved"
            )
        }

        val before = filesIn(folder = folders.data.resolve(PERSONAL_NOTES))

        openVault(
            name = PERSONAL_NOTES,
            shownText = WELCOME
        )

        expectAppDataFile()

        if (filesIn(folder = folders.data.resolve(PERSONAL_NOTES)) != before) {
            throw CaseFailure(message = "expected nothing written inside the vault")
        }
    }
}

private fun SuiteBuilder.xdgDataHome() {
    automated(
        id = "1.1-05",
        title = "On Linux, XDG_DATA_HOME moves the app data folder"
    ) {
        val environment = XdgEnvironment(xdgDataHome = "/tmp/xdg-test")

        val resolved = ResolvePlatformConventionsUseCase(
            systemEnvironment = environment
        )().appDataDirectory

        val isWrongDirectory = system == HostSystem.LINUX && resolved != "/tmp/xdg-test/notekea"

        if (isWrongDirectory) {
            throw CaseFailure(message = "expected /tmp/xdg-test/notekea, it is $resolved")
        }
    }
}

private fun CaseScope.pressDelete() {
    if (system == HostSystem.MACOS) {
        press(shortcut = Mod + Key.Backspace)
    } else {
        press(key = Key.Delete)
    }
}

private fun CaseScope.left(text: String): Float = ui
    .onAllNodes(matcher = hasText(text = text))
    .onFirst()
    .fetchSemanticsNode()
    .boundsInRoot
    .left

private fun CaseScope.expectAppDataFile() {
    ui
        .waitUntil(timeoutMillis = 10_000) {
            Files.list(folders.appData).use { it.findAny().isPresent }
        }
}

private fun filesIn(folder: Path): Set<String> = Files.walk(folder).use { paths ->
    paths
        .map { folder.relativize(it).toString() }
        .toList()
        .toSet()
}

/** The machine as JvmSystemEnvironment reads it, with XDG_DATA_HOME set. */
private class XdgEnvironment(
    private val xdgDataHome: String
) : SystemEnvironment by JvmSystemEnvironment() {
    override val operatingSystemName: String = "Linux"

    override fun environmentVariable(name: String): String? = if (name == "XDG_DATA_HOME") {
        xdgDataHome
    } else {
        JvmSystemEnvironment().environmentVariable(name = name)
    }
}

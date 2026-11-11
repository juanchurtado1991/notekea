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

        title(
            text =
            "Ajustes se ve como los Ajustes del Sistema de macOS, y el editor sigue el " +
                "Tamaño del texto y el Ancho de línea"
        )

        steps(
            text =
            "1. Abre la Playground Vault y una nota larga; pulsa Ctrl+, (⌘, en macOS).\n2. " +
                "Mira cada sección de la barra lateral.\n3. En Editor elige Tamaño del texto 20 " +
                "pt y Ancho de línea Legible, luego Ancho completo; mira la nota detrás.\n4. " +
                "Vuelve a poner Tamaño del texto en 16 pt y Ancho de línea en Ancho."
        )

        expected(
            text =
            "Paso 2: una barra lateral con un icono cuadrado de color por sección; los " +
                "ajustes de cada sección en grupos redondeados, el nombre a la izquierda y el " +
                "control a la derecha, y una nota breve bajo algunos grupos. Paso 3: el texto de " +
                "la nota crece, con los encabezados y el código, y sus líneas pasan a una " +
                "columna estrecha y luego a todo el panel. El paso 4 la deja como estaba."
        )
    }
}

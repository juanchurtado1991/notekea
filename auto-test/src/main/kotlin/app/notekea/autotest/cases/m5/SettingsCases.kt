package app.notekea.autotest.cases.m5

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.clickLast
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
    language()
    macLook()
}

private fun SuiteBuilder.language() {
    automated(
        id = "5.2-04",
        title = "Choosing Español in Settings → General shows Notekea in Spanish at once, and English brings it back"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        pressInWindow(shortcut = Mod + Key.Comma)
        expectText(text = LANGUAGE)
        click(text = "English")
        click(text = "Español")
        expectText(text = "Idioma")
        expectText(text = "Abrir la última bóveda al iniciar")
        click(text = "Español")
        click(text = "English")
        expectText(text = LANGUAGE)
    }
}

private fun SuiteBuilder.macLook() {
    manual(
        id = "5.2-05",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "El rediseño al estilo macOS, en tu sistema")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba) y la nota Welcome.\n" +
                "2. Mira la barra lateral, una carpeta abierta y la nota elegida.\n3. Abre Ajustes " +
                "(Ctrl+, o ⌘, en macOS) y mira los botones, el interruptor y el menú de Idioma.\n4. " +
                "Abre el chat (Ctrl+L, ⌘L en macOS) y haz una pregunta si hay un modelo.\n5. Haz clic " +
                "derecho en una nota de la lista y elige Eliminar nota; luego Cancelar."
        )

        expected(
            text =
            "La barra lateral es de un gris propio, distinto de la nota, con encabezados pequeños en " +
                "negrita (Marcadores, Archivos), los números de cada carpeta en gris y la nota elegida " +
                "rellena del color del tema con letras blancas. El texto de la interfaz se ve " +
                "compacto, como en una app de Mac, y la nota se lee igual que antes. Los botones son " +
                "bajos, el secundario blanco con borde fino, y el menú de Idioma tiene la doble flecha. " +
                "En el chat la pregunta va en negrita, sin burbujas, y las fuentes como líneas. En " +
                "macOS el aviso de eliminar muestra el logo arriba y los botones uno debajo del otro; " +
                "en Windows y Linux, en fila. Todo se lee bien en modo claro y oscuro."
        )
    }
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
        // The tree's section header says Files too: Settings' section is drawn over it, last.
        clickLast(text = "Files")
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

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
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba)."
private const val OPEN_APPEARANCE =
    "Haz clic en Ajustes (el engranaje abajo en la barra estrecha de la izquierda) y en Apariencia"
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
        click(text = "Appearance")
        scrollTo(text = THEME)
        click(text = THEME)
        click(text = "Dark")
        expectChosen(text = THEME)
        press(key = Key.Escape)
        relaunch()
        expectText(text = WELCOME)
        clickIcon(label = "Settings")
        click(text = "Appearance")
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
        click(text = "Appearance")
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
        title(text = "Reducir movimiento")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE, marca Reducir movimiento y pulsa " +
                "Esc.\n3. Pulsa dos veces el atajo de la barra lateral (Ctrl+\\, Cmd+\\ en " +
                "macOS), abre una carpeta en Archivos y cambia el panel lateral entre Inspector " +
                "y Chat.\n4. $OPEN_APPEARANCE, desmarca Reducir movimiento y repite el paso " +
                "3.\n5. Activa el ajuste del propio sistema (Reducir movimiento en macOS, " +
                "Efectos de animación desactivados en Windows, Animaciones desactivadas en " +
                "GNOME), vuelve a iniciar Notekea y repite el paso 3."
        )

        expected(
            text =
            "En el paso 3 todo cambia al instante, nada se desliza ni se desvanece. En el " +
                "paso 4 los paneles se deslizan y las filas se desvanecen como siempre. En el " +
                "paso 5 vuelven a cambiar al instante, con la casilla de Ajustes desmarcada; en " +
                "los escritorios Linux que no comparten este ajuste se deslizan."
        )
    }
}

private fun SuiteBuilder.themesLook() {
    manual(
        id = "2.12-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Todos los temas de color en claro y oscuro")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE.\n3. En Modo haz clic en Claro, luego en " +
                "la tarjeta de cada tema, y mira la ventana detrás del diálogo.\n4. En Modo haz " +
                "clic en Oscuro y vuelve a hacer clic en cada tarjeta.\n5. Pulsa Tab hasta que " +
                "una tarjeta tenga un anillo alrededor y pulsa Enter.\n6. Vuelve a elegir Azul " +
                "Workspace y Sistema."
        )

        expected(
            text =
            "Cada clic cambia toda la ventana al instante (la lista de la izquierda, las " +
                "pestañas, la nota y el logo) a los colores de la tarjeta, y todos los textos se " +
                "siguen leyendo fácilmente. La tarjeta elegida tiene borde y una marca. Enter " +
                "elige la tarjeta con el anillo, como un clic."
        )
    }
}

private fun SuiteBuilder.systemMode() {
    manual(
        id = "2.12-03",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Sistema sigue el modo claro u oscuro de la computadora")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. $OPEN_APPEARANCE y elige un tema que no sea Azul " +
                "Workspace.\n3. Con Modo en Sistema, cambia tu computadora entre modo claro y " +
                "oscuro.\n4. Haz clic en Claro en Modo y vuelve a cambiar tu computadora.\n5. " +
                "Vuelve a elegir Azul Workspace y Sistema, y deja tu computadora como estaba."
        )

        expected(
            text =
            "En Sistema, Notekea se pone claro u oscuro con la computadora, en el tema " +
                "elegido. En Claro se queda claro haga lo que haga la computadora."
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

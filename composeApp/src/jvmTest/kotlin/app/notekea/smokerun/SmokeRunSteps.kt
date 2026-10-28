@file:OptIn(ExperimentalTestApi::class)

package app.notekea.smokerun

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.withKeysDown
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import org.koin.mp.KoinPlatform

private const val WAIT_MILLIS = 10_000L
private const val OPEN_FOLDER = "Open folder…"

/** Waits until something on screen shows [text] (a part of it when [substring]). */
internal fun ComposeUiTest.waitForText(
    text: String,
    substring: Boolean = false
) = waitUntilAtLeastOneExists(matcher = hasText(text = text, substring = substring), timeoutMillis = WAIT_MILLIS)

/** Opens the copy of the vault [name] from the welcome screen, as Open folder… with the picker answering it. */
internal fun ComposeUiTest.openVault(
    app: SmokeRunApp,
    name: String,
    shownNote: String
) {
    app.folderPicker.nextPick = app.vaultPath(name = name)
    onNodeWithText(text = OPEN_FOLDER).performClick()
    waitForText(text = shownNote)
}

/** Presses [key] with the platform's primary modifier (Cmd on macOS, Ctrl elsewhere), and Shift when [withShift]. */
internal fun ComposeUiTest.pressShortcut(
    key: Key,
    withShift: Boolean = false
) {
    val isMac = KoinPlatform.getKoin().get<PlatformConventions>().operatingSystem == OperatingSystem.MACOS
    val modifiers = listOfNotNull(if (isMac) Key.MetaLeft else Key.CtrlLeft, Key.ShiftLeft.takeIf { withShift })
    onRoot().performKeyInput { withKeysDown(keys = modifiers) { pressKey(key = key) } }
}

internal const val SAMPLE_VAULT = "Sample Vault"
private const val FIND_NOTE = "Find a note by name or alias"
private const val START = "start"
private const val END = "end"

/**
 * One automated smoke run named [name]: the real app on copies of [vaults], its memory sampled when it starts and
 * when [block] ends ([SmokeRunReport]).
 */
internal fun smokeRun(
    name: String,
    vaults: List<String> = listOf(SAMPLE_VAULT),
    block: suspend ComposeUiTest.(SmokeRunApp) -> Unit
) = SmokeRunApp(vaults = vaults).use { app ->
    runComposeUiTest {
        setContent { app.Content() }
        SmokeRunReport.sample(run = name, moment = START)
        block(app)
        waitForIdle()
        SmokeRunReport.sample(run = name, moment = END)
    }
}

/** Opens the note [name] with the quick switcher (`Mod+P`), as a person who knows its name would. */
internal fun ComposeUiTest.openNote(
    name: String,
    shownText: String
) {
    pressShortcut(key = Key.P)
    waitForText(text = FIND_NOTE)
    onNode(matcher = hasSetTextAction() and hasText(text = FIND_NOTE)).performTextInput(text = name)
    waitForText(text = name)
    onNode(matcher = hasSetTextAction() and hasText(text = name)).performKeyInput { pressKey(key = Key.Enter) }
    waitForText(text = shownText, substring = true)
}

/** The note's text field, found by a part of its text. */
internal fun ComposeUiTest.editor(
    textPart: String
) = onNode(matcher = hasSetTextAction() and hasText(text = textPart, substring = true))

/** Waits until the file [path] of the vault copy holds [part], as an autosave writes it. */
internal fun ComposeUiTest.waitForFile(
    app: SmokeRunApp,
    path: String,
    part: String
) = waitUntil(timeoutMillis = WAIT_MILLIS) {
    java.io.File(app.vaultPath(name = SAMPLE_VAULT), path).let { it.exists() && it.readText().contains(other = part) }
}

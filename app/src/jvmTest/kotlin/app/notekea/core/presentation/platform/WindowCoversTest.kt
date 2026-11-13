package app.notekea.core.presentation.platform

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.designsystem.components.NotekeaConfirmDialog
import app.notekea.core.presentation.designsystem.components.NotekeaDialog
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPalette
import app.notekea.core.presentation.designsystem.components.menus.NotekeaPopover
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What is drawn over the window says so (the code review of 2026-11-13): the YouTube player, a native view, covered
 * the switch-vault dialog, the quick switcher and the menus, which it now hides under.
 */
@OptIn(ExperimentalTestApi::class)
class WindowCoversTest {
    @Test
    fun `a dialog, an alert, a popover and the palette each cover the window while shown, and no longer after`() =
        runComposeUiTest {
            val covers = WindowCovers()
            var shown by mutableStateOf(value = 0)

            setContent {
                CompositionLocalProvider(LocalWindowCovers provides covers) {
                    NotekeaTheme(darkTheme = false) { Cover(shown = shown) }
                }
            }

            val seen = (0..COVERS + 1).map {
                shown = it
                waitForIdle()
                covers.isCovered
            }

            assertEquals(
                expected = listOf(false, true, true, true, true, false),
                actual = seen
            )
        }

    @androidx.compose.runtime.Composable
    private fun Cover(shown: Int) {
        when (shown) {
            1 -> NotekeaDialog(onDismiss = {}) { Text(text = "Dialog") }

            2 -> NotekeaConfirmDialog(
                title = "Open another vault?",
                message = "This one closes.",
                confirmLabel = "Open",
                cancelLabel = "Cancel",
                buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
                onConfirm = {},
                onDismiss = {}
            )

            3 -> NotekeaPopover(onDismiss = {}) { Text(text = "Popover") }

            COVERS -> NotekeaPalette(
                query = "",
                onQueryChange = {},
                placeholder = "Search",
                onMove = {},
                onConfirm = {},
                onDismiss = {}
            ) {}
        }
    }

    private companion object {
        const val COVERS = 4
    }
}

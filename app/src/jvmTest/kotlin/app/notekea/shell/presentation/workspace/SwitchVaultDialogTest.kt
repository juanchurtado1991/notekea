package app.notekea.shell.presentation.workspace

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The rail's Open another vault asks before it leaves the vault (the developer's request of 2026-11-13). */
@OptIn(ExperimentalTestApi::class)
class SwitchVaultDialogTest {
    @Test
    fun `the button asks first, Cancel stays in the vault, and only the dialog's confirm leaves it`() =
        runComposeUiTest {
            var left = 0

            setContent {
                NotekeaTheme(darkTheme = false) {
                    val ask = switchVaultAsked(
                        vaultName = "Playground Vault",
                        conventions = PreviewPlatformConventions.linux,
                        onSwitchVault = {
                            left++
                        }
                    )

                    Text(
                        text = RAIL_BUTTON,
                        modifier = Modifier.clickable(onClick = ask)
                    )
                }
            }

            onNodeWithText(text = RAIL_BUTTON).performClick()
            onNodeWithText(text = "Open another vault?").assertExists()
            val afterAsking = left
            onNodeWithText(text = "Cancel").performClick()
            onNodeWithText(text = "Open another vault?").assertDoesNotExist()
            val afterCancel = left
            onNodeWithText(text = RAIL_BUTTON).performClick()
            onNodeWithText(text = "Open another vault").performClick()
            waitForIdle()

            assertEquals(
                expected = listOf(0, 0, 1),
                actual = listOf(afterAsking, afterCancel, left)
            )
        }

    private companion object {
        const val RAIL_BUTTON = "Rail button"
    }
}

package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** An option's badge in a narrow panel (the developer's smoke of 2026-11-14: "Recomendada" broke in two lines). */
@OptIn(ExperimentalTestApi::class)
class NotekeaOptionRowTest {
    private fun badgeHeightAt(widthDp: Int): Int {
        var height = 0

        runComposeUiTest {
            setContent {
                NotekeaTheme(darkTheme = true) {
                    Box(modifier = Modifier.width(width = widthDp.dp)) {
                        NotekeaOptionRow(
                            title = "Esta página y sus enlaces",
                            onClick = {},
                            supportingText = "Un nivel: las páginas que enlaza esta página",
                            trailing = "15 páginas",
                            badge = BADGE,
                            isSuggested = true
                        )
                    }
                }
            }

            // Unmerged: the row is one button, and merged the badge is the whole row.
            height = onNodeWithText(
                text = BADGE,
                useUnmergedTree = true
            )
                .fetchSemanticsNode()
                .size
                .height
        }

        return height
    }

    @Test
    fun `the badge stays on one line in a narrow panel`() {
        assertEquals(
            expected = badgeHeightAt(widthDp = WIDE_DP),
            actual = badgeHeightAt(widthDp = NARROW_DP)
        )
    }

    private companion object {
        const val BADGE = "Recomendada"
        const val WIDE_DP = 600
        const val NARROW_DP = 330
    }
}

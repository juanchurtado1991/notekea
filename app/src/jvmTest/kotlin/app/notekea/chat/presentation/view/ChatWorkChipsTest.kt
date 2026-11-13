package app.notekea.chat.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.context.WorkPlace
import app.notekea.chat.presentation.model.WorkPart
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chips above the chat's question field (2026-11-13). */
@OptIn(ExperimentalTestApi::class)
class ChatWorkChipsTest {
    @Test
    fun `the place shows with its section, the selection shortened, and each x removes its part`() =
        runComposeUiTest {
            val removed = mutableListOf<WorkPart>()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    ChatWorkChips(
                        working = WorkContext(
                            place = WorkPlace.NOTE,
                            name = "Recipes/Arepas",
                            section = "Ingredients",
                            selected = "Mix the water and salt, then add the flour"
                        ),
                        isPlaceHidden = false,
                        isSelectedHidden = false,
                        onRemove = { removed += it }
                    )
                }
            }

            onNodeWithText(text = "Arepas · Ingredients").assertExists()
            onNodeWithText(text = "Selected: “Mix the water and salt,…”").assertExists()

            val removes = onAllNodesWithContentDescription(label = "Don’t share with the chat")
            removes[1].performClick()
            removes[0].performClick()
            waitForIdle()

            assertEquals(
                expected = listOf(WorkPart.SELECTED, WorkPart.PLACE),
                actual = removed
            )
        }
}

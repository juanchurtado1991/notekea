package app.notekea.cards.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import app.notekea.cards.domain.CardChoices
import app.notekea.cards.domain.StudyCard
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The Study tab on screen (4.6-03). */
@OptIn(ExperimentalTestApi::class)
class StudyContentTest {
    // Auto-Test 4.6-03, 2026-11-13: the option clicked took the focus and lost it, and Enter went nowhere.
    @Test
    fun `an option clicked, Enter goes to the next card`() = runComposeUiTest {
        val intents = mutableListOf<StudyIntent>()

        var state by mutableStateOf(
            value = StudyState(
                isLoading = false,
                due = listOf(
                    StudyCard(
                        note = RelativePath(value = "Rivers.md"),
                        line = 4,
                        question = "Longest river of Portugal",
                        answer = "Tagus"
                    )
                ),
                choices = CardChoices(
                    options = listOf("Douro", "Tagus", "Minho", "Sado"),
                    correct = 1
                )
            )
        )

        fun handle(intent: StudyIntent) {
            intents += intent

            if (intent is StudyIntent.OnChoose) {
                state = state.copy(
                    chosen = intent.index,
                    isShown = true
                )
            }
        }

        setContent {
            NotekeaTheme(darkTheme = false) {
                StudyContent(
                    state = state,
                    onIntent = { handle(intent = it) }
                )
            }
        }

        onNodeWithText(text = "Tagus").performClick()
        waitForIdle()
        onRoot().performKeyInput { pressKey(key = Key.Enter) }
        waitForIdle()

        assertEquals(
            expected = listOf(StudyIntent.OnChoose(index = 1), StudyIntent.OnNext),
            actual = intents
        )
    }
}

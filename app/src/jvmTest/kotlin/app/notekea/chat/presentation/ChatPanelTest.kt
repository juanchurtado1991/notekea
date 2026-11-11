package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat panel on screen: what it offers without a model, a source that opens its passage, Enter that sends, and
 * Deep answer only where the model offers it.
 */
@OptIn(ExperimentalTestApi::class)
class ChatPanelTest {
    private fun ComposeUiTest.show(
        state: ChatState,
        intents: MutableList<ChatIntent>
    ) = setContent {
        NotekeaTheme(darkTheme = false) {
            Box(
                modifier = Modifier.size(
                    width = WIDTH_DP.dp,
                    height = HEIGHT_DP.dp
                )
            ) {
                ChatPanel(
                    state = state,
                    onIntent = {
                        if (it != ChatIntent.OnShown) intents += it
                    },
                    inputFocus = remember { FocusRequester() }
                )
            }
        }
    }

    @Test
    fun `without a model the chat says so, offers Settings and holds no question field`() =
        runComposeUiTest {
            val intents = mutableListOf<ChatIntent>()

            show(
                state = ChatState(model = ModelStatus.NO_MODEL),
                intents = intents
            )

            onNodeWithText(text = NO_MODEL_TITLE).assertExists()
            onNodeWithText(text = MODEL_ACTION).performClick()
            val fields = onAllNodes(matcher = hasSetTextAction()).fetchSemanticsNodes()

            assertEquals(
                expected = listOf<ChatIntent>(ChatIntent.OnModelSettingsOpen) to 0,
                actual = intents to fields.size
            )
        }

    @Test
    fun `a source of an answer opens its passage`() = runComposeUiTest {
        val intents = mutableListOf<ChatIntent>()

        show(
            state = ChatPreviewData.state.copy(entries = listOf(ChatPreviewData.answered)),
            intents = intents
        )

        onAllNodesWithText(text = STEPS_PLACE)[0].performClick()

        assertEquals(
            expected = listOf<ChatIntent>(
                ChatIntent.OnSourceClick(source = ChatPreviewData.sources[1])
            ),
            actual = intents
        )
    }

    @Test
    fun `Enter sends the question and Shift+Enter does not`() = runComposeUiTest {
        val intents = mutableListOf<ChatIntent>()

        show(
            state = ChatPreviewData.state.copy(
                entries = emptyList(),
                question = QUESTION
            ),
            intents = intents
        )

        onNode(matcher = hasSetTextAction()).run {
            performClick()

            performKeyInput {
                keyDown(key = Key.ShiftLeft)
                pressKey(key = Key.Enter)
                keyUp(key = Key.ShiftLeft)
            }

            performKeyInput { pressKey(key = Key.Enter) }
        }

        assertEquals(
            expected = ChatIntent.OnSend,
            actual = intents.last()
        )

        assertEquals(
            expected = 1,
            actual = intents.count {
                it == ChatIntent.OnSend
            }
        )
    }

    @Test
    fun `Deep answer shows only for a model that offers it, which Small does not`() =
        runComposeUiTest {
            val state = mutableStateOf(value = ChatPreviewData.state.copy(isDeepOffered = true))

            setContent {
                NotekeaTheme(darkTheme = false) {
                    ChatPanel(
                        state = state.value,
                        onIntent = {},
                        inputFocus = remember { FocusRequester() }
                    )
                }
            }

            onNodeWithText(text = DEEP_ANSWER).assertExists()
            state.value = state.value.copy(isDeepOffered = false)
            onNodeWithText(text = DEEP_ANSWER).assertDoesNotExist()
        }

    private companion object {
        const val WIDTH_DP = 340
        const val HEIGHT_DP = 640
        const val NO_MODEL_TITLE = "No AI model yet"
        const val MODEL_ACTION = "Choose a model"
        const val STEPS_PLACE = "Arepas › Steps"
        const val QUESTION = "How long does the dough rest?"
        const val DEEP_ANSWER = "Deep answer"
    }
}

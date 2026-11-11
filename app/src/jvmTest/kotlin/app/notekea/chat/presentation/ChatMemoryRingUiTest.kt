package app.notekea.chat.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's memory ring on screen, over the real [ChatViewModel] (the developer's goal of 2026-11-11, a ring like
 * Claude Code's): it fills as the chat talks, and Compact chat in the ⋯ menu empties it into a summary.
 */
@OptIn(ExperimentalTestApi::class)
class ChatMemoryRingUiTest {
    private val engine = FakeLlmEngine().apply {
        reply = { messages ->
            when {
                messages.first().text.startsWith("You turn a request") -> "{\"steps\":[{\"action\":\"chat\"}]}"
                messages.first().text.startsWith("Summarize this conversation") -> "They talked about arepas."
                else -> "A long reply about the arepas and their dough. ".repeat(n = REPLY_REPEATS)
            }
        }
    }

    private fun ComposeUiTest.shown(): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = FakeNoteContentStore(),
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = FakeActionRunner(),
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))

        setContent {
            val state by viewModel.state.collectAsState()

            NotekeaTheme(darkTheme = false) {
                Box(
                    modifier = Modifier.size(
                        width = WIDTH_DP.dp,
                        height = HEIGHT_DP.dp
                    )
                ) {
                    ChatPanel(
                        state = state,
                        onIntent = viewModel::onIntent,
                        inputFocus = remember { FocusRequester() }
                    )
                }
            }
        }

        return viewModel
    }

    private fun ComposeUiTest.ask(question: String) {
        onNode(matcher = hasSetTextAction()).run {
            performClick()
            performTextInput(text = question)
            performKeyInput { pressKey(key = Key.Enter) }
        }

        waitUntil(timeoutMillis = WAIT_MILLIS) {
            onAllNodes(matcher = ring()).fetchSemanticsNodes().isNotEmpty() &&
                onAllNodes(matcher = hasSetTextAction()).fetchSemanticsNodes().isNotEmpty()
        }

        waitForIdle()
    }

    /** The ring's share, read from what it says to a screen reader ("Chat memory 42 % · …"). */
    private fun ComposeUiTest.ringPercent(): Int {
        val said = onNode(matcher = ring())
            .fetchSemanticsNode()
            .config[SemanticsProperties.ContentDescription]
            .first()

        return said
            .substringAfter(delimiter = "Chat memory ")
            .substringBefore(delimiter = " ")
            .toInt()
    }

    private fun ring() = SemanticsMatcher(description = "the memory ring") { node ->
        node
            .config
            .getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
            .any { it.startsWith(prefix = "Chat memory ") }
    }

    @Test
    fun `the ring fills with each reply and Compact chat empties it`() = runComposeUiTest {
        shown()

        val filled = (1..QUESTIONS).map {
            ask(question = "tell me about the arepas, part $it")
            ringPercent()
        }

        onNodeWithContentDescription(label = "More").performClick()
        onNodeWithText(text = "Compact chat").performClick()

        waitUntil(timeoutMillis = WAIT_MILLIS) {
            ringPercent() < filled.last()
        }

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                filled.zipWithNext().all { (before, after) ->
                    after > before
                },
                engine.generated.any {
                    it
                        .first()
                        .text
                        .startsWith("Summarize this conversation")
                }
            )
        )
    }

    private companion object {
        const val WIDTH_DP = 360
        const val HEIGHT_DP = 720
        const val QUESTIONS = 4
        const val REPLY_REPEATS = 8
        const val WAIT_MILLIS = 10_000L
    }
}

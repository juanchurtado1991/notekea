package app.notekea.notes.presentation.related

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickDescription
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.descriptions
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.RelatedNote
import app.notekea.notes.domain.RelatedSnippet
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The inspector's Related section (`ROADMAP.md` 2.4, design A), rendered headless: what each state says, a card's
 * title, folder and passage, its three actions on hover, and "Already linked" opening (headers show in capitals).
 */
class RelatedSectionScreenTest {
    private var frame = 0L

    @Test
    fun `before indexing, with nothing related, or with an error, the section says why`() {
        val states = listOf(
            RelatedNotesState(),
            RelatedNotesState(isIndexReady = true),
            RelatedNotesState(isIndexReady = true, error = IndexRunError.MODEL)
        )

        val said = states.map { state -> render(state = state).texts.filterNot { it == "RELATED" } }

        assertEquals(
            expected = listOf(
                listOf("Related notes appear after indexing."),
                listOf("No related notes yet. They show as the vault grows."),
                listOf("Related notes need the search model, which could not be loaded.")
            ),
            actual = said
        )
    }

    @Test
    fun `a card shows its note, folder and passage, and its actions on hover send the note`() {
        val sent = mutableListOf<String>()
        val actions = RelatedActions(
            onOpen = { sent += "open ${it.value}" },
            onOpenBeside = { sent += "beside ${it.value}" },
            onInsertLink = { sent += "link ${it.value}" }
        )

        val rendered = render(
            state = READY,
            actions = actions,
            hoverText = "Tomato soup",
            clicks = listOf("Insert link", "Open to the side")
        )

        assertTrue(
            actual = listOf("Tomato soup", "Recipes", PASSAGE).all {
                it in rendered.texts
            },
            message = rendered.texts.toString()
        )
        assertTrue(actual = "Open" in rendered.descriptions, message = rendered.descriptions.toString())
        assertEquals(expected = listOf("link $SOUP", "beside $SOUP"), actual = sent)
    }

    @Test
    fun `already linked opens to list the notes the open note links`() {
        var state = READY
        val onIntent = { intent: RelatedNotesIntent ->
            if (intent == RelatedNotesIntent.OnLinkedToggle) state = state.copy(isLinkedOpen = !state.isLinkedOpen)
        }
        render(state = state, onIntent = onIntent, textClicks = listOf("ALREADY LINKED"))

        val opened = render(state = state).texts

        assertTrue(actual = "Weekly menu" in opened, message = opened.toString())
    }

    private class Rendered(val texts: List<String>, val descriptions: List<String>)

    private fun render(
        state: RelatedNotesState,
        actions: RelatedActions = RelatedActions(onOpen = {}, onOpenBeside = {}, onInsertLink = {}),
        onIntent: (RelatedNotesIntent) -> Unit = {},
        hoverText: String? = null,
        clicks: List<String> = emptyList(),
        textClicks: List<String> = emptyList()
    ): Rendered {
        var rendered = Rendered(texts = emptyList(), descriptions = emptyList())
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        RelatedSection(state = state, isOpen = true, onToggle = {
                        }, onIntent = onIntent, actions = actions)
                    }
                }
            }.use { scene ->
                settle(scene = scene)
                hoverText?.let { text ->
                    val node = scene.nodes().first {
                        it.config.getOrNull(SemanticsProperties.Text)?.any { t ->
                            t.text ==
                                text
                        } ==
                            true
                    }
                    scene.sendPointerEvent(eventType = PointerEventType.Move, position = node.boundsInRoot.center)
                    settle(scene = scene)
                }
                textClicks.forEach { scene.clickText(text = it) { settle(scene = scene) } }
                rendered = Rendered(texts = scene.texts(), descriptions = scene.descriptions())
                clicks.forEach { scene.clickDescription(description = it) { settle(scene = scene) } }
            }
        }
        return rendered
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    private companion object {
        const val SOUP = "Recipes/Tomato soup.md"
        const val PASSAGE = "Summer tomatoes from the beds make it sweeter."
        const val FRAME_NANOS = 16_000_000L
        const val SETTLE_FRAMES = 5
        const val WIDTH = 320
        const val HEIGHT = 600
        val READY = RelatedNotesState(
            related = listOf(
                RelatedNote(
                    path = RelativePath(value = SOUP),
                    score = 1f,
                    snippet = RelatedSnippet(text = PASSAGE, highlight = PASSAGE.indices)
                )
            ),
            linked = listOf(RelativePath(value = "Recipes/Weekly menu.md")),
            isIndexReady = true
        )
    }
}

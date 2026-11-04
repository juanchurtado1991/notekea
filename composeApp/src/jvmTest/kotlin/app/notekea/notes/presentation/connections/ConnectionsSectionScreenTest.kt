package app.notekea.notes.presentation.connections

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.TestTags
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickDescription
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.ConnectionsTab
import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.NoteMention
import app.notekea.notes.domain.NoteMentions
import app.notekea.notes.domain.RelatedNote
import app.notekea.notes.domain.RelatedSnippet
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.mentions.NoteMentionsIntent
import app.notekea.notes.presentation.mentions.NoteMentionsState
import app.notekea.notes.presentation.related.RelatedNotesState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The inspector's Connections (`ROADMAP.md` 2.4, 2.5, design A), rendered headless: the picker and its counts, what
 * each tab says when it lists nothing, its rows, and what their clicks and hover actions send.
 */
class ConnectionsSectionScreenTest {
    private var frame = 0L
    private val sent = mutableListOf<String>()
    private val actions = ConnectionActions(
        onOpen = { sent += "open ${it.value}" },
        onReveal = { path, range -> sent += "reveal ${path.value} $range" },
        onOpenBeside = { sent += "beside ${it.value}" },
        onInsertLink = { sent += "link ${it.value}" }
    )

    @Test
    fun `before indexing, with nothing related, or with an error, Related says why`() {
        val states = listOf(
            RelatedNotesState(),
            RelatedNotesState(isIndexReady = true),
            RelatedNotesState(isIndexReady = true, error = IndexRunError.Model)
        )

        val said = states.map {
            render(related = it).first { text ->
                text.startsWith(prefix = "Related ") ||
                    text.startsWith(prefix = "No ")
            }
        }

        assertEquals(
            expected = listOf(
                "Related notes appear after indexing.",
                "No related notes yet. They show as the vault grows.",
                "Related notes need the search model, which could not be loaded."
            ),
            actual = said
        )
    }

    @Test
    fun `the picker says how many each tab holds and a click chooses one`() {
        val chosen = mutableListOf<ConnectionsTab>()

        val shown = render(mentions = MENTIONS, onTabSelect = { chosen += it }, textClicks = listOf("Unlinked"))

        assertTrue(
            actual = listOf("Related", "1", "Linked", "Unlinked").all {
                it in shown
            },
            message = shown.toString()
        )
        assertEquals(expected = listOf(ConnectionsTab.UNLINKED), actual = chosen)
    }

    @Test
    fun `a related row opens its note on a click, and opens it beside or links it from its hover actions`() {
        render(
            hoverText = "Tomato soup",
            clicks = listOf("Insert link", "Open to the side"),
            textClicks = listOf("Tomato soup")
        )

        assertEquals(expected = listOf("link $SOUP", "beside $SOUP", "open $SOUP"), actual = sent)
    }

    @Test
    fun `a linked row reveals the mention in its note and says how many times it names the note`() {
        val shown = render(tab = ConnectionsTab.LINKED, mentions = MENTIONS, textClicks = listOf("Weekly menu"))

        assertTrue(
            actual = listOf("Weekly menu", "Recipes · 2 mentions", "Monday: Arepas and soup.").all {
                it in shown
            },
            message = shown.toString()
        )
        assertEquals(expected = listOf("reveal $MENU 8..17"), actual = sent)
    }

    @Test
    fun `an unlinked row links on its Link button, and a tab with nothing says so`() {
        val intents = mutableListOf<NoteMentionsIntent>()
        render(tab = ConnectionsTab.UNLINKED, mentions = MENTIONS, onMentionsIntent = {
            intents += it
        }, hoverText = "Lisbon trip", textClicks = listOf("Link"))
        val empty =
            render(tab = ConnectionsTab.UNLINKED, mentions = NoteMentionsState(note = RelativePath(value = AREPAS)))

        assertEquals(
            expected = listOf<NoteMentionsIntent>(NoteMentionsIntent.OnLink(mention = UNLINKED)),
            actual = intents
        )
        assertTrue(actual = "No other note names “Arepas” without a link." in empty, message = empty.toString())
    }

    @Test
    fun `an unlinked row shows Link without the pointer on it`() {
        val shown = render(tab = ConnectionsTab.UNLINKED, mentions = MENTIONS)

        assertTrue(actual = "Link" in shown, message = shown.toString())
    }

    @Test
    fun `a row's actions reach screen readers, with Related's on hover only on screen`() {
        var unlinked = emptyList<String>()
        var related = emptyList<String>()
        render(tab = ConnectionsTab.UNLINKED, mentions = MENTIONS, onScene = { unlinked = it.customActionLabels() })
        val shown = render(onScene = { related = it.customActionLabels() })

        assertEquals(
            expected = Triple(listOf("Link"), listOf("Open to the side", "Insert link"), false),
            actual = Triple(unlinked, related, "Insert link" in shown)
        )
    }

    private fun ImageComposeScene.customActionLabels(): List<String> = nodes().flatMap { node ->
        node.config.getOrNull(SemanticsActions.CustomActions).orEmpty().map { it.label }
    }

    @Test
    fun `a failed search says so and tries again, never that nothing was found`() {
        val intents = mutableListOf<NoteMentionsIntent>()

        val shown = render(
            tab = ConnectionsTab.LINKED,
            mentions = NoteMentionsState(note = RelativePath(value = AREPAS), searchFailed = true),
            onMentionsIntent = { intents += it },
            textClicks = listOf("Try again")
        )

        assertEquals(
            expected = Triple(true, false, listOf<NoteMentionsIntent>(NoteMentionsIntent.OnRetry)),
            actual = Triple(
                "The notes could not be searched." in shown,
                "No other note links here yet." in shown,
                intents
            )
        )
    }

    @Test
    fun `bars stand in for the rows the first time they are found, in Linked and in Related`() {
        val mentions = NoteMentionsState(note = RelativePath(value = AREPAS), isLoading = true)
        val related = RelatedNotesState(isIndexReady = true, isLoading = true)

        val loading = listOf(
            isLoadingShown(tab = ConnectionsTab.LINKED, mentions = mentions),
            isLoadingShown(tab = ConnectionsTab.RELATED, related = related),
            isLoadingShown(tab = ConnectionsTab.LINKED, mentions = MENTIONS.copy(isLoading = true))
        )

        assertEquals(expected = listOf(true, true, false), actual = loading)
    }

    private fun isLoadingShown(
        tab: ConnectionsTab,
        related: RelatedNotesState = RELATED,
        mentions: NoteMentionsState = NoteMentionsState()
    ): Boolean {
        var isShown = false
        render(tab = tab, related = related, mentions = mentions, onScene = { scene ->
            isShown = scene.nodes().any { node ->
                node.config.getOrNull(SemanticsProperties.TestTag) == TestTags.CONNECTIONS_LOADING
            }
        })
        return isShown
    }

    private fun render(
        tab: ConnectionsTab = ConnectionsTab.RELATED,
        related: RelatedNotesState = RELATED,
        mentions: NoteMentionsState = NoteMentionsState(),
        onTabSelect: (ConnectionsTab) -> Unit = {},
        onMentionsIntent: (NoteMentionsIntent) -> Unit = {},
        hoverText: String? = null,
        clicks: List<String> = emptyList(),
        textClicks: List<String> = emptyList(),
        onScene: (ImageComposeScene) -> Unit = {}
    ): List<String> {
        var texts = emptyList<String>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        ConnectionsSection(
                            tab = tab,
                            onTabSelect = onTabSelect,
                            isOpen = true,
                            onToggle = {},
                            related = related,
                            mentions = mentions,
                            onRelatedIntent = {},
                            onMentionsIntent = onMentionsIntent,
                            actions = actions
                        )
                    }
                }
            }.use { scene ->
                settle(scene = scene)
                hoverText?.let { hover(scene = scene, text = it) }
                texts = scene.texts()
                onScene(scene)
                clicks.forEach { scene.clickDescription(description = it) { settle(scene = scene) } }
                textClicks.forEach { scene.clickText(text = it) { settle(scene = scene) } }
            }
        }
        return texts
    }

    private fun hover(
        scene: ImageComposeScene,
        text: String
    ) {
        val node = scene.nodes().first {
            it.config.getOrNull(SemanticsProperties.Text)?.any { t -> t.text == text } ==
                true
        }
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = node.boundsInRoot.center)
        settle(scene = scene)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    private companion object {
        const val AREPAS = "Recipes/Arepas.md"
        const val MENU = "Recipes/Weekly menu.md"
        const val SOUP = "Recipes/Tomato soup.md"
        const val PASSAGE = "Summer tomatoes from the beds make it sweeter."
        const val FRAME_NANOS = 16_000_000L
        const val SETTLE_FRAMES = 5
        const val WIDTH = 320
        const val HEIGHT = 600
        val RELATED = RelatedNotesState(
            related = listOf(
                RelatedNote(
                    path = RelativePath(value = SOUP),
                    score = 1f,
                    snippet = RelatedSnippet(text = PASSAGE, highlight = PASSAGE.indices)
                )
            ),
            isIndexReady = true
        )
        val UNLINKED = NoteMention(
            source = RelativePath(value = "Travel/Lisbon trip.md"),
            range = 9..14,
            count = 1,
            passage = "We found arepas by the river.",
            highlight = 9..14
        )
        val MENTIONS = NoteMentionsState(
            note = RelativePath(value = AREPAS),
            mentions = NoteMentions(
                linked = listOf(
                    NoteMention(
                        source = RelativePath(value = MENU),
                        range = 8..17,
                        count = 2,
                        passage = "Monday: Arepas and soup.",
                        highlight = 8..13
                    )
                ),
                unlinked = listOf(UNLINKED)
            )
        )
    }
}

package app.notekea.selftest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import app.notekea.selftest.noteText
import app.notekea.selftest.pressInWindow
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val EMBEDS = "Embeds practice"
private const val EMBEDS_TEXT = "A chain of notes, each embedding the next"
private const val CIRCULAR = "Circular embed: this note is already shown above, so it stops here."
private const val OPEN_NOTE = "Open note"
private const val SCROLL_RESTORE_MILLIS = 500L

/** The cases of 1.15, notes embedded in notes (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.embedCases() {
    embedsInOrder()
    nestedEmbeds()
    blockEmbeds()
    embedNotices()
    embedsInEdit()
}

private fun SuiteBuilder.embedsInOrder() {
    automated(id = "1.15-01", title = "Every embed shows as a frame, or a notice that says why it cannot") {
        openEmbeds(isPreview = true)
        expectOrderInNote(
            texts = listOf(
                "Pantry", "Loop A", "Shopping basics › Dairy", "Shopping basics › Dessert", "Blocks › ^dough",
                "Blocks › ^corn", "Blocks › ^prices", "Blocks › ^nothing", "Weekend menu"
            )
        )
        expectCount(text = CIRCULAR, atLeast = 2)
        listOf("Fresh cheese", "Butter", "Shopping basics has no heading “Dessert”.", "Blocks has no block “^nothing”.")
            .forEach { expectText(text = it) }
        expectText(text = "This note does not exist yet.")
        expectText(text = "Create note")
        expectText(text = "see Pantry", substring = true)
    }
}

private fun SuiteBuilder.nestedEmbeds() {
    automated(id = "1.15-02", title = "Embeds inside embeds stop three notes deep and where they loop") {
        openEmbeds(isPreview = true)
        listOf("Corn flour, rice and beans.", "Shelf", "Bin", "Deep corner", "Loop B").forEach { expectText(text = it) }
        expectText(text = "Too deep to show: embeds go 3 notes deep at most.")
        expectOrderInNote(texts = listOf("Pantry", "Shelf", "Bin", "Deep corner"))
        expectOrderInNote(texts = listOf("Loop A", "Loop B", CIRCULAR))
        ui.waitUntil(timeoutMillis = 10_000) {
            ui.onAllNodes(
                matcher = hasContentDescription(value = "Arepas plate.png")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        expectNoText(text = "place:", substring = true)
    }
}

private fun SuiteBuilder.blockEmbeds() {
    automated(id = "1.15-03", title = "A block embed shows only that block, without its marker") {
        openEmbeds(isPreview = true)
        expectText(text = "Soak the corn flour for ten minutes, then shape the dough into discs.")
        expectText(text = "Buy fresh corn")
        listOf("Item", "Price", "Flour", "Cheese").forEach { expectText(text = it, substring = true) }
        expectNoText(text = "^dough", substring = false)
        expectNoText(text = "Buy fresh corn ^corn", substring = true)
    }
}

private fun SuiteBuilder.embedNotices() {
    automated(id = "1.15-04", title = "A notice opens the note, or creates the one that does not exist") {
        openEmbeds(isPreview = true)
        clickTextOnRowOf(rowText = "Shopping basics › Dessert", text = OPEN_NOTE)
        tab(name = "Shopping basics")
        backToEmbeds()
        clickTextOnRowOf(rowText = "Deep corner", text = OPEN_NOTE)
        tab(name = "Deep corner")
        backToEmbeds()
        clickTextOnRowOf(rowText = "Weekend menu", text = "Create note")
        expectFile(path = "$PLAYGROUND/Weekend menu.md")
        tab(name = "Weekend menu")
        backToEmbeds()
        scrollNoteTo(text = "Weekend menu")
        expectNoText(text = "This note does not exist yet.")
    }
}

private fun SuiteBuilder.embedsInEdit() {
    automated(id = "1.15-05", title = "In Edit every embed keeps its line, its frame right under it") {
        openEmbeds(isPreview = false)
        expectText(text = "![[Pantry]]", substring = true)
        expectText(text = "![[Blocks#^corn]]", substring = true)
        expectText(text = "Shopping basics › Dairy")
        val field = noteText(part = EMBEDS_TEXT)
        val text = field.fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        field.performClick()
        field.performTextInputSelection(selection = TextRange(index = text.indexOf("![[Pantry]]")))
        field.performTextInput(text = "Shopping\n\n")
        expectFile(path = "$PLAYGROUND/Editor/Embeds/$EMBEDS.md", containing = "Shopping\n\n![[Pantry]]")
        expectText(text = "Corn flour, rice and beans.")
    }
}

/** Goes back to the tab of Embeds practice, once it has scrolled back to where it was left. */
private fun CaseScope.backToEmbeds() {
    tab(name = EMBEDS).performClick()
    ui.waitForIdle()
    Thread.sleep(SCROLL_RESTORE_MILLIS)
    ui.waitForIdle()
}

private fun CaseScope.openEmbeds(isPreview: Boolean) {
    openPlaygroundNote(name = EMBEDS, shownText = EMBEDS_TEXT)
    if (isPreview) {
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = "![[Pantry]]", substring = true)
    }
}

private fun CaseScope.clickTextOnRowOf(
    rowText: String,
    text: String
) {
    scrollNoteTo(text = rowText)
    onRowOf(rowText = rowText, matcher = hasText(text = text) and inNote, anchor = hasText(text = rowText) and inNote)
        .performClick()
}

private fun CaseScope.expectCount(
    text: String,
    atLeast: Int
) {
    val holds = runCatching {
        ui.waitUntil(timeoutMillis = 10_000) {
            ui.onAllNodes(matcher = hasText(text = text)).fetchSemanticsNodes().size >= atLeast
        }
    }.isSuccess
    if (!holds) throw CaseFailure(message = "expected \"$text\" at least $atLeast times")
}

/** Fails unless [texts] first show in that order down the note, each found from the note's top. */
private fun CaseScope.expectOrderInNote(texts: List<String>) {
    val places = texts.map { placeInNote(text = it) }
    if (places != places.sorted()) throw CaseFailure(message = "expected ${texts.joinToString()} in that order")
}

/** How far down the note [text] first shows: where it is on screen plus how far the note is scrolled. */
private fun CaseScope.placeInNote(text: String): Float {
    scrollNoteTo(text = text)
    val scrolled = noteColumn().fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
    return firstShownTop(text = text)!! + scrolled
}

private fun CaseScope.noteColumn() = ui.onAllNodes(matcher = hasScrollAction() and hasVerticalScroll and inNote)[0]

/** Scrolls the open note, a screen at a time from its top, until [text] is drawn on screen in it. */
private fun CaseScope.scrollNoteTo(text: String) {
    val column = noteColumn()
    column.performSemanticsAction(key = SemanticsActions.ScrollBy) { it(0f, -SCROLL_TOP) }
    repeat(times = MAX_SCREENS) {
        val top = firstShownTop(text = text)
        if (top != null) {
            // Near the column's top, so what follows it (a frame's buttons) is on screen too.
            val columnTop = column.fetchSemanticsNode().boundsInRoot.top
            column.performSemanticsAction(key = SemanticsActions.ScrollBy) { it(0f, top - columnTop - TOP_MARGIN) }
            ui.waitForIdle()
            return
        }
        column.performSemanticsAction(key = SemanticsActions.ScrollBy) { it(0f, SCROLL_STEP) }
        ui.waitForIdle()
    }
    throw CaseFailure(message = "expected \"$text\" in the note")
}

private fun CaseScope.firstShownTop(text: String): Float? {
    val nodes = ui.onAllNodes(matcher = hasText(text = text) and inNote)
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).map { nodes[it] }.filter { it.isShown() }
        .minOfOrNull { it.fetchSemanticsNode().boundsInRoot.top }
}

private val hasVerticalScroll = SemanticsMatcher.keyIsDefined(key = SemanticsProperties.VerticalScrollAxisRange)
private const val SCROLL_TOP = 100_000f
private const val SCROLL_STEP = 300f
private const val TOP_MARGIN = 40f
private const val MAX_SCREENS = 40

// What the note's column holds, between the sidebar and the side panel, not the tree's rows of the same names.
private val inNote = SemanticsMatcher(description = "in the note") { node ->
    node.boundsInRoot.center.x in SIDEBAR_RIGHT..SIDE_PANEL_LEFT
}
private const val SIDEBAR_RIGHT = 390f
private const val SIDE_PANEL_LEFT = 930f

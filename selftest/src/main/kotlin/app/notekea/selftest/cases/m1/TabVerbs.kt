package app.notekea.selftest.cases.m1

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.MouseButton
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.rightClick
import app.notekea.selftest.clickInTree
import app.notekea.selftest.pressInWindow
import com.testbuddy.isShown
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.HostSystem
import com.testbuddy.ShortcutModifier.Mod

private const val TAB_STRIP_BOTTOM = 40f
private const val DRAG_STEPS = 12

/** The tab showing [name]: of the elements showing it, the one in the tab strip at the window's top. */
internal fun CaseScope.tab(name: String): SemanticsNodeInteraction {
    var found: SemanticsNodeInteraction? = null
    ui.waitUntil(timeoutMillis = 10_000) {
        found = tabsShowing(name = name).firstOrNull()
        found != null
    }
    return found ?: throw CaseFailure(message = "expected a tab \"$name\", there is none")
}

/** The tabs showing [name], from left to right. */
internal fun CaseScope.tabsShowing(name: String): List<SemanticsNodeInteraction> {
    val nodes = ui.onAllNodes(matcher = hasText(text = name))
    val count = nodes.fetchSemanticsNodes().size
    return (0 until count).map { nodes[it] }
        .filter { it.isShown() && it.fetchSemanticsNode().boundsInRoot.bottom < TAB_STRIP_BOTTOM }
        .sortedBy { it.fetchSemanticsNode().boundsInRoot.left }
}

/** Fails unless the tabs [names] are in that order from left to right. */
internal fun CaseScope.expectTabOrder(names: List<String>) {
    val lefts = names.map { tab(name = it).fetchSemanticsNode().boundsInRoot.left }
    if (lefts != lefts.sorted()) throw CaseFailure(message = "expected the tabs ${names.joinToString()} in that order")
}

internal fun CaseScope.expectNoTab(name: String) {
    ui.waitUntil(timeoutMillis = 10_000) { tabsShowing(name = name).isEmpty() }
}

internal fun CaseScope.middleClick(node: SemanticsNodeInteraction) {
    node.performMouseInput {
        moveTo(position = center)
        press(button = MouseButton.Tertiary)
        release(button = MouseButton.Tertiary)
    }
}

internal fun CaseScope.rightClickTab(name: String) {
    tab(name = name).performMouseInput { this.rightClick() }
}

/** Clicks [node] holding the system's main modifier (Cmd on macOS, Ctrl elsewhere), as Mod+click. */
internal fun CaseScope.modClick(node: SemanticsNodeInteraction) {
    val mod = if (system == HostSystem.MACOS) Key.MetaLeft else Key.CtrlLeft
    node.performKeyInput { keyDown(key = mod) }
    node.performMouseInput { this.click() }
    node.performKeyInput { keyUp(key = mod) }
}

/** Hovers the tab [name], so its ✕ shows, and clicks the ✕. */
internal fun CaseScope.closeTabWithX(name: String) {
    tab(name = name).performMouseInput { moveTo(position = center) }
    val closes = ui.onAllNodes(matcher = androidx.compose.ui.test.hasContentDescription(value = "Close tab"))
    ui.waitUntil(timeoutMillis = 10_000) { closes.fetchSemanticsNodes().isNotEmpty() }
    val tabBounds = tab(name = name).fetchSemanticsNode().boundsInRoot
    val count = closes.fetchSemanticsNodes().size
    val close = (0 until count).map { closes[it] }.minBy {
        val bounds = it.fetchSemanticsNode().boundsInRoot
        kotlin.math.abs(bounds.left - tabBounds.right)
    }
    close.performMouseInput { this.click() }
}

/** Drags the tab [name] with the mouse by [by] pixels, slowly, as a person would, and lets it go. */
internal fun CaseScope.dragTab(
    name: String,
    by: Offset
) {
    tab(name = name).performMouseInput {
        moveTo(position = center)
        press()
        repeat(times = DRAG_STEPS) { moveBy(delta = by / DRAG_STEPS.toFloat()) }
        release()
    }
}

/** The left edge of the tab [name], to drag another tab next to it. */
internal fun CaseScope.tabLeft(name: String): Float = tab(name = name).fetchSemanticsNode().boundsInRoot.left

internal fun CaseScope.tabCenterX(name: String): Float = tab(name = name).fetchSemanticsNode().boundsInRoot.center.x

/**
 * Opens the folder [name] of the tree without keeping its tab: a click opens a folder in a tab too (1.11), which the
 * cases about the order of tabs do not want.
 */
internal fun CaseScope.expandInTree(name: String) {
    clickInTree(text = name)
    tab(name = name)
    pressInWindow(shortcut = Mod + Key.W)
    expectNoTab(name = name)
}

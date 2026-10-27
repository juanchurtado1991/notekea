package app.notekea.core.presentation.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A click with the middle mouse button, which browsers and editors use for tabs (`ROADMAP.md` 1.11): it closes a tab
 * and opens a link or a file in a new tab behind the current one. The press is consumed so nothing else sees it; the
 * other buttons are left to the element.
 */
fun Modifier.onMiddleClick(
    onClick: () -> Unit
): Modifier = pointerInput(onClick) {
    awaitPointerEventScope {
        while (true) {
            val press = awaitPointerEvent(pass = PointerEventPass.Initial)
            if (press.type != PointerEventType.Press || !press.buttons.isTertiaryPressed) continue
            press.changes.forEach { it.consume() }
            val release = awaitPointerEvent(pass = PointerEventPass.Initial)
            if (release.type == PointerEventType.Release) onClick()
        }
    }
}

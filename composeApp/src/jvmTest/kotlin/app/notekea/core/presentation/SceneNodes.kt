package app.notekea.core.presentation

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull

/** Every node of the scene, unmerged, as a screen reader would list them. */
@OptIn(InternalComposeUiApi::class)
fun ImageComposeScene.nodes(): List<SemanticsNode> =
    semanticsOwners.flatMap { it.getAllSemanticsNodes(mergingEnabled = false) }

/** The texts the scene shows. */
fun ImageComposeScene.texts(): List<String> =
    nodes().mapNotNull { node -> node.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } }

/** The content descriptions the scene names (icons, dots). */
fun ImageComposeScene.descriptions(): List<String> =
    nodes().flatMap { node -> node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty() }

/**
 * Moves the pointer onto the middle of the node showing [text] (the [occurrence]-th one, from 0, when several show it),
 * presses and releases it, then renders [frames].
 */
fun ImageComposeScene.clickText(
    text: String,
    occurrence: Int = 0,
    frames: () -> Unit
) = clickNode(occurrence = occurrence, frames = frames) { node ->
    node.config.getOrNull(SemanticsProperties.Text).orEmpty().any { it.text == text }
}

/** As [clickText], on the node named [description] (an icon button). */
fun ImageComposeScene.clickDescription(
    description: String,
    frames: () -> Unit
) = clickNode(occurrence = 0, frames = frames) { node ->
    node.config.getOrNull(SemanticsProperties.ContentDescription)?.contains(element = description) == true
}

private fun ImageComposeScene.clickNode(
    occurrence: Int,
    frames: () -> Unit,
    isTarget: (SemanticsNode) -> Boolean
) {
    val center: Offset = nodes().filter(predicate = isTarget)[occurrence].boundsInRoot.center
    sendPointerEvent(eventType = PointerEventType.Move, position = center)
    sendPointerEvent(eventType = PointerEventType.Press, position = center)
    sendPointerEvent(eventType = PointerEventType.Release, position = center)
    frames()
}

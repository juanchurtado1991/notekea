package app.notekea.core.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp

/** The settings of a [focusBorder]; Compose reuses its node and only updates it when they change. */
internal data class FocusBorderElement(
    val color: Color,
    val shape: Shape,
    val width: Dp
) : ModifierNodeElement<FocusBorderElement.Node>() {
    override fun create(): Node = Node(color = color, shape = shape, width = width)

    override fun update(
        node: Node
    ) {
        node.color = color
        node.shape = shape
        node.width = width
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = INSPECTOR_NAME
    }

    /** Remembers whether the focus target after it is focused and draws the border while it is. */
    class Node(
        var color: Color,
        var shape: Shape,
        var width: Dp
    ) : Modifier.Node(),
        FocusEventModifierNode,
        DrawModifierNode {
        private var isFocused = false

        override fun onFocusEvent(
            focusState: FocusState
        ) {
            if (focusState.isFocused != isFocused) {
                isFocused = focusState.isFocused
                invalidateDraw()
            }
        }

        override fun ContentDrawScope.draw() {
            drawContent()
            if (!isFocused) return
            val stroke = width.toPx()
            val inner = Size(width = size.width - stroke, height = size.height - stroke)
            val outline = shape.createOutline(size = inner, layoutDirection = layoutDirection, density = this)
            translate(left = stroke / 2, top = stroke / 2) {
                drawOutline(outline = outline, color = color, style = Stroke(width = stroke))
            }
        }
    }

    companion object {
        private const val INSPECTOR_NAME = "focusBorder"
    }
}
